<script setup>
import {
    LineChartOutlined,
    DashboardOutlined,
    ExperimentOutlined,
    ThunderboltOutlined,
    ClockCircleOutlined,
    CheckCircleOutlined,
    SyncOutlined,
    FireOutlined,
    BarChartOutlined,
    ReloadOutlined,
    FullscreenOutlined,
    SwapOutlined
} from '@ant-design/icons-vue';
import { getTrainingMetrics, getInferenceMetrics, getTrainingMetricsMock, getInferenceMetricsMock, listExperimentsMock } from '@/api/modelMag/trainVis.js';
import * as echarts from 'echarts';

const lossChartRef = ref(null);
const accChartRef = ref(null);
const resourceChartRef = ref(null);
const qpsChartRef = ref(null);
const latencyChartRef = ref(null);
const compareChartRef = ref(null);
let charts = {};

const state = reactive({
    activeTab: 'training',
    loading: false,
    // training
    trainingData: null,
    // inference
    inferenceData: null,
    // experiments
    experiments: [],
    selectedExps: []
});

// ---- Training Tab ----
const loadTrainingData = async () => {
    state.loading = true;
    try {
        let res = await getTrainingMetrics('task_001').catch(() => null);
        if (!res || res.code !== 200 || !res.data?.metrics) res = await getTrainingMetricsMock('task_001');
        if (res.code === 200) {
            state.trainingData = res.data;
            nextTick(() => {
                renderLossChart();
                renderAccChart();
                renderResourceChart();
            });
        }
    } finally {
        state.loading = false;
    }
};

const renderLossChart = () => {
    if (!lossChartRef.value || !state.trainingData) return;
    charts.loss?.dispose();
    charts.loss = echarts.init(lossChartRef.value);
    const d = state.trainingData;
    const epochs = Array.from({ length: d.currentEpoch }, (_, i) => i + 1);
    charts.loss.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['训练Loss', '验证Loss'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
        xAxis: { type: 'category', data: epochs, name: 'Epoch' },
        yAxis: { type: 'value', name: 'Loss', splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
            { name: '训练Loss', type: 'line', smooth: true, data: d.metrics.loss, lineStyle: { width: 2 }, itemStyle: { color: '#1677ff' }, areaStyle: { opacity: 0.08 } },
            { name: '验证Loss', type: 'line', smooth: true, data: d.metrics.valLoss, lineStyle: { width: 2, type: 'dashed' }, itemStyle: { color: '#ff4d4f' }, areaStyle: { opacity: 0.05 } }
        ]
    });
};

