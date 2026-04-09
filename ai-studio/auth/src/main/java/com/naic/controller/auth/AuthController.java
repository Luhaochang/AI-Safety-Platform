package com.naic.controller.auth;

import com.naic.api.api.DecodedTokenInfo;
import com.naic.api.api.Result;
import com.naic.service.auth.AuthService;
import com.nimbusds.jose.JOSEException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.text.ParseException;
import java.util.Map;

/**
 * @author HuZhenSha
 * @since 2021/10/14
 */
@RestController
@RequestMapping("/auth")
@Api(tags = "认证中心Auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService service;

    @PostMapping("/token/check")
    @ApiOperation(value = "校验token", notes = "子系统调用")
    @ApiIgnore
    public Result<DecodedTokenInfo> checkTicket(@RequestBody Map<String, String> map) throws ParseException, JOSEException {
        String ticket = map.get("token");
        if (ticket == null){
            return Result.error("token must be given");
        }
        return Result.success(service.checkToken(ticket));
    }


    @PostMapping("/check")
    @ApiIgnore
    public Result<?> checkAuthentication(@RequestBody Map<String, String> map){
        String ticket = map.get("ticket");
        if (ticket == null){
            return Result.error("ticket must be given");
        }
        service.checkAuthentication(ticket);
        return Result.success();
    }
}
