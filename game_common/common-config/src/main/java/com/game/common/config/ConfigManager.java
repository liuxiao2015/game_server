package com.game.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏配置管理器
 * 
 * 功能说明：
 * - 负责游戏所有配置文件的统一加载和管理
 * - 提供配置文件的缓存机制，提升配置访问性能
 * - 支持配置的热更新和重新加载功能
 * - 集成JSON配置文件的解析和验证功能
 * 
 * 设计思路：
 * - 采用单例模式和静态方法，确保全局配置的统一性
 * - 使用Jackson库进行JSON配置文件的反序列化
 * - 通过注解机制关联配置类与配置文件
 * - 利用ConcurrentHashMap实现线程安全的配置缓存
 * 
 * 核心功能：
 * - 配置加载：从资源文件中加载配置并转换为Java对象
 * - 配置缓存：将已加载的配置缓存在内存中，避免重复IO
 * - 配置重载：支持运行时重新加载配置，实现热更新
 * - 配置验证：验证配置文件格式和必要字段的完整性
 * 
 * 支持的配置格式：
 * - 单一配置对象：JSON文件包含单个配置实例
 * - 配置对象数组：JSON文件包含配置对象的数组
 * - 自动适配：根据配置文件结构自动选择解析方式
 * 
 * 使用场景：
 * - 游戏启动时的配置初始化
 * - 运行时的配置数据访问
 * - 配置文件的热更新和重载
 * - 开发调试时的配置修改
 * 
 * 配置文件位置：
 * - 所有配置文件统一放置在classpath的config目录下
 * - 通过@ConfigTable注解指定具体的配置文件名
 * 
 * 异常处理：
 * - 配置文件不存在或格式错误时提供详细的错误信息
 * - 记录配置加载过程的详细日志便于问题定位
 * - 确保配置加载失败时系统能够正常处理异常
 *
 * @author lx
 * @date 2025/06/08
 */
public class ConfigManager {
    
    // 日志记录器，用于记录配置加载过程和错误信息
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    
    // Jackson对象映射器，用于JSON配置文件的序列化和反序列化
    // 配置为静态实例，确保全局统一的JSON处理行为
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 配置缓存映射表，存储已加载的配置实例
    // 使用ConcurrentHashMap确保多线程环境下的访问安全
    // key为配置类的Class对象，value为配置实例
    private static final ConcurrentHashMap<Class<?>, Object> configCache = new ConcurrentHashMap<>();
    
    /**
     * 加载游戏配置
     * 
     * 功能说明：
     * - 根据配置类加载对应的JSON配置文件
     * - 自动进行配置缓存，避免重复加载提升性能
     * - 支持单一对象和对象数组两种配置文件格式
     * - 提供完整的异常处理和错误日志记录
     * 
     * 加载流程：
     * 1. 检查配置缓存，如果已存在则直接返回
     * 2. 通过@ConfigTable注解获取配置文件名
     * 3. 从classpath的config目录下读取配置文件
     * 4. 使用Jackson进行JSON反序列化
     * 5. 处理单一对象和数组格式的兼容性
     * 6. 将配置实例缓存并返回
     * 
     * 配置文件格式支持：
     * - 单一配置对象：{"key1": "value1", "key2": "value2"}
     * - 单元素数组：[{"key1": "value1", "key2": "value2"}]
     * - 自动适配不同格式，优先尝试单一对象解析
     * 
     * @param configClass 配置类的Class对象，必须标注@ConfigTable注解
     * @param <T> 配置类的泛型类型
     * @return 配置实例，类型与输入的配置类一致
     * 
     * 异常情况：
     * - 配置类缺少@ConfigTable注解
     * - 配置文件在classpath中不存在
     * - JSON格式错误或字段类型不匹配
     * - 数组格式配置包含多个元素
     * 
     * 性能优化：
     * - 使用computeIfAbsent确保线程安全的单次加载
     * - 配置加载后缓存在内存中，后续访问无IO开销
     * - Jackson对象映射器复用，避免重复创建解析器
     * 
     * 使用示例：
     * TaskConfig taskConfig = ConfigManager.loadConfig(TaskConfig.class);
     * MonsterConfig monsterConfig = ConfigManager.loadConfig(MonsterConfig.class);
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadConfig(Class<T> configClass) {
        Object config = configCache.computeIfAbsent(configClass, clazz -> {
            try {
                // 获取配置类的@ConfigTable注解
                ConfigTable annotation = clazz.getAnnotation(ConfigTable.class);
                if (annotation == null) {
                    throw new IllegalArgumentException("Config class must have @ConfigTable annotation: " + clazz.getName());
                }
                
                // 构建配置文件路径
                String fileName = annotation.value();
                InputStream inputStream = ConfigManager.class.getClassLoader().getResourceAsStream("config/" + fileName);
                if (inputStream == null) {
                    throw new IllegalArgumentException("Config file not found: config/" + fileName);
                }
                
                Object loadedConfig;
                try {
                    // 首先尝试作为单一对象解析
                    loadedConfig = objectMapper.readValue(inputStream, clazz);
                } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
                    // 如果单一对象解析失败，尝试作为数组解析
                    loadedConfig = objectMapper.readValue(inputStream, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
                    if (loadedConfig instanceof List<?> list && list.size() == 1) {
                        // 如果是单元素数组，取第一个元素作为配置
                        loadedConfig = list.get(0);
                    } else {
                        throw new IllegalArgumentException("Config file must contain a single object or a single-element array: " + fileName);
                    }
                }
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
     * 重新加载指定配置
     * 
     * 功能说明：
     * - 清除指定配置类的缓存数据
     * - 重新从配置文件加载最新的配置内容
     * - 支持运行时的配置热更新功能
     * 
     * 重载流程：
     * 1. 从配置缓存中移除指定配置类的实例
     * 2. 调用loadConfig方法重新加载配置
     * 3. 新的配置实例会自动缓存供后续使用
     * 
     * @param configClass 需要重新加载的配置类
     * @param <T> 配置类的泛型类型
     * 
     * 使用场景：
     * - 开发阶段修改配置文件后的热更新
     * - 运营期间的配置调整和优化
     * - 配置文件错误修复后的重新加载
     * 
     * 注意事项：
     * - 重载会清除旧的配置缓存，确保获取最新配置
     * - 重载过程中可能存在短暂的配置不一致状态
     * - 建议在系统负载较低时进行配置重载
     */
    public static <T> void reloadConfig(Class<T> configClass) {
        configCache.remove(configClass);
        loadConfig(configClass);
    }
    
    /**
     * 清除所有配置缓存
     * 
     * 功能说明：
     * - 清空所有已缓存的配置实例
     * - 强制所有配置在下次访问时重新加载
     * - 主要用于系统重置和内存清理
     * 
     * 清理效果：
     * - 释放配置缓存占用的内存空间
     * - 确保下次配置访问时重新读取文件
     * - 重置所有配置的状态到初始加载状态
     * 
     * 使用场景：
     * - 系统维护时的内存清理
     * - 大量配置修改后的批量重载
     * - 系统重启前的资源清理
     * - 内存不足时的缓存清理
     * 
     * 注意事项：
     * - 清理后所有配置访问都需要重新加载文件
     * - 可能导致短期内的性能下降
     * - 建议在维护窗口期间执行
     */
    public static void clearCache() {
        configCache.clear();
        logger.info("Cleared all config cache");
    }
}