const renderAccChart = () => {
    if (!accChartRef.value || !state.trainingData) return;
    charts.acc?.dispose();
    charts.acc = echarts.init(accChartRef.value);
    const d = state.trainingData;
    const epochs = Array.from({ length: d.currentEpoch }, (_, i) => i + 1);
    charts.acc.setOption({
        tooltip: { trigger: 'axis', formatter: (params) => params.map(p => `${p.seriesName}: ${(p.value * 100).toFixed(2)}%`).join('<br/>') },
        legend: { data: ['训练Accuracy', '验证Accuracy'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
        xAxis: { type: 'category', data: epochs, name: 'Epoch' },
        yAxis: { type: 'value', name: 'Accuracy', min: 0, max: 1, splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
            { name: '训练Accuracy', type: 'line', smooth: true, data: d.metrics.accuracy, lineStyle: { width: 2 }, itemStyle: { color: '#52c41a' }, areaStyle: { opacity: 0.08 } },
            { name: '验证Accuracy', type: 'line', smooth: true, data: d.metrics.valAccuracy, lineStyle: { width: 2, type: 'dashed' }, itemStyle: { color: '#722ed1' }, areaStyle: { opacity: 0.05 } }
        ]
    });
};

const renderResourceChart = () => {
    if (!resourceChartRef.value || !state.trainingData) return;
    charts.resource?.dispose();
    charts.resource = echarts.init(resourceChartRef.value);
    const d = state.trainingData;
    const epochs = Array.from({ length: d.currentEpoch }, (_, i) => i + 1);
    charts.resource.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['GPU利用率', 'GPU显存', 'GPU温度'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
        xAxis: { type: 'category', data: epochs, name: 'Epoch' },
        yAxis: [
            { type: 'value', name: '%', max: 100, splitLine: { lineStyle: { type: 'dashed' } } },
            { type: 'value', name: '°C', max: 100, splitLine: { show: false } }
        ],
        series: [
            { name: 'GPU利用率', type: 'line', smooth: true, data: d.resourceUsage.gpuUtil, itemStyle: { color: '#722ed1' }, areaStyle: { opacity: 0.1 } },
            { name: 'GPU显存', type: 'line', smooth: true, data: d.resourceUsage.gpuMem, itemStyle: { color: '#13c2c2' }, areaStyle: { opacity: 0.1 } },
            { name: 'GPU温度', type: 'line', smooth: true, data: d.resourceUsage.gpuTemp, yAxisIndex: 1, itemStyle: { color: '#ff4d4f' }, lineStyle: { type: 'dashed' } }
        ]
    });
};

// ---- Inference Tab ----
const loadInferenceData = async () => {
    state.loading = true;
    try {
        let res = await getInferenceMetrics('service_001').catch(() => null);
        if (!res || res.code !== 200 || !res.data?.timeSeries) res = await getInferenceMetricsMock('service_001');
        if (res.code === 200) {
            state.inferenceData = res.data;
            nextTick(() => {
                renderQpsChart();
                renderLatencyChart();
            });
        }
    } finally {
        state.loading = false;
    }
};

const renderQpsChart = () => {
    if (!qpsChartRef.value || !state.inferenceData) return;
    charts.qps?.dispose();
    charts.qps = echarts.init(qpsChartRef.value);
    const d = state.inferenceData;
    charts.qps.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['QPS', '错误数'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
        xAxis: { type: 'category', data: d.timePoints, axisLabel: { interval: 9 } },
        yAxis: [
            { type: 'value', name: 'QPS', splitLine: { lineStyle: { type: 'dashed' } } },
            { type: 'value', name: '错误数', splitLine: { show: false } }
        ],
        series: [
            { name: 'QPS', type: 'line', smooth: true, data: d.timeSeries.qps, itemStyle: { color: '#1677ff' }, areaStyle: { opacity: 0.1 } },
            { name: '错误数', type: 'bar', data: d.timeSeries.errorCount, yAxisIndex: 1, itemStyle: { color: '#ff4d4f', opacity: 0.7 }, barWidth: 4 }
        ]
    });
};

const renderLatencyChart = () => {
    if (!latencyChartRef.value || !state.inferenceData) return;
    charts.latency?.dispose();
    charts.latency = echarts.init(latencyChartRef.value);
    const d = state.inferenceData;
    charts.latency.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['延迟(ms)', 'GPU利用率(%)'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '8%', containLabel: true },
        xAxis: { type: 'category', data: d.timePoints, axisLabel: { interval: 9 } },
        yAxis: [
            { type: 'value', name: 'ms', splitLine: { lineStyle: { type: 'dashed' } } },
            { type: 'value', name: '%', max: 100, splitLine: { show: false } }
        ],
        series: [
            { name: '延迟(ms)', type: 'line', smooth: true, data: d.timeSeries.latency, itemStyle: { color: '#faad14' }, areaStyle: { opacity: 0.1 } },
            { name: 'GPU利用率(%)', type: 'line', smooth: true, data: d.timeSeries.gpuUtil, yAxisIndex: 1, itemStyle: { color: '#722ed1' }, lineStyle: { type: 'dashed' } }
        ]
    });
};

// ---- Experiment Comparison ----
const loadExperiments = async () => {
    const res = await listExperimentsMock();
    if (res.code === 200) {
        state.experiments = res.data;
        state.selectedExps = res.data.map(e => e.id);
    }
};

