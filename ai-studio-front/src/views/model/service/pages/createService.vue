<template>
    <div>
        <div class="flex justify-end" style="margin: 10px">
            <el-button link @click="zoomF('in')" icon="ZoomIn" type="primary">放大</el-button>
            <el-button link @click="zoomF('out')" icon="ZoomOut" type="warning">缩小</el-button>
        </div>
        <div style="display: flex; align-items: center;position: relative">
            <div style="border: 1px solid #ccc; padding: 20px;" id="wrapper">
                <svg class="dagre" height="800" width="1000">
                    <g class="container"></g>
                </svg>

            </div>

            <a-drawer
                width="500"
                :open="state.visible"
                placement="right"
                :get-container="false"
                :style="{ position: 'absolute' }"
                :closable="true"
                title="节点详情"
                @close="closeDrawer"
            >
                <el-form :model="state.drawInfo" label-width="auto" label-position="right">
                    <el-form-item label="节点类型">
                        <el-input readonly v-model="state.drawInfo.opType"></el-input>
                    </el-form-item>
                    <el-form-item label="name">
                        <el-input readonly v-model="state.drawInfo.name"></el-input>
                    </el-form-item>
                    <el-form-item label="输入">
                        <div class="w-[380px]">
                            <a-list size="small" :data-source="state.drawInfo.input">
                                <template #renderItem="{item, index}">
                                    <a-list-item class="block"
                                                 :style="{'background-color': index % 2 !== 0 ? '' : '#f7f9ff'}"
                                    >
                                        <div class="flex justify-between items-center text-xs" style="color: #140e35">
                                            <div>{{ item }}</div>
                                        </div>
                                    </a-list-item>
                                </template>
                            </a-list>
                        </div>
                    </el-form-item>
                    <el-form-item label="输出">
                        <div class="w-[380px]">
                            <a-list size="small" :data-source="state.drawInfo.output">
                                <template #renderItem="{item, index}">
                                    <a-list-item class="block"
                                                 :style="{'background-color': index % 2 !== 0 ? '' : '#f7f9ff'}"
                                    >
                                        <div class="flex justify-between items-center text-xs" style="color: #140e35">
                                            <div>{{ item }}</div>
                                        </div>
                                    </a-list-item>
                                </template>
                            </a-list>
                        </div>
                    </el-form-item>

                    <el-form-item label="权重" v-if="state.drawInfo.weights">
                        <div class="w-[380px]">
                            <a-list size="small" :data-source="state.drawInfo.weights">
                                <template #renderItem="{item, index}">
                                    <a-list-item class="block"
                                                 :style="{'background-color': index % 2 !== 0 ? '' : '#f7f9ff'}"
                                    >
                                        <div class="flex justify-between items-center text-xs" style="color: #140e35">
                                            <div>{{ formatWeight(item.dims) }}</div>
                                        </div>
                                    </a-list-item>
                                </template>
                            </a-list>
                        </div>
                    </el-form-item>
                </el-form>
            </a-drawer>
        </div>
    </div>

</template>

<script setup>
import {ref, onMounted, watch} from "vue";
import dagreD3 from "dagre-d3";
import * as d3 from "d3";
import axios from "axios";

const nodeInfo = ref();
let nowNode = ref(''); // 标记当前高亮节点

const nodes = ref();
const edges = ref();
const g = ref();
const g_container = ref()
const zoom = ref()
const svg = ref()
const props = defineProps(['modelUrl'])

const state = reactive({
    visible: false,
    drawInfo: {
        name: '',
        opType: '',
        input: [],
        output: [],
        weights: []
    },
    graph: {
        // initScale: 1,
        transform: {
            x: 0,
            y: 0,
            k: 1
        }
    }
})

const formatWeight = (weight) => {
    const dimsString = weight.join(' x ');
    // 使用模板字符串将结果包裹在尖括号内
    const result = `<${dimsString}>`;
    return result;
}

