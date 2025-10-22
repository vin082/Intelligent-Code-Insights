package com.retail.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Order entity representing customer orders in the retail system
 * Manages order lifecycle, items, payments, and shipping information
 */
public class Order {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private List<OrderItem> orderItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private String shippingAddress;
    private String billingAddress;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    private String cancelReason;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED,
        REFUNDED
    }

    public enum PaymentStatus {
        PENDING,
        AUTHORIZED,
        CAPTURED,
        FAILED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }

    public Order() {
        this.orderItems = new ArrayList<>();
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
        this.orderDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.subtotal = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.shippingCost = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(String orderNumber, Long customerId) {
        this();
        this.orderNumber = orderNumber;
        this.customerId = customerId;
    }

    // Business logic methods

    /**
     * Adds an item to the order
     * @param orderItem the item to add
     */
    public void addItem(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to non-pending order");
        }
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        recalculateTotals();
    }

    /**
     * Removes an item from the order
     * @param orderItem the item to remove
     */
    public void removeItem(OrderItem orderItem) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot remove items from non-pending order");
        }
        this.orderItems.remove(orderItem);
        recalculateTotals();
    }

    /**
     * Recalculates all order totals based on current items
     */
    public void recalculateTotals() {
        this.subtotal = orderItems.stream()
                .map(OrderItem::calculateTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = subtotal
                .add(taxAmount)
                .add(shippingCost)
                .subtract(discountAmount);

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Applies a discount to the order
     * @param discount the discount amount
     */
    public void applyDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount must be non-negative");
        }
        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException("Discount cannot exceed subtotal");
        }
        this.discountAmount = discount;
        recalculateTotals();
    }

    /**
     * Calculates tax amount based on subtotal
     * @param taxRate the tax rate (e.g., 0.08 for 8%)
     */
    public void calculateTax(double taxRate) {
        if (taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        this.taxAmount = subtotal.multiply(BigDecimal.valueOf(taxRate))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        recalculateTotals();
    }

    /**
     * Confirms the order and transitions to CONFIRMED status
     */
    public void confirmOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        if (this.orderItems.isEmpty()) {
            throw new IllegalStateException("Cannot confirm order with no items");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks order as shipped with tracking information
     * @param trackingNumber the shipping tracking number
     */
    public void shipOrder(String trackingNumber) {
        if (this.status != OrderStatus.PROCESSING && this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed or processing to ship");
        }
        this.status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.shippedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks order as delivered
     */
    public void deliverOrder() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
        this.deliveredDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the order with a reason
     * @param reason the cancellation reason
     */
    public void cancelOrder(String reason) {
        if (this.status == OrderStatus.DELIVERED ||
            this.status == OrderStatus.CANCELLED ||
            this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException("Cannot cancel order in current status: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if order can be modified
     * @return true if order is in PENDING status
     */
    public boolean isModifiable() {
        return this.status == OrderStatus.PENDING;
    }

    /**
     * Checks if order can be cancelled
     * @return true if order can be cancelled
     */
    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING ||
               this.status == OrderStatus.CONFIRMED ||
               this.status == OrderStatus.PROCESSING;
    }

    /**
     * Gets the number of items in the order
     * @return total item count
     */
    public int getItemCount() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    // Getters and Setters

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
        recalculateTotals();
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(LocalDateTime shippedDate) {
        this.shippedDate = shippedDate;
    }

    public LocalDateTime getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(LocalDateTime deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public LocalDateTime getCancelledDate() {
        return cancelledDate;
    }

    public void setCancelledDate(LocalDateTime cancelledDate) {
        this.cancelledDate = cancelledDate;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId) &&
               Objects.equals(orderNumber, order.orderNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, orderNumber);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderNumber='" + orderNumber + '\'' +
                ", customerId=" + customerId +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", itemCount=" + getItemCount() +
                '}';
    }
}
