<template>
    <div class="login-container">
        <div class="login-content">
            <div class="login-left">
                <div class="login-info">
                    <div class="logo-title">
                        <img src="@/assets/logo/logo.png" alt="logo" class="logo-img" v-if="false">
                        <span>AI开发平台</span>
                    </div>
                    <div class="login-desc">
                        一站式人工智能开发平台，提供从数据处理、模型开发、模型训练到服务部署的全流程解决方案，助力企业快速落地AI应用。
                    </div>

                    <div class="process-flow">
                        <div class="flow-step">
                            <div class="icon-wrapper">
                                <el-icon><DataAnalysis /></el-icon>
                            </div>
                            <span class="step-label">数据处理</span>
                        </div>
                        <div class="flow-arrow">
                            <el-icon><ArrowRight /></el-icon>
                        </div>
                        <div class="flow-step">
                            <div class="icon-wrapper">
                                <el-icon><Edit /></el-icon>
                            </div>
                            <span class="step-label">模型开发</span>
                        </div>
                        <div class="flow-arrow">
                            <el-icon><ArrowRight /></el-icon>
                        </div>
                        <div class="flow-step">
                            <div class="icon-wrapper">
                                <el-icon><Cpu /></el-icon>
                            </div>
                            <span class="step-label">模型训练</span>
                        </div>
                        <div class="flow-arrow">
                            <el-icon><ArrowRight /></el-icon>
                        </div>
                        <div class="flow-step">
                            <div class="icon-wrapper">
                                <el-icon><Connection /></el-icon>
                            </div>
                            <span class="step-label">服务部署</span>
                        </div>
                    </div>
                </div>
                <div class="login-bg-decoration"></div>
            </div>

            <div class="login-right">
                <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
                    <h3 class="title">欢迎登录</h3>
                    <p class="sub-title">请输入您的账号和密码</p>

                    <el-form-item prop="username">
                        <el-input
                            v-model="loginForm.username"
                            type="text"
                            size="large"
                            auto-complete="off"
                            placeholder="账号"
                            @keyup.enter="handleLogin"
                        >
                            <template #prefix>
                                <el-icon class="input-icon"><User /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>
                    <el-form-item prop="password">
                        <el-input
                            v-model="loginForm.password"
                            type="password"
                            size="large"
                            auto-complete="off"
                            placeholder="密码"
                            show-password
                            @keyup.enter="handleLogin"
                        >
                            <template #prefix>
                                <el-icon class="input-icon"><Lock /></el-icon>
                            </template>
                        </el-input>
                    </el-form-item>

                    <div class="login-options">
                        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
                        <router-link class="link-type" :to="'/register'" v-if="register">立即注册</router-link>
                    </div>

                    <el-form-item style="width:100%;">
                        <el-button
                            :loading="loading"
                            size="large"
                            type="primary"
                            class="login-btn"
                            @click.prevent="handleLogin"
                        >
                            <span v-if="!loading">登 录</span>
                            <span v-else>登 录 中...</span>
                        </el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>

        <!--  底部  -->
        <div class="el-login-footer">
            <span>Copyright © 2018-2026 NAIC All Rights Reserved.</span>
        </div>
    </div>
</template>

<script setup>
import {getCodeImg} from "@/api/login";
import Cookies from "js-cookie";
import {encrypt, decrypt} from "@/utils/jsencrypt";
import useUserStore from '@/store/modules/user'
import { User, Lock, DataAnalysis, Cpu, Connection, Edit, ArrowRight } from "@element-plus/icons-vue";

const userStore = useUserStore()
const route = useRoute();
const router = useRouter();
const {proxy} = getCurrentInstance();

const loginForm = ref({
    username: "qc",
    password: "123456",
    rememberMe: false,
    code: "",
    uuid: "",

    key: ''
});

const loginRules = {
    username: [{required: true, trigger: "blur", message: "请输入您的账号"}],
    password: [{required: true, trigger: "blur", message: "请输入您的密码"}],
    code: [{required: true, trigger: "change", message: "请输入验证码"}]
};

const codeUrl = ref("");
const loading = ref(false);
// 验证码开关
const captchaEnabled = ref(true);
// 注册开关
const register = ref(false);
const redirect = ref(undefined);

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect;
}, {immediate: true});

function handleLogin() {
    proxy.$refs.loginRef.validate(valid => {
        if (valid) {
            loading.value = true;
            // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
            if (loginForm.value.rememberMe) {
                Cookies.set("username", loginForm.value.username, {expires: 30});
                Cookies.set("password", encrypt(loginForm.value.password), {expires: 30});
                Cookies.set("rememberMe", loginForm.value.rememberMe, {expires: 30});
            } else {
                // 否则移除
                Cookies.remove("username");
                Cookies.remove("password");
                Cookies.remove("rememberMe");
            }
            // 调用action的登录方法
            userStore.login(loginForm.value).then(() => {
                const query = route.query;
                const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
                    if (cur !== "redirect") {
                        acc[cur] = query[cur];
                    }
                    return acc;
                }, {});
                router.push({path: redirect.value || "/", query: otherQueryParams});
            }).catch(() => {
                loading.value = false;
                // 重新获取验证码
                if (captchaEnabled.value) {
                    console.log('err')
                }
            });
        }
    });
}

