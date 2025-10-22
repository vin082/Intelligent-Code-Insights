package com.retail.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for generating unique order numbers
 */
public class OrderNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong sequence = new AtomicLong(1000);

    /**
     * Generates a unique order number
     * Format: ORD-YYYYMMDD-SEQUENCE
     * @return unique order number
     */
    public String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        long sequencePart = sequence.getAndIncrement();
        return String.format("ORD-%s-%06d", datePart, sequencePart);
    }

    /**
     * Resets the sequence counter (for testing purposes)
     */
    public void resetSequence() {
        sequence.set(1000);
    }
}
