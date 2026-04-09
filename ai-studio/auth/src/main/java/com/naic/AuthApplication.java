package com.naic;

import com.naic.api.service.RedisService;
import com.naic.api.service.SmsService;
import com.naic.api.service.SmsServiceImpl;
import com.naic.api.service.StandaloneSmsServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan({"com.naic.api","com.naic"})
@MapperScan("com.naic.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    @Profile("standalone")
    SmsService standaloneSmsService(){
        return new StandaloneSmsServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(SmsService.class)
    SmsService smsService(RedisService redisService){
        return new SmsServiceImpl(redisService);
    }
}
