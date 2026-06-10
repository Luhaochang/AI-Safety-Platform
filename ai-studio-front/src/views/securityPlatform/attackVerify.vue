<script setup>
import {
  BugOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  EyeOutlined,
  PlusOutlined,
  ReloadOutlined,
  SyncOutlined,
  WarningOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons-vue';
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useSecurityStore } from '@/store/modules/security.js';

const securityStore = useSecurityStore();

const state = reactive({
  activeTab: 'tasks',
  poisonPreviewVisible: false,
  previewTask: null,
  executingAnimation: false,
  taskForm: {
    name: '',
    targetTopic: '',
    targetWrongAnswer: '',
    expectedAnswer: '',
    attackMethod: 'BOGA-RAGP',
    targetKb: '电力调度规程库',
    injectCount: 5,
    attackStrength: 'medium',
    targetQueries: '',
  },
});

const attackTasks = computed(() => securityStore.attackTasks);
const attackSteps = computed(() => securityStore.attackSteps);
const completedAttacks = computed(() => securityStore.completedAttacks);
const attackMethods = computed(() => securityStore.attackMethods);
const attackMethodOptions = computed(() => (attackMethods.value || []).map((item) => ({
  value: item.value,
  label: item.label,
  type: item.type,
})));

const attackStrengthOptions = [
  { value: 'low', label: '低' },
  { value: 'medium', label: '中等' },
  { value: 'high', label: '高' },
];

const attackStepTemplates = {
  BOGA_RAGP: ['目标查询扩展', '语义中心计算', '遗传演化', '代表解选取', '知识库注入', '攻击效果验证'],
  PoisonedRAG: ['目标查询分析', '检索频率评估', '对抗文档生成', '知识库注入', '攻击效果验证'],
  CorpusPoisoning: ['语料采样', '语义扰动', '知识库注入', '攻击效果验证'],
  DIGA: ['梯度信息获取', '语义扰动优化', '迭代精炼', '知识库注入', '攻击效果验证'],
  Phantom: ['触发查询设计', '幻觉文档生成', '检索劫持优化', '知识库注入', '攻击效果验证'],
};

const stepDetailMap = {
  '目标查询扩展': '已扩展生成 75 条语义相关查询',
  '语义中心计算': '完成主题向量聚合 (dim=768)',
  '遗传演化': '30 代 NSGA-II 演化完成',
  '代表解选取': '从 Pareto 前沿选取 5 个代表解',
  '知识库注入': '毒化文档写入向量索引',
  '攻击效果验证': '计算 ASR / RSR 指标',
  '目标查询分析': '解析 30 条目标查询语义向量',
  '检索频率评估': '评估文档在 Top-K 召回概率',
  '对抗文档生成': '生成候选毒化文档',
  '语料采样': '采样基础语料 180 条',
  '语义扰动': '构造高相似度伪证据',
  '梯度信息获取': '提取模型梯度，定位关键 token',
  '语义扰动优化': '完成梯度引导文本扰动',
  '迭代精炼': '3 轮迭代优化完成',
  '触发查询设计': '设计 10 条幻觉触发查询',
  '幻觉文档生成': '生成幻觉触发文档',
  '检索劫持优化': '优化检索排序权重',
};

const buildSteps = (method) => {
  const key = method === 'BOGA-RAGP' ? 'BOGA_RAGP' : method;
  const template = attackStepTemplates[key] || attackStepTemplates.BOGA_RAGP;
  return template.map((label, idx) => ({
    label,
    detail: idx === 0 ? '准备执行' : '等待执行',
    status: idx === 0 ? 'running' : 'wait',
  }));
};

const loadInitialData = async () => {
  await Promise.all([
    securityStore.fetchAttackMethods(),
    securityStore.fetchAttackTasks(),
    securityStore.fetchDefenseResults(),
  ]);
};

onMounted(loadInitialData);

const resetTaskForm = () => {
  state.taskForm = {
    name: '',
    targetTopic: '',
    targetWrongAnswer: '',
    expectedAnswer: '',
    attackMethod: attackMethodOptions.value[0]?.value || 'BOGA-RAGP',
    targetKb: '电力调度规程库',
    injectCount: 5,
    attackStrength: 'medium',
    targetQueries: '',
  };
};

