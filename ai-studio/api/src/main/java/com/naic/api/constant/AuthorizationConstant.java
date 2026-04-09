package com.naic.api.constant;

/**
 * @author HuZhenSha
 * @since 2021/9/23
 */
public interface AuthorizationConstant {

    String EXPOSE_HEADER = "Access-Control-Expose-Headers";

    /**
     * token 存放在header中的关键字
     */
    String ACCESS_TOKEN_KEYWORD = "Access-Token";

    String TICKET_KEYWORD = "Naic-Ticket";

    /**
     * authorization keyword
     */
    String AUTHORIZATION = "Authorization";

    /**
     * token subject
     */
    String JWT_SUBJECT = "JWT";

    /**
     * token过期时间
     */
    int ACCESS_TOKEN_EXPIRE_TIME = 8*60*60*1000;

    /**
     * refresh token 过期时间
     */
    int TICKET_EXPIRE_TIME = 15*24*60*60*1000;

    String LOGIN_USER_ID_HEADER = "Cur-Login-User";

    String LOGIN_USER_NAME_HEADER = "Cur-Login-User-Name";

    String X_CA_KEY = "x-ca-key";

    String X_CA_SIGNATURE = "x-ca-signature";

    String X_CA_TIMESTAMP ="x-ca-timestamp";

    String NAIC_TENANT = "Naic-Tenant";

}
