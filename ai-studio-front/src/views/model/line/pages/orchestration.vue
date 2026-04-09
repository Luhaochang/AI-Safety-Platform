<template>
  <div class="flex flex-col h-full bg-gray-50" style="height: calc(100vh - 84px);">
    <!-- Top Bar -->
    <div class="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-6 z-10 shadow-sm">
      <div class="flex items-center gap-4">
        <div class="cursor-pointer text-gray-500 hover:text-blue-600 flex items-center gap-1 text-sm font-medium"
             @click="router.go(-1)">
          <ArrowLeftOutlined/>
          返回列表
        </div>
        <div class="h-4 w-px bg-gray-300"></div>
        <div class="font-bold text-lg text-gray-800">任务编排: {{ lineInfo.lineName || lineId }}</div>
      </div>
      <div class="flex items-center gap-3">
        <div v-if="validationMessage" :class="validationStatus === 'success' ? 'text-green-600' : 'text-red-600'"
             class="text-sm font-medium mr-4 flex items-center gap-1">
          <CheckCircleOutlined v-if="validationStatus === 'success'"/>
          <CloseCircleOutlined v-else/>
          {{ validationMessage }}
        </div>
        <el-button @click="autoLayout">
          <template #icon>
            <AppstoreOutlined/>
          </template>
          一键美化
        </el-button>
        <el-button @click="validateFlow">校验</el-button>
        <el-button type="primary" @click="saveFlow" :loading="saving">
          <template #icon>
            <SaveOutlined/>
          </template>
          保存
        </el-button>
        <el-button type="primary" @click="runFlow" :disabled="validationStatus !== 'success'" :loading="running">
          <template #icon>
            <PlayCircleOutlined/>
          </template>
          运行
        </el-button>
      </div>
    </div>

    <div class="flex-1 flex overflow-hidden">
      <!-- Left Sidebar: Component Library -->
      <div class="w-64 bg-white border-r border-gray-200 flex flex-col shadow-sm z-10">
        <div class="p-3 bg-gray-50 border-b border-gray-200 font-bold text-xs text-gray-500 uppercase tracking-wider">
          组件库
        </div>
        <div class="flex-1 overflow-y-auto p-2">

          <!-- Data Engineering -->
          <div class="mb-6">
            <div class="px-2 py-1 text-xs font-bold text-gray-400 mb-2 flex items-center gap-1">
              <DatabaseOutlined/>
              数据工程
            </div>
            <div class="grid grid-cols-1 gap-2">
              <div
                  v-for="item in componentLibrary.data"
                  :key="item.type"
                  class="lib-item border-l-4 border-l-green-500"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
              >
                <component :is="item.icon" class="text-green-600"/>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

          <!-- Model Training -->
          <div class="mb-6">
            <div class="px-2 py-1 text-xs font-bold text-gray-400 mb-2 flex items-center gap-1">
              <RocketOutlined/>
              模型训练
            </div>
            <div class="grid grid-cols-1 gap-2">
              <div
                  v-for="item in componentLibrary.train"
                  :key="item.type"
                  class="lib-item border-l-4 border-l-red-500"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
              >
                <component :is="item.icon" class="text-red-500"/>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

          <!-- Evaluation -->
          <div class="mb-6">
            <div class="px-2 py-1 text-xs font-bold text-gray-400 mb-2 flex items-center gap-1">
              <ExperimentOutlined/>
              评估验收
            </div>
            <div class="grid grid-cols-1 gap-2">
              <div
                  v-for="item in componentLibrary.eval"
                  :key="item.type"
                  class="lib-item border-l-4 border-l-blue-500"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
              >
                <component :is="item.icon" class="text-blue-600"/>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

          <!-- AI Security -->
          <div class="mb-6">
            <div class="px-2 py-1 text-xs font-bold text-gray-400 mb-2 flex items-center gap-1">
              <SafetyCertificateOutlined/>
              AI 安全与对抗
            </div>
            <div class="grid grid-cols-1 gap-2">
              <div
                  v-for="item in componentLibrary.security"
                  :key="item.type"
                  class="lib-item border-l-4 border-l-orange-500"
                  draggable="true"
                  @dragstart="onDragStart($event, item)"
              >
                <component :is="item.icon" class="text-orange-500"/>
                <span>{{ item.label }}</span>
              </div>
            </div>
          </div>

        </div>
      </div>

      <!-- Center Canvas -->
      <div
          class="flex-1 relative bg-gray-100 overflow-hidden canvas-bg"
          ref="canvasRef"
          @drop="onDrop"
          @dragover.prevent
          @mousedown="onCanvasMouseDown"
      >
        <!-- Background Grid/Dots -->
        <svg class="absolute inset-0 w-full h-full pointer-events-none z-0">
          <defs>
            <pattern id="grid" width="20" height="20" patternUnits="userSpaceOnUse">
              <circle cx="1" cy="1" r="1" fill="#cbd5e1"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)"/>

          <!-- Connections -->
          <g v-if="nodes.length > 1">
            <path
                v-for="(conn, index) in connections"
                :key="index"
                :d="conn.path"
                stroke="#94a3b8"
                stroke-width="2"
                fill="none"
                marker-end="url(#arrow)"
                class="transition-all duration-300"
            />
          </g>
          <defs>
            <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto"
                    markerUnits="strokeWidth">
              <path d="M0,0 L0,6 L9,3 z" fill="#94a3b8"/>
            </marker>
          </defs>
        </svg>

        <!-- Nodes -->
        <div
            v-for="(node, index) in nodes"
            :key="node.id"
            class="absolute w-52 bg-white rounded-lg shadow-lg border-2 cursor-move z-10 transition-all duration-500 ease-in-out group hover:shadow-xl"
            :class="[
            selectedNodeId === node.id ? 'border-blue-500 ring-2 ring-blue-100 scale-105' : 'border-transparent hover:border-blue-300'
          ]"
            :style="{ left: node.x + 'px', top: node.y + 'px' }"
            @mousedown.stop="onNodeMouseDown($event, node)"
            @click.stop="selectNode(node)"
        >
          <!-- Node Header -->
          <div
              class="px-3 py-2 rounded-t-md border-b border-gray-100 flex justify-between items-center bg-gray-50"
              :style="{ borderLeft: `4px solid ${getNodeColor(node.category)}` }"
          >
            <div class="flex items-center gap-2">
              <component :is="node.icon" :style="{ color: getNodeColor(node.category) }"/>
              <span class="font-bold text-xs text-gray-700">{{ node.label }}</span>
            </div>
            <!-- Status Dot -->
            <div class="flex items-center gap-1">
              <div class="w-2 h-2 rounded-full bg-gray-300"></div>
            </div>
          </div>

          <!-- Node Body -->
          <div class="p-3 text-xs text-gray-500 bg-white rounded-b-md">
            <div class="mb-1 font-mono text-[10px] text-gray-400">ID: {{ node.id.toString().slice(-4) }}</div>
            <div class="line-clamp-2">{{ getNodeSummary(node) }}</div>
          </div>

          <!-- Ports -->
          <div
              class="absolute w-3 h-3 bg-white border-2 border-gray-400 rounded-full top-1/2 -left-1.5 transform -translate-y-1/2 hover:bg-blue-500 hover:border-blue-500 transition-colors z-20"></div>
          <div
              class="absolute w-3 h-3 bg-white border-2 border-gray-400 rounded-full top-1/2 -right-1.5 transform -translate-y-1/2 hover:bg-blue-500 hover:border-blue-500 transition-colors z-20"></div>

          <!-- Delete Button -->
          <div
              class="absolute -top-2 -right-2 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer bg-white rounded-full shadow-md p-0.5 z-30"
              @click.stop="removeNode(index)">
            <CloseCircleFilled class="text-red-500 text-lg"/>
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="nodes.length === 0"
             class="absolute inset-0 flex flex-col items-center justify-center text-gray-400 pointer-events-none">
          <NodeIndexOutlined class="text-6xl mb-4 opacity-10"/>
          <p class="text-lg font-medium opacity-40">从左侧拖拽组件构建 AI 流水线</p>
        </div>
      </div>

      <!-- Right Panel: Properties / Copilot -->
      <div class="w-80 bg-white border-l border-gray-200 flex flex-col shadow-sm z-10">
        <div class="p-3 border-b border-gray-200 font-bold text-sm flex items-center gap-2 bg-gray-50">
          <span
              class="w-6 h-6 rounded-full bg-gradient-to-r from-purple-500 to-indigo-500 text-white flex items-center justify-center text-xs shadow-sm">AI</span>
          配置 & Copilot
        </div>

        <div class="flex-1 overflow-y-auto p-4">
          <div v-if="selectedNode" class="animate-fade-in">
            <!-- Node Info Card -->
            <div class="mb-6 p-4 bg-white rounded-xl border border-gray-200 shadow-sm">
              <div class="flex items-center gap-3 mb-2">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center bg-gray-50 border border-gray-100">
                  <component :is="selectedNode.icon" class="text-xl"
                             :style="{ color: getNodeColor(selectedNode.category) }"/>
                </div>
                <div>
                  <div class="text-sm font-bold text-gray-800">{{ selectedNode.label }}</div>
                  <div class="text-xs text-gray-400">{{ selectedNode.type }}</div>
                </div>
              </div>
              <div class="text-xs text-gray-500 bg-gray-50 p-2 rounded">
                {{ getNodeDescription(selectedNode.type) }}
              </div>
            </div>

            <el-form label-position="top" size="small">
              <el-divider content-position="left">参数配置</el-divider>

              <!-- Common: Node Name -->
              <el-form-item label="节点名称">
                <el-input v-model="selectedNode.label"/>
              </el-form-item>

              <!-- Data: Dataset Load -->
              <template v-if="selectedNode.type === 'dataset_load'">
                <el-form-item label="选择数据集">
                  <el-select v-model="selectedNode.data.datasetId" placeholder="请选择数据集" class="w-full">
                    <el-option v-for="ds in availableDatasets" :key="ds.id" :label="ds.dataSetName" :value="ds.id"/>
                  </el-select>
                </el-form-item>
              </template>

              <!-- Data: Split -->
              <template v-if="selectedNode.type === 'data_split'">
                <el-form-item label="切分比例 (训练:验证:测试)">
                  <el-radio-group v-model="selectedNode.data.ratio">
                    <el-radio label="8:1:1">8:1:1</el-radio>
                    <el-radio label="7:2:1">7:2:1</el-radio>
                    <el-radio label="8:2">8:2</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="随机种子 (Seed)">
                  <el-input-number v-model="selectedNode.data.seed" :min="0"/>
                </el-form-item>
              </template>

              <!-- Train: CV -->
              <template v-if="selectedNode.type === 'cv_train'">
                <el-form-item label="骨干网络 (Backbone)">
                  <el-select v-model="selectedNode.data.modelId" @change="handleModelChange">
                    <el-option v-for="m in availableModels" :key="m.id" :label="m.name" :value="m.id"/>
                  </el-select>
                </el-form-item>

                <!-- Dynamic Params from Model -->
                <template v-if="selectedNode.data.modelParams && selectedNode.data.modelParams.length > 0">
                   <el-form-item v-for="(param, index) in selectedNode.data.modelParams" :key="index" :label="param.label || param.key">
                      <el-input v-model="param.value" :placeholder="param.des || param.label"/>
                      <div v-if="param.des" class="text-xs text-gray-400 mt-1">{{ param.des }}</div>
                   </el-form-item>
                </template>

                <template v-else>
                    <el-form-item label="训练轮次 (Epochs)">
                      <el-slider v-model="selectedNode.data.epochs" :min="1" :max="300" show-input/>
                    </el-form-item>
                    <el-form-item label="批大小 (Batch Size)">
                      <el-select v-model="selectedNode.data.batchSize">
                        <el-option label="16" :value="16"/>
                        <el-option label="32" :value="32"/>
                        <el-option label="64" :value="64"/>
                      </el-select>
                    </el-form-item>
                </template>
              </template>

              <!-- Train: LLM SFT -->
              <template v-if="selectedNode.type === 'llm_sft'">
                <el-form-item label="基座模型 (Base Model)">
                  <el-select v-model="selectedNode.data.modelId" @change="handleModelChange">
                    <el-option v-for="m in availableModels" :key="m.id" :label="m.name" :value="m.id"/>
                  </el-select>
                </el-form-item>

                 <!-- Dynamic Params from Model -->
                <template v-if="selectedNode.data.modelParams && selectedNode.data.modelParams.length > 0">
                   <el-form-item v-for="(param, index) in selectedNode.data.modelParams" :key="index" :label="param.label || param.key">
                      <el-input v-model="param.value" :placeholder="param.des || param.label"/>
                      <div v-if="param.des" class="text-xs text-gray-400 mt-1">{{ param.des }}</div>
                   </el-form-item>
                </template>

                <template v-else>
                    <el-form-item label="学习率 (Learning Rate)">
                      <el-input v-model="selectedNode.data.lr" placeholder="e.g. 2e-5"/>
                    </el-form-item>
                    <el-form-item label="最大长度 (Max Length)">
                      <el-input-number v-model="selectedNode.data.maxLength" :step="128" :min="512" :max="8192"/>
                    </el-form-item>
                </template>
              </template>

              <!-- Train: LoRA -->
              <template v-if="selectedNode.type === 'llm_lora'">
                <el-form-item label="LoRA Rank">
                  <el-input-number v-model="selectedNode.data.rank" :step="4" :min="4" :max="128"/>
                </el-form-item>
                <el-form-item label="LoRA Alpha">
                  <el-input-number v-model="selectedNode.data.alpha" :step="8" :min="8" :max="256"/>
                </el-form-item>
                <el-form-item label="Dropout">
                  <el-input-number v-model="selectedNode.data.dropout" :step="0.05" :min="0" :max="0.5"/>
                </el-form-item>
              </template>

              <!-- Eval -->
              <template v-if="selectedNode.category === 'eval'">
                <el-form-item label="评估指标">
                  <el-checkbox-group v-model="selectedNode.data.metrics">
                    <el-checkbox label="Accuracy" v-if="selectedNode.type.includes('cv')"/>
                    <el-checkbox label="mAP" v-if="selectedNode.type.includes('cv')"/>
                    <el-checkbox label="BLEU" v-if="selectedNode.type.includes('llm')"/>
                    <el-checkbox label="ROUGE" v-if="selectedNode.type.includes('llm')"/>
                    <el-checkbox label="Perplexity" v-if="selectedNode.type.includes('llm')"/>
                  </el-checkbox-group>
                </el-form-item>
              </template>

              <!-- Security -->
              <template v-if="selectedNode.category === 'security'">
                <el-form-item label="攻击方法" v-if="selectedNode.type === 'adv_attack'">
                  <el-select v-model="selectedNode.data.method">
                    <el-option label="FGSM" value="fgsm"/>
                    <el-option label="PGD" value="pgd"/>
                    <el-option label="CW" value="cw"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="扰动大小 (Epsilon)" v-if="selectedNode.type === 'adv_attack'">
                  <el-input-number v-model="selectedNode.data.epsilon" :step="0.01" :min="0" :max="1"/>
                </el-form-item>
                <el-form-item label="防御策略" v-if="selectedNode.type === 'adv_defense'">
                  <el-select v-model="selectedNode.data.strategy">
                    <el-option label="对抗训练" value="adv_training"/>
                    <el-option label="输入预处理" value="input_preprocessing"/>
                  </el-select>
                </el-form-item>
              </template>

            </el-form>

            <!-- Copilot Suggestion -->
            <div class="mt-6 pt-4 border-t border-dashed border-gray-200">
              <div class="flex items-center gap-2 mb-2">
                <BulbOutlined class="text-yellow-500"/>
                <span class="text-xs font-bold text-gray-600 uppercase">Copilot 智能建议</span>
              </div>
              <div
                  class="bg-gradient-to-br from-purple-50 to-indigo-50 border border-purple-100 rounded-lg p-3 text-xs text-gray-700 leading-relaxed shadow-sm">
                {{ getCopilotSuggestion(selectedNode) }}
              </div>
            </div>
          </div>

          <div v-else class="h-full flex flex-col items-center justify-center text-gray-400">
            <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4">
              <SettingOutlined class="text-3xl opacity-30"/>
            </div>
            <p class="text-sm font-medium">点击画布节点配置参数</p>
            <p class="text-xs mt-2 opacity-60">AI Copilot 将为您提供优化建议</p>
          </div>
        </div>

        <div class="p-3 border-top border-gray-200 bg-gray-50">
          <el-input placeholder="输入指令 (e.g. 优化学习率)..." size="small">
            <template #suffix>
              <SendOutlined class="cursor-pointer text-blue-500 hover:text-blue-600"/>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  PlayCircleOutlined,
  FileTextOutlined,
  RocketOutlined,
  BulbOutlined,
  NodeIndexOutlined,
  CloseCircleFilled,
  SettingOutlined,
  SendOutlined,
  DatabaseOutlined,
  ScissorOutlined,
  FilterOutlined,
  CameraOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  BarChartOutlined,
  ReadOutlined,
  ExperimentOutlined,
  AppstoreOutlined,
  SafetyCertificateOutlined,
  BugOutlined,
  SaveOutlined
} from '@ant-design/icons-vue';
import {ElMessage} from 'element-plus';
import {getModelLineById, updateModelLine, trainModelLine} from "@/api/modelMag/modelLine.js";
import {listDataset} from "@/api/dataMag/dataset.js";
import {listModel} from "@/api/modelMag/model.js";

