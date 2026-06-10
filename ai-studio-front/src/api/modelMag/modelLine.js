/**
 * 模型产线
 * **/
import request from "@/utils/request.js";

// 分页
export function listModelLine(params) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/condition',
        params,
        hideError: true
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

// ── Mock APIs ──────────────────────────────────────────────────────────
export function listModelLineMock(params) {
    const all = [
        { id: 1, lineName: '设备缺陷检测-YOLOv8训练线', status: 4, taskTypeId: 2, appSceneId: JSON.stringify([1]), modelId: 1, createTime: '2025-01-12 10:00:00', updateTime: '2025-01-13 18:30:00', duration: 12600000, bestAcc: 0.923 },
        { id: 2, lineName: '故障诊断BERT微调线', status: 2, taskTypeId: 9, appSceneId: JSON.stringify([2]), modelId: 2, createTime: '2025-02-05 09:00:00', updateTime: '2025-02-05 14:00:00', duration: 18200000, bestAcc: 0.891 },
        { id: 3, lineName: '负荷预测BiLSTM训练线', status: 4, taskTypeId: 10, appSceneId: JSON.stringify([3]), modelId: 4, createTime: '2025-02-10 11:00:00', updateTime: '2025-02-10 16:00:00', duration: 9800000, bestAcc: 0.956 },
        { id: 4, lineName: '安全帽检测-数据增强线', status: 3, taskTypeId: 2, appSceneId: JSON.stringify([5]), modelId: 7, createTime: '2025-03-01 14:00:00', updateTime: '2025-03-01 16:00:00', duration: 7200000, bestAcc: 0.872 },
        { id: 5, lineName: '电弧故障CNN训练线', status: 5, taskTypeId: 3, appSceneId: JSON.stringify([2]), modelId: 8, createTime: '2025-03-05 08:00:00', updateTime: '2025-03-05 10:00:00', duration: null, bestAcc: null },
    ];
    const pageSize = params?.pageSize || 10;
    const pageNo = params?.pageNo || 1;
    let filtered = all;
    if (params?.lineNameLike) filtered = filtered.filter(l => l.lineName.includes(params.lineNameLike));
    if (params?.taskTypeIdEq) filtered = filtered.filter(l => l.taskTypeId === params.taskTypeIdEq);
    const total = filtered.length;
    const records = filtered.slice((pageNo - 1) * pageSize, pageNo * pageSize);
    return Promise.resolve({ code: 200, data: { records, total } });
}
