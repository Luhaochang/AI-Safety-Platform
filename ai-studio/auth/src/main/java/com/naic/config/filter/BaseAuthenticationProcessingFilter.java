package com.naic.config.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * @author HuZhenSha
 * @since 2021/10/20
 */
public abstract class BaseAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
    protected BaseAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    protected BaseAuthenticationProcessingFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    protected void checkRequest(HttpServletRequest request){
        if(! request.getMethod().equals(HttpMethod.POST.name())){
            throw new AuthenticationServiceException(
                    "Authentication method not supported" + request.getMethod());
        }
        //以json的形式传递参数
        if (! request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE)) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
    }
}
