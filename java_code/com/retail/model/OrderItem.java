package com.retail.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * OrderItem entity representing individual line items in an order
 */
public class OrderItem {

    private Long orderItemId;
    private Order order;
    private Product product;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal totalPrice;

    public OrderItem() {
        this.quantity = 1;
        this.discount = BigDecimal.ZERO;
    }

    public OrderItem(Product product, Integer quantity) {
        this();
        this.product = product;
        this.productId = product.getProductId();
        this.productName = product.getName();
        this.productSku = product.getSku();
        this.unitPrice = product.getPrice();
        this.quantity = quantity;
        this.totalPrice = calculateTotal();
    }

    /**
     * Calculates total price for this line item
     * @return total price (quantity * unitPrice - discount)
     */
    public BigDecimal calculateTotal() {
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        this.totalPrice = subtotal.subtract(discount);
        return this.totalPrice;
    }

    /**
     * Updates quantity and recalculates total
     * @param newQuantity the new quantity
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculateTotal();
    }

    // Getters and Setters

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(orderItemId, orderItem.orderItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
