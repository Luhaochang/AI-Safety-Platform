package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.DataSetCnv;
import com.naic.productionline.domain.dto.DataSetDTO;
import com.naic.productionline.domain.vo.DataSetVO;
import com.naic.productionline.service.DataSetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@Api(tags = "数据集管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/data-set")
public class DataSetController {

    private final DataSetService dataSetService;

    private final DataSetCnv dataSetCnv;

    @PreAuthorize("@isPermitted.hasPermission('datamag:dataset:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<DataSetVO>> dataSetList(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize)
    {
        return Result.success(dataSetService.selectList(condition,pageNo,pageSize));
    }

    @GetMapping("by-id")
    @PreAuthorize("@isPermitted.hasPermission('datamag:dataset:query')")
    @ApiOperation("根据id查询数据集")
    @Log()
    public Result<DataSetVO> getDataSet(@RequestParam Long id)
    {
        return Result.success(dataSetCnv.poToVo(dataSetService.getById(id)));
    }

    @PostMapping
    @ApiOperation("新增数据集")
    @PreAuthorize("@isPermitted.hasPermission('datamag:dataset:add')")
    @Log()
    public Result<?> insertDataSet(@RequestBody DataSetDTO dto)
    {
        dataSetService.insertDataSet(dto);
        return Result.success("新增成功");
    }

    @PutMapping
    @ApiOperation("更新数据集")
    @PreAuthorize("@isPermitted.hasPermission('datamag:dataset:edit')")
    @Log()
    public Result<?> updateDataSet(@RequestBody DataSetDTO dto)
    {
        dataSetService.updateDataSet(dto);
        return Result.success("更新成功");
    }

    @DeleteMapping
    @ApiOperation("删除数据集")
    @PreAuthorize("@isPermitted.hasPermission('datamag:dataset:remove')")
    @Log()
    public Result<?> deleteDataSet(@RequestParam Long id)
    {
        dataSetService.deleteDataSet(id);
        return Result.success("删除成功");
    }

    @PostMapping("official")
    @ApiOperation("是否官方数据集：0.否 1.是")
    @Log()
    public Result<?> setOfficialDataSet(@RequestParam Long id,@RequestParam Integer isOfficial)
    {
        dataSetService.setOfficial(id,isOfficial);
        return Result.success("设置成功");
    }

    @PostMapping("check")
    @ApiOperation("校验")
    @Log()
    public Result<?> setCheckDataSet(@RequestParam Long id)
    {
        dataSetService.setChecked(id);
        return Result.success("设置成功");
    }
}