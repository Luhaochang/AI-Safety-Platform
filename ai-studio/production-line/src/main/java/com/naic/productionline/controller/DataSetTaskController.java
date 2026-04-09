package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.DataSetTaskCnv;
import com.naic.productionline.domain.dto.DataSetTaskDTO;
import com.naic.productionline.domain.vo.DataSetTaskVO;
import com.naic.productionline.service.DataSetTaskService;
import io.kubernetes.client.openapi.ApiException;
import io.minio.errors.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Api(tags = "数据集标注任务管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/data-set-task")
public class DataSetTaskController {

    private final DataSetTaskService dataSetTaskService;

    private final DataSetTaskCnv dataSetTaskCnv;

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<DataSetTaskVO>> dataSetList(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        return Result.success(dataSetTaskService.selectList(condition,pageNo,pageSize));
    }

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:query')")
    @GetMapping("by-id")
    @ApiOperation("根据id查询标注任务")
    @Log()
    public Result<DataSetTaskVO> getDataTaskSet(@RequestParam Long id)
    {
        return Result.success(dataSetTaskCnv.poToVo(dataSetTaskService.getById(id)));
    }

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:add')")
    @PostMapping
    @ApiOperation("新增标注任务")
    @Log()
    public Result<?> insertDataSet(@RequestBody DataSetTaskDTO dto){
        dataSetTaskService.insertDataSetTask(dto);
        return Result.success("新增成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:remove')")
    @DeleteMapping
    @ApiOperation("删除标注任务")
    @Log()
    public Result<?> deleteDataTaskSet(@RequestParam Long id) throws ApiException {
        dataSetTaskService.deleteDataSetTask(id);
        return Result.success("删除成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:mark')")
    @PostMapping("save")
    @ApiOperation("保存标注任务")
    @Log()
    public Result<String> saveDataSet(@RequestParam("file") MultipartFile file,@RequestParam("id") Long id,@RequestParam Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return Result.success(dataSetTaskService.saveTask(file,id,finishNum));
    }

    @PreAuthorize("@isPermitted.hasPermission('datamag:mark-task:mark')")
    @PostMapping("submit")
    @ApiOperation("提交标注任务")
    @Log()
    public Result<String> submitDataSet(@RequestParam("file") MultipartFile file,@RequestParam("id") Long id,@RequestParam Long finishNum) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return Result.success(dataSetTaskService.submitTask(file,id,finishNum));
    }
}
