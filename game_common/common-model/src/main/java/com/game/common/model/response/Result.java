package com.game.common.model.response;

import java.io.Serializable;

/**
 * Generic result wrapper for service method returns
 * Lightweight alternative to BaseResponse for internal service calls
 *
 * @author lx
 * @date 2024-01-01
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private T data;
    private int errorCode;

    public Result() {
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(true, "Success");
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, "Success", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, message, data);
    }

    public static <T> Result<T> failure(String message) {
        return new Result<>(false, message);
    }

    public static <T> Result<T> failure(int errorCode, String message) {
        Result<T> result = new Result<>(false, message);
        result.setErrorCode(errorCode);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode=" + errorCode +
                '}';
    }
}