package com.game.frame.data.repository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.Optional;

/**
 * 带缓存的Repository
 * @author lx
 * @date 2025/06/08
 */
@NoRepositoryBean
public interface CacheableRepository<T, ID extends Serializable> extends BaseRepository<T, ID> {

    /**
     * 根据ID查找实体（带缓存）
     * @param id 主键
     * @return 实体
     */
    @Override
    @Cacheable(value = "entities", key = "#root.targetClass.simpleName + ':' + #id")
    Optional<T> findById(ID id);

    /**
     * 保存实体（更新缓存）
     * @param entity 实体
     * @return 保存后的实体
     */
    @Override
    @CachePut(value = "entities", key = "#root.targetClass.simpleName + ':' + #result.id")
    <S extends T> S save(S entity);

    /**
     * 删除实体（清除缓存）
     * @param id 主键
     */
    @Override
    @CacheEvict(value = "entities", key = "#root.targetClass.simpleName + ':' + #id")
    void deleteById(ID id);

    /**
     * 删除实体（清除缓存）
     * @param entity 实体
     */
    @Override
    @CacheEvict(value = "entities", key = "#root.targetClass.simpleName + ':' + #entity.id")
    void delete(T entity);

    /**
     * 清除所有缓存
     */
    @CacheEvict(value = "entities", allEntries = true)
    void clearCache();
}