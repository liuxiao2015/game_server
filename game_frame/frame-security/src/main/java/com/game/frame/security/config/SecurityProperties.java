package com.game.frame.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 安全配置属性
 * @author lx
 * @date 2025/06/08
 */
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    
    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();
    
    /**
     * 加密配置
     */
    private Crypto crypto = new Crypto();
    
    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();
    
    /**
     * 防重放配置
     */
    private AntiReplay antiReplay = new AntiReplay();
    
    /**
     * 审计配置
     */
    private Audit audit = new Audit();

    public static class Jwt {
        private String secret = "defaultSecretKey32CharactersLong";
        private long expiration = 7200; // 2 hours
        private long refreshWindow = 1800; // 30 minutes
        
        // getters and setters
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpiration() { return expiration; }
        public void setExpiration(long expiration) { this.expiration = expiration; }
        public long getRefreshWindow() { return refreshWindow; }
        public void setRefreshWindow(long refreshWindow) { this.refreshWindow = refreshWindow; }
    }

    public static class Crypto {
        private String algorithm = "AES/GCM/NoPadding";
        private int keySize = 256;
        
        // getters and setters
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public int getKeySize() { return keySize; }
        public void setKeySize(int keySize) { this.keySize = keySize; }
    }

    public static class RateLimit {
        private int defaultQps = 100;
        private int burstCapacity = 200;
        
        // getters and setters
        public int getDefaultQps() { return defaultQps; }
        public void setDefaultQps(int defaultQps) { this.defaultQps = defaultQps; }
        public int getBurstCapacity() { return burstCapacity; }
        public void setBurstCapacity(int burstCapacity) { this.burstCapacity = burstCapacity; }
    }

    public static class AntiReplay {
        private int windowSize = 300; // 5 minutes
        
        // getters and setters
        public int getWindowSize() { return windowSize; }
        public void setWindowSize(int windowSize) { this.windowSize = windowSize; }
    }

    public static class Audit {
        private boolean enabled = true;
        private boolean async = true;
        private int batchSize = 100;
        
        // getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isAsync() { return async; }
        public void setAsync(boolean async) { this.async = async; }
        public int getBatchSize() { return batchSize; }
        public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    }

    // Main getters and setters
    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public Crypto getCrypto() { return crypto; }
    public void setCrypto(Crypto crypto) { this.crypto = crypto; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    public AntiReplay getAntiReplay() { return antiReplay; }
    public void setAntiReplay(AntiReplay antiReplay) { this.antiReplay = antiReplay; }
    public Audit getAudit() { return audit; }
    public void setAudit(Audit audit) { this.audit = audit; }
}