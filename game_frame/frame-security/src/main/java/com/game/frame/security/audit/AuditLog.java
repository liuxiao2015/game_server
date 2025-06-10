package com.game.frame.security.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志实体类
 * 
 * 功能说明：
 * - 记录系统中所有重要操作的详细信息，用于安全审计和合规检查
 * - 支持用户行为追踪、安全事件监控和操作回溯分析
 * - 提供完整的操作上下文信息，包括用户身份、操作时间、结果状态等
 * - 符合安全合规要求，支持审计报告生成和问题排查
 * 
 * 设计思路：
 * - 采用结构化数据模型，便于检索和分析
 * - 包含HTTP请求相关信息，支持Web应用审计
 * - 记录操作执行时长，便于性能分析和异常检测
 * - 支持扩展字段存储，适应不同业务场景的审计需求
 * 
 * 应用场景：
 * - 用户登录登出、权限变更等身份认证操作审计
 * - 敏感数据访问、修改、删除等数据操作审计
 * - 系统配置变更、管理员操作等管理行为审计
 * - 安全事件记录、异常行为监控等安全审计
 * 
 * 数据存储：
 * - 支持数据库持久化存储，便于长期保存和查询
 * - 可集成ELK、MongoDB等日志分析系统
 * - 支持数据归档和清理策略，控制存储成本
 * 
 * 安全特性：
 * - 审计日志本身不可篡改，确保审计数据的完整性
 * - 支持日志加密存储，保护敏感审计信息
 * - 提供数字签名验证，防止审计数据伪造
 *
 * @author lx
 * @date 2025/06/08
 */
public class AuditLog {
    
    /** 审计日志唯一标识符，用于日志检索和关联分析 */
    private String id;
    
    /** 操作用户ID，关联具体的用户身份信息 */
    private String userId;
    
    /** 操作用户名，便于审计报告中的可读性显示 */
    private String username;
    
    /** 操作类型，如LOGIN（登录）、UPDATE（更新）、DELETE（删除）等 */
    private String action;
    
    /** 资源标识，标明操作涉及的具体资源或模块 */
    private String resource;
    
    /** HTTP请求方法，如GET、POST、PUT、DELETE等 */
    private String method;
    
    /** 请求路径，记录具体的API端点或页面路径 */
    private String path;
    
    /** 客户端IP地址，用于地理位置分析和异常检测 */
    private String ip;
    
    /** 用户代理字符串，包含浏览器和操作系统信息 */
    private String userAgent;
    
    /** 操作发生时间，精确到秒，用于时序分析 */
    private LocalDateTime timestamp;
    
    /** 操作状态，SUCCESS（成功）、FAILED（失败）、ERROR（错误） */
    private String status;
    
    /** 错误信息描述，操作失败时记录具体错误原因 */
    private String errorMessage;
    
    /** 操作详细信息，存储额外的上下文数据和参数信息 */
    private Map<String, Object> details;
    
    /** 操作执行时长（毫秒），用于性能分析和异常检测 */
    private Long duration;

    /**
     * 默认构造函数
     * 
     * 功能说明：
     * - 创建新的审计日志实例，初始化默认值
     * - 自动设置操作时间为当前系统时间
     * - 默认操作状态为成功，避免遗漏状态设置
     * 
     * 初始化逻辑：
     * - timestamp：设置为当前LocalDateTime，确保时间准确性
     * - status：默认为SUCCESS，可后续根据实际情况修改
     * - 其他字段保持null，由具体业务逻辑填充
     * 
     * 使用场景：
     * - Spring框架的Bean创建和序列化场景
     * - 手动创建审计日志对象的起始点
     * - 日志工厂类中的对象初始化
     */
    public AuditLog() {
        // 设置操作时间为当前时间，确保审计日志的时间准确性
        this.timestamp = LocalDateTime.now();
        // 默认状态为成功，业务处理中可根据实际情况修改
        this.status = "SUCCESS";
    }

    /**
     * 带基础参数的构造函数
     * 
     * 功能说明：
     * - 创建包含核心业务信息的审计日志实例
     * - 调用默认构造函数完成基础初始化
     * - 设置最关键的三个审计要素：用户、操作、资源
     * 
     * 参数设计：
     * - userId：操作执行者，是审计的核心要素
     * - action：具体操作类型，描述用户做了什么
     * - resource：操作目标，标识影响的资源或数据
     * 
     * 使用场景：
     * - 业务层快速创建审计日志的常用方式
     * - AOP切面中自动生成审计记录
     * - 手动埋点记录关键业务操作
     * 
     * @param userId 操作用户ID，不应为空
     * @param action 操作类型，应使用标准化的操作名称
     * @param resource 资源标识，应包含足够的上下文信息
     */
    public AuditLog(String userId, String action, String resource) {
        // 调用默认构造函数完成基础字段初始化
        this();
        this.userId = userId;
        this.action = action;
        this.resource = resource;
    }

