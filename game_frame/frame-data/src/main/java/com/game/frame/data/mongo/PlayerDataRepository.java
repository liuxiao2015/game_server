package com.game.frame.data.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 玩家数据Repository
 * @author lx
 * @date 2025/06/08
 */
@Repository
public interface PlayerDataRepository extends MongoRepository<PlayerDataEntity, String> {

    /**
     * 根据用户ID查找玩家数据
     * @param userId 用户ID
     * @return 玩家数据
     */
    Optional<PlayerDataEntity> findByUserId(Long userId);

    /**
     * 根据玩家名查找
     * @param playerName 玩家名
     * @return 玩家数据列表
     */
    List<PlayerDataEntity> findByPlayerNameContaining(String playerName);

    /**
     * 查找指定时间段内的玩家数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 玩家数据列表
     */
    @Query("{'createTime': {$gte: ?0, $lte: ?1}}")
    List<PlayerDataEntity> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查找指定时间段内更新的玩家数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 玩家数据列表
     */
    @Query("{'updateTime': {$gte: ?0, $lte: ?1}}")
    List<PlayerDataEntity> findByUpdateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据游戏数据中的特定字段查找
     * @param fieldName 字段名
     * @param value 字段值
     * @return 玩家数据列表
     */
    @Query("{'gameData.?0': ?1}")
    List<PlayerDataEntity> findByGameDataField(String fieldName, Object value);

    /**
     * 统计玩家总数
     * @return 玩家总数
     */
    @Query(value = "{}", count = true)
    long countAllPlayers();
}