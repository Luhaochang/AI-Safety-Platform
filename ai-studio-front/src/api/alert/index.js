import request from '@/utils/request';

// 获取告警列表
export function getAlerts(params) {
    return request({
        url: '/system/alert/list',
        method: 'get',
        params
    });
}

// Mock 告警数据
export function getAlertsMock() {
    const alerts = [
        { id: 1, level: 'critical', message: 'GPU-Worker-02 GPU利用率超过95%', source: 'node-06', status: 'active', time: '2024-03-15 14:23:00' },
        { id: 2, level: 'warning', message: 'CPU-Worker-01 内存使用率超过80%', source: 'node-03', status: 'active', time: '2024-03-15 13:45:00' },
        { id: 3, level: 'warning', message: 'Storage-01 磁盘使用率超过85%', source: 'node-05', status: 'active', time: '2024-03-15 12:10:00' },
        { id: 4, level: 'info', message: 'GPU-Master-01 定时维护已完成', source: 'node-01', status: 'resolved', time: '2024-03-15 08:00:00' },
        { id: 5, level: 'warning', message: 'GPU-Worker-02 CPU温度偏高', source: 'node-06', status: 'active', time: '2024-03-15 14:30:00' }
    ];
    return Promise.resolve({
        code: 200,
        data: alerts
    });
}
