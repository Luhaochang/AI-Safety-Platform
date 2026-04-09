"""
服务类定义 —— 电力调度RAG应用系统核心服务层
包含知识库管理、调度助手、决策审计三个模块的业务逻辑类
"""
import os
import re
import json
import uuid
import traceback
from datetime import datetime
from typing import List, Optional, Dict, Any

import numpy as np

from models import (
    KnowledgeBase, Document, Chunk, CleanResult,
    Conversation, Message, Reference, RAGResult,
    DecisionLog, ChatDecisionLog, TwinDecisionLog, RetrievedPassage,
)


# ============================================================
#  模块一：知识库管理 服务类
# ============================================================

class TextExtractor:
    """文本提取器
    
    负责从不同格式的文档文件中提取纯文本内容，
    支持PDF、Word（.docx）和纯文本（.txt/.md）等格式。
    作为知识接入流程的第一步，为后续清洗与切分提供原始文本。
    """

    @staticmethod
    def extract(file_path: str, filename: str) -> str:
        lower = filename.lower()
        if lower.endswith('.pdf'):
            return TextExtractor.extract_from_pdf(file_path)
        elif lower.endswith('.docx') or lower.endswith('.doc'):
            return TextExtractor.extract_from_docx(file_path)
        else:
            return TextExtractor.extract_from_txt(file_path)

    @staticmethod
    def extract_from_txt(file_path: str) -> str:
        import chardet
        with open(file_path, 'rb') as f:
            raw = f.read()
        detected = chardet.detect(raw)
        encoding = detected.get('encoding', 'utf-8') or 'utf-8'
        return raw.decode(encoding, errors='ignore')

    @staticmethod
    def extract_from_pdf(file_path: str) -> str:
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

    @staticmethod
    def extract_from_docx(file_path: str) -> str:
        from docx import Document as DocxDocument
        doc = DocxDocument(file_path)
        return '\n'.join([para.text for para in doc.paragraphs if para.text.strip()])


class TextCleaner:
    """文本清洗器
    
    对提取的原始文本执行多种清洗规则，包括HTML标签去除、
    空白压缩、敏感信息脱敏（手机号/身份证/邮箱）、特殊字符清理
    和重复行消除等，降低噪声干扰，提高知识处理质量。
    """

    SUPPORTED_RULES = [
        "html_tags", "extra_whitespace", "phone_mask",
        "id_mask", "email_mask", "special_chars", "dedup_lines",
    ]

    def clean(self, text: str, rules: List[dict]) -> CleanResult:
        original = text
        applied = []
        for rule in rules:
            rid = rule.get("id", "")
            if not rule.get("enabled", True):
                continue
            handler = getattr(self, f"_rule_{rid}", None)
            if handler:
                text = handler(text)
                applied.append(self._rule_label(rid))
        cleaned = text.strip()
        orig_len = len(original)
        clean_len = len(cleaned)
        rate = f"{(1 - clean_len / max(orig_len, 1)) * 100:.1f}%"
        return CleanResult(
            original=original, cleaned=cleaned,
            original_length=orig_len, cleaned_length=clean_len,
            compression_rate=rate, applied_rules=applied,
        )

    def _rule_html_tags(self, text: str) -> str:
        text = re.sub(r'<[^>]+>', '', text)
        text = re.sub(r'&[a-zA-Z]+;', ' ', text)
        return text

    def _rule_extra_whitespace(self, text: str) -> str:
        text = re.sub(r'[ \t]+', ' ', text)
        text = re.sub(r'\n{3,}', '\n\n', text)
        return text

    def _rule_phone_mask(self, text: str) -> str:
        return re.sub(r'1[3-9]\d{9}', '1**********', text)

    def _rule_id_mask(self, text: str) -> str:
        return re.sub(r'\d{17}[\dXx]', '******************', text)

    def _rule_email_mask(self, text: str) -> str:
        return re.sub(r'[\w.+-]+@[\w-]+\.[\w.]+', '***@***.***', text)

    def _rule_special_chars(self, text: str) -> str:
        return re.sub(r'[\u200b\u200c\u200d\ufeff\u00a0]', '', text)

    def _rule_dedup_lines(self, text: str) -> str:
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
        return '\n'.join(deduped)

    @staticmethod
    def _rule_label(rid: str) -> str:
        labels = {
            "html_tags": "去除HTML标签", "extra_whitespace": "压缩多余空白",
            "phone_mask": "手机号脱敏", "id_mask": "身份证脱敏",
            "email_mask": "邮箱脱敏", "special_chars": "去除特殊字符",
            "dedup_lines": "去除重复行",
        }
        return labels.get(rid, rid)


