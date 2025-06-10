package com.game.frame.security.defense;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流控制注解
 * 
 * 功能说明：
 * - 提供方法级和类级的访问频率限制功能
 * - 支持多种限流算法和策略配置
 * - 集成令牌桶算法，支持突发流量处理
 * - 提供灵活的限流键定义和SpEL表达式支持
 * 
 * 设计思路：
 * - 基于AOP切面编程实现无侵入式限流
 * - 支持多维度限流：IP、用户、API、自定义键
 * - 令牌桶算法平衡流量控制和用户体验
 * - 时间窗口机制确保限流的准确性
 * 
 * 限流策略：
 * - QPS限制：每秒允许的请求数量
 * - 突发容量：短时间内允许的最大请求数
 * - 时间窗口：限流统计的时间范围
 * - 多级限流：支持全局和局部限流组合
 * 
 * 核心特性：
 * - 分布式限流：支持Redis集群部署
 * - 动态配置：支持运行时调整限流参数
 * - 监控统计：提供详细的限流数据和报表
 * - 异常处理：限流触发时的优雅降级
 * 
 * 使用场景：
 * - API接口的访问频率控制
 * - 用户操作的防刷限制
 * - 系统资源的保护机制
 * - 恶意攻击的防护措施
 * 
 * 限流算法：
 * - 令牌桶（Token Bucket）：支持突发流量
 * - 滑动窗口（Sliding Window）：精确控制
 * - 固定窗口（Fixed Window）：简单高效
 * - 漏桶（Leaky Bucket）：平滑限流
 * 
 * 使用示例：
 * <pre>
 * // 基于IP的限流，每秒最多10个请求
 * &#64;RateLimit(qps = 10, type = LimitType.IP)
 * public Result getUserInfo() { ... }
 * 
 * // 基于用户的限流，支持突发流量
 * &#64;RateLimit(qps = 5, burstCapacity = 10, type = LimitType.USER)
 * public Result updateUserData() { ... }
 * 
 * // 自定义限流键，使用SpEL表达式
 * &#64;RateLimit(qps = 20, key = "#request.gameId", type = LimitType.CUSTOM)
 * public Result joinGame(GameRequest request) { ... }
 * </pre>
 * 
 * 配置参数说明：
 * - qps：每秒查询率限制，默认100
 * - burstCapacity：突发容量，允许短时间超过qps
 * - key：限流键，支持SpEL表达式
 * - type：限流类型，支持IP/USER/API/CUSTOM
 * - window：时间窗口大小，单位：秒
 * 
 * 注意事项：
 * - 合理设置QPS避免影响正常用户体验
 * - 突发容量应适当大于QPS以应对短时峰值
 * - 自定义键要确保唯一性和稳定性
 * - 分布式环境下注意时钟同步问题
 * 
 * 性能考虑：
 * - 限流检查的响应时间应控制在毫秒级
 * - Redis操作的网络延迟影响
 * - 内存缓存的命中率优化
 * - 限流数据的过期清理机制
 *
 * @author lx
 * @date 2025/06/08
 * @since 1.0.0
 * @see com.game.frame.security.defense.RateLimitAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * QPS（每秒查询率）限制
     * 
     * 定义每秒允许的最大请求数量，用于：
     * - 基础流量控制和系统保护
     * - 防止单用户或IP的过度访问
     * - 确保系统资源的合理分配
     * - 避免恶意攻击导致的服务不可用
     * 
     * 设置建议：
     * - 根据系统处理能力和用户规模确定
     * - 考虑业务特性和用户行为模式
     * - 预留一定的系统余量和缓冲空间
     * - 区分不同接口的重要性和资源消耗
     * 
     * @return QPS限制值，默认100
     */
    int qps() default 100;
    
    /**
     * 突发容量限制
     * 
     * 允许短时间内超过QPS的最大请求数，实现：
     * - 令牌桶算法的突发流量支持
     * - 应对用户的正常突发操作
     * - 提升用户体验和系统灵活性
     * - 平衡流量控制和业务需求
     * 
     * 设计原理：
     * - 令牌桶初始容量为burstCapacity
     * - 以qps速率向桶中添加令牌
     * - 请求消耗令牌，无令牌时触发限流
     * - 允许短时间的高频操作
     * 
     * @return 突发容量，默认200（是QPS的2倍）
     */
    int burstCapacity() default 200;
    
    /**
     * 限流键定义（支持SpEL表达式）
     * 
     * 用于确定限流的粒度和范围，支持：
     * - 静态字符串：固定的限流标识
     * - SpEL表达式：动态计算限流键
     * - 方法参数：#paramName 访问参数值
     * - 对象属性：#param.property 访问属性
     * - 复合表达式：支持字符串拼接和运算
     * 
     * 常用表达式示例：
     * - "#request.userId"：基于用户ID限流
     * - "#request.gameId + ':' + #request.action"：游戏+操作组合
     * - "T(com.game.util.IpUtils).getClientIp(#request)"：获取客户端IP
     * 
     * 注意事项：
     * - 键值要保证稳定性和唯一性
     * - 避免包含敏感信息
     * - 考虑键的长度和Redis存储效率
     * 
     * @return 限流键表达式，空字符串表示使用默认键
     */
    String key() default "";
    
    /**
     * 限流类型定义
     * 
     * 指定限流的维度和策略，包括：
     * - IP：基于客户端IP地址限流
     * - USER：基于用户ID或会话限流  
     * - API：基于接口方法限流
     * - CUSTOM：基于自定义键限流
     * 
     * 不同类型的特点：
     * - IP限流：防止单IP恶意攻击，但可能误伤共享IP用户
     * - USER限流：精确控制单用户行为，需要用户认证支持
     * - API限流：保护特定接口，控制整体访问量
     * - CUSTOM限流：最灵活，支持复杂的业务逻辑
     * 
     * @return 限流类型，默认按IP限流
     */
    LimitType type() default LimitType.IP;
    
    /**
     * 限流时间窗口（秒）
     * 
     * 定义限流统计的时间范围，影响：
     * - 限流精度和响应速度
     * - 内存占用和计算开销
     * - 限流数据的有效期
     * - 系统恢复的时间
     * 
     * 窗口大小选择：
     * - 小窗口（5-30秒）：精确控制，快速响应
     * - 中窗口（1-5分钟）：平衡精度和性能
     * - 大窗口（5-60分钟）：长期控制，防止持续攻击
     * 
     * @return 时间窗口大小，单位：秒，默认60秒
     */
    int window() default 60;

    /**
     * 限流类型枚举
     * 
     * 定义支持的限流维度和策略类型
     */
    enum LimitType {
        /**
         * 基于IP地址的限流
         * - 适用于防止单个IP的恶意攻击
         * - 简单有效，无需用户认证
         * - 可能影响NAT环境下的多用户访问
         */
        IP,
        
        /**
         * 基于用户ID的限流
         * - 精确控制单个用户的访问频率
         * - 需要用户登录和认证支持
         * - 适用于用户行为控制和防刷
         */
        USER,
        
        /**
         * 基于API接口的限流
         * - 控制特定接口的总访问量
         * - 保护系统资源和核心功能
         * - 适用于热点接口和资源密集型操作
         */
        API,
        
        /**
         * 基于自定义键的限流
         * - 最灵活的限流方式
         * - 支持复杂的业务逻辑和多维度组合
         * - 需要合理设计限流键的生成策略
         */
        CUSTOM
    }
}