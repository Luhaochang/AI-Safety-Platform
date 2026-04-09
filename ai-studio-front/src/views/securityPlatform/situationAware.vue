<script setup>
import {
    AlertOutlined, EyeOutlined, ThunderboltOutlined, SafetyCertificateOutlined,
    ClockCircleOutlined, WarningOutlined, CheckCircleOutlined, CloseCircleOutlined,
    BellOutlined, NodeIndexOutlined, FileProtectOutlined, AimOutlined,
    RocketOutlined, BulbOutlined, HistoryOutlined, GlobalOutlined,
    DatabaseOutlined, SearchOutlined, SyncOutlined, FireOutlined,
    FileTextOutlined, BarChartOutlined, InfoCircleOutlined
} from '@ant-design/icons-vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const state = reactive({
    activeTab: 'overview',
    planDrawerVisible: false,
    selectedPlan: null,
    eventDrawerVisible: false,
    selectedEvent: null,
    generatingPlan: false,
});

// ====== Mock 态势概览统计 ======
const overviewStats = ref({
    riskLevel: '中风险',
    totalEvents: 156,
    attackEvents: 23,
    defenseEvents: 89,
    anomalyEvents: 12,
    normalEvents: 32,
    last24hAttacks: 8,
    blockedRate: 91.3,
});

// ====== Mock 攻击链事件 ======
const attackChainEvents = ref([
    { id: 'EVT-001', time: '2025-04-01 10:32:15', type: 'attack', severity: 'high', source: 'BOGA-RAGP', target: '电力调度规程库', action: '毒化文档注入', detail: '检测到5篇毒化文档注入知识库，目标主题：变压器油温处置', status: 'detected' },
    { id: 'EVT-002', time: '2025-04-01 10:32:18', type: 'anomaly', severity: 'high', source: '检索服务', target: '向量索引', action: '异常检索命中', detail: '3篇毒化文档进入Top-10检索结果，相似度异常偏高(>0.89)', status: 'detected' },
    { id: 'EVT-003', time: '2025-04-01 10:32:20', type: 'defense', severity: 'medium', source: '检索护栏', target: '候选证据集', action: '嵌入空间异常检测触发', detail: '检测到3篇候选证据嵌入Z-Score>2.5，判定为异常聚集', status: 'blocked' },
    { id: 'EVT-004', time: '2025-04-01 10:32:21', type: 'defense', severity: 'medium', source: '检索护栏', target: '候选证据集', action: '语句级PPL异常检测触发', detail: '2篇证据的局部PPL Z-Score>2.0，存在风格断裂', status: 'blocked' },
    { id: 'EVT-005', time: '2025-04-01 10:32:22', type: 'defense', severity: 'low', source: '输出护栏', target: '生成结果', action: '证据一致性校验通过', detail: '过滤后证据与生成结果一致性>0.85，输出安全', status: 'passed' },
    { id: 'EVT-006', time: '2025-04-01 10:35:42', type: 'attack', severity: 'high', source: 'PoisonedRAG', target: '电力调度规程库', action: '定向投毒注入', detail: '检测到3篇定向投毒文档，目标查询：110kV线路跳闸处置', status: 'detected' },
    { id: 'EVT-007', time: '2025-04-01 10:35:45', type: 'defense', severity: 'medium', source: '输入护栏', target: '用户查询', action: '提示注入检测触发', detail: '检测到查询中包含可疑指令注入片段，已净化', status: 'blocked' },
    { id: 'EVT-008', time: '2025-04-01 10:38:10', type: 'anomaly', severity: 'medium', source: '资源监控', target: 'GPU服务', action: '显存占用异常', detail: 'GPU显存占用突增至92%，可能影响推理服务稳定性', status: 'warning' },
]);