function getCookie() {
    const username = Cookies.get("username");
    const password = Cookies.get("password");
    const rememberMe = Cookies.get("rememberMe");
    loginForm.value = {
        username: username === undefined ? loginForm.value.username : username,
        password: password === undefined ? loginForm.value.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
    };
}

const replaceCaptcha = () => {
    loginForm.imgUrl = "" + "captcha/" + loginForm.captchaKey + ".jpg?" + Math.random();

}

getCookie();
</script>

<style lang='scss' scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-image: url("../assets/images/login-background.jpg");
    background-size: cover;
    background-position: center;
    position: relative;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.1);
        z-index: 0;
    }
}

.login-content {
    display: flex;
    width: 1000px;
    height: 600px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    box-shadow: 0 20px 50px rgba(0, 0, 0, 0.3);
    overflow: hidden;
    z-index: 1;
    backdrop-filter: blur(15px);
    border: 1px solid rgba(255, 255, 255, 0.2);

    @media (max-width: 1024px) {
        width: 90%;
        height: auto;
        flex-direction: column;
    }
}

.login-left {
    flex: 1.2;
    /* 调整为明亮蓝渐变 */
    background: linear-gradient(135deg, rgba(24, 144, 255, 0.85) 0%, rgba(56, 189, 248, 0.85) 100%);
    padding: 60px 40px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    position: relative;
    color: #ffffff;

    .login-info {
        z-index: 2;
    }

    .logo-title {
        display: flex;
        align-items: center;
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 30px;
        text-shadow: 0 2px 4px rgba(0,0,0,0.1);

        .logo-img {
            width: 40px;
            height: 40px;
            margin-right: 15px;
        }
    }

    .login-desc {
        font-size: 16px;
        line-height: 1.6;
        margin-bottom: 60px;
        opacity: 0.95;
        color: #f0f8ff;
    }

    .process-flow {
        display: flex;
        align-items: center;
        justify-content: space-between;

        .flow-step {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 12px;

            .icon-wrapper {
                width: 56px;
                height: 56px;
                background: rgba(255, 255, 255, 0.2);
                border-radius: 16px;
                display: flex;
                justify-content: center;
                align-items: center;
                backdrop-filter: blur(5px);
                border: 1px solid rgba(255, 255, 255, 0.3);
                transition: all 0.3s;

                .el-icon {
                    font-size: 28px;
                    color: #fff;
                }
            }

            .step-label {
                font-size: 13px;
                font-weight: 500;
                color: #fff;
            }

            &:hover .icon-wrapper {
                background: #fff;
                transform: translateY(-5px);
                box-shadow: 0 5px 15px rgba(0,0,0,0.2);

                .el-icon {
                    color: #1890ff; /* 明亮蓝 */
                }
            }
        }

        .flow-arrow {
            display: flex;
            align-items: center;
            justify-content: center;
            padding-bottom: 25px; /* Align with icon center */
            opacity: 0.6;
            color: #fff;

            .el-icon {
                font-size: 20px;
            }
        }
    }

    .login-bg-decoration {
        position: absolute;
        top: -50px;
        right: -50px;
        width: 300px;
        height: 300px;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, rgba(0,0,0,0) 70%);
        border-radius: 50%;
        z-index: 1;
    }

    &::after {
        content: '';
        position: absolute;
        bottom: -50px;
        left: -50px;
        width: 200px;
        height: 200px;
        background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, rgba(0,0,0,0) 70%);
        border-radius: 50%;
        z-index: 1;
    }
}

.login-right {
    flex: 0.8;
    padding: 60px 50px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.85);

    .title {
        font-size: 28px;
        color: #1890ff; /* 明亮蓝 */
        margin-bottom: 10px;
        font-weight: 600;
        text-align: left;
    }

    .sub-title {
        font-size: 14px;
        color: #606266;
        margin-bottom: 40px;
    }

    .login-form {
        .el-input {
            --el-input-height: 48px;

            :deep(.el-input__wrapper) {
                background-color: rgba(245, 247, 250, 0.8);
                box-shadow: none;
                border: 1px solid transparent;
                transition: all 0.3s;

                &:hover, &.is-focus {
                    background-color: #ffffff;
                    border-color: #38bdf8;
                    box-shadow: 0 0 0 1px #38bdf8 inset;
                }
            }

            .input-icon {
                font-size: 18px;
                color: #909399;
            }
        }

        .el-form-item {
            margin-bottom: 25px;
        }
    }

    .login-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 30px;

        .link-type {
            color: #1890ff; /* 明亮蓝 */
            font-size: 14px;
            text-decoration: none;

            &:hover {
                text-decoration: underline;
            }
        }
    }

    .login-btn {
        width: 100%;
        height: 48px;
        font-size: 16px;
        font-weight: 600;
        border-radius: 8px;
        /* 明亮蓝渐变 */
        background: linear-gradient(90deg, #1890ff 0%, #38bdf8 100%);
        border: none;
        transition: all 0.3s;

        &:hover {
            opacity: 0.9;
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(56, 189, 248, 0.4);
        }

        &:active {
            transform: translateY(0);
        }
    }
}

.el-login-footer {
    height: 40px;
    line-height: 40px;
    position: fixed;
    bottom: 20px;
    width: 100%;
    text-align: center;
    color: #fff;
    font-family: Arial;
    font-size: 12px;
    letter-spacing: 1px;
    text-shadow: 0 1px 2px rgba(0,0,0,0.5);
    z-index: 1;
}
</style>
