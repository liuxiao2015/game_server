package com.game.frame.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * MongoDB配置
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.game.frame.data.mongo")
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "game_logs";
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}