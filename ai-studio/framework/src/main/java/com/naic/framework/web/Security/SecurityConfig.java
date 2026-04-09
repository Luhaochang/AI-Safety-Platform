package com.naic.framework.web.Security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * spring security 策略配置
 * @author HuZhenSha
 * @since 2021/9/10
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // 其他配置 ...
                .authorizeRequests().anyRequest().permitAll()
                // 如果使用了@PreAuthorize注解，确保prePostEnabled为true
                .and()
                .csrf().disable(); // 关闭CSRF保护，如果你的应用不需要CSRF保护则不需要关闭
    }
}