    // ========== 访问器方法（Getters and Setters） ==========
    
    /**
     * 获取审计日志ID
     * @return 日志唯一标识符
     */
    public String getId() { return id; }
    
    /**
     * 设置审计日志ID
     * @param id 日志唯一标识符，通常由数据库或UUID生成
     */
    public void setId(String id) { this.id = id; }

    /**
     * 获取操作用户ID
     * @return 执行操作的用户标识
     */
    public String getUserId() { return userId; }
    
    /**
     * 设置操作用户ID
     * @param userId 用户标识，用于关联用户信息和权限验证
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * 获取操作用户名
     * @return 用户名，便于审计报告的可读性
     */
    public String getUsername() { return username; }
    
    /**
     * 设置操作用户名
     * @param username 用户名，应与userId对应，便于人工审计
     */
    public void setUsername(String username) { this.username = username; }

    /**
     * 获取操作类型
     * @return 操作行为标识，如LOGIN、UPDATE、DELETE等
     */
    public String getAction() { return action; }
    
    /**
     * 设置操作类型
     * @param action 操作行为，建议使用标准化的操作代码
     */
    public void setAction(String action) { this.action = action; }

    /**
     * 获取资源标识
     * @return 操作涉及的资源或模块名称
     */
    public String getResource() { return resource; }
    
    /**
     * 设置资源标识
     * @param resource 资源名称，应包含足够的定位信息
     */
    public void setResource(String resource) { this.resource = resource; }

    /**
     * 获取HTTP请求方法
     * @return HTTP方法，如GET、POST、PUT、DELETE等
     */
    public String getMethod() { return method; }
    
    /**
     * 设置HTTP请求方法
     * @param method HTTP方法，用于Web请求的审计追踪
     */
    public void setMethod(String method) { this.method = method; }

    /**
     * 获取请求路径
     * @return 请求的URL路径或API端点
     */
    public String getPath() { return path; }
    
    /**
     * 设置请求路径
     * @param path URL路径，便于定位具体的操作接口
     */
    public void setPath(String path) { this.path = path; }

    /**
     * 获取客户端IP地址
     * @return 发起操作的客户端IP
     */
    public String getIp() { return ip; }
    
    /**
     * 设置客户端IP地址
     * @param ip 客户端IP，用于地理位置分析和安全检测
     */
    public void setIp(String ip) { this.ip = ip; }

    /**
     * 获取用户代理字符串
     * @return 客户端浏览器或应用的标识信息
     */
    public String getUserAgent() { return userAgent; }
    
    /**
     * 设置用户代理字符串
     * @param userAgent 客户端标识，包含浏览器、操作系统等信息
     */
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    /**
     * 获取操作时间戳
     * @return 操作发生的精确时间
     */
    public LocalDateTime getTimestamp() { return timestamp; }
    
    /**
     * 设置操作时间戳
     * @param timestamp 操作时间，应确保时区正确性
     */
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    /**
     * 获取操作状态
     * @return 操作结果状态，SUCCESS/FAILED/ERROR
     */
    public String getStatus() { return status; }
    
    /**
     * 设置操作状态
     * @param status 操作结果，用于区分成功和各类失败情况
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * 获取错误信息
     * @return 操作失败时的详细错误描述
     */
    public String getErrorMessage() { return errorMessage; }
    
    /**
     * 设置错误信息
     * @param errorMessage 错误描述，应包含足够的问题定位信息
     */
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    /**
     * 获取操作详细信息
     * @return 扩展信息映射，包含操作参数和上下文数据
     */
    public Map<String, Object> getDetails() { return details; }
    
    /**
     * 设置操作详细信息
     * @param details 扩展信息，可存储请求参数、响应数据等
     */
    public void setDetails(Map<String, Object> details) { this.details = details; }

    /**
     * 获取操作执行时长
     * @return 操作耗时（毫秒），用于性能分析
     */
    public Long getDuration() { return duration; }
    
    /**
     * 设置操作执行时长
     * @param duration 执行耗时（毫秒），便于识别异常缓慢的操作
     */
    public void setDuration(Long duration) { this.duration = duration; }

    /**
     * 返回审计日志的字符串表示
     * 
     * 功能说明：
     * - 提供简洁的日志摘要信息，便于快速查看关键字段
     * - 用于日志输出、调试信息和简单的文本展示
     * - 不包含敏感信息，确保日志安全性
     * 
     * 包含字段：
     * - id：日志标识
     * - userId：操作用户
     * - action：操作类型
     * - resource：操作资源
     * - ip：客户端IP
     * - status：操作状态
     * - timestamp：操作时间
     * 
     * @return 日志对象的字符串描述
     */
    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", action='" + action + '\'' +
                ", resource='" + resource + '\'' +
                ", ip='" + ip + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}