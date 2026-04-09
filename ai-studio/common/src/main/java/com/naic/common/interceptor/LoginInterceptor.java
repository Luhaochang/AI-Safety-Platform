package com.naic.common.interceptor;

import com.naic.common.constant.AuthorizationConstant;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wangyunong
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    public LoginInterceptor() {
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) {
        String header = request.getHeader(AuthorizationConstant.LOGIN_USER_ID_HEADER);

        if (!StringUtils.isEmpty(header)) {
            Long userId = Long.parseLong(header);
            LoginIdHolder.setLoginId(userId);
        }
        return true;
    }
}
