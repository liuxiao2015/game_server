package com.game.common.api.service;

import com.game.common.model.entity.Session;
import com.game.common.model.response.Result;

/**
 * 会话服务接口
 * 
 * 功能说明：
 * - 管理用户登录会话的完整生命周期
 * - 提供会话创建、验证、刷新、销毁等核心功能
 * - 支持多设备登录和会话隔离机制
 * - 实现分布式环境下的会话一致性管理
 * 
 * 设计思路：
 * - 采用Token-based的无状态会话管理机制
 * - 支持会话的自动过期和手动销毁
 * - 提供设备级别的会话管理和安全控制
 * - 集成Redis等分布式缓存实现会话共享
 * 
 * 使用场景：
 * - 用户登录后的会话状态管理
 * - 接口调用时的身份认证和授权
 * - 用户强制下线和会话清理
 * - 多端同步和设备管理
 * 
 * 安全特性：
 * - Token具有时效性，防止永久有效的安全风险
 * - 支持IP绑定，防止Token被盗用
 * - 记录设备信息，支持异常登录检测
 *
 * @author lx
 * @date 2024-01-01
 */
public interface ISessionService {

    /**
     * 创建新的用户会话
     * 
     * 业务逻辑：
     * 1. 生成唯一的会话Token和会话ID
     * 2. 记录用户设备信息和登录环境
     * 3. 设置会话过期时间和安全策略
     * 4. 将会话信息存储到分布式缓存中
     * 5. 返回包含认证凭据的会话对象
     * 
     * @param userId 用户ID，用于关联会话和用户账号
     * @param deviceId 设备唯一标识，用于多设备管理和异常检测
     * @param clientIp 客户端IP地址，用于安全验证和地域分析
     * @param userAgent 用户代理字符串，记录客户端类型和版本信息
     * @return 会话对象，包含Token、过期时间、设备绑定等信息
     * 
     * 注意事项：
     * - 同一用户可以有多个有效会话（多设备登录）
     * - 会话Token具有防伪造和防篡改特性
     * - IP地址变化可能触发安全验证流程
     */
    Result<Session> createSession(Long userId, String deviceId, String clientIp, String userAgent);

    /**
     * 验证会话Token的有效性
     * 
     * 业务逻辑：
     * 1. 检查Token格式的合法性和完整性
     * 2. 从缓存中查询对应的会话信息
     * 3. 验证会话是否已过期或被销毁
     * 4. 检查IP地址等安全限制条件
     * 5. 更新会话的最后活跃时间
     * 
     * @param token 会话令牌，用于身份验证和会话识别
     * @return 有效会话对象；如果Token无效或已过期则返回null
     * 
     * 验证规则：
     * - Token必须在有效期内
     * - 会话未被主动销毁
     * - 设备信息和IP地址符合安全策略
     * - 用户账号状态正常（未被禁用或冻结）
     * 
     * 性能考虑：
     * - 支持会话信息缓存，减少数据库查询
     * - 采用异步方式更新活跃时间，避免阻塞验证流程
     */
    Result<Session> validateSession(String token);

    /**
     * 销毁指定Token的会话
     * 
     * 业务逻辑：
     * 1. 验证Token的有效性和存在性
     * 2. 从缓存和数据库中删除会话记录
     * 3. 清理相关的认证状态和权限缓存
     * 4. 记录会话销毁的审计日志
     * 5. 通知其他服务会话已失效
     * 
     * @param token 要销毁的会话令牌
     * @return 销毁操作的执行结果
     * 
     * 使用场景：
     * - 用户主动登出
     * - 管理员强制下线用户
     * - 检测到异常登录时的安全处理
     * - 会话过期或安全策略变更时的清理
     */
    Result<Void> destroySession(String token);

    /**
     * 销毁指定用户的所有会话
     * 
     * 业务逻辑：
     * 1. 查找用户所有有效的会话记录
     * 2. 批量删除会话Token和相关数据
     * 3. 清理用户相关的所有认证缓存
     * 4. 通知所有相关服务用户会话已全部失效
     * 
     * @param userId 用户ID
     * @return 批量销毁操作的执行结果
     * 
     * 使用场景：
     * - 用户账号被冻结或禁用
     * - 用户修改密码后强制重新登录
     * - 检测到账号安全风险时的保护措施
     * - 用户注销账号时的数据清理
     */
    Result<Void> destroyUserSessions(Long userId);

    /**
     * 刷新会话过期时间
     * 
     * 业务逻辑：
     * 1. 验证当前会话的有效性
     * 2. 计算新的过期时间点
     * 3. 更新缓存中的会话过期信息
     * 4. 生成新的Token（可选，用于更高安全性）
     * 5. 返回更新后的会话信息
     * 
     * @param token 当前会话令牌
     * @param extendSeconds 延长的秒数，必须为正数
     * @return 刷新后的会话对象，包含新的过期时间
     * 
     * 安全考虑：
     * - 延长时间不能超过系统设定的最大会话时长
     * - 频繁刷新可能触发安全监控机制
     * - 支持配置是否在刷新时生成新Token
     */
    Result<Session> refreshSession(String token, int extendSeconds);

    /**
     * 根据Token获取会话信息
     * 
     * 业务逻辑：
     * 1. 解析Token并验证其格式正确性
     * 2. 从缓存或数据库查询会话详细信息
     * 3. 返回完整的会话对象数据
     * 4. 不更新会话的活跃状态（只读操作）
     * 
     * @param token 会话令牌
     * @return 会话实体对象，包含用户ID、设备信息、创建时间等
     * 
     * 使用场景：
     * - 获取当前登录用户的详细信息
     * - 分析用户的登录设备和地域分布
     * - 会话管理和监控功能
     * - 审计和安全分析需求
     */
    Result<Session> getSession(String token);
}