class TextChunker:
    """文本分块器
    
    将长文本按照滑动窗口策略切分为适于语义检索的知识片段。
    通过chunk_size控制片段大小，chunk_overlap控制相邻片段重叠区域，
    在保证语义完整性的同时满足向量检索粒度要求。
    """

    def __init__(self, chunk_size: int = 512, chunk_overlap: int = 64):
        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

    def chunk(self, text: str) -> List[dict]:
        return self.sliding_window_chunk(text, self.chunk_size, self.chunk_overlap)

    @staticmethod
    def sliding_window_chunk(text: str, chunk_size: int = 512, overlap: int = 64) -> List[dict]:
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


class VectorStore:
    """向量存储管理器
    
    封装ChromaDB向量数据库的操作接口，负责向量集合的创建与删除、
    知识片段的向量化入库与检索、Chunk数据的查询与管理。
    是知识库管理模块的持久化存储核心。
    """

    def __init__(self, chroma_client, embedding_function):
        self.chroma_client = chroma_client
        self.embedding_function = embedding_function

    def create_collection(self, kb_id: str, metadata: dict = None) -> Any:
        return self.chroma_client.get_or_create_collection(
            name=kb_id, embedding_function=self.embedding_function,
            metadata=metadata or {}
        )

    def delete_collection(self, kb_id: str):
        try:
            self.chroma_client.delete_collection(name=kb_id)
        except Exception:
            pass

    def add_chunks(self, kb_id: str, ids: List[str],
                   documents: List[str], metadatas: List[dict],
                   batch_size: int = 500):
        collection = self.create_collection(kb_id)
        for i in range(0, len(ids), batch_size):
            collection.add(
                ids=ids[i:i + batch_size],
                documents=documents[i:i + batch_size],
                metadatas=metadatas[i:i + batch_size],
            )

    def delete_chunks_by_doc(self, kb_id: str, doc_id: str) -> int:
        try:
            collection = self.chroma_client.get_collection(
                name=kb_id, embedding_function=self.embedding_function
            )
            results = collection.get(where={"doc_id": doc_id}, include=[])
            if results["ids"]:
                collection.delete(ids=results["ids"])
                return len(results["ids"])
        except Exception:
            pass
        return 0

    def get_chunks(self, kb_id: str, doc_id: str,
                   page: int = 1, page_size: int = 20) -> dict:
        collection = self.create_collection(kb_id)
        results = collection.get(
            where={"doc_id": doc_id},
            include=["documents", "metadatas", "embeddings"]
        )
        chunks = []
        for i in range(len(results.get("ids", []))):
            emb = results["embeddings"][i] if results.get("embeddings") and i < len(results["embeddings"]) else []
            chunks.append(Chunk(
                id=results["ids"][i],
                text=results["documents"][i] if results.get("documents") else "",
                metadata=results["metadatas"][i] if results.get("metadatas") else {},
                embedding=list(emb[:8]) if emb is not None and len(emb) > 0 else [],
                embedding_dim=len(emb) if emb is not None else 0,
                chunk_index=results["metadatas"][i].get("chunk_index", 0) if results.get("metadatas") else 0,
            ))
        chunks.sort(key=lambda x: x.chunk_index)
        total = len(chunks)
        start = (page - 1) * page_size
        page_chunks = chunks[start:start + page_size]
        return {
            "chunks": [c.to_dict() for c in page_chunks],
            "total": total, "page": page, "pageSize": page_size,
        }

    def query(self, kb_id: str, query_text: str, top_k: int = 10) -> List[dict]:
        try:
            collection = self.chroma_client.get_collection(
                name=kb_id, embedding_function=self.embedding_function
            )
            if collection.count() == 0:
                return []
            n = min(top_k, collection.count())
            results = collection.query(
                query_texts=[query_text], n_results=n,
                include=["documents", "metadatas", "distances"]
            )
            items = []
            if results["ids"] and results["ids"][0]:
                for i in range(len(results["ids"][0])):
                    items.append({
                        "id": results["ids"][0][i],
                        "text": results["documents"][0][i],
                        "metadata": results["metadatas"][0][i] if results["metadatas"] else {},
                        "distance": results["distances"][0][i],
                        "kb_id": kb_id,
                    })
            return items
        except Exception as e:
            print(f"[VectorStore] 查询 {kb_id} 失败: {e}")
            return []


