package com.game.frame.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 统一认证服务
 * 处理完整的认证流程：账号密码验证、多因素认证、Token生成、登录日志
 * @author lx
 * @date 2025/06/08
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private static final String LOGIN_ATTEMPT_PREFIX = "security:login:attempt:";
    private static final String TEMP_AUTH_PREFIX = "security:temp:auth:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int TEMP_AUTH_EXPIRE_MINUTES = 10;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private MfaService mfaService;
    
    @Autowired
    private TokenManager tokenManager;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 第一阶段认证：账号密码验证
     */
    public AuthenticationResult authenticateCredentials(String username, String password, String clientIp) {
        try {
            // 检查账号是否被锁定
            if (isAccountLocked(username)) {
                logger.warn("Account locked due to too many failed attempts: {}", username);
                return AuthenticationResult.accountLocked();
            }
            
            // TODO: 从数据库验证用户名密码
            // User user = userService.findByUsername(username);
            // 模拟用户验证
            AuthUser user = validateUserCredentials(username, password);
            
            if (user == null) {
                recordFailedLoginAttempt(username);
                logger.warn("Invalid credentials for user: {}", username);
                return AuthenticationResult.invalidCredentials();
            }
            
            // 重置失败次数
            resetFailedLoginAttempts(username);
            
            // 检查是否需要多因素认证
            if (mfaService.isMfaEnabled(String.valueOf(user.getUserId()))) {
                // 生成临时认证Token
                String tempToken = generateTempAuthToken(user);
                logger.debug("Credentials verified, MFA required for user: {}", username);
                return AuthenticationResult.mfaRequired(tempToken);
            } else {
                // 直接生成最终Token
                String token = jwtTokenProvider.generateToken(user);
                String sessionId = UUID.randomUUID().toString();
                tokenManager.cacheToken(sessionId, token, jwtTokenProvider.getTokenExpiration());
                
                recordSuccessfulLogin(user, clientIp);
                logger.debug("Authentication successful for user: {}", username);
                return AuthenticationResult.success(token, sessionId);
            }
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", username, e);
            return AuthenticationResult.systemError();
        }
    }
    
    /**
     * 第二阶段认证：多因素认证
     */
    public AuthenticationResult completeMfaAuthentication(String tempToken, String mfaCode, String mfaType, String clientIp) {
        try {
            // 验证临时Token
            AuthUser user = validateTempAuthToken(tempToken);
            if (user == null) {
                logger.warn("Invalid or expired temp token");
                return AuthenticationResult.invalidToken();
            }
            
            boolean mfaValid = false;
            switch (mfaType.toLowerCase()) {
                case "totp":
                    mfaValid = mfaService.verifyTotpCode(String.valueOf(user.getUserId()), mfaCode);
                    break;
                case "sms":
                    mfaValid = mfaService.verifySmsCode(String.valueOf(user.getUserId()), mfaCode);
                    break;
                default:
                    logger.warn("Unsupported MFA type: {}", mfaType);
                    return AuthenticationResult.invalidMfaType();
            }
            
            if (!mfaValid) {
                logger.warn("Invalid MFA code for user: {}", user.getUserId());
                return AuthenticationResult.invalidMfaCode();
            }
            
            // MFA验证成功，生成最终Token
            String token = jwtTokenProvider.generateToken(user);
            String sessionId = UUID.randomUUID().toString();
            tokenManager.cacheToken(sessionId, token, jwtTokenProvider.getTokenExpiration());
            
            // 删除临时Token
            invalidateTempAuthToken(tempToken);
            
            recordSuccessfulLogin(user, clientIp);
            logger.debug("MFA authentication successful for user: {}", user.getUserId());
            return AuthenticationResult.success(token, sessionId);
            
        } catch (Exception e) {
            logger.error("MFA authentication failed", e);
            return AuthenticationResult.systemError();
        }
    }
    
    /**
     * 发送MFA验证码（短信）
     */
    public boolean sendMfaCode(String tempToken, String phoneNumber) {
        try {
            AuthUser user = validateTempAuthToken(tempToken);
            if (user == null) {
                logger.warn("Invalid temp token for sending MFA code");
                return false;
            }
            
            return mfaService.sendSmsCode(String.valueOf(user.getUserId()), phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send MFA code", e);
            return false;
        }
    }
    
    /**
     * 用户登出
     */
    public void logout(String token, String sessionId) {
        try {
            // 将Token加入黑名单
            long expireSeconds = jwtTokenProvider.getTokenRemainingTime(token);
            if (expireSeconds > 0) {
                tokenManager.addToBlacklist(token, expireSeconds);
            }
            
            // 删除缓存的Token
            if (sessionId != null) {
                tokenManager.removeCachedToken(sessionId);
            }
            
            logger.debug("User logged out successfully");
        } catch (Exception e) {
            logger.error("Logout failed", e);
        }
    }
    
    /**
     * 验证用户凭据（模拟实现）
     */
    private AuthUser validateUserCredentials(String username, String password) {
        // TODO: 实际实现应该从数据库查询用户信息
        // 这里提供一个简单的模拟实现
        if ("admin".equals(username) && "admin123".equals(password)) {
            return new AuthUser(1L, username, UUID.randomUUID().toString());
        }
        if ("user".equals(username) && "user123".equals(password)) {
            return new AuthUser(2L, username, UUID.randomUUID().toString());
        }
        return null;
    }
    
    /**
     * 生成临时认证Token
     */
    private String generateTempAuthToken(AuthUser user) {
        String tempToken = UUID.randomUUID().toString();
        String key = TEMP_AUTH_PREFIX + tempToken;
        String userInfo = user.getUserId() + ":" + user.getUsername();
        
        redisTemplate.opsForValue().set(key, userInfo, TEMP_AUTH_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return tempToken;
    }
    
    /**
     * 验证临时认证Token
     */
    private AuthUser validateTempAuthToken(String tempToken) {
        String key = TEMP_AUTH_PREFIX + tempToken;
        String userInfo = redisTemplate.opsForValue().get(key);
        
        if (userInfo == null) {
            return null;
        }
        
        String[] parts = userInfo.split(":");
        if (parts.length != 2) {
            return null;
        }
        
        return new AuthUser(Long.parseLong(parts[0]), parts[1], UUID.randomUUID().toString());
    }
    
    /**
     * 使临时认证Token失效
     */
    private void invalidateTempAuthToken(String tempToken) {
        String key = TEMP_AUTH_PREFIX + tempToken;
        redisTemplate.delete(key);
    }
    
    /**
     * 检查账号是否被锁定
     */
    private boolean isAccountLocked(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_LOGIN_ATTEMPTS;
    }
    
    /**
     * 记录失败的登录尝试
     */
    private void recordFailedLoginAttempt(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        String attempts = redisTemplate.opsForValue().get(key);
        
        int attemptCount = attempts == null ? 1 : Integer.parseInt(attempts) + 1;
        
        redisTemplate.opsForValue().set(key, String.valueOf(attemptCount), 
                LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES);
        
        logger.debug("Failed login attempt recorded for user: {} (attempt: {})", username, attemptCount);
    }
    
    /**
     * 重置失败的登录尝试
     */
    private void resetFailedLoginAttempts(String username) {
        String key = LOGIN_ATTEMPT_PREFIX + username;
        redisTemplate.delete(key);
    }
    
    /**
     * 记录成功登录
     */
    private void recordSuccessfulLogin(AuthUser user, String clientIp) {
        // TODO: 记录到审计日志
        logger.info("User logged in successfully: {} from IP: {}", user.getUsername(), clientIp);
    }
    
    /**
     * 认证结果封装类
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final String token;
        private final String sessionId;
        private final boolean mfaRequired;
        private final String tempToken;
        
        private AuthenticationResult(boolean success, String message, String token, String sessionId, 
                                   boolean mfaRequired, String tempToken) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.sessionId = sessionId;
            this.mfaRequired = mfaRequired;
            this.tempToken = tempToken;
        }
        
        public static AuthenticationResult success(String token, String sessionId) {
            return new AuthenticationResult(true, "Authentication successful", token, sessionId, false, null);
        }
        
        public static AuthenticationResult mfaRequired(String tempToken) {
            return new AuthenticationResult(false, "MFA required", null, null, true, tempToken);
        }
        
        public static AuthenticationResult invalidCredentials() {
            return new AuthenticationResult(false, "Invalid credentials", null, null, false, null);
        }
        
        public static AuthenticationResult accountLocked() {
            return new AuthenticationResult(false, "Account locked", null, null, false, null);
        }
        
        public static AuthenticationResult invalidToken() {
            return new AuthenticationResult(false, "Invalid token", null, null, false, null);
        }
        
        public static AuthenticationResult invalidMfaCode() {
            return new AuthenticationResult(false, "Invalid MFA code", null, null, false, null);
        }
        
        public static AuthenticationResult invalidMfaType() {
            return new AuthenticationResult(false, "Invalid MFA type", null, null, false, null);
        }
        
        public static AuthenticationResult systemError() {
            return new AuthenticationResult(false, "System error", null, null, false, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
        public String getSessionId() { return sessionId; }
        public boolean isMfaRequired() { return mfaRequired; }
        public String getTempToken() { return tempToken; }
    }
}