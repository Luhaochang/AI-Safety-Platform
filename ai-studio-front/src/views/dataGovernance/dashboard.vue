<script setup>
import {
    DashboardOutlined,
    DatabaseOutlined,
    SafetyCertificateOutlined,
    AppstoreOutlined,
    FileTextOutlined,
    RiseOutlined,
    ClockCircleOutlined,
    UserOutlined
} from '@ant-design/icons-vue';
import { getAssetOverview, getAssetOverviewMock } from '@/api/dataGovernance/asset.js';
import * as echarts from 'echarts';

const levelPieRef = ref(null);
const layerBarRef = ref(null);
const trendLineRef = ref(null);
let levelChart = null;
let layerChart = null;
let trendChart = null;

const state = reactive({
    loading: false,
    overview: {
        totalAssets: 0,
        datasetCount: 0,
        modelCount: 0,
        knowledgeCount: 0,
        gradeDistribution: [],
        categoryDistribution: [],
        recentActivity: []
    }
});

const actionColorMap = { '新增': '#52c41a', '修改分级': '#1677ff', '删除': '#ff4d4f', '数据标注': '#722ed1', '入库知识': '#fa8c16' };

const loadData = async () => {
    state.loading = true;
    try {
        let res = await getAssetOverview().catch(() => null);
        if (!res || res.code !== 200 || !res.data?.totalAssets) res = await getAssetOverviewMock();
        if (res.code === 200) {
            state.overview = res.data;
            nextTick(() => {
                renderLevelPie();
                renderLayerBar();
                renderTrendLine();
            });
        }
    } finally {
        state.loading = false;
    }
};

const renderLevelPie = () => {
    if (!levelPieRef.value) return;
    if (levelChart) levelChart.dispose();
    levelChart = echarts.init(levelPieRef.value);
    levelChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 0, textStyle: { fontSize: 12 } },
        series: [{
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
            label: { show: true, formatter: '{b}\n{d}%', fontSize: 11 },
            data: state.overview.gradeDistribution.map(d => ({
                name: d.name, value: d.value, itemStyle: { color: { '公开级': '#52c41a', '重要级': '#faad14', '核心级': '#ff4d4f' }[d.name] || '#1677ff' }
            }))
        }]
    });
};

const renderLayerBar = () => {
    if (!layerBarRef.value) return;
    if (layerChart) layerChart.dispose();
    layerChart = echarts.init(layerBarRef.value);
    const data = state.overview.categoryDistribution;
    const colors = ['#1677ff', '#13c2c2', '#722ed1', '#eb2f96', '#fa8c16', '#52c41a', '#f5222d'];
    layerChart.setOption({
        tooltip: { trigger: 'axis' },
        grid: { left: '3%', right: '8%', bottom: '3%', top: '10%', containLabel: true },
        xAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
        yAxis: { type: 'category', data: data.map(d => d.name), axisLabel: { fontSize: 11 } },
        series: [{
            type: 'bar',
            data: data.map((d, i) => ({ value: d.value, itemStyle: { color: colors[i % colors.length], borderRadius: [0, 6, 6, 0] } })),
            barWidth: 20,
            label: { show: true, position: 'right', fontSize: 12, fontWeight: 'bold' }
        }]
    });
};

const renderTrendLine = () => {
    if (!trendLineRef.value) return;
    if (trendChart) trendChart.dispose();
    trendChart = echarts.init(trendLineRef.value);
    const days = ['03-06', '03-07', '03-08', '03-09', '03-10', '03-11', '03-12'];
    trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['新增资产', '修改操作', '删除操作'], bottom: 0 },
        grid: { left: '3%', right: '4%', bottom: '12%', top: '10%', containLabel: true },
        xAxis: { type: 'category', data: days, boundaryGap: false },
        yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed' } } },
        series: [
            { name: '新增资产', type: 'line', smooth: true, data: [3, 5, 2, 4, 1, 6, 3], areaStyle: { opacity: 0.15 }, lineStyle: { width: 2 }, itemStyle: { color: '#52c41a' } },
            { name: '修改操作', type: 'line', smooth: true, data: [2, 3, 4, 1, 5, 2, 4], areaStyle: { opacity: 0.15 }, lineStyle: { width: 2 }, itemStyle: { color: '#1677ff' } },
            { name: '删除操作', type: 'line', smooth: true, data: [0, 1, 0, 2, 0, 1, 0], areaStyle: { opacity: 0.15 }, lineStyle: { width: 2 }, itemStyle: { color: '#ff4d4f' } }
        ]
    });
};

