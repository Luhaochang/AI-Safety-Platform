<template>
    <div class="screentable" ref="screentable">
        <el-table
            ref="screentElTable"
            :class="tableClass"
            :data="data"
            :height="tableHeight"
            :show-header="showHeader"
            :stripe="stripe"
            :border="border"
            :size="size"
            :row-class-name="rowClassName"
            :row-style="rowStyle"
            :cell-class-name="cellClassName"
            :cell-style="cellStyle"
            :header-row-class-name="headerRowClassName"
            :header-row-style="headerRowStyle"
            :header-cell-class-name="handleHeaderCellClass"
            :header-cell-style="headerCellStyle"
            @select="handleSelect"
            @select-all="handleSelectAll"
            @selection-change="handleSelectionChange"
            @row-click="on_select"
            @header-dragend="handleHeaderDragend"
            @header-click="handleHeaderClick"
            @sort-change='sortChange'>
            <slot></slot>
        </el-table>
    </div>
</template>
<script>
import {defineComponent, ref, onMounted,getCurrentInstance} from 'vue';
import { debounce } from '@/utils/index.js';
export default defineComponent({
    name: 'ScreenTable',
    props: {
        data: {
            type: Array,
            required: true
        },
        showHeader: {
            type: Boolean,
            default: true
        },
        stripe: {
            type: Boolean,
            default: true
        },
        border: {
            type: Boolean,
            default: true
        },
        emptyText: {
            type: String,
            default: '暂无数据'
        },
        size: {
            type: String
        },
        tableClass: {
            type: [Object,String]
        },
        rowClassName: {
            type: [Function,String]
        },
        rowStyle: {
            type: [Function,String]
        },
        cellClassName: {
            type: [Function,String]
        },
        cellStyle: {
            type: [Function,String]
        },
        headerRowClassName: {
            type: [Function,String]
        },
        headerRowStyle: {
            type: [Function,String]
        },
        // headerCellClassName: {
        //     type: [Function,String]
        // },
        headerCellStyle: {
            type: [Function,String]
        }
    },
    setup(props, context) {
        let {proxy} = getCurrentInstance();
        let tableHeight = ref(500);
        let resizeHandler = ref(null);
        let screentable = ref();
        let screentElTable = ref();


        let orderArray = ref([]);
        let curProp = ref(null);

        function initResizeEvent() {
            window.addEventListener('resize', resizeHandler);
        }
        function clearSelection() {
            screentElTable.value.clearSelection();
        }
        function toggleRowSelection(row, selected) {
            screentElTable.value.toggleRowSelection(row, selected);
        }
        function toggleAllSelection() {
            screentElTable.value.toggleAllSelection();
        }
        function setCurrentRow(row) {
            screentElTable.value.setCurrentRow(row);
        }
        function handleSelect(selection, row) {
            context.emit('select', selection, row);
        }
        function handleSelectAll(selection) {
            context.emit('select-all', selection);
        }
        function handleSelectionChange(val) {
            context.emit('selection-change', val);
        }
        function handleHeaderDragend(newWidth, oldWidth, column, event) {
            context.emit('header-dragend', newWidth, oldWidth, column, event);
        }
        async function sortChange({column, prop, order}){
            if (order) {
                //参与排序
                let flagIsHave = false;
                orderArray.value.forEach((element) => {
                    if (element.prop === prop) {
                        element.order = order;
                        flagIsHave = true;
                    }
                });
                if (!flagIsHave) {
                    orderArray.value.push({
                        prop: prop,
                        order: order,
                    });
                }
            } else {
                //不参与排序
                orderArray.value = orderArray.value.filter((element) => {
                    return element.prop !== curProp.value
                });
            }
            let res = await proxy.$fnc.getSort(orderArray.value)
            context.emit('sort-change',res);
        }
        function handleHeaderClick(column){
            // context.emit('header-click',column);
            curProp.value = column.property;
        }
        function handleHeaderCellClass({column}) {
            orderArray.value.forEach((element) => {
                if (column.property === element.prop) {
                    column.order = element.order;
                }
            });
        }
        function on_select(val){//点击行选中复选框
            screentElTable.value.toggleRowSelection(val);
        }
        onMounted(() =>{
            resizeHandler = debounce(() => {
                if (!screentable.value || !screentable.value.offsetHeight) {
                    return false;
                }
                let heightStyle = screentable.value.offsetHeight;
                tableHeight.value = heightStyle;
            }, 100);
            resizeHandler();
            initResizeEvent();
        })
        return {
            tableHeight,
            resizeHandler,
            screentable,
            screentElTable,
            initResizeEvent,
            clearSelection,
            toggleRowSelection,
            toggleAllSelection,
            setCurrentRow,
            handleSelect,
            handleSelectAll,
            handleSelectionChange,
            handleHeaderDragend,
            on_select,
            handleHeaderClick,
            sortChange,
            handleHeaderCellClass
        }
    }
});
</script>
<style lang="scss" scoped>
.screentable {
    overflow: hidden;
}
</style>
