<!--目标检测标注控制台-->
<script setup>
import {onMounted, ref} from 'vue'
import MarkCanvas from '@/components/MarkCanvas/src'
import bgImage from '@/assets/images/login-background.jpg'
import draggable from 'vuedraggable'
import {useRouter} from "vue-router";
import {UploadOutlined} from "@ant-design/icons-vue";
import JSZip from "jszip";
import Magnify from "@/views/dataset/components/magnify.vue";
import {MarkObjectType} from "@/components/MarkCanvas/src-js/object/index.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {getTaskById, savaTask, submitTask} from "@/api/dataMag/markTask.js";
import {getDatasetById, getDatasetByPath} from "@/api/dataMag/dataset.js";

const zip = new JSZip();
const router = useRouter()
const state = reactive({
    imgList: [],
    labelInfo: {
        status: 0,
        labelInput: '',
        colorInput: '#409EFF'
    },
    labelList: [
        {
            name: '标签1',
            value: 0,
            color: '#409EFF'
        },
        {
            name: '标签2',
            value: 1,
            color: '#409EFF'
        },
        {
            name: '标签3',
            value: 2,
            color: '#409EFF'
        },
        {
            name: '标签4',
            value: 3,
            color: '#409EFF'
        },
        {
            name: '标签5',
            value: 4,
            color: '#409EFF'
        },

    ],
    curImg: {},
    c_task: {
        id: null,
        label: [],
        datasetId: null,
        taskName: null
    }
})

const statusOpt = [
    {
        label: '全部数据',
        value: 0
    },
    {
        label: '已标注',
        value: 1
    },
    {
        label: '未标注',
        value: 2
    },
]


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
        fill: "",
    })

    initZip(taskID)

    // 画布移动状态监听  同步修改工具栏移动状态
    mark.value.app.on("onmove", (e) => {
        moveStatus.value = e.status
    })

    // 画布选中状态监听  同步修改工具栏选中状态
    mark.value.app.on("onselect", (e) => {
        selectStatus.value = e.status
    })

    // 画布绘制模式监听  同步修改工具栏绘制模式
    mark.value.app.on("ondraw", (e) => {
        drawType.value = e.type
    })

    // 标注对象完成通知
    mark.value.app.on("oncomplete", (e) => {
        showLabelInput(function (data) {
            if (data) {
                e.ok(data)
            } else {
                e.err()
            }
        })
    })

    // 监听标注对象变化
    mark.value.app.on("onchange", () => {
        // mark.value?.setObjectData(objectList.value)

        objectList.value = JSON.parse(JSON.stringify(mark.value.objects))
        // if (objectList.value.length !== 0) {
            state.imgList.filter(i => state.curImg.id === i.id)[0].json = objectList.value
        // }
    })

})

function toJSON() {
    let json = mark.value.toJSON()
}


const formatType = (type) => {
  switch (type) {
      case '矩形':
          return 'rect';
      case '多边形':
          return 'polygon';
      default:
          return ''
  }
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
        alert("JSON格式错误")
    }
}

function clearCanvas() {
    mark.value?.clear()
    drawType.value = ''
}

