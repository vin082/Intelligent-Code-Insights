package com.example.controller;

import com.example.model.Order;
import com.example.model.OrderStatus;
import com.example.service.OrderService;
import com.example.dto.OrderRequest;
import com.example.dto.OrderResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Order Management
 * Handles HTTP requests for order operations
 * Endpoints: Create, Read, Update, Cancel orders
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Creates a new order
     * POST /api/v1/orders
     * @param orderRequest The order creation request
     * @return The created order with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            // Validate order request
            if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Create order through service
            Order order = orderService.createOrder(orderRequest);

            // Convert to response DTO
            OrderResponse response = OrderResponse.fromOrder(order);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves an order by ID
     * GET /api/v1/orders/{orderId}
     * @param orderId The order UUID
     * @return The order details or 404 if not found
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        try {
            Order order = orderService.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

            OrderResponse response = OrderResponse.fromOrder(order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all orders for a customer
     * GET /api/v1/orders?customerId={customerId}
     * @param customerId The customer UUID
     * @return List of orders for the customer
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(
            @RequestParam UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<Order> orders = orderService.findOrdersByCustomer(customerId, page, size);

            List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::fromOrder)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates order status
     * PATCH /api/v1/orders/{orderId}/status
     * @param orderId The order UUID
     * @param newStatus The new status to set
     * @return Updated order
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus newStatus) {

        try {
            Order order = orderService.updateOrderStatus(orderId, newStatus)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

            OrderResponse response = OrderResponse.fromOrder(order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Invalid status transition
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancels an order
     * POST /api/v1/orders/{orderId}/cancel
     * @param orderId The order UUID to cancel
     * @return Cancelled order or error
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        try {
            Order order = orderService.cancelOrder(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

            OrderResponse response = OrderResponse.fromOrder(order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // Order cannot be cancelled (already shipped, etc.)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes an order (soft delete)
     * DELETE /api/v1/orders/{orderId}
     * @param orderId The order UUID to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID orderId) {
        try {
            boolean deleted = orderService.deleteOrder(orderId);

            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets order statistics for admin dashboard
     * GET /api/v1/orders/stats
     * @return Order statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatistics> getOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            OrderStatistics stats = orderService.getOrderStatistics(startDate, endDate);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Exception handler for OrderNotFoundException
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException e) {
        ErrorResponse error = new ErrorResponse("ORDER_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Custom exception for order not found scenarios
     */
    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    /**
     * Error response DTO
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }

    /**
     * Order statistics DTO
     */
    public static class OrderStatistics {
        private long totalOrders;
        private long completedOrders;
        private long cancelledOrders;
        private double totalRevenue;
        private double averageOrderValue;

        // Constructor, getters, setters...
        public OrderStatistics(long totalOrders, long completedOrders, long cancelledOrders,
                             double totalRevenue, double averageOrderValue) {
            this.totalOrders = totalOrders;
            this.completedOrders = completedOrders;
            this.cancelledOrders = cancelledOrders;
            this.totalRevenue = totalRevenue;
            this.averageOrderValue = averageOrderValue;
        }

        // Getters
        public long getTotalOrders() { return totalOrders; }
        public long getCompletedOrders() { return completedOrders; }
        public long getCancelledOrders() { return cancelledOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAverageOrderValue() { return averageOrderValue; }
    }
}
