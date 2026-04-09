<script setup>
import {
    ExperimentOutlined,
    ThunderboltOutlined,
    PlayCircleOutlined,
    CheckCircleOutlined,
    WarningOutlined,
    ClockCircleOutlined,
    FileSearchOutlined,
    ReloadOutlined,
    BarChartOutlined,
    AlertOutlined,
    RocketOutlined
} from '@ant-design/icons-vue';
import { useChatStore } from '@/store/modules/chat.js';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const chatStore = useChatStore();
const barChartRef = ref(null);
let barChartInstance = null;

const state = reactive({
    selectedScenario: null,
    running: false,
    resultVisible: false,
    currentResult: null
});

// 预置推演场景
const scenarios = [
    {
        id: 'sc_1',
        title: '220kV线路N-1故障推演',
        description: '模拟指定220kV线路跳闸后，分析潮流重新分布、相邻线路负荷变化及系统稳定性',
        icon: ThunderboltOutlined,
        color: '#ff4d4f',
        params: { faultType: '单相接地', faultLine: '220kV宝华线', method: 'N-1校核' }
    },
    {
        id: 'sc_2',
        title: '主变N-1检修推演',
        description: '模拟主变压器计划检修停运后，剩余主变承载全部负荷的运行状态和风险评估',
        icon: AlertOutlined,
        color: '#faad14',
        params: { faultType: '计划检修', equipment: '1号主变', method: 'N-1校核' }
    },
    {
        id: 'sc_3',
        title: '极端高温大负荷推演',
        description: '模拟夏季极端高温条件下全网负荷达到历史最高值时的运行状态和薄弱环节',
        icon: RocketOutlined,
        color: '#722ed1',
        params: { faultType: '极端场景', condition: '夏季极端高温40°C', method: '全网潮流分析' }
    }
];

const selectScenario = (sc) => {
    state.selectedScenario = sc;
    state.currentResult = null;
};

// 执行推演（Mock：从store已有数据匹配，或生成新结果）
const runSimulation = async () => {
    if (!state.selectedScenario) return;
    state.running = true;

    // 模拟推演耗时
    await new Promise(r => setTimeout(r, 2500));

    // 查找store中已有的匹配推演结果
    const existing = chatStore.twinSimulations.find(s =>
        s.params.faultType === state.selectedScenario.params.faultType
    );

    if (existing) {
        state.currentResult = existing;
    } else {
        // 生成新推演结果
        const newSim = {
            id: 'twin_' + Date.now(),
            title: state.selectedScenario.title,
            scenario: state.selectedScenario.description,
            user: '系统运行员',
            duration: ((Math.random() * 5 + 3).toFixed(1)) + 's',
            createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
            status: 'completed',
            params: { ...state.selectedScenario.params },
            result: {
                summary: '推演完成，当前场景下系统运行在安全范围内，部分设备接近预警值。',
                affectedLines: [
                    { name: '相关线路A', beforeLoad: '65%', afterLoad: '82%', status: '安全' },
                    { name: '相关线路B', beforeLoad: '58%', afterLoad: '75%', status: '安全' }
                ],
                recommendations: ['建议加强监视相关设备负荷变化', '提前准备应急限负荷预案'],
                riskLevel: '低风险'
            }
        };
        chatStore.addTwinSimulation(newSim);
        state.currentResult = newSim;
    }

    state.running = false;
    state.resultVisible = true;
    ElMessage.success('推演完成');

    nextTick(() => renderBarChart());
};

const riskColor = (level) => {
    return { '高风险': '#ff4d4f', '中风险': '#faad14', '低风险': '#52c41a' }[level] || '#1677ff';
};

const riskTagColor = (level) => {
    return { '高风险': 'red', '中风险': 'orange', '低风险': 'green' }[level] || 'default';
};

const renderBarChart = () => {
    if (!barChartRef.value || !state.currentResult?.result?.affectedLines) return;
    if (barChartInstance) barChartInstance.dispose();
    barChartInstance = echarts.init(barChartRef.value);

    const lines = state.currentResult.result.affectedLines;
    barChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['故障前', '故障后'], top: 5 },
        grid: { left: 10, right: 20, bottom: 10, top: 40, containLabel: true },
        xAxis: { type: 'category', data: lines.map(l => l.name), axisLabel: { fontSize: 11 } },
        yAxis: { type: 'value', name: '负荷率(%)', max: 120 },
        series: [
            {
                name: '故障前', type: 'bar', barWidth: 24,
                data: lines.map(l => parseFloat(l.beforeLoad)),
                itemStyle: { color: '#91d5ff', borderRadius: [4, 4, 0, 0] }
            },
            {
                name: '故障后', type: 'bar', barWidth: 24,
                data: lines.map(l => parseFloat(l.afterLoad)),
                itemStyle: {
                    color: (params) => {
                        const val = params.value;
                        return val >= 100 ? '#ff4d4f' : val >= 90 ? '#faad14' : '#52c41a';
                    },
                    borderRadius: [4, 4, 0, 0]
                }
            }
        ]
    });
};

// 历史推演记录
const historySimulations = computed(() => chatStore.twinSimulations);

const viewHistoryResult = (sim) => {
    state.currentResult = sim;
    state.resultVisible = true;
    nextTick(() => renderBarChart());
};

