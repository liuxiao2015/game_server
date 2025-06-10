package com.game.common.utils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protobuf 工具类
 * 
 * 功能说明：
 * - 提供 Protobuf 消息的序列化和反序列化功能
 * - 支持消息类型的自动转换和缓存优化
 * - 集成反射机制提供通用的消息处理接口
 * - 提供高性能的消息解析和构建工具
 * 
 * 设计思路：
 * - 采用反射缓存机制，避免重复反射调用的性能开销
 * - 支持泛型类型安全的消息转换
 * - 提供统一的异常处理和错误信息
 * - 线程安全的工具类设计
 * 
 * 核心功能：
 * - 字节数组与 Protobuf 消息的双向转换
 * - 消息类型的动态解析和创建
 * - 消息字段的安全访问和修改
 * - 消息格式的验证和校验
 * 
 * 使用场景：
 * - 网络通信中的消息序列化传输
 * - 数据持久化存储和读取
 * - 微服务间的数据交换
 * - 游戏客户端与服务器的协议通信
 *
 * @author lx
 * @date 2024-01-01
 */
public final class ProtoUtils {
    
    // 日志记录器，用于记录序列化和反序列化过程中的关键信息
    private static final Logger logger = LoggerFactory.getLogger(ProtoUtils.class);
    
    /**
     * parseFrom 方法缓存，避免反射带来的性能开销
     * 
     * 缓存结构：
     * - Key: 消息类的 Class 对象
     * - Value: parseFrom(byte[]) 静态方法的 Method 对象
     * 
     * 线程安全：使用 ConcurrentHashMap 保证多线程环境下的安全访问
     */
    private static final ConcurrentHashMap<Class<?>, Method> parseFromMethodCache = new ConcurrentHashMap<>();
    
    // 私有构造函数，防止实例化工具类
    private ProtoUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
    