// ====== Mock 风险分布 ======
const riskDistribution = ref([
    { area: '知识库入口', risk: 85, attacks: 8, blocked: 7 },
    { area: '检索链路', risk: 72, attacks: 12, blocked: 11 },
    { area: '生成链路', risk: 45, attacks: 3, blocked: 3 },
    { area: '输入链路', risk: 38, attacks: 5, blocked: 5 },
    { area: '输出链路', risk: 25, attacks: 2, blocked: 2 },
]);

// ====== Mock 预案 ======
const plans = ref([
    {
        id: 'PLAN-001', name: '知识库投毒应急响应预案', trigger: '检测到批量毒化文档注入',
        riskLevel: '高风险', createTime: '2025-04-01 10:33:00', status: 'active',
        steps: [
            '立即暂停受影响知识库的检索服务',
            '启用环境快照回滚至攻击前基线状态',
            '对注入时间窗口内的所有文档执行全量PPL扫描',
            '清除PPL异常或嵌入空间异常的文档',
            '重建向量索引并恢复检索服务',
            '启用增强检索护栏(α=3, Z-Score阈值降至2.0)',
            '记录攻击特征并更新防御规则库',
        ],
        evidence: '基于BOGA-RAGP攻击特征: 5篇毒化文档, 嵌入Z-Score均>2.5, PPL均>25'
    },
    {
        id: 'PLAN-002', name: '提示注入防御升级预案', trigger: '检测到PIA类攻击',
        riskLevel: '中风险', createTime: '2025-04-01 10:36:00', status: 'standby',
        steps: [
            '启用输入护栏强化模式(阈值降至0.6)',
            '对近期查询日志进行回溯扫描',
            '检查是否有异常指令通过输入层进入系统',
            '更新提示注入检测规则库',
            '生成安全报告并通知管理员',
        ],
        evidence: '检测到查询中包含指令注入片段，输入护栏已拦截'
    },
    {
        id: 'PLAN-003', name: '资源异常处理预案', trigger: 'GPU显存占用超过90%',
        riskLevel: '低风险', createTime: '2025-04-01 10:38:15', status: 'standby',
        steps: [
            '检查当前GPU上运行的模型服务负载',
            '如有空闲模型实例则释放资源',
            '必要时降低并发推理请求数',
            '通知运维人员关注资源使用趋势',
        ],
        evidence: 'GPU显存占用92%, 临近上限'
    },
]);

const severityColor = (s) => ({ high: 'red', medium: 'orange', low: 'blue' }[s] || 'default');
const severityText = (s) => ({ high: '高', medium: '中', low: '低' }[s] || s);
const eventTypeColor = (t) => ({ attack: 'red', defense: 'green', anomaly: 'orange', normal: 'blue' }[t] || 'default');
const eventTypeText = (t) => ({ attack: '攻击事件', defense: '防御动作', anomaly: '异常告警', normal: '正常事件' }[t] || t);
const statusIcon = (s) => ({ detected: 'warning', blocked: 'check', passed: 'check', warning: 'warning' }[s]);
const riskLevelColor = (l) => ({ '高风险': 'red', '中风险': 'orange', '低风险': 'green' }[l] || 'default');

const handleViewPlan = (plan) => {
    state.selectedPlan = plan;
    state.planDrawerVisible = true;
};

const handleViewEvent = (evt) => {
    state.selectedEvent = evt;
    state.eventDrawerVisible = true;
};

const handleActivatePlan = (plan) => {
    plan.status = 'active';
    ElMessage.success('预案已激活: ' + plan.name);
};

