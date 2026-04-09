<template>
    <div>
        <el-dialog title="选择窗口" v-model="selectDialogFormVisible" width="45%" :lock-scroll="false"
                   :append-to-body="true" @close="handleClose">
            <div class="h-[400px] overflow-auto">
                <el-row type="flex" justify="space-between">
                    <el-col :span="14">
                        <el-select v-model="category" style="width: 30%">
                            <el-option
                                v-for="item in categories"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value">
                            </el-option>
                        </el-select>
                        <el-input style="width: 40%" v-model="inputValue" @keydown.enter="getList">
                            <template #append>
                                <el-button icon="Search" @click="handleSearch"></el-button>
                            </template>
                        </el-input>
                    </el-col>
                    <el-col :span="6">
                        <el-button type="primary" @click="commit">选择</el-button>
                        <el-button @click="reset">清除</el-button>
                    </el-col>
                </el-row>
                <!-- 表格       -->
                <table-area style="margin-top: 20px" :items="items" :table-data="tableData" :count="count"
                            :current-page="currentPage" ref="multipleTable"
                            @emit-pageSize="handleSizeChange"
                            @emit-pageChange="handleCurrentChange"
                            @emit-sort-change="sortChange"
                            @emit-select="handleSelect"></table-area>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import {defineComponent, getCurrentInstance, reactive, ref} from "vue";
import tableArea from "@/components/tableArea/tableArea.vue";
import {ElMessage} from "element-plus";
import {listDataset} from "@/api/dataMag/dataset.js";

const props = defineProps(['isMarked','scene'])

const {proxy} = getCurrentInstance()
//控制窗口是否显示
let selectDialogFormVisible = ref(false)
//select选中的值
let category = ref("")
//输入的值
let inputValue = ref("")
//单选和多选的值
let select = ref([])
//查询种类
let categories = reactive([
    {
        value: ref('serialNumberType'),
        label: ref('单据规则分类')
    }
])
//表头
let items = reactive([
    {
        prop: ref("dataSetName"),
        label: ref("数据集名称"),
        isSort: 'custom'
    },
    {
        prop: ref("dataSetAbstract"),
        label: ref("数据集介绍"),
        isSort: 'custom'
    }
])
let tableData = ref([])
let currentPage = ref(1)
let count = ref(0)
let pageSize = ref(10)
let Asc = ref([])
let Desc = ref([])

//打开对话框
function openDialog() {
    selectDialogFormVisible.value = true;
    getList();
}

//清除
function reset() {
    inputValue.value = "";
    category.value = "";
    getList();
}

function handleSearch() {
    currentPage.value = 1;
    getList();
}

//页面大小改变
function handleSizeChange(val) {
    pageSize.value = val;
    currentPage.value = 1;
    getList();
}

//当前页改变
function handleCurrentChange(val) {
    currentPage.value = val;
    getList();
}

//处理单选多选
function handleSelect(val) {
    select.value = val;
}

//确定选择
function commit() {
    if (select.value.length === 1) {
        proxy.$emit("emit-commit", select.value[0]);
        selectDialogFormVisible.value = false;
    } else
        ElMessage("请选择一条记录");
}

//获取客户信息
function getList() {
    const param = {
        pageNo: currentPage.value,
        pageSize: pageSize.value,
        isMarkedEq: props.isMarked,
    };
    if (props.scene === 0) {
        param.sceneEq = props.scene;
    }else {
        param.secondarySceneEq= props.scene
    }
    switch (category.value) {
        case "serialNumberType": {
            if (inputValue.value !== '')
                param.serialNumberTypeLike = inputValue.value;
        }
            break;
        case "":
            break;
    }
    listDataset(param).then((res) => {
        if (res === undefined || null) {
            ElMessage.error('服务器未能正常返回！');
        } else if (res.code === 200) {
            tableData.value = res.data.records
            count.value = res.data.total;
        }
    })
}

//处理关闭后
function handleClose() {
    proxy.$refs.multipleTable.clearSelection();
    selectDialogFormVisible.value = false;
}

function sortChange({orderByAsc, orderByDesc}) {
    Asc.value = orderByAsc;
    Desc.value = orderByDesc;
    getList();
}

defineExpose({
    openDialog
})

</script>

<style scoped>

</style>
