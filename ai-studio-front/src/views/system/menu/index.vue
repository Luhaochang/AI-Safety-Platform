<template>
    <div class="min-h-screen bg-[#f5f7fa] flex flex-col">
        <!-- 1. 头部区域 -->
        <div class="py-4 px-8 bg-[#f5f7fa]">
            <div class="text-[#140E35] text-3xl font-semibold">菜单管理</div>
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
                            <AppstoreAddOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">创建菜单</div>
                        <div class="text-gray-400 text-xs text-center">定义菜单名称与路径</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 2 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <SettingOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">配置属性</div>
                        <div class="text-gray-400 text-xs text-center">设置图标与排序</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 3 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <LockOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">权限标识</div>
                        <div class="text-gray-400 text-xs text-center">配置后端权限字符</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 4 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <CheckCircleOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">生效应用</div>
                        <div class="text-gray-400 text-xs text-center">刷新页面即可生效</div>
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
                            <span class="text-gray-600 text-sm font-medium">菜单名称</span>
                            <el-input
                                v-model="queryParams.menuName"
                                placeholder="请输入菜单名称"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><AppstoreOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">状态</span>
                            <el-select v-model="queryParams.status" placeholder="菜单状态" clearable class="w-40">
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
                                <MenuOutlined />
                            </div>
                            <span class="font-bold text-gray-800 text-base">菜单列表</span>
                        </div>
                        <div class="flex items-center gap-3">
                            <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:menu:add']">新增菜单</el-button>
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
                        :data="menuList"
                        row-key="menuId"
                        :default-expand-all="isExpandAll"
                        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                        height="100%"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px', textAlign: 'center' }"
                        :cell-style="{ textAlign: 'center' }"
                        :row-style="{ height: '55px' }"
                    >
                        <el-table-column prop="menuName" label="菜单名称" :show-overflow-tooltip="true" min-width="180" align="left" header-align="center">
                            <template #default="scope">
                                <div class="inline-flex items-center gap-2">
                                    <FolderOutlined v-if="scope.row.menuType === 'M'" class="text-blue-500 text-lg" />
                                    <FileTextOutlined v-else-if="scope.row.menuType === 'C'" class="text-indigo-500 text-lg" />
                                    <ThunderboltOutlined v-else-if="scope.row.menuType === 'F'" class="text-orange-500 text-lg" />
                                    <span class="text-gray-700 font-medium">{{ scope.row.menuName }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="icon" label="图标" align="center" min-width="80">
                            <template #default="scope">
                                <div class="flex justify-center items-center w-full h-full">
                                    <svg-icon :icon-class="scope.row.icon"/>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column prop="orderNum" label="排序" min-width="80" align="center"></el-table-column>
                        <el-table-column prop="perms" label="权限标识" :show-overflow-tooltip="true" min-width="150" align="center"></el-table-column>
                        <el-table-column prop="component" label="组件路径" :show-overflow-tooltip="true" min-width="180" align="center"></el-table-column>
                        <el-table-column prop="status" label="状态" min-width="100" align="center">
                            <template #default="scope">
                                <dict-tag :options="sys_normal_disable" :value="scope.row.status"/>
                            </template>
                        </el-table-column>
                        <el-table-column label="创建时间" align="center" min-width="160" prop="createTime">
                            <template #default="scope">
                                <span class="text-gray-500 text-sm">{{ parseTime(scope.row.createTime) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" align="center" width="220" fixed="right">
                            <template #default="scope">
                                <div class="flex items-center justify-center gap-2">
                                    <el-tooltip content="修改" placement="top" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleUpdate(scope.row)" v-hasPermi="['system:menu:edit']">
                                            <EditOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="新增" placement="top" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleAdd(scope.row)" v-hasPermi="['system:menu:add']">
                                            <PlusOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="删除" placement="top" :show-after="500">
                                        <el-button link type="danger" class="!p-1" @click="handleDelete(scope.row)" v-hasPermi="['system:menu:remove']">
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

        <!-- 添加或修改菜单对话框 -->
        <el-dialog :title="title" v-model="open" width="680px" append-to-body>
            <el-form ref="menuRef" :model="form" :rules="rules" label-width="100px">
                <el-row>
                    <el-col :span="24">
                        <el-form-item label="上级菜单">
                            <el-tree-select
                                v-model="form.parentId"
                                :data="menuOptions"
                                :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
                                value-key="menuId"
                                placeholder="选择上级菜单"
                                check-strictly
                            />
                        </el-form-item>
                    </el-col>
                    <el-col :span="24">
                        <el-form-item label="菜单类型" prop="menuType">
                            <el-radio-group v-model="form.menuType">
                                <el-radio value="M">目录</el-radio>
                                <el-radio value="C">菜单</el-radio>
                                <el-radio value="F">按钮</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType != 'F'">
                        <el-form-item label="菜单图标" prop="icon">
                            <el-popover
                                placement="bottom-start"
                                :width="540"
                                trigger="click"
                            >
                                <template #reference>
                                    <el-input v-model="form.icon" placeholder="点击选择图标" @blur="showSelectIcon"
                                              readonly>
                                        <template #prefix>
                                            <svg-icon
                                                v-if="form.icon"
                                                :icon-class="form.icon"
                                                class="el-input__icon"
                                                style="height: 32px;width: 16px;"
                                            />
                                            <el-icon v-else style="height: 32px;width: 16px;">
                                                <search/>
                                            </el-icon>
                                        </template>
                                    </el-input>
                                </template>
                                <icon-select ref="iconSelectRef" @selected="selected" :active-icon="form.icon"/>
                            </el-popover>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="显示排序" prop="orderNum">
                            <el-input-number v-model="form.orderNum" controls-position="right" :min="0"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="菜单名称" prop="menuName">
                            <el-input v-model="form.menuName" placeholder="请输入菜单名称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType == 'C'">
                        <el-form-item prop="routeName">
                            <template #label>
                        <span>
                           <el-tooltip
                               content="默认不填则和路由地址相同：如地址为：`user`，则名称为`User`（注意：因为router会删除名称相同路由，为避免名字的冲突，特殊情况下请自定义，保证唯一性）"
                               placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           路由名称
                        </span>
                            </template>
                            <el-input v-model="form.routeName" placeholder="请输入路由名称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType != 'F'">
                        <el-form-item>
                            <template #label>
                        <span>
                           <el-tooltip content="选择是外链则路由地址需要以`http(s)://`开头" placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>是否外链
                        </span>
                            </template>
                            <el-radio-group v-model="form.isFrame">
                                <el-radio value="0">是</el-radio>
                                <el-radio value="1">否</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType != 'F'">
                        <el-form-item prop="path">
                            <template #label>
                        <span>
                           <el-tooltip content="访问的路由地址，如：`user`，如外网地址需内链访问则以`http(s)://`开头"
                                       placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           路由地址
                        </span>
                            </template>
                            <el-input v-model="form.path" placeholder="请输入路由地址"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType == 'C'">
                        <el-form-item prop="component">
                            <template #label>
                        <span>
                           <el-tooltip content="访问的组件路径，如：`system/user/index`，默认在`views`目录下"
                                       placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           组件路径
                        </span>
                            </template>
                            <el-input v-model="form.component" placeholder="请输入组件路径"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType != 'M'">
                        <el-form-item>
                            <el-input v-model="form.perms" placeholder="请输入权限标识" maxlength="100"/>
                            <template #label>
                        <span>
                           <el-tooltip
                               content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasPermi('system:user:list')`)"
                               placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           权限字符
                        </span>
                            </template>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType == 'C'">
                        <el-form-item>
                            <el-input v-model="form.query" placeholder="请输入路由参数" maxlength="255"/>
                            <template #label>
                        <span>
                           <el-tooltip content='访问路由的默认传递参数，如：`{"id": 1, "name": "ry"}`' placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           路由参数
                        </span>
                            </template>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType == 'C'">
                        <el-form-item>
                            <template #label>
                        <span>
                           <el-tooltip content="选择是则会被`keep-alive`缓存，需要匹配组件的`name`和地址保持一致"
                                       placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           是否缓存
                        </span>
                            </template>
                            <el-radio-group v-model="form.isCache">
                                <el-radio value="0">缓存</el-radio>
                                <el-radio value="1">不缓存</el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12" v-if="form.menuType != 'F'">
                        <el-form-item>
                            <template #label>
                        <span>
                           <el-tooltip content="选择隐藏则路由将不会出现在侧边栏，但仍然可以访问" placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           显示状态
                        </span>
                            </template>
                            <el-radio-group v-model="form.visible">
                                <el-radio
                                    v-for="dict in sys_show_hide"
                                    :key="dict.value"
                                    :value="dict.value"
                                >{{ dict.label }}
                                </el-radio>
                            </el-radio-group>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item>
                            <template #label>
                        <span>
                           <el-tooltip content="选择停用则路由将不会出现在侧边栏，也不能被访问" placement="top">
                              <el-icon><question-filled/></el-icon>
                           </el-tooltip>
                           菜单状态
                        </span>
                            </template>
                            <el-radio-group v-model="form.status">
                                <el-radio
                                    v-for="dict in sys_normal_disable"
                                    :key="dict.value"
                                    :value="dict.value"
                                >{{ dict.label }}
                                </el-radio>
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

<script setup name="Menu">
import {addMenu, delMenu, getMenu, listMenu, updateMenu} from "@/api/system/menu";
import SvgIcon from "@/components/SvgIcon";
import IconSelect from "@/components/IconSelect";
import {
  AppstoreAddOutlined,
  ArrowRightOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  LockOutlined,
  SettingOutlined,
  AppstoreOutlined,
  MenuOutlined,
  EditOutlined,
  PlusOutlined,
  DeleteOutlined,
  FolderOutlined,
  FileTextOutlined,
  ThunderboltOutlined
} from "@ant-design/icons-vue";

const {proxy} = getCurrentInstance();
const {sys_show_hide, sys_normal_disable} = proxy.useDict("sys_show_hide", "sys_normal_disable");

const showProcess = ref(true);

const menuList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const title = ref("");
const menuOptions = ref([]);
const isExpandAll = ref(false);
const refreshTable = ref(true);
const iconSelectRef = ref(null);

const data = reactive({
    form: {},
    queryParams: {
        menuName: undefined,
        visible: undefined
    },
    rules: {
        menuName: [{required: true, message: "菜单名称不能为空", trigger: "blur"}],
        orderNum: [{required: true, message: "菜单顺序不能为空", trigger: "blur"}],
        path: [{required: true, message: "路由地址不能为空", trigger: "blur"}]
    },
});

const {queryParams, form, rules} = toRefs(data);

/** 查询菜单列表 */
function getList() {
    loading.value = true;
    listMenu(queryParams.value).then(response => {
        menuList.value = proxy.handleTree(response.data, "menuId");
        loading.value = false;
    });
}

/** 查询菜单下拉树结构 */
function getTreeselect() {
    menuOptions.value = [];
    listMenu().then(response => {
        const menu = {menuId: 0, menuName: "主类目", children: []};
        menu.children = proxy.handleTree(response.data, "menuId");
        menuOptions.value.push(menu);

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
        menuId: undefined,
        parentId: 0,
        menuName: undefined,
        icon: undefined,
        menuType: "M",
        orderNum: undefined,
        isFrame: "1",
        isCache: "0",
        visible: "0",
        status: "0"
    };
    proxy.resetForm("menuRef");
}

/** 展示下拉图标 */
function showSelectIcon() {
    iconSelectRef.value.reset();
}

/** 选择图标 */
function selected(name) {
    form.value.icon = name;
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
    getTreeselect();
    if (row != null && row.menuId) {
        form.value.parentId = row.menuId;
    } else {
        form.value.parentId = 0;
    }
    open.value = true;
    title.value = "添加菜单";
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
async function handleUpdate(row) {
    reset();
    await getTreeselect();
    getMenu(row.menuId).then(response => {
        form.value = response.data;
        open.value = true;
        title.value = "修改菜单";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["menuRef"].validate(valid => {
        if (valid) {
            if (form.value.menuId !== undefined) {
                updateMenu(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addMenu(form.value).then(response => {
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
    proxy.$modal.confirm('是否确认删除名称为"' + row.menuName + '"的数据项?').then(function () {
        return delMenu(row.menuId);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
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