// 连线数组
const formatStyle = (node) => {
    switch (node.opType) {
        case 'Add':
            return "fill:#000;stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Relu':
            return "fill:rgb(112, 41, 33);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Conv':
            return "fill:rgb(51, 85, 136);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Identity':
            return "fill:#000;stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'MaxPool':
            return "fill:rgb(51, 85, 51);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Input':
            return "fill:#eee;stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Output':
            return "fill:#eee;stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Flatten':
            return "fill:rgb(108, 79, 71);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'GlobalAveragePool':
            return "fill:rgb(51, 85, 51);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
        case 'Gemm':
            return "fill:rgb(51, 85, 136);stroke:#000;stroke-width: 1px;cursor: pointer;align-items: center;justify-content: center;display: flex;";
    }
}

function scrolled() {
    const wrapper = d3.select('#wrapper');
    const x = wrapper.node().scrollLeft + wrapper.node().clientWidth / 2;
    const y = wrapper.node().scrollTop + wrapper.node().clientHeight / 2;
    const scale = state.graph.transform.k;
    wrapper.call(d3.zoom().translateTo, x / scale, y / scale);
}

function zoomed() {
    const scale = state.graph.transform.k;

    const scaledWidth = graphW.value * scale;
    const scaledHeight = graphH.value * scale;

    // Change SVG dimensions.
    svg.value.attr('width', scaledWidth).attr('height', scaledHeight);

    // Scale the image itself.
    g_container.value.attr('transform', `scale(${scale})`);

    // Move scrollbars.
    const wrapper = d3.select('#wrapper').node();
    wrapper.scrollLeft = state.graph.transform.x;
    wrapper.scrollTop = state.graph.transform.y;

    // If the image is smaller than the wrapper, move the image towards the
    // center of the wrapper.
    const dx = d3.max([0, wrapper.clientWidth / 2 - scaledWidth / 2]);
    const dy = d3.max([0, wrapper.clientHeight / 2 - scaledHeight / 2]);
    svg.value.attr('transform', `translate(${dx}, ${dy})`);
}

const graphW = ref()
const graphH = ref()

