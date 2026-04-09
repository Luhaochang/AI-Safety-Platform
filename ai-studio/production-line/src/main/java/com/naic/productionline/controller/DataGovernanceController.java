package com.naic.productionline.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.po.DataCategoryPO;
import com.naic.productionline.domain.po.DataGradePO;
import com.naic.productionline.domain.po.DataLineageEdgePO;
import com.naic.productionline.domain.po.DataLineageNodePO;
import com.naic.productionline.domain.po.DataAssetPO;
import com.naic.productionline.mapper.DataAssetMapper;
import com.naic.productionline.mapper.DataCategoryMapper;
import com.naic.productionline.mapper.DataGradeMapper;
import com.naic.productionline.mapper.DataLineageEdgeMapper;
import com.naic.productionline.mapper.DataLineageNodeMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "数据治理-分级分类&血缘")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/data-governance")
public class DataGovernanceController {

    private final DataGradeMapper dataGradeMapper;
    private final DataCategoryMapper dataCategoryMapper;
    private final DataAssetMapper dataAssetMapper;
    private final DataLineageNodeMapper lineageNodeMapper;
    private final DataLineageEdgeMapper lineageEdgeMapper;

    // ========== 分级 ==========

    @GetMapping("grade/list")
    @ApiOperation("获取分级列表")
    @Log()
    public Result<List<DataGradePO>> listGrades() {
        List<DataGradePO> list = dataGradeMapper.selectList(
                new LambdaQueryWrapper<DataGradePO>().orderByAsc(DataGradePO::getSort));
        return Result.success(list);
    }

    @PostMapping("grade")
    @ApiOperation("新增/修改分级")
    @Log()
    public Result<?> saveGrade(@RequestBody DataGradePO po) {
        if (po.getId() != null) {
            dataGradeMapper.updateById(po);
        } else {
            dataGradeMapper.insert(po);
        }
        return Result.success("操作成功");
    }

    @DeleteMapping("grade/{id}")
    @ApiOperation("删除分级")
    @Log()
    public Result<?> deleteGrade(@PathVariable Long id) {
        dataGradeMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ========== 分类 ==========

    @GetMapping("category/list")
    @ApiOperation("获取分类列表")
    @Log()
    public Result<List<DataCategoryPO>> listCategories() {
        List<DataCategoryPO> list = dataCategoryMapper.selectList(null);
        return Result.success(list);
    }

    @PostMapping("category")
    @ApiOperation("新增/修改分类")
    @Log()
    public Result<?> saveCategory(@RequestBody DataCategoryPO po) {
        if (po.getId() != null) {
            dataCategoryMapper.updateById(po);
        } else {
            dataCategoryMapper.insert(po);
        }
        return Result.success("操作成功");
    }

    @DeleteMapping("category/{id}")
    @ApiOperation("删除分类")
    @Log()
    public Result<?> deleteCategory(@PathVariable Long id) {
        dataCategoryMapper.deleteById(id);
        return Result.success("删除成功");
    }

    // ========== 分级分类统计 ==========

    @GetMapping("stats")
    @ApiOperation("获取分级分类统计")
    @Log()
    public Result<Map<String, Object>> getStats() {
        List<DataGradePO> grades = dataGradeMapper.selectList(null);
        List<DataCategoryPO> categories = dataCategoryMapper.selectList(null);
        Long totalAssets = dataAssetMapper.selectCount(null);

        // 按分级统计资产数
        List<Map<String, Object>> gradeDistribution = grades.stream().map(g -> {
            Long count = dataAssetMapper.selectCount(
                    new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getGradeId, g.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("name", g.getName());
            m.put("value", count);
            return m;
        }).collect(Collectors.toList());

        // 按分类统计资产数
        List<Map<String, Object>> categoryDistribution = categories.stream().map(c -> {
            Long count = dataAssetMapper.selectCount(
                    new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getCategoryId, c.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("name", c.getName().replace("类", ""));
            m.put("value", count);
            return m;
        }).collect(Collectors.toList());

        // 已分级分类的资产数（gradeId 和 categoryId 都不为空）
        Long classifiedAssets = dataAssetMapper.selectCount(
                new LambdaQueryWrapper<DataAssetPO>()
                        .isNotNull(DataAssetPO::getGradeId)
                        .isNotNull(DataAssetPO::getCategoryId));

        Map<String, Object> result = new HashMap<>();
        result.put("gradeDistribution", gradeDistribution);
        result.put("categoryDistribution", categoryDistribution);
        result.put("totalAssets", totalAssets);
        result.put("classifiedAssets", classifiedAssets);
        result.put("unclassifiedAssets", totalAssets - classifiedAssets);
        return Result.success(result);
    }

    // ========== 数据血缘 ==========

    @GetMapping("lineage")
    @ApiOperation("获取数据血缘关系")
    @Log()
    public Result<Map<String, Object>> getLineage() {
        List<DataLineageNodePO> nodes = lineageNodeMapper.selectList(null);
        List<DataLineageEdgePO> edgesPO = lineageEdgeMapper.selectList(null);

        // 将数据库字段 sourceId/targetId 映射为前端需要的 source/target
        List<Map<String, Object>> edges = new ArrayList<>();
        for (DataLineageEdgePO edge : edgesPO) {
            Map<String, Object> edgeMap = new HashMap<>();
            edgeMap.put("id", edge.getId());
            edgeMap.put("source", edge.getSourceId());
            edgeMap.put("target", edge.getTargetId());
            edgeMap.put("label", edge.getLabel());
            edges.add(edgeMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nodes", nodes);
        result.put("edges", edges);
        return Result.success(result);
    }
}
