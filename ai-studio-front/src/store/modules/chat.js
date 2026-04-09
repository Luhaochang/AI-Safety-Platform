import { defineStore } from 'pinia';
import { getChatHistoryReal, deleteConversationReal } from '@/api/ragScheduler/index.js';

// ========== 历史对话Mock数据（首次加载若后端为空则填充） ==========
const mockConversations = [
    {
        id: 'mock_conv_001',
        title: '220kV宝华线单相接地故障处置',
        user: '值班调度员',
        duration: '8.3s',
        createTime: '2025-03-15 09:12:00',
        updateTime: '2025-03-15 09:12:48',
        messages: [
            { role: 'user', content: '220kV宝华线发生单相接地故障，保护动作跳闸，如何处置？', time: '09:12:00' },
            { role: 'assistant', content: `## 220kV宝华线单相接地故障处置方案\n\n### 一、立即确认故障信息\n- 确认保护动作记录：接地距离保护I段或II段动作\n- 核实故障相别（A/B/C相）及重合闸动作情况\n- 查看故障录波数据，判断故障点位置\n\n### 二、故障隔离步骤\n1. **检查重合闸**：若重合成功，恢复正常运行并通知运维加强巡视\n2. **若重合失败**：立即拉开宝华线两侧断路器，隔离故障线路\n3. 通知宝华变、华安变运维人员赴现场检查\n\n### 三、负荷转供\n- 评估宝华线停运对系统的影响，检查相邻220kV华安线负荷率\n- 若华安线负荷率超70%，启动限负荷预案，协调110kV负荷转移\n- 向省调汇报故障情况和处置措施\n\n> **注意**：所有操作须经值长审核，严格执行操作票制度。`, time: '09:12:48', references: [
                { title: '220kV线路故障处置规程', section: 'Chunk #3', relevance: 0.95 },
                { title: '电网调度操作规范', section: 'Chunk #7', relevance: 0.88 }
            ] }
        ],
        retrievedPassages: []
    },
    {
        id: 'mock_conv_002',
        title: '主变过负荷紧急限负荷方案',
        user: '值班调度员',
        duration: '6.1s',
        createTime: '2025-03-14 14:35:00',
        updateTime: '2025-03-14 14:35:36',
        messages: [
            { role: 'user', content: '110kV某变电站1号主变因检修停运，2号主变负荷率已达91%，如何处置过负荷风险？', time: '14:35:00' },
            { role: 'assistant', content: `## 主变过负荷应急处置方案\n\n### 当前风险评估\n- 2号主变负荷率91%，已超预警阈值（85%），存在过负荷风险\n- 夏季高温时段，变压器散热能力下降约5-8%\n\n### 一、立即措施（0-30分钟）\n1. 安排运维人员赴现场监视2号主变油温和绕组温度\n2. 启动强迫风冷装置，提升散热能力\n3. 通知大工业用户按序错峰（预计降低负荷8-12%）\n\n### 二、负荷转移方案\n- 开合联络开关，将部分负荷转移至相邻变电站\n- 通知调度中心协调上级电网支援\n\n### 三、监控要求\n- 每15分钟上报一次主变温度和负荷率\n- 油温超过85°C立即启动降负荷操作\n\n> 参考：《变压器过负荷运行规程》第4.2节`, time: '14:35:36', references: [
                { title: '变压器运行规程', section: 'Chunk #12', relevance: 0.93 }
            ] }
        ],
        retrievedPassages: []
    },
    {
        id: 'mock_conv_003',
        title: '系统频率偏低调频操作',
        user: '值班调度员',
        duration: '5.7s',
        createTime: '2025-03-13 20:18:00',
        updateTime: '2025-03-13 20:18:34',
        messages: [
            { role: 'user', content: '系统频率持续偏低0.2Hz，维持在49.8Hz，如何进行调频操作？', time: '20:18:00' },
            { role: 'assistant', content: `## 系统频率偏低处置方案（49.8Hz）\n\n### 频率偏差分析\n- 当前频率49.8Hz，偏差-0.2Hz，超过正常偏差范围（±0.1Hz）\n- 可能原因：大容量机组跳闸、负荷突增、联络线功率波动\n\n### 一、一次调频（自动）\n- 确认各发电机组调速器正常工作，一次调频已自动响应\n- 检查各机组有功出力是否已上调至额定值\n\n### 二、二次调频（手动干预）\n1. 联系省调，申请增加AGC调节幅度\n2. 调用旋转备用容量：优先调用快速启动机组（燃气、抽水蓄能）\n3. 若频率继续下降至49.5Hz，启动低频减负荷装置\n\n### 三、排查原因\n- 检查是否有大容量机组非计划停运\n- 核实联络线计划/实际交换功率\n\n> 频率恢复至50Hz±0.05Hz后，向调度中心提交频率事件报告`, time: '20:18:34', references: [
                { title: '电网频率调节规程', section: 'Chunk #5', relevance: 0.91 }
            ] }
        ],
        retrievedPassages: []
    }
];

