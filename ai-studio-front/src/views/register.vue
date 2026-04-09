<template>
    <div class="register">
        <el-form ref="registerRef" :model="registerForm" :rules="registerRules" class="register-form" label-position="left" label-width="auto">
            <h3 class="title">系统注册</h3>
            <el-form-item prop="username" >
                <el-input
                    v-model="registerForm.username"
                    type="text"
                    size="large"
                    auto-complete="off"
                    placeholder="账号"
                >
                    <template #prefix>
                        <svg-icon icon-class="user" class="el-input__icon input-icon"/>
                    </template>
                </el-input>
            </el-form-item>

            <el-form-item prop="nickName">
                <el-input
                    v-model="registerForm.nickname"
                    type="text"
                    size="large"
                    auto-complete="off"
                    placeholder="昵称"
                >
                    <template #prefix>
                        <svg-icon icon-class="nickname" class="el-input__icon input-icon"/>
                    </template>
                </el-input>
            </el-form-item>

            <el-form-item prop="password" >
                <el-input
                    v-model="registerForm.password"
                    type="password"
                    size="large"
                    auto-complete="off"
                    placeholder="密码"
                    @keyup.enter="handleRegister"
                >
                    <template #prefix>
                        <svg-icon icon-class="password" class="el-input__icon input-icon"/>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword" >
                <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    size="large"
                    auto-complete="off"
                    placeholder="确认密码"
                    @keyup.enter="handleRegister"
                >
                    <template #prefix>
                        <svg-icon icon-class="password" class="el-input__icon input-icon"/>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="email">
                <el-input type="text" placeholder="请输入邮箱地址" v-model="registerForm.email" size="large"
                >
                    <template #prefix>
                        <svg-icon icon-class="email" class="el-input__icon input-icon"/>
                    </template>
                </el-input>
            </el-form-item>
            <el-form-item prop="mobile">
                <div class="view-field">
<!--                    <div class="input-name isRequired">手机号</div>-->
                    <div class="formInput-content">
                        <el-input size="large" v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11"
                                  @input="mobileInputChange" clearable>
                            <template #prefix>
                                <svg-icon icon-class="phone" class="el-input__icon input-icon"/>
                            </template>
                        </el-input>
                    </div>
                </div>
            </el-form-item>
            <el-form-item prop="captchaValue" class="captcha-item">
                <el-row style="width: 100%">
                    <el-col :span="10">
                        <el-input  size="large" v-model="registerForm.captchaValue" class="captchaInput" maxlength="4"
                                  placeholder="验证码" clearable>
                            <template #prefix>
                                <svg-icon icon-class="checkcode" class="el-input__icon input-icon"/>
                            </template>
                        </el-input>
                    </el-col>
                    <el-col :span="8" :offset="1">
                        <div
                            @click="refreshFlag">
                            <captcha :fresh="flag"
                                     @maked-code="getMakedCode"></captcha>
                        </div>
                    </el-col>
                    <el-col :span="4" :offset="1">
                        <el-link type="primary" @click="refreshFlag" :underline="false"
                                 icon="Refresh"
                                 class="replaceCaptchaText">换一幅
                        </el-link>
                    </el-col>
                </el-row>
            </el-form-item>

            <el-form-item prop="smsCode">
                <el-input v-model="registerForm.smsCode" size="large" maxlength="6" placeholder="短信验证码" clearable>
                    <template #prefix>
                        <span class="isRequired"></span>
                        <svg-icon icon-class="email" size="16px"/>
                    </template>
                    <template #append>
                        <div class="smsCode">
                            <div v-if="registerForm.counter <=0" class="button">
                                <el-button text @click="getSmsCode">获取验证码
                                </el-button>
                            </div>
                            <div v-if="registerForm.counter >0" class="text">{{ registerForm.countdownText }}</div>
                        </div>
                    </template>
                </el-input>
            </el-form-item>


            <el-form-item style="width:100%;">
                <el-button
                    :loading="loading"
                    size="large"
                    type="primary"
                    style="width:100%;"
                    @click.prevent="handleRegister"
                >
                    <span v-if="!loading">注 册</span>
                    <span v-else>注 册 中...</span>
                </el-button>
                <div style="float: right;">
                    <router-link class="link-type" :to="'/login'">使用已有账户登录</router-link>
                </div>
            </el-form-item>
        </el-form>
        <!--  底部  -->
        <div class="el-register-footer">
            <span>Copyright © 2018-2024 NAIC All Rights Reserved.</span>
        </div>
    </div>
</template>

<script setup>
import {ElMessageBox,ElMessage} from "element-plus";
import {getCodeImg, register, sendSms} from "@/api/login";
import Captcha from "@/components/captcha.vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

const router = useRouter();
const {proxy} = getCurrentInstance();

const flag = ref(true);
let code1 = ref('')

const registerForm = ref({
    nickname: '',
    username: "",
    password: "",
    confirmPassword: "",
    code: "",
    uuid: "",
    email: '',
    phone: '',
    smsCode: '',
    counter: 0,
    countdownText: '',//倒计时文本
    timer: '' ,//计时器对象,
    isRegisterCaptcha: false,

    captchaValue: '',
    successInfo: ''

});

