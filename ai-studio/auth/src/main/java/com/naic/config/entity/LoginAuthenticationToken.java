package com.naic.config.entity;

import com.naic.config.service.LoginFunction;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @author HuZhenSha
 * @since 2021/10/20
 */
public class LoginAuthenticationToken extends AbstractAuthenticationToken {

    /**用户登录信息*/
    private final Object principal;
    /**
     * 用户登录方式
     */
    private final LoginFunction loginFunction;

    /**
     * 创建一个未认证的授权令牌,
     * 这时传入的principal是用户名
     */
    public LoginAuthenticationToken(Object principal, LoginFunction loginFunction) {
        super(null);
        this.principal = principal;
        this.loginFunction = loginFunction;
        setAuthenticated(false);
    }

    /**
     * 创建一个已认证的授权令牌,这个方法严格来讲应该由AuthenticationProvider来调用，认证之后再创建以授权令牌
     *
     * 登录认证时，传入的principal为从userService中查出的UserDetails。
     * 参考UsernamePasswordAuthenticationToken实现
     */
    public LoginAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.loginFunction = null;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    public LoginFunction getLoginFunction(){
        return this.loginFunction;
    }

}