const route = useRoute();
const router = useRouter();
const lineId = ref(null);
const lineInfo = ref({});
const canvasRef = ref(null);
const saving = ref(false);
const running = ref(false);

// Colors
const COLORS = {
  data: '#10b981', // Green
  train: '#ef4444', // Red
  eval: '#3b82f6',   // Blue
  security: '#f97316' // Orange
};

// Component Library
const componentLibrary = reactive({
  data: [
    {
      type: 'dataset_load',
      label: '数据加载',
      category: 'data',
      icon: 'DatabaseOutlined',
      data: {datasetId: null, version: 'v1.0'}
    },
    {type: 'data_split', label: '数据切分', category: 'data', icon: 'ScissorOutlined', data: {ratio: '8:2', seed: 42}},
    // {type: 'data_clean', label: '数据清洗', category: 'data', icon: 'FilterOutlined', data: {method: 'auto'}}
  ],
  train: [
    {
      type: 'cv_train',
      label: '视觉模型训练',
      category: 'train',
      icon: 'CameraOutlined',
      data: {modelId: null, epochs: 50, batchSize: 32}
    },
    {
      type: 'llm_sft',
      label: '大模型全量微调',
      category: 'train',
      icon: 'RobotOutlined',
      data: {modelId: null, lr: '2e-5', maxLength: 2048}
    },
    {
      type: 'llm_lora',
      label: 'LoRA微调',
      category: 'train',
      icon: 'ThunderboltOutlined',
      data: {rank: 8, alpha: 16, dropout: 0.1}
    }
  ],
  eval: [
    {type: 'cv_eval', label: '视觉指标评估', category: 'eval', icon: 'BarChartOutlined', data: {metrics: ['Accuracy']}},
    {type: 'llm_eval', label: '文本生成评测', category: 'eval', icon: 'ReadOutlined', data: {metrics: ['BLEU']}}
  ],
  security: [
    {type: 'adv_attack', label: '对抗样本生成', category: 'security', icon: 'BugOutlined', data: {method: 'fgsm', epsilon: 0.03}},
    {type: 'adv_defense', label: '模型防御增强', category: 'security', icon: 'ShieldOutlined', data: {strategy: 'adv_training'}}
  ]
});

