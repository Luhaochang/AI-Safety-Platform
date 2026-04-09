/**
 * 数据采集与清洗 API
 **/
import request from "@/utils/request.js";

// 获取采集任务列表
export function listCrawlTasks(params) {
    return request({
        method: 'get',
        url: 'production-line/data-collect/task/list',
        params
    })
}

// 创建采集任务
export function createCrawlTask(data) {
    return request({
        method: 'post',
        url: 'production-line/data-collect/task',
        data
    })
}

// 获取清洗规则列表
export function listCleanRules(params) {
    return request({
        method: 'get',
        url: 'production-line/data-collect/clean-rule/list',
        params
    })
}

// --- Mock APIs ---

// 采集任务列表 (Mock)
export function listCrawlTasksMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    { id: 1, name: '国家能源局政策采集', url: 'https://www.nea.gov.cn/policy/', type: 'web', status: 'completed', pages: 23, fileCount: 23, totalSize: '4.5MB', schedule: 'manual', createTime: '2025-03-10 14:30', duration: '3分12秒', targetKb: 'GPU资源调度知识库' },
                    { id: 2, name: '电力行业标准采集', url: 'https://standards.cec.org.cn/', type: 'web', status: 'running', pages: 8, fileCount: 8, totalSize: '1.8MB', schedule: 'daily', createTime: '2025-03-12 09:15', duration: '进行中...', targetKb: '安全防御策略库' },
                    { id: 3, name: '内部Wiki知识采集', url: 'https://wiki.internal/power-dispatch/', type: 'web', status: 'pending', pages: 0, fileCount: 0, totalSize: '-', schedule: 'weekly', createTime: '2025-03-13 08:00', duration: '-', targetKb: '模型训练运维手册' },
                    { id: 4, name: 'SCADA设备数据导入', url: 'ftp://scada.internal/data/', type: 'ftp', status: 'completed', pages: 0, fileCount: 156, totalSize: '230MB', schedule: 'daily', createTime: '2025-03-11 02:00', duration: '12分45秒', targetKb: '' },
                    { id: 5, name: '故障案例文档批量导入', url: '本地上传', type: 'upload', status: 'completed', pages: 0, fileCount: 42, totalSize: '89MB', schedule: 'manual', createTime: '2025-03-09 16:20', duration: '2分08秒', targetKb: '推理服务部署指南' }
                ]
            });
        }, 300);
    });
}

// 清洗规则列表 (Mock)
export function listCleanRulesMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    { id: 1, name: 'HTML标签清除', description: '去除HTML标签和HTML实体，保留纯文本内容', type: 'text', enabled: true, order: 1 },
                    { id: 2, name: '特殊字符过滤', description: '过滤零宽字符、不可见字符、BOM等', type: 'text', enabled: true, order: 2 },
                    { id: 3, name: '重复行去重', description: '检测并去除完全重复的文本行', type: 'dedup', enabled: true, order: 3 },
                    { id: 4, name: '手机号脱敏', description: '自动检测并脱敏11位手机号码', type: 'privacy', enabled: true, order: 4 },
                    { id: 5, name: '空白字符压缩', description: '压缩多余空格、制表符和连续空行', type: 'normalize', enabled: true, order: 5 },
                    { id: 6, name: '身份证号脱敏', description: '检测并脱敏18位身份证号码', type: 'privacy', enabled: true, order: 6 },
                    { id: 7, name: '邮箱地址脱敏', description: '检测并脱敏电子邮箱地址', type: 'privacy', enabled: true, order: 7 }
                ]
            });
        }, 200);
    });
}

// 调用后端清洗文本接口
export function cleanText(data) {
    return request({
        method: 'post',
        url: 'production-line/data-clean/clean-text',
        data
    })
}

// 获取后端清洗规则列表
export function getCleanRules() {
    return request({
        method: 'get',
        url: 'production-line/data-clean/rules'
    })
}

// 批量清洗文件
export function batchClean(data) {
    return request({
        method: 'post',
        url: 'production-line/data-clean/batch-clean',
        data
    })
}

// 清洗统计 (Mock)
export function getCleanStatsMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    totalFiles: 229,
                    cleanedFiles: 198,
                    pendingFiles: 31,
                    duplicateRemoved: 17,
                    sensitiveDetected: 8,
                    totalSizeBefore: '325.3MB',
                    totalSizeAfter: '289.1MB',
                    compressionRate: '11.1%'
                }
            });
        }, 200);
    });
}
