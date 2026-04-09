package com.naic.config.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.naic.api.service.SmsService;
import com.naic.config.entity.LoginUserDetails;
import com.naic.config.service.LoadUserDetailsService;
import com.naic.domain.entity.cnv.SysUserCnv;
import com.naic.domain.entity.po.SysUserPO;
import com.naic.mapper.SysUserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/10/13
 */
@Slf4j
@Service
@AllArgsConstructor
public class LoadUserDetailsServiceImpl implements LoadUserDetailsService {

    private final SmsService smsService;
    private final SysUserMapper userMapper;
    private final SysUserCnv cnv;

    @Override
    public LoginUserDetails loadByUsername(Map<String, String> map) {
        String username = map.get("username");
        String password = map.get("password");
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new InternalAuthenticationServiceException("请填写完整信息");
        }
        SysUserPO user = userMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery().eq(SysUserPO::getUserName, username));
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("用户不存在");
        }
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        if (user.getPassword() != null && !encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("密码错误");
        }

        return generateUserDetails(user);
    }

    @Override
    public LoginUserDetails loadByTel(Map<String, String> map) {
        String tel = map.get("tel");
        String code = map.get("code");
        if (StringUtils.isEmpty(tel) || StringUtils.isEmpty(code)) {
            throw new InternalAuthenticationServiceException("请填写完整信息");
        }
        if (!smsService.check(tel, code)) {
            throw new BadCredentialsException("验证码错误");
        }
        SysUserPO user = userMapper.selectOne(Wrappers.<SysUserPO>lambdaQuery().eq(SysUserPO::getPhoneNumber, tel));
        if (user == null) {
            throw new AuthenticationCredentialsNotFoundException("用户不存在");
        }

        return generateUserDetails(user);
    }


    private LoginUserDetails generateUserDetails(SysUserPO user) {
        return new LoginUserDetails(cnv.poToRegisterUser(user));
    }

}
