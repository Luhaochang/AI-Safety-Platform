package com.naic.productionline.service.feign;

import com.naic.productionline.domain.dto.MlflowRunDTO;
import com.naic.productionline.domain.vo.mlflow.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wangyunong
 */
@FeignClient(name = "mlflow-client",url = "${mlflow.path}")
public interface MlflowFeign {

    /**
     * 获取实验id
     * @return ExperimentResponse
     */
    @GetMapping("/ajax-api/2.0/mlflow/experiments/search")
    ExperimentResponse searchExperiments(@RequestParam("max_results") Long maxResults);

    /**
     * 获取Metric
     * @param  runUuid runUuid
     * @param  metricKey metricKey
     * @param  maxResults maxResults
     * @return MetricResponse
     */
    @GetMapping("/ajax-api/2.0/mlflow/metrics/get-history")
    MetricResponse getMetric(@RequestParam("run_uuid" ) String runUuid, @RequestParam("metric_key" ) String metricKey, @RequestParam("max_results" ) Long maxResults);

    /**
     * 获取实验所有的run
     * @param  dto dto
     * @return RunResponse
     */
    @PostMapping("/ajax-api/2.0/mlflow/runs/search")
    RunResponse getRun(@RequestBody MlflowRunDTO dto);

    /**
     * 获取模型文件
     * @param  runUuid runUuid
     * @param  path path
     * @return ArtifactVO
     */
    @GetMapping("/ajax-api/2.0/mlflow/artifacts/list")
    ArtifactVO getArtifacts(@RequestParam("run_uuid") String runUuid, @RequestParam(value = "path",required = false) String path);

    /***
     * 获取模型地址
     */
    @GetMapping("/ajax-api/2.0/mlflow/logged-models/{modelId}")
    ModelInfoVO getModelArtifacts(@PathVariable("modelId") String modelId);

    /**
     * 获取模型信息
     */
    @GetMapping("/ajax-api/2.0/mlflow/runs/get")
    RunInfo getRunInfo(@RequestParam ("run_id") String runId);

}

