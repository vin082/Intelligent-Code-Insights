package com.retail.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Customer entity representing retail customers
 * Manages customer information, loyalty, and order history
 */
public class Customer {

    private Long customerId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private CustomerStatus status;
    private CustomerTier tier;
    private Integer loyaltyPoints;
    private String primaryAddress;
    private String secondaryAddress;
    private List<Order> orderHistory;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastPurchaseDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum CustomerStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        BLOCKED,
        PENDING_VERIFICATION
    }

    public enum CustomerTier {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND
    }

    public Customer() {
        this.status = CustomerStatus.PENDING_VERIFICATION;
        this.tier = CustomerTier.BRONZE;
        this.loyaltyPoints = 0;
        this.orderHistory = new ArrayList<>();
        this.registrationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Customer(String email, String firstName, String lastName) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Business logic methods

    /**
     * Gets customer's full name
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adds loyalty points to customer account
     * @param points points to add
     */
    public void addLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points must be non-negative");
        }
        this.loyaltyPoints += points;
        updateTierBasedOnPoints();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Redeems loyalty points
     * @param points points to redeem
     * @throws IllegalStateException if insufficient points
     */
    public void redeemLoyaltyPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points must be non-negative");
        }
        if (this.loyaltyPoints < points) {
            throw new IllegalStateException("Insufficient loyalty points");
        }
        this.loyaltyPoints -= points;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates customer tier based on loyalty points
     */
    private void updateTierBasedOnPoints() {
        if (loyaltyPoints >= 50000) {
            this.tier = CustomerTier.DIAMOND;
        } else if (loyaltyPoints >= 25000) {
            this.tier = CustomerTier.PLATINUM;
        } else if (loyaltyPoints >= 10000) {
            this.tier = CustomerTier.GOLD;
        } else if (loyaltyPoints >= 5000) {
            this.tier = CustomerTier.SILVER;
        } else {
            this.tier = CustomerTier.BRONZE;
        }
    }

    /**
     * Gets discount percentage based on customer tier
     * @return discount percentage
     */
    public double getTierDiscount() {
        switch (tier) {
            case DIAMOND:
                return 0.20; // 20% discount
            case PLATINUM:
                return 0.15; // 15% discount
            case GOLD:
                return 0.10; // 10% discount
            case SILVER:
                return 0.05; // 5% discount
            case BRONZE:
            default:
                return 0.0;  // No discount
        }
    }

    /**
     * Adds an order to customer's history
     * @param order the order to add
     */
    public void addOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        this.orderHistory.add(order);
        this.lastPurchaseDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if customer is eligible for promotions
     * @return true if customer is active and verified
     */
    public boolean isEligibleForPromotions() {
        return this.status == CustomerStatus.ACTIVE;
    }

    /**
     * Activates the customer account
     */
    public void activate() {
        if (this.status == CustomerStatus.BLOCKED) {
            throw new IllegalStateException("Cannot activate blocked customer");
        }
        this.status = CustomerStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspends the customer account
     */
    public void suspend() {
        this.status = CustomerStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Blocks the customer account
     */
    public void block() {
        this.status = CustomerStatus.BLOCKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates last login timestamp
     */
    public void recordLogin() {
        this.lastLoginDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets total number of orders placed
     * @return order count
     */
    public int getTotalOrders() {
        return orderHistory != null ? orderHistory.size() : 0;
    }

    // Getters and Setters

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public void setStatus(CustomerStatus status) {
        this.status = status;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public void setTier(CustomerTier tier) {
        this.tier = tier;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(Integer loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(String primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public String getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setSecondaryAddress(String secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
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
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId) &&
               Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", email='" + email + '\'' +
                ", name='" + getFullName() + '\'' +
                ", tier=" + tier +
                ", loyaltyPoints=" + loyaltyPoints +
                ", status=" + status +
                '}';
    }
}
