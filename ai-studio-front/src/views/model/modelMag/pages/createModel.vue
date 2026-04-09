<script setup>
import {ArrowRight, Delete, Plus, Monitor, Cpu, Connection, Files} from "@element-plus/icons-vue";
import {addCustomModel, addModel} from "@/api/modelMag/model.js";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import {getByRootCategory, getSonCategory} from "@/api/modelMag/label.js";
import {readme} from "@/views/model/modelMag/pages/templateReadMe.js";
import useUserStore from "@/store/modules/user.js";
import {getAllAppScene, getAllTaskType} from "@/api/modelMag/scene.js";
import {ref, reactive, onMounted, onUnmounted} from "vue";

const formRef = ref()
const router = useRouter()

const fromPage = ref(0) // 1: 官方；2: 打包
const lineId = ref()

const activeSceneType = ref('app') // app: 应用场景, task: 任务场景
const appSceneOptions = ref([])
const taskSceneOptions = ref([])

const state = reactive({
    formData: {
        name: '',
        appSceneId: [], // For App Scene (Multiple)
        params: [],
        description: '',
        author: useUserStore().name,
        tag: [],
        modelPut: '',
        sysId: [],
        hardwareId: [],
        taskTypeId: null // For Task Scene (Single)
    },
    sys_opt: [],
    hardware_opt: [],
    fileUrl: null
})

const rules = ref({
    name: [{required: true, message: "模型名称为必填字段", trigger: "change"}],
    author: [{required: true, message: "作者为必填字段", trigger: "change"}],
    appSceneId: [
        {
            validator: (rule, value, callback) => {
                if (activeSceneType.value === 'app') {
                    if (!state.formData.appSceneId || state.formData.appSceneId.length === 0) {
                        callback(new Error('请选择应用场景'));
                    } else {
                        callback();
                    }
                } else {
                    callback();
                }
            },
            trigger: "change",
            required: true
        }
    ],
    taskTypeId: [
        {
            validator: (rule, value, callback) => {
                if (activeSceneType.value === 'task') {
                    if (!state.formData.taskTypeId) {
                        callback(new Error('请选择任务场景'));
                    } else {
                        callback();
                    }
                } else {
                    callback();
                }
            },
            trigger: "change",
            required: true
        }
    ],
    params: [
        {
            validator: (rule, value, callback) => {
                if (fromPage.value === 2){
                    callback()
                }else if (fromPage.value === 1){
                    if (value.length < 1) {
                        callback(new Error('请输入至少一个参数'))
                    } else {
                        value.forEach(item => {
                            if (item.key === '' || item.value === '') {
                                callback(new Error('请填写完整参数名及默认值'))
                            }
                        })
                        callback()
                    }
                }
            },
            trigger: 'change',
            required: fromPage.value === 1
        }
    ],
    tag: [
        {
            validator:(rule, value, callback) =>{
                if (state.formData.sysId.length===0 || state.formData.hardwareId.length===0){
                    callback(new Error('请至少选择1个标签'));
                }else {
                    callback()
                }
            },
            trigger: 'change',
            required: true
        }
    ]
});

const appSceneCascaderProps = {
    multiple: true,
    emitPath: false,
    checkStrictly: false
}

const addParams = () => {
    state.formData.params.push({key: '', value: '', label: '', des: ''});
}
const removeParams = (index) => {
    state.formData.params.splice(index, 1);
}

