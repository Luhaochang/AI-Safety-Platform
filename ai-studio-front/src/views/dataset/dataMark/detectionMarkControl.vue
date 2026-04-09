<!--目标检测标注控制台-->
<script setup>
import {onMounted, ref, computed} from 'vue'
import MarkCanvas from '@/components/MarkCanvas/src'
import draggable from 'vuedraggable'
import {useRouter} from "vue-router";
import { ArrowLeftOutlined, InfoCircleOutlined, DragOutlined, SelectOutlined, BorderOutlined, GatewayOutlined, DeleteOutlined, SaveOutlined, CheckOutlined, FileImageOutlined } from "@ant-design/icons-vue";
import JSZip from "jszip";
import {ElMessage, ElMessageBox} from "element-plus";
import {getTaskById, savaTask, submitTask} from "@/api/dataMag/markTask.js";
import {getDatasetById, getDatasetByPath} from "@/api/dataMag/dataset.js";

const router = useRouter()
const state = reactive({
    imgList: [],
    labelInfo: {
        status: 0,
        labelInput: '',
        colorInput: '#409EFF'
    },
    labelList: [],
    curImg: {},
    c_task: {
        id: null,
        label: [],
        datasetId: null,
        taskName: null,
        finishNumber: 0,
        setNumber: 0,
    },
    pageNo: 1,
    pageSize: 20,
})

let moveStatus = ref(false)
let drawType = ref('')
let mark = ref(null)
let selectStatus = ref(true)

const selectLabel = (item) => {
    state.labelInfo.colorInput = item.color;
    state.labelInfo.labelInput = item.name;
}

function setDrawType(type) {
    if (state.imgList.length === 0){
        ElMessage.info('当前标注任务暂无数据集');
        return;
    }
    mark.value?.setDrawType(type)
}

let showLabel = ref(false)

let labelCallback = null


function clearLabel() {
    labelCallback(null)
    showLabel.value = false
}

function enterLabel() {
    // 为空判断
    if (!state.labelInfo.labelInput) return alert('请输入标签')
    // 为空判断
    if (!state.labelInfo.colorInput) return alert('请输入颜色')

    labelCallback({
        label: state.labelInfo.labelInput,
        color: state.labelInfo.colorInput
    })
    showLabel.value = false
}


function showLabelInput(callback, data) {
    labelCallback = callback
    showLabel.value = true
    state.labelInfo.labelInput = data ? data.label : state.labelInfo.labelInput
    state.labelInfo.colorInput = data ? data.color : state.labelInfo.colorInput
}

let objectList = ref([])

onMounted(() => {
    let taskID = router.currentRoute.value.params.taskId;
    state.c_task.id = taskID;

    mark.value = new MarkCanvas({
        view: "mark-box",
        fill: "#f7f8fa", // A lighter, more neutral background
    })

    initData(taskID)

    // Event listeners
    mark.value.app.on("onmove", (e) => { moveStatus.value = e.status })
    mark.value.app.on("onselect", (e) => { selectStatus.value = e.status })
    mark.value.app.on("ondraw", (e) => { drawType.value = e.type })
    mark.value.app.on("oncomplete", (e) => {
        showLabelInput(function (data) {
            if (data) e.ok(data); else e.err();
        })
    })
    mark.value.app.on("onchange", () => {
        objectList.value = JSON.parse(JSON.stringify(mark.value.objects));
        if (state.curImg) {
            const img = state.imgList.find(i => state.curImg.id === i.id);
            if (img) img.json = objectList.value;
        }
    })
})

const formatType = (type) => {
  return type === 'rect' ? '矩形' : type === 'polygon' ? '多边形' : '未知';
}

function importJSON(json) {
    try {
        let new_json = json.map((item, index) => ({
            id: index,
            type:  item.type,
            label: item.label,
            color: item.color,
            pointList: item.pointList
        }));
        mark.value?.setObjectData(new_json)
    } catch (err) {
        console.error("JSON格式错误", err);
    }
}

function clearCanvas() {
    mark.value?.clear()
    drawType.value = ''
}

async function loadImageList(url) {
    state.imgList = [];
    const response = await getDatasetByPath({catalogue: `${url}/images`});
    const zipData = response.data;

    for (let i = 0; i < zipData.length; i++) {
        let tempName = zipData[i].split('/').pop();
        if (/\.(png|jpg|jpeg)$/i.test(tempName)) {
            state.imgList.push({name: tempName, url: zipData[i], id: i, json: [], width: 0, height: 0, yoloText: null});
        }
    }
}

