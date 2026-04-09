<template>
    <div class="app-container home bg-[#f5f7fa] min-h-screen p-6">
        <el-scrollbar>
            <div class="grid grid-cols-3 gap-6 grid-rows-8" style="height: 1200px;">
                <!-- 开发统计 -->
                <div class="grid-item col-start-1 col-end-3 row-start-1 row-end-3 x-over bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="p-6 flex flex-col h-full">
                        <div class="flex items-center justify-between mb-2">
                            <div class="text-lg font-bold text-[#140E35] flex items-center gap-2">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                开发统计
                            </div>
                        </div>
                        <div class="flex-1 min-h-0">
                            <div class="grid grid-cols-4 gap-4 h-full">
                                <div class="h-full">
                                    <div class="stat-card bg-gradient-to-br from-blue-50 to-white border border-blue-100 rounded-lg p-3 h-full flex flex-col justify-between hover:shadow-md transition-all duration-300 relative overflow-hidden">
                                        <div class="absolute -right-4 -top-4 w-20 h-20 bg-blue-100 rounded-full opacity-20"></div>
                                        <div class="absolute -left-4 -bottom-4 w-16 h-16 bg-blue-200 rounded-full opacity-20"></div>
                                        <div class="flex justify-center">
                                            <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                                                <DatabaseOutlined class="text-xl" />
                                            </div>
                                        </div>
                                        <a-statistic
                                            title="数据集总数"
                                            :value="state.statisticsCount.dataSetNum"
                                            :precision="0"
                                            :value-style="{ color: '#1677ff', fontWeight: 600, fontSize: '22px', textAlign: 'center' }"
                                            class="mt-1"
                                        >
                                        </a-statistic>
                                        <div class="mt-2 pt-2 border-t border-blue-100">
                                            <a-statistic
                                                title="已标注数据集"
                                                :value="state.statisticsCount.markedDataSetNum"
                                                :precision="0"
                                                :value-style="{ color: '#52c41a', fontWeight: 600, fontSize: '18px', textAlign: 'center' }"
                                            >
                                            </a-statistic>
                                        </div>
                                        <div class="text-xs text-gray-400 text-center mt-1">截止 {{ formatDateYYYYMMDD(new Date()) }}</div>
                                    </div>
                                </div>
                                <div class="h-full">
                                    <div class="stat-card bg-gradient-to-br from-purple-50 to-white border border-purple-100 rounded-lg p-3 h-full flex flex-col justify-between hover:shadow-md transition-all duration-300 relative overflow-hidden">
                                        <div class="absolute -right-4 -top-4 w-20 h-20 bg-purple-100 rounded-full opacity-20"></div>
                                        <div class="absolute -left-4 -bottom-4 w-16 h-16 bg-purple-200 rounded-full opacity-20"></div>
                                        <div class="flex justify-center">
                                            <div class="w-10 h-10 rounded-full bg-purple-100 flex items-center justify-center text-purple-600">
                                                <AppstoreOutlined class="text-xl" />
                                            </div>
                                        </div>
                                        <a-statistic
                                            title="模型总数"
                                            :value="state.statisticsCount.modelNum"
                                            :precision="0"
                                            :value-style="{ color: '#722ed1', fontWeight: 600, fontSize: '22px', textAlign: 'center' }"
                                            class="mt-1"
                                        >
                                        </a-statistic>
                                        <div class="mt-2 pt-2 border-t border-purple-100">
                                            <a-statistic
                                                title="非基础模型"
                                                :value="state.statisticsCount.unofficialModelNum"
                                                :precision="0"
                                                :value-style="{ color: '#eb2f96', fontWeight: 600, fontSize: '18px', textAlign: 'center' }"
                                            >
                                            </a-statistic>
                                        </div>
                                        <div class="text-xs text-gray-400 text-center mt-1">截止 {{ formatDateYYYYMMDD(new Date()) }}</div>
                                    </div>
                                </div>
                                <div class="h-full">
                                    <div class="stat-card bg-gradient-to-br from-orange-50 to-white border border-orange-100 rounded-lg p-3 h-full flex flex-col justify-between hover:shadow-md transition-all duration-300 relative overflow-hidden">
                                        <div class="absolute -right-4 -top-4 w-20 h-20 bg-orange-100 rounded-full opacity-20"></div>
                                        <div class="absolute -left-4 -bottom-4 w-16 h-16 bg-orange-200 rounded-full opacity-20"></div>
                                        <div class="flex justify-center">
                                            <div class="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                                                <CodeOutlined class="text-xl" />
                                            </div>
                                        </div>
                                        <a-statistic
                                            title="训练作业总数"
                                            :value="state.statisticsCount.pipelineTaskNum"
                                            :precision="0"
                                            :value-style="{ color: '#fa8c16', fontWeight: 600, fontSize: '22px', textAlign: 'center' }"
                                            class="mt-1"
                                        >
                                        </a-statistic>
                                        <div class="mt-2 pt-2 border-t border-orange-100">
                                            <a-statistic
                                                title="已完成训练作业"
                                                :value="state.statisticsCount.finishedPipelineTaskNum"
                                                :precision="0"
                                                :value-style="{ color: '#faad14', fontWeight: 600, fontSize: '18px', textAlign: 'center' }"
                                            >
                                            </a-statistic>
                                        </div>
                                        <div class="text-xs text-gray-400 text-center mt-1">截止 {{ formatDateYYYYMMDD(new Date()) }}</div>
                                    </div>
                                </div>
                                <div class="h-full">
                                    <div class="stat-card bg-gradient-to-br from-cyan-50 to-white border border-cyan-100 rounded-lg p-3 h-full flex flex-col justify-between hover:shadow-md transition-all duration-300 relative overflow-hidden">
                                        <div class="absolute -right-4 -top-4 w-20 h-20 bg-cyan-100 rounded-full opacity-20"></div>
                                        <div class="absolute -left-4 -bottom-4 w-16 h-16 bg-cyan-200 rounded-full opacity-20"></div>
                                        <div class="flex justify-center">
                                            <div class="w-10 h-10 rounded-full bg-cyan-100 flex items-center justify-center text-cyan-600">
                                                <CloudServerOutlined class="text-xl" />
                                            </div>
                                        </div>
                                        <a-statistic
                                            title="服务总数"
                                            :value="state.statisticsCount.serviceNum"
                                            :precision="0"
                                            :value-style="{ color: '#13c2c2', fontWeight: 600, fontSize: '22px', textAlign: 'center' }"
                                            class="mt-1"
                                        >
                                        </a-statistic>
                                        <div class="mt-2 pt-2 border-t border-cyan-100">
                                            <a-statistic
                                                title="在线服务数"
                                                :value="state.statisticsCount.aliveServiceNum"
                                                :precision="0"
                                                :value-style="{ color: '#52c41a', fontWeight: 600, fontSize: '18px', textAlign: 'center' }"
                                            >
                                            </a-statistic>
                                        </div>
                                        <div class="text-xs text-gray-400 text-center mt-1">截止 {{ formatDateYYYYMMDD(new Date()) }}</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 资源管理 -->
                <div class="grid-item row-start-1 col-start-3 row-end-5 relative x-over bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="flex flex-col h-full">
                        <div class="p-4 pb-0">
                            <div class="flex items-center justify-between mb-3">
                                <div class="text-lg font-bold text-[#140E35] flex items-center gap-2">
                                    <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                    资源管理
                                </div>
                                <div class="flex items-center gap-2">
                                    <a-badge :count="state.activeAlertCount" :number-style="{ backgroundColor: '#ff4d4f', fontSize: '10px' }">
                                        <AlertOutlined class="text-gray-400 text-base" />
                                    </a-badge>
                                    <a-button type="primary" ghost size="small" @click="navigateTo3D">
                                        <ExpandOutlined /> 3D全景
                                    </a-button>
                                </div>
                            </div>
                            <!-- 资产层统计 -->
                            <div class="grid grid-cols-3 gap-1.5 mb-3">
                                <div v-for="layer in state.layerStats" :key="layer.key"
                                     class="rounded-lg px-2 py-1.5 border border-gray-100 bg-gray-50/50">
                                    <div class="flex items-center justify-between">
                                        <span class="text-[10px] font-medium" :style="{ color: layer.color }">{{ layer.name }}</span>
                                        <div>
                                            <span class="text-xs font-bold text-gray-800">{{ layer.count }}</span>
                                            <span class="text-[9px] text-gray-400 ml-0.5">{{ layer.unit }}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!-- 告警摘要 -->
                            <div v-if="state.alerts.length > 0" class="mb-2">
                                <div v-for="alert in state.alerts.slice(0, 2)" :key="alert.id"
                                     class="rounded px-2 py-1 text-[10px] mb-1 border"
                                     :style="{ borderColor: alertLevelColor(alert.level) + '30', backgroundColor: alertLevelColor(alert.level) + '08' }">
                                    <span class="text-gray-600 truncate block">{{ alert.message }}</span>
                                </div>
                            </div>
                        </div>
                        <div class="flex-1 px-4 overflow-auto custom-scrollbar">
                            <el-table :data="state.tableData" style="width: 100%" size="small" :header-cell-style="{background:'#f8fafc', color:'#606266'}">
                                <el-table-column label="节点" align="center" prop="nodeName" min-width="90">
                                    <template #default="scope">
                                        <div class="font-medium text-blue-600 text-xs">{{ scope.row.nodeName }}</div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="地址" align="center" prop="nodeAddress" width="90">
                                    <template #default="scope">
                                        <span class="text-xs">{{ scope.row.nodeAddress }}</span>
                                    </template>
                                </el-table-column>
                                <el-table-column label="CPU" align="center" width="80">
                                    <template #default="scope">
                                        <div class="flex flex-col items-center">
                                            <el-progress :percentage="parseFloat(scope.row.cpuUtilization) || 0" :status="getProgressStatus(scope.row.cpuUtilization)" :stroke-width="5" :show-text="false" class="w-12"/>
                                            <span class="text-[10px] font-bold">{{ formatPercentage(scope.row.cpuUtilization) }}</span>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="GPU" align="center" width="80">
                                    <template #default="scope">
                                        <div class="flex flex-col items-center">
                                            <el-progress :percentage="parseFloat(scope.row.gpuUtilization) || 0" :status="getProgressStatus(scope.row.gpuUtilization)" :stroke-width="5" :show-text="false" class="w-12"/>
                                            <span class="text-[10px] font-bold">{{ formatPercentage(scope.row.gpuUtilization) }}</span>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="内存" align="center" width="80">
                                    <template #default="scope">
                                        <div class="flex flex-col items-center">
                                            <el-progress :percentage="parseFloat(scope.row.memoryUtilization) || 0" :status="getProgressStatus(scope.row.memoryUtilization)" :stroke-width="5" :show-text="false" class="w-12"/>
                                            <span class="text-[10px] font-bold">{{ formatPercentage(scope.row.memoryUtilization) }}</span>
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                        <div class="p-3 border-t border-gray-100 flex justify-center bg-white">
                            <a-pagination @change="getTable" v-model:current="state.queryParams.pageNo"
                                          v-model:page-size="state.queryParams.pageSize" simple
                                          :total="state.queryParams.total" size="small"/>
                        </div>
                    </div>
                </div>

                <!-- 数据集标注进度 -->
                <div class="grid-item row-start-3 col-start-1 row-end-5 x-over bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="p-6 flex flex-col h-full">
                        <div class="flex items-center justify-between mb-4">
                            <div class="text-lg font-bold text-[#140E35] flex items-center gap-2">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                数据集标注进度
                            </div>
                        </div>
                        <div class="flex-1 flex flex-col justify-between">
                            <service-count-chart :width="state.chatWidth" :value-data="state.datasetCompletionRate" class="flex-1"/>
                            <div class="flex justify-center mt-2">
                                <el-pagination layout="prev, pager, next" @current-change="handleChangePage"
                                               :page-size="state.queryParamsOnProcess.pageSize"
                                               :current-page="state.queryParamsOnProcess.pageNo"
                                               :total="state.queryParamsOnProcess.total"
                                               small
                                               background
                                ></el-pagination>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 数据集分类统计 -->
                <div class="grid-item row-start-3 col-start-2 row-end-5 x-over bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="p-6 flex flex-col h-full">
                        <div class="flex items-center justify-between mb-4">
                            <div class="text-lg font-bold text-[#140E35] flex items-center gap-2">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                数据集分类统计
                            </div>
                        </div>
                        <div class="flex-1 min-h-0 flex flex-row items-center gap-4" v-if="state.datasetClassification.length !==0">
                            <!-- 左侧统计信息 -->
                            <div class="flex flex-col gap-3 flex-1 h-full">
                                <!-- 应用场景最多 -->
                                <div class="bg-gradient-to-br from-blue-50 to-white rounded-xl p-3 border border-blue-100 flex-1 flex flex-col justify-center relative overflow-hidden">
                                     <div class="absolute -right-6 -top-6 w-24 h-24 bg-blue-100 rounded-full opacity-30"></div>
                                     <div class="relative z-10">
                                        <div class="text-gray-500 text-sm mb-1 flex items-center gap-1">
                                            <PieChartOutlined class="text-blue-500 text-base"/>
                                            应用场景最多
                                        </div>
                                        <div class="text-xl font-bold text-gray-800 mb-2 truncate" :title="state.datasetClassification[0]?.name">
                                            {{ state.datasetClassification[0]?.name }}
                                            <span class="text-base text-blue-500 ml-1">{{ state.datasetClassification[0]?.percentage }}%</span>
                                        </div>
                                        <div class="flex flex-wrap gap-1">
                                            <span v-for="(item, index) in state.datasetClassification.slice(1, 4)" :key="index"
                                                  class="bg-white/80 px-1.5 py-0.5 rounded text-xs text-gray-600 border border-blue-100 truncate max-w-full">
                                                {{ item.name }}: {{ item.percentage }}%
                                            </span>
                                        </div>
                                     </div>
                                </div>

                                <!-- 用户和角色统计 -->
                                <div class="grid grid-cols-2 gap-3 flex-1 shrink-0">
                                    <div class="bg-gray-50 rounded-xl p-2 flex flex-col justify-center items-center border border-gray-100 hover:shadow-sm transition-all h-full">
                                        <div class="w-7 h-7 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 mb-0.5">
                                            <UserOutlined class="text-base"/>
                                        </div>
                                        <div class="text-gray-500 text-xs">用户总数</div>
                                        <div class="text-base font-bold text-gray-800">{{ state.userCount }}</div>
                                    </div>
                                    <div class="bg-gray-50 rounded-xl p-2 flex flex-col justify-center items-center border border-gray-100 hover:shadow-sm transition-all h-full">
                                        <div class="w-7 h-7 rounded-full bg-orange-100 flex items-center justify-center text-orange-600 mb-0.5">
                                            <TeamOutlined class="text-base"/>
                                        </div>
                                        <div class="text-gray-500 text-xs">角色总数</div>
                                        <div class="text-base font-bold text-gray-800">{{ state.roleCount }}</div>
                                    </div>
                                </div>
                            </div>

                            <!-- 右侧图表 -->
                            <div class="flex-1 h-full flex items-center justify-center min-w-0">
                                <host-sec-chart :chat-value="state.datasetClassification" class="w-full h-full" :show-legend="false"/>
                            </div>
                        </div>
                        <div class="flex-1 flex items-center justify-center" v-else>
                             <a-empty description="暂无数据"></a-empty>
                        </div>
                    </div>
                </div>

                <!-- 总览图表区域 -->
                <div class="grid-item col-start-1 col-end-4 row-start-5 row-end-9 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                    <div class="p-6 flex flex-col h-full">
                        <div class="flex items-center justify-between mb-6">
                            <div class="text-lg font-bold text-[#140E35] flex items-center gap-2">
                                <span class="w-1 h-5 bg-blue-600 rounded-full"></span>
                                平台总览
                            </div>
                        </div>

                        <div class="flex-container h-full w-full gap-6">
                            <!-- 第一行 -->
                            <div class="flex gap-6 h-full">
                                <div class="w-full bg-gray-50 rounded-xl p-4 border border-gray-100">
                                    <public-model-pie-chart :title="'发布模型情况统计'"
                                                            :width="state.chatWidth"
                                                            :value-data="state.releaseModelList" class="h-full"></public-model-pie-chart>
                                </div>
                                <!-- <div class="w-1/2 bg-gray-50 rounded-xl p-4 border border-gray-100">
                                    <public-model-bar-chart :width="state.chatWidth"
                                                            :value-data="state.modelTrainProcess"
                                                            :title="'模型部署进度统计'" class="h-full"></public-model-bar-chart>
                                </div> -->
                            </div>

                            <!-- 第二行 -->
                            <div class="bg-gray-50 rounded-xl p-4 border border-gray-100">
                                <task-count-chart :title="'模型训练时间分布'" :width="state.chatWidth" :type="1"
                                                  :value-data="state.trainingTimeList" class="h-full"/>
                            </div>

                            <!-- 第三行 -->
                            <div class="bg-gray-50 rounded-xl p-4 border border-gray-100">
                                <task-count-chart :title="'各服务调用情况'" :width="state.chatWidth" :type="2"
                                                  :value-data="state.modelDeploymentStatusList" class="h-full"/>
                            </div>

                            <!-- 第四行 -->
                            <div class="bg-gray-50 rounded-xl p-4 border border-gray-100">
                                <task-count-chart :title="'在线服务数统计'" :width="state.chatWidth" :type="3"
                                                  :value-data="state.onLineServiceList" class="h-full"/>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </el-scrollbar>
    </div>
