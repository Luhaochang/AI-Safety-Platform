package com.naic.api.api;

/**
 * @author HuZhenSha
 * @since 2021/9/16
 */
public class Assert {

    public static void fail(String msg){
        throw new ServiceException(msg);
    }

    public static void fail(IResultCode resultCode){
        throw new ServiceException(resultCode);
    }

}
