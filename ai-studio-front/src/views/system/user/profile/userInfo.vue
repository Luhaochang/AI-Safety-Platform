<template>
    <el-form ref="userRef" :model="state.form" :rules="rules" label-width="80px" label-position="top">
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="用户昵称" prop="nickName">
                    <el-input v-model="state.form.nickName" maxlength="30" placeholder="请输入用户昵称">
                        <template #prefix>
                            <el-icon><User /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="手机号码" prop="phoneNumber">
                    <el-input v-model="state.form.phoneNumber" maxlength="11" placeholder="请输入手机号码">
                        <template #prefix>
                            <el-icon><Phone /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
            </el-col>
        </el-row>
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                    <el-input v-model="state.form.email" maxlength="50" placeholder="请输入邮箱">
                        <template #prefix>
                            <el-icon><Message /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="性别">
                    <el-radio-group v-model="state.form.sex">
                        <el-radio value="0">男</el-radio>
                        <el-radio value="1">女</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-col>
        </el-row>
        <el-form-item style="margin-top: 20px;">
            <el-button type="primary" @click="submit">保存更改</el-button>
            <el-button @click="close">关闭</el-button>
        </el-form-item>
    </el-form>
</template>

<script setup>
import {updateUser, updateUserProfile} from "@/api/system/user";
import useUserStore from "@/store/modules/user.js";
import { User, Phone, Message } from "@element-plus/icons-vue";

const props = defineProps({
    user: {
        type: Object
    }
});

const emits = defineEmits(['refresh'])
const {proxy} = getCurrentInstance();

const state = reactive({
    form: {
        userId: null,
        sex: null,
        nickName: null,
        email: null,
        phoneNumber: null
    }
})

const rules = ref({
    nickName: [{required: true, message: "用户昵称不能为空", trigger: "blur"}],
    email: [{required: true, message: "邮箱地址不能为空", trigger: "blur"}, {
        type: "email",
        message: "请输入正确的邮箱地址",
        trigger: ["blur", "change"]
    }],
    phoneNumber: [{
        required: true,
        message: "手机号码不能为空",
        trigger: "blur"
    }, {pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/, message: "请输入正确的手机号码", trigger: "blur"}],
});

/** 提交按钮 */
function submit() {
    proxy.$refs.userRef.validate(valid => {
        if (valid) {
            updateUserProfile(state.form).then(response => {
                proxy.$modal.msgSuccess("修改成功");
                props.user.phoneNumber = state.form.phoneNumber;
                props.user.email = state.form.email;

                emits('refresh')
            });
        }
    });
};

/** 关闭按钮 */
function close() {
    proxy.$tab.closePage();
};

// 回显当前登录用户信息
watch(() => props.user, user => {
    if (user) {
        state.form = {nickName: user.nickName, phoneNumber: user.phoneNumber, email: user.email, sex: user.sex,userId: user.userId};
    }
}, {immediate: true});
</script>
