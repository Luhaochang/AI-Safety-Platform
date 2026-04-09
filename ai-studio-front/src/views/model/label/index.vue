<template>
    <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
        <div class="flex-1 overflow-x-hidden min-w-800">
            <div class="h-full min-w-800">
                <div class="min-w-min h-full py-6 px-8"
                     style="background: linear-gradient(180deg, #F7F9FF 0%, #F2F5FE 100%);">

                    <!-- Header -->
                    <div class="flex items-center justify-between mb-6 m-auto w-full">
                        <div class="text-[#140E35] text-3xl font-semibold">标签管理</div>
                        <el-button
                            type="primary"
                            size="large"
                            icon="Plus"
                            @click="handleAdd"
                            v-hasPermi="['system:dept:add']"
                        >新增标签
                        </el-button>
                    </div>

                    <!-- Process Flow -->
                    <div v-if="showProcess" class="w-full bg-white rounded-xl p-6 mb-6 relative shadow-sm border border-gray-100">
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
                                    <TagsOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">创建标签</div>
                                <div class="text-gray-400 text-xs text-center">定义模型分类标签</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 2 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <AppstoreOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">标签分类</div>
                                <div class="text-gray-400 text-xs text-center">管理标签层级结构</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 3 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <LinkOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">关联模型</div>
                                <div class="text-gray-400 text-xs text-center">将标签应用到模型</div>
                            </div>

                            <div class="h-16 flex items-center">
                                <ArrowRightOutlined class="text-gray-300 text-2xl" />
                            </div>

                            <!-- Step 4 -->
                            <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                                <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                                    <SearchOutlined />
                                </div>
                                <div class="text-[#140E35] font-medium text-base">检索应用</div>
                                <div class="text-gray-400 text-xs text-center">通过标签快速检索</div>
                            </div>
                        </div>
                    </div>

                    <!-- Search & Filter Bar -->
                    <div class="w-full bg-white rounded-lg p-4 mb-6 shadow-sm flex flex-col gap-4">
                        <div class="flex items-center gap-4 flex-wrap">
                            <div class="flex items-center gap-2">
                                <span class="text-gray-500 text-sm">标签类别:</span>
                                <el-select v-model="queryParams.categoryEq" placeholder="全部类别" clearable style="width: 160px" @change="handleQuery">
                                    <el-option
                                        v-for="dict in opt_list"
                                        :key="dict.value"
                                        :label="dict.label"
                                        :value="dict.value"
                                    />
                                </el-select>
                            </div>

                            <div class="flex items-center gap-2 ml-auto">
                                <el-input
                                    v-model="queryParams.tagNameLike"
                                    placeholder="搜索标签名称..."
                                    clearable
                                    style="width: 240px"
                                    @keyup.enter="handleQuery"
                                >
                                    <template #prefix>
                                        <el-icon><Search /></el-icon>
                                    </template>
                                </el-input>
                                <el-button type="primary" @click="handleQuery">搜索</el-button>
                                <el-button @click="resetQuery">重置</el-button>
                            </div>
                        </div>
                    </div>

                    <!-- Label List (Table) -->
                    <div class="w-full bg-white rounded-lg border border-gray-100 overflow-hidden" v-if="data.total > 0">
                        <el-table
                            v-if="refreshTable"
                            v-loading="loading"
                            :data="deptList"
                            :header-cell-style="{background:'#fafafa',color:'#606266', fontWeight:'600'}"
                        >
                            <el-table-column prop="tagName" label="标签名称" min-width="200">
                                <template #default="scope">
                                    <div class="flex items-center gap-2">
                                        <svg-icon v-if="scope.row.icon" :icon-class="scope.row.icon" class="w-5 h-5 text-gray-500" />
                                        <span class="font-medium text-gray-800">{{ scope.row.tagName }}</span>
                                    </div>
                                </template>
                            </el-table-column>

                            <el-table-column prop="category" label="类别" align="center" width="150">
                                <template #default="scope">
                                    <el-tag v-if="scope.row.category === 1" type="info" effect="light" round>系统支持</el-tag>
                                    <el-tag v-if="scope.row.category === 2" type="warning" effect="light" round>硬件支持</el-tag>
                                </template>
                            </el-table-column>

                            <el-table-column prop="isSuper" label="级别" align="center" width="150">
                                <template #default="scope">
                                    <el-tag :type="scope.row.isSuper === 0 ? '' : 'success'" effect="plain" size="small">
                                        {{ scope.row.isSuper === 0 ? '二级标签' : '一级标签' }}
                                    </el-tag>
                                </template>
                            </el-table-column>

                            <el-table-column label="创建时间" align="center" prop="createTime" width="200">
                                <template #default="scope">
                                    <span class="text-gray-500">{{ parseTime(scope.row.createTime) }}</span>
                                </template>
                            </el-table-column>

                            <el-table-column label="操作" align="center" width="180" fixed="right">
                                <template #default="scope">
                                    <div class="flex items-center justify-center gap-2">
                                        <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                                                   v-hasPermi="['modelmag:label:edit']">修改
                                        </el-button>
                                        <el-button link type="danger" icon="Delete"
                                                   @click="handleDelete(scope.row)" v-hasPermi="['modelmag:label:delete']">删除
                                        </el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>

                        <!-- Pagination (Moved inside the card) -->
                        <div class="flex justify-center py-4 border-t border-gray-100">
                            <a-pagination
                                v-model:current="queryParams.pageNo"
                                :total="data.total"
                                :page-size="queryParams.pageSize"
                                :page-size-options="['10', '20', '50']"
                                show-size-changer
                                show-quick-jumper
                                :show-total="total => `共 ${total} 条数据`"
                                @change="getList"
                                @showSizeChange="onShowSizeChange"
                            />
                        </div>
                    </div>

                    <div class="w-full m-auto py-20" v-else>
                        <el-empty description="暂无标签数据" />
                    </div>
                </div>
            </div>
        </div>

        <!-- 添加或修改部门对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body destroy-on-close>
            <el-form ref="deptRef" :model="form" :rules="rules" label-width="80px" class="pt-4">
                <el-form-item label="标签类别" prop="category">
                    <el-select
                        @change="() => {
                            form.isSuper = null
                        }"
                        v-model="form.category" placeholder="请选择类别" class="w-full">
                        <el-option :value="1" label="系统支持"></el-option>
                        <el-option :value="2" label="硬件支持"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="标签名称" prop="tagName">
                    <el-input v-model="form.tagName" placeholder="请输入标签名称"/>
                </el-form-item>

                <el-form-item label="标签级别" prop="isSuper">
                    <el-select
                        v-model="form.isSuper"
                        placeholder="请选择级别"
                        class="w-full"
                    >
                        <el-option label="一级标签" :value="1" v-if="form.category===2"></el-option>
                        <el-option label="二级标签" :value="0"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="上级标签" v-if="form.isSuper===0 && form.category===2" prop="parentId">
                    <el-input v-model="form.parentTagName" readonly placeholder="请选择上级标签" @click="openSelectDialog" class="cursor-pointer">
                        <template #append>
                            <el-button icon="Search" @click="openSelectDialog"></el-button>
                        </template>
                    </el-input>
                </el-form-item>

                <el-form-item label="菜单图标" prop="icon">
                    <el-popover
                        placement="bottom-start"
                        :width="540"
                        trigger="click"
                    >
                        <template #reference>
                            <el-input v-model="form.icon" placeholder="点击选择图标" @blur="showSelectIcon" readonly class="cursor-pointer">
                                <template #prefix>
                                    <svg-icon
                                        v-if="form.icon"
                                        :icon-class="form.icon"
                                        class="el-input__icon"
                                        style="height: 16px;width: 16px;"
                                    />
                                    <el-icon v-else class="el-input__icon"><Search /></el-icon>
                                </template>
                            </el-input>
                        </template>
                        <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon"/>
                    </el-popover>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="cancel">取 消</el-button>
                    <el-button type="primary" @click="submitForm">确 定</el-button>
                </div>
            </template>
        </el-dialog>

        <tag-select-dialog ref="tagSelectDialog" @emit-commit="handleSelectParentTag"></tag-select-dialog>
    </div>
