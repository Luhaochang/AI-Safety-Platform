"""
RAG Server - 基于 simple-rag 的完整 RAG 后端
集成: ChromaDB向量库 + BM25/Dense/Hybrid检索 + CrossEncoder重排 + 通义千问Qwen LLM
功能: 知识库管理、文档上传分块向量化、RAG对话、对话历史持久化

架构说明：
  - models.py:   数据模型层（KnowledgeBase, Document, Chunk, Conversation, Message, DecisionLog 等）
  - services.py: 服务逻辑层（TextExtractor, TextCleaner, TextChunker, VectorStore,
                              Retriever, Reranker, LLMGenerator, RAGPipeline,
                              ConversationManager, DecisionLogManager, LogExportService）
  - main.py:     API接口层（FastAPI路由，调用services完成业务处理）
"""

import os
import sys
import uuid
import json
import time
import shutil
import traceback
from datetime import datetime
from typing import Optional, List

import numpy as np
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

import chromadb
try:
    from chromadb.utils.embedding_functions import SentenceTransformerEmbeddingFunction
except ImportError:
    from chromadb.utils.embedding_functions.sentence_transformer import SentenceTransformerEmbeddingFunction
try:
    from rank_bm25 import BM25Okapi
    HAS_BM25 = True
except ImportError:
    HAS_BM25 = False
    print("[警告] rank-bm25 未安装，BM25检索不可用。请执行: pip install rank-bm25")
from sentence_transformers import SentenceTransformer, CrossEncoder
import requests as http_requests
import nltk

# 导入数据模型与服务类
from models import (
    KnowledgeBase, Document, Chunk, CleanResult,
    Conversation, Message, Reference, RAGResult,
    DecisionLog, ChatDecisionLog, TwinDecisionLog, RetrievedPassage,
)
from services import (
    TextExtractor, TextCleaner, TextChunker, VectorStore, MetadataManager,
    Retriever, Reranker, LLMGenerator, RAGPipeline,
    ConversationManager, DecisionLogManager, LogExportService,
)

# ========== 路径配置 ==========
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
SIMPLE_RAG_DIR = os.path.join(os.path.dirname(BASE_DIR), "simple-rag")
DATA_DIR = os.path.join(BASE_DIR, "data")
UPLOAD_DIR = os.path.join(DATA_DIR, "uploads")
CHROMA_DIR = os.path.join(DATA_DIR, "chromadb")
META_FILE = os.path.join(DATA_DIR, "metadata.json")
CHAT_HISTORY_FILE = os.path.join(DATA_DIR, "chat_history.json")

os.makedirs(UPLOAD_DIR, exist_ok=True)
os.makedirs(CHROMA_DIR, exist_ok=True)

# simple-rag 本地模型路径
SENTENCE_MODEL_DIR = os.path.join(
    SIMPLE_RAG_DIR, "models", "models--sentence-transformers--all-MiniLM-L6-v2",
    "snapshots", "c9745ed1d9f207416be6d2e6f8de32d1f16199bf"
)
CROSS_ENCODER_MODEL_DIR = os.path.join(
    SIMPLE_RAG_DIR, "models", "models--cross-encoder--ms-marco-MiniLM-L-6-v2",
    "snapshots", "c5ee24cb16019beea0893ab7796b1df96625c6b8"
)

# NLTK 数据
NLTK_DIR = os.path.join(SIMPLE_RAG_DIR, "rag", "nltk_data")
if os.path.exists(NLTK_DIR):
    nltk.data.path.append(NLTK_DIR)

# 通义千问 Qwen API 配置（DashScope OpenAI兼容接口）
QWEN_API_KEY = os.environ.get("QWEN_API_KEY", "sk-17e2670ac3c24c36a987e26926c8902f")
QWEN_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions"
QWEN_MODEL = os.environ.get("QWEN_MODEL", "qwen-plus")
QWEN_MODELS = [
    {"value": "qwen-turbo", "label": "Qwen-Turbo", "desc": "快速响应，适合简单对话"},
    {"value": "qwen-plus", "label": "Qwen-Plus", "desc": "平衡性能和成本（推荐）"},
    {"value": "qwen-max", "label": "Qwen-Max", "desc": "最强性能，复杂推理"},
    {"value": "qwen-long", "label": "Qwen-Long", "desc": "超长上下文"}
]

# ========== 文本提取工具 ==========

def extract_text_from_txt(file_path: str) -> str:
    import chardet
    with open(file_path, 'rb') as f:
        raw = f.read()
    detected = chardet.detect(raw)
    encoding = detected.get('encoding', 'utf-8') or 'utf-8'
    return raw.decode(encoding, errors='ignore')


def extract_text_from_pdf(file_path: str) -> str:
    text_parts = []
    try:
        import pdfplumber
        with pdfplumber.open(file_path) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    text_parts.append(page_text)
    except Exception:
        from PyPDF2 import PdfReader
        reader = PdfReader(file_path)
        for page in reader.pages:
            page_text = page.extract_text()
            if page_text:
                text_parts.append(page_text)
    return '\n'.join(text_parts)


def extract_text_from_docx(file_path: str) -> str:
    from docx import Document
    doc = Document(file_path)
    return '\n'.join([para.text for para in doc.paragraphs if para.text.strip()])


def extract_text(file_path: str, filename: str) -> str:
    lower = filename.lower()
    if lower.endswith('.pdf'):
        return extract_text_from_pdf(file_path)
    elif lower.endswith('.docx') or lower.endswith('.doc'):
        return extract_text_from_docx(file_path)
    else:
        return extract_text_from_txt(file_path)


# ========== 分块工具 ==========

def chunk_text(text: str, chunk_size: int = 512, overlap: int = 64) -> list:
    """滑动窗口分块（按字符）"""
    chunks = []
    start = 0
    idx = 0
    while start < len(text):
        end = min(start + chunk_size, len(text))
        chunk = text[start:end].strip()
        if len(chunk) > 20:
            chunks.append({
                'index': idx, 'text': chunk,
                'start': start, 'end': end, 'char_count': len(chunk)
            })
            idx += 1
        step = max(chunk_size - overlap, 1)
        start += step
    return chunks


