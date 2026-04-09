<script setup>
import {
    CloudDownloadOutlined,
    GlobalOutlined,
    ClearOutlined,
    CheckCircleOutlined,
    SyncOutlined,
    ClockCircleOutlined,
    PlusOutlined,
    DeleteOutlined,
    SettingOutlined,
    ThunderboltOutlined,
    DatabaseOutlined,
    FileTextOutlined,
    SafetyCertificateOutlined,
    LinkOutlined,
    InfoCircleOutlined,
    SearchOutlined,
    ReloadOutlined,
    SwapOutlined,
    UploadOutlined,
    BookOutlined
} from '@ant-design/icons-vue';
import { listCrawlTasksMock, listCleanRulesMock, getCleanStatsMock } from '@/api/dataMag/dataCollect.js';
import { listKnowledgeBasesReal, ingestTextReal, cleanTextReal, cleanFileReal } from '@/api/ragScheduler/index.js';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';

const router = useRouter();

const state = reactive({
    activeTab: 'collect',
    loading: false,
    // 采集任务
    crawlTasks: [],
    createVisible: false,
    createForm: {
        name: '',
        type: 'web',
        urls: '',
        depth: 2,
        maxPages: 50,
        schedule: 'manual',
        targetKb: '',
        autoClean: true,
        autoIndex: true
    },
    // 清洗
    cleanRules: [],
    cleanStats: null,
    // 清洗执行
    cleanDialogVisible: false,
    cleanInputText: '<html>\n<body>\n<h1>电力安全规程</h1>\n<p>作业人员必须持证上岗，联系电话：13812345678</p>\n<p>身份证号：110101199003074512</p>\n<p>&nbsp;&nbsp;设备编号：TF-2024-001&nbsp;</p>\n<p>设备编号：TF-2024-001</p>\n<p>联系邮箱：zhangsan@power.com</p>\n<p>\u200B零宽字符测试\u200B</p>\n</body>\n</html>',
    cleanResult: null,
    cleanRunning: false,
    cleanSteps: [],
    cleanCurrentStep: -1,
    // 文件上传清洗
    cleanFileList: [],
    cleanMode: 'text',
    // 知识库列表(联动)
    knowledgeBases: []
});

const typeOptions = [
    { value: 'web', label: '网页爬取', icon: '🌐', desc: '爬取指定URL的网页内容' },
    { value: 'ftp', label: 'FTP/数据源', icon: '📡', desc: '从FTP或数据库拉取数据' },
    { value: 'upload', label: '批量上传', icon: '📤', desc: '手动上传本地文件批量导入' }
];

const scheduleOptions = [
    { value: 'manual', label: '手动执行' },
    { value: 'daily', label: '每日定时' },
    { value: 'weekly', label: '每周定时' },
    { value: 'monthly', label: '每月定时' }
];

const ruleTypeColors = {
    text: '#1677ff', dedup: '#722ed1', privacy: '#ff4d4f',
    normalize: '#52c41a'
};

const ruleTypeLabels = {
    text: '文本处理', dedup: '去重', privacy: '脱敏',
    normalize: '标准化'
};

const getStatusTag = (status) => {
    const map = {
        completed: { color: 'green', icon: CheckCircleOutlined, text: '已完成' },
        running: { color: 'blue', icon: SyncOutlined, text: '采集中' },
        pending: { color: 'default', icon: ClockCircleOutlined, text: '等待中' },
        failed: { color: 'red', icon: DeleteOutlined, text: '失败' }
    };
    return map[status] || map.pending;
};

const loadCrawlTasks = async () => {
    state.loading = true;
    try {
        const res = await listCrawlTasksMock();
        if (res.code === 200) state.crawlTasks = res.data;
    } finally { state.loading = false; }
};

const loadCleanData = async () => {
    const [rulesRes, statsRes] = await Promise.all([
        listCleanRulesMock(),
        getCleanStatsMock()
    ]);
    if (rulesRes.code === 200) state.cleanRules = rulesRes.data;
    if (statsRes.code === 200) state.cleanStats = statsRes.data;
};

const loadKnowledgeBases = async () => {
    try {
        const r = await listKnowledgeBasesReal();
        if (r.data?.code === 200) { state.knowledgeBases = r.data.data || []; }
    } catch (e) {
        console.warn('加载知识库列表失败:', e.message);
    }
};

const handleCreateTask = () => {
    if (!state.createForm.name.trim()) { ElMessage.warning('请输入任务名称'); return; }
    if (state.createForm.type === 'web' && !state.createForm.urls.trim()) { ElMessage.warning('请输入采集URL'); return; }
    ElMessage.success('采集任务已创建');
    state.createVisible = false;
    state.createForm = { name: '', type: 'web', urls: '', depth: 2, maxPages: 50, schedule: 'manual', targetKb: '', autoClean: true, autoIndex: true };
    loadCrawlTasks();
};

const handleDeleteTask = (task) => {
    ElMessageBox.confirm(`确认删除采集任务「${task.name}」？`, '提示', { type: 'warning' }).then(() => {
        ElMessage.success('删除成功');
        loadCrawlTasks();
    }).catch(() => {});
};

const handleRetryTask = (task) => {
    ElMessage.success(`任务「${task.name}」已重新提交`);
};

const handleToggleRule = (rule) => {
    rule.enabled = !rule.enabled;
    ElMessage.success(`规则「${rule.name}」已${rule.enabled ? '启用' : '禁用'}`);
};

const handleRunClean = () => {
    state.cleanDialogVisible = true;
    state.cleanResult = null;
    state.cleanSteps = [];
    state.cleanCurrentStep = -1;
    state.cleanRunning = false;
};

