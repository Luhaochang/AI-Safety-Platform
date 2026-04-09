<script setup>
import 'codemirror/mode/javascript/javascript.js'
import {ArrowRight, Download, RefreshRight, DocumentCopy, Monitor, Timer} from "@element-plus/icons-vue";
import {
    deleteModelLineById,
    getChartDataInfo,
    getModelLineById, getModelResult, getTrainDetail,
    packageProductionLine,
    updateModelLine
} from "@/api/modelMag/modelLine.js";
import {useRouter} from "vue-router";
import {CopyOutlined, FileTextOutlined, BarChartOutlined, HddOutlined, InfoCircleOutlined} from '@ant-design/icons-vue';
import Codemirror, {createLinkMark, createLogMark, createTitle} from 'codemirror-editor-vue3';
import {messagelog} from "@/assets/logs.js";
import {ElMessage, ElMessageBox} from "element-plus";
import LineCharts from "@/views/model/line/components/lineCharts.vue";
import axios from "axios";
import yaml from 'js-yaml';
import {getLogs} from "@/api/modelMag/label.js";
import warning from '@/assets/icons/svg/警告.svg'
import preview from '@/assets/icons/svg/预览.svg'
import {rootExample} from "@/utils/applicationScene.js";

const router = useRouter()
const codeMirrorRef = ref()
const codeMirrorRef2 = ref()
const isEdit = ref(false)
// 标识当前文件是否为图片
const isImageFile = ref(false);
// 存储图片URL
const imageUrl = ref('');

const state = reactive({
    line: {
        id: null,
        paramsString: [],
        lineName: null,
        createTime: null,
        endTime: null,
        scene: null,
        status: null,
        modelName: null,
        dataSetName: null,
        runtime: null,
        isPackage: null,

        secondaryScene: null
    },
    c_tab: 0,
    chartData: [],
    tree: [],
    click_file_content: ''
})

const formatScene = (scene) => {
    switch (scene) {
        case 0:
            return '图片分类';
        case 1:
            return '目标检测';
        case 2:
            return '目标跟踪';
        case 3:
            return '图片分割';
        case 4:
            return 'OCR';
        default:
            return ''
    }
}

const labelStyle = {
    'width': '120px',
    'color': '#86909C',
    'font-weight': '400',
}

const contentStyle = {
    'color': '#1D2129',
    'font-weight': '500'
}

const logs = ref(``);  // code mirror 绑定的字符串
const all_logs = ref([]) // 用来保存接口所有日志
const logOptions = {
    mode: 'log',
    lineNumbers: true,
    styleActiveLine: false,
    readOnly: 'nocursor',
    autofocus: false,
    lineWrapping: true
};

const nameLine = ref('')

const columns = [
    {
        title: 'Name',
        dataIndex: 'name',
        key: 'name',
    },

    {
        title: 'Type',
        key: 'type',
        dataIndex: 'type',
    },

];

const editName = () => {
    nameLine.value = state.line.lineName;
    isEdit.value = true;
}

const updateName = () => {
    updateModelLine({
        id: state.line.id,
        lineName: nameLine.value
    }).then(res => {
        if (res.code === 200) {
            getLineById(state.line.id).then(() => {
                cancelUpdate();
            });
        }
    })
}

const cancelUpdate = () => {
    isEdit.value = false;
}
const getLineById = async (id) => {
    const res = await getModelLineById(id);
    if (res.code === 200 && res.data) {
        const {
            paramsString, lineName, createTime, scene, status, modelName, isPackage,
            dataSetName, secondaryScene
        } = res.data;
        state.line.lineName = lineName;
        state.line.isPackage = isPackage;
        state.line.scene = scene;
        state.line.status = status;
        state.line.modelName = modelName;
        state.line.dataSetName = dataSetName;
        state.line.secondaryScene = secondaryScene;
    }
}

