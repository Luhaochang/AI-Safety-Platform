package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.ModelTagRelationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Api(tags = "模型管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model")
public class ModelController {

    private final ModelService modelService;
    private final ModelTagRelationService modelTagRelationService;

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<ModelVO>> getCondition(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return Result.success(modelService.selectModel(condition, pageNo, pageSize));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:query')")
    @GetMapping("by-id")
    @ApiOperation("根据id查询模型(并增加浏览量)")
    @Log()
    public Result<ModelVO> getModelById(@RequestParam Long id) {
        return Result.success(modelService.getModelDetail(id));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:add')")
    @PostMapping
    @ApiOperation("新增官方模型")
    @Log()
    public Result<?> insertOfficialModel(@RequestBody ModelDTO dto) {
        modelService.insertModel(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:add')")
    @PostMapping("self-define")
    @ApiOperation("新增自定义模型")
    @Log()
    public Result<?> insertSelfDefineModel(@RequestParam Long taskId, @RequestBody ModelDTO dto) {
        modelService.insertSelfDefineModel(taskId, dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:edit')")
    @PutMapping
    @ApiOperation("编辑模型")
    @Log()
    public Result<?> updateModel(@RequestBody ModelDTO dto) {
        modelService.updateModel(dto);
        return Result.success("编辑模型");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:remove')")
    @DeleteMapping
    @ApiOperation("删除模型")
    @Log()
    public Result<?> deleteModel(@RequestParam Long id) {
        modelService.deleteModel(id);
        return Result.success("删除成功");
    }

    @PostMapping("is-offocial")
    @ApiOperation("设置模型是否为官方模型")
    @Log()
    public Result<?> setOfficial(@RequestParam Long id, @RequestParam Integer isOfficial) {
        modelService.setModelOfficial(id, isOfficial);
        return Result.success("设置成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model:list')")
    @GetMapping("by-tag")
    @ApiOperation("根据标签获取模型列表")
    @Log()
    public Result<List<ModelVO>> getModelByTag(@RequestParam List<Long> tagIdList) {
        return Result.success(modelTagRelationService.getModelByTag(tagIdList));
    }

    @GetMapping("options")
    @ApiOperation("获取模型配置选项(场景、任务、框架、提供方)")
    public Result<Map<String, List<Map<String, Object>>>> getModelOptions() {
        return Result.success(modelService.getModelOptions());
    }
}