</template>

<script setup>
import {
    ArrowUpOutlined,
    DatabaseOutlined,
    AppstoreOutlined,
    CodeOutlined,
    CloudServerOutlined,
    PieChartOutlined,
    UserOutlined,
    TeamOutlined,
    AlertOutlined,
    ExpandOutlined,
    RobotOutlined,
    BookOutlined
} from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { getAlerts, getAlertsMock } from '@/api/digitalTwin/index.js';
import {Empty} from 'ant-design-vue';
import ServiceCountChart from "@/views/chartsComponents/serviceCountChart.vue";
import HostSecChart from "@/views/chartsComponents/hostSecChart.vue";
import TaskCountChart from "@/views/chartsComponents/lineEfficiency/taskCountChart.vue";
import PublicModelPieChart from "@/views/chartsComponents/publicModelPieChart.vue";
import PublicModelBarChart from "@/views/chartsComponents/publicModelBarChart.vue";
import {formatDateYYYYMMDD} from "../utils/index.js";
import {
  getAllTaskTypes,
  getColonyInfo,
  getDatasetClassification,
  getDatasetMarkProgress,
  getModelClassification,
  getOverviewCount,
  getTrainProcess,
  getTrainTaskTimeDistribution
} from "@/api/home.js";
import {listUser} from "@/api/system/user";
import {listRole} from "@/api/system/role";

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
const version = ref('3.8.8')
const router = useRouter();

