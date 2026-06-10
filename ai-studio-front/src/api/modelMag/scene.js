import request from "@/utils/request.js";

// 根据应用场景查询
export function getChildScene(id) {
    return request({
        method: 'get',
        url: 'production-line/model/by-application',
        params: {
            applicationScene: id
        }
    })
}

// 根据任务场景查询 （5类）
export function getTaskSceneById(id) {
    return request({
        method: 'get',
        url: 'production-line/model/by-scene',
        params: {
            sceneId: id
        }
    })
}


export function getAllTaskType() {
    return request({
        method: 'get',
        url: 'production-line/task-type/all',
        hideError: true
    })
}

export function getAllAppScene() {
    return request({
        method: 'get',
        url: 'production-line/app-scene/all',
        hideError: true
    })
}

// ── Mock APIs ──────────────────────────────────────────────────────────
export function getAllTaskTypeMock() {
    return Promise.resolve({
        code: 200,
        data: [
            { id: 2, taskName: '目标检测', description: '在图像中定位并识别多个目标对象，适用于设备缺陷检测、安全防护场景', image: null },
            { id: 3, taskName: '图像分类', description: '对整张图像进行分类判断，适用于设备状态识别、故障类型分类', image: null },
            { id: 9, taskName: 'NLP文本分类', description: '基于自然语言处理的文本分类任务，适用于故障报告分析、知识提取', image: null },
            { id: 10, taskName: '时序预测', description: '基于历史时间序列数据进行未来趋势预测，适用于负荷预测、功率预测', image: null },
            { id: 11, taskName: '异常检测', description: '在时序或多维数据中识别异常事件，适用于电网稳定性监测', image: null },
        ]
    });
}

export function getAllAppSceneMock() {
    return Promise.resolve({
        code: 200,
        data: [
            { id: 1, appName: '输电线路巡检', industry: '输电', image: null, relatedTasksId: '2,3' },
            { id: 2, appName: '变电站设备检测', industry: '变电', image: null, relatedTasksId: '2,3' },
            { id: 3, appName: '配电网故障诊断', industry: '配电', image: null, relatedTasksId: '9,11' },
            { id: 4, appName: '电力负荷预测', industry: '调度', image: null, relatedTasksId: '10' },
            { id: 5, appName: '作业安全防护', industry: '安全', image: null, relatedTasksId: '2' },
            { id: 6, appName: '电能质量分析', industry: '配电', image: null, relatedTasksId: '10,11' },
        ]
    });
}
