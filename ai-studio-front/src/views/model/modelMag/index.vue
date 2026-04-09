<script setup>
import {
  ArrowRightOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  EditOutlined,
  FolderAddOutlined,
  ApiOutlined,
  RobotOutlined,
  LinkOutlined,
  CloudServerOutlined
} from "@ant-design/icons-vue";
import {useRouter} from "vue-router";
import {getModelOptions, listModel} from "@/api/modelMag/model.js";
import {Clock, Refresh, Search, Sort, Star, User, View} from "@element-plus/icons-vue";
import {nextTick, onMounted, reactive, ref} from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

// Qwen 外部API模型状态
const qwenConnected = computed(() => !!import.meta.env.VITE_QWEN_API_KEY);
const externalModels = [
    {
        name: '通义千问 Qwen-Plus',
        provider: '阿里云 DashScope',
        description: '通义千问大语言模型，支持多轮对话、文本生成、知识问答。已集成为调度助手后端引擎。',
        type: 'API调用',
        params: '~100B',
        context: '128K tokens',
        usage: '调度助手',
        endpoint: 'dashscope.aliyuncs.com',
        route: '/rag-scheduler/chat'
    }
];

const router = useRouter()
const cardScroll = ref(null) // 模型列表区域滚动条引用

// Filter options state
const options = reactive({
  appScenes: [],
  taskTypes: [],
  providers: [],
  frameworks: []
});

const state = reactive({
  modelList: [],
  total: 0,
  queryParams: {
    pageNo: 1,
    pageSize: 50,
    appSceneIdJson: undefined,
    taskTypeIdEq: undefined,
    nameLike: undefined,
    providerIdEq: undefined,
    frameIdEq: undefined,
    sort: 'default'
  },
  loading: false,
  showProcess: true
})

const scrollToTop = () => {
  nextTick(() => {
    if (cardScroll.value) {
      cardScroll.value.scrollTop = 0;
    }
  })
}

const handleFilterChange = () => {
  state.queryParams.pageNo = 1;
  scrollToTop();
  getList();
}

const toggleFilter = (field, value) => {
  if (value === undefined) {
    state.queryParams[field] = undefined;
  } else {
    if (state.queryParams[field] === value) {
      state.queryParams[field] = undefined;
    } else {
      state.queryParams[field] = value;
    }
  }
  handleFilterChange();
}

const handleSortChange = () => {
  state.queryParams.pageNo = 1;
  scrollToTop();
  getList();
}

const handlePageChange = (page) => {
  state.queryParams.pageNo = page;
  scrollToTop();
  getList();
}

const onShowSizeChange = (current, pageSize) => {
  state.queryParams.pageSize = pageSize;
  state.queryParams.pageNo = 1;
  scrollToTop();
  getList();
};

const resetFilter = () => {
  state.queryParams = {
    pageNo: 1,
    pageSize: 50,
    sort: 'default',
    appSceneIdJson: undefined,
    taskTypeIdEq: undefined,
    nameLike: undefined,
    providerIdEq: undefined,
    frameIdEq: undefined
  };
  getList();
  scrollToTop();
}

const openCreateModelDialog = () => {
  router.push('/modelmag/model-add/model-create');
}

const getList = () => {
  state.loading = true;
  // 复制一份参数，避免修改响应式对象
  const params = JSON.parse(JSON.stringify(state.queryParams));

  // 处理排序参数
  if (params.sort === 'time_desc') {
    params.orderByColumn = 'create_time';
    params.isAsc = 'desc';
  } else if (params.sort === 'name_asc') {
    params.orderByColumn = 'name';
    params.isAsc = 'asc';
  } else if (params.sort === 'views_desc') {
    params.orderByColumn = 'model_views';
    params.isAsc = 'desc';
  } else if (params.sort === 'default') {
    // 默认排序逻辑
    params.orderByColumn = 'model_score';
    params.isAsc = 'desc';
  }

  // 重要：删除原始的 'sort' 字段，以免后端把它当作查询条件报错
  delete params.sort;

  listModel(params).then(res => {
    if (res.code === 200) {
      state.modelList = res.data.records;
      state.total = res.data.total;
    }
    state.loading = false;
  }).catch(() => {
    state.loading = false;
  })
}