var dragStatus = ref(false)

function stopDrag() {
    mark.value?.sortObject(objectList.value.map(e => e.id))
}

function selectObj(id) {
    mark.value?.selectObjectById(id)
}

function setLabel(e, el) {
    e.preventDefault()
    showLabelInput(function (data) {
        if (data) {
            mark.value?.setObjectLabel(el.id, data)
        }
    }, el)
}

const goback = () => {
    router.go(-1);
}

const changeImg = (obj) => {
    clearCanvas();
    objectList.value = [];
    state.curImg = obj;

    const parseAndLoad = () => {
        // If we have raw YOLO text but no parsed JSON yet, parse it now
        if (obj.yoloText && (!obj.json || obj.json.length === 0)) {
            const lines = obj.yoloText.split('\n').filter(line => line.trim() !== '');
            const annotations = lines.map(line => {
                const parts = line.trim().split(/\s+/);
                const classId = parseInt(parts[0]);
                const nCx = parseFloat(parts[1]);
                const nCy = parseFloat(parts[2]);
                const nW = parseFloat(parts[3]);
                const nH = parseFloat(parts[4]);

                const labelInfo = state.labelList[classId];
                if (!labelInfo) return null;

                const w = nW * obj.width;
                const h = nH * obj.height;
                const cx = nCx * obj.width;
                const cy = nCy * obj.height;

                const x1 = cx - w / 2;
                const y1 = cy - h / 2;
                const x2 = cx + w / 2;
                const y2 = cy + h / 2;

                return {
                    type: 'rect',
                    label: labelInfo.name,
                    color: labelInfo.color,
                    pointList: [{x: x1, y: y1}, {x: x2, y: y2}]
                };
            }).filter(Boolean);

            obj.json = annotations;
        }
        importJSON(obj.json);
    };

    const loadImageOnCanvas = () => {
        mark.value.setBackground(obj.url);
        // Wait a tick for background to set? Usually synchronous enough for data
        setTimeout(parseAndLoad, 50);
    };

    if (!obj.width || !obj.height) {
        const img = new Image();
        img.onload = () => {
            obj.width = img.width;
            obj.height = img.height;
            loadImageOnCanvas();
        };
        img.src = obj.url;
    } else {
        loadImageOnCanvas();
    }
}

const computeCount = (list) => {
    return list.filter(item => (item.json && item.json.length > 0) || item.yoloText).length;
}

const generateYoloContent = (img) => {
    if (!img.width || !img.height) return '';

    return img.json.map(shape => {
        if (shape.type !== 'rect') return null;
        const classIndex = state.labelList.findIndex(l => l.name === shape.label);
        if (classIndex === -1) return null;

        const x1 = Math.min(shape.pointList[0].x, shape.pointList[1].x);
        const y1 = Math.min(shape.pointList[0].y, shape.pointList[1].y);
        const x2 = Math.max(shape.pointList[0].x, shape.pointList[1].x);
        const y2 = Math.max(shape.pointList[0].y, shape.pointList[1].y);

        const w = x2 - x1;
        const h = y2 - y1;
        const cx = x1 + w / 2;
        const cy = y1 + h / 2;

        const nCx = cx / img.width;
        const nCy = cy / img.height;
        const nW = w / img.width;
        const nH = h / img.height;

        return `${classIndex} ${nCx.toFixed(6)} ${nCy.toFixed(6)} ${nW.toFixed(6)} ${nH.toFixed(6)}`;
    }).filter(line => line !== null).join('\n');
}

const createFormData = () => {
    const formData = new FormData();

    state.imgList.forEach(img => {
        if (img.json.length > 0) {
            const yoloContent = generateYoloContent(img);
            if (yoloContent) {
                const fileName = img.name.substring(0, img.name.lastIndexOf('.')) + '.txt';
                const txtFile = new File([yoloContent], fileName, { type: 'text/plain' });
                formData.append('file', txtFile);
            }
        }
    });
    return formData;
}

const saveUpdate = async () => {
    const formData = createFormData();
    let count = computeCount(state.imgList);
    savaTask(state.c_task.id, count, formData).then(res => {
        if (res.code === 200) ElMessage.success('保存成功');
    });
}

const getTaskByTaskId = async (id) => {
   const {data,code} = await getTaskById(id);
   if (code === 200) return data;
}

const getDataset = async (id) => {
  const {data,code} = await getDatasetById(id);
  if (code === 200 && data) return {fileUrl: data.fileUrl, markUrl: data.markUrl};
}

