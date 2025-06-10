package com.game.service.gateway.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Dubbo configuration
 * Gateway only acts as a consumer, no service export
 *
 * @author lx
 * @date 2024-01-01
 */
@Configuration
/**
 * GatewayDubbo配置类
 * 
 * 功能说明：
 * - 配置系统或模块的参数和属性
 * - 支持配置的自动加载和验证
 * - 集成Spring Boot配置管理机制
 *
 * @author lx
 * @date 2024-01-01
 */
public class GatewayDubboConfig {

    /**
     * Application configuration for Gateway
     */
    @Bean
    public ApplicationConfig gatewayApplicationConfig() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("game-gateway");
        application.setVersion("1.0.0");
        application.setOwner("game-team");
        return application;
    }

    /**
     * Registry configuration
     */
    @Bean
    public RegistryConfig gatewayRegistryConfig() {
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("nacos://127.0.0.1:8848");
        registry.setUsername("nacos");
        registry.setPassword("nacos");
        return registry;
    }

    /**
     * Consumer configuration
     */
    @Bean
    public ConsumerConfig gatewayConsumerConfig() {
        ConsumerConfig consumer = new ConsumerConfig();
        consumer.setTimeout(3000);
        consumer.setRetries(1); // Reduced retries for gateway
        consumer.setLoadbalance("roundrobin");
        consumer.setCheck(false);
        consumer.setAsync(false);
        return consumer;
    }
}