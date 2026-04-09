package com.naic.productionline.service.feign;

import com.naic.productionline.domain.dto.MlflowRunDTO;
import com.naic.productionline.domain.vo.homePage.NodeExporterResult;
import com.naic.productionline.domain.vo.homePage.NodeExporterStatistics;
import com.naic.productionline.domain.vo.mlflow.ArtifactVO;
import com.naic.productionline.domain.vo.mlflow.ExperimentResponse;
import com.naic.productionline.domain.vo.mlflow.MetricResponse;
import com.naic.productionline.domain.vo.mlflow.RunResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author wangyunong
 */
@FeignClient(name = "node-exporter",url = "${prometheus.url}")
public interface NodeExporterFeign {

    /**
     * 获取资源信息
     * @return metrics string
     */
    @GetMapping("/api/v1/query")
    NodeExporterResult getMetrics(@RequestParam("query") String query);

}
