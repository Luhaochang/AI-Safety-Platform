<script setup>
import {UploadOutlined, FileTextOutlined, TableOutlined, ImportOutlined} from '@ant-design/icons-vue';
import {ElMessage, genFileId} from 'element-plus'
import request from "@/utils/request.js";
import JSZip from "jszip";
import video from '@/assets/icons/svg/video.svg'
import audio from '@/assets/icons/svg/audio.svg'
import other from '@/assets/icons/svg/其他.svg'
import xml from '@/assets/icons/svg/xml.svg'
import img from '@/assets/icons/svg/image.svg'
import folder from '@/assets/icons/svg/24gl-folder2.svg'
import {addDataset, checkDataset, updateDataset, uploadDataSet, uploadFile} from "@/api/dataMag/dataset.js";
import {resetForm} from "@/utils/ruoyi.js";
import {useRouter} from "vue-router";
import {getSonCategory} from "@/api/modelMag/label.js";

const router = useRouter()
const upload = ref()
const emits = defineEmits(['commit','startLoad'])

const dialogRef = ref()
const fileList = ref([])

const rules = {
    fileUrl: [{required: true, trigger: 'change', validator: (rule, value, cb) => {
        if (fileList.value.length === 0) {
            cb(new Error('请上传文件'))
        } else {
            cb()
        }
    }}],
}

const state = reactive({
    visible: false,
    mode: 'upload', // 'upload' | 'importPre'
    formInfo: {
        label: [],
        dataType: 1, // 1: Image, 2: Text, 3: Table
        "agreement": 0,
        "dataSetAbstract": "",
        "dataSetName": "",
        "dateAuthor": "",
        "fileUrl": "",
        "id": 0,
        "isMarked": 0,
        "isPublic": 0,
        scene: null
    },
    progress: {
        strokeColor: {
            '0%': '#108ee9',
            '100%': '#87d068',
        },
        strokeWidth: 3,
        format: percent => `${parseFloat(percent.toFixed(2))}%`,
        class: 'test',
    },
})

let checked = ref(false)
const title = ref('')

const setForm = (obj, mode = 'upload') => {
    state.mode = mode;
    title.value = mode === 'upload' ? '上传' : '导入预标注';

    state.formInfo = {
        label: [],
        dataType: 1,
        "agreement": 0,
        "dataSetAbstract": "",
        "dataSetName": "",
        "dateAuthor": "",
        "fileUrl": "",
        "isMarked": 0,
        "isPublic": 0,
        id: null
    }

    for (let key in state.formInfo) {
        if (obj.hasOwnProperty(key)) {
            state.formInfo[key] = obj[key];
        }
    }
    // Ensure type is set correctly from the dataset object, default to 1 if missing
    state.formInfo.dataType = obj.dataType || 1;
    state.formInfo.label = obj.tag;
    fileList.value = [];
    submitFileList.value = [];

    state.visible = true;
}

const dialogClose = () => {
    state.visible = false;
    dialogRef.value.resetFields()
}

let submitFileList = ref([]);

const beforeUpload = (file) => {
    if (state.mode === 'importPre') {
        // For pre-annotations, we expect txt files (YOLO format usually)
        const isTxt = /\.(txt)$/i.test(file.name);
        if (!isTxt) {
            ElMessage.error('仅支持 txt 格式文件');
            return false;
        }
        return true;
    }

    // For text/table, we might want to limit file types
    if (state.formInfo.dataType === 2 || state.formInfo.dataType === 3) {
        const isAllowed = /\.(txt|json|csv|xlsx|xls)$/i.test(file.name);
        if (!isAllowed) {
            ElMessage.error('仅支持 txt, json, csv, xlsx 格式文件');
            return false; // Stop upload
        }
    }
    // For images, we usually handle directory upload which is handled by customRequest
    return true;
}

// 解析文件夹 (Image) or Files (Text/Table/Pre-annotation)
const parseDir = async (e) => {
    let temp = e.file;
    submitFileList.value.push(temp);
}

// 上传数据集
const uploadDataSetByPath = async (path) => {
    let formData;
    for (const file of submitFileList.value) {
        formData = new FormData();
        formData.append('file', file);
        await uploadDataSet(formData, path)
    }
}

// 上传数据集json文件 (Only for Image datasets usually)
const generateJsonFile = async () => {
    if (state.formInfo.dataType !== 1) return; // Skip for text/table

    const jsonData = fileList.value.map(item => ({
        image_name: item.name,
        type: ''
    }))

    const jsonString = JSON.stringify(jsonData, null, 2);
    let file = new File([new Blob([jsonString], {type: 'text/json'})], 'file.json')

    let formData = new FormData();
    formData.append('file', file);

    await uploadDataSet(formData, state.formInfo.fileUrl);
}

