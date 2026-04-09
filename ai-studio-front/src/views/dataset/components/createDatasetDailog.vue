<script setup>
import {UploadOutlined} from '@ant-design/icons-vue';
import {ElMessage, genFileId} from 'element-plus'
import request from "@/utils/request.js";
import JSZip from "jszip";
import video from '@/assets/icons/svg/video.svg'
import audio from '@/assets/icons/svg/audio.svg'
import other from '@/assets/icons/svg/其他.svg'
import xml from '@/assets/icons/svg/xml.svg'
import img from '@/assets/icons/svg/image.svg'
import folder from '@/assets/icons/svg/24gl-folder2.svg'
import {addDataset, updateDataset, uploadDataSet, uploadFile} from "@/api/dataMag/dataset.js";
import {resetForm} from "@/utils/ruoyi.js";
import {useRouter} from "vue-router";
import {getSonCategory} from "@/api/modelMag/label.js";
import {getAllAppScene, getAllTaskType, getChildScene} from "@/api/modelMag/scene.js";

const router = useRouter()
const upload = ref()
const zip = new JSZip();
const emits = defineEmits(['commit'])
const props = {
    lazy: true,
    multiple: true,

    lazyLoad(node, resolve) {
        const {data} = node;
        if (data && data.value) {
            const nodes = [];
            //查询接口
            getSonCategory({superId: data.value}).then(res => {
                res.data.map((item) => {
                    let obj = {
                        value: item.id,
                        label: item.tagName,
                        leaf: true
                    }
                    nodes.push(obj);
                })
                //重新加载节点
                resolve(nodes);
            })
        } else {
            resolve([])
        }

    },
}

const options = ref([
    {
        label: '任务场景',
        options: [],
    },
]);
const dialogRef = ref()
const fileList = ref([])
const protocolList = [
    { label: '公共领域(CCO)', value: 0 },
    { label: '署名(CC BY 4.0)', value: 1 },
    { label: '开源使用/非商用(GPL 2)', value: 2 },
    { label: '其他', value: 3 },
]

const typeOpt = reactive([
    { label: '图片', value: 1 },
    { label: '文本', value: 2 },
    { label: '表格', value: 3 },
])

const checkFileUrl = (rule, value, cb) => {
    if (fileList.value.length === 0) {
        cb(new Error('请上传数据集'))
    } else {
        cb()
    }
};
const rules = {
    dataSetName: [{required: true, trigger: 'change', message: '请输入数据集名称'}],
    dataSetAbstract: [{required: true, trigger: 'change', message: '请输入数据集简介'}],
    isPublic: [{required: true, trigger: 'change', message: '请选择是否公开'}],
    label: [{required: true, trigger: 'change', message: '请选择任务场景'}],
    isMarked: [{required: true, trigger: 'change', message: '请选择是否标注'}],
    scene: [{required: true, trigger: 'change', message: '请选择应用场景'}],
    dataType: [{required: true, trigger: 'change', message: '请选择数据类型'}],
}


const state = reactive({
    visible: false,
    formInfo: {
        label: null,
        dataType: 1, // 1: Image, 2: Text, 3: Table
        "agreement": 0,
        "dataSetAbstract": "",
        "dataSetName": "",
        "dateAuthor": "",
        "fileUrl": "",
        "id": 0,
        "isMarked": 0,
        "isPublic": 0,
        scene: []
    },
    customLabel: false,
    progress: {
        strokeColor: {
            '0%': '#108ee9',
            '100%': '#87d068',
        },
        strokeWidth: 3,
        format: percent => `${parseFloat(percent.toFixed(2))}%`,
        class: 'test',
    },
    treeInfo: {
        expandedKeys: [],
        selectedKeys: [],
        temp: []
    }

})
const visible = ref(false);
let custom = ref('')
const title = ref('')
const openDialog = () => {
    state.visible = true;
    title.value = '创建'
    getTaskTypes();
    getAppScenes();
}

