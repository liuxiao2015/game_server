package com.game.common.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * Base request class for all API requests
 * Contains common fields like request ID, timestamp, signature, etc.
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