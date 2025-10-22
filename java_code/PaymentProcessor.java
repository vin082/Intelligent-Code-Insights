package com.example.service;

import com.example.model.Payment;
import com.example.model.PaymentStatus;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Payment Processing Service
 * Handles payment transactions using Stripe API
 * Implements retry logic and fraud detection
 */
public class PaymentProcessor {

    private static final int MAX_RETRIES = 3;
    private static final BigDecimal FRAUD_THRESHOLD = new BigDecimal("10000.00");

    private final String stripeApiKey;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentRepository paymentRepository;

    /**
     * Constructor
     * @param stripeApiKey Stripe API secret key
     * @param fraudDetectionService Service for fraud detection
     * @param paymentRepository Repository for payment persistence
     */
    public PaymentProcessor(String stripeApiKey,
                           FraudDetectionService fraudDetectionService,
                           PaymentRepository paymentRepository) {
        this.stripeApiKey = stripeApiKey;
        this.fraudDetectionService = fraudDetectionService;
        this.paymentRepository = paymentRepository;

        // Initialize Stripe API
        Stripe.apiKey = this.stripeApiKey;
    }

    /**
     * Processes a payment transaction
     * @param payment The payment object containing transaction details
     * @return Updated payment with transaction result
     */
    public Payment processPayment(Payment payment) {
        // Validate payment
        validatePayment(payment);

        // Check for fraud
        if (isSuspiciousFraud(payment)) {
            payment.setStatus(PaymentStatus.FRAUD_DETECTED);
            payment.setErrorMessage("Transaction flagged for fraud review");
            paymentRepository.save(payment);
            return payment;
        }

        // Attempt payment with retry logic
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                // Process payment through Stripe
                Charge charge = createStripeCharge(payment);

                // Update payment status
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setTransactionId(charge.getId());
                payment.setProcessedAt(new java.util.Date());

                // Save successful payment
                paymentRepository.save(payment);

                return payment;

            } catch (com.stripe.exception.CardException e) {
                // Card was declined - no retry needed
                payment.setStatus(PaymentStatus.DECLINED);
                payment.setErrorMessage(e.getMessage());
                paymentRepository.save(payment);
                return payment;

            } catch (com.stripe.exception.RateLimitException e) {
                // Rate limit - retry after delay
                attempts++;
                lastException = e;
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }

            } catch (com.stripe.exception.StripeException e) {
                // Other Stripe errors - retry
                attempts++;
                lastException = e;
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(500 * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        // All retries failed
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage("Payment failed after " + MAX_RETRIES + " attempts: " +
                               (lastException != null ? lastException.getMessage() : "Unknown error"));
        paymentRepository.save(payment);

        return payment;
    }

    /**
     * Creates a Stripe charge
     * @param payment The payment details
     * @return The created Stripe Charge object
     */
    private Charge createStripeCharge(Payment payment) throws com.stripe.exception.StripeException {
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = payment.getAmount().multiply(new BigDecimal("100")).longValue();

        // Create charge parameters
        ChargeCreateParams params = ChargeCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency(payment.getCurrency().toLowerCase())
            .setSource(payment.getPaymentToken()) // Card token from frontend
            .setDescription("Order #" + payment.getOrderId())
            .putMetadata("customer_id", payment.getCustomerId().toString())
            .putMetadata("order_id", payment.getOrderId().toString())
            .build();

        // Execute charge
        return Charge.create(params);
    }

    /**
     * Validates payment data before processing
     * @param payment The payment to validate
     */
    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        if (payment.getAmount() == null || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (payment.getPaymentToken() == null || payment.getPaymentToken().isEmpty()) {
            throw new IllegalArgumentException("Payment token is required");
        }
        if (payment.getCurrency() == null || payment.getCurrency().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
        if (payment.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
    }

    /**
     * Checks if payment is suspicious for fraud
     * Uses multiple fraud detection strategies
     * @param payment The payment to check
     * @return true if suspicious, false otherwise
     */
    private boolean isSuspiciousFraud(Payment payment) {
        // High-value transaction check
        if (payment.getAmount().compareTo(FRAUD_THRESHOLD) > 0) {
            return true;
        }

        // Check with fraud detection service (ML-based)
        double fraudScore = fraudDetectionService.calculateFraudScore(payment);
        if (fraudScore > 0.75) { // 75% fraud probability threshold
            return true;
        }

        // Check for rapid successive transactions from same customer
        int recentTransactionCount = paymentRepository
            .countRecentTransactionsByCustomer(payment.getCustomerId(), 5); // Last 5 minutes

        if (recentTransactionCount > 5) {
            return true;
        }

        return false;
    }

    /**
     * Refunds a payment
     * @param transactionId The Stripe transaction ID to refund
     * @param amount The amount to refund (null for full refund)
     * @return true if refund successful
     */
    public boolean refundPayment(String transactionId, BigDecimal amount) {
        try {
            // Find the original payment
            Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            // Validate refund amount
            if (amount != null && amount.compareTo(payment.getAmount()) > 0) {
                throw new IllegalArgumentException("Refund amount cannot exceed original payment");
            }

            // Create refund parameters
            Map<String, Object> refundParams = new HashMap<>();
            refundParams.put("charge", transactionId);

            if (amount != null) {
                long refundAmountInCents = amount.multiply(new BigDecimal("100")).longValue();
                refundParams.put("amount", refundAmountInCents);
            }

            // Execute refund
            com.stripe.model.Refund.create(refundParams);

            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            return true;

        } catch (Exception e) {
            // Log error
            System.err.println("Refund failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets payment status from Stripe
     * @param transactionId The transaction ID
     * @return Current payment status
     */
    public PaymentStatus getPaymentStatus(String transactionId) {
        try {
            Charge charge = Charge.retrieve(transactionId);

            if (charge.getRefunded()) {
                return PaymentStatus.REFUNDED;
            } else if (charge.getPaid()) {
                return PaymentStatus.COMPLETED;
            } else {
                return PaymentStatus.PENDING;
            }

        } catch (Exception e) {
            return PaymentStatus.FAILED;
        }
    }
}