const setForm = async (obj) => {
    title.value = '修改'

    for (let key in state.formInfo) {
        if (obj.hasOwnProperty(key)) {
            state.formInfo[key] = obj[key];
        }
    }

    // 先加载选项，以便正确回显
    await getTaskTypes();
    getAppScenes();

    // 处理 label (任务场景)
    // 优先使用 taskTypeId
    if (obj.taskTypeId) {
        state.formInfo.label = obj.taskTypeId;
    } else {
        // 如果没有 taskTypeId，尝试通过 tag 反查 ID
        let tagName = '';
        if (Array.isArray(obj.tag) && obj.tag.length > 0) {
            tagName = obj.tag[0];
        } else if (typeof obj.tag === 'string') {
            try {
                const parsed = JSON.parse(obj.tag);
                if (Array.isArray(parsed) && parsed.length > 0) tagName = parsed[0];
                else tagName = obj.tag;
            } catch (e) {
                tagName = obj.tag;
            }
        }

        if (tagName) {
            const found = options.value[0].options.find(opt => opt.label === tagName);
            if (found) {
                state.formInfo.label = found.value;
            } else {
                // 如果找不到，可能需要用户重新选择，或者暂时显示 tagName (但 select 绑定的是 ID，显示会是 tagName 字符串)
                // 这里暂时置空，强制用户重新选择，或者保留原值
                state.formInfo.label = null;
            }
        } else {
            state.formInfo.label = null;
        }
    }

    // 如果 scene 是字符串，尝试解析为数组，或者保持原样（取决于后端返回格式）
    // 这里假设后端返回的是逗号分隔的字符串或者已经是数组
    if (typeof obj.appSceneId === 'string') {
        state.formInfo.scene = obj.appSceneId.split(',').map(Number); // 假设是ID列表
    } else if (Array.isArray(obj.appSceneId)) {
        state.formInfo.scene = obj.appSceneId;
    } else {
        state.formInfo.scene = [];
    }

    state.visible = true;
}

const dialogClose = () => {
    state.visible = false;
    fileList.value = [];
    submitFileList.value = [];
    state.formInfo = {
        label: null,
        dataType: 1,

        "agreement": 0,
        "dataSetAbstract": "",
        "dataSetName": "",
        "dateAuthor": "",
        "fileUrl": "",
        "isMarked": 0,
        "isPublic": 0,
        id: null,
        scene: []
    }
    state.treeInfo.temp = [];
    dialogRef.value.resetFields()

}

const setVisible = (value) => {
    visible.value = value;
}

const customLabel = () => {
    state.formInfo.label.push(custom.value);
    custom.value = '';
    state.customLabel = false;
}

const deleteTag = (val, index) => {
    state.formInfo.label = state.formInfo.label.filter(tag => tag !== val);
}

let submitFileList = ref([]);

const submit = () => {
    dialogRef.value.validate(async (valid) => {
        if (!valid) return;

        // 处理多选的 scene，将其转换为逗号分隔的字符串或者保持数组（取决于后端接口要求）
        // 这里假设后端需要逗号分隔的字符串
      const sceneValue = Array.isArray(state.formInfo.scene)
          ? JSON.stringify(state.formInfo.scene)
          : JSON.stringify([state.formInfo.scene]);

        // 获取选中的任务场景名称
        const selectedOption = options.value[0].options.find(opt => opt.value === state.formInfo.label);
        const tagName = selectedOption ? selectedOption.label : '';
        const taskTypeId = state.formInfo.label;

        const payload = {
            "agreement": state.formInfo.isPublic === 0 ? null : state.formInfo.agreement,
            "dataSetAbstract": state.formInfo.dataSetAbstract,
            "dataSetName": state.formInfo.dataSetName,
            "dateAuthor": state.formInfo.isPublic === 0 ? null : state.formInfo.dateAuthor,
            "isMarked": state.formInfo.isMarked,
            "isPublic": state.formInfo.isPublic,
            "tag": JSON.stringify([tagName]), // 传名称
            "taskTypeId": taskTypeId, // 传ID
            "appSceneId": sceneValue,
            "dataType": state.formInfo.dataType // Pass the data type
        };

        switch (title.value) {
            case '创建':
                addDataset(payload).then(async res => {
                    if (res.code === 200) {
                        dialogClose();
                        ElMessage.success('添加成功');
                        emits('commit');

                    }
                }).catch(err => {
                })
                break;
            case '修改':
                updateDataset({
                    id: state.formInfo.id,
                    ...payload
                }).then(res => {
                    if (res.code === 200) {
                        dialogClose();
                        ElMessage.success('修改成功');
                        emits('commit')
                    }
                }).catch(err => {
                })

                break;
        }

    })
}

const handleSelectScene = (value) => {
    // value 是一个数组，包含所有选中的节点的 value（因为 emitPath: false）
    state.formInfo.scene = value;
}

const handleTypeChange = (val) => {
    state.formInfo.dataType=val;
}

const cascader = ref()

const SecSceneOptions = ref([]);

const getTaskTypes = async () => {
    const res = await getAllTaskType();
    if (res.code === 200) {
        options.value[0].options = res.data.map(item => ({
            value: item.id,
            label: item.taskName
        }));
    }
}

const getAppScenes = async () => {
    const res = await getAllAppScene();
    if (res.code === 200) {
        const groups = {};
        res.data.forEach(item => {
            if (!groups[item.industry]) {
                groups[item.industry] = [];
            }
            groups[item.industry].push(item);
        });

        SecSceneOptions.value = Object.keys(groups).map(industry => ({
            value: industry,
            label: industry,
            children: groups[industry].map(app => ({
                value: app.id,
                label: app.appName,
                leaf: true
            }))
        }));
    }
}

