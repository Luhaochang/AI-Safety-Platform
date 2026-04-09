<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getTaskById, savaTask, submitTask } from '@/api/dataMag/markTask.js';
import { getDatasetById, getDatasetByPath } from '@/api/dataMag/dataset.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight, Check, Edit, Document } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const taskId = route.params.taskId;

const state = reactive({
    loading: false,
    currentIndex: 0,
    total: 0,
    currentData: null,
    annotation: {
        output: '',
        label: ''
    },
    dataList: [],
    taskInfo: {},
    pageNo: 1,
    pageSize: 20,
    currentFileName: '',
});

const loadTaskInfo = async () => {
    const res = await getTaskById(taskId);
    if (res.code === 200) {
        state.taskInfo = res.data;
        const datasetRes = await getDatasetById(state.taskInfo.dataSetId);
        if (datasetRes.code === 200) {
            await loadJsonData(datasetRes.data.fileUrl);
        }
    }
}

const loadJsonData = async (url) => {
    state.loading = true;
    try {
        const res = await getDatasetByPath({catalogue: url});
        if (res.code === 200 && res.data && res.data.length > 0) {
            const jsonFiles = res.data.filter(u => u.endsWith('.json'));
            if (jsonFiles.length > 0) {
                state.currentFileName = jsonFiles[0].split('/').pop();
                // Add timestamp to prevent caching
                const response = await fetch(`${jsonFiles[0]}?t=${new Date().getTime()}`);
                const jsonData = await response.json();

                const finishNum = state.taskInfo.finishNumber || 0;

                state.dataList = jsonData.map((item, index) => ({
                    ...item,
                    _id: index,
                    _status: index < finishNum ? 1 : 0,
                    _annotation: item.output || '' // Pre-fill if exists
                }));

                // Use setNumber from DB as total if available, otherwise use list length
                state.total = state.taskInfo.setNumber || state.dataList.length;

                if (state.dataList.length > 0) {
                    // Resume based on database finishNumber
                    let targetIndex = finishNum;

                    // Ensure targetIndex is within bounds of the loaded data
                    if (targetIndex >= state.dataList.length) {
                        targetIndex = state.dataList.length - 1;
                    }
                    if (targetIndex < 0) targetIndex = 0;

                    selectData(targetIndex);
                    state.pageNo = Math.floor(targetIndex / state.pageSize) + 1;
                }
            }
        }
    } finally {
        state.loading = false;
    }
};

const currentPageData = computed(() => {
    const start = (state.pageNo - 1) * state.pageSize;
    const end = start + state.pageSize;
    return state.dataList.slice(start, end);
});

const selectData = (index) => {
    if (index >= 0 && index < state.dataList.length) {
        state.currentIndex = index;
        state.currentData = state.dataList[index];
        state.annotation.output = state.currentData._annotation || '';
    }
};

const handlePrev = () => {
    if (state.currentIndex > 0) {
        selectData(state.currentIndex - 1);
        // Auto switch page if needed
        const newPage = Math.floor((state.currentIndex) / state.pageSize) + 1;
        if (newPage !== state.pageNo) {
            state.pageNo = newPage;
        }
    }
};

const handleNext = () => {
    if (state.currentIndex < state.dataList.length - 1) {
        selectData(state.currentIndex + 1);
        // Auto switch page if needed
        const newPage = Math.floor((state.currentIndex) / state.pageSize) + 1;
        if (newPage !== state.pageNo) {
            state.pageNo = newPage;
        }
    }
};

const handlePageChange = (val) => {
    state.pageNo = val;
    // Select first item of the new page
    const firstIndex = (val - 1) * state.pageSize;
    if (firstIndex < state.dataList.length) {
        selectData(firstIndex);
    }
};

const handleSaveCurrent = () => {
    if (state.currentData) {
        state.currentData._annotation = state.annotation.output;
        state.currentData._status = 1;
        ElMessage.success('当前条目已暂存');
        handleNext();
    }
};