const handleResize = () => {
    levelChart?.resize();
    layerChart?.resize();
    trendChart?.resize();
};

onMounted(() => {
    loadData();
    window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize);
    levelChart?.dispose();
    layerChart?.dispose();
    trendChart?.dispose();
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                <DashboardOutlined class="text-blue-500" />
                数据资产看板
            </h2>
            <p class="text-gray-500 text-sm">全局视图展示数据资产的分布、分级及运营情况</p>
        </div>

        <!-- 顶部统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-6">
            <div class="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl p-5 text-white shadow-md">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-blue-100 text-sm mb-1">资产总数</div>
                        <div class="text-3xl font-bold">{{ state.overview.totalAssets }}</div>
                    </div>
                    <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center">
                        <DatabaseOutlined class="text-3xl" />
                    </div>
                </div>
                <div class="mt-3 pt-3 border-t border-white/20 flex items-center gap-1 text-sm text-blue-100">
                    <RiseOutlined /> 较上周增长 12%
                </div>
            </div>
            <div class="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-5 text-white shadow-md">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-green-100 text-sm mb-1">数据集</div>
                        <div class="text-3xl font-bold">{{ state.overview.datasetCount }}</div>
                    </div>
                    <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center">
                        <DatabaseOutlined class="text-3xl" />
                    </div>
                </div>
                <div class="mt-3 pt-3 border-t border-white/20 text-sm text-green-100">占比 {{ state.overview.totalAssets ? Math.round(state.overview.datasetCount / state.overview.totalAssets * 100) : 0 }}%</div>
            </div>
            <div class="bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl p-5 text-white shadow-md">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-purple-100 text-sm mb-1">模型</div>
                        <div class="text-3xl font-bold">{{ state.overview.modelCount }}</div>
                    </div>
                    <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center">
                        <AppstoreOutlined class="text-3xl" />
                    </div>
                </div>
                <div class="mt-3 pt-3 border-t border-white/20 text-sm text-purple-100">占比 {{ state.overview.totalAssets ? Math.round(state.overview.modelCount / state.overview.totalAssets * 100) : 0 }}%</div>
            </div>
            <div class="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl p-5 text-white shadow-md">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-orange-100 text-sm mb-1">知识文档</div>
                        <div class="text-3xl font-bold">{{ state.overview.knowledgeCount }}</div>
                    </div>
                    <div class="w-14 h-14 bg-white/20 rounded-xl flex items-center justify-center">
                        <FileTextOutlined class="text-3xl" />
                    </div>
                </div>
                <div class="mt-3 pt-3 border-t border-white/20 text-sm text-orange-100">占比 {{ state.overview.totalAssets ? Math.round(state.overview.knowledgeCount / state.overview.totalAssets * 100) : 0 }}%</div>
            </div>
        </div>

        <!-- 图表区域 -->
        <div class="grid grid-cols-3 gap-6 mb-6">
            <!-- 安全等级分布 -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                <div class="flex items-center gap-2 mb-4">
                    <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                    <span class="font-bold text-gray-800">数据分级分布</span>
                </div>
                <div ref="levelPieRef" style="height: 280px;"></div>
            </div>

            <!-- 数据层级分布 -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                <div class="flex items-center gap-2 mb-4">
                    <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                    <span class="font-bold text-gray-800">数据分类分布</span>
                </div>
                <div ref="layerBarRef" style="height: 280px;"></div>
            </div>

            <!-- 操作趋势 -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                <div class="flex items-center gap-2 mb-4">
                    <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                    <span class="font-bold text-gray-800">近7日操作趋势</span>
                </div>
                <div ref="trendLineRef" style="height: 280px;"></div>
            </div>
        </div>

        <!-- 最近活动 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
            <div class="flex items-center gap-2 mb-4">
                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                <span class="font-bold text-gray-800">最近操作记录</span>
            </div>
            <a-timeline>
                <a-timeline-item v-for="(act, idx) in state.overview.recentActivity" :key="idx"
                                 :color="actionColorMap[act.action] || '#1677ff'">
                    <div class="flex items-center gap-3">
                        <a-tag :color="actionColorMap[act.action] || '#1677ff'" size="small">{{ act.action }}</a-tag>
                        <span class="font-medium text-gray-700">{{ act.assetName }}</span>
                        <span class="text-gray-400 text-sm ml-auto flex items-center gap-1">
                            <UserOutlined /> {{ act.user }}
                            <ClockCircleOutlined class="ml-2" /> {{ act.time }}
                        </span>
                    </div>
                </a-timeline-item>
            </a-timeline>
        </div>
    </div>
</template>
