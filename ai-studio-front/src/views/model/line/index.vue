<script setup>
import {
  SearchOutlined,
  TagsOutlined,
  CloseOutlined,
  FolderAddOutlined,
  CloudUploadOutlined,
  AimOutlined,
  RocketOutlined,
  ArrowRightOutlined,
  // Refresh,
  DeleteOutlined,
  PlayCircleOutlined,
  FileTextOutlined,
  NodeIndexOutlined,
  DatabaseOutlined
} from "@ant-design/icons-vue";
import classification from "@/assets/images/dialog2.png";
import target from "@/assets/images/dialog1.png";
import track from "@/assets/images/dialog3.png";
import seg from "@/assets/images/dialog4.png";
import ocr from "@/assets/images/dialogOCR.png";
import time from "@/assets/images/time.png";
import {useRouter} from "vue-router";
import {
  addModelLine,
  changeProductLineStatus,
  deleteModelLineById,
  listModelLine,
} from "@/api/modelMag/modelLine.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {getSonCategory} from "@/api/modelMag/label.js";
import {getChildScene, getAllTaskType, getAllAppScene} from "@/api/modelMag/scene.js";
import { reactive, ref, onMounted } from "vue";
import {SecSceneOptions} from "@/utils/applicationScene.js";

const router = useRouter()
const sceneOpt = ref([])

let parentScene = ref('')

const validateSceneOrApplicationScene = (rule, value, callback) => {
  if (state.formData.appSceneId === null && state.formData.taskTypeId === null) {
    callback(new Error('任务场景和应用场景至少填写一个'));
  } else {
    callback();
  }
}
const state = reactive({
  activeKey: 0,
  modelList: [],
  type: undefined,
  taskTypeList: [],
  appSceneList: [],
  visible: false,
  showProcess: true,
  rules: {
    lineName: [{required: true, trigger: 'change', message: '请输入产线名称'}],
    appSceneId: [{validator: validateSceneOrApplicationScene, trigger: 'change'}]
  },
  formData: {
    lineName: '',
    appSceneId: null,
    taskTypeId: null
  },
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    taskTypeIdEq: undefined,
    lineNameLike: undefined
  },
  total: 0,
  childScene: []
})
const selectedIndex = ref()
const selectedIndex2 = ref()
const modelRef = ref()

const handlePageChange = (page) => {
  state.queryParams.pageNo = page;
  getList();
}

const openCreateModelLineDialog = () => {
  state.visible = true;
}

const createModelLine = () => {
  modelRef.value.validate(valid => {
    if (!valid) return;
    addModelLine({
      "lineName": state.formData.lineName,
      "appSceneId": JSON.stringify(Array.of(state.formData.appSceneId)),
      "taskTypeId": state.formData.taskTypeId
    }).then(res => {
      if (res.code === 200 && res.data) {
        ElMessage({
          type: "success",
          duration: 1000,
          message: '创建成功,3s后自动跳转到调参界面',
          onClose: () => {
            router.push(`/modelmag/model-line-orchestration/orchestration/${res.data}`)
          }
        })
      }
    })

  })
}

const closeModelDialog = () => {
  selectedIndex.value = null;
  state.visible = false;
  state.formData.appSceneId = null;
  modelRef.value.resetFields()
}

const handleSelectCard = (index, item) => {
  selectedIndex.value = index;
  state.formData.appSceneId = item.id;
  // state.subForm.sceneType = item.value;
  modelRef.value.validateField(['appSceneId']);

}

const queryByScene = (val) => {
  state.queryParams.taskTypeIdEq = val
  getList();
}

const getList = () => {
  listModelLine(state.queryParams).then(res => {
    if (res.code === 200) {
      state.modelList = res.data.records;
      state.total = res.data.total;

    }
  })
}

const getTaskTypes = () => {
  getAllTaskType().then(res => {
    if (res.code === 200) {
      state.taskTypeList = res.data;
      sceneOpt.value = res.data.map(item => ({
        ...item,
        title: item.taskName,
        value: item.id,
        description: item.description,
        cover: item.image ? item.image : getCoverImage(item.taskName)
      }));
    }
  })
}

