<template>
    <div class="min-h-screen bg-[#f5f7fa] flex flex-col">
        <!-- 1. 头部区域 -->
        <div class="py-4 px-8 bg-[#f5f7fa]">
            <div class="text-[#140E35] text-3xl font-semibold">用户管理</div>
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
                            <UserAddOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">创建用户</div>
                        <div class="text-gray-400 text-xs text-center">填写用户基本信息</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 2 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-indigo-50 flex items-center justify-center text-indigo-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <ApartmentOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">分配部门</div>
                        <div class="text-gray-400 text-xs text-center">设置用户归属部门</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 3 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-purple-50 flex items-center justify-center text-purple-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <SolutionOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">分配角色</div>
                        <div class="text-gray-400 text-xs text-center">配置用户角色权限</div>
                    </div>

                    <div class="h-16 flex items-center">
                        <ArrowRightOutlined class="text-gray-300 text-2xl" />
                    </div>

                    <!-- Step 4 -->
                    <div class="flex flex-col items-center gap-1 group cursor-default w-32">
                        <div class="w-16 h-16 rounded-2xl bg-orange-50 flex items-center justify-center text-orange-600 text-3xl shadow-sm group-hover:scale-110 transition-transform duration-300 mb-3">
                            <CheckCircleOutlined />
                        </div>
                        <div class="text-[#140E35] font-medium text-base">完成配置</div>
                        <div class="text-gray-400 text-xs text-center">用户可正常登录使用</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 3. 主体内容区域 -->
        <div class="px-8 pb-6 flex-1">
            <el-row :gutter="20" class="h-full">
                <!--部门数据-->
                <el-col :span="5" :xs="24">
                    <div class="bg-white rounded-xl h-full border border-gray-100 shadow-sm flex flex-col overflow-hidden">
                        <!-- 标题 -->
                        <div class="px-5 py-4 border-b border-gray-100 flex items-center gap-3 bg-gray-50/30">
                            <div class="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 shadow-sm">
                                <ApartmentOutlined />
                            </div>
                            <span class="font-bold text-gray-800 text-base">部门列表</span>
                        </div>

                        <!-- 搜索 -->
                        <div class="p-4">
                            <el-input
                                v-model="deptName"
                                clearable
                                placeholder="搜索部门名称..."
                                prefix-icon="Search"
                                class="w-full"
                            />
                        </div>

                        <!-- 树 -->
                        <div class="flex-1 overflow-auto custom-scrollbar px-2 pb-4">
                            <el-tree
                                ref="deptTreeRef"
                                :data="deptOptions"
                                :expand-on-click-node="false"
                                :filter-node-method="filterNode"
                                :props="{ label: 'label', children: 'children' }"
                                default-expand-all
                                highlight-current
                                node-key="id"
                                @node-click="handleNodeClick"
                                class="dept-tree"
                            >
                                <template #default="{ node, data }">
                                    <div class="flex items-center gap-2 w-full py-1">
                                        <!-- 根据层级显示不同图标 -->
                                        <BankOutlined v-if="node.level === 1" class="text-blue-600 text-lg" />
                                        <ClusterOutlined v-else-if="node.level === 2" class="text-indigo-500 text-lg" />
                                        <TeamOutlined v-else class="text-sky-400 text-lg" />
                                        <span class="text-gray-700 text-sm truncate">{{ node.label }}</span>
                                    </div>
                                </template>
                            </el-tree>
                        </div>
                    </div>
                </el-col>
                <!--用户数据-->
                <el-col :span="19" :xs="24">
                    <div class="bg-white rounded-xl h-full border border-gray-100 shadow-sm flex flex-col overflow-hidden">
                        <!-- 顶部搜索与操作区 -->
                        <div class="p-5 border-b border-gray-100 space-y-4 bg-white">
                            <!-- 搜索表单 -->
                            <div class="flex items-center gap-4 flex-wrap">
                                <div class="flex items-center gap-2">
                                    <span class="text-gray-600 text-sm font-medium">用户名称</span>
                                    <el-input
                                        v-model="queryParams.userNameLike"
                                        clearable
                                        placeholder="请输入用户名称"
                                        class="w-48"
                                        @keyup.enter="handleQuery"
                                    >
                                        <template #prefix><UserOutlined class="text-gray-400"/></template>
                                    </el-input>
                                </div>
                                <div class="flex items-center gap-2">
                                    <span class="text-gray-600 text-sm font-medium">手机号码</span>
                                    <el-input
                                        v-model="queryParams.phoneNumberLike"
                                        clearable
                                        placeholder="请输入手机号码"
                                        class="w-48"
                                        @keyup.enter="handleQuery"
                                    >
                                        <template #prefix><PhoneOutlined class="text-gray-400"/></template>
                                    </el-input>
                                </div>
                                <div class="flex items-center gap-2">
                                    <span class="text-gray-600 text-sm font-medium">状态</span>
                                    <el-select
                                        v-model="queryParams.statusEq"
                                        clearable
                                        placeholder="用户状态"
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
                                    <span class="font-bold text-gray-800 text-base">用户列表</span>
                                    <span class="text-gray-400 text-xs ml-2 bg-gray-100 px-2 py-0.5 rounded-full">共 {{ total }} 人</span>
                                </div>
                                <div class="flex items-center gap-3">
                                    <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['system:user:add']">新增用户</el-button>
                                    <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">删除</el-button>

                                    <el-dropdown trigger="click">
                                        <el-button plain>
                                            更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
                                        </el-button>
                                        <template #dropdown>
                                            <el-dropdown-menu>
                                                <el-dropdown-item icon="Upload" @click="handleImport" v-hasPermi="['system:user:import']">导入数据</el-dropdown-item>
                                                <el-dropdown-item icon="Download" @click="handleExport" v-hasPermi="['system:user:export']">导出数据</el-dropdown-item>
                                            </el-dropdown-menu>
                                        </template>
                                    </el-dropdown>

                                    <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns" :search="false"></right-toolbar>
                                </div>
                            </div>
                        </div>

                        <!-- 表格区域 -->
                        <div class="flex-1 overflow-hidden px-5 pb-0">
                            <el-table
                                v-loading="loading"
                                :data="userList"
                                @selection-change="handleSelectionChange"
                                height="100%"
                                :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '50px', textAlign: 'center' }"
                                :cell-style="{ textAlign: 'center' }"
                                :row-style="{ height: '55px' }"
                            >
                                <el-table-column align="center" type="selection" width="50"/>
                                <el-table-column v-if="columns[0].visible" key="userId" align="center" label="ID" prop="userId" width="80" />
                                <el-table-column v-if="columns[1].visible" key="userName" :show-overflow-tooltip="true" align="center" label="用户名称" prop="userName" min-width="160">
                                    <template #default="scope">
                                        <div class="flex items-center gap-3 ml-16">
                                            <div class="w-9 h-9 rounded-full bg-gradient-to-br from-blue-50 to-indigo-50 text-indigo-600 flex items-center justify-center text-sm font-bold border border-indigo-100 flex-shrink-0">
                                                {{ scope.row.userName.charAt(0).toUpperCase() }}
                                            </div>
                                            <div class="flex flex-col items-start">
                                                <span class="text-gray-800 font-medium leading-tight">{{ scope.row.userName }}</span>
                                                <span class="text-gray-400 text-xs mt-0.5">{{ scope.row.nickName }}</span>
                                            </div>
                                        </div>
                                    </template>
                                </el-table-column>

                                <el-table-column v-if="columns[3].visible" key="deptName" :show-overflow-tooltip="true" align="center" label="归属部门" prop="dept.deptName" min-width="140">
                                    <template #default="scope">
                                        <el-tag v-if="scope.row.dept" type="info" effect="plain" round class="border-0 bg-gray-100 text-gray-600">
                                            {{ scope.row.dept.deptName }}
                                        </el-tag>
                                    </template>
                                </el-table-column>

                                <el-table-column v-if="columns[4].visible" key="phoneNumber" align="center" label="手机号码" prop="phoneNumber" min-width="120"/>

                                <el-table-column v-if="columns[5].visible" key="status" align="center" label="状态" min-width="100">
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

                                <el-table-column v-if="columns[6].visible" align="center" label="创建时间" prop="createTime" min-width="160">
                                    <template #default="scope">
                                        <span class="text-gray-500 text-sm">{{ parseTime(scope.row.createTime) }}</span>
                                    </template>
                                </el-table-column>

                                <el-table-column align="center" label="操作" width="180" fixed="right">
                                    <template #default="scope">
                                        <div class="flex items-center justify-center gap-2">
                                            <el-tooltip content="修改" placement="top" :show-after="500">
                                                <el-button v-hasPermi="['system:user:edit']" type="primary" link class="!p-1" @click="handleUpdate(scope.row)">
                                                    <EditOutlined class="text-lg" />
                                                </el-button>
                                            </el-tooltip>
                                            <el-tooltip content="分配角色" placement="top" :show-after="500">
                                                <el-button v-hasPermi="['system:user:edit']" type="success" link class="!p-1" @click="handleAuthRole(scope.row)">
                                                    <SolutionOutlined class="text-lg" />
                                                </el-button>
                                            </el-tooltip>
                                            <el-tooltip content="重置密码" placement="top" :show-after="500">
                                                <el-button v-hasPermi="['system:user:resetPwd']" type="warning" link class="!p-1" @click="handleResetPwd(scope.row)">
                                                    <KeyOutlined class="text-lg" />
                                                </el-button>
                                            </el-tooltip>
                                            <el-tooltip content="删除" placement="top" :show-after="500">
                                                <el-button v-hasPermi="['system:user:remove']" type="danger" link class="!p-1" @click="handleDelete(scope.row)">
                                                    <DeleteOutlined class="text-lg" />
                                                </el-button>
                                            </el-tooltip>
                                        </div>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>

                        <!-- 分页 -->
                        <div class="p-4 border-t border-gray-100 bg-gray-50/30">
                            <div class="flex justify-end">
                                <a-pagination
                                    v-model:current="queryParams.pageNo"
                                    :page-size="queryParams.pageSize"
                                    :page-size-options="['10', '20', '50']"
                                    :show-total="total => `共 ${total} 条`"
                                    :total="total"
                                    show-quick-jumper
                                    show-size-changer
                                    @change="getList"
                                    @showSizeChange="getList"
                                />
                            </div>
                        </div>
                    </div>
                </el-col>
            </el-row>
        </div>

        <!-- 添加或修改用户配置对话框 -->
        <el-dialog v-model="open" :title="title" append-to-body width="600px">
            <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="用户昵称" prop="nickName">
                            <el-input v-model="form.nickName" maxlength="30" placeholder="请输入用户昵称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="归属部门" prop="deptId">
                            <el-tree-select
                                v-model="form.deptId"
                                :data="deptOptions"
                                :props="{ value: 'id', label: 'label', children: 'children' }"
                                check-strictly
                                placeholder="请选择归属部门"
                                value-key="id"
                            />
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="手机号码" prop="phoneNumber">
                            <el-input v-model="form.phoneNumber" maxlength="11" placeholder="请输入手机号码"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="邮箱" prop="email">
                            <el-input v-model="form.email" maxlength="50" placeholder="请输入邮箱"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item v-if="form.userId == undefined" label="用户名称" prop="userName">
                            <el-input v-model="form.userName" maxlength="30" placeholder="请输入用户名称"/>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
                            <el-input v-model="form.password" maxlength="20" placeholder="请输入用户密码"
                                      show-password type="password"/>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="用户性别">
                            <el-select v-model="form.sex" placeholder="请选择">
                                <el-option
                                    v-for="dict in sys_user_sex"
                                    :key="dict.value"
                                    :label="dict.label"
                                    :value="dict.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
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
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="岗位">
                            <el-select v-model="form.postIds" multiple placeholder="请选择">
                                <el-option
                                    v-for="item in postOptions"
                                    :key="item.postId"
                                    :disabled="item.status == 1"
                                    :label="item.postName"
                                    :value="item.postId"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="角色">
                            <el-select v-model="form.roleIds" multiple placeholder="请选择">
                                <el-option
                                    v-for="item in roleOptions"
                                    :key="item.roleId"
                                    :disabled="item.status == 1"
                                    :label="item.roleName"
                                    :value="item.roleId"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="24">
                        <el-form-item label="备注">
                            <el-input v-model="form.remark" placeholder="请输入内容" type="textarea"></el-input>
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

        <!-- 用户导入对话框 -->
        <el-dialog v-model="upload.open" :title="upload.title" append-to-body width="400px">
            <el-upload
                ref="uploadRef"
                :action="upload.url + '?updateSupport=' + upload.updateSupport"
                :auto-upload="false"
                :disabled="upload.isUploading"
                :headers="upload.headers"
                :limit="1"
                :on-progress="handleFileUploadProgress"
                :on-success="handleFileSuccess"
                accept=".xlsx, .xls"
                drag
            >
                <el-icon class="el-icon--upload">
                    <upload-filled/>
                </el-icon>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                <template #tip>
                    <div class="el-upload__tip text-center">
                        <div class="el-upload__tip">
                            <el-checkbox v-model="upload.updateSupport"/>
                            是否更新已经存在的用户数据
                        </div>
                        <span>仅允许导入xls、xlsx格式文件。</span>
                        <el-link :underline="false" style="font-size:12px;vertical-align: baseline;" type="primary"
                                 @click="importTemplate">下载模板
                        </el-link>
                    </div>
                </template>
            </el-upload>
            <template #footer>
                <div class="dialog-footer">
                    <el-button type="primary" @click="submitFileForm">确 定</el-button>
                    <el-button @click="upload.open = false">取 消</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script name="User" setup>
