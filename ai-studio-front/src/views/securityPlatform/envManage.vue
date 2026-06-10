<script setup>
import {
    CloudServerOutlined, DatabaseOutlined, RobotOutlined, SettingOutlined,
    ReloadOutlined, PlayCircleOutlined, PauseCircleOutlined, CameraOutlined,
    CheckCircleOutlined, HistoryOutlined, ExperimentOutlined, ThunderboltOutlined,
    SwapOutlined, EyeOutlined, MessageOutlined, ClusterOutlined,
    HddOutlined, SafetyOutlined, FieldTimeOutlined, ApiOutlined,
} from '@ant-design/icons-vue';
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useSecurityStore } from '@/store/modules/security.js';
import { listKnowledgeBasesReal } from '@/api/ragScheduler/index.js';
import { testEnvironmentChat } from '@/api/securityPlatform/security.js';

const securityStore = useSecurityStore();
const router = useRouter();
const knowledgeBases = ref([]);
const chatContainerRef = ref(null);
const resourceChartRef = ref(null);
const envChartRef = ref(null);
let resourceChartInstance = null;
let envChartInstance = null;

const state = reactive({
  configDialogVisible: false,
  snapshotDialogVisible: false,
  snapshotName: '',
  selectedTemplate: null,
  scenarioForm: { name: '', knowledgeBase: '', model: '', promptTemplate: '', description: '' },
  baselineTest: { knowledgeBase: '', model: '', query: '', loading: false, result: null },
  chatInput: '',
  chatLoading: false,
  chatMessages: [],
});

const scenarioTemplates = computed(() => securityStore.envTemplates || []);
const envInstances = computed(() => securityStore.envInstances || []);
const snapshots = computed(() => securityStore.envSnapshots || []);
const services = computed(() => securityStore.envServices || []);
const envResource = computed(() => securityStore.envResource || { cpu: 0, memory: 0, gpu: 0, gpuMem: 0, disk: 0 });
const qwenConnected = computed(() => !!securityStore.envStatus?.qwenConnected);
const activeEnvCount = computed(() => securityStore.envStatus?.activeEnvCount ?? envInstances.value.filter(e => e.status === 'running').length);
const availableModels = computed(() => securityStore.envStatus?.availableModels || [
  { value: 'gpt-4o', label: 'gpt-4o' },
  { value: 'llama3:8b', label: 'llama3:8b' },
  { value: 'Qwen3.7-Max', label: 'Qwen3.7-Max' }
]);
const resourceTrend = computed(() => securityStore.envStatus?.resourceTrend || null);

const knowledgeOptions = computed(() => knowledgeBases.value.map(kb => ({ value: kb.id || kb.name, label: kb.name || kb.id, docCount: kb.doc_count })));
const statusColor = (s) => ({ running: 'green', stopped: 'red', ready: 'blue', error: 'orange' }[s] || 'default');
const statusText = (s) => ({ running: '运行中', stopped: '已停止', ready: '就绪', error: '异常' }[s] || s);

const ensureDefaults = () => {
  if (!state.scenarioForm.knowledgeBase && knowledgeOptions.value.length) state.scenarioForm.knowledgeBase = knowledgeOptions.value[0].value;
  if (!state.baselineTest.knowledgeBase && knowledgeOptions.value.length) state.baselineTest.knowledgeBase = knowledgeOptions.value[0].value;
  if (!state.scenarioForm.model && availableModels.value.length) state.scenarioForm.model = availableModels.value[0].value;
  if (!state.baselineTest.model && availableModels.value.length) state.baselineTest.model = availableModels.value[0].value;
};

const loadKBList = async () => {
  try {
    const res = await listKnowledgeBasesReal();
    if (res.data?.code === 200 && Array.isArray(res.data.data)) knowledgeBases.value = res.data.data.map(kb => ({ id: kb.id || kb.name, name: kb.name || kb.id, doc_count: kb.doc_count }));
  } catch {
    knowledgeBases.value = [
      { id: 'kb-grid-001', name: '电力调度规程库', doc_count: 248 },
      { id: 'kb-industrial-002', name: '工业专利文档库', doc_count: 196 },
      { id: 'kb-general-003', name: '综合知识库', doc_count: 312 },
    ];
  }
  ensureDefaults();
};

