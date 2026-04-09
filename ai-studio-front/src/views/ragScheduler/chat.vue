<script setup>
import {
    SendOutlined,
    RobotOutlined,
    UserOutlined,
    BookOutlined,
    HistoryOutlined,
    DeleteOutlined,
    PlusOutlined,
    ThunderboltOutlined,
    FileTextOutlined,
    CopyOutlined,
    ReloadOutlined,
    BulbOutlined,
    ClusterOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined
} from '@ant-design/icons-vue';
import { ragChat, checkRagStatus, callQwenApi } from '@/api/ragScheduler/index.js';
import { useChatStore } from '@/store/modules/chat.js';
import { ElMessage, ElMessageBox } from 'element-plus';

const chatStore = useChatStore();
const inputRef = ref(null);
const chatContainerRef = ref(null);

const QWEN_MODELS = [
    { value: 'qwen-turbo', label: 'Qwen-Turbo', desc: '快速响应' },
    { value: 'qwen-plus',  label: 'Qwen-Plus',  desc: '平衡推荐' },
    { value: 'qwen-max',   label: 'Qwen-Max',   desc: '最强推理' },
    { value: 'qwen-long',  label: 'Qwen-Long',  desc: '超长上下文' },
];

const state = reactive({
    inputText: '',
    loading: false,
    showSuggestions: true,
    ragConnected: false,
    qwenConnected: false,
    qwenModel: 'qwen-plus',
});

const currentMessages = computed(() => {
    const conv = chatStore.currentConversation;
    return conv ? conv.messages : [];
});

const suggestions = [
    { icon: ThunderboltOutlined, text: '220kV宝华线发生单相接地故障，如何进行故障隔离与恢复供电？', color: '#1677ff' },
    { icon: ClusterOutlined, text: '主变压器过负荷运行，请给出紧急限负荷调度方案', color: '#faad14' },
    { icon: BulbOutlined, text: '系统频率持续偏低0.2Hz，如何进行一次调频和二次调频操作？', color: '#52c41a' },
    { icon: FileTextOutlined, text: '节假日用电高峰期如何制定负荷调度计划与备用方案？', color: '#ff4d4f' }
];

// 检查 RAG 服务状态
const checkConnection = async () => {
    try {
        const res = await checkRagStatus();
        if (res.data?.code === 200) {
            state.ragConnected = true;
            state.qwenConnected = res.data.data?.qwen === 'connected';
        }
    } catch { state.ragConnected = false; state.qwenConnected = false; }
};

const sendMessage = async () => {
    const text = state.inputText.trim();
    if (!text || state.loading) return;

    state.showSuggestions = false;
    state.inputText = '';
    state.loading = true;

    // 如果当前没有对话，先创建（后端会自动创建持久化记录）
    let convId = chatStore.currentConversationId;
    if (!convId) {
        convId = 'conv_' + Date.now();
        chatStore.createConversation(convId);
    }

    // 先在本地添加用户消息
    const userMsg = { role: 'user', content: text, time: new Date().toLocaleTimeString() };
    chatStore.addMessage(convId, userMsg);
    scrollToBottom();

    try {
        const startTime = Date.now();
        let assistantContent = null;
        let references = [];

        if (state.ragConnected) {
            // RAG 模式：检索知识库 → CrossEncoder重排 → Qwen生成
            const res = await ragChat(text, convId, null, state.qwenModel).catch(() => null);
            if (res?.data?.code === 200) {
                assistantContent = res.data.data.answer;
                references = res.data.data.references || [];
                // 后端已持久化，刷新对话历史
                await chatStore.loadHistory();
                chatStore.selectConversation(res.data.data.conversation_id || convId);
                scrollToBottom();
                return;
            }
        }

        if (!assistantContent) {
            // Fallback: 直接调用 Qwen API（RAG服务未连接时）
            const qwenRes = await callQwenApi(text, currentMessages.value, state.qwenModel).catch(() => null);
            if (qwenRes?.choices?.[0]?.message?.content) {
                assistantContent = qwenRes.choices[0].message.content;
            }
        }

        if (!assistantContent) {
            assistantContent = '抱歉，当前 RAG 服务或 Qwen API 均不可用，请检查后端是否启动。';
        }

        const duration = ((Date.now() - startTime) / 1000).toFixed(1) + 's';
        chatStore.addMessage(convId, {
            role: 'assistant', content: assistantContent,
            references, time: new Date().toLocaleTimeString()
        });
        chatStore.updateConversationMeta(convId, { duration });
    } catch (e) {
        chatStore.addMessage(convId, {
            role: 'assistant', content: '抱歉，处理请求时出现错误：' + (e.message || '未知错误'),
            time: new Date().toLocaleTimeString()
        });
    } finally {
        state.loading = false;
        scrollToBottom();
    }
};

