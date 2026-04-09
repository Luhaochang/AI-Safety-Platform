package com.naic.controller.auth;

import com.naic.api.api.Result;
import com.naic.api.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HuZhenSha
 * @since 2021/9/27
 */
@RestController
@RequestMapping("/sms")
@Api(tags = "Sms短信服务")
@AllArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService service;

    @PostMapping("/send")
    @ApiOperation("提醒发送短信")
    public Result<?> send(String tel) throws Exception {
        service.send(tel);
        return Result.success();
    }

    @PostMapping("/check")
    @ApiOperation("验证码认证")
    public Result<?> check(String tel, String code){
        boolean res = service.check(tel, code);
        if (! res){
            return Result.error("验证码错误或已失效");
        }
        return Result.success();
    }

}
