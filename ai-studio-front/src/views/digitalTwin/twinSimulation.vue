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
    RocketOutlined,
    PlusOutlined,
    EditOutlined,
    DeleteOutlined,
    SettingOutlined
} from '@ant-design/icons-vue';
import { useChatStore } from '@/store/modules/chat.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';

const chatStore = useChatStore();
const barChartRef = ref(null);
let barChartInstance = null;

const FAULT_TYPES = ['单相接地', '两相接地短路', '三相短路', '计划检修', '极端天气', '自定义'];
const METHODS = ['N-1校核', 'N-2校核', '全网潮流分析', '暂态稳定分析', '电压稳定分析'];
const PRESET_COLORS = ['#ff4d4f', '#faad14', '#722ed1', '#1677ff', '#52c41a', '#13c2c2'];

const state = reactive({
    selectedScenario: null,
    running: false,
    resultVisible: false,
    currentResult: null,
    scenarioDialogVisible: false,
    editingScenarioIdx: null,
    scenarioForm: {
        title: '', description: '', faultType: '单相接地',
        faultLine: '', equipment: '', method: 'N-1校核', condition: '', color: '#ff4d4f'
    }
});

const iconForFaultType = (ft) => ({
    '单相接地': ThunderboltOutlined, '两相接地短路': ThunderboltOutlined,
    '三相短路': ThunderboltOutlined, '计划检修': AlertOutlined,
    '极端天气': RocketOutlined, '自定义': ExperimentOutlined
}[ft] || ExperimentOutlined);

// 可编辑推演场景列表
const scenarios = ref([
    {
        id: 'sc_1', title: '220kV线路N-1故障推演',
        description: '模拟指定220kV线路跳闸后，分析潮流重新分布、相邻线路负荷变化及系统稳定性',
        icon: ThunderboltOutlined, color: '#ff4d4f',
        params: { faultType: '单相接地', faultLine: '220kV宝华线', equipment: '', method: 'N-1校核', condition: '' }
    },
    {
        id: 'sc_2', title: '主变N-1检修推演',
        description: '模拟主变压器计划检修停运后，剩余主变承载全部负荷的运行状态和风险评估',
        icon: AlertOutlined, color: '#faad14',
        params: { faultType: '计划检修', faultLine: '', equipment: '1号主变', method: 'N-1校核', condition: '' }
    },
    {
        id: 'sc_3', title: '极端高温大负荷推演',
        description: '模拟夏季极端高温条件下全网负荷达到历史最高值时的运行状态和薄弱环节',
        icon: RocketOutlined, color: '#722ed1',
        params: { faultType: '极端天气', faultLine: '', equipment: '', method: '全网潮流分析', condition: '夏季极端高温40°C' }
    }
]);

const selectScenario = (sc) => {
    state.selectedScenario = sc;
    state.currentResult = null;
};

const openNewScenario = () => {
    state.editingScenarioIdx = null;
    state.scenarioForm = { title: '', description: '', faultType: '单相接地', faultLine: '', equipment: '', method: 'N-1校核', condition: '', color: '#ff4d4f' };
    state.scenarioDialogVisible = true;
};

const openEditScenario = (sc, idx) => {
    state.editingScenarioIdx = idx;
    state.scenarioForm = {
        title: sc.title, description: sc.description,
        faultType: sc.params.faultType, faultLine: sc.params.faultLine || '',
        equipment: sc.params.equipment || '', method: sc.params.method,
        condition: sc.params.condition || '', color: sc.color
    };
    state.scenarioDialogVisible = true;
};

const handleSaveScenario = () => {
    const f = state.scenarioForm;
    if (!f.title.trim()) { ElMessage.warning('请输入场景名称'); return; }
    const sc = {
        id: state.editingScenarioIdx !== null ? scenarios.value[state.editingScenarioIdx].id : ('sc_' + Date.now()),
        title: f.title.trim(), description: f.description.trim(),
        icon: iconForFaultType(f.faultType), color: f.color,
        params: { faultType: f.faultType, faultLine: f.faultLine, equipment: f.equipment, method: f.method, condition: f.condition }
    };
    if (state.editingScenarioIdx !== null) {
        scenarios.value[state.editingScenarioIdx] = sc;
        if (state.selectedScenario?.id === sc.id) state.selectedScenario = sc;
        ElMessage.success('场景已更新');
    } else {
        scenarios.value.push(sc);
        ElMessage.success('场景已创建');
    }
    state.scenarioDialogVisible = false;
};

