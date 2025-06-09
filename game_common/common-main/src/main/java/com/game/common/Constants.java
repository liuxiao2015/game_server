package com.game.common;

/**
 * 全局常量定义类
 * 
 * 功能说明：
 * - 定义系统中使用的所有常量值
 * - 包含协议版本、默认配置、消息ID、错误码等
 * - 提供系统配置的中心化管理
 * - 避免魔法数字，提高代码可读性和维护性
 * 
 * 常量分类：
 * - 协议版本：定义通信协议的版本号
 * - 默认配置：服务器启动的默认参数设置
 * - 消息ID：客户端和服务器通信的消息类型标识
 * - 错误码：标准化的错误状态码定义
 * - 线程池名称：不同用途的线程池命名规范
 * 
 * 设计原则：
 * - 使用final修饰符确保常量不可变
 * - 按照功能分组组织常量，便于查找和维护
 * - 使用有意义的命名，体现常量的用途和含义
 * - 私有构造函数，防止实例化
 * 
 * 使用场景：
 * - 网络通信协议的参数配置
 * - 服务器启动时的默认设置
 * - 消息处理时的类型判断
 * - 异常处理时的错误码返回
 * - 线程池创建时的命名标识
 * 
 * 维护说明：
 * - 新增常量时应按照分类添加到对应区域
 * - 修改常量值需要考虑向后兼容性
 * - 废弃的常量应标记@Deprecated并说明替代方案
 *
 * @author lx
 * @date 2024-01-01
 */
public final class Constants {
    
    /**
     * 通信协议版本号
     * 用于客户端和服务器的版本兼容性检查
     */
    public static final String PROTOCOL_VERSION = "1.0.0";
    
    /**
     * 默认配置参数
     */
    // 服务器默认监听端口
    public static final int DEFAULT_PORT = 8888;
    // Netty Boss线程数，通常设置为1即可
    public static final int DEFAULT_BOSS_THREADS = 1;
    // Netty Worker线程数，默认为CPU核心数的2倍
    public static final int DEFAULT_WORKER_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    // 最大并发连接数限制
    public static final int DEFAULT_MAX_CONNECTIONS = 10000;
    // TCP连接队列大小
    public static final int DEFAULT_SO_BACKLOG = 1024;
    // 心跳间隔时间（毫秒）
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 30000; // 30 seconds
    // 心跳超时时间（毫秒）
    public static final int DEFAULT_HEARTBEAT_TIMEOUT = 90000;  // 90 seconds
    
    /**
     * 消息类型ID定义
     * 用于标识不同类型的网络消息
     */
    // 心跳请求消息
    public static final int MSG_HEARTBEAT_REQUEST = 1001;
    // 心跳响应消息
    public static final int MSG_HEARTBEAT_RESPONSE = 1002;
    // 登录请求消息
    public static final int MSG_LOGIN_REQUEST = 2001;
    // 登录响应消息
    public static final int MSG_LOGIN_RESPONSE = 2002;
    
    /**
     * 错误码定义
     * 用于标识不同类型的错误状态
     */
    // 操作成功
    public static final int SUCCESS = 0;
    // 未知错误
    public static final int ERROR_UNKNOWN = 1;
    // 无效请求参数
    public static final int ERROR_INVALID_REQUEST = 2;
    // 身份认证失败
    public static final int ERROR_AUTHENTICATION_FAILED = 3;
    // 权限不足
    public static final int ERROR_PERMISSION_DENIED = 4;
    // 资源未找到
    public static final int ERROR_RESOURCE_NOT_FOUND = 5;
    // 请求频率超限
    public static final int ERROR_RATE_LIMIT_EXCEEDED = 6;
    // 服务器内部错误
    public static final int ERROR_INTERNAL_SERVER_ERROR = 7;
    
    /**
     * 线程池命名规范
     * 用于标识不同用途的线程池
     */
    // Netty Boss线程池名称
    public static final String THREAD_POOL_BOSS = "NettyBoss";
    // Netty Worker线程池名称
    public static final String THREAD_POOL_WORKER = "NettyWorker";
    // 业务处理线程池名称
    public static final String THREAD_POOL_BUSINESS = "BusinessHandler";
    
    /**
     * 私有构造函数
     * 防止工具类被实例化
     */
    private Constants() {
        // Utility class, no instantiation
    }
}