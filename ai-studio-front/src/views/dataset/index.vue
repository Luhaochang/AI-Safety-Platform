<script setup>
import {
    SearchOutlined,
    TagsOutlined,
    LockOutlined,
    UnlockOutlined,
    FileImageOutlined,
    FileTextOutlined,
    TableOutlined,
    CloseOutlined,
    FolderAddOutlined,
    CloudUploadOutlined,
    AimOutlined,
    RocketOutlined,
    ArrowRightOutlined,
    ReloadOutlined
} from "@ant-design/icons-vue";
import CreateDatasetDailog from "@/views/dataset/components/createDatasetDailog.vue";
import {useRouter} from "vue-router";
import {listDataset} from "@/api/dataMag/dataset.js";
import {getAllTaskType} from "@/api/modelMag/scene.js";

const router = useRouter()
const datasetDialogRef = ref()
const state = reactive({
    activeKey: 0,
    datasetList: [],
    type: 2, // 权限筛选绑定值
    scene: -1, // 场景筛选绑定值
    marked: -1, // 标注状态筛选绑定值
    dataType: -1, // 数据类型筛选绑定值
    showProcess: true,
    queryParams: {
        pageNo: 1,
        pageSize: 12,
        dataSetNameLike: undefined,
        isPublicEq: undefined,
        taskTypeIdEq: undefined,
        isMarkedEq: undefined,
        dataTypeEq: undefined // Assuming backend supports filtering by type
    },
    total: 0
})

const sceneOptions = ref([
    { value: -1, label: '所有场景' }
])

const markedOptions = [
    { value: -1, label: '所有状态' },
    { value: 0, label: '未标注' },
    { value: 1, label: '标注中' },
    { value: 2, label: '已标注' },
]

const dataTypeOptions = [
    { value: -1, label: '所有类型' },
    { value: 1, label: '图片' },
    { value: 2, label: '文本' },
    { value: 3, label: '表格' },
]

const createDataset = () => {
    datasetDialogRef.value.openDialog();
}

const checkDetails = (item) => {
    router.push(`/datamag/dataset-detail/index/${item.id}`)
}

const getListByPage = () => {
    listDataset(state.queryParams).then(res => {
        if (res.code === 200) {
            state.datasetList = res.data.records.map(item => ({
                ...item,
                tag: JSON.parse(item.tag)
            }));
            state.total = res.data.total;

        }
    })
}

const conditionSearch = (val) => {
    if (val !== 2) {
        state.queryParams.isPublicEq = val;
    } else {
        state.queryParams.isPublicEq = undefined;
    }
    getListByPage()
}

const handleSceneChange = (val) => {
    state.queryParams.taskTypeIdEq = val === -1 ? undefined : val;
    getListByPage();
}

const handleMarkedChange = (val) => {
    state.queryParams.isMarkedEq = val === -1 ? undefined : val;
    getListByPage();
}

const handleDataTypeChange = (val) => {
    state.queryParams.dataTypeEq = val === -1 ? undefined : val;
    getListByPage();
}

const handlePagination = (pageNo) => {
    state.queryParams.pageNo = pageNo;
    getListByPage();
}

const queryByDatasetName = () => {
    setTimeout(() => {
        getListByPage()
    }, 1000)
}

const resetFilter = () => {
    state.type = 2;
    state.scene = -1;
    state.marked = -1;
    state.dataType = -1;
    state.queryParams.dataSetNameLike = undefined;
    state.queryParams.isPublicEq = undefined;
    state.queryParams.taskTypeIdEq = undefined;
    state.queryParams.isMarkedEq = undefined;
    state.queryParams.dataTypeEq = undefined;
    state.queryParams.pageNo = 1;
    getListByPage();
}

const getDataTypeIcon = (type) => {
    switch (type) {
        case 1: return FileImageOutlined;
        case 2: return FileTextOutlined;
        case 3: return TableOutlined;
        default: return FileImageOutlined;
    }
}

const getDataTypeLabel = (type) => {
    switch (type) {
        case 1: return '图片';
        case 2: return '文本';
        case 3: return '表格';
        default: return '未知';
    }
}

const getSceneOptions = () => {
    getAllTaskType().then(res => {
        if (res.code === 200) {
            sceneOptions.value = [
                { value: -1, label: '所有场景' },
                ...res.data.map(item => ({
                    value: item.id,
                    label: item.taskName
                }))
            ]
        }
    })
}

onMounted(() => {
    getListByPage()
    getSceneOptions()
})
</script>