import {getToken} from "@/utils/auth";
import {
  addUser,
  changeUserStatus,
  delUser,
  deptTreeSelect,
  getDetailByUserId,
  listUser,
  resetUserPwd,
  updateUser
} from "@/api/system/user";
import {
  ApartmentOutlined,
  ArrowRightOutlined,
  CheckCircleOutlined,
  CloseOutlined,
  SolutionOutlined,
  UserAddOutlined,
  FolderOutlined,
  BankOutlined,
  ClusterOutlined,
  TeamOutlined,
  UserOutlined,
  PhoneOutlined,
  EditOutlined,
  DeleteOutlined,
  KeyOutlined
} from "@ant-design/icons-vue";

const router = useRouter();
const {proxy} = getCurrentInstance();
const {sys_normal_disable} = proxy.useDict("sys_normal_disable");

const showProcess = ref(true);

const sys_user_sex = [
    {
        label: '男',
        value: '0'
    },
    {
        label: '女',
        value: '1',
    },
    {
        label: '未知',
        value: '2'
    }
]
const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const deptName = ref("");
const deptOptions = ref(undefined);
const initPassword = ref(undefined);
const postOptions = ref([]);
const roleOptions = ref([]);
/*** 用户导入参数 */
const upload = reactive({
    // 是否显示弹出层（用户导入）
    open: false,
    // 弹出层标题（用户导入）
    title: "",
    // 是否禁用上传
    isUploading: false,
    // 是否更新已经存在的用户数据
    updateSupport: 0,
    // 设置上传的请求头部
    headers: {Authorization: "Bearer " + getToken()},
    // 上传的地址
    url: import.meta.env.VITE_APP_BASE_API + "/system/user/importData"
});
// 列显隐信息
const columns = ref([
    {key: 0, label: `用户编号`, visible: true},
    {key: 1, label: `用户名称`, visible: true},
    {key: 2, label: `用户昵称`, visible: true},
    {key: 3, label: `部门`, visible: true},
    {key: 4, label: `手机号码`, visible: true},
    {key: 5, label: `状态`, visible: true},
    {key: 6, label: `创建时间`, visible: true}
]);