// State
const nodes = ref([]);
const selectedNodeId = ref(null);
const validationStatus = ref('');
const validationMessage = ref('');
const availableDatasets = ref([]);
const availableModels = ref([]);

const selectedNode = computed(() => {
  return nodes.value.find(n => n.id === selectedNodeId.value);
});

// Connections (Simple sequential connection for demo)
const connections = computed(() => {
  if (nodes.value.length < 2) return [];
  const conns = [];
  // Sort nodes by x position to determine flow
  const sortedNodes = [...nodes.value].sort((a, b) => a.x - b.x);

  for (let i = 0; i < sortedNodes.length - 1; i++) {
    const source = sortedNodes[i];
    const target = sortedNodes[i + 1];

    // Calculate path
    const startX = source.x + 208; // Width of node (w-52 = 13rem = 208px)
    const startY = source.y + 42;  // Approx center height
    const endX = target.x;
    const endY = target.y + 42;

    const cp1x = startX + 50;
    const cp1y = startY;
    const cp2x = endX - 50;
    const cp2y = endY;

    conns.push({
      path: `M ${startX} ${startY} C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${endX} ${endY}`
    });
  }
  return conns;
});

onMounted(async () => {
  lineId.value = route.params.lineId;
  if (lineId.value) {
    try {
      const res = await getModelLineById(lineId.value);
      if (res.code === 200 && res.data) {
        lineInfo.value = res.data;
        if (res.data.canvasSnapshot) {
          try {
            nodes.value = JSON.parse(res.data.canvasSnapshot);
            validateFlow();
          } catch (e) {
            console.error('Failed to parse canvas snapshot', e);
          }
        }
        // Fetch dynamic data based on scene
        const taskTypeId = res.data.taskTypeId;
        if (taskTypeId) {
          fetchDatasets(taskTypeId);
          fetchModels(taskTypeId);
        }
      }
    } catch (error) {
      console.error('Failed to fetch model line info', error);
    }
  }
});

