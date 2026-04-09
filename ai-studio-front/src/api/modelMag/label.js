import request from "@/utils/request.js";

// 分页
export function getListByCondition (params) {
    return request({
        method: 'get',
        url:'production-line/model-tag/condtion',
        params
    })
}

// 按类别查
export function getByRootCategory(params) {
    return request({
        url: 'production-line/model-tag/by-category',
        method: 'get',
        params
    })
}

// 查子类
export function getSonCategory(params) {
    return request({
        method: "get",
        url: 'production-line/model-tag/by-super',
        params
    })
}

// 新增标签
export function addLabel(params) {
    return request({
        method: 'post',
        url: 'production-line/model-tag',
        data: params
    })
}

//编辑标签
export function editLabel(params) {
    return request({
        method: "put",
        url: 'production-line/model-tag',
        data: params
    })
}

//删除标签
export function deleteLabel(params) {
    return request({
        method: "delete",
        url: 'production-line/model-tag',
        params: params
    })
}

// 获取服务器日志
export function getLogs(params=undefined) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/by-es',
        params
    })
}
