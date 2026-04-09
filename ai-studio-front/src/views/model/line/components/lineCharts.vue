<script setup>
import * as echarts from 'echarts';
let chart = null
const chartDom = ref()

const props = defineProps({
    lineData: Array,
    x: Array,
    title: String
})

watch(() => props.lineData,(newValue,oldValue) => {
    chart.setOption({
        series: [
            {
                data: newValue,
            }
        ]
    })
},{deep: true})

watch(() => props.x,(newValue,oldValue) => {
    chart.setOption({
        xAxis: {
            data: newValue,
        }
    })
},{deep: true})


const initChart =  () => {
    chart = echarts.init(chartDom.value);
    if (chart) {
        let opt = {
            grid: {
                top: '10%',
                right: 10,
                containLabel: false
            },
            xAxis: {
                name: 'Step',
                nameLocation: 'center',
                nameGap: 40,
                nameTextStyle: {
                    fontWeight: 'bolder',
                    fontSize: 18
                },
                type: 'category',
                boundaryGap: false,
                data: props.x,
                splitLine: {
                    show: true
                },
                axisLine: {
                    show: false,
                    onZero: false,

                },
            },
            yAxis: {
                offset: 10,
                type: 'value',
                boundaryGap: false,
                scale: true
            },
            splitArea: {                                //网格区域
                show: true                                  //是否显示
            },
            tooltip: {
                trigger: 'item',
            },
            series: [
                {
                    data: props.lineData,
                    type: 'line'
                }
            ]
        };
        chart.setOption(opt);
    }
}

window.addEventListener('resize', function() {
    chart.resize();
});
onMounted( () => {
    initChart()
})
</script>

<template>
    <div class="container-css">
        <div class="title">{{title}}</div>
        <div ref="chartDom" style="width: 100%;height: 300px">

        </div>
    </div>

</template>

<style scoped lang="scss">
.container-css{
    border: 1px solid rgb(209, 217, 225);
    border-radius: 4px;
    padding: 16px;
    background: rgb(255, 255, 255);
    position: relative;
    overflow: hidden;

    .title{
        color: rgb(17, 23, 28);
        font-size: 13px;
        line-height: 20px;
        font-weight: 600;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow-wrap: break-word
    }
}
</style>