const queryByName = () => {
  state.queryParams.pageNo = 1;
  getList();
}

const checkModelDetail = (model) => {
  router.push(`/modelmag/model-detail/index/${model.id}`)
}

// Helper functions
const getFrameworkName = (id) => {
  const framework = options.frameworks.find(f => f.value === id);
  return framework ? framework.label : '未知';
}

const formatModelSize = (bytes) => {
  if (!bytes || bytes === 0) return '未知';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

const getTaskTypeName = (id) => {
  const taskType = options.taskTypes.find(t => t.value === id);
  return taskType ? taskType.label : '通用';
}

const getEnvClass = (item) => {
  if ([2, 3, 4, 6].includes(item.taskTypeId)) return 'env-gpu'; // 视觉类
  if ([9, 10, 11].includes(item.taskTypeId)) return 'env-llm'; // 大模型
  return 'env-cpu'; // 其他
}

const getMetricColorClass = (item) => {
    const envClass = getEnvClass(item);
    if (envClass === 'env-gpu') return 'text-[#F58220]'; // 橙色
    if (envClass === 'env-llm') return 'text-[#005C99]'; // 蓝色
    return 'text-[#006054]'; // 绿色
}

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return dateStr.split(' ')[0];
}

onMounted(async () => {
  getList();
  try {
    const res = await getModelOptions();
    if (res.code === 200 && res.data) {
      options.appScenes = res.data.appScenes || [];
      options.taskTypes = res.data.taskTypes || [];
      options.providers = res.data.providers || [];
      options.frameworks = res.data.frameworks || [];
    }
  } catch (error) {
    console.error("Failed to fetch model options:", error);
  }
})
</script>