<template>
    <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
        <div class="flex-1 overflow-x-hidden min-w-800">
            <div class="h-full min-w-800">
                <div class="min-w-min h-full py-6 px-8"
                     style="background: linear-gradient(180deg, #F7F9FF 0%, #F2F5FE 100%);">
                    <div class="flex items-center justify-between mb-2 m-auto w-full">
                        <div class="h-11 #140E35 text-4xl leading-10 font-medium">数据集</div>
                        <el-button type="primary" v-hasPermi="['datamag:dataset:add']"  @click="createDataset()">创建数据集</el-button>
                    </div>

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
                                <div class="text-[#140E35] font-medium text-base">创建数据集</div>
                                <div class="text-gray-400 text-xs text-center">支持多种数据类型</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 2 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <CloudUploadOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">导入数据</div>
                                <div class="text-gray-400 text-xs text-center">本地/云端快速导入</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 3 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <AimOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">智能标注</div>
                                <div class="text-gray-400 text-xs text-center">AI辅助高效标注</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 4 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <RocketOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">模型训练</div>
                                <div class="text-gray-400 text-xs text-center">一键启动训练任务</div>
                            </div>
                        </div>
                    </div>

                    <div class="w-full m-auto flex justify-between items-center relative mb-4">
                        <div class="flex flex-col mr-0 min-w-[160px]">
                          <a-tabs v-model:active-key="state.activeKey">
                              <a-tab-pane tab="我创建的" :key="0"/>
                          </a-tabs>
                        </div>

                        <div class="flex items-center gap-3">
                            <el-button link type="primary" @click="resetFilter">
                                <template #icon><ReloadOutlined /></template>
                                重置筛选
                            </el-button>

                            <a-select style="width: 120px" v-model:value="state.dataType" @change="handleDataTypeChange">
                                <a-select-option v-for="opt in dataTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                            </a-select>

                            <a-select style="width: 120px" v-model:value="state.scene" @change="handleSceneChange">
                                <a-select-option v-for="opt in sceneOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                            </a-select>

                            <a-select style="width: 120px" v-model:value="state.marked" @change="handleMarkedChange">
                                <a-select-option v-for="opt in markedOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</a-select-option>
                            </a-select>

                            <a-select style="width: 120px" v-model:value="state.type" @change="conditionSearch">
                                <a-select-option :value="2">所有权限</a-select-option>
                                <a-select-option :value="0">私有</a-select-option>
                                <a-select-option :value="1">公开</a-select-option>
                            </a-select>
                            <a-input style="width: 200px" placeholder="请输入数据集名称" @change="queryByDatasetName" v-model:value="state.queryParams.dataSetNameLike">
                                <template #prefix>
                                    <SearchOutlined></SearchOutlined>
                                </template>
                            </a-input>
                        </div>
                    </div>

                    <div class="w-full m-auto" v-if="state.activeKey===0 && state.total>0">
                        <div class="grid grid-cols-4 gap-5 mb-4">
                            <div
                                class="bg-white rounded-lg hover-shadow cursor-pointer relative flex flex-col"
                                style="padding: 20px; height: 220px" v-for="item in state.datasetList"
                                @click="checkDetails(item)">

                                <div class="flex justify-between items-start mb-2">
                                    <div class="flex items-center gap-2 flex-1 mr-2 overflow-hidden">
                                        <component :is="getDataTypeIcon(item.dataType)" class="text-blue-500 text-lg flex-shrink-0" />
                                        <div class="text-base font-semibold text-[#140e35] truncate" :title="item?.dataSetName">
                                            {{ item?.dataSetName }}
                                        </div>
                                    </div>
                                    <div class="text-[#565772]">
                                        <LockOutlined v-if="item.isPublic===0"/>
                                        <UnlockOutlined v-else/>
                                    </div>
                                </div>

                                <div class="text-xs text-[#565772] line-clamp-2 mb-auto h-[30px] overflow-hidden">
                                    {{ item?.dataSetAbstract }}
                                </div>

                                <div class="flex items-center gap-1 my-3 overflow-hidden">
                                    <TagsOutlined class="text-[#565772]"/>
                                    <div class="label-item truncate max-w-[60px]" v-for="tag in item.tag" :title="tag">{{ tag }}</div>
                                </div>

                                <div class="flex justify-between items-center pt-3 border-t border-gray-100">
                                    <div class="flex gap-2">
                                         <el-tag v-if="item.isMarked === 2" type="success" size="small">已标注</el-tag>
                                         <el-tag v-if="item.isMarked === 1" type="primary" size="small">标注中</el-tag>
                                         <el-tag v-if="item.isMarked === 0" type="info" size="small">未标注</el-tag>
                                         <el-tag type="warning" size="small" effect="plain">{{ getDataTypeLabel(item.dataType) }}</el-tag>
                                    </div>
                                    <div class="text-xs text-[#999]">
                                        {{ item.createTime?.split(' ')[0] }}
                                    </div>
                                </div>
                            </div>
                        </div>


                        <div class="flex justify-center">
                            <a-pagination v-model:current="state.queryParams.pageNo" :total="state.total"
                                          show-less-items v-if="state.total>10"
                                          :page-size="state.queryParams.pageSize"
                                          @change="handlePagination"  />
                        </div>
                    </div>

                    <div class="w-full m-auto" v-else>
                        <a-empty></a-empty>
                    </div>
                </div>

            </div>
        </div>
        <create-dataset-dailog ref="datasetDialogRef" @commit="getListByPage"></create-dataset-dailog>
    </div>
</template>

<style scoped lang="scss">
.hover-shadow:hover {
    box-shadow: 0px 12px 40px 0px rgba(181, 199, 235, 0.5);
}

.label-item {
    display: flex;
    align-items: center;
    height: 24px;
    border: 1px solid #d9dee9;
    padding: 0 5px;
    color: #565772;
    font-size: 12px;
    border-radius: 4px;
}
</style>