const checkCaptchaValue = (rule, value, callback) => {
    if (!registerForm.value.isRegisterCaptcha) {
        callback();
        return;
    }


    if (value === '') {
        return callback(new Error('验证码不能为空'));
    } else {
        if (value.trim().length < 4) {
            callback(new Error('验证码长度为4个字符'))
        }else {
            callback()

        }
    }
}

const equalToPassword = (rule, value, callback) => {
    if (registerForm.value.password !== value) {
        callback(new Error("两次输入的密码不一致"));
    } else {
        callback();
    }
};

const registerRules = {
    username: [
        {required: true, trigger: "blur", message: "请输入您的账号"},
        {min: 2, max: 20, message: "用户账号长度必须介于 2 和 20 之间", trigger: "blur"}
    ],
    password: [
        {required: true, trigger: "blur", message: "请输入您的密码"},
        {min: 5, max: 20, message: "用户密码长度必须介于 5 和 20 之间", trigger: "blur"},
        {pattern: /^[^<>"'|\\]+$/, message: "不能包含非法字符：< > \" ' \\\ |", trigger: "blur"}
    ],
    confirmPassword: [
        {required: true, trigger: "blur", message: "请再次输入您的密码"},
        {required: true, validator: equalToPassword, trigger: "blur"}
    ],
    code: [{required: true, trigger: "change", message: "请输入验证码"}],
    captchaValue: [{validator: checkCaptchaValue, trigger: 'blur', required: true},
    ],
};

const codeUrl = ref("");
const loading = ref(false);
const captchaEnabled = ref(true);

function handleRegister() {
    proxy.$refs.registerRef.validate(valid => {
        if (valid) {
            loading.value = true;
            register({
                userName: registerForm.value.username,
                password: registerForm.value.password,
                code: registerForm.value.smsCode,
                tel: registerForm.value.phone,
                email: registerForm.value.email,
                nickName: registerForm.value.nickname
            }).then(res => {
                const username = registerForm.value.username;
                ElMessageBox.alert("<font color='red'>恭喜你，您的账号 " + username + " 注册成功！</font>", "系统提示", {
                    dangerouslyUseHTMLString: true,
                    type: "success",
                }).then(() => {
                    router.push("/login");
                }).catch(() => {
                });
            }).catch(() => {
                loading.value = false;
                if (captchaEnabled) {
                }
            });
        }
    });
}

function getCode() {
    getCodeImg().then(res => {
        captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (captchaEnabled.value) {
            codeUrl.value = "data:image/gif;base64," + res.img;
            registerForm.value.uuid = res.uuid;
        }
    });
}

const mobileInputChange = (val) => {
    registerForm.value.phone = registerForm.value.phone.replace(/[^0-9.]/g, '')
}

const refreshFlag = () => {
    flag.value = !flag.value;
}

function getMakedCode(code) {
    code1.value = code;
}

const getSmsCode = () => {

    if (registerForm.value.phone === null ||registerForm.value.phone === '') {
        ElMessage.error('请先填写手机号');
        return;
    }

    if (registerForm.value.captchaValue.toLowerCase() !== code1.value.toLocaleLowerCase()) {
        ElMessage.error('验证码输入不对，请重新输入');
        refreshFlag()
        return;
    }

    sendSms({
        tel: registerForm.value.phone
    })
        .then((response) => {
            const result = response.data;

            if (response.code === 200) {
                registerForm.value.successInfo = "短信验证码已发送";
                countdown();
            } else {
                //处理错误信息
                countdown();

            }

        })
        .catch((error) => {
            console.log(error);
        });

}

//倒计时
const countdown = () => {
    registerForm.value.counter = 60;//60秒
    registerForm.value.countdownText = registerForm.value.counter + "秒没收到后可重新获取";

    registerForm.value.timer = setInterval(() => {
        //替换文本
        registerForm.value.countdownText = registerForm.value.counter + "秒没收到后可重新获取";
        registerForm.value.counter--;
        if (registerForm.value.counter < 0) {
            // 当计时小于零时，取消该计时器
            clearInterval(registerForm.value.timer)
        }
    }, 1000)
}
</script>

<style lang='scss' scoped>
.register {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    background-image: url("../assets/images/login-background.jpg");
    background-size: cover;
}

.title {
    margin: 0px auto 30px auto;
    text-align: center;
    color: #707070;
}

.register-form {
    border-radius: 6px;
    background: #ffffff;
    width: 400px;
    padding: 25px 25px 5px 25px;

    .el-input {
        height: 40px;

        input {
            height: 40px;
        }
    }

    .input-icon {
        height: 39px;
        width: 14px;
        margin-left: 0px;
    }
}

.register-tip {
    font-size: 13px;
    text-align: center;
    color: #bfbfbf;
}

.register-code {
    width: 33%;
    height: 40px;
    float: right;

    img {
        cursor: pointer;
        vertical-align: middle;
    }
}

.el-register-footer {
    height: 40px;
    line-height: 40px;
    position: fixed;
    bottom: 0;
    width: 100%;
    text-align: center;
    color: #fff;
    font-family: Arial;
    font-size: 12px;
    letter-spacing: 1px;
}

.register-code-img {
    height: 40px;
    padding-left: 12px;
}
</style>
