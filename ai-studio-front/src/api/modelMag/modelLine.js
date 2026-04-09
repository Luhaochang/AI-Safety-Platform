/**
 * 模型产线
 * **/
import request from "@/utils/request.js";

// 分页
export function listModelLine(params) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/condition',
        params
    })
}

// 新增
export function addModelLine(data) {
    return request({
        method: 'post',
        url: 'production-line/pipeline',
        data: data
    })
}

// 修改
export function updateModelLine(data) {
    return request({
        method: 'put',
        url: 'production-line/pipeline',
        data: data
    })
}

// 删除
export function deleteModelLineById(params) {
    return request({
        method: 'delete',
        url: 'production-line/pipeline',
        params
    })
}

// 根据id查模型产线
export function getModelLineById(id){
    return request({
        method: 'get',
        url: 'production-line/pipeline/by-id',
        params: { id: id}
    })
}

// 训练模型产线
export function trainModelLine(id,map) {
    return request({
        method: 'post',
        url: 'production-line/pipeline/queue',
        params: {
            id: id,
        },
        data: map
        // headers: {'Content-Type':'application/x-www-form-urlencoded'}
    })
}

// 获取产线统计数据
export function getChartDataInfo(id) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/metric',
        params: {
            id: id
        }
    })
}

//  获取训练详情 - 时间
export function getTrainDetail(id){
    return request({
        method: 'get',
        url: 'production-line/pipeline/task-message',
        params: {
            id: id
        }
    })
}

// 获取模型路径
export function getModelResult(params) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/artifact-path',
        params: params
    })
}

// 产线打包
export function packageProductionLine(id) {
    return request({
        method: 'post',
        url: 'production-line/pipeline/package',
        params: {
            id: id
        }
    })
}

// 产线停止运行
export function changeProductLineStatus(params) {
    return request({
        method: 'post',
        url: 'production-line/pipeline/quit-continue',
        params: params
    })
}
