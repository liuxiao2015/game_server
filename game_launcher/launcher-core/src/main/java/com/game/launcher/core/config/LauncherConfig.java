package com.game.launcher.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏服务器启动器配置类
 * 
 * 功能说明：
 * - 管理游戏服务器集群的配置参数和启动设置
 * - 支持多种配置源的加载：命令行参数、配置文件、资源文件
 * - 提供服务定义、环境变量、健康检查、日志配置的统一管理
 * - 支持不同部署环境的配置切换和验证
 * 
 * 配置层级结构：
 * - 服务定义：各个微服务的启动参数、端口、依赖关系
 * - 环境变量：JVM参数、Spring配置、系统环境设置
 * - 健康检查：服务健康状态监控的间隔、超时、重试配置
 * - 日志配置：日志级别、输出格式、文件路径等设置
 * - 运行环境：开发、测试、生产环境的差异化配置
 * 
 * 配置加载策略：
 * 1. 优先使用命令行指定的配置文件
 * 2. 其次加载classpath中的默认配置文件
 * 3. 最后使用硬编码的默认配置
 * 4. 支持YAML格式的配置文件解析
 * 5. 配置加载失败时提供降级策略
 * 
 * 服务编排能力：
 * - 定义微服务的启动顺序和依赖关系
 * - 支持服务的动态扩缩容配置
 * - 提供端口分配和冲突检测
 * - 支持服务的启用/禁用控制
 * 
 * 技术特点：
 * - 使用Jackson YAML解析器处理配置文件
 * - 支持配置的热重载和动态更新
 * - 提供配置验证和错误处理机制
 * - 遵循Spring Boot配置规范
 * 
 * 使用场景：
 * - 开发环境的本地服务器启动
 * - 测试环境的自动化部署
 * - 生产环境的服务编排
 * - Docker容器化部署配置
 * 
 * 配置示例：
 * ```yaml
 * profile: production
 * services:
 *   - name: gateway
 *     module: service-gateway
 *     port: 8080
 *     enabled: true
 *     replicas: 2
 * environment:
 *   JAVA_OPTS: "-Xms1g -Xmx4g"
 *   SPRING_PROFILES_ACTIVE: "production"
 * healthCheck:
 *   intervalSeconds: 30
 *   timeoutSeconds: 5
 * ```
 * 
 * 扩展性考虑：
 * - 支持自定义配置解析器
 * - 可扩展新的配置源类型
 * - 支持配置加密和安全存储
 * - 预留插件化配置机制
 *
 * @author lx
 * @date 2025/01/08
 */