const getRuntime = async (id) => {
    const res = await getTrainDetail(id);
    if (res.code === 200 && res.data) {
        const {startTime, endTime, timeDiff} = res.data;
        state.line.createTime = startTime ? startTime : '';
        state.line.endTime = endTime ? endTime : '';
        state.line.paramsString = res.data.paramsList;

        function formatTimeDiffFlexible(timeDiff) {
            const [hours, minutes, seconds] = timeDiff.split(':').map(Number);
            let result = '';

            if (hours > 0) {
                result += `${hours}小时`;
            }
            result += `${minutes}分${seconds}秒`;


            // 去除结果字符串开头可能的多余空格（如果小时为0则不会添加"小时"部分）
            return result.trim();
        }

        if (timeDiff) {
            state.line.runtime = formatTimeDiffFlexible(timeDiff);
        } else {
            state.line.runtime = null
        }

    }
}

let intervalId = ref(null)

let currentIndex = 0;


const startLogging = () => {
    if (intervalId.value !== null) {
        clearInterval(intervalId.value); // 防止重复启动定时器
    }

    intervalId.value = setInterval(() => {
        if (currentIndex < all_logs.value.length) {
            logs.value += all_logs.value[currentIndex] + '\n';
            currentIndex++;
        } else {
            intervalId.value = null;
            clearInterval(intervalId.value); // 所有日志都已显示，停止定时器
        }
    }, 10); // 每秒显示一行，你可以根据需要调整这个时间间隔
};

const getRootFileData = async () => {
    const nodeList = await getArtifactData('model');
    // 获取模型输入输出
    const fileObj = nodeList.find(item => item.path === 'model/MLmodel');

    let newUrl = '';
    if (fileObj && fileObj.basePath) {
        const response = await axios.get(fileObj.basePath, {
            // 如果文件内容不是通过 RESTful API 提供的，您可能需要调整这里的请求方式
            // 比如使用 Blob 或 FileReader 来处理本地文件
            // responseType: 'text', // 假设文件是文本文件
        });
        const parsedConfig = yaml.load(response.data)
        const {signature,run_id} = parsedConfig;
        newUrl = run_id;
        if (signature) {
            const newList = []

            const inputObj = {
                name: 'Input',
                type: '',
                children: signature.inputs? JSON.parse(signature.inputs).map(item => {
                    return {
                        name: '- (required)',
                        type: createType(item)
                    }
                }) : []
            }
            const outputObj = {
                name: 'Output',
                type: '',
                children: signature.outputs ? JSON.parse(signature.outputs).map(item => {
                    return {
                        name: '- (required)',
                        type: createType(item)
                    }
                }) : []
            }


            newList.push(inputObj)
            newList.push(outputObj)


            schema.value = newList;

            function createType(obj) {

                const tensorTypeString = obj.type; // "tensor"
                const tensorSpec = obj["tensor-spec"];
                const dtype = tensorSpec.dtype; // "float64"
                const shape = tensorSpec.shape; // [-1, 4]

                // 注意：由于-1在形状表示中通常不是有效的，我们在这里保留它作为原始输入的一部分，
                // 但在实际应用中，您可能需要用一个占位符或特定的表示法来替代它。

                // 拼接字符串
                const tensorString = `${tensorTypeString}(dtype: ${dtype}, shape:[ ${shape.toString()}])`;

                return tensorString;
            }

        } else {
            schema.value = [];
        }
    }


    // 获取模型样例
    const fileObj2 = nodeList.find(item => item.path === 'model/serving_input_payload.json');

    if (fileObj2) {
        const response2 = await axios.get(fileObj2.basePath, {
            // 如果文件内容不是通过 RESTful API 提供的，您可能需要调整这里的请求方式
            // 比如使用 Blob 或 FileReader 来处理本地文件
            responseType: 'text', // 假设文件是文本文件
        });
        InputExample.value = rootExample.replace(/"inputs":\s*\[[^\]]*\]/, `"inputs": ${JSON.stringify(yaml.load(response2.data).inputs)}`);
        InputExample.value = InputExample.value.replace(/model_uri = '[^']*'/, `model_uri = '${newUrl}'`);

    }else {
        InputExample.value = newUrl? rootExample.replace(/model_uri = '[^']*'/, `model_uri = '${newUrl }'`) : rootExample;

    }
}

