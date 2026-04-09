package com.naic.api.api;

/**
 * @author HuZhenSha
 * @since 2021/9/13
 */
public interface IResultCode {

    /**
     * 消息
     *
     * @return String
     */
    String getMessage();

    /**
     * 状态码
     *
     * @return int
     */
    Integer getCode();

}