const data = reactive({
    form: {},
    queryParams: {
        pageNo: 1,
        pageSize: 10,
        delFlagEq: 0,
        userNameLike: undefined,
        phoneNumberLike: undefined,
        statusEq: undefined,
        deptIdEq: undefined,
    },
    rules: {
        userName: [{required: true, message: "用户名称不能为空", trigger: "blur"}, {
            min: 2,
            max: 20,
            message: "用户名称长度必须介于 2 和 20 之间",
            trigger: "blur"
        }],
        nickName: [{required: true, message: "用户昵称不能为空", trigger: "blur"}],
        password: [{required: true, message: "用户密码不能为空", trigger: "blur"}, {
            min: 5,
            max: 20,
            message: "用户密码长度必须介于 5 和 20 之间",
            trigger: "blur"
        }, {pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur"}],
        email: [{type: "email", message: "请输入正确的邮箱地址", trigger: ["blur", "change"]}],
        phoneNumber: [{pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur"}]
    }
});

const {queryParams, form, rules} = toRefs(data);

const filterNode = (value, data) => {
    if (!value) return true;
    return data.label.indexOf(value) !== -1;
};

/** 根据名称筛选部门树 */
watch(deptName, val => {
    proxy.$refs["deptTreeRef"].filter(val);
});

/** 查询部门下拉树结构 */
function getDeptTree() {
    deptTreeSelect({delFlagEq: 0}).then(response => {
        deptOptions.value = response.data;
    });
}

/** 查询用户列表 */
function getList() {
    loading.value = true;
    listUser(queryParams.value).then(res => {
        loading.value = false;
        userList.value = res.data.records;
        total.value = res.data.total;
    });
}

/** 节点单击事件 */
function handleNodeClick(data) {
    queryParams.value.deptIdEq = data.id;
    queryParams.value.statusEq = 0;
    handleQuery();
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
    queryParams.value.deptIdEq = undefined;
    handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
    const userIds = row.userId || ids.value;
    proxy.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function () {
        return delUser(userIds);
    }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
    }).catch(() => {
    });
}

/** 导出按钮操作 */
function handleExport() {
    proxy.download("system/user/export", {
        ...queryParams.value,
    }, `user_${new Date().getTime()}.xlsx`);
}

/** 用户状态修改  */
function handleStatusChange(row) {
    let text = row.status === "0" ? "启用" : "停用";
    proxy.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗?').then(function () {
        return changeUserStatus(row.userId, row.status);
    }).then(() => {
        proxy.$modal.msgSuccess(text + "成功");
    }).catch(function () {
        row.status = row.status === "0" ? "1" : "0";
    });
}

