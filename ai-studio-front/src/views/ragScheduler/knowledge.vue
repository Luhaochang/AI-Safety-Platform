<script setup>
import {
    BookOutlined, PlusOutlined, SearchOutlined, FileTextOutlined, DeleteOutlined,
    CloudUploadOutlined, DatabaseOutlined, FileWordOutlined, FilePdfOutlined,
    FileMarkdownOutlined, EyeOutlined, ReloadOutlined, InfoCircleOutlined,
    ScissorOutlined, CheckCircleOutlined
} from '@ant-design/icons-vue';
import {
    listKnowledgeBasesReal, createKnowledgeBaseReal,
    deleteKnowledgeBaseReal, listDocumentsReal, uploadDocumentReal,
    deleteDocumentReal, getDocumentChunksReal, searchKnowledgeReal
} from '@/api/ragScheduler/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';

const state = reactive({
    knowledgeBases: [],
    loading: false,
    selectedKb: null,
    documents: [],
    docLoading: false,
    activeTab: 'documents',
    createDialogVisible: false,
    uploadDialogVisible: false,
    createForm: { name: '', description: '' },
    uploadFileList: [],
    uploadLoading: false,
    chunkSize: 512,
    chunkOverlap: 64,
    chunkDialogVisible: false,
    chunkDialogDoc: null,
    chunkData: { chunks: [], total: 0, page: 1, pageSize: 10 },
    chunkLoading: false,
    searchQuery: '',
    searchResults: [],
    searchLoading: false,
});

const getFileIcon = (name) => {
    if (!name) return FileTextOutlined;
    if (name.endsWith('.pdf')) return FilePdfOutlined;
    if (name.endsWith('.md')) return FileMarkdownOutlined;
    if (name.endsWith('.docx') || name.endsWith('.doc')) return FileWordOutlined;
    return FileTextOutlined;
};

// ====== 知识库列表 ======
const loadKnowledgeBases = async () => {
    state.loading = true;
    try {
        const res = await listKnowledgeBasesReal();
        if (res.data?.code === 200) {
            state.knowledgeBases = res.data.data || [];
        }
        if (state.knowledgeBases.length > 0 && !state.selectedKb) {
            selectKb(state.knowledgeBases[0]);
        }
    } catch (e) {
        console.error('加载知识库失败:', e);
        ElMessage.error('加载知识库失败，请确认 RAG 服务已启动');
    } finally {
        state.loading = false;
    }
};

// ====== 选择知识库 → 加载文档 ======
const selectKb = async (kb) => {
    state.selectedKb = kb;
    state.activeTab = 'documents';
    state.docLoading = true;
    try {
        const res = await listDocumentsReal(kb.id);
        if (res.data?.code === 200) {
            state.documents = res.data.data || [];
        }
    } catch {
        state.documents = [];
    } finally {
        state.docLoading = false;
    }
};

// ====== 创建知识库 ======
const handleCreateKb = async () => {
    if (!state.createForm.name.trim()) { ElMessage.warning('请输入知识库名称'); return; }
    try {
        const res = await createKnowledgeBaseReal(state.createForm.name, state.createForm.description);
        if (res.data?.code === 200) {
            ElMessage.success('知识库创建成功');
            state.createDialogVisible = false;
            state.createForm = { name: '', description: '' };
            state.selectedKb = null;
            await loadKnowledgeBases();
        }
    } catch (e) {
        const detail = e.response?.data?.detail;
        const msg = Array.isArray(detail) ? detail.map(d => d.msg || d).join('; ') : (detail || e.message || '未知错误');
        ElMessage.error('创建失败: ' + msg);
    }
};