class MetadataManager:
    """元数据管理器
    
    负责知识库和文档元数据的持久化存储与读写，
    维护知识库与文档的注册信息、状态和统计数据。
    """

    def __init__(self, meta_file: str):
        self.meta_file = meta_file

    def load(self) -> dict:
        if os.path.exists(self.meta_file):
            with open(self.meta_file, 'r', encoding='utf-8') as f:
                return json.load(f)
        return {"knowledge_bases": {}, "documents": {}}

    def save(self, meta: dict):
        with open(self.meta_file, 'w', encoding='utf-8') as f:
            json.dump(meta, f, ensure_ascii=False, indent=2)

    def get_knowledge_base(self, kb_id: str) -> Optional[KnowledgeBase]:
        meta = self.load()
        kb_data = meta.get("knowledge_bases", {}).get(kb_id)
        if kb_data:
            kb = KnowledgeBase.from_dict(kb_id, kb_data)
            # 计算文档数和字数
            for doc_id, doc_info in meta.get("documents", {}).items():
                if doc_info.get("kb_id") == kb_id:
                    kb.document_count += 1
                    kb.word_count += doc_info.get("word_count", 0)
            return kb
        return None

    def list_knowledge_bases(self) -> List[KnowledgeBase]:
        meta = self.load()
        kbs = []
        for kb_id, kb_info in meta.get("knowledge_bases", {}).items():
            kb = KnowledgeBase.from_dict(kb_id, kb_info)
            for doc_id, doc_info in meta.get("documents", {}).items():
                if doc_info.get("kb_id") == kb_id:
                    kb.document_count += 1
                    kb.word_count += doc_info.get("word_count", 0)
            kbs.append(kb)
        return kbs

    def list_documents(self, kb_id: str) -> List[Document]:
        meta = self.load()
        docs = []
        for doc_id, doc_info in meta.get("documents", {}).items():
            if doc_info.get("kb_id") == kb_id:
                docs.append(Document.from_dict(doc_id, doc_info))
        return docs

    def register_knowledge_base(self, kb: KnowledgeBase):
        meta = self.load()
        meta["knowledge_bases"][kb.id] = {
            "name": kb.name, "description": kb.description,
            "chunk_count": kb.chunk_count,
            "create_time": kb.create_time, "update_time": kb.update_time,
        }
        self.save(meta)

    def remove_knowledge_base(self, kb_id: str) -> List[str]:
        meta = self.load()
        docs_to_delete = [did for did, d in meta.get("documents", {}).items()
                          if d.get("kb_id") == kb_id]
        file_paths = []
        for did in docs_to_delete:
            fp = meta["documents"][did].get("file_path", "")
            if fp:
                file_paths.append(fp)
            del meta["documents"][did]
        if kb_id in meta.get("knowledge_bases", {}):
            del meta["knowledge_bases"][kb_id]
        self.save(meta)
        return file_paths

    def register_document(self, doc: Document):
        meta = self.load()
        meta["documents"][doc.id] = {
            "kb_id": doc.kb_id, "name": doc.name,
            "file_path": doc.file_path, "size": doc.size,
            "status": doc.status, "chunk_count": doc.chunk_count,
            "word_count": doc.word_count, "chunk_size": doc.chunk_size,
            "chunk_overlap": doc.chunk_overlap, "create_time": doc.create_time,
        }
        # 更新知识库的chunk_count和update_time
        kb = meta.get("knowledge_bases", {}).get(doc.kb_id, {})
        kb["chunk_count"] = kb.get("chunk_count", 0) + doc.chunk_count
        kb["update_time"] = doc.create_time
        self.save(meta)

    def remove_document(self, doc_id: str) -> Optional[Document]:
        meta = self.load()
        if doc_id not in meta.get("documents", {}):
            return None
        doc_info = meta["documents"][doc_id]
        doc = Document.from_dict(doc_id, doc_info)
        # 更新知识库chunk_count
        kb = meta.get("knowledge_bases", {}).get(doc.kb_id, {})
        kb["chunk_count"] = max(0, kb.get("chunk_count", 0) - doc.chunk_count)
        del meta["documents"][doc_id]
        self.save(meta)
        return doc