function transformJsonList(list) {
    return list.map((item, index) => {
        const { info, tags: { detection_tags: { shapes } } } = item;
        const { md5, path } = info;

        const marks = shapes.map(shape => ({
            color: shape.tag[0].label_key_name,
            label: shape.tag[0].label_value_name[0],
            pointList: [
                { x: shape.pts[0], y: shape.pts[1] },
                { x: shape.pts[2], y: shape.pts[3] }
            ],
            type: shape.frame_type
        }));

        return { id: index, name: md5, url: path, json: marks };
    });
}

const loadMarkedJson = async (url) => {
    // 1. Try loading from 'labels' directory (YOLO format)
    try {
        const res = await getDatasetByPath({ catalogue: `${url}/labels` });
        if (res.code === 200 && res.data && res.data.length > 0) {
            const txtFileUrls = res.data.filter(u => u.endsWith('.txt'));

            // Fetch all text files
            const filePromises = txtFileUrls.map(fileUrl =>
                fetch(fileUrl).then(response => {
                    if (!response.ok) throw new Error(`Failed to fetch ${fileUrl}`);
                    return response.text();
                }).then(text => ({
                    url: fileUrl,
                    content: text
                }))
            );

            const fileContents = await Promise.all(filePromises);

            fileContents.forEach(({ url, content }) => {
                const fileName = url.split('/').pop();
                const imgNameBase = fileName.substring(0, fileName.lastIndexOf('.'));

                // Find corresponding image by name (ignoring extension)
                const img = state.imgList.find(i => i.name.substring(0, i.name.lastIndexOf('.')) === imgNameBase);
                if (img) {
                    img.yoloText = content;
                }
            });
            // If we found labels, we can return early or continue to check for legacy
            // But usually if labels exist, we use them.
            return;
        }
    } catch (e) {
        console.log("No 'labels' directory found or error reading it, checking legacy formats.", e);
    }

    // 2. Fallback: Legacy annotations.json
    try {
        const res = await getDatasetByPath({ catalogue: url });
        const jsonFile = res.data.find(u => u.endsWith('annotations.json'));
        if (jsonFile) {
            const response = await fetch(jsonFile);
            const data = await response.json();
            const temp = transformJsonList(data);
            state.imgList.forEach(item => {
                const obj = temp.find(i => i.name === item.name);
                if (obj) {
                    item.json = obj.json;
                }
            });
        }
    } catch (e) {
        console.error("Error loading legacy annotations.json", e);
    }
};

const initData = async (id) => {
    const taskData = await getTaskByTaskId(id);
    if (taskData) {
        state.c_task = {
            id: taskData.id,
            label: taskData.label ? JSON.parse(taskData.label) : [],
            datasetId: taskData.dataSetId,
            taskName: taskData.taskName,
            finishNumber: taskData.finishNumber || 0,
            setNumber: taskData.setNumber || 0,
        };
        state.labelList = state.c_task.label;
        const {fileUrl} = await getDataset(taskData.dataSetId);
        await loadImageList(fileUrl);
        if (fileUrl) {
            await loadMarkedJson(fileUrl);
        }
        // Finally, load the first image
        if (state.imgList.length > 0) {
            changeImg(state.imgList[0]);
        }
    }
}

const finish = () => {
    ElMessageBox.confirm('当前操作不可逆，确定提交该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        const formData = createFormData();
        submitTask(state.c_task.id, computeCount(state.imgList), formData).then(res => {
            if (res.code === 200) {
                router.go(-1);
                ElMessage.success('提交成功');
            }
        });
    }).catch(() => {});
}

const currentPageImages = computed(() => {
    const start = (state.pageNo - 1) * state.pageSize;
    const end = start + state.pageSize;
    return state.imgList.slice(start, end);
});

const handlePageChange = (val) => {
    state.pageNo = val;
    // Select first image of the new page
    const firstIndex = (val - 1) * state.pageSize;
    if (firstIndex < state.imgList.length) {
        changeImg(state.imgList[firstIndex]);
    }
};

</script>