# ========== RAG 核心组件（来自 simple-rag） ==========

class RAGEngine:
    """集成 simple-rag 的 BM25 + Dense + Hybrid 检索 + CrossEncoder 重排 + Qwen LLM"""

    def __init__(self):
        self.chroma_client = chromadb.PersistentClient(path=CHROMA_DIR)

        # 加载 SentenceTransformer 模型（用于 Dense 检索和 Embedding）
        model_path = SENTENCE_MODEL_DIR if os.path.exists(SENTENCE_MODEL_DIR) else 'all-MiniLM-L6-v2'
        print(f"[RAG] 加载 SentenceTransformer 模型: {model_path}")
        self.sentence_model = SentenceTransformer(model_path)
        self.embedding_function = SentenceTransformerEmbeddingFunction(
            model_name=model_path
        )

        # 加载 CrossEncoder 重排模型
        rerank_path = CROSS_ENCODER_MODEL_DIR if os.path.exists(CROSS_ENCODER_MODEL_DIR) else 'cross-encoder/ms-marco-MiniLM-L-6-v2'
        print(f"[RAG] 加载 CrossEncoder 模型: {rerank_path}")
        self.cross_encoder = CrossEncoder(rerank_path)

        # Qwen LLM 连接状态
        self.qwen_available = False
        self._check_qwen()

        print("[RAG] 引擎初始化完成")

    def _check_qwen(self):
        if not QWEN_API_KEY:
            self.qwen_available = False
            print("[RAG] Qwen API Key 未配置，RAG对话将仅返回检索结果")
            return
        try:
            resp = http_requests.post(
                QWEN_API_URL,
                headers={"Authorization": f"Bearer {QWEN_API_KEY}", "Content-Type": "application/json"},
                json={"model": QWEN_MODEL, "messages": [{"role": "user", "content": "你好"}], "max_tokens": 50},
                timeout=15
            )
            if resp.status_code == 200:
                self.qwen_available = True
                print(f"[RAG] Qwen API 已连接，当前模型: {QWEN_MODEL}")
            else:
                # 尝试解析错误详情
                try:
                    err_body = resp.json()
                    err_msg = err_body.get("error", {}).get("message", resp.text[:200])
                except Exception:
                    err_msg = resp.text[:200]
                print(f"[RAG] Qwen API 连接失败: HTTP {resp.status_code} - {err_msg}")
                # 如果是认证错误(401)则确认不可用，其他错误可能只是请求格式问题，标记为可用
                if resp.status_code in (401, 403):
                    self.qwen_available = False
                else:
                    # 400可能只是ping请求格式不被接受，实际API仍可用
                    self.qwen_available = True
                    print(f"[RAG] Qwen API 标记为可用（非认证错误，实际调用时可能正常）")
        except Exception as e:
            self.qwen_available = False
            print(f"[RAG] Qwen API 连接失败: {e}")

    def get_collection(self, kb_id: str):
        return self.chroma_client.get_or_create_collection(
            name=kb_id, embedding_function=self.embedding_function
        )

    # --- BM25 检索 ---
    def bm25_retrieve(self, query: str, documents: list, top_k: int = 10) -> list:
        if not documents:
            return []
        if not HAS_BM25:
            return []
        tokenized_docs = [doc.split() for doc in documents]
        bm25 = BM25Okapi(tokenized_docs)
        scores = bm25.get_scores(query.split())
        top_n = np.argsort(scores)[::-1][:top_k]
        return [{"text": documents[i], "score": float(scores[i]), "index": int(i)} for i in top_n]

    # --- Dense 检索（ChromaDB） ---
    def dense_retrieve(self, query: str, kb_ids: list, top_k: int = 10) -> list:
        all_results = []
        for kid in kb_ids:
            try:
                collection = self.chroma_client.get_collection(
                    name=kid, embedding_function=self.embedding_function
                )
                if collection.count() == 0:
                    continue
                n = min(top_k, collection.count())
                results = collection.query(
                    query_texts=[query], n_results=n,
                    include=["documents", "metadatas", "distances"]
                )
                if results["ids"] and results["ids"][0]:
                    for i in range(len(results["ids"][0])):
                        all_results.append({
                            "id": results["ids"][0][i],
                            "text": results["documents"][0][i],
                            "metadata": results["metadatas"][0][i] if results["metadatas"] else {},
                            "distance": results["distances"][0][i],
                            "kb_id": kid,
                        })
            except Exception as e:
                print(f"[RAG] Dense检索 {kid} 失败: {e}")
        all_results.sort(key=lambda x: x["distance"])
        return all_results[:top_k]

    # --- Hybrid 检索（BM25 + Dense 融合，RRF） ---
    def hybrid_retrieve(self, query: str, kb_ids: list, top_k: int = 5) -> list:
        candidate_k = top_k * 3

        # Dense 检索
        dense_results = self.dense_retrieve(query, kb_ids, candidate_k)

        # 收集所有文档文本用于 BM25
        all_docs_text = [r["text"] for r in dense_results]
        if not all_docs_text:
            return []

        # BM25 检索
        bm25_results = self.bm25_retrieve(query, all_docs_text, candidate_k)

        # RRF 融合
        doc_scores = {}
        k = 60  # RRF 常数

        for rank, dr in enumerate(dense_results, start=1):
            txt = dr["text"]
            doc_scores.setdefault(txt, {"score": 0, "data": dr})
            doc_scores[txt]["score"] += 0.5 / (k + rank)

        for rank, br in enumerate(bm25_results, start=1):
            txt = br["text"]
            if txt in doc_scores:
                doc_scores[txt]["score"] += 0.5 / (k + rank)
            else:
                # 查找对应的 dense_result 数据
                matching = next((d for d in dense_results if d["text"] == txt), None)
                if matching:
                    doc_scores[txt] = {"score": 0.5 / (k + rank), "data": matching}

        sorted_docs = sorted(doc_scores.values(), key=lambda x: x["score"], reverse=True)
        return [item["data"] for item in sorted_docs[:top_k]]

    # --- CrossEncoder 重排 ---
    def rerank(self, query: str, documents: list, top_k: int = 5) -> list:
        if not documents:
            return []
        texts = [d["text"] if isinstance(d, dict) else d for d in documents]
        pairs = [[query, t] for t in texts]
        scores = self.cross_encoder.predict(pairs)

        scored = list(zip(documents, scores))
        scored.sort(key=lambda x: x[1], reverse=True)

        results = []
        for doc, score in scored[:top_k]:
            if isinstance(doc, dict):
                doc["rerank_score"] = float(score)
                results.append(doc)
            else:
                results.append({"text": doc, "rerank_score": float(score)})
        return results

    # --- Qwen LLM 生成 ---
    def llm_generate(self, prompt: str, model: str = None) -> str:
        if not self.qwen_available:
            self._check_qwen()
        if not self.qwen_available:
            return None

        use_model = model or QWEN_MODEL
        try:
            resp = http_requests.post(
                QWEN_API_URL,
                headers={"Authorization": f"Bearer {QWEN_API_KEY}", "Content-Type": "application/json"},
                json={
                    "model": use_model,
                    "messages": [
                        {"role": "system", "content": "你是一个专业的电力调度决策助手。请根据提供的参考资料回答问题，用Markdown格式，专业、准确、有条理。"},
                        {"role": "user", "content": prompt}
                    ],
                    "temperature": 0.7,
                    "max_tokens": 2000
                },
                timeout=60
            )
            resp.raise_for_status()
            data = resp.json()
            return data.get("choices", [{}])[0].get("message", {}).get("content", "")
        except Exception as e:
            print(f"[RAG] Qwen 生成失败: {e}")
            return None

    # --- 完整 RAG Pipeline ---
    def rag_query(self, query: str, kb_ids: list = None, top_k: int = 5, rerank_top_k: int = 3, model: str = None) -> dict:
        """完整 RAG 流程：检索 → 重排 → 生成"""
        meta = load_metadata()
        if not kb_ids:
            kb_ids = list(meta.get("knowledge_bases", {}).keys())

        if not kb_ids:
            return {
                "answer": "当前没有知识库，请先创建知识库并上传文档。",
                "contexts": [], "initial_docs": []
            }

        # 1. Hybrid 检索
        retrieved = self.hybrid_retrieve(query, kb_ids, top_k)
        initial_docs = [{"text": r["text"], "metadata": r.get("metadata", {}), "distance": r.get("distance", 0)} for r in retrieved]

        if not retrieved:
            return {
                "answer": "未检索到相关文档，请确保知识库中已上传相关文档。",
                "contexts": [], "initial_docs": []
            }

        # 2. CrossEncoder 重排
        reranked = self.rerank(query, retrieved, rerank_top_k)
        contexts = [{"text": r["text"], "metadata": r.get("metadata", {}), "rerank_score": r.get("rerank_score", 0)} for r in reranked]

        # 3. LLM 生成
        context_text = "\n\n".join([r["text"] for r in reranked])
        prompt = f"""你是一个专业的电力调度决策助手。请根据以下检索到的参考资料回答用户的问题。
请用Markdown格式回答，包含清晰的标题、分步骤说明和注意事项。回答要专业、准确、有条理。
如果参考资料不足以回答问题，请如实说明。

## 参考资料
{context_text}

## 用户问题
{query}

## 回答
"""
        answer = self.llm_generate(prompt, model)

        if not answer:
            # Qwen API 不可用时，返回检索结果摘要
            answer = f"## 检索结果摘要\n\n根据知识库检索到以下相关内容：\n\n"
            for i, ctx in enumerate(reranked, 1):
                text = ctx["text"] if isinstance(ctx, dict) else ctx
                doc_name = ctx.get("metadata", {}).get("doc_name", "未知文档") if isinstance(ctx, dict) else "未知文档"
                answer += f"### 来源 {i}: {doc_name}\n{text[:300]}...\n\n"
            answer += "\n> **注意**: Qwen API 未连接，以上为检索结果摘要。配置 API Key 后可获得完整的 AI 回答。"

        return {
            "answer": answer,
            "contexts": contexts,
            "initial_docs": initial_docs
        }


