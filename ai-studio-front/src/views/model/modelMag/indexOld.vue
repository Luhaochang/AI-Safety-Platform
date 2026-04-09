<script setup>
import {
  ArrowRightOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  EditOutlined,
  FolderAddOutlined
} from "@ant-design/icons-vue";
import classification from "@/assets/images/classification.png";
import target from "@/assets/images/target.png";
import seg from "@/assets/images/segmentation.png";
import ocr from "@/assets/images/ocr.png";
import {useRouter} from "vue-router";
import {listModel} from "@/api/modelMag/model.js";
import {Clock, Refresh, Search, Sort, User, View} from "@element-plus/icons-vue";
import {nextTick, onMounted, reactive, ref} from "vue";
import SvgIcon from "@/components/SvgIcon/index.vue"; // 项目中有SvgIcon组件

const router = useRouter()
const cardScroll = ref(null) // 模型列表区域滚动条引用

// 1. 模型类别
const categoryOptions = [
  { label: '文本生成', value: 5, icon: 'edit' }, // 使用 edit.svg
  { label: '图像理解', value: 6, icon: 'eye' }, // 使用 eye.svg
  { label: '文生图', value: 7, icon: 'image' }, // 使用 image.svg
  { label: '图片分类', value: 0, icon: 'classification', isImg: true, src: classification },
  { label: '目标检测', value: 1, icon: 'target', isImg: true, src: target },
  { label: '图像分割', value: 3, icon: 'segmentation', isImg: true, src: seg },
  { label: 'OCR', value: 4, icon: 'ocr', isImg: true, src: ocr },
]

// 2. 提供方 (添加图标)
const providerOptions = [
  { label: '深度求索', value: 'DeepSeek', icon: 'deepseek' }, // 使用 deepseek.svg
  { label: '阿里巴巴', value: 'Alibaba', icon: 'qwen' }, // 使用 qwen.svg
  { label: '科大讯飞', value: 'iFlytek', icon: 'spark' }, // 使用 spark.svg
  { label: '月之暗面', value: 'Moonshot', icon: 'kimi' }, // 使用 kimi.svg
  { label: 'Meta AI', value: 'Meta', icon: 'meta' }, // 使用 meta.svg
  { label: 'Stability AI', value: 'Stability', icon: 'stability' }, // 使用 stability.svg
]

// 3. 业务场景
const businessOptions = [
  { label: '智能巡检', value: 1, icon: 'eye-open' }, // 使用 eye-open.svg
  { label: '故障诊断', value: 2, icon: 'tool' }, // 使用 tool.svg
  { label: '停电预判', value: 3, icon: 'flash', isText: true, text: '⚡' },
  { label: '操作票生成', value: 4, icon: 'form' }, // 使用 form.svg
  { label: '智能客服', value: 5, icon: 'peoples' }, // 使用 peoples.svg
]

// 4. 模型框架 (添加图标)
const frameworkOptions = [
  { label: 'Pytorch', value: 'Pytorch', icon: 'PyTorch' }, // 使用 PyTorch.svg
  { label: 'Tensorflow', value: 'Tensorflow', icon: 'TensorFlow' }, // 使用 TensorFlow.svg
  { label: 'MindSpore', value: 'MindSpore', icon: 'huawei' }, // 使用 huawei.svg
]

const state = reactive({
  modelList: [],
  total: 0,
  queryParams: {
    pageNo: 1,
    pageSize: 50,
    sceneEq: undefined, // 模型类别
    secondarySceneEq: undefined, // 业务场景
    nameLike: undefined,
    provider: undefined, // 提供方
    framework: undefined, // 框架
    sort: 'default' // 排序
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
    // 点击“全部”
    state.queryParams[field] = undefined;
  } else {
    // 点击具体选项
    if (state.queryParams[field] === value) {
      // 如果已选中，则取消选中（相当于选全部）
      state.queryParams[field] = undefined;
    } else {
      // 否则选中该项
      state.queryParams[field] = value;
    }
  }
  handleFilterChange();
}

const handleSortChange = () => {
  state.queryParams.pageNo = 1;
  scrollToTop(); // 排序变化也回到顶部
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
    sceneEq: undefined,
    secondarySceneEq: undefined,
    nameLike: undefined,
    provider: undefined,
    framework: undefined
  };
  getList();
  scrollToTop();
}

const openCreateModelDialog = () => {
  router.push('/modelmag/model-add/model-create');
}

