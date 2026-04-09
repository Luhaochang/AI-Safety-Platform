<script setup>
import {useRouter} from "vue-router";
import {TagOutlined, UploadOutlined, FileImageOutlined, FileOutlined, DeleteOutlined, PlusOutlined, DownloadOutlined, CloudUploadOutlined, FolderOutlined, PieChartOutlined, HistoryOutlined, ExportOutlined, FileTextOutlined, TableOutlined, SearchOutlined} from "@ant-design/icons-vue";
import {ArrowRight, Timer} from "@element-plus/icons-vue";
import JSZip from "jszip";
import img from "@/assets/icons/svg/image.svg";
import audio from "@/assets/icons/svg/audio.svg";
import video from "@/assets/icons/svg/video.svg";
import xml from "@/assets/icons/svg/xml.svg";
import other from "@/assets/icons/svg/其他.svg";
import {ElMessage, ElMessageBox} from "element-plus";
import * as echarts from 'echarts';
import {
    deleteDatasetById,
    deleteSImg, downloadDataset,
    getDatasetById,
    getDatasetByPath,
    packageDataset,
    uploadDataSet,
    getDatasetStats,
    getDatasetVersions,
    createDatasetVersion,
    exportDatasetFormat,
    importPreAnnotations
} from "@/api/dataMag/dataset.js";
import CreateDatasetDailog from "@/views/dataset/components/createDatasetDailog.vue";
import useUserStore from "@/store/modules/user.js";
import UploadDatasetDialog from "@/views/dataset/components/uploadDatasetDialog.vue";

const zip = new JSZip();
const activeKey = ref(1)
const router = useRouter()
const dialogRef = ref()
const uploadDialogRef = ref()
const tableRef = ref()
const state = reactive({
    id: 0,
    fileList: [],
    c_dataset: {
        "createUser": null,
        "createUserName": null,
        "createTime": null,
        "updateUser": null,
        "updateUserName": null,
        "updateTime": null,
        "remark": null,
        "id": null,
        "dataSetName": null,
        "scene": null,
        "dataSetAbstract": null,
        "fileUrl": null,
        "markUrl": null,
        "tag": null,
        "agreement": null,
        "dateAuthor": null,
        "isPublic": null,
        "isMarked": 0,
        "isChecked": null,
        "isOfficial": null,
        isPackage: null,
        size: 0,
        type: 1
    }
})

// Preview State
const imagePreview = reactive({
    visible: false,
    url: '',
    labels: []
});
const tablePreview = reactive({
    visible: false,
    loading: false,
    headers: [],
    rows: [],
    fileName: ''
});
const textPreview = reactive({
    visible: false,
    loading: false,
    content: '',
    fileName: ''
});


// Pagination & Search
const pagination = reactive({
    currentPage: 1,
    pageSize: 10,
    total: 0
})
const filterText = ref('')

const filteredList = computed(() => {
    let list = state.fileList;
    if (filterText.value) {
        list = list.filter(item => item.fileName.toLowerCase().includes(filterText.value.toLowerCase()))
    }
    return list
})

watch(() => filteredList.value, (val) => {
    pagination.total = val.length
    if ((pagination.currentPage - 1) * pagination.pageSize >= pagination.total && pagination.total > 0) {
        pagination.currentPage = 1
    }
}, { immediate: true })

const paginatedList = computed(() => {
    const start = (pagination.currentPage - 1) * pagination.pageSize
    const end = start + pagination.pageSize
    return filteredList.value.slice(start, end)
})

watch(() => paginatedList.value, () => {
    nextTick(() => {
        if (tableRef.value) {
            tableRef.value.doLayout();
        }
    })
})

const handleSizeChange = (val) => {
    pagination.pageSize = val
    pagination.currentPage = 1
}

const handleCurrentChange = (val) => {
    pagination.currentPage = val
}

const handleCommand = (item) => {
    switch (item) {
        case 'delete':
            deleteDataset();
            break
        case 'update':
            dialogRef.value.setForm(state.c_dataset);
            break;
        case 'upload':
            uploadDialogRef.value.setForm(state.c_dataset, 'upload');
            break;
        case 'importPre':
            uploadDialogRef.value.setForm(state.c_dataset, 'importPre');
            break;
    }
}

