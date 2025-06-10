package com.game.frame.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 数据访问层基础Repository接口
 * 
 * 功能说明：
 * - 提供通用的数据访问方法，包括基础的CRUD操作和逻辑删除功能
 * - 扩展Spring Data JPA的JpaRepository，添加游戏业务特定的数据操作方法
 * - 支持逻辑删除机制，避免物理删除带来的数据丢失风险
 * - 作为所有具体Repository接口的父接口，确保API一致性
 * 
 * 设计思路：
 * - 使用@NoRepositoryBean注解防止Spring自动实现此接口
 * - 继承JpaRepository获得完整的JPA数据访问能力
 * - 添加deleted字段支持，实现软删除功能
 * - 泛型设计支持不同的实体类型和主键类型
 * 
 * 使用场景：
 * - 游戏用户数据的安全删除，保留用户历史记录用于数据分析
 * - 游戏配置数据的版本管理，支持配置回滚和历史查询
 * - 游戏物品和装备的删除恢复，避免误删除造成的用户投诉
 * - 审计和合规要求，保留完整的数据变更历史
 * 
 * 技术特点：
 * - 逻辑删除：通过deleted字段标记删除状态，不物理删除数据
 * - 批量操作：支持批量逻辑删除，提升操作效率
 * - 类型安全：泛型约束确保类型安全
 * - 扩展性强：便于添加新的通用数据操作方法
 * 
 * 逻辑删除机制：
 * - deleted=0: 正常数据，未删除状态
 * - deleted=1: 已删除数据，逻辑删除状态
 * - 查询时需要显式指定deleted状态，避免查询到已删除数据
 * - 支持单个和批量逻辑删除操作
 * 
 * @param <T> 实体类型，必须是数据库实体类
 * @param <ID> 主键类型，必须实现Serializable接口
 * 
 * @author lx
 * @date 2025/06/08
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 根据ID查找未删除的实体
     * 
     * 功能说明：
     * - 根据主键ID查找指定删除状态的实体
     * - 主要用于查找未删除(deleted=0)的有效数据
     * - 返回Optional类型，避免空指针异常
     * 
     * 业务逻辑：
     * - 根据主键ID和删除标记查询实体
     * - 通常deleted=0表示正常数据，deleted=1表示已删除
     * - 支持灵活的删除状态查询
     * 
     * @param id 实体主键ID
     * @param deleted 删除状态标记，0-未删除，1-已删除
     * @return Optional<T> 查询结果，可能为空
     */
    Optional<T> findByIdAndDeletedEquals(ID id, Integer deleted);

    /**
     * 查找所有指定删除状态的实体
     * 
     * 功能说明：
     * - 查询所有符合删除状态条件的实体列表
     * - 主要用于获取所有有效数据或已删除数据
     * - 返回List集合，空集合表示无匹配数据
     * 
     * 业务逻辑：
     * - 根据删除标记筛选实体数据
     * - 支持查询有效数据列表或回收站数据列表
     * - 不进行分页处理，适合数据量较小的场景
     * 
     * 注意事项：
     * - 数据量大时建议使用分页查询方法
     * - 考虑添加其他筛选条件避免返回过多数据
     * 
     * @param deleted 删除状态标记，0-未删除，1-已删除
     * @return List<T> 实体列表，可能为空集合
     */
    List<T> findByDeletedEquals(Integer deleted);

    /**
     * 逻辑删除单个实体
     * 
     * 功能说明：
     * - 根据主键ID将实体标记为已删除状态
     * - 不进行物理删除，只更新deleted字段值
     * - 支持数据恢复和审计追踪
     * 
     * 业务逻辑：
     * 1. 根据主键ID定位目标实体
     * 2. 更新实体的deleted字段为指定值
     * 3. 返回受影响的行数，用于验证操作结果
     * 
     * 使用场景：
     * - 用户删除游戏角色或物品
     * - 管理员下架游戏配置或内容
     * - 系统自动清理过期数据
     * 
     * @param id 要删除的实体主键ID
     * @param deleted 删除状态值，通常为1表示已删除
     * @return int 受影响的行数，1表示删除成功，0表示实体不存在
     */
    int updateDeletedById(ID id, Integer deleted);

    /**
     * 批量逻辑删除实体
     * 
     * 功能说明：
     * - 根据主键ID列表批量将实体标记为已删除状态
     * - 提供高效的批量删除操作，避免多次数据库访问
     * - 支持事务性批量操作，保证数据一致性
     * 
     * 业务逻辑：
     * 1. 遍历主键ID列表
     * 2. 批量更新所有匹配实体的deleted字段
     * 3. 返回总的受影响行数
     * 
     * 使用场景：
     * - 批量清理过期的游戏数据
     * - 管理员批量下架违规内容
     * - 用户批量删除游戏物品
     * - 定时任务批量归档历史数据
     * 
     * 性能优化：
     * - 使用IN查询一次性更新多条记录
     * - 避免N+1问题，减少数据库访问次数
     * - 支持事务回滚，确保操作的原子性
     * 
     * @param ids 要删除的实体主键ID列表
     * @param deleted 删除状态值，通常为1表示已删除
     * @return int 受影响的总行数，表示成功删除的实体数量
     */
    int updateDeletedByIdIn(List<ID> ids, Integer deleted);
}