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
 * Launcher configuration
 * Manages configuration for service orchestration and deployment
 *
 * @author lx
 * @date 2025/01/08
 */
public class LauncherConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LauncherConfig.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    
    private List<ServiceDefinition> services = new ArrayList<>();
    private Map<String, String> environment = new HashMap<>();
    private HealthCheckConfig healthCheck = new HealthCheckConfig();
    private LogConfig logging = new LogConfig();
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