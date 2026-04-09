# RAG Knowledge Base Server

基于 FastAPI + ChromaDB 的知识库管理后端服务，提供真实的文档上传、文本提取、分块、向量化、存储和检索功能。

## 安装步骤

### 1. 创建 Python 虚拟环境（推荐）

```bash
cd rag-server
python -m venv venv

# Windows:
venv\Scripts\activate

# macOS/Linux:
source venv/bin/activate
```

### 2. 安装依赖

```bash
pip install -r requirements.txt
```

> ChromaDB 会自动下载默认的 Embedding 模型（all-MiniLM-L6-v2，约 80MB），首次启动时需要联网。

### 3. 启动服务

```bash
python main.py
```

服务默认运行在 `http://localhost:8765`

### 4. 验证服务

浏览器访问 http://localhost:8765/api/rag/status ，看到如下 JSON 即为成功：

```json
{
  "code": 200,
  "data": {
    "status": "running",
    "chromadb": "connected",
    "knowledge_bases": 0,
    "documents": 0,
    "total_chunks": 0
  }
}
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/rag/status | 服务状态 |
| GET | /api/rag/knowledge-bases | 知识库列表 |
| POST | /api/rag/knowledge-bases | 创建知识库 |
| DELETE | /api/rag/knowledge-bases/{kb_id} | 删除知识库 |
| GET | /api/rag/knowledge-bases/{kb_id}/documents | 文档列表 |
| POST | /api/rag/knowledge-bases/{kb_id}/upload | 上传文档 |
| DELETE | /api/rag/documents/{doc_id} | 删除文档 |
| GET | /api/rag/documents/{doc_id}/chunks | 查看 Chunks（含向量） |
| POST | /api/rag/search | 语义检索 |
| POST | /api/rag/knowledge-bases/{kb_id}/ingest-text | 直接入库文本 |

## 数据存储

- `data/chromadb/` - ChromaDB 向量数据库文件
- `data/uploads/` - 上传的原始文档
- `data/metadata.json` - 知识库和文档元数据