onBeforeUnmount(() => {
    if (barChartInstance) barChartInstance.dispose();
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                <ExperimentOutlined class="text-purple-500" />
                孪生推演
            </h2>
            <p class="text-gray-500 text-sm">基于数字孪生模型进行电网故障仿真推演，分析潮流变化与风险评估</p>
        </div>

        <div class="grid grid-cols-3 gap-6">
            <!-- 左侧：推演场景选择 -->
            <div class="col-span-1 space-y-4">
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <h3 class="font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <ThunderboltOutlined class="text-orange-500" /> 选择推演场景
                    </h3>
                    <div class="space-y-3">
                        <div v-for="sc in scenarios" :key="sc.id"
                             class="border rounded-xl p-4 cursor-pointer transition-all"
                             :class="state.selectedScenario?.id === sc.id ? 'border-purple-400 bg-purple-50 shadow-sm' : 'border-gray-200 hover:border-purple-200 hover:shadow-sm'"
                             @click="selectScenario(sc)">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-lg flex items-center justify-center"
                                     :style="{ backgroundColor: sc.color + '15' }">
                                    <component :is="sc.icon" class="text-lg" :style="{ color: sc.color }" />
                                </div>
                                <div class="font-medium text-gray-800 text-sm">{{ sc.title }}</div>
                            </div>
                            <p class="text-xs text-gray-500 leading-relaxed">{{ sc.description }}</p>
                        </div>
                    </div>

                    <a-button type="primary" block size="large" class="mt-4"
                              :loading="state.running"
                              :disabled="!state.selectedScenario"
                              @click="runSimulation">
                        <PlayCircleOutlined /> {{ state.running ? '推演计算中...' : '开始推演' }}
                    </a-button>
                </div>

                <!-- 推演参数 -->
                <div v-if="state.selectedScenario" class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <h3 class="font-bold text-gray-800 mb-3 text-sm">推演参数</h3>
                    <a-descriptions :column="1" size="small" bordered>
                        <a-descriptions-item v-for="(val, key) in state.selectedScenario.params" :key="key" :label="key">
                            {{ val }}
                        </a-descriptions-item>
                    </a-descriptions>
                </div>
            </div>

            <!-- 右侧：推演结果 + 历史 -->
            <div class="col-span-2 space-y-4">
                <!-- 推演结果 -->
                <div v-if="state.currentResult" class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="font-bold text-gray-800 flex items-center gap-2">
                            <BarChartOutlined class="text-blue-500" /> 推演结果
                        </h3>
                        <a-tag :color="riskTagColor(state.currentResult.result.riskLevel)" size="large">
                            {{ state.currentResult.result.riskLevel }}
                        </a-tag>
                    </div>

                    <!-- 结论 -->
                    <div class="rounded-xl p-4 mb-4 border"
                         :style="{ backgroundColor: riskColor(state.currentResult.result.riskLevel) + '08',
                                   borderColor: riskColor(state.currentResult.result.riskLevel) + '30' }">
                        <div class="text-sm text-gray-700 leading-relaxed">{{ state.currentResult.result.summary }}</div>
                    </div>

                    <!-- 负荷对比图 -->
                    <div ref="barChartRef" style="height: 260px" class="mb-4"></div>

                    <!-- 影响设备表格 -->
                    <a-table :dataSource="state.currentResult.result.affectedLines" :pagination="false" size="small" rowKey="name" class="mb-4">
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
                    <div class="bg-blue-50 rounded-xl p-4 border border-blue-100">
                        <div class="text-sm font-bold text-gray-800 mb-2">建议措施</div>
                        <div v-for="(rec, rIdx) in state.currentResult.result.recommendations" :key="rIdx"
                             class="flex items-start gap-2 text-sm text-gray-700 mb-1.5 last:mb-0">
                            <span class="text-blue-500 font-bold">{{ rIdx + 1 }}.</span>
                            <span>{{ rec }}</span>
                        </div>
                    </div>
                </div>

                <!-- 未选择场景时的提示 -->
                <div v-if="!state.currentResult" class="bg-white rounded-xl shadow-sm border border-gray-100 p-12 flex flex-col items-center justify-center text-gray-400">
                    <ExperimentOutlined class="text-5xl mb-4 text-gray-300" />
                    <div class="text-lg mb-1">选择场景并开始推演</div>
                    <div class="text-sm">从左侧选择一个推演场景，点击"开始推演"查看仿真结果</div>
                </div>

                <!-- 历史推演记录 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <h3 class="font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <ClockCircleOutlined class="text-gray-400" /> 历史推演记录
                    </h3>
                    <a-table :dataSource="historySimulations" :pagination="false" size="small" rowKey="id">
                        <a-table-column title="推演场景" dataIndex="title" :width="250" />
                        <a-table-column title="风险等级" :width="100" align="center">
                            <template #default="{ record }">
                                <a-tag :color="riskTagColor(record.result.riskLevel)" size="small">{{ record.result.riskLevel }}</a-tag>
                            </template>
                        </a-table-column>
                        <a-table-column title="操作员" dataIndex="user" :width="100" align="center" />
                        <a-table-column title="耗时" dataIndex="duration" :width="80" align="center" />
                        <a-table-column title="时间" dataIndex="createTime" :width="170" align="center">
                            <template #default="{ record }">
                                <span class="text-xs text-gray-500"><ClockCircleOutlined /> {{ record.createTime }}</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="操作" :width="80" align="center">
                            <template #default="{ record }">
                                <a-button type="link" size="small" @click="viewHistoryResult(record)">
                                    <FileSearchOutlined /> 查看
                                </a-button>
                            </template>
                        </a-table-column>
                    </a-table>
                </div>
            </div>
        </div>
    </div>
</template>