const useSuggestion = (text) => {
    state.inputText = text;
    sendMessage();
};

const scrollToBottom = () => {
    nextTick(() => {
        if (chatContainerRef.value) {
            chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight;
        }
    });
};

const selectHistory = (conv) => {
    chatStore.selectConversation(conv.id);
    state.showSuggestions = false;
    nextTick(() => scrollToBottom());
};

const startNewChat = () => {
    chatStore.selectConversation(null);
    state.showSuggestions = true;
};

const handleDeleteConv = (conv) => {
    ElMessageBox.confirm(`确认删除对话「${conv.title}」？`, '提示', { type: 'warning' })
        .then(() => chatStore.deleteConversation(conv.id))
        .catch(() => {});
};

const copyMessage = (content) => {
    navigator.clipboard.writeText(content).then(() => ElMessage.success('已复制'));
};

const renderMarkdown = (text) => {
    if (!text) return '';
    return text
        .replace(/### (.*)/g, '<h4 class="text-sm font-bold text-gray-800 mt-3 mb-1">$1</h4>')
        .replace(/## (.*)/g, '<h3 class="text-base font-bold text-gray-800 mt-4 mb-2">$1</h3>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/`(.*?)`/g, '<code class="bg-gray-100 px-1.5 py-0.5 rounded text-xs text-red-600 font-mono">$1</code>')
        .replace(/^- (.*)/gm, '<li class="ml-4 text-sm text-gray-700 leading-relaxed">$1</li>')
        .replace(/^(\d+)\. (.*)/gm, '<li class="ml-4 text-sm text-gray-700 leading-relaxed"><span class="font-medium text-blue-600">$1.</span> $2</li>')
        .replace(/\n\n/g, '<br/>')
        .replace(/> (.*)/g, '<blockquote class="border-l-3 border-blue-400 pl-3 py-1 my-2 text-sm text-gray-500 bg-blue-50/50 rounded-r">$1</blockquote>');
};

onMounted(async () => {
    await checkConnection();
    await chatStore.loadHistory();
});
</script>

<template>
    <div class="app-container flex h-[calc(100vh-84px)] bg-[#f5f7fa]">
        <!-- 左侧: 历史对话列表 -->
        <div class="w-72 bg-white border-r border-gray-200 flex flex-col">
            <div class="p-4 border-b border-gray-100">
                <a-button type="primary" block size="large" @click="startNewChat">
                    <PlusOutlined /> 新建对话
                </a-button>
            </div>
            <div class="flex-1 overflow-auto p-3">
                <div class="text-xs text-gray-400 mb-2 px-1 flex items-center gap-1">
                    <HistoryOutlined /> 历史对话（持久化保存）
                </div>
                <div v-if="chatStore.sortedConversations.length === 0" class="text-center text-gray-400 text-xs py-6">
                    暂无对话记录
                </div>
                <div class="space-y-1">
                    <div v-for="item in chatStore.sortedConversations" :key="item.id"
                         class="rounded-lg px-3 py-2.5 cursor-pointer hover:bg-gray-50 transition-colors group"
                         :class="{ 'bg-blue-50 border border-blue-200': chatStore.currentConversationId === item.id, 'border border-transparent': chatStore.currentConversationId !== item.id }"
                         @click="selectHistory(item)">
                        <div class="flex items-center justify-between">
                            <div class="text-sm font-medium text-gray-700 truncate flex-1">{{ item.title }}</div>
                            <a-button type="text" size="small" danger
                                      class="opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0 ml-1"
                                      @click.stop="handleDeleteConv(item)">
                                <DeleteOutlined class="text-xs" />
                            </a-button>
                        </div>
                        <div class="text-xs text-gray-400 truncate mt-1">{{ item.messages?.[item.messages.length - 1]?.content?.substring(0, 30) || '' }}</div>
                        <div class="flex items-center justify-between mt-1">
                            <span class="text-xs text-gray-300">{{ (item.updateTime || item.createTime || '').substring(5, 16) }}</span>
                            <span class="text-xs text-gray-400">{{ item.messages?.length || 0 }}条</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 右侧: 对话区 -->
        <div class="flex-1 flex flex-col">
            <!-- 对话头部 -->
            <div class="bg-white px-6 py-4 border-b border-gray-100 flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                        <RobotOutlined class="text-white text-lg" />
                    </div>
                    <div>
                        <div class="font-bold text-gray-800">AI调度决策助手</div>
                        <div class="text-xs text-gray-400">基于RAG知识库的智能调度决策引擎（Hybrid检索 + CrossEncoder重排 + LLM）</div>
                    </div>
                </div>
                <div class="flex items-center gap-2">
                    <a-tag :color="state.ragConnected ? 'green' : 'red'">
                        <CheckCircleOutlined v-if="state.ragConnected" />
                        <ExclamationCircleOutlined v-else />
                        {{ state.ragConnected ? 'RAG 已连接' : 'RAG 未连接' }}
                    </a-tag>
                    <a-tag :color="state.qwenConnected ? 'blue' : 'orange'">
                        <RobotOutlined />
                        {{ state.qwenConnected ? 'Qwen 已连接' : 'Qwen 未连接' }}
                    </a-tag>
                    <!-- Qwen 模型选择 -->
                    <a-dropdown :trigger="['click']">
                        <a-button size="small">
                            {{ QWEN_MODELS.find(m => m.value === state.qwenModel)?.label || 'Qwen-Plus' }}
                            <span class="ml-1 text-gray-400">▾</span>
                        </a-button>
                        <template #overlay>
                            <a-menu @click="({key}) => state.qwenModel = key">
                                <a-menu-item v-for="m in QWEN_MODELS" :key="m.value">
                                    <span class="font-medium">{{ m.label }}</span>
                                    <span class="text-gray-400 text-xs ml-2">{{ m.desc }}</span>
                                </a-menu-item>
                            </a-menu>
                        </template>
                    </a-dropdown>
                </div>
            </div>

            <!-- 消息区 -->
            <div ref="chatContainerRef" class="flex-1 overflow-auto p-6 space-y-6">
                <!-- 欢迎 & 建议 -->
                <div v-if="state.showSuggestions && currentMessages.length === 0" class="flex flex-col items-center justify-center h-full">
                    <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center mb-6 shadow-lg">
                        <RobotOutlined class="text-white text-4xl" />
                    </div>
                    <h2 class="text-xl font-bold text-gray-800 mb-2">AI调度决策助手</h2>
                    <p class="text-gray-500 text-sm mb-8 text-center max-w-md">
                        我可以帮助你处理电网故障研判、调度操作指导、负荷管理和应急处置等决策。<br/>
                        我会检索知识库中的调度规程、故障处置手册等文档，经 CrossEncoder 重排后给出专业建议。
                    </p>
                    <div class="grid grid-cols-2 gap-3 max-w-lg w-full">
                        <div v-for="(s, idx) in suggestions" :key="idx"
                             class="bg-white rounded-xl p-4 border border-gray-200 hover:border-blue-300 hover:shadow-md cursor-pointer transition-all"
                             @click="useSuggestion(s.text)">
                            <component :is="s.icon" class="text-lg mb-2" :style="{ color: s.color }" />
                            <div class="text-sm text-gray-700 leading-relaxed">{{ s.text }}</div>
                        </div>
                    </div>
                </div>

                <!-- 消息列表 -->
                <template v-for="(msg, idx) in currentMessages" :key="idx">
                    <div v-if="msg.role === 'user'" class="flex justify-end">
                        <div class="max-w-2xl">
                            <div class="bg-blue-600 text-white rounded-2xl rounded-tr-md px-5 py-3 text-sm leading-relaxed shadow-sm">
                                {{ msg.content }}
                            </div>
                            <div class="text-xs text-gray-400 mt-1 text-right">{{ msg.time }}</div>
                        </div>
                    </div>

                    <div v-else class="flex gap-3">
                        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0 mt-1">
                            <RobotOutlined class="text-white text-sm" />
                        </div>
                        <div class="max-w-2xl flex-1">
                            <div class="bg-white rounded-2xl rounded-tl-md px-5 py-4 shadow-sm border border-gray-100">
                                <div class="prose prose-sm text-sm leading-relaxed text-gray-700" v-html="renderMarkdown(msg.content)"></div>

                                <div v-if="msg.references && msg.references.length > 0" class="mt-3 pt-3 border-t border-gray-100">
                                    <div class="text-xs text-gray-400 mb-2 flex items-center gap-1">
                                        <BookOutlined /> 检索来源（RAG）
                                    </div>
                                    <div class="space-y-1">
                                        <div v-for="(ref, rIdx) in msg.references" :key="rIdx"
                                             class="flex items-center gap-2 text-xs bg-gray-50 rounded-lg px-3 py-2">
                                            <FileTextOutlined class="text-blue-400" />
                                            <span class="text-gray-700 font-medium">{{ ref.title }}</span>
                                            <span class="text-gray-400">{{ ref.section }}</span>
                                            <span class="ml-auto text-blue-500">{{ typeof ref.relevance === 'number' ? Math.round(ref.relevance * 100) + '%' : '' }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="flex items-center gap-2 mt-1">
                                <span class="text-xs text-gray-400">{{ msg.time }}</span>
                                <a-button type="text" size="small" class="text-gray-400 hover:text-gray-600" @click="copyMessage(msg.content)">
                                    <CopyOutlined class="text-xs" />
                                </a-button>
                            </div>
                        </div>
                    </div>
                </template>

                <div v-if="state.loading" class="flex gap-3">
                    <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center flex-shrink-0">
                        <RobotOutlined class="text-white text-sm" />
                    </div>
                    <div class="bg-white rounded-2xl rounded-tl-md px-5 py-4 shadow-sm border border-gray-100">
                        <div class="flex items-center gap-2 text-sm text-gray-500">
                            <a-spin size="small" />
                            <span>正在检索知识库并生成决策（Hybrid检索 → CrossEncoder重排 → LLM生成）...</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 输入区 -->
            <div class="bg-white border-t border-gray-200 p-4">
                <div class="max-w-4xl mx-auto flex items-end gap-3">
                    <div class="flex-1 relative">
                        <a-textarea ref="inputRef" v-model:value="state.inputText"
                                    placeholder="描述你的调度需求或问题..."
                                    :auto-size="{ minRows: 1, maxRows: 4 }"
                                    class="pr-12"
                                    @pressEnter.prevent="sendMessage" />
                    </div>
                    <a-button type="primary" shape="circle" size="large" @click="sendMessage"
                              :disabled="!state.inputText.trim() || state.loading"
                              class="flex-shrink-0">
                        <SendOutlined />
                    </a-button>
                </div>
                <div class="text-xs text-gray-400 text-center mt-2">AI助手基于电力调度知识库生成建议，实际操作须严格依据现场规程并经值长审核</div>
            </div>
        </div>
    </div>
</template>