const getList = () => {
  state.loading = true;
  // 模拟排序和额外过滤，因为后端可能不支持
  listModel(state.queryParams).then(res => {
    if (res.code === 200) {
      let list = res.data.records;

      // 前端模拟排序 (如果后端不支持)
      if (state.queryParams.sort === 'name_asc') {
        list.sort((a, b) => a.name.localeCompare(b.name));
      } else if (state.queryParams.sort === 'time_desc') {
        list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      }
      // 使用次数排序暂无法模拟，需后端支持

      state.modelList = list;
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

// 辅助函数
const getCategoryIcon = (val) => {
  const found = categoryOptions.find(opt => opt.value === val);
  if (found) {
    if (found.isImg) return { type: 'img', val: found.src };
    if (found.icon) return { type: 'svg', val: found.icon };
  }
  return { type: 'svg', val: 'component' };
}

const getCategoryName = (val) => {
  const found = categoryOptions.find(opt => opt.value === val);
  return found ? found.label : '通用模型';
}

const getEnvClass = (item) => {
  // 根据类别或框架返回颜色条样式
  if (item.scene === 0 || item.scene === 1) return 'env-gpu'; // 视觉类用 GPU
  if (item.scene === 5 || item.scene === 6) return 'env-llm'; // 大模型用 LLM
  return 'env-cpu'; // 其他用 CPU
}

const getRandomUsage = () => {
  return Math.floor(Math.random() * 5000) + 100;
}

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return dateStr.split(' ')[0];
}

onMounted(() => {
  getList();
})
</script>

<template>
  <div class="min-h-screen bg-[#f5f7fa] flex flex-col">

    <!-- 1. 头部区域 -->
    <div class="py-4 px-8 bg-[#f5f7fa]">
      <div class="flex items-center justify-between">
        <div class="text-[#140E35] text-3xl font-semibold">模型库</div>
        <el-button type="primary" size="large" @click="openCreateModelDialog"
                   v-hasPermi="['modelmag:model:create-off']">
          + 创建模型
        </el-button>
      </div>
    </div>

    <!-- 2. 使用流程区域  -->
    <div class="px-8 pb-6 bg-[#f5f7fa]">
      <div v-if="state.showProcess" class="w-full bg-white rounded-xl p-6 relative shadow-sm border border-gray-100">
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
            <div class="text-[#140E35] font-medium text-base">创建模型</div>
            <div class="text-gray-400 text-xs text-center">上传或导入模型文件</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl" />
          </div>

          <!-- Step 2 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <EditOutlined />
            </div>
            <div class="text-[#140E35] font-medium text-base">模型配置</div>
            <div class="text-gray-400 text-xs text-center">配置参数与环境</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl" />
          </div>

          <!-- Step 3 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <BarChartOutlined />
            </div>
            <div class="text-[#140E35] font-medium text-base">模型评估</div>
            <div class="text-gray-400 text-xs text-center">验证模型性能指标</div>
          </div>

          <div class="h-16 flex items-center">
            <ArrowRightOutlined class="text-gray-300 text-2xl" />
          </div>

          <!-- Step 4 -->
          <div class="flex flex-col items-center gap-1 group cursor-default w-32">
            <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
              <CheckCircleOutlined />
            </div>
            <div class="text-[#140E35] font-medium text-base">发布应用</div>
            <div class="text-gray-400 text-xs text-center">部署到生产环境</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 主体分栏区域 -->
    <div class="flex items-start px-8 pb-6 gap-6 flex-1">

      <!-- 左侧导航栏 (Sticky) -->
      <div class="w-[260px] flex-shrink-0 bg-white rounded-xl border border-[#e3e8ef] shadow-sm p-4 sticky top-4 overflow-y-auto custom-scrollbar"
           style="height: calc(100vh - 32px);">

        <!-- 1. 模型类别 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">模型类别</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.sceneEq === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('sceneEq', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in categoryOptions" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.sceneEq === opt.value }"
                 @click="toggleFilter('sceneEq', opt.value)">
                            <span class="icon-wrapper">
                                <img v-if="opt.isImg" :src="opt.src" class="w-5 h-5 object-contain" />
                                <svg-icon v-else :icon-class="opt.icon" class="w-5 h-5" />
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <!-- 2. 提供方 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">提供方</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.provider === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('provider', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in providerOptions" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.provider === opt.value }"
                 @click="toggleFilter('provider', opt.value)">
                            <span class="icon-wrapper">
                                <span v-if="opt.isText" class="text-lg leading-none">{{ opt.text }}</span>
                                <svg-icon v-else :icon-class="opt.icon" class="w-5 h-5" />
                            </span>
              <span>{{ opt.label }}</span>
            </div>
          </div>
        </div>

        <!-- 3. 业务场景 -->
        <div class="mb-6">
          <div class="flex justify-between items-center mb-3 px-2">
            <div class="text-[13px] font-bold text-gray-500 uppercase">业务场景</div>
            <div class="text-[13px] cursor-pointer transition-colors"
                 :class="state.queryParams.secondarySceneEq === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('secondarySceneEq', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in businessOptions" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.secondarySceneEq === opt.value }"
                 @click="toggleFilter('secondarySceneEq', opt.value)">
                            <span class="icon-wrapper">
                                <span v-if="opt.isText" class="text-lg leading-none">{{ opt.text }}</span>
                                <svg-icon v-else :icon-class="opt.icon" class="w-5 h-5" />
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
                 :class="state.queryParams.framework === undefined ? 'text-[#1677ff] font-bold' : 'text-gray-400 hover:text-[#1677ff]'"
                 @click="toggleFilter('framework', undefined)">
              全部
            </div>
          </div>
          <div class="flex flex-col gap-1">
            <div v-for="opt in frameworkOptions" :key="opt.value"
                 class="nav-item"
                 :class="{ active: state.queryParams.framework === opt.value }"
                 @click="toggleFilter('framework', opt.value)">
                            <span class="icon-wrapper">
                                <span v-if="opt.isText" class="text-lg leading-none">{{ opt.text }}</span>
                                <svg-icon v-else :icon-class="opt.icon" class="w-5 h-5" />
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
              <el-icon class="mr-1"><Refresh /></el-icon> 重置筛选
            </el-button>

            <el-input
                v-model="state.queryParams.nameLike"
                placeholder="搜索模型名称..."
                class="w-[260px]"
                :prefix-icon="Search"
                @input="queryByName"
                clearable
            />

            <el-select v-model="state.queryParams.sort" placeholder="排序" style="width: 160px" @change="handleSortChange">
              <template #prefix><el-icon><Sort /></el-icon></template>
              <el-option label="综合排序" value="default" />
              <el-option label="按更新时间 (新-旧)" value="time_desc" />
              <el-option label="按模型名称 (A-Z)" value="name_asc" />
              <el-option label="按使用次数 (高-低)" value="usage_desc" />
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
                  <div class="flex gap-3 items-start">
                    <div class="model-icon">
                      <img v-if="getCategoryIcon(item.scene).type === 'img'" :src="getCategoryIcon(item.scene).val" class="w-6 h-6 object-contain" />
                      <svg-icon v-else :icon-class="getCategoryIcon(item.scene).val" class="w-6 h-6" />
                    </div>
                    <div class="overflow-hidden">
                      <div class="font-bold text-[15px] text-[#2c3e50] truncate" :title="item.name">{{ item.name }}</div>
                      <div class="text-xs text-[#7f8c8d] truncate">ID: {{ item.id }}</div>
                    </div>
                  </div>
                  <div class="runtime-tag" :class="getEnvClass(item).replace('env-', 'tag-')">
                    {{ getCategoryName(item.scene) }}
                  </div>
                </div>

                <div class="text-[13px] text-[#57606f] h-[40px] line-clamp-2 mb-4 leading-relaxed">
                  {{ item.description || '暂无描述' }}
                </div>

                <div class="metrics-box">
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">框架</span>
                    <span class="metric-val text-[#006054]">PyTorch</span>
                  </div>
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">大小</span>
                    <span class="metric-val text-[#006054]">1.2G</span>
                  </div>
                  <div class="metric-item">
                    <span class="text-[#7f8c8d]">评分</span>
                    <span class="metric-val text-[#006054]">4.9</span>
                  </div>
                </div>
              </div>
              <div class="card-actions">
                <div class="flex items-center gap-4 text-xs text-[#95a5a6] w-full">
                  <div class="flex items-center gap-1 truncate max-w-[30%]">
                    <el-icon><User /></el-icon> {{ item.author }}
                  </div>
                  <div class="flex items-center gap-1">
                    <el-icon><Clock /></el-icon> {{ formatDate(item.createTime) }}
                  </div>
                  <div class="flex items-center gap-1 ml-auto">
                    <el-icon><View /></el-icon> {{ getRandomUsage() }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="flex flex-col items-center justify-center py-20">
            <el-empty description="暂无符合条件的模型" />
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
/* 国网绿 (State Grid Green): #006054 (近似) */
/* 辅助色: 浅绿 #E6F0EE, 橙色 #F58220 (用于强调/警告) */

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
  margin-right: 8px;
}

