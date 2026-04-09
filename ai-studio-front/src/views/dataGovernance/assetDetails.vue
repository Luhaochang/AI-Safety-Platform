<script setup>
import {
    SearchOutlined,
    DatabaseOutlined,
    SafetyCertificateOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    PlusOutlined,
    ReloadOutlined,
    FileTextOutlined,
    AppstoreOutlined,
    LinkOutlined,
    TagOutlined,
    DashboardOutlined,
    RiseOutlined,
    ClockCircleOutlined,
    UserOutlined,
    BarChartOutlined,
    TableOutlined
} from '@ant-design/icons-vue';
import { listAssets, getAssetOverview, listAssetsMock, getAssetOverviewMock } from '@/api/dataGovernance/asset.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import * as echarts from 'echarts';
import LineageView from './lineage.vue';

const router = useRouter();

const levelPieRef = ref(null);
const layerBarRef = ref(null);
const trendLineRef = ref(null);
let levelChart = null;
let layerChart = null;
let trendChart = null;

const state = reactive({
    activeTab: 'catalog',
    assetList: [],
    loading: false,
    total: 0,
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        assetNameLike: undefined,
        grade: undefined,
        category: undefined,
        dataLayer: undefined
    },
    overview: {
        totalAssets: 0,
        datasetCount: 0,
        modelCount: 0,
        knowledgeCount: 0,
        gradeDistribution: [],
        categoryDistribution: [],
        recentActivity: []
    },
    dialogVisible: false,
    dialogTitle: '注册数据资产',
    form: {
        id: null, assetName: '', grade: undefined, category: undefined,
        dataLayer: undefined, source: '', description: ''
    },
    detailVisible: false,
    detailData: null
});

const gradeOptions = [
    { value: '公开级', label: '公开级', color: '#52c41a' },
    { value: '重要级', label: '重要级', color: '#faad14' },
    { value: '核心级', label: '核心级', color: '#ff4d4f' }
];

const categoryOptions = [
    { value: '设备数据类', label: '设备数据类' },
    { value: '运行数据类', label: '运行数据类' },
    { value: '调度指令类', label: '调度指令类' },
    { value: '历史案例类', label: '历史案例类' },
    { value: '专家知识类', label: '专家知识类' },
    { value: '规程文档类', label: '规程文档类' },
    { value: '用户信息类', label: '用户信息类' }
];

const layerOptions = [
    { value: '原始数据', label: '原始数据' },
    { value: '清洗数据', label: '清洗数据' },
    { value: '标注数据', label: '标注数据' },
    { value: '特征数据', label: '特征数据' },
    { value: '模型产物', label: '模型产物' },
    { value: '知识文档', label: '知识文档' }
];

const actionColorMap = { '新增': '#52c41a', '修改分级': '#1677ff', '删除': '#ff4d4f', '数据标注': '#722ed1', '入库知识': '#fa8c16' };

const getGradeColor = (g) => {
    const map = { '公开级': '#52c41a', '重要级': '#faad14', '核心级': '#ff4d4f' };
    return map[g] || '#999';
};

const getCategoryIcon = (cat) => {
    const map = { '设备数据类': '🔌', '运行数据类': '⚡', '调度指令类': '📋', '历史案例类': '📚', '专家知识类': '🧠', '规程文档类': '📖', '用户信息类': '👤' };
    return map[cat] || '📄';
};

const loadData = async () => {
    state.loading = true;
    try {
        let res = await listAssets(state.queryParams).catch(() => null);
        if (!res || res.code !== 200 || !res.data?.records?.length) res = await listAssetsMock(state.queryParams);
        if (res.code === 200) {
            state.assetList = res.data.records;
            state.total = res.data.total;
        }
    } finally {
        state.loading = false;
    }
};