const handleTabChange = (key) => {
    state.activeTab = key;
    // 先清理所有图表
    Object.values(charts).forEach(c => c?.dispose());
    charts = {};
    // 再加载新数据
    nextTick(() => {
        if (key === 'training') loadTrainingData();
        else if (key === 'inference') loadInferenceData();
        else if (key === 'experiments') loadExperiments();
    });
};

const handleResize = () => {
    Object.values(charts).forEach(c => c?.resize());
};

onMounted(() => {
    loadTrainingData();
    window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    Object.values(charts).forEach(c => c?.dispose());
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <a-tabs v-model:activeKey="state.activeTab" @change="handleTabChange" size="large">
            <!-- ===== 训练过程监控 ===== -->
            <a-tab-pane key="training">
                <template #tab>
                    <span class="flex items-center gap-1"><LineChartOutlined /> 训练过程监控</span>
                </template>

                <div v-if="state.trainingData">
                    <!-- 训练概要 -->
                    <div class="grid grid-cols-6 gap-4 mb-6">
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">训练进度</div>
                            <div class="text-lg font-bold text-blue-600">{{ state.trainingData.currentEpoch }}/{{ state.trainingData.totalEpochs }}</div>
                            <el-progress :percentage="Math.round(state.trainingData.currentEpoch / state.trainingData.totalEpochs * 100)" :stroke-width="4" :show-text="false" class="mt-2" />
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">当前Loss</div>
                            <div class="text-lg font-bold text-orange-500">{{ state.trainingData.metrics.loss[state.trainingData.currentEpoch - 1] }}</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">当前Accuracy</div>
                            <div class="text-lg font-bold text-green-500">{{ (state.trainingData.metrics.accuracy[state.trainingData.currentEpoch - 1] * 100).toFixed(2) }}%</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">最佳Epoch</div>
                            <div class="text-lg font-bold text-purple-500">#{{ state.trainingData.bestMetrics.bestEpoch }}</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">已用时间</div>
                            <div class="text-lg font-bold text-gray-700">{{ state.trainingData.elapsedTime }}</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">预计剩余</div>
                            <div class="text-lg font-bold text-gray-700">{{ state.trainingData.estimatedRemaining }}</div>
                        </div>
                    </div>

                    <!-- Loss & Accuracy 图表 -->
                    <div class="grid grid-cols-2 gap-6 mb-6">
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                            <div class="flex items-center gap-2 mb-3">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                <span class="font-bold text-gray-800">Loss曲线</span>
                                <a-tag color="blue" size="small">实时</a-tag>
                            </div>
                            <div ref="lossChartRef" style="height: 300px;"></div>
                        </div>
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                            <div class="flex items-center gap-2 mb-3">
                                <span class="w-1 h-5 bg-green-600 rounded-full"></span>
                                <span class="font-bold text-gray-800">Accuracy曲线</span>
                                <a-tag color="green" size="small">实时</a-tag>
                            </div>
                            <div ref="accChartRef" style="height: 300px;"></div>
                        </div>
                    </div>

                    <!-- 资源消耗 -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <div class="flex items-center gap-2 mb-3">
                            <span class="w-1 h-5 bg-purple-600 rounded-full"></span>
                            <span class="font-bold text-gray-800">GPU资源消耗</span>
                        </div>
                        <div ref="resourceChartRef" style="height: 280px;"></div>
                    </div>
                </div>
                <div v-else class="flex items-center justify-center py-20">
                    <a-spin size="large" tip="加载训练数据..." />
                </div>
            </a-tab-pane>

            <!-- ===== 推理性能分析 ===== -->
            <a-tab-pane key="inference">
                <template #tab>
                    <span class="flex items-center gap-1"><DashboardOutlined /> 推理性能分析</span>
                </template>

                <div v-if="state.inferenceData">
                    <!-- 推理概要 -->
                    <div class="grid grid-cols-6 gap-4 mb-6">
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">总请求数</div>
                            <div class="text-lg font-bold text-blue-600">{{ (state.inferenceData.summary.totalRequests / 1000).toFixed(1) }}K</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">平均QPS</div>
                            <div class="text-lg font-bold text-green-500">{{ state.inferenceData.summary.avgQps }}</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">P50延迟</div>
                            <div class="text-lg font-bold text-orange-500">{{ state.inferenceData.summary.p50Latency }}ms</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">P99延迟</div>
                            <div class="text-lg font-bold text-red-500">{{ state.inferenceData.summary.p99Latency }}ms</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">成功率</div>
                            <div class="text-lg font-bold text-green-500">{{ state.inferenceData.summary.successRate }}%</div>
                        </div>
                        <div class="bg-white rounded-xl border border-gray-100 p-4 shadow-sm text-center">
                            <div class="text-xs text-gray-400 mb-1">运行时长</div>
                            <div class="text-lg font-bold text-gray-700">{{ state.inferenceData.uptime }}</div>
                        </div>
                    </div>

                    <!-- QPS & 延迟图表 -->
                    <div class="grid grid-cols-2 gap-6">
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                            <div class="flex items-center gap-2 mb-3">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                <span class="font-bold text-gray-800">QPS & 错误数</span>
                            </div>
                            <div ref="qpsChartRef" style="height: 300px;"></div>
                        </div>
                        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                            <div class="flex items-center gap-2 mb-3">
                                <span class="w-1 h-5 bg-orange-600 rounded-full"></span>
                                <span class="font-bold text-gray-800">延迟 & GPU利用率</span>
                            </div>
                            <div ref="latencyChartRef" style="height: 300px;"></div>
                        </div>
                    </div>
                </div>
                <div v-else class="flex items-center justify-center py-20">
                    <a-spin size="large" tip="加载推理数据..." />
                </div>
            </a-tab-pane>

            <!-- ===== 实验对比 ===== -->
            <a-tab-pane key="experiments">
                <template #tab>
                    <span class="flex items-center gap-1"><ExperimentOutlined /> 实验对比</span>
                </template>

                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="flex items-center justify-between mb-4">
                        <div class="flex items-center gap-2">
                            <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                            <span class="font-bold text-gray-800">训练实验记录</span>
                        </div>
                        <a-button @click="loadExperiments"><ReloadOutlined /> 刷新</a-button>
                    </div>

                    <a-table :dataSource="state.experiments" :pagination="false" rowKey="id" size="middle">
                        <a-table-column title="实验名称" dataIndex="name" :width="180">
                            <template #default="{ record }">
                                <span class="font-medium text-blue-600">{{ record.name }}</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="模型" dataIndex="model" :width="120" align="center">
                            <template #default="{ record }"><a-tag>{{ record.model }}</a-tag></template>
                        </a-table-column>
                        <a-table-column title="数据集" dataIndex="dataset" :width="180" :ellipsis="true" />
                        <a-table-column title="Epochs" dataIndex="epochs" :width="80" align="center" />
                        <a-table-column title="最佳Accuracy" dataIndex="bestAcc" :width="130" align="center">
                            <template #default="{ record }">
                                <span class="font-bold text-green-600">{{ (record.bestAcc * 100).toFixed(1) }}%</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="最佳Loss" dataIndex="bestLoss" :width="100" align="center">
                            <template #default="{ record }">
                                <span class="font-bold text-orange-600">{{ record.bestLoss }}</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="学习率" dataIndex="lr" :width="100" align="center">
                            <template #default="{ record }"><span class="font-mono text-xs">{{ record.lr }}</span></template>
                        </a-table-column>
                        <a-table-column title="Batch" dataIndex="batchSize" :width="70" align="center" />
                        <a-table-column title="耗时" dataIndex="duration" :width="100" align="center" />
                        <a-table-column title="状态" :width="100" align="center">
                            <template #default="{ record }">
                                <a-tag :color="record.status === 'completed' ? 'green' : 'blue'">
                                    <template v-if="record.status === 'running'"><SyncOutlined :spin="true" /> 训练中</template>
                                    <template v-else><CheckCircleOutlined /> 已完成</template>
                                </a-tag>
                            </template>
                        </a-table-column>
                    </a-table>
                </div>
            </a-tab-pane>
        </a-tabs>
    </div>
</template>
