<script setup>
import {AimOutlined, TableOutlined, FontSizeOutlined} from "@ant-design/icons-vue";
import {useRouter} from "vue-router";
import datasetSelectDialog from "@/views/dataset/components/datasetSelectDialog.vue";
import {addTask} from "@/api/dataMag/markTask.js";
import {ElMessage} from "element-plus";
import JSZip from "jszip";
import {getDatasetByPath} from "@/api/dataMag/dataset.js";

const zip = new JSZip();
const formRef = ref()
const datasetSelectDialogRef = ref()
const router = useRouter()

const rules = computed(() => {
    const r = {
        taskName: [{required: true, message: "名称不能为空", trigger: "change"}],
        dataType: [{required: true, message: "请选择标注类型", trigger: "change"}],
        dataSetName: [{required: true, message: "请选择数据集", trigger: "change"}],
    }
    if (state.subForm.dataType === 1) {
        r.labels = [{required: true, message: '请至少选择一个标签', trigger: "change"}]
    }
    return r
})

const markTypes = [
    { label: '图片数据集', value: 1, icon: AimOutlined, desc: '支持YOLO格式' },
    { label: 'JSON数据集', value: 2, icon: FontSizeOutlined, desc: 'JSON格式数据' },
    { label: 'CSV数据集', value: 3, icon: TableOutlined, desc: 'CSV格式数据' }
]

const activeType = ref('')

const state = reactive({
    subForm: {
        id: 0,
        "taskName": "",
        taskDescription: '',
        dataType: null,
        labels: [],
        dataSetId: null,
        dataSetName: '',
        dataSetCount: 0,
        label: ''
    }
})

const handleSelectType = (typeItem) => {
    activeType.value = typeItem.value;
    state.subForm.labels = [];
    state.subForm.dataType = typeItem.value;

    if (typeItem.value === 1) {
        // Initialize with one empty label for convenience
        // state.subForm.labels.push({name: '', color: '#409EFF'});
    }
    formRef.value.validateField(['dataType']);
}

const openSelectDialog = (ref) => {
    switch (ref) {
        case 'datasetSelectDialogRef':
            datasetSelectDialogRef.value.openDialog();
            break;
    }
}

const handleDatasetSelect = async (val) => {
    state.subForm.dataSetId = val.id;
    state.subForm.dataSetName = val.dataSetName;

    if (val.fileUrl) {
        const res = await getDatasetByPath({catalogue: `${val.fileUrl}`});
        if (res.code === 200 && res.data && res.data.length !== 0) {

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
            if (jsonUrls.length > 0) {
                const temp  = await (await fetch(jsonUrls[0])).json();
                state.subForm.dataSetCount = temp.length;
            } else {
                 state.subForm.dataSetCount = res.data.length;
            }

        } else {
            ElMessage.warning('当前绑定数据集仍未上传数据');
            state.subForm.dataSetCount = 0;
            return;
        }
    }
}

const goBack = () => {
    router.push('/datamag/mark-task')
}

const addLabels = () => {
    state.subForm.labels.push({
        name: '',
        color: '#409EFF'
    });
}

const removeTag = (index) => {
    state.subForm.labels.splice(index, 1);
}

const onSubmit = () => {
    formRef.value.validate(valid => {
        if (!valid) return;
        if (state.subForm.dataSetCount === 0) {
            ElMessage.warning('请先上传数据集');
            return;
        }
        addTask({
            "dataSetId": state.subForm.dataSetId,
            "label": JSON.stringify(state.subForm.labels),
            "dataType": state.subForm.dataType,
            "taskDescription": state.subForm.taskDescription,
            "taskName": state.subForm.taskName,
            "setNumber": state.subForm.dataSetCount
        })
            .then(res => {
                if (res.code === 200) {
                    ElMessage.success('新增成功');
                    goBack()
                }
            })
    })
}
</script>