<template>
    <div class="flex flex-col bg-white" style="height: calc(100vh - 84px); overflow: hidden;">
        <!-- Header -->
        <div class="h-16 bg-white border-b flex items-center justify-between px-6 shadow-sm flex-shrink-0 z-10">
            <div class="flex items-center gap-4">
                <el-button link class="!text-gray-600 hover:!text-blue-600" @click="goback">
                    <el-icon class="mr-1"><ArrowLeftOutlined /></el-icon> 返回
                </el-button>
                <div class="h-6 w-px bg-gray-200"></div>
                <div class="flex flex-col">
                    <span class="font-semibold text-gray-800 text-lg">{{ state.c_task.taskName }}</span>
                    <span class="text-xs text-gray-400">目标检测标注任务</span>
                </div>
            </div>
            <div class="flex items-center gap-6">
                <div class="flex flex-col items-end">
                    <div class="text-xs text-gray-500 mb-1">标注进度</div>
                    <div class="flex items-center gap-2">
                        <span class="font-mono font-bold text-blue-600">{{ state.c_task.finishNumber }}</span>
                        <span class="text-gray-300">/</span>
                        <span class="text-gray-600">{{ state.c_task.setNumber }}</span>
                    </div>
                </div>
                <el-progress
                    type="circle"
                    :percentage="state.c_task.setNumber > 0 ? Math.round((state.c_task.finishNumber / state.c_task.setNumber) * 100) : 0"
                    :width="40"
                    :stroke-width="4"
                    :show-text="false"
                />
                <div class="h-8 w-px bg-gray-200 mx-2"></div>
                <div class="flex gap-3">
                    <el-button plain @click="saveUpdate">
                        <template #icon><SaveOutlined /></template>
                        保存进度
                    </el-button>
                    <el-button type="primary" @click="finish">
                        <template #icon><CheckOutlined /></template>
                        提交任务
                    </el-button>
                </div>
            </div>
        </div>

        <!-- Main Content -->
        <div class="flex-1 flex overflow-hidden">
            <!-- Left: Image List -->
            <div class="w-64 bg-white border-r flex flex-col flex-shrink-0 z-10 shadow-lg">
                <div class="p-4 border-b bg-gray-50/50 flex justify-between items-center flex-shrink-0">
                    <span class="font-medium text-gray-700">图片列表</span>
                    <el-tag size="small" type="info">{{ state.imgList.length }} 张</el-tag>
                </div>
                <div class="flex-1 overflow-y-auto custom-scrollbar">
                    <div
                        v-for="(item, index) in currentPageImages"
                        :key="index"
                        class="p-3 border-b border-gray-50 cursor-pointer hover:bg-blue-50 transition-all flex items-center gap-3"
                        :class="{'bg-blue-50 border-l-4 border-l-blue-500': state.curImg.id === item.id, 'border-l-4 border-l-transparent': state.curImg.id !== item.id}"
                        @click="changeImg(item)"
                    >
                        <div class="w-12 h-12 rounded bg-gray-100 flex-shrink-0 overflow-hidden border border-gray-200">
                            <img :src="item.url" class="w-full h-full object-cover" loading="lazy" />
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="text-sm text-gray-700 truncate mb-1" :title="item.name">{{ item.name }}</div>
                            <div class="flex items-center gap-2">
                                <el-tag v-if="(item.json && item.json.length > 0) || item.yoloText" type="success" size="small" effect="dark" class="scale-90 origin-left">已标注</el-tag>
                                <el-tag v-else type="info" size="small" effect="plain" class="scale-90 origin-left">未标注</el-tag>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- Pagination -->
                <div class="p-2 border-t bg-gray-50/50 flex justify-center flex-shrink-0">
                    <el-pagination
                        small
                        layout="prev, pager, next"
                        :total="state.imgList.length"
                        :page-size="state.pageSize"
                        v-model:current-page="state.pageNo"
                        @current-change="handlePageChange"
                    />
                </div>
            </div>

            <!-- Center: Toolbar & Canvas -->
            <div class="flex-1 flex flex-col bg-gray-50 relative min-w-0">
                <!-- Toolbar -->
                <div class="absolute top-4 left-1/2 transform -translate-x-1/2 z-10 bg-white rounded-lg shadow-lg p-2 flex gap-2 border border-gray-200">
                    <el-tooltip content="移动画布 (Space)" placement="bottom">
                        <button class="tool-btn" :class="{ active: moveStatus }" @click="mark?.setMoveMode(true)">
                            <DragOutlined />
                        </button>
                    </el-tooltip>
                    <el-tooltip content="选择对象" placement="bottom">
                        <button class="tool-btn" :class="{ active: selectStatus }" @click="mark?.setSelectMode(!selectStatus)">
                            <SelectOutlined />
                        </button>
                    </el-tooltip>
                    <div class="w-px bg-gray-200 mx-1"></div>
                    <el-tooltip content="绘制矩形" placement="bottom">
                        <button class="tool-btn" :class="{ active: drawType === 'rect' }" @click="setDrawType('rect')">
                            <BorderOutlined />
                        </button>
                    </el-tooltip>
                    <el-tooltip content="绘制多边形" placement="bottom">
                        <button class="tool-btn" :class="{ active: drawType === 'polygon' }" @click="setDrawType('polygon')">
                            <GatewayOutlined />
                        </button>
                    </el-tooltip>
                    <div class="w-px bg-gray-200 mx-1"></div>
                    <el-tooltip content="清空画布" placement="bottom">
                        <button class="tool-btn text-red-500 hover:bg-red-50" @click="clearCanvas">
                            <DeleteOutlined />
                        </button>
                    </el-tooltip>
                </div>

                <!-- Canvas Area -->
                <div class="flex-1 overflow-hidden relative bg-gray-50 p-4">
                    <div id="mark-box" class="w-full h-full bg-white rounded-lg shadow-inner border border-gray-200"></div>
                </div>
            </div>

            <!-- Right: Object List -->
            <div class="w-72 bg-white border-l flex flex-col flex-shrink-0 z-10 shadow-lg">
                <div class="p-4 border-b bg-gray-50/50 flex justify-between items-center">
                    <span class="font-medium text-gray-700">标注对象列表</span>
                    <el-tag size="small" type="info">{{ objectList.length }} 个对象</el-tag>
                </div>

                <div class="flex-1 overflow-y-auto p-2 custom-scrollbar">
                    <draggable v-model="objectList" group="people" @start="dragStatus = true" @end="stopDrag" item-key="id">
                        <template #item="{ element }">
                            <div
                                @click="selectObj(element.id)"
                                class="group flex items-center gap-3 p-3 mb-2 rounded-lg border border-gray-100 cursor-pointer hover:border-blue-300 hover:bg-blue-50 transition-all"
                                :class="{ 'bg-blue-50 border-blue-500 ring-1 ring-blue-500': element.select }"
                                @contextmenu="(e) => setLabel(e, element)"
                            >
                                <div class="w-3 h-3 rounded-full flex-shrink-0" :style="{ background: element.color }"></div>
                                <div class="flex-1 truncate text-sm font-medium text-gray-700">{{ element.label }}</div>
                                <div class="text-xs text-gray-400">{{ formatType(element.type) }}</div>
                            </div>
                        </template>
                    </draggable>
                    <div v-if="objectList.length === 0" class="flex flex-col items-center justify-center h-40 text-gray-400 text-sm">
                        <el-empty description="暂无标注对象" :image-size="60" />
                    </div>
                </div>

                <div class="p-4 border-t bg-gray-50/30">
                    <div class="text-xs text-gray-500 mb-2 flex items-center gap-1">
                        <InfoCircleOutlined /> 操作提示
                    </div>
                    <div class="text-xs text-gray-400 leading-relaxed">
                        1. 鼠标滚轮缩放画布<br>
                        2. 按住空格键拖动画布<br>
                        3. 右键点击对象修改标签<br>
                        4. Delete键删除选中对象
                    </div>
                </div>
            </div>
        </div>

        <!-- Label Dialog -->
        <el-dialog
            v-model="showLabel"
            title="选择标签"
            width="400px"
            destroy-on-close
            center
        >
            <el-form :model="state.labelInfo" label-position="top">
                <el-form-item label="标签类别">
                    <el-select
                        v-model="state.labelInfo.labelInput"
                        @change="selectLabel"
                        value-key="label"
                        placeholder="请选择标签"
                        class="w-full"
                    >
                        <el-option
                            v-for="item in state.labelList"
                            :label="item.name"
                            :value="item"
                            :key="item.name"
                        >
                            <div class="flex items-center gap-2">
                                <div class="w-3 h-3 rounded-full" :style="{background: item.color}"></div>
                                <span>{{ item.name }}</span>
                            </div>
                        </el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="标签颜色">
                    <div class="flex items-center gap-3">
                        <el-color-picker v-model="state.labelInfo.colorInput" disabled />
                        <span class="text-gray-500 text-sm">{{ state.labelInfo.colorInput }}</span>
                    </div>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="flex justify-end gap-3">
                    <el-button @click="clearLabel">取消</el-button>
                    <el-button type="primary" @click="enterLabel">确定</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<style scoped>
.tool-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    color: #666;
    transition: all 0.2s;
    font-size: 16px;
}

.tool-btn:hover {
    background-color: #f3f4f6;
    color: #333;
}

.tool-btn.active {
    background-color: #eff6ff;
    color: #3b82f6;
}

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #e2e8f0;
    border-radius: 3px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #cbd5e1;
}
</style>