const getAppScenes = () => {
  getAllAppScene().then(res => {
    if (res.code === 200) {
      // 按 industry 分组
      const grouped = res.data.reduce((acc, item) => {
        const industry = item.industry || '其他';
        if (!acc[industry]) {
          acc[industry] = [];
        }
        acc[industry].push(item);
        return acc;
      }, {});

      // 转换为 Segmented 选项格式
      state.appSceneList = Object.keys(grouped).map(industry => ({
        label: industry,
        value: industry,
        children: grouped[industry] // 保存子项以便后续使用
      }));

      if (state.appSceneList.length > 0) {
        parentScene.value = state.appSceneList[0].value;
        // 默认展示第一个分类下的场景
        state.childScene = state.appSceneList[0].children.map(item => ({
            ...item,
            taskTypeIdName: item.appName, // 统一字段名以便展示
            cover: item.image ? item.image : getCoverImage(item.appName),
            relatedTasksId: item.relatedTasksId // 保存关联任务ID
        }));
      }
    }
  })
}

const getCoverImage = (taskName) => {
  if (!taskName) return classification;
  if (taskName.includes('分类')) return classification;
  if (taskName.includes('检测')) return target;
  if (taskName.includes('跟踪') || taskName.includes('追踪')) return track;
  if (taskName.includes('分割')) return seg;
  if (taskName.includes('OCR')) return ocr;
  if (taskName.includes('时序')) return time;
  return classification; // 默认图片
}

const queryByName = () => {
  setTimeout(() => {
    getList()
  }, 1000)
}

const clickLine = (line) => {
  //  0.配置中1.排队中2.运行中3.已停止4.运行完成5.运行失败
  switch (line.status) {
    case 0:
      router.push(`/modelmag/model-line-orchestration/orchestration/${line.id}`);
      break;
    case 1: //
      ElMessage.info('当前产线正在排队中，请等待完成');
      break;
    case 2: //
      router.push(`/modelmag/model-line-detail/index/${line.id}`);
      break;
    case 3: //
      router.push(`/modelmag/model-line-detail/index/${line.id}`);
      break;
    case 4: //
      router.push(`/modelmag/model-line-detail/index/${line.id}`);
      break;
    case 5: //
      router.push(`/modelmag/model-line-detail/index/${line.id}`);
      break;
    default:
      break;
  }
}

const handleOrchestration = (item) => {
  router.push(`/modelmag/model-line-orchestration/orchestration/${item.id}`);
}

// 删除产线
const deleteLine = (item) => {
  ElMessageBox.confirm('确定删除该产线吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteModelLineById({id: item.id}).then(res => {
      if (res.code === 200) {
        getList();
        ElMessage.success('删除成功');
      }
    })
  }).catch(() => {
  });
}

const forkLine = (item) => {
  addModelLine({
    "dataSetId": item.dataSetId,
    "lineName": item.lineName + '_副本',
    "modelId": item.modelId,
    "paramsString": item.paramsString,
    "appSceneId": item.appSceneId
  }).then(res => {
    if (res.code === 200 && res.data) {
      ElMessage.success('fork成功');
      getList();
    }
  })
}

const handleSelectSecScene = (index,val) => {
  selectedIndex2.value = index;
  state.formData.taskTypeId = val.id;

  // 筛选关联的任务场景
  if (val.relatedTasksId) {
      const relatedIds = val.relatedTasksId.split(',').map(Number);
      // 过滤 taskTypeList
      const filteredTasks = state.taskTypeList.filter(task => relatedIds.includes(task.id));

      // 更新 sceneOpt
      sceneOpt.value = filteredTasks.map(item => ({
        ...item,
        title: item.taskName,
        value: item.id,
        description: item.description,
        cover: item.image ? item.image : getCoverImage(item.taskName)
      }));
  } else {
      // 如果没有关联ID，可能显示全部或者清空，这里假设显示全部或者保持原样，
      // 但为了严谨，这里可以重置为全部任务场景
      sceneOpt.value = state.taskTypeList.map(item => ({
        ...item,
        title: item.taskName,
        value: item.id,
        description: item.description,
        cover: item.image ? item.image : getCoverImage(item.taskName)
      }));
  }

  // 自动切换到任务场景 Tab
  activeKey.value = 2;
}

