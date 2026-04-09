import request from '@/utils/request'

// 登录方法
export function login(username, password, code, uuid) {
  const data = {
    username,
    password,
    code,
    uuid
  }
  return request({
    url: 'auth/login',
    // headers: {
    //   isToken: false,
    //   repeatSubmit: false
    // },
    method: 'post',
    data: data
  })
}

// 注册方法
export function register(data) {
  return request({
    url: 'auth/system/user/register',
    // headers: {
    //   isToken: false
    // },
    method: 'post',
    data: data
  })
}

// 用户 - 发短信验证码
export function sendSms(info){
  return request({
    url: 'auth/sms/send',
    method: 'post',
    params: info
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: 'auth/system/user',
    method: 'get'
  })
/*  const res = {
    "msg": "操作成功",
    "code": 200,
    "permissions": [
      "*:*:*"
    ],
    "roles": [
      "admin"
    ],
    "user": {
      "createBy": "admin",
      "createTime": "2024-06-30 11:27:11",
      "updateBy": null,
      "updateTime": null,
      "remark": "管理员",
      "userId": 1,
      "deptId": 103,
      "userName": "admin",
      "nickName": "若依",
      "email": "ry@163.com",
      "phonenumber": "15888888888",
      "sex": "1",
      "avatar": "",
      "password": "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2",
      "status": "0",
      "delFlag": "0",
      "loginIp": "113.233.214.68",
      "loginDate": "2024-08-30T10:56:22.000+08:00",
      "dept": {
        "createBy": null,
        "createTime": null,
        "updateBy": null,
        "updateTime": null,
        "remark": null,
        "deptId": 103,
        "parentId": 101,
        "ancestors": "0,100,101",
        "deptName": "研发部门",
        "orderNum": 1,
        "leader": "若依",
        "phone": null,
        "email": null,
        "status": "0",
        "delFlag": null,
        "parentName": null,
        "children": []
      },
      "roles": [
        {
          "createBy": null,
          "createTime": null,
          "updateBy": null,
          "updateTime": null,
          "remark": null,
          "roleId": 1,
          "roleName": "超级管理员",
          "roleKey": "admin",
          "roleSort": 1,
          "dataScope": "1",
          "menuCheckStrictly": false,
          "deptCheckStrictly": false,
          "status": "0",
          "delFlag": null,
          "flag": false,
          "menuIds": null,
          "deptIds": null,
          "permissions": null,
          "admin": true
        }
      ],
      "roleIds": null,
      "postIds": null,
      "roleId": null,
      "admin": true
    }
  }

  return new Promise((resolve,reject) => {
    resolve(res)
  })*/

}

// 获取用户权限信息
export function getPermission() {
  return request({
    method: 'get',
    url: 'auth/system/user/getInfo'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