const state = reactive({
    chatWidth: 0,
    tableData: [],
    queryParams: {
        pageNo: 1,
        pageSize: 5,
        total: 0
    },

    queryParamsOnProcess: {
        pageNo: 1,
        pageSize: 5,
        total: 0
    },

    statisticsCount: {
        "dataSetNum": 0,
        "markedDataSetNum": 0,
        "modelNum": 0,
        "unofficialModelNum": 0,
        "pipelineTaskNum": 0,
        "finishedPipelineTaskNum": 0,
        "serviceNum": 0,
        "aliveServiceNum": 0
    },

    releaseModelList: [],
    modelTrainProcess: [],
    trainingTimeList: [],
    modelDeploymentStatusList: [],
    onLineServiceList: [],

    datasetClassification: [],

    datasetCompletionRate: [],

    userCount: 0,
    roleCount: 0,

    taskTypes: [],

    // 资源管理数据
    layerStats: [
        { key: 'infra', name: '算力资源', count: 6, unit: '节点', color: '#4299e1' },
        { key: 'data', name: '数据资产', count: 48, unit: '资产', color: '#48bb78' },
        { key: 'model', name: '模型资产', count: 12, unit: '模型', color: '#9f7aea' },
        { key: 'knowledge', name: '知识资产', count: 4, unit: '知识库', color: '#ed8936' },
        { key: 'service', name: '推理服务', count: 8, unit: '服务', color: '#38b2ac' },
        { key: 'app', name: '业务应用', count: 3, unit: '应用', color: '#ed64a6' }
    ],
    alerts: [],
    activeAlertCount: 0
})

