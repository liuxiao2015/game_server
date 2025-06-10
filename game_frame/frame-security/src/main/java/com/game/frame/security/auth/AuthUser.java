package com.game.frame.security.auth;

/**
 * 认证用户信息实体类
 * 
 * 功能说明：
 * - 封装已认证用户的会话信息和权限数据
 * - 在用户认证成功后维护用户的身份状态
 * - 提供权限验证和会话管理的核心数据结构
 * - 支持基于角色的访问控制（RBAC）和细粒度权限管理
 * 
 * 设计思路：
 * - 采用简洁的POJO设计，便于序列化和网络传输
 * - 包含用户身份、会话、权限、角色等认证核心要素
 * - 记录登录时间和IP，便于安全审计和异常检测
 * - 支持权限和角色的数组形式存储，便于快速权限检查
 * 
 * 使用场景：
 * - Spring Security认证成功后的用户主体信息
 * - JWT Token中携带的用户身份声明
 * - 会话存储中保存的用户状态信息
 * - 权限拦截器中的权限验证数据源
 * 
 * 安全特性：
 * - 不存储明文密码，保护用户隐私
 * - 包含会话ID，支持会话失效和并发控制
 * - 记录登录时间和IP，便于异常登录检测
 * - 支持权限动态刷新，满足实时权限控制需求
 * 
 * 数据流转：
 * - 认证阶段：用户登录成功后创建AuthUser实例
 * - 会话维护：存储在Redis或内存中维护用户状态
 * - 权限检查：在业务操作前验证用户权限
 * - 注销清理：用户登出时清除相关认证信息
 *
 * @author lx
 * @date 2025/06/08
 */
public class AuthUser {
    
    /** 用户唯一标识符，关联用户基础信息和业务数据 */
    private Long userId;
    
    /** 用户名，通常为登录账号，便于显示和日志记录 */
    private String username;
    
    /** 会话标识符，用于会话管理和并发控制 */
    private String sessionId;
    
    /** 用户权限列表，存储具体的功能权限代码 */
    private String[] permissions;
    
    /** 用户角色列表，存储角色标识，用于基于角色的权限管理 */
    private String[] roles;
    
    /** 登录时间戳（毫秒），用于会话超时检查和安全审计 */
    private Long loginTime;
    
    /** 登录IP地址，用于安全监控和异地登录检测 */
    private String loginIp;

    /**
     * 默认构造函数
     * 
     * 功能说明：
     * - 创建空的认证用户实例，用于框架初始化和序列化场景
     * - 所有字段保持默认值，由后续的setter方法填充数据
     * 
     * 使用场景：
     * - Spring框架的Bean创建和依赖注入
     * - JSON反序列化时的对象实例化
     * - 手动构建用户信息的起始点
     */
    public AuthUser() {}

    /**
     * 带基础参数的构造函数
     * 
     * 功能说明：
     * - 创建包含核心认证信息的用户实例
     * - 自动设置登录时间为当前时间戳
     * - 适用于用户认证成功后快速创建用户对象
     * 
     * 参数说明：
     * - userId：用户的唯一标识，通常为数据库主键
     * - username：用户登录名，便于显示和日志记录
     * - sessionId：会话标识，用于会话管理和安全控制
     * 
     * 自动设置：
     * - loginTime：设置为当前系统时间戳，记录认证时刻
     * 
     * 使用场景：
     * - 用户登录成功后创建认证用户对象
     * - JWT Token生成时构建用户声明信息
     * - 会话管理中创建用户状态实例
     * 
     * @param userId 用户唯一标识符，不应为null
     * @param username 用户名，应为有效的登录账号
     * @param sessionId 会话标识符，用于会话追踪和管理
     */
    public AuthUser(Long userId, String username, String sessionId) {
        this.userId = userId;
        this.username = username;
        this.sessionId = sessionId;
        // 自动记录登录时间，便于会话超时检查和审计
        this.loginTime = System.currentTimeMillis();
    }

    // ========== 访问器方法（Getters and Setters） ==========
    
    /**
     * 获取用户ID
     * @return 用户唯一标识符
     */
    public Long getUserId() { return userId; }
    
    /**
     * 设置用户ID
     * @param userId 用户唯一标识符，应与用户表主键对应
     */
    public void setUserId(Long userId) { this.userId = userId; }
    
    /**
     * 获取用户名
     * @return 用户登录名或显示名称
     */
    public String getUsername() { return username; }
    
    /**
     * 设置用户名
     * @param username 用户名，用于界面显示和日志记录
     */
    public void setUsername(String username) { this.username = username; }
    
    /**
     * 获取会话ID
     * @return 当前会话的唯一标识符
     */
    public String getSessionId() { return sessionId; }
    
    /**
     * 设置会话ID
     * @param sessionId 会话标识符，用于会话追踪和并发控制
     */
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    /**
     * 获取用户权限列表
     * @return 权限代码数组，包含用户拥有的所有功能权限
     */
    public String[] getPermissions() { return permissions; }
    
    /**
     * 设置用户权限列表
     * @param permissions 权限数组，应包含具体的权限代码
     */
    public void setPermissions(String[] permissions) { this.permissions = permissions; }
    
    /**
     * 获取用户角色列表
     * @return 角色标识数组，用于基于角色的权限管理
     */
    public String[] getRoles() { return roles; }
    
    /**
     * 设置用户角色列表
     * @param roles 角色数组，包含用户所属的所有角色
     */
    public void setRoles(String[] roles) { this.roles = roles; }
    
    /**
     * 获取登录时间
     * @return 用户登录的时间戳（毫秒）
     */
    public Long getLoginTime() { return loginTime; }
    
    /**
     * 设置登录时间
     * @param loginTime 登录时间戳，用于会话超时检查
     */
    public void setLoginTime(Long loginTime) { this.loginTime = loginTime; }
    
    /**
     * 获取登录IP地址
     * @return 用户登录时的客户端IP地址
     */
    public String getLoginIp() { return loginIp; }
    
    /**
     * 设置登录IP地址
     * @param loginIp 客户端IP，用于安全监控和异地登录检测
     */
    public void setLoginIp(String loginIp) { this.loginIp = loginIp; }
    
    /**
     * 返回认证用户的字符串表示
     * 
     * 功能说明：
     * - 提供用户对象的简洁描述，便于日志记录和调试
     * - 包含关键的身份信息，但不暴露敏感数据
     * - 用于系统日志、调试输出和简单的状态显示
     * 
     * 包含信息：
     * - userId：用户标识
     * - username：用户名
     * - sessionId：会话标识
     * - loginTime：登录时间
     * - loginIp：登录IP
     * 
     * 安全考虑：
     * - 不包含权限和角色信息，避免敏感信息泄露
     * - 适合在日志中记录，便于问题排查
     * 
     * @return 用户对象的字符串描述
     */
    @Override
    public String toString() {
        return "AuthUser{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", loginTime=" + loginTime +
                ", loginIp='" + loginIp + '\'' +
                '}';
    }
}