const handleDeleteScenario = (sc, idx) => {
    ElMessageBox.confirm(`确认删除场景「${sc.title}」？`, '提示', { type: 'warning' })
        .then(() => {
            scenarios.value.splice(idx, 1);
            if (state.selectedScenario?.id === sc.id) { state.selectedScenario = null; state.currentResult = null; }
            ElMessage.success('场景已删除');
        }).catch(() => {});
};

// 根据参数动态生成Mock推演结果
const generateSimResult = (sc) => {
    const { faultType, faultLine, equipment, method, condition } = sc.params;
    const faultName = faultLine || equipment || '目标设备';
    const riskMap = { '单相接地': '低风险', '两相接地短路': '中风险', '三相短路': '高风险', '计划检修': '中风险', '极端天气': '中风险', '自定义': '低风险' };
    const riskLevel = riskMap[faultType] || '低风险';

    const r = () => Math.floor(Math.random() * 15);
    const b1 = 50 + r(), a1 = b1 + 15 + r();
    const b2 = 45 + r(), a2 = b2 + 12 + r();
    const affectedLines = [
        { name: faultLine ? `${faultLine}相邻线路` : (equipment ? `${equipment}出线` : '主干线路A'), beforeLoad: `${b1}%`, afterLoad: `${a1}%`, status: a1 >= 90 ? '预警' : '安全' },
        { name: faultLine ? '区域联络线' : (equipment ? '站内备用设备' : '主干线路B'), beforeLoad: `${b2}%`, afterLoad: `${a2}%`, status: a2 >= 90 ? '预警' : '安全' }
    ];
    if (faultType === '三相短路' || riskLevel === '高风险') {
        const b3 = 55 + r(), a3 = b3 + 20 + r();
        affectedLines.push({ name: '区域备用通道', beforeLoad: `${b3}%`, afterLoad: `${a3}%`, status: '预警' });
    }
    const hasWarning = affectedLines.some(l => l.status === '预警');
    const recommendations = [
        `立即确认${faultName}的保护动作记录及实际运行状态`,
        hasWarning ? '存在过载预警，立即启动限负荷预案并通知相关运维人员' : '加强相邻设备运行监视，关注负荷变化趋势',
        method === 'N-1校核' ? '完成N-1安全校核，评估相邻线路负荷裕度' : `按${method}要求出具仿真分析报告`
    ];
    if (condition) recommendations.push(`特别关注 ${condition} 对设备运行能力的影响`);
    return {
        summary: `${sc.title}推演完成。${faultName}发生${faultType}后，${hasWarning ? '部分设备出现过载预警，需紧急处置' : '系统运行在安全范围内，无需立即干预'}。风险等级：${riskLevel}。`,
        affectedLines, recommendations, riskLevel
    };
};