const tabChange = async (tab) => {
    if (tab === 1) {
        await nextTick(async () => {
            if (pollingInterval) {
                clearInterval(pollingInterval);
            }
            await onmessage();
        })
    } else if (tab === 2) {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null; // 可选：将引用设置为 null 以表明没有轮询在运行
        }
        await getChartData();
    } else if (tab === 3) {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null; // 可选：将引用设置为 null 以表明没有轮询在运行
        }
        buildTree().then(treeData => {
            state.tree = treeData;

            // 初始化跟节点
            rootFile.value = true;
            getRootFileData();
            selectedKeys.value = ['model']
        })
    } else {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null; // 可选：将引用设置为 null 以表明没有轮询在运行
        }
    }
}
let pollingInterval = null;

const scrollToBottom = () => {
    if (codeMirrorRef.value.cminstance) {
        const editor = codeMirrorRef.value.cminstance;
        const doc = editor.getDoc();
        doc.setCursor({line: doc.size - 1, ch: doc.getLine(doc.size - 1).length});

        editor.scrollIntoView({line: doc.size - 1, ch: 0}, 100); // 10 是动画的持续时间（毫秒）
    }
};

watch(() => {
}, () => {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }
}, {immediate: false, deep: false, flush: 'post'}); // 注意：这里的 watch 只是为了演示如何清理，实际上你可能需要更精确的控制

async function onmessage() {
    const res = await getLogs({taskId: state.line.id});
    if (res.code === 200 && res.data.length !== 0) {
        const newLogLines = res.data.map(item => `${item["@timestamp"]} - ${item.message}`);
        all_logs.value = newLogLines;

    }
}


watch(() => all_logs.value, (newValue, oldValue) => {
    startLogging();
}, {deep: true})

const deleteLine = () => {
    ElMessageBox.confirm('确定删除该产线吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteModelLineById({id: state.line.id}).then(res => {
            if (res.code === 200) {
                router.push('/modelmag/model-line');
                ElMessage.success('删除成功');
            }
        })
    }).catch(() => {
    });
}

const packageLine = async () => {
    const res = await packageProductionLine(state.line.id);
    if (res.code === 200) {
        ElMessage.success(res.message);
        await getLineById(state.line.id);
    }
}

const publishModel = async () => {
    sessionStorage.setItem('modelInfo', JSON.stringify({
        lineId: state.line.id,
        schema: schema.value,
        InputExample: InputExample.value
    }))
    await router.push(`/modelmag/model-add/model-create/${state.line.id}/${state.line.secondaryScene}/${state.line.scene}`);
}

const getChartData = async () => {
    state.chartData = [];
    const res = await getTrainDetail(state.line.id);
    if (res.code === 200 && res.data.metricList && res.data.metricList.length !== 0) {

        for (let key in res.data.metricList) {
            let temp = res.data.metricList[key];
            state.chartData.push({
                x: temp.map(item => item.step),
                y: temp.map(item => item.value),
                name: key
            })
        }
    }
}

const getArtifactData = async (path = undefined) => {
    const res = await getModelResult({id: state.line.id, path: path ? path : undefined});
    if (res.code === 200 && res.data.length !== 0) {
        return res.data;
    }
}

const buildTree = async (rootPath = '') => {
    let tree = [];

    const processNodes = async (path) => {
        const nodes = await getArtifactData(path);
        const branch = [];

        for (const node of nodes) {
            const {path: nodePath, is_dir, basePath, file_size} = node;
            let title = nodePath;
            // if (!isDir) {
            // 如果是文件，则提取文件名作为 title
            const parts = nodePath.split('/');
            title = parts[parts.length - 1];
            // }

            const treeNode = {
                title: title,
                key: nodePath,
                // children: []
            };

            if (is_dir) {
                // 如果是目录，则递归获取其子目录和文件
                const subNodes = await processNodes(nodePath);
                if (subNodes.length > 0) {
                    treeNode.children = subNodes;
                }
            } else {
                // 如果是文件，则添加文件信息
                treeNode.isLeaf = true;
                treeNode.basePath = basePath;
                treeNode.fileSize = file_size;
                // treeNode.title =
            }

            branch.push(treeNode);
        }

        return branch;
    };

    // 从根目录开始构建树
    const rootNodes = await processNodes(rootPath);
    if (rootNodes.length > 0) {
        tree = rootNodes;
    }

    return tree;
};

const fileToLarge = ref(false)
const unsupportedFile = ref(false)
const rootFile = ref(false)
const schema = ref([])
const InputExample = ref('')

