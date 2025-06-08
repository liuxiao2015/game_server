package com.game.common.model.exception;

/**
 * RPC service exception for Dubbo services
 * Contains error code, error message and exception chain
 *
 * @author lx
 * @date 2024-01-01
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int errorCode;
    private final String errorMessage;

    public ServiceException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ServiceException(int errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}