async function getTable(url) {
    const res = await getColonyInfo(state.queryParams);

    if (res.code === 200) {
        state.tableData = res.data.records;
        state.queryParams.total = res.data.total;
    }

}

const getChartData = () => {


    state.onLineServiceList = [
        {
            date: '星期一',
            count: 112,
        },
        {
            date: '星期二',
            count: 130,
        },
        {
            date: '星期三',
            count: 304,
        },
        {
            date: '星期四',
            count: 274,
        },
        {
            date: '星期五',
            count: 301,
        },
        {
            date: '星期六',
            count: 218,
        },
        {
            date: '星期日',
            count: 177,
        },
    ]


    state.modelDeploymentStatusList = [
        {
            count: 172,
            date: '2024-10-28',
            click: 100
        },
        {
            count: 201,
            date: '2024-10-27',
            click: 239
        },
        {
            count: 834,
            date: '2024-10-26',
            click: 493
        },
        {
            count: 232,
            date: '2024-10-25',
            click: 23
        },
        {
            count: 943,
            date: '2024-10-24',
            click: 784
        },
        {
            count: 80,
            date: '2024-10-23',
            click: 369
        },
        {
            count: 103,
            date: '2024-10-22',
            click: 792
        },
        {
            count: 1003,
            date: '2024-10-21',
            click: 463
        },
        {
            count: 23,
            date: '2024-10-20',
            click: 766
        }
    ];

}