const stopTrainLine = async (item) => {
  const res = await changeProductLineStatus({id: item.id, status: 0});
  if (res.code === 200) {
    ElMessage.success('产线已停止')
    getList();
  }

}

const continueTrainLine = async (item) => {
  const res = await changeProductLineStatus({id: item.id, status: 1});
  if (res.code === 200) {
    ElMessage.success('产线将要继续')
    getList();
  }
}

const openDialog = async () => {
  if (state.appSceneList && state.appSceneList.length > 0) {
    // 如果没有选中值，默认选中第一个
    if (!parentScene.value) {
      parentScene.value = state.appSceneList[0].value;
      handleChangeParentScene(parentScene.value);
    } else {
        // 如果已有选中值，刷新对应的子场景列表
        handleChangeParentScene(parentScene.value);
    }
  } else {
    // 如果列表为空，尝试重新获取
    await getAppScenes();
  }
}
const handleChangeParentScene = (val) => {
  selectedIndex2.value = null;
  state.formData.taskTypeId = null;

  // 在本地数据中查找对应的 industry
  const selectedIndustry = state.appSceneList.find(item => item.value === val);
  if (selectedIndustry && selectedIndustry.children) {
      state.childScene = selectedIndustry.children.map(item => ({
          ...item,
          taskTypeIdName: item.appName,
          cover: item.image ? item.image : getCoverImage(item.appName),
          relatedTasksId: item.relatedTasksId // 确保传递 relatedTasksId
      }));
  } else {
      state.childScene = [];
  }
}

const activeKey = ref(1)
onMounted(() => {
  getList()
  getTaskTypes()
  getAppScenes()
})

const formatDuration = (ms) => {
  if (!ms) return '-';
  const totalSeconds = Math.floor(ms / 1000);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;

  if (hours > 0) {
    return `${hours}时${minutes}分${seconds}秒`;
  } else if (minutes > 0) {
    return `${minutes}分${seconds}秒`;
  } else {
    return `${seconds}秒`;
  }
}

const getRuntimeTagClass = (scene) => {
  // 这里可能需要根据新的scene id逻辑调整，暂时保留默认逻辑或根据实际情况修改
  // 假设scene是ID，无法直接判断，暂时默认cpu
  return 'tag-gpu';
}

const getRuntimeLabel = (scene) => {
   // 同上
  return 'GPU-Base';
}

const getStatusClass = (status) => {
  // 0.配置中1.排队中2.运行中3.已停止4.运行完成5.运行失败
  switch (status) {
    case 2: return 'status-run';
    case 4: return 'status-ok';
    case 5: return 'status-err';
    default: return 'status-run'; // 默认
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '配置中';
    case 1: return '排队中';
    case 2: return '运行中';
    case 3: return '已停止';
    case 4: return 'Success';
    case 5: return 'Failed';
    default: return 'Unknown';
  }
}

const getSceneName = (scene) => {
  // scene现在是ID，需要在taskTypeList中查找
  const found = state.taskTypeList.find(item => item.id === scene);
  return found ? found.taskName : '未知场景';
}

const getSceneIcon = (scene) => {
  const found = state.taskTypeList.find(item => item.id === scene);
  if (found) {
      return found.image ? found.image : getCoverImage(found.taskName);
  }
  return '';
}

const handleTabChange = (key) => {
  if (key === 2 && !state.formData.taskTypeId) {
    ElMessage.warning('请先选择应用场景');
    // 强制切回应用场景
    setTimeout(() => {
      activeKey.value = 1;
    }, 0);
  }
}

</script>

