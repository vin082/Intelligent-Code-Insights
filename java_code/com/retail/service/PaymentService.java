package com.retail.service;

import com.retail.model.Customer;
import com.retail.exception.PaymentProcessingException;
import com.retail.util.PaymentGatewayClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling payment processing operations
 * Integrates with payment gateways and manages payment transactions
 */
public class PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;
    private final Map<Long, PaymentTransaction> transactionHistory;

    public PaymentService(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
        this.transactionHistory = new HashMap<>();
    }

    /**
     * Processes a payment for an order
     * @param orderId the order ID
     * @param amount the payment amount
     * @param paymentMethod the payment method
     * @param customer the customer
     * @return true if payment successful
     */
    public boolean processPayment(Long orderId, BigDecimal amount, String paymentMethod, Customer customer) {
        try {
            validatePaymentRequest(amount, paymentMethod);

            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrderId(orderId);
            transaction.setCustomerId(customer.getCustomerId());
            transaction.setAmount(amount);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setStatus(PaymentStatus.PROCESSING);
            transaction.setTimestamp(LocalDateTime.now());

            // Call payment gateway
            boolean gatewayResponse = paymentGatewayClient.authorizePayment(
                    customer.getEmail(),
                    amount,
                    paymentMethod
            );

            if (gatewayResponse) {
                transaction.setStatus(PaymentStatus.CAPTURED);
                transactionHistory.put(orderId, transaction);
                return true;
            } else {
                transaction.setStatus(PaymentStatus.FAILED);
                transactionHistory.put(orderId, transaction);
                return false;
            }

        } catch (Exception e) {
            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage());
        }
    }

    /**
     * Processes a refund for an order
     * @param orderId the order ID
     * @param amount the refund amount
     * @return true if refund successful
     */
    public boolean refundPayment(Long orderId, BigDecimal amount) {
        PaymentTransaction originalTransaction = transactionHistory.get(orderId);

        if (originalTransaction == null) {
            throw new IllegalArgumentException("No payment transaction found for order: " + orderId);
        }

        if (originalTransaction.getStatus() != PaymentStatus.CAPTURED) {
            throw new IllegalStateException("Cannot refund payment that was not captured");
        }

        try {
            boolean refundSuccess = paymentGatewayClient.refundPayment(orderId, amount);

            if (refundSuccess) {
                originalTransaction.setStatus(PaymentStatus.REFUNDED);
                originalTransaction.setRefundedAt(LocalDateTime.now());
                return true;
            }

            return false;

        } catch (Exception e) {
            throw new PaymentProcessingException("Refund processing failed: " + e.getMessage());
        }
    }

    /**
     * Validates payment request parameters
     * @param amount the payment amount
     * @param paymentMethod the payment method
     */
    private void validatePaymentRequest(BigDecimal amount, String paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    /**
     * Retrieves payment transaction for an order
     * @param orderId the order ID
     * @return payment transaction
     */
    public PaymentTransaction getTransaction(Long orderId) {
        return transactionHistory.get(orderId);
    }

    // Inner class for payment transaction
    public static class PaymentTransaction {
        private Long orderId;
        private Long customerId;
        private BigDecimal amount;
        private String paymentMethod;
        private PaymentStatus status;
        private LocalDateTime timestamp;
        private LocalDateTime refundedAt;

        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public LocalDateTime getRefundedAt() { return refundedAt; }
        public void setRefundedAt(LocalDateTime refundedAt) { this.refundedAt = refundedAt; }
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        CAPTURED,
        FAILED,
        REFUNDED
    }
}