const loadOverview = async () => {
    let res = await getAssetOverview().catch(() => null);
    if (!res || res.code !== 200 || !res.data?.totalAssets) res = await getAssetOverviewMock();
    if (res.code === 200) {
        state.overview = res.data;
        if (state.activeTab === 'dashboard') {
            nextTick(() => {
                renderLevelPie();
                renderLayerBar();
                renderTrendLine();
            });
        }
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

const handleTabChange = (key) => {
    state.activeTab = key;
    if (key === 'dashboard') {
        nextTick(() => {
            renderLevelPie();
            renderLayerBar();
            renderTrendLine();
        });
    }
};

const handleSearch = () => { state.queryParams.pageNo = 1; loadData(); };
const handleReset = () => {
    state.queryParams = { pageNo: 1, pageSize: 10, assetNameLike: undefined, grade: undefined, category: undefined, dataLayer: undefined };
    loadData();
};

const handleAdd = () => {
    state.dialogTitle = '注册数据资产';
    state.form = { id: null, assetName: '', grade: undefined, category: undefined, dataLayer: undefined, source: '', description: '' };
    state.dialogVisible = true;
};

const handleEdit = (row) => { state.dialogTitle = '编辑资产'; state.form = { ...row }; state.dialogVisible = true; };
const handleView = (row) => { state.detailData = row; state.detailVisible = true; };

const handleDelete = (row) => {
    ElMessageBox.confirm(`确认删除资产「${row.assetName}」？`, '提示', { type: 'warning' }).then(() => {
        ElMessage.success('删除成功'); loadData(); loadOverview();
    }).catch(() => {});
};

const handleSubmit = () => {
    ElMessage.success(state.form.id ? '修改成功' : '注册成功');
    state.dialogVisible = false; loadData(); loadOverview();
};

const handlePageChange = (page) => { state.queryParams.pageNo = page; loadData(); };

const goToDataset = () => router.push('/datamag/dataset');
const goToMarkTask = () => router.push('/datamag/mark-task');
const goToLineage = (assetName) => {
    ElMessage.info(`查看「${assetName}」的血缘关系`);
};

onMounted(() => {
    loadData();
    loadOverview();
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
                <DatabaseOutlined class="text-blue-500" />
                资产管理
            </h2>
            <p class="text-gray-500 text-sm">统一管理数据资产目录、可视化看板与数据血缘</p>
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
            <div class="bg-gradient-to-br from-green-500 to-green-600 rounded-xl p-5 text-white shadow-md cursor-pointer" @click="goToDataset">
                <div class="flex items-center justify-between">
                    <div>
                        <div class="text-green-100 text-sm mb-1">数据集 <LinkOutlined class="text-xs" /></div>
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

        <!-- 标签页切换 -->
        <a-tabs v-model:activeKey="state.activeTab" @change="handleTabChange" class="mb-4">
            <a-tab-pane key="catalog">
                <template #tab>
                    <span class="flex items-center gap-2">
                        <TableOutlined /> 资产目录
                    </span>
                </template>
            </a-tab-pane>
            <a-tab-pane key="dashboard">
                <template #tab>
                    <span class="flex items-center gap-2">
                        <BarChartOutlined /> 可视化看板
                    </span>
                </template>
            </a-tab-pane>
            <a-tab-pane key="lineage">
                <template #tab>
                    <span class="flex items-center gap-2">
                        <LinkOutlined /> 数据血缘
                    </span>
                </template>
            </a-tab-pane>
        </a-tabs>

        <!-- 资产目录视图 -->
        <div v-show="state.activeTab === 'catalog'" class="flex gap-6">
          <div class="flex-1 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
            <!-- 搜索栏 -->
            <div class="p-5 border-b border-gray-100">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3 flex-wrap">
                        <a-input v-model:value="state.queryParams.assetNameLike" placeholder="搜索资产名称" style="width: 200px" allowClear @pressEnter="handleSearch">
                            <template #prefix><SearchOutlined class="text-gray-400" /></template>
                        </a-input>
                        <a-select v-model:value="state.queryParams.grade" placeholder="数据分级" style="width: 120px" allowClear>
                            <a-select-option v-for="item in gradeOptions" :key="item.value" :value="item.value">
                                <a-badge :color="item.color" :text="item.label" />
                            </a-select-option>
                        </a-select>
                        <a-select v-model:value="state.queryParams.category" placeholder="数据分类" style="width: 140px" allowClear>
                            <a-select-option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                        </a-select>
                        <a-select v-model:value="state.queryParams.dataLayer" placeholder="数据层级" style="width: 130px" allowClear>
                            <a-select-option v-for="item in layerOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                        </a-select>
                        <a-button type="primary" @click="handleSearch"><SearchOutlined /> 搜索</a-button>
                        <a-button @click="handleReset"><ReloadOutlined /> 重置</a-button>
                    </div>
                    <div class="flex items-center gap-2">
                        <a-button @click="goToDataset"><DatabaseOutlined /> 创建数据集</a-button>
                        <a-button @click="goToMarkTask"><TagOutlined /> 标注任务</a-button>
                        <a-button type="primary" @click="handleAdd"><PlusOutlined /> 注册资产</a-button>
                    </div>
                </div>
            </div>

            <!-- 资产表格 -->
            <div class="p-5">
                <a-table :dataSource="state.assetList" :loading="state.loading" :pagination="false" rowKey="id" :scroll="{ x: 1200 }">
                    <a-table-column title="资产名称" dataIndex="assetName" :width="200">
                        <template #default="{ record }">
                            <div class="flex items-center gap-2">
                                <span class="text-lg">{{ getCategoryIcon(record.category) }}</span>
                                <a class="text-blue-600 hover:text-blue-800 cursor-pointer font-medium" @click="handleView(record)">{{ record.assetName }}</a>
                            </div>
                        </template>
                    </a-table-column>
                    <a-table-column title="数据分级" dataIndex="grade" :width="100" align="center">
                        <template #default="{ record }">
                            <a-tag :color="getGradeColor(record.grade)">
                                <SafetyCertificateOutlined /> {{ record.grade }}
                            </a-tag>
                        </template>
                    </a-table-column>
                    <a-table-column title="数据分类" dataIndex="category" :width="120" align="center">
                        <template #default="{ record }">
                            <a-tag>{{ record.category }}</a-tag>
                        </template>
                    </a-table-column>
                    <a-table-column title="数据层级" dataIndex="dataLayer" :width="100" align="center">
                        <template #default="{ record }">
                            <a-tag color="processing">{{ record.dataLayer }}</a-tag>
                        </template>
                    </a-table-column>
                    <a-table-column title="数据来源" dataIndex="source" :width="110" align="center" />
                    <a-table-column title="责任人" dataIndex="owner" :width="80" align="center" />
                    <a-table-column title="大小" dataIndex="size" :width="80" align="center" />
                    <a-table-column title="创建时间" dataIndex="createTime" :width="160" align="center" />
                    <a-table-column title="操作" :width="200" align="center" fixed="right">
                        <template #default="{ record }">
                            <div class="flex items-center justify-center gap-1">
                                <a-button type="link" size="small" @click="handleView(record)"><EyeOutlined /> 详情</a-button>
                                <a-button type="link" size="small" @click="goToLineage(record.assetName)"><LinkOutlined /> 血缘</a-button>
                                <a-button type="link" size="small" @click="handleEdit(record)"><EditOutlined /></a-button>
                                <a-button type="link" size="small" danger @click="handleDelete(record)"><DeleteOutlined /></a-button>
                            </div>
                        </template>
                    </a-table-column>
                </a-table>

                <div class="flex justify-end mt-4">
                    <a-pagination v-model:current="state.queryParams.pageNo" :total="state.total"
                                  :pageSize="state.queryParams.pageSize" @change="handlePageChange"
                                  show-size-changer show-quick-jumper />
                </div>
            </div>
          </div>
          <!-- 分级分类侧栏 -->
          <div class="w-64 flex-shrink-0 space-y-4">
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
              <div class="flex items-center gap-2 mb-3">
                <SafetyCertificateOutlined class="text-blue-500" />
                <span class="font-bold text-gray-800 text-sm">数据分级</span>
              </div>
              <div class="space-y-2">
                <div v-for="item in gradeOptions" :key="item.value"
                     class="flex items-center justify-between p-2 rounded-lg cursor-pointer hover:bg-gray-50 transition-all"
                     :class="state.queryParams.grade === item.value ? 'bg-blue-50 ring-1 ring-blue-200' : ''"
                     @click="state.queryParams.grade = state.queryParams.grade === item.value ? undefined : item.value; handleSearch()">
                  <div class="flex items-center gap-2">
                    <div class="w-3 h-3 rounded-full" :style="{ backgroundColor: item.color }"></div>
                    <span class="text-sm text-gray-700">{{ item.label }}</span>
                  </div>
                  <span class="text-xs text-gray-400">{{ state.assetList.filter(a => a.grade === item.value).length }}</span>
                </div>
              </div>
            </div>
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
              <div class="flex items-center gap-2 mb-3">
                <AppstoreOutlined class="text-purple-500" />
                <span class="font-bold text-gray-800 text-sm">数据分类</span>
              </div>
              <div class="space-y-1">
                <div v-for="item in categoryOptions" :key="item.value"
                     class="flex items-center justify-between p-2 rounded-lg cursor-pointer hover:bg-gray-50 transition-all text-sm"
                     :class="state.queryParams.category === item.value ? 'bg-purple-50 ring-1 ring-purple-200' : ''"
                     @click="state.queryParams.category = state.queryParams.category === item.value ? undefined : item.value; handleSearch()">
                  <span class="text-gray-700">{{ getCategoryIcon(item.value) }} {{ item.label }}</span>
                  <span class="text-xs text-gray-400">{{ state.assetList.filter(a => a.category === item.value).length }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 可视化看板视图 -->
        <div v-show="state.activeTab === 'dashboard'">
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

        <!-- 数据血缘视图 -->
        <div v-show="state.activeTab === 'lineage'">
            <LineageView />
        </div>

        <!-- 注册/编辑对话框 -->
        <a-modal v-model:open="state.dialogVisible" :title="state.dialogTitle" width="600px" @ok="handleSubmit">
            <a-form :model="state.form" layout="vertical" class="mt-4">
                <a-form-item label="资产名称" required>
                    <a-input v-model:value="state.form.assetName" placeholder="请输入资产名称" />
                </a-form-item>
                <div class="grid grid-cols-2 gap-4">
                    <a-form-item label="数据分级" required>
                        <a-select v-model:value="state.form.grade" placeholder="选择分级">
                            <a-select-option v-for="item in gradeOptions" :key="item.value" :value="item.value">
                                <a-badge :color="item.color" :text="item.label" />
                            </a-select-option>
                        </a-select>
                    </a-form-item>
                    <a-form-item label="数据分类" required>
                        <a-select v-model:value="state.form.category" placeholder="选择分类">
                            <a-select-option v-for="item in categoryOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                        </a-select>
                    </a-form-item>
                </div>
                <div class="grid grid-cols-2 gap-4">
                    <a-form-item label="数据层级">
                        <a-select v-model:value="state.form.dataLayer" placeholder="选择层级">
                            <a-select-option v-for="item in layerOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                        </a-select>
                    </a-form-item>
                    <a-form-item label="数据来源">
                        <a-input v-model:value="state.form.source" placeholder="请输入来源" />
                    </a-form-item>
                </div>
                <a-form-item label="描述">
                    <a-textarea v-model:value="state.form.description" placeholder="请输入描述" :rows="3" />
                </a-form-item>
            </a-form>
        </a-modal>

        <!-- 详情抽屉 -->
        <a-drawer v-model:open="state.detailVisible" title="资产详情" width="520" v-if="state.detailData">
            <a-descriptions bordered :column="1" size="small">
                <a-descriptions-item label="资产名称">{{ state.detailData.assetName }}</a-descriptions-item>
                <a-descriptions-item label="数据分级">
                    <a-tag :color="getGradeColor(state.detailData.grade)">{{ state.detailData.grade }}</a-tag>
                </a-descriptions-item>
                <a-descriptions-item label="数据分类"><a-tag>{{ state.detailData.category }}</a-tag></a-descriptions-item>
                <a-descriptions-item label="数据层级"><a-tag color="processing">{{ state.detailData.dataLayer }}</a-tag></a-descriptions-item>
                <a-descriptions-item label="数据来源">{{ state.detailData.source }}</a-descriptions-item>
                <a-descriptions-item label="责任人">{{ state.detailData.owner }}</a-descriptions-item>
                <a-descriptions-item label="大小">{{ state.detailData.size }}</a-descriptions-item>
                <a-descriptions-item label="创建时间">{{ state.detailData.createTime }}</a-descriptions-item>
                <a-descriptions-item label="更新时间">{{ state.detailData.updateTime }}</a-descriptions-item>
                <a-descriptions-item label="描述">{{ state.detailData.description }}</a-descriptions-item>
            </a-descriptions>
            <div class="mt-4 pt-4 border-t border-gray-200">
                <div class="text-sm font-bold text-gray-700 mb-3">关联操作</div>
                <div class="flex gap-2">
                    <a-button size="small" @click="goToLineage(state.detailData.assetName)"><LinkOutlined /> 查看血缘</a-button>
                    <a-button size="small" @click="goToDataset"><DatabaseOutlined /> 数据集管理</a-button>
                    <a-button size="small" @click="goToMarkTask"><TagOutlined /> 标注任务</a-button>
                </div>
            </div>
        </a-drawer>
    </div>
</template>
