<script setup>
import {
    CloudServerOutlined, DatabaseOutlined, RocketOutlined, SettingOutlined,
    ReloadOutlined, PlayCircleOutlined, PauseCircleOutlined, CameraOutlined,
    CheckCircleOutlined, CloseCircleOutlined, SyncOutlined, HistoryOutlined,
    AppstoreOutlined, ClusterOutlined, SaveOutlined, ExperimentOutlined,
    SwapOutlined, InfoCircleOutlined, ThunderboltOutlined, EyeOutlined
} from '@ant-design/icons-vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';

const state = reactive({
    activeScenario: null,
    envInstances: [],
    snapshots: [],
    loading: false,
    snapshotLoading: false,
    configDialogVisible: false,
    snapshotDialogVisible: false,
    snapshotName: '',
    scenarioForm: {
        name: '',
        knowledgeBase: '',
        model: '',
        promptTemplate: '',
        description: ''
    }
});

// ====== Mock 场景模板 ======
const scenarioTemplates = ref([
    { id: 1, name: '标准RAG安全验证', desc: '基于电力调度知识库的标准攻防验证场景', kb: '电力调度规程库', model: 'Qwen2-7B-Instruct', status: 'ready', icon: 'experiment' },
    { id: 2, name: 'BOGA-RAGP攻击测试', desc: '针对双目标遗传算法投毒攻击的专项验证', kb: '工业专利文档库', model: 'GPT-4o', status: 'ready', icon: 'thunder' },
    { id: 3, name: '组合式护栏防御评测', desc: '输入+检索+输出三级护栏联合防御测试', kb: '综合知识库', model: 'Llama-3-8B', status: 'ready', icon: 'shield' },
    { id: 4, name: '跨模型迁移验证', desc: '验证攻击方法在不同检索器与生成模型间的迁移性', kb: '金融报告库(FinQA)', model: '多模型切换', status: 'ready', icon: 'swap' },
]);

// ====== Mock 环境实例 ======
const envInstances = ref([
    { id: 'ENV-001', name: '标准RAG安全验证', scenario: '标准RAG安全验证', status: 'running', kb: '电力调度规程库', kbVersion: 'v2.3', model: 'Qwen2-7B-Instruct', retriever: 'Contriever', vectorIndex: 'ChromaDB-HNSW', promptTemplate: '标准调度问答模板', createTime: '2025-04-01 09:30:00', uptime: '2h 35min' },
    { id: 'ENV-002', name: 'BOGA攻击测试环境', scenario: 'BOGA-RAGP攻击测试', status: 'stopped', kb: '工业专利文档库', kbVersion: 'v1.1', model: 'GPT-4o', retriever: 'DPR-nq', vectorIndex: 'ChromaDB-HNSW', promptTemplate: '工业问答模板', createTime: '2025-04-01 10:15:00', uptime: '-' },
    { id: 'ENV-003', name: '护栏联合防御环境', scenario: '组合式护栏防御评测', status: 'running', kb: '综合知识库', kbVersion: 'v3.0', model: 'Llama-3-8B', retriever: 'ANCE', vectorIndex: 'ChromaDB-HNSW', promptTemplate: '安全增强模板', createTime: '2025-04-01 11:00:00', uptime: '1h 20min' },
]);

// ====== Mock 快照 ======
const snapshots = ref([
    { id: 'SNAP-001', name: '攻击前基线快照', envId: 'ENV-001', createTime: '2025-04-01 09:35:00', size: '256MB', desc: '标准验证环境初始状态' },
    { id: 'SNAP-002', name: 'BOGA攻击后快照', envId: 'ENV-001', createTime: '2025-04-01 10:20:00', size: '312MB', desc: '注入15篇毒化文档后的知识库状态' },
    { id: 'SNAP-003', name: '防御部署后快照', envId: 'ENV-003', createTime: '2025-04-01 11:05:00', size: '280MB', desc: '三级护栏全部启用后的环境状态' },
]);

// ====== Mock 资源监控 ======
const resourceStats = ref({
    cpu: 45, memory: 62, gpu: 78, gpuMem: 54, disk: 38,
    services: [
        { name: '检索服务 (Contriever)', status: 'running', port: 8001, latency: '12ms' },
        { name: '生成服务 (Qwen2-7B)', status: 'running', port: 8002, latency: '850ms' },
        { name: '向量数据库 (ChromaDB)', status: 'running', port: 8765, latency: '3ms' },
        { name: '嵌入服务 (SentenceTransformer)', status: 'running', port: 8003, latency: '45ms' },
        { name: '困惑度评估器 (Qwen2.5-1.5B)', status: 'stopped', port: 8004, latency: '-' },
    ]
});

