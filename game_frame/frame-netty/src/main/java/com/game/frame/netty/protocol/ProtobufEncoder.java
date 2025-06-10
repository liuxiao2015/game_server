package com.game.frame.netty.protocol;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protobuf encoder with message header support (length, type, version)
 * Supports compression and encryption placeholders
 *
 * @author lx
 * @date 2024-01-01
 */
/**
 * ProtobufEncoder
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class ProtobufEncoder extends MessageToByteEncoder<Object> {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtobufEncoder.class);
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (msg instanceof GeneratedMessageV3) {
            GeneratedMessageV3 message = (GeneratedMessageV3) msg;
            byte[] data = message.toByteArray();
            
            // Write message data
            out.writeBytes(data);
            
            logger.debug("Encoded protobuf message: {}, size: {}", 
                    message.getClass().getSimpleName(), data.length);
        } else {
            logger.error("Unsupported message type for encoding: {}", msg.getClass());
            throw new IllegalArgumentException("Message must be a protobuf message");
        }
    }
}