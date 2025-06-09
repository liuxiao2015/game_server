package com.game.common.api.service;

import com.game.common.model.request.UserLoginRequest;
import com.game.common.model.response.UserLoginResponse;
import com.game.common.model.response.Result;

/**
 * 用户服务接口
 * 
 * 功能说明：
 * - 提供用户登录认证功能，支持账号密码验证
 * - 提供用户信息查询和更新操作
 * - 支持用户账号存在性检查
 * - 实现用户数据的基础CRUD操作
 * 
 * 设计思路：
 * - 定义统一的用户服务接口规范，便于不同实现的切换
 * - 使用Result封装返回结果，提供统一的错误处理机制
 * - 分离用户认证和用户信息管理职责
 * 
 * 使用场景：
 * - 客户端登录时的用户认证
 * - 游戏内用户信息展示和修改
 * - 用户数据的持久化管理
 * - 分布式服务间的用户信息共享
 *
 * @author lx
 * @date 2024-01-01
 */
public interface IUserService {

    /**
     * 用户登录认证
     * 
     * 业务逻辑：
     * 1. 验证用户账号密码的正确性
     * 2. 检查用户账号状态（是否被禁用、锁定等）
     * 3. 生成用户会话Token用于后续认证
     * 4. 更新用户最后登录时间和IP信息
     * 5. 返回用户基础信息和认证凭据
     * 
     * @param request 登录请求对象，包含账号、密码、设备信息等
     * @return 登录响应，包含用户信息、Token、会话ID等认证数据
     * 
     * 注意事项：
     * - 密码验证失败会记录失败次数，防止暴力破解
     * - Token具有过期时间，需要定期刷新
     * - 支持同一账号多设备登录的业务场景
     */
    UserLoginResponse login(UserLoginRequest request);

    /**
     * 根据用户ID获取用户信息
     * 
     * 业务逻辑：
     * 1. 验证用户ID的有效性
     * 2. 从缓存或数据库查询用户详细信息
     * 3. 过滤敏感信息（如密码、支付密码等）
     * 4. 返回用户的公开信息
     * 
     * @param userId 用户唯一标识ID
     * @return 用户信息对象，包含昵称、头像、等级、经验等基础数据
     * 
     * 注意事项：
     * - 不返回用户的敏感信息如密码、手机号等
     * - 支持用户信息的缓存机制，提高查询性能
     */
    Result<com.game.common.model.entity.User> getUserInfo(Long userId);

    /**
     * 更新用户信息
     * 
     * 业务逻辑：
     * 1. 验证用户身份和权限
     * 2. 校验新昵称的合法性（长度、敏感词等）
     * 3. 验证头像URL的有效性和格式
     * 4. 更新用户信息到数据库
     * 5. 清除相关缓存，确保数据一致性
     * 
     * @param userId 要更新的用户ID
     * @param nickname 新的用户昵称，需符合平台昵称规范
     * @param avatar 新的用户头像URL，需为有效的图片链接
     * @return 更新操作的结果，成功或失败信息
     * 
     * 注意事项：
     * - 昵称修改可能有频率限制和费用
     * - 头像需要经过内容审核
     * - 更新后需要通知相关模块刷新用户信息
     */
    Result<Void> updateUserInfo(Long userId, String nickname, String avatar);

    /**
     * 检查用户账号是否存在
     * 
     * 业务逻辑：
     * 1. 在用户数据库中查询指定账号
     * 2. 考虑账号的各种状态（正常、注销、冻结等）
     * 3. 返回账号存在性的布尔结果
     * 
     * @param account 要检查的用户账号
     * @return 账号存在性检查结果，true表示账号已存在
     * 
     * 使用场景：
     * - 新用户注册时的账号重复性检查
     * - 登录前的账号有效性预检查
     * - 找回密码时的账号验证
     */
    Result<Boolean> userExists(String account);

    /**
     * 根据账号获取用户信息
     * 
     * 业务逻辑：
     * 1. 使用账号作为查询条件检索用户数据
     * 2. 验证账号格式的有效性
     * 3. 从数据库或缓存中获取完整用户信息
     * 4. 处理账号不存在的异常情况
     * 
     * @param account 用户账号，可以是用户名、邮箱、手机号等
     * @return 用户实体对象，包含用户的详细信息
     * 
     * 使用场景：
     * - 登录时根据账号查询用户信息
     * - 管理后台根据账号查询用户详情
     * - 用户信息关联查询和数据迁移
     * 
     * 注意事项：
     * - 账号查询需要考虑大小写敏感性
     * - 返回结果不包含密码等敏感信息
     */
    Result<com.game.common.model.entity.User> getUserByAccount(String account);
}