// 执行推演
const runSimulation = async () => {
    if (!state.selectedScenario) return;
    state.running = true;
    await new Promise(r => setTimeout(r, 2000 + Math.random() * 1500));

    const result = generateSimResult(state.selectedScenario);
    const newSim = {
        id: 'twin_' + Date.now(),
        title: state.selectedScenario.title,
        scenario: state.selectedScenario.description,
        user: '系统运行员',
        duration: ((2 + Math.random() * 4).toFixed(1)) + 's',
        createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        status: 'completed',
        params: { ...state.selectedScenario.params },
        result
    };
    chatStore.addTwinSimulation(newSim);
    state.currentResult = newSim;
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
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="font-bold text-gray-800 flex items-center gap-2">
                            <ThunderboltOutlined class="text-orange-500" /> 推演场景
                        </h3>
                        <a-button size="small" type="primary" @click="openNewScenario">
                            <PlusOutlined /> 新建场景
                        </a-button>
                    </div>
                    <div class="space-y-3">
                        <div v-for="(sc, idx) in scenarios" :key="sc.id"
                             class="border rounded-xl p-4 cursor-pointer transition-all group relative"
                             :class="state.selectedScenario?.id === sc.id ? 'border-purple-400 bg-purple-50 shadow-sm' : 'border-gray-200 hover:border-purple-200 hover:shadow-sm'"
                             @click="selectScenario(sc)">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-lg flex-shrink-0 flex items-center justify-center"
                                     :style="{ backgroundColor: sc.color + '18' }">
                                    <component :is="sc.icon" class="text-lg" :style="{ color: sc.color }" />
                                </div>
                                <div class="font-medium text-gray-800 text-sm flex-1 min-w-0 truncate">{{ sc.title }}</div>
                                <div class="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0" @click.stop>
                                    <a-tooltip title="编辑场景">
                                        <a-button type="text" size="small" @click="openEditScenario(sc, idx)">
                                            <EditOutlined class="text-blue-500" />
                                        </a-button>
                                    </a-tooltip>
                                    <a-tooltip title="删除场景">
                                        <a-button type="text" size="small" @click="handleDeleteScenario(sc, idx)">
                                            <DeleteOutlined class="text-red-400" />
                                        </a-button>
                                    </a-tooltip>
                                </div>
                            </div>
                            <p class="text-xs text-gray-500 leading-relaxed">{{ sc.description }}</p>
                        </div>
                        <div v-if="scenarios.length === 0" class="text-center text-gray-400 text-sm py-6">
                            暂无场景，点击「新建场景」创建
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
                    <div class="flex items-center justify-between mb-3">
                        <h3 class="font-bold text-gray-800 text-sm">当前推演参数</h3>
                        <a-button type="link" size="small" @click="openEditScenario(state.selectedScenario, scenarios.findIndex(s => s.id === state.selectedScenario.id))">
                            <EditOutlined /> 修改
                        </a-button>
                    </div>
                    <a-descriptions :column="1" size="small" bordered>
                        <a-descriptions-item label="故障类型">{{ state.selectedScenario.params.faultType }}</a-descriptions-item>
                        <a-descriptions-item v-if="state.selectedScenario.params.faultLine" label="故障线路">{{ state.selectedScenario.params.faultLine }}</a-descriptions-item>
                        <a-descriptions-item v-if="state.selectedScenario.params.equipment" label="故障设备">{{ state.selectedScenario.params.equipment }}</a-descriptions-item>
                        <a-descriptions-item label="分析方法">{{ state.selectedScenario.params.method }}</a-descriptions-item>
                        <a-descriptions-item v-if="state.selectedScenario.params.condition" label="附加条件">{{ state.selectedScenario.params.condition }}</a-descriptions-item>
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

    <!-- 场景配置弹窗 -->
    <a-modal v-model:open="state.scenarioDialogVisible"
             :title="state.editingScenarioIdx !== null ? '编辑推演场景' : '新建推演场景'"
             width="560px" @ok="handleSaveScenario" okText="保存" cancelText="取消">
        <a-form layout="vertical" class="mt-4">
            <a-form-item label="场景名称" required>
                <a-input v-model:value="state.scenarioForm.title" placeholder="如：110kV线路N-1故障推演" />
            </a-form-item>
            <a-form-item label="场景描述">
                <a-textarea v-model:value="state.scenarioForm.description" :rows="2" placeholder="简要描述该推演场景的内容和目标" />
            </a-form-item>
            <div class="grid grid-cols-2 gap-4">
                <a-form-item label="故障类型" required>
                    <a-select v-model:value="state.scenarioForm.faultType" style="width:100%">
                        <a-select-option v-for="ft in FAULT_TYPES" :key="ft" :value="ft">{{ ft }}</a-select-option>
                    </a-select>
                </a-form-item>
                <a-form-item label="分析方法" required>
                    <a-select v-model:value="state.scenarioForm.method" style="width:100%">
                        <a-select-option v-for="m in METHODS" :key="m" :value="m">{{ m }}</a-select-option>
                    </a-select>
                </a-form-item>
            </div>
            <div class="grid grid-cols-2 gap-4">
                <a-form-item label="故障线路">
                    <a-input v-model:value="state.scenarioForm.faultLine" placeholder="如：220kV宝华线" />
                </a-form-item>
                <a-form-item label="故障设备">
                    <a-input v-model:value="state.scenarioForm.equipment" placeholder="如：1号主变、10kV母线" />
                </a-form-item>
            </div>
            <a-form-item label="附加条件">
                <a-input v-model:value="state.scenarioForm.condition" placeholder="如：夏季极端高温40°C、台风橙色预警" />
            </a-form-item>
            <a-form-item label="标识颜色">
                <div class="flex gap-3">
                    <div v-for="c in PRESET_COLORS" :key="c"
                         class="w-7 h-7 rounded-full cursor-pointer border-2 transition-all"
                         :style="{ backgroundColor: c, borderColor: state.scenarioForm.color === c ? '#1677ff' : 'transparent', transform: state.scenarioForm.color === c ? 'scale(1.2)' : 'scale(1)' }"
                         @click="state.scenarioForm.color = c">
                    </div>
                </div>
            </a-form-item>
        </a-form>
    </a-modal>
</template>