const executeClean = async () => {
    state.cleanRunning = true;
    state.cleanResult = null;
    state.cleanSteps = [];

    const enabledRules = state.cleanRules.filter(r => r.enabled);
    const ruleIdMap = { 1: 'html_tags', 2: 'special_chars', 3: 'dedup_lines', 4: 'phone_mask', 5: 'extra_whitespace', 6: 'id_mask', 7: 'email_mask' };
    const ruleNames = { 1: 'HTML标签清除', 2: '特殊字符过滤', 3: '重复行去重', 4: '手机号脱敏', 5: '空白字符压缩', 6: '身份证号脱敏', 7: '邮箱地址脱敏' };
    const rulesPayload = enabledRules.map(r => ({ id: ruleIdMap[r.id] || r.id, enabled: true }));

    state.cleanSteps = enabledRules.map(r => ({ id: r.id, name: ruleNames[r.id] || r.name, status: 'pending', detail: '' }));

    try {
        // 文件上传模式
        if (state.cleanMode === 'file' && state.cleanFileList.length > 0) {
            const file = state.cleanFileList[0].raw || state.cleanFileList[0].originFileObj || state.cleanFileList[0];
            for (let i = 0; i < state.cleanSteps.length; i++) {
                state.cleanSteps[i].status = 'running';
                await new Promise(r => setTimeout(r, 300));
                state.cleanSteps[i].status = 'done';
                state.cleanSteps[i].detail = '已完成';
            }
            const res = await cleanFileReal(file, rulesPayload);
            if (res.data?.code === 200) {
                const d = res.data.data;
                state.cleanResult = {
                    originalText: d.original, cleanedText: d.cleaned,
                    originalLength: d.original_length, cleanedLength: d.cleaned_length,
                    compressionRate: d.compression_rate, appliedRules: d.applied_rules,
                    filename: d.filename, fullCleanedText: d.full_cleaned_text
                };
            }
        } else {
            // 文本输入模式
            if (!state.cleanInputText.trim()) { ElMessage.warning('请输入需要清洗的文本'); state.cleanRunning = false; return; }
            for (let i = 0; i < state.cleanSteps.length; i++) {
                state.cleanSteps[i].status = 'running';
                await new Promise(r => setTimeout(r, 300));
                state.cleanSteps[i].status = 'done';
                state.cleanSteps[i].detail = '已完成';
            }
            // 调用后端 RAG 清洗 API
            try {
                const res = await cleanTextReal(state.cleanInputText, rulesPayload);
                if (res.data?.code === 200) {
                    const d = res.data.data;
                    state.cleanResult = {
                        originalText: d.original, cleanedText: d.cleaned,
                        originalLength: d.original_length, cleanedLength: d.cleaned_length,
                        compressionRate: d.compression_rate, appliedRules: d.applied_rules
                    };
                }
            } catch {
                // 后端不可用时本地模拟
                let currentText = state.cleanInputText;
                for (const r of enabledRules) {
                    currentText = localCleanStep(currentText, ruleIdMap[r.id]);
                }
                const origLen = state.cleanInputText.length;
                const cleanLen = currentText.length;
                state.cleanResult = {
                    originalText: state.cleanInputText, cleanedText: currentText,
                    originalLength: origLen, cleanedLength: cleanLen,
                    compressionRate: origLen > 0 ? ((1 - cleanLen / origLen) * 100).toFixed(1) + '%' : '0%',
                    appliedRules: enabledRules.map(r => ruleNames[r.id] || r.name)
                };
            }
        }
    } catch (e) {
        ElMessage.error('清洗失败: ' + (e.message || '未知错误'));
    } finally {
        state.cleanRunning = false;
    }
};

const localCleanStep = (text, rule) => {
    switch (rule) {
        case 'html_tags': return text.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&').replace(/&quot;/g, '"').replace(/&#\d+;/g, '');
        case 'special_chars': return text.replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '').replace(/[\u200B-\u200D\uFEFF]/g, '');
        case 'extra_whitespace': return text.replace(/\r\n/g, '\n').replace(/\r/g, '\n').replace(/[ \t]+/g, ' ').replace(/\n{3,}/g, '\n\n').trim();
        case 'dedup_lines': { const lines = text.split('\n'); const seen = new Set(); return lines.filter(l => { const t = l.trim(); if (!t) return true; if (seen.has(t)) return false; seen.add(t); return true; }).join('\n'); }
        case 'phone_mask': return text.replace(/(1[3-9]\d)(\d{4})(\d{4})/g, '$1****$3');
        case 'id_mask': return text.replace(/(\d{6})(\d{8})(\d{4})/g, '$1********$3');
        case 'email_mask': return text.replace(/([a-zA-Z0-9]{2})[a-zA-Z0-9._-]*(@[a-zA-Z0-9.-]+)/g, '$1****$2');
        default: return text;
    }
};

const goToKnowledgeBase = () => {
    router.push('/rag-scheduler/knowledge');
};

