<script setup>
import * as echarts from 'echarts';
import 'echarts/theme/macarons.js';
import 'echarts/theme/dark.js';
import 'echarts/theme/infographic.js';
import 'echarts/theme/shine.js';
import 'echarts/theme/roma.js';
import 'echarts/theme/vintage.js';

const chartRef = ref()
let chart = null

const props = defineProps(['title', 'width', 'valueData', 'type'])

watch(() => props.valueData, (newValue, oldValue) => {
    state.yData = newValue;
    setChartOption()
}, {deep: true})

const state = reactive({
    dataList: [
    ],

    xData: [],
    yData: []
})

const setChartOption = () => {
    if (chart) {
        let opt = {
            title: {
                show: state.yData.length === 0,
                text: '暂无数据',
                top: '36%',
                left: '40%'
            },
            tooltip: {
                trigger: 'item',
                formatter: '{b}: {c} ({d}%)' // Match hostSecChart tooltip format
            },
            legend: {
                show: true, // Keep legend shown as requested
                orient: 'vertical',
                top: 'middle',
                right: '0%', // Position legend to the right
                itemWidth: 10,
                itemHeight: 10,
                textStyle: {
                    fontSize: 12,
                    color: '#666'
                }
            },
            grid: {
                left: '0%',
                right: '0%',
                bottom: '5%',
                top: "3%",
                containLabel: true,
                borderColor: ''
            },
            toolbox: {
                feature: {
                }
            },
            series: [
                {
                    name: '发布模型情况', // More descriptive name
                    type: 'pie',
                    radius: ['50%', '80%'], // Match hostSecChart radius
                    center: ['35%', '50%'], // Adjust center to make space for legend on the right
                    avoidLabelOverlap: false,
                    itemStyle: { // Match hostSecChart itemStyle
                        borderRadius: 5,
                        borderColor: '#fff',
                        borderWidth: 2
                    },
                    label: {
                        show: false,
                        position: 'center'
                    },
                    emphasis: {
                        label: {
                            show: true,
                            fontSize: 14, // Match hostSecChart fontSize
                            fontWeight: 'bold',
                            formatter: '{b}\n{c}' // Match hostSecChart formatter
                        },
                        scale: true,
                        scaleSize: 10
                    },
                    labelLine: {
                        show: false
                    },
                    data: state.yData
                }
            ]
        }

        chart.setOption(opt);
    }
}

const initChart = () => {
    chart = echarts.init(chartRef.value,'macarons');
}

const resizeChart = () => {
    if (chart) {
        chart.resize();
    }
}

watch(() => props.width, (newValue, oldValue) => {
    if (chart) {
        chart.resize();
    }
}, {deep: true})

onMounted(async () => {
    initChart();
    setChartOption();
    window.addEventListener('resize', resizeChart);
})

onUnmounted(() => {
    window.removeEventListener('resize', resizeChart);
    if (chart) {
        chart.dispose();
    }
})

</script>

<template>
    <div style="min-width: 100px;height: 100%">
        <div style="margin-top: -24px;margin-bottom: 15px;font-size: 14px;color: rgb(0,0,0,0.9)">
            <span>{{ title }}</span></div>
        <div ref="chartRef" class="chart" style="width: 100%;height: calc(100% - 39px);"/>
    </div>
</template>

<style scoped lang="scss">

</style>
