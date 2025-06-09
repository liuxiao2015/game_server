package com.game.frame.security.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 审计注解
 * @author lx
 * @date 2025/06/08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    
    /**
     * 操作类型
     */
    String action() default "";
    
    /**
     * 资源标识
     */
    String resource() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean logParameters() default false;
    
    /**
     * 是否记录返回结果
     */
    boolean logResult() default false;
    
    /**
     * 是否记录异常信息
     */
    boolean logException() default true;
}