const getDetailInfoById = async (id) => {
    const res = await getDatasetById(id)
    if (res.code === 200) {
        state.c_dataset = { ...state.c_dataset, ...res.data };
        state.c_dataset.scene = res.data.scene;
        state.c_dataset.dataType = res.data.dataType;
        state.c_dataset.type = Number(res.data.type || res.data.dataType || 1);

        if (res.data.tag) {
            try {
                state.c_dataset.tag = JSON.parse(res.data.tag);
            } catch (e) {
                state.c_dataset.tag = [];
            }
        }
    }
}

const deleteDataset = () => {
    ElMessageBox.confirm('确定删除该数据集吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        deleteDatasetById({id: state.id}).then(res => {
            if (res.code === 200) {
                ElMessage.success('删除成功')
                router.go(-1);
            }
        })
    }).catch(() => {
    });
}


async function getImageFileSize(imageUrl) {
    try {
        const response = await fetch(imageUrl);
        const contentLength = response.headers.get('Content-Length');
        if (contentLength) {
            const sizeInBytes = parseInt(contentLength, 10);
            const sizeInMB =
                (sizeInBytes / (1024 * 1024)).toFixed(2)
            return sizeInMB;
        } else {
            throw new Error('Content-Length header not found');
        }
    } catch (error) {
        console.error('Failed to fetch image size:', error);
        return null;
    }
}

const getBasePath = () => {
    return state.c_dataset.type === 1 ? state.c_dataset.fileUrl + '/images' : state.c_dataset.fileUrl;
}

const loadZip = async (url) => {
    let path = getBasePath();

    const res = await getDatasetByPath({catalogue: path});
    if (res.code === 200) {
        state.fileList = await Promise.all(res.data.map(async item => {
            return {
                url: item,
                size: 0,
                fileName: item.split('/').pop(),
                isClick: false,
                labels: []
            };
        }));

        if (state.c_dataset.isMarked === 2 && state.c_dataset.type === 1) {
            await loadLabels();
        }

        nextTick(() => {
            if (tableRef.value) {
                tableRef.value.doLayout();
            }
        })
    }
}

const loadLabels = async () => {
    const labelsPath = state.c_dataset.fileUrl + '/labels';
    try {
        const res = await getDatasetByPath({catalogue: labelsPath});
        if (res.code === 200) {
            const labelFiles = res.data;
            const labelMap = {};
            labelFiles.forEach(url => {
                const fileName = url.split('/').pop();
                const nameNoExt = fileName.replace(/\.[^/.]+$/, "");
                labelMap[nameNoExt] = url;
            });

            await Promise.all(state.fileList.map(async (file) => {
                const nameNoExt = file.fileName.replace(/\.[^/.]+$/, "");
                if (labelMap[nameNoExt]) {
                    try {
                        const response = await fetch(labelMap[nameNoExt]);
                        const text = await response.text();
                        file.labels = parseYoloLabels(text);
                    } catch (e) {
                        console.error(`Failed to load label for ${file.fileName}`, e);
                    }
                }
            }));
        }
    } catch (e) {
        console.error("Failed to load labels directory", e);
    }
}

const parseYoloLabels = (text) => {
    const lines = text.trim().split('\n');
    return lines.map(line => {
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 5) {
            return {
                class: parts[0],
                x: parseFloat(parts[1]),
                y: parseFloat(parts[2]),
                w: parseFloat(parts[3]),
                h: parseFloat(parts[4])
            };
        }
        return null;
    }).filter(item => item !== null);
}

const openTablePreview = async (file) => {
    tablePreview.fileName = file.fileName;
    tablePreview.visible = true;
    tablePreview.loading = true;
    try {
        const response = await fetch(file.url);
        const text = await response.text();
        const { headers, rows } = parseCsvData(text);
        tablePreview.headers = headers;
        tablePreview.rows = rows;
    } catch (e) {
        ElMessage.error("加载或解析CSV文件失败");
        console.error(e);
    } finally {
        tablePreview.loading = false;
    }
}