async function loadImage(url) {
    state.imgList = [];

    const response = await getDatasetByPath({catalogue: `${url}/images`});

    const zipData = response.data;

    let index = 0;
    let result = {
        blobArr: [],
        data: {}
    };
    for (let key in zipData) {
        let tempName = zipData[key].split('/').pop();
        if (tempName.indexOf('.png') !== -1 || tempName.indexOf('.jpg') !== -1) {

            result.blobArr.push({name: tempName, url: zipData[key], label: '', id: index,json: []})
            index++;
        }
        //json文件转为字符串
        if (tempName.indexOf('.json') !== -1) {
            //@ts-ignore
            let jsonString = await zipData.file(tempName).async("string");
            result.data = JSON.parse(jsonString);
            //后面还发现貌似有asnyc('json')方法直接得到json格式，可能就不用走以上两步了
        }
    }
    state.imgList = result.blobArr;

    if (state.imgList.length !== 0 ) {
        state.curImg = state.imgList[0];
        mark.value.setBackground(state.imgList[0].url);
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
    importJSON(obj.json)
    mark.value.setBackground(obj.url);
}

function transformImgList(imglist) {
    return imglist.map(img => {
        const shapes = img.json.map(shape => {
            // const frameType = shape.type === 'rect' ? '矩形' : shape.type === 'polyline' ? '折线' : '多边形';
            let pts = [];
            let bbox = [];

            if (shape.type === 'rect') {
                pts = [shape.pointList[0].x, shape.pointList[0].y, shape.pointList[1].x, shape.pointList[1].y];
                bbox = [shape.pointList[0].x, shape.pointList[0].y, Math.abs(shape.pointList[1].x - shape.pointList[0].x), Math.abs(shape.pointList[1].y - shape.pointList[0].y)];
            } else if (shape.type === 'polyline' || shape.type === 'polygon') {
                pts = shape.pointList.map(p => [p.x, p.y]).flat();
                // 对于多边形和折线，bbox计算可能需要更复杂的逻辑，这里简单使用所有点的最小最大边界
                const minX = Math.min(...shape.pointList.map(p => p.x));
                const minY = Math.min(...shape.pointList.map(p => p.y));
                const maxX = Math.max(...shape.pointList.map(p => p.x));
                const maxY = Math.max(...shape.pointList.map(p => p.y));
                bbox = [minX, minY, maxX - minX, maxY - minY];
            }

            return {
                frame_type: shape.type,
                pts: pts,
                bbox: bbox,
                tag: [{
                    label_key_name: shape.color,
                    label_value_name: [shape.label]
                }]
            };
        });

        return {
            tags: {
                detection_tags: {
                    shapes: shapes
                }
            },
            info: {
                md5: img.name,
                path: img.url
            }
        };
    });
}


const computeCount = (list) => {
    let count  = 0; //  已标注图片计数
    list.forEach(item => {
        if (item.json.length !== 0) {
            count++;
        }
    })
    return count;
}
const saveUpdate = () => {
    const temp = transformImgList(state.imgList)
    const temp1 = JSON.stringify(temp)
    let file = new File([new Blob([temp1], {type: 'text/json'})], 'file.txt')
    const formData = new FormData()
    formData.append('file', file)

    let count = computeCount(state.imgList);
    savaTask(state.c_task.id,count,formData)
        .then(res => {
            if (res.code === 200) {

                ElMessage.success('保存成功');

            }
        })
}


const getTaskByTaskId = async (id) => {
   const {data,code} = await getTaskById(id);
   if (code === 200) {
       return data
   }
}

const getDataset = async (id) => {
  const {data,code} = await getDatasetById(id);
  if (code === 200 && data) {
      return {fileUrl: data.fileUrl,markUrl: data.markUrl}
  }
}

// 转换json函数
function transformJsonList(list) {
    return list.map((item, index) => {
        const { info, tags: { detection_tags: { shapes } } } = item;
        const { md5, path } = info;

        const marks = shapes.map(shape => ({
            color: shape.tag[0].label_key_name,
            label: shape.tag[0].label_value_name[0],
            pointList: [
                { x: shape.pts[0], y: shape.pts[1] },
                { x: shape.pts[2], y: shape.pts[3] } // 注意索引从0开始，修正你的索引错误
            ],
            type: shape.frame_type  // 文字转英文
        }));

        return {
            id: index,
            name: md5,
            url: path,
            json: marks
        };
    });
}


const loadMarkedJson = async (url) => {
    const res = await getDatasetByPath({catalogue: `${url}`});

    function getJsonUrls(urls) {
        const jsonUrls = [];
        urls.forEach(url => {
            if (url.endsWith('.json')) {
                jsonUrls.push(url);
            }
        });
        return jsonUrls;
    }

    const jsonUrls = getJsonUrls(res.data);

    // const timestamp = new Date().getTime(); // 获取当前时间戳
    // const newUrl = `${url}?timestamp=${timestamp}`;
    fetch(jsonUrls[0])
        .then(response => {
            return response.json();
        })
        .then(data => {
            const temp  = transformJsonList(data)

            state.imgList.forEach(item => {
                let obj = temp.find(i => i.name === item.name);
                if (obj) {
                    item.json = obj.json;
                }
            })

            importJSON(state.curImg.json);
        })
}

const initZip = async (id) => {
    const {dataSetId,label,taskName} = await getTaskByTaskId(id);
    state.c_task.id = id;
    state.c_task.label = label? JSON.parse(label):[];
    state.c_task.datasetId = dataSetId?dataSetId:null;
    state.c_task.taskName = taskName?taskName:'';

    state.labelList = JSON.parse(label);
    const {fileUrl,markUrl} = await getDataset(dataSetId);

    await loadImage(fileUrl);
    if (fileUrl){   // 加载已标注json
       await loadMarkedJson(fileUrl)
    }
}

const finish = () => {
    ElMessageBox.confirm('当前操作不可逆，确定提交该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        let count = computeCount(state.imgList);
        const temp = transformImgList(state.imgList)
        const temp1 = JSON.stringify(temp)
        let file = new File([new Blob([temp1], {type: 'text/json'})], 'file.txt')
        const formData = new FormData()
        formData.append('file', file)

        submitTask(state.c_task.id,count,formData).then(res => {
            if (res.code === 200) {
                router.go(-1);
                ElMessage.success('提交成功');
            }
        })
    }).catch(() => { });
}

