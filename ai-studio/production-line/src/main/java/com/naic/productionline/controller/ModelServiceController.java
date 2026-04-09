package com.naic.productionline.controller;

import com.naic.api.api.DataPage;
import com.naic.api.api.Result;
import com.naic.log.annotation.Log;
import com.naic.productionline.domain.cnv.ModelCnv;
import com.naic.productionline.domain.dto.ModelDTO;
import com.naic.productionline.domain.dto.ModelServiceDTO;
import com.naic.productionline.domain.po.ModelPO;
import com.naic.productionline.domain.vo.ModelServiceVO;
import com.naic.productionline.domain.vo.ModelVO;
import com.naic.productionline.domain.vo.taskLogTools.TaskLog;
import com.naic.productionline.service.ModelService;
import com.naic.productionline.service.ModelServiceService;
import com.naic.productionline.service.ModelTagRelationService;
import io.kubernetes.client.openapi.ApiException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@Api(tags = "服务管理")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/model-service")
public class ModelServiceController {

    private final ModelServiceService modelServiceService;

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:list')")
    @GetMapping("condition")
    @ApiOperation("条件查询")
    @Log()
    public Result<DataPage<ModelServiceVO>> getCondition(@RequestParam(required = false) @ApiIgnore Map<String, Object> condition, @RequestParam Integer pageNo, @RequestParam Integer pageSize){
        return Result.success(modelServiceService.selectService(condition,pageNo,pageSize));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:query')")
    @GetMapping("by-id")
    @ApiOperation("根据id查询服务")
    @Log()
    public Result<ModelServiceVO> getServiceById(@RequestParam Long id){
        return Result.success(modelServiceService.getById(id));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:add')")
    @PostMapping
    @ApiOperation("部署服务")
    @Log()
    public Result<?> insertService(@RequestBody ModelServiceDTO dto) throws ApiException {
        modelServiceService.insertService(dto);
        return Result.success("开始部署");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:delete')")
    @DeleteMapping
    @ApiOperation("删除服务")
    @Log()
    public Result<?> deleteService(@RequestParam Long id) throws ApiException {
        modelServiceService.deleteService(id);
        return Result.success("删除成功");
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:query')")
    @GetMapping("by-es")
    @ApiOperation("根据es查询服务运行日志")
    @Log()
    public Result<List<TaskLog>> getDeploymentByEs(@RequestParam Long serviceId){
        return Result.success(modelServiceService.queryDeploymentByEs(serviceId));
    }

    @PreAuthorize("@isPermitted.hasPermission('modelmag:service-list:change')")
    @PostMapping("quit-continue")
    @ApiOperation("暂停/继续服务 0.暂停 1.继续")
    @Log()
    public Result<?> quitService(@RequestParam Long serviceId,@RequestParam Integer status) throws ApiException {
        modelServiceService.quitOrContinueService(serviceId,status);
        return Result.success("操作完成");
    }

    @GetMapping("inference-metrics")
    @ApiOperation("获取推理服务性能指标（可视化）")
    @Log()
    public Result<Map<String, Object>> getInferenceMetrics(@RequestParam(required = false) String id) {
        // 后续对接Prometheus/Grafana真实指标，当前返回模拟数据供前端可视化
        java.util.List<String> timePoints = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        for (int i = 59; i >= 0; i--) {
            timePoints.add(now.minusMinutes(i).format(fmt));
        }
        java.util.Random rand = new java.util.Random(42);
        java.util.List<Integer> qps = new java.util.ArrayList<>();
        java.util.List<Integer> latency = new java.util.ArrayList<>();
        java.util.List<Integer> errorCount = new java.util.ArrayList<>();
        java.util.List<Integer> gpuUtil = new java.util.ArrayList<>();
        for (int i = 0; i < 60; i++) {
            qps.add(25 + rand.nextInt(20));
            latency.add(30 + rand.nextInt(30));
            errorCount.add(rand.nextInt(3));
            gpuUtil.add(50 + rand.nextInt(30));
        }
        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalRequests", 125680);
        summary.put("avgQps", 35.2);
        summary.put("avgLatency", 42);
        summary.put("p50Latency", 35);
        summary.put("p95Latency", 68);
        summary.put("p99Latency", 120);
        summary.put("errorRate", 0.12);
        summary.put("successRate", 99.88);
        Map<String, Object> timeSeries = new java.util.HashMap<>();
        timeSeries.put("qps", qps);
        timeSeries.put("latency", latency);
        timeSeries.put("errorCount", errorCount);
        timeSeries.put("gpuUtil", gpuUtil);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("serviceId", id);
        result.put("serviceName", "目标检测推理服务");
        result.put("status", "running");
        result.put("uptime", "72h 15m");
        result.put("timePoints", timePoints);
        result.put("summary", summary);
        result.put("timeSeries", timeSeries);
        return Result.success(result);
    }

}
