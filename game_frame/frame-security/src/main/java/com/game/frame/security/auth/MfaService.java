package com.game.frame.security.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * 多因素认证服务
 * 支持 TOTP（时间型一次性密码）、短信验证码、设备指纹
 * @author lx
 * @date 2025/06/08
 */
@Service
public class MfaService {
    
    private static final Logger logger = LoggerFactory.getLogger(MfaService.class);
    
    private static final String MFA_CODE_PREFIX = "security:mfa:code:";
    private static final String MFA_SECRET_PREFIX = "security:mfa:secret:";
    private static final String DEVICE_FINGERPRINT_PREFIX = "security:device:";
    
    private static final long TOTP_TIME_STEP = 30; // 30 seconds
    private static final int TOTP_DIGITS = 6;
    private static final int SMS_CODE_LENGTH = 6;
    private static final int SMS_CODE_EXPIRE_MINUTES = 5;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 生成用户的 TOTP 密钥
     */
    public String generateTotpSecret(String userId) {
        try {
            byte[] secret = new byte[20]; // 160 bits
            secureRandom.nextBytes(secret);
            String secretBase32 = Base64.getEncoder().encodeToString(secret);
            
            // 存储到 Redis
            String key = MFA_SECRET_PREFIX + userId;
            redisTemplate.opsForValue().set(key, secretBase32);
            
            logger.debug("Generated TOTP secret for user: {}", userId);
            return secretBase32;
        } catch (Exception e) {
            logger.error("Failed to generate TOTP secret for user: {}", userId, e);
            throw new RuntimeException("Failed to generate TOTP secret", e);
        }
    }
    
    /**
     * 验证 TOTP 代码
     */
    public boolean verifyTotpCode(String userId, String code) {
        try {
            String key = MFA_SECRET_PREFIX + userId;
            String secret = redisTemplate.opsForValue().get(key);
            
            if (secret == null) {
                logger.warn("No TOTP secret found for user: {}", userId);
                return false;
            }
            
            long currentTime = System.currentTimeMillis() / 1000L;
            long timeWindow = currentTime / TOTP_TIME_STEP;
            
            // 检查当前时间窗口和前后各一个时间窗口
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTotpCode(secret, timeWindow + i);
                if (code.equals(expectedCode)) {
                    logger.debug("TOTP code verified successfully for user: {}", userId);
                    return true;
                }
            }
            
            logger.warn("Invalid TOTP code for user: {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("Failed to verify TOTP code for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 生成 TOTP 代码
     */
    private String generateTotpCode(String secret, long timeCounter) throws Exception {
        byte[] secretBytes = Base64.getDecoder().decode(secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeCounter).array();
        
        SecretKeySpec secretKey = new SecretKeySpec(secretBytes, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        
        byte[] hash = mac.doFinal(data);
        int offset = hash[hash.length - 1] & 0xF;
        
        int code = ((hash[offset] & 0x7F) << 24) |
                   ((hash[offset + 1] & 0xFF) << 16) |
                   ((hash[offset + 2] & 0xFF) << 8) |
                   (hash[offset + 3] & 0xFF);
        
        code = code % (int) Math.pow(10, TOTP_DIGITS);
        return String.format("%0" + TOTP_DIGITS + "d", code);
    }
    
    /**
     * 发送短信验证码
     */
    public boolean sendSmsCode(String userId, String phoneNumber) {
        try {
            String code = generateSmsCode();
            String key = MFA_CODE_PREFIX + userId;
            
            // 存储验证码，设置过期时间
            redisTemplate.opsForValue().set(key, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            
            // TODO: 集成短信服务商发送短信
            // smsService.sendCode(phoneNumber, code);
            
            logger.debug("SMS code sent to user: {} at phone: {}", userId, phoneNumber);
            logger.debug("SMS code for testing: {}", code); // 仅测试环境
            
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS code to user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 验证短信验证码
     */
    public boolean verifySmsCode(String userId, String code) {
        try {
            String key = MFA_CODE_PREFIX + userId;
            String storedCode = redisTemplate.opsForValue().get(key);
            
            if (storedCode == null) {
                logger.warn("No SMS code found for user: {}", userId);
                return false;
            }
            
            if (storedCode.equals(code)) {
                // 验证成功后删除验证码
                redisTemplate.delete(key);
                logger.debug("SMS code verified successfully for user: {}", userId);
                return true;
            }
            
            logger.warn("Invalid SMS code for user: {}", userId);
            return false;
        } catch (Exception e) {
            logger.error("Failed to verify SMS code for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 生成短信验证码
     */
    private String generateSmsCode() {
        int code = secureRandom.nextInt((int) Math.pow(10, SMS_CODE_LENGTH));
        return String.format("%0" + SMS_CODE_LENGTH + "d", code);
    }
    
    /**
     * 注册设备指纹
     */
    public String registerDeviceFingerprint(String userId, String deviceInfo) {
        try {
            String fingerprint = generateDeviceFingerprint(deviceInfo);
            String key = DEVICE_FINGERPRINT_PREFIX + userId;
            
            // 存储设备指纹
            redisTemplate.opsForSet().add(key, fingerprint);
            
            logger.debug("Device fingerprint registered for user: {}", userId);
            return fingerprint;
        } catch (Exception e) {
            logger.error("Failed to register device fingerprint for user: {}", userId, e);
            throw new RuntimeException("Failed to register device fingerprint", e);
        }
    }
    
    /**
     * 验证设备指纹
     */
    public boolean verifyDeviceFingerprint(String userId, String deviceInfo) {
        try {
            String fingerprint = generateDeviceFingerprint(deviceInfo);
            String key = DEVICE_FINGERPRINT_PREFIX + userId;
            
            Boolean isMember = redisTemplate.opsForSet().isMember(key, fingerprint);
            
            if (Boolean.TRUE.equals(isMember)) {
                logger.debug("Device fingerprint verified successfully for user: {}", userId);
                return true;
            } else {
                logger.warn("Unknown device fingerprint for user: {}", userId);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to verify device fingerprint for user: {}", userId, e);
            return false;
        }
    }
    
    /**
     * 生成设备指纹
     */
    private String generateDeviceFingerprint(String deviceInfo) {
        try {
            // 简单的设备指纹生成，实际应用中可以使用更复杂的算法
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(deviceInfo.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate device fingerprint", e);
        }
    }
    
    /**
     * 检查用户是否启用了多因素认证
     */
    public boolean isMfaEnabled(String userId) {
        String key = MFA_SECRET_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * 禁用用户的多因素认证
     */
    public void disableMfa(String userId) {
        String secretKey = MFA_SECRET_PREFIX + userId;
        String codeKey = MFA_CODE_PREFIX + userId;
        String deviceKey = DEVICE_FINGERPRINT_PREFIX + userId;
        
        redisTemplate.delete(secretKey);
        redisTemplate.delete(codeKey);
        redisTemplate.delete(deviceKey);
        
        logger.debug("MFA disabled for user: {}", userId);
    }
}