# ============================================================
#  模块二：调度助手 服务类
# ============================================================

class Retriever:
    """检索器
    
    实现多策略知识检索，包括基于BM25的稀疏检索、
    基于SentenceTransformer的稠密检索，以及RRF融合的混合检索。
    为RAG流程提供候选知识片段召回能力。
    """

    def __init__(self, sentence_model, vector_store: VectorStore):
        self.sentence_model = sentence_model
        self.vector_store = vector_store
        # 检查BM25可用性
        try:
            from rank_bm25 import BM25Okapi
            self._bm25_cls = BM25Okapi
            self.bm25_available = True
        except ImportError:
            self._bm25_cls = None
            self.bm25_available = False

    def dense_retrieve(self, query: str, kb_ids: List[str],
                       top_k: int = 10) -> List[dict]:
        all_results = []
        for kid in kb_ids:
            results = self.vector_store.query(kid, query, top_k)
            all_results.extend(results)
        all_results.sort(key=lambda x: x.get("distance", 0))
        return all_results[:top_k]

    def bm25_retrieve(self, query: str, documents: List[str],
                      top_k: int = 10) -> List[dict]:
        if not documents or not self.bm25_available:
            return []
        tokenized_docs = [doc.split() for doc in documents]
        bm25 = self._bm25_cls(tokenized_docs)
        scores = bm25.get_scores(query.split())
        top_n = np.argsort(scores)[::-1][:top_k]
        return [{"text": documents[i], "score": float(scores[i]),
                 "index": int(i)} for i in top_n]

    def hybrid_retrieve(self, query: str, kb_ids: List[str],
                        top_k: int = 5) -> List[dict]:
        candidate_k = top_k * 3
        dense_results = self.dense_retrieve(query, kb_ids, candidate_k)
        all_docs_text = [r["text"] for r in dense_results]
        if not all_docs_text:
            return []
        bm25_results = self.bm25_retrieve(query, all_docs_text, candidate_k)
        return self._rrf_fusion(dense_results, bm25_results, top_k)

    @staticmethod
    def _rrf_fusion(dense_results: List[dict], bm25_results: List[dict],
                    top_k: int, k: int = 60) -> List[dict]:
        doc_scores = {}
        for rank, dr in enumerate(dense_results, start=1):
            txt = dr["text"]
            doc_scores.setdefault(txt, {"score": 0, "data": dr})
            doc_scores[txt]["score"] += 0.5 / (k + rank)
        for rank, br in enumerate(bm25_results, start=1):
            txt = br["text"]
            if txt in doc_scores:
                doc_scores[txt]["score"] += 0.5 / (k + rank)
            else:
                matching = next((d for d in dense_results if d["text"] == txt), None)
                if matching:
                    doc_scores[txt] = {"score": 0.5 / (k + rank), "data": matching}
        sorted_docs = sorted(doc_scores.values(), key=lambda x: x["score"], reverse=True)
        return [item["data"] for item in sorted_docs[:top_k]]


