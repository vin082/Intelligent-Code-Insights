package com.retail.util;

/**
 * Utility class for sending SMS messages
 */
public class SmsClient {

    private final String apiKey;

    public SmsClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Sends an SMS message
     * @param phoneNumber recipient phone number
     * @param message SMS message content
     */
    public void sendSms(String phoneNumber, String message) {
        // Simulate SMS sending
        System.out.println("Sending SMS to: " + phoneNumber);
        System.out.println("Message: " + message);
    }
}
