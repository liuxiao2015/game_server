package com.game.common;

/**
 * Business异常类
 * 
 * 功能说明：
 * - 封装业务处理过程中的异常情况
 * - 提供详细的错误信息和错误码
 * - 支持异常的分级处理和统一管理
 * - 便于异常的统计分析和问题排查
 * 
 * 设计特点：
 * - 继承标准异常类，保持异常处理的一致性
 * - 包含错误码和详细的错误描述信息
 * - 支持异常的链式传递和原因追踪
 * 
 * 使用场景：
 * - 业务逻辑验证失败时抛出
 * - 数据处理异常的统一处理
 * - 服务调用失败的错误封装
 *
 * @author lx
 * @date 2024-01-01
 */
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    /**
     * Creates a new BusinessException with error code and message
     * 
     * @param code error code
     * @param message error message
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * Creates a new BusinessException with error code, message and cause
     * 
     * @param code error code
     * @param message error message
     * @param cause the cause
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * Gets the error code
     * 
     * @return error code
     */
    public int getCode() {
        return code;
    }
    
    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}