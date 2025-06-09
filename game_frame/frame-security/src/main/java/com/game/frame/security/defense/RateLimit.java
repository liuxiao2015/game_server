package com.game.frame.security.defense;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * @author lx
 * @date 2025/06/08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * QPS限制
     */
    int qps() default 100;
    
    /**
     * 突发容量
     */
    int burstCapacity() default 200;
    
    /**
     * 限流键（支持SpEL表达式）
     */
    String key() default "";
    
    /**
     * 限流类型
     */
    LimitType type() default LimitType.IP;
    
    /**
     * 限流时间窗口（秒）
     */
    int window() default 60;

    enum LimitType {
        IP,     // 按IP限流
        USER,   // 按用户限流
        API,    // 按API限流
        CUSTOM  // 自定义限流键
    }
}