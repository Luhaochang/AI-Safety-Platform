package com.naic.config.handler;

import com.naic.api.api.Result;
import com.naic.api.utils.ResponseJsonData;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 未登录Handler
 * @author HuZhenSha
 * @since 2021/9/14
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        // 认证异常authException用户请求
        ResponseJsonData.response(response, Result.error(authException.getMessage()));
    }
}
