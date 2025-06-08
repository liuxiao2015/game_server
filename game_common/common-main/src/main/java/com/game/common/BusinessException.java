package com.game.common;

/**
 * Business exception with error code and message
 * Supports chained exceptions
 *
 * @author lx
 * @date 2024-01-01
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    /**
     * Creates a new BusinessException with error code and message
     * 
     * @param code error code
     * @param message error message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * Creates a new BusinessException with error code, message and cause
     * 
     * @param code error code
     * @param message error message
     * @param cause the cause
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * Gets the error code
     * 
     * @return error code
     */
    public int getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}