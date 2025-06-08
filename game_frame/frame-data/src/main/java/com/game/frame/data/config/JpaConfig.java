package com.game.frame.data.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置
 * @author lx
 * @date 2025/06/08
 */
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.game.frame.data.repository",
    "com.game.common.entity"
})
@EntityScan(basePackages = {
    "com.game.frame.data.entity",
    "com.game.common.entity"
})
public class JpaConfig {
    // JPA配置通过application.yml进行
}