const ensureAddModel = () => {
    formRef.value.validate(valid => {
        if (!valid) return;
        const mapParams = state.formData.params.reduce((acc, item) => {
            acc[item.key] = item.value;
            return acc;
        }, {});

        const hardwareId = state.formData.hardwareId;
        const list = hardwareId.length > 0? hardwareId.map(sub => sub[sub.length -1]) : [];

        let appSceneIdVal = null;
        let taskTypeIdVal = null;

        if (activeSceneType.value === 'app') {
            // Send as JSON string of array
            appSceneIdVal = JSON.stringify(state.formData.appSceneId);
        } else {
            taskTypeIdVal = state.formData.taskTypeId;
        }

        const payload = {
            "author": state.formData.author,
            "description": state.formData.description,
            "fileUrl": fromPage.value === 1 ? state.fileUrl : "",
            "name": state.formData.name,
            "paramsString": JSON.stringify(state.formData.params),
            "appSceneId": appSceneIdVal,
            "taskTypeId": taskTypeIdVal,
            "configMap": JSON.stringify(mapParams),
            tagIdList: [...state.formData.sysId, ...list],
            "readme": readme,
            "modelPut": state.formData.modelPut
        };

        if (fromPage.value === 1) {
            addModel(payload).then(res => {
                if (res.code === 200) {
                    ElMessage.success('添加成功');
                    router.go(-1);
                }
            });
        } else if (fromPage.value === 2) {
            addCustomModel(lineId.value, payload).then(res => {
                if (res.code === 200) {
                    ElMessage.success('添加成功');
                    router.push('/modelmag/model');
                }
            });
        }
    });
}

const getCategory = async (category) => {
    const res = await getByRootCategory({category: category});
    if (res.code === 200 && res.data && res.data.length !== 0) {
        if (category === 1) {
            state.sys_opt = res.data;
        } else if (category === 2) {
            state.hardware_opt = res.data.map(item => ({
                label: item.tagName,
                value: item.id,
                leaf: item.isSuper === 0
            }));
        }
    }
}

const hardwareProps = {
    lazy: true,
    multiple: true,
    lazyLoad(node, resolve) {
        const {data} = node;
        if (data && data.value) {
            const nodes = [];
            getSonCategory({superId: data.value}).then(res => {
                res.data.map((item) => {
                    let obj = {
                        value: item.id,
                        label: item.tagName,
                        leaf: true
                    }
                    nodes.push(obj);
                })
                resolve(nodes);
            })
        }else {
            resolve([])
        }
    },
}

const handleSelect = (value) => {
    if (!value || value.length === 0) {
        state.formData.hardwareId = [];
    }
}

onMounted(async () => {
    await getCategory(1);
    await getCategory(2);

    // Load App Scenes
    const appRes = await getAllAppScene();
    if (appRes.code === 200) {
        const grouped = {};
        appRes.data.forEach(item => {
            const industry = item.industry || '其他';
            if (!grouped[industry]) grouped[industry] = [];
            grouped[industry].push({
                value: item.id,
                label: item.appName
            });
        });
        appSceneOptions.value = Object.keys(grouped).map(key => ({
            value: key,
            label: key,
            children: grouped[key]
        }));
    }

    // Load Task Scenes
    const taskRes = await getAllTaskType();
    if (taskRes.code === 200) {
        taskSceneOptions.value = taskRes.data.map(item => ({
            value: item.id,
            label: item.taskName
        }));
    }

    let id = router.currentRoute.value.params.id;

    if (id) {
        fromPage.value = 2;
        lineId.value = id;
        state.formData.modelPut = sessionStorage.getItem('modelInfo');
    } else {
        fromPage.value = 1;
    }
})

onUnmounted(() => {
    sessionStorage.removeItem('modelInfo');
})
</script>