<template>
    <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
        <div class="flex-1 overflow-x-hidden min-w-800 ">
            <div class="h-full min-w-800">
                <div class="h-full py-6 px-8 w-full"
                     style="background: linear-gradient(180deg, #F7F9FF 0%, #F2F5FE 100%);">
                    <a-page-header
                        title="新建标注任务"
                        @back="goBack"></a-page-header>
                    <div class="w-full m-auto">
                        <div
                            class="min-w-min w-full bg-white rounded-lg mb-4 flex items-center justify-between hover-shadow cursor-pointer"
                            style="padding: 18px 24px 20px">
                            <el-form :model="state.subForm" label-position="left" label-width="auto" :rules="rules"
                                     ref="formRef">
                                <el-form-item label="任务名称" prop="taskName">
                                    <div class="flex flex-col">
                                        <el-input class="w-[100%]" v-model="state.subForm.taskName"
                                                  placeholder="请输入任务名称"></el-input>
                                        <div class="mb-0 text-zinc-500" style="font-size: 11px;">
                                            请输入不超过60个字符，仅支持中英文、数字、下划线_、短横-，只能以中英文、数字开头
                                        </div>
                                    </div>
                                </el-form-item>
                                <el-form-item label="任务描述">
                                    <el-input type="textarea" v-model="state.subForm.taskDescription" :rows="4"
                                              class="w-[300px]"
                                              placeholder="请输入不超过200字的任务描述" maxlength="200"
                                              show-word-limit></el-input>
                                </el-form-item>

                                <el-form-item label="选择数据集" prop="dataSetName">
                                    <el-input class="w-[250px]" readonly placeholder="请选择数据集"
                                              v-model="state.subForm.dataSetName">
                                        <template #append>
                                            <el-button icon="search"
                                                       @click="openSelectDialog('datasetSelectDialogRef')"></el-button>
                                        </template>
                                    </el-input>
                                </el-form-item>

                                <el-form-item label="标注类型" prop="dataType">
                                    <div class="flex flex-row gap-4">
                                        <div v-for="item in markTypes" :key="item.value"
                                             @click="handleSelectType(item)"
                                             class="cursor-pointer border rounded-lg p-4 flex flex-col items-center justify-center gap-2 transition-all"
                                             :class="{'border-blue-500 bg-blue-50': activeType === item.value, 'border-gray-200 hover:border-blue-300': activeType !== item.value}"
                                             style="width: 160px; height: 120px;">
                                            <component :is="item.icon" class="text-3xl" :class="activeType === item.value ? 'text-blue-500' : 'text-gray-400'"/>
                                            <div class="font-medium" :class="activeType === item.value ? 'text-blue-600' : 'text-gray-700'">{{ item.label }}</div>
                                            <div class="text-xs text-gray-400 text-center">{{ item.desc }}</div>
                                        </div>
                                    </div>
                                </el-form-item>

                                <el-form-item label="标注值" prop="labels"
                                              v-if="state.subForm.dataType === 1">
                                    <div class="flex flex-col items-start gap-2">
                                        <div v-for="(tag, index) in state.subForm.labels" :key="index"
                                             class="flex flex-row gap-2">
                                            <el-input v-model="tag.name" placeholder="输入标签名"></el-input>
                                            <el-color-picker v-model="tag.color" placement="top-end"></el-color-picker>
                                            <el-button link icon="delete" @click="removeTag(index)"></el-button>
                                        </div>
                                        <el-button link type="primary" icon="plus" @click="addLabels">添加</el-button>
                                    </div>
                                </el-form-item>

                                <el-form-item>
                                    <el-button type="primary" @click="onSubmit">提交</el-button>
                                    <el-button @click="goBack">取消</el-button>
                                </el-form-item>

                            </el-form>

                        </div>
                    </div>
                </div>

            </div>
        </div>
        <dataset-select-dialog :is-marked="0" ref="datasetSelectDialogRef"
                               @emit-commit="handleDatasetSelect"></dataset-select-dialog>
    </div>
</template>

<style scoped lang="scss">
.ant-page-header {
    padding: 5px 0px 20px;
}
</style>