<template>
  <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
    <div class="flex-1 overflow-x-hidden min-w-800">
      <div class="h-full min-w-800">
        <div class="min-w-min h-full py-6 px-8"
             style="background: linear-gradient(180deg, #F7F9FF 0%, #F2F5FE 100%);">

          <!-- Header -->
          <div class="flex items-center justify-between mb-6 m-auto w-full">
            <div class="text-[#140E35] text-3xl font-semibold">模型开发</div>
            <el-button type="primary" size="large" icon="Plus" @click="openCreateModelLineDialog()" v-hasPermi="['modelmag:model-line:add']">
              创建模型
            </el-button>
          </div>

          <!-- Process Flow -->
          <div v-if="state.showProcess" class="w-full bg-white rounded-xl p-6 mb-6 relative shadow-sm border border-gray-100">
            <div class="absolute right-4 top-4 cursor-pointer hover:bg-gray-100 p-1 rounded-full transition-all" @click="state.showProcess = false">
              <CloseOutlined class="text-gray-400 text-lg" />
            </div>
            <div class="font-bold text-lg mb-8 text-[#140E35] flex items-center gap-2">
              <span class="w-1 h-6 bg-blue-600 rounded-full"></span>
              开发流程
            </div>
            <div class="flex justify-around items-start px-4 relative">
              <!-- Step 1 -->
              <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                <div class="w-16 h-16 rounded-2xl bg-blue-50 flex items-center justify-center text-blue-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                  <FolderAddOutlined />
                </div>
                <div class="text-[#140E35] font-medium text-base">创建任务</div>
                <div class="text-gray-400 text-xs text-center">选择场景与模型</div>
              </div>

              <div class="h-16 flex items-center">
                <ArrowRightOutlined class="text-gray-300 text-2xl" />
              </div>

              <!-- Step 2 -->
              <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                  <DatabaseOutlined />
                </div>
                <div class="text-[#140E35] font-medium text-base">数据准备</div>
                <div class="text-gray-400 text-xs text-center">选择训练/评估数据</div>
              </div>

              <div class="h-16 flex items-center">
                <ArrowRightOutlined class="text-gray-300 text-2xl" />
              </div>

              <!-- Step 3 -->
              <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                  <NodeIndexOutlined />
                </div>
                <div class="text-[#140E35] font-medium text-base">流程编排</div>
                <div class="text-gray-400 text-xs text-center">拖拽节点低代码开发</div>
              </div>

              <div class="h-16 flex items-center">
                <ArrowRightOutlined class="text-gray-300 text-2xl" />
              </div>

              <!-- Step 4 -->
              <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                  <RocketOutlined />
                </div>
                <div class="text-[#140E35] font-medium text-base">训练评估</div>
                <div class="text-gray-400 text-xs text-center">模型训练与效果评估</div>
              </div>
            </div>
          </div>

          <!-- Search & Filter Bar -->
          <div class="w-full bg-white rounded-lg p-4 mb-6 shadow-sm flex flex-col gap-4">
            <div class="flex justify-between items-center border-b pb-2">
              <a-tabs v-model:active-key="state.activeKey" class="custom-tabs">
                <a-tab-pane tab="我创建的" :key="0"/>
              </a-tabs>
            </div>

            <div class="flex items-center gap-4 flex-wrap">
              <div class="flex items-center gap-2">
                <span class="text-gray-500 text-sm">场景类型:</span>
                <a-select
                    v-model:value="state.type"
                    @change="queryByScene"
                    style="width: 160px"
                    placeholder="全部类型"
                    allowClear
                >
                  <a-select-option :value="undefined">全部类型</a-select-option>
                  <a-select-option v-for="item in state.taskTypeList" :key="item.id" :value="item.id">{{ item.taskName }}</a-select-option>
                </a-select>
              </div>

              <div class="flex items-center gap-2 ml-auto">
                <a-input
                    placeholder="搜索任务名称"
                    v-model:value="state.queryParams.lineNameLike"
                    style="width: 240px"
                    @pressEnter="queryByName"
                >
                  <template #prefix>
                    <SearchOutlined class="text-gray-400"/>
                  </template>
                </a-input>
                <el-button type="primary" @click="getList">搜索</el-button>
              </div>
            </div>
          </div>

          <!-- Task List -->
          <div class="w-full m-auto" v-if="state.activeKey===0 && state.total>0">
            <div class="grid gap-4">
              <div
                  class="bg-white rounded-lg p-5 hover:shadow-lg transition-all duration-300 border border-gray-100 cursor-pointer group"
                  v-for="item in state.modelList"
                  :key="item.id"
                  @click="clickLine(item)"
              >
                <div class="flex justify-between items-start">
                  <!-- Left: Icon & Info -->
                  <div class="flex gap-5 flex-1">
                    <!-- Icon -->
                    <div class="w-14 h-14 rounded-xl bg-blue-50 flex items-center justify-center text-blue-600 text-2xl flex-shrink-0 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300 overflow-hidden p-2">
                      <img :src="getSceneIcon(item.appSceneId)" class="w-full h-full object-contain" v-if="getSceneIcon(item.appSceneId)"/>
                      <FileTextOutlined v-else />
                    </div>

                    <!-- Info -->
                    <div class="flex flex-col justify-between flex-1">
                      <div>
                        <div class="flex items-center gap-3 mb-1">
                          <div class="text-lg font-bold text-gray-800 group-hover:text-blue-600 transition-colors">{{ item.lineName }}</div>
                          <span class="status-badge" :class="getStatusClass(item.status)">
                                                        <span class="status-dot" style="background:currentColor"></span>
                                                        {{ getStatusText(item.status) }}
                                                    </span>
                        </div>
                        <div class="text-gray-500 text-sm line-clamp-1 mb-2">
                          ID: {{ item.id }}
                        </div>
                      </div>

                      <div class="flex items-center gap-4 text-xs text-gray-400">
                        <div class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                          <TagsOutlined />
                          <span>{{ getSceneName(item.taskTypeId) }}</span>
                        </div>
                        <div class="flex items-center gap-1 bg-gray-50 px-2 py-1 rounded">
                                                    <span class="runtime-tag" :class="getRuntimeTagClass(item.appSceneId)">
                                                        {{ getRuntimeLabel(item.appSceneId) }}
                                                    </span>
                        </div>
                        <div>创建于: {{ item.createTime }}</div>
                      </div>
                    </div>
                  </div>

                  <!-- Right: Actions -->
                  <div class="flex flex-col items-end justify-center h-full gap-4 min-w-[150px]">
                    <div class="text-gray-400 text-xs mb-auto">
                      耗时: {{ formatDuration(item.trainTime) }}
                    </div>

                    <div class="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                      <el-button
                          type="primary"
                          link
                          icon="PlayCircleOutlined"
                          @click.stop="handleOrchestration(item)"
                      >
                        编排
                      </el-button>
                      <el-button
                          type="primary"
                          link
                          icon="FileTextOutlined"
                          @click.stop="clickLine(item)"
                      >
                        日志
                      </el-button>
                      <el-button
                          type="danger"
                          link
                          icon="DeleteOutlined"
                          @click.stop="deleteLine(item)"
                          v-hasPermi="['modelmag:model-line:remove']"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="flex justify-center mt-6">
              <a-pagination
                  v-model:current="state.queryParams.pageNo"
                  :total="state.total"
                  show-less-items
                  :page-size="state.queryParams.pageSize"
                  @change="handlePageChange"
              />
            </div>
          </div>

          <div class="w-full m-auto py-20" v-else>
            <a-empty description="暂无模型任务"></a-empty>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建弹窗 -->
    <el-dialog
        @open="openDialog"
        @close="closeModelDialog"
        width="800px"
        v-model="state.visible"
        class="custom-dialog"
        destroy-on-close
    >
      <template #header>
        <div class="dialog-header">
          创建模型任务
        </div>
      </template>
      <div class="dialog-content">
        <el-form ref="modelRef" :model="state.formData" label-position="top" :rules="state.rules">
          <el-form-item label="任务名称" prop="lineName">
            <el-input v-model="state.formData.lineName" placeholder="请输入任务名称" size="large"></el-input>
          </el-form-item>

          <el-form-item label="场景选择" prop="appSceneId" class="scene-form-item">
            <template #label>
              <div class="scene-tabs">
                <a-tabs v-model:active-key="activeKey" type="card" @change="handleTabChange">
                  <a-tab-pane tab="应用场景" :key="1"/>
                  <a-tab-pane tab="任务场景" :key="2"/>
                </a-tabs>
              </div>
            </template>

            <div class="scene-selection-area">
              <!-- 应用场景 -->
              <div v-if="activeKey===1" class="w-full">
                <a-segmented v-model:value="parentScene" :options="state.appSceneList"
                             @change="handleChangeParentScene"
                             class="mb-4 w-full"
                             block>
                </a-segmented>

                <div class="scene-grid">
                  <div
                      v-for="(item,index) in state.childScene"
                      :key="item.id"
                      class="scene-card"
                      :class="{ 'active': selectedIndex2 === index }"
                      @click="handleSelectSecScene(index,item)"
                  >
                    <div class="check-mark" v-if="selectedIndex2 === index">
                      <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 16 16" fill="none">
                        <path d="M4 7 3 8l4 4 6-7-1-1-5 6-3-3Z" fill="#fff"></path>
                      </svg>
                    </div>
                    <img :src="item.cover" class="scene-card-icon">
                    <div class="scene-card-info">
                      <div class="scene-card-title">{{ item.taskTypeIdName}}</div>
                      <div class="scene-card-desc" :title="item.description">{{ item.description }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 任务场景 -->
              <div v-if="activeKey===2" class="w-full">
                <!-- 占位符，保持与应用场景一致的布局结构 -->
                <div class="mb-4 w-full h-[32px]"></div>

                <div class="scene-grid">
                  <div
                      v-for="(item,index) in sceneOpt"
                      :key="item.value"
                      class="scene-card"
                      :class="{ 'active': selectedIndex === index }"
                      @click="handleSelectCard(index,item)"
                  >
                    <div class="check-mark" v-if="selectedIndex === index">
                      <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 16 16" fill="none">
                        <path d="M4 7 3 8l4 4 6-7-1-1-5 6-3-3Z" fill="#fff"></path>
                      </svg>
                    </div>
                    <img :src="item.cover" class="scene-card-icon">
                    <div class="scene-card-info">
                      <div class="scene-card-title">{{ item.title }}</div>
                      <div class="scene-card-desc" :title="item.description">{{ item.description }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeModelDialog">取消</el-button>
          <el-button type="primary" @click="createModelLine">立即创建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
:deep(.custom-tabs .ant-tabs-nav) {
  margin-bottom: 0;
}
:deep(.custom-tabs .ant-tabs-tab) {
  padding: 12px 0;
  margin-right: 24px;
}

.hover-shadow:hover {
  box-shadow: 0px 12px 40px 0px rgba(181, 199, 235, 0.5);
}

/* 运行时标签 */
.runtime-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 600;
  border: 1px solid transparent;
}

.tag-cpu {
  color: #16a34a;
  background: #dcfce7;
  border-color: #bbf7d0;
}

.tag-gpu {
  color: #dc2626;
  background: #fee2e2;
  border-color: #fecaca;
}

/* 状态标签 */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-run {
  color: #1677ff;
}

.status-ok {
  color: #52c41a;
}

.status-err {
  color: #ff4d4f;
}

/* 弹窗样式 */
.dialog-header {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  color: #1f2937;
}

.scene-selection-area {
  margin-top: 16px;
  max-height: 400px;
  overflow-y: auto;
  padding: 4px;
  width: 100%;
  box-sizing: border-box;
}

.scene-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.scene-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.scene-card:hover {
  border-color: #1677ff;
  box-shadow: 0 4px 12px rgba(22, 119, 255, 0.1);
}

.scene-card.active {
  border-color: #1677ff;
  background: #f0f7ff;
}

.check-mark {
  position: absolute;
  top: 0;
  right: 0;
  background: #1677ff;
  color: white;
  width: 20px;
  height: 20px;
  border-bottom-left-radius: 8px;
  border-top-right-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.scene-card-icon {
  width: 48px;
  height: 48px;
  margin-right: 16px;
  flex-shrink: 0;
}

.scene-card-info {
  flex: 1;
  overflow: hidden;
}

.scene-card-title {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
  font-size: 14px;
}

.scene-card-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 覆盖 Ant Design Tabs 样式 */
:deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

.scene-form-item {
  width: 100%;
}
.scene-form-item :deep(.el-form-item__content) {
  width: 100%;
  display: block;
}
</style>