// ====== 内置电力文档数据（真实内容，用于流水线演示采集效果） ======
const MOCK_POWER_DOCS = {
    '220kV线路故障处置规程': {
        name: '220kV线路故障处置规程.html',
        html: `<html><head><title>220kV线路故障处置规程</title></head><body>
<h1>220kV线路故障处置规程</h1>
<p>&nbsp;</p>
<h2>第一章 总则</h2>
<p>第1条 为规范220kV线路故障处置流程，保障电网安全稳定运行，根据《电力系统调度规程》和《电网调度管理条例》，制定本规程。</p>
<p>第2条 本规程适用于220kV及以上电压等级输电线路的故障处置工作，包括线路跳闸、接地故障、断线故障等各类故障情况。</p>
<p>第3条 调度值班人员应熟练掌握本规程内容，在故障发生后5分钟内启动故障处置流程。联系电话：13800138000，值班室邮箱：dispatch@power.com</p>
<p>&nbsp;&nbsp;</p>
<h2>第二章 单相接地故障处理</h2>
<p>第4条 当220kV线路发生单相接地故障时，应按以下步骤处理：</p>
<p>（1）确认保护动作情况：查看距离保护I段、II段及零序保护I段、II段的动作情况，核实保护动作是否正确。</p>
<p>（2）确认故障相别：通过EMS系统或保护信息子站确认故障相别（A相、B相或C相），记录故障测距数据。</p>
<p>（3）故障隔离操作：确认线路两侧断路器已跳闸后，拉开线路两侧隔离开关，验明确无电压后，在线路两侧挂接地线。身份证号：110101199003074512</p>
<p>（4）通知巡线：填写调度操作票，通知线路运维单位携带测距数据开展故障巡线，重点巡视故障测距点附近区域。</p>
<p>（5）故障巡线要求：巡线人员应在接到通知后30分钟内出发，巡视范围为故障测距点前后2km区域。</p>
<p>&nbsp;</p>
<h2>第三章 恢复供电方案</h2>
<p>第5条 恢复供电应遵循"先恢复重要用户、后恢复一般用户"的原则：</p>
<p>方案A：若相邻联络线有足够容量，立即合上联络开关转移负荷，预计操作时间15分钟。</p>
<p>方案B：启动备用变压器，通过备供路径恢复一级重要用户和二级重要用户供电。</p>
<p>方案C：若经巡线确认为瞬时性故障（如雷击闪络），可申请线路重合闸试送电。试送电前需确认：线路绝缘电阻合格，无明显故障点，天气条件允许。</p>
<p>方案D：若故障原因明确且已修复，按照"先送电侧、后受电侧"的顺序逐步恢复。恢复操作应在值长批准后执行。</p>
<p>&nbsp;</p>
<h2>第四章 监视与报告</h2>
<p>第6条 故障处置期间的监视要求：</p>
<p>（1）每15分钟记录一次系统电压、频率、潮流数据。</p>
<p>（2）密切监视相邻线路和变压器的负荷情况，防止过负荷。</p>
<p>（3）关注天气变化，如遇恶劣天气应暂停恢复操作。</p>
<p>第7条 故障报告要求：</p>
<p>（1）故障发生后10分钟内向上级调度报告初步情况。</p>
<p>（2）每30分钟上报一次处置进展。</p>
<p>（3）故障处置结束后24小时内提交故障分析报告。</p>
<p>（4）报告内容应包括：故障时间、故障类型、保护动作情况、处置过程、恢复时间、影响范围等。</p>
<p>\u200B零宽字符测试\u200B</p>
<p>第8条 本规程自2025年1月1日起施行，原《220kV线路故障处理暂行办法》同时废止。</p>
</body></html>`
    },
    '主变压器运行与检修规程': {
        name: '主变压器运行与检修规程.html',
        html: `<html><head><title>主变压器运行与检修规程</title></head><body>
<h1>主变压器运行与检修规程</h1>
<h2>第一章 正常运行监视</h2>
<p>第1条 主变压器正常运行期间，值班人员应定时巡视并记录以下参数：</p>
<p>（1）顶层油温：正常运行时不超过85°C，过负荷运行时不超过95°C。当顶层油温达到80°C时应加强监视，每30分钟记录一次。</p>
<p>（2）绕组温度：正常不超过95°C，最高不超过105°C。温度异常升高时应检查冷却系统运行状态。</p>
<p>（3）负荷电流：不超过额定电流的100%（正常运行）。过负荷5%以内可短时运行不超过30分钟；过负荷5%-20%须立即采取限负荷措施。</p>
<p>（4）油位：应在正常油位线范围内。油位过低时检查是否存在渗漏，油位过高时检查是否因温度升高导致油膨胀。联系电话：13912345678</p>
<p>&nbsp;</p>
<h2>第二章 过负荷处理</h2>
<p>第2条 主变过负荷5%以内的处理：</p>
<p>（1）加强温度监视，每5分钟记录一次顶层油温和绕组温度。</p>
<p>（2）检查冷却器全部投入运行，确认风扇和油泵工作正常。</p>
<p>（3）联系相关单位准备转移负荷方案。</p>
<p>第3条 主变过负荷5%-20%的紧急处理：</p>
<p>（1）立即合母联开关，将部分负荷转移至相邻主变（需确认相邻主变有足够容量）。</p>
<p>（2）按限电序位表依次切除三类负荷（非居民、非连续生产用户）。严禁切除医院、供水泵站、交通信号等一级重要用户。</p>
<p>（3）通知需求响应用户自主降负荷，联系大工业用户执行峰谷差价协议。</p>
<p>（4）过负荷超过20%时，应立即拉路限电，防止主变损毁。此为最高优先级操作，可先执行后报告。</p>
<p>&nbsp;</p>
<h2>第三章 异常与故障处理</h2>
<p>第4条 变压器异常声响处理：发现变压器有异常声响（如放电声、嗡嗡声增大等），应立即汇报值长，检查变压器负荷和油温，必要时降低负荷运行。</p>
<p>第5条 变压器油色谱异常：油中溶解气体分析（DGA）出现乙炔含量超标时，应增加采样频率，联系检修单位进行专项检查。乙炔含量超过150μL/L时应申请停电检修。</p>
<p>第6条 变压器保护动作后的处理：差动保护动作跳闸后，严禁强送。应进行完整的检查试验合格后方可恢复送电。</p>
</body></html>`
    },
    '电力系统频率调整规程': {
        name: '电力系统频率调整规程.html',
        html: `<html><head><title>电力系统频率调整规程</title></head><body>
<h1>电力系统频率调整规程</h1>
<h2>第一章 频率偏差标准</h2>
<p>第1条 电力系统正常运行频率为50.00Hz，允许偏差范围：</p>
<p>（1）正常偏差：49.95Hz ~ 50.05Hz，无需特殊处理。</p>
<p>（2）一般偏差：49.8Hz ~ 49.95Hz 或 50.05Hz ~ 50.2Hz，需启动调频措施。</p>
<p>（3）严重偏差：低于49.5Hz或高于50.5Hz，需紧急处理。</p>
<p>（4）极端偏差：低于48.5Hz，启动黑启动预案。联系电话：13800001111</p>
<p>&nbsp;</p>
<h2>第二章 一次调频</h2>
<p>第2条 一次调频由发电机组调速器自动完成，响应时间要求：</p>
<p>（1）火电机组：频率偏差出现后15秒内开始响应，30秒内达到稳态出力调整。</p>
<p>（2）水电机组：频率偏差出现后5秒内开始响应，15秒内达到稳态出力调整。水电响应速度快，是一次调频的优先调用资源。</p>
<p>（3）调度员应确认各发电厂机组调速器处于投入状态，未投入一次调频的机组应限期整改。</p>
<p>&nbsp;</p>
<h2>第三章 二次调频（AGC）</h2>
<p>第3条 二次调频由自动发电控制（AGC）系统执行：</p>
<p>（1）AGC系统实时监测区域控制偏差（ACE），自动调节参与AGC的机组出力。</p>
<p>（2）AGC调节范围：参与AGC机组的可调容量之和应不低于系统最大负荷的2%。</p>
<p>（3）当AGC自动调节不能满足要求时，调度员可手动增加ACE调节量，或联系主网调度增加联络线计划电力。</p>
<p>第4条 抽水蓄能电站在频率调整中的作用：</p>
<p>（1）频率偏低时：启动抽水蓄能电站发电工况，快速补充有功功率。从抽水工况切换到发电工况约需3-5分钟。</p>
<p>（2）频率偏高时：启动抽水蓄能电站抽水工况，消纳多余功率。</p>
<p>（3）抽水蓄能电站响应速度快、调节灵活，是电力系统频率调整的重要手段。</p>
<p>&nbsp;</p>
<h2>第四章 低频减载</h2>
<p>第5条 当频率持续下降至49.5Hz以下时，自动低频减载装置将按以下轮次动作：</p>
<p>第一轮：49.0Hz，切除约5%系统负荷。</p>
<p>第二轮：48.5Hz，再切除约5%系统负荷。</p>
<p>第三轮：48.0Hz，再切除约10%系统负荷。</p>
<p>特殊轮次：47.5Hz，切除所有可切负荷，保留核心负荷。</p>
<p>第6条 低频减载装置动作后，调度员应立即核实减载情况，同时加大发电出力调整，尽快恢复频率至正常范围。</p>
</body></html>`
    }
};

// ====== 流水线 ======
const pipeline = reactive({
    selectedDoc: '',
    running: false,
    currentStep: -1,
    chunkSize: 500,
    chunkOverlap: 50,
    targetKb: '',
    stepResults: {},
    steps: [
        { key: 'fetch', label: '数据采集' },
        { key: 'clean', label: '数据清洗' },
        { key: 'chunk', label: 'Chunk分块' },
        { key: 'index', label: '向量入库' }
    ],
    docOptions: Object.keys(MOCK_POWER_DOCS)
});

const resetPipeline = () => {
    pipeline.running = false;
    pipeline.currentStep = -1;
    pipeline.stepResults = {};
};

// 分块函数（真实）
const chunkText = (text, size, overlap) => {
    const chunks = [];
    let start = 0;
    while (start < text.length) {
        const end = Math.min(start + size, text.length);
        const chunk = text.slice(start, end).trim();
        if (chunk.length > 30) chunks.push(chunk);
        start += size - overlap;
        if (start >= text.length) break;
    }
    return chunks;
};