const draw = () => {
    // 创建 Graph 对象
    g.value = new dagreD3.graphlib.Graph()
        .setGraph({
            zoom: 1,
            rankdir: "TB", // 流程图从下向上显示，默认'TB'，可取值'TB'、'BT'、'LR'、'RL'
            //ranker: "network-simplex",//连线算法
            // edgesep: 200,
            marginX: 200,
            // nodesep: 70, // 节点之间间距
            // ranksep: 100, // 层与层之间的间距
        })
        .setDefaultEdgeLabel(function () {
            return {};
        });
    nodes.value.forEach((node) => {
        g.value.setNode(node.name, {
            id: node.name,
            label: `<foreignObject id='${node.name}'  style="display:flex;align-items: center;justify-content: center;width: 100%;height: 100%" >
        <div id='${
                node.name
            }' xmlns='http://www.w3.org/1999/xhtml' style='width:100%; height: 100%;padding: 10px;display: flex;align-items: center;justify-content: center'>
          <div id='${node.name}' class='nodeBox'>
            <div id='${node.name}' class='${node.opType}'>${node.opType}</div>
        </div>
          </div>
        </div>
        </foreignObject>`, //node.nodeName,
            labelType: "html",
            // width: 120,
            // height: 86,
            // shape: "rect", //节点形状，可以设置rect(长方形),circle,ellipse(椭圆),diamond(菱形) 四种形状，还可以使用render.shapes()自定义形状
            // style: "fill:#fff;stroke:#a0cfff;stroke-width: 2px;cursor: pointer", //节点样式,可设置节点的颜色填充、节点边框
            style: formatStyle(node), //节点样式,可设置节点的颜色填充、节点边框
            labelStyle: "fill: #fff;font-weight:bold;cursor: pointer;color:#fff;display:flex;align-items: center;justify-content: center;", //节点标签样式, 可设置节点标签的文本样式（颜色、粗细、大小）
            rx: 5, // 设置圆角
            ry: 5, // 设置圆角
            paddingBottom: 0,
            paddingLeft: 0,
            paddingRight: 0,
            paddingTop: 0,
        });
    });

    // Graph添加节点之间的连线
    if (nodes.value.length > 1) {
        edges.value.forEach((edge) => {
            g.value.setEdge(edge.start, edge.end, {
                curve: d3.curveBasis, //d3.curveBasis, // 设置为贝塞尔曲线
                style: "stroke: #000; fill: none; stroke-width: 1px;", // 连线样式
                arrowheadStyle: "fill: #000;stroke: #000;", //箭头样式，可以设置箭头颜色
                arrowhead: "normal", //箭头形状，可以设置 normal,vee,undirected 三种样式，默认为 normal
            });
        });
    }

    // 获取要绘制流程图的绘图容器
    g_container.value = d3.select("svg.dagre").select("g.container");

    // 创建渲染器
    const render = new dagreD3.render();
    // 在绘图容器上运行渲染器绘制流程图
    render(g_container.value, g.value);
    svg.value = d3.select("svg.dagre");


    let {clientWidth, clientHeight} = svg.value._groups[0][0];
    let {width, height} = g.value.graph();

    svg.value.attr('width', width ).attr('height', height);
    state.graph.transform.x = -(clientWidth + width * 1) / 2;
    state.graph.transform.y = 0;

    graphH.value = height;
    graphW.value = width;

    zoom.value = d3
        .zoom() // 缩放支持
        .scaleExtent([0.5, 2]) // 缩放范围
        .on("zoom", function (current) {
            g_container.value.attr("transform", current.transform);
            state.graph.transform = current.transform;
        });


    svg.value.call(zoom.value); // 缩放生效

    if (width > 1000) {
        svg.value
            .transition()
            .duration(1000) // 1s完成过渡
            .call(
                zoom.value.transform,
                d3.zoomIdentity // 居中显示
                    .translate(
                        -(clientWidth + width * 1) / 2,
                        0
                    )
                    .scale(1) // 默认缩放比例
            );
    }


    //  节点点击事件
    g_container.value.on(
        "click",
        (e) => {
            // const {k, x, y} = state.graph.transform; // 获取当前的缩放和平移状态
            const pointer = d3.pointer(e); // 获取点击位置（未考虑缩放和平移）
            // const trueX = (pointer[0] - x) / k; // 考虑缩放和平移后的真实x坐标
            // const trueY = (pointer[1] - y) / k;

            if (nowNode.value === (e.target.id)) {
                for (let a in g.value._nodes) {
                    const c_node = nodes.value.filter(item => item.name === a);

                    g.value._nodes[a].style = formatStyle(c_node[0]);
                }
                nowNode.value = "";
                render(g_container.value, g.value);
                return;
            }
            nodes.value.forEach((item) => {
                if (item.name === (e.target.id)) {
                    nowNode.value = item.name;
                    nodeInfo.value = item;
                    for (let a in g.value._nodes) {
                        g.value._nodes[a].style =
                            formatStyle(nodes.value.filter(i => i.name === a)[0])
                    }
                    g.value._nodes[nodeInfo.value.name].style = "stroke:#ef4444;stroke-width: 2px;cursor: pointer;fill: #2563eb"
                    openDialog()

                }
            });


            render(g_container.value, g.value);

        },
        true
    );

    svg.value.on("wheel.zoom", null);
    svg.value.on("dblclick.zoom", null);

};

const parserFile = async (URL) => {
    try {
        // let response = await axios.get('conv.json');
        let response = await axios.get(URL);
        const {graph} = response.data;
        const {node, initializer, input, output} = graph;
        const lines = []
        // 构建起始点
        node.forEach(item => {
            if (item.input.filter(i => i === 'input').length !== 0) {
                input[0].input = [];
                input[0].output = ['input'];
                input[0].opType = 'Input'
            }
        })

        // 构建终结点
        node.forEach(item => {
            if (item.output[0] === 'output') {
                output[0].input = ['output']; // output是线
                output[0].output = [];
                output[0].opType = 'Output'
            }
        })

        node.push(input[0]);
        node.push(output[0]);
        const out_nodes = node.map(item => ({
            output: item.output[0],
            node: item.name
        }))

        function findWeights(nodeName, inputNames, initializer) {
            return inputNames.map(inputName => {
                return initializer.find(weightItem => weightItem.name === inputName);
            }).filter(weight => weight !== undefined); // 过滤掉未找到的权重
        }

        node.forEach(item => {
            const weights = findWeights(item.name, item.input, initializer);


            // 如果找到了匹配的权重，则将其添加到节点的 weights 属性中（数组形式）
            if (weights.length > 0) {
                weights.forEach(item => {
                })
                item.weights = weights;
            }
        })


        for (let i of out_nodes) {
            for (let j of node) {
                const temp = j.input.filter(item => item === i.output);
                if (temp.length > 0) {
                    lines.push({
                        name: i.output,
                        end: j.name,
                        start: i.node
                    });
                }
            }
        }

        edges.value = lines;
        nodes.value = node;

    } catch (err) {
        console.log(err)
    }
}

