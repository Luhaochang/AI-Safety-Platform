# rag-server — RAG 知识库检索增强生成服务

基于 FastAPI + ChromaDB + SentenceTransformers 的 RAG 后端服务，提供知识库管理、文档处理、语义检索和大模型生成回答等完整功能。

## 技术栈

| 组件 | 用途 |
|------|------|
| FastAPI | Web 框架 & API 接口 |
| ChromaDB | 向量数据库（持久化存储） |
| SentenceTransformers (all-MiniLM-L6-v2) | 文本向量化编码 |
| CrossEncoder (ms-marco-MiniLM-L-6-v2) | 检索结果精排 |
| rank-bm25 | BM25 稀疏检索 |
| 通义千问 Qwen API | 大语言模型生成回答 |
| pdfplumber / PyPDF2 / python-docx | 多格式文档解析 |
| NLTK | 自然语言处理工具 |

## 环境要求

- **Python** >= 3.9
- 推荐使用 conda 或 venv 创建独立虚拟环境

## 安装 & 启动

```bash
# 1. 进入 rag-server 目录
cd rag-server

# 2. 创建虚拟环境（推荐）
conda create -n rag python=3.10
conda activate rag
# 或使用 venv:
# python -m venv venv && venv\Scripts\activate  (Windows)
# python -m venv venv && source venv/bin/activate  (Linux/Mac)

# 3. 安装依赖
pip install -r requirements.txt

# 4. 启动服务
python main.py

# 服务启动在 http://localhost:8765
# API 文档：http://localhost:8765/docs
```

## 本地模型说明

RAG 服务使用两个本地模型进行向量化和重排序，首次启动会自动从 HuggingFace 下载：

| 模型 | 大小 | 用途 |
|------|------|------|
| `sentence-transformers/all-MiniLM-L6-v2` | ~80MB | 文本向量化编码 |
| `cross-encoder/ms-marco-MiniLM-L-6-v2` | ~80MB | 检索结果交叉编码重排 |

> 如果项目同级目录存在 `simple-rag/models/` 并已下载过模型，服务会优先使用本地缓存路径，无需重复下载。

## 通义千问 API 配置

调度助手的大模型生成功能依赖通义千问 Qwen API：

```bash
# 方式一：环境变量（推荐）
export QWEN_API_KEY=sk-your-api-key-here
export QWEN_MODEL=qwen-plus  # 可选：qwen-turbo / qwen-plus / qwen-max / qwen-long

# 方式二：直接在 main.py 中修改默认值
```

> API Key 从 [DashScope 控制台](https://dashscope.console.aliyun.com/) 获取。未配置时 LLM 生成功能不可用，但知识库管理和检索功能仍可正常使用。

## 架构说明

```
rag-server/
├── main.py             API 接口层（FastAPI 路由）
│                       知识库 CRUD、文档上传、检索、对话、日志等接口
├── services.py         服务逻辑层
│                       TextExtractor    文本提取（PDF/Word/TXT）
│                       TextCleaner      文本清洗（去标签/脱敏/去重）
│                       TextChunker      滑动窗口文本切块
│                       VectorStore      ChromaDB 向量库操作
│                       MetadataManager  元数据 JSON 持久化
│                       Retriever        BM25 + Dense 混合检索
│                       Reranker         CrossEncoder 交叉编码重排
│                       LLMGenerator     通义千问 LLM 调用
│                       RAGPipeline      完整 RAG 流程编排
│                       ConversationManager  会话历史管理
│                       DecisionLogManager   决策日志管理
│                       LogExportService     日志 CSV 导出
├── models.py           数据模型层
│                       KnowledgeBase, Document, Chunk, Conversation,
│                       Message, Reference, RAGResult, DecisionLog 等
├── requirements.txt    Python 依赖清单
└── data/               运行时数据目录（自动创建）
    ├── chromadb/       ChromaDB 持久化存储
    ├── uploads/        上传文档暂存
    ├── metadata.json   知识库 & 文档元数据
    └── chat_history.json  对话历史记录
```

## API 接口概览

服务启动后可访问 `http://localhost:8765/docs` 查看完整的 Swagger 文档。主要接口：

### 知识库管理
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/rag/knowledge-bases` | 获取知识库列表 |
| POST | `/api/rag/knowledge-bases` | 创建知识库 |
| DELETE | `/api/rag/knowledge-bases/{kb_id}` | 删除知识库 |
| POST | `/api/rag/knowledge-bases/{kb_id}/upload` | 上传文档（PDF/Word/TXT） |
| GET | `/api/rag/knowledge-bases/{kb_id}/chunks` | 查询文档分块（含向量） |

### 语义检索 & 对话
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/rag/search` | 语义检索（返回相关片段） |
| POST | `/api/rag/chat` | RAG 对话（检索 + 生成） |
| GET | `/api/rag/conversations` | 获取会话历史 |

### 决策审计
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/rag/decision-logs` | 获取决策日志列表 |
| GET | `/api/rag/decision-logs/{log_id}` | 获取日志详情 |
| GET | `/api/rag/decision-logs/export` | 导出日志为 CSV |

## 与前端的连接

前端（ai-studio-front）通过 Vite 代理访问本服务：

```
前端 /rag-api/*  →  代理  →  http://localhost:8765/api/rag/*
```

只要 rag-server 在 8765 端口运行，前端 RAG 模块即可自动连接。未运行时前端会自动降级为 Mock 数据。

## 常见问题

**Q: 启动时模型下载很慢？**
A: 可设置 HuggingFace 镜像：`export HF_ENDPOINT=https://hf-mirror.com`

**Q: ChromaDB 报错 `sqlite3` 版本过低？**
A: 升级 Python 到 3.10+，或安装 `pip install pysqlite3-binary` 并在代码中 monkey-patch。

**Q: 内存不足？**
A: 两个模型合计约需 1GB 内存。如资源有限，可在 `main.py` 中禁用 CrossEncoder 重排。

**Q: 上传文档后搜索不到内容？**
A: 检查文档是否成功完成了"提取 → 清洗 → 切块 → 向量化"流水线，可在前端知识库详情页查看分块结果。
