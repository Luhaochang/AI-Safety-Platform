package com.naic.productionline;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import com.naic.productionline.config.AirflowConfig;

@EnableDiscoveryClient()
@ComponentScan({"com.naic.api","com.naic.common","com.naic.framework","com.naic.productionline"})
@MapperScan("com.naic.productionline.mapper")
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.naic.api","com.naic.common","com.naic.framework","com.naic.productionline"} )
@EnableConfigurationProperties(AirflowConfig.class)
public class ProductionLineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductionLineApplication.class, args);
    }

}