</script>

<template>
    <div class="page">
        <a-page-header
            @back="goback"
            :title="state.c_task.taskName">
            <template #extra>
                <el-button @click="saveUpdate">保存</el-button>
                <el-button type="primary" @click="finish">提交</el-button>
            </template>
        </a-page-header>

        <div class="w-full" style="background: rgb(243,244,247);min-height: calc(100vh - 164px);padding: 30px 50px">
            <div class="bg-white flex flex-row">
                <div class="w-[100%] flex justify-between" style="padding: 20px 50px 20px;border: silver 1px solid">
                    <div class="tools_bar">
                        <button title="不用选择 按住空格键自动进入平移模式" :class="{ active: moveStatus }">
                            <!-- 移动 -->
                            <svg t="1689228627753" class="icon" viewBox="0 0 1024 1024" version="1.1"
                                 xmlns="http://www.w3.org/2000/svg"
                                 p-id="2933" width="32" height="32">
                                <path
                                    d="M971.989333 485.376l1.365334 1.792 1.322666 1.962667 0.341334 0.512 0.896 1.536a44.458667 44.458667 0 0 1 2.474666 5.162666l0.213334 0.64 0.512 1.408a42.581333 42.581333 0 0 1 1.066666 3.84l0.256 1.066667a42.794667 42.794667 0 0 1 0.853334 7.253333v2.858667c0 0.725333-0.042667 1.493333-0.128 2.218667l-0.128 1.322666-0.128 1.109334a42.794667 42.794667 0 0 1-0.256 1.450666l-0.213334 1.152a42.368 42.368 0 0 1-0.981333 3.797334l-0.341333 1.152a42.325333 42.325333 0 0 1-3.2 7.210666l-0.853334 1.493334-2.005333 2.901333a44.714667 44.714667 0 0 1-4.266667 4.949333l-120.661333 120.661334a42.666667 42.666667 0 1 1-60.330667-60.330667l47.786667-47.786667L554.666667 554.666667v280.96l47.872-47.786667a42.666667 42.666667 0 0 1 60.330666 60.288l-120.661333 120.704-3.242667 2.858667-2.133333 1.706666-0.938667 0.64-1.536 0.981334-1.493333 0.896a44.458667 44.458667 0 0 1-6.4 2.944l-0.810667 0.256a42.538667 42.538667 0 0 1-3.84 1.066666l-1.109333 0.256a42.794667 42.794667 0 0 1-7.253333 0.853334h-2.816c-0.768 0-1.493333-0.042667-2.218667-0.128l-1.365333-0.128-1.066667-0.128a42.794667 42.794667 0 0 1-1.493333-0.256l-1.152-0.213334-1.237334-0.256-1.194666-0.341333a54.997333 54.997333 0 0 1-1.322667-0.384l-1.194667-0.341333a42.325333 42.325333 0 0 1-7.168-3.2l-1.536-0.853334a42.752 42.752 0 0 1-2.901333-2.005333l-0.896-0.682667a44.714667 44.714667 0 0 1-4.010667-3.541333l-120.704-120.704a42.666667 42.666667 0 1 1 60.330667-60.330667L469.333333 835.584V554.666667H188.330667l47.829333 47.829333a42.666667 42.666667 0 0 1-60.330667 60.330667l-120.661333-120.661334-3.114667-3.498666a42.965333 42.965333 0 0 1-0.256-0.256l-0.170666-0.256-0.725334-0.938667-1.578666-2.346667-0.341334-0.554666a42.325333 42.325333 0 0 1-0.341333-0.512l-0.554667-0.981334a42.538667 42.538667 0 0 1-2.944-6.4l-0.256-0.810666-0.384-1.152-0.384-1.322667a42.368 42.368 0 0 1-0.810666-3.626667l-0.213334-1.450666a42.666667 42.666667 0 0 1-0.128-1.109334l-0.128-0.938666a42.922667 42.922667 0 0 1-0.085333-1.493334L42.666667 512.810667v-1.621334l0.085333-1.706666 0.085333-1.493334 0.128-0.981333 0.170667-1.365333 0.426667-2.346667 0.213333-1.066667 0.341333-1.28a42.581333 42.581333 0 0 1 2.56-7.04l0.682667-1.365333 0.725333-1.365333 0.554667-1.024a42.410667 42.410667 0 0 1 3.242667-4.693334l0.170666-0.170666 3.114667-3.456L175.786667 361.130667a42.666667 42.666667 0 0 1 60.330666 60.330666L188.330667 469.333333h280.96V188.330667L421.546667 236.16a42.666667 42.666667 0 1 1-60.373334-60.330667l120.704-120.661333 3.456-3.114667 0.298667-0.256 0.256-0.170666 0.896-0.725334 2.389333-1.578666 0.512-0.341334a42.410667 42.410667 0 0 1 0.512-0.341333l1.024-0.554667a42.538667 42.538667 0 0 1 6.4-2.944l0.810667-0.256a42.538667 42.538667 0 0 1 1.152-0.384l1.322667-0.384a42.368 42.368 0 0 1 3.584-0.810666l1.493333-0.213334a42.666667 42.666667 0 0 1 1.066667-0.128l0.981333-0.128 1.493333-0.085333 1.706667-0.085333h1.621333l1.706667 0.085333 1.493333 0.085333 0.938667 0.128 1.365333 0.170667 2.346667 0.426667 1.109333 0.213333 1.28 0.341333a42.581333 42.581333 0 0 1 6.997334 2.56l1.408 0.682667 1.365333 0.725333 0.981333 0.554667a42.325333 42.325333 0 0 1 4.693334 3.242667l0.170666 0.170666 3.498667 3.114667 120.661333 120.661333a42.666667 42.666667 0 1 1-60.330666 60.330667L554.666667 188.330667V469.333333h281.002666l-47.786666-47.872a42.666667 42.666667 0 0 1 60.288-60.330666l120.704 120.704 1.706666 1.749333 0.256 0.341333 0.426667 0.512 0.469333 0.554667-0.896-1.066667-1.962666-2.133333 3.029333 3.413333 0.128 0.170667z"
                                    fill="#333333" p-id="2934"></path>
                            </svg>
                        </button>
                        <button :class="{ active: selectStatus }" @click="mark?.setSelectMode(!selectStatus)">
                            <!-- 选择 -->
                            <svg t="1689228737226" class="icon" viewBox="0 0 1024 1024" version="1.1"
                                 xmlns="http://www.w3.org/2000/svg"
                                 p-id="7059" width="32" height="32">
                                <path
                                    d="M580.76 908.62a53.366667 53.366667 0 0 1-46.28-26.666667L404.1 656.133333l-154.666667 148.586667A21.333333 21.333333 0 0 1 213.333333 789.333333V106.666667a21.333333 21.333333 0 0 1 6.706667-15.526667c5.333333-5.02 15.26-8.74 25.72-2.7l91.946667 53.086667 498.833333 288a21.333333 21.333333 0 0 1-4.733333 38.966666l-206 59.64 130.386666 225.826667a53.333333 53.333333 0 0 1-19.526666 72.86l-129.333334 74.666667a53.02 53.02 0 0 1-26.573333 7.133333zM408.953333 600.546667a20.666667 20.666667 0 0 1 3 0.213333 21.333333 21.333333 0 0 1 15.48 10.453333l144 249.413334a10.666667 10.666667 0 0 0 14.566667 3.906666l129.333333-74.666666a10.666667 10.666667 0 0 0 3.906667-14.573334l-144-249.413333a21.333333 21.333333 0 0 1 12.546667-31.16l184.053333-53.286667L256 143.62v595.633333l138.18-132.76a21.333333 21.333333 0 0 1 14.773333-5.946666z"
                                    fill="#333333" p-id="7060"></path>
                            </svg>
                        </button>
                        <button :class="{ active: drawType === 'rect' }" @click="setDrawType('rect')">
                            <svg t="1689232578153" class="icon" viewBox="0 0 1024 1024" version="1.1"
                                 xmlns="http://www.w3.org/2000/svg"
                                 p-id="17746" width="32" height="32">
                                <path
                                    d="M904.50393 795.651268V225.084385c32.463366-10.869558 55.862285-41.503253 55.862286-77.623915 0-45.212738-36.647665-81.863473-81.864496-81.863473-35.721573 0-66.084091 22.890336-77.262688 54.798047H226.150158c-11.177574-31.907711-41.538045-54.79907-77.262688-54.79907-45.213761 0-81.864496 36.651758-81.864496 81.864496 0 35.530215 22.63758 65.76175 54.271045 77.086679v571.642379c-31.633465 11.32493-54.271044 41.555441-54.271045 77.084632 0 45.212738 36.652781 81.864496 81.864496 81.864496 34.495652 0 63.98222-21.342074 76.036767-51.532677h577.543786c12.05557 30.190603 41.546232 51.532677 76.035743 51.532677 45.217854 0 81.864496-36.651758 81.864496-81.864496-0.002047-36.120662-23.400966-66.753333-55.864332-77.622892z m-105.182574 56.789401H228.067834c-7.227612-27.548426-28.447912-49.415456-55.609529-57.570183V225.865167c26.077935-7.829316 46.683229-28.300556 54.682414-54.305836H800.249495c7.835456 25.473161 27.768437 45.63434 53.090149 53.80851V795.367813c-26.39516 8.519024-46.930869 30.065759-54.018288 57.072856z"
                                    p-id="17747" fill="#333333"></path>
                            </svg>
                        </button>
                        <button :class="{ active: drawType === 'polygon' }" @click="setDrawType('polygon')">
                            <svg t="1689228887315" class="icon" viewBox="0 0 1024 1024" version="1.1"
                                 xmlns="http://www.w3.org/2000/svg"
                                 p-id="14086" width="32" height="32">
                                <path
                                    d="M680.71 915.22H343.29c-39.53 0-76.37-21.27-96.12-55.52L78.46 567.49c-19.75-34.22-19.75-76.77 0-110.99l168.71-292.2c19.75-34.24 56.6-55.52 96.12-55.52h337.42c39.53 0 76.37 21.27 96.12 55.52l168.71 292.2c19.75 34.22 19.75 76.77 0 110.99L776.83 859.7c-19.75 34.24-56.59 55.52-96.12 55.52zM343.29 175.69c-15.7 0-30.35 8.43-38.21 22.04l-168.71 292.2c-7.84 13.61-7.84 30.52 0 44.13l168.71 292.2c7.86 13.61 22.51 22.04 38.21 22.04h337.42c15.7 0 30.35-8.43 38.21-22.04l168.71-292.2c7.84-13.61 7.84-30.52 0-44.13l-168.71-292.2c-7.86-13.61-22.51-22.04-38.21-22.04H343.29z"
                                    fill="#333333" p-id="14087"></path>
                            </svg>
                        </button>
                        <div class="b"></div>
                        <button @click="toJSON">导出</button>