<template>
  <div class="min-h-screen bg-[#f5f7fa] flex flex-col">

    <!-- 1. 头部区域 -->
    <div class="py-4 px-8 bg-[#f5f7fa]">
      <div class="flex items-center justify-between">
        <div class="text-[#140E35] text-3xl font-semibold">模型库</div>
        <div class="flex items-center gap-3">
          <el-button size="large" @click="router.push('/modelmag/service-list')">
            <ApiOutlined class="mr-1" /> 部署服务
          </el-button>
          <el-button type="primary" size="large" @click="openCreateModelDialog"
                     v-hasPermi="['modelmag:model:create-off']">
            + 创建模型
          </el-button>
        </div>
      </div>
    </div>

    <!-- 2. 使用流程区域 -->
    <div class="px-8 pb-6 bg-[#f5f7fa]">
      <div v-if="state.showProcess" class="w-full bg-white rounded-xl p-6 relative shadow-sm border border-gray-100">
        <div class="absolute right-4 top-4 cursor-pointer hover:bg-gray-100 p-1 rounded-full transition-all"
             @click="state.showProcess = false">
          <CloseOutlined class="text-gray-400 text-lg"/>
        </div>
        <div class="font-bold text-lg mb-8 text-[#140E35] flex items-center gap-2">
          <span class="w-1 h-6 bg-blue-600 rounded-full"></span>
          使用流程
        </div>
        <div class="flex justify-around items-start px-4 relative">
          <!-- Step 1 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div
                class="w-16 h-16 rounded-2xl bg-blue-50 flex items-center justify-center text-blue-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <FolderAddOutlined/>
            </div>
            <div class="text-[#140E35] font-medium text-base">创建模型</div>
            <div class="text-gray-400 text-xs text-center">上传或导入模型文件</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl"/>
          </div>

          <!-- Step 2 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div
                class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <EditOutlined/>
            </div>
            <div class="text-[#140E35] font-medium text-base">模型配置</div>
            <div class="text-gray-400 text-xs text-center">配置参数与环境</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl"/>
          </div>

          <!-- Step 3 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div
                class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <BarChartOutlined/>
            </div>
            <div class="text-[#140E35] font-medium text-base">模型评估</div>
            <div class="text-gray-400 text-xs text-center">验证模型性能指标</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl"/>
          </div>

          <!-- Step 4 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div
                class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <CheckCircleOutlined/>
            </div>
            <div class="text-[#140E35] font-medium text-base">发布应用</div>
            <div class="text-gray-400 text-xs text-center">部署到生产环境</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 2.5 外部API模型 -->
    <div class="px-8 pb-6 bg-[#f5f7fa]">
      <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
        <div class="font-bold text-base mb-4 text-[#140E35] flex items-center gap-2">
          <span class="w-1 h-5 bg-purple-600 rounded-full"></span>
          外部API模型
          <a-tag color="purple" size="small">LLM</a-tag>
        </div>
        <div class="grid grid-cols-1 gap-4">
          <div v-for="model in externalModels" :key="model.name"
               class="border rounded-xl p-5 flex items-center gap-5 hover:shadow-md transition-all cursor-pointer group"
               :class="qwenConnected ? 'border-blue-200 bg-blue-50/30' : 'border-gray-200'"
               @click="router.push(model.route)">
            <div class="w-14 h-14 rounded-2xl flex items-center justify-center flex-shrink-0"
                 :class="qwenConnected ? 'bg-blue-100 text-blue-600' : 'bg-gray-100 text-gray-400'">
              <RobotOutlined class="text-2xl" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors">{{ model.name }}</span>
                <a-tag v-if="qwenConnected" color="green" size="small">已连接</a-tag>
                <a-tag v-else color="orange" size="small">未配置</a-tag>
                <a-tag color="purple" size="small">{{ model.type }}</a-tag>
              </div>
              <div class="text-sm text-gray-500 mb-2">{{ model.description }}</div>
              <div class="flex items-center gap-4 text-xs text-gray-400">
                <span><CloudServerOutlined /> {{ model.provider }}</span>
                <span>参数量: {{ model.params }}</span>
                <span>上下文: {{ model.context }}</span>
                <span><LinkOutlined /> {{ model.endpoint }}</span>
                <span class="ml-auto text-blue-500">应用于: {{ model.usage }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 主体分栏区域 -->
    <div class="flex items-start px-8 pb-6 gap-6 flex-1">

      <!-- 左侧导航栏 (Sticky) -->
      <div
          class="w-[260px] flex-shrink-0 bg-white rounded-xl border border-[#e3e8ef] shadow-sm p-4 sticky top-4 overflow-y-auto custom-scrollbar"
          style="height: calc(100vh - 32px);">

        <!-- 1. 任务类型 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">任务类型</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.taskTypeIdEq === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('taskTypeIdEq', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in options.taskTypes" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.taskTypeIdEq === opt.value }"
                 @click="toggleFilter('taskTypeIdEq', opt.value)">
                            <span class="icon-wrapper">
                                <svg-icon icon-class="component" class="w-5 h-5"/>
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <!-- 2. 应用场景 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">应用场景</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.appSceneIdJson === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('appSceneIdJson', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in options.appScenes" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.appSceneIdJson === opt.value }"
                 @click="toggleFilter('appSceneIdJson', opt.value)">
                             <span class="icon-wrapper">
                                <svg-icon icon-class="example" class="w-5 h-5"/>
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <!-- 3. 提供方 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">提供方</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.providerIdEq === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('providerIdEq', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in options.providers" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.providerIdEq === opt.value }"
                 @click="toggleFilter('providerIdEq', opt.value)">
                            <span class="icon-wrapper">
                                <svg-icon icon-class="peoples" class="w-5 h-5"/>
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <!-- 4. 模型框架 -->
        <div>
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">模型框架</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.frameIdEq === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('frameIdEq', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in options.frameworks" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.frameIdEq === opt.value }"
                 @click="toggleFilter('frameIdEq', opt.value)">
                            <span class="icon-wrapper">
                                <svg-icon icon-class="skill" class="w-5 h-5"/>
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

      </div>

      <!-- 右侧内容区域 (Sticky + Independent Scroll) -->
      <div class="flex-1 flex flex-col sticky top-4" style="height: calc(100vh - 32px);">

        <!-- 顶部工具栏 -->
        <div class="flex justify-between items-center mb-4 flex-shrink-0 bg-[#f5f7fa] z-10">
          <!-- 左侧：统计信息 -->
          <div class="text-sm text-gray-500">
            共找到 <span class="font-bold text-[#1677ff]">{{ state.total }}</span> 个模型
          </div>

          <!-- 右侧：重置、搜索、排序 -->
          <div class="flex items-center gap-3">
            <el-button link type="primary" @click="resetFilter">
              <el-icon class="mr-1">
                <Refresh/>
              </el-icon>
              重置筛选
            </el-button>

            <el-input
                v-model="state.queryParams.nameLike"
                placeholder="搜索模型名称..."
                class="w-[260px]"
                :prefix-icon="Search"
                @input="queryByName"
                clearable
            />

            <el-select v-model="state.queryParams.sort" placeholder="排序" style="width: 160px"
                       @change="handleSortChange">
              <template #prefix>
                <el-icon>
                  <Sort/>
                </el-icon>
              </template>
              <el-option label="综合排序" value="default"/>
              <el-option label="按更新时间 (新-旧)" value="time_desc"/>
              <el-option label="按模型名称 (A-Z)" value="name_asc"/>
              <el-option label="按浏览次数 (高-低)" value="views_desc"/>
            </el-select>
          </div>
        </div>

        <!-- 模型列表 (独立滚动区域) -->
        <div class="flex-1 overflow-y-auto custom-scrollbar pr-2" ref="cardScroll">
          <div v-if="state.modelList.length > 0" class="grid-container pb-4">
            <div v-for="item in state.modelList" :key="item.id"
                 class="model-card mt-2"
                 @click="checkModelDetail(item)">
              <div class="env-stripe" :class="getEnvClass(item)"></div>
              <div class="card-main">
                <div class="card-header">
                  <div class="flex gap-3 items-start overflow-hidden flex-1">
                    <div class="model-icon">
                      <svg-icon icon-class="component" class="w-6 h-6"/>
                    </div>
                    <div class="overflow-hidden flex-1 min-w-0">
                      <el-tooltip :content="item.name" placement="top" :show-after="500">
                        <div class="font-bold text-[15px] text-[#2c3e50] truncate" :title="item.name">{{
                            item.name
                          }}
                        </div>
                      </el-tooltip>
                      <div class="text-xs text-[#7f8c8d] truncate">ID: {{ item.id }}</div>
                    </div>
                  </div>
                  <div class="runtime-tag ml-2 flex-shrink-0" :class="getEnvClass(item).replace('env-', 'tag-')">
                    {{ getTaskTypeName(item.taskTypeId) }}
                  </div>
                </div>

                <div class="text-[13px] text-[#57606f] h-[40px] line-clamp-2 mb-4 leading-relaxed">
                  {{ item.description || '暂无描述' }}
                </div>

                <div class="metrics-box">
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">框架</span>
                    <span class="metric-val" :class="getMetricColorClass(item)">{{ getFrameworkName(item.frameId) }}</span>
                  </div>
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">大小</span>
                    <span class="metric-val" :class="getMetricColorClass(item)">{{ formatModelSize(item.modelSize) }}</span>
                  </div>
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">评分</span>
                    <span class="metric-val flex items-center gap-1" :class="getMetricColorClass(item)">
                                            <el-icon color="#f5c518"><Star/></el-icon>
                                            {{ item.modelScore || '暂无评分' }}
                                        </span>
                  </div>
                </div>
              </div>
              <div class="card-actions">
                <div class="flex items-center gap-4 text-xs text-[#95a5a6] w-full">
                  <div class="flex items-center gap-1 truncate max-w-[30%]">
                    <el-icon>
                      <User/>
                    </el-icon>
                    {{ item.author }}
                  </div>
                  <div class="flex items-center gap-1">
                    <el-icon>
                      <Clock/>
                    </el-icon>
                    {{ formatDate(item.createTime) }}
                  </div>
                  <div class="flex items-center gap-1 ml-auto">
                    <el-icon>
                      <View/>
                    </el-icon>
                    {{ item.modelViews }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="flex flex-col items-center justify-center py-20">
            <el-empty description="暂无符合条件的模型"/>
          </div>

          <!-- 分页 -->
          <div class="flex justify-center mt-8 mb-8" v-if="state.total > 0">
            <a-pagination
                v-model:current="state.queryParams.pageNo"
                :total="state.total"
                :page-size="state.queryParams.pageSize"
                :page-size-options="['10', '20', '50']"
                show-size-changer
                show-quick-jumper
                :show-total="total => `共 ${total} 条数据`"
                @change="handlePageChange"
                @showSizeChange="onShowSizeChange"
            />
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
/* 全局变量 */
$primary: #1677ff;
$primary-hover: #4096ff;
$bg-dark: #001529;
$success: #52c41a;
$warning: #faad14;
$error: #ff4d4f;

/* 国家电网配色参考 */
$c-sgcc-green: #006054; /* 国网绿 - 主色调 */
$c-sgcc-light: #E6F0EE; /* 浅绿背景 */
$c-sgcc-orange: #F58220; /* 橙色 - 强调 */
$c-sgcc-blue: #005C99; /* 辅助蓝 - 科技感 */

$border: #e3e8ef;
$text-main: #1f2937;
$text-sec: #6b7280;

/* 自定义滚动条 */
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

/* Grid 自适应布局 */
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

/* 导航项样式 */
.nav-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  color: $text-sec;
  transition: all 0.2s;

  &:hover {
    background-color: #f3f4f6;
    color: $primary;
  }

  &.active {
    background-color: #e6f4ff;
    color: $primary;
    font-weight: 600;
  }
}

