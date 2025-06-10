package com.game.frame.dubbo.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ConsumerConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo global configuration
 * Application, registry, protocol, consumer and provider configurations
 *
 * @author lx
 * @date 2024-01-01
 */
@Configuration
/**
 * Dubbo配置类
 * 
 * 功能说明：
 * - 配置系统或模块的参数和属性
 * - 支持配置的自动加载和验证
 * - 集成Spring Boot配置管理机制
 *
 * @author lx
 * @date 2024-01-01
 */
public class DubboConfig {

    /**
     * Application configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "dubbo.application")
    public ApplicationConfig applicationConfig() {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("game-server");
        application.setVersion("1.0.0");
        application.setOwner("game-team");
        application.setEnvironment("dev");
        return application;
    }

    /**
     * Registry configuration (Nacos)
     */
    @Bean
    @ConfigurationProperties(prefix = "dubbo.registry")
    public RegistryConfig registryConfig() {
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("nacos://127.0.0.1:8848");
        registry.setUsername("nacos");
        registry.setPassword("nacos");
        return registry;
    }

    /**
     * Protocol configuration (Triple)
     */
    @Bean
    @ConfigurationProperties(prefix = "dubbo.protocol")
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setName("tri");
        protocol.setPort(20880);
        protocol.setThreads(200);
        protocol.setIothreads(Runtime.getRuntime().availableProcessors() + 1);
        protocol.setServer("netty");
        protocol.setSerialization("hessian2");
        return protocol;
    }

    /**
     * Consumer configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "dubbo.consumer")
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumer = new ConsumerConfig();
        consumer.setTimeout(3000);
        consumer.setRetries(2);
        consumer.setLoadbalance("roundrobin");
        consumer.setCheck(false);
        consumer.setAsync(false);
        return consumer;
    }

    /**
     * Provider configuration
     */
    @Bean
    @ConfigurationProperties(prefix = "dubbo.provider")
    public ProviderConfig providerConfig() {
        ProviderConfig provider = new ProviderConfig();
        provider.setTimeout(3000);
        provider.setThreads(200);
        provider.setAccepts(1000);
        provider.setSerialization("hessian2");
        return provider;
    }
}