package com.game.common.config;

/**
 * 表配置基类
 * 提供ID索引、数据验证、关联检查等基础功能
 *
 * @author lx
 * @date 2025/06/08
 */
public abstract class TableConfig {
    
    /**
     * 获取配置ID
     * 
     * @return 配置ID
     */
    public abstract int getId();
    
    /**
     * 验证配置数据
     * 
     * @return true if valid
     */
    public boolean validate() {
        return getId() > 0;
    }
}