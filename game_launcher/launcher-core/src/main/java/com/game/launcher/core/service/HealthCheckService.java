package com.game.launcher.core.service;

import com.game.launcher.core.config.LauncherConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Health check service
 * Performs health checks on services to ensure they are running correctly
 *
 * @author lx
 * @date 2025/01/08
 */
/**
 * HealthCheckService
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class HealthCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    
    private final LauncherConfig.HealthCheckConfig config;

    public HealthCheckService(LauncherConfig.HealthCheckConfig config) {
        this.config = config;
    }

    /**
     * Check if service is healthy
     */
    public boolean isServiceHealthy(LauncherConfig.ServiceDefinition service) {
        try {
            return performHealthCheck(service);
            
        } catch (Exception e) {
            logger.debug("Health check failed for service {}: {}", service.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Perform actual health check
     */
    private boolean performHealthCheck(LauncherConfig.ServiceDefinition service) {
        int attempts = 0;
        
        while (attempts < config.getRetries()) {
            try {
                if (checkServiceEndpoint(service)) {
                    return true;
                }
                
                attempts++;
                if (attempts < config.getRetries()) {
                    Thread.sleep(1000); // Wait 1 second between retries
                }
                
            } catch (Exception e) {
                logger.debug("Health check attempt {} failed for service {}: {}", 
                        attempts + 1, service.getName(), e.getMessage());
                attempts++;
            }
        }
        
        return false;
    }

    /**
     * Check service endpoint
     */
    private boolean checkServiceEndpoint(LauncherConfig.ServiceDefinition service) {
        try {
            String healthUrl = String.format("http://localhost:%d%s", 
                    service.getPort(), service.getHealthCheckPath());
            
            URL url = URI.create(healthUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(config.getTimeoutSeconds() * 1000);
            connection.setReadTimeout(config.getTimeoutSeconds() * 1000);
            
            int responseCode = connection.getResponseCode();
            
            boolean healthy = responseCode >= 200 && responseCode < 300;
            
            if (healthy) {
                logger.debug("Service {} health check passed ({})", service.getName(), responseCode);
            } else {
                logger.debug("Service {} health check failed ({})", service.getName(), responseCode);
            }
            
            return healthy;
            
        } catch (IOException e) {
            logger.debug("Health check connection failed for service {}: {}", 
                    service.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Get health status for all services
     */
    public HealthStatus getOverallHealthStatus(LauncherConfig config) {
        HealthStatus status = new HealthStatus();
        
        for (LauncherConfig.ServiceDefinition service : config.getServices()) {
            if (service.isEnabled()) {
                boolean healthy = isServiceHealthy(service);
                status.addServiceStatus(service.getName(), healthy);
            }
        }
        
        return status;
    }

    /**
     * Health status container
     */
    public static class HealthStatus {
        private final java.util.Map<String, Boolean> serviceStatus = new java.util.HashMap<>();
        private int totalServices = 0;
        private int healthyServices = 0;

        public void addServiceStatus(String serviceName, boolean healthy) {
            serviceStatus.put(serviceName, healthy);
            totalServices++;
            if (healthy) {
                healthyServices++;
            }
        }

        public boolean isOverallHealthy() {
            return healthyServices == totalServices && totalServices > 0;
        }

        public double getHealthPercentage() {
            return totalServices > 0 ? (double) healthyServices / totalServices * 100 : 0;
        }

        public java.util.Map<String, Boolean> getServiceStatus() {
            return serviceStatus;
        }

        public int getTotalServices() {
            return totalServices;
        }

        public int getHealthyServices() {
            return healthyServices;
        }

        public int getUnhealthyServices() {
            return totalServices - healthyServices;
        }

        @Override
        public String toString() {
            return String.format("HealthStatus{healthy=%d/%d (%.1f%%)}", 
                    healthyServices, totalServices, getHealthPercentage());
        }
    }
}