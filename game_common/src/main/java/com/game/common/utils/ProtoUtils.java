package com.game.common.utils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protobuf utility class providing serialization, deserialization, and type conversion
 *
 * @author lx
 * @date 2024-01-01
 */
public final class ProtoUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(ProtoUtils.class);
    
    /**
     * Cache for parseFrom methods to avoid reflection overhead
     */
    private static final ConcurrentHashMap<Class<?>, Method> methodCache = new ConcurrentHashMap<>();
    
    private ProtoUtils() {
        // Utility class, no instantiation
    }
    
    /**
     * Serializes a protobuf message to byte array
     * 
     * @param message the protobuf message
     * @return serialized bytes
     */
    public static byte[] serialize(Message message) {
        if (message == null) {
            return new byte[0];
        }
        return message.toByteArray();
    }
    
    /**
     * Deserializes byte array to protobuf message
     * 
     * @param data the byte array
     * @param clazz the message class
     * @param <T> message type
     * @return deserialized message
     * @throws InvalidProtocolBufferException if deserialization fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends GeneratedMessageV3> T deserialize(byte[] data, Class<T> clazz) 
            throws InvalidProtocolBufferException {
        if (data == null || data.length == 0) {
            throw new InvalidProtocolBufferException("Empty data");
        }
        
        try {
            // Check cache for the parseFrom method
            java.lang.reflect.Method parseFromMethod = methodCache.computeIfAbsent(clazz, key -> {
                try {
                    return key.getMethod("parseFrom", byte[].class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("Failed to find parseFrom method for class: " + key.getName(), e);
                }
            });
            return (T) parseFromMethod.invoke(null, (Object) data);
        } catch (Exception e) {
            throw new InvalidProtocolBufferException("Failed to deserialize: " + e.getMessage());
        }
    }
    
    /**
     * Safely deserializes byte array to protobuf message
     * Returns null if deserialization fails
     * 
     * @param data the byte array
     * @param clazz the message class
     * @param <T> message type
     * @return deserialized message or null
     */
    public static <T extends GeneratedMessageV3> T safeDeserialize(byte[] data, Class<T> clazz) {
        try {
            return deserialize(data, clazz);
        } catch (Exception e) {
            logger.error("Failed to deserialize protobuf message: {}", clazz.getSimpleName(), e);
            return null;
        }
    }
    
    /**
     * Gets the message size in bytes
     * 
     * @param message the protobuf message
     * @return size in bytes
     */
    public static int getMessageSize(Message message) {
        if (message == null) {
            return 0;
        }
        return message.getSerializedSize();
    }
    
    /**
     * Checks if a message is valid (not null and initialized)
     * 
     * @param message the protobuf message
     * @return true if valid
     */
    public static boolean isValidMessage(Message message) {
        return message != null && message.isInitialized();
    }
}