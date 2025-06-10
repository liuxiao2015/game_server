package com.game.common;

/**
 * 游戏服务器统一响应结果封装类
 * 
 * 功能说明：
 * - 统一API响应格式，遵循RESTful设计规范
 * - 封装业务操作的成功/失败状态和结果数据
 * - 提供标准化的错误码和错误信息处理机制
 * - 支持泛型数据，适应不同业务场景的数据返回需求
 * 
 * 设计特点：
 * - 采用建造者模式，提供链式调用的便捷API
 * - 内置时间戳，便于客户端和服务端的时序处理
 * - 兼容前端框架的数据结构要求
 * - 支持序列化和反序列化，便于网络传输
 * 
 * 业务场景：
 * - 用户登录认证结果返回
 * - 游戏操作（背包、任务、战斗）结果封装
 * - 系统异常和业务异常的统一处理
 * - 批量操作结果的集合返回
 * 
 * 使用示例：
 * ```java
 * // 成功返回用户信息
 * Result<User> result = Result.success(user);
 * 
 * // 失败返回错误信息
 * Result<Void> error = Result.failure(ErrorCode.USER_NOT_FOUND, "用户不存在");
 * 
 * // 检查操作结果
 * if (result.isSuccess()) {
 *     User user = result.getData();
 *     // 处理业务逻辑
 * }
 * ```
 * 
 * 注意事项：
 * - 错误码应遵循系统统一的错误码规范
 * - 错误信息应该对用户友好，避免技术细节泄露
 * - 大数据量返回时考虑分页处理
 * - 敏感数据返回时需要进行脱敏处理
 *
 * @param <T> 响应数据的泛型类型
 * @author lx
 * @date 2024-01-01
 */
public class Result<T> {
    
    /** 响应状态码，0表示成功，非0表示各种错误情况 */
    private int code;
    
    /** 响应消息，成功时为"success"，失败时为具体错误描述 */
    private String message;
    
    /** 响应数据，具体的业务数据对象，可以为null */
    private T data;
    
    /** 响应时间戳，服务器生成响应的毫秒时间戳，用于客户端时序处理 */
    private long timestamp;
    
    /**
     * 默认构造函数
     * 
     * 功能说明：
     * - 初始化Result对象，自动设置当前时间戳
     * - 为后续的链式调用准备基础对象
     * 
     * 使用场景：
     * - 静态工厂方法内部调用
     * - 框架序列化时的对象重建
     */
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 创建包含数据的成功结果
     * 
     * 功能说明：
     * - 构造成功状态的响应对象，包含具体的业务数据
     * - 自动设置成功状态码和成功消息
     * - 适用于需要返回数据的业务操作
     * 
     * 业务场景：
     * - 用户登录成功，返回用户信息和Token
     * - 查询操作成功，返回查询结果
     * - 游戏操作成功，返回更新后的游戏状态
     * 
     * @param data 要返回的业务数据，可以是任意类型的对象
     * @param <T> 数据的泛型类型
     * @return 包含数据的成功结果对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = Constants.SUCCESS;
        result.message = "success";
        result.data = data;
        return result;
    }
    
    /**
     * 创建不包含数据的成功结果
     * 
     * 功能说明：
     * - 构造成功状态的响应对象，不包含具体数据
     * - 适用于操作型API，只需要确认操作成功状态
     * 
     * 业务场景：
     * - 删除操作成功确认
     * - 更新操作成功确认
     * - 配置修改成功确认
     * - 系统状态重置成功确认
     * 
     * @param <T> 数据的泛型类型
     * @return 不包含数据的成功结果对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 创建包含错误码和错误信息的失败结果
     * 
     * 功能说明：
     * - 构造失败状态的响应对象，包含具体的错误码和错误描述
     * - 遵循系统统一的错误码规范，便于客户端错误处理
     * - 自动将data设置为null，避免敏感信息泄露
     * 
     * 业务场景：
     * - 参数校验失败，返回具体的校验错误信息
     * - 业务规则验证失败，返回业务错误码和说明
     * - 系统异常，返回通用错误码和用户友好的错误信息
     * - 权限验证失败，返回权限相关的错误码
     * 
     * 注意事项：
     * - 错误信息应该对用户友好，避免暴露系统内部实现细节
     * - 错误码应该有统一的规范和文档说明
     * 
     * @param code 错误码，应遵循系统错误码规范
     * @param message 错误描述信息，应该对用户友好且便于理解
     * @param <T> 数据的泛型类型
     * @return 包含错误信息的失败结果对象
     */
    public static <T> Result<T> failure(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = null;
        return result;
    }
    
    /**
     * 从业务异常创建失败结果
     * 
     * 功能说明：
     * - 从BusinessException对象中提取错误码和错误信息
     * - 简化异常处理逻辑，统一异常到Result的转换
     * - 保持错误信息的一致性和规范性
     * 
     * 业务场景：
     * - 业务层抛出的已知业务异常
     * - 参数校验失败的异常处理
     * - 权限校验失败的异常处理
     * - 数据访问层的业务异常处理
     * 
     * @param e 业务异常对象，包含错误码和错误信息
     * @param <T> 数据的泛型类型
     * @return 从异常转换的失败结果对象
     */
    public static <T> Result<T> failure(BusinessException e) {
        return failure(e.getCode(), e.getMessage());
    }
    
    /**
     * 检查操作结果是否成功
     * 
     * 功能说明：
     * - 通过判断状态码是否为成功码来确定操作是否成功
     * - 提供简便的成功状态判断方法，避免直接比较状态码
     * 
     * 业务场景：
     * - 调用方法后的结果状态检查
     * - 条件分支处理的判断依据
     * - 日志记录和监控统计的状态判断
     * 
     * @return true表示操作成功，false表示操作失败
     */
    public boolean isSuccess() {
        return code == Constants.SUCCESS;
    }
    
    /**
     * 获取响应状态码
     * 
     * @return 状态码，0表示成功，非0表示不同类型的错误
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 设置响应状态码
     * 
     * @param code 状态码值
     */
    public void setCode(int code) {
        this.code = code;
    }
    
    /**
     * 获取响应消息
     * 
     * @return 响应消息字符串
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 设置响应消息
     * 
     * @param message 响应消息内容
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * 获取响应数据
     * 
     * @return 响应的业务数据对象
     */
    public T getData() {
        return data;
    }
    
    /**
     * 设置响应数据
     * 
     * @param data 要设置的业务数据对象
     */
    public void setData(T data) {
        this.data = data;
    }
    
    /**
     * 获取响应时间戳
     * 
     * @return 响应生成的时间戳（毫秒）
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * 设置响应时间戳
     * 
     * @param timestamp 时间戳值（毫秒）
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 返回对象的字符串表示
     * 
     * 功能说明：
     * - 提供Result对象的可读字符串表示
     * - 包含所有重要字段信息，便于日志记录和调试
     * - 遵循Java标准的toString格式规范
     * 
     * @return 包含所有字段信息的字符串表示
     */
    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}