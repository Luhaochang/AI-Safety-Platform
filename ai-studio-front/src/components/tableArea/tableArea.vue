<template>
    <div class="tableArea">
        <screen-table
            class="screen-content"
            table-class="custom-table"
            header-row-class-name="custom-table-header"
            border
            stripe
            :default-sort="{prop: 'updateTime', order: 'descending'}"
            :data="tableData"
            @sort-change='sortChange'
            @selection-change="handleSelectionChange"
            ref="multipleTable"
        >
            <el-table-column fixed type="selection" width="40"></el-table-column>
            <el-table-column v-for="(item) in items" :key="item.prop" :prop="item.prop" :label="item.label" :formatter="item.formatterFun"
                             min-width="120"
                             :sortable="item.isSort"
                            :show-overflow-tooltip="item.showTooltip?item.showTooltip:false">
            </el-table-column>
        </screen-table>
        <div class="screen-footer flex justify-end" >
            <el-pagination
                background
                layout="sizes, prev, pager, next, total"
                :page-sizes="[10, 20, 50, 100]"
                :total="count"
                :current-page="currentPage"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            >
            </el-pagination>
        </div>
    </div>
</template>

<script>
import {defineComponent, getCurrentInstance} from "vue";
import ScreenTable from "@/components/ScreenTable";
export default defineComponent({
    name: "tableArea",
    components: {
        ScreenTable
    },
    props: {
        //表头
        items: {
            type: Array,
            default: () => [
                {
                    prop: "prop1",
                    label: "列1"
                },
                {
                    prop: "prop2",
                    label: "列2"
                }
            ]
        },
        //表格数据
        tableData: {
            type:Array,
            default:() => []
        },
        //当前页
        currentPage: {
            type:Number,
            default: 1
        },
        //总条目数
        count: {
            type:Number,
            default:  0
        },
        //格式化方法
        formatterFun:{
            type:Function,
            default:null
        }

    },
    setup() {
        const {proxy} = getCurrentInstance()
        //设置固定列
        function setFixed(index) {
            if (index === 1)
                return "left";
            else
                return true;
        }
        //处理单选多选
        function handleSelectionChange(val) {
            proxy.$emit("emit-select",val);
        }
        //页面大小改变函数
        function handleSizeChange(val) {
            proxy.$emit("emit-pageSize",val);
        }
        // 页面跳转函数
        function handleCurrentChange(val) {
            proxy.$emit("emit-pageChange",val);
        }
        function clearSelection() {
            proxy.$refs.multipleTable.clearSelection();
        }
        // 布尔类型转换
        // formatBoolean: function (row, column, cellValue) {
        //     var ret = '';  //你想在页面展示的值
        //     if (cellValue) {
        //         ret = "是";  //根据自己的需求设定
        //     } else {
        //         ret = "否";
        //     }
        //     return ret;
        // }

        function sortChange({ orderByAsc, orderByDesc }) {
            proxy.$emit("emit-sort-change", { orderByAsc, orderByDesc });
        }
        return {
            setFixed,
            handleSelectionChange,
            handleSizeChange,
            handleCurrentChange,
            clearSelection,
            sortChange
        }
    }
});
</script>

<style scoped>
.tableArea {
    flex-direction: column;
    height: calc(100% - 55px);
    display: flex;
}
.screen-content {
    flex-grow: 1;
    flex-shrink: 0;
    padding: 0 20px;
}
.screen-footer  {
    padding: 20px;
    height: 32px;
}
</style>
