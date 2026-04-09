<script setup>
import {ArrowRight} from "@element-plus/icons-vue";
import {getModelLineById, getModelResult} from "@/api/modelMag/modelLine.js";
import {useRouter} from "vue-router";
import {ForkOutlined, HeartOutlined, CopyOutlined, UserOutlined,CheckCircleOutlined,
    SyncOutlined,
    CloseCircleOutlined,
    ExclamationCircleOutlined,
    ClockCircleOutlined,
    MinusCircleOutlined,} from '@ant-design/icons-vue';
import Codemirror from 'codemirror-editor-vue3';
import {defaultModelIntroduceMarkdown} from "@/assets/logs.js";
import {deleteModel, getModelById, updateModel} from "@/api/modelMag/model.js";
import Vditor from "vditor";
import 'vditor/dist/index.css';
import {ElMessage, ElMessageBox} from "element-plus";
import {addService} from "@/api/modelMag/service.js";
import CreateService from "@/views/model/service/pages/createService.vue";
import warning from "@/assets/icons/svg/警告.svg";
import preview from "@/assets/icons/svg/预览.svg";
import axios from "axios";
import yaml from "js-yaml";
import {rootExample} from "@/utils/applicationScene.js";
import ModelDevelopDialog from "@/views/model/components/modelDevelopDialog.vue";
import {deleteJupyterImage, getJupyterServiceById} from "@/api/modelMag/jupyter.js";

const router = useRouter()
const developDialogRef = ref()

const labelStyle = {
    'margin-right': '20px',
    'width': '120px',
    'line-height': '24px',
    'text-align': 'right',
    'display': 'block',
    'color': '#565772',
}

const formRef = ref()
const deploy_rules = ref({
    name: [{required: true, message: "服务名称为必填字段", trigger: "change"}]
})

const selectedKeys = ref([]);
const fileToLarge = ref(false)
const rootFile = ref(false)
const schema = ref([])
const InputExample = ref('')
const allowUpload = ref(false);
const c_file_URL = ref()

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

const state = reactive({
    visible: false,
    jupyter_dialog_visible: false,
    formInfo: {
        name: '',
        description: '',
        modelId: null
    },
    model: {
        id: null,
        paramsString: [],
        name: null,
        createTime: null,
        scene: null,
        author: null,
        description: null,
        readme: null,
        tagList: [],
        isOfficial: null,
        fileUrl: null,
        classDict: null,
        secondaryScene: null
    },
    c_tab: 0,
    vditor: null,
    artifacts: {
        info: {}
    },
    click_file_content: '',
    tree: [],
    jupyter: []
})

const logOptions = {
    mode: 'log',
    lineNumbers: true,
    readOnly: true,
    styleActiveLine: true,
    // readOnly: 'nocursor',
    line: true,
    autofocus: true,
    lineWrapping: true
    // viewportMargin: 50,
};