# ========== 全局 RAG 引擎实例 ==========
rag_engine: RAGEngine = None

# ========== 元数据管理 ==========

def load_metadata() -> dict:
    if os.path.exists(META_FILE):
        with open(META_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {"knowledge_bases": {}, "documents": {}}


def save_metadata(meta: dict):
    with open(META_FILE, 'w', encoding='utf-8') as f:
        json.dump(meta, f, ensure_ascii=False, indent=2)


# ========== 对话历史管理 ==========

def load_chat_history() -> dict:
    if os.path.exists(CHAT_HISTORY_FILE):
        with open(CHAT_HISTORY_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return {"conversations": []}


def save_chat_history(data: dict):
    with open(CHAT_HISTORY_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)


# ========== FastAPI App ==========

app = FastAPI(title="RAG Server (simple-rag)", version="2.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def startup_event():
    global rag_engine
    print("=" * 60)
    print("RAG Server 启动中...")
    print(f"  ChromaDB 数据: {CHROMA_DIR}")
    print(f"  simple-rag 路径: {SIMPLE_RAG_DIR}")
    print(f"  SentenceTransformer: {SENTENCE_MODEL_DIR}")
    print(f"  CrossEncoder: {CROSS_ENCODER_MODEL_DIR}")
    print(f"  Qwen API: {QWEN_MODEL} (DashScope)")
    print("=" * 60)
    try:
        rag_engine = RAGEngine()
    except Exception as e:
        print(f"[RAG] 引擎初始化失败: {e}")
        traceback.print_exc()
        rag_engine = None

    # 预置演示数据（若数据库为空）
    if rag_engine:
        _init_demo_data()


def _init_demo_data():
    """首次启动时预置演示知识库 + 文档 + Chunks，方便截图展示"""
    meta = load_metadata()
    if meta.get("knowledge_bases"):
        return  # 已有数据，跳过

    print("[RAG] 初始化演示数据...")
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    DEMO_DOCS = [
        {
            "id": "doc_demo_001",
            "name": "220kV线路故障处置规程.txt",
            "text": (
                "第一章 总则\n"
                "1.1 为规范220kV输电线路故障的处置流程，保障电网安全稳定运行，根据《电力系统调度规程》和《国家电网公司输电线路运行规程》，特制定本规程。\n"
                "1.2 本规程适用于220kV及以上电压等级输电线路发生各类故障时的调度处置操作。\n\n"
                "第二章 单相接地故障处置\n"
                "2.1 故障判断：当220kV线路发生单相接地故障时，保护装置应正确动作。调度员应立即确认以下信息：\n"
                "（1）保护动作类型：接地距离保护I段或II段、零序电流保护等；\n"
                "（2）故障相别：A相、B相或C相；\n"
                "（3）重合闸动作情况：重合成功或重合失败。\n"
                "2.2 重合闸成功处置：若自动重合闸动作成功，线路恢复正常运行。调度员应：\n"
                "（1）记录保护动作信息和故障录波数据；\n"
                "（2）通知运维人员对线路进行特巡，重点检查杆塔、绝缘子、导线等设备；\n"
                "（3）关注线路运行状态，若短时间内再次跳闸，按永久性故障处置。\n"
                "2.3 重合闸失败处置：若重合闸未成功或未装设重合闸，按以下步骤处置：\n"
                "（1）立即确认线路两侧断路器均已跳开，线路已退出运行；\n"
                "（2）通知运维人员对故障线路进行全线巡视，查找故障点；\n"
                "（3）评估线路停运对系统运行方式的影响，必要时调整电网运行方式；\n"
                "（4）故障排除后，按调度规程进行线路试送电操作。\n\n"
                "第三章 相间短路故障处置\n"
                "3.1 两相短路或三相短路为严重故障，保护装置应可靠快速动作切除故障。\n"
                "3.2 调度员接到相间短路故障报告后应：\n"
                "（1）确认保护正确动作，线路两侧断路器已跳开；\n"
                "（2）禁止自动重合闸，防止合闸于永久性故障；\n"
                "（3）立即进行系统稳定分析，检查相邻线路是否过负荷；\n"
                "（4）如有必要，启动紧急限负荷预案，确保系统安全。\n"
                "3.3 负荷转供方案：线路停运后应及时评估负荷转供需求，通过调整开关状态实现负荷转移。\n\n"
                "第四章 线路强送电\n"
                "4.1 强送电条件：故障排除且运维确认设备完好后，方可进行强送电。\n"
                "4.2 强送电操作步骤：\n"
                "（1）确认线路两侧隔离开关在合闸位置；\n"
                "（2）确认线路保护、重合闸装置投入正常；\n"
                "（3）选择一侧断路器进行试送电（优先选择系统侧）；\n"
                "（4）送电成功后合上另一侧断路器，恢复正常运行方式。"
            )
        },
        {
            "id": "doc_demo_002",
            "name": "主变压器过负荷运行管理规程.txt",
            "text": (
                "第一章 总则\n"
                "1.1 为加强主变压器过负荷运行管理，防止因过负荷导致设备损坏和电网事故，制定本规程。\n"
                "1.2 主变压器正常运行最大负荷率不应超过额定容量的85%，短时过负荷不应超过额定容量的120%。\n\n"
                "第二章 过负荷监视\n"
                "2.1 调度员应密切监视各主变压器的负荷率，当负荷率达到80%时发出预警。\n"
                "2.2 监视内容包括：有功功率、无功功率、电流、油温、绕组温度、环境温度。\n"
                "2.3 夏季高温时段（环境温度超过35°C），主变压器散热能力下降约5-8%，应适当降低允许负荷率。\n\n"
                "第三章 过负荷应急处置\n"
                "3.1 一级预警（负荷率85%-100%）：\n"
                "（1）通知变电站运维人员加强现场监视；\n"
                "（2）启动强迫风冷装置，提升散热能力；\n"
                "（3）协调调整无功补偿，降低变压器无功负荷。\n"
                "3.2 二级预警（负荷率100%-120%）：\n"
                "（1）立即启动负荷转移方案，通过联络开关将部分负荷转至相邻变电站；\n"
                "（2）通知大工业用户按有序用电方案执行限电；\n"
                "（3）每15分钟上报一次主变温度和负荷数据。\n"
                "3.3 紧急处置（负荷率超过120%或油温超过85°C）：\n"
                "（1）立即执行拉闸限电操作，按负荷优先级依次切除非重要负荷；\n"
                "（2）通知上级调度，申请系统层面支援；\n"
                "（3）做好主变压器停运准备，确保备用变压器可投入。\n\n"
                "第四章 N-1检修期间管理\n"
                "4.1 单台主变检修期间，在运主变承担全部负荷，应提前制定运行方案：\n"
                "（1）评估在运主变最大允许负荷，制定限负荷方案；\n"
                "（2）安排大用户错峰用电，降低高峰负荷；\n"
                "（3）备齐应急物资，确保快速响应突发情况。"
            )
        },
        {
            "id": "doc_demo_003",
            "name": "电网频率异常应急处置预案.txt",
            "text": (
                "第一章 总则\n"
                "1.1 电网正常运行频率为50Hz，允许偏差范围为±0.2Hz。当频率超出此范围时，应立即启动应急处置。\n"
                "1.2 频率异常分类：\n"
                "（1）低频率事件：频率低于49.8Hz；\n"
                "（2）高频率事件：频率高于50.2Hz。\n\n"
                "第二章 低频率事件处置\n"
                "2.1 频率49.5Hz-49.8Hz（一般低频）：\n"
                "（1）确认各发电机组一次调频功能正常投入；\n"
                "（2）检查AGC系统运行状态，必要时手动增加调节出力；\n"
                "（3）联系省调，了解系统整体频率情况和原因分析。\n"
                "2.2 频率49.0Hz-49.5Hz（严重低频）：\n"
                "（1）立即调用全部旋转备用容量；\n"
                "（2）启动快速响应机组（燃气轮机、抽水蓄能机组）；\n"
                "（3）准备执行低频减负荷方案（UFLS）。\n"
                "2.3 频率低于49.0Hz（紧急低频）：\n"
                "（1）低频减负荷装置自动动作，按轮次切除负荷；\n"
                "（2）通知所有电厂满发，最大限度增加出力；\n"
                "（3）向上级调度报告，启动电网黑启动预案准备。\n\n"
                "第三章 高频率事件处置\n"
                "3.1 频率50.2Hz-50.5Hz：\n"
                "（1）检查是否有大负荷突然切除；\n"
                "（2）通过AGC系统降低发电出力；\n"
                "（3）必要时手动降低机组出力。\n"
                "3.2 频率超过50.5Hz：\n"
                "（1）按优先级减少发电出力，优先降低火电机组出力；\n"
                "（2）安排部分机组解列备用；\n"
                "（3）启动高频切机方案。\n\n"
                "第四章 事后分析\n"
                "4.1 频率恢复正常后，应在24小时内提交频率事件分析报告。\n"
                "4.2 报告内容包括：事件时间线、原因分析、处置措施、影响评估和改进建议。"
            )
        }
    ]

    kb_id = "kb_demo_001"
    try:
        collection = rag_engine.chroma_client.get_or_create_collection(
            name=kb_id, embedding_function=rag_engine.embedding_function,
            metadata={"kb_name": "电力调度知识库", "description": "包含电力调度规程、故障处置手册和应急预案等专业文档"}
        )

        total_chunks = 0
        for doc_info in DEMO_DOCS:
            doc_id = doc_info["id"]
            doc_name = doc_info["name"]
            text = doc_info["text"]
            chunks = chunk_text(text, 512, 64)

            ids = []
            documents = []
            metadatas = []
            for c in chunks:
                chunk_id = f"{doc_id}_chunk_{c['index']}"
                ids.append(chunk_id)
                documents.append(c['text'])
                metadatas.append({
                    "doc_id": doc_id, "doc_name": doc_name, "kb_id": kb_id,
                    "chunk_index": c['index'], "start": c['start'], "end": c['end'],
                    "char_count": c['char_count']
                })

            if ids:
                collection.add(ids=ids, documents=documents, metadatas=metadatas)
            total_chunks += len(chunks)

            meta.setdefault("documents", {})[doc_id] = {
                "name": doc_name, "kb_id": kb_id, "chunk_count": len(chunks),
                "word_count": len(text), "size": f"{len(text.encode('utf-8'))/1024:.1f}KB",
                "status": "已索引", "create_time": now
            }

        meta["knowledge_bases"][kb_id] = {
            "name": "电力调度知识库",
            "description": "包含电力调度规程、故障处置手册和应急预案等专业文档",
            "chunk_count": total_chunks,
            "create_time": now, "update_time": now,
        }
        save_metadata(meta)
        print(f"[RAG] 演示数据初始化完成: 1个知识库, {len(DEMO_DOCS)}篇文档, {total_chunks}个Chunks")
    except Exception as e:
        print(f"[RAG] 演示数据初始化失败: {e}")
        traceback.print_exc()


# ========== API: 服务状态 ==========

@app.get("/api/rag/status")
def get_status():
    # 如果 Qwen 未连接，懒重试检测
    if rag_engine and not rag_engine.qwen_available:
        rag_engine._check_qwen()
    meta = load_metadata()
    kb_count = len(meta.get("knowledge_bases", {}))
    doc_count = len(meta.get("documents", {}))
    total_chunks = sum(kb.get("chunk_count", 0) for kb in meta.get("knowledge_bases", {}).values())
    return {
        "code": 200,
        "data": {
            "status": "running",
            "chromadb": "connected",
            "qwen": "connected" if (rag_engine and rag_engine.qwen_available) else "disconnected",
            "qwen_model": QWEN_MODEL,
            "knowledge_bases": kb_count,
            "documents": doc_count,
            "total_chunks": total_chunks,
        }
    }


# ========== API: 知识库管理 ==========

@app.get("/api/rag/knowledge-bases")
def list_knowledge_bases():
    meta = load_metadata()
    kbs = []
    for kb_id, kb_info in meta.get("knowledge_bases", {}).items():
        doc_count = 0
        word_count = 0
        for doc_id, doc_info in meta.get("documents", {}).items():
            if doc_info.get("kb_id") == kb_id:
                doc_count += 1
                word_count += doc_info.get("word_count", 0)
        kbs.append({
            "id": kb_id,
            "name": kb_info["name"],
            "description": kb_info.get("description", ""),
            "documentCount": doc_count,
            "wordCount": word_count,
            "chunkCount": kb_info.get("chunk_count", 0),
            "createTime": kb_info.get("create_time", ""),
            "updateTime": kb_info.get("update_time", ""),
        })
    return {"code": 200, "data": kbs}


@app.post("/api/rag/knowledge-bases")
def create_knowledge_base(name: str = Form(...), description: str = Form("")):
    if not rag_engine:
        raise HTTPException(status_code=503, detail="RAG 引擎未初始化，请检查后端日志")
    kb_id = "kb_" + uuid.uuid4().hex[:8]
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    try:
        rag_engine.chroma_client.get_or_create_collection(
            name=kb_id, embedding_function=rag_engine.embedding_function,
            metadata={"kb_name": name, "description": description}
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"ChromaDB 创建失败: {str(e)}")

    meta = load_metadata()
    meta["knowledge_bases"][kb_id] = {
        "name": name, "description": description,
        "chunk_count": 0, "create_time": now, "update_time": now,
    }
    save_metadata(meta)
    return {"code": 200, "data": {"id": kb_id, "name": name}, "message": "知识库创建成功"}


@app.delete("/api/rag/knowledge-bases/{kb_id}")
def delete_knowledge_base(kb_id: str):
    meta = load_metadata()
    if kb_id not in meta.get("knowledge_bases", {}):
        raise HTTPException(status_code=404, detail="知识库不存在")
    try:
        rag_engine.chroma_client.delete_collection(name=kb_id)
    except Exception:
        pass
    docs_to_delete = [did for did, d in meta.get("documents", {}).items() if d.get("kb_id") == kb_id]
    for did in docs_to_delete:
        fp = meta["documents"][did].get("file_path", "")
        if fp and os.path.exists(fp):
            os.remove(fp)
        del meta["documents"][did]
    del meta["knowledge_bases"][kb_id]
    save_metadata(meta)
    return {"code": 200, "message": "知识库已删除"}


# ========== API: 文档管理 ==========

@app.get("/api/rag/knowledge-bases/{kb_id}/documents")
def list_documents(kb_id: str):
    meta = load_metadata()
    docs = []
    for doc_id, doc_info in meta.get("documents", {}).items():
        if doc_info.get("kb_id") == kb_id:
            docs.append({
                "id": doc_id, "name": doc_info["name"],
                "size": doc_info.get("size", ""), "status": doc_info.get("status", "已索引"),
                "chunks": doc_info.get("chunk_count", 0),
                "wordCount": doc_info.get("word_count", 0),
                "createTime": doc_info.get("create_time", ""),
            })
    return {"code": 200, "data": docs}


@app.post("/api/rag/knowledge-bases/{kb_id}/upload")
async def upload_document(
    kb_id: str,
    file: UploadFile = File(...),
    chunk_size: int = Form(512),
    chunk_overlap: int = Form(64),
):
    meta = load_metadata()
    if kb_id not in meta.get("knowledge_bases", {}):
        raise HTTPException(status_code=404, detail="知识库不存在")

    doc_id = "doc_" + uuid.uuid4().hex[:8]
    kb_upload_dir = os.path.join(UPLOAD_DIR, kb_id)
    os.makedirs(kb_upload_dir, exist_ok=True)
    file_path = os.path.join(kb_upload_dir, f"{doc_id}_{file.filename}")
    with open(file_path, 'wb') as f:
        content = await file.read()
        f.write(content)

    file_size = len(content)
    size_str = f"{file_size / 1024:.1f}KB" if file_size < 1024 * 1024 else f"{file_size / 1024 / 1024:.1f}MB"

    try:
        raw_text = extract_text(file_path, file.filename)
    except Exception as e:
        os.remove(file_path)
        raise HTTPException(status_code=400, detail=f"文本提取失败: {str(e)}")

    if not raw_text.strip():
        os.remove(file_path)
        raise HTTPException(status_code=400, detail="文档内容为空")

    chunks = chunk_text(raw_text, chunk_size=chunk_size, overlap=chunk_overlap)
    if not chunks:
        os.remove(file_path)
        raise HTTPException(status_code=400, detail="分块结果为空")

    try:
        collection = rag_engine.get_collection(kb_id)
        ids = [f"{doc_id}_chunk_{c['index']}" for c in chunks]
        documents = [c['text'] for c in chunks]
        metadatas = [{
            "doc_id": doc_id, "doc_name": file.filename,
            "chunk_index": c['index'], "start": c['start'], "end": c['end'],
            "char_count": c['char_count'],
            "create_time": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        } for c in chunks]
        batch_size = 500
        for i in range(0, len(ids), batch_size):
            collection.add(
                ids=ids[i:i+batch_size],
                documents=documents[i:i+batch_size],
                metadatas=metadatas[i:i+batch_size],
            )
    except Exception as e:
        os.remove(file_path)
        raise HTTPException(status_code=500, detail=f"ChromaDB 入库失败: {str(e)}")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    meta["documents"][doc_id] = {
        "kb_id": kb_id, "name": file.filename, "file_path": file_path,
        "size": size_str, "status": "已索引",
        "chunk_count": len(chunks), "word_count": len(raw_text),
        "chunk_size": chunk_size, "chunk_overlap": chunk_overlap, "create_time": now,
    }
    kb_meta = meta["knowledge_bases"][kb_id]
    kb_meta["update_time"] = now
    kb_meta["chunk_count"] = kb_meta.get("chunk_count", 0) + len(chunks)
    save_metadata(meta)

    return {
        "code": 200,
        "data": {"doc_id": doc_id, "name": file.filename, "chunks": len(chunks), "word_count": len(raw_text), "size": size_str},
        "message": f"文档上传成功，已切分为 {len(chunks)} 个 Chunk 并向量化入库"
    }


@app.delete("/api/rag/documents/{doc_id}")
def delete_document(doc_id: str):
    meta = load_metadata()
    if doc_id not in meta.get("documents", {}):
        raise HTTPException(status_code=404, detail="文档不存在")
    doc_info = meta["documents"][doc_id]
    kb_id = doc_info["kb_id"]
    try:
        collection = rag_engine.chroma_client.get_collection(name=kb_id)
        results = collection.get(where={"doc_id": doc_id}, include=[])
        if results["ids"]:
            collection.delete(ids=results["ids"])
            kb_meta = meta["knowledge_bases"].get(kb_id, {})
            kb_meta["chunk_count"] = max(0, kb_meta.get("chunk_count", 0) - len(results["ids"]))
    except Exception:
        pass
    fp = doc_info.get("file_path", "")
    if fp and os.path.exists(fp):
        os.remove(fp)
    del meta["documents"][doc_id]
    save_metadata(meta)
    return {"code": 200, "message": "文档已删除"}


# ========== API: Chunk 详情 ==========

@app.get("/api/rag/documents/{doc_id}/chunks")
def get_document_chunks(doc_id: str, page: int = Query(1, ge=1), page_size: int = Query(20, ge=1, le=100)):
    try:
        meta = load_metadata()
        if doc_id not in meta.get("documents", {}):
            raise HTTPException(status_code=404, detail="文档不存在")
        doc_info = meta["documents"][doc_id]
        kb_id = doc_info["kb_id"]
        if not rag_engine:
            raise HTTPException(status_code=503, detail="RAG 引擎未初始化")

        collection = rag_engine.get_collection(kb_id)
        results = collection.get(where={"doc_id": doc_id}, include=["documents", "metadatas", "embeddings"])

        chunks = []
        for i in range(len(results.get("ids", []))):
            emb = results["embeddings"][i] if results.get("embeddings") and i < len(results["embeddings"]) else []
            chunks.append({
                "id": results["ids"][i],
                "text": results["documents"][i] if results.get("documents") else "",
                "metadata": results["metadatas"][i] if results.get("metadatas") else {},
                "embedding_preview": list(emb[:8]) if emb is not None and len(emb) > 0 else [],
                "embedding_dim": len(emb) if emb is not None else 0,
            })
        chunks.sort(key=lambda x: x.get("metadata", {}).get("chunk_index", 0))
        total = len(chunks)
        start = (page - 1) * page_size
        return {"code": 200, "data": {"chunks": chunks[start:start+page_size], "total": total, "page": page, "pageSize": page_size}}
    except HTTPException:
        raise
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"查询 Chunk 失败: {str(e)}")


# ========== API: 语义检索 ==========

@app.post("/api/rag/search")
def search_knowledge(query: str = Form(...), kb_id: str = Form(None), top_k: int = Form(5)):
    meta = load_metadata()
    kb_ids = [kb_id] if kb_id else list(meta.get("knowledge_bases", {}).keys())
    results = rag_engine.dense_retrieve(query, kb_ids, top_k)
    for r in results:
        kid = r.get("kb_id", "")
        r["kb_name"] = meta.get("knowledge_bases", {}).get(kid, {}).get("name", kid)
    return {"code": 200, "data": results}


# ========== API: 文本入库（流水线） ==========

@app.post("/api/rag/knowledge-bases/{kb_id}/ingest-text")
def ingest_text(kb_id: str, text: str = Form(...), doc_name: str = Form("pipeline_doc.txt"),
                chunk_size: int = Form(512), chunk_overlap: int = Form(64)):
    meta = load_metadata()
    if kb_id not in meta.get("knowledge_bases", {}):
        raise HTTPException(status_code=404, detail="知识库不存在")
    if not text.strip():
        raise HTTPException(status_code=400, detail="文本为空")

    doc_id = "doc_" + uuid.uuid4().hex[:8]
    chunks = chunk_text(text, chunk_size=chunk_size, overlap=chunk_overlap)
    if not chunks:
        raise HTTPException(status_code=400, detail="分块为空")

    try:
        collection = rag_engine.get_collection(kb_id)
        ids = [f"{doc_id}_chunk_{c['index']}" for c in chunks]
        documents = [c['text'] for c in chunks]
        metadatas = [{"doc_id": doc_id, "doc_name": doc_name, "chunk_index": c['index'],
                      "start": c['start'], "end": c['end'], "char_count": c['char_count'],
                      "create_time": datetime.now().strftime("%Y-%m-%d %H:%M:%S")} for c in chunks]
        for i in range(0, len(ids), 500):
            collection.add(ids=ids[i:i+500], documents=documents[i:i+500], metadatas=metadatas[i:i+500])
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"入库失败: {str(e)}")

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    meta["documents"][doc_id] = {
        "kb_id": kb_id, "name": doc_name, "file_path": "",
        "size": f"{len(text.encode('utf-8'))/1024:.1f}KB", "status": "已索引",
        "chunk_count": len(chunks), "word_count": len(text),
        "chunk_size": chunk_size, "chunk_overlap": chunk_overlap, "create_time": now,
    }
    kb_meta = meta["knowledge_bases"][kb_id]
    kb_meta["update_time"] = now
    kb_meta["chunk_count"] = kb_meta.get("chunk_count", 0) + len(chunks)
    save_metadata(meta)
    return {"code": 200, "data": {"doc_id": doc_id, "chunks": len(chunks), "word_count": len(text)},
            "message": f"已切分 {len(chunks)} 个 Chunk 并入库"}


# ========== API: RAG 对话 ==========

@app.post("/api/rag/chat")
def rag_chat(
    query: str = Form(...),
    conversation_id: str = Form(None),
    kb_id: str = Form(None),
    model: str = Form(None),
):
    """RAG 对话：检索知识库 → CrossEncoder 重排 → Qwen 生成回答"""
    if not rag_engine:
        raise HTTPException(status_code=503, detail="RAG 引擎未初始化，请检查后端日志")
    kb_ids = [kb_id] if kb_id else None
    result = rag_engine.rag_query(query, kb_ids=kb_ids, model=model)

    # 构建 references（检索来源）
    references = []
    for ctx in result.get("contexts", []):
        meta = ctx.get("metadata", {})
        references.append({
            "title": meta.get("doc_name", "未知文档"),
            "section": f"Chunk #{meta.get('chunk_index', '?')}",
            "relevance": round(max(0, 1 - ctx.get("distance", 1)), 2) if "distance" in ctx else round(ctx.get("rerank_score", 0), 2),
            "text": ctx.get("text", "")[:200],
        })

    # 保存到对话历史
    conv_id = conversation_id or ("conv_" + uuid.uuid4().hex[:8])
    history = load_chat_history()
    conv = next((c for c in history["conversations"] if c["id"] == conv_id), None)
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    if not conv:
        conv = {
            "id": conv_id,
            "title": query[:20] + "..." if len(query) > 20 else query,
            "createTime": now,
            "messages": []
        }
        history["conversations"].insert(0, conv)

    conv["messages"].append({"role": "user", "content": query, "time": now})
    conv["messages"].append({
        "role": "assistant", "content": result["answer"],
        "references": references, "time": now
    })
    conv["updateTime"] = now

    # 只保留最近50个对话
    history["conversations"] = history["conversations"][:50]
    save_chat_history(history)

    return {
        "code": 200,
        "data": {
            "conversation_id": conv_id,
            "answer": result["answer"],
            "references": references,
            "contexts": result.get("contexts", []),
            "initial_docs": result.get("initial_docs", []),
        }
    }


# ========== API: 对话历史 ==========

@app.get("/api/rag/chat/history")
def get_chat_history():
    """获取所有对话历史"""
    history = load_chat_history()
    return {"code": 200, "data": history.get("conversations", [])}


@app.get("/api/rag/chat/history/{conversation_id}")
def get_conversation(conversation_id: str):
    """获取单个对话详情"""
    history = load_chat_history()
    conv = next((c for c in history["conversations"] if c["id"] == conversation_id), None)
    if not conv:
        raise HTTPException(status_code=404, detail="对话不存在")
    return {"code": 200, "data": conv}


@app.delete("/api/rag/chat/history/{conversation_id}")
def delete_conversation(conversation_id: str):
    """删除对话"""
    history = load_chat_history()
    history["conversations"] = [c for c in history["conversations"] if c["id"] != conversation_id]
    save_chat_history(history)
    return {"code": 200, "message": "对话已删除"}


# ========== API: 数据清洗 ==========

@app.post("/api/rag/clean-text")
def clean_text_api(
    text: str = Form(None),
    rules: str = Form("[]"),
):
    """文本清洗 API，支持多种清洗规则"""
    import re
    if not text:
        raise HTTPException(status_code=400, detail="文本为空")

    rule_list = json.loads(rules) if rules else []
    original = text
    applied = []

    for rule in rule_list:
        rid = rule.get("id", "")
        if not rule.get("enabled", True):
            continue
        if rid == "html_tags":
            text = re.sub(r'<[^>]+>', '', text)
            text = re.sub(r'&[a-zA-Z]+;', ' ', text)
            applied.append("去除HTML标签")
        elif rid == "extra_whitespace":
            text = re.sub(r'[ \t]+', ' ', text)
            text = re.sub(r'\n{3,}', '\n\n', text)
            applied.append("压缩多余空白")
        elif rid == "phone_mask":
            text = re.sub(r'1[3-9]\d{9}', '1**********', text)
            applied.append("手机号脱敏")
        elif rid == "id_mask":
            text = re.sub(r'\d{17}[\dXx]', '******************', text)
            applied.append("身份证脱敏")
        elif rid == "email_mask":
            text = re.sub(r'[\w.+-]+@[\w-]+\.[\w.]+', '***@***.***', text)
            applied.append("邮箱脱敏")
        elif rid == "special_chars":
            text = re.sub(r'[\u200b\u200c\u200d\ufeff\u00a0]', '', text)
            applied.append("去除特殊字符")
        elif rid == "dedup_lines":
            lines = text.split('\n')
            seen = set()
            deduped = []
            for line in lines:
                stripped = line.strip()
                if stripped and stripped not in seen:
                    seen.add(stripped)
                    deduped.append(line)
                elif not stripped:
                    deduped.append(line)
            text = '\n'.join(deduped)
            applied.append("去除重复行")

    return {
        "code": 200,
        "data": {
            "original": original,
            "cleaned": text.strip(),
            "original_length": len(original),
            "cleaned_length": len(text.strip()),
            "compression_rate": f"{(1 - len(text.strip()) / max(len(original), 1)) * 100:.1f}%",
            "applied_rules": applied,
        }
    }


@app.post("/api/rag/clean-file")
async def clean_file_api(
    file: UploadFile = File(...),
    rules: str = Form("[]"),
):
    """上传文件进行清洗"""
    # 保存临时文件
    temp_path = os.path.join(UPLOAD_DIR, f"temp_{uuid.uuid4().hex[:8]}_{file.filename}")
    with open(temp_path, 'wb') as f:
        content = await file.read()
        f.write(content)

    try:
        text = extract_text(temp_path, file.filename)
    except Exception as e:
        os.remove(temp_path)
        raise HTTPException(status_code=400, detail=f"文件解析失败: {str(e)}")
    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

    # 复用 clean_text_api 的逻辑
    import re
    rule_list = json.loads(rules) if rules else []
    original = text
    applied = []
    for rule in rule_list:
        rid = rule.get("id", "")
        if not rule.get("enabled", True):
            continue
        if rid == "html_tags":
            text = re.sub(r'<[^>]+>', '', text)
            text = re.sub(r'&[a-zA-Z]+;', ' ', text)
            applied.append("去除HTML标签")
        elif rid == "extra_whitespace":
            text = re.sub(r'[ \t]+', ' ', text)
            text = re.sub(r'\n{3,}', '\n\n', text)
            applied.append("压缩多余空白")
        elif rid == "phone_mask":
            text = re.sub(r'1[3-9]\d{9}', '1**********', text)
            applied.append("手机号脱敏")
        elif rid == "id_mask":
            text = re.sub(r'\d{17}[\dXx]', '******************', text)
            applied.append("身份证脱敏")
        elif rid == "email_mask":
            text = re.sub(r'[\w.+-]+@[\w-]+\.[\w.]+', '***@***.***', text)
            applied.append("邮箱脱敏")
        elif rid == "special_chars":
            text = re.sub(r'[\u200b\u200c\u200d\ufeff\u00a0]', '', text)
            applied.append("去除特殊字符")
        elif rid == "dedup_lines":
            lines = text.split('\n')
            seen = set()
            deduped = []
            for line in lines:
                stripped = line.strip()
                if stripped and stripped not in seen:
                    seen.add(stripped)
                    deduped.append(line)
                elif not stripped:
                    deduped.append(line)
            text = '\n'.join(deduped)
            applied.append("去除重复行")

    return {
        "code": 200,
        "data": {
            "filename": file.filename,
            "original": original[:2000],
            "cleaned": text.strip()[:2000],
            "original_length": len(original),
            "cleaned_length": len(text.strip()),
            "compression_rate": f"{(1 - len(text.strip()) / max(len(original), 1)) * 100:.1f}%",
            "applied_rules": applied,
            "full_cleaned_text": text.strip(),
        }
    }


# ========== API: Qwen 模型管理 ==========

@app.get("/api/rag/ollama/models")
def list_qwen_models():
    """获取可用的 Qwen 模型列表"""
    models = [{"name": m["value"], "label": m["label"], "desc": m["desc"]} for m in QWEN_MODELS]
    return {"code": 200, "data": models}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8765, reload=True)
