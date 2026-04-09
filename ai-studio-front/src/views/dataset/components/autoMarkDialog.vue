<script setup>
import ServiceSelectDialog from "@/views/model/service/components/serviceSelectDialog.vue";
import {
    addAutoMarkTask,
    autoMarkSubmit,
    deleteAutoMarkTask,
    editAutoMarkTask,
    getAutoMarkTaskByTaskId
} from "@/api/dataMag/autoMark.js";
import Pagination from "@/components/Pagination/index.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {deleteService} from "@/api/modelMag/service.js";
import {submitTask} from "@/api/dataMag/markTask.js";

const serviceDialogRef = ref()
const formRef = ref()
const state = reactive({
    title: '添加',
    visible: false,
    addDialog: false,
    autoMarkFrom: {
        modelUrl: '',
        modelId: null,
        modelName: '',

        name: '',
        id: null
    },
    tabList: [],
    taskInfo: {
        id: null
    },
    queryParams: {
        pageNo: 1,
        pageSize: 10,
    },
    total: 0

})

const rules = {
    name:  [{required: true, trigger: 'change', message: '请输入名称'}],
    modelName:  [{required: true, trigger: 'change', message: '请选择服务'}],
}

const formatStatus = ({status}) => {
    switch (status) {
        case 0:
            return '未标注';
        case 1:
            return '排队中';
        case 2:
            return '标注中';
        case 3:
            return '已标注';
        case 4:
            return '标注失败';
        default:
            return ''
    }
}

const openDialog = (task) => {
    const {id} = task;
    state.taskInfo.id = id;
    state.visible = true;

    getList(id)
}

const dialogClose = () => {
    state.visible = false;
    state.tabList = [];

}

const openSelectDialog = () => {
    serviceDialogRef.value.openDialog()
}
const handleSelectService = (val) => {
    state.autoMarkFrom.modelName = val.name;
    state.autoMarkFrom.modelId = val.id;
    state.autoMarkFrom.modelUrl = val.serviceUrl;
}

const getList = async (id) => {
    const res = await getAutoMarkTaskByTaskId({
        dataSetTaskId: id,
        pageNo: state.queryParams.pageNo,
        pageSize: state.queryParams.pageSize
    });

    if (res.code === 200) {
        state.tabList = res.data.records;
        state.total = res.data.total;
    }
}

const handlePageChange = (val) => {
    state.queryParams.pageNo = val;
    getList(state.taskInfo.id)
}

const deleteATask = (row) => {
    ElMessageBox.confirm('确定删除该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteAutoMarkTask({id: row.id}).then(res => {
            if (res.code === 200) {
                ElMessage.success('删除成功');
                getList(state.taskInfo.id);
            }
        })
    }).catch(() => {
    });
}

// 提交任务
const submitATask = async (row) => {
    const res = await autoMarkSubmit({
        id: row.id
    })
    if (res.code === 200) {
        ElMessage.success('开始标注');
    }
}

const addFormOpen = () => {
    state.addDialog = true;
}

const addFormClose = () => {
    state.autoMarkFrom.modelUrl = '';
    state.autoMarkFrom.modelId = null;
    state.autoMarkFrom.name = '';
    state.autoMarkFrom.id = null;
    state.autoMarkFrom.modelName = ''
    formRef.value.resetFields()
    state.addDialog = false;

}

const addSubTask = async () => {
    formRef.value.validate( async valid => {
        if (!valid) return;

        switch (state.title){
            case '添加':
                const res = await addAutoMarkTask({
                    "dataSetTaskId": state.taskInfo.id,
                    "name": state.autoMarkFrom.name,
                    "serviceId": state.autoMarkFrom.modelId
                });
                if (res.code === 200) {
                    ElMessage.success('添加成功');
                    await getList(state.taskInfo.id);
                    addFormClose()
                }
                break;
            case '修改':
                const response = await editAutoMarkTask({
                    "id": state.autoMarkFrom.id,
                    "dataSetTaskId": state.taskInfo.id,
                    "name": state.autoMarkFrom.name,
                    "serviceId": state.autoMarkFrom.modelId
                });
                if (response.code === 200) {
                    ElMessage.success('修改成功');
                    await getList(state.taskInfo.id);
                    addFormClose()
                }
                break;

        }


    })
}

const updateSubTask = (row) => {
    state.title = '修改';
    state.autoMarkFrom.name = row.name;
    state.autoMarkFrom.modelId = row.serviceId;
    state.autoMarkFrom.modelName = row.serviceName;
    state.autoMarkFrom.id = row.id;
    state.addDialog = true;

}
defineExpose({
    openDialog
})
</script>

<template>
    <div>
        <el-dialog
            width="60%"
            style="height: 500px;"
            v-model="state.visible"
            @close="dialogClose"
            title="自动标注任务">
            <div class="flex">
                <el-button icon="plus" type="primary" @click="addFormOpen">新增</el-button>
            </div>
            <div class="p-[10px]">
                <el-table :data="state.tabList" border stripe >
                    <el-table-column label="任务名称" prop="name"></el-table-column>
                    <el-table-column label="在线服务" prop="serviceName"></el-table-column>
                    <el-table-column label="标注状态" prop="status" :formatter="formatStatus"></el-table-column>
                    <el-table-column label="操作" align="center">
                        <template #default="scope">
                            <el-tooltip content="修改任务" v-if="scope.row.status === 0">
                                <el-button icon="edit" link @click="updateSubTask(scope.row)"></el-button>
                            </el-tooltip>

                            <el-tooltip content="开始标注" v-if="scope.row.status === 0">
                                <el-button  icon="CircleCheckFilled" link @click="submitATask(scope.row)"></el-button>
                            </el-tooltip>

                            <el-tooltip content="删除">
                                <el-button icon="delete" link @click="deleteATask(scope.row)"></el-button>

                            </el-tooltip>
                        </template>
                    </el-table-column>
                </el-table>

                <div class="flex justify-end p-[10px]">
                    <a-pagination v-model:current="state.queryParams.pageNo" :total="state.total"
                                  show-less-items v-if="state.total>0" @change="handlePageChange"/>
                </div>
            </div>


            <template #footer class="custom-footer">
                <div class="custom-footer">
                    <el-button @click="dialogClose">关闭</el-button>
                </div>
            </template>
        </el-dialog>


        <el-dialog
            v-model="state.addDialog"
            width="45%"
            align-center
            :title="state.title+'任务'">
            <el-form ref="formRef" :model="state.autoMarkFrom" label-width="auto" label-position="right" :rules="rules">
                <el-form-item label="子任务名称" prop="name">
                    <el-input v-model="state.autoMarkFrom.name"></el-input>
                </el-form-item>
                <el-form-item label="模型" prop="modelName">
                    <el-input readonly v-model="state.autoMarkFrom.modelName">
                        <template #append>
                            <el-button icon="search" @click="openSelectDialog"></el-button>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item label="服务地址" v-if="state.title === '添加'">
                    {{ state.autoMarkFrom.modelUrl }}
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="addSubTask" type="primary">确认</el-button>
                <el-button @click="addFormClose">取消</el-button>
            </template>
        </el-dialog>

        <service-select-dialog ref="serviceDialogRef" @emit-commit="handleSelectService"></service-select-dialog>

    </div>

</template>

<style scoped lang="scss">
.custom-footer {
    position: absolute;
    bottom: 0;
    width: 98%;
    display: flex;
    justify-content: flex-end; /* 如果按钮需要靠右 */
    padding: 20px; /* 适当的内边距 */
    background-color: #fff; /* 确保与对话框背景一致 */
}
</style>
