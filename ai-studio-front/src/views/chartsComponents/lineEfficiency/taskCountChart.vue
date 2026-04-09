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
    switch (props.type) {
        case 1:
            state.xData = newValue.map(item => item.region);
            state.yData = newValue.map(item => item.count);
            setChartOption()
            break;
        case 2:
            state.xData = newValue.map(item => item.date);
            state.yData = newValue.map(item => item.count);
            const temp = newValue.map(item => item.click);
            chart.setOption({
                title: {
                    show: state.yData.length === 0,
                    text: '暂无数据',
                    top: '36%',
                    left: '40%'
                },
                xAxis: {
                    type: 'category',
                    axisTick: {
                        show: false
                    },
                    boundaryGap: props.type !== 3 && props.type !== 2,
                    data: state.xData,
                    axisLabel: {
                    },
                    splitLine: {
                        show: true,
                        lineStyle: {
                            color: 'rgb(240,240,240)'
                        }
                    }
                },
                yAxis: [
                    {
                        type: 'value',
                        name: '调用次数',
                        position: 'left',
                        axisLabel: {
                            formatter: '{value} 次'
                        },
                        splitLine: {
                            show: false,
                            lineStyle: {
                                color: 'rgb(240,240,240)'
                            }
                        },
                        axisTick: {
                            show: false
                        }
                    },
                    {
                        type: 'value',
                        name: '平均响应时间',
                        position: 'right',
                        axisLabel: {
                            formatter: '{value} ms'
                        },
                        splitLine: {
                            show: false,
                            lineStyle: {
                                color: 'rgb(240,240,240)'
                            }
                        },
                        axisTick: {
                            show: false
                        }
                    }
                ],
                series: [
                    {
                        name: '总体调用次数',
                        type: 'line',
                        data: state.yData,
                        yAxisIndex: 0 // 使用左侧的Y轴
                    },
                    {
                        name: '平均响应时间',
                        type: 'line',
                        data: temp,
                        yAxisIndex: 1 // 使用右侧的Y轴
                    }
                ]
            });
            // setChartOption()
            break;
        case 3:
            state.xData = newValue.map(item => item.date);
            state.yData = newValue.map(item => item.count);
            setChartOption()
            break;
    }
}, {deep: true})

const state = reactive({
    // chart: null
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
                trigger: 'axis'
            },
            legend: {
                show: false,
            },
            grid: {
                // height: '100%',
                // show: true,
                left: '0%',
                right: '3%',
                bottom: '5%',
                top: "3%",
                containLabel: true,
                borderColor: ''
            },
            toolbox: {
                feature: {
                }
            },
            xAxis: {
                type: 'category',
                boundaryGap: props.type !== 3 && props.type !== 2,
                data: state.xData,
                axisTick: {
                    show: false
                },
                axisLabel: {
                },
                splitLine: {
                    show: true,
                    lineStyle: {
                        color: 'rgb(240,240,240)'
                    }
                }
            },
            yAxis: {
                type: 'value',
                splitLine: {
                    show: true,
                    lineStyle: {
                        color: 'rgb(240,240,240)'
                    }
                },
                axisTick: {
                    show: false
                }
            },
            series: {
                data: state.yData,
                barWidth: 50,
                bottom: 0,
                type: props.type !== 3 && props.type !== 2? 'bar' : 'line',
                smooth: true,
                animationDuration: 2800,
                animationEasing: 'cubicInOut'
            }
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