const submit = () => {
    dialogRef.value.validate(async (valid) => {
        if (!valid) return;

        if (checked.value) {
            await checkDataset({id: state.formInfo.id,isChecked: 1});
        }

        dialogClose();

        ElMessage.info('正在上传中，请勿关闭界面');
        emits('startLoad')

        if (state.mode === 'importPre') {
            // Import Pre-annotations: Upload to /labels folder
            await uploadDataSetByPath(`${state.formInfo.fileUrl}/labels`);
        } else {
            // Normal Upload
            if (state.formInfo.dataType === 1) {
                // Image Dataset Logic
                await generateJsonFile();
                await uploadDataSetByPath(`${state.formInfo.fileUrl}/images`);
            } else {
                // Text/Table Dataset Logic: Upload directly to root or specific folder
                // Assuming backend handles file parsing based on type
                await uploadDataSetByPath(`${state.formInfo.fileUrl}`);
            }
        }

        emits('commit');
    })
}

defineExpose({
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
                    <span>{{ title + (state.mode === 'upload' ? '数据集' : '') }}</span>
                </div>
            </div>
        </template>

        <template #default>
            <div class=" mt-5" style="padding: 0  50px 0px">
                <el-form ref="dialogRef" :model="state.formInfo" label-width="auto" :rules="rules">
                    <el-form-item label="数据集名称" prop="dataSetName">
                        <el-input v-model="state.formInfo.dataSetName" readonly></el-input>
                    </el-form-item>

                    <el-form-item label="简介摘要" prop="dataSetAbstract">
                        <el-input type="textarea" :rows="4" v-model="state.formInfo.dataSetAbstract" readonly
                                  placeholder="此数据集包含什么内容，可做什么用，最多200字"></el-input>
                    </el-form-item>

                    <el-form-item :label="state.mode === 'upload' ? '上传数据集' : '上传标注文件'" prop="fileUrl">
                        <!-- Image Dataset: Directory Upload -->
                        <a-upload
                            v-if="state.mode === 'upload' && state.formInfo.dataType === 1"
                            ref="upload"
                            action="#"
                            v-model:file-list="fileList"
                            :custom-request="parseDir"
                            :before-upload="beforeUpload"
                            :progress="state.progress"
                            directory>
                            <el-button>
                                <template #icon>
                                    <UploadOutlined/>
                                </template>
                                上传图片文件夹
                            </el-button>
                            <span class=" text-xs ml-2 text-gray-400">
                                ( 请选择包含图片的文件夹 )
                            </span>
                        </a-upload>

                        <!-- Text/Table Dataset OR Pre-annotation: File Upload -->
                        <a-upload
                            v-else
                            ref="upload"
                            action="#"
                            v-model:file-list="fileList"
                            :custom-request="parseDir"
                            :before-upload="beforeUpload"
                            :progress="state.progress"
                            multiple>
                            <el-button>
                                <template #icon>
                                    <ImportOutlined v-if="state.mode === 'importPre'"/>
                                    <FileTextOutlined v-else-if="state.formInfo.dataType === 2"/>
                                    <TableOutlined v-else/>
                                </template>
                                {{ state.mode === 'importPre' ? '上传标注文件' : '上传文件' }}
                            </el-button>
                            <span class=" text-xs ml-2 text-gray-400">
                                {{ state.mode === 'importPre' ? '( 支持 .txt 格式 )' : '( 支持 .txt, .json, .csv, .xlsx )' }}
                            </span>
                        </a-upload>

                        <!-- File List Display -->
                        <div class="mt-2">
                             <template v-if="fileList.length > 0">
                                <el-space v-if="fileList.length < 10" direction="vertical" alignment="start">
                                    <div v-for="file in fileList" :key="file.uid" class="flex items-center gap-2">
                                        <el-icon :size="15"><CollectionTag/></el-icon>
                                        <span :style="file.status === 'error' ? 'color: red' : ''">{{ file.name }}</span>
                                        <el-button link icon="delete" @click="() => { const idx = fileList.indexOf(file); if(idx > -1) fileList.splice(idx, 1); }"></el-button>
                                    </div>
                                </el-space>

                                <el-space v-else :direction="'vertical'" alignment="flex-start" :size="0">
                                    <div v-for="index in 10" :key="index" class="flex items-center gap-2">
                                        <el-icon :size="15"><CollectionTag/></el-icon>
                                        <span :style="fileList[index-1].status === 'error' ? 'color: red' : ''">{{ fileList[index - 1].name }}</span>
                                        <el-button link icon="delete" @click="() => fileList.splice(index-1,1)"></el-button>
                                    </div>
                                    <div class="text-gray-400 pl-6">... 等 {{ fileList.length }} 个文件</div>
                                </el-space>
                             </template>
                        </div>
                    </el-form-item>

                    <el-form-item label="是否已校验数据" v-if="state.formInfo.isMarked===2 && state.mode === 'upload'">
                        <a-checkbox v-model:checked="checked" >{{`${checked ? '是' : '否'}`}}</a-checkbox>
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