<!--                        <button @click="importJSON">导入</button>-->
                        <div class="b"></div>
                        <button @click="clearCanvas">清空画布</button>
                    </div>

                    <el-space :size="40" >
                        <el-popover
                            placement="top-start"
                            :width="200"
                            trigger="hover"
                        >
                            <template #reference>
                                <el-text>
                                    操作说明
                                    <el-icon size="15">
                                        <InfoFilled/>
                                    </el-icon>
                                </el-text>
                            </template>
                            <template #default>
                                <div>
                                    <b>画布操作</b>：
                                    1.双击鼠标恢复大小;&nbsp;&nbsp;&nbsp;2.Ctrl + 滚轮缩放;&nbsp;&nbsp;&nbsp;3.按住空格拖动画布<br/>
                                    <b>多边形绘制</b>：
                                    1.右键删除最后一个点;&nbsp;&nbsp;&nbsp;2.点击第一个点或者按回车 完成绘制<br/>
                                    <b>标注对象</b>：<br/>
                                    1.切换到选择模式 移动到对象上，点击中对象<br/>
                                    2.可以点击拖动，按住调整控制点位置<br/>
                                    3.按Delete键删除对象<br/>
                                </div>
                            </template>
                        </el-popover>

                        <el-text>作业状态</el-text>

                        <el-select v-model="state.labelInfo.status" class="w-[100px]">
                            <el-option v-for="item in statusOpt" :value="item.value" :label="item.label"></el-option>
                        </el-select>
                    </el-space>
                </div>