const selectedKeys = ref([]);

const selectTree = async (val, {selectedNodes}) => {
    if (!selectedNodes || selectedNodes.length === 0) return;
    const {basePath, isLeaf, fileSize, title} = selectedNodes[0]
    const MAX_FILE_SIZE = 50 * 1024 * 1024;

    // 重置状态
    unsupportedFile.value = false;
    fileToLarge.value = false;
    rootFile.value = false;

    if (isLeaf) {
        // 检查是否为不支持预览的文件类型
        if (title.endsWith('.pt') || title.endsWith('.pth')) {
             state.click_file_content = '';
             unsupportedFile.value = true;
             allowUpload.value = true;
             c_file_URL.value = basePath;
             return;
        }

        if (fileSize < MAX_FILE_SIZE) {
            try {
                // 加载文件内容
                const response = await axios.get(basePath, {
                    // 如果文件内容不是通过 RESTful API 提供的，您可能需要调整这里的请求方式
                    // 比如使用 Blob 或 FileReader 来处理本地文件
                    responseType: 'blob', // 假设文件是文本文件
                });

                // 检查文件类型
                const contentType = response.headers['content-type'];
                if (contentType && contentType.startsWith('image/')) {
                    // 如果是图片，创建 Blob URL
                    state.click_file_content = URL.createObjectURL(response.data);
                } else {
                    // 如果是文本，读取文本内容
                    // 限制文本预览大小，防止卡死
                    const text = await response.data.text();
                    if (text.length > 100000) {
                         state.click_file_content = text.substring(0, 100000) + '\n\n... (File too large, content truncated)';
                    } else {
                         state.click_file_content = text;
                    }
                }

                fileToLarge.value = false;

                allowUpload.value = true;
                c_file_URL.value = basePath;
            } catch (error) {
                console.error('Failed to load file content:', error);
                // 可以在这里处理加载失败的情况，比如显示错误消息
            }
        } else {
            allowUpload.value = true;
            c_file_URL.value = basePath;

            state.click_file_content = '';
            fileToLarge.value = true;
        }

    } else {
        state.click_file_content = '';
        fileToLarge.value = false;
        c_file_URL.value = null;
        allowUpload.value = false;

        if (selectedKeys.value[0] === 'model') {
            rootFile.value = true;
            await getRootFileData()
        } else {
            rootFile.value = false;
        }


    }
}

const allowUpload = ref(false);
const c_file_URL = ref()
const downloadFile = async () => {
    try {
        const response = await fetch(c_file_URL.value);

        // 检查响应是否成功 (状态码在200-299之间)
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        // 获取文件的二进制数据
        const blob = await response.blob();

        // 创建一个指向Blob对象的URL
        const url = URL.createObjectURL(blob);

        // 创建一个隐藏的<a>元素并设置其href和download属性
        const a = document.createElement('a');
        a.href = url;
        // 这里你可以设置一个文件名，如果服务器没有正确设置Content-Disposition

        const pathParts = c_file_URL.value.split('/');

        // 数组的最后一个元素是文件名
        const fileName = pathParts[pathParts.length - 1];
        a.download = fileName; // 或者从响应头中提取文件名

        // 触发点击事件以下载文件
        document.body.appendChild(a);
        a.click();

        // 移除<a>元素
        document.body.removeChild(a);

        // 释放URL对象
        URL.revokeObjectURL(url);
    } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
    }

}

const copyId = () => {
    if (state.line.id) {
        navigator.clipboard.writeText(state.line.id).then(() => {
            ElMessage.success('ID copied to clipboard');
        }).catch(err => {
            console.error('Failed to copy: ', err);
        });
    }
}

onMounted(async () => {
    state.line.id = router.currentRoute.value.params.modelId;
    await getLineById(state.line.id);

    await getRuntime(state.line.id);
})

// 更好的做法是使用 onUnmounted 钩子来清理
onUnmounted(() => {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }
});

</script>

