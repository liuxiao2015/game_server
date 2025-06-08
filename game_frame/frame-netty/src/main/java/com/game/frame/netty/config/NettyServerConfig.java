package com.game.frame.netty.config;

import com.game.common.Constants;

/**
 * Netty server configuration
 * Contains all server configuration parameters
 *
 * @author lx
 * @date 2024-01-01
 */
public class NettyServerConfig {
    
    private int port = Constants.DEFAULT_PORT;
    private int bossThreads = Constants.DEFAULT_BOSS_THREADS;
    private int workerThreads = Constants.DEFAULT_WORKER_THREADS;
    private int maxConnections = Constants.DEFAULT_MAX_CONNECTIONS;
    private int soBacklog = Constants.DEFAULT_SO_BACKLOG;
    private int heartbeatInterval = Constants.DEFAULT_HEARTBEAT_INTERVAL;
    private int heartbeatTimeout = Constants.DEFAULT_HEARTBEAT_TIMEOUT;
    
    // TCP options
    private boolean tcpNoDelay = true;
    private boolean soKeepAlive = true;
    private boolean soReuseAddr = true;
    private int connectTimeoutMillis = 10000;
    
    // Buffer sizes
    private int sendBufferSize = 65536;
    private int receiveBufferSize = 65536;
    
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
     * @param port the port to bind
     * @return this config for chaining
     */
    public NettyServerConfig setPort(int port) {
        this.port = port;
        return this;
    }
    
    /**
     * Gets the boss thread count
     * 
     * @return boss thread count
     */
    public int getBossThreads() {
        return bossThreads;
    }
    
    /**
     * Sets the boss thread count
     * 
     * @param bossThreads boss thread count
     * @return this config for chaining
     */
    public NettyServerConfig setBossThreads(int bossThreads) {
        this.bossThreads = bossThreads;
        return this;
    }
    
    /**
     * Gets the worker thread count
     * 
     * @return worker thread count
     */
    public int getWorkerThreads() {
        return workerThreads;
    }
    
    /**
     * Sets the worker thread count
     * 
     * @param workerThreads worker thread count
     * @return this config for chaining
     */
    public NettyServerConfig setWorkerThreads(int workerThreads) {
        this.workerThreads = workerThreads;
        return this;
    }
    
    /**
     * Gets the maximum connection limit
     * 
     * @return max connections
     */
    public int getMaxConnections() {
        return maxConnections;
    }
    
    /**
     * Sets the maximum connection limit
     * 
     * @param maxConnections max connections
     * @return this config for chaining
     */
    public NettyServerConfig setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }
    
    /**
     * Gets the SO_BACKLOG value
     * 
     * @return SO_BACKLOG value
     */
    public int getSoBacklog() {
        return soBacklog;
    }
    
    /**
     * Sets the SO_BACKLOG value
     * 
     * @param soBacklog SO_BACKLOG value
     * @return this config for chaining
     */
    public NettyServerConfig setSoBacklog(int soBacklog) {
        this.soBacklog = soBacklog;
        return this;
    }
    
    /**
     * Gets the heartbeat interval in milliseconds
     * 
     * @return heartbeat interval
     */
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    /**
     * Sets the heartbeat interval in milliseconds
     * 
     * @param heartbeatInterval heartbeat interval
     * @return this config for chaining
     */
    public NettyServerConfig setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
        return this;
    }
    
    /**
     * Gets the heartbeat timeout in milliseconds
     * 
     * @return heartbeat timeout
     */
    public int getHeartbeatTimeout() {
        return heartbeatTimeout;
    }
    
    /**
     * Sets the heartbeat timeout in milliseconds
     * 
     * @param heartbeatTimeout heartbeat timeout
     * @return this config for chaining
     */
    public NettyServerConfig setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
        return this;
    }
    
    // TCP option getters and setters
    
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }
    
    public NettyServerConfig setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
        return this;
    }
    
    public boolean isSoKeepAlive() {
        return soKeepAlive;
    }
    
    public NettyServerConfig setSoKeepAlive(boolean soKeepAlive) {
        this.soKeepAlive = soKeepAlive;
        return this;
    }
    
    public boolean isSoReuseAddr() {
        return soReuseAddr;
    }
    
    public NettyServerConfig setSoReuseAddr(boolean soReuseAddr) {
        this.soReuseAddr = soReuseAddr;
        return this;
    }
    
    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }
    
    public NettyServerConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        return this;
    }
    
    public int getSendBufferSize() {
        return sendBufferSize;
    }
    
    public NettyServerConfig setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }
    
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }
    
    public NettyServerConfig setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }
    
    @Override
    public String toString() {
        return "NettyServerConfig{" +
                "port=" + port +
                ", bossThreads=" + bossThreads +
                ", workerThreads=" + workerThreads +
                ", maxConnections=" + maxConnections +
                ", soBacklog=" + soBacklog +
                ", heartbeatInterval=" + heartbeatInterval +
                ", heartbeatTimeout=" + heartbeatTimeout +
                ", tcpNoDelay=" + tcpNoDelay +
                ", soKeepAlive=" + soKeepAlive +
                ", soReuseAddr=" + soReuseAddr +
                ", connectTimeoutMillis=" + connectTimeoutMillis +
                ", sendBufferSize=" + sendBufferSize +
                ", receiveBufferSize=" + receiveBufferSize +
                '}';
    }
}