/** 更多操作 */
function handleCommand(command, row) {
    switch (command) {
        case "handleResetPwd":
            handleResetPwd(row);
            break;
        case "handleAuthRole":
            handleAuthRole(row);
            break;
        default:
            break;
    }
}

/** 跳转角色分配 */
function handleAuthRole(row) {
    const userId = row.userId;
    router.push("/system/user-auth/role/" + userId);
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
    proxy.$prompt('请输入"' + row.userName + '"的新密码', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: "用户密码长度必须介于 5 和 20 之间",
        inputValidator: (value) => {
            if (/<|>|"|'|\||\\/.test(value)) {
                return "不能包含非法字符：< > \" ' \\\ |"
            }
        },
    }).then(({value}) => {
        resetUserPwd(row.userId, value).then(response => {
            proxy.$modal.msgSuccess("修改成功，新密码是：" + value);
        });
    }).catch(() => {
    });
}

/** 选择条数  */
function handleSelectionChange(selection) {
    ids.value = selection.map(item => item.userId);
    single.value = selection.length != 1;
    multiple.value = !selection.length;
}

/** 导入按钮操作 */
function handleImport() {
    upload.title = "用户导入";
    upload.open = true;
}

/** 下载模板操作 */
function importTemplate() {
    proxy.download("system/user/importTemplate", {}, `user_template_${new Date().getTime()}.xlsx`);
}

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
    upload.isUploading = true;
}

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
    upload.open = false;
    upload.isUploading = false;
    proxy.$refs["uploadRef"].handleRemove(file);
    proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", {dangerouslyUseHTMLString: true});
    getList();
}

