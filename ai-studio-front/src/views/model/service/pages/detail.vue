<script setup>
import Codemirror, {createLinkMark, createLogMark, createTitle} from 'codemirror-editor-vue3';
import {useRouter} from "vue-router";
import {getServiceById, getServiceLogs} from "@/api/modelMag/service.js";
import Vditor from "vditor";

const router = useRouter();
const logOpt = {
    mode: 'log',
    lineNumbers: true,
    styleActiveLine: false,
    readOnly: 'nocursor',
    autofocus: false,
    lineWrapping: true
}
const state = reactive({
    c_service: {
        id: null,
        model: null,
        description: null,
        name: null,
        readme: '',
        serviceUrl: null
    },
    c_tab: 0,
    logs_con: ''
})

const serviceLogRef = ref()
const all_logs = ref([]) // 用来保存接口所有日志
let intervalId =ref( null)
let currentIndex = 0;

const labelStyle = {
    width: '120px',
    fontSize: '14px',
    color: '#86909c',
    fontWeight: 'normal'
}

const getDataByServiceId = async (id) => {
    const res = await getServiceById({id: id});
    if (res.code === 200) {
        for (let i in res.data) {
            if (state.c_service.hasOwnProperty(i)) {
                state.c_service[i] = res.data[i];
            }
        }
    }
}

const getLogs = async (id) => {
    const res = await getServiceLogs({serviceId: id});
    if (res.code === 200 && res.data && res.data.length !==0) {
        const newLogLines = res.data.map(item => `${item["@timestamp"]} - ${item.message}`);
        all_logs.value = newLogLines;
    }
}


const startLogging = () => {
    if (intervalId.value !== null) {
        clearInterval(intervalId.value); // 防止重复启动定时器
    }

    intervalId.value = setInterval(() => {
        if (currentIndex < all_logs.value.length) {
            state.logs_con += all_logs.value[currentIndex] + '\n';
            currentIndex++;
        } else {
            intervalId.value = null;
            clearInterval(intervalId.value); // 所有日志都已显示，停止定时器
        }
    }, 10); // 每秒显示一行，你可以根据需要调整这个时间间隔
};

const scrollToBottom = () => {
    if (serviceLogRef.value.cminstance) {
        const editor = serviceLogRef.value.cminstance;
        const doc = editor.getDoc();
        doc.setCursor({line: doc.size - 1, ch: doc.getLine(doc.size - 1).length});
        editor.scrollIntoView({line: doc.size - 1, ch: 0}, 10); // 10 是动画的持续时间（毫秒）
    }
};

const changeTab = (val) => {
    if (val === 1){
        nextTick(() => {
            Vditor.preview(document.getElementById('preview'), state.c_service.readme, {
                speech: {
                    enable: false,
                },
                anchor: 1,
                hljs: {
                    style: 'github'
                },
            })
        })
    }
}


onMounted( () => {
    const id = router.currentRoute.value.params.serviceId;
    getDataByServiceId(id);

    getLogs(id);

    startLogging()
})
</script>
<template>
    <div class="main-content">
        <div class="full-con">
            <el-page-header @back="router.go(-1)" class="page-header">
                <template #title>
                    <span class="back-text">返回</span>
                </template>
                <template #content>
                    <span class="header-title"> 服务详情 </span>
                </template>
            </el-page-header>

            <div class="content-plane">
                <div class="plane-item">
                    <a-descriptions :column="2" ref="formRef" layout="horizontal">
                        <template #title>
                            <div class="section-title">基本信息</div>
                        </template>
                        <a-descriptions-item label="服务名称" :label-style="labelStyle">
                            <div class="acud-form-item-extra">
                                {{ state.c_service?.name }}
                            </div>
                        </a-descriptions-item>

                        <a-descriptions-item label="服务描述" :label-style="labelStyle">
                            <div class="acud-form-item-extra">
                                {{ state.c_service.description }}
                            </div>
                        </a-descriptions-item>

                        <a-descriptions-item label="服务地址" :label-style="labelStyle">
                            <div class="acud-form-item-extra">
                                <a v-if="state.c_service?.serviceUrl" :href="state.c_service?.serviceUrl" target="_blank" class="link-text">
                                    {{ state.c_service?.serviceUrl }}
                                </a>
                                <span v-else>-</span>
                            </div>
                        </a-descriptions-item>

                        <a-descriptions-item label="绑定模型" :label-style="labelStyle">
                            <div class="acud-form-item-extra">
                                {{ state.c_service.model?.name }}
                            </div>
                        </a-descriptions-item>

                        <a-descriptions-item label="地域" :label-style="labelStyle">
                            <div class="acud-form-item-extra">
                                {{ '华北-北京' }}
                            </div>
                        </a-descriptions-item>
                    </a-descriptions>
                </div>

                <div class="plane-item">
                    <a-tabs v-model:active-key="state.c_tab" @change="changeTab">
                        <a-tab-pane :key="0" tab="服务日志">
                            <div class="log-wrapper">
                                <Codemirror
                                    ref="serviceLogRef"
                                    v-model:value="state.logs_con"
                                    :options="logOpt"
                                    class="log-editor"
                                    :height="600"
                                    placeholder="暂无日志"
                                    @change="scrollToBottom"></Codemirror>
                            </div>
                        </a-tab-pane>
                    </a-tabs>

                </div>
            </div>

        </div>

    </div>
</template>

<style scoped lang="scss">
.main-content {
    min-height: calc(100vh - 84px);
    background: #f2f3f5;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    overflow: auto;
    position: relative;

    .content-plane {
        background: transparent;
        padding: 20px !important;
        flex: 1 1;
        margin: 0 !important;

        .plane-item {
            background-color: #ffffff;
            border-radius: 4px;
            padding: 24px;
            margin-bottom: 16px;
            box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
        }
    }
}

.page-header {
    padding: 16px 24px;
    background: #ffffff;
    border-bottom: 1px solid #e5e6eb;
}

.back-text {
    color: #86909c;
    font-size: 14px;
    cursor: pointer;
    transition: color 0.2s;
    &:hover {
        color: #1d2129;
    }
}

.header-title {
    color: #1d2129;
    font-size: 16px;
    font-weight: 600;
}

.section-title {
    font-size: 16px;
    font-weight: 600;
    color: #1d2129;
    margin-bottom: 20px;
    padding-left: 12px;
    border-left: 4px solid #165dff;
    line-height: 1.4;
}

.acud-form-item-extra {
    color: #1d2129;
    min-height: auto;
    font-size: 14px;
    line-height: 1.5;
    word-break: break-all;
    word-wrap: break-word;
}

.link-text {
    color: #165dff;
    text-decoration: none;
    &:hover {
        text-decoration: underline;
    }
}

.log-wrapper {
    border: 1px solid #e5e6eb;
    border-radius: 4px;
    overflow: hidden;
}

.log-editor {
    font-size: 13px;
    line-height: 1.6;
    :deep(.CodeMirror) {
        height: 600px !important;
    }
}

.full-con {
    margin-bottom: 40px;
}
.vditor-reset--anchor{
    padding-top: 20px;
    padding-left: 20px;
}
</style>