// ========== 孪生推演Mock数据（保留） ==========
const mockTwinSimulations = [
    {
        id: 'twin_1', title: '220kV宝华线N-1故障潮流推演',
        scenario: '模拟220kV宝华线跳闸后，分析潮流重新分布和相邻线路负荷变化',
        user: '系统运行员', duration: '5.2s', createTime: '2025-03-12 11:00:00', status: 'completed',
        params: { faultType: '单相接地', faultLine: '220kV宝华线', method: 'N-1校核' },
        result: {
            summary: '宝华线跳闸后，相邻华安线负荷率由62%升至85%，龙泉线由55%升至71%，均在安全限额内。',
            affectedLines: [
                { name: '220kV华安线', beforeLoad: '62%', afterLoad: '85%', status: '安全' },
                { name: '220kV龙泉线', beforeLoad: '55%', afterLoad: '71%', status: '安全' },
            ],
            recommendations: ['建议预开启龙华联络开关，分担华安线负荷约15%', '通知华安线运维加强巡视'],
            riskLevel: '低风险'
        }
    },
    {
        id: 'twin_2', title: '主变N-1过负荷潮流转移推演',
        scenario: '模拟110kV某站1号主变停运检修时，2号主变承载全部负荷的运行状态',
        user: '计划调度员', duration: '4.8s', createTime: '2025-03-11 09:15:00', status: 'completed',
        params: { faultType: '计划检修', equipment: '1号主变', method: 'N-1校核' },
        result: {
            summary: '1号主变停运后，2号主变负荷率由45%升至93%，高峰时段存在过负荷风险。',
            affectedLines: [{ name: '2号主变', beforeLoad: '45%', afterLoad: '93%', status: '预警' }],
            recommendations: ['检修期间安排大工业用户错峰用电', '通知2号主变运维加强油温监视'],
            riskLevel: '中风险'
        }
    }
];

export const useChatStore = defineStore('chat', {
    state: () => ({
        conversations: [],
        twinSimulations: [...mockTwinSimulations],
        currentConversationId: null,
        historyLoaded: false,
    }),
    getters: {
        sortedConversations(state) {
            return [...state.conversations].sort((a, b) => {
                const ta = a.updateTime || a.createTime || '';
                const tb = b.updateTime || b.createTime || '';
                return new Date(tb) - new Date(ta);
            });
        },
        currentConversation(state) {
            return state.conversations.find(c => c.id === state.currentConversationId) || null;
        },
        decisionLogs(state) {
            const chatLogs = state.conversations.map(conv => {
                const firstQ = conv.messages.find(m => m.role === 'user');
                const firstA = conv.messages.find(m => m.role === 'assistant');
                return {
                    id: conv.id, type: 'chat', typeLabel: '调度对话',
                    question: firstQ?.content || '', answer: firstA?.content || '',
                    retrievedPassages: conv.retrievedPassages || [],
                    user: conv.user || '值班调度员', duration: conv.duration || '-',
                    createTime: conv.createTime, messages: conv.messages
                };
            });
            const twinLogs = state.twinSimulations.map(sim => ({
                id: sim.id, type: 'twin', typeLabel: '孪生推演',
                question: sim.title, answer: sim.result.summary,
                retrievedPassages: [], twinResult: sim.result, twinParams: sim.params,
                scenario: sim.scenario, user: sim.user, duration: sim.duration,
                createTime: sim.createTime, riskLevel: sim.result.riskLevel
            }));
            return [...chatLogs, ...twinLogs].sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
        }
    },
    actions: {
        // 从后端加载持久化的对话历史，并与 mock 历史数据合并
        async loadHistory() {
            try {
                const res = await getChatHistoryReal();
                if (res.data?.code === 200 && Array.isArray(res.data.data)) {
                    const realConvs = res.data.data;
                    // 去掉已被真实数据覆盖的 mock 条目（避免重复）
                    const realIds = new Set(realConvs.map(c => c.id));
                    const filteredMock = mockConversations.filter(c => !realIds.has(c.id));
                    this.conversations = [...realConvs, ...filteredMock];
                    this.historyLoaded = true;
                }
            } catch (e) {
                console.warn('加载对话历史失败，使用本地数据:', e.message);
                if (!this.historyLoaded) {
                    this.conversations = [...mockConversations];
                }
                this.historyLoaded = true;
            }
        },
        selectConversation(id) {
            this.currentConversationId = id;
        },
        createConversation(id) {
            const convId = id || ('conv_' + Date.now());
            const conv = {
                id: convId, title: '新对话', user: '值班调度员', duration: '-',
                createTime: new Date().toLocaleString('zh-CN', { hour12: false }),
                messages: [], retrievedPassages: []
            };
            this.conversations.unshift(conv);
            this.currentConversationId = convId;
            return convId;
        },
        addMessage(conversationId, message) {
            const conv = this.conversations.find(c => c.id === conversationId);
            if (conv) {
                conv.messages.push(message);
                if (message.role === 'user' && conv.title === '新对话') {
                    conv.title = message.content.length > 20 ? message.content.substring(0, 20) + '...' : message.content;
                }
            }
        },
        // 用后端返回的完整对话数据更新本地
        upsertConversation(convData) {
            const idx = this.conversations.findIndex(c => c.id === convData.id);
            if (idx >= 0) {
                this.conversations[idx] = convData;
            } else {
                this.conversations.unshift(convData);
            }
        },
        updateConversationMeta(conversationId, meta) {
            const conv = this.conversations.find(c => c.id === conversationId);
            if (conv) Object.assign(conv, meta);
        },
        async deleteConversation(conversationId) {
            try {
                await deleteConversationReal(conversationId);
            } catch (e) { /* 忽略后端错误 */ }
            this.conversations = this.conversations.filter(c => c.id !== conversationId);
            if (this.currentConversationId === conversationId) {
                this.currentConversationId = null;
            }
        },
        addTwinSimulation(simulation) {
            this.twinSimulations.unshift(simulation);
        }
    }
});