const fetchDatasets = async (taskTypeId) => {
  try {
    const res = await listDataset({ taskTypeIdEq: taskTypeId, pageNo: 1, pageSize: 1000 });
    if (res.code === 200) {
      availableDatasets.value = res.data.records;
    }
  } catch (error) {
    console.error('Failed to fetch datasets', error);
  }
};

const fetchModels = async (taskTypeId) => {
  try {
    const res = await listModel({ taskTypeIdEq: taskTypeId, pageNo: 1, pageSize: 1000 });
    if (res.code === 200) {
      availableModels.value = res.data.records;
    }
  } catch (error) {
    console.error('Failed to fetch models', error);
  }
};

const handleModelChange = (modelId) => {
  const model = availableModels.value.find(m => m.id === modelId);
  if (model && model.paramsString) {
    try {
      const params = JSON.parse(model.paramsString);
      // Update selected node data with model params
      if (selectedNode.value) {
        selectedNode.value.data.modelParams = params;
      }
    } catch (e) {
      console.error('Failed to parse model params', e);
      if (selectedNode.value) {
        selectedNode.value.data.modelParams = [];
      }
    }
  } else {
      if (selectedNode.value) {
        selectedNode.value.data.modelParams = [];
      }
  }
};


// Drag & Drop Logic
const onDragStart = (event, item) => {
  event.dataTransfer.setData('application/json', JSON.stringify(item));
  event.dataTransfer.effectAllowed = 'copy';
};

