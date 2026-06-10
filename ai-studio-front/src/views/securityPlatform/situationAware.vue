<script setup>
import {
    AlertOutlined, EyeOutlined, ThunderboltOutlined, SafetyCertificateOutlined,
    ClockCircleOutlined, WarningOutlined, CheckCircleOutlined, CloseCircleOutlined,
    BellOutlined, NodeIndexOutlined, FileProtectOutlined, AimOutlined,
    RocketOutlined, BulbOutlined, HistoryOutlined, GlobalOutlined,
    DatabaseOutlined, SearchOutlined, SyncOutlined, FireOutlined,
    FileTextOutlined, BarChartOutlined, InfoCircleOutlined
} from '@ant-design/icons-vue';
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useSecurityStore } from '@/store/modules/security.js';

const securityStore = useSecurityStore();

const state = reactive({
    activeTab: 'overview',
    planDrawerVisible: false,
    selectedPlan: null,
    eventDrawerVisible: false,
    selectedEvent: null,
    generatingPlan: false,
});

const completedAttacks = computed(() => securityStore.completedAttacks);
const defenseResults = computed(() => securityStore.defenseResultsSorted);

const parseTime = (value, idx = 0) => {
    if (!value) {
        const fallback = new Date(Date.now() + idx * 60000);
        return fallback;
    }
    const date = new Date(value.replace(/-/g, '/'));
    if (Number.isNaN(date.getTime())) {
        const fallback = new Date(Date.now() + idx * 60000);
        return fallback;
    }
    return date;
};

const formatHour = (date) => `${String(date.getHours()).padStart(2, '0')}:00`;

const attackChainEvents = computed(() => {
    const events = [];
    completedAttacks.value.forEach((task, idx) => {
        const runTime = parseTime(task.createTime, idx);
        events.push({
            id: `${task.id}-attack`,
            time: runTime.toISOString().slice(0, 19).replace('T', ' '),
            type: 'attack',
            severity: task.strength === '高' ? 'high' : task.strength === '低' ? 'low' : 'medium',
            source: task.method,
            target: task.targetKb || '目标知识库',
            action: '毒化文档注入',
            detail: `注入 ${task.poisonDocs?.length || 0} 篇毒化文档，目标主题：${task.targetTopic || '未指定'}`,
            status: 'detected',
        });
    });

    defenseResults.value.forEach((result, idx) => {
        const metrics = result.metrics || {};
        const runTime = parseTime(result.createdAt, completedAttacks.value.length + idx);
        events.push({
            id: `${result.id}-defense`,
            time: runTime.toISOString().slice(0, 19).replace('T', ' '),
            type: 'defense',
            severity: metrics.asrAfter && metrics.asrAfter < 10 ? 'low' : 'medium',
            source: '组合护栏',
            target: '候选证据集',
            action: `护栏拦截 ${metrics.blocked || 0} 篇毒化证据`,
            detail: `检索护栏过滤 ${metrics.retrievalBlocked || 0} 篇，输出重写 ${metrics.outputRewritten || 0} 次`,
            status: 'blocked',
        });
        if ((metrics.filtered || 0) > 0) {
            events.push({
                id: `${result.id}-anomaly`,
                time: runTime.toISOString().slice(0, 19).replace('T', ' '),
                type: 'anomaly',
                severity: metrics.filtered > 2 ? 'high' : 'medium',
                source: '护栏检测',
                target: '日志分析',
                action: '发现可疑证据',
                detail: `检测到 ${metrics.filtered} 篇潜在异常证据需人工复核`,
                status: 'warning',
            });
        }
    });

    if (!events.length) {
        const now = new Date();
        events.push({
            id: 'EVT-DEFAULT',
            time: now.toISOString().slice(0, 19).replace('T', ' '),
            type: 'attack',
            severity: 'low',
            source: '监控',
            target: '系统',
            action: '暂无实战数据',
            detail: '请先在“攻击验证与风险复现”模块执行攻击任务',
            status: 'detected',
        });
    }

    return events.sort((a, b) => parseTime(a.time).getTime() - parseTime(b.time).getTime());
});

