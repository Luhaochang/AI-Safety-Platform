/**
 * 数字孪生 API
 **/
import request from "@/utils/request.js";

// 获取集群节点实时状态
export function getClusterRealtime() {
    return request({
        method: 'get',
        url: 'production-line/digital-twin/cluster/realtime'
    })
}

// 获取场景配置
export function getSceneConfig() {
    return request({
        method: 'get',
        url: 'production-line/digital-twin/scene/config'
    })
}

// 获取告警列表
export function getAlerts(params) {
    return request({
        method: 'get',
        url: 'production-line/digital-twin/alerts',
        params
    })
}

// 获取训练任务流转状态
export function getTaskFlow() {
    return request({
        method: 'get',
        url: 'production-line/digital-twin/task-flow'
    })
}

// --- Mock APIs ---

// 集群节点实时状态 (Mock)
export function getClusterRealtimeMock() {
    const nodes = [
        { id: 'node-01', name: 'Master-01', ip: '10.33.38.21', role: 'master', status: 'running',
          cpu: { cores: 64, usage: 45 + Math.random() * 10 }, gpu: { count: 4, model: 'A100-80G', usage: 72 + Math.random() * 15, memUsage: 65 + Math.random() * 15, temperature: 68 + Math.random() * 10 },
          memory: { total: 256, used: 180 + Math.random() * 30 }, disk: { total: 4000, used: 2800 },
          tasks: [{ id: 't1', name: 'YOLOv8-训练', status: 'running', progress: 67 }],
          position: { x: -2, y: 0, z: 0 }
        },
        { id: 'node-02', name: 'Worker-01', ip: '10.33.38.22', role: 'worker', status: 'running',
          cpu: { cores: 64, usage: 30 + Math.random() * 10 }, gpu: { count: 4, model: 'A100-80G', usage: 55 + Math.random() * 15, memUsage: 48 + Math.random() * 15, temperature: 62 + Math.random() * 10 },
          memory: { total: 256, used: 120 + Math.random() * 40 }, disk: { total: 4000, used: 1500 },
          tasks: [{ id: 't2', name: 'BERT-微调', status: 'running', progress: 34 }],
          position: { x: 0, y: 0, z: 0 }
        },
        { id: 'node-03', name: 'Worker-02', ip: '10.33.38.23', role: 'worker', status: 'running',
          cpu: { cores: 32, usage: 15 + Math.random() * 10 }, gpu: { count: 2, model: 'V100-32G', usage: 20 + Math.random() * 15, memUsage: 18 + Math.random() * 10, temperature: 45 + Math.random() * 8 },
          memory: { total: 128, used: 45 + Math.random() * 20 }, disk: { total: 2000, used: 800 },
          tasks: [],
          position: { x: 2, y: 0, z: 0 }
        },
        { id: 'node-04', name: 'Worker-03', ip: '10.33.38.24', role: 'worker', status: 'warning',
          cpu: { cores: 64, usage: 88 + Math.random() * 8 }, gpu: { count: 4, model: 'A100-80G', usage: 92 + Math.random() * 5, memUsage: 90 + Math.random() * 8, temperature: 82 + Math.random() * 5 },
          memory: { total: 256, used: 230 + Math.random() * 15 }, disk: { total: 4000, used: 3600 },
          tasks: [
              { id: 't3', name: 'ResNet50-训练', status: 'running', progress: 89 },
              { id: 't4', name: '推理服务-检测', status: 'running', progress: 100 }
          ],
          position: { x: -2, y: 0, z: 2 }
        },
        { id: 'node-05', name: 'Inference-01', ip: '10.33.38.25', role: 'inference', status: 'running',
          cpu: { cores: 32, usage: 25 + Math.random() * 10 }, gpu: { count: 2, model: 'T4-16G', usage: 60 + Math.random() * 15, memUsage: 55 + Math.random() * 10, temperature: 58 + Math.random() * 8 },
          memory: { total: 64, used: 38 + Math.random() * 10 }, disk: { total: 1000, used: 400 },
          tasks: [{ id: 't5', name: '推理服务-NLP', status: 'running', progress: 100 }],
          position: { x: 0, y: 0, z: 2 }
        },
        { id: 'node-06', name: 'Inference-02', ip: '10.33.38.26', role: 'inference', status: 'offline',
          cpu: { cores: 32, usage: 0 }, gpu: { count: 2, model: 'T4-16G', usage: 0, memUsage: 0, temperature: 30 },
          memory: { total: 64, used: 0 }, disk: { total: 1000, used: 350 },
          tasks: [],
          position: { x: 2, y: 0, z: 2 }
        }
    ];
    nodes.forEach(n => {
        n.cpu.usage = Math.min(100, Math.max(0, Math.round(n.cpu.usage)));
        if (n.gpu.usage) {
            n.gpu.usage = Math.min(100, Math.max(0, Math.round(n.gpu.usage)));
            n.gpu.memUsage = Math.min(100, Math.max(0, Math.round(n.gpu.memUsage)));
            n.gpu.temperature = Math.round(n.gpu.temperature);
        }
        n.memory.used = Math.round(n.memory.used);
    });
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({ code: 200, data: { nodes, timestamp: Date.now() } });
        }, 200);
    });
}

// 告警列表 (Mock)
export function getAlertsMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: [
                    { id: 1, nodeId: 'node-04', nodeName: 'Worker-03', level: 'warning', type: 'GPU温度过高', message: 'GPU #2 温度达到87°C，接近阈值90°C', time: '2025-03-12 10:25:00', status: 'active' },
                    { id: 2, nodeId: 'node-04', nodeName: 'Worker-03', level: 'warning', type: '磁盘空间不足', message: '磁盘使用率达90%，剩余400GB', time: '2025-03-12 09:50:00', status: 'active' },
                    { id: 3, nodeId: 'node-06', nodeName: 'Inference-02', level: 'critical', type: '节点离线', message: '节点已离线超过30分钟', time: '2025-03-12 09:30:00', status: 'active' },
                    { id: 4, nodeId: 'node-01', nodeName: 'Master-01', level: 'info', type: '任务完成', message: 'YOLOv8训练任务预计20分钟内完成', time: '2025-03-12 10:20:00', status: 'resolved' }
                ]
            });
        }, 200);
    });
}

// 训练任务流转 (Mock)
export function getTaskFlowMock() {
    return new Promise((resolve) => {
        setTimeout(() => {
            resolve({
                code: 200,
                data: {
                    flows: [
                        { id: 'f1', from: 'node-01', to: 'node-04', taskName: '数据分发-ResNet50', type: 'data', bandwidth: '2.3GB/s' },
                        { id: 'f2', from: 'node-02', to: 'node-05', taskName: '模型推送-BERT', type: 'model', bandwidth: '800MB/s' },
                        { id: 'f3', from: 'node-04', to: 'node-01', taskName: '梯度同步', type: 'gradient', bandwidth: '1.5GB/s' }
                    ]
                }
            });
        }, 200);
    });
}
