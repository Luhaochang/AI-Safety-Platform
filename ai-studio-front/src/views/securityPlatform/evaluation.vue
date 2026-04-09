<script setup>
import {
    BarChartOutlined, FileTextOutlined, PlayCircleOutlined, PauseCircleOutlined,
    CheckCircleOutlined, ClockCircleOutlined, EyeOutlined, DownloadOutlined,
    ReloadOutlined, HistoryOutlined, ExperimentOutlined, ThunderboltOutlined,
    SafetyCertificateOutlined, AlertOutlined, AimOutlined, StepForwardOutlined,
    StepBackwardOutlined, SyncOutlined, TrophyOutlined, LineChartOutlined,
    FieldTimeOutlined, DatabaseOutlined, CloudServerOutlined
} from '@ant-design/icons-vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const state = reactive({
    activeTab: 'tasks',
    reportDrawerVisible: false,
    selectedReport: null,
    replayVisible: false,
    replayPlaying: false,
    replayStep: 0,
    generatingReport: false,
});

// ====== Mock 验证任务 ======
const verifyTasks = ref([
    {
        id: 'TASK-001', name: '标准攻防验证任务 #1', status: 'completed',
        scenario: '标准RAG安全验证', createTime: '2025-04-01 09:30:00',
        endTime: '2025-04-01 11:45:00', duration: '2h 15min',
        stages: [
            { name: '环境初始化', status: 'done', duration: '45s', time: '09:30:00' },
            { name: 'BOGA-RAGP攻击执行', status: 'done', duration: '3min 42s', time: '09:31:00' },
            { name: '检索护栏防御', status: 'done', duration: '1min 26s', time: '09:35:00' },
            { name: '态势分析', status: 'done', duration: '28s', time: '09:37:00' },
            { name: '预案生成', status: 'done', duration: '15s', time: '09:37:30' },
            { name: '效果评估', status: 'done', duration: '52s', time: '09:38:00' },
        ],
        metrics: { asr_before: 90.7, asr_after: 4.7, rsr_before: 93.3, rsr_after: 8.0, dacc: 94.7, oacc: 97.3, avgLatency: 1.26, peakGpu: 85 }
    },
    {
        id: 'TASK-002', name: 'PoisonedRAG防御对比 #2', status: 'completed',
        scenario: 'BOGA-RAGP攻击测试', createTime: '2025-04-01 13:00:00',
        endTime: '2025-04-01 14:30:00', duration: '1h 30min',
        stages: [
            { name: '环境初始化', status: 'done', duration: '38s', time: '13:00:00' },
            { name: 'PoisonedRAG攻击执行', status: 'done', duration: '2min 15s', time: '13:01:00' },
            { name: '组合式护栏防御', status: 'done', duration: '1min 52s', time: '13:04:00' },
            { name: '态势分析', status: 'done', duration: '32s', time: '13:06:00' },
            { name: '预案生成', status: 'done', duration: '18s', time: '13:06:30' },
            { name: '效果评估', status: 'done', duration: '48s', time: '13:07:00' },
        ],
        metrics: { asr_before: 84.7, asr_after: 2.7, rsr_before: 88.0, rsr_after: 6.0, dacc: 92.0, oacc: 95.3, avgLatency: 1.32, peakGpu: 78 }
    },
    {
        id: 'TASK-003', name: '跨模型迁移验证 #3', status: 'running',
        scenario: '跨模型迁移验证', createTime: '2025-04-01 15:00:00',
        endTime: '-', duration: '-',
        stages: [
            { name: '环境初始化', status: 'done', duration: '52s', time: '15:00:00' },
            { name: '多模型攻击执行', status: 'done', duration: '5min 30s', time: '15:01:00' },
            { name: '检索护栏防御', status: 'running', duration: '-', time: '15:07:00' },
            { name: '态势分析', status: 'pending', duration: '-', time: '-' },
            { name: '预案生成', status: 'pending', duration: '-', time: '-' },
            { name: '效果评估', status: 'pending', duration: '-', time: '-' },
        ],
        metrics: { asr_before: null, asr_after: null, rsr_before: null, rsr_after: null, dacc: null, oacc: null, avgLatency: null, peakGpu: null }
    },
]);

