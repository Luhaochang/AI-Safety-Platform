<script setup>

import {useRouter} from "vue-router";
import DatasetSelectDialog from "@/views/dataset/components/datasetSelectDialog.vue";
import {getModelLineById, trainModelLine, updateModelLine} from "@/api/modelMag/modelLine.js";
import ModelSelectDialog from "@/views/model/components/modelSelectDialog.vue";
import {ElMessage} from "element-plus";
import { ref, reactive, computed, onMounted } from "vue";
import { ArrowLeft, InfoFilled } from "@element-plus/icons-vue";

const router = useRouter()
const selectDatasetDialogRef = ref()
const modelSelectDialogRef = ref()
const step_1_formRef = ref()
const step_2_formRef = ref()

const rules_1 = {
    modelId: [{required: true, trigger: 'change', message: '请选择模型'}],
    datasetName: [{required: true, trigger: 'change', message: '请选择数据集'}],
}

const rules_2 = {
    epochs: [{required: true, trigger: 'change', message: '轮次(Epochs)不能为空'}],
    batch_size: [{required: true, trigger: 'change', message: '批大小(Batch Size)不能为空'}],
    class_num: [{required: true, trigger: 'change', message: '类别数量(Class Num)不能为空'}],
    learning_rate: [{required: true, trigger: 'change', message: '学习率(Learning Rate)不能为空'}],
}
const state = reactive({
    current: 0,
    steps: [
        {
            title: '数据准备',
            content: 'Second-content',
            disabled: false
        },
        {
            title: '参数准备',
            content: 'Last-content',
            disabled: false
        },
        {
            title: '提交训练',
            content: 'Last-content',
            disabled: false
        },
    ],
    commitInfo: {
        id: null,
        modelId: null,
        modelName: null,
        lineName: '',
        scene: null,
        secondaryScene: null,
        datasetName: '',
        dataSetId: null,

        isSegmentation: false,
        trainSet: 80,
    },

    params: {
        paramsForm: [],
        paramsMap: {}
    },
    modelList: [
        {
            label: 'model-1',
            value: 0
        },
        {
            label: 'model-2',
            value: 1
        },
        {
            label: 'model-3',
            value: 2
        },
        {
            label: 'model-4',
            value: 3
        }

    ]
})

const testSet = computed(() => {
    return 100 - state.commitInfo.trainSet;
})

const openSelectDialog = (ref) => {
    switch (ref) {
        case 'selectDatasetDialogRef':
            selectDatasetDialogRef.value.openDialog();
            break;
        case 'modelSelectDialogRef':
            modelSelectDialogRef.value.openDialog();
            break;
    }
}

// 数据集选择框确认
const handleSelectDataset = (val) => {
    const {id, dataSetName} = val;
    state.commitInfo.dataSetId = id;
    state.commitInfo.datasetName = dataSetName;
}

//  模型选择框确认
const handleSelectModel = (val) => {
    try {
        state.params.paramsForm = JSON.parse(val.paramsString);
        state.commitInfo.modelName = val.name;
        state.commitInfo.modelId = val.id;
    }catch (e) {
        ElMessage.warning('当前模型参数不符合规范');
        return;
    }


}

const handleChangeSteps = (val) => {

}

const nextSteps = () => {
    if (state.current === 0) {
        step_1_formRef.value.validate(valid => {
            if (!valid) return;
            state.current++;
        })

    } else if (state.current === 1) {
        step_2_formRef.value.validate(valid => {
            if (!valid) return;
            state.current++;
        })
    }
}

const previousStep = () => {
    state.current--;
}

const formatInput = () => {
    if (state.commitInfo.trainSet > 99) {
        state.commitInfo.trainSet = 99
    }

    const numValue = Number(state.commitInfo.trainSet);

    if (!isNaN(numValue)) {
        // 检查数字是否在有效范围内
        if (numValue < 1) {
            // 如果小于 1，可以设置为 1 或其他默认值
            state.commitInfo.trainSet = 1;
        } else if (numValue > 99) {
            // 如果大于 99，设置为 99
            state.commitInfo.trainSet = 99;
        } else {
            // 如果在范围内，但原始值是字符串，则更新为数字类型
            // 注意：这一步其实是可选的，因为 v-model 可能会自动处理
            // 但如果你确实需要确保它是数字类型，可以这样做
            state.commitInfo.trainSet = numValue;
        }
    } else {
        state.commitInfo.trainSet = 80;
    }
    //这里判断是否是个数字
}

// 保存模型数据
const updateLine = async () => {
    await updateModelLine({
        "dataSetId": state.commitInfo.dataSetId,
        "id": state.commitInfo.id,
        "lineName": state.commitInfo.lineName,
        "modelId": state.commitInfo.modelId,
        "paramsString": JSON.stringify(state.params.paramsForm),
        "scene": state.commitInfo.scene,
        "secondaryScene": state.commitInfo.secondaryScene
    });
}

