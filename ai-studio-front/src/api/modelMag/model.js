/***
 模型空间
 */
import request from "@/utils/request.js";

// 分页
export function listModel(params) {
    return request({
        method: 'get',
        url: 'production-line/model/condition',
        params,
        hideError: true
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
        method: 'get',
        hideError: true
    })
}

// ── Mock APIs ──────────────────────────────────────────────────────────
export function listModelMock(params) {
    const all = [
        { id: 1, name: 'YOLOv8-设备缺陷检测', taskTypeId: 2, providerName: '电科院', modelScore: 4.8, modelViews: 1253, createTime: '2025-01-10 10:00:00', description: '基于YOLOv8的输电线路设备缺陷自动检测模型，支持绝缘子、线夹等15类缺陷识别。', modelSize: 22548480, frameId: 1, appSceneId: JSON.stringify([1]), status: 4 },
        { id: 2, name: 'BERT-故障诊断分类', taskTypeId: 9, providerName: '电科院', modelScore: 4.6, modelViews: 987, createTime: '2025-01-15 09:00:00', description: '基于BERT的电力设备故障诊断文本分类模型，覆盖32类常见故障类型，F1达91.2%。', modelSize: 438000000, frameId: 2, appSceneId: JSON.stringify([2]), status: 4 },
        { id: 3, name: 'ResNet50-绝缘子识别', taskTypeId: 3, providerName: '南网科研', modelScore: 4.5, modelViews: 756, createTime: '2025-01-20 14:00:00', description: '针对航拍图像中绝缘子状态识别，准确率96.8%，支持正常/自爆/污秽三类分类。', modelSize: 102400000, frameId: 1, appSceneId: JSON.stringify([1]), status: 4 },
        { id: 4, name: 'BiLSTM-电力负荷预测', taskTypeId: 10, providerName: '清华能源院', modelScore: 4.7, modelViews: 1102, createTime: '2025-02-01 11:00:00', description: '双向LSTM短期电力负荷预测模型，预测精度MAPE < 2.1%，支持96点日前预测。', modelSize: 8500000, frameId: 3, appSceneId: JSON.stringify([3]), status: 4 },
        { id: 5, name: 'GCN-配网拓扑分析', taskTypeId: 10, providerName: '南网科研', modelScore: 4.4, modelViews: 612, createTime: '2025-02-10 16:00:00', description: '图卷积网络用于配电网拓扑识别和故障区域定位，在复杂网络结构下定位准确率95.3%。', modelSize: 15600000, frameId: 3, appSceneId: JSON.stringify([4]), status: 4 },
        { id: 6, name: 'Transformer-异常检测', taskTypeId: 11, providerName: '电科院', modelScore: 4.3, modelViews: 534, createTime: '2025-02-18 13:00:00', description: '基于Transformer的时序异常检测，用于PMU量测数据中电网异常事件的实时识别。', modelSize: 67000000, frameId: 2, appSceneId: JSON.stringify([2]), status: 4 },
        { id: 7, name: 'YOLOv8s-安全帽检测', taskTypeId: 2, providerName: '本地训练', modelScore: 4.2, modelViews: 489, createTime: '2025-03-01 10:00:00', description: '变电站作业人员安全帽及安全绳佩戴检测，支持实时视频流处理，检测速度45FPS。', modelSize: 44800000, frameId: 1, appSceneId: JSON.stringify([5]), status: 4 },
        { id: 8, name: 'CNN-电弧故障识别', taskTypeId: 3, providerName: '本地训练', modelScore: 4.1, modelViews: 367, createTime: '2025-03-05 15:00:00', description: '基于卷积神经网络的配电线路电弧故障识别，误报率 < 0.5%，满足实时监测要求。', modelSize: 33000000, frameId: 1, appSceneId: JSON.stringify([2]), status: 4 },
    ];
    const pageSize = params?.pageSize || 12;
    const pageNo = params?.pageNo || 1;
    let filtered = all;
    if (params?.nameLike) filtered = filtered.filter(m => m.name.includes(params.nameLike));
    if (params?.taskTypeIdEq) filtered = filtered.filter(m => m.taskTypeId === params.taskTypeIdEq);
    const total = filtered.length;
    const records = filtered.slice((pageNo - 1) * pageSize, pageNo * pageSize);
    return Promise.resolve({ code: 200, data: { records, total } });
}

export function getModelOptionsMock() {
    return Promise.resolve({
        code: 200,
        data: {
            taskTypes: [
                { value: 2, label: '目标检测' }, { value: 3, label: '图像分类' },
                { value: 9, label: 'NLP分类' }, { value: 10, label: '时序预测' }, { value: 11, label: '异常检测' }
            ],
            appScenes: [
                { value: 1, label: '设备巡检' }, { value: 2, label: '故障诊断' },
                { value: 3, label: '负荷预测' }, { value: 4, label: '拓扑分析' }, { value: 5, label: '安全防护' }
            ],
            providers: [
                { value: 1, label: '电科院' }, { value: 2, label: '南网科研' },
                { value: 3, label: '清华能源院' }, { value: 4, label: '本地训练' }
            ],
            frameworks: [
                { value: 1, label: 'PyTorch' }, { value: 2, label: 'TensorFlow' }, { value: 3, label: 'PaddlePaddle' }
            ]
        }
    });
}
