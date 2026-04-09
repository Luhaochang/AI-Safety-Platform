/***
 模型空间
 */
import request from "@/utils/request.js";

// 分页
export function listModel(params) {
    return request({
        method: 'get',
        url: 'production-line/model/condition',
        params
    })
}

// 添加官方模型
export function addModel(data) {
    return request({
        method: 'post',
        url: 'production-line/model',
        data: data
    })
}

// 添加自定义模型
export function addCustomModel(id,data) {
    return request({
        method: 'post',
        url: 'production-line/model/self-define',
        params: {taskId: id},
        data: data
    })
}

// 修改模型
export function updateModel(data) {
    return request({
        method: "put",
        url: 'production-line/model',
        data: data
    })
}

// 删除模型
export function deleteModel(params){
    return request({
        method: "delete",
        url: 'production-line/model',
        params
    })
}

// 根据id查模型
export function getModelById(params) {
    return request({
        method: 'get',
        url: 'production-line/model/by-id',
        params
    })
}

// 根据tagId list查询model
export function getModelByTagId(tagIdList) {
    return request({
        method: 'get',
        url: 'production-line/model/by-tag',
        params: {
            tagIdList: tagIdList
        }
    })
}

// 获取模型字典选项
export function getModelOptions() {
    return request({
        url: 'production-line/model/options',
        method: 'get'
    })
}