<template>
    <div class="bg-gray-50 min-h-screen flex flex-col">
        <div class="a-model-create-hd bg-white shadow-sm z-10 relative">
            <div class="a-model-create-hd-c">
                <el-breadcrumb :separator-icon="ArrowRight" class="mb-[20px]">
                    <el-breadcrumb-item :to="{path: '/modelmag/model-line'}">模型产线</el-breadcrumb-item>
                    <el-breadcrumb-item>产线详情</el-breadcrumb-item>
                </el-breadcrumb>
                <div>
                    <div class="flex justify-between items-center">
                        <div class="flex items-center leading-[46px]">
                            <div class="text-2xl text-gray-900 font-semibold mr-4">
                                {{ state.line.lineName }}
                            </div>
                            <el-button v-hasPermi="['modelmag:model-line:edit']" icon="edit" link :size="'large'"
                                       v-if="!isEdit" @click="editName"></el-button>

                            <div class="text-sm ml-[10px]" v-else>
                                <div class="flex items-center gap-2">
                                    <el-input v-model="nameLine" size="small" class="w-48"/>
                                    <el-button type="primary" size="small" @click="updateName">确认</el-button>
                                    <el-button size="small" @click="cancelUpdate">取消</el-button>
                                </div>
                            </div>
                        </div>
                        <div class="flex items-center gap-3">
                            <el-button type="primary" @click="packageLine"
                                       v-if="state.line.isPackage === 0 && state.line.status ===4">构建镜像
                            </el-button>
                            <el-button type="primary" @click="publishModel" v-hasPermi="['modelmag:model:add']"
                                       v-if="state.line.isPackage===2">发布模型
                            </el-button>
                            <el-button icon="delete" @click="deleteLine" type="danger" plain>删除</el-button>

                        </div>
                    </div>

                    <div class="flex mt-[12px] mb-[4px] text-sm text-gray-500 items-center">
                        <div class="mr-[24px] flex items-center bg-gray-100 px-3 py-1 rounded-full">
                            <span class="mr-2 text-gray-500">Pipeline ID:</span>
                            <span class="font-mono text-gray-700 font-medium">{{ state.line.id }}</span>
                            <el-button link class="ml-2 text-gray-400 hover:text-blue-500" @click="copyId">
                                <template #icon>
                                    <CopyOutlined/>
                                </template>
                            </el-button>
                        </div>
                        <div class="flex items-center bg-gray-100 px-3 py-1 rounded-full">
                            <el-icon class="mr-2"><Timer /></el-icon>
                            <span class="mr-2">Runtime:</span>
                            <span class="font-medium text-gray-700">{{ state.line.runtime || '-' }}</span>
                        </div>
                    </div>
                </div>
            </div>
            <a-tabs v-model:active-key="state.c_tab" @change="tabChange" class="mt-6 custom-tabs">
                <a-tab-pane :key="0">
                    <template #tab>
                        <span><InfoCircleOutlined /> 基础信息</span>
                    </template>
                </a-tab-pane>
                <a-tab-pane :key="1">
                    <template #tab>
                        <span><FileTextOutlined /> 日志信息</span>
                    </template>
                </a-tab-pane>
                <a-tab-pane :key="2">
                    <template #tab>
                        <span><BarChartOutlined /> 数据统计</span>
                    </template>
                </a-tab-pane>
                <a-tab-pane :key="3" v-if="state.line.status === 4">
                    <template #tab>
                        <span><HddOutlined /> 模型制品</span>
                    </template>
                </a-tab-pane>
            </a-tabs>
        </div>

        <div class="a-model-create-bd flex-1">
            <!-- Tab 0: Basic Info -->
            <div v-if="state.c_tab === 0" class="bg-white p-8 rounded-xl shadow-sm border border-gray-100">
                <div class="text-lg font-bold mb-8 text-gray-900 flex items-center">
                    <span class="w-1 h-6 bg-blue-600 rounded-full mr-3"></span>
                    基础信息概览
                </div>
                <a-descriptions :column="2" :label-style="labelStyle" :content-style="contentStyle" size="middle">
                    <a-descriptions-item label="产线模版">{{ formatScene(state.line.scene) }}</a-descriptions-item>
                    <a-descriptions-item label="微调模型">{{ state.line.modelName }}</a-descriptions-item>
                    <a-descriptions-item label="数据集">{{ state.line.dataSetName }}</a-descriptions-item>
                    <a-descriptions-item label="任务状态">
                        <a-badge v-if="state.line.status ===4" status="success" text="运行成功"/>
                        <a-badge v-else-if="state.line.status ===1" status="warning" text="排队中"/>
                        <a-badge v-else-if="state.line.status ===2" status="processing" text="运行中"/>
                        <a-badge v-else-if="state.line.status ===3" status="error" text="已停止"/>
                        <a-badge v-else-if="state.line.status ===5" status="error" text="运行失败"/>
                    </a-descriptions-item>
                    <a-descriptions-item label="开始时间">{{ state.line.createTime }}</a-descriptions-item>
                    <a-descriptions-item label="结束时间">{{ state.line.endTime }}</a-descriptions-item>

                    <a-descriptions-item label="训练参数配置" :span="2">
                        <div class="bg-gray-50 rounded-lg p-4 border border-gray-200 mt-2">
                            <a-list size="small" :data-source="state.line.paramsString" :split="false" :grid="{ gutter: 16, column: 2 }">
                                <template #renderItem="{item}">
                                    <a-list-item class="!p-0 mb-2">
                                        <div class="flex justify-between items-center w-full text-sm bg-white p-3 rounded border border-gray-100 shadow-sm hover:shadow-md transition-shadow">
                                            <span class="text-gray-500 font-medium">{{ item.key }}</span>
                                            <span class="text-gray-900 font-mono font-semibold">{{ item.value }}</span>
                                        </div>
                                    </a-list-item>
                                </template>
                            </a-list>
                        </div>
                    </a-descriptions-item>
                </a-descriptions>
            </div>

            <!-- Tab 1: Logs -->
            <div v-else-if="state.c_tab === 1" class="bg-white p-8 rounded-xl shadow-sm border border-gray-100">
                <div class="log-wrapper">
                    <Codemirror
                        @change="scrollToBottom"
                        ref="codeMirrorRef"
                        v-model:value="logs"
                        :options="logOptions"
                        class="log-editor"
                        :height="600"
                        placeholder="Waiting for logs..."
                    ></Codemirror>
                </div>
            </div>

            <!-- Tab 2: Charts -->
            <div v-else-if="state.c_tab === 2" class="bg-white p-8 rounded-xl shadow-sm flex flex-col border border-gray-100">
                <div class="mb-8 w-full flex justify-between items-center">
                    <div class="text-lg font-bold text-gray-900 flex items-center">
                        <span class="w-1 h-6 bg-blue-600 rounded-full mr-3"></span>
                        训练数据统计
                    </div>
                    <el-button @click="getChartData" :icon="RefreshRight" circle plain type="primary" class="shadow-sm hover:shadow-md transition-shadow">
                    </el-button>
                </div>
                <div class="chart-con">
                    <line-charts v-for="item in state.chartData" :line-data="item.y" :x="item.x"
                                 :title="item.name"/>
                </div>
            </div>

            <!-- Tab 3: Artifacts -->
            <div v-else-if="state.c_tab === 3" class="bg-white rounded-xl shadow-sm flex h-[800px] overflow-hidden border border-gray-200">
                <div class="w-[320px] min-w-[320px] border-r border-gray-200 bg-gray-50 flex flex-col">
                    <div class="p-4 border-b border-gray-200 font-semibold text-gray-700 bg-gray-100 flex items-center">
                        <el-icon class="mr-2"><DocumentCopy /></el-icon>
                        文件列表
                    </div>
                    <div class="flex-1 overflow-y-auto p-2">
                        <a-directory-tree
                            v-if="state.tree.length"
                            :multiple="false"
                            :default-expand-all="true"
                            :show-line="{ showLeafIcon: false }"
                            :tree-data="state.tree"
                            @select="selectTree"
                            v-model:selected-keys="selectedKeys"
                            class="bg-transparent"
                        >
                        </a-directory-tree>
                    </div>
                </div>

                <div class="flex-1 flex flex-col h-full overflow-hidden bg-white">
                     <div class="h-14 border-b border-gray-200 flex justify-between items-center px-6 bg-white shadow-sm z-10">
                        <div class="flex items-center text-gray-600 text-sm truncate">
                            <span class="font-medium mr-2">Selected:</span>
                            <span class="bg-gray-100 px-2 py-1 rounded text-gray-800 font-mono">{{ c_file_URL ? c_file_URL.split('/').pop() : 'None' }}</span>
                        </div>
                        <el-tooltip content="下载文件" placement="top" v-if="allowUpload">
                            <el-button type="primary" plain size="small" @click="downloadFile">
                                <template #icon>
                                    <el-icon>
                                        <Download/>
                                    </el-icon>
                                </template>
                              下载文件
                            </el-button>
                        </el-tooltip>
                    </div>

                    <div class="flex-1 overflow-auto p-6 relative bg-gray-50/50">
                        <div v-if="state.click_file_content !== ''" class="w-full h-full flex justify-center items-start">
                            <img v-if="c_file_URL && (c_file_URL.endsWith('.png') || c_file_URL.endsWith('.jpg') || c_file_URL.endsWith('.jpeg'))"
                                 :src="state.click_file_content"
                                 class="max-w-full max-h-full object-contain shadow-lg rounded-lg border border-gray-200" />
                             <div v-else class="w-full h-full bg-white p-4 rounded-lg shadow-sm border border-gray-200 overflow-auto">
                                 <pre class="whitespace-pre-wrap font-mono text-sm text-gray-800">{{ state.click_file_content }}</pre>
                             </div>
                        </div>

                        <a-empty
                            v-else-if="state.click_file_content==='' && unsupportedFile"
                            class="flex flex-col items-center justify-center h-full"
                            :image="warning"
                            :image-style="{height: '160px'}"
                        >
                            <template #description>
                                <div class="text-center">
                                    <h3 class="text-lg font-medium text-gray-900 mb-1">不支持预览</h3>
                                    <p class="text-gray-500">该文件格式不支持在线预览，请下载后查看</p>
                                </div>
                            </template>
                        </a-empty>

                        <a-empty
                            v-else-if="state.click_file_content==='' && fileToLarge && !rootFile"
                            class="flex flex-col items-center justify-center h-full"
                            :image="warning"
                            :image-style="{height: '160px'}"
                        >
                            <template #description>
                                <div class="text-center">
                                    <h3 class="text-lg font-medium text-gray-900 mb-1">文件过大</h3>
                                    <p class="text-gray-500">无法预览超过 50MB 的文件</p>
                                </div>
                            </template>
                        </a-empty>

                        <a-empty
                            v-else
                            class="flex flex-col items-center justify-center h-full"
                            :image="preview"
                            :image-style="{height: '160px'}"
                        >
                             <template #description>
                                <div class="text-center">
                                    <h3 class="text-lg font-medium text-gray-900 mb-1">选择文件预览</h3>
                                    <p class="text-gray-500 text-sm">支持图片、文本、JSON 等格式</p>
                                </div>
                            </template>
                        </a-empty>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped lang="scss">
