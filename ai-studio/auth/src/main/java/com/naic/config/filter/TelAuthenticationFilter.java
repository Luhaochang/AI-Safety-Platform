package com.naic.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.naic.config.entity.LoginAuthenticationToken;
import com.naic.config.service.LoadUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 对用户的token进行鉴别
 * @author HuZhenSha
 * @since 2021/9/23
 */
public class TelAuthenticationFilter extends BaseAuthenticationProcessingFilter {

    private final LoadUserDetailsService loadUserDetailsService;


    public TelAuthenticationFilter(String defaultFilterProcessesUrl, LoadUserDetailsService loadUserDetailsService) {
        super(defaultFilterProcessesUrl);
        this.loadUserDetailsService = loadUserDetailsService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException {

        checkRequest(request);
        //将传入的json数据转换成map再通过get("key")获得
        Map<?, ?> map = new ObjectMapper().readValue(request.getInputStream(), Map.class);
        LoginAuthenticationToken token  = new LoginAuthenticationToken(map, loadUserDetailsService::loadByTel);
        // 设置其他补充信息
        // Stores additional details about the authentication request.
        // These might be an IP address, certificate serial number etc.
        token.setDetails(new WebAuthenticationDetails(request));
        return this.getAuthenticationManager().authenticate(token);
    }

}
