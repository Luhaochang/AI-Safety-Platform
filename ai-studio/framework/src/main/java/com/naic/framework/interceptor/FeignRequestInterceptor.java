package com.naic.framework.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wyn
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 尝试从RequestContextHolder获取当前的HttpServletRequest
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 从HttpServletRequest中提取你需要的信息
            String authHeader = request.getHeader("Cur-Login-User");

            // 将信息添加到Feign的请求头中
            if (authHeader != null) {
                template.header("Cur-Login-User", authHeader);
            }
        }
    }
}
