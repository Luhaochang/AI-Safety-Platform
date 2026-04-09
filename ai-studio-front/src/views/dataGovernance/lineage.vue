<script setup>
import {
    ApartmentOutlined,
    NodeIndexOutlined,
    ZoomInOutlined,
    ZoomOutOutlined,
    FullscreenOutlined,
    ReloadOutlined,
    InfoCircleOutlined,
    DatabaseOutlined,
    TagsOutlined,
    SafetyCertificateOutlined
} from '@ant-design/icons-vue';
import { getDataLineage, getDataLineageMock } from '@/api/dataGovernance/classification.js';
import * as d3 from 'd3';

const svgRef = ref(null);
const containerRef = ref(null);
const state = reactive({
    loading: false,
    lineageData: { nodes: [], edges: [] },
    selectedNode: null,
    zoomLevel: 1,
    filterLayer: '',
    filterGrade: ''
});

const layerOrder = ['原始数据', '清洗数据', '标注数据', '特征数据', '模型产物', '推理服务', '业务应用'];
const layerColorMap = { '原始数据': '#1677ff', '清洗数据': '#52c41a', '标注数据': '#faad14', '特征数据': '#722ed1', '模型产物': '#eb2f96', '推理服务': '#13c2c2', '业务应用': '#fa8c16' };
const gradeColors = { '公开级': '#52c41a', '重要级': '#faad14', '核心级': '#ff4d4f' };

const nodeTypeIcons = {
    '设备数据类': '🔌', '运行数据类': '⚡', '调度指令类': '📋', '历史案例类': '📚',
    '专家知识类': '🧠', '规程文档类': '📖', '用户信息类': '👤',
    '原始数据': '📦', '清洗数据': '🧹', '标注数据': '🏷️', '特征数据': '🔢',
    '模型产物': '🤖', '推理服务': '🚀', '业务应用': '⚡'
};

const loadData = async () => {
    state.loading = true;
    try {
        let res = await getDataLineage().catch(() => null);
        if (!res || res.code !== 200 || !res.data?.nodes?.length) {
            res = await getDataLineageMock();
        }
        if (res.code === 200) {
            state.lineageData = res.data;
            nextTick(() => renderGraph());
        }
    } finally {
        state.loading = false;
    }
};

