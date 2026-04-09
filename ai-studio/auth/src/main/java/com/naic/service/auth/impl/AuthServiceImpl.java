package com.naic.service.auth.impl;

import com.alibaba.fastjson.JSON;
import com.naic.api.api.*;
import com.naic.service.auth.AuthService;
import com.naic.service.auth.LoginUserDaoService;
import com.naic.utils.JwtUtil;
import com.nimbusds.jose.JOSEException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;

/**
 * @author HuZhenSha
 * @since 2021/10/14
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final LoginUserDaoService loginUserDaoService;


    @Override
    public DecodedTokenInfo checkToken(String token) throws ParseException, JOSEException {
        TokenPayload accessInfo = JwtUtil.verifyTokenByHmac(token, TokenPayload.class);
        checkAuthentication(accessInfo.getTicket());
        long curMills = System.currentTimeMillis();
        // 构造返回对象
        DecodedTokenInfo decodedTokenInfo = new DecodedTokenInfo();
        if (accessInfo.getExp() < curMills) {
            // access token 过期  ticket 没过期
            // 刷新 access token
            decodedTokenInfo.setNewToken(refreshToken(accessInfo.getTicket()));
        }
        decodedTokenInfo.setPayload(accessInfo);
        return decodedTokenInfo;
    }


    @Override
    public void checkAuthentication(String ticket) {
        if (!loginUserDaoService.checkLogin(ticket)) {
            Assert.fail(ResultCode.UN_AUTHORIZED);
        }
    }

    @Override
    public String refreshToken(String ticket) throws ParseException, JOSEException {
        checkAuthentication(ticket);
        Payload ticketInfo = JwtUtil.verifyTokenByHmac(ticket, Payload.class);
        return JwtUtil.generateTokenByHmac(JSON.toJSONString(new TokenPayload(ticketInfo, ticket)));
    }

}
