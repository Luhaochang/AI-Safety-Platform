package com.naic.framework.handler;

import com.naic.framework.api.Result;
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
public class frameworkExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public Result<String> dealRuntimeException(AccessDeniedException e){
        log.error("权限不足异常", e);
        return Result.error("权限不足异常");
    }


}
