<script setup>
import {
  ArrowRightOutlined,
  CloseOutlined,
  CloudServerOutlined,
  EditOutlined,
  MonitorOutlined,
  RocketOutlined,
  SearchOutlined
} from "@ant-design/icons-vue";
import {useRouter} from "vue-router";
import {deleteService, getServiceList, stopService} from "@/api/modelMag/service.js";
import {ElMessage, ElMessageBox} from "element-plus";

const router = useRouter()
const loading = ref(true);
const showSearch = ref(true);
const {proxy} = getCurrentInstance();
const queryRef = ref()
const refreshTable = ref(true);

const state = reactive({
    activeKey: 0,
    serviceList: [],
    type: 0,
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        statusEq: undefined,
        nameLike: undefined
    },
    total: 0,
    showProcess: true
})

const handlePageChange = (page) => {
    state.queryParams.pageNo = page;
    getServiceData();
}

const onShowSizeChange = (current, pageSize) => {
    state.queryParams.pageSize = pageSize;
    state.queryParams.pageNo = 1;
    getServiceData();
};

const getServiceData = async () => {
    loading.value = true;

    const res = await getServiceList(state.queryParams);
    if (res.code === 200) {
        state.serviceList = res.data.records.map(item => ({
            isShowUrl: false,
            ...item
        }));
        state.total = res.data.total;
        loading.value = false;

    }
}
const handleDelete = (item) => {
    ElMessageBox.confirm('确定删除该模型吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteService({id: item.id}).then(res => {
            if (res.code === 200) {
                getServiceData();
                ElMessage.success('删除成功');
            }
        })
    }).catch(() => {
    });
}

const conditionSearch = () => {
    state.queryParams.pageNo = 1;
    state.queryParams.statusEq = state.queryParams.statusEq  === 0 ? undefined : state.queryParams.statusEq;
    getServiceData()
}

function resetQuery() {
    state.queryParams.statusEq = null;
    state.queryParams.nameLike = null;
    state.queryParams.pageNo = 1;
    getServiceData();
}

const checkoutDetail = (item) => {
    router.push(`/modelmag/service-detail/ser/${parseInt(item.id)}`);
}

const changeServiceStatus = (row,status) => {
    ElMessageBox.confirm('是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        const res = await stopService({serviceId: row.id,status: status});
        if (res.code === 200) {
            ElMessage.success('操作成功');
            await getServiceData();
        }
    }).catch(() => {
    });
}

const copyUrl = (url) => {
    navigator.clipboard.writeText(url)
        .then(() => {
            // 复制成功后的处理，比如显示一个提示信息
            ElMessage.success('链接已复制到剪贴板');
        })
        .catch(err => {
            // 复制失败的处理
            ElMessage.error('复制链接到剪贴板失败');
        });
}
onMounted(async () => {
    await getServiceData();
})
</script>

