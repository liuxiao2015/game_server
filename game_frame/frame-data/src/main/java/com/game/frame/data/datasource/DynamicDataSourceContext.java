package com.game.frame.data.datasource;

/**
 * 动态数据源上下文
 * @author lx
 * @date 2025/06/08
 */
public class DynamicDataSourceContext {

    private static final ThreadLocal<DataSourceType> contextHolder = new ThreadLocal<>();

    /**
     * 设置数据源类型
     * @param dataSourceType 数据源类型
     */
    public static void setDataSourceType(DataSourceType dataSourceType) {
        contextHolder.set(dataSourceType);
    }

    /**
     * 获取当前数据源类型
     * @return 数据源类型
     */
    public static DataSourceType getDataSourceType() {
        return contextHolder.get();
    }

    /**
     * 清除数据源类型
     */
    public static void clearDataSourceType() {
        contextHolder.remove();
    }
}