// 获取总体计数
const getCount = async () => {
    const res = await getOverviewCount();
    if (res.code === 200 && res.data) {
        state.statisticsCount = res.data;
    }
}

// 获取数据集标注进度
const getDatasetMarkProcessData = async () => {
    const res = await getDatasetMarkProgress(state.queryParamsOnProcess);
    if (res.code === 200 && res.data) {
        state.datasetCompletionRate = res.data.records.map(item => ({
            name: item.dataSetTaskName,
            value: item.completionProgress
        }))
        state.queryParamsOnProcess.total = res.data.total;
    }
}

function formatTaskType(id) {
    const taskType = state.taskTypes.find(item => item.id === id);
    return taskType ? taskType.taskName : '未知类型';
}

// 获取数据集分类
const getDatasetClassificationData = async () => {

    const res = await getDatasetClassification();
    if (res.code === 200 && res.data) {
        state.datasetClassification = res.data.map(item => ({
            name: formatTaskType(item.taskTypeId),
            value: item.dataSetNum
        }))

        let totalValue = state.datasetClassification.reduce((sum, dataSet) => sum + dataSet.value, 0);
        state.datasetClassification.sort((a, b) => b.value - a.value); // 从大到小排序
        state.datasetClassification.forEach(dataSet => {
            dataSet.percentage = ((dataSet.value / totalValue) * 100).toFixed(0);
        });


    }
}

