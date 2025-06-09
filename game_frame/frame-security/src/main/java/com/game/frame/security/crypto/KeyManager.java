package com.game.frame.security.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 密钥管理器
 * @author lx
 * @date 2025/06/08
 */
@Component
public class KeyManager {
    private static final Logger logger = LoggerFactory.getLogger(KeyManager.class);
    
    private static final String KEY_PREFIX = "security:key:";
    private static final SecureRandom secureRandom = new SecureRandom();
    
    // In-memory key cache for performance
    private final Map<String, KeysetHandle> keyCache = new ConcurrentHashMap<>();
    private final Map<String, KeyPair> rsaKeyCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    public void initialize() {
        try {
            // Initialize Google Tink
            AeadConfig.register();
            logger.info("KeyManager initialized successfully");
        } catch (GeneralSecurityException e) {
            logger.error("Failed to initialize KeyManager", e);
            throw new RuntimeException("KeyManager initialization failed", e);
        }
    }

    /**
     * 生成密钥
     */
    public String generateKey(String keyId, KeyType keyType) {
        try {
            switch (keyType) {
                case AES:
                    return generateAESKey(keyId);
                case RSA:
                    return generateRSAKeyPair(keyId);
                case TINK_AEAD:
                    return generateTinkAeadKey(keyId);
                default:
                    throw new IllegalArgumentException("Unsupported key type: " + keyType);
            }
        } catch (Exception e) {
            logger.error("Failed to generate key for ID: {}, type: {}", keyId, keyType, e);
            throw new RuntimeException("Key generation failed", e);
        }
    }

    /**
     * 密钥存储（使用Tink）
     */
    public void storeKey(String keyId, String keyData, KeyType keyType) {
        try {
            String redisKey = KEY_PREFIX + keyType.name().toLowerCase() + ":" + keyId;
            
            // Store in Redis with expiration
            redisTemplate.opsForValue().set(redisKey, keyData, 24, TimeUnit.HOURS);
            
            logger.info("Key stored successfully: {}", keyId);
        } catch (Exception e) {
            logger.error("Failed to store key: {}", keyId, e);
            throw new RuntimeException("Key storage failed", e);
        }
    }

    /**
     * 密钥获取
     */
    public String getKey(String keyId, KeyType keyType) {
        try {
            String redisKey = KEY_PREFIX + keyType.name().toLowerCase() + ":" + keyId;
            String keyData = redisTemplate.opsForValue().get(redisKey);
            
            if (keyData == null) {
                logger.warn("Key not found: {}", keyId);
                return null;
            }
            
            return keyData;
        } catch (Exception e) {
            logger.error("Failed to get key: {}", keyId, e);
            return null;
        }
    }

    /**
     * 密钥轮换
     */
    public String rotateKey(String keyId, KeyType keyType) {
        try {
            // Archive old key
            String oldKey = getKey(keyId, keyType);
            if (oldKey != null) {
                archiveKey(keyId, oldKey, keyType);
            }
            
            // Generate new key
            String newKey = generateKey(keyId, keyType);
            
            logger.info("Key rotated successfully: {}", keyId);
            return newKey;
            
        } catch (Exception e) {
            logger.error("Failed to rotate key: {}", keyId, e);
            throw new RuntimeException("Key rotation failed", e);
        }
    }

    /**
     * 密钥分发
     */
    public void distributeKey(String keyId, KeyType keyType, String[] targetServices) {
        try {
            String keyData = getKey(keyId, keyType);
            if (keyData == null) {
                throw new IllegalArgumentException("Key not found: " + keyId);
            }
            
            for (String service : targetServices) {
                String distributionKey = KEY_PREFIX + "distributed:" + service + ":" + keyId;
                redisTemplate.opsForValue().set(distributionKey, keyData, 1, TimeUnit.HOURS);
                logger.debug("Key distributed to service: {}", service);
            }
            
            logger.info("Key distributed successfully: {} to {} services", keyId, targetServices.length);
            
        } catch (Exception e) {
            logger.error("Failed to distribute key: {}", keyId, e);
            throw new RuntimeException("Key distribution failed", e);
        }
    }