const renderGraph = () => {
    if (!svgRef.value || !containerRef.value) return;

    const container = containerRef.value;
    const width = container.clientWidth;
    const height = container.clientHeight || 600;

    d3.select(svgRef.value).selectAll('*').remove();

    const svg = d3.select(svgRef.value)
        .attr('width', width)
        .attr('height', height);

    const g = svg.append('g');

    // Zoom behavior
    const zoom = d3.zoom()
        .scaleExtent([0.3, 3])
        .on('zoom', (event) => {
            g.attr('transform', event.transform);
            state.zoomLevel = event.transform.k;
        });
    svg.call(zoom);

    const { nodes, edges } = state.lineageData;

    // Filter
    let filteredNodes = nodes;
    if (state.filterLayer) {
        filteredNodes = nodes.filter(n => n.layer === state.filterLayer);
    }
    if (state.filterGrade) {
        filteredNodes = filteredNodes.filter(n => n.grade === state.filterGrade);
    }
    const filteredIds = new Set(filteredNodes.map(n => n.id));
    const filteredEdges = edges.filter(e => filteredIds.has(e.source) && filteredIds.has(e.target));

    // Layout: position nodes by layer
    const layerGroups = {};
    filteredNodes.forEach(n => {
        if (!layerGroups[n.layer]) layerGroups[n.layer] = [];
        layerGroups[n.layer].push(n);
    });

    const nodeWidth = 180;
    const nodeHeight = 60;
    const layerGap = 220;
    const nodeGap = 90;
    const offsetX = 80;
    const offsetY = 60;

    const nodePositions = {};
    const sortedLayers = Object.keys(layerGroups).sort((a, b) => layerOrder.indexOf(a) - layerOrder.indexOf(b));
    sortedLayers.forEach((layer, layerIdx) => {
        const group = layerGroups[layer];
        const totalHeight = group.length * (nodeHeight + nodeGap) - nodeGap;
        const startY = (height - totalHeight) / 2;
        group.forEach((node, i) => {
            nodePositions[node.id] = {
                x: offsetX + layerIdx * layerGap,
                y: Math.max(offsetY, startY + i * (nodeHeight + nodeGap))
            };
        });
    });

    // Arrow marker
    svg.append('defs').append('marker')
        .attr('id', 'arrowhead')
        .attr('viewBox', '0 -5 10 10')
        .attr('refX', 10).attr('refY', 0)
        .attr('markerWidth', 8).attr('markerHeight', 8)
        .attr('orient', 'auto')
        .append('path').attr('d', 'M0,-5L10,0L0,5')
        .attr('fill', '#999');

    // Edges
    g.selectAll('.edge')
        .data(filteredEdges)
        .enter().append('path')
        .attr('class', 'edge')
        .attr('d', d => {
            const s = nodePositions[d.source];
            const t = nodePositions[d.target];
            if (!s || !t) return '';
            const sx = s.x + nodeWidth;
            const sy = s.y + nodeHeight / 2;
            const tx = t.x;
            const ty = t.y + nodeHeight / 2;
            const mx = (sx + tx) / 2;
            return `M${sx},${sy} C${mx},${sy} ${mx},${ty} ${tx},${ty}`;
        })
        .attr('fill', 'none')
        .attr('stroke', '#c0c0c0')
        .attr('stroke-width', 2)
        .attr('marker-end', 'url(#arrowhead)')
        .attr('opacity', 0.7);

    // Edge labels
    g.selectAll('.edge-label')
        .data(filteredEdges)
        .enter().append('text')
        .attr('class', 'edge-label')
        .attr('x', d => {
            const s = nodePositions[d.source];
            const t = nodePositions[d.target];
            if (!s || !t) return 0;
            return (s.x + nodeWidth + t.x) / 2;
        })
        .attr('y', d => {
            const s = nodePositions[d.source];
            const t = nodePositions[d.target];
            if (!s || !t) return 0;
            return (s.y + t.y) / 2 + nodeHeight / 2 - 5;
        })
        .attr('text-anchor', 'middle')
        .attr('font-size', '11px')
        .attr('fill', '#888')
        .text(d => d.label);

    // Node groups
    const nodeGroup = g.selectAll('.node')
        .data(filteredNodes)
        .enter().append('g')
        .attr('class', 'node')
        .attr('transform', d => `translate(${nodePositions[d.id]?.x || 0},${nodePositions[d.id]?.y || 0})`)
        .style('cursor', 'pointer')
        .on('click', (event, d) => { state.selectedNode = d; });

    // Node rect
    nodeGroup.append('rect')
        .attr('width', nodeWidth)
        .attr('height', nodeHeight)
        .attr('rx', 10).attr('ry', 10)
        .attr('fill', 'white')
        .attr('stroke', d => layerColorMap[d.layer] || '#999')
        .attr('stroke-width', 2)
        .attr('filter', 'drop-shadow(0 2px 4px rgba(0,0,0,0.1))');

    // Color strip at top
    nodeGroup.append('rect')
        .attr('width', nodeWidth)
        .attr('height', 4)
        .attr('rx', 10).attr('ry', 10)
        .attr('fill', d => layerColorMap[d.layer] || '#999');

    // Node icon
    nodeGroup.append('text')
        .attr('x', 12).attr('y', 35)
        .attr('font-size', '18px')
        .text(d => nodeTypeIcons[d.type] || '📄');

    // Node name
    nodeGroup.append('text')
        .attr('x', 38).attr('y', 30)
        .attr('font-size', '12px')
        .attr('font-weight', '600')
        .attr('fill', '#333')
        .text(d => d.name.length > 10 ? d.name.substring(0, 10) + '...' : d.name);

    // Node level badge
    nodeGroup.append('text')
        .attr('x', 38).attr('y', 48)
        .attr('font-size', '10px')
        .attr('fill', d => gradeColors[d.grade] || '#999')
        .text(d => `🔒 ${d.grade}`);

    // Fit to view
    svg.call(zoom.transform, d3.zoomIdentity.translate(20, 0).scale(0.85));
};

const handleZoom = (direction) => {
    const svg = d3.select(svgRef.value);
    const zoom = d3.zoom().scaleExtent([0.3, 3]);
    if (direction === 'in') {
        svg.transition().call(zoom.scaleBy, 1.3);
    } else {
        svg.transition().call(zoom.scaleBy, 0.7);
    }
};

const handleReset = () => {
    state.filterLayer = '';
    state.filterGrade = '';
    state.selectedNode = null;
    loadData();
};

watch(() => [state.filterLayer, state.filterGrade], () => {
    nextTick(() => renderGraph());
});

onMounted(() => {
    loadData();
});
</script>

