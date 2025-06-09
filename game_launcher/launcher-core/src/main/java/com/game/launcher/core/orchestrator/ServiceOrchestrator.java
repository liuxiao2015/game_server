package com.game.launcher.core.orchestrator;

import com.game.launcher.core.config.LauncherConfig;
import com.game.launcher.core.service.HealthCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Service orchestrator
 * Manages service startup order, dependencies, and health monitoring
 *
 * @author lx
 * @date 2025/01/08
 */
public class ServiceOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(ServiceOrchestrator.class);
    
    private final LauncherConfig config;
    private final HealthCheckService healthCheckService;
    private final ExecutorService executor;
    private final Map<String, ServiceInstance> serviceInstances;
    private final Map<String, CompletableFuture<Void>> startupFutures;
    private volatile boolean isShuttingDown = false;

    public ServiceOrchestrator(LauncherConfig config) {
        this.config = config;
        this.healthCheckService = new HealthCheckService(config.getHealthCheck());
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "orchestrator-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        this.serviceInstances = new ConcurrentHashMap<>();
        this.startupFutures = new ConcurrentHashMap<>();
    }

    /**
     * Start all services according to dependency order
     */
    public void start() {
        try {
            logger.info("Starting service orchestration with {} services", config.getServices().size());
            
            // Validate configuration
            validateConfiguration();
            
            // Calculate startup order based on dependencies
            List<LauncherConfig.ServiceDefinition> startupOrder = calculateStartupOrder();
            logger.info("Service startup order: {}", 
                    startupOrder.stream().map(LauncherConfig.ServiceDefinition::getName).toList());
            
            // Start services in order
            for (LauncherConfig.ServiceDefinition service : startupOrder) {
                if (service.isEnabled()) {
                    startService(service);
                    waitForServiceReady(service);
                } else {
                    logger.info("Skipping disabled service: {}", service.getName());
                }
            }
            
            // Start health monitoring
            startHealthMonitoring();
            
            logger.info("All services started successfully");
            
        } catch (Exception e) {
            logger.error("Failed to start services: {}", e.getMessage(), e);
            shutdown();
            throw new RuntimeException("Service orchestration failed", e);
        }
    }

    /**
     * Shutdown all services gracefully
     */
    public void shutdown() {
        if (isShuttingDown) {
            return;
        }
        
        isShuttingDown = true;
        logger.info("Starting graceful shutdown of all services");
        
        try {
            // Cancel all startup futures
            startupFutures.values().forEach(future -> future.cancel(true));
            
            // Stop services in reverse dependency order
            List<LauncherConfig.ServiceDefinition> shutdownOrder = calculateShutdownOrder();
            
            for (LauncherConfig.ServiceDefinition service : shutdownOrder) {
                stopService(service);
            }
            
            // Shutdown executor
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            
            logger.info("All services shut down successfully");
            
        } catch (Exception e) {
            logger.error("Error during shutdown: {}", e.getMessage(), e);
        }
    }

    /**
     * Start a single service
     */
    private void startService(LauncherConfig.ServiceDefinition service) {
        logger.info("Starting service: {}", service.getName());
        
        try {
            // Wait for dependencies to be ready
            waitForDependencies(service);
            
            // Create service instance
            ServiceInstance instance = createServiceInstance(service);
            serviceInstances.put(service.getName(), instance);
            
            // Start the service asynchronously
            CompletableFuture<Void> startupFuture = CompletableFuture.runAsync(() -> {
                try {
                    instance.start();
                    logger.info("Service started: {}", service.getName());
                } catch (Exception e) {
                    logger.error("Failed to start service {}: {}", service.getName(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }, executor);
            
            startupFutures.put(service.getName(), startupFuture);
            
        } catch (Exception e) {
            logger.error("Error starting service {}: {}", service.getName(), e.getMessage(), e);
            throw new RuntimeException("Failed to start service: " + service.getName(), e);
        }
    }

    /**
     * Stop a single service
     */
    private void stopService(LauncherConfig.ServiceDefinition service) {
        logger.info("Stopping service: {}", service.getName());
        
        try {
            ServiceInstance instance = serviceInstances.get(service.getName());
            if (instance != null) {
                instance.stop();
                serviceInstances.remove(service.getName());
            }
            
        } catch (Exception e) {
            logger.error("Error stopping service {}: {}", service.getName(), e.getMessage(), e);
        }
    }

    /**
     * Wait for service to be ready
     */
    private void waitForServiceReady(LauncherConfig.ServiceDefinition service) {
        try {
            CompletableFuture<Void> startupFuture = startupFutures.get(service.getName());
            if (startupFuture != null) {
                startupFuture.get(service.getStartupTimeoutSeconds(), TimeUnit.SECONDS);
            }
            
            // Perform health check
            if (!healthCheckService.isServiceHealthy(service)) {
                throw new RuntimeException("Service failed health check: " + service.getName());
            }
            
            logger.info("Service ready: {}", service.getName());
            
        } catch (TimeoutException e) {
            logger.error("Service startup timeout: {}", service.getName());
            throw new RuntimeException("Service startup timeout: " + service.getName());
        } catch (Exception e) {
            logger.error("Error waiting for service {}: {}", service.getName(), e.getMessage(), e);
            throw new RuntimeException("Service startup failed: " + service.getName(), e);
        }
    }

    /**
     * Wait for service dependencies to be ready
     */
    private void waitForDependencies(LauncherConfig.ServiceDefinition service) {
        for (String dependency : service.getDependencies()) {
            ServiceInstance dependencyInstance = serviceInstances.get(dependency);
            if (dependencyInstance == null || !dependencyInstance.isRunning()) {
                throw new RuntimeException("Dependency not ready: " + dependency);
            }
        }
    }

    /**
     * Create service instance
     */
    private ServiceInstance createServiceInstance(LauncherConfig.ServiceDefinition service) {
        // This is a simplified implementation - in practice, this would create
        // actual service instances (Docker containers, processes, etc.)
        return new MockServiceInstance(service);
    }

    /**
     * Calculate startup order based on dependencies
     */
    private List<LauncherConfig.ServiceDefinition> calculateStartupOrder() {
        List<LauncherConfig.ServiceDefinition> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        
        for (LauncherConfig.ServiceDefinition service : config.getServices()) {
            if (!visited.contains(service.getName())) {
                topologicalSort(service, visited, visiting, ordered);
            }
        }
        
        return ordered;
    }

    /**
     * Calculate shutdown order (reverse of startup order)
     */
    private List<LauncherConfig.ServiceDefinition> calculateShutdownOrder() {
        List<LauncherConfig.ServiceDefinition> startupOrder = calculateStartupOrder();
        Collections.reverse(startupOrder);
        return startupOrder;
    }

    /**
     * Topological sort for dependency resolution
     */
    private void topologicalSort(LauncherConfig.ServiceDefinition service, Set<String> visited, 
                                Set<String> visiting, List<LauncherConfig.ServiceDefinition> ordered) {
        
        if (visiting.contains(service.getName())) {
            throw new RuntimeException("Circular dependency detected: " + service.getName());
        }
        
        if (visited.contains(service.getName())) {
            return;
        }
        
        visiting.add(service.getName());
        
        // Visit dependencies first
        for (String dependencyName : service.getDependencies()) {
            LauncherConfig.ServiceDefinition dependency = findServiceByName(dependencyName);
            if (dependency != null) {
                topologicalSort(dependency, visited, visiting, ordered);
            }
        }
        
        visiting.remove(service.getName());
        visited.add(service.getName());
        ordered.add(service);
    }

    /**
     * Find service definition by name
     */
    private LauncherConfig.ServiceDefinition findServiceByName(String name) {
        return config.getServices().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validate configuration
     */
    private void validateConfiguration() {
        if (config.getServices().isEmpty()) {
            throw new RuntimeException("No services defined in configuration");
        }
        
        // Check for duplicate service names
        Set<String> names = new HashSet<>();
        for (LauncherConfig.ServiceDefinition service : config.getServices()) {
            if (!names.add(service.getName())) {
                throw new RuntimeException("Duplicate service name: " + service.getName());
            }
        }
        
        // Check for invalid dependencies
        Set<String> serviceNames = names;
        for (LauncherConfig.ServiceDefinition service : config.getServices()) {
            for (String dependency : service.getDependencies()) {
                if (!serviceNames.contains(dependency)) {
                    throw new RuntimeException("Invalid dependency: " + dependency + " for service: " + service.getName());
                }
            }
        }
    }

    /**
     * Start health monitoring
     */
    private void startHealthMonitoring() {
        ScheduledExecutorService healthMonitor = Executors.newScheduledThreadPool(1);
        
        healthMonitor.scheduleAtFixedRate(() -> {
            if (!isShuttingDown) {
                for (LauncherConfig.ServiceDefinition service : config.getServices()) {
                    if (service.isEnabled()) {
                        boolean healthy = healthCheckService.isServiceHealthy(service);
                        if (!healthy) {
                            logger.warn("Service health check failed: {}", service.getName());
                        }
                    }
                }
            }
        }, config.getHealthCheck().getIntervalSeconds(), config.getHealthCheck().getIntervalSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Mock service instance for demonstration
     */
    private static class MockServiceInstance implements ServiceInstance {
        private final LauncherConfig.ServiceDefinition service;
        private volatile boolean running = false;

        public MockServiceInstance(LauncherConfig.ServiceDefinition service) {
            this.service = service;
        }

        @Override
        public void start() {
            logger.info("Mock starting service: {}", service.getName());
            // Simulate startup time
            try {
                Thread.sleep(1000 + new Random().nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            running = true;
        }

        @Override
        public void stop() {
            logger.info("Mock stopping service: {}", service.getName());
            running = false;
        }

        @Override
        public boolean isRunning() {
            return running;
        }
    }

    /**
     * Service instance interface
     */
    private interface ServiceInstance {
        void start();
        void stop();
        boolean isRunning();
    }
}