<template>
    <div class="page-container">
        <!-- Content -->
        <div class="page-content">
            <el-card class="main-card" shadow="hover">
                <div class="card-header">
                    <div class="header-left">
                        <div class="title">创建新模型</div>
                        <div class="subtitle">请填写模型的基础信息、应用场景及相关参数配置</div>
                    </div>
                    <el-button link @click="router.go(-1)">
                        <el-icon class="mr-1"><ArrowRight style="transform: rotate(180deg)"/></el-icon> 返回
                    </el-button>
                </div>

                <el-divider class="header-divider" />

                <el-form ref="formRef" :model="state.formData" :rules="rules" label-position="top" size="large">

                    <!-- Section: Basic Info -->
                    <div class="form-section">
                        <div class="section-header">
                            <span class="section-title">基础信息</span>
                        </div>
                        <el-row :gutter="32">
                            <el-col :span="12">
                                <el-form-item label="模型名称" prop="name">
                                    <el-input v-model="state.formData.name" placeholder="请输入模型名称" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="模型所有者" prop="author">
                                    <el-input v-model="state.formData.author" disabled class="is-disabled-dark" />
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-form-item label="模型简介" prop="description">
                            <el-input type="textarea" v-model="state.formData.description" :rows="3" placeholder="请输入模型简介..." maxlength="150" show-word-limit />
                        </el-form-item>
                    </div>

                    <!-- Section: Scene & Tags -->
                    <div class="form-section">
                        <div class="section-header">
                            <span class="section-title">场景与标签</span>
                        </div>

                        <el-form-item label="场景类型" class="scene-type-item">
                             <el-radio-group v-model="activeSceneType" class="custom-radio-group">
                                <el-radio-button label="app">
                                    <div class="radio-content"><el-icon><Connection /></el-icon> 应用场景</div>
                                </el-radio-button>
                                <el-radio-button label="task">
                                    <div class="radio-content"><el-icon><Files /></el-icon> 任务场景</div>
                                </el-radio-button>
                            </el-radio-group>
                        </el-form-item>

                        <el-form-item v-if="activeSceneType === 'app'" label="选择应用场景" prop="appSceneId" required>
                            <el-cascader
                                class="w-full"
                                v-model="state.formData.appSceneId"
                                :options="appSceneOptions"
                                :props="appSceneCascaderProps"
                                placeholder="请选择应用场景（支持多选）"
                                clearable
                                collapse-tags
                                collapse-tags-tooltip
                            />
                        </el-form-item>

                        <el-form-item v-if="activeSceneType === 'task'" label="选择任务场景" prop="taskTypeId" required>
                            <el-select class="w-full" v-model="state.formData.taskTypeId" placeholder="请选择任务场景">
                                 <el-option v-for="item in taskSceneOptions" :key="item.value" :label="item.label" :value="item.value" />
                            </el-select>
                        </el-form-item>

                        <el-row :gutter="32">
                            <el-col :span="12">
                                <el-form-item label="系统标签" prop="sysId">
                                    <template #label>
                                        <div class="label-with-icon"><el-icon><Monitor /></el-icon> 系统标签</div>
                                    </template>
                                    <el-select v-model="state.formData.sysId" multiple collapse-tags placeholder="选择系统标签" class="w-full">
                                        <el-option v-for="opt in state.sys_opt" :key="opt.id" :value="opt.id" :label="opt.tagName" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col :span="12">
                                <el-form-item label="硬件标签" prop="hardwareId">
                                    <template #label>
                                        <div class="label-with-icon"><el-icon><Cpu /></el-icon> 硬件标签</div>
                                    </template>
                                    <el-cascader
                                        v-model="state.formData.hardwareId"
                                        :options="state.hardware_opt"
                                        :props="hardwareProps"
                                        collapse-tags
                                        placeholder="选择硬件标签"
                                        class="w-full"
                                        @change="handleSelect"
                                    />
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </div>

                    <!-- Section: Parameters -->
                    <div class="form-section">
                        <div class="section-header flex justify-between items-center">
                            <span class="section-title">模型参数配置</span>
                            <el-button type="primary" plain icon="Plus" size="default" @click="addParams">添加参数</el-button>
                        </div>

                        <div class="params-wrapper">
                            <div v-if="state.formData.params.length === 0" class="empty-params">
                                <el-empty description="暂无参数配置" :image-size="60"></el-empty>
                            </div>
                            <div v-else class="params-table">
                                <div class="params-header-row">
                                    <div class="col-3">参数名 (Key)</div>
                                    <div class="col-3">默认值 (Value)</div>
                                    <div class="col-3">中文名 (Label)</div>
                                    <div class="col-3">描述 (Desc)</div>
                                    <div class="col-1 text-center">操作</div>
                                </div>
                                <div v-for="(tag, index) in state.formData.params" :key="index" class="params-row">
                                    <div class="col-3"><el-input v-model="tag.key" placeholder="Key" /></div>
                                    <div class="col-3"><el-input v-model="tag.value" placeholder="Value" /></div>
                                    <div class="col-3"><el-input v-model="tag.label" placeholder="Label" /></div>
                                    <div class="col-3"><el-input v-model="tag.des" placeholder="Description" /></div>
                                    <div class="col-1 text-center">
                                        <el-button type="danger" link icon="Delete" @click="removeParams(index)"></el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <el-form-item label="模型文件地址" v-if="fromPage === 1" class="mt-6">
                        <el-input v-model="state.fileUrl" placeholder="请输入模型文件 URL" />
                    </el-form-item>

                    <!-- Footer Actions -->
                    <div class="form-footer">
                        <el-button class="btn-cancel" @click="router.go(-1)">取消</el-button>
                        <el-button class="btn-submit" type="primary" @click="ensureAddModel">确认创建</el-button>
                    </div>

                </el-form>
            </el-card>
        </div>
    </div>
