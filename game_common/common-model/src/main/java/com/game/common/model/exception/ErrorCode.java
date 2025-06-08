package com.game.common.model.exception;

/**
 * Unified error code definitions
 * Categorized by system errors, business errors, and parameter errors
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