// ====== Mock 评估报告 ======
const reports = ref([
    {
        id: 'RPT-001', taskId: 'TASK-001', name: '标准攻防验证报告', createTime: '2025-04-01 11:45:00',
        summary: '本次验证在标准RAG安全验证场景下完成了从BOGA-RAGP攻击到组合式护栏防御的完整闭环。攻击阶段ASR达90.7%、RSR达93.3%，表明攻击方法具有较强的检索命中能力和生成误导能力。部署三级护栏后，ASR降至4.7%、OACC提升至97.3%，防御效果显著。检索护栏成功拦截87.5%的毒化证据，输入护栏拦截2次提示注入尝试，输出护栏触发1次一致性校验重写。整体验证流程稳定，资源开销可控。',
        metrics: {
            attack: { asr: 90.7, rsr: 93.3, poisonDocs: 5, avgPpl: 28.3, avgSimilarity: 0.861 },
            defense: { dacc: 94.7, oacc: 97.3, blockedCount: 7, falsePositive: 1, avgLatency: 1.26 },
            resource: { peakCpu: 52, peakGpu: 85, peakMem: 65, peakGpuMem: 60, avgLatency: 1.26 }
        }
    },
    {
        id: 'RPT-002', taskId: 'TASK-002', name: 'PoisonedRAG防御对比报告', createTime: '2025-04-01 14:30:00',
        summary: '本次验证以PoisonedRAG作为攻击方法，在BOGA攻击测试场景下进行防御效果对比。攻击阶段ASR为84.7%，部署组合式护栏后降至2.7%。检索护栏对PoisonedRAG的定向投毒文本拦截率达93.3%，表明本文方法对不同攻击范式均具备有效防御能力。',
        metrics: {
            attack: { asr: 84.7, rsr: 88.0, poisonDocs: 3, avgPpl: 22.5, avgSimilarity: 0.838 },
            defense: { dacc: 92.0, oacc: 95.3, blockedCount: 5, falsePositive: 0, avgLatency: 1.32 },
            resource: { peakCpu: 48, peakGpu: 78, peakMem: 60, peakGpuMem: 54, avgLatency: 1.32 }
        }
    },
]);

// ====== Mock 回放时间轴 ======
const replayTimeline = ref([
    { time: '09:30:00', event: '环境初始化完成', type: 'env', detail: '加载电力调度规程库(v2.3)、Qwen2-7B模型、Contriever检索器' },
    { time: '09:31:00', event: 'BOGA-RAGP攻击开始', type: 'attack', detail: '目标主题：变压器油温超标处置，注入5篇毒化文档' },
    { time: '09:32:15', event: '毒化文档注入知识库', type: 'attack', detail: '5篇文档成功写入ChromaDB，向量索引已更新' },
    { time: '09:32:18', event: '异常检索命中检测', type: 'anomaly', detail: '3篇毒化文档进入Top-10，相似度异常偏高' },
    { time: '09:35:00', event: '检索护栏启动', type: 'defense', detail: '检索扩展至Top-20，开始嵌入空间异常检测' },
    { time: '09:35:12', event: '嵌入空间异常拦截', type: 'defense', detail: '3篇证据嵌入Z-Score>2.5，判定为毒化证据并过滤' },
    { time: '09:35:18', event: 'PPL异常检测完成', type: 'defense', detail: '2篇证据PPL Z-Score>2.0，进一步确认并过滤' },
    { time: '09:35:22', event: '证据重排完成', type: 'defense', detail: '剩余17篇证据按相似度重排，选取Top-10输入生成模型' },
    { time: '09:35:45', event: '输出护栏校验通过', type: 'defense', detail: '证据一致性>0.85，生成结果安全' },
    { time: '09:37:00', event: '态势分析完成', type: 'analysis', detail: '当前风险等级：中风险，拦截率91.3%' },
    { time: '09:37:30', event: '预案自动生成', type: 'plan', detail: '生成知识库投毒应急响应预案(PLAN-001)' },
    { time: '09:38:00', event: '效果评估完成', type: 'eval', detail: 'ASR从90.7%降至4.7%，OACC达97.3%' },
]);