const saveProgress = async () => {
    const resultData = state.dataList.map(item => {
        const { _id, _status, _annotation, ...rest } = item;
        return { ...rest, output: _annotation };
    });

    const jsonStr = JSON.stringify(resultData, null, 2);
    const file = new File([jsonStr], state.currentFileName || 'annotations.json', { type: 'application/json' });
    const formData = new FormData();
    formData.append('file', file);

    const labeledCount = state.dataList.filter(i => i._status === 1).length;

    savaTask(taskId, labeledCount, formData).then(res => {
        if (res.code === 200) {
            ElMessage.success('保存进度成功');
        }
    });
};

const finishTask = () => {
    ElMessageBox.confirm('确定提交该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        const resultData = state.dataList.map(item => {
            const { _id, _status, _annotation, ...rest } = item;
            return { ...rest, output: _annotation };
        });

        const jsonStr = JSON.stringify(resultData, null, 2);
        const file = new File([jsonStr], state.currentFileName || 'result.json', { type: 'application/json' });
        const formData = new FormData();
        formData.append('file', file);

        const labeledCount = state.dataList.filter(i => i._status === 1).length;

        submitTask(taskId, labeledCount, formData).then(res => {
            if (res.code === 200) {
                ElMessage.success('提交成功');
                router.go(-1);
            }
        });
    });
};

const goBack = () => {
    router.go(-1);
};

onMounted(() => {
    loadTaskInfo();
});
</script>