</template>

<style scoped lang="scss">
.page-container {
    background-color: #f5f7fa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}

.page-content {
    flex: 1;
    padding: 24px 40px;

    .main-card {
        max-width: 1000px;
        margin: 0 auto;
        border-radius: 8px;
        border: none;

        :deep(.el-card__body) {
            padding: 32px 40px;
        }
    }
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 16px;

    .header-left {
        .title {
            font-size: 24px;
            color: #1f2f3d;
            font-weight: 600;
            margin-bottom: 8px;
            line-height: 1.2;
        }
        .subtitle {
            font-size: 14px;
            color: #909399;
            line-height: 1.5;
        }
    }
}

.header-divider {
    margin: 0 0 32px 0;
}

.form-section {
    margin-bottom: 32px;

    .section-header {
        margin-bottom: 20px;
        display: flex;
        align-items: center;

        .section-title {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            position: relative;
            padding-left: 12px;

            &::before {
                content: '';
                position: absolute;
                left: 0;
                top: 50%;
                transform: translateY(-50%);
                width: 4px;
                height: 16px;
                background: #409eff;
                border-radius: 2px;
            }
        }
    }
}

.w-full {
    width: 100%;
}

.label-with-icon {
    display: flex;
    align-items: center;
    gap: 4px;
}

.radio-content {
    display: flex;
    align-items: center;
    gap: 6px;
}

.params-wrapper {
    background: #f8f9fa;
    border-radius: 6px;
    padding: 16px;
    border: 1px solid #ebeef5;
}

.params-table {
    .params-header-row {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;
        font-size: 13px;
        color: #606266;
        font-weight: 500;
        padding: 0 8px;
    }

    .params-row {
        display: flex;
        gap: 12px;
        margin-bottom: 12px;
        align-items: center;

        &:last-child {
            margin-bottom: 0;
        }
    }

    .col-3 {
        flex: 3;
    }
    .col-1 {
        flex: 1;
    }
}

.form-footer {
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: flex-end;
    gap: 16px;

    .btn-submit {
        min-width: 120px;
    }
    .btn-cancel {
        min-width: 100px;
    }
}

:deep(.el-form-item__label) {
    font-weight: 500;
}

:deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    &:hover {
        box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    &.is-focus {
        box-shadow: 0 0 0 1px #409eff inset;
    }
}

.is-disabled-dark :deep(.el-input__inner) {
    -webkit-text-fill-color: #606266;
}
</style>