<!--                <div class="w-[30%] flex items-center font-bold	"-->
<!--                     style="padding:20px 50px  20px ;border: silver 1px solid">标注控制台-->
<!--                </div>-->
            </div>
            <div class="bg-white flex flex-row">
                <div class="w-[100%] flex flex-col flex-nowrap relative min-h-[200px] gap-4"
                     style="padding: 20px 50px;border: silver 1px solid">
<!--                    <div class="flex flex-row gap-7 flex-wrap w-full justify-between"-->
<!--                         v-if="state.treeInfo.temp.length !==0">-->
<!--                        <div v-for="(item,index) in currentPageImages" @click="selectImg(item,index)" class="relative">-->
<!--                            &lt;!&ndash;                            <el-image fit="fill" :preview-src-list="previewSrcList" :initial-index="index" :src="item.url" class="img-card" :class="{'label-scene-list-active': selectedIndex === index}"/>&ndash;&gt;-->
<!--                            <el-image fit="fill" :src="item.url" class="img-card"-->
<!--                                      :class="{'label-scene-list-active': selectedIndex === index}"/>-->
<!--                            <el-tag type="primary" v-if="item.label !== ''" class="absolute top-1 left-1">-->
<!--                                {{ formatType(item.label) }}-->
<!--                            </el-tag>-->
<!--                            <a-tag :bordered="false" color="cyan" v-if="item.label !==''"-->
<!--                                   class="absolute bottom-2 left-1">{{ formatLabel(item.label) }}-->
<!--                            </a-tag>-->
<!--                        </div>-->
<!--                    </div>-->

                    <div class="mark-box" >
                        <div id="mark-box"/>
                        <div class="object-list">
                            <p class="flex justify-center">标注控制台</p>
                            <draggable v-model="objectList" group="people" @start="dragStatus = true" @end="stopDrag" item-key="id">
                                <template #item="{ element }">
                                    <div @click="selectObj(element.id)" class="item" :class="{ active: element.select }"
                                         @contextmenu="(e) => setLabel(e, element)">
                                        <b :style="{ background: element.color }"></b>{{ element.label }}
                                    </div>
                                </template>
                            </draggable>
                        </div>
                    </div>
