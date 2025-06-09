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
    
    /**

    
     * Result方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    private Result(boolean success, T data, String errorMsg, int errorCode) {
        this.success = success;
        this.data = data;
        this.errorMsg = errorMsg;
        this.errorCode = errorCode;
    }
    
    /**

    
     * success方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public static <T> Result<T> success() {
        return new Result<>(true, null, null, 0);
    }
    
    /**

    
     * success方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, 0);
    }
    
    /**

    
     * failure方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public static <T> Result<T> failure(String errorMsg) {
        return new Result<>(false, null, errorMsg, -1);
    }
    
    /**

    
     * failure方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
    public static <T> Result<T> failure(int errorCode, String errorMsg) {
        return new Result<>(false, null, errorMsg, errorCode);
    }
    
    /**

    
     * isSuccess方法

    
     * 

    
     * 功能说明：

    
     * - 执行核心业务逻辑处理

    
     * - 提供数据验证和错误处理

    
     * - 确保操作的原子性和一致性

    
     */

    
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