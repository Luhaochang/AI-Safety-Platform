<script setup>

import {deployJupyterImage, getJupyterImage} from "@/api/modelMag/jupyter.js";
import {ElMessage} from "element-plus";
import DatasetSelectDialog from "@/views/dataset/components/datasetSelectDialog.vue";

const formRef = ref()
const emits = defineEmits(['commit-emit'])
const datasetSelectDialogRef = ref()
const state = reactive({
    visible: false,
    formInfo: {
        id: null,
        name: '',
        jupyterId: '',
        modelId: '',
        datasetId: '',
        datasetName: ''
    },
    sec_sence: null,
    imageList: []
})

const rules = {
    name: [{required: true, trigger: 'change', message: '请输入名称'}],
    jupyterId: [{required: true, trigger: 'change', message: '请选择镜像'}],
    datasetName: [{required: true, trigger: 'change', message: '请选择数据集'}]
}

const openDialog = (modelId,scene) => {
    state.visible = true;
    state.formInfo.modelId = modelId;
    state.sec_sence = scene;
    getImages()
}

const dialogClose = () => {
    state.visible = false;
    state.formInfo.name = '';
    state.formInfo.id = null;
    state.formInfo.jupyterId = '';
    state.formInfo.datasetName = '';
    state.formInfo.datasetId = '';

    setTimeout(function () {
        formRef.value.clearValidate();
    }, 30);
}

const getImages = async () => {
    const res = await getJupyterImage();
    if (res.code === 200) {
        state.imageList = res.data;
    }
}

const submit = () => {
    formRef.value.validate(async valid => {
        if (!valid) return;
        const res = await deployJupyterImage(state.formInfo);
        if (res.code === 200) {
            ElMessage.success('正在构建，请稍后');
            dialogClose()
            // emits('commit-emit')
        }
    })
}

const openSelectDialog = () => {
  datasetSelectDialogRef.value.openDialog();
}

const handleSelectDataset = (val) => {
    const {id, dataSetName} = val;
    state.formInfo.datasetId = id;
    state.formInfo.datasetName = dataSetName;
}

defineExpose({
    openDialog
})
</script>

<template>
    <el-dialog
        width="30%"
        title="模型开发"
        @close="dialogClose"
        style="height: 300px"
        v-model="state.visible">
        <el-form ref="formRef" :model="state.formInfo" label-position="right" :rules="rules" label-width="auto">
            <el-form-item label="镜像名称" prop="name">
                <el-input v-model="state.formInfo.name"></el-input>
            </el-form-item>

            <el-form-item label="jupyter镜像" prop="jupyterId">
                <el-select v-model="state.formInfo.jupyterId" >
                    <el-option v-for="item in state.imageList" :value="item.id" :label="item.name"></el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="数据集" prop="datasetName">
                <el-input v-model="state.formInfo.datasetName" readonly>
                    <template #append>
                        <el-button icon="search" @click="openSelectDialog"></el-button>
                    </template>
                </el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="custom-footer">
                <el-button type="primary" @click="submit">确认</el-button>
                <el-button @click="dialogClose">取消</el-button>
            </div>
        </template>

        <dataset-select-dialog ref="datasetSelectDialogRef" :is-marked="2" :scene="state.sec_sence" @emit-commit="handleSelectDataset"></dataset-select-dialog>
    </el-dialog>
</template>

<style scoped lang="scss">
.custom-footer {
    //position: absolute;
    //bottom: 0;
    width: 98%;
    display: flex;
    justify-content: flex-end; /* 如果按钮需要靠右 */
    //padding: 20px; /* 适当的内边距 */
    background-color: #fff; /* 确保与对话框背景一致 */
}
</style>
