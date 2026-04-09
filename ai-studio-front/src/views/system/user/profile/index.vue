<template>
    <div class="app-container">
        <el-card class="profile-wrapper" :body-style="{ padding: '0px', display: 'flex', minHeight: '650px' }">
            <!-- 左侧侧边栏 -->
            <div class="profile-sidebar">
                <div class="sidebar-header">
                    <userAvatar/>
                    <div class="user-name">{{ state.user.userName }}</div>
                    <div class="user-role">{{ state.roleGroup || '普通用户' }}</div>
                </div>

                <!-- 导航菜单 -->
                <div class="profile-menu">
                    <div
                        class="menu-item"
                        :class="{ active: activeTab === 'userinfo' }"
                        @click="activeTab = 'userinfo'"
                    >
                        <el-icon><User /></el-icon>
                        <span>基本资料</span>
                        <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                    </div>
                    <div
                        class="menu-item"
                        :class="{ active: activeTab === 'resetPwd' }"
                        @click="activeTab = 'resetPwd'"
                    >
                        <el-icon><Lock /></el-icon>
                        <span>修改密码</span>
                        <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                    </div>
                </div>

                <div class="sidebar-divider"></div>

                <!-- 详细信息列表 -->
                <div class="info-list">
                    <div class="info-item">
                        <el-icon><Phone /></el-icon>
                        <span>{{ state.user.phoneNumber || '暂无手机号' }}</span>
                    </div>
                    <div class="info-item">
                        <el-icon><Message /></el-icon>
                        <span :title="state.user.email">{{ state.user.email || '暂无邮箱' }}</span>
                    </div>
                    <div class="info-item">
                        <el-icon><OfficeBuilding /></el-icon>
                        <span>{{ state.user.dept ? state.user.dept.deptName : '暂无部门' }} / {{ state.postGroup || '无岗位' }}</span>
                    </div>
                    <div class="info-item">
                        <el-icon><Calendar /></el-icon>
                        <span>{{ state.user.createTime }}</span>
                    </div>
                </div>
            </div>

            <!-- 右侧内容区 -->
            <div class="profile-content">
                <div class="content-header">
                    <div class="header-title">
                        {{ activeTab === 'userinfo' ? '基本资料' : '修改密码' }}
                    </div>
                    <div class="header-desc">
                        {{ activeTab === 'userinfo' ? '查看或更新您的个人详细信息' : '定期修改密码以保护账号安全' }}
                    </div>
                </div>
                <el-divider />
                <div class="content-body">
                    <transition name="el-fade-in-linear" mode="out-in">
                        <div v-if="activeTab === 'userinfo'" key="userinfo" class="tab-content">
                            <userInfo :user="state.user" @refresh="getUser"/>
                        </div>
                        <div v-else-if="activeTab === 'resetPwd'" key="resetPwd" class="tab-content">
                            <resetPwd />
                        </div>
                    </transition>
                </div>
            </div>
        </el-card>
    </div>
</template>

<script setup name="Profile">
import userAvatar from "./userAvatar";
import userInfo from "./userInfo";
import resetPwd from "./resetPwd";
import {getUserProfile} from "@/api/system/user";
import { User, Phone, Message, OfficeBuilding, Calendar, Lock, ArrowRight } from "@element-plus/icons-vue";

const activeTab = ref("userinfo");
const state = reactive({
    user: {},
    roleGroup: {},
    postGroup: {}
});

function getUser() {
    getUserProfile().then(response => {
        state.user = response.data.user;
        state.roleGroup = response.roleGroup;
        state.postGroup = response.postGroup;
    });
};

getUser();
</script>

<style scoped lang="scss">
.profile-wrapper {
    border: none;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    overflow: hidden;
    border-radius: 8px;
}

.profile-sidebar {
    width: 320px;
    background-color: #f8f9fa;
    border-right: 1px solid #ebeef5;
    display: flex;
    flex-direction: column;
    padding: 40px 20px;
    flex-shrink: 0;

    .sidebar-header {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-bottom: 30px;

        .user-name {
            font-size: 20px;
            font-weight: 600;
            color: #303133;
            margin-top: 15px;
            margin-bottom: 5px;
        }

        .user-role {
            font-size: 13px;
            color: #909399;
            background-color: #e9e9eb;
            padding: 2px 10px;
            border-radius: 10px;
        }
    }

    .profile-menu {
        display: flex;
        flex-direction: column;
        gap: 10px;

        .menu-item {
            display: flex;
            align-items: center;
            padding: 12px 20px;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s;
            color: #606266;
            font-size: 15px;

            .el-icon {
                margin-right: 12px;
                font-size: 18px;
            }

            .arrow-icon {
                margin-left: auto;
                margin-right: 0;
                font-size: 14px;
                opacity: 0;
                transition: opacity 0.3s;
            }

            &:hover {
                background-color: #fff;
                color: var(--el-color-primary);
                box-shadow: 0 2px 8px rgba(0,0,0,0.04);

                .arrow-icon {
                    opacity: 1;
                }
            }

            &.active {
                background-color: var(--el-color-primary);
                color: #fff;
                box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.3);

                .arrow-icon {
                    opacity: 1;
                }
            }
        }
    }

    .sidebar-divider {
        height: 1px;
        background-color: #e4e7ed;
        margin: 30px 10px;
    }

    .info-list {
        display: flex;
        flex-direction: column;
        gap: 15px;
        padding: 0 10px;

        .info-item {
            display: flex;
            align-items: center;
            color: #606266;
            font-size: 14px;

            .el-icon {
                margin-right: 12px;
                color: #909399;
                font-size: 16px;
            }

            span {
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }
        }
    }
}

.profile-content {
    flex: 1;
    padding: 40px 50px;
    background-color: #fff;
    display: flex;
    flex-direction: column;

    .content-header {
        margin-bottom: 10px;

        .header-title {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 8px;
        }

        .header-desc {
            font-size: 14px;
            color: #909399;
        }
    }

    .content-body {
        flex: 1;
        padding-top: 20px;

        .tab-content {
            max-width: 600px;
        }
    }
}

@media (max-width: 768px) {
    .profile-wrapper {
        flex-direction: column !important;
    }
    .profile-sidebar {
        width: 100%;
        border-right: none;
        border-bottom: 1px solid #ebeef5;
    }
    .profile-content {
        padding: 20px;
    }
}
</style>
