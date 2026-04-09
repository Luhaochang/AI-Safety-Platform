<!--  图像分类标注控制台    -->
<script setup>
import JSZip from "jszip";

const zip = new JSZip();

import {UploadOutlined, DownOutlined} from "@ant-design/icons-vue";
import {useRouter} from "vue-router";
import {getTaskById, savaTask, submitTask} from "@/api/dataMag/markTask.js";
import {getDatasetById, getDatasetByPath} from "@/api/dataMag/dataset.js";

import fs from "fs"
import {ElMessage, ElMessageBox} from "element-plus";

const router = useRouter()
const statusOpt = [
    {
        label: '全部数据',
        value: 0
    },
    {
        label: '已标注',
        value: 1
    },
    {
        label: '未标注',
        value: 2
    },
]

const selectedIndex = ref()

const state = reactive({
    treeInfo: {
        temp: [],
        labelList: [
            {
                label: '标签1',
                value: 0
            },
            {
                label: '标签2',
                value: 1
            },
            {
                label: '标签3',
                value: 2
            },
            {
                label: '标签4',
                value: 3
            },
            {
                label: '标签5',
                value: 4
            },

        ]
    },
    labelInfo: {
        status: 0
    },
    queryParams: {
        pageNo: 1,
        pageSize: 5,
        total: 0
    },
    curImg: {},
    c_task: {
        "remark": null,
        "id": null,
        "dataSetId": null,
        "taskName": "",
        "taskDescription": " des",
        "label": "",
        "scene": null,

        "fileUrl": '',
        "markUrl": null
    }
})

const formatType = (val) => {
    if (val !== '') {
        return '已标注';
    } else return ''
}

const formatLabel = (val) => {
    if (val) {
        return state.treeInfo.labelList.filter(item => item.value === val)[0].label;
    }
}

const handleSizeChange = (val) => {
}

const handleCurrentChange = (val) => {
    state.queryParams.pageNo = val;

}

const currentPageImages = computed(() => {
    const start = (state.queryParams.pageNo - 1) * state.queryParams.pageSize;
    const end = start + state.queryParams.pageSize;
    return state.treeInfo.temp.slice(start, end);
});


// 读取zip
const loadZip = async (url) => {
    const response = await getDatasetByPath({catalogue: `${url}/images`});
    if (response.data && response.code === 200 && response.data.length !== 0) {

        const zipData = response.data;
        state.treeInfo.temp = [];

        let result = {
            blobArr: [],
            data: {}
        };

        let index = 0;
        for (let key in zipData) {
            let tempName = zipData[key].split('/').pop();

            if (tempName.indexOf('.png') !== -1 || tempName.indexOf('.jpg') !== -1 || tempName.indexOf('.jpeg') !== -1) {
                result.blobArr.push({name: tempName, url: zipData[key], label: '', id: index})
                index++;
            }
        }
        state.treeInfo.temp = result.blobArr;
        state.queryParams.total = state.treeInfo.temp.length;
    }
}

// 读取json赋予label
const loadJsonFromFile = async (url) => {
    if (url) {
        const res = await getDatasetByPath({catalogue: `${url}`});

        function getJsonUrls(urls) {
            const jsonUrls = [];
            urls.forEach(url => {
                if (url.endsWith('.json')) {
                    jsonUrls.push(url);
                }
            });
            return jsonUrls;
        }

        const jsonUrls = getJsonUrls(res.data);


        const timestamp = new Date().getTime(); // 获取当前时间戳
        const newUrl = `${jsonUrls[0]}?timestamp=${timestamp}`;
        fetch(newUrl).then(response => {
            return response.json();
        }).then(data => {
            data.forEach((item, index) => {
                // 假设每个item只有一个classification_tag
                let classificationTag = item.type;

                state.treeInfo.temp.forEach(i => {
                    if (i.name === item.image_name) {
                        i.label = classificationTag ? classificationTag : ''
                    }
                })

            });
        })
    } else {
        return;
    }
}
const selectImg = (item, index) => {
    selectedIndex.value = index;
    state.curImg = item;
}

const changeImgLabel = (val) => {
    state.treeInfo.temp.filter(i => i.id === state.curImg.id)[0].label = val;
}

const saveDataSet = () => {
    let saveArr = []
    state.treeInfo.temp.forEach(item => {
        saveArr.push(
            {
                image_name: item.name,
                type: item.label === '' ? '' : item.label
            }
        )
    })
    const temp = JSON.stringify(saveArr)
    let file = new File([new Blob([temp], {type: 'text/json'})], 'file.json')

    const formData = new FormData()
    formData.append('file', file);

    let count = 0;
    state.treeInfo.temp.forEach(item => {
        if (item.label !== '') {
            count++;
        }
    });
    savaTask(state.c_task.id, count, formData).then(res => {
        if (res.code === 200) {
            ElMessage.success('保存成功');
        }
    })

}

const goback = () => {
    router.go(-1);
}

const getTask = async (id) => {
    const res = await getTaskById(id);
    if (res.code === 200) {
        state.c_task = res.data;
        state.treeInfo.labelList = JSON.parse(state.c_task.label).map(item => ({
            label: item.name,
            value: item.name
        }));

        const {fileUrl, markUrl} = await getUrl(state.c_task.dataSetId);
        state.c_task.fileUrl = fileUrl;
        state.c_task.markUrl = markUrl;

    }
}