const selectTree = async (val, {selectedNodes}) => {
    const {basePath, isLeaf, fileSize} = selectedNodes[0]
    const MAX_FILE_SIZE = 50 * 1024 * 1024;
    if (isLeaf) {
        if (fileSize < MAX_FILE_SIZE) {
            try {
                // 加载文件内容
                const response = await axios.get(basePath, {
                    // 如果文件内容不是通过 RESTful API 提供的，您可能需要调整这里的请求方式
                    // 比如使用 Blob 或 FileReader 来处理本地文件
                    responseType: 'text', // 假设文件是文本文件
                });

                // 更新状态以展示文件内容
                state.click_file_content = response.data;
                fileToLarge.value = false;

                allowUpload.value = true;
                c_file_URL.value = basePath;
            } catch (error) {
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

const getArtifactData = async (path = undefined) => {
    const res = await getModelResult({id: state.artifacts.info.lineId, path: path ? path : undefined});
    if (res.code === 200 && res.data.length !== 0) {
        return res.data;
    }
}
const getRootFileData = async () => {
    const nodeList = await getArtifactData('model');
    // 获取模型输入输出
    const fileObj = nodeList.find(item => item.path === 'model/MLmodel');

    if (fileObj.basePath) {
        const response = await axios.get(fileObj.basePath, {
            // 如果文件内容不是通过 RESTful API 提供的，您可能需要调整这里的请求方式
            // 比如使用 Blob 或 FileReader 来处理本地文件
            // responseType: 'text', // 假设文件是文本文件
        });
        const parsedConfig = yaml.load(response.data)
        const {signature} = parsedConfig;
        if (signature && signature.inputs && signature.outputs) {
            const newList = []

            const inputObj = {
                name: 'Input',
                type: '',
                children: JSON.parse(signature.inputs).map(item => {
                    return {
                        name: '- (required)',
                        type: createType(item)
                    }
                })
            }
            const outputObj = {
                name: 'Output',
                type: '',
                children: JSON.parse(signature.outputs).map(item => {
                    return {
                        name: '- (required)',
                        type: createType(item)
                    }
                })
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
    }
}

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

const logs = ref()


const getModel = async (id) => {
    const res = await getModelById({id: id});
    if (res.code === 200 && res.data) {
        try {
            const {
                name,
                createTime,
                author,
                scene,
                paramsString,
                description,
                readme,
                tagList,
                isOfficial,
                fileUrl,
                modelPut,
                classDict,
                secondaryScene
            } = res.data;
            state.model.name = name;
            state.model.paramsString = JSON.parse(paramsString);
            state.model.scene = scene;
            state.model.author = author;
            state.model.createTime = createTime;
            state.model.description = description === 'null' ? null : description;
            state.model.readme = readme;
            state.model.tagList = tagList;
            state.model.isOfficial = isOfficial;  // 1: 官方模型; 0: 非官方
            state.model.fileUrl = fileUrl;
            state.model.classDict = classDict;
            state.model.secondaryScene = secondaryScene;

            if (modelPut !== undefined && modelPut !== null) {
                state.artifacts.info = JSON.parse(modelPut);
                InputExample.value = state.artifacts.info.InputExample;
                schema.value = state.artifacts.info.schema;

            } else {
                state.artifacts.info = {}; // 或者 null，取决于你的需求
            }

            loadPreviewMarkdown();
        } catch (e) {
        }

    }
}

const editModel = () => {
    isPreview.value = !isPreview.value;
    if (isPreview.value) {
        loadPreviewMarkdown();
        state.model.readme = state.vditor.getValue();
        updateModelReadMe()
    } else {
        loadEditMarkdown()
    }
}

const updateModelReadMe = () => {
    updateModel({
        id: state.model.id,
        readme: state.model.readme,
        tagIdList: state.model.tagList.map(item => item.id)
    }).then(res => {
        if (res.code === 200) {
            ElMessage.success('更新成功');
        }
    })
}

const isPreview = ref(true)
const loadPreviewMarkdown = () => {
    nextTick(() => {
        Vditor.preview(document.getElementById('preview'), state.model.readme, {
            speech: {
                enable: false,
            },
            anchor: 1,
            hljs: {
                style: 'github'
            }
        })
    })
}

const loadEditMarkdown = () => {
    nextTick(() => {
        state.vditor = new Vditor('vditor', {
            minHeight: 200,
            preview: {
                mode: "both",

            },
            mode: 'sv',
            value: state.model.readme,
            cache: {
                enable: false,
                id: 'markdown',
                after(markdown) {
                    state.model.readme = markdown;
                }
            },
            after() {
            },

        })
    })
}

const deleteModelById = () => {
    ElMessageBox.confirm('确定删除该模型吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteModel({id: state.model.id}).then(res => {
            if (res.code === 200) {
                router.push('/modelmag/model');
                ElMessage.success('删除成功');
            }
        })
    }).catch(() => {
    });
}

const deployModel = () => {
    state.visible = true;
    state.formInfo.modelId = state.model.id;
}

const createService = async () => {
    formRef.value.validate(async valid => {
        if (!valid) return;
        const res = await addService(state.formInfo);
        if (res.code === 200) {
            ElMessage({
                message: '创建成功,请等待部署',
                type: "success",
                duration: 1000,
                onClose: () => {
                    dialogClose();
                    router.push('/modelmag/service-list')
                }
            });
        }
    })

}

const dialogClose = () => {
    state.formInfo.modelId = null;
    state.formInfo.name = '';
    state.formInfo.description = '';
    state.visible = false;
    formRef.value.resetFields();
}

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
let pollingInterval = null;

let modelJsonUrl = ref('')
const tabChange =async (tab) => {
    if (tab === 3) {
        if (pollingInterval) {
            clearInterval(pollingInterval);
            pollingInterval = null; // 可选：将引用设置为 null 以表明没有轮询在运行
        }
        buildTree().then(treeData => {
            state.tree = treeData;
        })
    }else {
        if (tab === 2) {
            //todo 模型结构加载还没搞完
            // getArtifactData('json').then(res => {
            //     modelJsonUrl.value = res[0].basePath;
            // })
        }
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
            // 如果是文件，则提取文件名作为 title
            const parts = nodePath.split('/');
            title = parts[parts.length - 1];
            const treeNode = {
                title: title,
                key: nodePath,
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

const modelDevelop = () => {
    console.log(state.model,'model')
    developDialogRef.value.openDialog(state.model.id,state.model.secondaryScene);
}

// 处理回调
const handleCommit = () => {
    // queryModelDeployStatus(state.model.id)
}

const deleteJupyter = (row) => {
    ElMessageBox.confirm('确定删除该服务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteJupyterImage({id: row.id}).then(res => {
            if (res.code === 200) {
                queryModelDeployStatus(state.model.id);
                ElMessage.success('删除成功');
            }
        })
    }).catch(() => {
    });
}

// 查询模型可开发状态
const queryModelDeployStatus = async (id) => {
    const res = await getJupyterServiceById({modelId: id});
    if (res.code === 200) {
        state.jupyter = res.data;
    }
}

const startJupyter = async () => {
    // window.open(state.jupyter, '_blank')
    await queryModelDeployStatus(state.model.id);
    state.jupyter_dialog_visible = true;


}

const jupyterDialogClose = () => {
    state.jupyter_dialog_visible = false;
    state.jupyter = [];
}

onMounted(async () => {
    state.model.id = router.currentRoute.value.params.modelId;
    await getModel(state.model.id);
})
</script>

<template>
    <div class="model-detail-container">
        <!-- Content Section -->
        <div class="detail-content">
            <div class="content-card">
                <!-- Header Info inside Card -->
                <div class="card-header-section">
                    <el-breadcrumb :separator-icon="ArrowRight" class="breadcrumb">
                        <el-breadcrumb-item :to="{path: '/modelmag/model'}">模型空间</el-breadcrumb-item>
                        <el-breadcrumb-item>模型详情</el-breadcrumb-item>
                    </el-breadcrumb>

                    <div class="header-main">
                        <div class="title-row">
                            <div class="title-left">
                                <h1 class="model-name">{{ state.model.name }}</h1>
                                <el-button
                                    :icon="isPreview ? 'Edit' : 'Close'"
                                    link
                                    @click="editModel"
                                    v-hasPermi="['modelmag:model:edit']"
                                    class="edit-btn"
                                >
                                    {{ isPreview ? '编辑模型' : '退出编辑' }}
                                </el-button>
                            </div>
                            <div class="action-buttons">
                                <el-button type="primary" @click="modelDevelop" plain icon="Tools">模型开发</el-button>
                                <el-button type="success" @click="startJupyter" plain icon="Monitor">Jupyter</el-button>
                                <el-button type="primary" @click="deployModel" icon="Upload">部署</el-button>
                                <el-button type="danger" plain icon="Delete" @click="deleteModelById">删除</el-button>
                            </div>
                        </div>

                        <div class="meta-info">
                            <div class="meta-item">
                                <span class="label">Repo ID:</span>
                                <span class="value">{{ state.model.id }}</span>
                                <el-button link class="copy-btn">
                                    <template #icon><CopyOutlined /></template>
                                </el-button>
                            </div>
                            <div class="meta-item">
                                <span class="label">创建时间:</span>
                                <span class="value">{{ state.model.createTime }}</span>
                            </div>
                            <div class="meta-item">
                                <span class="label">作者:</span>
                                <span class="value"><UserOutlined class="mr-1"/>{{ state.model.author }}</span>
                            </div>
                        </div>

                        <div class="description">
                            {{ state.model.description || '暂无简介' }}
                        </div>

                        <div class="tags-section">
                            <template v-if="state.model.tagList && state.model.tagList.length > 0">
                                <el-tag
                                    v-for="tag in state.model.tagList.slice(0, 5)"
                                    :key="tag.id"
                                    effect="plain"
                                    class="model-tag"
                                >
                                    {{ tag.tagName }}
                                </el-tag>

                                <a-popover v-if="state.model.tagList.length > 5">
                                    <template #content>
                                        <div class="flex flex-wrap gap-2 max-w-[200px]">
                                            <el-tag v-for="tag in state.model.tagList.slice(5)" :key="tag.id" effect="plain">
                                                {{ tag.tagName }}
                                            </el-tag>
                                        </div>
                                    </template>
                                    <el-tag effect="plain" class="more-tag">
                                        <el-icon><More /></el-icon>
                                    </el-tag>
                                </a-popover>
                            </template>
                            <el-tag v-else effect="plain" type="info">暂无标签</el-tag>
                        </div>
                    </div>
                </div>

                <el-divider class="header-divider" />

                <a-tabs v-model:active-key="state.c_tab" @change="tabChange" class="custom-tabs">
                    <a-tab-pane :key="0" tab="模型介绍">
                        <div class="tab-pane-content markdown-body">
                            <div id="preview" v-if="isPreview"></div>
                            <div id="vditor" v-else></div>
                        </div>
                    </a-tab-pane>

                    <a-tab-pane :key="1" tab="模型基本信息">
                        <div class="tab-pane-content info-pane">
                            <a-descriptions :column="1" :label-style="labelStyle" bordered size="middle">
                                <a-descriptions-item label="模型应用场景">
                                    {{ formatScene(state.model.scene) }}
                                </a-descriptions-item>
                                <a-descriptions-item label="微调模型">
                                    {{ state.model.name }}
                                </a-descriptions-item>
                                <a-descriptions-item label="模型字典">
                                    {{ state.model.classDict || '-' }}
                                </a-descriptions-item>
                                <a-descriptions-item label="模型参数配置">
                                    <div class="params-list" v-if="state.model.paramsString && state.model.paramsString.length">
                                        <div v-for="(item, index) in state.model.paramsString" :key="index" class="param-item">
                                            <span class="param-key">{{ item.key }}</span>
                                            <span class="param-value">{{ item.value }}</span>
                                        </div>
                                    </div>
                                    <span v-else>-</span>
                                </a-descriptions-item>
                                <a-descriptions-item label="支持环境">
                                    <div class="flex flex-wrap gap-2">
                                        <template v-if="state.model.tagList && state.model.tagList.length > 0">
                                            <el-tag v-for="tag in state.model.tagList" :key="tag.id" effect="plain" size="small">
                                                {{ tag.tagName }}
                                            </el-tag>
                                        </template>
                                        <span v-else>-</span>
                                    </div>
                                </a-descriptions-item>
                                <a-descriptions-item label="模型类型">
                                    <a-badge v-if="state.model.isOfficial === 0" status="processing" text="非官方模型" />
                                    <a-badge v-else-if="state.model.isOfficial === 1" status="success" text="官方模型" />
                                    <span v-else>-</span>
                                </a-descriptions-item>
                                <a-descriptions-item label="输出路径">
                                    <span class="break-all">{{ state.model.fileUrl || '-' }}</span>
                                </a-descriptions-item>
                            </a-descriptions>
                        </div>
                    </a-tab-pane>

                    <a-tab-pane :key="2" tab="模型结构">
                        <div class="tab-pane-content structure-pane">
                            <a-empty description="暂不支持预览该模型结构" />
                            <!-- <CreateService :model-url="modelJsonUrl"/> -->
                        </div>
                    </a-tab-pane>

                    <a-tab-pane :key="3" tab="模型制品" v-if="state.model.isOfficial === 0 && JSON.stringify(state.artifacts.info) !== '{}'">
                        <div class="tab-pane-content artifacts-pane">
                            <div class="artifacts-layout">
                                <div class="tree-sidebar">
                                    <a-directory-tree
                                        v-if="state.tree.length"
                                        :multiple="false"
                                        :default-expand-all="true"
                                        :show-line="{ showLeafIcon: false }"
                                        :tree-data="state.tree"
                                        @select="selectTree"
                                        v-model:selected-keys="selectedKeys"
                                    ></a-directory-tree>
                                </div>

                                <div class="file-viewer">
                                    <div v-if="state.click_file_content !== ''" class="viewer-container">
                                        <div class="viewer-toolbar" v-if="allowUpload">
                                            <el-tooltip content="下载文件" placement="top">
                                                <el-button link @click="downloadFile">
                                                    <el-icon size="18"><Download /></el-icon> 下载
                                                </el-button>
                                            </el-tooltip>
                                        </div>
                                        <Codemirror
                                            ref="codeMirrorRef2"
                                            v-model:value="state.click_file_content"
                                            :autofocus="false"
                                            height="100%"
                                            :options="logOptions"
                                            placeholder="none"
                                            class="code-mirror-editor"
                                        ></Codemirror>
                                    </div>

                                    <div v-else-if="state.click_file_content==='' && fileToLarge && !rootFile" class="empty-state">
                                        <a-empty :image="warning" description="文件过大，无法预览 (需小于50M)" />
                                    </div>

                                    <div v-else-if="state.click_file_content===''&& rootFile && !fileToLarge" class="mlflow-view">
                                        <div class="mlflow-header">
                                            <h2>MLflow Model</h2>
                                            <p>下面的代码片段演示了如何使用日志模型进行预测。</p>
                                        </div>
                                        <div class="mlflow-content">
                                            <div class="mlflow-section">
                                                <h3>模型结构</h3>
                                                <a-table :columns="columns" :data-source="schema" :pagination="false" size="small" bordered></a-table>
                                            </div>
                                            <div class="mlflow-section">
                                                <h3>部署前验证</h3>
                                                <Codemirror
                                                    v-model:value="InputExample"
                                                    :autofocus="false"
                                                    height="300px"
                                                    :options="logOptions"
                                                    class="code-mirror-editor"
                                                ></Codemirror>
                                            </div>
                                        </div>
                                    </div>

                                    <div v-else class="empty-state">
                                        <a-empty :image="preview" description="请选择一个文件进行预览" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </a-tab-pane>
                </a-tabs>
            </div>
        </div>

        <!-- Dialogs -->
        <el-dialog @close="dialogClose" v-model="state.visible" title="部署服务" width="500px" destroy-on-close>
            <el-form ref="formRef" :model="state.formInfo" label-position="top" :rules="deploy_rules">
                <el-form-item label="服务名称" prop="name">
                    <el-input v-model="state.formInfo.name" placeholder="请输入服务名" maxlength="20" show-word-limit />
                    <div class="form-tip">支持中文、英文、数字、中划线(-)、下划线(_)，1-20个字符以内</div>
                </el-form-item>
                <el-form-item label="服务描述" prop="description">
                    <el-input type="textarea" :rows="4" placeholder="请输入服务描述" maxlength="300" show-word-limit v-model="state.formInfo.description" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogClose">取消</el-button>
                <el-button @click="createService" type="primary">一键部署</el-button>
            </template>
        </el-dialog>

        <el-dialog v-model="state.jupyter_dialog_visible" @close="jupyterDialogClose" title="Jupyter服务" width="800px">
            <el-table border :data="state.jupyter" stripe>
                <el-table-column label="服务名称" prop="serviceName" min-width="120" />
                <el-table-column label="镜像" min-width="150">
                    <template #default="scope">{{ scope.row?.jupyterImage?.name || '-' }}</template>
                </el-table-column>
                <el-table-column label="服务地址" min-width="200">
                    <template #default="scope">
                        <el-link :disabled="scope.row.status !== 2" target="_blank" :href="scope.row.serviceUrl" type="primary" :underline="false">
                            {{ scope.row.serviceUrl }}
                        </el-link>
                    </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" align="center" width="100">
                    <template #default="scope">
                        <a-tag v-if="scope.row.status === 1" color="processing"><template #icon><sync-outlined spin /></template>部署中</a-tag>
                        <a-tag v-if="scope.row.status === 2" color="success"><template #icon><check-circle-outlined /></template>成功</a-tag>
                        <a-tag v-if="scope.row.status === 3" color="error"><template #icon><close-circle-outlined /></template>失败</a-tag>
                        <a-tag v-if="scope.row.status === 4" color="warning"><template #icon><exclamation-circle-outlined /></template>暂停</a-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                    <template #default="scope">
                        <el-button type="danger" link icon="Delete" @click="deleteJupyter(scope.row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <template #footer>
                <el-button @click="jupyterDialogClose">关闭</el-button>
            </template>
        </el-dialog>

        <model-develop-dialog ref="developDialogRef"></model-develop-dialog>
    </div>
</template>

<style scoped lang="scss">
.model-detail-container {
    background-color: #f5f7fa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.detail-content {
    flex: 1;
    padding: 24px 40px;

    .content-card {
        max-width: 1400px;
        margin: 0 auto;
        background: #fff;
        border-radius: 8px;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
        min-height: 600px;
    }
}

.card-header-section {
    padding: 24px 32px 0;

    .breadcrumb {
        margin-bottom: 20px;
    }

    .header-main {
        .title-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;

            .title-left {
                display: flex;
                align-items: center;
                gap: 12px;

                .model-name {
                    font-size: 24px;
                    font-weight: 600;
                    color: #1f2f3d;
                    margin: 0;
                }
            }
        }

        .meta-info {
            display: flex;
            gap: 24px;
            font-size: 13px;
            color: #606266;
            margin-bottom: 12px;

            .meta-item {
                display: flex;
                align-items: center;
                gap: 6px;

                .label {
                    color: #909399;
                }
                .value {
                    font-weight: 500;
                }
            }
        }

        .description {
            font-size: 14px;
            color: #606266;
            line-height: 1.6;
            margin-bottom: 16px;
            max-width: 800px;
        }

        .tags-section {
            display: flex;
            gap: 8px;
            align-items: center;
        }
    }
}

.header-divider {
    margin: 24px 0 0;
}

.tab-pane-content {
    padding: 24px;
    min-height: 400px;
}

.info-pane {
    max-width: 1000px;
}

.params-list {
    border: 1px solid #ebeef5;
    border-radius: 4px;

    .param-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 12px;
        border-bottom: 1px solid #ebeef5;
        font-size: 13px;

        &:last-child {
            border-bottom: none;
        }

        &:nth-child(even) {
            background-color: #fafafa;
        }

        .param-key {
            color: #606266;
        }
        .param-value {
            font-weight: 500;
            color: #303133;
        }
    }
}

.structure-pane {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 400px;
}

.artifacts-pane {
    height: 800px;
    padding: 0;
}

.artifacts-layout {
    display: flex;
    height: 100%;
    border-top: 1px solid #f0f0f0;

    .tree-sidebar {
        width: 300px;
        border-right: 1px solid #f0f0f0;
        overflow-y: auto;
        padding: 16px;
        background: #fafafa;
    }

    .file-viewer {
        flex: 1;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        background: #fff;

        .viewer-container {
            height: 100%;
            display: flex;
            flex-direction: column;
        }

        .viewer-toolbar {
            padding: 8px 16px;
            border-bottom: 1px solid #f0f0f0;
            display: flex;
            justify-content: flex-end;
        }

        .code-mirror-editor {
            flex: 1;
            overflow: auto;
        }

        .empty-state {
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .mlflow-view {
            padding: 24px;
            overflow-y: auto;
            height: 100%;

            .mlflow-header {
                margin-bottom: 24px;
                h2 {
                    margin-bottom: 8px;
                    font-size: 20px;
                }
                p {
                    color: #606266;
                }
            }

            .mlflow-content {
                display: flex;
                gap: 24px;

                .mlflow-section {
                    flex: 1;
                    h3 {
                        font-size: 16px;
                        margin-bottom: 12px;
                        font-weight: 600;
                    }
                }
            }
        }
    }
}

.form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}

.break-all {
    word-break: break-all;
}

:deep(.ant-tabs-nav) {
    padding: 0 24px;
    margin: 0;
}
</style>
