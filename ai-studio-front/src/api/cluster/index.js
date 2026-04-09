import request from '@/utils/request';

// 获取集群节点列表
export function getClusterNodes(params) {
    return request({
        url: '/system/cluster/nodes',
        method: 'get',
        params
    });
}

// Mock 集群节点数据
export function getClusterNodesMock(params) {
    const nodes = [
        { nodeId: 'node-01', nodeName: 'GPU-Master-01', nodeType: 'GPU', status: 'running', ip: '192.168.1.101', cpu: '32核', memory: '128GB', gpu: 'A100 x4', cpuUsage: '45.2', memUsage: '62.8', gpuUsage: '78.5', diskUsage: '55.0', uptime: '32天' },
        { nodeId: 'node-02', nodeName: 'GPU-Worker-01', nodeType: 'GPU', status: 'running', ip: '192.168.1.102', cpu: '32核', memory: '128GB', gpu: 'A100 x4', cpuUsage: '38.7', memUsage: '55.3', gpuUsage: '82.1', diskUsage: '48.6', uptime: '32天' },
        { nodeId: 'node-03', nodeName: 'CPU-Worker-01', nodeType: 'CPU', status: 'running', ip: '192.168.1.103', cpu: '64核', memory: '256GB', gpu: '-', cpuUsage: '72.1', memUsage: '81.4', gpuUsage: '0', diskUsage: '67.2', uptime: '15天' },
        { nodeId: 'node-04', nodeName: 'CPU-Worker-02', nodeType: 'CPU', status: 'running', ip: '192.168.1.104', cpu: '64核', memory: '256GB', gpu: '-', cpuUsage: '55.8', memUsage: '44.2', gpuUsage: '0', diskUsage: '39.8', uptime: '15天' },
        { nodeId: 'node-05', nodeName: 'Storage-01', nodeType: 'Storage', status: 'running', ip: '192.168.1.105', cpu: '16核', memory: '64GB', gpu: '-', cpuUsage: '22.3', memUsage: '35.1', gpuUsage: '0', diskUsage: '88.7', uptime: '60天' },
        { nodeId: 'node-06', nodeName: 'GPU-Worker-02', nodeType: 'GPU', status: 'warning', ip: '192.168.1.106', cpu: '32核', memory: '128GB', gpu: 'V100 x2', cpuUsage: '91.5', memUsage: '89.2', gpuUsage: '95.3', diskUsage: '72.4', uptime: '8天' }
    ];
    return Promise.resolve({
        code: 200,
        data: { records: nodes, total: nodes.length }
    });
}