const eventCategoryCounts = computed(() => {
    const counts = { attack: 0, defense: 0, anomaly: 0 };
    attackChainEvents.value.forEach((evt) => {
        if (evt.type === 'attack') counts.attack += 1;
        if (evt.type === 'defense') counts.defense += 1;
        if (evt.type === 'anomaly') counts.anomaly += 1;
    });
    counts.normal = Math.max(0, counts.defense - counts.anomaly);
    return counts;
});

const timelineBuckets = computed(() => {
    const bucketMap = new Map();
    attackChainEvents.value.forEach((evt, idx) => {
        const label = formatHour(parseTime(evt.time, idx));
        if (!bucketMap.has(label)) {
            bucketMap.set(label, { attack: 0, defense: 0, anomaly: 0 });
        }
        const bucket = bucketMap.get(label);
        if (evt.type === 'attack') bucket.attack += 1;
        if (evt.type === 'defense') bucket.defense += 1;
        if (evt.type === 'anomaly') bucket.anomaly += 1;
    });
    if (!bucketMap.size) {
        bucketMap.set('00:00', { attack: 0, defense: 0, anomaly: 0 });
    }
    const labels = Array.from(bucketMap.keys()).sort();
    return {
        labels,
        attack: labels.map((label) => bucketMap.get(label).attack),
        defense: labels.map((label) => bucketMap.get(label).defense),
        anomaly: labels.map((label) => bucketMap.get(label).anomaly),
    };
});

const eventPieData = computed(() => {
    const counts = eventCategoryCounts.value;
    return [
        { value: counts.attack, name: '攻击事件', itemStyle: { color: '#ff4d4f' } },
        { value: counts.defense, name: '防御触发', itemStyle: { color: '#52c41a' } },
        { value: counts.anomaly, name: '异常告警', itemStyle: { color: '#faad14' } },
        { value: Math.max(0, counts.normal), name: '正常事件', itemStyle: { color: '#1677ff' } },
    ];
});

const aggregateMetrics = computed(() => defenseResults.value.reduce((acc, item) => {
    const metrics = item.metrics || {};
    acc.totalEvidence += metrics.totalEvidence || 0;
    acc.blocked += metrics.blocked || 0;
    acc.passed += metrics.passed || 0;
    acc.filtered += metrics.filtered || 0;
    acc.inputBlocked += metrics.inputBlocked || 0;
    acc.retrievalBlocked += metrics.retrievalBlocked || 0;
    acc.outputRewritten += metrics.outputRewritten || 0;
    acc.peakGpu = Math.max(acc.peakGpu, metrics.peakGpu || 0);
    acc.asrBefore.push(metrics.asrBefore || 0);
    acc.asrAfter.push(metrics.asrAfter || 0);
    return acc;
}, {
    totalEvidence: 0,
    blocked: 0,
    passed: 0,
    filtered: 0,
    inputBlocked: 0,
    retrievalBlocked: 0,
    outputRewritten: 0,
    peakGpu: 0,
    asrBefore: [],
    asrAfter: [],
}));

const overviewStats = computed(() => {
    const counts = eventCategoryCounts.value;
    const totals = aggregateMetrics.value;
    const totalEvents = counts.attack + counts.defense + counts.anomaly + counts.normal;
    const blockedRate = totals.totalEvidence
        ? Math.round((totals.blocked / totals.totalEvidence) * 100)
        : (defenseResults.value.length ? 90 : 0);
    const riskLevel = blockedRate >= 90 ? '低风险' : blockedRate >= 80 ? '中风险' : '高风险';
    const last24hAttacks = Math.min(counts.attack, completedAttacks.value.length);
    return {
        riskLevel,
        totalEvents,
        attackEvents: counts.attack,
        defenseEvents: counts.defense,
        anomalyEvents: counts.anomaly,
        normalEvents: counts.normal,
        last24hAttacks,
        blockedRate,
    };
});

const calcRiskScore = (blocked, total, bias = 0) => {
    if (!total) return 30 + bias;
    const ratio = blocked / total;
    return Math.min(100, Math.max(15, Math.round((1 - ratio) * 100 + bias)));
};

