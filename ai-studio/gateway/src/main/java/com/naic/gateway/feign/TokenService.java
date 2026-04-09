package com.naic.gateway.feign;

import com.naic.api.api.DecodedTokenInfo;
import com.naic.api.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/10/11
 */
@FeignClient(value = "ai-studio-auth")
public interface TokenService {


    /**
     * 校验令牌
     * @param map token
     * @return {@link Result<DecodedTokenInfo>} tokenPayload
     */
    @PostMapping("/auth/token/check")
    Result<DecodedTokenInfo> checkToken(@RequestBody Map<String, String> map);

}