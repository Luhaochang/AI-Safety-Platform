<!--服务在线数统计图-->
<script setup>
import * as echarts from 'echarts';
import 'echarts/theme/macarons.js';
import { nextTick, onMounted, reactive, ref, watch } from 'vue';

const props = defineProps(['width','valueData'])
const chartsRef = ref()
const state = reactive({
    dataList: [],
    xData: [],

    opts: {},
})

let chat = null

const initCharts = () => {
    if (chartsRef.value) {
        chat = echarts.init(chartsRef.value, 'macarons');
    }
}

const setOpt = () => {
    if (chat){
        chat.setOption({
            grid: {
                top: 0,
                bottom: '10%',
                right: '8%',
                left: '15%'
            },
            xAxis: {
                type: 'value',
                axisLabel: {
                    formatter: '{value}%' // 显示百分比格式
                },
                min:0,
                max: 100
            },
            yAxis: {
                type: 'category',
                data: state.xData,
                axisTick: {show: false},
                inverse: true,

            },
            dataZoom: [
                {
                    type: 'inside',
                    start: 0,
                    end: 50,
                    yAxisIndex: 0,
                    zoomOnMouseWheel: false,
                    moveOnMouseWheel: true,
                },
                {
                    show: true,
                    yAxisIndex: 0,
                    width: 10,
                    height: '90%',
                    showDataShadow: false,
                    left: '93%',
                    fillerColor: "rgb(244,244,244)", // 滚动条颜色
                    backgroundColor: 'rgba(255,255,255,.4)',

                    handleSize: 0,
                    handleStyle: {
                        color: "rgba(244,244,244)"
                    },
                    moveHandleSize: 0
                }
            ],
            tooltip: {
                show: true,
                trigger: 'axis',
                formatter: function (params) {
                    // 假设'数据条'是我们要显示进度的系列名
                    let dataBarSeries = params.find(p => p.seriesName === '数据条');
                    if (dataBarSeries) {
                        return `完成进度 :<br/>${dataBarSeries.name} : ${dataBarSeries.value.toFixed(2) }%`;
                    } else {
                        return '';
                    }
                }
            },
            series: [
                {
                    name: '数据条',
                    yAxisIndex: 0,
                    type: 'bar',
                    data: state.dataList.map(value => {
                        if (value === null || value === undefined) return 0;
                        return Number((value * 100).toFixed(2));
                    }),
                    itemStyle: {
                        borderRadius: 2,
                        // color: '#2684E5',
                    },
                    barWidth: 14,
                    barGap: 2,
                    label: {
                        show: false,
                        offset: [0, -5],
                    },
                    labelLayout: {
                        x: '91%',
                    },
                },
            ]
        })
    }
}

watch(() => props.valueData, async (newValue,oldValue) => {
    if (newValue && newValue.length > 0) {
        state.xData = newValue.map(item => item.pipelineTaskName);
        state.dataList = newValue.map(item => item.completionProgress);

        await nextTick();
        if (!chat) {
            initCharts();
        }
        setOpt()
    } else {
        state.dataList = [];
        if (chat) {
            chat.dispose();
            chat = null;
        }
    }
},{deep: true})

watch(() => props.width, (newValue, oldValue) => {
    if (chat) {
        chat.resize()
    }
}, {deep: true})

onMounted(async () => {
    if (props.valueData && props.valueData.length > 0) {
        state.xData = props.valueData.map(item => item.pipelineTaskName);
        state.dataList = props.valueData.map(item => item.completionProgress);
        await nextTick();
        initCharts();
        setOpt();
    }
})
</script>

<template>
    <div style="flex: 1;min-width: 180px">
        <div ref="chartsRef" class="chart" v-if="state.dataList.length !==0"/>
        <a-empty v-else/>
    </div>
</template>

<style scoped lang="scss">
.chart {
    min-height: 180px;
}
</style>