/** 提交上传文件 */
function submitFileForm() {
    proxy.$refs["uploadRef"].submit();
}

/** 重置操作表单 */
function reset() {
    form.value = {
        userId: undefined,
        deptIdEq: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        phoneNumber: undefined,
        email: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        postIds: [],
        roleIds: []
    };
    proxy.resetForm("userRef");
}

/** 取消按钮 */
function cancel() {
    open.value = false;
    reset();
}

/** 新增按钮操作 */
function handleAdd() {
    reset();
    getDetailByUserId().then(response => {
        postOptions.value = response.data.posts;
        roleOptions.value = response.data.roles;
        open.value = true;
        title.value = "添加用户";
        form.value.password = initPassword.value;
    });
}

/** 修改按钮操作 */
function handleUpdate(row) {
    reset();
    const userId = row.userId || ids.value;
    getDetailByUserId(userId[0]|| userId).then(response => {
        form.value = response.data.data;
        postOptions.value = response.data.posts;
        roleOptions.value = response.data.roles;
        form.value.postIds = response.data.postIds;
        form.value.roleIds = response.data.roleIds;
        open.value = true;
        title.value = "修改用户";
        form.password = "";
    });
}

/** 提交按钮 */
function submitForm() {
    proxy.$refs["userRef"].validate(valid => {
        if (valid) {
            if (form.value.userId != undefined) {
                updateUser(form.value).then(response => {
                    proxy.$modal.msgSuccess("修改成功");
                    open.value = false;
                    getList();
                });
            } else {
                addUser(form.value).then(response => {
                    proxy.$modal.msgSuccess("新增成功");
                    open.value = false;
                    getList();
                });
            }
        }
    });
}

getDeptTree();
getList();
</script>

<style lang="scss" scoped>
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

:deep(.dept-tree) {
    .el-tree-node__content {
        height: 36px;
        border-radius: 6px;
        margin-bottom: 2px;

        &:hover {
            background-color: #f0f9ff;
        }
    }

    .el-tree-node.is-current > .el-tree-node__content {
        background-color: #e0f2fe;
        color: #0284c7;
        font-weight: 500;

        .text-gray-700 {
            color: #0284c7;
        }
    }
}
</style>