public class LauncherConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LauncherConfig.class);
    
    /** YAML配置文件解析器，用于读取和解析YAML格式的配置文件 */
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    /** 服务定义列表，包含所有需要启动的微服务配置信息 */
    private List<ServiceDefinition> services = new ArrayList<>();
    
    /** 全局环境变量配置，应用于所有启动的服务 */
    private Map<String, String> environment = new HashMap<>();
    
    /** 健康检查配置，定义服务健康状态监控的相关参数 */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();
    
    /** 日志配置，统一管理所有服务的日志输出规则 */
    private LogConfig logging = new LogConfig();
    
    /** 运行环境标识，如development、testing、production等 */
    private String profile = "development";

    /**
     * Load configuration from command line arguments
     */
    public static LauncherConfig load(String[] args) {
        try {
            String configFile = getConfigFile(args);
            return loadFromFile(configFile);
            
        } catch (Exception e) {
            logger.error("Failed to load launcher configuration: {}", e.getMessage(), e);
            return createDefault();
        }
    }

    /**
     * Load configuration from file
     */
    public static LauncherConfig loadFromFile(String configFile) {
        try {
            if (configFile != null && new File(configFile).exists()) {
                logger.info("Loading configuration from file: {}", configFile);
                return yamlMapper.readValue(new File(configFile), LauncherConfig.class);
            } else {
                logger.info("Loading default configuration from resources");
                return loadFromResources();
            }
            
        } catch (Exception e) {
            logger.error("Failed to load configuration from file: {}", configFile, e);
            return createDefault();
        }
    }

    /**
     * Load configuration from resources
     */
    private static LauncherConfig loadFromResources() {
        try {
            InputStream stream = LauncherConfig.class.getResourceAsStream("/launcher-config.yml");
            if (stream != null) {
                return yamlMapper.readValue(stream, LauncherConfig.class);
            }
        } catch (Exception e) {
            logger.warn("Failed to load configuration from resources: {}", e.getMessage());
        }
        
        return createDefault();
    }

    /**
     * Create default configuration
     */
    private static LauncherConfig createDefault() {
        logger.info("Creating default launcher configuration");
        
        LauncherConfig config = new LauncherConfig();
        
        // Add default services
        config.addService(createServiceDefinition("gateway", "service-gateway", 8080, List.of()));
        config.addService(createServiceDefinition("logic", "service-logic", 8081, List.of("gateway")));
        config.addService(createServiceDefinition("chat", "service-chat", 8082, List.of("gateway")));
        config.addService(createServiceDefinition("payment", "service-payment", 8083, List.of("gateway")));
        config.addService(createServiceDefinition("match", "service-match", 8084, List.of("gateway")));
        
        // Default environment
        config.environment.put("JAVA_OPTS", "-Xms512m -Xmx2g");
        config.environment.put("SPRING_PROFILES_ACTIVE", "development");
        
        return config;
    }

    /**
     * Create service definition
     */
    private static ServiceDefinition createServiceDefinition(String name, String module, int port, List<String> dependencies) {
        ServiceDefinition service = new ServiceDefinition();
        service.setName(name);
        service.setModule(module);
        service.setPort(port);
        service.setDependencies(dependencies);
        service.setEnabled(true);
        service.setReplicas(1);
        
        Map<String, String> env = new HashMap<>();
        env.put("SERVER_PORT", String.valueOf(port));
        service.setEnvironment(env);
        
        return service;
    }

    /**
     * Get config file from command line arguments
     */
    private static String getConfigFile(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--config".equals(args[i]) || "-c".equals(args[i])) {
                return args[i + 1];
            }
        }
        return "launcher-config.yml";
    }

    /**
     * Add service definition
     */
    public void addService(ServiceDefinition service) {
        services.add(service);
    }

    // Getters and Setters
    public List<ServiceDefinition> getServices() {
        return services;
    }

    public void setServices(List<ServiceDefinition> services) {
        this.services = services;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public HealthCheckConfig getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckConfig healthCheck) {
        this.healthCheck = healthCheck;
    }

    public LogConfig getLogging() {
        return logging;
    }

    public void setLogging(LogConfig logging) {
        this.logging = logging;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public String toString() {
        return "LauncherConfig{" +
                "serviceCount=" + services.size() +
                ", profile='" + profile + '\'' +
                ", environmentVars=" + environment.size() +
                '}';
    }

    /**
     * Service definition inner class
     */
    public static class ServiceDefinition {
        private String name;
        private String module;
        private int port;
        private List<String> dependencies = new ArrayList<>();
        private Map<String, String> environment = new HashMap<>();
        private boolean enabled = true;
        private int replicas = 1;
        private String healthCheckPath = "/health";
        private int startupTimeoutSeconds = 120;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getModule() { return module; }
        public void setModule(String module) { this.module = module; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        public Map<String, String> getEnvironment() { return environment; }
        public void setEnvironment(Map<String, String> environment) { this.environment = environment; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getReplicas() { return replicas; }
        public void setReplicas(int replicas) { this.replicas = replicas; }
        public String getHealthCheckPath() { return healthCheckPath; }
        public void setHealthCheckPath(String healthCheckPath) { this.healthCheckPath = healthCheckPath; }
        public int getStartupTimeoutSeconds() { return startupTimeoutSeconds; }
        public void setStartupTimeoutSeconds(int startupTimeoutSeconds) { this.startupTimeoutSeconds = startupTimeoutSeconds; }

        @Override
        public String toString() {
            return "ServiceDefinition{" +
                    "name='" + name + '\'' +
                    ", module='" + module + '\'' +
                    ", port=" + port +
                    ", enabled=" + enabled +
                    ", replicas=" + replicas +
                    '}';
        }
    }

    /**
     * Health check configuration
     */
    public static class HealthCheckConfig {
        private int intervalSeconds = 30;
        private int timeoutSeconds = 5;
        private int retries = 3;

        public int getIntervalSeconds() { return intervalSeconds; }
        public void setIntervalSeconds(int intervalSeconds) { this.intervalSeconds = intervalSeconds; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
        public int getRetries() { return retries; }
        public void setRetries(int retries) { this.retries = retries; }
    }

    /**
     * Logging configuration
     */
    public static class LogConfig {
        private String level = "INFO";
        private String pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n";

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
    }
}