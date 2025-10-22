package com.retail.util;

/**
 * Utility class for sending emails
 */
public class EmailClient {

    private final String smtpHost;
    private final int smtpPort;

    public EmailClient(String smtpHost, int smtpPort) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    /**
     * Sends an email
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     */
    public void sendEmail(String to, String subject, String body) {
        // Simulate email sending
        System.out.println("Sending email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + body);
    }
}