<!--                    <a-empty description="暂无图片" ></a-empty>-->
                    <magnify v-if="state.imgList.length !==0" :img-list="state.imgList" @click-img="changeImg"></magnify>
                </div>
<!--                <div class="w-[30%] flex flex-col" style="border: silver 1px solid;padding: 20px 20px">
                    <a-space direction="vertical" :size="30">
                        <a-input-search placeholder="请输入标签值搜索"></a-input-search>
                        <div>分类结果</div>
                        <el-radio-group v-model="state.curImg.label" @change="selectImgLabel">
                            <el-radio class="mb-2" :value="item.value" border v-for="item in state.labelList">
                                {{ item.label }}
                            </el-radio>
                        </el-radio-group>
                    </a-space>

&lt;!&ndash;                    <textarea v-model="jsonValue" cols="100" rows="8"></textarea>&ndash;&gt;


                </div>-->
            </div>


        </div>


        <el-dialog
            v-model="showLabel"
            title="标注对象"
            width="50%">
            <el-form :model="state.labelInfo" label-width="auto" label-position="right">
                <el-form-item label="标签">
                    <el-select v-model="state.labelInfo.labelInput" @change="selectLabel" value-key="label">
                        <el-option v-for="item in state.labelList" :label="item.name" :value="item" :key="item.name"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="标签颜色">
                    <el-color-picker disabled v-model="state.labelInfo.colorInput"/>
                </el-form-item>

                <el-form-item class="justify-end">
                    <div class="flex w-full justify-end">
                        <el-button @click="clearLabel">取消</el-button>
                        <el-button type="primary" @click="enterLabel">提交</el-button>
                    </div>

                </el-form-item>
            </el-form>
        </el-dialog>

