package com.game.common;

/**
 * Unified response result following Alibaba specifications
 *
 * @author lx
 * @date 2024-01-01
 */
/**
 * Result
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class Result<T> {
    
    private int code;
    private String message;
    private T data;
    private long timestamp;
    
    /**
     * Creates a new Result
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a successful result with data
     * 
     * @param data result data
     * @param <T> data type
     * @return successful result
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = Constants.SUCCESS;
        result.message = "success";
        result.data = data;
        return result;
    }
    
    /**
     * Creates a successful result without data
     * 
     * @param <T> data type
     * @return successful result
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * Creates a failed result with error code and message
     * 
     * @param code error code
     * @param message error message
     * @param <T> data type
     * @return failed result
     */
    public static <T> Result<T> failure(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = null;
        return result;
    }
    
    /**
     * Creates a failed result with exception
     * 
     * @param e the exception
     * @param <T> data type
     * @return failed result
     */
    public static <T> Result<T> failure(BusinessException e) {
        return failure(e.getCode(), e.getMessage());
    }
    
    /**
     * Checks if the result is successful
     * 
     * @return true if successful
     */
    public boolean isSuccess() {
        return code == Constants.SUCCESS;
    }
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
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
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}