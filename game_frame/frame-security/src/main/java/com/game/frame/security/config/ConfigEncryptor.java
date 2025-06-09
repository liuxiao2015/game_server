package com.game.frame.security.config;

import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置加密器
 * 负责配置文件的加密、解密、签名验证
 * @author lx
 * @date 2025/06/08
 */
@Component
public class ConfigEncryptor {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigEncryptor.class);
    
    private static final String ENCRYPTED_PREFIX = "ENC(";
    private static final String ENCRYPTED_SUFFIX = ")";
    private static final Pattern ENCRYPTED_PATTERN = Pattern.compile("ENC\\(([^)]+)\\)");
    private static final String SIGNATURE_EXTENSION = ".sig";
    private static final String BACKUP_EXTENSION = ".backup";
    
    @Autowired
    private StringEncryptor stringEncryptor;
    
    private final SecureRandom secureRandom = new SecureRandom();
    
    /**
     * 加密配置文件
     */
    public void encryptConfigFile(File configFile) throws Exception {
        if (!configFile.exists()) {
            throw new FileNotFoundException("Configuration file not found: " + configFile.getAbsolutePath());
        }
        
        logger.info("Encrypting configuration file: {}", configFile.getAbsolutePath());
        
        try {
            // 创建备份
            File backupFile = new File(configFile.getAbsolutePath() + BACKUP_EXTENSION);
            Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            // 读取原始配置
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(configFile.toPath())) {
                properties.load(input);
            }
            
            // 加密敏感配置项
            encryptSensitiveProperties(properties);
            
            // 写入加密后的配置
            try (OutputStream output = Files.newOutputStream(configFile.toPath())) {
                properties.store(output, "Encrypted configuration file");
            }
            
            // 生成配置文件签名
            generateConfigSignature(configFile);
            
            logger.info("Configuration file encrypted successfully: {}", configFile.getAbsolutePath());
            
        } catch (Exception e) {
            logger.error("Failed to encrypt configuration file: {}", configFile.getAbsolutePath(), e);
            throw new RuntimeException("Configuration encryption failed", e);
        }
    }
    
    /**
     * 解密配置文件
     */
    public Properties decryptConfigFile(File configFile) throws Exception {
        if (!configFile.exists()) {
            throw new FileNotFoundException("Configuration file not found: " + configFile.getAbsolutePath());
        }
        
        logger.debug("Decrypting configuration file: {}", configFile.getAbsolutePath());
        
        try {
            // 验证配置文件签名
            if (!verifyConfigSignature(configFile)) {
                throw new SecurityException("Configuration file signature verification failed");
            }
            
            // 读取配置文件
            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(configFile.toPath())) {
                properties.load(input);
            }
            
            // 解密敏感配置项
            decryptSensitiveProperties(properties);
            
            logger.debug("Configuration file decrypted successfully: {}", configFile.getAbsolutePath());
            return properties;
            
        } catch (Exception e) {
            logger.error("Failed to decrypt configuration file: {}", configFile.getAbsolutePath(), e);
            throw new RuntimeException("Configuration decryption failed", e);
        }
    }
    
    /**
     * 加密敏感配置项
     */
    private void encryptSensitiveProperties(Properties properties) {
        String[] sensitiveKeys = {
            "password", "secret", "key", "token", "credential",
            "jdbc.password", "redis.password", "jwt.secret",
            "oauth.client.secret", "encryption.key", "private.key"
        };
        
        for (String key : properties.stringPropertyNames()) {
            String lowerKey = key.toLowerCase();
            boolean isSensitive = false;
            
            // 检查是否是敏感配置
            for (String sensitivePattern : sensitiveKeys) {
                if (lowerKey.contains(sensitivePattern)) {
                    isSensitive = true;
                    break;
                }
            }
            
            if (isSensitive) {
                String value = properties.getProperty(key);
                if (value != null && !isAlreadyEncrypted(value)) {
                    String encryptedValue = encryptValue(value);
                    properties.setProperty(key, encryptedValue);
                    logger.debug("Encrypted sensitive property: {}", key);
                }
            }
        }
    }
    
    /**
     * 解密敏感配置项
     */
    private void decryptSensitiveProperties(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value != null && isAlreadyEncrypted(value)) {
                String decryptedValue = decryptValue(value);
                properties.setProperty(key, decryptedValue);
                logger.debug("Decrypted property: {}", key);
            }
        }
    }
    
    /**
     * 加密单个值
     */
    public String encryptValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        try {
            String encrypted = stringEncryptor.encrypt(value);
            return ENCRYPTED_PREFIX + encrypted + ENCRYPTED_SUFFIX;
        } catch (Exception e) {
            logger.error("Failed to encrypt value", e);
            throw new RuntimeException("Value encryption failed", e);
        }
    }
    
    /**
     * 解密单个值
     */
    public String decryptValue(String encryptedValue) {
        if (!isAlreadyEncrypted(encryptedValue)) {
            return encryptedValue;
        }
        
        try {
            Matcher matcher = ENCRYPTED_PATTERN.matcher(encryptedValue);
            if (matcher.find()) {
                String ciphertext = matcher.group(1);
                return stringEncryptor.decrypt(ciphertext);
            }
            return encryptedValue;
        } catch (Exception e) {
            logger.error("Failed to decrypt value", e);
            throw new RuntimeException("Value decryption failed", e);
        }
    }
    
    /**
     * 检查值是否已加密
     */
    public boolean isAlreadyEncrypted(String value) {
        return value != null && 
               value.startsWith(ENCRYPTED_PREFIX) && 
               value.endsWith(ENCRYPTED_SUFFIX);
    }
    
    /**
     * 生成配置文件签名
     */
    public void generateConfigSignature(File configFile) throws Exception {
        try {
            byte[] fileContent = Files.readAllBytes(configFile.toPath());
            String signature = calculateFileSignature(fileContent);
            
            File signatureFile = new File(configFile.getAbsolutePath() + SIGNATURE_EXTENSION);
            Files.write(signatureFile.toPath(), signature.getBytes(StandardCharsets.UTF_8));
            
            logger.debug("Generated signature for config file: {}", configFile.getName());
            
        } catch (Exception e) {
            logger.error("Failed to generate config signature", e);
            throw new RuntimeException("Signature generation failed", e);
        }
    }
    
    /**
     * 验证配置文件签名
     */
    public boolean verifyConfigSignature(File configFile) {
        try {
            File signatureFile = new File(configFile.getAbsolutePath() + SIGNATURE_EXTENSION);
            if (!signatureFile.exists()) {
                logger.warn("Signature file not found for: {}", configFile.getName());
                return false;
            }
            
            byte[] fileContent = Files.readAllBytes(configFile.toPath());
            String currentSignature = calculateFileSignature(fileContent);
            
            String savedSignature = new String(Files.readAllBytes(signatureFile.toPath()), StandardCharsets.UTF_8);
            
            boolean valid = currentSignature.equals(savedSignature.trim());
            if (!valid) {
                logger.error("Configuration file signature verification failed: {}", configFile.getName());
            }
            
            return valid;
            
        } catch (Exception e) {
            logger.error("Failed to verify config signature", e);
            return false;
        }
    }
    
    /**
     * 计算文件签名
     */
    private String calculateFileSignature(byte[] fileContent) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(fileContent);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * 批量加密配置文件
     */
    public void encryptConfigFiles(String configDirectory) throws Exception {
        Path configDir = Paths.get(configDirectory);
        if (!Files.exists(configDir)) {
            throw new FileNotFoundException("Configuration directory not found: " + configDirectory);
        }
        
        logger.info("Encrypting configuration files in directory: {}", configDirectory);
        
        try {
            Files.walk(configDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".properties") || 
                              path.toString().endsWith(".yml") || 
                              path.toString().endsWith(".yaml"))
                .forEach(path -> {
                    try {
                        encryptConfigFile(path.toFile());
                    } catch (Exception e) {
                        logger.error("Failed to encrypt config file: {}", path, e);
                    }
                });
                
            logger.info("Configuration files encryption completed");
            
        } catch (Exception e) {
            logger.error("Failed to encrypt configuration files in directory", e);
            throw new RuntimeException("Batch encryption failed", e);
        }
    }
    
    /**
     * 恢复配置文件备份
     */
    public void restoreConfigBackup(File configFile) throws Exception {
        File backupFile = new File(configFile.getAbsolutePath() + BACKUP_EXTENSION);
        if (!backupFile.exists()) {
            throw new FileNotFoundException("Backup file not found: " + backupFile.getAbsolutePath());
        }
        
        try {
            Files.copy(backupFile.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Configuration file restored from backup: {}", configFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to restore config backup", e);
            throw new RuntimeException("Backup restoration failed", e);
        }
    }
    
    /**
     * 清理备份文件
     */
    public void cleanupBackupFiles(String configDirectory) {
        try {
            Path configDir = Paths.get(configDirectory);
            Files.walk(configDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(BACKUP_EXTENSION))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.debug("Deleted backup file: {}", path);
                    } catch (Exception e) {
                        logger.warn("Failed to delete backup file: {}", path, e);
                    }
                });
        } catch (Exception e) {
            logger.error("Failed to cleanup backup files", e);
        }
    }
    
    /**
     * 验证配置文件完整性
     */
    public boolean validateConfigIntegrity(String configDirectory) {
        try {
            Path configDir = Paths.get(configDirectory);
            boolean allValid = true;
            
            Files.walk(configDir)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".properties") || 
                              path.toString().endsWith(".yml") || 
                              path.toString().endsWith(".yaml"))
                .forEach(path -> {
                    boolean valid = verifyConfigSignature(path.toFile());
                    if (!valid) {
                        logger.error("Configuration file integrity check failed: {}", path);
                    }
                });
                
            return allValid;
            
        } catch (Exception e) {
            logger.error("Failed to validate config integrity", e);
            return false;
        }
    }
}