<template>
    <div class="min-h-screen bg-[#f5f7fa] flex flex-col">
        <!-- 1. 头部区域 -->
        <div class="py-4 px-8 bg-[#f5f7fa]">
            <div class="text-[#140E35] text-3xl font-semibold">岗位管理</div>
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
                            <IdcardOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">创建岗位</div>
                        <div class="text-gray-400 text-xs text-center">定义岗位名称与编码</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 2 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <OrderedListOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">设置排序</div>
                        <div class="text-gray-400 text-xs text-center">调整岗位显示顺序</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 3 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <UserAddOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">关联用户</div>
                        <div class="text-gray-400 text-xs text-center">将用户分配至岗位</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 4 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <CheckCircleOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">启用岗位</div>
                        <div class="text-gray-400 text-xs text-center">岗位生效可被引用</div>
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
                            <span class="text-gray-600 text-sm font-medium">岗位编码</span>
                            <el-input
                                v-model="queryParams.postCodeLike"
                                placeholder="请输入岗位编码"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><BarcodeOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">岗位名称</span>
                            <el-input
                                v-model="queryParams.postNameLike"
                                placeholder="请输入岗位名称"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><IdcardOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">状态</span>
                            <el-select v-model="queryParams.statusEq" placeholder="岗位状态" clearable class="w-40">
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
                                <SolutionOutlined />
                            </div>
                            <span class="font-bold text-gray-800 text-base">岗位列表</span>
                            <span class="text-gray-400 text-xs ml-2 bg-gray-100 px-2 py-0.5 rounded-full">共 {{ total }} 个</span>
                        </div>
                        <div class="flex items-center gap-3">
                            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:post:add']">新增岗位</el-button>
                            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:post:remove']">删除</el-button>
                            <el-button type="warning" plain icon="Download" @click="handleExport" v-hasPermi="['system:post:export']">导出数据</el-button>
                            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :search="false"></right-toolbar>
                        </div>
                    </div>
                </div>

                <!-- 表格数据 -->
                <div class="flex-1 overflow-hidden px-5 pb-0">
                    <el-table
                        v-loading="loading"
                        :data="postList"
                        @selection-change="handleSelectionChange"
                        height="100%"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px', textAlign: 'center' }"
                        :cell-style="{ textAlign: 'center' }"
                        :row-style="{ height: '55px' }"
                    >
                        <el-table-column type="selection" width="55" align="center"/>
                        <el-table-column label="岗位编号" align="center" prop="postId" min-width="100"/>
                        <el-table-column label="岗位编码" align="center" prop="postCode" min-width="150">
                            <template #default="scope">
                                <el-tag type="info" effect="plain" class="border-0 bg-gray-100 text-gray-600">{{ scope.row.postCode }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="岗位名称" align="center" prop="postName" min-width="150">
                            <template #default="scope">
                                <span class="font-medium text-gray-800">{{ scope.row.postName }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="岗位排序" align="center" prop="postSort" min-width="100"/>
                        <el-table-column label="状态" align="center" prop="status" min-width="100">
                            <template #default="scope">
                                <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
                            </template>
                        </el-table-column>
                        <el-table-column label="创建时间" align="center" prop="createTime" min-width="160">
                            <template #default="scope">
                                <span class="text-gray-500 text-sm">{{ parseTime(scope.row.createTime) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" width="180" align="center" fixed="right">
                            <template #default="scope">
                                <div class="flex items-center justify-center gap-2">
                                    <el-tooltip content="修改" placement="top" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleUpdate(scope.row)" v-hasPermi="['system:post:edit']">
                                            <EditOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="删除" placement="top" :show-after="500">
                                        <el-button link type="danger" class="!p-1" @click="handleDelete(scope.row)" v-hasPermi="['system:post:remove']">
                                            <DeleteOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                </div>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <div class="p-4 border-t border-gray-100 bg-gray-50/30">
                    <div class="flex justify-end">
                        <a-pagination
                            v-model:current="queryParams.pageNo"
                            :total="total"
                            :page-size="queryParams.pageSize"
                            :page-size-options="['10', '20', '50']"
                            show-size-changer
                            show-quick-jumper
                            :show-total="total => `共 ${total} 条数据`"
                            @change="getList"
                            @showSizeChange="getList"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加或修改岗位对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="postRef" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="岗位名称" prop="postName">
                    <el-input v-model="form.postName" placeholder="请输入岗位名称"/>
                </el-form-item>
                <el-form-item label="岗位编码" prop="postCode">
                    <el-input v-model="form.postCode" placeholder="请输入编码名称"/>
                </el-form-item>
                <el-form-item label="岗位顺序" prop="postSort">
                    <el-input-number v-model="form.postSort" controls-position="right" :min="0"/>
                </el-form-item>
                <el-form-item label="岗位状态" prop="status">
                    <el-radio-group v-model="form.status">
                        <el-radio
                            v-for="dict in sys_normal_disable"
                            :key="dict.value"
                            :value="dict.value"
                        >{{ dict.label }}
                        </el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"/>
                </el-form-item>
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

<script setup name="Post">
import {addPost, delPost, getPost, listPost, updatePost} from "@/api/system/post";
import {
  ArrowRightOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  IdcardOutlined,
  OrderedListOutlined,
  UserAddOutlined,
  BarcodeOutlined,
  SolutionOutlined,
  EditOutlined,
  DeleteOutlined
} from "@ant-design/icons-vue";

const {proxy} = getCurrentInstance();
const {sys_normal_disable} = proxy.useDict("sys_normal_disable");

const showProcess = ref(true);

const postList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
    form: {},
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        postCodeLike: undefined,
        postNameLike: undefined,
        statusEq: undefined
    },
    rules: {
        postName: [{required: true, message: "岗位名称不能为空", trigger: "blur"}],
        postCode: [{required: true, message: "岗位编码不能为空", trigger: "blur"}],
        postSort: [{required: true, message: "岗位顺序不能为空", trigger: "blur"}],
    }
});

const {queryParams, form, rules} = toRefs(data);

/** 查询岗位列表 */
function getList() {
    loading.value = true;
    listPost(queryParams.value).then(response => {
        if (response.code === 200) {
            const {data} = response;
            postList.value = data.records;
            total.value = data.total;
            loading.value = false;
        }
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
        postId: undefined,
        postCode: undefined,
        postName: undefined,
        postSort: 0,
        status: "0",
        remark: undefined
    };
    proxy.resetForm("postRef");
}

/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNo = 1;
    getList();
}

/** 重置按钮操作 */
function resetQuery() {
    proxy.resetForm("queryRef");
    handleQuery();
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.postId);
    single.value = selection.length != 1;
    multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    open.value = true;
    title.value = "添加岗位";
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const postId = row.postId || ids.value;
    getPost(postId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改岗位";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["postRef"].validate(valid => {
        if (valid) {
            if (form.value.postId !== undefined) {
                updatePost(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addPost(form.value).then(response => {
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
    const postIds = row.postId || ids.value;
    proxy.$modal.confirm('是否确认删除岗位编号为"' + postIds + '"的数据项？').then(function () {
        return delPost(postIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

/** 导出按钮操作 */
function handleExport() {
    proxy.download("system/post/export", {
        ...queryParams.value
    }, `post_${new Date().getTime()}.xlsx`);
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