const onDrop = (event) => {
  const dataStr = event.dataTransfer.getData('application/json');
  if (!dataStr) return;

  const item = JSON.parse(dataStr);
  const rect = canvasRef.value.getBoundingClientRect();

  const x = event.clientX - rect.left - 104; // Center the node (208/2)
  const y = event.clientY - rect.top - 20;

  const newNode = {
    id: Date.now(),
    ...item,
    x,
    y,
    data: {...item.data} // Deep copy data
  };

  nodes.value.push(newNode);
  selectNode(newNode);
  validateFlow();
};

// Node Moving Logic
let isDraggingNode = false;
let dragOffset = {x: 0, y: 0};
let draggedNode = null;

const onNodeMouseDown = (event, node) => {
  isDraggingNode = true;
  draggedNode = node;
  dragOffset.x = event.clientX - node.x;
  dragOffset.y = event.clientY - node.y;

  document.addEventListener('mousemove', onMouseMove);
  document.addEventListener('mouseup', onMouseUp);
};

const onMouseMove = (event) => {
  if (!isDraggingNode || !draggedNode) return;

  draggedNode.x = event.clientX - dragOffset.x;
  draggedNode.y = event.clientY - dragOffset.y;
};

const onMouseUp = () => {
  isDraggingNode = false;
  draggedNode = null;
  document.removeEventListener('mousemove', onMouseMove);
  document.removeEventListener('mouseup', onMouseUp);
};

