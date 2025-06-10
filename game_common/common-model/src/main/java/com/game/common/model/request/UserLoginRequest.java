package com.game.common.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求对象
 * 
 * 功能说明：
 * - 封装客户端登录请求的所有必要信息
 * - 包含账号密码、设备信息、客户端版本等关键数据
 * - 提供输入参数验证和格式校验功能
 * - 支持多平台客户端的统一登录协议
 * 
 * 数据字段：
 * - account: 用户账号（用户名、邮箱、手机号等）
 * - password: 用户密码（需要前端加密传输）
 * - deviceInfo: 设备信息（设备型号、操作系统版本等）
 * - clientVersion: 客户端版本号（用于兼容性检查）
 * - platform: 客户端平台（Android、iOS、Web等）
 * 
 * 安全考虑：
 * - 密码字段在日志输出时会被隐藏
 * - 支持账号格式的多种验证规则
 * - 设备信息用于异常登录检测
 * 
 * 验证规则：
 * - 账号长度：3-50字符，不能为空
 * - 密码长度：6-100字符，不能为空
 * - 其他字段为可选项，用于增强功能
 *
 * @author lx
 * @date 2024-01-01
 */
public class UserLoginRequest extends BaseRequest {

    // 序列化版本号，用于对象序列化和反序列化的版本控制
    private static final long serialVersionUID = 1L;

    // 用户账号，支持用户名、邮箱、手机号等多种格式
    // 长度限制：3-50字符，不能为空白字符
    @JsonProperty("account")
    @NotBlank(message = "Account cannot be blank")
    @Size(min = 3, max = 50, message = "Account length must be between 3 and 50 characters")
    private String account;

    // 用户密码，建议客户端使用MD5或SHA256加密后传输
    // 长度限制：6-100字符，支持密文传输
    @JsonProperty("password")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100 characters")
    private String password;

    // 设备信息，JSON格式字符串，包含设备型号、系统版本、屏幕分辨率等
    // 用于设备管理、异常登录检测和数据统计分析
    @JsonProperty("deviceInfo")
    private String deviceInfo;

    // 客户端版本号，格式如"1.0.0"，用于版本兼容性检查和强制更新控制
    @JsonProperty("clientVersion")
    private String clientVersion;

    // 客户端平台标识，如"Android"、"iOS"、"Web"、"PC"等
    // 用于平台差异化处理和功能适配
    @JsonProperty("platform")
    private String platform;

    /**
     * 默认构造方法
     * 创建空的登录请求对象，通常用于JSON反序列化
     */
    public UserLoginRequest() {
        super();
    }

    /**
     * 构造包含基础登录信息的请求对象
     * 
     * @param requestId 请求唯一标识，用于请求追踪和重复提交防护
     * @param account 用户账号
     * @param password 用户密码
     */
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