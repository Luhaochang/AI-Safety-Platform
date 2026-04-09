package com.naic.api.service;

/**
 * @author HuZhenSha
 * @since 2021/12/2
 */
public class StandaloneSmsServiceImpl implements SmsService {
    @Override
    public void send(String tel) throws Exception {

    }

    @Override
    public void sendExpirationReminder(String tel) throws Exception {

    }

    @Override
    public boolean check(String tel, String code) {
        return true;
    }
}
