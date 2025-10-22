package com.retail.util;

import java.math.BigDecimal;

/**
 * Client for integrating with payment gateway services
 */
public class PaymentGatewayClient {

    private final String apiEndpoint;
    private final String apiKey;

    public PaymentGatewayClient(String apiEndpoint, String apiKey) {
        this.apiEndpoint = apiEndpoint;
        this.apiKey = apiKey;
    }

    /**
     * Authorizes a payment through the gateway
     * @param customerEmail customer email
     * @param amount payment amount
     * @param paymentMethod payment method
     * @return true if authorization successful
     */
    public boolean authorizePayment(String customerEmail, BigDecimal amount, String paymentMethod) {
        // Simulate payment gateway call
        System.out.println("Authorizing payment via gateway:");
        System.out.println("Customer: " + customerEmail);
        System.out.println("Amount: $" + amount);
        System.out.println("Method: " + paymentMethod);

        // Simulate success for amounts less than 10000
        return amount.compareTo(new BigDecimal("10000")) < 0;
    }

    /**
     * Processes a refund through the gateway
     * @param orderId order ID
     * @param amount refund amount
     * @return true if refund successful
     */
    public boolean refundPayment(Long orderId, BigDecimal amount) {
        // Simulate refund processing
        System.out.println("Processing refund via gateway:");
        System.out.println("Order ID: " + orderId);
        System.out.println("Amount: $" + amount);
        return true;
    }
}
