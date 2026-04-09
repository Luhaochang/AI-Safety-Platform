import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi";

// 查询用户列表
export function listUser(query) {
  return request({
    url: 'auth/system/user/list',
    method: 'get',
    params: query
  })
}

// 查询用户详细
export function getUser(userId) {
  return request({
    url: 'auth/system/user',
    method: 'get'
  })
}

export function getDetailByUserId(userId) {
  return request({
    method: 'get',
    url: 'auth/system/user/by-id/',
    params: {
      userId: userId
    }
  })
}

// 新增用户
export function addUser(data) {
  return request({
    url: 'auth/system/user',
    method: 'post',
    data: data
  })
}

// 修改用户
export function updateUser(data) {
  return request({
    url: 'auth/system/user',
    method: 'put',
    data: data
  })
}

// 删除用户
export function delUser(userId) {
  return request({
    url: 'auth/system/user/' + userId,
    method: 'delete'
  })
}

// 用户密码重置
export function resetUserPwd(userId, oldPassword,newPassword) {
  const data = {
    id:userId,
    oldPassword: oldPassword,
    newPassword: newPassword
  }
  return request({
    url: 'auth/system/user/resetPwd',
    method: 'put',
    data: data
  })
}

// 用户状态修改
export function changeUserStatus(userId, status) {
  const data = {
    userId,
    status
  }
  return request({
    url: 'auth/system/user/changeStatus',
    method: 'put',
    data: data
  })
}

// 查询用户个人信息
export function getUserProfile() {
  return request({
    url: 'auth/system/user',
    method: 'get'
  })
}

// 修改用户个人信息
export function updateUserProfile(data) {
  return request({
    url: 'auth/system/user/info',
    method: 'put',
    data: data
  })
}

// 用户密码重置
export function updateUserPwd(oldPassword, newPassword) {
  const data = {
    oldPassword,
    newPassword
  }
  return request({
    url: 'auth/system/user/profile/updatePwd',
    method: 'put',
    params: data
  })
}

// 用户头像上传
export function uploadAvatar(data) {
  return request({
    url: 'auth/resources/upload-file',
    method: 'post',
    // headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    headers: {'Content-Type':'multipart/form-data'},
    data: data
  })
}

// 查询授权角色
export function getAuthRole(userId) {
  return request({
    url: 'auth/system/user/authRole/' + userId,
    method: 'get'
  })
}

// 保存授权角色
export function updateAuthRole(data) {
  return request({
    url: 'auth/system/user/authRole',
    method: 'put',
    params: data
  })
}

// 查询部门下拉树结构
export function deptTreeSelect(params) {
  return request({
    url: 'auth/system/user/deptTree',
    method: 'get',
    params:params
  })
}
