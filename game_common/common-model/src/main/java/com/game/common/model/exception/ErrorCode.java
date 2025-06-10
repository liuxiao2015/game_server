package com.game.common.model.exception;

/**
 * 统一错误码定义类
 * 
 * 功能说明：
 * - 定义系统中使用的所有错误码常量
 * - 按照系统错误、业务错误、参数错误等进行分类管理
 * - 提供错误码到错误消息的映射功能
 * - 确保错误处理的标准化和一致性
 * 
 * 错误码分类：
 * - 1000-1999：系统级错误（网络、数据库、缓存等）
 * - 2000-2999：参数错误（缺失、无效、格式错误等）
 * - 3000-3999：认证授权错误（Token、权限、用户验证等）
 * - 4000-4999：业务逻辑错误（用户状态、游戏规则等）
 * - 5000-5999：服务调用错误（RPC、超时、过载等）
 * 
 * 设计原则：
 * - 错误码唯一性，避免重复定义
 * - 分类清晰，便于错误定位和处理
 * - 错误消息国际化友好，支持多语言
 * - 便于监控和统计，支持错误率分析
 * 
 * 使用方式：
 * - ErrorCode.SYSTEM_ERROR 获取错误码
 * - ErrorCode.getMessage(code) 获取错误描述
 * - 在异常类中使用错误码标识具体错误类型
 * 
 * 维护说明：
 * - 新增错误码时要避免与现有错误码冲突
 * - 修改错误码需要考虑向后兼容性
 * - 废弃的错误码应保留一段时间，避免影响旧版本客户端
 *
 * @author lx
 * @date 2024-01-01
 */
public final class ErrorCode {

    private ErrorCode() {
        // Utility class
    }

    // System Error Codes (1000-1999)
    public static final int SYSTEM_ERROR = 1000;
    public static final int NETWORK_ERROR = 1001;
    public static final int TIMEOUT_ERROR = 1002;
    public static final int DATABASE_ERROR = 1003;
    public static final int CACHE_ERROR = 1004;
    public static final int CONFIG_ERROR = 1005;

    // Parameter Error Codes (2000-2999)
    public static final int PARAMETER_ERROR = 2000;
    public static final int PARAMETER_MISSING = 2001;
    public static final int PARAMETER_INVALID = 2002;
    public static final int PARAMETER_FORMAT_ERROR = 2003;

    // Authentication & Authorization Error Codes (3000-3999)
    public static final int AUTH_ERROR = 3000;
    public static final int TOKEN_INVALID = 3001;
    public static final int TOKEN_EXPIRED = 3002;
    public static final int PERMISSION_DENIED = 3003;
    public static final int USER_NOT_FOUND = 3004;
    public static final int PASSWORD_ERROR = 3005;

    // Business Error Codes (4000-4999)
    public static final int BUSINESS_ERROR = 4000;
    public static final int USER_ALREADY_EXISTS = 4001;
    public static final int USER_ALREADY_ONLINE = 4002;
    public static final int GAME_NOT_FOUND = 4003;
    public static final int GAME_FULL = 4004;
    public static final int INSUFFICIENT_BALANCE = 4005;

    // Service Error Codes (5000-5999)
    public static final int SERVICE_ERROR = 5000;
    public static final int SERVICE_UNAVAILABLE = 5001;
    public static final int SERVICE_TIMEOUT = 5002;
    public static final int SERVICE_OVERLOAD = 5003;
    public static final int RPC_ERROR = 5004;

    // Error Messages
    public static String getMessage(int errorCode) {
        switch (errorCode) {
            case SYSTEM_ERROR:
                return "System error";
            case NETWORK_ERROR:
                return "Network error";
            case TIMEOUT_ERROR:
                return "Operation timeout";
            case DATABASE_ERROR:
                return "Database error";
            case CACHE_ERROR:
                return "Cache error";
            case CONFIG_ERROR:
                return "Configuration error";
            case PARAMETER_ERROR:
                return "Parameter error";
            case PARAMETER_MISSING:
                return "Required parameter missing";
            case PARAMETER_INVALID:
                return "Invalid parameter";
            case PARAMETER_FORMAT_ERROR:
                return "Parameter format error";
            case AUTH_ERROR:
                return "Authentication error";
            case TOKEN_INVALID:
                return "Invalid token";
            case TOKEN_EXPIRED:
                return "Token expired";
            case PERMISSION_DENIED:
                return "Permission denied";
            case USER_NOT_FOUND:
                return "User not found";
            case PASSWORD_ERROR:
                return "Password error";
            case BUSINESS_ERROR:
                return "Business error";
            case USER_ALREADY_EXISTS:
                return "User already exists";
            case USER_ALREADY_ONLINE:
                return "User already online";
            case GAME_NOT_FOUND:
                return "Game not found";
            case GAME_FULL:
                return "Game is full";
            case INSUFFICIENT_BALANCE:
                return "Insufficient balance";
            case SERVICE_ERROR:
                return "Service error";
            case SERVICE_UNAVAILABLE:
                return "Service unavailable";
            case SERVICE_TIMEOUT:
                return "Service timeout";
            case SERVICE_OVERLOAD:
                return "Service overload";
            case RPC_ERROR:
                return "RPC call error";
            default:
                return "Unknown error";
        }
    }
}