package com.game.service.payment.channel;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Payment channel interface
 * Defines common operations for all payment channels
 *
 * @author lx
 * @date 2025/01/08
 */
/**
 * PaymentChannel
 * 
 * 功能说明：
 * - 提供核心业务功能实现
 * - 支持模块化设计和扩展
 * - 集成框架的标准组件和服务
 *
 * @author lx
 * @date 2024-01-01
 */
public interface PaymentChannel {

    /**
     * Get channel name
     */
    String getChannelName();

    /**
     * Create payment order
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Query payment status
     */
    PaymentStatus queryStatus(String orderId);

    /**
     * Process refund
     */
    RefundResponse refund(RefundRequest request);

    /**
     * Reconcile payments for a specific date
     */
    ReconciliationResult reconcile(Date date);

    /**
     * Verify callback signature
     */
    boolean verifyCallback(Map<String, String> params, String signature);

    /**
     * Parse callback data
     */
    CallbackData parseCallback(Map<String, String> params);

    /**
     * Payment request data
     */
    class PaymentRequest {
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String productName;
        private String callbackUrl;
        private String returnUrl;
        private Map<String, Object> extraParams;

        public PaymentRequest() {}

        public PaymentRequest(String orderId, BigDecimal amount, String currency, String productName) {
            this.orderId = orderId;
            this.amount = amount;
            this.currency = currency;
            this.productName = productName;
        }

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCallbackUrl() { return callbackUrl; }
        public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
        public Map<String, Object> getExtraParams() { return extraParams; }
        public void setExtraParams(Map<String, Object> extraParams) { this.extraParams = extraParams; }
    }

    /**
     * Payment response data
     */
    class PaymentResponse {
        private boolean success;
        private String channelOrderId;
        private String paymentUrl;
        private String paymentCode;
        private String errorCode;
        private String errorMessage;
        private Map<String, Object> extraData;

        public PaymentResponse() {}

        public PaymentResponse(boolean success) {
            this.success = success;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getChannelOrderId() { return channelOrderId; }
        public void setChannelOrderId(String channelOrderId) { this.channelOrderId = channelOrderId; }
        public String getPaymentUrl() { return paymentUrl; }
        public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }
        public String getPaymentCode() { return paymentCode; }
        public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Map<String, Object> getExtraData() { return extraData; }
        public void setExtraData(Map<String, Object> extraData) { this.extraData = extraData; }
    }

    /**
     * Payment status enumeration
     */
    enum PaymentStatus {
        PENDING,    // Payment pending
        SUCCESS,    // Payment successful
        FAILED,     // Payment failed
        CANCELLED,  // Payment cancelled
        UNKNOWN     // Status unknown
    }

    /**
     * Refund request data
     */
    class RefundRequest {
        private String orderId;
        private String refundId;
        private BigDecimal refundAmount;
        private String reason;

        public RefundRequest() {}

        public RefundRequest(String orderId, String refundId, BigDecimal refundAmount, String reason) {
            this.orderId = orderId;
            this.refundId = refundId;
            this.refundAmount = refundAmount;
            this.reason = reason;
        }

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Refund response data
     */
    class RefundResponse {
        private boolean success;
        private String refundId;
        private String errorCode;
        private String errorMessage;

        public RefundResponse() {}

        public RefundResponse(boolean success) {
            this.success = success;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Reconciliation result
     */
    class ReconciliationResult {
        private boolean success;
        private int totalCount;
        private int successCount;
        private int failedCount;
        private BigDecimal totalAmount;
        private String errorMessage;

        public ReconciliationResult() {}

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * Callback data
     */
    class CallbackData {
        private String orderId;
        private String channelOrderId;
        private PaymentStatus status;
        private BigDecimal amount;
        private String currency;
        private Date payTime;
        private Map<String, Object> extraData;

        public CallbackData() {}

        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public String getChannelOrderId() { return channelOrderId; }
        public void setChannelOrderId(String channelOrderId) { this.channelOrderId = channelOrderId; }
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
        public Date getPayTime() { return payTime; }
        public void setPayTime(Date payTime) { this.payTime = payTime; }
        public Map<String, Object> getExtraData() { return extraData; }
        public void setExtraData(Map<String, Object> extraData) { this.extraData = extraData; }
    }
}