.a-model-create-hd {
    border-bottom: 1px solid #EFF0F2;
    padding-top: 24px;
    padding-left: 32px;
    padding-right: 32px;

    .a-model-create-hd-c {
        width: 100%;
        margin: 0 auto;
    }

    :deep(.ant-tabs-nav) {
        margin: 0;

        &::before {
            border-bottom: none;
        }

        .ant-tabs-tab {
            padding: 12px 0;
            margin: 0 32px 0 0;
            font-size: 15px;

            .anticon {
                margin-right: 8px;
            }
        }
    }
}

.a-model-create-bd {
    padding: 24px 32px;
    width: 100%;
    margin: 0 auto;
}

:deep(.ant-descriptions-item-label) {
    color: #86909C;
}

:deep(.ant-descriptions-item-content) {
    color: #1D2129;
}

.chart-con {
    display: grid;
    gap: 24px;
    grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
}

/* Scrollbar styling */
::-webkit-scrollbar {
    width: 6px;
    height: 6px;
}
::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 3px;
}
::-webkit-scrollbar-track {
    background: transparent;
}

.log-wrapper {
    border: 1px solid #e5e6eb;
    border-radius: 4px;
    overflow: hidden;
}

.log-editor {
    font-size: 13px;
    line-height: 1.6;
    :deep(.CodeMirror) {
        height: 600px !important;
    }
}
</style>