const handleGeneratePlan = () => {
    state.generatingPlan = true;
    ElMessage.info('正在分析当前态势并生成处置预案...');
    setTimeout(() => {
        state.generatingPlan = false;
        const now = new Date();
        const timeStr = now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0') + '-' + String(now.getDate()).padStart(2,'0') + ' ' + String(now.getHours()).padStart(2,'0') + ':' + String(now.getMinutes()).padStart(2,'0') + ':' + String(now.getSeconds()).padStart(2,'0');
        const newPlanId = 'PLAN-' + String(plans.value.length + 1).padStart(3, '0');
        const newPlan = {
            id: newPlanId,
            name: '知识库多源攻击综合响应预案',
            trigger: '检测到多种攻击方法并发作用于同一知识库',
            riskLevel: '高风险',
            createTime: timeStr,
            status: 'active',
            steps: [
                '立即暂停受影响知识库的检索服务，切换至备份知识库',
                '启用环境快照回滚至最近安全基线状态',
                '对攻击窗口内所有新增文档执行全量PPL扫描和嵌入空间异常检测',
                '清除PPL异常或嵌入空间异常的文档，并记录攻击特征签名',
                '升级检索护栏参数：扩展因子α提升至3, Z-Score阈值降至1.8',
                '重建向量索引并恢复检索服务，启用增强监控模式',
                '生成完整攻击报告并通知安全管理员',
                '将攻击特征录入防御规则库，更新全局防护策略',
            ],
            evidence: '基于当前态势分析: 检测到BOGA-RAGP和PoisonedRAG两种攻击方法并发作用, 共计' + attackChainEvents.value.filter(e => e.type === 'attack').length + '次攻击事件, 风险等级:高'
        };
        plans.value.unshift(newPlan);
        ElMessage.success('预案生成成功！' + newPlanId + ' - ' + newPlan.name + ' 已自动激活');
    }, 2500);
};

// ====== ECharts ======
const timelineChartRef = ref(null);
const riskMapRef = ref(null);
const eventPieRef = ref(null);

