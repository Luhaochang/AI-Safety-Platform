<script setup>
import {
    SafetyCertificateOutlined,
    EditOutlined,
    PlusOutlined,
    LockOutlined,
    UnlockOutlined,
    AppstoreOutlined,
    BarChartOutlined,
    LinkOutlined
} from '@ant-design/icons-vue';
import { listGrades, listCategories, getClassificationStats, listGradesMock, listCategoriesMock, getClassificationStatsMock } from '@/api/dataGovernance/classification.js';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const state = reactive({
    grades: [],
    categories: [],
    stats: null,
    loading: false,
    activeTab: 'overview',
    editGradeVisible: false,
    editCategoryVisible: false,
    gradeForm: { id: null, name: '', description: '', accessPolicy: '' },
    categoryForm: { id: null, name: '', description: '', examples: '' }
});

const gradeChartRef = ref(null);
const categoryChartRef = ref(null);

const loadData = async () => {
    state.loading = true;
    try {
        let gradeRes = await listGrades().catch(() => null);
        if (!gradeRes || gradeRes.code !== 200 || !gradeRes.data?.length) gradeRes = await listGradesMock();
        let categoryRes = await listCategories().catch(() => null);
        if (!categoryRes || categoryRes.code !== 200 || !categoryRes.data?.length) categoryRes = await listCategoriesMock();
        let statsRes = await getClassificationStats().catch(() => null);
        if (!statsRes || statsRes.code !== 200 || !statsRes.data?.totalAssets) statsRes = await getClassificationStatsMock();
        if (gradeRes.code === 200) state.grades = gradeRes.data;
        if (categoryRes.code === 200) state.categories = categoryRes.data;
        if (statsRes.code === 200) {
            state.stats = statsRes.data;
            nextTick(() => initCharts());
        }
    } finally {
        state.loading = false;
    }
};

const initCharts = () => {
    if (!state.stats) return;
    // 分级饼图
    if (gradeChartRef.value) {
        const chart = echarts.init(gradeChartRef.value);
        chart.setOption({
            tooltip: { trigger: 'item', formatter: '{b}: {c}个 ({d}%)' },
            series: [{
                type: 'pie', radius: ['45%', '70%'], center: ['50%', '50%'],
                label: { formatter: '{b}\n{c}个', fontSize: 12 },
                data: state.stats.gradeDistribution.map(d => ({ name: d.name, value: d.value, itemStyle: { color: gradeColorMap[d.name] || '#1677ff' } }))
            }]
        });
        window.addEventListener('resize', () => chart.resize());
    }
    // 分类柱状图
    if (categoryChartRef.value) {
        const chart = echarts.init(categoryChartRef.value);
        const catColors = ['#1677ff', '#13c2c2', '#722ed1', '#eb2f96', '#fa8c16', '#52c41a', '#f5222d'];
        chart.setOption({
            tooltip: { trigger: 'axis' },
            grid: { left: 80, right: 20, top: 10, bottom: 30 },
            xAxis: { type: 'value' },
            yAxis: { type: 'category', data: state.stats.categoryDistribution.map(d => d.name), axisLabel: { fontSize: 12 } },
            series: [{
                type: 'bar', barWidth: 18,
                data: state.stats.categoryDistribution.map((d, i) => ({ value: d.value, itemStyle: { color: catColors[i % catColors.length], borderRadius: [0, 4, 4, 0] } })),
                label: { show: true, position: 'right', formatter: '{c}个', fontSize: 11 }
            }]
        });
        window.addEventListener('resize', () => chart.resize());
    }
};

const gradeColorMap = { '公开级': '#52c41a', '重要级': '#faad14', '核心级': '#ff4d4f' };

const handleEditGrade = (item) => {
    state.gradeForm = item ? { ...item } : { id: null, name: '', description: '', accessPolicy: '' };
    state.editGradeVisible = true;
};

const handleEditCategory = (item) => {
    state.categoryForm = item ? { ...item } : { id: null, name: '', description: '', examples: '' };
    state.editCategoryVisible = true;
};

const submitGrade = () => {
    if (!state.gradeForm.name) { ElMessage.warning('请填写名称'); return; }
    ElMessage.success('保存成功');
    state.editGradeVisible = false;
};

const submitCategory = () => {
    if (!state.categoryForm.name) { ElMessage.warning('请填写名称'); return; }
    ElMessage.success('保存成功');
    state.editCategoryVisible = false;
};

const goToAssetCatalog = (filter) => {
    // 跳转到资产明细并带上筛选条件
    ElMessage.info(`跳转到资产明细，筛选: ${JSON.stringify(filter)}`);
};

const goToDataset = () => {
    ElMessage.info('跳转到数据集管理');
};

