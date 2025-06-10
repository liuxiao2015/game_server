package com.game.common.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Base响应对象
 * 
 * 功能说明：
 * - 封装网络通信的数据传输对象
 * - 提供数据序列化和反序列化支持
 * - 实现参数验证和格式校验
 * - 支持JSON和其他格式的数据转换
 * 
 * 数据结构：
 * - 包含业务处理所需的核心字段
 * - 支持可选字段和默认值设置
 * - 提供数据完整性验证机制
 * 
 * 使用场景：
 * - 客户端与服务器的数据交互
 * - 微服务间的接口调用
 * - API接口的参数传递
 *
 * @author lx
 * @date 2024-01-01
 */
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final String SUCCESS_MESSAGE = "Success";

    @JsonProperty("code")
    private int code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("requestId")
    private String requestId;

    public BaseResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public BaseResponse(int code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public BaseResponse(int code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(SUCCESS_CODE, message, data);
    }

    public static <T> BaseResponse<T> error(String message) {
        return new BaseResponse<>(ERROR_CODE, message);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message);
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    @Override
    public String toString() {
        return "BaseResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}