const getUrl = async (id) => {
    const res = await getDatasetById(id);
    if (res.code === 200) {
        return {fileUrl: res.data.fileUrl, markUrl: res.data.markUrl};
    }
}


const finish = () => {
    ElMessageBox.confirm('当前操作不可逆，确定提交该任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        let saveArr = []
        state.treeInfo.temp.forEach(item => {
            saveArr.push(
                {
                    image_name: item.name,
                    type: item.label === '' ? '' : item.label

                }
            )
        })

        const temp = JSON.stringify(saveArr)
        let file = new File([new Blob([temp], {type: 'text/json'})], 'file.txt')

        const formData = new FormData()
        formData.append('file', file)

        let count = 0;
        state.treeInfo.temp.forEach(item => {
            if (item.label !== '') {
                count++;
            }
        });


        submitTask(state.c_task.id, count, formData).then(res => {
            if (res.code === 200) {
                router.go(-1);
                ElMessage.success('提交成功');
            }
        })
    }).catch(() => {
    });
}

onMounted(async () => {
    let taskID = router.currentRoute.value.params.taskId;

    getTask(taskID)
        .then(() => {
            loadZip(state.c_task.fileUrl)
                .then(() => {
                    loadJsonFromFile(state.c_task.fileUrl)
                })
        })
})
</script>

<template>
    <div>
        <a-page-header
            @back="goback"
            :title="state.c_task.taskName">
            <template #extra>
                <el-button @click="saveDataSet">保存</el-button>
                <el-button type="primary" @click="finish">提交</el-button>
            </template>
        </a-page-header>

        <div class="w-full" style="background: rgb(243,244,247);min-height: calc(100vh - 164px);padding: 30px 50px">
            <div class="bg-white flex flex-row">
                <div class="w-[70%] flex justify-end" style="padding: 20px 50px 20px;border: silver 1px solid">
                    <el-space :size="40">
                        <el-popover
                            placement="top-start"
                            title="使用说明"
                            :width="200"
                            trigger="hover"
                            content="勾选图片然后选择标签"
                        >
                            <template #reference>
                                <el-text>
                                    快捷键
                                    <el-icon size="15">
                                        <InfoFilled/>
                                    </el-icon>
                                </el-text>
                            </template>
                        </el-popover>

                        <el-text>作业状态</el-text>

                        <el-select v-model="state.labelInfo.status" class="w-[100px]">
                            <el-option v-for="item in statusOpt" :value="item.value" :label="item.label"></el-option>
                        </el-select>
                    </el-space>
                </div>
                <div class="w-[30%] flex items-center font-bold	"
                     style="padding:20px 50px  20px ;border: silver 1px solid">标注控制台
                </div>
            </div>
            <div class="bg-white flex flex-row">
                <div class="w-[70%] flex flex-col flex-nowrap relative min-h-[200px] gap-4"
                     style="padding: 20px 50px;border: silver 1px solid">
                    <div class="flex flex-row gap-7 flex-wrap w-full justify-between"
                         v-if="state.treeInfo.temp.length !==0">
                        <div v-for="(item,index) in currentPageImages" @click="selectImg(item,index)" class="relative">
                            <!--                            <el-image fit="fill" :preview-src-list="previewSrcList" :initial-index="index" :src="item.url" class="img-card" :class="{'label-scene-list-active': selectedIndex === index}"/>-->
                            <el-image fit="fill" :src="item.url" class="img-card"
                                      :class="{'label-scene-list-active': selectedIndex === index}"/>
                            <el-tag type="primary" v-if="item.label !== ''" class="absolute top-1 left-1">
                                {{ formatType(item.label) }}
                            </el-tag>
                            <a-tag :bordered="false" color="cyan" v-if="item.label !==''"
                                   class="absolute bottom-2 left-1">{{ formatLabel(item.label) }}
                            </a-tag>
                        </div>
                    </div>

                    <a-empty description="暂无图片" v-else></a-empty>

                    <div class="flex justify-end items-center">
                        <el-pagination
                            :current-page="state.queryParams.pageNo"
                            :page-size="state.queryParams.pageSize"
                            layout="total, prev, pager, next"
                            :total="state.queryParams.total"
                            @size-change="handleSizeChange"
                            @current-change="handleCurrentChange"
                        ></el-pagination>
                    </div>


                </div>
                <div class="w-[30%] flex" style="border: silver 1px solid;padding: 20px 20px">
                    <a-space direction="vertical" :size="30">
                        <div>分类结果</div>
                        <el-radio-group v-model="state.curImg.label" @change="changeImgLabel">
                            <el-radio class="mb-2" :value="item.value" border v-for="item in state.treeInfo.labelList">
                                {{ item.label }}
                            </el-radio>
                        </el-radio-group>
                    </a-space>

                </div>
            </div>


        </div>


    </div>
</template>

<style scoped lang="scss">
:deep(.el-select__wrapper) {
    box-shadow: none;
}

:deep(.el-select__wrapper.is-hovering:not(.is-focused)) {
    box-shadow: none;
}

.img-card {
    width: 200px;
    height: 150px;
    object-fit: fill;
}

.img-card:hover {
    border: #1c84c6 1px solid;
}

.label-scene-list-active {
    border: #1c84c6 1px solid;
}

</style>
