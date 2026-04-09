<template>
    <div class="magnify">
        <div class="left-container">
            <!-- 缩略图容器 -->
            <div class="carousel">
                <!-- 左箭头 -->
                <el-button size="small" icon="ArrowLeftBold" class="arrow" @click="leftArrowClick"></el-button>
                <!-- 缩略图展示盒子 -->
                <div class="show_box">
                    <ul class="picture_container" ref="middlePicture">
                        <li class="picture_item" @click="selectImg(item,index)" @mouseover="tabPicture(item,index)" v-for="(item, index) in state.pictureList" :key="index">
                            <img :src="item?.url" class="small_img" alt="">
                        </li>
                    </ul>
                </div>
                <!-- 向右箭头 -->
                <el-button size="small" icon="ArrowRightBold" class="arrow" @click="rightArrowClick"></el-button>
            </div>
        </div>

        <div class="zoom-box" v-show="state.zoomVisiable" ref="zoomBox">
            <img :src="state.previewImg" alt="" ref="bigImg">
        </div>

    </div>
</template>

<script setup>
import {reactive, defineProps, watch, onMounted,ref,defineEmits} from "vue";

const emits = defineEmits(['move','clickImg'])
const currentIndex = ref(0); //图片索引
const previewBox = ref()
const hoverBox = ref()
const middlePicture = ref()
const zoomBox = ref()
const bigImg = ref()
const state = reactive({
    zoomVisiable: false,
    hoverVisiable: false,
    pictureList: [],
    previewImg: '',
    middleLeft: 0, // 当前放置小图盒子的定位left值,
    itemWidth: 60, // 缩略图每张的宽度

    houverWidth: 0,
    houverHeight: 0,
    pWidth: 0,
    pHeight: 0,
    imgWidth: 0,
    imgHeight: 0,
    bWidth: 0,
    bHeight: 0,
    scroll: 0,

})

const props = defineProps(['imgList'])
function offset(el) {
    let top = el.offsetTop;
    let left = el.offsetLeft;
    while (el.offsetParent) {
        el = el.offsetParent;
        top += el.offsetTop;
        left += el.offsetLeft;
    }
    return {
        left: left,
        top: top
    }
}


function refreshList() {
    if (props.imgList && props.imgList.length>0) {
        state.pictureList = props.imgList;
        state.previewImg = state.pictureList[0].url;
    }
}
function out() {
    state.zoomVisiable = false;
}
function move(ev) {
    init();
    // 鼠标距离屏幕距离
    let moveX = ev.clientX;
    let moveY = ev.clientY;
    // 大盒子距离顶部的距离
    let offsetLeft = offset(previewBox.value).left;

    let offsetTop = offset(previewBox.value).top;
    let left = moveX - offsetLeft - state.houverWidth / 2;
    let top
    if(state.scroll > 0) {
        top = moveY - offsetTop + state.scroll - state.houverHeight / 2;
    }else {
        top = moveY - offsetTop - state.houverHeight / 2;
    }
    let maxWidth = state.pWidth - state.houverWidth;
    let maxHeight = state.pWidth - state.houverHeight;
    left = left < 0 ? 0 : left > maxWidth ? maxWidth : left;
    top = top < 0 ? 0 : top > maxHeight ? maxHeight : top;
    let percentX = left / (maxWidth);
    let percentY = top / (maxHeight);
    hoverBox.value.style.left = left + 'px';
    hoverBox.value.style.top = top  + 'px';
    bigImg.value.style.left = percentX * (state.bWidth - state.imgWidth) + 'px';
    bigImg.value.style.top = percentY * (state.bHeight - state.imgHeight) + 'px';
    emits('move', ev);
    state.zoomVisiable = true;
}
function init() {

    state.houverWidth = hoverBox.value.offsetWidth;
    state.houverHeight = hoverBox.value.offsetHeight;
    state.pWidth = previewBox.value.offsetWidth;
    state.pHeight = previewBox.value.offsetHeight;
    state.imgWidth = bigImg.value.offsetWidth;
    state.imgHeight = bigImg.value.offsetHeight;
    state.bWidth = zoomBox.value.offsetWidth;
    state.bHeight = zoomBox.value.offsetHeight;
    state.scroll = document.documentElement.scrollTop || document.body.scrollTop;
}
// 切换图片
function tabPicture (item,index) {
    state.previewImg = item.url;
    currentIndex.value = index;
}

function selectImg(item,index) {
    emits('clickImg',item)
}
// 点击左边箭头
function leftArrowClick () {
    if (currentIndex.value > 0) {
        currentIndex.value--;
        // 更新预览框的图片
        state.previewImg = state.pictureList[currentIndex.value].url;
        emits('clickImg',state.pictureList[currentIndex.value])

    }else {
        currentIndex.value = state.pictureList.length -1;
        state.previewImg = state.pictureList[currentIndex.value].url;
        emits('clickImg',state.pictureList[currentIndex.value])

    }
}
// 点击右边箭头
function rightArrowClick () {
    if (currentIndex.value < state.pictureList.length - 1) {
        currentIndex.value++;
        // 更新预览框的图片
        state.previewImg = state.pictureList[currentIndex.value].url;
        emits('clickImg',state.pictureList[currentIndex.value])
    }else {
        currentIndex.value=0;
        state.previewImg = state.pictureList[currentIndex.value].url;
        emits('clickImg',state.pictureList[currentIndex.value])

    }
}

watch(() => props.imgList,(newValue,oldValue) => {
    state.pictureList = newValue;
    refreshList();

},{deep:true})

onMounted(() => {
    refreshList()
})

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.magnify {
    position: relative;
    .left-container {
        width: 100%;
        height: 100%;
        .preview-box {
            width: 350px;
            height: 350px;
            border: 1px solid #dededd;
            position: relative;
            display: flex;
            align-items: center;
            &:hover .hover-box{
                display: block;
            }
            .hover-box {
                position: absolute;
                display: none;
                left: 0;
                top: 0;
                width: 100px;
                height: 100px;
                border: 1px solid #545454;
                background: url('https://img-tmdetail.alicdn.com/tps/i4/T12pdtXaldXXXXXXXX-2-2.png') repeat 0 0;
                cursor: move;
                user-select: none;
            }
        }
        .carousel {
            width: 100%;
            margin-top: 5px;
            height: 50px;
            display: -webkit-flex;
            align-items: center;
            justify-content: center;

            .arrow {
                flex-basis: 25px;
                cursor: pointer;
            }
            .show_box {
                overflow: hidden;
                position: relative;
                height: 50px;
                width: 260px;
                .picture_container {
                    width: 200%;
                    height: 100%;
                    position: absolute;
                    overflow: hidden;
                    top: 0;
                    left: 0;
                    margin: 0;
                    .picture_item {
                        width: 60px;
                        height: 100%;
                        float: left;
                        padding: 5px;
                        box-sizing: border-box;
                        .small_img {
                            width: 100%;
                            height: 100%;
                        }
                    }
                    .picture_item:hover {
                        border: 2px solid #f2019f;
                    }
                }
            }
        }
    }
    .zoom-box {
        background-color: #FFFFFF;
        width: 480px;
        height: 480px;
        z-index: 3;
        overflow: hidden;
        position: absolute;
        left: 355px;
        border: 1px solid #dc7a7a;;
        top: 0;
        img {
            position: absolute;
            top: 0;
            left: 0;
        }
    }
}
</style>
