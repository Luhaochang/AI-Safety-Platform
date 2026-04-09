import request from "@/utils/request.js";

// 分页
export function getAutoMarkTaskByTaskId(params) {
    return request({
        method: 'get',
        url: 'production-line/auto-label/condition',
        params
    })
}

// 新增
export function addAutoMarkTask(data) {
    return request({
        method: 'post',
        url: 'production-line/auto-label',
        data: data
    })
}

// 修改
export function editAutoMarkTask(data) {
    return request({
        method: 'put',
        url: 'production-line/auto-label',
        data: data
    })
}

// 删除
export function deleteAutoMarkTask(params) {
    return request({
        method: 'delete',
        url: 'production-line/auto-label',
        params
    })
}

// 提交自动标注
export function autoMarkSubmit(params) {
    return request({
        method: 'post',
        url: 'production-line/auto-label/queue',
        params
    })
}
