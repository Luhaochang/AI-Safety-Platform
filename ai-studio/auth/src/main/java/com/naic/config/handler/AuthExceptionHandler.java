package com.naic.config.handler;

import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * @author wangyunong
 */
@ControllerAdvice
@ResponseBody
@Slf4j
@Order(1)
public class AuthExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> dealRuntimeException(AccessDeniedException e){
        log.error("权限不足异常", e);
        return Result.error("权限不足异常");
    }


}