const statusColor = (s) => ({ running: 'green', stopped: 'red', ready: 'blue', error: 'orange' }[s] || 'default');
const statusText = (s) => ({ running: '运行中', stopped: '已停止', ready: '就绪', error: '异常' }[s] || s);

const handleInitEnv = (tpl) => {
    state.activeScenario = tpl;
    state.configDialogVisible = true;
    state.scenarioForm = { name: tpl.name + ' - 实例', knowledgeBase: tpl.kb, model: tpl.model, promptTemplate: '标准调度问答模板', description: tpl.desc };
};

const handleConfirmInit = () => {
    state.configDialogVisible = false;
    const newId = 'ENV-' + String(envInstances.value.length + 1).padStart(3, '0');
    const now = new Date();
    const timeStr = now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0') + '-' + String(now.getDate()).padStart(2,'0') + ' ' + String(now.getHours()).padStart(2,'0') + ':' + String(now.getMinutes()).padStart(2,'0') + ':' + String(now.getSeconds()).padStart(2,'0');
    envInstances.value.unshift({
        id: newId,
        name: state.scenarioForm.name,
        scenario: state.activeScenario.name,
        status: 'running',
        kb: state.scenarioForm.knowledgeBase,
        kbVersion: 'v2.3',
        model: state.scenarioForm.model,
        retriever: 'Contriever',
        vectorIndex: 'ChromaDB-HNSW',
        promptTemplate: state.scenarioForm.promptTemplate,
        createTime: timeStr,
        uptime: '0min'
    });
    ElMessage.success('环境初始化成功！实例 ' + newId + ' 已创建并启动运行');
    // 模拟服务全部启动
    setTimeout(() => {
        resourceStats.value.services.forEach(s => { s.status = 'running'; s.latency = s.latency === '-' ? '50ms' : s.latency; });
    }, 500);
};

const handleToggleEnv = (env) => {
    env.status = env.status === 'running' ? 'stopped' : 'running';
    env.uptime = env.status === 'running' ? '0min' : '-';
    ElMessage.success(env.status === 'running' ? '环境已启动' : '环境已停止');
};

const handleSnapshot = (env) => {
    state.snapshotDialogVisible = true;
    state.snapshotName = env.name + ' - 快照 ' + new Date().toLocaleTimeString();
};

const handleConfirmSnapshot = () => {
    state.snapshotDialogVisible = false;
    const newSnapId = 'SNAP-' + String(snapshots.value.length + 1).padStart(3, '0');
    const now = new Date();
    const timeStr = now.getFullYear() + '-' + String(now.getMonth()+1).padStart(2,'0') + '-' + String(now.getDate()).padStart(2,'0') + ' ' + String(now.getHours()).padStart(2,'0') + ':' + String(now.getMinutes()).padStart(2,'0') + ':' + String(now.getSeconds()).padStart(2,'0');
    snapshots.value.unshift({
        id: newSnapId,
        name: state.snapshotName,
        envId: envInstances.value[0]?.id || 'ENV-001',
        createTime: timeStr,
        size: (200 + Math.floor(Math.random() * 150)) + 'MB',
        desc: '手动创建的环境快照',
    });
    ElMessage.success('快照 ' + newSnapId + ' 创建成功');
};

const handleRollback = (snap) => {
    ElMessage.success('环境已回滚至: ' + snap.name);
};

