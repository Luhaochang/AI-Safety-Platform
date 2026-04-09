package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.AutoLabelCnv;
import com.naic.productionline.domain.cnv.PipelineTaskCnv;
import com.naic.productionline.domain.dto.AutoLabelDTO;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.AutoLabelPO;
import com.naic.productionline.domain.po.DataSetTaskPO;
import com.naic.productionline.domain.po.ModelServicePO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.AutoLabelVO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.domain.vo.PipelineTaskVO;
import com.naic.productionline.domain.vo.TaskFlowVO;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.service.*;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Api(tags = "自动标注管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/auto-label")
public class AutoLabelController {

    private final AutoLabelService autoLabelService;
    private final AutoLabelCnv autoLabelCnv;
    private final DataSetTaskService dataSetTaskService;
    private final ModelServiceService modelServiceService;
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<AutoLabelVO>> conditionList(@RequestParam Long dataSetTaskId, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        return Result.success(autoLabelService.selectList(dataSetTaskId,pageNo,pageSize));
    }

    @PostMapping
    @ApiOperation("新增自动标注任务")
    @Log()
    public Result<?> insertTask(@RequestBody AutoLabelDTO dto){
        DataSetTaskPO task= dataSetTaskService.getById(dto.getDataSetTaskId());
        if(!task.getStatus().equals(0)){
            throw new ServiceException("该标注任务已经完成，无法重复标注");
        }
        return Result.success(autoLabelService.insertAutoLabel(dto));
    }

    @PutMapping
    @ApiOperation("修改自动标注任务")
    @Log()
    public Result<?> updateTask(@RequestBody AutoLabelDTO dto){
        autoLabelService.updateAutoLabel(dto);
        return Result.success("修改成功");
    }

    @DeleteMapping
    @ApiOperation("删除自动标注任务")
    @Log()
    public Result<?> deleteTask(@RequestParam Long id) throws ApiException {
        autoLabelService.deleteAutoLabel(id);
        return Result.success("删除成功");
    }

    @PostMapping("queue")
    @ApiOperation("提交自动标注任务")
    @Log()
    public Result<?> queueTask(@RequestParam Long id) throws IOException, ApiException {
        AutoLabelPO autoLabel=autoLabelService.getById(id);
        DataSetTaskPO task= dataSetTaskService.getById(autoLabel.getDataSetTaskId());
        if(!task.getStatus().equals(0)){
            throw new ServiceException("该标注任务已经完成，无法重复标注");
        }
        ModelServiceVO modelServiceVO= modelServiceService.getById(autoLabel.getServiceId());
        if(!modelServiceVO.getStatus().equals(2)){
            throw new ServiceException("服务已下线，无法标注");
        }
        autoLabelService.queue(id);
        return Result.success("成功提交训练");
    }


}
