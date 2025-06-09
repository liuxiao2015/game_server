package com.game.frame.data.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 动态数据源实现
 * @author lx
 * @date 2025/06/08
 */
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType dataSourceType = DynamicDataSourceContext.getDataSourceType();
        if (dataSourceType == null) {
            // 默认使用主数据源
            return DataSourceType.MASTER;
        }
        return dataSourceType;
    }
}