<template>
    <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
        <div class="flex-1 overflow-x-hidden min-w-800">
            <div class="h-full min-w-800">
                <div class="min-w-min h-full py-6 px-8"
                     style="background: linear-gradient(180deg, #F7F9FF 0%, #F2F5FE 100%);">

                    <!-- Header -->
                    <div class="flex items-center justify-between mb-6 m-auto w-full">
                        <div class="text-[#140E35] text-3xl font-semibold">部署服务</div>
                        <!-- 部署服务一般没有直接的新建按钮，通常是从模型列表跳转，这里保留搜索功能 -->
                    </div>

                    <!-- Process Flow -->
                    <div v-if="state.showProcess" class="w-full bg-white rounded-xl p-6 mb-6 relative shadow-sm border border-gray-100">
                        <div class="absolute right-4 top-4 cursor-pointer hover:bg-gray-100 p-1 rounded-full transition-all" @click="state.showProcess = false">
                            <CloseOutlined class="text-gray-400 text-lg" />
                        </div>
                        <div class="font-bold text-lg mb-8 text-[#140E35] flex items-center gap-2">
                            <span class="w-1 h-6 bg-blue-600 rounded-full"></span>
                            使用流程
                        </div>
                        <div class="flex justify-around items-start px-4 relative">
                            <!-- Step 1 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-blue-50 flex items-center justify-center text-blue-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <CloudServerOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">选择模型</div>
                                <div class="text-gray-400 text-xs text-center">选择已训练好的模型</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 2 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <EditOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">配置服务</div>
                                <div class="text-gray-400 text-xs text-center">设置资源与参数</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 3 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <RocketOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">启动部署</div>
                                <div class="text-gray-400 text-xs text-center">一键发布在线服务</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 4 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <MonitorOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">调用监控</div>
                                <div class="text-gray-400 text-xs text-center">API调用与性能监控</div>
                            </div>
                        </div>
                    </div>

                    <!-- Search & Filter Bar -->
                    <div class="w-full bg-white rounded-lg p-4 mb-6 shadow-sm flex flex-col gap-4">
                        <div class="flex justify-between items-center border-b pb-2">
                            <a-tabs v-model:active-key="state.activeKey" class="custom-tabs">
                                <a-tab-pane tab="我的服务" :key="0"/>
                            </a-tabs>
                        </div>

                        <div class="flex items-center gap-4 flex-wrap">
                            <div class="flex items-center gap-2">
                                <span class="text-gray-500 text-sm">服务状态:</span>
                                <a-select v-model:value="state.queryParams.statusEq" placeholder="全部状态" style="width: 160px" @change="conditionSearch" allowClear>
                                    <a-select-option :value="0">全部状态</a-select-option>
                                    <a-select-option :value="1">部署中</a-select-option>
                                    <a-select-option :value="2">部署完成</a-select-option>
                                    <a-select-option :value="3">部署失败</a-select-option>
                                </a-select>
                            </div>

                            <div class="flex items-center gap-2 ml-auto">
                                <a-input placeholder="搜索服务名称" v-model:value="state.queryParams.nameLike" style="width: 240px" @pressEnter="conditionSearch">
                                    <template #prefix>
                                        <SearchOutlined class="text-gray-400"/>
                                    </template>
                                </a-input>
                                <el-button type="primary" @click="conditionSearch">搜索</el-button>
                                <el-button @click="resetQuery">重置</el-button>
                            </div>
                        </div>
                    </div>

                    <!-- Service List (Table) -->
                    <div class="w-full m-auto" v-if="state.total > 0">
                        <div class="grid gap-4">
                            <div
                                class="bg-white rounded-lg p-5 hover:shadow-lg transition-all duration-300 border border-gray-100 cursor-pointer group"
                                v-for="item in state.serviceList"
                                :key="item.id"
                                @click="checkoutDetail(item)"
                            >
                                <div class="flex justify-between items-start">
                                    <!-- Left: Icon & Info -->
                                    <div class="flex gap-5 flex-1">
                                        <!-- Icon -->
                                        <div class="w-14 h-14 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600 text-2xl flex-shrink-0 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300">
                                            <CloudServerOutlined />
                                        </div>

                                        <!-- Info -->
                                        <div class="flex flex-col justify-between flex-1">
                                            <div>
                                                <div class="flex items-center gap-3 mb-1">
                                                    <div class="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors">{{ item.name }}</div>
                                                    <el-tag v-if="item.status === 1" type="primary" effect="light" round>部署中</el-tag>
                                                    <el-tag v-if="item.status === 2" type="success" effect="light" round>部署完成</el-tag>
                                                    <el-tag v-if="item.status === 3" type="danger" effect="light" round>部署失败</el-tag>
                                                    <el-tag v-if="item.status === 4" type="warning" effect="light" round>已暂停</el-tag>
                                                </div>
                                                <div class="text-gray-500 text-sm line-clamp-1 mb-2" :title="item.description">
                                                    {{ item.description || '暂无描述' }}
                                                </div>
                                            </div>

                                            <div class="flex items-center gap-4 text-xs text-gray-400">
                                                <div class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                                                    <span class="font-medium">URL:</span>
                                                    <span class="truncate max-w-[200px]" :title="item.serviceUrl">{{ item.serviceUrl || '暂无' }}</span>
                                                    <el-button link icon="CopyDocument" size="small" @click.stop="copyUrl(item.serviceUrl)" title="复制链接" v-if="item.serviceUrl"></el-button>
                                                </div>
                                                <div>创建于: {{ item.createTime }}</div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Right: Actions -->
                                    <div class="flex flex-col items-end justify-center h-full gap-4 min-w-[150px]">
                                        <div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                            <el-button
                                                v-if="item.status === 2"
                                                type="warning"
                                                link
                                                icon="VideoPause"
                                                v-hasPermi="['modelmag:service-list:change']"
                                                @click.stop="changeServiceStatus(item,0)"
                                            >停止</el-button>

                                            <el-button
                                                v-if="item.status === 4"
                                                type="success"
                                                link
                                                icon="VideoPlay"
                                                v-hasPermi="['modelmag:service-list:change']"
                                                @click.stop="changeServiceStatus(item,1)"
                                            >启动</el-button>

                                            <el-button
                                                type="danger"
                                                link
                                                icon="Delete"
                                                @click.stop="handleDelete(item)"
                                                v-hasPermi="['modelmag:label:edit']"
                                            >删除</el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="flex justify-center mt-6">
                            <a-pagination
                                v-model:current="state.queryParams.pageNo"
                                :total="state.total"
                                :page-size="state.queryParams.pageSize"
                                :page-size-options="['10', '20', '50']"
                                show-size-changer
                                show-quick-jumper
                                :show-total="total => `共 ${total} 条数据`"
                                @change="handlePageChange"
                                @showSizeChange="onShowSizeChange"
                            />
                        </div>
                    </div>

                    <div class="w-full m-auto py-20" v-else>
                        <a-empty description="暂无服务"></a-empty>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
:deep(.custom-tabs .ant-tabs-nav) {
    margin-bottom: 0;
}
:deep(.custom-tabs .ant-tabs-tab) {
    padding: 12px 0;
    margin-right: 24px;
}
</style>
