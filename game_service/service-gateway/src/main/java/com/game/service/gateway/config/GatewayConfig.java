package com.game.service.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway configuration with service port, connection limit, and timeout configuration
 *
 * @author lx
 * @date 2024-01-01
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayConfig {
    
    private int port = 8888;
    private int maxConnections = 10000;
    private int connectTimeoutMs = 10000;
    private int heartbeatIntervalMs = 30000;
    private int heartbeatTimeoutMs = 90000;
    private int bossThreads = 1;
    private int workerThreads = Runtime.getRuntime().availableProcessors() * 2;
    
    /**
     * Gets the server port
     * 
     * @return server port
     */
    public int getPort() {
        return port;
    }
    
    /**
     * Sets the server port
     * 
     * @param port server port
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Gets the maximum connections
     * 
     * @return max connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * Sets the maximum connections
     * 
     * @param maxConnections max connections
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    /**
     * Gets the connect timeout
     * 
     * @return connect timeout in milliseconds
     */
    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }
    
    /**
     * Sets the connect timeout
     * 
     * @param connectTimeoutMs connect timeout in milliseconds
     */
    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }
    
    /**
     * Gets the heartbeat interval
     * 
     * @return heartbeat interval in milliseconds
     */
    public int getHeartbeatIntervalMs() {
        return heartbeatIntervalMs;
    }
    
    /**
     * Sets the heartbeat interval
     * 
     * @param heartbeatIntervalMs heartbeat interval in milliseconds
     */
    public void setHeartbeatIntervalMs(int heartbeatIntervalMs) {
        this.heartbeatIntervalMs = heartbeatIntervalMs;
    }
    
    /**
     * Gets the heartbeat timeout
     * 
     * @return heartbeat timeout in milliseconds
     */
    public int getHeartbeatTimeoutMs() {
        return heartbeatTimeoutMs;
    }
    
    /**
     * Sets the heartbeat timeout
     * 
     * @param heartbeatTimeoutMs heartbeat timeout in milliseconds
     */
    public void setHeartbeatTimeoutMs(int heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }
    
    /**
     * Gets the boss threads
     * 
     * @return boss threads
     */
    public int getBossThreads() {
        return bossThreads;
    }
    
    /**
     * Sets the boss threads
     * 
     * @param bossThreads boss threads
     */
    public void setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
    }
    
    /**
     * Gets the worker threads
     * 
     * @return worker threads
     */
    public int getWorkerThreads() {
        return workerThreads;
    }
    
    /**
     * Sets the worker threads
     * 
     * @param workerThreads worker threads
     */
    public void setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
    }
    
    @Override
    public String toString() {
        return "GatewayConfig{" +
                "port=" + port +
                ", maxConnections=" + maxConnections +
                ", connectTimeoutMs=" + connectTimeoutMs +
                ", heartbeatIntervalMs=" + heartbeatIntervalMs +
                ", heartbeatTimeoutMs=" + heartbeatTimeoutMs +
                ", bossThreads=" + bossThreads +
                ", workerThreads=" + workerThreads +
                '}';
    }
}