const trainLine = async () => {
    state.params.paramsMap = state.params.paramsForm.reduce((acc,item) => {
        acc[item.key] = item.value;
        return acc;
    },{});

    const res = await trainModelLine(
        parseInt(state.commitInfo.id),
        state.params.paramsMap
    )
    if (res.code === 200) {
        ElMessage.success('训练进行中，请等待');
    }
}
const startTrain = async () => {
    // update 更新
    await updateLine();

    // 训练
    await trainLine()

    router.go(-1)
}

const getLineInfoById = async (ID) => {
    const res = await getModelLineById(ID);
    if (res.code === 200 && res.data) {
        const {lineName,scene, paramsString,modelId,modelName,dataSetId,dataSetName,secondaryScene} = res.data;
        state.commitInfo.lineName = lineName;
        state.commitInfo.scene = scene;
        state.commitInfo.secondaryScene = secondaryScene;

        if (paramsString) {
            state.params.paramsForm = JSON.parse(paramsString);
        }

        if (modelId && modelName) {
            state.commitInfo.modelId = modelId;
            state.commitInfo.modelName = modelName;
        }

        if (dataSetId && dataSetName) {
            state.commitInfo.dataSetId = dataSetId;
            state.commitInfo.datasetName = dataSetName;
        }
    }
}
onMounted(() => {
    state.commitInfo.id = router.currentRoute.value.params.lineId;
    getLineInfoById(state.commitInfo.id)

})
</script>

<template>
    <div class="min-w-[800px]">
        <div class="step">
            <div class="step-header">
                <div class="step-head-wrapper">
                    <a class="header-back" @click="router.go(-1)">
                        <el-icon>
                            <ArrowLeft/>
                        </el-icon>
                        返回列表
                    </a>

                    <a-steps @change="handleChangeSteps" style="width: 768px;margin: 0px auto;" :current="state.current"
                             :items="state.steps"></a-steps>
                </div>
            </div>
            <div class="step-body">
                <!--      第一步 数据准备          -->
                <div class="step-0" v-if="state.current===0">
                    <a-row>
                        <a-col :span="15">
                            <div class="paddlex-step-title">请选择模型并添加数据集</div>
                            <div class="block mt-[24px]">
                                <el-form ref="step_1_formRef" :model="state.commitInfo" label-width="auto"
                                         label-position="right" :rules="rules_1">
                                    <el-form-item label="选择模型" prop="modelId">
                                        <el-input v-model="state.commitInfo.modelName" readonly>
                                            <template #append>
                                                <el-button icon="search" @click="openSelectDialog('modelSelectDialogRef')"></el-button>
                                            </template>
                                        </el-input>
                                    </el-form-item>
                                    <el-form-item label="添加数据集" prop="datasetName">
                                        <el-input class="w-[60%]" readonly placeholder="请选择数据集"
                                                  v-model="state.commitInfo.datasetName">
                                            <template #append>
                                                <el-button icon="search"
                                                           @click="openSelectDialog('selectDatasetDialogRef')"></el-button>
                                            </template>
                                        </el-input>
                                    </el-form-item>

                                    <el-form-item label="数据处理">
                                        <template #label>
                                            <el-space>
                                                数据处理
                                                <a-tooltip placement="top" color="white">
                                                    <template #title>
                                                        <span style="color: #666666;font-size: 12px">
                                                            选择数据处理操作会在用户个人数据集中创建一个新的数据集，并在本次训练中替换为处理后的数据集，默认占比80:20
                                                        </span>
                                                    </template>
                                                    <el-icon :size="16">
                                                        <InfoFilled/>
                                                    </el-icon>
                                                </a-tooltip>
                                            </el-space>
                                        </template>
                                        <a-row>
                                            <a-checkbox v-model:checked="state.commitInfo.isSegmentation">自定义切分
                                            </a-checkbox>
                                        </a-row>

                                        <div v-if="state.commitInfo.isSegmentation">
                                            <a-row class="mt-[8px]">
                                                <div class="flex items-center">
                                                    <span>
                                                        训练集占比
                                                        <a-input v-model:value="state.commitInfo.trainSet" :min="1"
                                                                 @blur="formatInput"
                                                                 :max="99"
                                                                 style="width: 104px; text-indent: 6px; margin-left: 16px;"
                                                                 suffix="%"/>
                                                    </span>
                                                    <a-slider style="width: 270px;
                                                                     margin: 10px 24px 10px 12px !important;"
                                                              v-model:value="state.commitInfo.trainSet"></a-slider>
                                                    <span>
                                                        验证集占比
                                                        <a-input v-model:value="testSet"

                                                                 :min="1"
                                                                 :max="99"
                                                                 style="width: 104px; text-indent: 6px; margin-left: 16px;"
                                                                 suffix="%"/>
                                                    </span>
                                                </div>
                                            </a-row>
                                            <a-row class="text-xs mt-[8px]">
                                                本工具可完成数据集的训练集和验证集按比例随机切分，如果上传数据集中含数据切分文件，会进行重新切分并保存为新的数据集，切分占比1-99之间，不可为0
                                            </a-row>
                                        </div>

                                    </el-form-item>
                                </el-form>
                            </div>


                        </a-col>
                        <a-col :span="1">
                            <div class="paddlex-step-split"></div>
                        </a-col>
                        <a-col :span="8"></a-col>
                    </a-row>
                </div>

                <!--      第二步 参数准备          -->
                <div class="step-0" v-if="state.current===1">
                    <div class="paddlex-step-title">
                        请设置模型参数
                        <span class="ml-[16px]"
                              style="color: #565772;font-size: 14px;font-style: normal;font-weight: 400;line-height: 24px">
                            {{ state.commitInfo.modelName }}
                        </span>
                    </div>
                    <div class="flex mt-[24px]">
                        <el-form ref="step_2_formRef" label-position="top" label-width="auto" :model="state.params"
                                 >
                            <el-form-item v-for="(param,index) in state.params.paramsForm"
                                          :label="param.label"
                                          :prop="'paramsForm.' + index + '.value'"
                                          :rules="[{required: true, trigger: 'change', message: param.label+'不能为空'}]">
                                <el-input v-model="param.value"></el-input>
                                <div class="mt-1"
                                     style="color: #9498ac;font-size: 12px;line-height: 20px;min-height: 20px">
                                    {{param.des}}
                                </div>
                            </el-form-item>

                        </el-form>
                    </div>
                </div>


                <!--      第三步 提交训练          -->
                <div class="step-3" v-if="state.current===2">
                    提交训练
                </div>


            </div>
            <div class="step-footer">
                <a-button type="primary" v-if="state.current === state.steps.length-1" @click="startTrain" v-hasPermi="['modelmag:model-line:train']">提交训练
                </a-button>
                <a-button type="primary" v-if="state.current < state.steps.length-1" @click="nextSteps">下一步
                </a-button>
                <a-button class="ml-[16px]" v-if="state.current>0" @click="previousStep">上一步</a-button>
            </div>
        </div>

        <dataset-select-dialog ref="selectDatasetDialogRef" :scene="state.commitInfo.secondaryScene? state.commitInfo.secondaryScene:state.commitInfo.scene" :is-marked="2"
                               @emit-commit="handleSelectDataset"></dataset-select-dialog>
        <model-select-dialog ref="modelSelectDialogRef" @emit-commit="handleSelectModel" :scene="state.commitInfo.secondaryScene? state.commitInfo.secondaryScene:state.commitInfo.scene"></model-select-dialog>
    </div>
