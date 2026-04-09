package com.naic.productionline.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * minio工具类
 * </p>*
 * @author wangyunong
 * @data 2023/10/24
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    @Value("${minio.accessKey}")
    private String accessKey;

    /**
     * 密钥
     */
    @Value("${minio.secretKey}")
    private String secretKey;

    /**
     * 访问api Url
     */
    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 文件存储路径前缀
     */
    @Value("${minio.downloadPath}")
    private String downloadPath;

    /**
     * bucket name
     */
    @Value("${minio.bucket}")
    private String bucket;


    /**
     * 创建MinIo客户端
     *
     * @return
     */
    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
