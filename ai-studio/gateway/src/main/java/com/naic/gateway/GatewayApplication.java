package com.naic.gateway;

import com.naic.api.handler.EntityMetaObjectHandler;
import com.naic.api.service.RedisService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @author HuZhenSha
 * @since 2021/9/15
 */
@SpringBootApplication
@EnableFeignClients()
@EnableDiscoveryClient
@MapperScan("com.naic.api.mapper")
@ComponentScan(value = {"com.naic.api", "com.naic.gateway"}, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {EntityMetaObjectHandler.class, RedisService.class}))
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
