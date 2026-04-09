<script setup>
import {
    FileSearchOutlined,
    SearchOutlined,
    EyeOutlined,
    ClockCircleOutlined,
    UserOutlined,
    BookOutlined,
    ReloadOutlined,
    ExportOutlined,
    MessageOutlined,
    ExperimentOutlined,
    FileTextOutlined,
    WarningOutlined,
    CheckCircleOutlined,
    RobotOutlined
} from '@ant-design/icons-vue';
import { useChatStore } from '@/store/modules/chat.js';

const chatStore = useChatStore();

const state = reactive({
    keyword: '',
    typeFilter: 'all',
    detailVisible: false,
    detailData: null
});

// 从store获取合并后的决策日志（对话 + 孪生推演）
const filteredLogs = computed(() => {
    let logs = chatStore.decisionLogs;
    if (state.typeFilter !== 'all') {
        logs = logs.filter(l => l.type === state.typeFilter);
    }
    if (state.keyword.trim()) {
        const kw = state.keyword.trim().toLowerCase();
        logs = logs.filter(l =>
            l.question.toLowerCase().includes(kw) ||
            l.answer.toLowerCase().includes(kw)
        );
    }
    return logs;
});

const handleView = (record) => {
    state.detailData = record;
    state.detailVisible = true;
};

const handleReset = () => {
    state.keyword = '';
    state.typeFilter = 'all';
};

const riskColor = (level) => {
    return { '高风险': 'red', '中风险': 'orange', '低风险': 'green' }[level] || 'default';
};

