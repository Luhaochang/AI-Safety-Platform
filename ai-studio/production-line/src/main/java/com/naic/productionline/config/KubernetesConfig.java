package com.naic.productionline.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * kubernetes工具类
 * </p>*
 * @author wangyunong
 * @data 2024/10/14
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kubernetes-set")
public class KubernetesConfig {

    /**
     * 密钥
     */
    @Value("${kubernetes-set.token}")
    private String token;

    /**
     * k8s主机地址
     */
    @Value("${kubernetes-set.basePath}")
    private String basePath;

    /**
     * k8s接口访问地址
     */
    @Value("${kubernetes-set.pointPath}")
    private String pointPath;

    /**
     * 命名空间
     */
    @Value("${kubernetes-set.nameSpace}")
    private String nameSpace;

    /**
     * config
     */
    @Value("${kubernetes-set.config}")
    private String config;

    /**
     * datasetConfig
     */
    @Value("${kubernetes-set.datasetConfig}")
    private String datasetConfig;

    /**
     * MLFLOW_S3_ENDPOINT_URL
     */
    @Value("${kubernetes-set.endPointUrlName}")
    private String endPointUrlName;

    /**
     * MLFLOW_S3_ENDPOINT_URL
     */
    @Value("${kubernetes-set.endPointUrl}")
    private String endPointUrl;

    /**
     * AWS_ACCESS_KEY_ID
     */
    @Value("${kubernetes-set.accessKeyIdName}")
    private String accessKeyIdName;
    /**
     * AWS_ACCESS_KEY_ID
     */
    @Value("${kubernetes-set.accessKeyId}")
    private String accessKeyId;

    /**
     * AWS_SECRET_ACCESS_KEY
     */
    @Value("${kubernetes-set.secretAccessKeyName}")
    private String secretAccessKeyName;
    /**
     * AWS_SECRET_ACCESS_KEY
     */
    @Value("${kubernetes-set.secretAccessKey}")
    private String secretAccessKey;

    /**
     * deployContainerPort
     */
    @Value("${kubernetes-set.deployContainerPort}")
    private Integer deployContainerPort;

    /**
     * jupyterDeployContainerPort
     */
    @Value("${kubernetes-set.jupyterDeployContainerPort}")
    private Integer jupyterDeployContainerPort;

    /**
     * nodePortBasePath
     */
    @Value("${kubernetes-set.nodePortBasePath}")
    private String nodePortBasePath;

    /**
     * autoLabelImageUrl
     */
    @Value("${kubernetes-set.autoLabelImageUrl}")
    private String autoLabelImageUrl;

}