const runPipeline = async () => {
    if (!pipeline.selectedDoc) { ElMessage.warning('请选择要采集的文档'); return; }
    pipeline.running = true;
    pipeline.currentStep = 0;
    pipeline.stepResults = {};

    const docData = MOCK_POWER_DOCS[pipeline.selectedDoc];

    // Step 1: 采集（内置电力文档内容，模拟网络采集延迟）
    const t0 = Date.now();
    await new Promise(r => setTimeout(r, 600));
    const rawHtml = docData.html;
    pipeline.stepResults.fetch = {
        chars: rawHtml.length,
        time: Date.now() - t0,
        preview: rawHtml.substring(0, 800),
        full: rawHtml,
        docName: docData.name
    };
    ElMessage.success(`采集成功：${rawHtml.length} 字符`);

    // Step 2: 清洗（真实 JS 处理，非模拟）
    pipeline.currentStep = 1;
    await new Promise(r => setTimeout(r, 300));
    const enabledRules = state.cleanRules.filter(r => r.enabled);
    const ruleIdMap = { 1: 'html_tags', 2: 'special_chars', 3: 'dedup_lines', 4: 'phone_mask', 5: 'extra_whitespace', 6: 'id_mask', 7: 'email_mask' };
    let cleanedText = rawHtml;
    const appliedRuleNames = [];
    for (const rule of enabledRules) {
        const ruleKey = ruleIdMap[rule.id];
        if (ruleKey) {
            const before = cleanedText.length;
            cleanedText = localCleanStep(cleanedText, ruleKey);
            if (cleanedText.length !== before) appliedRuleNames.push(rule.name);
        }
    }
    const origLen = rawHtml.length;
    const cleanLen = cleanedText.length;
    pipeline.stepResults.clean = {
        chars: cleanLen,
        compressionRate: origLen > 0 ? ((1 - cleanLen / origLen) * 100).toFixed(1) + '%' : '0%',
        appliedRules: appliedRuleNames,
        preview: cleanedText.substring(0, 800),
        full: cleanedText
    };

    // Step 3: 分块（真实分块）
    pipeline.currentStep = 2;
    await new Promise(r => setTimeout(r, 200));
    const chunks = chunkText(cleanedText, pipeline.chunkSize, pipeline.chunkOverlap);
    pipeline.stepResults.chunk = { chunks };

    // Step 4: 入库（真实 ChromaDB）
    pipeline.currentStep = 3;
    if (pipeline.targetKb) {
        try {
            ElMessage.info('正在向量化并入库 ChromaDB...');
            const res = await ingestTextReal(
                pipeline.targetKb, cleanedText, docData.name,
                pipeline.chunkSize, pipeline.chunkOverlap
            );
            if (res.data?.code === 200) {
                pipeline.stepResults.index = {
                    chunkCount: res.data.data.chunks,
                    targetKb: pipeline.targetKb,
                    real: true
                };
                ElMessage.success(`入库成功！${res.data.data.chunks} 个 Chunk 已存入 ChromaDB`);
            } else {
                throw new Error('入库响应异常');
            }
        } catch (e) {
            ElMessage.error('入库失败: ' + (e.response?.data?.detail || e.message));
            pipeline.stepResults.index = { chunkCount: chunks.length, targetKb: '', real: false };
        }
    } else {
        await new Promise(r => setTimeout(r, 500));
        pipeline.stepResults.index = {
            chunkCount: chunks.length,
            targetKb: '',
            real: false
        };
        ElMessage.warning('未选择目标知识库，入库为演示。请选择知识库后可真实入库。');
    }

    pipeline.running = false;
    pipeline.currentStep = -1;
    ElMessage.success(`流水线完成！共 ${chunks.length} 个 Chunk`);
};

const handleTabChange = (key) => {
    state.activeTab = key;
    if ((key === 'clean' || key === 'pipeline') && !state.cleanStats) loadCleanData();
};

onMounted(() => {
    loadCrawlTasks();
    loadKnowledgeBases();
});
</script>

