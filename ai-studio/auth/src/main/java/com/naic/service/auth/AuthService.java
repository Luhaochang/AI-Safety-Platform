package com.naic.service.auth;

import com.naic.api.api.DecodedTokenInfo;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

/**
 * @author HuZhenSha
 * @since 2021/10/14
 */
public interface AuthService {

    /**
     * 校验解码 token
     * @param token token
     * @return payload
     * @throws ParseException parse exception
     * @throws JOSEException jose exception
     */
    DecodedTokenInfo checkToken(String token) throws ParseException, JOSEException;


    /**
     * 检查是否登录
     * @param ticket sso ticket
     */
    void checkAuthentication(String ticket);

    /**
     * 刷新 token
     * @param ticket old token
     * @return new token
     * @throws ParseException parse exception
     * @throws JOSEException jose exception
     */
    String refreshToken(String ticket) throws ParseException, JOSEException;

}
