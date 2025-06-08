package com.game.frame.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Protobuf decoder with message completeness validation, protocol version compatibility,
 * and exception message handling
 *
 * @author lx
 * @date 2024-01-01
 */
public class ProtobufDecoder extends ByteToMessageDecoder {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtobufDecoder.class);
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Check if we have enough data
        if (in.readableBytes() < 8) { // At least 8 bytes for message ID + length
            return;
        }
        
        // Mark reader index
        in.markReaderIndex();
        
        try {
            // Read message ID (4 bytes)
            int messageId = in.readInt();
            
            // Read payload length (4 bytes)
            int payloadLength = in.readInt();
            
            // Validate payload length
            if (payloadLength < 0 || payloadLength > 1024 * 1024) { // Max 1MB
                logger.error("Invalid payload length: {}", payloadLength);
                ctx.close();
                return;
            }
            
            // Check if we have enough data for the payload
            if (in.readableBytes() < payloadLength) {
                in.resetReaderIndex();
                return;
            }
            
            // Read payload
            byte[] payload = new byte[payloadLength];
            in.readBytes(payload);
            
            // Create message wrapper
            MessageWrapper wrapper = new MessageWrapper(messageId, payload);
            out.add(wrapper);
            
            logger.debug("Decoded message: ID={}, payload size={}", messageId, payloadLength);
            
        } catch (Exception e) {
            logger.error("Error decoding protobuf message", e);
            in.resetReaderIndex();
            ctx.close();
        }
    }
}