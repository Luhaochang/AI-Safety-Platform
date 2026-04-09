/**
 * 数据资产管理 API
 **/
import request from "@/utils/request.js";

// 数据资产分页查询
export function listAssets(params) {
    return request({
        method: 'get',
        url: 'production-line/data-asset/condition',
        params
    })
}

// 新增数据资产
export function addAsset(data) {
    return request({
        method: 'post',
        url: 'production-line/data-asset',
        data
    })
}

// 修改数据资产
export function updateAsset(data) {
    return request({
        method: 'put',
        url: 'production-line/data-asset',
        data
    })
}

// 删除数据资产
export function deleteAsset(params) {
    return request({
        method: 'delete',
        url: 'production-line/data-asset',
        params
    })
}

// 根据ID查询数据资产
export function getAssetById(id) {
    return request({
        method: 'get',
        url: 'production-line/data-asset/by-id',
        params: { id }
    })
}

// 获取资产统计概览
export function getAssetOverview() {
    return request({
        method: 'get',
        url: 'production-line/data-asset/overview'
    })
}

// --- Mock APIs (后端未实现前使用) ---

// 获取资产列表 (Mock) — 使用分级(3)+分类(7)维度
export function listAssetsMock(params) {
    const grades = ['公开级', '重要级', '核心级'];
    const categories = ['设备数据类', '运行数据类', '调度指令类', '历史案例类', '专家知识类', '规程文档类', '用户信息类'];
    const layers = ['原始数据', '清洗数据', '标注数据', '特征数据', '模型产物', '知识文档'];
    const sources = ['本地上传', 'SCADA采集', '训练产出', '推理产出', '爬虫采集', '人工录入'];
    const assetNames = [
        '变压器监测数据集', '电网负荷时序数据', '调度操作票记录', '220kV线路故障案例',
        '故障诊断专家规则', '电力安全操作规程', '用户用电行为数据', '开关柜状态数据集',
        '继电保护动作数据', '负荷预测训练数据', '故障标注数据集-v2', 'PMU量测数据集',
        '调度策略知识文档', '设备检修标准文档', '配电网拓扑数据', '风电出力预测数据',
        '事故处置经验库', '电能质量监测数据', 'BERT-故障分类模型', 'YOLOv8-设备检测模型'
    ];
    const list = [];
    const total = 48;
    const start = ((params.pageNo || 1) - 1) * (params.pageSize || 10);
    for (let i = 0; i < (params.pageSize || 10) && (start + i) < total; i++) {
        const idx = start + i;
        list.push({
            id: idx + 1,
            assetName: assetNames[idx % assetNames.length] + (idx >= assetNames.length ? `_${Math.floor(idx / assetNames.length) + 1}` : ''),
            grade: grades[idx % grades.length],
            category: categories[idx % categories.length],
            dataLayer: layers[idx % layers.length],
            source: sources[idx % sources.length],
            owner: idx % 3 === 0 ? 'admin' : idx % 3 === 1 ? '调度员A' : '运维员B',
            size: Math.floor(Math.random() * 5000 + 100) + 'MB',
            createTime: '2025-0' + ((idx % 9) + 1) + '-' + String((idx % 28) + 1).padStart(2, '0') + ' 10:00:00',
            updateTime: '2025-0' + ((idx % 9) + 1) + '-' + String((idx % 28) + 1).padStart(2, '0') + ' 14:00:00',
            description: `${categories[idx % categories.length]}相关数据资产，用于电力AI平台的训练与推理`
        });
    }
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, data: { records: list, total } });
        }, 300);
    });
}

// 获取资产概览统计 (Mock)
export function getAssetOverviewMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    totalAssets: 48,
                    datasetCount: 18,
                    modelCount: 8,
                    knowledgeCount: 22,
                    gradeDistribution: [
                        { name: '公开级', value: 23 },
                        { name: '重要级', value: 18 },
                        { name: '核心级', value: 7 }
                    ],
                    categoryDistribution: [
                        { name: '设备数据', value: 8 },
                        { name: '运行数据', value: 11 },
                        { name: '调度指令', value: 5 },
                        { name: '历史案例', value: 6 },
                        { name: '专家知识', value: 9 },
                        { name: '规程文档', value: 7 },
                        { name: '用户信息', value: 2 }
                    ],
                    recentActivity: [
                        { action: '新增', assetName: '风电出力预测数据', time: '2025-03-12 10:30:00', user: 'admin' },
                        { action: '修改分级', assetName: 'YOLOv8-设备检测模型', time: '2025-03-12 09:15:00', user: '运维员B' },
                        { action: '数据标注', assetName: '故障标注数据集-v2', time: '2025-03-11 16:45:00', user: '调度员A' },
                        { action: '新增', assetName: '电能质量监测数据', time: '2025-03-11 14:20:00', user: 'admin' },
                        { action: '入库知识', assetName: '调度策略知识文档', time: '2025-03-11 11:00:00', user: '调度员A' }
                    ]
                }
            });
        }, 300);
    });
}