// ====== 删除知识库 ======
const handleDeleteKb = (kb) => {
    ElMessageBox.confirm(`确认删除知识库「${kb.name}」及其所有文档？此操作不可恢复！`, '危险操作', {
        type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消'
    }).then(async () => {
        try {
            await deleteKnowledgeBaseReal(kb.id);
            ElMessage.success('知识库已删除');
            if (state.selectedKb?.id === kb.id) state.selectedKb = null;
            await loadKnowledgeBases();
        } catch (e) {
            const detail = e.response?.data?.detail;
            const msg = Array.isArray(detail) ? detail.map(d => d.msg || d).join('; ') : (JSON.stringify(detail) || e.message || '未知错误');
            ElMessage.error('删除失败: ' + msg);
        }
    }).catch(() => {});
};

// ====== 上传文档 ======
const handleUpload = async () => {
    if (!state.selectedKb) { ElMessage.warning('请先选择知识库'); return; }
    if (state.uploadFileList.length === 0) { ElMessage.warning('请选择文件'); return; }

    state.uploadLoading = true;
    let successCount = 0;

    for (const fileItem of state.uploadFileList) {
        const file = fileItem.raw || fileItem.originFileObj || fileItem;
        try {
            const res = await uploadDocumentReal(state.selectedKb.id, file, state.chunkSize, state.chunkOverlap);
            if (res.data?.code === 200) {
                successCount++;
                ElMessage.success(`${file.name}: ${res.data.message}`);
            } else {
                ElMessage.error(`${file.name}: 上传失败`);
            }
        } catch (e) {
            ElMessage.error(`${file.name}: ${e.response?.data?.detail || e.message}`);
        }
    }

    state.uploadLoading = false;
    state.uploadDialogVisible = false;
    state.uploadFileList = [];

    if (successCount > 0) {
        await loadKnowledgeBases();
        if (state.selectedKb) await selectKb(state.selectedKb);
    }
};

const handleBeforeUpload = (file) => {
    state.uploadFileList.push(file);
    return false;
};

const handleRemoveFile = (file) => {
    const idx = state.uploadFileList.indexOf(file);
    if (idx > -1) state.uploadFileList.splice(idx, 1);
};

// ====== 删除文档 ======
const handleDeleteDoc = (doc) => {
    ElMessageBox.confirm(`确认删除文档「${doc.name}」？将同时删除其所有 Chunk 和向量。`, '提示', {
        type: 'warning'
    }).then(async () => {
        try {
            await deleteDocumentReal(doc.id);
            ElMessage.success('文档已删除');
            if (state.selectedKb) {
                await loadKnowledgeBases();
                await selectKb(state.selectedKb);
            }
        } catch (e) {
            ElMessage.error('删除失败: ' + (e.response?.data?.detail || e.message));
        }
    }).catch(() => {});
};

// ====== 查看 Chunk 详情 ======
const viewChunks = async (doc) => {
    state.chunkDialogDoc = doc;
    state.chunkDialogVisible = true;
    state.chunkData = { chunks: [], total: 0, page: 1, pageSize: 10 };
    await loadChunks(1);
};

const loadChunks = async (page) => {
    state.chunkLoading = true;
    try {
        const res = await getDocumentChunksReal(state.chunkDialogDoc.id, page, state.chunkData.pageSize);
        if (res.data?.code === 200) {
            state.chunkData = res.data.data;
        }
    } catch (e) {
        ElMessage.error('加载 Chunk 失败: ' + e.message);
    } finally {
        state.chunkLoading = false;
    }
};

// ====== 语义搜索 ======
const handleSearch = async () => {
    if (!state.searchQuery.trim()) return;
    state.searchLoading = true;
    try {
        const res = await searchKnowledgeReal(state.searchQuery, state.selectedKb?.id || null, 5);
        if (res.data?.code === 200) {
            state.searchResults = res.data.data || [];
        }
    } catch (e) {
        ElMessage.error('搜索失败: ' + e.message);
    } finally {
        state.searchLoading = false;
    }
};

const formatVector = (arr) => {
    if (!arr || arr.length === 0) return '[]';
    return '[' + arr.map(v => typeof v === 'number' ? v.toFixed(4) : v).join(', ') + ', ...]';
};

