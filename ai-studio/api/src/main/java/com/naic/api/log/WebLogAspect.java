package com.naic.api.log;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @author fx, HuZhenSha
 * @date 2021/3/30 10:11
 */
@Aspect
@Component
@Slf4j
public class WebLogAspect {


    /** 匹配所有controller为切点 */
    @Pointcut("execution(* com.naic..*Controller.*(..))")
    public void webLog() {}


    /**
     * 环绕
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime time = LocalDateTime.now();
        Object result = joinPoint.proceed();
        LogUtil.logInfo(getFunctionDescription(joinPoint), time,
                joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName(),
                joinPoint.getArgs(), result);
        return result;
    }

    @AfterThrowing(value = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Exception {
        LocalDateTime time = LocalDateTime.now();
        LogUtil.logError(getFunctionDescription(joinPoint), time,
                joinPoint.getSignature().getDeclaringTypeName() + joinPoint.getSignature().getName(),
                joinPoint.getArgs(), e.getMessage());
    }


    /**
     * 获取切面注解的描述
     * @param joinPoint 切点
     * @return 描述信息
     * @throws Exception 异常
     */
    public String getFunctionDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class<?> targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] clazzArray = method.getParameterTypes();
                if (clazzArray.length == arguments.length) {
                    ApiOperation des = method.getAnnotation(ApiOperation.class);
                    if (des != null){
                        return des.value();
                    }
                }
            }
        }
        return null;
    }
}
