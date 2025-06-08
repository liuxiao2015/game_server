package com.game.common.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User login request
 * Contains account, password, device information and client version
 *
 * @author lx
 * @date 2024-01-01
 */
public class UserLoginRequest extends BaseRequest {

    private static final long serialVersionUID = 1L;

    @JsonProperty("account")
    @NotBlank(message = "Account cannot be blank")
    @Size(min = 3, max = 50, message = "Account length must be between 3 and 50 characters")
    private String account;

    @JsonProperty("password")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100 characters")
    private String password;

    @JsonProperty("deviceInfo")
    private String deviceInfo;

    @JsonProperty("clientVersion")
    private String clientVersion;

    @JsonProperty("platform")
    private String platform;

    public UserLoginRequest() {
        super();
    }

    public UserLoginRequest(String requestId, String account, String password) {
        super(requestId);
        this.account = account;
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public String toString() {
        return "UserLoginRequest{" +
                "account='" + account + '\'' +
                ", password='***'" +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", clientVersion='" + getClientVersion() + '\'' +
                ", platform='" + platform + '\'' +
                ", requestId='" + getRequestId() + '\'' +
                '}';
    }
}