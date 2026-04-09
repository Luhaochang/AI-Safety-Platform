package com.naic.log.aspect;

import com.naic.log.annotation.Log;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Operation log AOP aspect
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null);
    }

    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e);
    }

    private void handleLog(JoinPoint joinPoint, Log controllerLog, Exception e) {
        try {
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            if (e != null) {
                log.error("[OperationLog] {}.{} failed: {}", className, methodName, e.getMessage());
            } else {
                log.info("[OperationLog] {}.{} executed", className, methodName);
            }
        } catch (Exception ex) {
            log.error("LogAspect error", ex);
        }
    }
}
