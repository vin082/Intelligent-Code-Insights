package com.retail.service;

import com.retail.model.Order;
import com.retail.model.Customer;
import com.retail.model.Product;
import com.retail.util.EmailClient;
import com.retail.util.SmsClient;

/**
 * Service for sending notifications to customers and staff
 */
public class NotificationService {

    private final EmailClient emailClient;
    private final SmsClient smsClient;

    public NotificationService(EmailClient emailClient, SmsClient smsClient) {
        this.emailClient = emailClient;
        this.smsClient = smsClient;
    }

    public void sendOrderConfirmation(Order order, Customer customer) {
        String subject = "Order Confirmation - " + order.getOrderNumber();
        String message = buildOrderConfirmationMessage(order);
        emailClient.sendEmail(customer.getEmail(), subject, message);
    }

    public void sendShippingNotification(Order order, Customer customer) {
        String subject = "Your order has shipped - " + order.getOrderNumber();
        String message = "Your order has been shipped. Tracking number: " + order.getTrackingNumber();
        emailClient.sendEmail(customer.getEmail(), subject, message);

        if (customer.getPhoneNumber() != null) {
            smsClient.sendSms(customer.getPhoneNumber(), "Your order " + order.getOrderNumber() + " has shipped!");
        }
    }

    public void sendDeliveryNotification(Order order, Customer customer) {
        String subject = "Order Delivered - " + order.getOrderNumber();
        String message = "Your order has been delivered. Thank you for shopping with us!";
        emailClient.sendEmail(customer.getEmail(), subject, message);
    }

    public void sendCancellationNotification(Order order, Customer customer) {
        String subject = "Order Cancelled - " + order.getOrderNumber();
        String message = "Your order has been cancelled. Reason: " + order.getCancelReason();
        emailClient.sendEmail(customer.getEmail(), subject, message);
    }

    public void sendLowStockAlert(Product product) {
        String subject = "Low Stock Alert - " + product.getSku();
        String message = "Product " + product.getName() + " is low on stock. Current: " +
                        product.getStockQuantity() + ", Reorder level: " + product.getReorderLevel();
        emailClient.sendEmail("inventory@retail.com", subject, message);
    }

    private String buildOrderConfirmationMessage(Order order) {
        return "Thank you for your order!\n\n" +
               "Order Number: " + order.getOrderNumber() + "\n" +
               "Total Amount: $" + order.getTotalAmount() + "\n" +
               "Items: " + order.getItemCount() + "\n\n" +
               "We'll notify you when your order ships.";
    }
}
