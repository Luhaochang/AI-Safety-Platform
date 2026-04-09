<template>
    <div class="s-canvas">
        <canvas id="s-canvas"
                :width="contentWidth"
                :height="contentHeight"></canvas>
    </div>
</template>
<script setup>

import {ref, defineProps, watch, defineEmits, onMounted} from "vue";

let identifyCodes = ref('abcdefghijkmnpqrstuvwxyzABCEFGHJKLMNPQRSTWXYZ1234567890')

let identifyCode = ref('')

const emit = defineEmits(['makedCode'])
const props = defineProps({
    fresh: {
        default: true,
        type: Boolean
    },
    fontSizeMin: {
        type: Number,
        default: 20
    },
    fontSizeMax: {
        type: Number,
        default: 36
    },
    backgroundColorMin: {
        type: Number,
        default: 180
    },
    backgroundColorMax: {
        type: Number,
        default: 240
    },
    colorMin: {
        type: Number,
        default: 50
    },
    colorMax: {
        type: Number,
        default: 160
    },
    lineColorMin: {
        type: Number,
        default: 40
    },
    lineColorMax: {
        type: Number,
        default: 180
    },
    dotColorMin: {
        type: Number,
        default: 0
    },
    dotColorMax: {
        type: Number,
        default: 255
    },
    contentWidth: {
        type: Number,
        default: 112
    },
    contentHeight: {
        type: Number,
        default: 38
    }
})


// 生成一个随机数
function randomNum(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

// 生成一个随机的颜色
function randomColor(min, max) {
    let r = randomNum(min, max);
    let g = randomNum(min, max);
    let b = randomNum(min, max);
    return 'rgb(' + r + ',' + g + ',' + b + ')';
}

function drawPic() {
    let canvas = document.getElementById('s-canvas');
    let ctx = canvas.getContext('2d');
    ctx.textBaseline = 'bottom';
    // 绘制背景
    ctx.fillStyle = randomColor(
        props.backgroundColorMin,
        props.backgroundColorMax
    );
    ctx.fillRect(0, 0, props.contentWidth, props.contentHeight);
    // 绘制文字
    for (let i = 0; i < identifyCode.value.length; i++) {
        drawText(ctx, identifyCode.value[i], i);
    }
    //    this.drawLine(ctx)
    drawDot(ctx);
}

function drawText(ctx, txt, i) {
    ctx.fillStyle = randomColor(props.colorMin, props.colorMax);
    ctx.font =
        randomNum(props.fontSizeMin, props.fontSizeMax) + 'px SimHei';
    let x = (i + 1) * (props.contentWidth / (identifyCode.value.length + 1));
    let y = randomNum(props.fontSizeMax, props.contentHeight - 5);
    let deg = randomNum(-10, 10);
    // 修改坐标原点和旋转角度
    ctx.translate(x, y);
    ctx.rotate((deg * Math.PI) / 180);
    ctx.fillText(txt, 0, 0);
    // 恢复坐标原点和旋转角度
    ctx.rotate((-deg * Math.PI) / 180);
    ctx.translate(-x, -y);
}

function drawLine(ctx) {
    //     绘制干扰线
    for (let i = 0; i < 3; i++) {
        ctx.strokeStyle = randomColor(
            props.lineColorMin,
            props.lineColorMax
        );
        ctx.beginPath();
        ctx.moveTo(
            randomNum(0, props.contentWidth),
            randomNum(0, props.contentHeight)
        );
        ctx.lineTo(
            randomNum(0, props.contentWidth),
            randomNum(0, props.contentHeight)
        );
        ctx.stroke();
    }
}

function drawDot(ctx) {
    // 绘制干扰点
    for (let i = 0; i < 30; i++) {
        ctx.fillStyle = randomColor(0, 255);
        ctx.beginPath();
        ctx.arc(
            randomNum(0, props.contentWidth),
            randomNum(0, props.contentHeight),
            1,
            0,
            2 * Math.PI
        );
        ctx.fill();
    }
}

// 生成四位随机验证码
function makeCode(o, l) {
    identifyCode.value = '';
    for (let i = 0; i < l; i++) {
        identifyCode.value += identifyCodes.value[
            randomNum(0, identifyCodes.value.length)
            ];
    }

    //绘制图片
    drawPic();

    //传值给父组件
    emit('makedCode', identifyCode.value);
}


watch(() => props.fresh,(newValue,oldValue) => {
    makeCode(identifyCodes.value, 4);
},{deep: true})

onMounted(() => {
    makeCode(identifyCodes.value, 4);
})

</script>