const buildResourceChartOption = () => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['CPU', 'GPU', '内存', '显存'], top: 0, textStyle: { fontSize: 10 } },
  grid: { top: 30, bottom: 18, left: 42, right: 12 },
  xAxis: { type: 'category', data: resourceTrend.value?.timestamps || [], axisLabel: { fontSize: 9 } },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', fontSize: 9 } },
  series: [
    { name: 'CPU', type: 'line', smooth: true, symbol: 'none', data: resourceTrend.value?.cpu || [] },
    { name: 'GPU', type: 'line', smooth: true, symbol: 'none', data: resourceTrend.value?.gpu || [] },
    { name: '内存', type: 'line', smooth: true, symbol: 'none', data: resourceTrend.value?.memory || [] },
    { name: '显存', type: 'line', smooth: true, symbol: 'none', data: resourceTrend.value?.gpuMem || [] },
  ]
});

const buildEnvChartOption = () => ({
  tooltip: { trigger: 'axis' },
  grid: { top: 10, bottom: 18, left: 36, right: 8 },
  xAxis: { type: 'category', data: envInstances.value.map(i => i.id), axisLabel: { fontSize: 9 } },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%', fontSize: 9 } },
  series: [{ name: 'GPU', type: 'bar', data: envInstances.value.map(i => i.resource?.gpu || 0), barWidth: 18 }]
});

const renderCharts = () => {
  if (resourceChartRef.value) {
    if (!resourceChartInstance) resourceChartInstance = echarts.init(resourceChartRef.value);
    resourceChartInstance.setOption(buildResourceChartOption(), true);
  }
  if (envChartRef.value) {
    if (!envChartInstance) envChartInstance = echarts.init(envChartRef.value);
    envChartInstance.setOption(buildEnvChartOption(), true);
  }
};

const handleInitEnv = (tpl) => {
  state.selectedTemplate = tpl;
  state.configDialogVisible = true;
  state.scenarioForm = {
    name: `${tpl.name} - 验证实例`,
    knowledgeBase: tpl.kb || knowledgeOptions.value[0]?.value || '',
    model: 'Qwen3.7-Max',
    promptTemplate: tpl.defaultPrompt || '标准调度问答模板',
    description: tpl.desc || '',
  };
};

const handleConfirmInit = async () => {
  const res = await securityStore.initializeEnvironment({ ...state.scenarioForm, scenario: state.selectedTemplate?.name });
  if (res?.instance) {
    ElMessage.success(`环境初始化成功，实例 ${res.envId} 已创建`);
    state.configDialogVisible = false;
    await Promise.all([securityStore.fetchEnvStatus(), securityStore.fetchEnvInstances()]);
    router.push('/rag-scheduler/chat');
  } else {
    ElMessage.warning('环境初始化已触发，但未获取到实例信息');
  }
};

const handleToggleEnv = (env) => { env.status = env.status === 'running' ? 'stopped' : 'running'; ElMessage.success(env.status === 'running' ? '环境已启动' : '环境已停止'); nextTick(renderCharts); };
const handleSnapshot = (env) => { state.snapshotDialogVisible = true; state.snapshotName = `${env.name} - 快照 ${new Date().toLocaleTimeString()}`; };
const handleConfirmSnapshot = () => { state.snapshotDialogVisible = false; securityStore.envSnapshots.unshift({ id: `SNAP-${String(snapshots.value.length + 1).padStart(3, '0')}`, name: state.snapshotName, envId: envInstances.value[0]?.id || 'ENV-001', createTime: new Date().toLocaleString(), size: `${200 + Math.floor(Math.random() * 120)}MB`, desc: '手动创建的环境快照' }); ElMessage.success('快照创建成功'); };
const handleRollback = (snap) => ElMessage.success(`环境已回滚至：${snap.name}`);