<template>
    <div class="flex flex-col bg-gray-50" style="height: calc(100vh - 84px); overflow: hidden;">
        <!-- Header -->
        <div class="h-16 bg-white border-b flex items-center justify-between px-6 shadow-sm flex-shrink-0 z-10">
            <div class="flex items-center gap-4">
                <el-button link class="!text-gray-600 hover:!text-blue-600" @click="goBack">
                    <el-icon class="mr-1"><ArrowLeft /></el-icon> 返回
                </el-button>
                <div class="h-6 w-px bg-gray-200"></div>
                <div class="flex flex-col">
                    <span class="font-semibold text-gray-800 text-lg">{{ state.taskInfo.taskName }}</span>
                    <span class="text-xs text-gray-400">文本生成标注任务</span>
                </div>
            </div>
            <div class="flex items-center gap-6">
                <div class="flex flex-col items-end">
                    <div class="text-xs text-gray-500 mb-1">标注进度</div>
                    <div class="flex items-center gap-2">
                        <span class="font-mono font-bold text-blue-600">{{ state.currentIndex + 1 }}</span>
                        <span class="text-gray-300">/</span>
                        <span class="text-gray-600">{{ state.total }}</span>
                    </div>
                </div>
                <el-progress
                    type="circle"
                    :percentage="state.total > 0 ? Math.round(((state.currentIndex + 1) / state.total) * 100) : 0"
                    :width="40"
                    :stroke-width="4"
                    :show-text="false"
                />
                <div class="h-8 w-px bg-gray-200 mx-2"></div>
                <div class="flex gap-3">
                    <el-button plain @click="saveProgress">保存进度</el-button>
                    <el-button type="primary" @click="finishTask">提交任务</el-button>
                </div>
            </div>
        </div>

        <!-- Main Content -->
        <div class="flex-1 flex overflow-hidden">
            <!-- Left: Data List -->
            <div class="w-80 bg-white border-r flex flex-col flex-shrink-0 shadow-[4px_0_24px_rgba(0,0,0,0.02)] z-0">
                <div class="p-4 border-b bg-gray-50/50 flex justify-between items-center flex-shrink-0">
                    <div class="font-medium text-gray-700 flex items-center gap-2">
                        <el-icon><Document /></el-icon> 数据列表
                    </div>
                    <el-tag size="small" type="info" effect="plain">{{ state.total }} 条数据</el-tag>
                </div>
                <div class="flex-1 overflow-y-auto custom-scrollbar">
                    <div
                        v-for="(item) in currentPageData"
                        :key="item._id"
                        class="group p-4 border-b border-gray-50 cursor-pointer hover:bg-blue-50/50 transition-all duration-200 relative"
                        :class="{'bg-blue-50 !border-l-4 !border-l-blue-500': item._id === state.currentIndex, 'border-l-4 border-l-transparent': item._id !== state.currentIndex}"
                        @click="selectData(item._id)"
                    >
                        <div class="flex items-center justify-between mb-2">
                            <span class="font-medium text-gray-700 group-hover:text-blue-600 transition-colors">Item #{{ item._id + 1 }}</span>
                            <el-tag v-if="item._status === 1" type="success" size="small" effect="dark" round>
                                <el-icon class="mr-1"><Check /></el-icon> 已标注
                            </el-tag>
                            <el-tag v-else type="info" size="small" effect="plain" round>未标注</el-tag>
                        </div>
                        <div class="text-gray-500 text-sm line-clamp-2 leading-relaxed">{{ item.instruction || item.input || 'No content' }}</div>
                    </div>
                </div>
                <!-- Pagination -->
                <div class="p-3 border-t bg-gray-50/50 flex justify-center flex-shrink-0">
                    <el-pagination
                        small
                        background
                        layout="prev, pager, next"
                        :total="state.total"
                        :page-size="state.pageSize"
                        v-model:current-page="state.pageNo"
                        @current-change="handlePageChange"
                    />
                </div>
            </div>

            <!-- Center: Workspace -->
            <div class="flex-1 p-6 overflow-y-auto bg-gray-50/30 flex flex-col gap-6 custom-scrollbar">
                <div v-if="state.currentData" class="flex-1 flex flex-col gap-6 min-h-0 max-w-5xl mx-auto w-full">
                    <!-- Input Display -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex-shrink-0">
                        <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/30 flex items-center gap-2">
                            <div class="w-1 h-4 bg-blue-500 rounded-full"></div>
                            <span class="font-medium text-gray-700">输入内容 (Input)</span>
                        </div>
                        <div class="p-6">
                            <div class="bg-gray-50 rounded-lg p-5 text-gray-700 leading-relaxed text-base min-h-[120px] max-h-[300px] overflow-y-auto custom-scrollbar border border-gray-100 whitespace-pre-wrap font-sans">
                                {{ state.currentData.instruction || state.currentData.input }}
                            </div>
                        </div>
                    </div>

                    <!-- Output Edit -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden flex-1 flex flex-col min-h-0">
                        <div class="px-6 py-4 border-b border-gray-100 bg-gray-50/30 flex items-center justify-between">
                            <div class="flex items-center gap-2">
                                <div class="w-1 h-4 bg-green-500 rounded-full"></div>
                                <span class="font-medium text-gray-700">期望输出 (Output)</span>
                            </div>
                            <el-tag size="small" effect="plain"><el-icon class="mr-1"><Edit /></el-icon> 编辑模式</el-tag>
                        </div>
                        <div class="flex-1 p-6 flex flex-col min-h-0">
                            <el-input
                                v-model="state.annotation.output"
                                type="textarea"
                                placeholder="在此处输入期望的输出内容..."
                                class="flex-1 h-full custom-textarea"
                                resize="none"
                            />
                        </div>
                    </div>

                    <!-- Footer Actions -->
                    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex justify-between items-center flex-shrink-0 sticky bottom-0 z-10">
                        <el-button plain :icon="ArrowLeft" @click="handlePrev" :disabled="state.currentIndex === 0">上一条</el-button>

                        <div class="flex gap-4">
                            <el-button type="primary" size="large" class="!px-8" @click="handleSaveCurrent">
                                <el-icon class="mr-2"><Check /></el-icon> 确认并下一条
                            </el-button>
                        </div>

                        <el-button plain :icon="ArrowRight" @click="handleNext" :disabled="state.currentIndex === state.dataList.length - 1">下一条</el-button>
                    </div>
                </div>

                <div v-else class="flex-1 flex flex-col items-center justify-center text-gray-400 gap-4">
                    <el-empty description="暂无数据或正在加载..." />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
:deep(.custom-textarea .el-textarea__inner) {
    height: 100%;
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
    padding: 1.5rem;
    font-size: 15px;
    line-height: 1.6;
    border: 1px solid #e5e7eb;
    border-radius: 0.5rem;
    background-color: #fff;
    transition: all 0.2s;
}

:deep(.custom-textarea .el-textarea__inner:focus) {
    border-color: #409eff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #cbd5e1;
}
</style>