const parseCsvData = (csvText) => {
    const lines = csvText.trim().split('\n');
    const headers = lines[0].split(',').map(h => h.trim());
    // Optimization: Only parse top 100 rows for preview to avoid freezing
    const rows = lines.slice(1, 101).map(line => {
        const values = line.split(',');
        let row = {};
        headers.forEach((header, index) => {
            row[header] = values[index] ? values[index].trim() : '';
        });
        return row;
    });
    return { headers, rows };
}

const openTextPreview = async (file) => {
    textPreview.fileName = file.fileName;
    textPreview.visible = true;
    textPreview.loading = true;
    try {
        const response = await fetch(file.url);
        const text = await response.text();

        if (file.fileName.endsWith('.jsonl')) {
            // Handle JSONL: parse each line
            const lines = text.trim().split('\n').filter(line => line);
            const jsonObjects = lines.map((line, index) => {
                try {
                    return JSON.parse(line);
                } catch (e) {
                    console.error(`Error parsing line ${index + 1} in ${file.fileName}:`, e);
                    return { error: `Invalid JSON on line ${index + 1}`, content: line };
                }
            });
            textPreview.content = JSON.stringify(jsonObjects, null, 2);
        } else {
            // Handle JSON or plain text
            try {
                const json = JSON.parse(text);
                textPreview.content = JSON.stringify(json, null, 2);
            } catch (e) {
                textPreview.content = text; // Fallback to raw text
            }
        }
    } catch (e) {
        ElMessage.error("加载文本文件失败");
        console.error(e);
    } finally {
        textPreview.loading = false;
    }
}


const downloadZip = async () => {
    let url;
    const res = await downloadDataset({datasetId: state.c_dataset.id});
    if (res.code === 200) {
        url = res.data;
        const link = document.createElement('a');
        link.href = url;
        link.download = state.c_dataset.dataSetName + '.zip';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
}

const packageData = async () => {
    const res = await packageDataset({datasetId: state.c_dataset.id});
    if (res.code === 200) {
        ElMessage.success('打包中，请等待打包完成');
    }
}

const initPage = async () => {
    try {
        state.id = router.currentRoute.value.params.detailId;
        await getDetailInfoById(state.id);
        if (state.c_dataset.fileUrl) {
            await loadZip(state.c_dataset.fileUrl);
        } else {
            console.error('fileUrl is not available');
        }
    } catch (e) {
        console.error('Error during fetching data or loading zip:', e);
    }
}

const beforeUploadFile = (file) => {
    if (state.c_dataset.type === 1) {
        const isPNG = file.type === 'image/png' || file.type === 'image/jpeg' || file.type === 'image/jpg';
        if (!isPNG) {
            ElMessage.error(`${file.name} is not a png/jpg file`);
            return false;
        }
        return isPNG;
    }
    return true;
}

const uploadFile = async (e) => {
    const uploadDataSetByPath = async (path) => {
        let formData = new FormData();
        formData.append('file', e.file);
        await uploadDataSet(formData, path);
    }
    const generateJsonFile = async () => {
        if (state.c_dataset.type !== 1) return;
        const jsonData = state.fileList.map(item => ({
            image_name: item.fileName,
            type: ''
        }))
        const jsonString = JSON.stringify(jsonData, null, 2);
        let file = new File([new Blob([jsonString], {type: 'text/json'})], 'file.json')
        let formData = new FormData();
        formData.append('file', file);
        await uploadDataSet(formData, state.c_dataset.fileUrl);
    }
    await uploadDataSetByPath(getBasePath())
    await initPage();
    await generateJsonFile();
}

const deleteFile = (scope) => {
    ElMessageBox.confirm('确定删除该文件吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(async () => {
        state.fileList.splice(scope.$index, 1);
        const jsonData = state.fileList.map(item => ({
            image_name: item.fileName,
            type: ''
        }))
        const jsonString = JSON.stringify(jsonData, null, 2);
        let file = new File([new Blob([jsonString], {type: 'text/json'})], 'file.json')
        let formData = new FormData();
        formData.append('file', file);
        const res = await deleteSImg({
            catalogue: `${getBasePath()}/${scope.row.fileName}`,
            datasetId: state.c_dataset.id
        }, formData);
        if (res.code === 200) {
            ElMessage.success('删除成功');
            await initPage();
        }
    }).catch(() => {
    });
}

let loading = ref(false)

const callbackUpload = () => {
    loading.value = true;
}

