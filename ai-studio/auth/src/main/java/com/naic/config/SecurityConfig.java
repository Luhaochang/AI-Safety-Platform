package com.naic.config;

import com.naic.config.filter.TelAuthenticationFilter;
import com.naic.config.filter.UsernamePasswordLoginFilter;
import com.naic.config.provider.LoginAuthenticationProvider;
import com.naic.config.service.impl.LoadUserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

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

    private final LoadUserDetailsServiceImpl loadUserDetailsService;

    private final AuthenticationSuccessHandler loginSuccessHandler;
    private final AuthenticationFailureHandler loginFailureHandler;

    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final LogoutSuccessHandler logoutSuccessHandler;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                //没有权限处理
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                //未登录处理
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint)
                .and()
                //退出成功处理
                .logout().logoutUrl("/logout").logoutSuccessHandler(logoutSuccessHandler).permitAll();

        // 禁用session
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 对于用户自定义的filter，如果要加入spring security 的FilterChain中，必须指定加到已有的那个filter之前或者之后。
        httpSecurity.addFilterBefore(telAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        //将自定义的过滤器(以json格式登录的过滤器)加进来，第二参数表示加到usernamePasswordAuthenticationFilter所在的位置
        httpSecurity.addFilterAt(usernamePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        /*
        有3中方式可以实现动态权限控制：
        （1）扩展access()的SpEL表达式.  扩展access()的SpEL表达式：自定义授权逻辑myAuthService是自定义的类，canAccess是它的方法
        .anyRequest().access("@myAuthService.canAccess(request,authentication)")
        （2）自定义AccessDecisionManager.   自定义AccessDecisionManager
        .withObjectPostProcessor(new MyObjectPostProcessor())
        .anyRequest().authenticated()
        （3）自定义Filter：MyFilterSecurityInterceptor
         */
        httpSecurity.authorizeRequests().anyRequest().permitAll()
                /*.access("@userAuthentication.authenticate(request, authentication)")*/;

        //开启跨域访问
        httpSecurity.cors().disable();
        //开启模拟请求，比如API POST测试工具的测试，不开启时，API POST为报403错误
        httpSecurity.csrf().disable();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/swagger-ui/**");
        web.ignoring().antMatchers("/swagger-resources/**");
        web.ignoring().antMatchers("/v3/**");
        web.ignoring().antMatchers("/doc.html");
        web.ignoring().antMatchers("/webjars/**");
        // 放行注册请求
        web.ignoring().antMatchers("/register");
        //对于在header里面增加token等类似情况，放行所有OPTIONS请求。
        web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
        web.ignoring().antMatchers("/static/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        // 配置所有的Provider
        auth.authenticationProvider(loginAuthenticationProvider());
    }

    @Bean
    UsernamePasswordLoginFilter usernamePasswordAuthenticationFilter() throws Exception{
        UsernamePasswordLoginFilter filter = new UsernamePasswordLoginFilter("/login", loadUserDetailsService);
        // 将存放身份信息bean加入
        filter.setAuthenticationManager(authenticationManagerBean());
        filter.setAuthenticationSuccessHandler(loginSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler);
        return filter;

    }

    @Bean
    TelAuthenticationFilter telAuthenticationFilter() throws Exception {
        TelAuthenticationFilter telAuthenticationFilter = new TelAuthenticationFilter("/login/tel", loadUserDetailsService);
        telAuthenticationFilter.setAuthenticationManager(authenticationManagerBean());
        telAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);
        telAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler);
        return telAuthenticationFilter;
    }

    @Bean
    LoginAuthenticationProvider loginAuthenticationProvider(){
        return new LoginAuthenticationProvider();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}