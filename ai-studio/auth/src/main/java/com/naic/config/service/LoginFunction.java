package com.naic.config.service;

import com.naic.config.entity.LoginUserDetails;
import com.nimbusds.jose.JOSEException;

import java.util.Map;

/**
 * 动态登录方式
 * @author HuZhenSha
 * @since 2021/10/20
 */
@FunctionalInterface
public interface LoginFunction {

    /**
     * 登录
     * @param map 登录信息
     * @throws JOSEException jose exception
     * @return
     */
    LoginUserDetails login(Map<String, String> map) throws JOSEException;

}