// ====== ECharts 资源监控 ======
const resourceChartRef = ref(null);
onMounted(() => {
    if (resourceChartRef.value) {
        const chart = echarts.init(resourceChartRef.value);
        chart.setOption({
            tooltip: { trigger: 'axis' },
            legend: { data: ['CPU', 'GPU', '内存', '显存'], top: 0, textStyle: { fontSize: 11 } },
            grid: { top: 30, bottom: 25, left: 40, right: 15 },
            xAxis: { type: 'category', data: ['09:00', '09:30', '10:00', '10:30', '11:00', '11:30', '12:00'], axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', fontSize: 10 } },
            series: [
                { name: 'CPU', type: 'line', smooth: true, data: [30, 35, 45, 52, 45, 38, 42], lineStyle: { width: 2 }, areaStyle: { opacity: 0.05 } },
                { name: 'GPU', type: 'line', smooth: true, data: [60, 65, 78, 85, 78, 72, 75], lineStyle: { width: 2 }, areaStyle: { opacity: 0.05 } },
                { name: '内存', type: 'line', smooth: true, data: [55, 58, 62, 65, 62, 60, 61], lineStyle: { width: 2 }, areaStyle: { opacity: 0.05 } },
                { name: '显存', type: 'line', smooth: true, data: [40, 45, 54, 60, 54, 50, 52], lineStyle: { width: 2 }, areaStyle: { opacity: 0.05 } },
            ]
        });
        window.addEventListener('resize', () => chart.resize());
    }
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="mb-6">
            <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                <CloudServerOutlined class="text-blue-500" />
                资源与环境管理
            </h2>
            <p class="text-gray-500 text-sm">对RAG应用环境进行统一装载、配置、启停与恢复，支持快照管理与场景快速切换</p>
        </div>

        <div class="grid grid-cols-3 gap-6">
            <!-- 左栏：场景模板 + 环境实例 -->
            <div class="col-span-2 space-y-6">
                <!-- 场景模板 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 验证场景模板
                    </div>
                    <div class="grid grid-cols-2 gap-4">
                        <div v-for="tpl in scenarioTemplates" :key="tpl.id"
                             class="border border-gray-200 rounded-lg p-4 hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
                             @click="handleInitEnv(tpl)">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white"
                                     :class="tpl.icon === 'experiment' ? 'bg-blue-500' : tpl.icon === 'thunder' ? 'bg-red-500' : tpl.icon === 'shield' ? 'bg-green-500' : 'bg-purple-500'">
                                    <ExperimentOutlined v-if="tpl.icon === 'experiment'" />
                                    <ThunderboltOutlined v-else-if="tpl.icon === 'thunder'" />
                                    <CheckCircleOutlined v-else-if="tpl.icon === 'shield'" />
                                    <SwapOutlined v-else />
                                </div>
                                <div>
                                    <div class="font-medium text-gray-800 group-hover:text-blue-600">{{ tpl.name }}</div>
                                    <div class="text-xs text-gray-400">{{ tpl.model }}</div>
                                </div>
                            </div>
                            <div class="text-xs text-gray-500">{{ tpl.desc }}</div>
                            <div class="flex items-center justify-between mt-2">
                                <div class="flex items-center gap-2">
                                    <a-tag size="small" color="blue">{{ tpl.kb }}</a-tag>
                                    <a-tag size="small" :color="statusColor(tpl.status)">{{ statusText(tpl.status) }}</a-tag>
                                </div>
                                <a-button type="primary" size="small" class="opacity-0 group-hover:opacity-100 transition-opacity">
                                    <RocketOutlined /> 初始化
                                </a-button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 环境实例列表 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-green-500 rounded-full"></span> 环境实例
                    </div>
                    <a-table :dataSource="envInstances" :pagination="false" rowKey="id" size="small">
                        <a-table-column title="实例ID" dataIndex="id" :width="100">
                            <template #default="{ record }">
                                <span class="font-mono text-xs text-blue-600">{{ record.id }}</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="环境名称" dataIndex="name" :width="180" />
                        <a-table-column title="状态" dataIndex="status" :width="90" align="center">
                            <template #default="{ record }">
                                <a-tag :color="statusColor(record.status)">
                                    <CheckCircleOutlined v-if="record.status==='running'" />
                                    <CloseCircleOutlined v-else />
                                    {{ statusText(record.status) }}
                                </a-tag>
                            </template>
                        </a-table-column>
                        <a-table-column title="知识库" dataIndex="kb" :width="140" />
                        <a-table-column title="生成模型" dataIndex="model" :width="140" />
                        <a-table-column title="检索器" dataIndex="retriever" :width="100" />
                        <a-table-column title="运行时长" dataIndex="uptime" :width="90" align="center" />
                        <a-table-column title="操作" :width="200" align="center">
                            <template #default="{ record }">
                                <div class="flex items-center justify-center gap-1">
                                    <a-button type="link" size="small" @click="handleToggleEnv(record)">
                                        <PlayCircleOutlined v-if="record.status==='stopped'" />
                                        <PauseCircleOutlined v-else />
                                        {{ record.status === 'running' ? '停止' : '启动' }}
                                    </a-button>
                                    <a-button type="link" size="small" @click="handleSnapshot(record)">
                                        <CameraOutlined /> 快照
                                    </a-button>
                                    <a-button type="link" size="small" danger>
                                        <ReloadOutlined /> 重置
                                    </a-button>
                                </div>
                            </template>
                        </a-table-column>
                    </a-table>
                </div>

                <!-- 快照管理 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-purple-500 rounded-full"></span> 环境快照
                    </div>
                    <a-table :dataSource="snapshots" :pagination="false" rowKey="id" size="small">
                        <a-table-column title="快照ID" dataIndex="id" :width="110">
                            <template #default="{ record }">
                                <span class="font-mono text-xs text-purple-600">{{ record.id }}</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="名称" dataIndex="name" :width="200" />
                        <a-table-column title="关联环境" dataIndex="envId" :width="100" />
                        <a-table-column title="大小" dataIndex="size" :width="80" align="center" />
                        <a-table-column title="创建时间" dataIndex="createTime" :width="160" />
                        <a-table-column title="说明" dataIndex="desc" />
                        <a-table-column title="操作" :width="120" align="center">
                            <template #default="{ record }">
                                <a-button type="link" size="small" @click="handleRollback(record)">
                                    <HistoryOutlined /> 回滚
                                </a-button>
                            </template>
                        </a-table-column>
                    </a-table>
                </div>
            </div>

            <!-- 右栏：资源监控 + 服务状态 -->
            <div class="space-y-6">
                <!-- 资源使用概览 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-orange-500 rounded-full"></span> 资源使用概览
                    </div>
                    <div class="space-y-3">
                        <div v-for="(item, idx) in [{label:'CPU', val: resourceStats.cpu, color:'#1677ff'}, {label:'GPU', val: resourceStats.gpu, color:'#722ed1'}, {label:'内存', val: resourceStats.memory, color:'#52c41a'}, {label:'显存', val: resourceStats.gpuMem, color:'#fa8c16'}, {label:'磁盘', val: resourceStats.disk, color:'#13c2c2'}]" :key="idx">
                            <div class="flex items-center justify-between text-xs text-gray-600 mb-1">
                                <span>{{ item.label }}</span>
                                <span class="font-medium">{{ item.val }}%</span>
                            </div>
                            <a-progress :percent="item.val" :strokeColor="item.color" :showInfo="false" size="small" />
                        </div>
                    </div>
                </div>

                <!-- 资源趋势 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2">
                        <span class="w-1 h-4 bg-cyan-500 rounded-full"></span> 资源趋势
                    </div>
                    <div ref="resourceChartRef" style="height: 200px;"></div>
                </div>

                <!-- 服务状态 -->
                <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                        <span class="w-1 h-4 bg-red-500 rounded-full"></span> 服务状态
                    </div>
                    <div class="space-y-2">
                        <div v-for="svc in resourceStats.services" :key="svc.name"
                             class="flex items-center justify-between p-2 rounded-lg bg-gray-50 text-xs">
                            <div class="flex items-center gap-2">
                                <span class="w-2 h-2 rounded-full" :class="svc.status === 'running' ? 'bg-green-500' : 'bg-red-400'"></span>
                                <span class="text-gray-700 font-medium">{{ svc.name }}</span>
                            </div>
                            <div class="flex items-center gap-3 text-gray-400">
                                <span>:{{ svc.port }}</span>
                                <span>{{ svc.latency }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 场景配置弹窗 -->
        <a-modal v-model:open="state.configDialogVisible" title="初始化验证环境" width="560px" @ok="handleConfirmInit">
            <a-form layout="vertical" class="mt-4">
                <a-form-item label="环境名称"><a-input v-model:value="state.scenarioForm.name" /></a-form-item>
                <a-form-item label="知识库"><a-input v-model:value="state.scenarioForm.knowledgeBase" disabled /></a-form-item>
                <a-form-item label="生成模型"><a-input v-model:value="state.scenarioForm.model" disabled /></a-form-item>
                <a-form-item label="提示模板">
                    <a-select v-model:value="state.scenarioForm.promptTemplate" style="width:100%">
                        <a-select-option value="标准调度问答模板">标准调度问答模板</a-select-option>
                        <a-select-option value="安全增强模板">安全增强模板</a-select-option>
                        <a-select-option value="工业问答模板">工业问答模板</a-select-option>
                    </a-select>
                </a-form-item>
                <a-form-item label="说明"><a-textarea v-model:value="state.scenarioForm.description" :rows="2" /></a-form-item>
            </a-form>
        </a-modal>

        <!-- 快照弹窗 -->
        <a-modal v-model:open="state.snapshotDialogVisible" title="创建环境快照" @ok="handleConfirmSnapshot">
            <a-form layout="vertical" class="mt-4">
                <a-form-item label="快照名称"><a-input v-model:value="state.snapshotName" /></a-form-item>
            </a-form>
        </a-modal>
    </div>
</template>
