package com.naic.productionline.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author wyn
 */
@Configuration
public class AirflowFeignConfig {

    private final AirflowConfig airflowConfig;

    public AirflowFeignConfig(AirflowConfig airflowConfig) {
        this.airflowConfig = airflowConfig;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String auth = airflowConfig.getUsername() + ":" + airflowConfig.getPassword();
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "Basic " + new String(encodedAuth);
                template.header("Authorization", authHeader);
                template.header("Content-Type", "application/json");
            }
        };
    }
}
