package com.game.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 配置表注解
 * 标记配置类对应的配置文件
 *
 * @author lx
 * @date 2025/06/08
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigTable {
    
    /**
     * 配置文件名
     * 
     * @return 文件名
     */
    String value();
}