const onCanvasMouseDown = () => {
  selectedNodeId.value = null;
};

const selectNode = (node) => {
  selectedNodeId.value = node.id;
};

const removeNode = (index) => {
  if (nodes.value[index].id === selectedNodeId.value) {
    selectedNodeId.value = null;
  }
  nodes.value.splice(index, 1);
  validateFlow();
};

const autoLayout = () => {
  if (nodes.value.length === 0) return;

  // Sort by current X position to maintain relative order
  const sortedNodes = [...nodes.value].sort((a, b) => a.x - b.x);

  const startX = 100;
  const startY = 150;
  const gapX = 280; // Node width (208) + space

  sortedNodes.forEach((node, index) => {
    // Find the original node in the ref array to update it
    const originalNode = nodes.value.find(n => n.id === node.id);
    if (originalNode) {
      originalNode.x = startX + index * gapX;
      originalNode.y = startY;
    }
  });

  ElMessage.success('已自动美化布局');
};

const getNodeColor = (category) => {
  return COLORS[category] || '#666';
};

const getNodeDescription = (type) => {
  const map = {
    'dataset_load': '从数据仓库加载原始数据，支持版本控制。',
    'data_split': '将数据集划分为训练集、验证集和测试集。',
    'cv_train': '使用 GPU 训练计算机视觉模型。',
    'llm_sft': '对大语言模型进行全参数监督微调。',
    'llm_lora': '使用 LoRA 技术进行高效参数微调。',
    'cv_eval': '评估视觉模型在测试集上的表现。',
    'llm_eval': '评估大模型生成的文本质量。',
    'adv_attack': '生成对抗样本以测试模型鲁棒性。',
    'adv_defense': '应用防御策略增强模型安全性。'
  };
  return map[type] || '通用处理节点';
};

