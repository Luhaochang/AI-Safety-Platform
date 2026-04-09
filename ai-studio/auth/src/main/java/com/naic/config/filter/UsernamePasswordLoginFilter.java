package com.naic.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naic.config.entity.LoginAuthenticationToken;
import com.naic.config.service.LoadUserDetailsService;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 定义一个Filter来拦截/login。
 * spring security默认支持form方式登录，所以对于使用json发送登录信息的情况，我们自己定义一个Filter
 * 定义以json格式登录的过滤器
 * @author HuZhenSha
 * @since 2021/9/14
 */
public class UsernamePasswordLoginFilter extends BaseAuthenticationProcessingFilter {

    private final LoadUserDetailsService loadUserDetailsService;


    public UsernamePasswordLoginFilter(String defaultFilterProcessesUrl, LoadUserDetailsService loadUserDetailsService) {
        super(defaultFilterProcessesUrl);
        this.loadUserDetailsService = loadUserDetailsService;
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        checkRequest(request);

        //将传入的json数据转换成map再通过get("key")获得
        Map<?, ?> map = new ObjectMapper().readValue(request.getInputStream(), Map.class);
        LoginAuthenticationToken token  = new LoginAuthenticationToken(map, loadUserDetailsService::loadByUsername);
        // 设置其他补充信息
        // Stores additional details about the authentication request.
        // These might be an IP address, certificate serial number etc.
        token.setDetails(new WebAuthenticationDetails(request));
        return this.getAuthenticationManager().authenticate(token);
    }
}