package com.game.frame.data.annotation;

import com.game.frame.data.datasource.DataSourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源切换注解
 * @author lx
 * @date 2025/06/08
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSource {
    
    /**
     * 数据源类型
     * @return 数据源类型
     */
    DataSourceType value() default DataSourceType.MASTER;
}