const getNodeSummary = (node) => {
  if (node.type === 'dataset_load') {
    const ds = availableDatasets.value.find(d => d.id === node.data.datasetId);
    return ds ? ds.dataSetName : '未选择数据';
  }
  if (node.type === 'cv_train' || node.type === 'llm_sft') {
    const model = availableModels.value.find(m => m.id === node.data.modelId);
    return model ? model.name : '未选择模型';
  }
  if (node.type === 'llm_lora') return `Rank: ${node.data.rank}, Alpha: ${node.data.alpha}`;
  if (node.type === 'adv_attack') return `Method: ${node.data.method}, Eps: ${node.data.epsilon}`;
  if (node.type === 'adv_defense') return `Strategy: ${node.data.strategy}`;
  return '配置参数...';
};

const getCopilotSuggestion = (node) => {
  if (node.type === 'llm_lora') return '🤖 LoRA (Low-Rank Adaptation) 是一种高效的微调技术。通过调整 Rank 和 Alpha 参数，可以在显存占用和模型性能之间找到平衡点。建议根据显存大小和任务难度进行尝试。';
  if (node.type === 'cv_train') return '🤖 视觉模型的训练效果受多种因素影响，包括骨干网络的选择、训练轮次（Epochs）以及批大小（Batch Size）。建议在训练初期监控 Loss 曲线，以判断是否需要调整学习率。';
  if (node.type === 'dataset_load') return '🤖 高质量的数据集是训练优秀模型的前提。请确保所选数据集的标注准确无误，且数据分布能够代表实际应用场景。';
  if (node.type === 'adv_attack') return '🤖 对抗攻击旨在发现模型的潜在弱点。FGSM 适合快速评估，而 PGD 等迭代方法通常能生成更具攻击性的样本。请根据评估时间和深度的需求进行选择。';
  if (node.type === 'adv_defense') return '🤖 防御策略的选择应基于预期的攻击类型。对抗训练虽然能提升鲁棒性，但有时会牺牲一部分在干净数据上的准确率，这是一种权衡。';
  if (node.type === 'llm_sft') return '🤖 全量微调（SFT）能够最大程度地激发大模型的潜力，但也需要巨大的计算资源。请确保您的硬件环境足以支撑所选基座模型的全参数训练。';
  if (node.type === 'data_split') return '🤖 合理的数据切分对于评估模型泛化能力至关重要。通常建议保留一部分数据作为验证集和测试集，以避免模型在训练集上过拟合。';
  return '🤖 AI Copilot 正在持续监控您的配置。点击任意节点，我将为您提供针对性的建议和说明。';
};