<template>
    <div>
        <!-- 工具栏 -->
        <div class="flex items-center justify-between mb-4">
            <div class="text-base font-bold text-gray-700 flex items-center gap-2">
                <ApartmentOutlined class="text-blue-500" />
                数据血缘追踪
                <span class="text-sm text-gray-400 font-normal ml-2">可视化展示数据从采集到推理的全链路流转关系</span>
            </div>
            <div class="flex items-center gap-2">
                <a-select v-model:value="state.filterLayer" style="width: 140px" placeholder="数据层级">
                    <a-select-option value="">所有层级</a-select-option>
                    <a-select-option v-for="name in layerOrder" :key="name" :value="name">{{ name }}</a-select-option>
                </a-select>
                <a-select v-model:value="state.filterGrade" style="width: 120px" placeholder="数据分级" allowClear>
                    <a-select-option value="公开级">公开级</a-select-option>
                    <a-select-option value="重要级">重要级</a-select-option>
                    <a-select-option value="核心级">核心级</a-select-option>
                </a-select>
                <a-button @click="handleReset"><ReloadOutlined /> 重置</a-button>
            </div>
        </div>

        <div class="flex gap-6">
            <!-- 血缘图主区域 -->
            <div class="flex-1 bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden" ref="containerRef">
                <!-- 图例 -->
                <div class="p-4 border-b border-gray-100 flex items-center justify-between">
                    <div class="flex items-center gap-4">
                        <span class="text-sm text-gray-500">图例:</span>
                        <div v-for="name in layerOrder" :key="name" class="flex items-center gap-1">
                            <div class="w-3 h-3 rounded-full" :style="{ backgroundColor: layerColorMap[name] }"></div>
                            <span class="text-xs text-gray-600">{{ name }}</span>
                        </div>
                    </div>
                    <div class="flex items-center gap-1">
                        <a-button size="small" @click="handleZoom('in')"><ZoomInOutlined /></a-button>
                        <span class="text-xs text-gray-400 w-12 text-center">{{ Math.round(state.zoomLevel * 100) }}%</span>
                        <a-button size="small" @click="handleZoom('out')"><ZoomOutOutlined /></a-button>
                    </div>
                </div>
                <div style="height: 600px; position: relative;">
                    <svg ref="svgRef" style="width: 100%; height: 100%;"></svg>
                    <div v-if="state.loading" class="absolute inset-0 flex items-center justify-center bg-white/80">
                        <a-spin size="large" tip="加载中..." />
                    </div>
                </div>
            </div>

            <!-- 节点详情侧栏 -->
            <div class="w-72 bg-white rounded-xl shadow-sm border border-gray-100 p-5 self-start" v-if="state.selectedNode">
                <div class="flex items-center justify-between mb-4">
                    <h3 class="font-bold text-gray-800 flex items-center gap-2">
                        <InfoCircleOutlined class="text-blue-500" /> 节点详情
                    </h3>
                    <a-button type="text" size="small" @click="state.selectedNode = null">✕</a-button>
                </div>
                <a-descriptions :column="1" size="small" bordered>
                    <a-descriptions-item label="名称">{{ state.selectedNode.name }}</a-descriptions-item>
                    <a-descriptions-item label="类型">
                        <a-tag>{{ state.selectedNode.type }}</a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="层级">
                        <a-tag :color="layerColorMap[state.selectedNode.layer]">
                            {{ state.selectedNode.layer }}
                        </a-tag>
                    </a-descriptions-item>
                    <a-descriptions-item label="数据分级">
                        <a-tag :color="gradeColors[state.selectedNode.grade]">
                            {{ state.selectedNode.grade }}
                        </a-tag>
                    </a-descriptions-item>
                </a-descriptions>

                <div class="mt-4">
                    <div class="text-sm font-medium text-gray-700 mb-2">上游依赖</div>
                    <div v-for="edge in state.lineageData.edges.filter(e => e.target === state.selectedNode.id)" :key="edge.source"
                         class="bg-gray-50 rounded-lg px-3 py-2 mb-2 text-sm flex items-center gap-2">
                        <NodeIndexOutlined class="text-blue-400" />
                        {{ state.lineageData.nodes.find(n => n.id === edge.source)?.name }}
                        <span class="text-gray-400 text-xs ml-auto">{{ edge.label }}</span>
                    </div>
                    <div class="text-sm font-medium text-gray-700 mb-2 mt-3">下游产出</div>
                    <div v-for="edge in state.lineageData.edges.filter(e => e.source === state.selectedNode.id)" :key="edge.target"
                         class="bg-gray-50 rounded-lg px-3 py-2 mb-2 text-sm flex items-center gap-2">
                        <NodeIndexOutlined class="text-green-400" />
                        {{ state.lineageData.nodes.find(n => n.id === edge.target)?.name }}
                        <span class="text-gray-400 text-xs ml-auto">{{ edge.label }}</span>
                    </div>
                </div>
            </div>

            <!-- 空状态提示 -->
            <div class="w-72 bg-white rounded-xl shadow-sm border border-gray-100 p-5 self-start flex flex-col items-center justify-center" v-else style="min-height: 200px;">
                <ApartmentOutlined class="text-4xl text-gray-300 mb-3" />
                <div class="text-gray-400 text-sm text-center">点击图中节点<br/>查看详细信息</div>
            </div>
        </div>
    </div>
</template>
