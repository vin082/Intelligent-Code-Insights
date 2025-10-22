package com.retail.repository;

import com.retail.model.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order data access
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long orderId);
    List<Order> findAll();
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    void deleteById(Long orderId);
}
