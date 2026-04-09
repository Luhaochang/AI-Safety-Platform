package com.naic.framework.api;

import javax.servlet.http.HttpServletResponse;

/**
 * des: 返回结果状态
 * @author HuZhenSha
 * @date 2021/3/5 15:23
 */
public enum ResultCode implements IResultCode{

    /**
     * 成功
     */
    SUCCESS(HttpServletResponse.SC_OK, "操作成功"),

    /**
     * 业务异常
     */
    FAILURE(HttpServletResponse.SC_BAD_REQUEST, "业务异常"),

    /**
     * 请求未授权
     */
    UN_AUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "请求未授权"),

    /**
     * 404 没找到请求
     */
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "404 没找到请求"),

    /**
     * 消息不能读取
     */
    MSG_NOT_READABLE(HttpServletResponse.SC_BAD_REQUEST, "消息不能读取"),

    /**
     * 不支持当前请求方法
     */
    METHOD_NOT_SUPPORTED(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "不支持当前请求方法"),

    /**
     * 不支持当前媒体类型
     */
    MEDIA_TYPE_NOT_SUPPORTED(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "不支持当前媒体类型"),

    /**
     * 请求被拒绝
     */
    REQ_REJECT(HttpServletResponse.SC_FORBIDDEN, "请求被拒绝"),

    /**
     * 服务器异常
     */
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器异常"),

    /**
     * 缺少必要的请求参数
     */
    PARAM_MISS(HttpServletResponse.SC_BAD_REQUEST, "缺少必要的请求参数"),

    /**
     * 请求参数类型错误
     */
    PARAM_TYPE_ERROR(HttpServletResponse.SC_BAD_REQUEST, "请求参数类型错误"),

    /**
     * 请求参数绑定错误
     */
    PARAM_BIND_ERROR(HttpServletResponse.SC_BAD_REQUEST, "请求参数绑定错误"),

    /**
     * 参数校验失败
     */
    PARAM_VALID_ERROR(HttpServletResponse.SC_BAD_REQUEST, "参数校验失败"),

    /**
     * 该记录已经存在
     */
    DUPLICATED_RECORD_ERROR(10001, "该记录已经存在"),
    /**
     * 该记录不存在
     */
    NONE_RECORD_ERROR(10002, "该记录不存在"),
    /**
     * 提交内容有误
     */
    CONTENT_ERROR(1003, "提交内容有误"),
    /**
     * 重复的主键
     */
    DUPLICATED_KEY_ERROR(1004,"重复的主键"),
    /**
     * 主键不存在
     */
    NONE_KEY_ERROR(1005,"主键不存在"),
    /**
     * 存在交叉的时间段
     */
    EXIST_CROSS_DATE(41009, "存在交叉的时间段"),
    /**
     * 该车间目前没有休息日
     */
    REST_DAY_NOT_EXIST(41005, "该车间目前没有休息日"),
    /**
     * 月份错误
     */
    MONTH_ERROR(41006, "月份错误"),
    /**
     * 数据库异常：某产线不存在
     */
    DATABASE_EXCEPTION_LINE_NULL(41010, "数据库异常：某产线不存在");
    private final int code;

    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }


}
