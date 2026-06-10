import request from "@/utils/request.js";

// 数据集分页
export function listDataset(params) {
    return request({
        method: 'get',
        url: 'production-line/data-set/condition',
        params,
        hideError: true
    })
}

//  新增数据集
export function addDataset(params) {
    return request({
        method: 'post',
        url: 'production-line/data-set',
        data: params
    })
}

//  修改数据集
export function updateDataset(params) {
    return request({
        method: 'put',
        url: 'production-line/data-set',
        data: params
    })
}

// 删除数据集
export function deleteDatasetById(params) {
    return request({
        method: 'delete',
        url: 'production-line/data-set',
        params:params
    })
}

// 根据id查询数据集
export function getDatasetById(id){
    return request({
        method: 'get',
        url: 'production-line/data-set/by-id',
        params: {
            id: id
        }
    })
}

// 文件上传
export function uploadFile(data,onUploadProgress) {
    // let formData = new FormData();
    // formData.append('file',data);
    return request.post('production-line/resources/upload-file',data,{headers: {'Content-Type':'multipart/form-data'},onUploadProgress})
}

// 数据集上传
export function uploadDataSet(data,path) {
    return request({
        method: 'post',
        url: 'production-line/resources/upload-dataset',
        params: {
            catalogue: path
        },
        data: data,
        headers: {'Content-Type':'multipart/form-data'}
    })
}


// 获取数据集内容
export function getDatasetByPath(params) {
    return request({
        method: 'get',
        url: 'production-line/resources/dataset',
        params: params
    })
}

//  删除数据集中内容
export function deleteSImg(params,file) {
    return request({
        method: 'post',
        url: 'production-line/resources/delete-file',
        params:params,
        data: file,
        headers: {'Content-Type':'multipart/form-data'}
    })
}


// 校验数据集
export function checkDataset(params) {
    return request({
        method: "post",
        url: 'production-line/data-set/check',
        params
    })
}

// 打包数据集
export function packageDataset(params) {
    return request({
        method: 'post',
        url: 'production-line/resources/zip-dataset',
        params
    })
}

// 数据集下载
export function downloadDataset(params) {
    return request({
        method: 'post',
        url: 'production-line/resources/get-zip',
        params
    })
}

// ── Mock APIs ──────────────────────────────────────────────────────────
export function listDatasetMock(params) {
    const all = [
        { id: 1, dataSetName: '输电线路缺陷图像集', isPublic: 1, taskTypeId: 2, dataType: 1, isMarked: 2, fileNum: 8542, createTime: '2025-01-08 10:00:00', updateTime: '2025-01-20 16:00:00', description: '涵盖绝缘子、线夹、防震锤等15类设备缺陷图像，含标注文件，用于目标检测训练。', tag: JSON.stringify(['目标检测', '设备巡检', '已标注']), size: '2.3GB' },
        { id: 2, dataSetName: '变电站设备状态图像库', isPublic: 0, taskTypeId: 3, dataType: 1, isMarked: 2, fileNum: 3218, createTime: '2025-01-15 09:00:00', updateTime: '2025-02-01 14:00:00', description: '变电站一二次设备外观状态图像，按正常/异常/严重三类分级标注。', tag: JSON.stringify(['图像分类', '变电', '已标注']), size: '1.1GB' },
        { id: 3, dataSetName: '电力负荷时序数据集', isPublic: 1, taskTypeId: 10, dataType: 3, isMarked: 2, fileNum: 365, createTime: '2025-01-20 11:00:00', updateTime: '2025-03-01 10:00:00', description: '某省级电网2024年全年96点日负荷时序数据，含温度、节假日等特征字段。', tag: JSON.stringify(['时序预测', '负荷', '已标注']), size: '128MB' },
        { id: 4, dataSetName: '故障诊断报告语料库', isPublic: 1, taskTypeId: 9, dataType: 2, isMarked: 2, fileNum: 12600, createTime: '2025-02-01 14:00:00', updateTime: '2025-02-15 17:00:00', description: '电力设备运行故障诊断报告文本语料，涵盖32类故障，用于NLP分类与信息抽取训练。', tag: JSON.stringify(['NLP', '故障诊断', '已标注']), size: '856MB' },
        { id: 5, dataSetName: 'PMU量测异常数据集', isPublic: 0, taskTypeId: 11, dataType: 3, isMarked: 1, fileNum: 1820, createTime: '2025-02-10 10:00:00', updateTime: '2025-03-05 13:00:00', description: '同步相量测量单元(PMU)量测数据，包含正常、振荡、电压越限等多类异常事件。', tag: JSON.stringify(['异常检测', 'PMU', '标注中']), size: '412MB' },
        { id: 6, dataSetName: '安全帽佩戴检测数据集', isPublic: 1, taskTypeId: 2, dataType: 1, isMarked: 2, fileNum: 5340, createTime: '2025-02-18 09:00:00', updateTime: '2025-02-28 15:00:00', description: '变电站、施工现场人员安全帽及安全绳佩戴情况检测图像，含标注框文件。', tag: JSON.stringify(['目标检测', '安全防护', '已标注']), size: '1.8GB' },
        { id: 7, dataSetName: '配电网拓扑结构数据', isPublic: 0, taskTypeId: 10, dataType: 3, isMarked: 0, fileNum: 980, createTime: '2025-03-01 11:00:00', updateTime: '2025-03-01 11:00:00', description: '配电网拓扑结构与状态数据，包含节点、支路及量测信息，用于拓扑识别与状态估计研究。', tag: JSON.stringify(['配网', '未标注']), size: '245MB' },
        { id: 8, dataSetName: '风电出力预测样本集', isPublic: 1, taskTypeId: 10, dataType: 3, isMarked: 2, fileNum: 730, createTime: '2025-03-05 14:00:00', updateTime: '2025-03-10 16:00:00', description: '某风电场2年历史发电数据，含气象特征(风速、风向、温度等)，15分钟分辨率。', tag: JSON.stringify(['时序预测', '风电', '已标注']), size: '312MB' },
    ];
    const pageSize = params?.pageSize || 12;
    const pageNo = params?.pageNo || 1;
    let filtered = all;
    if (params?.dataSetNameLike) filtered = filtered.filter(d => d.dataSetName.includes(params.dataSetNameLike));
    if (params?.isPublicEq !== undefined && params?.isPublicEq !== null) filtered = filtered.filter(d => d.isPublic === params.isPublicEq);
    if (params?.taskTypeIdEq !== undefined && params?.taskTypeIdEq !== null && params?.taskTypeIdEq !== -1) filtered = filtered.filter(d => d.taskTypeId === params.taskTypeIdEq);
    if (params?.isMarkedEq !== undefined && params?.isMarkedEq !== null && params?.isMarkedEq !== -1) filtered = filtered.filter(d => d.isMarked === params.isMarkedEq);
    if (params?.dataTypeEq !== undefined && params?.dataTypeEq !== null && params?.dataTypeEq !== -1) filtered = filtered.filter(d => d.dataType === params.dataTypeEq);
    const total = filtered.length;
    const records = filtered.slice((pageNo - 1) * pageSize, pageNo * pageSize);
    return Promise.resolve({ code: 200, data: { records, total } });
}

