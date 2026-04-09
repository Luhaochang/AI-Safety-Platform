<script setup>
import {
    SearchOutlined,
    TagsOutlined,
    AppstoreOutlined,
    FileImageOutlined,
    FontSizeOutlined,
    TableOutlined,
    VideoCameraOutlined,
    AimOutlined,
    BarChartOutlined,
    FolderAddOutlined,
    ArrowRightOutlined,
    CloseOutlined,
    EditOutlined,
    CheckCircleOutlined,
    ReloadOutlined
} from "@ant-design/icons-vue";
import CreateDatasetDailog from "@/views/dataset/components/createDatasetDailog.vue";
import {useRouter} from "vue-router";
import {deleteTask, listTask, savaTask, submitTask} from "@/api/dataMag/markTask.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {getDatasetById, getDatasetByPath} from "@/api/dataMag/dataset.js";
import axios from "axios";
import ServiceSelectDialog from "@/views/model/service/components/serviceSelectDialog.vue";
import AutoMarkDialog from "@/views/dataset/components/autoMarkDialog.vue";

const autoMarkDialogRef = ref()
const router = useRouter()
const state = reactive({
    activeKey: 0,
    showProcess: true,
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        taskNameLike: '',
        dataType: undefined,
        status: undefined
    },
    taskList: [],
    total: 0,
})

const dataTypeOptions = [
    { value: undefined, label: '全部类型' },
    { value: 1, label: '图片' },
    { value: 2, label: 'JSON' },
    { value: 3, label: 'CSV' },
]

const statusOptions = [
    { value: undefined, label: '全部状态' },
    { value: 0, label: '标注中' },
    { value: 1, label: '已完成' },
]

const formatDataType = (val) => {
    const option = dataTypeOptions.find(opt => opt.value === val);
    return option ? option.label : '未知类型';
}

const getDataTypeIcon = (val) => {
    switch (val) {
        case 1: return AimOutlined; // Detection
        case 2: return FontSizeOutlined; // JSON
        case 3: return TableOutlined; // CSV
        default: return TagsOutlined;
    }
}

const createTask = () => {
    router.push('/datamag/mark-task-add/task-create');
}

const statusFormat = (status) => {
    switch (status) {
        case 0:
            return '标注中';
        case 1:
            return '已完成';
    }
}

const toMark = (item) => {
    switch (item.dataType) {
        case 1:
            router.push(`/datamag/mark-detection/mark-by-obj-detection/${item.id}`);
            break;
        case 2:
            router.push(`/datamag/mark-text/mark-by-text/${item.id}`);
            break;
        case 3:
            router.push(`/datamag/mark-table/mark-by-table/${item.id}`);
            break;
    }
}

const getList = () => {
    listTask(state.queryParams).then(res => {
        if (res.code === 200) {
            state.taskList = res.data.records;
            state.total = res.data.total;
        }
    })
}

const handleSearch = () => {
    state.queryParams.pageNo = 1;
    getList();
}

const resetFilter = () => {
    state.queryParams.taskNameLike = '';
    state.queryParams.dataType = undefined;
    state.queryParams.status = undefined;
    state.queryParams.pageNo = 1;
    getList();
}

