package com.naic.api.handler;

import com.naic.api.api.Result;
import com.naic.api.api.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * @author wangyunong
 */
@ControllerAdvice
@ResponseBody
@Slf4j
@Order(2)
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Result<String> dealServiceException(ServiceException e){
        log.error("服务器异常", e);
        if (e.getResultCode() != null){
            return Result.error(e.getResultCode());
        } else {
            return Result.error(e.getMessage());
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> dealRuntimeException(RuntimeException e){
        log.error("服务器异常", e);
        return Result.error("服务器异常");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> dealException(Exception e){
        log.error("服务器异常", e);
        return Result.error("服务器异常");
    }

}
