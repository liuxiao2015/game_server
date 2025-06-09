package com.game.frame.security.rbac;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解
 * @author lx
 * @date 2025/06/08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    
    /**
     * 权限代码
     */
    String value();
    
    /**
     * 多个权限的逻辑关系
     */
    Logical logical() default Logical.AND;
    
    /**
     * 是否可选（如果用户没有权限是否抛出异常）
     */
    boolean optional() default false;

    enum Logical {
        AND, OR
    }
}