package com.retail.service;

import com.retail.model.Order;
import com.retail.model.OrderItem;
import com.retail.model.Product;
import com.retail.model.Customer;
import com.retail.repository.OrderRepository;
import com.retail.repository.ProductRepository;
import com.retail.repository.CustomerRepository;
import com.retail.exception.InsufficientStockException;
import com.retail.exception.OrderNotFoundException;
import com.retail.exception.InvalidOrderStateException;
import com.retail.util.OrderNumberGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for order management operations
 * Handles order creation, modification, fulfillment, and cancellation
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OrderNumberGenerator orderNumberGenerator;

    public OrderService(OrderRepository orderRepository,
                       ProductRepository productRepository,
                       CustomerRepository customerRepository,
                       InventoryService inventoryService,
                       PaymentService paymentService,
                       NotificationService notificationService,
                       OrderNumberGenerator orderNumberGenerator) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    /**
     * Creates a new order for a customer
     * @param customerId the customer ID
     * @param shippingAddress shipping address
     * @return created order
     */
    public Order createOrder(Long customerId, String shippingAddress) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        if (!customer.isEligibleForPromotions()) {
            throw new IllegalStateException("Customer is not eligible to place orders");
        }

        String orderNumber = orderNumberGenerator.generateOrderNumber();
        Order order = new Order(orderNumber, customerId);
        order.setShippingAddress(shippingAddress);
        order.setCreatedBy("SYSTEM");

        return orderRepository.save(order);
    }

    /**
     * Adds a product to an order
     * @param orderId the order ID
     * @param productId the product ID
     * @param quantity the quantity to add
     * @return updated order
     */
    public Order addProductToOrder(Long orderId, Long productId, int quantity) {
        Order order = getOrderById(orderId);

        if (!order.isModifiable()) {
            throw new InvalidOrderStateException("Order cannot be modified in current state: " + order.getStatus());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!product.isAvailable()) {
            throw new IllegalStateException("Product is not available: " + product.getName());
        }

        // Check stock availability
        if (!inventoryService.checkAvailability(productId, quantity)) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        OrderItem orderItem = new OrderItem(product, quantity);
        order.addItem(orderItem);

        return orderRepository.save(order);
    }

    /**
     * Removes a product from an order
     * @param orderId the order ID
     * @param orderItemId the order item ID
     * @return updated order
     */
    public Order removeProductFromOrder(Long orderId, Long orderItemId) {
        Order order = getOrderById(orderId);

        if (!order.isModifiable()) {
            throw new InvalidOrderStateException("Order cannot be modified");
        }

        OrderItem itemToRemove = order.getOrderItems().stream()
                .filter(item -> item.getOrderItemId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Order item not found"));

        order.removeItem(itemToRemove);

        return orderRepository.save(order);
    }

    /**
     * Confirms an order and processes payment
     * @param orderId the order ID
     * @param paymentMethod the payment method
     * @return confirmed order
     */
    public Order confirmOrder(Long orderId, String paymentMethod) {
        Order order = getOrderById(orderId);

        // Validate order can be confirmed
        if (order.getOrderItems().isEmpty()) {
            throw new IllegalStateException("Cannot confirm order with no items");
        }

        // Reserve inventory for all items
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.reserveStock(item.getProductId(), item.getQuantity());
        }

        // Calculate tax (8% tax rate example)
        order.calculateTax(0.08);

        // Apply customer tier discount
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        BigDecimal tierDiscount = order.getSubtotal()
                .multiply(BigDecimal.valueOf(customer.getTierDiscount()));
        if (tierDiscount.compareTo(BigDecimal.ZERO) > 0) {
            order.applyDiscount(tierDiscount);
        }

        // Process payment
        order.setPaymentMethod(paymentMethod);
        boolean paymentSuccess = paymentService.processPayment(
                order.getOrderId(),
                order.getTotalAmount(),
                paymentMethod,
                customer
        );

        if (!paymentSuccess) {
            // Release reserved inventory
            for (OrderItem item : order.getOrderItems()) {
                inventoryService.releaseReservedStock(item.getProductId(), item.getQuantity());
            }
            throw new IllegalStateException("Payment processing failed");
        }

        order.setPaymentStatus(Order.PaymentStatus.CAPTURED);
        order.confirmOrder();

        // Award loyalty points (1 point per dollar spent)
        int loyaltyPoints = order.getTotalAmount().intValue();
        customer.addLoyaltyPoints(loyaltyPoints);
        customerRepository.save(customer);

        // Send confirmation notification
        notificationService.sendOrderConfirmation(order, customer);

        return orderRepository.save(order);
    }

    /**
     * Ships an order
     * @param orderId the order ID
     * @param trackingNumber the tracking number
     * @return shipped order
     */
    public Order shipOrder(Long orderId, String trackingNumber) {
        Order order = getOrderById(orderId);

        // Reduce actual inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.reduceStock(item.getProductId(), item.getQuantity());
        }

        order.shipOrder(trackingNumber);

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        notificationService.sendShippingNotification(order, customer);

        return orderRepository.save(order);
    }

    /**
     * Marks an order as delivered
     * @param orderId the order ID
     * @return delivered order
     */
    public Order deliverOrder(Long orderId) {
        Order order = getOrderById(orderId);
        order.deliverOrder();

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        notificationService.sendDeliveryNotification(order, customer);

        return orderRepository.save(order);
    }

    /**
     * Cancels an order
     * @param orderId the order ID
     * @param reason the cancellation reason
     * @return cancelled order
     */
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        if (!order.isCancellable()) {
            throw new InvalidOrderStateException("Order cannot be cancelled in current state: " + order.getStatus());
        }

        // Release inventory
        for (OrderItem item : order.getOrderItems()) {
            inventoryService.releaseReservedStock(item.getProductId(), item.getQuantity());
        }

        // Process refund if payment was captured
        if (order.getPaymentStatus() == Order.PaymentStatus.CAPTURED) {
            paymentService.refundPayment(order.getOrderId(), order.getTotalAmount());
            order.setPaymentStatus(Order.PaymentStatus.REFUNDED);
        }

        order.cancelOrder(reason);

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        notificationService.sendCancellationNotification(order, customer);

        return orderRepository.save(order);
    }

    /**
     * Retrieves an order by ID
     * @param orderId the order ID
     * @return the order
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    /**
     * Retrieves all orders for a customer
     * @param customerId the customer ID
     * @return list of orders
     */
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Retrieves orders by status
     * @param status the order status
     * @return list of orders
     */
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /**
     * Retrieves orders placed within a date range
     * @param startDate start date
     * @param endDate end date
     * @return list of orders
     */
    public List<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    /**
     * Calculates total revenue from completed orders
     * @param startDate start date
     * @param endDate end date
     * @return total revenue
     */
    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return orders.stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Updates order shipping address
     * @param orderId the order ID
     * @param newAddress the new address
     * @return updated order
     */
    public Order updateShippingAddress(Long orderId, String newAddress) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot update address for shipped or delivered orders");
        }

        order.setShippingAddress(newAddress);
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