const cascaderProps = {
    lazy: false,
    multiple: true, // 开启多选
    emitPath: false,
}

const getTagName = (val) => {
    const found = options.value[0].options.find(opt => opt.value === val);
    if (found) {
        return found.label;
    }
    return val;
}

defineExpose({
    openDialog,
    setForm
})
</script>

<template>
    <el-dialog
        v-model="state.visible"
        width="600"
        @close="dialogClose"
    >
        <template #header>
            <div class="m-0 text-center"
                 style="color: rgba(0, 0, 0, .85);padding: 10px 24px;border-bottom: 1px solid #f0f0f0;border-radius: 6px 6px 0 0;">
                <div class="title">
                    <span>{{ title + '数据集' }}</span>
                </div>
            </div>
        </template>

        <template #default>
            <div class=" mt-5" style="padding: 0  50px 0px">


                <el-form ref="dialogRef" :model="state.formInfo" label-width="auto" :rules="rules">
                    <el-form-item label="数据集名称" prop="dataSetName">
                        <el-input v-model="state.formInfo.dataSetName"></el-input>
                    </el-form-item>

                    <el-form-item label="数据类型" prop="dataType">
                        <el-radio-group v-model="state.formInfo.dataType" @change="handleTypeChange">
                            <el-radio-button v-for="item in typeOpt" :key="item.value" :value="item.value">{{ item.label }}</el-radio-button>
                        </el-radio-group>
                    </el-form-item>

                    <el-form-item label="任务场景" prop="label">
                        <el-select v-model="state.formInfo.label" placeholder="Select"
                                   style="width: 100%">
                            <el-option-group
                                v-for="group in options"
                                :key="group.label"
                                :label="group.label"
                            >
                                <div style="display: flex;flex-wrap: wrap;max-width: 500px">
                                    <el-option
                                        v-for="item in group.options"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value"
                                    >
                                        <template #default>
                                            <div class="flex">{{ item.label }}</div>
                                        </template>
                                    </el-option>
                                </div>

                            </el-option-group>
                        </el-select>
                    </el-form-item>

                    <el-form-item label="应用场景" prop="scene" >
                        <el-cascader
                            clearable
                            ref="cascader"
                            @change="handleSelectScene"
                            :props="cascaderProps"
                            v-model="state.formInfo.scene" :options="SecSceneOptions"
                            collapse-tags
                            collapse-tags-tooltip
                        />
                    </el-form-item>

                    <el-form-item label="标注状态" prop="isMarked" v-if="state.formInfo.dataType !== 5">
                        <el-radio-group v-model="state.formInfo.isMarked">
                            <el-radio-button :value="0">未标注</el-radio-button>
                            <el-radio-button :value="2">已标注</el-radio-button>
                        </el-radio-group>
                    </el-form-item>

                    <el-form-item label="是否公开" prop="isPublic">
                        <el-radio-group v-model="state.formInfo.isPublic">
                            <el-radio :value="0">否</el-radio>
                            <el-radio :value="1">是</el-radio>
                        </el-radio-group>
                        <div style="color:#a6a6a6; font-size: 12px; line-height: 1.5; margin-top: 5px;">数据集公开后可被其他用户访问，但不可删除或转为私有</div>
                    </el-form-item>

                    <el-form-item label="开源协议" v-if="state.formInfo.isPublic===1">
                        <el-select v-model="state.formInfo.agreement">
                            <el-option v-for="item in protocolList" :label="item.label" :value="item.value"></el-option>
                        </el-select>
                    </el-form-item>

                    <el-form-item label="数据集作者" v-if="state.formInfo.isPublic===1">
                        <el-input v-model="state.formInfo.dateAuthor" placeholder="请填写数据集所属机构/个体">
                        </el-input>
                    </el-form-item>

                    <el-form-item label="简介摘要" prop="dataSetAbstract">
                        <el-input type="textarea" :rows="4" v-model="state.formInfo.dataSetAbstract"
                                  placeholder="此数据集包含什么内容，可做什么用，最多200字"></el-input>
                    </el-form-item>

                </el-form>


            </div>
        </template>

        <template #footer>
            <el-button type="primary" @click="submit">确认</el-button>
            <el-button @click="dialogClose">取消</el-button>
        </template>

    </el-dialog>
</template>

<style scoped lang="scss">
.title {
    margin: 0;
    color: rgba(0, 0, 0, .85);
    font-weight: 500;
    font-size: 16px;
    line-height: 22px;
    word-wrap: break-word;
}
</style>