.icon-wrapper {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

/* 模型卡片样式 */
.model-card {
  background: white;
  border-radius: 12px;
  border: 1px solid $border;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  height: 100%;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
    border-color: $c-sgcc-green;
  }
}

.env-stripe {
  height: 4px;
  width: 100%;

  &.env-cpu {
    background: $c-sgcc-green;
  }

  &.env-gpu {
    background: $c-sgcc-orange;
  }

  &.env-llm {
    background: $c-sgcc-blue;
  }
}

.card-main {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: start;
  margin-bottom: 16px;
}

.model-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  background: #f8fafc;
  flex-shrink: 0;
}

.runtime-tag {
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: bold;
  text-transform: uppercase;
  border: 1px solid transparent;
  white-space: nowrap;

  &.tag-cpu {
    color: $c-sgcc-green;
    background: rgba(0, 96, 84, 0.1);
    border-color: rgba(0, 96, 84, 0.2);
  }

  &.tag-gpu {
    color: $c-sgcc-orange;
    background: rgba(245, 130, 32, 0.1);
    border-color: rgba(245, 130, 32, 0.2);
  }

  &.tag-llm {
    color: $c-sgcc-blue;
    background: rgba(0, 92, 153, 0.1);
    border-color: rgba(0, 92, 153, 0.2);
  }
}

.metrics-box {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px 16px;
  margin-top: auto;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.metric-val {
  font-weight: bold;
  font-size: 13px;
  font-family: 'Monaco', monospace;
  color: $c-sgcc-green;
}

.card-actions {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fcfcfc;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d9d9d9 inset;

  &:hover {
    box-shadow: 0 0 0 1px $primary inset;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px $primary inset;
  }
}
</style>
