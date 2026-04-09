<!--安全系数仪表盘-->
<script setup>
import * as echarts from 'echarts';

const props = defineProps({
    chatValue: {
        type: Array,
        default: () => []
    },
    showLegend: {
        type: Boolean,
        default: false
    }
})

const chartRef = ref()
const state = reactive({
    instance: null,
    dataList: [],
    opt: {
        tooltip: {
            trigger: 'item',
            show: true,
            formatter: '{b}: {c} ({d}%)'
        },
        legend: {
            top: 'middle',
            right: '0%',
            orient: 'vertical',
            show: false,
            itemWidth: 10,
            itemHeight: 10,
            textStyle: {
                fontSize: 12,
                color: '#666'
            }
        },
        series: [
            {
                name: '数据集分类',
                type: 'pie',
                radius: ['40%', '70%'], // 调整半径，为标签留出更多空间
                center: ['50%', '50%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 5,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: true, // 默认显示标签
                    position: 'outside', // 标签位置
                    formatter: '{b}\n{d}%', // 显示名称和百分比
                    fontSize: 12,
                    color: '#333'
                },
                emphasis: {
                   scale: false
                },
                labelLine: {
                    show: true, // 显示标签引导线
                    length: 5, // 引导线长度
                    length2: 5 // 第二段引导线长度
                },
                data: []
            }
        ]
    }
})

const initChart = () => {
    if (chartRef.value) {
        state.instance = echarts.init(chartRef.value);
        updateChartOption();
        state.instance.setOption(state.opt);

        window.addEventListener('resize', resizeChart);
    }
}

const resizeChart = () => {
    if (state.instance) {
        state.instance.resize();
    }
}

const updateChartOption = () => {
    state.opt.series[0].data = props.chatValue;

    if (props.showLegend) {
        state.opt.legend.show = true;
        // 如果显示图例，调整饼图位置，给图例留出空间
        state.opt.series[0].center = ['35%', '50%'];
    } else {
        state.opt.legend.show = false;
        state.opt.series[0].center = ['50%', '50%'];
    }
}

watch(() => props.chatValue, (newValue) => {
    if (state.instance) {
        updateChartOption();
        state.instance.setOption(state.opt);
    }
}, {deep: true})

watch(() => props.showLegend, (newValue) => {
    if (state.instance) {
        updateChartOption();
        state.instance.setOption(state.opt);
    }
})

onMounted(() => {
    initChart()
})

onUnmounted(() => {
    window.removeEventListener('resize', resizeChart);
    if (state.instance) {
        state.instance.dispose();
    }
})
</script>

<template>
    <div class="chart-container">
        <div ref="chartRef" class="chart"/>
    </div>
</template>

<style scoped lang="scss">
.chart-container {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
.chart {
    height: 100%;
    width: 100%;
    min-height: 200px;
}
</style>