// --- Mock APIs for Advanced Features ---

// 获取数据集统计信息 (进度, 类别分布)
export function getDatasetStats(id) {
    // Mock data
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    labeledCount: 142,
                    totalCount: 500,
                    classDistribution: [
                        { name: '鸟巢', value: 50 },
                        { name: '绝缘子', value: 200 },
                        { name: '安全帽', value: 120 },
                        { name: '其他', value: 130 }
                    ]
                }
            });
        }, 500);
    });
}

// 获取数据集版本列表
export function getDatasetVersions(id) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    { id: 1, version: 'v1.0', createTime: '2023-10-01 10:00:00', note: '初始版本', count: 100 },
                    { id: 2, version: 'v1.1', createTime: '2023-10-05 14:30:00', note: '增加鸟巢数据', count: 142 }
                ]
            });
        }, 300);
    });
}

// 创建新版本 (快照)
export function createDatasetVersion(data) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, msg: '版本创建成功' });
        }, 500);
    });
}

// 导出数据集
export function exportDatasetFormat(params) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, data: 'http://mock-download-url.zip', msg: '导出链接已生成' });
        }, 1000);
    });
}

// 获取标注数据列表 (用于标注工作台)
export function getAnnotationList(params) {
    // params: { datasetId, pageNo, pageSize, status (all, labeled, unlabeled) }
    return new Promise((resolve) => {
        setTimeout(() => {
            // Mocking different data types based on dataset type (handled in frontend logic usually, but here we return generic structure)
            const list = [];
            for (let i = 0; i < params.pageSize; i++) {
                list.push({
                    id: params.pageNo * params.pageSize + i,
                    url: `https://picsum.photos/id/${i + 10}/800/600`, // Random image
                    text: "这是一段测试文本，用于NLP标注任务。", // For NLP
                    row: { id: i, col1: 'Data ' + i, col2: 123, col3: 'Normal' }, // For Table
                    status: i % 3 === 0 ? 1 : 0, // 0: unlabeled, 1: labeled
                    annotations: i % 3 === 0 ? (
                        // Mock annotations based on type (simplified)
                        [{ label: '鸟巢', bbox: [100, 100, 200, 200] }]
                    ) : []
                });
            }
            resolve({
                code: 200,
                data: {
                    records: list,
                    total: 500
                }
            });
        }, 500);
    });
}

// 保存标注
export function saveAnnotation(data) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, msg: '标注保存成功' });
        }, 200);
    });
}

// 导入预标注 (YOLO txt)
export function importPreAnnotations(data) {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, msg: '预标注导入成功' });
        }, 1000);
    });
}
