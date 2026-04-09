package com.naic.api.utils.mergeutil.aspect;


import com.naic.api.utils.mergeutil.annonation.MergeResult;
import com.naic.api.utils.mergeutil.core.EntityMerge;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 切面处理
 * @author fx
 */
@Aspect
@Component
@AllArgsConstructor
@ConditionalOnProperty(name = "merge.aop.enabled", matchIfMissing = true)
public class MergeAspect {

    private final EntityMerge entityMerge;

    @Pointcut("@annotation(com.naic.api.utils.mergeutil.annonation.MergeResult)")
    public void methodPointcut() {
    }


    @Around("methodPointcut()&&@annotation(annotation)")
    public Object interceptor(ProceedingJoinPoint pjp, MergeResult annotation) throws Throwable {
        try {
            return entityMerge.merge(pjp, annotation);
        }catch(Exception e){
            return pjp.proceed();
        }
    }
}
