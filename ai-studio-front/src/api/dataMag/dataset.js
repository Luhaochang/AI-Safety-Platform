import request from "@/utils/request.js";

// 数据集分页
export function listDataset(params) {
    return request({
        method: 'get',
        url: 'production-line/data-set/condition',
        params
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
