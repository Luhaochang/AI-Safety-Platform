<script setup>
import { TresCanvas, useRenderLoop } from '@tresjs/core';
import { OrbitControls, Stars } from '@tresjs/cientos';
import {
    CloudServerOutlined, WarningOutlined, CheckCircleOutlined, CloseCircleOutlined,
    ReloadOutlined, InfoCircleOutlined, AlertOutlined, DatabaseOutlined,
    AppstoreOutlined, RobotOutlined, LinkOutlined, ExpandOutlined
} from '@ant-design/icons-vue';
import { getClusterRealtime, getAlerts, getClusterRealtimeMock, getAlertsMock } from '@/api/digitalTwin/index.js';
import { useRouter } from 'vue-router';

const router = useRouter();
let refreshTimer = null;

const state = reactive({
    loading: false,
    nodes: [],
    alerts: [],
    clusterSummary: { totalNodes: 0, onlineNodes: 0, totalGPU: 0, runningTasks: 0, avgGpuUsage: 0, avgCpuUsage: 0 },
    layerStats: [
        { key: 'infra', name: '算力资源', count: 6, unit: '节点', color: '#4299e1', route: null },
        { key: 'data', name: '数据资产', count: 48, unit: '资产', color: '#48bb78', route: '/datamag/asset-details' },
        { key: 'model', name: '模型资产', count: 12, unit: '模型', color: '#9f7aea', route: '/modelmag/model' },
        { key: 'knowledge', name: '知识资产', count: 4, unit: '知识库', color: '#ed8936', route: '/rag-scheduler/knowledge' },
        { key: 'service', name: '推理服务', count: 8, unit: '服务', color: '#38b2ac', route: '/modelmag/service-list' },
        { key: 'app', name: '业务应用', count: 3, unit: '应用', color: '#ed64a6', route: '/rag-scheduler/chat' }
    ],
    panelCollapsed: false
});

// ====== 3D Scene Refs ======
const sphereRef = ref(null);
const ringRef = ref(null);
const ring2Ref = ref(null);

// ====== 3D Equipment Data ======
const serverRacks = [
    { id: 'r1', zOff: -4, ledColor: '#48bb78' },
    { id: 'r2', zOff: -1.5, ledColor: '#48bb78' },
    { id: 'r3', zOff: 1, ledColor: '#fc8181' },
    { id: 'r4', zOff: 3.5, ledColor: '#48bb78' }
];

// ====== Data Loading ======
const loadData = async () => {
    state.loading = true;
    try {
        const [clusterRes, alertRes] = await Promise.all([
            getClusterRealtime().catch(() => getClusterRealtimeMock()),
            getAlerts().catch(() => getAlertsMock())
        ]);
        if (clusterRes.code === 200) {
            state.nodes = clusterRes.data.nodes;
            const online = state.nodes.filter(n => n.status !== 'offline');
            state.clusterSummary = {
                totalNodes: state.nodes.length,
                onlineNodes: online.length,
                totalGPU: state.nodes.reduce((s, n) => s + n.gpu.count, 0),
                runningTasks: state.nodes.reduce((s, n) => s + n.tasks.length, 0),
                avgGpuUsage: Math.round(online.reduce((s, n) => s + n.gpu.usage, 0) / Math.max(1, online.length)),
                avgCpuUsage: Math.round(online.reduce((s, n) => s + n.cpu.usage, 0) / Math.max(1, online.length))
            };
        }
        if (alertRes.code === 200) state.alerts = alertRes.data;
    } finally { state.loading = false; }
};

// ====== Animation Loop ======
const { onLoop } = useRenderLoop();
const elapsed = ref(0);

onLoop(({ delta }) => {
    elapsed.value += delta;
    if (sphereRef.value) {
        sphereRef.value.rotation.y += delta * 0.25;
        sphereRef.value.rotation.x = Math.sin(elapsed.value * 0.15) * 0.08;
    }
    if (ringRef.value) ringRef.value.rotation.z += delta * 0.35;
    if (ring2Ref.value) ring2Ref.value.rotation.x += delta * 0.25;
});

// ====== Navigation ======
const navigateTo = (route) => { if (route) router.push(route); };
const alertLevelColor = (level) => ({ critical: '#ff4d4f', warning: '#faad14' }[level] || '#1677ff');

onMounted(() => { loadData(); refreshTimer = setInterval(loadData, 8000); });
onBeforeUnmount(() => { if (refreshTimer) clearInterval(refreshTimer); });
</script>