</template>

<script setup name="Dept">
import {listDept} from "@/api/system/dept";
import {addLabel, deleteLabel, editLabel, getListByCondition} from "@/api/modelMag/label.js";
import SvgIcon from "@/components/SvgIcon/index.vue";
import IconSelect from "@/components/IconSelect/index.vue";
import TagSelectDialog from "@/views/model/components/tagSelectDialog.vue";
import {parseTime} from "../../../utils/ruoyi.js";
import {Search} from "@element-plus/icons-vue";
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  CloseOutlined,
  LinkOutlined,
  SearchOutlined,
  TagsOutlined
} from "@ant-design/icons-vue";

const {proxy} = getCurrentInstance();
const iconSelectRef = ref(null);
const tagSelectDialog = ref()
const deptRef = ref()

const opt_list = [
    {
        label: '系统支持',
        value: 1
    },
    {
        label: '硬件支持',
        value: 2
    }
]

const deptList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const deptOptions = ref([]);
const isExpandAll = ref(true);
const refreshTable = ref(true);
const showProcess = ref(true);

const data = reactive({
    form: {},
    total: 0,
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        tagNameLike: undefined,
        categoryEq: undefined
    },
    rules: {
        parentId: [{required: true, message: "上级部门不能为空", trigger: "blur"}],
        deptName: [{required: true, message: "部门名称不能为空", trigger: "blur"}],
        orderNum: [{required: true, message: "显示排序不能为空", trigger: "blur"}],
        email: [{type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"]}],
        phone: [{pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur"}]
    },
});

const {queryParams, form, rules} = toRefs(data);

const formatCategory = (row) => {
  switch (row.category) {
      case 1:
          return '系统支持';
      case 2:
          return '硬件支持'
  }
}

function showSelectIcon() {
    iconSelectRef.value.reset();
}

function selected(name) {
    form.value.icon = name;
}

function openSelectDialog(){
    tagSelectDialog.value.openDialog()
}

const handleSelectParentTag = (val) => {
    form.value.parentTagName = val.tagName;
    form.value.parentId = val.id;
}
/** 查询部门列表 */
function getList() {
    loading.value = true;
    getListByCondition(queryParams.value).then(response => {
        deptList.value = response.data.records;
        data.total = response.data.total;
        loading.value = false;
    });
}

const onShowSizeChange = (current, pageSize) => {
    queryParams.value.pageSize = pageSize;
    queryParams.value.pageNo = 1;
    getList();
};

/** 取消按钮 */
function cancel() {
    open.value = false;
    reset();
}

/** 表单重置 */
function reset() {
    form.value = {
        parentTagName: undefined,
        parentId: undefined,
        tagName: undefined,
        isSuper: undefined,
        category: undefined,
        icon: undefined
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
    title.value = "添加标签";
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
    form.value = row;
    open.value = true;
    title.value = "修改";
}

/** 提交按钮 */
function submitForm() {
    deptRef.value.validate(valid => {
        if (valid) {
            if (form.value.id !== undefined) {
                editLabel(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                if (form.value.category ===1){
                    form.value.parentId = undefined;
                    form.value.parentTagName = undefined;
                }

                addLabel(form.value).then(response => {
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
    proxy.$modal.confirm('是否确认删除名称为"' + row.tagName + '"的数据项?').then(function () {
        return deleteLabel({id:row.id});
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

getList();
</script>

<style scoped lang="scss">
/* 保持与其他页面一致的风格 */
</style>
