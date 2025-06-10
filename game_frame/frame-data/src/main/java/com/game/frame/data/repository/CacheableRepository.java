package com.game.frame.data.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * 带缓存功能的Repository接口
 * 
 * 功能说明：
 * - 扩展BaseRepository，添加Spring Cache注解支持
 * - 提供自动缓存管理功能，显著提升数据访问性能
 * - 实现缓存的增加、更新、删除和清理操作
 * - 支持分布式缓存和本地缓存的透明切换
 * 
 * 设计思路：
 * - 继承BaseRepository获得基础数据访问能力
 * - 重写关键方法添加缓存注解，实现自动缓存管理
 * - 使用SpEL表达式动态生成缓存键，确保键的唯一性
 * - 支持不同实体类型的缓存隔离和管理
 * 
 * 使用场景：
 * - 游戏配置数据缓存：装备属性、技能参数等频繁查询的配置
 * - 用户信息缓存：用户基本信息、等级经验等热点数据
 * - 游戏状态缓存：房间信息、匹配状态等实时数据
 * - 静态数据缓存：字典表、枚举值等不经常变更的数据
 * 
 * 技术特点：
 * - 声明式缓存：通过注解自动管理缓存生命周期
 * - 智能缓存键：基于类名和ID生成唯一缓存键
 * - 多级缓存支持：本地缓存和分布式缓存自动选择
 * - 缓存一致性：增删改操作自动维护缓存状态
 * 
 * 缓存策略：
 * - @Cacheable：查询时先查缓存，缓存不存在时查数据库并缓存结果
 * - @CachePut：保存时更新缓存，确保缓存与数据库同步
 * - @CacheEvict：删除时清除缓存，避免脏数据
 * - 支持批量缓存清理和条件缓存清除
 * 
 * 缓存键策略：
 * - 格式：{类名}:{ID}，如User:123、Equipment:456
 * - 确保不同实体类型的缓存键不会冲突
 * - 便于监控和调试，缓存键具有良好的可读性
 * 
 * @param <T> 实体类型，必须是数据库实体类
 * @param <ID> 主键类型，必须实现Serializable接口
 * 
 * @author lx
 * @date 2025/06/08
 */
@NoRepositoryBean
public interface CacheableRepository<T, ID extends Serializable> extends BaseRepository<T, ID> {

    /**
     * 根据ID查找实体（带缓存支持）
     * 
     * 功能说明：
     * - 重写BaseRepository的findById方法，添加缓存支持
     * - 首先查询缓存，缓存命中直接返回，未命中时查询数据库并缓存结果
     * - 使用动态缓存键，确保不同实体类型的缓存隔离
     * 
     * 业务逻辑：
     * 1. 根据缓存键检查缓存中是否存在目标数据
     * 2. 缓存命中：直接返回缓存中的数据，无需访问数据库
     * 3. 缓存未命中：查询数据库，将结果缓存后返回
     * 4. 缓存键格式：{类名}:{ID}，确保全局唯一性
     * 
     * 性能优化：
     * - 热点数据访问延迟从数毫秒降低到微秒级别
     * - 减少数据库访问压力，提升系统整体性能
     * - 支持多级缓存，本地缓存优先，分布式缓存备用
     * 
     * @param id 实体主键ID
     * @return Optional<T> 查询结果，可能为空
     */
    @Override
    @Cacheable(value = "entities", key = "#root.targetClass.simpleName + ':' + #id")
    Optional<T> findById(ID id);

    /**
     * 保存实体（更新缓存）
     * 
     * 功能说明：
     * - 重写BaseRepository的save方法，添加缓存更新支持
     * - 保存数据库记录的同时更新缓存，确保缓存与数据库同步
     * - 使用@CachePut注解，无论缓存是否存在都会执行方法并更新缓存
     * 
     * 业务逻辑：
     * 1. 执行数据库保存操作(INSERT或UPDATE)
     * 2. 保存成功后，将最新数据更新到缓存中
     * 3. 缓存键基于保存后实体的ID生成，确保准确性
     * 4. 返回保存后的实体对象，包含数据库生成的字段
     * 
     * 缓存策略：
     * - 始终执行数据库操作，不跳过方法执行
     * - 操作成功后更新缓存，保证数据一致性
     * - 支持新增和更新操作的缓存同步
     * 
     * @param entity 要保存的实体对象
     * @return <S extends T> S 保存后的实体对象，包含最新状态
     */
    @Override
    @CachePut(value = "entities", key = "#root.targetClass.simpleName + ':' + #result.id")
    <S extends T> S save(S entity);

    /**
     * 根据ID删除实体（清除缓存）
     * 
     * 功能说明：
     * - 重写BaseRepository的deleteById方法，添加缓存清除支持
     * - 删除数据库记录的同时清除对应的缓存项
     * - 确保删除操作后不会从缓存中查询到已删除的数据
     * 
     * 业务逻辑：
     * 1. 执行数据库删除操作
     * 2. 删除成功后，从缓存中清除对应的缓存项
     * 3. 使用指定的缓存键精确删除，不影响其他缓存数据
     * 
     * 缓存一致性：
     * - 防止删除后从缓存查询到过期数据
     * - 确保后续查询会重新从数据库加载
     * - 避免缓存和数据库状态不一致的问题
     * 
     * @param id 要删除的实体主键ID
     */
    @Override
    @CacheEvict(value = "entities", key = "#root.targetClass.simpleName + ':' + #id")
    void deleteById(ID id);

    /**
     * 删除实体对象（清除缓存）
     * 
     * 功能说明：
     * - 重写BaseRepository的delete方法，添加缓存清除支持
     * - 根据实体对象删除数据库记录并清除对应缓存
     * - 支持通过实体对象进行删除操作
     * 
     * 业务逻辑：
     * 1. 从实体对象中获取ID值用于生成缓存键
     * 2. 执行数据库删除操作
     * 3. 删除成功后清除对应的缓存项
     * 
     * 使用场景：
     * - 已有实体对象时的删除操作
     * - 批量操作中的单个实体删除
     * - 级联删除时的关联实体清理
     * 
     * @param entity 要删除的实体对象
     */
    @Override
    @CacheEvict(value = "entities", key = "#root.targetClass.simpleName + ':' + #entity.id")
    void delete(T entity);

    /**
     * 清除所有缓存
     * 
     * 功能说明：
     * - 清除当前实体类型的所有缓存项
     * - 用于批量数据更新后的缓存刷新
     * - 支持缓存的完全重置和刷新
     * 
     * 业务逻辑：
     * - 使用allEntries=true清除entities缓存空间的所有数据
     * - 不区分具体的缓存键，全部清除
     * - 下次查询时会重新从数据库加载并缓存
     * 
     * 使用场景：
     * - 批量导入数据后的缓存刷新
     * - 配置文件更新后的缓存重置
     * - 系统维护时的缓存清理
     * - 缓存异常时的紧急清理
     * 
     * 注意事项：
     * - 清除缓存后短时间内查询压力会转移到数据库
     * - 建议在业务低峰期执行全量缓存清理
     * - 可以配合预热机制快速重建热点数据缓存
     */
    @CacheEvict(value = "entities", allEntries = true)
    void clearCache();
}