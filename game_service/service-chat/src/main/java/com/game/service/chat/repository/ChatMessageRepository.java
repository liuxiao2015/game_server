package com.game.service.chat.repository;

import com.game.service.chat.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Chat message repository for ElasticSearch operations
 * Provides CRUD and search operations for chat messages
 *
 * @author lx
 * @date 2025/01/08
 */
@Repository
/**
 * ChatMessage数据访问接口
 * 
 * 功能说明：
 * - 提供数据持久化操作接口
 * - 封装数据库查询和更新逻辑
 * - 支持JPA和MyBatis数据访问
 *
 * @author lx
 * @date 2024-01-01
 */
public interface ChatMessageRepository extends ElasticsearchRepository<ChatMessage, String> {

    /**
     * Find messages by channel ID
     */
    Page<ChatMessage> findByChannelIdAndDeletedFalseOrderByTimestampDesc(Long channelId, Pageable pageable);

    /**
     * Find messages by sender ID
     */
    Page<ChatMessage> findBySenderIdAndDeletedFalseOrderByTimestampDesc(Long senderId, Pageable pageable);

    /**
     * Find messages by channel and time range
     */
    Page<ChatMessage> findByChannelIdAndTimestampBetweenAndDeletedFalseOrderByTimestampDesc(
            Long channelId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Search messages by content
     */
    List<ChatMessage> findByChannelIdAndContentContainingAndDeletedFalseOrderByTimestampDesc(
            Long channelId, String keyword, Pageable pageable);

    /**
     * Search messages across all channels by content
     */
    List<ChatMessage> findByContentContainingAndDeletedFalseOrderByTimestampDesc(
            String keyword, Pageable pageable);

    /**
     * Count messages by channel
     */
    long countByChannelIdAndDeletedFalse(Long channelId);

    /**
     * Find messages for offline users (messages sent after a specific time)
     */
    List<ChatMessage> findByChannelIdInAndTimestampAfterAndDeletedFalseOrderByTimestampAsc(
            List<Long> channelIds, LocalDateTime afterTime);
}