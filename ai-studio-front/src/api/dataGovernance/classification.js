/**
 * 数据分级分类管理 API
 * 分级：公开级、重要级、核心级（3级）
 * 分类：设备数据类、运行数据类、调度指令类、历史案例类、专家知识类、规程文档类、用户信息类（7类）
 **/
import request from "@/utils/request.js";

// 获取分级列表
export function listGrades(params) {
    return request({ method: 'get', url: 'production-line/data-governance/grade/list', params })
}

// 获取分类列表
export function listCategories(params) {
    return request({ method: 'get', url: 'production-line/data-governance/category/list', params })
}

// 新增/修改分级
export function saveGrade(data) {
    return request({ method: 'post', url: 'production-line/data-governance/grade', data })
}

// 新增/修改分类
export function saveCategory(data) {
    return request({ method: 'post', url: 'production-line/data-governance/category', data })
}

// 删除分级
export function deleteGrade(id) {
    return request({ method: 'delete', url: `production-line/data-governance/grade/${id}` })
}

// 删除分类
export function deleteCategory(id) {
    return request({ method: 'delete', url: `production-line/data-governance/category/${id}` })
}

// 获取分级分类统计
export function getClassificationStats() {
    return request({ method: 'get', url: 'production-line/data-governance/stats' })
}

// 获取数据血缘关系
export function getDataLineage() {
    return request({ method: 'get', url: 'production-line/data-governance/lineage' })
}

// --- Mock APIs ---

// 分级列表 (Mock) — 3级
export function listGradesMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    {
                        id: 1, name: '公开级', sort: 1,
                        description: '可公开使用的一般性数据，泄露不会对电力系统运行造成影响',
                        accessPolicy: '所有平台用户可访问',
                        examples: '公开技术文档、设备型号参数、已脱敏的统计报表',
                        assetCount: 23
                    },
                    {
                        id: 2, name: '重要级', sort: 2,
                        description: '涉及电力系统运行细节的数据，泄露可能导致局部运行风险',
                        accessPolicy: '经审批的内部人员可访问，需记录访问日志',
                        examples: '设备运行参数、调度指令记录、模型训练数据集',
                        assetCount: 18
                    },
                    {
                        id: 3, name: '核心级', sort: 3,
                        description: '电力系统核心敏感数据，泄露可能危及电网安全或国家安全',
                        accessPolicy: '仅限授权管理员访问，需双人复核',
                        examples: '电网拓扑结构、调度决策模型、安全防护策略、用户隐私数据',
                        assetCount: 7
                    }
                ]
            });
        }, 200);
    });
}

// 分类列表 (Mock) — 7类
export function listCategoriesMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    {
                        id: 1, name: '设备数据类',
                        description: '电力设备的基础信息、运行参数、健康状态等结构化数据',
                        examples: '变压器参数、开关状态、传感器读数、设备台账',
                        assetCount: 8
                    },
                    {
                        id: 2, name: '运行数据类',
                        description: '电力系统实时运行产生的时序数据和状态数据',
                        examples: '电压/电流/功率时序、负荷曲线、故障录波、PMU数据',
                        assetCount: 11
                    },
                    {
                        id: 3, name: '调度指令类',
                        description: '电力调度操作中产生的指令、操作票和执行记录',
                        examples: '调度操作票、倒闸操作指令、负荷调整指令、事故处置指令',
                        assetCount: 5
                    },
                    {
                        id: 4, name: '历史案例类',
                        description: '历史故障案例、处置经验和事故分析报告',
                        examples: '故障分析报告、事故处置记录、典型缺陷案例、抢修记录',
                        assetCount: 6
                    },
                    {
                        id: 5, name: '专家知识类',
                        description: '领域专家的经验知识、决策规则和判断依据',
                        examples: '故障诊断规则、运维经验总结、调度策略知识、安全规程解读',
                        assetCount: 9
                    },
                    {
                        id: 6, name: '规程文档类',
                        description: '电力行业标准、操作规程、管理制度等规范性文档',
                        examples: '电力安全规程、调度管理规程、设备检修标准、应急预案',
                        assetCount: 7
                    },
                    {
                        id: 7, name: '用户信息类',
                        description: '涉及用户个人信息、用电行为等隐私相关数据',
                        examples: '用户用电数据、个人身份信息、缴费记录、投诉工单',
                        assetCount: 2
                    }
                ]
            });
        }, 200);
    });
}

// 获取分级分类统计 (Mock)
export function getClassificationStatsMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
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
                    totalAssets: 48,
                    classifiedAssets: 42,
                    unclassifiedAssets: 6
                }
            });
        }, 200);
    });
}

// 获取数据血缘关系 (Mock) — 基于电力AI场景
export function getDataLineageMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    nodes: [
                        { id: 'dev_1', name: '变压器监测数据', type: '设备数据类', layer: '原始数据', grade: '重要级' },
                        { id: 'op_1', name: '电网运行时序数据', type: '运行数据类', layer: '原始数据', grade: '重要级' },
                        { id: 'doc_1', name: '电力安全规程', type: '规程文档类', layer: '原始数据', grade: '公开级' },
                        { id: 'case_1', name: '历史故障案例库', type: '历史案例类', layer: '原始数据', grade: '公开级' },
                        { id: 'clean_1', name: '清洗后设备数据集', type: '设备数据类', layer: '清洗数据', grade: '重要级' },
                        { id: 'clean_2', name: '清洗后运行数据集', type: '运行数据类', layer: '清洗数据', grade: '重要级' },
                        { id: 'label_1', name: '故障标注数据集', type: '运行数据类', layer: '标注数据', grade: '核心级' },
                        { id: 'kb_1', name: '调度知识库(RAG)', type: '专家知识类', layer: '标注数据', grade: '重要级' },
                        { id: 'model_1', name: '故障诊断模型', type: '模型产物', layer: '模型产物', grade: '核心级' },
                        { id: 'model_2', name: '负荷预测模型', type: '模型产物', layer: '模型产物', grade: '核心级' },
                        { id: 'svc_1', name: '故障诊断推理服务', type: '推理服务', layer: '推理服务', grade: '核心级' },
                        { id: 'svc_2', name: '负荷预测推理服务', type: '推理服务', layer: '推理服务', grade: '核心级' },
                        { id: 'app_1', name: '电力调度决策应用', type: '业务应用', layer: '业务应用', grade: '核心级' }
                    ],
                    edges: [
                        { source: 'dev_1', target: 'clean_1', label: '数据清洗' },
                        { source: 'op_1', target: 'clean_2', label: '数据清洗' },
                        { source: 'clean_1', target: 'label_1', label: '故障标注' },
                        { source: 'clean_2', target: 'label_1', label: '特征关联' },
                        { source: 'doc_1', target: 'kb_1', label: '知识抽取' },
                        { source: 'case_1', target: 'kb_1', label: '案例入库' },
                        { source: 'label_1', target: 'model_1', label: '模型训练' },
                        { source: 'clean_2', target: 'model_2', label: '模型训练' },
                        { source: 'model_1', target: 'svc_1', label: '服务部署' },
                        { source: 'model_2', target: 'svc_2', label: '服务部署' },
                        { source: 'svc_1', target: 'app_1', label: '诊断服务' },
                        { source: 'svc_2', target: 'app_1', label: '预测服务' },
                        { source: 'kb_1', target: 'app_1', label: 'RAG检索' }
                    ]
                }
            });
        }, 400);
    });
}