const endLoading = () => {
    loading.value = false;
    initPage()
}

function hasPermission(permission) {
    const permissions = useUserStore().permissions;
    return permissions.includes(permission) || permissions.includes('*:*:*');
}

const checkSize = async (row) => {
    let size;
    try {
        size = await getImageFileSize(row.url);
    } catch (error) {
        console.error('Error fetching image size:', error);
        size = 0;
    }
    row.size = size;
    row.isClick = true;
}

const openImagePreview = (file) => {
    imagePreview.url = file.url;
    imagePreview.labels = file.labels;
    imagePreview.visible = true;
}

const downloadSingleFile = (row) => {
    const link = document.createElement('a');
    link.href = row.url;
    link.setAttribute('download', row.fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
};

const getDataTypeLabel = (type) => {
    switch (type) {
        case 1: return '图片';
        case 2: return '文本';
        case 3: return '表格';
        default: return '未知';
    }
}

const getDataTypeIcon = (type) => {
    switch (type) {
        case 1: return FileImageOutlined;
        case 2: return FileTextOutlined;
        case 3: return TableOutlined;
        default: return FileImageOutlined;
    }
}

watch(activeKey, (val) => {
    if (val === 1) {
        nextTick(() => {
            if (tableRef.value) {
                tableRef.value.doLayout();
            }
        });
    }
});

onMounted(() => {
    initPage()
})
</script>

<template>
    <div class="flex p-0 flex-row" style="min-height: calc(100vh - 84px);">
        <div class="flex-1 overflow-x-hidden min-w-800">
            <div class="min-h-full min-w-800">
                <div class="min-w-min h-full py-6 px-4">
                    <!--         面包屑           -->
                    <div class="bg-transparent mb-4">
                        <el-breadcrumb :separator-icon="ArrowRight">
                            <el-breadcrumb-item :to="{path: '/datamag/dataset'}">数据集</el-breadcrumb-item>
                            <el-breadcrumb-item>{{ state.c_dataset.dataSetName }}</el-breadcrumb-item>
                        </el-breadcrumb>
                    </div>

                    <!--        主体            -->
                    <div class="relative w-full h-full">
                        <!--          header              -->
                        <div class="bg-white rounded-lg p-6 mb-4 shadow-sm">
                            <div class="base-info">
                                <div class="flex justify-between items-start mb-4">
                                    <div class="flex items-center gap-3">
                                        <div class="text-xl font-semibold text-[#140e35]">
                                            {{ state.c_dataset.dataSetName }}
                                        </div>
                                        <el-tag v-if="state.c_dataset.isPublic === 0" type="info" effect="plain" size="small">私有</el-tag>
                                        <el-tag v-else type="success" effect="plain" size="small">公开</el-tag>
                                        <el-tag type="warning" effect="plain" size="small">{{ getDataTypeLabel(state.c_dataset.type) }}</el-tag>
                                    </div>

                                    <div class="operate flex gap-2">
                                        <el-button v-if="state.c_dataset.isPublic === 0" icon="unlock" plain>设为公开</el-button>
                                        <el-dropdown @command="handleCommand">
                                            <el-button icon="more" plain></el-button>
                                            <template #dropdown>
                                                <el-dropdown-menu>
                                                    <div
                                                        v-if="state.c_dataset.isMarked === 0 || (state.c_dataset.isMarked === 2 && state.c_dataset.isChecked===0)"
                                                        v-hasPermi="['datamag:dataset:edit']">
                                                        <el-dropdown-item :command="'upload'">
                                                            上传数据
                                                        </el-dropdown-item>
                                                        <el-dropdown-item :command="'importPre'" v-if="state.c_dataset.type === 1">
                                                            导入预标注
                                                        </el-dropdown-item>
                                                    </div>
                                                    <div
                                                        v-hasPermi="['datamag:dataset:edit']">
                                                        <el-dropdown-item :command="'update'">
                                                            修改信息
                                                        </el-dropdown-item>
                                                    </div>
                                                    <div
                                                        v-hasPermi="['datamag:dataset:remove']">
                                                        <el-dropdown-item :command="'delete'" class="text-red-500">
                                                            删除数据集
                                                        </el-dropdown-item>
                                                    </div>
                                                </el-dropdown-menu>
                                            </template>
                                        </el-dropdown>
                                    </div>
                                </div>

                                <div class="text-[#565772] text-sm mb-4 leading-relaxed">
                                    {{ state.c_dataset.dataSetAbstract || '暂无描述' }}
                                </div>

                                <div class="flex items-center gap-6 text-sm text-[#666]">
                                    <div class="flex items-center gap-2">
                                        <TagOutlined />
                                        <div class="flex gap-2">
                                            <el-tag v-for="tag in state.c_dataset.tag" :key="tag" type="info" size="small" effect="light">
                                                {{ tag }}
                                            </el-tag>
                                            <span v-if="!state.c_dataset.tag?.length">无标签</span>
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <el-icon><Timer/></el-icon>
                                        <span>{{ state.c_dataset.createTime }}</span>
                                    </div>
                                </div>
                            </div>
                        </div>


                        <!--         tab               -->
                        <div class="bg-white rounded-lg shadow-sm min-h-[500px]">
                            <a-tabs v-model:activeKey="activeKey" class="px-6 pt-2">
                                <a-tab-pane :key="1" tab="数据预览">
                                    <div class="py-4">
                                        <div class="flex justify-between items-center mb-4">
                                            <div class="flex items-center gap-4">
                                                <div class="text-base font-medium flex items-center gap-2">
                                                    <component :is="getDataTypeIcon(state.c_dataset.type)" />
                                                    <span>文件列表</span>
                                                </div>
                                                <el-input
                                                    v-model="filterText"
                                                    placeholder="搜索文件名"
                                                    prefix-icon="Search"
                                                    style="width: 200px"
                                                    clearable
                                                />
                                            </div>

                                            <div class="flex gap-3">
                                                <a-upload
                                                    v-if="state.c_dataset.isMarked === 0"
                                                    :show-upload-list="false"
                                                    max-count="1"
                                                    :before-upload="beforeUploadFile"
                                                    :custom-request="uploadFile"
                                                >
                                                    <el-button type="primary" plain icon="Plus">
                                                        添加文件
                                                    </el-button>
                                                </a-upload>

                                                <el-button type="primary" plain icon="Box"
                                                           v-if="state.c_dataset.isPackage===0"
                                                           @click="packageData">打包下载
                                                </el-button>

                                                <el-button type="primary" plain icon="Loading"
                                                           v-else-if="state.c_dataset.isPackage === 2"
                                                           disabled
                                                >打包中
                                                </el-button>

                                                <el-button type="primary" plain icon="Download"
                                                           v-else
                                                           @click="downloadZip">下载数据集
                                                </el-button>
                                            </div>
                                        </div>

                                        <div class="border rounded-lg overflow-hidden">
                                            <div class="flex h-full">
                                                <!-- Right Content -->
                                                <div class="flex-1 p-4 bg-white min-h-[400px]">
                                                    <a-spin tip="上传中..." :spinning="loading">
                                                        <el-table
                                                            ref="tableRef"
                                                            :key="`${state.c_dataset.isMarked}-${state.c_dataset.type}`"
                                                            :data="paginatedList"
                                                            style="width: 100%"
                                                            :header-cell-style="{background:'#f8f9fa',color:'#606266'}"
                                                        >
                                                            <el-table-column label="文件名" prop="fileName" min-width="200">
                                                                <template #default="scope">
                                                                    <div class="flex items-center gap-2">
                                                                        <FileOutlined class="text-gray-400" />
                                                                        <span class="truncate" :title="scope.row.fileName">{{ scope.row.fileName }}</span>
                                                                    </div>
                                                                </template>
                                                            </el-table-column>

                                                            <el-table-column label="大小" width="120" align="center">
                                                                <template #default="scope">
                                                                    <el-button link type="primary" size="small" v-if="!scope.row.isClick" @click="checkSize(scope.row)">点击查看</el-button>
                                                                    <span v-if="scope.row.isClick" class="text-gray-500 text-sm">{{ scope.row.size + ' MB' }}</span>
                                                                </template>
                                                            </el-table-column>

                                                            <el-table-column label="操作" width="120" align="center">
                                                                <template #default="scope">
                                                                    <el-button v-if="state.c_dataset.type === 1" type="primary" link icon="View" @click="openImagePreview(scope.row)" title="预览图片" />
                                                                    <el-button v-if="state.c_dataset.type === 3 && scope.row.fileName.endsWith('.csv')" type="primary" link icon="View" @click="openTablePreview(scope.row)" title="预览表格" />
                                                                    <el-button v-if="state.c_dataset.type === 2 && (scope.row.fileName.endsWith('.json') || scope.row.fileName.endsWith('.jsonl'))" type="primary" link icon="View" @click="openTextPreview(scope.row)" title="预览文本" />
                                                                    <el-button type="primary" link icon="Download" @click="downloadSingleFile(scope.row)" title="下载文件" />
                                                                    <el-button v-if="state.c_dataset.isMarked === 0" type="danger" link icon="Delete" @click="deleteFile(scope)" title="删除文件" />
                                                                </template>
                                                            </el-table-column>
                                                        </el-table>

                                                        <div class="mt-4 flex justify-end" v-if="pagination.total > 0">
                                                            <el-pagination
                                                                v-model:current-page="pagination.currentPage"
                                                                v-model:page-size="pagination.pageSize"
                                                                :page-sizes="[10, 20, 50, 100]"
                                                                layout="total, sizes, prev, pager, next, jumper"
                                                                :total="pagination.total"
                                                                @size-change="handleSizeChange"
                                                                @current-change="handleCurrentChange"
                                                            />
                                                        </div>

                                                        <div v-if="state.fileList.length === 0" class="py-10 text-center text-gray-400">
                                                            <el-empty description="暂无数据文件" :image-size="100"></el-empty>
                                                        </div>
                                                    </a-spin>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </a-tab-pane>
                            </a-tabs>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <create-dataset-dailog ref="dialogRef" @commit="initPage"></create-dataset-dailog>

        <upload-dataset-dialog ref="uploadDialogRef" @commit="endLoading"
                               @start-load="callbackUpload"></upload-dataset-dialog>

        <!-- Custom Image Preview Dialog -->
        <el-dialog v-model="imagePreview.visible" title="图片预览" width="80%" top="5vh">
            <div class="relative w-full h-full flex items-center justify-center">
                <div class="relative">
                    <img :src="imagePreview.url" alt="Preview" class="max-w-full max-h-[80vh] object-contain">
                    <div v-if="imagePreview.labels && imagePreview.labels.length > 0" class="absolute inset-0">
                        <div v-for="(box, index) in imagePreview.labels" :key="index"
                             class="absolute border-2 border-red-500"
                             :style="{
                                 left: (box.x - box.w / 2) * 100 + '%',
                                 top: (box.y - box.h / 2) * 100 + '%',
                                 width: box.w * 100 + '%',
                                 height: box.h * 100 + '%'
                             }"
                        ></div>
                    </div>
                </div>
            </div>
        </el-dialog>

        <!-- Custom Table Preview Dialog -->
        <el-dialog v-model="tablePreview.visible" :title="`预览: ${tablePreview.fileName}`" width="80%" top="5vh">
            <el-table v-loading="tablePreview.loading" :data="tablePreview.rows" stripe style="width: 100%" max-height="70vh">
                <el-table-column v-for="header in tablePreview.headers" :key="header" :prop="header" :label="header" />
            </el-table>
        </el-dialog>

        <!-- Custom Text Preview Dialog -->
        <el-dialog v-model="textPreview.visible" :title="`预览: ${textPreview.fileName}`" width="80%" top="5vh">
            <div v-loading="textPreview.loading" class="bg-gray-100 p-4 rounded max-h-[70vh] overflow-y-auto">
                <pre class="whitespace-pre-wrap break-all">{{ textPreview.content }}</pre>
            </div>
        </el-dialog>

    </div>
</template>

<style scoped lang="scss">
/* 移除旧的样式，使用 Tailwind CSS 类 */
:deep(.ant-tabs-nav) {
    margin-bottom: 0;
    border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-tabs-tab) {
    padding: 12px 0;
    margin: 0 32px 0 0;
    font-size: 15px;
}

:deep(.ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
    color: #140e35;
    font-weight: 600;
    text-shadow: none;
}

:deep(.ant-tabs-ink-bar) {
    background: #2932e1;
}
</style>
