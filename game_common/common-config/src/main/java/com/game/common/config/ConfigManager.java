package com.game.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理器
 * 负责配置文件加载、配置验证、配置缓存和热更新支持
 *
 * @author lx
 * @date 2025/06/08
 */
public class ConfigManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConcurrentHashMap<Class<?>, Object> configCache = new ConcurrentHashMap<>();
    
    /**
     * 加载配置
     * 
     * @param configClass 配置类
     * @param <T> 配置类型
     * @return 配置实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadConfig(Class<T> configClass) {
        Object config = configCache.computeIfAbsent(configClass, clazz -> {
            try {
                ConfigTable annotation = clazz.getAnnotation(ConfigTable.class);
                if (annotation == null) {
                    throw new IllegalArgumentException("Config class must have @ConfigTable annotation: " + clazz.getName());
                }
                
                String fileName = annotation.value();
                InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("config/" + fileName);
                if (inputStream == null) {
                    throw new IllegalArgumentException("Config file not found: config/" + fileName);
                }
                
                Object loadedConfig = objectMapper.readValue(inputStream, clazz);
                logger.info("Loaded config: {} from {}", clazz.getSimpleName(), fileName);
                return loadedConfig;
                
            } catch (Exception e) {
                logger.error("Failed to load config: {}", clazz.getName(), e);
                throw new RuntimeException("Failed to load config", e);
            }
        });
        return (T) config;
    }
    
    /**
     * 重新加载配置
     * 
     * @param configClass 配置类
     * @param <T> 配置类型
     */
    public static <T> void reloadConfig(Class<T> configClass) {
        configCache.remove(configClass);
        loadConfig(configClass);
    }
    
    /**
     * 清除所有配置缓存
     */
    public static void clearCache() {
        configCache.clear();
        logger.info("Cleared all config cache");
    }
}