<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getDatasetById, getDatasetByPath } from '@/api/dataMag/dataset.js';
import { getTaskById, savaTask, submitTask } from '@/api/dataMag/markTask.js';

import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeft, ArrowRight, Check, Warning } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const taskId = route.params.taskId;

const state = reactive({
    loading: false,
    dataList: [],
    allData: [],
    columns: [],
    pageNo: 1,
    pageSize: 20,
    total: 0,
    taskInfo: {},
    datasetUrl: '',
    currentFileName: '',
    labeledCount: 0
});

// Custom CSV Parser
const parseCSV = (text) => {
    const arr = [];
    let quote = false;
    let col = 0, c = 0;
    let row = 0;

    for (; c < text.length; c++) {
        let cc = text[c], nc = text[c+1];
        arr[row] = arr[row] || [];
        arr[row][col] = arr[row][col] || '';

        if (cc == '"' && quote && nc == '"') { arr[row][col] += cc; ++c; }
        else if (cc == '"') { quote = !quote; }
        else if (cc == ',' && !quote) { ++col; }
        else if (cc == '\r' && nc == '\n' && !quote) { ++row; col = 0; ++c; }
        else if (cc == '\n' && !quote) { ++row; col = 0; }
        else { arr[row][col] += cc; }
    }

    if (arr.length === 0) return [];

    const headers = arr[0];
    const data = [];
    for(let i=1; i<arr.length; i++) {
        if (arr[i].length > 0 && (arr[i].length > 1 || arr[i][0] !== '')) { // Skip empty rows
             let obj = {};
             // Handle case where row might have fewer columns than headers
             for(let j=0; j<headers.length; j++) {
                 obj[headers[j]] = arr[i][j] || '';
             }
             data.push(obj);
        }
    }
    return data;
};

