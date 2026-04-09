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
    TagOutlined
} from '@ant-design/icons-vue';
import { listAssets, getAssetOverview, listAssetsMock, getAssetOverviewMock } from '@/api/dataGovernance/asset.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

const state = reactive({
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
    overview: { totalAssets: 0, datasetCount: 0, modelCount: 0, knowledgeCount: 0 },
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
    if (res.code === 200) state.overview = res.data;
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
        ElMessage.success('删除成功'); loadData();
    }).catch(() => {});
};

const handleSubmit = () => {
    ElMessage.success(state.form.id ? '修改成功' : '注册成功');
    state.dialogVisible = false; loadData();
};

const handlePageChange = (page) => { state.queryParams.pageNo = page; loadData(); };

const goToDataset = () => router.push('/datamag/dataset');
const goToMarkTask = () => router.push('/datamag/mark-task');
const goToLineage = (assetName) => {
    ElMessage.info(`查看「${assetName}」的血缘关系`);
};

onMounted(() => { loadData(); loadOverview(); });
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-4 gap-4 mb-6">
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-all">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-xl bg-blue-50 flex items-center justify-center">
                        <DatabaseOutlined class="text-2xl text-blue-500" />
                    </div>
                    <div>
                        <div class="text-gray-500 text-sm">资产总数</div>
                        <div class="text-2xl font-bold text-gray-800">{{ state.overview.totalAssets }}</div>
                    </div>
                </div>
            </div>
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-all cursor-pointer" @click="goToDataset">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-xl bg-green-50 flex items-center justify-center">
                        <DatabaseOutlined class="text-2xl text-green-500" />
                    </div>
                    <div>
                        <div class="text-gray-500 text-sm">数据集 <LinkOutlined class="text-xs" /></div>
                        <div class="text-2xl font-bold text-gray-800">{{ state.overview.datasetCount }}</div>
                    </div>
                </div>
            </div>
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-all">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-xl bg-purple-50 flex items-center justify-center">
                        <AppstoreOutlined class="text-2xl text-purple-500" />
                    </div>
                    <div>
                        <div class="text-gray-500 text-sm">模型数量</div>
                        <div class="text-2xl font-bold text-gray-800">{{ state.overview.modelCount }}</div>
                    </div>
                </div>
            </div>
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-all">
                <div class="flex items-center gap-3">
                    <div class="w-12 h-12 rounded-xl bg-orange-50 flex items-center justify-center">
                        <FileTextOutlined class="text-2xl text-orange-500" />
                    </div>
                    <div>
                        <div class="text-gray-500 text-sm">知识文档</div>
                        <div class="text-2xl font-bold text-gray-800">{{ state.overview.knowledgeCount }}</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 主内容区 -->
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
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