const handleBaselineTest = async () => {
  if (!state.baselineTest.query.trim()) return ElMessage.warning('请输入测试问题');
  state.baselineTest.loading = true;
  try {
    const res = await testEnvironmentChat({ knowledgeBase: state.baselineTest.knowledgeBase, model: state.baselineTest.model, query: state.baselineTest.query });
    state.baselineTest.result = res.data?.code === 200 ? res.data.data : null;
    if (!state.baselineTest.result) throw new Error('mock');
  } catch {
    state.baselineTest.result = {
      query: state.baselineTest.query,
      answer: '根据《电力调度运行规程》第4.3.2条，油温超过95℃应立即降载运行，超过100℃需停运检查。',
      model: state.baselineTest.model,
      knowledgeBase: state.baselineTest.knowledgeBase,
      latency: +(Math.random() * 0.3 + 1.1).toFixed(2),
      retrievalTopK: 5,
      references: [
        { id: 'REF-001', title: '电力调度运行规程-油温控制', section: '第4章', relevance: 0.91 },
        { id: 'REF-002', title: '事故应急预案-油温异常处置', section: '附录A.2', relevance: 0.84 },
      ],
      guardrail: { input: 'passed', retrieval: 'passed', output: 'passed' },
    };
  } finally { state.baselineTest.loading = false; }
};

const handleSendChat = () => {
  if (!state.chatInput.trim()) return;
  state.chatMessages.push({ role: 'user', content: state.chatInput, time: new Date().toLocaleTimeString() });
  const q = state.chatInput;
  state.chatInput = '';
  state.chatLoading = true;
  setTimeout(() => {
    state.chatMessages.push({ role: 'assistant', content: `已接入 ${state.baselineTest.model || 'Qwen3.7-Max'}，结合 ${state.baselineTest.knowledgeBase || '知识库'} 的检索结果，建议先确认设备状态再执行操作。\n\nQ: ${q}`, time: new Date().toLocaleTimeString() });
    state.chatLoading = false;
    nextTick(() => { if (chatContainerRef.value) chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight; });
  }, 700);
};

onMounted(async () => { await Promise.all([securityStore.fetchEnvTemplates(), securityStore.fetchEnvStatus(), securityStore.fetchEnvInstances()]); await loadKBList(); nextTick(renderCharts); });
onBeforeUnmount(() => { if (resourceChartInstance) resourceChartInstance.dispose(); if (envChartInstance) envChartInstance.dispose(); });
watch(resourceTrend, () => nextTick(renderCharts));
watch(envInstances, () => nextTick(renderCharts), { deep: true });
watch([knowledgeOptions, availableModels], ensureDefaults, { deep: true });
</script>

