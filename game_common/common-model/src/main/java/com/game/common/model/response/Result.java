package com.game.common.model.response;

import java.io.Serializable;

/**
 * 通用结果包装器
 * 
 * 功能说明：
 * - 为服务方法返回值提供统一的结果封装
 * - 包含成功状态、错误信息、业务数据和错误码
 * - 作为BaseResponse的轻量级替代方案，用于内部服务调用
 * - 支持泛型设计，适用于不同类型的返回数据
 * 
 * 核心字段：
 * - success: 操作成功标识，true表示成功，false表示失败
 * - message: 操作结果消息，成功时为提示信息，失败时为错误描述
 * - data: 业务数据，成功时包含实际返回的业务对象
 * - errorCode: 错误码，失败时用于标识具体的错误类型
 * 
 * 设计优势：
 * - 统一错误处理：所有服务方法都返回相同格式的结果
 * - 类型安全：使用泛型确保数据类型的安全性
 * - 轻量高效：相比完整的响应对象，减少了不必要的字段
 * - 序列化支持：实现Serializable接口，支持分布式传输
 * 
 * 使用模式：
 * - 成功结果：Result.success(data) 或 Result.success(message, data)
 * - 失败结果：Result.error(message) 或 Result.error(errorCode, message)
 * - 判断结果：result.isSuccess() 检查操作是否成功
 * - 获取数据：result.getData() 获取业务数据
 * 
 * 适用场景：
 * - Dubbo RPC服务的返回值封装
 * - 内部服务调用的结果传递
 * - 业务逻辑层的统一异常处理
 * - 分布式服务间的数据交换
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