const openDialog = () => {
    state.visible = true;
    for (let key in state.drawInfo) {
        if (nodeInfo.value.hasOwnProperty(key)) {
            state.drawInfo[key] = nodeInfo.value[key];
        }
    }
}

const closeDrawer = () => {
    state.visible = false;
}

const zoomF = (dir) => {
    const currentTransform = d3.zoomTransform(svg.value.node()); // 获取当前变换
    const { k: currentK, x: currentX, y: currentY } = currentTransform;

    if (dir === 'in') {
        const transform = d3.zoomIdentity
            .translate(currentX-1000, currentY-200)
            .scale(currentK+0.5); // 获取到目标 transform
        svg.value
            .transition()
            .duration(1000) // 1s完成过渡
            .call(
                zoom.value.transform,
                transform
            )
    } else if (dir === 'out') {
        const transform = d3.zoomIdentity
            .translate(currentX+1000, currentY+200)
            .scale(currentK-0.5); // 获取到目标 transform
        svg.value
            .transition()
            .duration(1000) // 1s完成过渡
            .call(
                zoom.value.transform,
                transform
            )
    }
}

function zoomCenter(targetGroupBBox = { width: 0, height: 0, x: 0, y: 0 }) { // 跟之前居中一样
    const scaleX = graphW.value / targetGroupBBox.width;
    const scaleY = graphH.value / targetGroupBBox.height;

    let k = Math.min(scaleX, scaleY) * 0.7;
    k = Math.max(k, 0.5);
    k = Math.min(k, 2); // 用画布大小 / 当前svg得大小 获取到比例值

    // 算出居中得x、y坐标（往俩盒子 一个大盒子（画布）和另外一个盒子（图形撑起来得））怎么让图形撑起来得居中呢！！！
    // 用画布自身得一半 减去 gropu得一半 * 缩放 K, 再减去gropu得translate
    const translateByX = 1800 / 2 - (targetGroupBBox.width / 2) * k - (targetGroupBBox.x * k);
    const translateByY = 800 / 2 - (targetGroupBBox.height / 2) * k - (targetGroupBBox.y * k);

    const transform = d3.zoomIdentity
        .translate(translateByX, translateByY)
        .scale(k); // 获取到目标 transform

    svg.value.transition() // 过渡效果
        .duration(1000)
        .call(zoom.value.transform, transform);
}

watch(() => props.modelUrl, (newValue,oldValue) => {
    parserFile(newValue).then(() => {
        draw();
    })
}, {deep: true})

onMounted(() => {
});

defineExpose()
</script>

<style>
.label-container {
    margin: 0 !important;
    padding: 0 !important;
    box-sizing: border-box !important;
}

.nodeA {
    color: #01579b;
    font-family: HarmonyOS Sans SC;
    font-size: 16px;
    font-style: normal;
    font-weight: 700;
}

.nodeBox {
    display: flex;
    justify-content: center;
    width: 100%;
    height: 100%;
}

.MaxPool {
    color: #ffffff;
}

.Input {
    color: #000000;
}

.Output {
    color: #000000;
}

.Add {
    color: #ffffff;
}

.Relu {
    color: #ffffff;
}

.Conv {
    color: #ffffff;
}


.dagre {
    margin: auto;
}

.container {
    width: 300px;
    height: 200px;
    overflow: auto;

    div {
        display: flex;
        justify-content: center;
        align-items: center;
    }
}

#wrapper {
    width: 1800px;
    height: 800px;
    overflow: auto;
    border: solid #888 1px;
}

</style>