const handlePreviewPoison = (task) => {
  state.previewTask = task;
  state.poisonPreviewVisible = true;
};

let stepTimer = null;
const clearStepTimer = () => {
  if (stepTimer) {
    clearInterval(stepTimer);
    stepTimer = null;
  }
};

const handleRunTask = (task) => {
  if (!task) return;
  clearStepTimer();
  const steps = buildSteps(task.method);
  securityStore.setAttackSteps(steps);
  securityStore.startAttack(task.id, steps);
  state.activeTab = 'process';
  state.executingAnimation = true;
  let currentIdx = 0;

  stepTimer = setInterval(async () => {
    if (currentIdx < securityStore.attackSteps.length) {
      const currentStep = {
        ...securityStore.attackSteps[currentIdx],
        status: 'finish',
        detail: stepDetailMap[securityStore.attackSteps[currentIdx].label] || '已完成',
      };
      securityStore.attackSteps.splice(currentIdx, 1, currentStep);
      if (currentIdx + 1 < securityStore.attackSteps.length) {
        securityStore.attackSteps.splice(currentIdx + 1, 1, {
          ...securityStore.attackSteps[currentIdx + 1],
          status: 'running',
          detail: '执行中...',
        });
      }
      currentIdx += 1;
      return;
    }

    clearStepTimer();
    state.executingAnimation = false;
    const result = await securityStore.executeAttack({
      taskId: task.id,
      attackMethod: task.method,
      targetTopic: task.targetTopic,
      expectedAnswer: task.expectedAnswer,
    });
    if (result) ElMessage.success(`攻击完成，ASR=${result.asr}% · RSR=${result.rsr}%`);
    await securityStore.fetchDefenseResults();
  }, 1100);
};

const handleSubmitTask = async () => {
  if (!state.taskForm.name || !state.taskForm.targetTopic) {
    ElMessage.warning('请填写任务名称和攻击目标主题');
    return;
  }
  const queries = state.taskForm.targetQueries
    ? state.taskForm.targetQueries.split('\n').map((q) => q.trim()).filter(Boolean)
    : [];
  const task = await securityStore.createAttack({ ...state.taskForm, targetQueries: queries });
  if (task) {
    ElMessage.success(`攻击任务 ${task.id} 已创建并开始执行`);
    handleRunTask(task);
  }
};

const strengthColor = (s) => ({ 高: 'red', 中等: 'orange', 低: 'green' }[s] || 'default');
const currentRunningStepIdx = computed(() => securityStore.attackSteps.findIndex((s) => s.status === 'running'));
const processTitle = computed(() => (securityStore.runningAttack ? `${securityStore.runningAttack.method} 攻击执行流程 — ${securityStore.runningAttack.name}` : '攻击执行流程'));

const attackChartRef = ref(null);
let attackChartInstance = null;
const attackEffectRows = computed(() => completedAttacks.value
  .map((task) => ({
    method: `${task.method} (${task.name})`,
    type: task.method.includes('PoisonedRAG') || task.method.includes('DIGA') || task.method.includes('Corpus') ? '白盒' : '黑盒',
    asr: task.asr ?? 0,
    rsr: task.rsr ?? 0,
    ppl: task.poisonDocs?.length
      ? +(task.poisonDocs.reduce((s, d) => s + (parseFloat(d.ppl) || 0), 0) / task.poisonDocs.length).toFixed(1)
      : null,
  }))
  .concat([
    { method: 'PoisonedRAG (黑盒)', type: '黑盒', asr: 76.0, rsr: 80.0, ppl: 35.1 },
    { method: 'PoisonedRAG (白盒)', type: '白盒', asr: 84.7, rsr: 88.0, ppl: 22.5 },
    { method: 'CorpusPoisoning', type: '白盒', asr: 80.0, rsr: 85.3, ppl: 19.8 },
    { method: 'RAG Paradox', type: '黑盒', asr: 68.0, rsr: 72.0, ppl: 32.6 },
    { method: 'DIGA', type: '黑盒', asr: 42.7, rsr: 58.7, ppl: 45.2 },
  ]));

