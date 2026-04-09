package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.PipelineTaskCnv;
import com.naic.productionline.domain.dto.PipelineTaskDTO;
import com.naic.productionline.domain.po.PipelineTaskPO;
import com.naic.productionline.domain.vo.*;
import com.naic.productionline.domain.vo.mlflow.ArtifactPathVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.service.DataSetService;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.PipelineTaskService;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Api(tags = "模型产线管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/pipeline")
public class PipelineTaskController {

    private final PipelineTaskService pipelineTaskService;

    private final PipelineTaskCnv pipelineTaskCnv;

    private final ModelService modelService;

    private final DataSetService dataSetService;

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<PipelineTaskVO>> conditionList(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo,@RequestParam Integer pageSize){
        return Result.success(pipelineTaskService.selectList(condition,pageNo,pageSize));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:query')")
    @GetMapping("by-id")
    @ApiOperation("根据id查询模型产线")
    @Log()
    public Result<PipelineTaskVO> getTask(@RequestParam Long id){
        PipelineTaskVO vo=pipelineTaskCnv.poToVo(pipelineTaskService.getById(id));
        vo.setModelName(vo.getModelId()==null?"请选择模型":modelService.getById(vo.getModelId()).getName());
        vo.setDataSetName(vo.getDataSetId()==null?"请选择数据集":dataSetService.getById(vo.getDataSetId()).getDataSetName());
        return Result.success(vo);
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:add')")
    @PostMapping
    @ApiOperation("新增模型产线")
    @Log()
    public Result<?> insertTask(@RequestBody PipelineTaskDTO dto){
        return Result.success(pipelineTaskService.insertPipelineTask(dto));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:edit')")
    @PutMapping
    @ApiOperation("修改模型产线")
    @Log()
    public Result<?> updateTask(@RequestBody PipelineTaskDTO dto){
        pipelineTaskService.updatePipelineTask(dto);
        return Result.success("修改成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:remove')")
    @DeleteMapping
    @ApiOperation("删除模型产线")
    @Log()
    public Result<?> deleteTask(@RequestParam Long id) throws ApiException {
        pipelineTaskService.deletePipelineTask(id);
        return Result.success("删除成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:train')")
    @PostMapping("queue")
    @ApiOperation("提交训练")
    @Log()
    public Result<?> queueTask(@RequestParam Long id,@RequestBody Map<String,String> map) throws IOException, ApiException {
        PipelineTaskPO task=pipelineTaskService.getById(id);
        if(!task.getStatus().equals(0)){
            throw new ServiceException("任务已经存在训练记录，无法重复训练");
        }
        pipelineTaskService.queue(id, map);
        return Result.success("成功提交训练");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:train')")
    @PostMapping("quit")
    @ApiOperation("取消训练")
    @Log()
    public Result<?> quitOrContinueTask(@RequestParam Long id) throws ApiException {
        PipelineTaskPO task=pipelineTaskService.getById(id);
        if(!task.getStatus().equals(2)){
            throw new ServiceException("任务不在训练状态");
        }
        pipelineTaskService.quit(id);
        return Result.success("操作成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:query')")
    @GetMapping("task-message")
    @ApiOperation("根据id查询模型产线运行信息")
    @Log()
    public Result<TaskFlowVO> getTaskMessage(@RequestParam Long id) throws IOException, InterruptedException {
        return Result.success(pipelineTaskService.getMlflowMessage(id));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:query')")
    @GetMapping("artifact-path")
    @ApiOperation("根据id查询模型产线的模型文件")
    @Log()
    public Result<List<ArtifactPathVO>> getArtifact(@RequestParam Long id, @RequestParam(required = false) String path) throws IOException, InterruptedException {
        return Result.success(pipelineTaskService.getArtifacts(id,path));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:train')")
    @PostMapping("package")
    @ApiOperation("导出镜像文件")
    @Log()
    public Result<?> exportDockerImage(@RequestParam Long id) throws IOException, InterruptedException {
        PipelineTaskPO task= pipelineTaskService.getById(id);
        if(task==null){
            throw new ServiceException("没有此产线");
        }
        if(!task.getStatus().equals(4)){
            throw new ServiceException("该产线未运行成功无法导出镜像");
        }
        if(task.getIsPackage().equals(2)){
            throw new ServiceException("该产线已经导出完成");
        }
        pipelineTaskService.getImageUrl(id);
        return Result.success("镜像开始导出");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:model-line:query')")
    @GetMapping("by-es")
    @ApiOperation(value = "查询es中数据---新分页查询")
    @Log()
    public Result<List<TaskLog>> search(@RequestParam Long taskId){
        return Result.success(pipelineTaskService.queryByEs(taskId));
    }

    @PostMapping("package-status")
    @ApiOperation("产线文件导出状态")
    @Log()
    public Result<?> updatePackageStatus(@RequestParam Long taskId,@RequestParam Boolean success){
        pipelineTaskService.setPackageStatus(taskId,success);
        return Result.success("状态修改完成");
    }

    @GetMapping("training-metrics")
    @ApiOperation("获取训练实时指标（可视化）")
    @Log()
    public Result<Map<String, Object>> getTrainingMetrics(@RequestParam(required = false) String id) {
        // 后续对接MLflow真实指标，当前返回模拟数据供前端可视化
        int epochs = 50;
        int currentEpoch = 35;
        List<Double> loss = new ArrayList<>();
        List<Double> valLoss = new ArrayList<>();
        List<Double> accuracy = new ArrayList<>();
        List<Double> valAccuracy = new ArrayList<>();
        List<Double> learningRate = new ArrayList<>();
        List<Integer> gpuUtil = new ArrayList<>();
        List<Integer> gpuMem = new ArrayList<>();
        List<Integer> gpuTemp = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 1; i <= currentEpoch; i++) {
            loss.add(Math.round((2.5 * Math.exp(-0.06 * i) + rand.nextDouble() * 0.1) * 10000.0) / 10000.0);
            valLoss.add(Math.round((2.8 * Math.exp(-0.05 * i) + rand.nextDouble() * 0.15) * 10000.0) / 10000.0);
            accuracy.add(Math.round((1 - 1.2 * Math.exp(-0.08 * i) + rand.nextDouble() * 0.02) * 10000.0) / 10000.0);
            valAccuracy.add(Math.round((1 - 1.3 * Math.exp(-0.07 * i) + rand.nextDouble() * 0.03) * 10000.0) / 10000.0);
            learningRate.add(Math.round((0.001 * Math.pow(0.95, i)) * 1000000.0) / 1000000.0);
            gpuUtil.add(75 + rand.nextInt(20));
            gpuMem.add(60 + rand.nextInt(25));
            gpuTemp.add(65 + rand.nextInt(15));
        }
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("loss", loss);
        metrics.put("valLoss", valLoss);
        metrics.put("accuracy", accuracy);
        metrics.put("valAccuracy", valAccuracy);
        metrics.put("learningRate", learningRate);
        Map<String, Object> resource = new HashMap<>();
        resource.put("gpuUtil", gpuUtil);
        resource.put("gpuMem", gpuMem);
        resource.put("gpuTemp", gpuTemp);
        Map<String, Object> bestMetrics = new HashMap<>();
        bestMetrics.put("bestEpoch", currentEpoch - 3);
        bestMetrics.put("bestLoss", loss.stream().mapToDouble(Double::doubleValue).min().orElse(0));
        bestMetrics.put("bestAcc", accuracy.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", id);
        result.put("totalEpochs", epochs);
        result.put("currentEpoch", currentEpoch);
        result.put("status", "running");
        result.put("startTime", "2025-03-12 08:30:00");
        result.put("elapsedTime", "2h 15m");
        result.put("estimatedRemaining", "1h 30m");
        result.put("metrics", metrics);
        result.put("resourceUsage", resource);
        result.put("bestMetrics", bestMetrics);
        return Result.success(result);
    }
}