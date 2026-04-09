<!--服务在线数统计图-->
<script setup>
import * as echarts from 'echarts';
// import 'echarts/theme/macarons.js';

const props = defineProps(['width','valueData'])
const chartsRef = ref()
const state = reactive({
    dataList: [

    ],
    xData: [],

    opts: {},
})

let chat = null

const initCharts = () => {
    chat = echarts.init(chartsRef.value,);
}

const setOpt = () => {
    if (chat){
        chat.setOption({
            grid: {
                top: 0,
                bottom: '10%',
                right: '4%',
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
                inverse: true

            },
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
                    data: state.dataList.map(value => Number(value.toFixed(2)*100)), // 确保所有数据都是数字，并且保留两位小数
                    itemStyle: {
                        borderRadius: 2,
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

watch(() => props.valueData,(newValue,oldValue) => {
    state.xData = newValue.map(item => item.name);
    state.dataList = newValue.map(item => item.value);
    setOpt()
},{deep: true})

watch(() => props.width, (newValue, oldValue) => {
    chat.resize()
}, {deep: true})
onMounted(() => {
    initCharts();

    setOpt()
})
</script>

<template>
    <div style="flex: 1;min-width: 180px">
        <div ref="chartsRef" class="chart"/>
    </div>
</template>

<style scoped lang="scss">
.chart {
    min-height: 180px;
}
</style>
