/**
 * 服务部署
 * **/

import request from "@/utils/request.js";

// 分页
export function getServiceList (params) {
    return request({
        method: 'get',
        url: 'production-line/model-service/condition',
        params: params,
        hideError: true
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

// ── Mock APIs ──────────────────────────────────────────────────────────
export function getServiceListMock(params) {
    const all = [
        { id: 1, name: 'YOLOv8-设备缺陷检测-API', status: 1, url: 'http://ai.power.local:8501/v1/detect', modelName: 'YOLOv8-设备缺陷检测', createTime: '2025-01-14 09:00:00', updateTime: '2025-01-14 09:00:00', requestCount: 25681, avgLatency: 38, gpuUtil: 72, isShowUrl: false },
        { id: 2, name: 'BiLSTM-负荷预测-API', status: 1, url: 'http://ai.power.local:8502/v1/forecast', modelName: 'BiLSTM-电力负荷预测', createTime: '2025-02-11 11:00:00', updateTime: '2025-03-10 16:00:00', requestCount: 18432, avgLatency: 12, gpuUtil: 35, isShowUrl: false },
        { id: 3, name: 'BERT-故障诊断-API', status: 0, url: 'http://ai.power.local:8503/v1/diagnose', modelName: 'BERT-故障诊断分类', createTime: '2025-02-06 14:00:00', updateTime: '2025-02-06 14:00:00', requestCount: 8920, avgLatency: 65, gpuUtil: 0, isShowUrl: false },
        { id: 4, name: 'YOLOv8s-安全帽检测-API', status: 2, url: 'http://ai.power.local:8504/v1/safety', modelName: 'YOLOv8s-安全帽检测', createTime: '2025-03-02 10:00:00', updateTime: '2025-03-08 16:30:00', requestCount: 4230, avgLatency: 25, gpuUtil: 0, isShowUrl: false },
    ];
    const pageSize = params?.pageSize || 10;
    const pageNo = params?.pageNo || 1;
    let filtered = all;
    if (params?.nameLike) filtered = filtered.filter(s => s.name.includes(params.nameLike));
    if (params?.statusEq !== undefined && params?.statusEq !== null) filtered = filtered.filter(s => s.status === params.statusEq);
    const total = filtered.length;
    const records = filtered.slice((pageNo - 1) * pageSize, pageNo * pageSize);
    return Promise.resolve({ code: 200, data: { records, total } });
}
