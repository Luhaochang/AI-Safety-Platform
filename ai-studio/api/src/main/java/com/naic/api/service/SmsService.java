package com.naic.api.service;

/**
 * @author HuZhenSha
 * @since 2021/10/9
 */
public interface SmsService {

    /**
     * 发送短信
     * @param tel tel
     * @throws  Exception exception
     */
    void send(String tel) throws Exception;

    /**
     * 发送到期提醒短信
     * @param tel tel
     * @throws  Exception exception
     */
    void sendExpirationReminder(String tel) throws Exception;

    /**
     * 校验用户验证码
     * @param tel tel
     * @param code 验证码
     * @return bool
     */
    boolean check(String tel, String code);

}
