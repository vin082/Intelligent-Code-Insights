package com.retail.controller;

import com.retail.model.Order;
import com.retail.service.OrderService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for order management endpoints
 */
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order
     */
    public Order createOrder(Long customerId, String shippingAddress) {
        return orderService.createOrder(customerId, shippingAddress);
    }

    /**
     * Adds product to order
     */
    public Order addProductToOrder(Long orderId, Long productId, int quantity) {
        return orderService.addProductToOrder(orderId, productId, quantity);
    }

    /**
     * Confirms and processes order
     */
    public Order confirmOrder(Long orderId, String paymentMethod) {
        return orderService.confirmOrder(orderId, paymentMethod);
    }

    /**
     * Ships an order
     */
    public Order shipOrder(Long orderId, String trackingNumber) {
        return orderService.shipOrder(orderId, trackingNumber);
    }

    /**
     * Cancels an order
     */
    public Order cancelOrder(Long orderId, String reason) {
        return orderService.cancelOrder(orderId, reason);
    }

    /**
     * Gets order by ID
     */
    public Order getOrder(Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /**
     * Gets all orders for a customer
     */
    public List<Order> getCustomerOrders(Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    /**
     * Gets orders by status
     */
    public List<Order> getOrdersByStatus(String status) {
        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        return orderService.getOrdersByStatus(orderStatus);
    }

    /**
     * Gets orders within date range
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderService.getOrdersByDateRange(startDate, endDate);
    }
}
