<template>
    <div class="min-h-screen bg-[#f5f7fa] flex flex-col">
        <!-- 1. 头部区域 -->
        <div class="py-4 px-8 bg-[#f5f7fa]">
            <div class="text-[#140E35] text-3xl font-semibold">角色管理</div>
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
                            <UsergroupAddOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">创建角色</div>
                        <div class="text-gray-400 text-xs text-center">定义角色名称与标识</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 2 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <MenuOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">菜单权限</div>
                        <div class="text-gray-400 text-xs text-center">分配角色可访问菜单</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 3 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <DatabaseOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">数据权限</div>
                        <div class="text-gray-400 text-xs text-center">配置数据可见范围</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 4 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <UserSwitchOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">分配用户</div>
                        <div class="text-gray-400 text-xs text-center">将角色授权给用户</div>
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
                            <span class="text-gray-600 text-sm font-medium">角色名称</span>
                            <el-input
                                v-model="queryParams.roleNameLike"
                                placeholder="请输入角色名称"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><UserOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">权限字符</span>
                            <el-input
                                v-model="queryParams.roleKeyLike"
                                placeholder="请输入权限字符"
                                clearable
                                class="w-48"
                                @keyup.enter="handleQuery"
                            >
                                <template #prefix><KeyOutlined class="text-gray-400"/></template>
                            </el-input>
                        </div>
                        <div class="flex items-center gap-2">
                            <span class="text-gray-600 text-sm font-medium">状态</span>
                            <el-select
                                v-model="queryParams.statusEq"
                                placeholder="角色状态"
                                clearable
                                class="w-40"
                            >
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
                                <TeamOutlined />
                            </div>
                            <span class="font-bold text-gray-800 text-base">角色列表</span>
                            <span class="text-gray-400 text-xs ml-2 bg-gray-100 px-2 py-0.5 rounded-full">共 {{ total }} 个</span>
                        </div>
                        <div class="flex items-center gap-3">
                            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增角色</el-button>
                            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
                            <el-button type="warning" plain icon="Download" @click="handleExport">导出数据</el-button>
                            <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :search="false"></right-toolbar>
                        </div>
                    </div>
                </div>

                <!-- 表格数据 -->
                <div class="flex-1 overflow-hidden px-5 pb-0">
                    <el-table
                        v-loading="loading"
                        :data="roleList"
                        @selection-change="handleSelectionChange"
                        height="100%"
                        :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px', textAlign: 'center' }"
                        :cell-style="{ textAlign: 'center' }"
                        :row-style="{ height: '55px' }"
                    >
                        <el-table-column type="selection" width="55" align="center"/>
                        <el-table-column label="角色编号" prop="roleId" min-width="100" align="center"/>
                        <el-table-column label="角色名称" prop="roleName" :show-overflow-tooltip="true" min-width="150" align="center">
                            <template #default="scope">
                                <span class="font-medium text-gray-800">{{ scope.row.roleName }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="权限字符" prop="roleKey" :show-overflow-tooltip="true" min-width="150" align="center">
                            <template #default="scope">
                                <el-tag type="info" effect="plain" class="border-0 bg-gray-100 text-gray-600">{{ scope.row.roleKey }}</el-tag>
                            </template>
                        </el-table-column>
                        <el-table-column label="显示顺序" prop="roleSort" min-width="100" align="center"/>
                        <el-table-column label="状态" align="center" min-width="100">
                            <template #default="scope">
                                <el-switch
                                    v-model="scope.row.status"
                                    active-value="0"
                                    inactive-value="1"
                                    inline-prompt
                                    active-text="启用"
                                    inactive-text="停用"
                                    style="--el-switch-on-color: #10b981; --el-switch-off-color: #cbd5e1"
                                    @change="handleStatusChange(scope.row)"
                                ></el-switch>
                            </template>
                        </el-table-column>
                        <el-table-column label="创建时间" align="center" prop="createTime" min-width="160">
                            <template #default="scope">
                                <span class="text-gray-500 text-sm">{{ parseTime(scope.row.createTime) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" align="center" width="200" fixed="right">
                            <template #default="scope">
                                <div class="flex items-center justify-center gap-2">
                                    <el-tooltip content="修改" placement="top" v-if="scope.row.roleId !== 1" :show-after="500">
                                        <el-button link type="primary" class="!p-1" @click="handleUpdate(scope.row)">
                                            <EditOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="数据权限" placement="top" v-if="scope.row.roleId !== 1" :show-after="500">
                                        <el-button link type="success" class="!p-1" @click="handleDataScope(scope.row)">
                                            <DatabaseOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="分配用户" placement="top" v-if="scope.row.roleId !== 1" :show-after="500">
                                        <el-button link type="warning" class="!p-1" @click="handleAuthUser(scope.row)">
                                            <UserSwitchOutlined class="text-lg" />
                                        </el-button>
                                    </el-tooltip>
                                    <el-tooltip content="删除" placement="top" v-if="scope.row.roleId !== 1" :show-after="500">
                                        <el-button link type="danger" class="!p-1" @click="handleDelete(scope.row)">
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

        <!-- 添加或修改角色配置对话框 -->
        <el-dialog :title="title" v-model="open" width="500px" append-to-body>
            <el-form ref="roleRef" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="角色名称" prop="roleName">
                    <el-input v-model="form.roleName" placeholder="请输入角色名称"/>
                </el-form-item>
                <el-form-item prop="roleKey">
                    <template #label>
                  <span>
                     <el-tooltip content="控制器中定义的权限字符，如：@PreAuthorize(`@ss.hasRole('admin')`)"
                                 placement="top">
                        <el-icon><question-filled/></el-icon>
                     </el-tooltip>
                     权限字符
                  </span>
                    </template>
                    <el-input v-model="form.roleKey" placeholder="请输入权限字符"/>
                </el-form-item>
                <el-form-item label="角色顺序" prop="roleSort">
                    <el-input-number v-model="form.roleSort" controls-position="right" :min="0"/>
                </el-form-item>
                <el-form-item label="状态">
                    <el-radio-group v-model="form.status">
                        <el-radio
                            v-for="dict in sys_normal_disable"
                            :key="dict.value"
                            :value="dict.value"
                        >{{ dict.label }}
                        </el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="菜单权限">
                    <el-checkbox v-model="menuExpand" @change="handleCheckedTreeExpand($event, 'menu')">展开/折叠
                    </el-checkbox>
                    <el-checkbox v-model="menuNodeAll" @change="handleCheckedTreeNodeAll($event, 'menu')">全选/全不选
                    </el-checkbox>
                    <el-checkbox v-model="form.menuCheckStrictly" @change="handleCheckedTreeConnect($event, 'menu')">
                        父子联动
                    </el-checkbox>
                    <el-tree
                        class="tree-border"
                        :data="menuOptions"
                        show-checkbox
                        ref="menuRef"
                        node-key="id"
                        :check-strictly="!form.menuCheckStrictly"
                        empty-text="加载中，请稍候"
                        :props="{ label: 'label', children: 'children' }"
                    ></el-tree>
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitForm">确 定</el-button>
                    <el-button @click="cancel">取 消</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 分配角色数据权限对话框 -->
        <el-dialog :title="title" v-model="openDataScope" width="500px" append-to-body>
            <el-form :model="form" label-width="80px">
                <el-form-item label="角色名称">
                    <el-input v-model="form.roleName" :disabled="true"/>
                </el-form-item>
                <el-form-item label="权限字符">
                    <el-input v-model="form.roleKey" :disabled="true"/>
                </el-form-item>
                <el-form-item label="权限范围">
                    <el-select v-model="form.dataScope" @change="dataScopeSelectChange">
                        <el-option
                            v-for="item in dataScopeOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="数据权限" v-show="form.dataScope == 2">
                    <el-checkbox v-model="deptExpand" @change="handleCheckedTreeExpand($event, 'dept')">展开/折叠
                    </el-checkbox>
                    <el-checkbox v-model="deptNodeAll" @change="handleCheckedTreeNodeAll($event, 'dept')">全选/全不选
                    </el-checkbox>
                    <el-checkbox v-model="form.deptCheckStrictly" @change="handleCheckedTreeConnect($event, 'dept')">
                        父子联动
                    </el-checkbox>
                    <el-tree
                        class="tree-border"
                        :data="deptOptions"
                        show-checkbox
                        default-expand-all
                        ref="deptRef"
                        node-key="id"
                        :check-strictly="!form.deptCheckStrictly"
                        empty-text="加载中，请稍候"
                        :props="{ label: 'label', children: 'children' }"
                    ></el-tree>
                </el-form-item>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitDataScope">确 定</el-button>
                    <el-button @click="cancelDataScope">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup name="Role">
import {
  addRole,
  changeRoleStatus,
  dataScope,
  delRole,
  deptTreeSelect,
  getRole,
  listRole,
  updateRole
} from "@/api/system/role";
import {roleMenuTreeselect, treeselect as menuTreeselect} from "@/api/system/menu";
import {
  ArrowRightOutlined,
  CloseOutlined,
  DatabaseOutlined,
  MenuOutlined,
  UsergroupAddOutlined,
  UserSwitchOutlined,
  UserOutlined,
  KeyOutlined,
  TeamOutlined,
  EditOutlined,
  DeleteOutlined
} from "@ant-design/icons-vue";

const router = useRouter();
const {proxy} = getCurrentInstance();
const {sys_normal_disable} = proxy.useDict("sys_normal_disable");

const showProcess = ref(true);

const roleList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const menuOptions = ref([]);
const menuExpand = ref(false);
const menuNodeAll = ref(false);
const deptExpand = ref(true);
const deptNodeAll = ref(false);
const deptOptions = ref([]);
const openDataScope = ref(false);
const menuRef = ref(null);
const deptRef = ref(null);

/** 数据范围选项*/
const dataScopeOptions = ref([
    {value: "1", label: "全部数据权限"},
    {value: "2", label: "自定数据权限"},
    {value: "3", label: "本部门数据权限"},
    {value: "4", label: "本部门及以下数据权限"},
    {value: "5", label: "仅本人数据权限"}
]);

const data = reactive({
    form: {},
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        roleNameLike: undefined,
        roleKeyLike: undefined,
        statusEq: undefined,
        delFlagEq: 0
    },
    rules: {
        roleName: [{required: true, message: "角色名称不能为空", trigger: "blur"}],
        roleKey: [{required: true, message: "权限字符不能为空", trigger: "blur"}],
        roleSort: [{required: true, message: "角色顺序不能为空", trigger: "blur"}]
    },
});

const {queryParams, form, rules} = toRefs(data);

/** 查询角色列表 */
function getList() {
    loading.value = true;
    listRole(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
        const {data} = response
        roleList.value = data.records;
        total.value = data.total;
        loading.value = false;
    });
}

/** 搜索按钮操作 */
function handleQuery() {
    queryParams.value.pageNo = 1;
    getList();
}

/** 重置按钮操作 */
function resetQuery() {
    dateRange.value = [];
    proxy.resetForm("queryRef");
    handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
    const roleIds = row.roleId || ids.value;
    proxy.$modal.confirm('是否确认删除角色编号为"' + roleIds + '"的数据项?').then(function () {
        return delRole(roleIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

/** 导出按钮操作 */
function handleExport() {
    proxy.download("system/role/export", {
        ...queryParams.value,
    }, `role_${new Date().getTime()}.xlsx`);
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.roleId);
    single.value = selection.length != 1;
    multiple.value = !selection.length;
}

/** 角色状态修改 */
function handleStatusChange(row) {
    let text = row.status === "0" ? "启用" : "停用";
    proxy.$modal.confirm('确认要"' + text + '""' + row.roleName + '"角色吗?').then(function () {
        return changeRoleStatus(row.roleId, row.status);
    }).then(() => {
        proxy.$modal.msgSuccess(text + "成功");
    }).catch(function () {
        row.status = row.status === "0" ? "1" : "0";
    });
}

/** 更多操作 */
function handleCommand(command, row) {
    switch (command) {
        case "handleDataScope":
            handleDataScope(row);
            break;
        case "handleAuthUser":
            handleAuthUser(row);
            break;
        default:
            break;
    }
}

/** 分配用户 */
function handleAuthUser(row) {
    router.push("/system/role-auth/user/" + row.roleId);
}

/** 查询菜单树结构 */
function getMenuTreeselect() {
    menuTreeselect().then(response => {
        menuOptions.value = response.data;
    });
}

/** 所有部门节点数据 */
function getDeptAllCheckedKeys() {
    // 目前被选中的部门节点
    let checkedKeys = deptRef.value.getCheckedKeys();
    // 半选中的部门节点
    let halfCheckedKeys = deptRef.value.getHalfCheckedKeys();
    checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
    return checkedKeys;
}

/** 重置新增的表单以及其他数据  */
function reset() {
    if (menuRef.value != undefined) {
        menuRef.value.setCheckedKeys([]);
    }
    menuExpand.value = false;
    menuNodeAll.value = false;
    deptExpand.value = true;
    deptNodeAll.value = false;
    form.value = {
        roleId: undefined,
        roleName: undefined,
        roleKey: undefined,
        roleSort: 0,
        status: "0",
        menuIds: [],
        deptIds: [],
        menuCheckStrictly: true,
        deptCheckStrictly: true,
        remark: undefined
    };
    proxy.resetForm("roleRef");
}

/** 添加角色 */
function handleAdd() {
    reset();
    getMenuTreeselect();
    open.value = true;
    title.value = "添加角色";
}

/** 修改角色 */
function handleUpdate(row) {
    reset();
    const roleId = row.roleId || ids.value;
    const roleMenu = getRoleMenuTreeselect(roleId);
    getRole(roleId).then(response => {
        form.value = response.data;
        form.value.roleSort = Number(form.value.roleSort);
        open.value = true;
        nextTick(() => {
            roleMenu.then((res) => {
                let checkedKeys = res.checkedKeys;
                checkedKeys.forEach((v) => {
                    nextTick(() => {
                        menuRef.value.setChecked(v, true, false);
                    });
                });
            });
        });
        title.value = "修改角色";
    });
}

/** 根据角色ID查询菜单树结构 */
function getRoleMenuTreeselect(roleId) {
    return roleMenuTreeselect(roleId).then(response => {
        menuOptions.value = response.data.menus;
        return response.data;
    });
}

/** 根据角色ID查询部门树结构 */
function getDeptTree(roleId) {
    return deptTreeSelect(roleId).then(response => {
        deptOptions.value = response.data.deptList;
        return response;
    });
}

/** 树权限（展开/折叠）*/
function handleCheckedTreeExpand(value, type) {
    if (type == "menu") {
        let treeList = menuOptions.value;
        for (let i = 0; i < treeList.length; i++) {
            menuRef.value.store.nodesMap[treeList[i].id].expanded = value;
        }
    } else if (type == "dept") {
        let treeList = deptOptions.value;
        for (let i = 0; i < treeList.length; i++) {
            deptRef.value.store.nodesMap[treeList[i].id].expanded = value;
        }
    }
}

/** 树权限（全选/全不选） */
function handleCheckedTreeNodeAll(value, type) {
    if (type == "menu") {
        menuRef.value.setCheckedNodes(value ? menuOptions.value : []);
    } else if (type == "dept") {
        deptRef.value.setCheckedNodes(value ? deptOptions.value : []);
    }
}

/** 树权限（父子联动） */
function handleCheckedTreeConnect(value, type) {
    if (type == "menu") {
        form.value.menuCheckStrictly = value ? true : false;
    } else if (type == "dept") {
        form.value.deptCheckStrictly = value ? true : false;
    }
}

/** 所有菜单节点数据 */
function getMenuAllCheckedKeys() {
    // 目前被选中的菜单节点
    let checkedKeys = menuRef.value.getCheckedKeys();
    // 半选中的菜单节点
    let halfCheckedKeys = menuRef.value.getHalfCheckedKeys();
    checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
    return checkedKeys;
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["roleRef"].validate(valid => {
        if (valid) {
            if (form.value.roleId != undefined) {
                form.value.menuIds = getMenuAllCheckedKeys();
                updateRole(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                form.value.menuIds = getMenuAllCheckedKeys();
                proxy.$modal.msgSuccess("新增成功");
                open.value = false;
                getList();
                addRole(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}

/** 取消按钮 */
function cancel() {
    open.value = false;
    reset();
}

/** 选择角色权限范围触发 */
function dataScopeSelectChange(value) {
    if (value !== "2") {
        deptRef.value.setCheckedKeys([]);
    }
}

/** 分配数据权限操作 */
function handleDataScope(row) {
    reset();
    const deptTreeSelect = getDeptTree(row.roleId);
    getRole(row.roleId).then(response => {
        form.value = response.data;
        openDataScope.value = true;
        nextTick(() => {
            deptTreeSelect.then(res => {
                nextTick(() => {
                    if (deptRef.value) {
                        deptRef.value.setCheckedKeys(res.data.checkedKeys);
                    }
                });
            });
        });
        title.value = "分配数据权限";
    });
}

/** 提交按钮（数据权限） */
function submitDataScope() {
    if (form.value.roleId != undefined) {
        form.value.deptIds = getDeptAllCheckedKeys();
        dataScope(form.value).then(response => {
            proxy.$modal.msgSuccess("修改成功");
            openDataScope.value = false;
            getList();
        });
    }
}

/** 取消按钮（数据权限）*/
function cancelDataScope() {
    openDataScope.value = false;
    reset();
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