const deleteTaskById = (item) => {
    ElMessageBox.confirm('确定删除该标注任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        // Logic to save current progress before delete (as in original code)
        // Simplified for clarity, keeping original logic structure
        const res = await getDatasetById(item.dataSetId);
        if (res.code === 200 && res.data) {
             // ... existing logic to save json ...
             // For brevity, assuming we can just delete or keeping the save logic if critical
             // Re-implementing the save-before-delete logic from original code:
            const jsonRes = await getDatasetByPath({catalogue: `${res.data.fileUrl}`});
            function getJsonUrls(urls) {
                const jsonUrls = [];
                urls.forEach(url => {
                    if (url.endsWith('.json')) {
                        jsonUrls.push(url);
                    }
                });
                return jsonUrls;
            }
            let temp = []
            const jsonUrls = getJsonUrls(jsonRes.data);
            if (jsonUrls.length > 0) {
                const timestamp = new Date().getTime();
                const newUrl = `${jsonUrls[0]}?timestamp=${timestamp}`;
                fetch(newUrl).then(response => response.json()).then(data => {
                    data.forEach((item) => {
                        item.type = '';
                        temp.push(item);
                    });
                    const a = JSON.stringify(temp)
                    let file = new File([new Blob([a], {type: 'text/json'})], 'file.json')
                    const formData = new FormData()
                    formData.append('file', file);
                    savaTask(item.id, temp.length, formData).then(res0 => {
                        if (res0.code === 200) {
                            deleteTask({id: item.id}).then(res1 => {
                                if (res1.code === 200) {
                                    getList();
                                    ElMessage.success('删除成功');
                                }
                            })
                        }
                    })
                }).catch(() => {
                     // If fetch fails, try delete anyway
                     deleteTask({id: item.id}).then(res1 => {
                        if (res1.code === 200) {
                            getList();
                            ElMessage.success('删除成功');
                        }
                    })
                })
            } else {
                 deleteTask({id: item.id}).then(res1 => {
                    if (res1.code === 200) {
                        getList();
                        ElMessage.success('删除成功');
                    }
                })
            }
        } else {
             deleteTask({id: item.id}).then(res1 => {
                if (res1.code === 200) {
                    getList();
                    ElMessage.success('删除成功');
                }
            })
        }
    }).catch(() => {});
}

const autoMark = async (item) => {
    autoMarkDialogRef.value.openDialog(item)
}

const calculateProgress = (item) => {
    if (!item.setNumber || item.setNumber === 0) return 0;
    return Math.round(((item.finishNumber || 0) / item.setNumber) * 100);
}