const riskDistribution = computed(() => {
    const totals = aggregateMetrics.value;
    const totalEvidence = Math.max(1, totals.totalEvidence);
    return [
        {
            area: '知识库入口',
            risk: calcRiskScore(totals.blocked, totalEvidence, 25),
            attacks: totalEvidence,
            blocked: totals.blocked,
        },
        {
            area: '检索链路',
            risk: calcRiskScore(totals.retrievalBlocked, totalEvidence, 20),
            attacks: totalEvidence,
            blocked: totals.retrievalBlocked,
        },
        {
            area: '生成链路',
            risk: calcRiskScore(totals.outputRewritten || totals.filtered, totalEvidence, 10),
            attacks: totalEvidence,
            blocked: totals.outputRewritten,
        },
        {
            area: '输入链路',
            risk: calcRiskScore(totals.inputBlocked, completedAttacks.value.length || totalEvidence, 15),
            attacks: completedAttacks.value.length || totalEvidence,
            blocked: totals.inputBlocked,
        },
        {
            area: '输出链路',
            risk: calcRiskScore(totals.passed, totalEvidence, 5),
            attacks: totalEvidence,
            blocked: totals.passed,
        },
    ];
});

const generatedPlans = ref([]);
const planStateOverrides = reactive({});

const plans = computed(() => {
    const dynamicPlans = defenseResults.value.map((result) => {
        const metrics = result.metrics || {};
        const riskLevel = metrics.asrBefore && metrics.asrBefore > 88 ? '高风险'
            : metrics.asrBefore && metrics.asrBefore > 75 ? '中风险' : '低风险';
        const defaultStatus = riskLevel === '高风险' ? 'active' : 'standby';
        const basePlan = {
            id: `PLAN-${result.id}`,
            name: `${result.attackName} 防御复盘预案`,
            trigger: `${result.attackMethod} 攻击复盘`,
            riskLevel,
            createTime: result.createdAt,
            status: defaultStatus,
            steps: [
                `复盘 ${result.attackMethod} 攻击，清理 ${metrics.blocked || 0} 篇异常证据并回滚知识库`,
                `重建向量索引，确保 OACC ≥ ${metrics.oacc || 90}%`,
                `启用输入护栏强化模式，持续监控提示注入 ${metrics.inputBlocked || 0} 次`,
                `输出护栏维持一致性阈值 0.7，确认 ASR 降至 ${metrics.asrAfter || 5}% 以下`,
            ],
            evidence: `ASR ${metrics.asrBefore || '-'}% → ${metrics.asrAfter || '-'}%，拦截 ${metrics.blocked || 0}/${metrics.totalEvidence || 0} 条异常证据`,
        };
        return basePlan;
    });

    const manualPlans = generatedPlans.value.map((plan) => ({ ...plan }));
    const allPlans = [...manualPlans, ...dynamicPlans];
    return allPlans.map((plan) => ({
        ...plan,
        status: planStateOverrides[plan.id] || plan.status,
    }));
});

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
    planStateOverrides[plan.id] = 'active';
    ElMessage.success('预案已激活: ' + plan.name);
};

const handleGeneratePlan = () => {
    state.generatingPlan = true;
    ElMessage.info('正在分析当前态势并生成处置预案...');
    setTimeout(() => {
        state.generatingPlan = false;
        const now = new Date();
        const timeStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
        const newPlanId = 'PLAN-' + String(generatedPlans.value.length + defenseResults.value.length + 1).padStart(3, '0');
        const attackCount = attackChainEvents.value.filter(e => e.type === 'attack').length;
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
                '升级检索护栏参数：扩展因子α提升至3，Z-Score阈值降至1.8',
                '重建向量索引并恢复检索服务，启用增强监控模式',
                '生成完整攻击报告并通知安全管理员',
                '将攻击特征录入防御规则库，更新全局防护策略',
            ],
            evidence: `基于当前态势分析：检测到 ${attackCount} 次攻击事件并发，风险等级评估为高。`
        };
        generatedPlans.value.unshift(newPlan);
        planStateOverrides[newPlan.id] = 'active';
        ElMessage.success('预案生成成功！' + newPlanId + ' - ' + newPlan.name + ' 已自动激活');
    }, 2500);
};