const truncateMarkdown = (text, maxLen = 80) => {
    if (!text) return '';
    const plain = text.replace(/[#*>`\-\n]/g, ' ').replace(/\s+/g, ' ').trim();
    return plain.length > maxLen ? plain.substring(0, maxLen) + '...' : plain;
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
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                <FileSearchOutlined class="text-blue-500" />
                决策日志
            </h2>
            <p class="text-gray-500 text-sm">记录调度决策对话的检索上下文与推理结果，以及孪生推演的仿真分析记录</p>
        </div>

        <!-- 主内容 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <!-- 搜索栏 -->
            <div class="p-5 border-b border-gray-100 flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <a-input v-model:value="state.keyword" placeholder="搜索问题或答案" style="width: 300px" allowClear @pressEnter="() => {}">
                        <template #prefix><SearchOutlined class="text-gray-400" /></template>
                    </a-input>
                    <a-radio-group v-model:value="state.typeFilter" button-style="solid" size="small">
                        <a-radio-button value="all">全部</a-radio-button>
                        <a-radio-button value="chat"><MessageOutlined /> 对话</a-radio-button>
                        <a-radio-button value="twin"><ExperimentOutlined /> 推演</a-radio-button>
                    </a-radio-group>
                    <a-button @click="handleReset"><ReloadOutlined /> 重置</a-button>
                </div>
                <a-button><ExportOutlined /> 导出</a-button>
            </div>

            <!-- 日志表格 -->
            <div class="p-5">
                <a-table :dataSource="filteredLogs" :pagination="{ pageSize: 10, showQuickJumper: true }" rowKey="id">
                    <a-table-column title="类型" dataIndex="type" :width="100" align="center">
                        <template #default="{ record }">
                            <a-tag :color="record.type === 'chat' ? 'blue' : 'purple'">
                                <MessageOutlined v-if="record.type === 'chat'" />
                                <ExperimentOutlined v-else />
                                {{ record.typeLabel }}
                            </a-tag>
                        </template>
                    </a-table-column>
                    <a-table-column title="问题/场景" dataIndex="question" :width="300" :ellipsis="true">
                        <template #default="{ record }">
                            <a class="text-blue-600 hover:text-blue-800 cursor-pointer" @click="handleView(record)">
                                {{ record.question }}
                            </a>
                        </template>
                    </a-table-column>
                    <a-table-column title="回答/结论" dataIndex="answer" :width="300" :ellipsis="true">
                        <template #default="{ record }">
                            <span class="text-gray-600">{{ truncateMarkdown(record.answer) }}</span>
                        </template>
                    </a-table-column>
                    <a-table-column title="用户" dataIndex="user" :width="110" align="center">
                        <template #default="{ record }">
                            <span class="flex items-center justify-center gap-1 text-gray-600">
                                <UserOutlined /> {{ record.user }}
                            </span>
                        </template>
                    </a-table-column>
                    <a-table-column title="耗时" dataIndex="duration" :width="80" align="center">
                        <template #default="{ record }">
                            <a-tag>{{ record.duration }}</a-tag>
                        </template>
                    </a-table-column>
                    <a-table-column title="时间" dataIndex="createTime" :width="170" align="center">
                        <template #default="{ record }">
                            <span class="text-gray-500 text-xs flex items-center justify-center gap-1">
                                <ClockCircleOutlined /> {{ record.createTime }}
                            </span>
                        </template>
                    </a-table-column>
                    <a-table-column title="操作" :width="80" align="center">
                        <template #default="{ record }">
                            <a-button type="link" size="small" @click="handleView(record)"><EyeOutlined /> 详情</a-button>
                        </template>
                    </a-table-column>
                </a-table>
            </div>
        </div>

        <!-- 详情抽屉 -->
        <a-drawer v-model:open="state.detailVisible" title="决策详情" width="680" v-if="state.detailData">
            <div class="space-y-6">
                <!-- 基本信息 -->
                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 基本信息
                    </div>
                    <a-descriptions bordered :column="2" size="small">
                        <a-descriptions-item label="类型">
                            <a-tag :color="state.detailData.type === 'chat' ? 'blue' : 'purple'">{{ state.detailData.typeLabel }}</a-tag>
                        </a-descriptions-item>
                        <a-descriptions-item label="用户">{{ state.detailData.user }}</a-descriptions-item>
                        <a-descriptions-item label="耗时">{{ state.detailData.duration }}</a-descriptions-item>
                        <a-descriptions-item label="时间">{{ state.detailData.createTime }}</a-descriptions-item>
                    </a-descriptions>
                </div>

                <!-- 问题/场景 -->
                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-orange-500 rounded-full"></span>
                        {{ state.detailData.type === 'chat' ? '用户问题' : '推演场景' }}
                    </div>
                    <div class="bg-orange-50 rounded-xl p-4 text-sm text-gray-700 border border-orange-100">
                        {{ state.detailData.type === 'twin' ? state.detailData.scenario : state.detailData.question }}
                    </div>
                </div>

                <!-- AI回答（对话类型） -->
                <div v-if="state.detailData.type === 'chat'">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-green-500 rounded-full"></span> AI决策回答
                    </div>
                    <div class="bg-green-50 rounded-xl p-4 text-sm text-gray-700 border border-green-100 prose prose-sm max-h-80 overflow-auto"
                         v-html="renderMarkdown(state.detailData.answer)">
                    </div>
                </div>

                <!-- Top-k 检索语段（对话类型） -->
                <div v-if="state.detailData.type === 'chat' && state.detailData.retrievedPassages?.length > 0">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-purple-500 rounded-full"></span> Top-K 检索语段（{{ state.detailData.retrievedPassages.length }}条）
                    </div>
                    <div class="space-y-3">
                        <div v-for="(passage, pIdx) in state.detailData.retrievedPassages" :key="pIdx"
                             class="bg-gray-50 rounded-xl p-4 border border-gray-200">
                            <div class="flex items-center justify-between mb-2">
                                <div class="flex items-center gap-2">
                                    <a-tag color="purple" size="small">Top-{{ pIdx + 1 }}</a-tag>
                                    <span class="text-xs font-medium text-gray-700">
                                        <FileTextOutlined class="text-blue-400" /> {{ passage.docTitle }}
                                    </span>
                                    <span class="text-xs text-gray-400">{{ passage.section }}</span>
                                </div>
                                <span class="text-xs font-bold text-blue-500">{{ Math.round(passage.relevance * 100) }}%</span>
                            </div>
                            <div class="text-sm text-gray-600 leading-relaxed bg-white rounded-lg p-3 border border-gray-100">
                                {{ passage.content }}
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 推演结果（孪生推演类型） -->
                <div v-if="state.detailData.type === 'twin' && state.detailData.twinResult">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-green-500 rounded-full"></span> 推演结论
                        <a-tag :color="riskColor(state.detailData.riskLevel)" size="small">{{ state.detailData.riskLevel }}</a-tag>
                    </div>
                    <div class="bg-green-50 rounded-xl p-4 text-sm text-gray-700 border border-green-100 mb-4">
                        {{ state.detailData.twinResult.summary }}
                    </div>

                    <!-- 影响分析 -->
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-red-500 rounded-full"></span> 影响设备/线路
                    </div>
                    <a-table :dataSource="state.detailData.twinResult.affectedLines" :pagination="false" size="small" rowKey="name" class="mb-4">
                        <a-table-column title="设备/线路" dataIndex="name" />
                        <a-table-column title="故障前负荷率" dataIndex="beforeLoad" align="center" />
                        <a-table-column title="故障后负荷率" dataIndex="afterLoad" align="center" />
                        <a-table-column title="状态" dataIndex="status" align="center">
                            <template #default="{ record }">
                                <a-tag :color="record.status === '安全' ? 'green' : record.status === '预警' ? 'orange' : 'red'">
                                    <CheckCircleOutlined v-if="record.status === '安全'" />
                                    <WarningOutlined v-else />
                                    {{ record.status }}
                                </a-tag>
                            </template>
                        </a-table-column>
                    </a-table>

                    <!-- 建议措施 -->
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 建议措施
                    </div>
                    <div class="bg-blue-50 rounded-xl p-4 border border-blue-100">
                        <div v-for="(rec, rIdx) in state.detailData.twinResult.recommendations" :key="rIdx"
                             class="flex items-start gap-2 text-sm text-gray-700 mb-2 last:mb-0">
                            <span class="text-blue-500 font-bold mt-0.5">{{ rIdx + 1 }}.</span>
                            <span>{{ rec }}</span>
                        </div>
                    </div>
                </div>

                <!-- 完整对话记录（对话类型） -->
                <div v-if="state.detailData.type === 'chat' && state.detailData.messages?.length > 0">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-cyan-500 rounded-full"></span> 完整对话记录（{{ state.detailData.messages.length }}条）
                    </div>
                    <div class="space-y-3 max-h-96 overflow-auto">
                        <div v-for="(msg, mIdx) in state.detailData.messages" :key="mIdx"
                             class="rounded-lg p-3 text-sm"
                             :class="msg.role === 'user' ? 'bg-blue-50 border border-blue-100' : 'bg-gray-50 border border-gray-200'">
                            <div class="flex items-center gap-2 mb-1">
                                <a-tag :color="msg.role === 'user' ? 'blue' : 'green'" size="small">
                                    <UserOutlined v-if="msg.role === 'user'" />
                                    <RobotOutlined v-else />
                                    {{ msg.role === 'user' ? '用户' : 'AI助手' }}
                                </a-tag>
                                <span class="text-xs text-gray-400">{{ msg.time }}</span>
                            </div>
                            <div class="text-gray-700 leading-relaxed" :class="msg.role === 'assistant' ? 'max-h-32 overflow-auto' : ''">
                                {{ msg.role === 'user' ? msg.content : truncateMarkdown(msg.content, 200) }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </a-drawer>
    </div>
</template>
