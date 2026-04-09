package com.naic.productionline.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * mlflow工具类
 * </p>*
 * @author wangyunong
 * @data 2024/10/15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mlflow")
public class MlflowConfig {

    /**
     * MLflow主机地址
     */
    @Value("${mlflow.path}")
    private String path;

    /**
     * MLflow模型文件地址
     */
    @Value("${mlflow.downloadPath}")
    private String downloadPath;

    /**
     * experiment_max_results
     */
    @Value("${mlflow.experimentMaxResults}")
    private Long experimentMaxResults;

    /**
     * metric_max_results
     */
    @Value("${mlflow.metricMaxResults}")
    private Long metricMaxResults;

    /**
     * run_max_results
     */
    @Value("${mlflow.runMaxResults}")
    private Long runMaxResults;

    /**
     * image_header
     */
    @Value("${mlflow.imageHeader}")
    private String imageHeader;
}