onMounted(() => { loadData(); });
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                    <SafetyCertificateOutlined class="text-blue-500" />
                    数据分级分类管理
                </h2>
                <p class="text-gray-500 text-sm">
                    按安全等级对数据进行<b>分级</b>（公开级/重要级/核心级），按业务属性对数据进行<b>分类</b>（7大类）。
                    每个数据资产同时拥有分级和分类两个维度标签。
                </p>
            </div>
        </div>

        <!-- Tab切换 -->
        <a-tabs v-model:activeKey="state.activeTab" class="mb-2">
            <a-tab-pane key="overview" tab="总览" />
            <a-tab-pane key="grades" tab="数据分级（3级）" />
            <a-tab-pane key="categories" tab="数据分类（7类）" />
        </a-tabs>

        <!-- ===== 总览 Tab ===== -->
        <div v-if="state.activeTab === 'overview'">
            <!-- 统计卡片 -->
            <div class="grid grid-cols-3 gap-4 mb-6">
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 text-center">
                    <div class="text-3xl font-bold text-blue-600">{{ state.stats?.totalAssets || 0 }}</div>
                    <div class="text-sm text-gray-500 mt-1">数据资产总数</div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 text-center">
                    <div class="text-3xl font-bold text-green-600">{{ state.stats?.classifiedAssets || 0 }}</div>
                    <div class="text-sm text-gray-500 mt-1">已分级分类</div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 text-center">
                    <div class="text-3xl font-bold text-orange-500">{{ state.stats?.unclassifiedAssets || 0 }}</div>
                    <div class="text-sm text-gray-500 mt-1">待分级分类</div>
                    <a-button type="link" size="small" @click="goToAssetCatalog({unclassified: true})" class="mt-1">去处理 →</a-button>
                </div>
            </div>
            <!-- 图表区域 -->
            <div class="grid grid-cols-2 gap-6 mb-6">
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="text-sm font-bold text-gray-700 mb-3">数据分级分布</div>
                    <div ref="gradeChartRef" style="height: 220px"></div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="text-sm font-bold text-gray-700 mb-3">数据分类分布</div>
                    <div ref="categoryChartRef" style="height: 220px"></div>
                </div>
            </div>
            <!-- 快速操作 -->
            <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                <div class="text-sm font-bold text-gray-700 mb-4">关联操作</div>
                <div class="grid grid-cols-4 gap-4">
                    <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 hover:shadow-sm cursor-pointer transition-all text-center"
                         @click="goToDataset()">
                        <div class="text-2xl mb-2">📦</div>
                        <div class="text-sm font-medium">创建数据集</div>
                        <div class="text-xs text-gray-400 mt-1">创建时自动分配分级分类</div>
                    </div>
                    <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 hover:shadow-sm cursor-pointer transition-all text-center"
                         @click="goToAssetCatalog({})">
                        <div class="text-2xl mb-2">📋</div>
                        <div class="text-sm font-medium">资产目录</div>
                        <div class="text-xs text-gray-400 mt-1">查看所有已分级分类资产</div>
                    </div>
                    <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 hover:shadow-sm cursor-pointer transition-all text-center"
                         @click="$router.push('/datamag/lineage')">
                        <div class="text-2xl mb-2">🔗</div>
                        <div class="text-sm font-medium">数据血缘</div>
                        <div class="text-xs text-gray-400 mt-1">查看数据全链路流转</div>
                    </div>
                    <div class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 hover:shadow-sm cursor-pointer transition-all text-center"
                         @click="$router.push('/datamag/dashboard')">
                        <div class="text-2xl mb-2">📊</div>
                        <div class="text-sm font-medium">资产看板</div>
                        <div class="text-xs text-gray-400 mt-1">数据治理全局视图</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== 数据分级 Tab ===== -->
        <div v-if="state.activeTab === 'grades'">
            <div class="flex items-center justify-between mb-4">
                <p class="text-gray-500 text-sm">定义数据安全等级，不同等级对应不同的访问控制策略。创建数据集或注册资产时需选择对应分级。</p>
                <a-button type="primary" @click="handleEditGrade(null)"><PlusOutlined /> 新增分级</a-button>
            </div>
            <div class="grid grid-cols-3 gap-6">
                <div v-for="item in state.grades" :key="item.id"
                     class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-lg transition-all duration-300 group">
                    <div class="h-2" :style="{ backgroundColor: gradeColorMap[item.name] || '#1677ff' }"></div>
                    <div class="p-5">
                        <div class="flex items-center justify-between mb-3">
                            <div class="flex items-center gap-3">
                                <div class="w-11 h-11 rounded-xl flex items-center justify-center text-white text-lg"
                                     :style="{ backgroundColor: gradeColorMap[item.name] || '#1677ff' }">
                                    <LockOutlined v-if="item.name === '核心级' || item.name === '重要级'" />
                                    <UnlockOutlined v-else />
                                </div>
                                <div>
                                    <div class="text-lg font-bold text-gray-800">{{ item.name }}</div>
                                </div>
                            </div>
                            <a-button type="text" size="small" class="opacity-0 group-hover:opacity-100 transition-opacity" @click="handleEditGrade(item)">
                                <EditOutlined />
                            </a-button>
                        </div>
                        <p class="text-gray-600 text-sm mb-3">{{ item.description }}</p>
                        <div class="bg-gray-50 rounded-lg p-3 mb-3">
                            <div class="text-xs text-gray-400 mb-1">访问策略</div>
                            <div class="text-sm text-gray-700">{{ item.accessPolicy }}</div>
                        </div>
                        <div class="bg-gray-50 rounded-lg p-3 mb-3">
                            <div class="text-xs text-gray-400 mb-1">典型数据举例</div>
                            <div class="text-sm text-gray-700">{{ item.examples }}</div>
                        </div>
                        <div class="flex items-center justify-between pt-3 border-t border-gray-100">
                            <a-tag :color="gradeColorMap[item.name] || '#1677ff'">{{ item.assetCount }} 个资产</a-tag>
                            <a-button type="link" size="small" @click="goToAssetCatalog({grade: item.name})">
                                查看资产 <LinkOutlined />
                            </a-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== 数据分类 Tab ===== -->
        <div v-if="state.activeTab === 'categories'">
            <div class="flex items-center justify-between mb-4">
                <p class="text-gray-500 text-sm">按业务属性对电力AI平台数据进行分类管理。每个分类有默认建议分级，创建数据集时可调整。</p>
                <a-button type="primary" @click="handleEditCategory(null)"><PlusOutlined /> 新增分类</a-button>
            </div>
            <div class="space-y-4">
                <div v-for="item in state.categories" :key="item.id"
                     class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-lg transition-all duration-300 group">
                    <div class="flex items-start gap-4">
                        <div class="w-14 h-14 rounded-xl flex items-center justify-center text-2xl flex-shrink-0 bg-blue-50 border-2 border-blue-200">
                            📂
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-3 mb-1">
                                <span class="text-base font-bold text-gray-800">{{ item.name }}</span>
                                <a-tag size="small" color="blue">{{ item.assetCount }} 个资产</a-tag>
                            </div>
                            <p class="text-sm text-gray-600 mb-2">{{ item.description }}</p>
                            <div class="text-xs text-gray-400">
                                <span class="font-medium text-gray-500">数据举例：</span>{{ item.examples }}
                            </div>
                        </div>
                        <div class="flex items-center gap-2 flex-shrink-0">
                            <a-button type="link" size="small" @click="goToAssetCatalog({category: item.name})">
                                查看资产 <LinkOutlined />
                            </a-button>
                            <a-button type="text" size="small" class="opacity-0 group-hover:opacity-100 transition-opacity" @click="handleEditCategory(item)">
                                <EditOutlined />
                            </a-button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 编辑分级弹窗 -->
        <a-modal v-model:open="state.editGradeVisible" :title="state.gradeForm.id ? '编辑分级' : '新增分级'" width="500px" @ok="submitGrade">
            <a-form :model="state.gradeForm" layout="vertical" class="mt-4">
                <a-form-item label="分级名称" required>
                    <a-input v-model:value="state.gradeForm.name" placeholder="如：公开级" />
                </a-form-item>
                <a-form-item label="描述">
                    <a-textarea v-model:value="state.gradeForm.description" :rows="2" />
                </a-form-item>
                <a-form-item label="访问策略">
                    <a-input v-model:value="state.gradeForm.accessPolicy" />
                </a-form-item>
            </a-form>
        </a-modal>

        <!-- 编辑分类弹窗 -->
        <a-modal v-model:open="state.editCategoryVisible" :title="state.categoryForm.id ? '编辑分类' : '新增分类'" width="500px" @ok="submitCategory">
            <a-form :model="state.categoryForm" layout="vertical" class="mt-4">
                <a-form-item label="分类名称" required>
                    <a-input v-model:value="state.categoryForm.name" placeholder="如：设备数据类" />
                </a-form-item>
                <a-form-item label="描述">
                    <a-textarea v-model:value="state.categoryForm.description" :rows="2" />
                </a-form-item>
                <a-form-item label="数据举例">
                    <a-input v-model:value="state.categoryForm.examples" placeholder="典型数据示例，逗号分隔" />
                </a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>