onMounted(async () => {
    await loadKnowledgeBases();
});
</script>

<template>
    <div class="app-container flex flex-col h-[calc(100vh-84px)] bg-[#f5f7fa]">
        <div class="flex flex-1 overflow-hidden">
        <!-- 左侧: 知识库列表 -->
        <div class="w-80 bg-white border-r border-gray-200 flex flex-col flex-shrink-0">
            <div class="p-4 border-b border-gray-100">
                <div class="flex items-center justify-between mb-1">
                    <h3 class="font-bold text-gray-800 flex items-center gap-2">
                        <BookOutlined class="text-blue-500" /> 知识库
                    </h3>
                    <a-button type="primary" size="small" @click="state.createDialogVisible = true">
                        <PlusOutlined /> 新建
                    </a-button>
                </div>
            </div>
            <div class="flex-1 overflow-auto p-3 space-y-2">
                <div v-if="state.loading" class="flex justify-center py-8"><a-spin /></div>
                <div v-if="!state.loading && state.knowledgeBases.length === 0" class="text-center text-gray-400 text-sm py-8">
                    暂无知识库，请点击「新建」创建
                </div>
                <div v-for="kb in state.knowledgeBases" :key="kb.id"
                     class="rounded-xl border p-4 cursor-pointer transition-all hover:shadow-md group relative"
                     :class="state.selectedKb?.id === kb.id ? 'border-blue-400 bg-blue-50/50 shadow-sm' : 'border-gray-200 bg-white hover:border-blue-200'"
                     @click="selectKb(kb)">
                    <div class="flex items-center gap-3 mb-2">
                        <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                             :class="state.selectedKb?.id === kb.id ? 'bg-blue-100' : 'bg-gray-100'">
                            <DatabaseOutlined class="text-lg" :class="state.selectedKb?.id === kb.id ? 'text-blue-500' : 'text-gray-400'" />
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="font-medium text-gray-800 text-sm truncate">{{ kb.name }}</div>
                            <div class="text-xs text-gray-400">{{ kb.documentCount }} 篇文档 · {{ kb.chunkCount || 0 }} Chunks</div>
                        </div>
                        <a-button type="text" size="small" danger
                                  class="opacity-0 group-hover:opacity-100 transition-opacity"
                                  @click.stop="handleDeleteKb(kb)">
                            <DeleteOutlined />
                        </a-button>
                    </div>
                    <p class="text-xs text-gray-500 leading-relaxed line-clamp-2">{{ kb.description || '暂无描述' }}</p>
                    <div class="flex items-center justify-between mt-2 pt-2 border-t border-gray-100">
                        <span class="text-xs text-gray-400">{{ ((kb.wordCount || 0) / 1000).toFixed(1) }}K 字</span>
                        <span class="text-xs text-gray-400">{{ kb.updateTime || kb.createTime }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 右侧: 知识库详情 -->
        <div class="flex-1 flex flex-col overflow-hidden">
            <template v-if="state.selectedKb">
                <!-- 知识库头部 -->
                <div class="bg-white px-6 py-4 border-b border-gray-100 flex-shrink-0">
                    <div class="flex items-center justify-between">
                        <div>
                            <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
                                <DatabaseOutlined class="text-blue-500" />
                                {{ state.selectedKb.name }}
                            </h2>
                            <p class="text-sm text-gray-500 mt-1">{{ state.selectedKb.description || '暂无描述' }}</p>
                        </div>
                        <div class="flex items-center gap-2">
                            <a-button @click="selectKb(state.selectedKb)"><ReloadOutlined /> 刷新</a-button>
                            <a-button type="primary" @click="state.uploadDialogVisible = true; state.uploadFileList = []">
                                <CloudUploadOutlined /> 上传文档
                            </a-button>
                        </div>
                    </div>
                    <div class="flex items-center gap-6 mt-3">
                        <div class="flex items-center gap-2 text-sm">
                            <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center"><FileTextOutlined class="text-blue-500" /></div>
                            <div><div class="text-gray-400 text-xs">文档数</div><div class="font-bold text-gray-800">{{ state.selectedKb.documentCount }}</div></div>
                        </div>
                        <div class="flex items-center gap-2 text-sm">
                            <div class="w-8 h-8 rounded-lg bg-green-50 flex items-center justify-center"><DatabaseOutlined class="text-green-500" /></div>
                            <div><div class="text-gray-400 text-xs">总字数</div><div class="font-bold text-gray-800">{{ ((state.selectedKb.wordCount || 0) / 1000).toFixed(1) }}K</div></div>
                        </div>
                        <div class="flex items-center gap-2 text-sm">
                            <div class="w-8 h-8 rounded-lg bg-purple-50 flex items-center justify-center"><ScissorOutlined class="text-purple-500" /></div>
                            <div><div class="text-gray-400 text-xs">Chunks</div><div class="font-bold text-gray-800">{{ state.selectedKb.chunkCount || 0 }}</div></div>
                        </div>
                    </div>
                </div>

                <!-- Tab切换 -->
                <div class="bg-white border-b border-gray-100 px-6 flex-shrink-0">
                    <a-tabs v-model:activeKey="state.activeTab" size="small">
                        <a-tab-pane key="documents" tab="文档列表" />
                        <a-tab-pane key="search" tab="语义检索" />
                    </a-tabs>
                </div>

                <!-- Tab: 文档列表 -->
                <div class="flex-1 overflow-auto p-6" v-if="state.activeTab === 'documents'">
                    <div v-if="state.docLoading" class="flex justify-center py-12"><a-spin size="large" tip="加载文档中..." /></div>
                    <div v-else-if="state.documents.length === 0" class="flex flex-col items-center justify-center py-16 text-gray-400">
                        <CloudUploadOutlined class="text-5xl mb-3 text-gray-300" />
                        <div class="text-sm mb-2">该知识库暂无文档</div>
                        <a-button type="primary" size="small" @click="state.uploadDialogVisible = true; state.uploadFileList = []">
                            <CloudUploadOutlined /> 上传第一份文档
                        </a-button>
                    </div>
                    <div v-else class="space-y-3">
                        <div v-for="doc in state.documents" :key="doc.id"
                             class="bg-white rounded-xl border border-gray-200 p-4 hover:shadow-md transition-all flex items-center gap-4">
                            <div class="w-12 h-12 rounded-xl bg-gray-50 flex items-center justify-center flex-shrink-0">
                                <component :is="getFileIcon(doc.name)" class="text-2xl text-blue-400" />
                            </div>
                            <div class="flex-1 min-w-0">
                                <div class="font-medium text-gray-800 truncate">{{ doc.name }}</div>
                                <div class="flex items-center gap-3 mt-1 text-xs text-gray-400">
                                    <span>{{ doc.size }}</span><span>|</span>
                                    <span>{{ doc.chunks }} 个 Chunk</span><span>|</span>
                                    <span>{{ ((doc.wordCount || 0) / 1000).toFixed(1) }}K 字</span><span>|</span>
                                    <span>{{ doc.createTime }}</span>
                                </div>
                            </div>
                            <a-tag :color="doc.status === '已索引' ? 'green' : 'orange'">{{ doc.status }}</a-tag>
                            <div class="flex items-center gap-1">
                                <a-button type="link" size="small" @click="viewChunks(doc)">
                                    <EyeOutlined /> 查看Chunks
                                </a-button>
                                <a-button type="text" size="small" danger @click="handleDeleteDoc(doc)">
                                    <DeleteOutlined />
                                </a-button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Tab: 语义检索 -->
                <div class="flex-1 overflow-auto p-6" v-if="state.activeTab === 'search'">
                    <div class="max-w-3xl mx-auto">
                        <div class="bg-white rounded-xl border border-gray-200 p-5 mb-5">
                            <h3 class="font-bold text-gray-800 mb-3 flex items-center gap-2">
                                <SearchOutlined class="text-blue-500" /> 语义检索
                            </h3>
                            <div class="flex gap-2">
                                <a-input v-model:value="state.searchQuery" placeholder="输入查询内容，如：变压器过负荷如何处理？"
                                         @pressEnter="handleSearch" size="large" />
                                <a-button type="primary" size="large" :loading="state.searchLoading" @click="handleSearch">
                                    <SearchOutlined /> 检索
                                </a-button>
                            </div>
                            <div class="text-xs text-gray-400 mt-2">
                                基于向量相似度在「{{ state.selectedKb.name }}」中检索最相关的文档片段
                            </div>
                        </div>

                        <div v-if="state.searchResults.length > 0" class="space-y-3">
                            <div v-for="(result, idx) in state.searchResults" :key="result.id"
                                 class="bg-white rounded-xl border border-gray-200 p-4">
                                <div class="flex items-center justify-between mb-2">
                                    <div class="flex items-center gap-2">
                                        <a-tag color="blue">Top {{ idx + 1 }}</a-tag>
                                        <span class="text-xs text-gray-500">{{ result.metadata?.doc_name }}</span>
                                        <a-tag size="small">Chunk #{{ result.metadata?.chunk_index }}</a-tag>
                                    </div>
                                    <a-tag color="green">相似度 {{ (1 - result.distance).toFixed(4) }}</a-tag>
                                </div>
                                <div class="text-sm text-gray-700 bg-gray-50 rounded-lg p-3 leading-relaxed">
                                    {{ result.text }}
                                </div>
                                <div class="mt-2 text-xs text-gray-400">
                                    距离: {{ result.distance?.toFixed(4) }} | 字符数: {{ result.metadata?.char_count }}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </template>

            <!-- 未选择知识库 -->
            <div v-else class="flex-1 flex flex-col items-center justify-center text-gray-400">
                <BookOutlined class="text-6xl mb-4 text-gray-300" />
                <div class="text-lg mb-1">选择一个知识库</div>
                <div class="text-sm">从左侧列表选择知识库查看文档，或创建新的知识库</div>
            </div>
        </div>
        </div>

        <!-- 创建知识库对话框 -->
        <a-modal v-model:open="state.createDialogVisible" title="创建知识库" @ok="handleCreateKb" width="480px">
            <a-form :model="state.createForm" layout="vertical" class="mt-2">
                <a-form-item label="知识库名称" required>
                    <a-input v-model:value="state.createForm.name" placeholder="如：220kV线路故障处置规程库" />
                </a-form-item>
                <a-form-item label="描述">
                    <a-textarea v-model:value="state.createForm.description" placeholder="描述知识库的内容和用途" :rows="3" />
                </a-form-item>
            </a-form>
        </a-modal>

        <!-- 上传文档对话框（真实上传 → 提取 → 分块 → 向量化 → 入库） -->
        <a-modal v-model:open="state.uploadDialogVisible" title="上传文档到知识库" width="580px"
                 :footer="null" :maskClosable="false">
            <div class="mt-2">
                <a-upload-dragger name="file" :multiple="true" :fileList="state.uploadFileList"
                                  :beforeUpload="handleBeforeUpload" @remove="handleRemoveFile"
                                  :disabled="state.uploadLoading">
                    <p class="text-4xl text-blue-400 mb-2"><CloudUploadOutlined /></p>
                    <p class="text-gray-700 font-medium">点击或拖拽文件到此区域</p>
                    <p class="text-gray-400 text-xs mt-1">支持 PDF、Markdown、Word、TXT 格式</p>
                </a-upload-dragger>

                <div class="mt-4 grid grid-cols-2 gap-3">
                    <div>
                        <div class="text-xs text-gray-600 mb-1">Chunk 大小（字符）</div>
                        <a-input-number v-model:value="state.chunkSize" :min="100" :max="2000" style="width:100%" />
                    </div>
                    <div>
                        <div class="text-xs text-gray-600 mb-1">重叠字符数</div>
                        <a-input-number v-model:value="state.chunkOverlap" :min="0" :max="200" style="width:100%" />
                    </div>
                </div>

                <div class="mt-3 p-3 bg-blue-50 rounded-lg text-xs text-blue-700">
                    <InfoCircleOutlined /> 上传流程：文件上传 → 文本提取 → 按 {{ state.chunkSize }} 字符分块（重叠 {{ state.chunkOverlap }}） → Embedding 向量化 → 存入 ChromaDB
                </div>

                <div class="mt-4 flex justify-end gap-2">
                    <a-button @click="state.uploadDialogVisible = false" :disabled="state.uploadLoading">取消</a-button>
                    <a-button type="primary" @click="handleUpload"
                              :loading="state.uploadLoading"
                              :disabled="state.uploadFileList.length === 0">
                        <CloudUploadOutlined /> {{ state.uploadLoading ? '处理中...' : '上传并入库' }}
                    </a-button>
                </div>
            </div>
        </a-modal>

        <!-- Chunk 详情对话框（含向量和元数据） -->
        <a-modal v-model:open="state.chunkDialogVisible"
                 :title="'文档 Chunks — ' + (state.chunkDialogDoc?.name || '')"
                 width="900px" :footer="null">
            <div v-if="state.chunkLoading" class="flex justify-center py-8"><a-spin tip="加载 Chunk 数据..." /></div>
            <div v-else>
                <div class="mb-3 text-sm text-gray-500">
                    共 <b class="text-gray-800">{{ state.chunkData.total }}</b> 个 Chunk，
                    当前第 {{ state.chunkData.page }} 页
                </div>
                <div class="space-y-3 max-h-[500px] overflow-auto">
                    <div v-for="chunk in state.chunkData.chunks" :key="chunk.id"
                         class="border border-gray-200 rounded-xl p-4">
                        <!-- 头部：ID + 元数据 -->
                        <div class="flex items-center justify-between mb-2">
                            <div class="flex items-center gap-2">
                                <a-tag color="purple">Chunk #{{ chunk.metadata?.chunk_index }}</a-tag>
                                <span class="text-xs text-gray-400">{{ chunk.metadata?.char_count }} 字符</span>
                                <span class="text-xs text-gray-400">位置 [{{ chunk.metadata?.start }}-{{ chunk.metadata?.end }}]</span>
                            </div>
                            <a-tag color="cyan">{{ chunk.embedding_dim }}维向量</a-tag>
                        </div>
                        <!-- 文本内容 -->
                        <div class="text-sm text-gray-700 bg-gray-50 rounded-lg p-3 leading-relaxed mb-2 max-h-[100px] overflow-auto">
                            {{ chunk.text }}
                        </div>
                        <!-- 向量预览 -->
                        <div class="bg-purple-50 rounded-lg p-2">
                            <div class="text-xs text-purple-600 font-bold mb-1">Embedding 向量预览（前8维 / 共{{ chunk.embedding_dim }}维）：</div>
                            <code class="text-xs text-purple-800 break-all">{{ formatVector(chunk.embedding_preview) }}</code>
                        </div>
                        <!-- 元数据 -->
                        <div class="mt-2 flex flex-wrap gap-2">
                            <a-tag v-for="(val, key) in chunk.metadata" :key="key" size="small" color="default">
                                {{ key }}: {{ val }}
                            </a-tag>
                        </div>
                    </div>
                </div>
                <!-- 分页 -->
                <div v-if="state.chunkData.total > state.chunkData.pageSize" class="mt-4 flex justify-center">
                    <a-pagination size="small"
                                  :current="state.chunkData.page"
                                  :pageSize="state.chunkData.pageSize"
                                  :total="state.chunkData.total"
                                  @change="loadChunks" />
                </div>
            </div>
        </a-modal>
    </div>
</template>
