import request from "@/utils/request.js";

// 总体数量统计
export function getOverviewCount() {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/summary'
    })
}

// 数据集分类 - 饼图
export function getDatasetClassification() {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/data-set'
    })
}

// 数据集标注进度 - 条形图
export function getDatasetMarkProgress(params) {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/data-set-task',
        params: params
    })
}

// 训练任务时间分布 - 柱状图
export function getTrainTaskTimeDistribution() {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/finished-pipeline-task'
    })
}

// 模型分类统计 - 饼图
export function getModelClassification() {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/model'
    })
}

// 模型训练进度 - 条形图
export function getTrainProcess() {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/pipeline-task'
    })
}


// 集群信息 - 分页
export function getColonyInfo(params) {
    return request({
        method: 'get',
        url: 'production-line/home-page-statistics/node',
        params
    })
}

// 获取所有任务类型
export function getAllTaskTypes() {
    return request({
        method: 'get',
        url: 'production-line/task-type/all'
    })
}
