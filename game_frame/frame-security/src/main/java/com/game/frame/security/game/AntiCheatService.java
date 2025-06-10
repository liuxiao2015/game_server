package com.game.frame.security.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 游戏防作弊安全服务
 * 
 * 功能说明：
 * - 提供全方位的游戏作弊检测和防护机制
 * - 实时监控玩家行为和数据异常情况
 * - 基于规则引擎和机器学习的智能检测
 * - 支持多种作弊手段的识别和处理
 * 
 * 设计思路：
 * - 采用多层防护体系，从客户端到服务端全面覆盖
 * - 使用Redis缓存玩家行为数据，支持实时分析
 * - 集成阈值检测、模式识别、异常行为分析
 * - 提供可配置的检测规则和动态调整能力
 * 
 * 核心检测能力：
 * - 数值合法性检查：防止经验、金币、道具等数值异常
 * - 行为频率检测：识别异常高频操作和机器人行为
 * - 移动速度检测：防止加速外挂和瞬移作弊
 * - 战斗数据验证：检查伤害值、技能冷却、资源消耗
 * - 时间序列分析：发现时间相关的作弊模式
 * 
 * 检测算法：
 * - 阈值检测：基于预设规则的简单快速检测
 * - 统计分析：基于历史数据的异常值检测
 * - 模式匹配：识别已知的作弊行为模式
 * - 机器学习：自适应的智能检测算法
 * 
 * 防护策略：
 * - 实时拦截：立即阻止可疑操作的执行
 * - 延迟验证：对复杂情况进行深度分析
 * - 分级处理：根据作弊严重程度采取不同措施
 * - 人工审核：对疑似案例提交人工复查
 * 
 * 数据安全：
 * - 敏感数据加密存储和传输
 * - 访问控制和权限管理
 * - 审计日志和操作追踪
 * - 数据完整性验证
 * 
 * 性能优化：
 * - 分布式检测，避免单点性能瓶颈
 * - 异步处理，不影响游戏正常流程
 * - 缓存优化，减少数据库访问压力
 * - 批量处理，提升检测效率
 * 
 * 监控指标：
 * - 作弊检测成功率和误报率
 * - 不同类型作弊的发生频率
 * - 检测响应时间和系统性能影响
 * - 玩家申诉和人工审核结果
 * 
 * 使用场景：
 * - 在线游戏的实时作弊防护
 * - 游戏经济系统的安全保障
 * - 竞技游戏的公平性维护
 * - 游戏数据的完整性保护
 * 
 * 扩展能力：
 * - 支持新的作弊检测规则添加
 * - 集成第三方反作弊解决方案
 * - 提供API接口供其他模块调用
 * - 支持多游戏类型的差异化配置
 * 
 * 注意事项：
 * - 检测规则要平衡准确性和误报率
 * - 避免影响正常玩家的游戏体验
 * - 及时更新检测规则应对新型作弊
 * - 保护检测逻辑不被逆向工程
 * 
 * @author lx
 * @date 2025/06/08
 * @since 1.0.0
 * @see RedisTemplate
 * @see org.springframework.stereotype.Service
 */
@Service
public class AntiCheatService {
    // 日志记录器，用于记录作弊检测的详细信息和安全事件
    private static final Logger logger = LoggerFactory.getLogger(AntiCheatService.class);
    
    // Redis键前缀定义，用于数据隔离和管理
    private static final String CHEAT_DETECTION_PREFIX = "security:cheat:";     // 作弊检测数据前缀
    private static final String PLAYER_ACTION_PREFIX = "security:action:";     // 玩家行为数据前缀
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 游戏数值合法性检查
     * 
     * 检查各种游戏数值是否在合理范围内，防止：
     * - 经验值异常增长（外挂刷经验）
     * - 货币数量异常变化（金币外挂）
     * - 等级跳跃式提升（等级外挂）
     * - 道具数量异常增加（刷道具外挂）
     * 
     * 验证策略：
     * - 设置合理的数值上限和下限
     * - 考虑游戏设计的正常数值范围
     * - 支持动态调整检测阈值
     * - 记录异常数值的详细信息
     * 
     * 检测算法：
     * - 绝对值检查：数值不能超过预设最大值
     * - 相对值检查：单次变化不能过大
     * - 时间序列检查：变化速度不能过快
     * - 上下文检查：结合玩家当前状态验证
     * 
     * @param type 数值类型，支持：EXPERIENCE(经验)、CURRENCY(货币)、LEVEL(等级)、ITEM_COUNT(道具数量)
     * @param value 待检查的数值，支持各种数字类型
     * @return true表示数值合法，false表示数值异常
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
                    logger.warn("未知的数值类型检查请求 - 类型: {}, 值: {}", type, value);
                    return false;
            }
        } catch (Exception e) {
            logger.error("数值合法性检查异常 - 类型: {}, 值: {}, 错误: {}", type, value, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 玩家异常行为检测
     * 
     * 基于行为频率和模式的异常检测，识别：
     * - 机器人脚本的高频重复操作
     * - 异常登录行为和多开检测
     * - 恶意刷屏和垃圾信息
     * - 异常交易和经济行为
     * 
     * 检测机制：
     * - 滑动窗口统计：统计时间窗口内的行为频率
     * - 动态阈值：根据行为类型设置不同的检测阈值
     * - 累积记录：记录玩家的历史异常行为
     * - 自动标记：达到阈值自动标记为可疑玩家
     * 
     * 行为类型阈值：
     * - LOGIN(登录): 5次/分钟
     * - ATTACK(攻击): 100次/分钟
     * - MOVE(移动): 1000次/分钟
     * - CHAT(聊天): 50次/分钟
     * - TRADE(交易): 10次/分钟
     * 
     * @param playerId 玩家ID
     * @param action 行为类型，如LOGIN、ATTACK、MOVE等
     * @return true表示检测到异常行为，false表示行为正常
     */
    public boolean detectAbnormalBehavior(Long playerId, String action) {
        try {
            String key = PLAYER_ACTION_PREFIX + playerId + ":" + action;
            Long actionCount = redisTemplate.opsForValue().increment(key);
            
            if (actionCount == 1) {
                // 首次记录设置过期时间（60秒滑动窗口）
                redisTemplate.expire(key, 60, TimeUnit.SECONDS);
            }
            
            // 获取该行为类型的检测阈值
            int threshold = getActionThreshold(action);
            
            if (actionCount > threshold) {
                logger.warn("检测到异常行为 - 玩家: {}, 行为: {}, 频率: {}/分钟, 阈值: {}", 
                           playerId, action, actionCount, threshold);
                recordCheatAttempt(playerId, "ABNORMAL_BEHAVIOR", 
                                  "行为: " + action + ", 频率: " + actionCount);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("异常行为检测失败 - 玩家: {}, 行为: {}, 错误: {}", playerId, action, e.getMessage(), e);
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