/* 模型卡片样式 (复刻 Grid AI Pro) */
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
    box-shadow: 0 12px 24px rgba(0,0,0,0.06);
    border-color: $c-sgcc-green; /* 悬停边框改为国网绿 */
  }
}

.env-stripe {
  height: 4px;
  width: 100%;

  /* 统一使用国网绿或其变体，或者用不同深浅区分 */
  &.env-cpu { background: $c-sgcc-green; }
  &.env-gpu { background: $c-sgcc-orange; } /* 视觉类用橙色区分 */
  &.env-llm { background: $c-sgcc-blue; } /* 大模型用蓝色区分 */
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
}

.runtime-tag {
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: bold;
  text-transform: uppercase;
  border: 1px solid transparent;
  white-space: nowrap;

  /* 标签底纹配色调整 */
  &.tag-cpu { color: $c-sgcc-green; background: rgba(0, 96, 84, 0.1); border-color: rgba(0, 96, 84, 0.2); }
  &.tag-gpu { color: $c-sgcc-orange; background: rgba(245, 130, 32, 0.1); border-color: rgba(245, 130, 32, 0.2); }
  &.tag-llm { color: $c-sgcc-blue; background: rgba(0, 92, 153, 0.1); border-color: rgba(0, 92, 153, 0.2); }
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
}

.metric-val {
  font-weight: bold;
  font-size: 13px;
  font-family: 'Monaco', monospace;
  /* 统一使用国网绿，体现统一性 */
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