const handleChangePage = async (val) => {
    state.queryParamsOnProcess.pageNo = val;
    await getDatasetMarkProcessData()
}

// 模型分类 - bar
const getModelClassificationData = async () => {
    const res = await getModelClassification();
    if (res.code === 200 && res.data) {
        state.releaseModelList = res.data.map(item => ({
            name: formatTaskType(item.taskTypeId),
            value: item.modelNum
        }))

    }
}

// 模型训练进度 - line
const getModelTrainProcessData = async () => {
    const res = await getTrainProcess();
    if (res.code === 200 && res.data) {
        state.modelTrainProcess = res.data;
    }
}

// 训练时间分布 - bar
const getTranTimeData = async () => {
    function formatTimeDistribution(type) {
        switch (type) {
            case 0:
                return '<1小时';
            case 1:
                return '1~24小时';
            case 2:
                return '1~7天';
            case 3:
                return '1周~1月';
            case 4:
                return '>1月'
        }
    }

    const res = await getTrainTaskTimeDistribution();
    if (res.code === 200 && res.data.length > 0) {
        state.trainingTimeList = res.data.map(item => ({
            region: formatTimeDistribution(item.type),
            count: item.modelNum
        }));
    }
}

const formatPercentage = (val) => {
  const num = parseFloat(val);
  return (isNaN(num) ? 0 : num).toFixed(2) + '%'
}