onMounted(() => {
    getList();
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
                        <div class="text-[#140E35] text-3xl font-semibold">标注任务</div>
                        <el-button type="primary" size="large" icon="Plus" @click="createTask()" v-hasPermi="['datamag:mark-task:add']">
                            新建任务
                        </el-button>
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
                                    <FolderAddOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">新建任务</div>
                                <div class="text-gray-400 text-xs text-center">创建标注任务</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 2 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <EditOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">在线标注</div>
                                <div class="text-gray-400 text-xs text-center">支持多种标注类型</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 3 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <BarChartOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">进度监控</div>
                                <div class="text-gray-400 text-xs text-center">实时查看标注进度</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 4 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <CheckCircleOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">任务完成</div>
                                <div class="text-gray-400 text-xs text-center">构建高质量数据集</div>
                            </div>
                        </div>
                    </div>

                    <!-- Search & Filter Bar -->
                    <div class="w-full bg-white rounded-lg p-4 mb-6 shadow-sm flex flex-col gap-4">
                        <div class="flex justify-between items-center border-b pb-2">
                            <a-tabs v-model:active-key="state.activeKey" class="custom-tabs">
                                <a-tab-pane tab="我创建的" :key="0"/>
                            </a-tabs>
                        </div>

                        <div class="flex items-center gap-4 flex-wrap">
                            <el-button link type="primary" @click="resetFilter">
                                <template #icon><ReloadOutlined /></template>
                                重置筛选
                            </el-button>

                            <div class="flex items-center gap-2">
                                <span class="text-gray-500 text-sm">标注类型:</span>
                                <a-select v-model:value="state.queryParams.dataType" placeholder="全部类型" style="width: 160px" @change="handleSearch" allowClear>
                                    <a-select-option v-for="item in dataTypeOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                                </a-select>
                            </div>

                            <div class="flex items-center gap-2">
                                <span class="text-gray-500 text-sm">状态:</span>
                                <a-select v-model:value="state.queryParams.status" placeholder="全部状态" style="width: 160px" @change="handleSearch" allowClear>
                                    <a-select-option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
                                </a-select>
                            </div>

                            <div class="flex items-center gap-2 ml-auto">
                                <a-input placeholder="搜索任务名称" v-model:value="state.queryParams.taskNameLike" style="width: 240px" @pressEnter="handleSearch">
                                    <template #prefix>
                                        <SearchOutlined class="text-gray-400"/>
                                    </template>
                                </a-input>
                                <el-button type="primary" @click="handleSearch">搜索</el-button>
                            </div>
                        </div>
                    </div>

                    <!-- Task List -->
                    <div class="w-full m-auto" v-if="state.activeKey===0 && state.total>0">
                        <div class="grid gap-4">
                            <div
                                class="bg-white rounded-lg p-5 hover:shadow-lg transition-all duration-300 border border-gray-100 cursor-pointer group"
                                v-for="item in state.taskList"
                                :key="item.id"
                            >
                                <div class="flex justify-between items-start">
                                    <!-- Left: Icon & Info -->
                                    <div class="flex gap-5 flex-1">
                                        <!-- Icon -->
                                        <div class="w-14 h-14 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600 text-2xl flex-shrink-0 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300">
                                            <component :is="getDataTypeIcon(item.dataType)" />
                                        </div>

                                        <!-- Info -->
                                        <div class="flex flex-col justify-between flex-1">
                                            <div>
                                                <div class="flex items-center gap-3 mb-1">
                                                    <div class="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors">{{ item.taskName }}</div>
                                                    <el-tag size="small" :type="item.status === 1 ? 'success' : 'warning'" effect="light" round>
                                                        {{ statusFormat(item.status) }}
                                                    </el-tag>
                                                </div>
                                                <div class="text-gray-500 text-sm line-clamp-1 mb-2" :title="item.taskDescription">
                                                    {{ item.taskDescription || '暂无描述' }}
                                                </div>
                                            </div>

                                            <div class="flex items-center gap-4 text-xs text-gray-400">
                                                <div class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                                                    <TagsOutlined />
                                                    <span>{{ formatDataType(item.dataType) }}</span>
                                                </div>
                                                <div class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                                                    <span class="font-medium">数据集:</span>
                                                    <span>{{ item.dataSetName || '未知' }}</span>
                                                </div>
                                                <div>更新于: {{ item.updateTime }}</div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Right: Progress & Actions -->
                                    <div class="flex flex-col items-end justify-between h-full gap-4 min-w-[200px]">
                                        <div class="w-full">
                                            <div class="flex justify-between text-xs text-gray-500 mb-1">
                                                <span>标注进度</span>
                                                <span>{{ item.finishNumber || 0 }} / {{ item.setNumber || 0 }}</span>
                                            </div>
                                            <el-progress
                                                :percentage="calculateProgress(item)"
                                                :status="item.status === 1 ? 'success' : ''"
                                                :stroke-width="8"
                                            />
                                        </div>

                                        <div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                                            <el-button
                                                v-if="item.status !== 1"
                                                type="primary"
                                                link
                                                icon="MagicStick"
                                                @click.stop="autoMark(item)"
                                                v-hasPermi="['datamag:mark-task:mark']"
                                            >
                                                自动标注
                                            </el-button>

                                            <el-button
                                                v-if="item.status !== 1"
                                                type="primary"
                                                size="small"
                                                @click.stop="toMark(item)"
                                                v-hasPermi="['datamag:mark-task:mark']"
                                            >
                                                去标注
                                            </el-button>
                                            <el-button
                                                v-else
                                                type="success"
                                                size="small"
                                                plain
                                                disabled
                                            >
                                                已完成
                                            </el-button>

                                            <el-button
                                                type="danger"
                                                link
                                                icon="Delete"
                                                @click.stop="deleteTaskById(item)"
                                                v-hasPermi="['datamag:mark-task:remove']"
                                            >
                                            </el-button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="flex justify-center mt-6">
                            <a-pagination
                                v-model:current="state.queryParams.pageNo"
                                :total="state.total"
                                show-less-items
                                :page-size="state.queryParams.pageSize"
                                @change="getList"
                            />
                        </div>
                    </div>

                    <div class="w-full m-auto py-20" v-else>
                        <a-empty description="暂无标注任务"></a-empty>
                    </div>
                </div>
            </div>
        </div>
        <auto-mark-dialog ref="autoMarkDialogRef"></auto-mark-dialog>
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
