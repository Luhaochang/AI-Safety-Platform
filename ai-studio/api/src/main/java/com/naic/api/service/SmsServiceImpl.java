package com.naic.api.service;

import com.alibaba.fastjson.JSON;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import com.naic.api.api.ServiceException;
import com.naic.api.constant.BusinessConstant;
import com.naic.api.entity.dto.SmsEntity;
import com.naic.api.utils.RandomCode;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author HuZhenSha
 * @since 2021/10/9
 */
@AllArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final RedisService redisService;


    @Override
    public void send(String tel) throws Exception {
        String oldCode = redisService.get(BusinessConstant.VERIFICATION_CODE_PREFIX + tel);
        if(oldCode!=null){
            Long expireTime = redisService.expireTimeGet(BusinessConstant.VERIFICATION_CODE_PREFIX + tel);
            if (expireTime>240){
                throw new ServiceException("请在一分钟后再次尝试发送短信验证码！");
            }
        }

        com.aliyun.dysmsapi20170525.Client client = createClient("YOUR_ACCESS_KEY_ID", "YOUR_ACCESS_KEY_SECRET");
        String code = RandomCode.randomVerificationCode();
        redisService
                .set(BusinessConstant.VERIFICATION_CODE_PREFIX + tel, code, Duration.ofMinutes(BusinessConstant.VERIFICATION_CODE_EXPIRATION_MINUTE));
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setTemplateParam(JSON.toJSONString(new SmsEntity().setCode(code)))
                .setPhoneNumbers(tel)
                .setSignName("北京云辰")
                .setTemplateCode("SMS_225825037");
        client.sendSms(sendSmsRequest);
    }

    @Override
    public void sendExpirationReminder(String tel) throws Exception {
        com.aliyun.dysmsapi20170525.Client client = createClient("YOUR_ACCESS_KEY_ID", "YOUR_ACCESS_KEY_SECRET");
        String code = RandomCode.randomVerificationCode();
        redisService
                .set(BusinessConstant.VERIFICATION_CODE_PREFIX + tel, code, Duration.ofMinutes(BusinessConstant.VERIFICATION_CODE_EXPIRATION_MINUTE));
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setTemplateParam(JSON.toJSONString(new SmsEntity().setCode(code)))
                .setPhoneNumbers(tel)
                .setSignName("北京云辰")
                .setTemplateCode("SMS_225825037");
        client.sendSms(sendSmsRequest);
    }

    @Override
    public boolean check(String tel, String code) {
        if (code == null){
            return false;
        }
        String curCode = redisService.get(BusinessConstant.VERIFICATION_CODE_PREFIX + tel);
        return curCode != null && curCode.equals(code);
    }


    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId AccessKey ID
     * @param accessKeySecret AccessKey Secret
     * @return Client
     * @throws Exception exception
     */
    private com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

}