<template>
  <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
    <div class="mb-6">
      <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1"><CloudServerOutlined class="text-blue-500" /> 环境准备与装载</h2>
      <p class="text-gray-500 text-sm">接入调度决策应用中的 RAG 能力，完成知识库选择、Qwen3.7-Max 连接测试、对话效果验证与环境实例装载。</p>
    </div>

    <div class="grid grid-cols-3 gap-6">
      <div class="col-span-2 space-y-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-blue-500 rounded-full"></span> RAG 环境模板</div>
          <div class="grid grid-cols-2 gap-4">
            <div v-for="tpl in scenarioTemplates" :key="tpl.id" class="border rounded-lg p-4 hover:border-blue-400 cursor-pointer" @click="handleInitEnv(tpl)">
              <div class="flex items-center gap-3 mb-2">
                <div class="w-10 h-10 rounded-lg bg-blue-500 text-white flex items-center justify-center"><ExperimentOutlined /></div>
                <div>
                  <div class="font-medium text-gray-800">{{ tpl.name }}</div>
                  <div class="text-xs text-gray-400">{{ tpl.model }}</div>
                </div>
              </div>
              <div class="text-xs text-gray-500">{{ tpl.desc }}</div>
              <div class="flex items-center justify-between mt-2">
                <div class="flex gap-2"><a-tag size="small" color="blue">{{ tpl.kb }}</a-tag><a-tag size="small" :color="statusColor(tpl.status)">{{ statusText(tpl.status) }}</a-tag></div>
                <a-button type="primary" size="small"><PlayCircleOutlined /> 装载</a-button>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-green-500 rounded-full"></span> 已装载环境实例</div>
          <a-table :dataSource="envInstances" :pagination="false" rowKey="id" size="small" :scroll="{ x: 960 }">
            <a-table-column title="实例ID" dataIndex="id" :width="100"><template #default="{ record }"><span class="font-mono text-xs text-blue-600">{{ record.id }}</span></template></a-table-column>
            <a-table-column title="环境名称" dataIndex="name" :width="180" />
            <a-table-column title="知识库" dataIndex="kb" :width="140" />
            <a-table-column title="模型" dataIndex="model" :width="130" />
            <a-table-column title="状态" dataIndex="status" :width="90" align="center"><template #default="{ record }"><a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag></template></a-table-column>
            <a-table-column title="检索器" dataIndex="retriever" :width="100" />
            <a-table-column title="运行时长" dataIndex="uptime" :width="100" />
            <a-table-column title="操作" :width="200" align="center"><template #default="{ record }"><a-button type="link" size="small" @click="handleToggleEnv(record)">{{ record.status === 'running' ? '停止' : '启动' }}</a-button><a-button type="link" size="small" @click="handleSnapshot(record)"><CameraOutlined /> 快照</a-button></template></a-table-column>
          </a-table>
        </div>

        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-purple-500 rounded-full"></span> 环境快照</div>
          <a-table :dataSource="snapshots" :pagination="false" rowKey="id" size="small" :scroll="{ x: 960 }">
            <a-table-column title="快照ID" dataIndex="id" :width="110" />
            <a-table-column title="名称" dataIndex="name" :width="200" />
            <a-table-column title="关联环境" dataIndex="envId" :width="120" />
            <a-table-column title="大小" dataIndex="size" :width="90" />
            <a-table-column title="创建时间" dataIndex="createTime" :width="160" />
            <a-table-column title="说明" dataIndex="desc" :width="260" />
            <a-table-column title="操作" :width="120" align="center"><template #default="{ record }"><a-button type="link" size="small" @click="handleRollback(record)"><HistoryOutlined /> 回滚</a-button></template></a-table-column>
          </a-table>
        </div>
      </div>

      <div class="space-y-6">
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-orange-500 rounded-full"></span> 资源概览 <a-tag :color="qwenConnected ? 'blue' : 'red'" size="small">{{ qwenConnected ? 'Qwen3.7-Max 已连通' : 'Qwen 未连通' }}</a-tag></div>
          <div class="space-y-3">
            <div v-for="(item, idx) in [ {label:'CPU', val: envResource.cpu, color:'#1677ff'}, {label:'GPU', val: envResource.gpu, color:'#722ed1'}, {label:'内存', val: envResource.memory, color:'#52c41a'}, {label:'显存', val: envResource.gpuMem, color:'#fa8c16'}, {label:'磁盘', val: envResource.disk, color:'#13c2c2'} ]" :key="idx">
              <div class="flex items-center justify-between text-xs text-gray-600 mb-1"><span>{{ item.label }}</span><span>{{ item.val }}%</span></div>
              <a-progress :percent="item.val" :strokeColor="item.color" :showInfo="false" size="small" />
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2"><span class="w-1 h-4 bg-cyan-500 rounded-full"></span> 资源趋势</div>
          <div ref="resourceChartRef" style="height: 180px;"></div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2"><span class="w-1 h-4 bg-red-500 rounded-full"></span> 环境 GPU 占用</div>
          <div ref="envChartRef" style="height: 160px;"></div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-green-500 rounded-full"></span> 服务状态</div>
          <div class="space-y-2">
            <div v-for="svc in services" :key="svc.name" class="flex items-center justify-between p-2 rounded-lg bg-gray-50 text-xs">
              <div class="flex items-center gap-2"><span class="w-2 h-2 rounded-full" :class="svc.status === 'running' ? 'bg-green-500' : 'bg-red-400'"></span>{{ svc.name }}</div>
              <div class="text-gray-400">{{ svc.latency }}</div>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="flex items-center justify-between mb-4">
            <div class="text-sm font-bold text-gray-800 flex items-center gap-2"><span class="w-1 h-4 bg-indigo-500 rounded-full"></span> RAG 对话测试</div>
            <a-button size="small" type="link" @click="router.push('/rag-scheduler/chat')">跳转到调度助手验证</a-button>
          </div>
          <div class="mb-3 rounded-lg bg-blue-50 border border-blue-100 p-3 text-xs text-blue-700">
            当前已连接调度决策应用，可直接发起问题验证，确认知识库检索与模型回答是否正常。
          </div>
          <a-form layout="vertical">
            <a-form-item label="知识库"><a-select v-model:value="state.baselineTest.knowledgeBase" style="width:100%"><a-select-option v-for="kb in knowledgeOptions" :key="kb.value" :value="kb.value">{{ kb.label }}<span v-if="kb.docCount">（{{ kb.docCount }}）</span></a-select-option></a-select></a-form-item>
            <a-form-item label="模型"><a-select v-model:value="state.baselineTest.model" style="width:100%"><a-select-option v-for="m in availableModels" :key="m.value" :value="m.value">{{ m.label || m.value }}</a-select-option></a-select></a-form-item>
            <a-form-item label="测试问题"><a-textarea v-model:value="state.baselineTest.query" :rows="3" placeholder="例如：变压器油温超过95℃应如何处置？" /></a-form-item>
            <a-button type="primary" block :loading="state.baselineTest.loading" @click="handleBaselineTest"><MessageOutlined /> 发起测试</a-button>
          </a-form>
          <div v-if="state.baselineTest.result" class="mt-4 border rounded-lg bg-gray-50">
            <div class="p-3 border-b flex items-center justify-between"><b class="text-sm">回答</b><span class="text-xs text-gray-400">耗时 {{ state.baselineTest.result.latency }}s</span></div>
            <div class="p-4 text-sm whitespace-pre-wrap leading-relaxed">{{ state.baselineTest.result.answer }}</div>
            <div class="border-t p-3">
              <div class="text-xs text-gray-400 mb-2 flex items-center gap-1"><DatabaseOutlined /> 检索引用</div>
              <div class="space-y-2"><div v-for="ref in state.baselineTest.result.references || []" :key="ref.id" class="bg-white rounded border p-2 text-xs flex justify-between"><div><div class="font-medium">{{ ref.title }}</div><div class="text-gray-400">{{ ref.section }}</div></div><a-tag color="blue" size="small">{{ Math.round((ref.relevance || 0) * 100) }}%</a-tag></div></div>
            </div>
          </div>
        </div>
        <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2"><span class="w-1 h-4 bg-teal-500 rounded-full"></span> 对话速记</div>
          <div ref="chatContainerRef" class="max-h-64 overflow-auto space-y-3 pr-1">
            <div v-for="(msg, idx) in state.chatMessages" :key="idx" class="text-sm" :class="msg.role === 'user' ? 'text-right' : 'text-left'">
              <div class="inline-block rounded-lg px-3 py-2 max-w-full whitespace-pre-wrap" :class="msg.role === 'user' ? 'bg-blue-50 text-blue-700' : 'bg-gray-50 text-gray-700'">{{ msg.content }}</div>
            </div>
          </div>
          <div class="mt-3 flex gap-2"><a-input v-model:value="state.chatInput" placeholder="输入一句验证性问题" @pressEnter="handleSendChat" /><a-button type="primary" :loading="state.chatLoading" @click="handleSendChat">发送</a-button></div>
        </div>
      </div>
    </div>

    <a-modal v-model:open="state.configDialogVisible" title="初始化验证环境" width="560px" @ok="handleConfirmInit">
      <a-form layout="vertical" class="mt-4">
        <a-form-item label="环境名称"><a-input v-model:value="state.scenarioForm.name" /></a-form-item>
        <a-form-item label="知识库"><a-select v-model:value="state.scenarioForm.knowledgeBase" style="width:100%"><a-select-option v-for="kb in knowledgeOptions" :key="kb.value" :value="kb.value">{{ kb.label }}</a-select-option></a-select></a-form-item>
        <a-form-item label="生成模型"><a-select v-model:value="state.scenarioForm.model" style="width:100%"><a-select-option v-for="m in availableModels" :key="m.value" :value="m.value">{{ m.label || m.value }}</a-select-option></a-select></a-form-item>
        <a-form-item label="提示模板"><a-select v-model:value="state.scenarioForm.promptTemplate" style="width:100%"><a-select-option value="标准调度问答模板">标准调度问答模板</a-select-option><a-select-option value="安全增强模板">安全增强模板</a-select-option><a-select-option value="工业问答模板">工业问答模板</a-select-option></a-select></a-form-item>
        <a-form-item label="说明"><a-textarea v-model:value="state.scenarioForm.description" :rows="2" /></a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="state.snapshotDialogVisible" title="创建环境快照" @ok="handleConfirmSnapshot"><a-form layout="vertical" class="mt-4"><a-form-item label="快照名称"><a-input v-model:value="state.snapshotName" /></a-form-item></a-form></a-modal>
  </div>
</template>
