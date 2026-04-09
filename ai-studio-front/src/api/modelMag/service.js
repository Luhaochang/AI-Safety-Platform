/**
 * 服务部署
 * **/

import request from "@/utils/request.js";

// 分页
export function getServiceList (params) {
    return request({
        method: 'get',
        url: 'production-line/model-service/condition',
        params: params
    })
}

//  创建服务
export function addService(params) {
    return request({
        method: 'post',
        url: 'production-line/model-service',
        data: params
    })
}

// 删除服务
export function deleteService(params){
    return request({
        method: 'delete',
        url: 'production-line/model-service',
        params
    })
}

// 通过id查询服务
export function getServiceById(params) {
    return request({
        method: 'get',
        url: 'production-line/model-service/by-id',
        params
    })
}

export function getServiceLogs(params) {
    return request({
        method: 'get',
        url: 'production-line/model-service/by-es',
        params
    })
}

export function stopService(params) {
    return request({
        method: 'post',
        url: 'production-line/model-service/quit-continue',
        params:params
    })
}