    /**
     * 获取Tink AEAD密钥句柄
     */
    public KeysetHandle getTinkKeysetHandle(String keyId) {
        try {
            // Check cache first
            KeysetHandle handle = keyCache.get(keyId);
            if (handle != null) {
                return handle;
            }
            
            // Try to load from storage
            String keyData = getKey(keyId, KeyType.TINK_AEAD);
            if (keyData != null) {
                // In a real implementation, you would deserialize the keyset
                // For now, generate a new one
                handle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
                keyCache.put(keyId, handle);
                return handle;
            }
            
            // Generate new keyset if not found
            handle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
            keyCache.put(keyId, handle);
            
            // Store for future use (in real implementation, serialize the handle)
            storeKey(keyId, "tink_keyset_placeholder", KeyType.TINK_AEAD);
            
            return handle;
            
        } catch (Exception e) {
            logger.error("Failed to get Tink keyset handle: {}", keyId, e);
            throw new RuntimeException("Failed to get Tink keyset handle", e);
        }
    }

    /**
     * 获取RSA密钥对
     */
    public KeyPair getRSAKeyPair(String keyId) {
        try {
            // Check cache first
            KeyPair keyPair = rsaKeyCache.get(keyId);
            if (keyPair != null) {
                return keyPair;
            }
            
            // Try to load from storage
            String keyData = getKey(keyId, KeyType.RSA);
            if (keyData != null) {
                // In a real implementation, deserialize the key pair
                // For now, generate a new one
                keyPair = generateRSAKeyPairObject();
                rsaKeyCache.put(keyId, keyPair);
                return keyPair;
            }
            
            // Generate new key pair if not found
            keyPair = generateRSAKeyPairObject();
            rsaKeyCache.put(keyId, keyPair);
            
            // Store for future use
            String serializedKeyPair = serializeRSAKeyPair(keyPair);
            storeKey(keyId, serializedKeyPair, KeyType.RSA);
            
            return keyPair;
            
        } catch (Exception e) {
            logger.error("Failed to get RSA key pair: {}", keyId, e);
            throw new RuntimeException("Failed to get RSA key pair", e);
        }
    }

    // Private helper methods
    private String generateAESKey(String keyId) {
        byte[] keyBytes = new byte[32]; // 256-bit key
        secureRandom.nextBytes(keyBytes);
        String keyData = Base64.getEncoder().encodeToString(keyBytes);
        
        storeKey(keyId, keyData, KeyType.AES);
        return keyData;
    }

    private String generateRSAKeyPair(String keyId) throws Exception {
        KeyPair keyPair = generateRSAKeyPairObject();
        String serializedKeyPair = serializeRSAKeyPair(keyPair);
        
        storeKey(keyId, serializedKeyPair, KeyType.RSA);
        rsaKeyCache.put(keyId, keyPair);
        
        return serializedKeyPair;
    }

    private String generateTinkAeadKey(String keyId) throws Exception {
        KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_GCM);
        keyCache.put(keyId, keysetHandle);
        
        // In a real implementation, serialize the keyset handle
        String serializedKeyset = "tink_keyset_" + System.currentTimeMillis();
        storeKey(keyId, serializedKeyset, KeyType.TINK_AEAD);
        
        return serializedKeyset;
    }

    private KeyPair generateRSAKeyPairObject() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048, secureRandom);
        return keyGen.generateKeyPair();
    }

    private String serializeRSAKeyPair(KeyPair keyPair) {
        // Simplified serialization - in production, use proper key serialization
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        return publicKey + ":" + privateKey;
    }

    private void archiveKey(String keyId, String keyData, KeyType keyType) {
        try {
            String archiveKey = KEY_PREFIX + "archive:" + keyType.name().toLowerCase() + ":" + 
                               keyId + ":" + System.currentTimeMillis();
            redisTemplate.opsForValue().set(archiveKey, keyData, 30, TimeUnit.DAYS);
            logger.debug("Key archived: {}", keyId);
        } catch (Exception e) {
            logger.error("Failed to archive key: {}", keyId, e);
        }
    }

    /**
     * 清理过期密钥
     */
    public void cleanupExpiredKeys() {
        try {
            // Clear in-memory caches periodically
            keyCache.clear();
            rsaKeyCache.clear();
            logger.info("Key caches cleared");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired keys", e);
        }
    }

    /**
     * 获取密钥统计信息
     */
    public Map<String, Object> getKeyStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("cachedTinkKeys", keyCache.size());
        stats.put("cachedRSAKeys", rsaKeyCache.size());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    /**
     * 密钥类型枚举
     */
    public enum KeyType {
        AES, RSA, TINK_AEAD
    }
}