<template>
    <div class="relative w-full h-[calc(100vh-84px)] bg-[#050810] overflow-hidden">
        <!-- ====== 3D Scene ====== -->
        <TresCanvas clear-color="#050810" shadows>
            <TresPerspectiveCamera :position="[20, 14, 20]" :fov="45" />
            <OrbitControls :enable-damping="true" :damping-factor="0.05"
                           :max-polar-angle="Math.PI / 2.15" :min-distance="8" :max-distance="45" />

            <!-- Lighting -->
            <TresAmbientLight :intensity="0.2" color="#b0c4de" />
            <TresDirectionalLight :position="[15, 20, 10]" :intensity="0.7" color="#ffffff" cast-shadow />

            <!-- Stars -->
            <Stars :radius="80" :depth="60" :count="4000" :size="0.4" />

            <!-- Ground -->
            <TresMesh :rotation-x="-Math.PI/2" :position-y="-0.02" receive-shadow>
                <TresPlaneGeometry :args="[50, 50]" />
                <TresMeshStandardMaterial color="#080c18" :metalness="0.95" :roughness="0.05" />
            </TresMesh>
            <TresGridHelper :args="[50, 50, '#111d2e', '#0a1220']" />

            <!-- ====== Zone: Computing Center (Left) ====== -->
            <TresGroup :position-x="-8">
                <!-- Platform -->
                <TresMesh :position-y="0.05" receive-shadow>
                    <TresBoxGeometry :args="[4.5, 0.1, 12]" />
                    <TresMeshStandardMaterial color="#0c1425" :metalness="0.8" :roughness="0.2" />
                </TresMesh>

                <!-- Server Racks -->
                <TresGroup v-for="rack in serverRacks" :key="rack.id" :position-z="rack.zOff">
                    <!-- Body -->
                    <TresMesh :position-y="1.5" cast-shadow>
                        <TresBoxGeometry :args="[1.2, 3, 0.8]" />
                        <TresMeshStandardMaterial color="#1a2538" :metalness="0.7" :roughness="0.3" />
                    </TresMesh>
                    <!-- Front Panel -->
                    <TresMesh :position="[0.61, 1.5, 0]">
                        <TresBoxGeometry :args="[0.02, 2.8, 0.7]" />
                        <TresMeshStandardMaterial color="#0d1520" />
                    </TresMesh>
                    <!-- LED Strip -->
                    <TresMesh :position="[0.62, 1.5, 0.28]">
                        <TresBoxGeometry :args="[0.02, 2.5, 0.04]" />
                        <TresMeshStandardMaterial :color="rack.ledColor" :emissive="rack.ledColor" :emissive-intensity="3" />
                    </TresMesh>
                    <!-- Status LED -->
                    <TresMesh :position="[0.62, 2.8, -0.2]">
                        <TresSphereGeometry :args="[0.05, 8, 8]" />
                        <TresMeshStandardMaterial :color="rack.ledColor" :emissive="rack.ledColor" :emissive-intensity="5" />
                    </TresMesh>
                    <!-- Vent Slots -->
                    <TresMesh v-for="s in 5" :key="'v'+s" :position="[0.62, 0.4 + s * 0.45, 0]">
                        <TresBoxGeometry :args="[0.01, 0.02, 0.5]" />
                        <TresMeshStandardMaterial color="#1a365d" :emissive="'#1a365d'" :emissive-intensity="0.5" />
                    </TresMesh>
                </TresGroup>

                <!-- Zone Label Pillar -->
                <TresMesh :position="[0, 3.3, -5.5]">
                    <TresBoxGeometry :args="[2.5, 0.4, 0.05]" />
                    <TresMeshStandardMaterial color="#0a1628" :emissive="'#4299e1'" :emissive-intensity="1" />
                </TresMesh>
                <TresPointLight :position="[0, 4, 0]" color="#4299e1" :intensity="6" :distance="10" />
            </TresGroup>

            <!-- ====== Zone: Central Data Hub ====== -->
            <TresGroup>
                <!-- Hex Platform -->
                <TresMesh :position-y="0.1" receive-shadow>
                    <TresCylinderGeometry :args="[3.5, 4, 0.2, 6]" />
                    <TresMeshStandardMaterial color="#0c1425" :metalness="0.9" :roughness="0.1" />
                </TresMesh>

                <!-- Wireframe Icosphere -->
                <TresMesh ref="sphereRef" :position-y="3.8">
                    <TresIcosahedronGeometry :args="[2, 1]" />
                    <TresMeshStandardMaterial color="#1a365d" :emissive="'#4299e1'" :emissive-intensity="0.6"
                                              :wireframe="true" :transparent="true" :opacity="0.85" />
                </TresMesh>
                <!-- Inner Core -->
                <TresMesh :position-y="3.8">
                    <TresSphereGeometry :args="[1, 20, 20]" />
                    <TresMeshStandardMaterial :emissive="'#63b3ed'" :emissive-intensity="2.5"
                                              :transparent="true" :opacity="0.5" color="#0a1628" />
                </TresMesh>
                <!-- Orbit Ring 1 -->
                <TresMesh ref="ringRef" :position-y="3.8" :rotation-x="1.05">
                    <TresTorusGeometry :args="[3, 0.03, 16, 120]" />
                    <TresMeshStandardMaterial :emissive="'#4fc3f7'" :emissive-intensity="4" color="#4fc3f7" />
                </TresMesh>
                <!-- Orbit Ring 2 -->
                <TresMesh ref="ring2Ref" :position-y="3.8" :rotation="[0.5, 0.8, 0]">
                    <TresTorusGeometry :args="[2.6, 0.02, 16, 100]" />
                    <TresMeshStandardMaterial :emissive="'#a78bfa'" :emissive-intensity="4" color="#a78bfa" />
                </TresMesh>
                <!-- Support Pillar -->
                <TresMesh :position-y="1.2">
                    <TresCylinderGeometry :args="[0.18, 0.35, 2.4, 8]" />
                    <TresMeshStandardMaterial color="#2d3748" :metalness="0.8" :roughness="0.2" />
                </TresMesh>

                <TresPointLight :position-y="3.8" color="#63b3ed" :intensity="10" :distance="14" />
            </TresGroup>

            <!-- ====== Zone: Transformers (Right) ====== -->
            <TresGroup :position-x="8">
                <!-- Platform -->
                <TresMesh :position-y="0.05" receive-shadow>
                    <TresBoxGeometry :args="[4.5, 0.1, 8]" />
                    <TresMeshStandardMaterial color="#140c08" :metalness="0.8" :roughness="0.2" />
                </TresMesh>

                <!-- Transformer 1 -->
                <TresGroup :position-z="-2">
                    <TresMesh :position-y="1.3" cast-shadow>
                        <TresCylinderGeometry :args="[0.85, 0.95, 2.6, 16]" />
                        <TresMeshStandardMaterial color="#4a3728" :metalness="0.6" :roughness="0.4" />
                    </TresMesh>
                    <TresMesh :position-y="2.8">
                        <TresSphereGeometry :args="[0.65, 16, 8, 0, 6.28, 0, 1.57]" />
                        <TresMeshStandardMaterial color="#5a4738" :metalness="0.7" :roughness="0.3" />
                    </TresMesh>
                    <!-- Cooling Fins -->
                    <TresMesh v-for="f in 8" :key="'f1-'+f"
                              :position="[Math.cos(f*0.785)*1, 1.3, Math.sin(f*0.785)*1]"
                              :rotation-y="-f*0.785">
                        <TresBoxGeometry :args="[0.03, 2, 0.3]" />
                        <TresMeshStandardMaterial color="#6b5b4d" :metalness="0.5" :roughness="0.5" />
                    </TresMesh>
                    <!-- Bushings -->
                    <TresMesh v-for="t in 3" :key="'b1-'+t" :position="[(t-2)*0.4, 3.3, 0]">
                        <TresCylinderGeometry :args="[0.06, 0.06, 1, 8]" />
                        <TresMeshStandardMaterial color="#dd6b20" :emissive="'#ed8936'" :emissive-intensity="1.5" />
                    </TresMesh>
                </TresGroup>

                <!-- Transformer 2 -->
                <TresGroup :position-z="2.5">
                    <TresMesh :position-y="1.3" cast-shadow>
                        <TresCylinderGeometry :args="[0.85, 0.95, 2.6, 16]" />
                        <TresMeshStandardMaterial color="#4a3728" :metalness="0.6" :roughness="0.4" />
                    </TresMesh>
                    <TresMesh :position-y="2.8">
                        <TresSphereGeometry :args="[0.65, 16, 8, 0, 6.28, 0, 1.57]" />
                        <TresMeshStandardMaterial color="#5a4738" :metalness="0.7" :roughness="0.3" />
                    </TresMesh>
                    <TresMesh v-for="f in 8" :key="'f2-'+f"
                              :position="[Math.cos(f*0.785)*1, 1.3, Math.sin(f*0.785)*1]"
                              :rotation-y="-f*0.785">
                        <TresBoxGeometry :args="[0.03, 2, 0.3]" />
                        <TresMeshStandardMaterial color="#6b5b4d" :metalness="0.5" :roughness="0.5" />
                    </TresMesh>
                    <TresMesh v-for="t in 3" :key="'b2-'+t" :position="[(t-2)*0.4, 3.3, 0]">
                        <TresCylinderGeometry :args="[0.06, 0.06, 1, 8]" />
                        <TresMeshStandardMaterial color="#dd6b20" :emissive="'#ed8936'" :emissive-intensity="1.5" />
                    </TresMesh>
                </TresGroup>

                <TresPointLight :position="[0, 4, 0]" color="#ed8936" :intensity="6" :distance="10" />
            </TresGroup>

            <!-- ====== Zone: Display Screens (Front) ====== -->
            <TresGroup :position-z="-9">
                <!-- Screen: Data Dashboard -->
                <TresGroup :position-x="-4.5">
                    <TresMesh :position-y="2.2" cast-shadow>
                        <TresBoxGeometry :args="[2.8, 2, 0.08]" />
                        <TresMeshStandardMaterial color="#0a1628" :emissive="'#1a365d'" :emissive-intensity="0.3" />
                    </TresMesh>
                    <TresMesh :position="[0, 2.2, 0.05]">
                        <TresPlaneGeometry :args="[2.6, 1.8]" />
                        <TresMeshStandardMaterial :emissive="'#2b6cb0'" :emissive-intensity="1.8"
                                                  :transparent="true" :opacity="0.35" color="#1a365d" />
                    </TresMesh>
                    <TresMesh :position-y="0.6">
                        <TresCylinderGeometry :args="[0.08, 0.15, 1.2, 8]" />
                        <TresMeshStandardMaterial color="#2d3748" :metalness="0.8" />
                    </TresMesh>
                </TresGroup>
                <!-- Screen: Model Monitor -->
                <TresGroup>
                    <TresMesh :position-y="2.5" cast-shadow>
                        <TresBoxGeometry :args="[3.2, 2.2, 0.08]" />
                        <TresMeshStandardMaterial color="#0a1628" :emissive="'#44337a'" :emissive-intensity="0.3" />
                    </TresMesh>
                    <TresMesh :position="[0, 2.5, 0.05]">
                        <TresPlaneGeometry :args="[3, 2]" />
                        <TresMeshStandardMaterial :emissive="'#6b46c1'" :emissive-intensity="1.8"
                                                  :transparent="true" :opacity="0.35" color="#322659" />
                    </TresMesh>
                    <TresMesh :position-y="0.7">
                        <TresCylinderGeometry :args="[0.1, 0.2, 1.4, 8]" />
                        <TresMeshStandardMaterial color="#2d3748" :metalness="0.8" />
                    </TresMesh>
                </TresGroup>
                <!-- Screen: RAG -->
                <TresGroup :position-x="4.5">
                    <TresMesh :position-y="2.2" cast-shadow>
                        <TresBoxGeometry :args="[2.8, 2, 0.08]" />
                        <TresMeshStandardMaterial color="#0a1628" :emissive="'#744210'" :emissive-intensity="0.3" />
                    </TresMesh>
                    <TresMesh :position="[0, 2.2, 0.05]">
                        <TresPlaneGeometry :args="[2.6, 1.8]" />
                        <TresMeshStandardMaterial :emissive="'#c05621'" :emissive-intensity="1.8"
                                                  :transparent="true" :opacity="0.35" color="#744210" />
                    </TresMesh>
                    <TresMesh :position-y="0.6">
                        <TresCylinderGeometry :args="[0.08, 0.15, 1.2, 8]" />
                        <TresMeshStandardMaterial color="#2d3748" :metalness="0.8" />
                    </TresMesh>
                </TresGroup>
            </TresGroup>

            <!-- ====== Connection Pipes ====== -->
            <!-- Computing → Hub -->
            <TresMesh :position="[-4, 0.5, 0]">
                <TresBoxGeometry :args="[4.5, 0.06, 0.06]" />
                <TresMeshStandardMaterial :emissive="'#4299e1'" :emissive-intensity="3" color="#4299e1"
                                          :transparent="true" :opacity="0.7" />
            </TresMesh>
            <!-- Hub → Transformers -->
            <TresMesh :position="[4, 0.5, 0]">
                <TresBoxGeometry :args="[4.5, 0.06, 0.06]" />
                <TresMeshStandardMaterial :emissive="'#ed8936'" :emissive-intensity="3" color="#ed8936"
                                          :transparent="true" :opacity="0.7" />
            </TresMesh>
            <!-- Hub → Screens -->
            <TresMesh :position="[0, 0.5, -5]">
                <TresBoxGeometry :args="[0.06, 0.06, 5]" />
                <TresMeshStandardMaterial :emissive="'#a78bfa'" :emissive-intensity="3" color="#a78bfa"
                                          :transparent="true" :opacity="0.7" />
            </TresMesh>
            <!-- Branch pipes to side screens -->
            <TresMesh :position="[-2.25, 0.5, -7.5]">
                <TresBoxGeometry :args="[4.5, 0.04, 0.04]" />
                <TresMeshStandardMaterial :emissive="'#63b3ed'" :emissive-intensity="2" color="#63b3ed"
                                          :transparent="true" :opacity="0.5" />
            </TresMesh>
            <TresMesh :position="[2.25, 0.5, -7.5]">
                <TresBoxGeometry :args="[4.5, 0.04, 0.04]" />
                <TresMeshStandardMaterial :emissive="'#ed8936'" :emissive-intensity="2" color="#ed8936"
                                          :transparent="true" :opacity="0.5" />
            </TresMesh>
        </TresCanvas>

        <!-- ====== HTML Overlay: Header ====== -->
        <div class="absolute top-0 left-0 right-0 px-5 py-3 flex items-center justify-between z-10
                    bg-gradient-to-b from-[#050810ee] via-[#050810aa] to-transparent pointer-events-none">
            <div class="flex items-center gap-3 pointer-events-auto">
                <div class="w-9 h-9 rounded-lg bg-blue-500/20 flex items-center justify-center backdrop-blur">
                    <CloudServerOutlined class="text-lg text-blue-400" />
                </div>
                <div>
                    <h1 class="text-sm font-bold text-white leading-tight">电力AI平台 · 数字孪生</h1>
                    <div class="text-[10px] text-gray-500">3D全景态势感知 · 鼠标拖拽旋转 · 滚轮缩放</div>
                </div>
            </div>
            <div class="flex items-center gap-3 pointer-events-auto">
                <span class="text-green-400 flex items-center gap-1 text-xs">
                    <span class="w-1.5 h-1.5 rounded-full bg-green-400 animate-pulse"></span> 实时
                </span>
                <a-button type="primary" ghost size="small" @click="loadData"><ReloadOutlined /></a-button>
            </div>
        </div>

        <!-- ====== HTML Overlay: Left - Layer Stats ====== -->
        <div class="absolute top-16 left-3 w-44 space-y-1.5 z-10" :class="{ 'opacity-30 hover:opacity-100': state.panelCollapsed }">
            <div v-for="layer in state.layerStats" :key="layer.key"
                 class="backdrop-blur-md bg-[#0d1117cc] rounded-lg border border-gray-800/50 px-3 py-2
                        cursor-pointer hover:border-gray-600 transition-all group"
                 @click="navigateTo(layer.route)">
                <div class="flex items-center justify-between">
                    <span class="text-[11px] font-medium" :style="{ color: layer.color }">{{ layer.name }}</span>
                    <div class="text-right">
                        <span class="text-sm font-bold text-white">{{ layer.count }}</span>
                        <span class="text-[10px] text-gray-500 ml-0.5">{{ layer.unit }}</span>
                    </div>
                </div>
            </div>
            <!-- Cluster Summary -->
            <div class="backdrop-blur-md bg-[#0d1117cc] rounded-lg border border-gray-800/50 px-3 py-2 mt-2">
                <div class="text-[10px] text-gray-500 mb-1">集群概要</div>
                <div class="grid grid-cols-2 gap-x-3 gap-y-1 text-[11px]">
                    <div class="flex justify-between"><span class="text-gray-400">节点</span><span class="text-blue-400 font-bold">{{ state.clusterSummary.onlineNodes }}/{{ state.clusterSummary.totalNodes }}</span></div>
                    <div class="flex justify-between"><span class="text-gray-400">GPU</span><span class="text-purple-400 font-bold">{{ state.clusterSummary.totalGPU }}</span></div>
                    <div class="flex justify-between"><span class="text-gray-400">GPU%</span><span class="font-bold" :style="{ color: state.clusterSummary.avgGpuUsage > 80 ? '#fc8181' : '#48bb78' }">{{ state.clusterSummary.avgGpuUsage }}%</span></div>
                    <div class="flex justify-between"><span class="text-gray-400">任务</span><span class="text-orange-400 font-bold">{{ state.clusterSummary.runningTasks }}</span></div>
                </div>
            </div>
        </div>

        <!-- ====== HTML Overlay: Right - Alerts ====== -->
        <div class="absolute top-16 right-3 w-56 z-10">
            <div class="backdrop-blur-md bg-[#0d1117cc] rounded-lg border border-gray-800/50 p-3">
                <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-1.5">
                        <AlertOutlined class="text-red-400 text-xs" />
                        <span class="text-xs font-bold text-gray-200">告警</span>
                    </div>
                    <a-badge :count="state.alerts.filter(a => a.status === 'active').length"
                             :number-style="{ backgroundColor: '#ff4d4f', fontSize: '9px', minWidth: '14px', height: '14px', lineHeight: '14px' }" />
                </div>
                <div class="space-y-1.5 max-h-48 overflow-auto">
                    <div v-for="alert in state.alerts.slice(0, 4)" :key="alert.id"
                         class="rounded px-2 py-1.5 text-[10px] border"
                         :style="{ borderColor: alertLevelColor(alert.level) + '30', backgroundColor: alertLevelColor(alert.level) + '10' }">
                        <div class="font-medium text-gray-300 truncate">{{ alert.message }}</div>
                        <div class="text-gray-600 mt-0.5">{{ alert.nodeName }} · {{ alert.time }}</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ====== HTML Overlay: Bottom - Navigation ====== -->
        <div class="absolute bottom-4 left-1/2 -translate-x-1/2 z-10">
            <div class="backdrop-blur-md bg-[#0d1117cc] rounded-xl border border-gray-800/50 px-4 py-2 flex items-center gap-3">
                <div v-for="nav in [
                    { label: '资产明细', route: '/datamag/asset-details', icon: DatabaseOutlined, color: 'text-green-400' },
                    { label: '孪生推演', route: '/rag-scheduler/simulation', icon: AppstoreOutlined, color: 'text-purple-400' },
                    { label: 'RAG助手', route: '/rag-scheduler/chat', icon: RobotOutlined, color: 'text-orange-400' },
                    { label: '血缘追踪', route: '/datamag/lineage', icon: LinkOutlined, color: 'text-blue-400' }
                ]" :key="nav.route"
                     class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg hover:bg-white/5 cursor-pointer transition-all"
                     @click="navigateTo(nav.route)">
                    <component :is="nav.icon" :class="nav.color" class="text-xs" />
                    <span class="text-[11px] text-gray-300">{{ nav.label }}</span>
                </div>
            </div>
        </div>

        <!-- ====== 3D Zone Labels (CSS positioned) ====== -->
        <div class="absolute top-[58%] left-[18%] z-10 pointer-events-none text-center">
            <div class="text-[10px] text-blue-400 font-bold tracking-wider">COMPUTING</div>
            <div class="text-[9px] text-gray-500">算力资源区</div>
        </div>
        <div class="absolute top-[40%] left-[48%] -translate-x-1/2 z-10 pointer-events-none text-center">
            <div class="text-[10px] text-cyan-400 font-bold tracking-wider">DATA HUB</div>
            <div class="text-[9px] text-gray-500">数据枢纽</div>
        </div>
        <div class="absolute top-[58%] right-[18%] z-10 pointer-events-none text-center">
            <div class="text-[10px] text-orange-400 font-bold tracking-wider">POWER</div>
            <div class="text-[9px] text-gray-500">电力设备区</div>
        </div>
        <div class="absolute top-[25%] left-[48%] -translate-x-1/2 z-10 pointer-events-none text-center">
            <div class="text-[10px] text-purple-400 font-bold tracking-wider">DISPLAY</div>
            <div class="text-[9px] text-gray-500">业务大屏</div>
        </div>
    </div>
</template>

<style scoped>
.relative {
    position: relative;
}
</style>
