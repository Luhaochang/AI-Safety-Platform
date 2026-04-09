<template>
    <div class="app-container">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span class="text-lg font-bold">集群资源管理</span>
                </div>
            </template>
            
            <!-- 资源层统计 -->
            <div class="grid grid-cols-6 gap-4 mb-6">
                <div v-for="layer in layerStats" :key="layer.key"
                     class="rounded-lg p-4 border-2 hover:shadow-lg transition-all duration-300"
                     :style="{ borderColor: layer.color + '40', backgroundColor: layer.color + '08' }">
                    <div class="flex flex-col items-center">
                        <span class="text-sm font-medium mb-2" :style="{ color: layer.color }">{{ layer.name }}</span>
                        <div>
                            <span class="text-2xl font-bold text-gray-800">{{ layer.count }}</span>
                            <span class="text-xs text-gray-400 ml-1">{{ layer.unit }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 告警信息 -->
            <el-alert
                v-if="alerts.length > 0"
                :title="`当前有 ${activeAlertCount} 条活跃告警`"
                type="warning"
                :closable="false"
                class="mb-4"
            >
                <div v-for="alert in alerts.slice(0, 3)" :key="alert.id" class="text-sm mb-1">
                    {{ alert.message }}
                </div>
            </el-alert>

            <!-- 集群节点表格 -->
            <el-table :data="tableData" style="width: 100%" v-loading="loading">
                <el-table-column label="节点名称" prop="nodeName" min-width="120">
                    <template #default="scope">
                        <span class="font-medium text-blue-600">{{ scope.row.nodeName }}</span>
                    </template>
                </el-table-column>
                <el-table-column label="节点地址" prop="nodeAddress" min-width="140" />
                <el-table-column label="CPU使用率" align="center" width="150">
                    <template #default="scope">
                        <el-progress :percentage="parseFloat(scope.row.cpuUtilization) || 0" 
                                     :status="getProgressStatus(scope.row.cpuUtilization)" />
                    </template>
                </el-table-column>
                <el-table-column label="GPU使用率" align="center" width="150">
                    <template #default="scope">
                        <el-progress :percentage="parseFloat(scope.row.gpuUtilization) || 0" 
                                     :status="getProgressStatus(scope.row.gpuUtilization)" />
                    </template>
                </el-table-column>
                <el-table-column label="内存使用率" align="center" width="150">
                    <template #default="scope">
                        <el-progress :percentage="parseFloat(scope.row.memoryUtilization) || 0" 
                                     :status="getProgressStatus(scope.row.memoryUtilization)" />
                    </template>
                </el-table-column>
                <el-table-column label="状态" align="center" width="100">
                    <template #default="scope">
                        <el-tag :type="scope.row.status === 'running' ? 'success' : 'danger'">
                            {{ scope.row.status === 'running' ? '运行中' : '离线' }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>

            <div class="mt-4 flex justify-end">
                <el-pagination
                    v-model:current-page="queryParams.pageNo"
                    v-model:page-size="queryParams.pageSize"
                    :total="queryParams.total"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper"
                    @size-change="getTable"
                    @current-change="getTable"
                />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { getClusterNodes, getClusterNodesMock } from '@/api/cluster/index.js';
import { getAlerts, getAlertsMock } from '@/api/alert/index.js';

const loading = ref(false);
const tableData = ref([]);
const alerts = ref([]);
const activeAlertCount = ref(0);

const layerStats = ref([
    { key: 'infra', name: '算力资源', count: 6, unit: '节点', color: '#4299e1' },
    { key: 'data', name: '数据资产', count: 48, unit: '资产', color: '#48bb78' },
    { key: 'model', name: '模型资产', count: 12, unit: '模型', color: '#9f7aea' },
    { key: 'knowledge', name: '知识资产', count: 4, unit: '知识库', color: '#ed8936' },
    { key: 'service', name: '推理服务', count: 8, unit: '服务', color: '#38b2ac' },
    { key: 'app', name: '业务应用', count: 3, unit: '应用', color: '#ed64a6' }
]);

const queryParams = reactive({
    pageNo: 1,
    pageSize: 10,
    total: 0
});

const getTable = async () => {
    loading.value = true;
    try {
        let res = await getClusterNodes(queryParams).catch(() => null);
        if (!res || res.code !== 200) {
            res = await getClusterNodesMock(queryParams);
        }
        if (res.code === 200) {
            tableData.value = res.data.records || res.data;
            queryParams.total = res.data.total || res.data.length;
        }
    } finally {
        loading.value = false;
    }
};

const loadAlertData = async () => {
    try {
        let res = await getAlerts().catch(() => null);
        if (!res || res.code !== 200) res = await getAlertsMock();
        if (res.code === 200) {
            alerts.value = res.data.slice(0, 5);
            activeAlertCount.value = res.data.filter(a => a.status === 'active').length;
        }
    } catch (e) { /* ignore */ }
};

const getProgressStatus = (value) => {
    const num = parseFloat(value) || 0;
    if (num >= 90) return 'exception';
    if (num >= 70) return 'warning';
    return 'success';
};

onMounted(() => {
    getTable();
    loadAlertData();
});
</script>

<style scoped>
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
