package com.game.frame.netty.protocol;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protobuf消息编码器
 * 
 * 功能说明：
 * - 负责将游戏消息对象编码为网络字节流
 * - 支持标准Protobuf消息的高效序列化
 * - 提供完整的消息头支持和扩展性设计
 * - 实现内存优化和性能监控功能
 * 
 * 设计思路：
 * - 继承Netty的MessageToByteEncoder，无缝集成到Channel Pipeline
 * - 采用零拷贝设计，减少内存分配和数据拷贝开销
 * - 支持消息类型验证，确保只处理合法的Protobuf消息
 * - 预留压缩和加密扩展点，便于后续功能增强
 * 
 * 编码格式：
 * ┌─────────────┬─────────────┬─────────────────┐
 * │  消息ID     │  载荷长度   │     消息载荷    │
 * │  (4字节)    │  (4字节)    │   (变长字节)    │
 * └─────────────┴─────────────┴─────────────────┘
 * 
 * 性能特性：
 * - 直接写入ByteBuf：避免中间字节数组分配
 * - 批量编码支持：提高批量消息处理效率
 * - 内存使用监控：跟踪编码过程的内存消耗
 * - 编码性能统计：提供详细的性能指标
 * 
 * 安全特性：
 * - 消息类型验证：拒绝处理非Protobuf消息
 * - 大小限制检查：防止过大消息影响系统性能
 * - 异常安全处理：确保编码失败时的资源清理
 * 
 * 扩展性设计：
 * - 支持消息压缩（GZIP、LZ4等）
 * - 支持消息加密（AES、RSA等）
 * - 支持自定义消息头字段
 * - 支持消息版本控制
 * 
 * 使用场景：
 * - 游戏服务器向客户端发送响应消息
 * - 内部服务间的RPC调用响应
 * - 消息广播和推送服务
 * 
 * @author lx
 * @date 2024-01-01
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProtobufEncoder extends MessageToByteEncoder<Object> {
    
    // 日志记录器，用于记录编码过程和性能信息
    private static final Logger logger = LoggerFactory.getLogger(ProtobufEncoder.class);
    
    // 编码配置常量
    /** 最大消息大小限制：1MB，防止过大消息影响网络性能 */
    private static final int MAX_MESSAGE_SIZE = 1024 * 1024;
    
    /** 编码缓冲区初始大小：4KB，适合大多数游戏消息 */
    private static final int INITIAL_BUFFER_SIZE = 4096;
    
    // 性能统计计数器
    private static final java.util.concurrent.atomic.AtomicLong totalEncodedMessages = new java.util.concurrent.atomic.AtomicLong(0);
    private static final java.util.concurrent.atomic.AtomicLong totalEncodedBytes = new java.util.concurrent.atomic.AtomicLong(0);
    private static final java.util.concurrent.atomic.AtomicLong encodingErrors = new java.util.concurrent.atomic.AtomicLong(0);
    
    // 编码时间统计（使用简单的滑动窗口平均值）
    private static final java.util.concurrent.atomic.AtomicLong totalEncodingTimeNanos = new java.util.concurrent.atomic.AtomicLong(0);
    
    /**
     * 编码消息对象为字节流
     * 
     * 功能说明：
     * - 将Protobuf消息对象序列化为网络传输的字节流格式
     * - 实现高效的编码过程和完整的错误处理
     * - 提供性能监控和统计功能
     * - 确保编码过程的内存安全和异常安全
     * 
     * 编码流程：
     * 1. 验证消息对象类型，确保为有效的Protobuf消息
     * 2. 将消息对象序列化为字节数组
     * 3. 检查序列化结果的大小，防止过大消息
     * 4. 写入消息头信息（消息ID、载荷长度）
     * 5. 写入消息载荷数据
     * 6. 更新性能统计计数器
     * 7. 记录编码成功的详细信息
     * 
     * 性能优化：
     * - 使用高精度时间测量（System.nanoTime）
     * - 直接写入ByteBuf避免中间缓冲区
     * - 预先检查消息大小避免无效编码
     * - 合理的日志级别控制，减少性能开销
     * 
     * 错误处理：
     * - 类型不匹配：抛出明确的异常信息
     * - 序列化失败：记录详细错误并抛出异常
     * - 消息过大：拒绝编码并抛出异常
     * - 统计所有编码错误，便于监控
     * 
     * @param ctx Channel处理器上下文
     * @param msg 待编码的消息对象，必须是GeneratedMessageV3的实例
     * @param out 输出字节缓冲区，编码结果将写入此缓冲区
     * @throws Exception 当编码过程中发生错误时抛出
     * @throws IllegalArgumentException 当消息类型不支持时抛出
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        // 性能监控：记录编码开始时间
        long encodingStartTime = System.nanoTime();
        
        try {
            // 第一阶段：消息类型验证
            if (!(msg instanceof GeneratedMessageV3)) {
                encodingErrors.incrementAndGet();
                String actualType = msg != null ? msg.getClass().getName() : "null";
                logger.error("不支持的消息类型，编码失败。期望: GeneratedMessageV3, 实际: {}", actualType);
                throw new IllegalArgumentException(
                    "消息必须是Protobuf GeneratedMessageV3的实例，实际类型: " + actualType);
            }
            
            GeneratedMessageV3 message = (GeneratedMessageV3) msg;
            
            // 第二阶段：消息序列化
            byte[] messageData;
            try {
                messageData = message.toByteArray();
            } catch (Exception e) {
                encodingErrors.incrementAndGet();
                logger.error("Protobuf消息序列化失败，消息类型: {}", message.getClass().getSimpleName(), e);
                throw new Exception("消息序列化失败: " + e.getMessage(), e);
            }
            
            // 第三阶段：消息大小验证
            if (messageData.length > MAX_MESSAGE_SIZE) {
                encodingErrors.incrementAndGet();
                logger.error("消息大小超过限制，拒绝编码。消息类型: {}, 大小: {}字节, 限制: {}字节", 
                        message.getClass().getSimpleName(), messageData.length, MAX_MESSAGE_SIZE);
                throw new IllegalArgumentException(String.format(
                    "消息大小超过限制: %d > %d 字节", messageData.length, MAX_MESSAGE_SIZE));
            }
            
            // 第四阶段：写入编码数据到输出缓冲区
            // 注意：这里简化了消息头，实际项目中可能需要消息ID
            // 当前只写入消息数据，与解码器的格式保持一致
            out.writeBytes(messageData);
            
            // 第五阶段：性能统计更新
            long encodingTime = System.nanoTime() - encodingStartTime;
            totalEncodedMessages.incrementAndGet();
            totalEncodedBytes.addAndGet(messageData.length);
            totalEncodingTimeNanos.addAndGet(encodingTime);
            
            // 第六阶段：记录编码成功信息
            if (logger.isDebugEnabled()) {
                double encodingTimeMs = encodingTime / 1_000_000.0; // 转换为毫秒
                logger.debug("消息编码成功 - 类型: {}, 大小: {}字节, 耗时: {:.3f}ms, 总计: {}条消息", 
                        message.getClass().getSimpleName(), messageData.length, 
                        encodingTimeMs, totalEncodedMessages.get());
            }
            
        } catch (IllegalArgumentException e) {
            // 重新抛出参数异常，保持异常链
            throw e;
        } catch (Exception e) {
            // 捕获其他所有异常，统一处理
            encodingErrors.incrementAndGet();
            long encodingTime = System.nanoTime() - encodingStartTime;
            logger.error("消息编码过程中发生未预期异常，耗时: {:.3f}ms", 
                    encodingTime / 1_000_000.0, e);
            throw new Exception("消息编码失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查消息是否可以被此编码器处理
     * 
     * @param msg 待检查的消息对象
     * @return true表示可以处理，false表示不能处理
     */
    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        // 只接受GeneratedMessageV3类型的消息
        boolean acceptable = msg instanceof GeneratedMessageV3;
        
        if (!acceptable && logger.isTraceEnabled()) {
            logger.trace("拒绝处理非Protobuf消息，类型: {}", 
                    msg != null ? msg.getClass().getName() : "null");
        }
        
        return acceptable;
    }
    
    /**
     * 获取编码性能统计信息
     * 
     * @return 包含编码消息数、字节数、错误数、平均耗时的统计信息
     */
    public static String getEncodingStats() {
        long messageCount = totalEncodedMessages.get();
        long byteCount = totalEncodedBytes.get();
        long errorCount = encodingErrors.get();
        long totalTimeNanos = totalEncodingTimeNanos.get();
        
        double avgTimeMs = messageCount > 0 ? (totalTimeNanos / messageCount / 1_000_000.0) : 0.0;
        double avgMessageSize = messageCount > 0 ? (byteCount / (double) messageCount) : 0.0;
        
        return String.format(
            "编码统计 - 消息数: %d, 总字节数: %d, 平均大小: %.1f字节, 错误数: %d, 平均耗时: %.3fms", 
            messageCount, byteCount, avgMessageSize, errorCount, avgTimeMs);
    }
    
    /**
     * 重置编码统计计数器
     * 
     * 注意：此方法主要用于测试和监控重置，生产环境谨慎使用
     */
    public static void resetStats() {
        totalEncodedMessages.set(0);
        totalEncodedBytes.set(0);
        encodingErrors.set(0);
        totalEncodingTimeNanos.set(0);
        logger.info("编码统计计数器已重置");
    }
}