/**
 * RAG调度决策 API
 **/
import request from "@/utils/request.js";
import axios from 'axios';

// ====== 通义千问 Qwen API（通过Vite代理转发） ======
const QWEN_SYSTEM_PROMPT = `你是一个专业的电力调度决策助手。你的职责是基于电力调度规程、故障处置手册等专业知识，为调度员提供故障研判、操作指导、负荷管理和应急处置等方面的决策建议。
请用Markdown格式回答，包含清晰的标题、分步骤说明和注意事项。回答要专业、准确、有条理。`;

// 可用的 Qwen 模型列表
export const QWEN_MODELS = [
    { value: 'qwen-turbo', label: 'Qwen-Turbo', desc: '快速响应，适合简单对话', maxTokens: 8000 },
    { value: 'qwen-plus', label: 'Qwen-Plus', desc: '平衡性能和成本（推荐）', maxTokens: 32000 },
    { value: 'qwen-max', label: 'Qwen-Max', desc: '最强性能，复杂推理', maxTokens: 8000 },
    { value: 'qwen-long', label: 'Qwen-Long', desc: '超长上下文（100万tokens）', maxTokens: 1000000 }
];

export async function callQwenApi(query, historyMessages = [], modelName = 'qwen-plus') {
    const apiKey = import.meta.env.VITE_QWEN_API_KEY;
    if (!apiKey) return null;

    // 构建消息上下文
    const messages = [{ role: 'system', content: QWEN_SYSTEM_PROMPT }];
    // 取最近6条历史消息作为上下文
    const recentHistory = historyMessages.slice(-6);
    for (const msg of recentHistory) {
        if (msg.role === 'user' || msg.role === 'assistant') {
            messages.push({ role: msg.role, content: msg.content });
        }
    }
    // 如果最后一条不是当前query，追加
    if (messages[messages.length - 1]?.content !== query) {
        messages.push({ role: 'user', content: query });
    }

    const res = await axios.post('/dashscope-api/compatible-mode/v1/chat/completions', {
        model: modelName,
        messages,
        temperature: 0.7,
        max_tokens: 2000
    }, {
        headers: {
            'Authorization': `Bearer ${apiKey}`,
            'Content-Type': 'application/json'
        },
        timeout: 30000
    });
    return res.data;
}

// 发送对话消息
export function sendChatMessage(data) {
    return request({
        method: 'post',
        url: 'production-line/rag-scheduler/chat',
        data
    })
}

// 获取对话历史
export function getChatHistory(params) {
    return request({
        method: 'get',
        url: 'production-line/rag-scheduler/chat/history',
        params
    })
}

// 获取决策审计日志
export function listDecisionLogs(params) {
    return request({
        method: 'get',
        url: 'production-line/rag-scheduler/decision-log/condition',
        params
    })
}

// 获取知识库列表
export function listKnowledgeBases(params) {
    return request({
        method: 'get',
        url: 'production-line/rag-scheduler/knowledge-base/list',
        params
    })
}

// 获取知识库文档列表
export function listDocuments(knowledgeId) {
    return request({
        method: 'get',
        url: 'production-line/rag-scheduler/knowledge-base/documents',
        params: { knowledgeId }
    })
}

// ====== RAG Python 后端 API（真实 ChromaDB） ======

// 检查 RAG 服务状态
export function checkRagStatus() {
    return axios.get('/rag-api/status', { timeout: 3000 });
}

// 真实知识库列表
export function listKnowledgeBasesReal() {
    return axios.get('/rag-api/knowledge-bases');
}

// 创建知识库
export function createKnowledgeBaseReal(name, description) {
    const formData = new FormData();
    formData.append('name', name);
    formData.append('description', description || '');
    return axios.post('/rag-api/knowledge-bases', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

// 删除知识库
export function deleteKnowledgeBaseReal(kbId) {
    return axios.delete(`/rag-api/knowledge-bases/${kbId}`);
}

// 真实文档列表
export function listDocumentsReal(kbId) {
    return axios.get(`/rag-api/knowledge-bases/${kbId}/documents`);
}

// 上传文档（真实：上传 → 提取 → 分块 → 向量化 → 入库 ChromaDB）
export function uploadDocumentReal(kbId, file, chunkSize = 512, chunkOverlap = 64) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('chunk_size', chunkSize);
    formData.append('chunk_overlap', chunkOverlap);
    return axios.post(`/rag-api/knowledge-bases/${kbId}/upload`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 120000,
    });
}

// 删除文档
export function deleteDocumentReal(docId) {
    return axios.delete(`/rag-api/documents/${docId}`);
}

// 获取文档 Chunks（含向量和元数据）
export function getDocumentChunksReal(docId, page = 1, pageSize = 20) {
    return axios.get(`/rag-api/documents/${docId}/chunks`, {
        params: { page, page_size: pageSize }
    });
}

// 语义检索
export function searchKnowledgeReal(query, kbId, topK = 5) {
    const formData = new FormData();
    formData.append('query', query);
    if (kbId) formData.append('kb_id', kbId);
    formData.append('top_k', topK);
    return axios.post('/rag-api/search', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

// 直接入库文本（流水线用）
export function ingestTextReal(kbId, text, docName, chunkSize = 512, chunkOverlap = 64) {
    const formData = new FormData();
    formData.append('text', text);
    formData.append('doc_name', docName);
    formData.append('chunk_size', chunkSize);
    formData.append('chunk_overlap', chunkOverlap);
    return axios.post(`/rag-api/knowledge-bases/${kbId}/ingest-text`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

// ====== RAG 对话（真实：检索+重排+LLM生成） ======
export function ragChat(query, conversationId = null, kbId = null, model = null) {
    const formData = new FormData();
    formData.append('query', query);
    if (conversationId) formData.append('conversation_id', conversationId);
    if (kbId) formData.append('kb_id', kbId);
    if (model) formData.append('model', model);
    return axios.post('/rag-api/chat', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 120000
    });
}

// ====== 对话历史持久化 ======
export function getChatHistoryReal() {
    return axios.get('/rag-api/chat/history');
}

export function getConversationReal(conversationId) {
    return axios.get(`/rag-api/chat/history/${conversationId}`);
}

export function deleteConversationReal(conversationId) {
    return axios.delete(`/rag-api/chat/history/${conversationId}`);
}

// ====== 数据清洗 API ======
export function cleanTextReal(text, rules) {
    const formData = new FormData();
    formData.append('text', text);
    formData.append('rules', JSON.stringify(rules));
    return axios.post('/rag-api/clean-text', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
}

export function cleanFileReal(file, rules) {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('rules', JSON.stringify(rules));
    return axios.post('/rag-api/clean-file', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        timeout: 60000,
    });
}