const renderAttackChart = () => {
  if (!attackChartRef.value) return;
  if (!attackChartInstance) attackChartInstance = echarts.init(attackChartRef.value);
  attackChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['ASR(%)', 'RSR(%)'], top: 0 },
    grid: { top: 40, bottom: 40, left: 55, right: 20 },
    xAxis: { type: 'category', data: attackEffectRows.value.map((item) => item.method), axisLabel: { fontSize: 9, interval: 0 } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [
      { name: 'ASR(%)', type: 'bar', data: attackEffectRows.value.map((item) => item.asr), itemStyle: { color: '#f5222d' } },
      { name: 'RSR(%)', type: 'bar', data: attackEffectRows.value.map((item) => item.rsr), itemStyle: { color: '#1677ff' } },
    ],
  }, true);
};

watch(attackEffectRows, () => {
  if (state.activeTab === 'compare') nextTick(renderAttackChart);
}, { deep: true });
watch(() => state.activeTab, (tab) => { if (tab === 'compare') nextTick(renderAttackChart); });
onBeforeUnmount(() => { clearStepTimer(); if (attackChartInstance) attackChartInstance.dispose(); });
</script>

<template>
  <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
          <BugOutlined class="text-red-500" /> 攻击配置与执行
        </h2>
        <p class="text-gray-500 text-sm">在任务配置页面完成参数填写，点击底部按钮即可创建并立即开始攻击，随后进入过程监控与结果查看。</p>
      </div>
      <a-button type="primary" @click="resetTaskForm">
        <PlusOutlined /> 重置配置
      </a-button>
    </div>

    <a-tabs v-model:activeKey="state.activeTab">
      <a-tab-pane key="tasks" tab="任务配置">
        <div class="grid grid-cols-3 gap-6">
          <div class="col-span-1 bg-white rounded-xl shadow-sm border p-5">
            <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
              <span class="w-1 h-4 bg-red-500 rounded-full"></span> 攻击参数配置
            </div>
            <a-form layout="vertical">
              <a-form-item label="任务名称"><a-input v-model:value="state.taskForm.name" placeholder="例如：变压器油温投毒测试" /></a-form-item>
              <a-form-item label="攻击目标主题"><a-input v-model:value="state.taskForm.targetTopic" placeholder="例如：变压器油温超标处置" /></a-form-item>
              <a-form-item label="目标错误回答"><a-textarea v-model:value="state.taskForm.targetWrongAnswer" :rows="3" placeholder="攻击希望模型输出的错误结论" /></a-form-item>
              <a-form-item label="期望正确回答"><a-textarea v-model:value="state.taskForm.expectedAnswer" :rows="3" placeholder="正确回答用于对照评估" /></a-form-item>
              <div class="grid grid-cols-2 gap-3">
                <a-form-item label="攻击方法">
                  <a-select v-model:value="state.taskForm.attackMethod" style="width:100%">
                    <a-select-option v-for="method in attackMethodOptions" :key="method.value" :value="method.value">
                      {{ method.label }}（{{ method.type }}）
                    </a-select-option>
                  </a-select>
                </a-form-item>
                <a-form-item label="目标知识库">
                  <a-select v-model:value="state.taskForm.targetKb" style="width:100%">
                    <a-select-option value="电力调度规程库">电力调度规程库</a-select-option>
                    <a-select-option value="工业专利文档库">工业专利文档库</a-select-option>
                    <a-select-option value="综合知识库">综合知识库</a-select-option>
                  </a-select>
                </a-form-item>
              </div>
              <div class="grid grid-cols-2 gap-3">
                <a-form-item label="注入文档数"><a-input-number v-model:value="state.taskForm.injectCount" :min="1" :max="20" style="width:100%" /></a-form-item>
                <a-form-item label="攻击强度">
                  <a-select v-model:value="state.taskForm.attackStrength" style="width:100%">
                    <a-select-option v-for="s in attackStrengthOptions" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
                  </a-select>
                </a-form-item>
              </div>
              <a-form-item label="目标查询（每行一条）">
                <a-textarea v-model:value="state.taskForm.targetQueries" :rows="4" placeholder="油温达到120度是否需要停止运行？\n变压器油温超标如何处理？" />
              </a-form-item>
              <a-button type="primary" block @click="handleSubmitTask">
                <PlayCircleOutlined /> 新建并执行攻击任务
              </a-button>
            </a-form>
          </div>

          <div class="col-span-2 space-y-4">
            <div class="bg-white rounded-xl shadow-sm border p-5">
              <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
                <span class="w-1 h-4 bg-blue-500 rounded-full"></span> 历史配置信息
              </div>
              <div class="space-y-4">
                <div v-for="task in attackTasks" :key="task.id" class="border rounded-xl p-4 hover:border-blue-300 transition-colors">
                  <div class="flex items-start justify-between gap-4">
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2 mb-1">
                        <span class="font-medium text-gray-800 truncate">{{ task.name }}</span>
                        <a-tag size="small" color="purple">{{ task.method }}</a-tag>
                        <a-tag size="small" :color="strengthColor(task.strength)">强度: {{ task.strength }}</a-tag>
                        <a-tag size="small" :color="task.status === 'completed' ? 'green' : task.status === 'running' ? 'blue' : 'default'">{{ task.status === 'completed' ? '已执行' : task.status === 'running' ? '执行中' : '待执行' }}</a-tag>
                      </div>
                      <div class="text-xs text-gray-400 flex flex-wrap items-center gap-x-3 gap-y-1">
                        <span class="font-mono">{{ task.id }}</span>
                        <span>{{ task.createTime }}</span>
                        <span>知识库：{{ task.targetKb }}</span>
                        <span>注入：{{ task.injectCount }} 篇</span>
                      </div>
                    </div>
                    <div class="flex items-center gap-2 flex-shrink-0">
                      <a-button v-if="task.status === 'completed'" size="small" @click="handlePreviewPoison(task)"><EyeOutlined /> 查看毒化</a-button>
                      <a-button v-if="task.status === 'completed'" size="small" type="primary" ghost @click="handleRunTask(task)"><ReloadOutlined /> 重新执行</a-button>
                    </div>
                  </div>
                  <div class="grid grid-cols-2 gap-3 mt-3 text-sm">
                    <div class="bg-gray-50 rounded-lg p-3">
                      <div class="text-xs text-gray-400 mb-1">目标主题</div>
                      <div class="text-gray-700">{{ task.targetTopic }}</div>
                    </div>
                    <div class="bg-gray-50 rounded-lg p-3">
                      <div class="text-xs text-gray-400 mb-1">目标查询</div>
                      <div class="text-gray-700 line-clamp-2">{{ task.targetQueries?.length ? task.targetQueries.join('；') : '—' }}</div>
                    </div>
                    <div class="bg-gray-50 rounded-lg p-3">
                      <div class="text-xs text-gray-400 mb-1">目标错误回答</div>
                      <div class="text-gray-700 line-clamp-2">{{ task.targetWrongAnswer || '—' }}</div>
                    </div>
                    <div class="bg-gray-50 rounded-lg p-3">
                      <div class="text-xs text-gray-400 mb-1">期望正确回答</div>
                      <div class="text-gray-700 line-clamp-2">{{ task.expectedAnswer || '—' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </a-tab-pane>

      <a-tab-pane key="process" tab="执行监控">
        <div class="bg-white rounded-xl shadow-sm border p-6">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
            <span class="w-1 h-4 bg-red-500 rounded-full"></span> {{ processTitle }}
            <a-tag v-if="state.executingAnimation" color="processing" class="ml-2"><SyncOutlined spin /> 执行中</a-tag>
            <a-tag v-else color="success" class="ml-2"><CheckCircleOutlined /> 已完成</a-tag>
          </div>
          <a-steps :current="currentRunningStepIdx >= 0 ? currentRunningStepIdx : attackSteps.length" direction="vertical" size="small">
            <a-step v-for="(step, idx) in attackSteps" :key="idx" :title="step.label" :description="step.detail" :status="step.status === 'finish' ? 'finish' : step.status === 'running' ? 'process' : 'wait'" />
          </a-steps>
          <div v-if="!state.executingAnimation && securityStore.latestCompletedAttack" class="mt-6 border-t pt-4">
            <div class="text-sm font-bold text-gray-800 mb-3">攻击结果摘要</div>
            <div class="grid grid-cols-4 gap-4">
              <div class="bg-red-50 rounded-lg p-3 text-center">
                <div class="text-xs text-gray-400">攻击方法</div>
                <div class="text-sm font-bold text-red-600">{{ securityStore.latestCompletedAttack.method }}</div>
              </div>
              <div class="bg-red-50 rounded-lg p-3 text-center">
                <div class="text-xs text-gray-400">ASR</div>
                <div class="text-xl font-bold text-red-600">{{ securityStore.latestCompletedAttack.asr }}%</div>
              </div>
              <div class="bg-blue-50 rounded-lg p-3 text-center">
                <div class="text-xs text-gray-400">RSR</div>
                <div class="text-xl font-bold text-blue-600">{{ securityStore.latestCompletedAttack.rsr }}%</div>
              </div>
              <div class="bg-gray-50 rounded-lg p-3 text-center">
                <div class="text-xs text-gray-400">耗时</div>
                <div class="text-sm font-bold text-gray-700">{{ securityStore.latestCompletedAttack.duration }}</div>
              </div>
            </div>
            <div class="mt-4">
              <div class="text-xs text-gray-400 mb-2">已注入毒化文档</div>
              <div v-for="doc in securityStore.latestCompletedAttack.poisonDocs" :key="doc.id" class="bg-red-50 border border-red-100 rounded-lg p-3 mb-2 text-sm text-gray-600">
                <span class="font-medium text-red-600">{{ doc.title }}</span>
                <span class="text-gray-400 ml-2">PPL={{ doc.ppl }} | Sim={{ doc.similarity }}</span>
              </div>
            </div>
          </div>
        </div>
      </a-tab-pane>

      <a-tab-pane key="compare" tab="效果评估">
        <div class="bg-white rounded-xl shadow-sm border p-6">
          <div class="text-sm font-bold text-gray-800 mb-4 flex items-center gap-2">
            <span class="w-1 h-4 bg-orange-500 rounded-full"></span> 攻击效果对比
          </div>
          <div ref="attackChartRef" style="height: 320px;"></div>
          <a-table class="mt-4" :pagination="false" size="small" :dataSource="attackEffectRows" rowKey="method">
            <a-table-column title="方法" dataIndex="method" :width="220" />
            <a-table-column title="类型" dataIndex="type" :width="80" align="center">
              <template #default="{ record }">
                <a-tag :color="record.type === '黑盒' ? 'orange' : 'blue'">{{ record.type }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column title="ASR(%)" dataIndex="asr" :width="100" align="center" />
            <a-table-column title="RSR(%)" dataIndex="rsr" :width="100" align="center" />
            <a-table-column title="PPL" dataIndex="ppl" :width="80" align="center" />
          </a-table>
        </div>
      </a-tab-pane>
    </a-tabs>

    <a-drawer v-model:open="state.poisonPreviewVisible" title="毒化文本预览" width="700" v-if="state.previewTask">
      <div class="space-y-4">
        <div v-for="doc in state.previewTask.poisonDocs" :key="doc.id" class="border rounded-lg p-4">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center gap-2">
              <a-tag color="red" size="small">毒化文档</a-tag>
              <span class="font-medium text-gray-800">{{ doc.title }}</span>
            </div>
            <a-tag :color="doc.injected ? 'green' : 'default'" size="small">{{ doc.injected ? '已注入' : '未注入' }}</a-tag>
          </div>
          <div class="bg-gray-50 rounded p-3 text-sm text-gray-600 leading-relaxed mb-2">{{ doc.content }}</div>
          <div class="flex items-center gap-4 text-xs text-gray-400">
            <span>PPL: {{ doc.ppl }}</span>
            <span>Sim: {{ doc.similarity }}</span>
          </div>
        </div>
      </div>
    </a-drawer>
  </div>
</template>