// Validation
const validateFlow = () => {
  validationStatus.value = '';
  validationMessage.value = '';

  if (nodes.value.length === 0) {
    validationStatus.value = 'error';
    validationMessage.value = '流程不能为空';
    return;
  }

  const hasData = nodes.value.some(n => n.category === 'data');
  const hasTrain = nodes.value.some(n => n.category === 'train');

  if (!hasData) {
    validationStatus.value = 'error';
    validationMessage.value = '缺少数据工程节点';
    return;
  }
  if (!hasTrain) {
    validationStatus.value = 'error';
    validationMessage.value = '缺少模型训练节点';
    return;
  }

  // Check configuration
  for (const node of nodes.value) {
    if (node.type === 'dataset_load' && !node.data.datasetId) {
      validationStatus.value = 'error';
      validationMessage.value = `节点 "${node.label}" 未配置数据集`;
      return;
    }
    if ((node.type === 'cv_train' || node.type === 'llm_sft') && !node.data.modelId) {
      validationStatus.value = 'error';
      validationMessage.value = `节点 "${node.label}" 未配置模型`;
      return;
    }
  }

  validationStatus.value = 'success';
  validationMessage.value = '编排正确';
};

const saveFlow = async () => {
  saving.value = true;
  try {
    const datasetNode = nodes.value.find(n => n.type === 'dataset_load');
    const trainNode = nodes.value.find(n => n.category === 'train');

    const payload = {
      id: lineId.value,
      canvasSnapshot: JSON.stringify(nodes.value),
    };

    if (datasetNode && datasetNode.data.datasetId) {
        payload.dataSetId = datasetNode.data.datasetId;
    }
    if (trainNode && trainNode.data.modelId) {
        payload.modelId = trainNode.data.modelId;
    }
    if (trainNode) {
        payload.paramsString = JSON.stringify(trainNode.data);
    }

    const updateRes = await updateModelLine(payload);

    if (updateRes.code !== 200) {
      throw new Error(updateRes.msg || '保存失败');
    }
    ElMessage.success('保存成功');
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    saving.value = false;
  }
};

const runFlow = async () => {
  if (validationStatus.value !== 'success') {
    ElMessage.error('请先修正流程错误');
    return;
  }

  running.value = true;
  try {
    // 1. Save first
    await saveFlow();

    // 2. Start Training
    const trainNode = nodes.value.find(n => n.category === 'train');
    const trainParams = trainNode ? trainNode.data : {};

    const map = trainParams.modelParams.reduce((acc,item) => {
      acc[item.key] = item.value;
      return acc;
    },{});

    const trainRes = await trainModelLine(lineId.value, map);

    if (trainRes.code === 200) {
      ElMessage.success('任务已提交到集群');
      setTimeout(() => {
        router.push('/modelmag/model-line');
      }, 1000);
    } else {
      throw new Error(trainRes.msg || '训练启动失败');
    }
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    running.value = false;
  }
};
</script>

<style scoped>
.lib-item {
  padding: 10px 12px;
  margin-bottom: 8px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: grab;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #374151;
  transition: all 0.2s;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.lib-item:hover {
  border-color: #3b82f6;
  color: #2563eb;
  background: #eff6ff;
  transform: translateX(4px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.canvas-bg {
  background-image: radial-gradient(#cbd5e1 1px, transparent 1px);
  background-size: 20px 20px;
}
</style>