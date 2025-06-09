package com.game.frame.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Repository基类
 * @author lx
 * @date 2025/06/08
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {

    /**
     * 根据ID查找未删除的实体
     * @param id 主键
     * @return 实体
     */
    Optional<T> findByIdAndDeletedEquals(ID id, Integer deleted);

    /**
     * 查找所有未删除的实体
     * @return 实体列表
     */
    List<T> findByDeletedEquals(Integer deleted);

    /**
     * 逻辑删除
     * @param id 主键
     * @return 影响行数
     */
    int updateDeletedById(ID id, Integer deleted);

    /**
     * 批量逻辑删除
     * @param ids 主键列表
     * @return 影响行数
     */
    int updateDeletedByIdIn(List<ID> ids, Integer deleted);
}