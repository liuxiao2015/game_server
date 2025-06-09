package com.game.frame.security.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 防作弊服务
 * @author lx
 * @date 2025/06/08
 */
@Service
public class AntiCheatService {
    private static final Logger logger = LoggerFactory.getLogger(AntiCheatService.class);
    
    private static final String CHEAT_DETECTION_PREFIX = "security:cheat:";
    private static final String PLAYER_ACTION_PREFIX = "security:action:";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 数值合法性检查
     */
    public boolean checkValueLegality(String type, Object value) {
        try {
            switch (type.toUpperCase()) {
                case "EXPERIENCE":
                    return checkExperienceValue(value);
                case "CURRENCY":
                    return checkCurrencyValue(value);
                case "LEVEL":
                    return checkLevelValue(value);
                case "ITEM_COUNT":
                    return checkItemCountValue(value);
                default:
                    logger.warn("Unknown value type for legality check: {}", type);
                    return false;
            }
        } catch (Exception e) {
            logger.error("Error checking value legality for type {}, value {}", type, value, e);
            return false;
        }
    }

    /**
     * 行为异常检测
     */
    public boolean detectAbnormalBehavior(Long playerId, String action) {
        try {
            String key = PLAYER_ACTION_PREFIX + playerId + ":" + action;
            Long actionCount = redisTemplate.opsForValue().increment(key);
            
            if (actionCount == 1) {
                // Set expiration for the first increment
                redisTemplate.expire(key, 60, TimeUnit.SECONDS);
            }
            
            // Define thresholds for different actions
            int threshold = getActionThreshold(action);
            
            if (actionCount > threshold) {
                logger.warn("Abnormal behavior detected - Player: {}, Action: {}, Count: {}", 
                           playerId, action, actionCount);
                recordCheatAttempt(playerId, "ABNORMAL_BEHAVIOR", 
                                  "Action: " + action + ", Count: " + actionCount);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error detecting abnormal behavior for player {}, action {}", playerId, action, e);
            return false;
        }
    }

    /**
     * 速度检测
     */
    public boolean checkSpeedHack(Long playerId, Movement movement) {
        try {
            String key = PLAYER_ACTION_PREFIX + playerId + ":position";
            
            // Get last position and timestamp
            String lastDataStr = (String) redisTemplate.opsForValue().get(key);
            
            if (lastDataStr != null) {
                String[] parts = lastDataStr.split(",");
                if (parts.length == 4) {
                    double lastX = Double.parseDouble(parts[0]);
                    double lastY = Double.parseDouble(parts[1]);
                    double lastZ = Double.parseDouble(parts[2]);
                    long lastTime = Long.parseLong(parts[3]);
                    
                    // Calculate distance and time
                    double distance = calculateDistance(lastX, lastY, lastZ, 
                                                      movement.getX(), movement.getY(), movement.getZ());
                    long timeDiff = movement.getTimestamp() - lastTime;
                    
                    if (timeDiff > 0) {
                        double speed = distance / (timeDiff / 1000.0); // units per second
                        
                        // Check against maximum allowed speed
                        double maxSpeed = getMaxAllowedSpeed(movement.getMovementType());
                        
                        if (speed > maxSpeed) {
                            logger.warn("Speed hack detected - Player: {}, Speed: {}, Max: {}", 
                                       playerId, speed, maxSpeed);
                            recordCheatAttempt(playerId, "SPEED_HACK", 
                                              "Speed: " + speed + ", Max: " + maxSpeed);
                            return true;
                        }
                    }
                }
            }
            
            // Update current position
            String currentData = movement.getX() + "," + movement.getY() + "," + 
                               movement.getZ() + "," + movement.getTimestamp();
            redisTemplate.opsForValue().set(key, currentData, 300, TimeUnit.SECONDS);
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking speed hack for player {}", playerId, e);
            return false;
        }
    }

    /**
     * 检查战斗数据合法性
     */
    public boolean checkBattleDataLegality(Long playerId, BattleData battleData) {
        try {
            // Check damage values
            if (battleData.getDamage() < 0 || battleData.getDamage() > getMaxDamage(playerId)) {
                recordCheatAttempt(playerId, "INVALID_DAMAGE", 
                                  "Damage: " + battleData.getDamage());
                return false;
            }
            
            // Check skill cooldown
            if (!checkSkillCooldown(playerId, battleData.getSkillId())) {
                recordCheatAttempt(playerId, "SKILL_COOLDOWN_VIOLATION", 
                                  "Skill: " + battleData.getSkillId());
                return false;
            }
            
            // Check resource consumption
            if (!checkResourceConsumption(playerId, battleData)) {
                recordCheatAttempt(playerId, "RESOURCE_CHEAT", 
                                  "Invalid resource consumption");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error checking battle data legality for player {}", playerId, e);
            return false;
        }
    }

    /**
     * 记录作弊尝试
     */
    private void recordCheatAttempt(Long playerId, String cheatType, String details) {
        try {
            String key = CHEAT_DETECTION_PREFIX + playerId + ":" + cheatType;
            Long attempts = redisTemplate.opsForValue().increment(key);
            
            if (attempts == 1) {
                redisTemplate.expire(key, 24, TimeUnit.HOURS);
            }
            
            // Log the attempt
            logger.warn("Cheat attempt recorded - Player: {}, Type: {}, Details: {}, Total: {}", 
                       playerId, cheatType, details, attempts);
            
            // If too many attempts, flag for investigation
            if (attempts >= 5) {
                flagPlayerForInvestigation(playerId, cheatType, attempts);
            }
            
        } catch (Exception e) {
            logger.error("Failed to record cheat attempt for player {}", playerId, e);
        }
    }

    /**
     * 标记玩家需要调查
     */
    private void flagPlayerForInvestigation(Long playerId, String cheatType, Long attempts) {
        String key = CHEAT_DETECTION_PREFIX + "flagged:" + playerId;
        String value = cheatType + ":" + attempts + ":" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, value, 7, TimeUnit.DAYS);
        
        logger.error("PLAYER FLAGGED FOR INVESTIGATION - Player: {}, Type: {}, Attempts: {}", 
                    playerId, cheatType, attempts);
    }

    // Helper methods
    private boolean checkExperienceValue(Object value) {
        if (!(value instanceof Number)) return false;
        long exp = ((Number) value).longValue();
        return exp >= 0 && exp <= 1000000; // Max 1M exp gain at once
    }

    private boolean checkCurrencyValue(Object value) {
        if (!(value instanceof Number)) return false;
        long currency = ((Number) value).longValue();
        return currency >= 0 && currency <= 100000; // Max 100K currency gain at once
    }

    private boolean checkLevelValue(Object value) {
        if (!(value instanceof Number)) return false;
        int level = ((Number) value).intValue();
        return level >= 1 && level <= 100; // Level range 1-100
    }

    private boolean checkItemCountValue(Object value) {
        if (!(value instanceof Number)) return false;
        int count = ((Number) value).intValue();
        return count >= 0 && count <= 999; // Max stack size
    }

    private int getActionThreshold(String action) {
        switch (action.toUpperCase()) {
            case "LOGIN": return 5;
            case "ATTACK": return 100;
            case "MOVE": return 1000;
            case "CHAT": return 50;
            case "TRADE": return 10;
            default: return 20;
        }
    }

    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    private double getMaxAllowedSpeed(String movementType) {
        switch (movementType.toUpperCase()) {
            case "WALK": return 5.0;
            case "RUN": return 15.0;
            case "MOUNT": return 30.0;
            case "FLY": return 50.0;
            default: return 10.0;
        }
    }

    private long getMaxDamage(Long playerId) {
        // This would typically check player's stats/equipment
        return 10000; // Placeholder max damage
    }

    private boolean checkSkillCooldown(Long playerId, String skillId) {
        String key = PLAYER_ACTION_PREFIX + playerId + ":skill:" + skillId;
        return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private boolean checkResourceConsumption(Long playerId, BattleData battleData) {
        // Placeholder - would check mana/stamina consumption
        return true;
    }

    // Inner classes for data structures
    public static class Movement {
        private double x, y, z;
        private long timestamp;
        private String movementType;

        // Constructors, getters, setters
        public Movement(double x, double y, double z, long timestamp, String movementType) {
            this.x = x; this.y = y; this.z = z;
            this.timestamp = timestamp;
            this.movementType = movementType;
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getZ() { return z; }
        public long getTimestamp() { return timestamp; }
        public String getMovementType() { return movementType; }
    }

    public static class BattleData {
        private long damage;
        private String skillId;
        private long timestamp;

        public BattleData(long damage, String skillId, long timestamp) {
            this.damage = damage;
            this.skillId = skillId;
            this.timestamp = timestamp;
        }

        public long getDamage() { return damage; }
        public String getSkillId() { return skillId; }
        public long getTimestamp() { return timestamp; }
    }
}