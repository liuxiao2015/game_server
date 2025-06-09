package com.game.service.payment.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment order entity
 * Represents a payment order with all necessary information
 *
 * @author lx
 * @date 2025/01/08
 */
@Entity
@Table(name = "payment_order")
/**
 * PaymentOrder
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public class PaymentOrder {
    
    @Id
    private String orderId;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String productId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(name = "payment_channel")
    private String paymentChannel;
    
    @Column(name = "channel_order_id")
    private String channelOrderId;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "pay_time")
    private LocalDateTime payTime;
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "callback_url")
    private String callbackUrl;
    
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    /**
     * Order status enumeration
     */
    public enum OrderStatus {
        CREATED,        // Order created
        PENDING,        // Payment pending
        PAID,           // Payment successful
        CANCELLED,      // Order cancelled
        EXPIRED,        // Order expired
        REFUNDED,       // Order refunded
        FAILED          // Payment failed
    }

    public PaymentOrder() {
        this.createTime = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }

    public PaymentOrder(String orderId, Long userId, String productId, BigDecimal amount, String currency) {
        this();
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getPaymentChannel() {
        return paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {
        this.paymentChannel = paymentChannel;
    }

    public String getChannelOrderId() {
        return channelOrderId;
    }

    public void setChannelOrderId(String channelOrderId) {
        this.channelOrderId = channelOrderId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    @Override
    public String toString() {
        return "PaymentOrder{" +
                "orderId='" + orderId + '\'' +
                ", userId=" + userId +
                ", productId='" + productId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", paymentChannel='" + paymentChannel + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}