<!--        <div class="label-mark" v-show="showLabel" @click="clearLabel"></div>-->
<!--        <div class="label-input" v-show="showLabel">-->
<!--            <input type="text" v-model="labelInput"/>-->
<!--            <input type="color" v-model="colorInput"/><br/>-->
<!--            <button @click="clearLabel">取消</button>-->
<!--            <button @click="enterLabel">提交</button>-->
<!--        </div>-->



    </div>
</template>

<style scoped>
.page {

}

.label-mark {
    position: fixed;
    background: rgba(0, 0, 0, 0.5);
    top: 0px;
    left: 0px;
    right: 0px;
    bottom: 0px;
    z-index: 8;
}

.label-input {
    position: fixed;
    width: 400px;
    padding: 30px;
    background: #ccc;
    top: 50px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 9;
}

.tools_bar {
    display: flex;
    align-items: center;
    justify-content: center;
}

.tools_bar .b {
    height: 26px;
    width: 2px;
    background: rgb(219, 219, 219);
    margin: 0 20px;
}

.tools_bar button {
    margin: 5px;
    padding: 4px 8px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    background: #ccc;
    outline: none;
}

.tools_bar button.active {
    background: #000;
}

.tools_bar button.active svg path {
    fill: #fff;
}

.tools_bar svg {
    width: 20px;
    height: 20px;
}

.mark-box {
    display: flex;
    height: 700px;
    width: 100%;
}

#mark-box {
    flex: 1;
    height: 700px;
    overflow: hidden;
    margin-right: 15px;
    /* cursor: none; */
}

.object-list {
    width: 200px;
    height: 100%;
    overflow: hidden;
    overflow-y: auto;
    border-left: 1px solid #dcdcdc;

}

.object-list .item {
    background: rgb(197, 197, 197);
    text-align: left;
    padding: 0 10px;
    height: 30px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 30px;
    font-size: 14px;
    cursor: pointer;
    margin-bottom: 2px;
}


.object-list .item b {
    display: inline-block;
    width: 15px;
    height: 15px;
    margin-right: 10px;
    vertical-align: middle;
    margin-top: -1px;
}

.object-list .item:hover {
    background: #989898;
}

.object-list .item.active {
    background: rgb(27, 27, 27);
    color: #fff;
}
</style>

