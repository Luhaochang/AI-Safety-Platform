package com.naic.framework.config;

import com.naic.framework.interceptor.FeignRequestInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author wyn
 */
@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor customRequestInterceptor() {
        return new FeignRequestInterceptor();
    }
}