class Reranker:
    """重排器
    
    基于CrossEncoder模型对候选检索结果进行精细化重排序，
    通过交叉编码计算查询与每个候选片段的语义相关性评分，
    筛选出与当前问题最相关的知识片段，提升检索质量。
    """

    def __init__(self, cross_encoder):
        self.cross_encoder = cross_encoder

    def rerank(self, query: str, documents: List[dict],
               top_k: int = 5) -> List[dict]:
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


class LLMGenerator:
    """大语言模型生成器
    
    封装通义千问Qwen API的调用逻辑，负责构造增强提示并调用大语言模型
    生成面向电力调度场景的回答。支持多模型切换和连接状态检测。
    """

    SYSTEM_PROMPT = "你是一个专业的电力调度决策助手。请根据提供的参考资料回答问题，用Markdown格式，专业、准确、有条理。"

    def __init__(self, api_key: str, api_url: str, default_model: str = "qwen-plus"):
        self.api_key = api_key
        self.api_url = api_url
        self.default_model = default_model
        self.available = False
        self.check_connection()

    def check_connection(self) -> bool:
        if not self.api_key:
            self.available = False
            return False
        import requests as http_requests
        try:
            resp = http_requests.post(
                self.api_url,
                headers={"Authorization": f"Bearer {self.api_key}",
                         "Content-Type": "application/json"},
                json={"model": self.default_model,
                      "messages": [{"role": "user", "content": "你好"}],
                      "max_tokens": 50},
                timeout=15
            )
            if resp.status_code == 200:
                self.available = True
            elif resp.status_code in (401, 403):
                self.available = False
            else:
                self.available = True
        except Exception:
            self.available = False
        return self.available

    def generate(self, prompt: str, model: str = None) -> Optional[str]:
        if not self.available:
            self.check_connection()
        if not self.available:
            return None
        import requests as http_requests
        use_model = model or self.default_model
        try:
            resp = http_requests.post(
                self.api_url,
                headers={"Authorization": f"Bearer {self.api_key}",
                         "Content-Type": "application/json"},
                json={
                    "model": use_model,
                    "messages": [
                        {"role": "system", "content": self.SYSTEM_PROMPT},
                        {"role": "user", "content": prompt}
                    ],
                    "temperature": 0.7, "max_tokens": 2000
                },
                timeout=60
            )
            resp.raise_for_status()
            data = resp.json()
            return data.get("choices", [{}])[0].get("message", {}).get("content", "")
        except Exception as e:
            print(f"[LLMGenerator] 生成失败: {e}")
            return None

    def build_rag_prompt(self, query: str, contexts: List[dict]) -> str:
        context_text = "\n\n".join([c.get("text", "") for c in contexts])
        return f"""你是一个专业的电力调度决策助手。请根据以下检索到的参考资料回答用户的问题。
请用Markdown格式回答，包含清晰的标题、分步骤说明和注意事项。回答要专业、准确、有条理。
如果参考资料不足以回答问题，请如实说明。

## 参考资料
{context_text}

## 用户问题
{query}

## 回答
"""


