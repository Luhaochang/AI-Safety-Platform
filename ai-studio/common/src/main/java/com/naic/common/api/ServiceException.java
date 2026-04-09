package com.naic.common.api;

/**
 * @author HuZhenSha
 * @date 2021/3/10 16:35
 */
public class ServiceException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private final IResultCode resultCode;

    public ServiceException(String message) {
        super(message);
        this.resultCode = null;
    }

    public ServiceException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public ServiceException(IResultCode resultCode, Throwable cause) {
        super(cause);
        this.resultCode = resultCode;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

    public Throwable doFillInStackTrace() {
        return super.fillInStackTrace();
    }

    public IResultCode getResultCode() {
        return this.resultCode;
    }
}
