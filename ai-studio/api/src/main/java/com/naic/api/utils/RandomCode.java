package com.naic.api.utils;

import java.util.Random;

/**
 * @author HuZhenSha
 * @since 2021/9/17
 */
public class RandomCode {

    /**
     * 邀请码组合 - 数字、大写字母 - 常量
     * 不包换：0,O,1,I
     */
    private static final String RULE_INVITATION_CODE = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int RULE_INVITATION_CODE_LENGTH = 32;
    private static final int CODE_LENGTH_6 = 6;

    /**
     * 验证码组合 - 数字
     */
    private static final String RULE_VERIFICATION_CODE = "123456789";
    private static final int RULE_VERIFICATION_CODE_LENGTH = 9;
    private static final int CODE_LENGTH_4 = 4;

    /**
     * 产生指定位数的随机邀请码
     */
    public static String randomCode() {
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < RandomCode.CODE_LENGTH_6; i++) {
            int j = random.nextInt(RULE_INVITATION_CODE_LENGTH);
            char c = RULE_INVITATION_CODE.charAt(j);
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }

    /**
     * 产生指定位数的随机验证码
     */
    public static String randomVerificationCode(){
        Random random = new Random();
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < RandomCode.CODE_LENGTH_4; i++) {
            int j = random.nextInt(RULE_VERIFICATION_CODE_LENGTH);
            char c = RULE_VERIFICATION_CODE.charAt(j);
            stringBuffer.append(c);
        }
        return stringBuffer.toString();
    }


}
