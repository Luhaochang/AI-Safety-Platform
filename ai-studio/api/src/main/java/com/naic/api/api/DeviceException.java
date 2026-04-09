package com.naic.api.api;

/**
 * @author hyh
 */
public class DeviceException extends RuntimeException {
    private final IResultCode resultCode;

    public DeviceException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public IResultCode getResultEnum() {
        return resultCode;
    }

    public IResultCode getResultCode() {
        return this.resultCode;
    }
}
