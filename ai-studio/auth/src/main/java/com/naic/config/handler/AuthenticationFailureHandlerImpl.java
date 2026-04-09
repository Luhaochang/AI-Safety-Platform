package com.naic.config.handler;

import com.naic.api.api.Result;
import com.naic.api.utils.ResponseJsonData;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录失败Handler
 * deal UsernameNotFoundException BadCredentialsException
 * @author HuZhenSha
 * @since 2021/9/14
 */
@Component
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
        ResponseJsonData.response(response, Result.error(exception.getMessage()));
    }
}
