package com.naic.productionline.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * airflow工具类
 * </p>*
 * @author wangyunong
 * @data 2024/10/15
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "airflow")
public class AirflowConfig {

    /**
     * airflow主机地址
     */
    @Value("${airflow.path}")
    private String path;

    /**
     * DAG名称
     */
    @Value("${airflow.dagName}")
    private String dagName;

    /**
     * airflow username
     */
    @Value("${airflow.username}")
    private String username;

    /**
     * airflow password
     */
    @Value("${airflow.password}")
    private String password;

    /**
     * jobDelete
     */
    @Value("${airflow.jobDelete}")
    private String jobDelete;

    /**
     * serviceDelete
     */
    @Value("${airflow.serviceDelete}")
    private String serviceDelete;

    /**
     * serviceScale
     */
    @Value("${airflow.serviceScale}")
    private String serviceScale;

    /**
     * jobScale
     */
    @Value("${airflow.jobScale}")
    private String jobScale;
}