const formatMemory = (val) => {
    if (!val || !val.memoryNumber) return '0GB';
    return  (val.memoryNumber / 1024 / 1024 /1024).toFixed(0) + 'GB'
}

const getProgressStatus = (val) => {
    const percentage = parseFloat(val);
    if (isNaN(percentage)) return 'success';
    if (percentage < 60) return 'success';
    if (percentage < 85) return 'warning';
    return 'exception';
}

// 获取用户总数
const getUserTotal = async () => {
    const res = await listUser({ pageNo: 1, pageSize: 1, delFlagEq: 0 });
    if (res.code === 200) {
        state.userCount = res.data.total;
    }
}

// 获取角色总数
const getRoleTotal = async () => {
    const res = await listRole({ pageNo: 1, pageSize: 1, delFlagEq: 0 });
    if (res.code === 200) {
        state.roleCount = res.data.total;
    }
}

// 获取所有任务类型
const getTaskTypes = async () => {
    const res = await getAllTaskTypes();
    if (res.code === 200) {
        state.taskTypes = res.data;
    }
}

// 加载资源管理告警数据
const loadAlertData = async () => {
    try {
        let res = await getAlerts().catch(() => null);
        if (!res || res.code !== 200) res = await getAlertsMock();
        if (res.code === 200) {
            state.alerts = res.data.slice(0, 3);
            state.activeAlertCount = res.data.filter(a => a.status === 'active').length;
        }
    } catch (e) { /* ignore */ }
};

const navigateTo3D = () => {
    router.push('/digital-twin/overview');
};

const alertLevelColor = (level) => ({ critical: '#ff4d4f', warning: '#faad14' }[level] || '#1677ff');

onMounted(async () => {
    await getTaskTypes(); // 先获取任务类型
    await getCount();
    await getDatasetMarkProcessData();
    await getDatasetClassificationData();
    await getModelClassificationData();
    await getModelTrainProcessData();
    await getTranTimeData();
    await getTable();
    await getUserTotal();
    await getRoleTotal();
    await loadAlertData();

    getChartData()
    window.onresize = () => {   //屏幕尺寸变化就重新赋值
        const el = document.querySelector('.flex-container');
        if (el) {
            state.chatWidth = el.clientWidth;
        }
    };
})
</script>

<style scoped lang="scss">
.home {
    font-family: "Inter", "Helvetica Neue", Helvetica, Arial, sans-serif;
    color: #1f2937;
}

.custom-scrollbar {
    &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
    }
    &::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 3px;
    }
    &::-webkit-scrollbar-track {
        background: transparent;
    }
}

.flex-container {
    display: grid;
    grid-template-columns:repeat(2, 1fr);
    grid-template-rows:repeat(2, 1fr);
    min-width: 200px;
}

:deep(.ant-statistic-title) {
    text-align: center;
    color: #6b7280;
    font-size: 14px;
    margin-bottom: 8px;
}

:deep(.ant-statistic-content) {
    text-align: center;
}

:deep(.el-table th.el-table__cell) {
    background-color: #f8fafc !important;
    color: #475569;
    font-weight: 600;
}

:deep(.el-progress-bar__outer) {
    background-color: #e2e8f0;
}
</style>
