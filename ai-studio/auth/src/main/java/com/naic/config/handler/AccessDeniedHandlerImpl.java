package com.naic.config.handler;

import com.naic.api.api.Result;
import com.naic.api.api.ResultCode;
import com.naic.api.utils.ResponseJsonData;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限不足Handler
 * @author HuZhenSha
 * @since 2021/9/14
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        ResponseJsonData.response(response, Result.error(ResultCode.REQ_REJECT));
    }
}
