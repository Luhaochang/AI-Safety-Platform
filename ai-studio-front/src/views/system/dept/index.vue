<template>
    <div class="min-h-screen bg-[#f5f7fa] flex flex-col">
        <!-- 1. 头部区域 -->
        <div class="py-4 px-8 bg-[#f5f7fa]">
            <div class="text-[#140E35] text-3xl font-semibold">部门管理</div>
        </div>

        <!-- 2. 使用流程区域 -->
        <div class="px-8 pb-6 bg-[#f5f7fa]">
            <div v-if="showProcess" class="w-full bg-white rounded-xl p-6 relative shadow-sm border border-gray-100">
                <div class="absolute right-4 top-4 cursor-pointer hover:bg-gray-100 p-1 rounded-full transition-all" @click="showProcess = false">
                    <CloseOutlined class="text-gray-400 text-lg" />
                </div>
                <div class="font-bold text-lg mb-8 text-[#140E35] flex items-center gap-2">
                    <span class="w-1 h-6 bg-blue-600 rounded-full"></span>
                    使用流程
                </div>
                <div class="flex justify-around items-start px-4 relative">
                    <!-- Step 1 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-blue-50 flex items-center justify-center text-blue-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <ApartmentOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">创建部门</div>
                        <div class="text-gray-400 text-xs text-center">建立组织架构节点</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 2 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <ClusterOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">层级关系</div>
                        <div class="text-gray-400 text-xs text-center">设置上下级归属</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 3 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <TeamOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">人员关联</div>
                        <div class="text-gray-400 text-xs text-center">部门下关联用户</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 4 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <CheckCircleOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">架构生效</div>
                        <div class="text-gray-400 text-xs text-center">组织架构正式启用</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. 主体内容区域 -->
        <div class="px-8 pb-6 flex-1">
            <div class="bg-white rounded-xl h-full border border-gray-100 shadow-sm flex flex-col overflow-hidden">
                <!-- 顶部搜索与操作区 -->
                <div class="p-5 border-b border-gray-100 space-y-4 bg-white">
                    <!-- 搜索表单 -->
                    <div class="flex items-center gap-4 flex-wrap">
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">部门名称</span>
                            <el-input
                                v-model="queryParams.deptNameLike"
                                placeholder="请输入部门名称"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><ApartmentOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">状态</span>
                            <el-select v-model="queryParams.statusEq" placeholder="部门状态" clearable class="w-40">
                                <el-option
                                    v-for="dict in sys_normal_disable"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                />
                            </el-select>
                        </div>
                        <div class="flex items-center gap-2 ml-auto">
                            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
                        </div>
                    </div>

                    <!-- 分割线 -->
                    <div class="h-px bg-gray-100 w-full"></div>

                    <!-- 操作按钮行 -->
                    <div class="flex justify-between items-center">
                        <div class="flex items-center gap-2">
                            <div class="w-8 h-8 rounded-lg bg-indigo-50 flex items-center justify-center text-indigo-600 shadow-sm">
                                <ClusterOutlined />
                            </div>
                            <span class="font-bold text-gray-800 text-base">部门列表</span>
                        </div>
                        <div class="flex items-center gap-3">
                            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:dept:add']">新增部门</el-button>
                            <el-button type="info" plain icon="Sort" @click="toggleExpandAll">展开/折叠</el-button>
                            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :search="false"></right-toolbar>
                        </div>
                    </div>
                </div>

                <!-- 表格数据 -->
                <div class="flex-1 overflow-hidden px-5 pb-0">
                    <el-table
                        v-if="refreshTable"
                        v-loading="loading"
                        :data="deptList"
                        row-key="deptId"
                        :default-expand-all="isExpandAll"
                        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                        height="100%"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px', textAlign: 'center' }"
                        :cell-style="{ textAlign: 'center' }"
                        :row-style="{ height: '55px' }"
                    >
                        <el-table-column prop="deptName" label="部门名称" min-width="260" align="left" header-align="center">
                            <template #default="scope">
                                <div class="inline-flex items-center gap-2">
                                    <!-- 根据层级显示不同图标 -->
                                    <BankOutlined v-if="scope.row.parentId === 0" class="text-blue-600 text-lg" />
                                    <ClusterOutlined v-else-if="scope.row.children && scope.row.children.length > 0" class="text-indigo-500 text-lg" />
                                    <TeamOutlined v-else class="text-sky-400 text-lg" />
                                    <span class="text-gray-700 font-medium">{{ scope.row.deptName }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="orderNum" label="排序" min-width="100" align="center"></el-table-column>
                        <el-table-column prop="status" label="状态" min-width="100" align="center">
                            <template #default="scope">
                                <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
                            </template>
                        </el-table-column>
                        <el-table-column label="创建时间" align="center" prop="createTime" min-width="200">
                            <template #default="scope">
                                <span class="text-gray-500 text-sm">{{ parseTime(scope.row.createTime) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" align="center" width="220" fixed="right">
                            <template #default="scope">
                                <div class="flex items-center justify-center gap-2">
                                    <el-tooltip content="修改" placement="top" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleUpdate(scope.row)" v-hasPermi="['system:dept:edit']">
                                            <EditOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="新增" placement="top" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleAdd(scope.row)" v-hasPermi="['system:dept:add']">
                                            <PlusOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="删除" placement="top" v-if="scope.row.parentId != 0" :show-after="500">
                                        <el-button link type="danger" class="!p-1" @click="handleDelete(scope.row)" v-hasPermi="['system:dept:remove']">
                                            <DeleteOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </div>
        </div>

        <!-- 添加或修改部门对话框 -->
        <el-dialog :title="title" v-model="open" width="600px" append-to-body>
            <el-form ref="deptRef" :model="form" :rules="rules" label-width="80px">
                <el-row>
                    <el-col :span="24" v-if="form.parentId !== 0">
                        <el-form-item label="上级部门" prop="parentId">
                            <el-tree-select
                                v-model="form.parentId"
                                :data="deptOptions"
                                :props="{ value: 'deptId', label: 'deptName', children: 'children' }"
                                value-key="deptId"
                                placeholder="选择上级部门"
                                check-strictly
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="部门名称" prop="deptName">
                            <el-input v-model="form.deptName" placeholder="请输入部门名称" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="显示排序" prop="orderNum">
                            <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="负责人" prop="leader">
                            <el-input v-model="form.leader" placeholder="请输入负责人" maxlength="20" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="联系电话" prop="phone">
                            <el-input v-model="form.phone" placeholder="请输入联系电话" maxlength="11" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="部门状态">
                            <el-radio-group v-model="form.status">
                                <el-radio
                                    v-for="dict in sys_normal_disable"
                                    :key="dict.value"
                                    :value="dict.value"
                                >{{ dict.label }}</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                </el-row>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="Dept">
import {addDept, delDept, getDept, listDept, listDeptExcludeChild, updateDept} from "@/api/system/dept";
import {
  ApartmentOutlined,
  ArrowRightOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  ClusterOutlined,
  TeamOutlined,
  EditOutlined,
  PlusOutlined,
  DeleteOutlined,
  BankOutlined
} from "@ant-design/icons-vue";

const { proxy } = getCurrentInstance();
const { sys_normal_disable } = proxy.useDict("sys_normal_disable");

const showProcess = ref(true);

const deptList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const deptOptions = ref([]);
const isExpandAll = ref(true);
const refreshTable = ref(true);

const data = reactive({
    form: {},
    queryParams: {
        deptNameLike: undefined,
        statusEq: undefined
    },
    rules: {
        parentId: [{ required: true, message: "上级部门不能为空", trigger: "blur" }],
        deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
        orderNum: [{ required: true, message: "显示排序不能为空", trigger: "blur" }],
        email: [{ type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"] }],
        phone: [{ pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur" }]
    },
});

const { queryParams, form, rules } = toRefs(data);

/** 查询部门列表 */
function getList() {
    loading.value = true;
    listDept(queryParams.value).then(response => {
        deptList.value = proxy.handleTree(response.data, "deptId");
        loading.value = false;
    });
}

/** 取消按钮 */
function cancel() {
    open.value = false;
    reset();
}

/** 表单重置 */
function reset() {
    form.value = {
        deptId: undefined,
        parentId: undefined,
        deptName: undefined,
        orderNum: 0,
        leader: undefined,
        phone: undefined,
        email: undefined,
        status: "0"
    };
    proxy.resetForm("deptRef");
}

/** 搜索按钮操作 */
function handleQuery() {
    getList();
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}

/** 新增按钮操作 */
function handleAdd(row) {
    reset();
    listDept().then(response => {
        deptOptions.value = proxy.handleTree(response.data, "deptId");
    });
    if (row != undefined) {
        form.value.parentId = row.deptId;
    }
    open.value = true;
    title.value = "添加部门";
}

/** 展开/折叠操作 */
function toggleExpandAll() {
    refreshTable.value = false;
    isExpandAll.value = !isExpandAll.value;
    nextTick(() => {
        refreshTable.value = true;
    });
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    listDeptExcludeChild(row.deptId).then(response => {
        deptOptions.value = proxy.handleTree(response.data, "deptId");
    });
    getDept(row.deptId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改部门";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["deptRef"].validate(valid => {
        if (valid) {
            if (form.value.deptId != undefined) {
                updateDept(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addDept(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}

/** 删除按钮操作 */
function handleDelete(row) {
    proxy.$modal.confirm('是否确认删除名称为"' + row.deptName + '"的数据项?').then(function() {
        return delDept(row.deptId);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {});
}

getList();
</script>

<style scoped lang="scss">
.custom-scrollbar {
    &::-webkit-scrollbar {
        width: 6px;
        height: 6px;
    }
    &::-webkit-scrollbar-thumb {
        background: #cbd5e1;
        border-radius: 3px;
    }
    &::-webkit-scrollbar-track {
        background: transparent;
    }
}
</style>
