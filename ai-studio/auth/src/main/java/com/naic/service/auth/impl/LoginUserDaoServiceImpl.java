package com.naic.service.auth.impl;

import com.alibaba.fastjson.JSON;
import com.naic.api.api.Assert;
import com.naic.api.api.Payload;
import com.naic.api.api.ResultCode;
import com.naic.api.api.TokenPayload;
import com.naic.api.constant.AuthorizationConstant;
import com.naic.api.entity.vo.RegisterUserVO;
import com.naic.api.service.RedisService;
import com.naic.service.auth.LoginUserDaoService;
import com.naic.utils.JwtUtil;
import com.nimbusds.jose.JOSEException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;

/**
 * @author HuZhenSha
 * @since 2021/11/12
 */
@Service
@AllArgsConstructor
public class LoginUserDaoServiceImpl implements LoginUserDaoService {

    private final RedisService redisService;

    @Override
    public void addUser(String ticket, RegisterUserVO user) {
        if (user != null){
            removeUserById(user.getUserId());
            redisService.set(AuthorizationConstant.AUTHORIZATION + user.getUserId(), ticket, Duration.ofMillis(AuthorizationConstant.TICKET_EXPIRE_TIME));
        }
        redisService.set(ticket, JSON.toJSONString(user), Duration.ofMillis(AuthorizationConstant.TICKET_EXPIRE_TIME));
    }

    @Override
    public void removeUserById(Long userId) {
        if (userId != null){
            String pre = redisService.get(AuthorizationConstant.AUTHORIZATION + userId);
            if (pre != null){
                redisService.remove(pre);
            }
            redisService.remove(AuthorizationConstant.AUTHORIZATION + userId);
        }
    }

    @Override
    public void removeUserByToken(String token) {
        if (token != null){
            try {
                TokenPayload tokenPayload = JwtUtil.verifyTokenByHmac(token, TokenPayload.class);
                removeUserByTicket(tokenPayload.getTicket());
            } catch (ParseException | JOSEException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void removeUserByTicket(String ticket) {
        if (ticket != null){
            redisService.remove(ticket);
            try {
                Payload payload = JwtUtil.verifyTokenByHmac(ticket, Payload.class);
                redisService.remove(AuthorizationConstant.AUTHORIZATION + payload.getUserId());
            } catch (ParseException | JOSEException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public RegisterUserVO loadUserByTicket(String ticket) {
        String userStr = redisService.get(ticket);
        if (userStr == null){
            Assert.fail(ResultCode.UN_AUTHORIZED);
        }
        return JSON.parseObject(userStr, RegisterUserVO.class);
    }

    @Override
    public boolean checkLogin(String ticket) {
        return redisService.hasKey(ticket);
    }
}