</template>

<style scoped lang="scss">
.step {
    .step-header {
        text-align: center;
        padding: 34px 0px;
        background: linear-gradient(297.48deg, rgba(245, 248, 255, 0.6) 40.43%, #F5F7FF 76.93%);
        background-blend-mode: multiply;

        .step-head-wrapper {
            width: 1280px;
            margin: 0px auto;
            position: relative;

            .header-back {
                position: absolute;
                z-index: 100;
                left: 0px;
                top: 3px;
                color: #140e35;
                font-size: 14px;
                font-style: normal;
                font-weight: 400;
                line-height: 24px;
                cursor: pointer;
            }

        }


    }

    .step-body {
        padding: 40px 0px;
        width: 1280px;
        margin: 0px auto;
        height: calc(100vh - 100px - 84px - 56px);
        box-sizing: border-box;
        overflow: auto;

        .step-0 {
            .paddlex-step-title {
                color: #140E35;
                font-size: var(--studio-fontSize-18);
                font-style: normal;
                font-weight: 600;
                line-height: 27px;
            }

            .paddlex-step-selectpl-item {
                flex-shrink: 0;
                cursor: pointer;
                width: 400px;
                height: 130px;
                box-sizing: border-box;
                padding: 16px 24px;
                border-radius: 8px;
                border: 1px solid #d9dee9;
                background: #FFF;
                box-shadow: 0px 1px 2px 0px rgba(122, 161, 225, 0.1);
                position: relative;
            }

            .paddlex-step-split {
                width: 1px;
                height: 100%;
                background-color: #e5eaf4;
                margin: 0px auto;
            }
        }
    }

    .step-footer {
        height: 84px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-top: solid 1px #E5EAF4;
    }


}
</style>