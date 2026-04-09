package com.naic.productionline.service.feign;

import com.naic.api.api.Result;
import com.naic.productionline.config.AirflowConfig;
import com.naic.productionline.config.AirflowFeignConfig;
import com.naic.productionline.domain.dto.AirFlowDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wangyunong
 */
@FeignClient(name = "airflow-client",url = "${airflow.path}", configuration = AirflowFeignConfig.class)
public interface AirflowFeign {

    /**
     * 导出镜像
     * @param airFlowDTO airFlowDTO
     * @return {@link String}
     */
    @PostMapping("/api/v1/dags/"+"${airflow.dagName}"+"/dagRuns")
    String dagRun(@RequestBody AirFlowDTO airFlowDTO);

    /**
     * 删除job
     * @param airFlowDTO airFlowDTO
     * @return {@link String}
     */
    @PostMapping("/api/v1/dags/"+"${airflow.jobDelete}"+"/dagRuns")
    String deleteJob(@RequestBody AirFlowDTO airFlowDTO);

    /**
     * 删除service
     * @param airFlowDTO airFlowDTO
     * @return {@link String}
     */
    @PostMapping("/api/v1/dags/"+"${airflow.serviceDelete}"+"/dagRuns")
    String deleteService(@RequestBody AirFlowDTO airFlowDTO);

    /**
     * 中断/继续service运行
     * @param airFlowDTO airFlowDTO
     * @return {@link String}
     */
    @PostMapping("/api/v1/dags/"+"${airflow.serviceScale}"+"/dagRuns")
    String scaleService(@RequestBody AirFlowDTO airFlowDTO);

    /**
     * 中断/继续job运行
     * @param airFlowDTO airFlowDTO
     * @return {@link String}
     */
    @PostMapping("/api/v1/dags/"+"${airflow.jobScale}"+"/dagRuns")
    String scaleJob(@RequestBody AirFlowDTO airFlowDTO);

}
