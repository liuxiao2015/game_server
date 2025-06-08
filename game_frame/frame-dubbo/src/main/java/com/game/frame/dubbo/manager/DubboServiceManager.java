package com.game.frame.dubbo.manager;

import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Dubbo service manager for dynamic service registration/deregistration
 * and service version/group management
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
public class DubboServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(DubboServiceManager.class);

    private final Map<String, ServiceConfig<?>> serviceConfigs = new ConcurrentHashMap<>();
    private final Map<String, ReferenceConfig<?>> referenceConfigs = new ConcurrentHashMap<>();

    /**
     * Dynamically registers a service
     *
     * @param interfaceClass service interface class
     * @param implementation service implementation
     * @param version service version
     * @param group service group
     * @param <T> service type
     * @return service key
     */
    public <T> String registerService(Class<T> interfaceClass, T implementation, String version, String group) {
        String serviceKey = generateServiceKey(interfaceClass.getName(), version, group);
        
        if (serviceConfigs.containsKey(serviceKey)) {
            logger.warn("Service already registered: {}", serviceKey);
            return serviceKey;
        }

        ServiceConfig<T> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(interfaceClass);
        serviceConfig.setRef(implementation);
        serviceConfig.setVersion(version);
        serviceConfig.setGroup(group);
        
        try {
            serviceConfig.export();
            serviceConfigs.put(serviceKey, serviceConfig);
            logger.info("Successfully registered service: {}", serviceKey);
        } catch (Exception e) {
            logger.error("Failed to register service: {}", serviceKey, e);
            throw new RuntimeException("Service registration failed", e);
        }
        
        return serviceKey;
    }

    /**
     * Dynamically deregisters a service
     *
     * @param serviceKey service key
     */
    public void unregisterService(String serviceKey) {
        ServiceConfig<?> serviceConfig = serviceConfigs.remove(serviceKey);
        if (serviceConfig != null) {
            try {
                serviceConfig.unexport();
                logger.info("Successfully unregistered service: {}", serviceKey);
            } catch (Exception e) {
                logger.error("Failed to unregister service: {}", serviceKey, e);
            }
        } else {
            logger.warn("Service not found for unregistration: {}", serviceKey);
        }
    }

    /**
     * Gets a service reference
     *
     * @param interfaceClass service interface class
     * @param version service version
     * @param group service group
     * @param <T> service type
     * @return service reference
     */
    @SuppressWarnings("unchecked")
    public <T> T getReference(Class<T> interfaceClass, String version, String group) {
        String serviceKey = generateServiceKey(interfaceClass.getName(), version, group);
        
        ReferenceConfig<T> referenceConfig = (ReferenceConfig<T>) referenceConfigs.get(serviceKey);
        if (referenceConfig == null) {
            referenceConfig = new ReferenceConfig<>();
            referenceConfig.setInterface(interfaceClass);
            referenceConfig.setVersion(version);
            referenceConfig.setGroup(group);
            referenceConfig.setCheck(false);
            
            referenceConfigs.put(serviceKey, referenceConfig);
            logger.info("Created reference config for service: {}", serviceKey);
        }
        
        return referenceConfig.get();
    }

    /**
     * Gets service metadata information
     *
     * @param serviceKey service key
     * @return service metadata
     */
    public ServiceMetadata getServiceMetadata(String serviceKey) {
        ServiceConfig<?> serviceConfig = serviceConfigs.get(serviceKey);
        if (serviceConfig != null) {
            return new ServiceMetadata(
                serviceKey,
                serviceConfig.getInterface(),
                serviceConfig.getVersion(),
                serviceConfig.getGroup(),
                serviceConfig.isExported()
            );
        }
        return null;
    }

    /**
     * Gets all registered service keys
     *
     * @return set of service keys
     */
    public java.util.Set<String> getRegisteredServices() {
        return serviceConfigs.keySet();
    }

    /**
     * Generates service key from interface name, version and group
     *
     * @param interfaceName interface name
     * @param version version
     * @param group group
     * @return service key
     */
    private String generateServiceKey(String interfaceName, String version, String group) {
        return String.format("%s:%s:%s", interfaceName, version != null ? version : "default", group != null ? group : "default");
    }

    /**
     * Shuts down all services
     */
    public void shutdown() {
        logger.info("Shutting down DubboServiceManager...");
        
        // Unexport all services
        serviceConfigs.forEach((key, config) -> {
            try {
                config.unexport();
                logger.debug("Unexported service: {}", key);
            } catch (Exception e) {
                logger.error("Failed to unexport service: {}", key, e);
            }
        });
        
        // Destroy all references
        referenceConfigs.forEach((key, config) -> {
            try {
                config.destroy();
                logger.debug("Destroyed reference: {}", key);
            } catch (Exception e) {
                logger.error("Failed to destroy reference: {}", key, e);
            }
        });
        
        serviceConfigs.clear();
        referenceConfigs.clear();
        logger.info("DubboServiceManager shutdown completed");
    }

    /**
     * Service metadata information
     */
    public static class ServiceMetadata {
        private final String serviceKey;
        private final String interfaceName;
        private final String version;
        private final String group;
        private final boolean exported;

        public ServiceMetadata(String serviceKey, String interfaceName, String version, String group, boolean exported) {
            this.serviceKey = serviceKey;
            this.interfaceName = interfaceName;
            this.version = version;
            this.group = group;
            this.exported = exported;
        }

        public String getServiceKey() { return serviceKey; }
        public String getInterfaceName() { return interfaceName; }
        public String getVersion() { return version; }
        public String getGroup() { return group; }
        public boolean isExported() { return exported; }

        @Override
        public String toString() {
            return String.format("ServiceMetadata{serviceKey='%s', interfaceName='%s', version='%s', group='%s', exported=%s}",
                    serviceKey, interfaceName, version, group, exported);
        }
    }
}