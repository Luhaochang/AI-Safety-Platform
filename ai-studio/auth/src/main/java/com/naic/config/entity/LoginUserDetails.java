package com.naic.config.entity;

import com.naic.api.entity.vo.RegisterUserVO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * spring security 登录用户类
 * @author HuZhenSha
 * @date 2021/9/10 15:55
 */
@Getter
@Setter
public class LoginUserDetails implements UserDetails {

    private final RegisterUserVO user;

    public LoginUserDetails(RegisterUserVO user) {
        this.user = user;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    /**
     * 账号过期
     * @return boolean
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号锁定
     * @return boolean
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否过期
     * Indicates whether the user's credentials (password) has expired. Expired credentials prevent authentication.
     * @return boolean
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号启用
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
