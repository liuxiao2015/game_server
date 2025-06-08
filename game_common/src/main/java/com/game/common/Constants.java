package com.game.common;

/**
 * Global constants definition
 *
 * @author lx
 * @date 2024-01-01
 */
public final class Constants {
    
    /**
     * Protocol version
     */
    public static final String PROTOCOL_VERSION = "1.0.0";
    
    /**
     * Default configuration
     */
    public static final int DEFAULT_PORT = 8888;
    public static final int DEFAULT_BOSS_THREADS = 1;
    public static final int DEFAULT_WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    public static final int DEFAULT_MAX_CONNECTIONS = 10000;
    public static final int DEFAULT_SO_BACKLOG = 1024;
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 30000; // 30 seconds
    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 90000;  // 90 seconds
    
    /**
     * Message IDs
     */
    public static final int MSG_HEARTBEAT_REQUEST = 1001;
    public static final int MSG_HEARTBEAT_RESPONSE = 1002;
    public static final int MSG_LOGIN_REQUEST = 2001;
    public static final int MSG_LOGIN_RESPONSE = 2002;
    
    /**
     * Error codes
     */
    public static final int SUCCESS = 0;
    public static final int ERROR_UNKNOWN = 1;
    public static final int ERROR_INVALID_REQUEST = 2;
    public static final int ERROR_AUTHENTICATION_FAILED = 3;
    public static final int ERROR_PERMISSION_DENIED = 4;
    public static final int ERROR_RESOURCE_NOT_FOUND = 5;
    public static final int ERROR_RATE_LIMIT_EXCEEDED = 6;
    public static final int ERROR_INTERNAL_SERVER_ERROR = 7;
    
    /**
     * Thread pool names
     */
    public static final String THREAD_POOL_BOSS = "NettyBoss";
    public static final String THREAD_POOL_WORKER = "NettyWorker";
    public static final String THREAD_POOL_BUSINESS = "BusinessHandler";
    
    private Constants() {
        // Utility class, no instantiation
    }
}