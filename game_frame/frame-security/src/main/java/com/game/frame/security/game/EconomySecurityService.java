package com.game.frame.security.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 经济安全服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class EconomySecurityService {
    private static final Logger logger = LoggerFactory.getLogger(EconomySecurityService.class);
    
    private static final String ECONOMY_PREFIX = "security:economy:";
    private static final String TRANSACTION_PREFIX = "security:transaction:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 监控异常交易
     */
    public boolean checkAbnormalTransaction(Long playerId, Transaction transaction) {
        try {
            // Check transaction amount
            if (isAbnormalAmount(transaction)) {
                recordEconomySuspicion(playerId, "ABNORMAL_AMOUNT", 
                                      "Amount: " + transaction.getAmount() + 
                                      ", Type: " + transaction.getType());
                return true;
            }
            
            // Check transaction frequency
            if (isHighFrequencyTrading(playerId, transaction)) {
                recordEconomySuspicion(playerId, "HIGH_FREQUENCY", 
                                      "Type: " + transaction.getType());
                return true;
            }
            
            // Check for circular trading (money laundering)
            if (isCircularTrading(playerId, transaction)) {
                recordEconomySuspicion(playerId, "CIRCULAR_TRADING", 
                                      "Target: " + transaction.getTargetPlayerId());
                return true;
            }
            
            // Check resource generation rate
            if (isAbnormalResourceGeneration(playerId, transaction)) {
                recordEconomySuspicion(playerId, "ABNORMAL_GENERATION", 
                                      "Resource: " + transaction.getResourceType());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking transaction for player {}", playerId, e);
            return false;
        }
    }

    /**
     * 货币流向分析
     */
    public void analyzeCurrencyFlow(Transaction transaction) {
        try {
            String flowKey = ECONOMY_PREFIX + "flow:" + transaction.getType();
            
            // Record transaction in flow analysis
            Map<String, Object> flowData = new HashMap<>();
            flowData.put("amount", transaction.getAmount());
            flowData.put("timestamp", System.currentTimeMillis());
            flowData.put("sourcePlayer", transaction.getSourcePlayerId());
            flowData.put("targetPlayer", transaction.getTargetPlayerId());
            
            redisTemplate.opsForList().rightPush(flowKey, flowData);
            redisTemplate.expire(flowKey, 7, TimeUnit.DAYS);
            
            // Keep only recent transactions (last 1000)
            redisTemplate.opsForList().trim(flowKey, -1000, -1);
            
            // Analyze flow patterns
            analyzeFlowPatterns(transaction.getType());
            
        } catch (Exception e) {
            logger.error("Error analyzing currency flow", e);
        }
    }

    /**
     * 通货膨胀监控
     */
    public void monitorInflation() {
        try {
            // Monitor total currency in system
            String totalCurrencyKey = ECONOMY_PREFIX + "total:currency";
            String dailyGenerationKey = ECONOMY_PREFIX + "daily:generation:" + getCurrentDay();
            
            // Calculate inflation rate
            Long totalCurrency = (Long) redisTemplate.opsForValue().get(totalCurrencyKey);
            Long dailyGeneration = (Long) redisTemplate.opsForValue().get(dailyGenerationKey);
            
            if (totalCurrency != null && dailyGeneration != null && totalCurrency > 0) {
                double inflationRate = (double) dailyGeneration / totalCurrency * 100;
                
                // Alert if inflation rate is too high
                if (inflationRate > 5.0) { // 5% daily inflation threshold
                    logger.warn("HIGH INFLATION DETECTED - Daily rate: {}%, Total: {}, Generated: {}", 
                               inflationRate, totalCurrency, dailyGeneration);
                    
                    recordEconomyAlert("HIGH_INFLATION", 
                                      "Rate: " + inflationRate + "%, Total: " + totalCurrency);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring inflation", e);
        }
    }

    /**
     * 检查价格操控
     */
    public boolean checkPriceManipulation(String itemId, long price, Long playerId) {
        try {
            String priceHistoryKey = ECONOMY_PREFIX + "price:" + itemId;
            
            // Get recent price data
            Object lastPriceObj = redisTemplate.opsForValue().get(priceHistoryKey);
            
            if (lastPriceObj != null) {
                long lastPrice = Long.parseLong(lastPriceObj.toString());
                
                // Check for extreme price changes
                if (lastPrice > 0) {
                    double priceChangeRatio = Math.abs((double)(price - lastPrice) / lastPrice);
                    
                    if (priceChangeRatio > 0.5) { // 50% price change threshold
                        logger.warn("Potential price manipulation - Item: {}, Old: {}, New: {}, Player: {}", 
                                   itemId, lastPrice, price, playerId);
                        
                        recordEconomySuspicion(playerId, "PRICE_MANIPULATION", 
                                              "Item: " + itemId + ", Change: " + (priceChangeRatio * 100) + "%");
                        return true;
                    }
                }
            }
            
            // Update price history
            redisTemplate.opsForValue().set(priceHistoryKey, price, 24, TimeUnit.HOURS);
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking price manipulation", e);
            return false;
        }
    }

    /**
     * 检查资源重复利用
     */
    public boolean checkResourceDuplication(Long playerId, String resourceType, long amount) {
        try {
            String resourceKey = ECONOMY_PREFIX + "resource:" + playerId + ":" + resourceType;
            
            // Get current resource amount
            Object currentAmountObj = redisTemplate.opsForValue().get(resourceKey);
            long currentAmount = currentAmountObj != null ? Long.parseLong(currentAmountObj.toString()) : 0;
            
            // Check for impossible resource increases
            long maxPossibleGeneration = getMaxResourceGeneration(resourceType, 60); // per minute
            
            if (amount > maxPossibleGeneration) {
                logger.warn("Potential resource duplication - Player: {}, Resource: {}, Amount: {}, Max: {}", 
                           playerId, resourceType, amount, maxPossibleGeneration);
                
                recordEconomySuspicion(playerId, "RESOURCE_DUPLICATION", 
                                      "Resource: " + resourceType + ", Amount: " + amount);
                return true;
            }
            
            // Update resource tracking
            redisTemplate.opsForValue().set(resourceKey, currentAmount + amount, 1, TimeUnit.HOURS);
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking resource duplication", e);
            return false;
        }
    }

    // Private helper methods
    private boolean isAbnormalAmount(Transaction transaction) {
        long threshold = getAmountThreshold(transaction.getType());
        return transaction.getAmount() > threshold;
    }

    private boolean isHighFrequencyTrading(Long playerId, Transaction transaction) {
        String key = TRANSACTION_PREFIX + playerId + ":" + transaction.getType();
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        }
        
        int threshold = getFrequencyThreshold(transaction.getType());
        return count > threshold;
    }

    private boolean isCircularTrading(Long playerId, Transaction transaction) {
        if (transaction.getTargetPlayerId() == null) return false;
        
        String key = TRANSACTION_PREFIX + "circular:" + playerId + ":" + transaction.getTargetPlayerId();
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, 300, TimeUnit.SECONDS); // 5 minutes window
        }
        
        return count > 3; // More than 3 back-and-forth trades in 5 minutes
    }

    private boolean isAbnormalResourceGeneration(Long playerId, Transaction transaction) {
        if (!"RESOURCE_GENERATION".equals(transaction.getType())) return false;
        
        String key = ECONOMY_PREFIX + "generation:" + playerId + ":" + transaction.getResourceType();
        Long totalGenerated = redisTemplate.opsForValue().increment(key, transaction.getAmount());
        
        if (totalGenerated == transaction.getAmount()) {
            redisTemplate.expire(key, 3600, TimeUnit.SECONDS); // 1 hour window
        }
        
        long maxHourlyGeneration = getMaxResourceGeneration(transaction.getResourceType(), 3600);
        return totalGenerated > maxHourlyGeneration;
    }

    private void analyzeFlowPatterns(String transactionType) {
        // Placeholder for flow pattern analysis
        // Could implement detection of:
        // - Money laundering patterns
        // - Bot trading patterns
        // - Market manipulation
    }

    private void recordEconomySuspicion(Long playerId, String suspicionType, String details) {
        String key = ECONOMY_PREFIX + "suspicion:" + playerId + ":" + suspicionType;
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }
        
        logger.warn("Economy suspicion - Player: {}, Type: {}, Details: {}, Count: {}", 
                   playerId, suspicionType, details, count);
        
        if (count >= 3) {
            flagPlayerForEconomyInvestigation(playerId, suspicionType, count);
        }
    }

    private void recordEconomyAlert(String alertType, String details) {
        String key = ECONOMY_PREFIX + "alert:" + alertType;
        redisTemplate.opsForValue().set(key, details, 1, TimeUnit.HOURS);
        
        logger.error("ECONOMY ALERT - Type: {}, Details: {}", alertType, details);
    }

    private void flagPlayerForEconomyInvestigation(Long playerId, String reason, Long count) {
        String key = ECONOMY_PREFIX + "flagged:" + playerId;
        String value = reason + ":" + count + ":" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, value, 7, TimeUnit.DAYS);
        
        logger.error("PLAYER FLAGGED FOR ECONOMY INVESTIGATION - Player: {}, Reason: {}, Count: {}", 
                    playerId, reason, count);
    }

    private long getAmountThreshold(String transactionType) {
        switch (transactionType.toUpperCase()) {
            case "TRADE": return 100000;
            case "AUCTION": return 500000;
            case "GIFT": return 10000;
            case "RESOURCE_GENERATION": return 1000;
            default: return 50000;
        }
    }

    private int getFrequencyThreshold(String transactionType) {
        switch (transactionType.toUpperCase()) {
            case "TRADE": return 10;
            case "AUCTION": return 5;
            case "GIFT": return 3;
            default: return 20;
        }
    }

    private long getMaxResourceGeneration(String resourceType, int timeWindowSeconds) {
        // Base generation per second for different resources
        double baseGeneration = switch (resourceType.toUpperCase()) {
            case "GOLD" -> 1.0;
            case "WOOD" -> 5.0;
            case "STONE" -> 3.0;
            case "FOOD" -> 10.0;
            default -> 2.0;
        };
        
        return (long) (baseGeneration * timeWindowSeconds);
    }

    private String getCurrentDay() {
        return String.valueOf(System.currentTimeMillis() / (24 * 60 * 60 * 1000));
    }

    // Inner class for transaction data
    public static class Transaction {
        private Long sourcePlayerId;
        private Long targetPlayerId;
        private String type;
        private long amount;
        private String resourceType;
        private long timestamp;

        public Transaction(Long sourcePlayerId, Long targetPlayerId, String type, 
                          long amount, String resourceType) {
            this.sourcePlayerId = sourcePlayerId;
            this.targetPlayerId = targetPlayerId;
            this.type = type;
            this.amount = amount;
            this.resourceType = resourceType;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters
        public Long getSourcePlayerId() { return sourcePlayerId; }
        public Long getTargetPlayerId() { return targetPlayerId; }
        public String getType() { return type; }
        public long getAmount() { return amount; }
        public String getResourceType() { return resourceType; }
        public long getTimestamp() { return timestamp; }
    }
}