package com.naic.utils;

import com.alibaba.fastjson.JSON;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import java.text.ParseException;

/**
 * @author wangyunong
 * @since 2024/3/8
 */
public class JwtUtil {

    /**
     * 密钥  至少32位
     */
    private static final String SECRET = "naic-SaaS-ai-studio-plat111111111111111111111111111";

    /**
     * 对称加密（HMAC）
     * @param payloadStr 对象json串
     * @return jwt
     * @throws JOSEException Javascript Object Signing and Encryption (JOSE) exception.
     */
    public static String generateTokenByHmac(String payloadStr) throws JOSEException {
        //创建JWS头，设置签名算法和类型
        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS256)
                .type(JOSEObjectType.JWT)
                .build();
        //将负载信息封装到Payload中
        Payload payload = new Payload(payloadStr);
        //创建JWS对象
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);
        //创建HMAC签名器
        JWSSigner jwsSigner = new MACSigner(SECRET);
        //签名
        jwsObject.sign(jwsSigner);
        return jwsObject.serialize();
    }

    public static <T> T verifyTokenByHmac(String token, Class<T> clazz) throws ParseException, JOSEException {
        //从token中解析JWS对象
        JWSObject jwsObject = JWSObject.parse(token);
        //创建HMAC验证器
        JWSVerifier jwsVerifier = new MACVerifier(SECRET);
        String payload = jwsObject.getPayload().toString();
        return JSON.parseObject(payload, clazz);
    }

}
