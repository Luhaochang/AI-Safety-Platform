/**
 * 训练推理可视化增强 API
 **/
import request from "@/utils/request.js";

// 获取训练实时指标
export function getTrainingMetrics(taskId) {
    return request({
        method: 'get',
        url: 'production-line/pipeline/training-metrics',
        params: { id: taskId }
    })
}

// 获取推理服务性能指标
export function getInferenceMetrics(serviceId) {
    return request({
        method: 'get',
        url: 'production-line/model-service/inference-metrics',
        params: { id: serviceId }
    })
}

// --- Mock APIs ---

// 训练指标 (Mock)
export function getTrainingMetricsMock(taskId) {
    const epochs = 50;
    const currentEpoch = Math.floor(Math.random() * 30) + 15;
    const lossData = [];
    const accData = [];
    const lrData = [];
    const valLossData = [];
    const valAccData = [];
    for (let i = 1; i <= currentEpoch; i++) {
        lossData.push(+(2.5 * Math.exp(-0.06 * i) + Math.random() * 0.1).toFixed(4));
        accData.push(+(1 - 1.2 * Math.exp(-0.08 * i) + Math.random() * 0.02).toFixed(4));
        lrData.push(+(0.001 * Math.pow(0.95, i)).toFixed(6));
        valLossData.push(+(2.8 * Math.exp(-0.05 * i) + Math.random() * 0.15).toFixed(4));
        valAccData.push(+(1 - 1.3 * Math.exp(-0.07 * i) + Math.random() * 0.03).toFixed(4));
    }
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    taskId,
                    totalEpochs: epochs,
                    currentEpoch,
                    status: 'running',
                    startTime: '2025-03-12 08:30:00',
                    elapsedTime: '2h 15m',
                    estimatedRemaining: '1h 30m',
                    metrics: {
                        loss: lossData,
                        accuracy: accData,
                        learningRate: lrData,
                        valLoss: valLossData,
                        valAccuracy: valAccData
                    },
                    resourceUsage: {
                        gpuUtil: Array.from({ length: currentEpoch }, () => Math.round(75 + Math.random() * 20)),
                        gpuMem: Array.from({ length: currentEpoch }, () => Math.round(60 + Math.random() * 25)),
                        gpuTemp: Array.from({ length: currentEpoch }, () => Math.round(65 + Math.random() * 15))
                    },
                    bestMetrics: {
                        bestEpoch: currentEpoch - 3,
                        bestLoss: Math.min(...lossData),
                        bestAcc: Math.max(...accData)
                    }
                }
            });
        }, 500);
    });
}

// 推理性能 (Mock)
export function getInferenceMetricsMock(serviceId) {
    const timePoints = [];
    const now = new Date();
    for (let i = 59; i >= 0; i--) {
        const t = new Date(now.getTime() - i * 60000);
        timePoints.push(t.getHours().toString().padStart(2, '0') + ':' + t.getMinutes().toString().padStart(2, '0'));
    }
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    serviceId,
                    serviceName: '目标检测推理服务',
                    status: 'running',
                    uptime: '72h 15m',
                    timePoints,
                    summary: {
                        totalRequests: 125680,
                        avgQps: 35.2,
                        avgLatency: 42,
                        p50Latency: 35,
                        p95Latency: 68,
                        p99Latency: 120,
                        errorRate: 0.12,
                        successRate: 99.88
                    },
                    timeSeries: {
                        qps: timePoints.map(() => Math.round(25 + Math.random() * 20)),
                        latency: timePoints.map(() => Math.round(30 + Math.random() * 30)),
                        errorCount: timePoints.map(() => Math.floor(Math.random() * 3)),
                        gpuUtil: timePoints.map(() => Math.round(50 + Math.random() * 30))
                    }
                }
            });
        }, 500);
    });
}

// 实验列表 (Mock)
export function listExperimentsMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    { id: 'exp_1', name: 'YOLOv8-baseline', model: 'YOLOv8n', dataset: '电力缺陷数据集', epochs: 50, bestAcc: 0.923, bestLoss: 0.312, lr: 0.001, batchSize: 32, duration: '3h 20m', status: 'completed', createTime: '2025-03-10' },
                    { id: 'exp_2', name: 'YOLOv8-augmented', model: 'YOLOv8n', dataset: '电力缺陷数据集(增强)', epochs: 50, bestAcc: 0.941, bestLoss: 0.278, lr: 0.001, batchSize: 32, duration: '4h 05m', status: 'completed', createTime: '2025-03-11' },
                    { id: 'exp_3', name: 'YOLOv8s-large', model: 'YOLOv8s', dataset: '电力缺陷数据集(增强)', epochs: 80, bestAcc: 0.956, bestLoss: 0.215, lr: 0.0005, batchSize: 16, duration: '7h 45m', status: 'running', createTime: '2025-03-12' },
                    { id: 'exp_4', name: 'ResNet50-baseline', model: 'ResNet50', dataset: '图片分类数据集', epochs: 30, bestAcc: 0.887, bestLoss: 0.456, lr: 0.01, batchSize: 64, duration: '1h 50m', status: 'completed', createTime: '2025-03-09' }
                ]
            });
        }, 300);
    });
}