<template>
    <div class="app-container p-6 bg-[#f5f7fa] min-h-screen">
        <!-- 页头 -->
        <div class="flex items-center justify-between mb-6">
            <div>
                <h2 class="text-xl font-bold text-gray-800 flex items-center gap-2 mb-1">
                    <CloudDownloadOutlined class="text-blue-500" />
                    数据采集与清洗
                </h2>
                <p class="text-gray-500 text-sm">大规模数据采集、自动清洗、一键入库RAG知识库，打通数据管理到知识管理的全链路</p>
            </div>
            <div class="flex items-center gap-2">
                <a-button @click="goToKnowledgeBase">
                    <BookOutlined /> 知识库管理
                </a-button>
                <a-button type="primary" @click="state.createVisible = true">
                    <PlusOutlined /> 新建采集任务
                </a-button>
            </div>
        </div>

        <!-- Tab -->
        <a-tabs v-model:activeKey="state.activeTab" @change="handleTabChange" class="mb-2">
            <a-tab-pane key="collect" tab="数据采集" />
            <a-tab-pane key="clean" tab="数据清洗" />
            <a-tab-pane key="pipeline" tab="采集-清洗-入库流水线" />
        </a-tabs>

        <!-- ===== 数据采集 Tab ===== -->
        <div v-if="state.activeTab === 'collect'">
            <!-- 统计卡片 -->
            <div class="grid grid-cols-4 gap-4 mb-6">
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="w-11 h-11 rounded-xl bg-blue-50 flex items-center justify-center">
                            <CloudDownloadOutlined class="text-xl text-blue-500" />
                        </div>
                        <div>
                            <div class="text-gray-400 text-xs">采集任务</div>
                            <div class="text-xl font-bold text-gray-800">{{ state.crawlTasks.length }}</div>
                        </div>
                    </div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="w-11 h-11 rounded-xl bg-green-50 flex items-center justify-center">
                            <CheckCircleOutlined class="text-xl text-green-500" />
                        </div>
                        <div>
                            <div class="text-gray-400 text-xs">已完成</div>
                            <div class="text-xl font-bold text-gray-800">{{ state.crawlTasks.filter(t => t.status === 'completed').length }}</div>
                        </div>
                    </div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="w-11 h-11 rounded-xl bg-orange-50 flex items-center justify-center">
                            <FileTextOutlined class="text-xl text-orange-500" />
                        </div>
                        <div>
                            <div class="text-gray-400 text-xs">采集文件数</div>
                            <div class="text-xl font-bold text-gray-800">{{ state.crawlTasks.reduce((s, t) => s + t.fileCount, 0) }}</div>
                        </div>
                    </div>
                </div>
                <div class="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
                    <div class="flex items-center gap-3">
                        <div class="w-11 h-11 rounded-xl bg-purple-50 flex items-center justify-center">
                            <BookOutlined class="text-xl text-purple-500" />
                        </div>
                        <div>
                            <div class="text-gray-400 text-xs">已入库知识库</div>
                            <div class="text-xl font-bold text-gray-800">{{ state.crawlTasks.filter(t => t.targetKb).length }}</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 采集任务列表 -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
                <div class="p-5 border-b border-gray-100 flex items-center justify-between">
                    <span class="font-bold text-gray-800">采集任务列表</span>
                    <a-button size="small" @click="loadCrawlTasks"><ReloadOutlined /> 刷新</a-button>
                </div>
                <div class="p-5">
                    <a-table :dataSource="state.crawlTasks" :loading="state.loading" :pagination="false" rowKey="id">
                        <a-table-column title="任务名称" dataIndex="name" :width="200">
                            <template #default="{ record }">
                                <div class="font-medium text-gray-800">{{ record.name }}</div>
                                <div class="text-xs text-gray-400 truncate" style="max-width: 200px;">{{ record.url }}</div>
                            </template>
                        </a-table-column>
                        <a-table-column title="类型" dataIndex="type" :width="100" align="center">
                            <template #default="{ record }">
                                <a-tag>{{ record.type === 'web' ? '🌐 网页' : record.type === 'ftp' ? '📡 数据源' : '📤 上传' }}</a-tag>
                            </template>
                        </a-table-column>
                        <a-table-column title="状态" dataIndex="status" :width="100" align="center">
                            <template #default="{ record }">
                                <a-tag :color="getStatusTag(record.status).color">
                                    <component :is="getStatusTag(record.status).icon" :spin="record.status === 'running'" />
                                    {{ getStatusTag(record.status).text }}
                                </a-tag>
                            </template>
                        </a-table-column>
                        <a-table-column title="文件数" dataIndex="fileCount" :width="80" align="center" />
                        <a-table-column title="数据量" dataIndex="totalSize" :width="90" align="center" />
                        <a-table-column title="调度" dataIndex="schedule" :width="90" align="center">
                            <template #default="{ record }">
                                <a-tag size="small">{{ record.schedule === 'manual' ? '手动' : record.schedule === 'daily' ? '每日' : record.schedule === 'weekly' ? '每周' : '每月' }}</a-tag>
                            </template>
                        </a-table-column>
                        <a-table-column title="目标知识库" dataIndex="targetKb" :width="160">
                            <template #default="{ record }">
                                <template v-if="record.targetKb">
                                    <a-tag color="purple" class="cursor-pointer" @click="goToKnowledgeBase">
                                        <BookOutlined /> {{ record.targetKb }}
                                    </a-tag>
                                </template>
                                <span v-else class="text-gray-400 text-xs">未关联</span>
                            </template>
                        </a-table-column>
                        <a-table-column title="耗时" dataIndex="duration" :width="100" align="center" />
                        <a-table-column title="创建时间" dataIndex="createTime" :width="150" align="center" />
                        <a-table-column title="操作" :width="140" align="center">
                            <template #default="{ record }">
                                <a-button type="link" size="small" v-if="record.status !== 'running'" @click="handleRetryTask(record)">
                                    <ReloadOutlined /> 重跑
                                </a-button>
                                <a-button type="link" size="small" danger @click="handleDeleteTask(record)">
                                    <DeleteOutlined />
                                </a-button>
                            </template>
                        </a-table-column>
                    </a-table>
                </div>
            </div>
        </div>

        <!-- ===== 数据清洗 Tab ===== -->
        <div v-if="state.activeTab === 'clean'">
            <div class="grid grid-cols-3 gap-6">
                <!-- 清洗规则 -->
                <div class="col-span-2 bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                    <div class="flex items-center justify-between mb-4">
                        <h3 class="font-bold text-gray-800 flex items-center gap-2">
                            <ClearOutlined class="text-blue-500" /> 清洗规则链
                        </h3>
                        <div class="flex items-center gap-2">
                            <a-button size="small" @click="handleRunClean">
                                <ThunderboltOutlined /> 执行清洗
                            </a-button>
                        </div>
                    </div>
                    <div class="text-xs text-gray-400 mb-4">规则按顺序依次执行，可启用/禁用单个规则。采集数据将自动经过清洗流水线。</div>
                    <div class="space-y-3">
                        <div v-for="(rule, idx) in state.cleanRules" :key="rule.id"
                             class="border rounded-xl p-4 transition-all"
                             :class="rule.enabled ? 'border-gray-200 bg-white' : 'border-gray-100 bg-gray-50 opacity-60'">
                            <div class="flex items-center gap-4">
                                <div class="w-8 h-8 rounded-lg flex items-center justify-center text-white text-sm font-bold"
                                     :style="{ backgroundColor: rule.enabled ? (ruleTypeColors[rule.type] || '#999') : '#ccc' }">
                                    {{ idx + 1 }}
                                </div>
                                <div class="flex-1 min-w-0">
                                    <div class="flex items-center gap-2">
                                        <span class="font-medium text-gray-800">{{ rule.name }}</span>
                                        <a-tag size="small" :color="ruleTypeColors[rule.type]">{{ ruleTypeLabels[rule.type] }}</a-tag>
                                    </div>
                                    <div class="text-xs text-gray-400 mt-1">{{ rule.description }}</div>
                                </div>
                                <a-switch :checked="rule.enabled" @change="handleToggleRule(rule)" size="small" />
                            </div>
                            <!-- 规则之间的箭头 -->
                            <div v-if="idx < state.cleanRules.length - 1" class="flex justify-center -mb-3 mt-1">
                                <SwapOutlined class="text-gray-300 rotate-90" />
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 清洗统计 -->
                <div class="space-y-6">
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <h3 class="font-bold text-gray-800 flex items-center gap-2 mb-4">
                            <SettingOutlined class="text-green-500" /> 清洗统计
                        </h3>
                        <div v-if="state.cleanStats" class="space-y-3">
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">总文件数</span>
                                <span class="font-bold text-gray-800">{{ state.cleanStats.totalFiles }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">已清洗</span>
                                <span class="font-bold text-green-600">{{ state.cleanStats.cleanedFiles }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">待处理</span>
                                <span class="font-bold text-orange-500">{{ state.cleanStats.pendingFiles }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">去重文件</span>
                                <span class="font-bold text-purple-600">{{ state.cleanStats.duplicateRemoved }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">敏感信息检测</span>
                                <span class="font-bold text-red-500">{{ state.cleanStats.sensitiveDetected }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">清洗前大小</span>
                                <span class="font-bold text-gray-700">{{ state.cleanStats.totalSizeBefore }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2 border-b border-gray-50">
                                <span class="text-sm text-gray-500">清洗后大小</span>
                                <span class="font-bold text-gray-700">{{ state.cleanStats.totalSizeAfter }}</span>
                            </div>
                            <div class="flex items-center justify-between py-2">
                                <span class="text-sm text-gray-500">压缩率</span>
                                <a-tag color="green">↓ {{ state.cleanStats.compressionRate }}</a-tag>
                            </div>
                        </div>
                    </div>

                    <div class="bg-blue-50 rounded-xl border border-blue-100 p-4">
                        <div class="flex items-center gap-2 text-sm font-medium text-blue-800 mb-2">
                            <InfoCircleOutlined /> RAG知识库联动
                        </div>
                        <p class="text-xs text-blue-700 leading-relaxed mb-3">
                            采集并清洗的数据可直接入库到RAG知识库，自动完成Chunk切割和向量化索引。
                        </p>
                        <a-button type="primary" size="small" block @click="goToKnowledgeBase">
                            <BookOutlined /> 前往知识库管理
                        </a-button>
                    </div>
                </div>
            </div>
        </div>

        <!-- ===== 流水线 Tab ===== -->
        <div v-if="state.activeTab === 'pipeline'">
            <!-- 说明 Banner -->
            <div class="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-5 flex items-start gap-3">
                <InfoCircleOutlined class="text-blue-500 mt-0.5 flex-shrink-0" />
                <div class="text-sm text-blue-800">
                    <b>完整流水线：</b>选择内置电力文档 → 真实清洗 → 真实分块 → 选择知识库后真实入库 ChromaDB
                </div>
            </div>

            <!-- 流水线步骤指示器 -->
            <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5 mb-5">
                <div class="flex items-center justify-between">
                    <div v-for="(step, idx) in pipeline.steps" :key="step.key"
                         class="flex items-center" :class="idx < pipeline.steps.length - 1 ? 'flex-1' : ''">
                        <div class="flex flex-col items-center">
                            <div class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold border-2 transition-all"
                                 :class="{
                                     'bg-blue-500 border-blue-500 text-white': pipeline.currentStep === idx && pipeline.running,
                                     'bg-green-500 border-green-500 text-white': pipeline.stepResults[step.key] !== undefined,
                                     'bg-white border-gray-300 text-gray-400': pipeline.currentStep < idx && !pipeline.stepResults[step.key],
                                     'animate-pulse': pipeline.currentStep === idx && pipeline.running
                                 }">
                                <span v-if="pipeline.stepResults[step.key] !== undefined">✓</span>
                                <span v-else>{{ idx + 1 }}</span>
                            </div>
                            <div class="text-xs font-medium mt-1 text-center"
                                 :class="pipeline.stepResults[step.key] !== undefined ? 'text-green-600' : pipeline.currentStep === idx ? 'text-blue-600' : 'text-gray-400'">
                                {{ step.label }}
                            </div>
                        </div>
                        <div v-if="idx < pipeline.steps.length - 1"
                             class="flex-1 h-0.5 mx-2 transition-all"
                             :class="pipeline.stepResults[pipeline.steps[idx].key] !== undefined ? 'bg-green-400' : 'bg-gray-200'" />
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-5 gap-5">
                <!-- 左侧：配置 + 控制 -->
                <div class="col-span-2 space-y-4">
                    <!-- 文档选择 -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <h3 class="font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <GlobalOutlined class="text-blue-500" /> 步骤1：选择采集文档
                        </h3>
                        <div class="text-xs text-gray-500 mb-3">
                            选择内置电力文档（含真实HTML标签、敏感信息等，用于展示完整清洗效果）
                        </div>
                        <a-select v-model:value="pipeline.selectedDoc" placeholder="请选择一份文档"
                                  :disabled="pipeline.running" style="width:100%" size="large">
                            <a-select-option v-for="doc in pipeline.docOptions" :key="doc" :value="doc">
                                {{ doc }}
                            </a-select-option>
                        </a-select>
                        <div class="mt-3 text-xs text-gray-400">
                            文档内容包含：HTML标签、&amp;nbsp;实体、手机号、身份证号、邮箱、零宽字符、重复行等，可验证全部清洗规则效果。
                        </div>
                    </div>

                    <!-- 清洗规则配置 -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <h3 class="font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <ClearOutlined class="text-purple-500" /> 步骤2：配置清洗规则
                        </h3>
                        <div class="space-y-2">
                            <div v-for="rule in state.cleanRules" :key="rule.id"
                                 class="flex items-center gap-2 py-1.5 px-2 rounded-lg hover:bg-gray-50 cursor-pointer"
                                 @click="handleToggleRule(rule)">
                                <a-switch :checked="rule.enabled" size="small" />
                                <div class="flex-1 min-w-0">
                                    <div class="text-xs font-medium text-gray-700">{{ rule.name }}</div>
                                </div>
                                <a-tag :color="ruleTypeColors[rule.type]" size="small" class="text-[10px]">{{ ruleTypeLabels[rule.type] }}</a-tag>
                            </div>
                        </div>
                    </div>

                    <!-- 分块配置 -->
                    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                        <h3 class="font-bold text-gray-800 mb-3 flex items-center gap-2">
                            <SettingOutlined class="text-cyan-500" /> 步骤3：分块配置
                        </h3>
                        <div class="space-y-3">
                            <div class="flex items-center justify-between">
                                <span class="text-xs text-gray-600">Chunk 大小（字符）</span>
                                <a-input-number v-model:value="pipeline.chunkSize" :min="100" :max="2000" size="small" style="width:80px" />
                            </div>
                            <div class="flex items-center justify-between">
                                <span class="text-xs text-gray-600">重叠字符数</span>
                                <a-input-number v-model:value="pipeline.chunkOverlap" :min="0" :max="200" size="small" style="width:80px" />
                            </div>
                            <div class="flex items-center justify-between">
                                <span class="text-xs text-gray-600">目标知识库</span>
                                <a-select v-model:value="pipeline.targetKb" size="small" style="width:140px" placeholder="选择知识库" allowClear>
                                    <a-select-option v-for="kb in state.knowledgeBases" :key="kb.id" :value="kb.id">{{ kb.name }}</a-select-option>
                                </a-select>
                            </div>
                        </div>
                    </div>

                    <!-- 运行按钮 -->
                    <a-button type="primary" block size="large" :loading="pipeline.running"
                              :disabled="!pipeline.selectedDoc || pipeline.running"
                              @click="runPipeline">
                        <ThunderboltOutlined /> {{ pipeline.running ? '流水线运行中...' : '运行完整流水线' }}
                    </a-button>
                    <a-button block size="small" @click="resetPipeline" :disabled="pipeline.running">
                        <ReloadOutlined /> 重置
                    </a-button>
                </div>

                <!-- 右侧：结果展示 -->
                <div class="col-span-3 space-y-4">
                    <!-- Step 1 结果: 采集 -->
                    <div class="bg-white rounded-xl shadow-sm border p-5 transition-all"
                         :class="pipeline.stepResults.fetch ? 'border-green-200' : 'border-gray-100 opacity-50'">
                        <div class="flex items-center justify-between mb-3">
                            <h4 class="font-bold text-gray-700 flex items-center gap-2">
                                <CloudDownloadOutlined :class="pipeline.stepResults.fetch ? 'text-green-500' : 'text-gray-300'" />
                                采集结果
                            </h4>
                            <a-tag v-if="pipeline.stepResults.fetch" color="green">
                                {{ pipeline.stepResults.fetch.chars }} 字符 · {{ pipeline.stepResults.fetch.time }}ms
                            </a-tag>
                            <a-tag v-else color="default">等待采集</a-tag>
                        </div>
                        <pre v-if="pipeline.stepResults.fetch"
                             class="text-xs text-gray-600 bg-gray-50 rounded-lg p-3 max-h-[120px] overflow-auto whitespace-pre-wrap">{{ pipeline.stepResults.fetch.preview }}</pre>
                        <div v-else class="h-16 bg-gray-50 rounded-lg flex items-center justify-center text-gray-300 text-sm">
                            运行流水线后显示抓取的原始HTML
                        </div>
                    </div>

                    <!-- Step 2 结果: 清洗 -->
                    <div class="bg-white rounded-xl shadow-sm border p-5 transition-all"
                         :class="pipeline.stepResults.clean ? 'border-purple-200' : 'border-gray-100 opacity-50'">
                        <div class="flex items-center justify-between mb-3">
                            <h4 class="font-bold text-gray-700 flex items-center gap-2">
                                <ClearOutlined :class="pipeline.stepResults.clean ? 'text-purple-500' : 'text-gray-300'" />
                                清洗结果（真实处理）
                            </h4>
                            <div v-if="pipeline.stepResults.clean" class="flex items-center gap-2">
                                <a-tag color="purple">压缩 {{ pipeline.stepResults.clean.compressionRate }}</a-tag>
                                <a-tag color="green">{{ pipeline.stepResults.clean.chars }} 字符</a-tag>
                            </div>
                            <a-tag v-else color="default">等待清洗</a-tag>
                        </div>
                        <div v-if="pipeline.stepResults.clean" class="space-y-2">
                            <div class="text-xs text-gray-500">已应用规则：
                                <a-tag v-for="r in pipeline.stepResults.clean.appliedRules" :key="r" size="small" color="purple">{{ r }}</a-tag>
                            </div>
                            <pre class="text-xs text-gray-700 bg-purple-50 rounded-lg p-3 max-h-[120px] overflow-auto whitespace-pre-wrap">{{ pipeline.stepResults.clean.preview }}</pre>
                        </div>
                        <div v-else class="h-16 bg-gray-50 rounded-lg flex items-center justify-center text-gray-300 text-sm">
                            显示清洗后的纯文本（去除HTML、脱敏等）
                        </div>
                    </div>

                    <!-- Step 3 结果: 分块 -->
                    <div class="bg-white rounded-xl shadow-sm border p-5 transition-all"
                         :class="pipeline.stepResults.chunk ? 'border-cyan-200' : 'border-gray-100 opacity-50'">
                        <div class="flex items-center justify-between mb-3">
                            <h4 class="font-bold text-gray-700 flex items-center gap-2">
                                <SettingOutlined :class="pipeline.stepResults.chunk ? 'text-cyan-500' : 'text-gray-300'" />
                                分块结果（Chunking）
                            </h4>
                            <a-tag v-if="pipeline.stepResults.chunk" color="cyan">
                                共 {{ pipeline.stepResults.chunk.chunks.length }} 个 Chunk
                            </a-tag>
                            <a-tag v-else color="default">等待分块</a-tag>
                        </div>
                        <div v-if="pipeline.stepResults.chunk" class="space-y-2 max-h-[160px] overflow-auto">
                            <div v-for="(chunk, idx) in pipeline.stepResults.chunk.chunks.slice(0, 5)" :key="idx"
                                 class="text-xs p-2 bg-cyan-50 border border-cyan-100 rounded-lg">
                                <div class="text-cyan-600 font-bold mb-1">Chunk #{{ idx + 1 }} · {{ chunk.length }}字符</div>
                                <div class="text-gray-600 line-clamp-2">{{ chunk.substring(0, 120) }}...</div>
                            </div>
                            <div v-if="pipeline.stepResults.chunk.chunks.length > 5" class="text-xs text-gray-400 text-center py-1">
                                ... 还有 {{ pipeline.stepResults.chunk.chunks.length - 5 }} 个 Chunk
                            </div>
                        </div>
                        <div v-else class="h-16 bg-gray-50 rounded-lg flex items-center justify-center text-gray-300 text-sm">
                            按 {{ pipeline.chunkSize }} 字符分块，重叠 {{ pipeline.chunkOverlap }} 字符
                        </div>
                    </div>

                    <!-- Step 4: 入库 -->
                    <div class="bg-white rounded-xl shadow-sm border p-5 transition-all"
                         :class="pipeline.stepResults.index ? 'border-green-200 bg-green-50/30' : 'border-gray-100 opacity-50'">
                        <div class="flex items-center justify-between mb-3">
                            <h4 class="font-bold text-gray-700 flex items-center gap-2">
                                <BookOutlined :class="pipeline.stepResults.index ? 'text-green-500' : 'text-gray-300'" />
                                向量化入库
                            </h4>
                            <a-tag v-if="pipeline.stepResults.index?.real" color="green">✓ 已入库 ChromaDB</a-tag>
                            <a-tag v-else-if="pipeline.stepResults.index" color="orange">未选择知识库</a-tag>
                            <a-tag v-else color="default">等待入库</a-tag>
                        </div>
                        <div v-if="pipeline.stepResults.index" class="text-sm space-y-1">
                            <div class="text-green-700">✅ 共 {{ pipeline.stepResults.index.chunkCount }} 个 Chunk</div>
                            <div v-if="pipeline.stepResults.index.real" class="text-green-700">
                                ✅ 已真实存入 ChromaDB 向量数据库！前往知识库管理可查看 Chunks 和向量。
                            </div>
                            <div v-else class="text-orange-600 text-xs">
                                ⚠️ 未选择目标知识库，当前为演示。在步骤3中选择知识库后可真实入库。
                            </div>
                            <a-button type="primary" size="small" class="mt-3" @click="goToKnowledgeBase">
                                <BookOutlined /> 前往知识库管理查看
                            </a-button>
                        </div>
                        <div v-else class="h-16 bg-gray-50 rounded-lg flex items-center justify-center text-gray-300 text-sm">
                            Embedding 向量化 → 存入向量数据库
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 创建采集任务对话框 -->
        <a-modal v-model:open="state.createVisible" title="新建采集任务" width="600px" @ok="handleCreateTask">
            <a-form :model="state.createForm" layout="vertical" class="mt-4">
                <a-form-item label="任务名称" required>
                    <a-input v-model:value="state.createForm.name" placeholder="如：电力行业标准文档采集" />
                </a-form-item>
                <a-form-item label="采集方式">
                    <div class="grid grid-cols-3 gap-3">
                        <div v-for="opt in typeOptions" :key="opt.value"
                             class="border rounded-xl p-3 cursor-pointer text-center transition-all"
                             :class="state.createForm.type === opt.value ? 'border-blue-400 bg-blue-50 shadow-sm' : 'border-gray-200 hover:border-blue-200'"
                             @click="state.createForm.type = opt.value">
                            <div class="text-2xl mb-1">{{ opt.icon }}</div>
                            <div class="text-sm font-medium">{{ opt.label }}</div>
                            <div class="text-xs text-gray-400 mt-1">{{ opt.desc }}</div>
                        </div>
                    </div>
                </a-form-item>
                <a-form-item v-if="state.createForm.type === 'web'" label="采集URL（每行一个）" required>
                    <a-textarea v-model:value="state.createForm.urls" :rows="3"
                                placeholder="https://www.nea.gov.cn/policy/&#10;https://standards.cec.org.cn/" />
                </a-form-item>
                <div class="grid grid-cols-3 gap-3" v-if="state.createForm.type === 'web'">
                    <a-form-item label="爬取深度">
                        <a-input-number v-model:value="state.createForm.depth" :min="1" :max="5" style="width:100%" />
                    </a-form-item>
                    <a-form-item label="最大页数">
                        <a-input-number v-model:value="state.createForm.maxPages" :min="1" :max="500" style="width:100%" />
                    </a-form-item>
                    <a-form-item label="定时调度">
                        <a-select v-model:value="state.createForm.schedule">
                            <a-select-option v-for="s in scheduleOptions" :key="s.value" :value="s.value">{{ s.label }}</a-select-option>
                        </a-select>
                    </a-form-item>
                </div>
                <a-form-item v-if="state.createForm.type === 'upload'" label="上传文件">
                    <a-upload-dragger name="file" :multiple="true" action="#" :beforeUpload="() => false">
                        <p class="text-3xl text-blue-400 mb-2"><UploadOutlined /></p>
                        <p class="text-gray-600 text-sm">点击或拖拽文件到此区域</p>
                        <p class="text-gray-400 text-xs mt-1">支持 PDF、Word、Markdown、TXT、CSV</p>
                    </a-upload-dragger>
                </a-form-item>
                <a-form-item label="目标知识库（可选，关联RAG知识库）">
                    <a-select v-model:value="state.createForm.targetKb" placeholder="选择知识库（采集完成后自动入库）" allowClear>
                        <a-select-option v-for="kb in state.knowledgeBases" :key="kb.id" :value="kb.name">
                            <DatabaseOutlined /> {{ kb.name }} ({{ kb.documentCount }}篇)
                        </a-select-option>
                    </a-select>
                </a-form-item>
                <div class="grid grid-cols-2 gap-3">
                    <a-form-item>
                        <a-checkbox v-model:checked="state.createForm.autoClean">采集后自动清洗</a-checkbox>
                    </a-form-item>
                    <a-form-item>
                        <a-checkbox v-model:checked="state.createForm.autoIndex">清洗后自动向量化索引</a-checkbox>
                    </a-form-item>
                </div>
                <div class="bg-blue-50 rounded-lg p-3 text-xs text-blue-700">
                    <InfoCircleOutlined /> 完整流程：数据采集 → 格式解析 → 清洗去重脱敏 → 分级分类 → Chunk切割 → 向量化 → 入库知识库
                </div>
            </a-form>
        </a-modal>

        <!-- 清洗执行对话框 -->
        <a-modal v-model:open="state.cleanDialogVisible" title="执行数据清洗" width="900px" :footer="null" :maskClosable="false">
            <div class="mt-4">
                <!-- 输入模式切换 -->
                <div class="mb-4">
                    <div class="flex items-center gap-4 mb-3">
                        <a-radio-group v-model:value="state.cleanMode" button-style="solid" :disabled="state.cleanRunning">
                            <a-radio-button value="text">文本输入</a-radio-button>
                            <a-radio-button value="file">文档上传</a-radio-button>
                        </a-radio-group>
                        <span class="text-xs text-gray-400">支持粘贴文本或上传 PDF/Word/TXT 文档进行清洗</span>
                    </div>
                    <div v-if="state.cleanMode === 'text'">
                        <a-textarea v-model:value="state.cleanInputText" :rows="6" placeholder="请粘贴需要清洗的文本..." :disabled="state.cleanRunning" />
                    </div>
                    <div v-else>
                        <a-upload-dragger name="file" :fileList="state.cleanFileList" :multiple="false"
                                          :beforeUpload="(f) => { state.cleanFileList = [f]; return false; }"
                                          @remove="() => state.cleanFileList = []"
                                          :disabled="state.cleanRunning">
                            <p class="text-3xl text-blue-400 mb-2"><UploadOutlined /></p>
                            <p class="text-gray-600 text-sm">点击或拖拽文档到此区域</p>
                            <p class="text-gray-400 text-xs mt-1">支持 PDF、Word、Markdown、TXT 格式</p>
                        </a-upload-dragger>
                    </div>
                </div>

                <!-- 执行按钮 -->
                <div class="mb-4 flex items-center gap-3">
                    <a-button type="primary" @click="executeClean" :loading="state.cleanRunning" :disabled="state.cleanRunning">
                        <ThunderboltOutlined /> {{ state.cleanRunning ? '清洗中...' : '开始清洗' }}
                    </a-button>
                    <span class="text-xs text-gray-400">将按顺序执行所有已启用的清洗规则</span>
                </div>

                <!-- 执行过程 -->
                <div v-if="state.cleanSteps.length > 0" class="mb-4 border rounded-xl p-4 bg-gray-50">
                    <div class="text-sm font-bold text-gray-700 mb-3">清洗执行过程：</div>
                    <div class="space-y-2">
                        <div v-for="(step, idx) in state.cleanSteps" :key="step.id" class="flex items-center gap-3 py-1">
                            <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold text-white"
                                 :class="step.status === 'done' ? 'bg-green-500' : step.status === 'running' ? 'bg-blue-500 animate-pulse' : 'bg-gray-300'">
                                {{ step.status === 'done' ? '✓' : idx + 1 }}
                            </div>
                            <div class="flex-1">
                                <span class="text-sm" :class="step.status === 'running' ? 'text-blue-600 font-bold' : step.status === 'done' ? 'text-green-700' : 'text-gray-400'">
                                    {{ step.name }}
                                </span>
                                <span v-if="step.status === 'running'" class="ml-2 text-xs text-blue-400">
                                    <SyncOutlined spin /> 执行中...
                                </span>
                            </div>
                            <span v-if="step.detail" class="text-xs text-gray-500">{{ step.detail }}</span>
                        </div>
                    </div>
                </div>

                <!-- 清洗结果 -->
                <div v-if="state.cleanResult" class="border rounded-xl overflow-hidden">
                    <div class="bg-green-50 px-4 py-3 border-b border-green-100 flex items-center justify-between">
                        <span class="font-bold text-green-800 flex items-center gap-2">
                            <CheckCircleOutlined /> 清洗完成
                        </span>
                        <div class="flex items-center gap-4 text-xs">
                            <span class="text-gray-500">原始长度: <b class="text-gray-800">{{ state.cleanResult.originalLength }}</b> 字符</span>
                            <span class="text-gray-500">清洗后: <b class="text-green-600">{{ state.cleanResult.cleanedLength }}</b> 字符</span>
                            <a-tag color="green">压缩率 {{ state.cleanResult.compressionRate }}</a-tag>
                        </div>
                    </div>
                    <div class="grid grid-cols-2 divide-x">
                        <div class="p-4">
                            <div class="text-xs font-bold text-red-500 mb-2">清洗前（原始文本）</div>
                            <pre class="text-xs text-gray-600 bg-red-50 rounded-lg p-3 max-h-[200px] overflow-auto whitespace-pre-wrap">{{ state.cleanResult.originalText }}</pre>
                        </div>
                        <div class="p-4">
                            <div class="text-xs font-bold text-green-600 mb-2">清洗后（结果文本）</div>
                            <pre class="text-xs text-gray-600 bg-green-50 rounded-lg p-3 max-h-[200px] overflow-auto whitespace-pre-wrap">{{ state.cleanResult.cleanedText }}</pre>
                        </div>
                    </div>
                </div>
            </div>
        </a-modal>
    </div>
</template>
