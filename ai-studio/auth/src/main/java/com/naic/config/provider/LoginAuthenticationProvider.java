package com.naic.config.provider;

import com.naic.config.entity.LoginAuthenticationToken;
import com.naic.config.entity.LoginUserDetails;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/10/20
 */
@AllArgsConstructor
public class LoginAuthenticationProvider implements AuthenticationProvider {

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LoginAuthenticationToken loginAuthenticationToken = ((LoginAuthenticationToken) authentication);
        Map<String, String> map = (Map<String, String>) authentication.getPrincipal();
        // 获取用户
        LoginUserDetails userDetails = loginAuthenticationToken.getLoginFunction().login(map);
        // 构造已认证的authentication,这时的principal为User对象
        LoginAuthenticationToken authenticatedToken = new LoginAuthenticationToken(userDetails, authentication.getAuthorities());
        // 设置一些其他信息。Stores additional details about the authentication request.
        // These might be an IP address, certificate serial number etc.
        authenticatedToken.setDetails(authenticatedToken.getDetails());
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (LoginAuthenticationToken.class
                .isAssignableFrom(authentication));
    }
}