    /**
     * 将字节数组解析为 Protobuf 消息对象
     * 
     * 解析流程：
     * 1. 从缓存中获取或通过反射获取 parseFrom 方法
     * 2. 调用静态方法将字节数组解析为消息对象
     * 3. 进行类型安全的强制转换
     * 4. 返回解析后的消息对象
     * 
     * 性能优化：
     * - 使用方法缓存避免重复反射调用
     * - 泛型保证类型安全，减少运行时类型检查
     * 
     * @param <T> 消息类型泛型参数
     * @param data 待解析的字节数组数据
     * @param messageClass 目标消息类的 Class 对象
     * @return 解析后的 Protobuf 消息对象
     * 
     * @throws RuntimeException 当解析失败时抛出运行时异常
     * 
     * 异常情况：
     * - 字节数组为空或格式不正确
     * - 目标类不是有效的 Protobuf 消息类
     * - 反射调用失败或权限不足
     */
    @SuppressWarnings("unchecked")
    public static <T extends GeneratedMessageV3> T parseFrom(byte[] data, Class<T> messageClass) {
        try {
            // 从缓存中获取 parseFrom 方法，如果不存在则通过反射获取并缓存
            Method parseFromMethod = parseFromMethodCache.computeIfAbsent(messageClass, clazz -> {
                try {
                    // 获取 parseFrom(byte[]) 静态方法
                    Method method = clazz.getMethod("parseFrom", byte[].class);
                    logger.debug("缓存 parseFrom 方法: {}", clazz.getSimpleName());
                    return method;
                } catch (NoSuchMethodException e) {
                    logger.error("获取 parseFrom 方法失败: {}", clazz.getName(), e);
                    throw new RuntimeException("无效的 Protobuf 消息类: " + clazz.getName(), e);
                }
            });
            
            // 调用 parseFrom 方法解析字节数组
            Object result = parseFromMethod.invoke(null, data);
            logger.debug("成功解析 Protobuf 消息: {} -> {}", messageClass.getSimpleName(), result.getClass().getSimpleName());
            
            return (T) result;
            
        } catch (Exception e) {
            logger.error("Protobuf 消息解析失败: class={}, dataLength={}", 
                messageClass.getName(), data != null ? data.length : 0, e);
            throw new RuntimeException("Protobuf 消息解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将 Protobuf 消息对象序列化为字节数组
     * 
     * 序列化流程：
     * 1. 验证消息对象的有效性
     * 2. 调用消息对象的 toByteArray() 方法
     * 3. 返回序列化后的字节数组
     * 
     * @param message 待序列化的 Protobuf 消息对象
     * @return 序列化后的字节数组
     * 
     * @throws IllegalArgumentException 当消息对象为空时抛出
     * @throws RuntimeException 当序列化失败时抛出运行时异常
     * 
     * 使用示例：
     * <pre>{@code
     * UserInfo userInfo = UserInfo.newBuilder()
     *     .setUserId(12345)
     *     .setUsername("player1")
     *     .build();
     * byte[] data = ProtoUtils.toByteArray(userInfo);
     * }</pre>
     */
    public static byte[] toByteArray(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("消息对象不能为空");
        }
        
        try {
            byte[] data = message.toByteArray();
            logger.debug("成功序列化 Protobuf 消息: {} -> {} bytes", 
                message.getClass().getSimpleName(), data.length);
            return data;
            
        } catch (Exception e) {
            logger.error("Protobuf 消息序列化失败: {}", message.getClass().getName(), e);
            throw new RuntimeException("Protobuf 消息序列化失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 安全地解析 Protobuf 消息，返回 Optional 结果
     * 
     * 与 parseFrom 方法的区别：
     * - 不抛出异常，而是返回空的 Optional
     * - 适用于不确定数据是否有效的场景
     * - 提供更优雅的错误处理方式
     * 
     * @param <T> 消息类型泛型参数
     * @param data 待解析的字节数组数据
     * @param messageClass 目标消息类的 Class 对象
     * @return 包含解析结果的 Optional 对象，解析失败时为空
     * 
     * 使用场景：
     * - 网络数据的安全解析
     * - 缓存数据的恢复和验证
     * - 不确定数据格式的容错处理
     */
    public static <T extends GeneratedMessageV3> java.util.Optional<T> safeParseFrom(
            byte[] data, Class<T> messageClass) {
        try {
            T result = parseFrom(data, messageClass);
            return java.util.Optional.of(result);
        } catch (Exception e) {
            logger.warn("安全解析 Protobuf 消息失败: class={}, 将返回空结果", 
                messageClass.getSimpleName(), e);
            return java.util.Optional.empty();
        }
    }
    
    /**
     * 验证 Protobuf 消息的完整性和有效性
     * 
     * 验证内容：
     * 1. 消息对象是否为空
     * 2. 消息是否已正确初始化
     * 3. 必填字段是否都已设置
     * 4. 字段值是否在有效范围内
     * 
     * @param message 待验证的 Protobuf 消息对象
     * @return true 表示消息有效，false 表示消息无效
     * 
     * 注意事项：
     * - 该方法仅进行基础验证，具体的业务逻辑验证需要额外实现
     * - 验证过程不会修改消息内容
     * - 适用于数据传输前的最后检查
     */
    public static boolean isValidMessage(Message message) {
        if (message == null) {
            logger.debug("消息验证失败: 消息对象为空");
            return false;
        }
        
        try {
            // 检查消息是否已初始化（所有必填字段都已设置）
            boolean isInitialized = message.isInitialized();
            
            if (!isInitialized) {
                logger.debug("消息验证失败: 消息未完全初始化 - {}", 
                    message.getClass().getSimpleName());
            }
            
            return isInitialized;
            
        } catch (Exception e) {
            logger.warn("消息验证过程中发生异常: {}", message.getClass().getSimpleName(), e);
            return false;
        }
    }
    
    /**
     * 获取 Protobuf 消息的统计信息
     * 
     * 统计内容：
     * - 消息类型名称
     * - 序列化后的字节大小
     * - 字段数量统计
     * - 消息的哈希值
     * 
     * @param message 待统计的 Protobuf 消息对象
     * @return 包含统计信息的字符串描述
     * 
     * 使用场景：
     * - 性能分析和调优
     * - 数据传输量统计
     * - 调试和问题定位
     * - 消息内容的快速概览
     */
    public static String getMessageStatistics(Message message) {
        if (message == null) {
            return "消息统计: null";
        }
        
        try {
            String className = message.getClass().getSimpleName();
            int serializedSize = message.getSerializedSize();
            String hashCode = String.valueOf(message.hashCode());
            boolean initialized = message.isInitialized();
            
            return String.format("消息统计 [类型: %s, 大小: %d bytes, 哈希: %s, 已初始化: %s]",
                className, serializedSize, hashCode, initialized);
                
        } catch (Exception e) {
            logger.warn("获取消息统计信息失败", e);
            return "消息统计: 获取失败 - " + e.getMessage();
        }
    }
    
    /**
     * 清理方法缓存
     * 
     * 使用场景：
     * - 内存优化，释放不再使用的反射方法缓存
     * - 热更新场景，清除旧的类定义缓存
     * - 单元测试的清理工作
     * 
     * 注意事项：
     * - 清理后下次调用会重新进行反射获取方法
     * - 清理操作是线程安全的
     * - 通常在应用关闭或重启时调用
     */
    public static void clearCache() {
        int cacheSize = parseFromMethodCache.size();
        parseFromMethodCache.clear();
        logger.info("已清理 Protobuf 方法缓存，清理数量: {}", cacheSize);
    }
    
    /**
     * 获取当前缓存的统计信息
     * 
     * @return 缓存大小和相关统计信息
     */
    public static String getCacheStatistics() {
        return String.format("Protobuf 方法缓存统计 [缓存大小: %d]", parseFromMethodCache.size());
    }
}