class RAGPipeline:
    """RAG流水线
    
    编排完整的检索增强生成流程：混合检索 → CrossEncoder重排 → LLM生成。
    协调Retriever、Reranker和LLMGenerator三个组件，
    将用户查询转化为有据可依的专业回答。
    """

    def __init__(self, retriever: Retriever, reranker: Reranker,
                 generator: LLMGenerator, metadata_mgr: MetadataManager):
        self.retriever = retriever
        self.reranker = reranker
        self.generator = generator
        self.metadata_mgr = metadata_mgr

    def query(self, query: str, kb_ids: List[str] = None,
              top_k: int = 5, rerank_top_k: int = 3,
              model: str = None) -> RAGResult:
        # 确定检索范围
        if not kb_ids:
            meta = self.metadata_mgr.load()
            kb_ids = list(meta.get("knowledge_bases", {}).keys())
        if not kb_ids:
            return RAGResult(answer="当前没有知识库，请先创建知识库并上传文档。")

        # 1. 混合检索
        retrieved = self.retriever.hybrid_retrieve(query, kb_ids, top_k)
        initial_docs = [{"text": r["text"],
                         "metadata": r.get("metadata", {}),
                         "distance": r.get("distance", 0)} for r in retrieved]
        if not retrieved:
            return RAGResult(answer="未检索到相关文档，请确保知识库中已上传相关文档。")

        # 2. CrossEncoder重排
        reranked = self.reranker.rerank(query, retrieved, rerank_top_k)
        contexts = [{"text": r["text"],
                     "metadata": r.get("metadata", {}),
                     "rerank_score": r.get("rerank_score", 0)} for r in reranked]

        # 3. 构建引用来源
        references = []
        for ctx in reranked:
            meta = ctx.get("metadata", {})
            ref = Reference(
                title=meta.get("doc_name", "未知文档"),
                section=f"Chunk #{meta.get('chunk_index', '?')}",
                relevance=round(max(0, 1 - ctx.get("distance", 1)), 2)
                if "distance" in ctx else round(ctx.get("rerank_score", 0), 2),
                text=ctx.get("text", "")[:200],
            )
            references.append(ref)

        # 4. LLM生成
        prompt = self.generator.build_rag_prompt(query, reranked)
        answer = self.generator.generate(prompt, model)
        if not answer:
            answer = self._build_fallback_answer(reranked)

        return RAGResult(
            answer=answer, contexts=contexts,
            initial_docs=initial_docs, references=references,
        )

    @staticmethod
    def _build_fallback_answer(reranked: List[dict]) -> str:
        answer = "## 检索结果摘要\n\n根据知识库检索到以下相关内容：\n\n"
        for i, ctx in enumerate(reranked, 1):
            text = ctx["text"] if isinstance(ctx, dict) else ctx
            doc_name = ctx.get("metadata", {}).get("doc_name", "未知文档") if isinstance(ctx, dict) else "未知文档"
            answer += f"### 来源 {i}: {doc_name}\n{text[:300]}...\n\n"
        answer += "\n> **注意**: Qwen API 未连接，以上为检索结果摘要。配置 API Key 后可获得完整的 AI 回答。"
        return answer


class ConversationManager:
    """会话管理器
    
    负责对话历史的持久化存储与管理，包括会话创建、消息追加、
    历史查询和会话删除等操作，维护调度助手的对话上下文。
    """

    def __init__(self, history_file: str):
        self.history_file = history_file

    def load_all(self) -> List[dict]:
        if os.path.exists(self.history_file):
            with open(self.history_file, 'r', encoding='utf-8') as f:
                data = json.load(f)
            return data.get("conversations", [])
        return []

    def _save_all(self, conversations: List[dict]):
        with open(self.history_file, 'w', encoding='utf-8') as f:
            json.dump({"conversations": conversations}, f, ensure_ascii=False, indent=2)

    def get_conversation(self, conv_id: str) -> Optional[dict]:
        convs = self.load_all()
        return next((c for c in convs if c["id"] == conv_id), None)

    def save_message(self, conv_id: str, query: str, answer: str,
                     references: List[dict]) -> str:
        convs = self.load_all()
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        conv = next((c for c in convs if c["id"] == conv_id), None)
        if not conv:
            conv = {
                "id": conv_id,
                "title": query[:20] + "..." if len(query) > 20 else query,
                "createTime": now, "messages": []
            }
            convs.insert(0, conv)
        conv["messages"].append({"role": "user", "content": query, "time": now})
        conv["messages"].append({
            "role": "assistant", "content": answer,
            "references": references, "time": now
        })
        conv["updateTime"] = now
        convs = convs[:50]
        self._save_all(convs)
        return conv_id

    def delete_conversation(self, conv_id: str):
        convs = self.load_all()
        convs = [c for c in convs if c["id"] != conv_id]
        self._save_all(convs)