const replayTypeColor = (t) => ({
    env: '#1677ff', attack: '#ff4d4f', anomaly: '#faad14',
    defense: '#52c41a', analysis: '#722ed1', plan: '#13c2c2', eval: '#eb2f96'
}[t] || '#999');

const handleViewReport = (report) => {
    state.selectedReport = report;
    state.reportDrawerVisible = true;
};

const handleExportReport = (report) => {
    ElMessage.success('报告已导出: ' + report.name);
};

const handleGenerateReport = (task) => {
    state.generatingReport = true;
    ElMessage.info('正在汇总指标并生成评估报告...');
    setTimeout(() => {
        state.generatingReport = false;
        const now = new Date();
        const timeStr = now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0') + '-' + String(now.getDate()).padStart(2,'0') + ' ' + String(now.getHours()).padStart(2,'0') + ':' + String(now.getMinutes()).padStart(2,'0') + ':' + String(now.getSeconds()).padStart(2,'0');
        const newReportId = 'RPT-' + String(reports.value.length + 1).padStart(3, '0');
        const newReport = {
            id: newReportId,
            taskId: task.id,
            name: task.name + ' 评估报告',
            createTime: timeStr,
            summary: '本次验证在' + task.scenario + '场景下完成了从攻击执行到防御部署的完整闭环。攻击阶段ASR达' + task.metrics.asr_before + '%，部署三级护栏后ASR降至' + task.metrics.asr_after + '%，OACC提升至' + task.metrics.oacc + '%，防御效果显著。检索护栏成功拦截多篇毒化证据，输入护栏拦截提示注入尝试，整体验证流程稳定，资源开销可控。',
            metrics: {
                attack: { asr: task.metrics.asr_before, rsr: task.metrics.rsr_before, poisonDocs: 5, avgPpl: 28.3, avgSimilarity: 0.861 },
                defense: { dacc: task.metrics.dacc, oacc: task.metrics.oacc, blockedCount: 7, falsePositive: 1, avgLatency: task.metrics.avgLatency },
                resource: { peakCpu: 52, peakGpu: task.metrics.peakGpu, peakMem: 65, peakGpuMem: 60, avgLatency: task.metrics.avgLatency }
            }
        };
        reports.value.unshift(newReport);
        ElMessage.success('评估报告 ' + newReportId + ' 生成成功！');
        state.activeTab = 'reports';
    }, 2000);
};

const handleStartReplay = () => {
    state.replayVisible = true;
    state.replayPlaying = true;
    state.replayStep = 0;
    const timer = setInterval(() => {
        if (state.replayStep < replayTimeline.value.length - 1) {
            state.replayStep++;
        } else {
            state.replayPlaying = false;
            clearInterval(timer);
        }
    }, 1500);
};

const handleReplayPause = () => { state.replayPlaying = false; };
const handleReplayStep = (dir) => {
    if (dir > 0 && state.replayStep < replayTimeline.value.length - 1) state.replayStep++;
    if (dir < 0 && state.replayStep > 0) state.replayStep--;
};

const stageStatusColor = (s) => ({ done: 'green', running: 'blue', pending: 'default' }[s] || 'default');

// ====== ECharts ======
const metricsChartRef = ref(null);
const latencyChartRef = ref(null);