// ====== ECharts ======
const timelineChartRef = ref(null);
const riskMapRef = ref(null);
const eventPieRef = ref(null);
let timelineChartInstance = null;
let riskMapInstance = null;
let eventPieInstance = null;

const buildTimelineOption = () => ({
    tooltip: { trigger: 'axis' },
    legend: { data: ['攻击事件', '防御触发', '异常告警'], top: 0, textStyle: { fontSize: 11 } },
    grid: { top: 30, bottom: 25, left: 45, right: 15 },
    xAxis: { type: 'category', data: timelineBuckets.value.labels, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', axisLabel: { fontSize: 10 } },
    series: [
        { name: '攻击事件', type: 'bar', stack: 'total', data: timelineBuckets.value.attack, itemStyle: { color: '#ff4d4f' } },
        { name: '防御触发', type: 'bar', stack: 'total', data: timelineBuckets.value.defense, itemStyle: { color: '#52c41a' } },
        { name: '异常告警', type: 'bar', stack: 'total', data: timelineBuckets.value.anomaly, itemStyle: { color: '#faad14' } },
    ]
});

const buildRiskMapOption = () => ({
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

const buildEventPieOption = () => ({
    tooltip: { trigger: 'item' },
    series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: eventPieData.value,
        label: { fontSize: 11 },
    }]
});

const renderTimelineChart = () => {
    if (!timelineChartRef.value) return;
    if (!timelineChartInstance) {
        timelineChartInstance = echarts.init(timelineChartRef.value);
        window.addEventListener('resize', handleResize);
    }
    timelineChartInstance.setOption(buildTimelineOption(), true);
    timelineChartInstance.resize();
};

const renderRiskChart = () => {
    if (!riskMapRef.value) return;
    if (!riskMapInstance) {
        riskMapInstance = echarts.init(riskMapRef.value);
        window.addEventListener('resize', handleResize);
    }
    riskMapInstance.setOption(buildRiskMapOption(), true);
    riskMapInstance.resize();
};

const renderEventPie = () => {
    if (!eventPieRef.value) return;
    if (!eventPieInstance) {
        eventPieInstance = echarts.init(eventPieRef.value);
        window.addEventListener('resize', handleResize);
    }
    eventPieInstance.setOption(buildEventPieOption(), true);
    eventPieInstance.resize();
};

const renderOverviewCharts = () => {
    renderTimelineChart();
    renderRiskChart();
    renderEventPie();
};

const handleResize = () => {
    if (timelineChartInstance) timelineChartInstance.resize();
    if (riskMapInstance) riskMapInstance.resize();
    if (eventPieInstance) eventPieInstance.resize();
};

onMounted(async () => {
    await Promise.all([
        securityStore.fetchAttackTasks(),
        securityStore.fetchDefenseResults(),
        securityStore.fetchSituation()
    ]);
    if (state.activeTab === 'overview') {
        nextTick(renderOverviewCharts);
    }
});

watch(() => state.activeTab, (tab) => {
    if (tab === 'overview') {
        nextTick(renderOverviewCharts);
    }
});

watch([timelineBuckets, riskDistribution, eventPieData], () => {
    if (state.activeTab === 'overview') {
        nextTick(renderOverviewCharts);
    } else {
        if (timelineChartInstance) timelineChartInstance.setOption(buildTimelineOption(), true);
        if (riskMapInstance) riskMapInstance.setOption(buildRiskMapOption(), true);
        if (eventPieInstance) eventPieInstance.setOption(buildEventPieOption(), true);
    }
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    if (timelineChartInstance) {
        timelineChartInstance.dispose();
        timelineChartInstance = null;
    }
    if (riskMapInstance) {
        riskMapInstance.dispose();
        riskMapInstance = null;
    }
    if (eventPieInstance) {
        eventPieInstance.dispose();
        eventPieInstance = null;
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