# ============================================================
#  模块三：决策审计 服务类
# ============================================================

class DecisionLogManager:
    """决策日志管理器
    
    负责从对话历史和孪生推演记录中生成结构化决策日志，
    提供日志查询、关键词搜索、分类筛选和统计功能。
    """

    def __init__(self, conversation_mgr: ConversationManager):
        self.conversation_mgr = conversation_mgr

    def get_all_logs(self) -> List[dict]:
        convs = self.conversation_mgr.load_all()
        logs = []
        for conv in convs:
            first_q = next((m for m in conv.get("messages", [])
                           if m.get("role") == "user"), None)
            first_a = next((m for m in conv.get("messages", [])
                           if m.get("role") == "assistant"), None)
            logs.append({
                "id": conv["id"], "type": "chat", "typeLabel": "调度对话",
                "question": first_q["content"] if first_q else "",
                "answer": first_a["content"] if first_a else "",
                "user": conv.get("user", "值班调度员"),
                "duration": conv.get("duration", "-"),
                "createTime": conv.get("createTime", ""),
                "messages": conv.get("messages", []),
                "retrievedPassages": conv.get("retrievedPassages", []),
            })
        logs.sort(key=lambda x: x.get("createTime", ""), reverse=True)
        return logs

    def search_logs(self, keyword: str = "", type_filter: str = "all",
                    logs: List[dict] = None) -> List[dict]:
        if logs is None:
            logs = self.get_all_logs()
        if type_filter != "all":
            logs = [l for l in logs if l.get("type") == type_filter]
        if keyword.strip():
            kw = keyword.strip().lower()
            logs = [l for l in logs if
                    kw in l.get("question", "").lower() or
                    kw in l.get("answer", "").lower()]
        return logs

    def get_log_detail(self, log_id: str) -> Optional[dict]:
        logs = self.get_all_logs()
        return next((l for l in logs if l["id"] == log_id), None)

    def get_statistics(self) -> dict:
        logs = self.get_all_logs()
        return {
            "total": len(logs),
            "chat_count": sum(1 for l in logs if l.get("type") == "chat"),
            "twin_count": sum(1 for l in logs if l.get("type") == "twin"),
        }


class LogExportService:
    """日志导出服务
    
    支持将决策日志导出为Excel或CSV格式，
    便于离线复查、归档和统一管理。
    """

    @staticmethod
    def export_to_csv(logs: List[dict]) -> str:
        import csv
        import io
        output = io.StringIO()
        writer = csv.writer(output)
        writer.writerow(["ID", "类型", "问题/场景", "回答/结论",
                         "用户", "耗时", "时间"])
        for log in logs:
            writer.writerow([
                log.get("id", ""), log.get("typeLabel", ""),
                log.get("question", ""), log.get("answer", "")[:200],
                log.get("user", ""), log.get("duration", ""),
                log.get("createTime", ""),
            ])
        return output.getvalue()

    @staticmethod
    def format_log_data(log: dict) -> dict:
        return {
            "id": log.get("id"), "type": log.get("typeLabel"),
            "question": log.get("question"),
            "answer_summary": log.get("answer", "")[:200],
            "user": log.get("user"),
            "duration": log.get("duration"),
            "time": log.get("createTime"),
        }
