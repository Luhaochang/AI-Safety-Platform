package com.naic.productionline.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.po.DataAssetPO;
import com.naic.productionline.domain.po.DataCategoryPO;
import com.naic.productionline.domain.po.DataGradePO;
import com.naic.productionline.mapper.DataAssetMapper;
import com.naic.productionline.mapper.DataCategoryMapper;
import com.naic.productionline.mapper.DataGradeMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "数据资产管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/data-asset")
public class DataAssetController {

    private final DataAssetMapper dataAssetMapper;
    private final DataGradeMapper dataGradeMapper;
    private final DataCategoryMapper dataCategoryMapper;

    @GetMapping("condition")
    @ApiOperation("分页查询数据资产")
    @Log()
    public Result<Map<String, Object>> listAssets(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long categoryId) {

        LambdaQueryWrapper<DataAssetPO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(assetName)) {
            wrapper.like(DataAssetPO::getAssetName, assetName);
        }
        if (gradeId != null) {
            wrapper.eq(DataAssetPO::getGradeId, gradeId);
        }
        if (categoryId != null) {
            wrapper.eq(DataAssetPO::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(DataAssetPO::getCreateTime);

        IPage<DataAssetPO> page = dataAssetMapper.selectPage(
                new Page<>(pageNo, pageSize), wrapper);

        // 填充分级/分类名称
        Map<Long, String> gradeMap = dataGradeMapper.selectList(null).stream()
                .collect(Collectors.toMap(DataGradePO::getId, DataGradePO::getName));
        Map<Long, String> categoryMap = dataCategoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(DataCategoryPO::getId, DataCategoryPO::getName));

        List<Map<String, Object>> records = page.getRecords().stream().map(po -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", po.getId());
            map.put("assetName", po.getAssetName());
            map.put("grade", gradeMap.getOrDefault(po.getGradeId(), "未分级"));
            map.put("category", categoryMap.getOrDefault(po.getCategoryId(), "未分类"));
            map.put("gradeId", po.getGradeId());
            map.put("categoryId", po.getCategoryId());
            map.put("dataLayer", po.getDataLayer());
            map.put("source", po.getSource());
            map.put("owner", po.getOwner());
            map.put("size", po.getSize());
            map.put("description", po.getDescription());
            map.put("createTime", po.getCreateTime());
            map.put("updateTime", po.getUpdateTime());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        return Result.success(result);
    }

    @GetMapping("by-id")
    @ApiOperation("根据ID查询数据资产")
    @Log()
    public Result<DataAssetPO> getById(@RequestParam Long id) {
        return Result.success(dataAssetMapper.selectById(id));
    }

    @PostMapping
    @ApiOperation("新增数据资产")
    @Log()
    public Result<?> addAsset(@RequestBody DataAssetPO po) {
        dataAssetMapper.insert(po);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("修改数据资产")
    @Log()
    public Result<?> updateAsset(@RequestBody DataAssetPO po) {
        dataAssetMapper.updateById(po);
        return Result.success("修改成功");
    }

    @DeleteMapping
    @ApiOperation("删除数据资产")
    @Log()
    public Result<?> deleteAsset(@RequestParam Long id) {
        dataAssetMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("overview")
    @ApiOperation("资产统计概览")
    @Log()
    public Result<Map<String, Object>> getOverview() {
        Long totalAssets = dataAssetMapper.selectCount(null);
        List<DataGradePO> grades = dataGradeMapper.selectList(null);
        List<DataCategoryPO> categories = dataCategoryMapper.selectList(null);

        // 按数据层统计（数据集、模型、知识文档）
        Long datasetCount = dataAssetMapper.selectCount(
                new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getDataLayer, "原始数据"));
        Long modelCount = dataAssetMapper.selectCount(
                new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getDataLayer, "模型产出"));
        Long knowledgeCount = dataAssetMapper.selectCount(
                new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getDataLayer, "知识文档"));

        // 按分级统计
        List<Map<String, Object>> gradeDist = grades.stream().map(g -> {
            Long count = dataAssetMapper.selectCount(
                    new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getGradeId, g.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("name", g.getName());
            m.put("value", count);
            return m;
        }).collect(Collectors.toList());

        // 按分类统计
        List<Map<String, Object>> categoryDist = categories.stream().map(c -> {
            Long count = dataAssetMapper.selectCount(
                    new LambdaQueryWrapper<DataAssetPO>().eq(DataAssetPO::getCategoryId, c.getId()));
            Map<String, Object> m = new HashMap<>();
            m.put("name", c.getName().replace("类", ""));
            m.put("value", count);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("totalAssets", totalAssets);
        result.put("datasetCount", datasetCount);
        result.put("modelCount", modelCount);
        result.put("knowledgeCount", knowledgeCount);
        result.put("gradeDistribution", gradeDist);
        result.put("categoryDistribution", categoryDist);
        return Result.success(result);
    }
}