// Custom CSV Stringifier
const unparseCSV = (data) => {
    if (data.length === 0) return '';
    // Exclude internal fields starting with _
    const headers = Object.keys(data[0]).filter(k => !k.startsWith('_'));
    // Add is_anomaly if not present (it is mapped from _isAnomaly)
    if (!headers.includes('is_anomaly')) headers.push('is_anomaly');

    const csvRows = [];
    csvRows.push(headers.map(h => `"${h}"`).join(','));

    for (const row of data) {
        const values = headers.map(header => {
            let val = row[header];
            if (header === 'is_anomaly') {
                val = row._isAnomaly ? 1 : 0;
            }
            const escaped = ('' + (val === undefined || val === null ? '' : val)).replace(/"/g, '""');
            return `"${escaped}"`;
        });
        csvRows.push(values.join(','));
    }
    return csvRows.join('\n');
};

const loadTaskInfo = async () => {
    const res = await getTaskById(taskId);
    if (res.code === 200) {
        state.taskInfo = res.data;
        const datasetRes = await getDatasetById(state.taskInfo.dataSetId);
        if (datasetRes.code === 200) {
            state.datasetUrl = datasetRes.data.fileUrl;
            await loadCsvData();
        }
    }
}

const loadCsvData = async () => {
    state.loading = true;
    try {
        const res = await getDatasetByPath({catalogue: state.datasetUrl});
        if (res.code === 200 && res.data && res.data.length > 0) {
            const csvFiles = res.data.filter(url => url.endsWith('.csv'));
            if (csvFiles.length > 0) {
                state.currentFileName = csvFiles[0].split('/').pop();
                // Add timestamp to prevent caching
                const response = await fetch(`${csvFiles[0]}?t=${new Date().getTime()}`);
                const csvText = await response.text();

                const rawData = parseCSV(csvText);

                // Generate columns from first row
                if (rawData.length > 0) {
                    state.columns = Object.keys(rawData[0]).map(key => ({
                        prop: key,
                        label: key,
                        width: 150
                    }));
                }

                const finishNum = state.taskInfo.finishNumber || 0;
                state.labeledCount = finishNum;

                state.allData = rawData.map((item, index) => ({
                    ...item,
                    _id: index,
                    _status: index < finishNum ? 1 : 0,
                    _isAnomaly: item.is_anomaly == 1 || item.is_anomaly == '1' || item.is_anomaly == 'true'
                }));

                // Use actual data length for total to ensure correct pagination
                state.total = state.allData.length;

                // Resume based on database finishNumber
                if (state.allData.length > 0) {
                    let targetIndex = finishNum;
                    if (targetIndex >= state.allData.length) targetIndex = state.allData.length - 1;
                    if (targetIndex < 0) targetIndex = 0;

                    state.pageNo = Math.floor(targetIndex / state.pageSize) + 1;
                }

                updatePageData();
            }
        }
    } finally {
        state.loading = false;
    }
};

const updatePageData = () => {
    const start = (state.pageNo - 1) * state.pageSize;
    const end = start + state.pageSize;
    state.dataList = state.allData.slice(start, end);
}

const handlePageChange = (val) => {
    state.pageNo = val;
    updatePageData();
};

const handleAnomalyChange = (row) => {
    row._status = 1; // Mark as modified
    // Update in allData
    const target = state.allData.find(d => d._id === row._id);
    if (target) {
        target._isAnomaly = row._isAnomaly;
        target._status = 1;
    }
};

const updateProgress = () => {
    // Calculate progress based on the end of the current page
    const currentProgress = Math.min(state.total, state.pageNo * state.pageSize);
    if (currentProgress > state.labeledCount) {
        state.labeledCount = currentProgress;
    }
};

const saveProgress = async () => {
    updateProgress();

    const csv = unparseCSV(state.allData);
    const file = new File([csv], state.currentFileName, { type: 'text/csv' });
    const formData = new FormData();
    formData.append('file', file);

    savaTask(taskId, state.labeledCount, formData).then(res => {
        if (res.code === 200) {
            ElMessage.success('保存成功');
        }
    });
};

const finishTask = () => {
    ElMessageBox.confirm('确定提交该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        updateProgress();

        const csv = unparseCSV(state.allData);
        const file = new File([csv], state.currentFileName, { type: 'text/csv' });
        const formData = new FormData();
        formData.append('file', file);

        submitTask(taskId, state.labeledCount, formData).then(res => {
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
                    <span class="text-xs text-gray-400">表格异常检测标注</span>
                </div>
            </div>
            <div class="flex items-center gap-6">
                <div class="flex flex-col items-end">
                    <div class="text-xs text-gray-500 mb-1">标注进度</div>
                    <div class="flex items-center gap-2">
                        <span class="font-mono font-bold text-blue-600">{{ state.labeledCount }}</span>
                        <span class="text-gray-300">/</span>
                        <span class="text-gray-600">{{ state.total }}</span>
                    </div>
                </div>
                <el-progress
                    type="circle"
                    :percentage="state.total > 0 ? Math.round((state.labeledCount / state.total) * 100) : 0"
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
        <div class="flex-1 p-6 overflow-hidden flex flex-col">
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 flex-1 flex flex-col overflow-hidden">
                <div class="p-4 border-b bg-gray-50/50 flex justify-between items-center flex-shrink-0">
                    <div class="flex items-center gap-2">
                        <span class="font-medium text-gray-700">数据列表</span>
                        <el-tag size="small" type="info" effect="plain">{{ state.total }} 行数据</el-tag>
                    </div>
                    <div class="flex items-center gap-2 text-sm text-gray-500 bg-orange-50 px-3 py-1.5 rounded-md border border-orange-100">
                        <el-icon class="text-orange-500"><Warning /></el-icon>
                        <span>请勾选异常数据行 (Is_Anomaly)</span>
                    </div>
                </div>

                <div class="flex-1 overflow-hidden p-0">
                    <el-table
                        v-loading="state.loading"
                        :data="state.dataList"
                        height="100%"
                        border
                        stripe
                        style="width: 100%"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600' }"
                    >
                        <!-- Index Column -->
                        <el-table-column
                            type="index"
                            label="序号"
                            width="80"
                            align="center"
                            fixed="left"
                        >
                            <template #default="scope">
                                {{ (state.pageNo - 1) * state.pageSize + scope.$index + 1 }}
                            </template>
                        </el-table-column>

                        <el-table-column
                            v-for="col in state.columns"
                            :key="col.prop"
                            :prop="col.prop"
                            :label="col.label"
                            :min-width="150"
                            show-overflow-tooltip
                        />

                        <!-- Annotation Column -->
                        <el-table-column label="是否异常 (Is_Anomaly)" width="180" align="center" fixed="right">
                            <template #default="scope">
                                <div class="flex justify-center">
                                    <el-checkbox
                                        v-model="scope.row._isAnomaly"
                                        @change="handleAnomalyChange(scope.row)"
                                        size="large"
                                        class="custom-checkbox"
                                        :class="{'is-checked-anomaly': scope.row._isAnomaly}"
                                    >
                                        <span :class="scope.row._isAnomaly ? 'text-red-500 font-medium' : 'text-gray-500'">
                                            {{ scope.row._isAnomaly ? '异常' : '正常' }}
                                        </span>
                                    </el-checkbox>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <div class="p-3 border-t bg-gray-50/50 flex justify-end flex-shrink-0">
                    <el-pagination
                        v-model:current-page="state.pageNo"
                        v-model:page-size="state.pageSize"
                        :total="state.total"
                        :page-sizes="[20, 50, 100]"
                        layout="total, sizes, prev, pager, next, jumper"
                        @size-change="handlePageChange"
                        @current-change="handlePageChange"
                        background
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
:deep(.el-table__inner-wrapper::before) {
    display: none;
}

:deep(.custom-checkbox .el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: #ef4444;
    border-color: #ef4444;
}

:deep(.custom-checkbox .el-checkbox__label) {
    padding-left: 8px;
}
</style>