onMounted(() => {
    // 事件时间线
    if (timelineChartRef.value) {
        const chart = echarts.init(timelineChartRef.value);
        chart.setOption({
            tooltip: { trigger: 'axis' },
            legend: { data: ['攻击事件', '防御触发', '异常告警'], top: 0, textStyle: { fontSize: 11 } },
            grid: { top: 30, bottom: 25, left: 45, right: 15 },
            xAxis: { type: 'category', data: ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00'], axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
            series: [
                { name: '攻击事件', type: 'bar', stack: 'total', data: [0, 1, 5, 3, 2, 0, 1], itemStyle: { color: '#ff4d4f' } },
                { name: '防御触发', type: 'bar', stack: 'total', data: [2, 5, 15, 12, 8, 4, 6], itemStyle: { color: '#52c41a' } },
                { name: '异常告警', type: 'bar', stack: 'total', data: [1, 0, 3, 2, 1, 0, 1], itemStyle: { color: '#faad14' } },
            ]
        });
        window.addEventListener('resize', () => chart.resize());
    }

    // 风险热力
    if (riskMapRef.value) {
        const chart = echarts.init(riskMapRef.value);
        chart.setOption({
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: { top: 10, bottom: 25, left: 90, right: 30 },
            xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', fontSize: 10 } },
            yAxis: { type: 'category', data: riskDistribution.value.map(d => d.area), axisLabel: { fontSize: 11 } },
            series: [{
                type: 'bar',
                data: riskDistribution.value.map(d => ({
                    value: d.risk,
                    itemStyle: { color: d.risk > 70 ? '#ff4d4f' : d.risk > 50 ? '#faad14' : '#52c41a', borderRadius: [0, 4, 4, 0] }
                })),
                barWidth: 20,
                label: { show: true, position: 'right', formatter: '{c}%', fontSize: 11 }
            }]
        });
        window.addEventListener('resize', () => chart.resize());
    }

    // 事件饼图
    if (eventPieRef.value) {
        const chart = echarts.init(eventPieRef.value);
        chart.setOption({
            tooltip: { trigger: 'item' },
            series: [{
                type: 'pie', radius: ['40%', '70%'],
                data: [
                    { value: 23, name: '攻击事件', itemStyle: { color: '#ff4d4f' } },
                    { value: 89, name: '防御触发', itemStyle: { color: '#52c41a' } },
                    { value: 12, name: '异常告警', itemStyle: { color: '#faad14' } },
                    { value: 32, name: '正常事件', itemStyle: { color: '#1677ff' } },
                ],
                label: { fontSize: 11 },
            }]
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
                <AlertOutlined class="text-orange-500" />
                态势感知与预案生成
            </h2>
            <p class="text-gray-500 text-sm">对攻防全链路关键事件进行统一采集与可视化展示，并结合当前态势生成处置预案</p>
        </div>

        <a-tabs v-model:activeKey="state.activeTab">
            <!-- Tab1: 态势总览 -->
            <a-tab-pane key="overview" tab="态势总览">
                <!-- 顶部统计卡片 -->
                <div class="grid grid-cols-5 gap-4 mb-6">
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
                        <div class="text-xs text-gray-400 mb-1">当前风险等级</div>
                        <a-tag :color="riskLevelColor(overviewStats.riskLevel)" class="text-base px-3 py-0.5">
                            {{ overviewStats.riskLevel }}
                        </a-tag>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
                        <div class="text-xs text-gray-400 mb-1">总事件数</div>
                        <div class="text-2xl font-bold text-gray-800">{{ overviewStats.totalEvents }}</div>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
                        <div class="text-xs text-gray-400 mb-1">攻击事件</div>
                        <div class="text-2xl font-bold text-red-600">{{ overviewStats.attackEvents }}</div>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
                        <div class="text-xs text-gray-400 mb-1">防御触发</div>
                        <div class="text-2xl font-bold text-green-600">{{ overviewStats.defenseEvents }}</div>
                    </div>
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 text-center">
                        <div class="text-xs text-gray-400 mb-1">拦截率</div>
                        <div class="text-2xl font-bold text-blue-600">{{ overviewStats.blockedRate }}%</div>
                    </div>
                </div>

                <div class="grid grid-cols-3 gap-6">
                    <!-- 事件时间线 -->
                    <div class="col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 事件时间分布
                        </div>
                        <div ref="timelineChartRef" style="height: 220px;"></div>
                    </div>
                    <!-- 事件分类 -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <span class="w-1 h-4 bg-purple-500 rounded-full"></span> 事件分类
                        </div>
                        <div ref="eventPieRef" style="height: 220px;"></div>
                    </div>
                </div>

                <!-- 风险热力图 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mt-6">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-red-500 rounded-full"></span> 链路风险分布
                    </div>
                    <div class="grid grid-cols-2 gap-6">
                        <div ref="riskMapRef" style="height: 200px;"></div>
                        <div>
                            <a-table :dataSource="riskDistribution" :pagination="false" rowKey="area" size="small">
                                <a-table-column title="链路区域" dataIndex="area" :width="100" />
                                <a-table-column title="风险值" dataIndex="risk" :width="80" align="center">
                                    <template #default="{ record }">
                                        <span class="font-bold" :class="record.risk > 70 ? 'text-red-600' : record.risk > 50 ? 'text-orange-500' : 'text-green-600'">{{ record.risk }}%</span>
                                    </template>
                                </a-table-column>
                                <a-table-column title="攻击次数" dataIndex="attacks" :width="80" align="center" />
                                <a-table-column title="拦截次数" dataIndex="blocked" :width="80" align="center" />
                                <a-table-column title="拦截率" :width="80" align="center">
                                    <template #default="{ record }">
                                        {{ record.attacks > 0 ? ((record.blocked / record.attacks) * 100).toFixed(0) : 0 }}%
                                    </template>
                                </a-table-column>
                            </a-table>
                        </div>
                    </div>
                </div>
            </a-tab-pane>

            <!-- Tab2: 攻击链路分析 -->
            <a-tab-pane key="chain" tab="攻击链路分析">
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-red-500 rounded-full"></span> 攻击事件链追踪
                    </div>
                    <a-timeline mode="left">
                        <a-timeline-item v-for="evt in attackChainEvents" :key="evt.id"
                            :color="evt.type === 'attack' ? 'red' : evt.type === 'defense' ? 'green' : 'orange'">
                            <div class="bg-gray-50 rounded-lg p-4 mb-2 cursor-pointer hover:bg-gray-100 transition-colors" @click="handleViewEvent(evt)">
                                <div class="flex items-center justify-between mb-1">
                                    <div class="flex items-center gap-2">
                                        <a-tag :color="eventTypeColor(evt.type)" size="small">{{ eventTypeText(evt.type) }}</a-tag>
                                        <a-tag :color="severityColor(evt.severity)" size="small">{{ severityText(evt.severity) }}</a-tag>
                                        <span class="font-medium text-gray-800 text-sm">{{ evt.action }}</span>
                                    </div>
                                    <span class="text-xs text-gray-400 flex items-center gap-1">
                                        <ClockCircleOutlined /> {{ evt.time }}
                                    </span>
                                </div>
                                <div class="text-sm text-gray-600">{{ evt.detail }}</div>
                                <div class="flex items-center gap-2 mt-1 text-xs text-gray-400">
                                    <span>来源: {{ evt.source }}</span>
                                    <span>目标: {{ evt.target }}</span>
                                    <a-tag v-if="evt.status === 'blocked'" color="green" size="small"><CheckCircleOutlined /> 已拦截</a-tag>
                                    <a-tag v-else-if="evt.status === 'detected'" color="orange" size="small"><WarningOutlined /> 已检测</a-tag>
                                    <a-tag v-else-if="evt.status === 'passed'" color="blue" size="small"><CheckCircleOutlined /> 已通过</a-tag>
                                    <a-tag v-else color="red" size="small"><WarningOutlined /> 告警</a-tag>
                                </div>
                            </div>
                        </a-timeline-item>
                    </a-timeline>
                </div>
            </a-tab-pane>

            <!-- Tab3: 预案生成 -->
            <a-tab-pane key="plans" tab="预案生成">
                <div class="flex items-center justify-between mb-4">
                    <div class="text-sm text-gray-500">已生成 {{ plans.length }} 份处置预案</div>
                    <a-button type="primary" :loading="state.generatingPlan" @click="handleGeneratePlan">
                        <BulbOutlined /> 基于当前态势生成预案
                    </a-button>
                </div>
                <div class="space-y-4">
                    <div v-for="plan in plans" :key="plan.id"
                         class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="flex items-center justify-between mb-3">
                            <div class="flex items-center gap-3">
                                <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                                     :class="plan.status === 'active' ? 'bg-green-50 text-green-600' : 'bg-gray-50 text-gray-400'">
                                    <FileProtectOutlined class="text-xl" />
                                </div>
                                <div>
                                    <div class="font-medium text-gray-800">{{ plan.name }}</div>
                                    <div class="text-xs text-gray-400 flex items-center gap-2">
                                        <span class="font-mono">{{ plan.id }}</span>
                                        <a-tag :color="riskLevelColor(plan.riskLevel)" size="small">{{ plan.riskLevel }}</a-tag>
                                        <a-tag :color="plan.status === 'active' ? 'green' : 'default'" size="small">
                                            {{ plan.status === 'active' ? '已激活' : '待命' }}
                                        </a-tag>
                                    </div>
                                </div>
                            </div>
                            <div class="flex items-center gap-2">
                                <a-button v-if="plan.status !== 'active'" size="small" type="primary" ghost @click="handleActivatePlan(plan)">
                                    <ThunderboltOutlined /> 激活
                                </a-button>
                                <a-button size="small" @click="handleViewPlan(plan)">
                                    <EyeOutlined /> 详情
                                </a-button>
                            </div>
                        </div>
                        <div class="bg-gray-50 rounded-lg p-3">
                            <div class="text-xs text-gray-400 mb-1">触发条件: {{ plan.trigger }}</div>
                            <div class="text-xs text-gray-500">{{ plan.evidence }}</div>
                        </div>
                        <div class="mt-3">
                            <div class="text-xs text-gray-400 mb-2">处置步骤:</div>
                            <div class="space-y-1">
                                <div v-for="(step, idx) in plan.steps.slice(0, 3)" :key="idx" class="flex items-start gap-2 text-sm text-gray-600">
                                    <span class="w-5 h-5 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs flex-shrink-0 mt-0.5">{{ idx + 1 }}</span>
                                    {{ step }}
                                </div>
                                <div v-if="plan.steps.length > 3" class="text-xs text-blue-500 ml-7 cursor-pointer" @click="handleViewPlan(plan)">
                                    查看全部 {{ plan.steps.length }} 步 →
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </a-tab-pane>
        </a-tabs>

        <!-- 预案详情抽屉 -->
        <a-drawer v-model:open="state.planDrawerVisible" title="预案详情" width="600" v-if="state.selectedPlan">
            <div class="space-y-4">
                <a-descriptions bordered size="small" :column="2">
                    <a-descriptions-item label="预案ID">{{ state.selectedPlan.id }}</a-descriptions-item>
                    <a-descriptions-item label="风险等级"><a-tag :color="riskLevelColor(state.selectedPlan.riskLevel)">{{ state.selectedPlan.riskLevel }}</a-tag></a-descriptions-item>
                    <a-descriptions-item label="状态"><a-tag :color="state.selectedPlan.status === 'active' ? 'green' : 'default'">{{ state.selectedPlan.status === 'active' ? '已激活' : '待命' }}</a-tag></a-descriptions-item>
                    <a-descriptions-item label="生成时间">{{ state.selectedPlan.createTime }}</a-descriptions-item>
                    <a-descriptions-item label="触发条件" :span="2">{{ state.selectedPlan.trigger }}</a-descriptions-item>
                </a-descriptions>
                <div>
                    <div class="text-sm font-bold text-gray-800 mb-2">触发依据</div>
                    <div class="bg-orange-50 border border-orange-200 rounded-lg p-3 text-sm text-orange-700">{{ state.selectedPlan.evidence }}</div>
                </div>
                <div>
                    <div class="text-sm font-bold text-gray-800 mb-3">处置步骤</div>
                    <a-steps direction="vertical" :current="-1" size="small">
                        <a-step v-for="(step, idx) in state.selectedPlan.steps" :key="idx" :title="'步骤 ' + (idx + 1)" :description="step" />
                    </a-steps>
                </div>
            </div>
        </a-drawer>

        <!-- 事件详情抽屉 -->
        <a-drawer v-model:open="state.eventDrawerVisible" title="事件详情" width="520" v-if="state.selectedEvent">
            <div class="space-y-4">
                <a-descriptions bordered size="small" :column="2">
                    <a-descriptions-item label="事件ID">{{ state.selectedEvent.id }}</a-descriptions-item>
                    <a-descriptions-item label="时间">{{ state.selectedEvent.time }}</a-descriptions-item>
                    <a-descriptions-item label="类型"><a-tag :color="eventTypeColor(state.selectedEvent.type)">{{ eventTypeText(state.selectedEvent.type) }}</a-tag></a-descriptions-item>
                    <a-descriptions-item label="严重度"><a-tag :color="severityColor(state.selectedEvent.severity)">{{ severityText(state.selectedEvent.severity) }}</a-tag></a-descriptions-item>
                    <a-descriptions-item label="来源">{{ state.selectedEvent.source }}</a-descriptions-item>
                    <a-descriptions-item label="目标">{{ state.selectedEvent.target }}</a-descriptions-item>
                    <a-descriptions-item label="动作" :span="2">{{ state.selectedEvent.action }}</a-descriptions-item>
                </a-descriptions>
                <div>
                    <div class="text-sm font-bold text-gray-800 mb-2">详细描述</div>
                    <div class="bg-gray-50 rounded-lg p-4 text-sm text-gray-600 leading-relaxed">{{ state.selectedEvent.detail }}</div>
                </div>
            </div>
        </a-drawer>
    </div>
</template>
