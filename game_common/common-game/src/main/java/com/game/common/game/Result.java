package com.game.common.game;

/**
 * 通用结果包装类
 *
 * @author lx
 * @date 2025/06/08
 */
public class Result<T> {
    
    private final boolean success;
    private final T data;
    private final String errorMsg;
    private final int errorCode;
    
    private Result(boolean success, T data, String errorMsg, int errorCode) {
        this.success = success;
        this.data = data;
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }
    
    public static <T> Result<T> success() {
        return new Result<>(true, null, null, 0);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, 0);
    }
    
    public static <T> Result<T> failure(String errorMsg) {
        return new Result<>(false, null, errorMsg, -1);
    }
    
    public static <T> Result<T> failure(int errorCode, String errorMsg) {
        return new Result<>(false, null, errorMsg, errorCode);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorMsg() {
        return errorMsg;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}