onMounted(() => {
    if (metricsChartRef.value) {
        const chart = echarts.init(metricsChartRef.value);
        chart.setOption({
            tooltip: { trigger: 'axis' },
            legend: { data: ['攻击前ASR', '防御后ASR', '防御后OACC'], top: 0, textStyle: { fontSize: 11 } },
            grid: { top: 35, bottom: 25, left: 50, right: 15 },
            xAxis: { type: 'category', data: ['TASK-001\n标准验证', 'TASK-002\nPoisonedRAG'], axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', fontSize: 10 } },
            series: [
                { name: '攻击前ASR', type: 'bar', data: [90.7, 84.7], barWidth: 24, itemStyle: { color: '#ff4d4f', borderRadius: [3,3,0,0] } },
                { name: '防御后ASR', type: 'bar', data: [4.7, 2.7], barWidth: 24, itemStyle: { color: '#faad14', borderRadius: [3,3,0,0] } },
                { name: '防御后OACC', type: 'bar', data: [97.3, 95.3], barWidth: 24, itemStyle: { color: '#52c41a', borderRadius: [3,3,0,0] } },
            ]
        });
        window.addEventListener('resize', () => chart.resize());
    }

    if (latencyChartRef.value) {
        const chart = echarts.init(latencyChartRef.value);
        chart.setOption({
            tooltip: { trigger: 'axis' },
            legend: { data: ['本文方法', 'TrustRAG', 'RobustRAG', 'InstructRAG'], top: 0, textStyle: { fontSize: 11 } },
            grid: { top: 35, bottom: 25, left: 45, right: 15 },
            xAxis: { type: 'category', data: ['NQ-open', 'FinQA', 'PANDAX'], axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', axisLabel: { formatter: '{value}s', fontSize: 10 } },
            series: [
                { name: '本文方法', type: 'bar', data: [1.20, 1.28, 1.26], barWidth: 18, itemStyle: { color: '#1677ff', borderRadius: [3,3,0,0] } },
                { name: 'TrustRAG', type: 'bar', data: [1.75, 1.90, 1.85], barWidth: 18, itemStyle: { color: '#52c41a', borderRadius: [3,3,0,0] } },
                { name: 'RobustRAG', type: 'bar', data: [2.30, 2.50, 2.43], barWidth: 18, itemStyle: { color: '#faad14', borderRadius: [3,3,0,0] } },
                { name: 'InstructRAG', type: 'bar', data: [1.45, 1.58, 1.52], barWidth: 18, itemStyle: { color: '#722ed1', borderRadius: [3,3,0,0] } },
            ]
        });
        window.addEventListener('resize', () => chart.resize());
    }
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                <BarChartOutlined class="text-purple-500" />
                效果评测与复盘分析
            </h2>
            <p class="text-gray-500 text-sm">对攻防验证全过程进行量化评估、统计分析和过程回放，形成可追踪的验证闭环</p>
        </div>

        <a-tabs v-model:activeKey="state.activeTab">
            <!-- Tab1: 验证任务 -->
            <a-tab-pane key="tasks" tab="验证任务">
                <div class="space-y-4">
                    <div v-for="task in verifyTasks" :key="task.id"
                         class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="flex items-center justify-between mb-4">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                                     :class="task.status === 'completed' ? 'bg-green-50 text-green-600' : 'bg-blue-50 text-blue-600'">
                                    <CheckCircleOutlined v-if="task.status==='completed'" class="text-xl" />
                                    <SyncOutlined v-else spin class="text-xl" />
                                </div>
                                <div>
                                    <div class="font-medium text-gray-800">{{ task.name }}</div>
                                    <div class="text-xs text-gray-400 flex items-center gap-2">
                                        <span class="font-mono">{{ task.id }}</span>
                                        <span>{{ task.scenario }}</span>
                                        <a-tag :color="task.status === 'completed' ? 'green' : 'blue'" size="small">
                                            {{ task.status === 'completed' ? '已完成' : '执行中' }}
                                        </a-tag>
                                        <span v-if="task.duration !== '-'"><ClockCircleOutlined /> {{ task.duration }}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="flex items-center gap-2" v-if="task.status === 'completed'">
                                <a-button size="small" @click="handleStartReplay"><HistoryOutlined /> 回放</a-button>
                                <a-button v-if="reports.find(r => r.taskId === task.id)" size="small" type="primary" ghost @click="handleViewReport(reports.find(r => r.taskId === task.id))">
                                    <FileTextOutlined /> 查看报告
                                </a-button>
                                <a-button v-else size="small" type="primary" :loading="state.generatingReport" @click="handleGenerateReport(task)">
                                    <FileTextOutlined /> 生成报告
                                </a-button>
                            </div>
                        </div>

                        <!-- 流程阶段 -->
                        <div class="flex items-center gap-2 mb-4">
                            <div v-for="(stage, idx) in task.stages" :key="idx"
                                 class="flex-1 bg-gray-50 rounded-lg p-2 text-center text-xs"
                                 :class="stage.status === 'running' ? 'bg-blue-50 border border-blue-200' : stage.status === 'done' ? '' : 'opacity-40'">
                                <div class="font-medium text-gray-700 mb-0.5">{{ stage.name }}</div>
                                <a-tag :color="stageStatusColor(stage.status)" size="small">
                                    <CheckCircleOutlined v-if="stage.status==='done'" />
                                    <SyncOutlined v-else-if="stage.status==='running'" spin />
                                    <ClockCircleOutlined v-else />
                                    {{ stage.duration !== '-' ? stage.duration : '等待' }}
                                </a-tag>
                            </div>
                        </div>

                        <!-- 核心指标 -->
                        <div v-if="task.metrics.asr_before !== null" class="grid grid-cols-6 gap-3">
                            <div class="bg-red-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">攻击前ASR</div>
                                <div class="text-base font-bold text-red-600">{{ task.metrics.asr_before }}%</div>
                            </div>
                            <div class="bg-green-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">防御后ASR</div>
                                <div class="text-base font-bold text-green-600">{{ task.metrics.asr_after }}%</div>
                            </div>
                            <div class="bg-blue-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">DACC</div>
                                <div class="text-base font-bold text-blue-600">{{ task.metrics.dacc }}%</div>
                            </div>
                            <div class="bg-purple-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">OACC</div>
                                <div class="text-base font-bold text-purple-600">{{ task.metrics.oacc }}%</div>
                            </div>
                            <div class="bg-orange-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">平均延迟</div>
                                <div class="text-base font-bold text-orange-600">{{ task.metrics.avgLatency }}s</div>
                            </div>
                            <div class="bg-cyan-50 rounded-lg p-2 text-center">
                                <div class="text-xs text-gray-400">GPU峰值</div>
                                <div class="text-base font-bold text-cyan-600">{{ task.metrics.peakGpu }}%</div>
                            </div>
                        </div>
                    </div>
                </div>
            </a-tab-pane>

            <!-- Tab2: 统计分析 -->
            <a-tab-pane key="analysis" tab="统计分析">
                <div class="grid grid-cols-2 gap-6">
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <span class="w-1 h-4 bg-red-500 rounded-full"></span> 攻击与防御效果对比
                        </div>
                        <div ref="metricsChartRef" style="height: 280px;"></div>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 运行时间开销对比
                        </div>
                        <div ref="latencyChartRef" style="height: 280px;"></div>
                    </div>
                </div>

                <!-- 综合评估指标 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mt-6">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-green-500 rounded-full"></span> 综合评估总结
                    </div>
                    <div class="grid grid-cols-4 gap-4 mb-4">
                        <div class="bg-gradient-to-br from-red-50 to-white border border-red-100 rounded-lg p-4 text-center">
                            <ThunderboltOutlined class="text-2xl text-red-500 mb-2" />
                            <div class="text-xs text-gray-400">攻击威胁度</div>
                            <div class="text-xl font-bold text-red-600">90.7%</div>
                            <div class="text-xs text-gray-400 mt-1">BOGA-RAGP ASR</div>
                        </div>
                        <div class="bg-gradient-to-br from-green-50 to-white border border-green-100 rounded-lg p-4 text-center">
                            <SafetyCertificateOutlined class="text-2xl text-green-500 mb-2" />
                            <div class="text-xs text-gray-400">防御有效性</div>
                            <div class="text-xl font-bold text-green-600">97.3%</div>
                            <div class="text-xs text-gray-400 mt-1">最优OACC</div>
                        </div>
                        <div class="bg-gradient-to-br from-blue-50 to-white border border-blue-100 rounded-lg p-4 text-center">
                            <AimOutlined class="text-2xl text-blue-500 mb-2" />
                            <div class="text-xs text-gray-400">检测准确率</div>
                            <div class="text-xl font-bold text-blue-600">94.7%</div>
                            <div class="text-xs text-gray-400 mt-1">最优DACC</div>
                        </div>
                        <div class="bg-gradient-to-br from-purple-50 to-white border border-purple-100 rounded-lg p-4 text-center">
                            <FieldTimeOutlined class="text-2xl text-purple-500 mb-2" />
                            <div class="text-xs text-gray-400">响应时延</div>
                            <div class="text-xl font-bold text-purple-600">1.26s</div>
                            <div class="text-xs text-gray-400 mt-1">平均推理延迟</div>
                        </div>
                    </div>
                    <div class="bg-gray-50 rounded-lg p-4 text-sm text-gray-600 leading-relaxed">
                        <strong>结论：</strong>本次验证结果表明，本文提出的组合式护栏防御框架能够在不显著影响正常问答性能的前提下，
                        有效识别并过滤投毒证据。在BOGA-RAGP攻击场景下，防御后ASR从90.7%降至4.7%，OACC达97.3%，
                        DACC达94.7%。与TrustRAG、RobustRAG、InstructRAG等基线方法相比，本文方法在检测准确率和
                        输出准确率上均有显著提升，且运行时间开销保持在1.20-1.32s的可接受范围内。
                        系统各功能模块协同运行稳定，资源占用合理，具备较好的工程部署可行性。
                    </div>
                </div>
            </a-tab-pane>

            <!-- Tab3: 过程回放 -->
            <a-tab-pane key="replay" tab="过程回放">
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="flex items-center justify-between mb-4">
                        <div class="text-sm font-bold text-gray-800 flex items-center gap-2">
                            <span class="w-1 h-4 bg-orange-500 rounded-full"></span> TASK-001 验证过程回放
                        </div>
                        <div class="flex items-center gap-2">
                            <a-button size="small" @click="handleReplayStep(-1)" :disabled="state.replayStep === 0">
                                <StepBackwardOutlined />
                            </a-button>
                            <a-button size="small" type="primary" @click="state.replayPlaying ? handleReplayPause() : handleStartReplay()">
                                <PauseCircleOutlined v-if="state.replayPlaying" />
                                <PlayCircleOutlined v-else />
                                {{ state.replayPlaying ? '暂停' : '播放' }}
                            </a-button>
                            <a-button size="small" @click="handleReplayStep(1)" :disabled="state.replayStep >= replayTimeline.length - 1">
                                <StepForwardOutlined />
                            </a-button>
                            <span class="text-xs text-gray-400 ml-2">{{ state.replayStep + 1 }} / {{ replayTimeline.length }}</span>
                        </div>
                    </div>

                    <!-- 进度条 -->
                    <a-progress :percent="((state.replayStep + 1) / replayTimeline.length * 100)" :showInfo="false" :strokeColor="{ '0%': '#1677ff', '100%': '#52c41a' }" class="mb-4" />

                    <!-- 时间轴 -->
                    <div class="space-y-3">
                        <div v-for="(item, idx) in replayTimeline" :key="idx"
                             class="flex items-start gap-3 p-3 rounded-lg transition-all duration-300"
                             :class="idx <= state.replayStep ? 'bg-gray-50' : 'opacity-30'"
                             :style="idx === state.replayStep ? 'background: #e6f4ff; border: 1px solid #91caff;' : ''">
                            <div class="w-3 h-3 rounded-full mt-1.5 flex-shrink-0" :style="{ background: replayTypeColor(item.type) }"></div>
                            <div class="flex-1">
                                <div class="flex items-center gap-2 mb-0.5">
                                    <span class="text-xs font-mono text-gray-400">{{ item.time }}</span>
                                    <span class="font-medium text-sm text-gray-800">{{ item.event }}</span>
                                    <a-tag v-if="idx === state.replayStep" color="blue" size="small"><SyncOutlined spin /> 当前</a-tag>
                                </div>
                                <div class="text-xs text-gray-500">{{ item.detail }}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </a-tab-pane>

            <!-- Tab4: 评估报告 -->
            <a-tab-pane key="reports" tab="评估报告">
                <div class="space-y-4">
                    <div v-for="report in reports" :key="report.id"
                         class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="flex items-center justify-between mb-3">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center text-purple-600">
                                    <FileTextOutlined class="text-xl" />
                                </div>
                                <div>
                                    <div class="font-medium text-gray-800">{{ report.name }}</div>
                                    <div class="text-xs text-gray-400">{{ report.id }} | 关联任务: {{ report.taskId }} | {{ report.createTime }}</div>
                                </div>
                            </div>
                            <div class="flex items-center gap-2">
                                <a-button size="small" @click="handleViewReport(report)"><EyeOutlined /> 查看</a-button>
                                <a-button size="small" @click="handleExportReport(report)"><DownloadOutlined /> 导出</a-button>
                            </div>
                        </div>
                        <div class="bg-gray-50 rounded-lg p-3 text-sm text-gray-600 leading-relaxed">
                            {{ report.summary.substring(0, 150) }}...
                        </div>
                    </div>
                </div>
            </a-tab-pane>
        </a-tabs>

        <!-- 报告详情抽屉 -->
        <a-drawer v-model:open="state.reportDrawerVisible" title="评估报告详情" width="700" v-if="state.selectedReport">
            <div class="space-y-5">
                <a-descriptions bordered size="small" :column="2">
                    <a-descriptions-item label="报告ID">{{ state.selectedReport.id }}</a-descriptions-item>
                    <a-descriptions-item label="关联任务">{{ state.selectedReport.taskId }}</a-descriptions-item>
                    <a-descriptions-item label="生成时间" :span="2">{{ state.selectedReport.createTime }}</a-descriptions-item>
                </a-descriptions>

                <div>
                    <div class="text-sm font-bold text-gray-800 mb-2">报告摘要</div>
                    <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 text-sm text-gray-700 leading-relaxed">{{ state.selectedReport.summary }}</div>
                </div>

                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3">攻击效果指标</div>
                    <div class="grid grid-cols-3 gap-3">
                        <div class="bg-red-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">ASR</div>
                            <div class="text-lg font-bold text-red-600">{{ state.selectedReport.metrics.attack.asr }}%</div>
                        </div>
                        <div class="bg-red-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">RSR</div>
                            <div class="text-lg font-bold text-red-600">{{ state.selectedReport.metrics.attack.rsr }}%</div>
                        </div>
                        <div class="bg-red-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">注入文档数</div>
                            <div class="text-lg font-bold text-red-600">{{ state.selectedReport.metrics.attack.poisonDocs }}</div>
                        </div>
                    </div>
                </div>

                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3">防御效果指标</div>
                    <div class="grid grid-cols-3 gap-3">
                        <div class="bg-green-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">DACC</div>
                            <div class="text-lg font-bold text-green-600">{{ state.selectedReport.metrics.defense.dacc }}%</div>
                        </div>
                        <div class="bg-green-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">OACC</div>
                            <div class="text-lg font-bold text-green-600">{{ state.selectedReport.metrics.defense.oacc }}%</div>
                        </div>
                        <div class="bg-green-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">拦截数</div>
                            <div class="text-lg font-bold text-green-600">{{ state.selectedReport.metrics.defense.blockedCount }}</div>
                        </div>
                    </div>
                </div>

                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3">资源开销指标</div>
                    <div class="grid grid-cols-3 gap-3">
                        <div class="bg-blue-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">CPU峰值</div>
                            <div class="text-lg font-bold text-blue-600">{{ state.selectedReport.metrics.resource.peakCpu }}%</div>
                        </div>
                        <div class="bg-blue-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">GPU峰值</div>
                            <div class="text-lg font-bold text-blue-600">{{ state.selectedReport.metrics.resource.peakGpu }}%</div>
                        </div>
                        <div class="bg-blue-50 rounded-lg p-3 text-center">
                            <div class="text-xs text-gray-400">平均延迟</div>
                            <div class="text-lg font-bold text-blue-600">{{ state.selectedReport.metrics.resource.avgLatency }}s</div>
                        </div>
                    </div>
                </div>
            </div>
        </a-drawer>
    </div>
</template>
