package com.game.common.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 基础请求类
 * 
 * 功能说明：
 * - 为所有API请求提供通用的基础字段和功能
 * - 包含请求ID、时间戳、签名、客户端版本等公共属性
 * - 提供请求的唯一性标识和重复提交防护
 * - 支持请求的安全校验和版本兼容性检查
 * 
 * 核心字段：
 * - requestId: 请求唯一标识，用于请求追踪和幂等性控制
 * - timestamp: 请求时间戳，用于请求有效期验证
 * - signature: 请求签名，用于安全验证和防篡改
 * - clientVersion: 客户端版本号，用于兼容性检查
 * - deviceId: 设备唯一标识，用于设备管理和安全控制
 * 
 * 设计特点：
 * - 抽象类设计，强制子类实现具体的请求类型
 * - 实现Serializable接口，支持网络传输和缓存存储
 * - 使用Jackson注解，支持JSON序列化和反序列化
 * - 集成Bean Validation，提供参数验证功能
 * 
 * 安全机制：
 * - 时间戳防重放攻击，请求有效期控制
 * - 签名验证防篡改，确保请求完整性
 * - 设备ID绑定，支持设备级的访问控制
 * 
 * 使用方式：
 * - 所有具体的请求类都应继承此基础类
 * - 在请求处理前自动验证公共字段
 * - 通过AOP切面统一处理签名验证等安全检查
 *
 * @author lx
 * @date 2024-01-01
 */
public abstract class BaseRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("requestId")
    @NotNull(message = "Request ID cannot be null")
    private String requestId;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("clientVersion")
    private String clientVersion;

    @JsonProperty("deviceId")
    private String deviceId;

    public BaseRequest() {
        this.timestamp = System.currentTimeMillis();
    }

    public BaseRequest(String requestId) {
        this();
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return "BaseRequest{" +
                "requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", signature='" + signature + '\'' +
                ", clientVersion='" + clientVersion + '\'' +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}