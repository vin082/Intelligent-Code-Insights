package com.retail.service;

import com.retail.model.Product;
import com.retail.repository.ProductRepository;
import com.retail.exception.InsufficientStockException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing inventory and stock operations
 * Handles stock tracking, reservations, and reordering
 */
public class InventoryService {

    private final ProductRepository productRepository;
    private final Map<Long, Integer> reservedStock; // productId -> reserved quantity
    private final NotificationService notificationService;

    public InventoryService(ProductRepository productRepository,
                           NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.reservedStock = new HashMap<>();
    }

    /**
     * Checks if sufficient stock is available for a product
     * @param productId the product ID
     * @param requestedQuantity the requested quantity
     * @return true if stock is available
     */
    public boolean checkAvailability(Long productId, int requestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int reserved = reservedStock.getOrDefault(productId, 0);
        int availableStock = product.getStockQuantity() - reserved;

        return availableStock >= requestedQuantity;
    }

    /**
     * Reserves stock for an order
     * @param productId the product ID
     * @param quantity the quantity to reserve
     */
    public void reserveStock(Long productId, int quantity) {
        if (!checkAvailability(productId, quantity)) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                    ". Requested: " + quantity + ", Available: " + (product.getStockQuantity() - reservedStock.getOrDefault(productId, 0))
            );
        }

        int currentReserved = reservedStock.getOrDefault(productId, 0);
        reservedStock.put(productId, currentReserved + quantity);
    }

    /**
     * Releases reserved stock (e.g., when order is cancelled)
     * @param productId the product ID
     * @param quantity the quantity to release
     */
    public void releaseReservedStock(Long productId, int quantity) {
        int currentReserved = reservedStock.getOrDefault(productId, 0);
        int newReserved = Math.max(0, currentReserved - quantity);
        reservedStock.put(productId, newReserved);
    }

    /**
     * Reduces actual stock quantity (e.g., when order is shipped)
     * @param productId the product ID
     * @param quantity the quantity to reduce
     */
    public void reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.reduceStock(quantity);
        productRepository.save(product);

        // Release the reservation
        releaseReservedStock(productId, quantity);

        // Check if reorder is needed
        if (product.needsReorder()) {
            notificationService.sendLowStockAlert(product);
        }
    }

    /**
     * Adds stock to inventory (e.g., when receiving new shipment)
     * @param productId the product ID
     * @param quantity the quantity to add
     */
    public void addStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.addStock(quantity);
        productRepository.save(product);
    }

    /**
     * Sets reorder level for a product
     * @param productId the product ID
     * @param reorderLevel the reorder level threshold
     */
    public void setReorderLevel(Long productId, int reorderLevel) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        product.setReorderLevel(reorderLevel);
        productRepository.save(product);
    }

    /**
     * Gets all products that need reordering
     * @return list of products needing reorder
     */
    public List<Product> getProductsNeedingReorder() {
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(Product::needsReorder)
                .collect(Collectors.toList());
    }

    /**
     * Gets available stock for a product (actual - reserved)
     * @param productId the product ID
     * @return available stock quantity
     */
    public int getAvailableStock(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int reserved = reservedStock.getOrDefault(productId, 0);
        return product.getStockQuantity() - reserved;
    }

    /**
     * Gets reserved stock for a product
     * @param productId the product ID
     * @return reserved stock quantity
     */
    public int getReservedStock(Long productId) {
        return reservedStock.getOrDefault(productId, 0);
    }

    /**
     * Performs inventory audit and returns discrepancies
     * @param actualCounts map of productId to actual counted quantity
     * @return map of productId to discrepancy amount
     */
    public Map<Long, Integer> performInventoryAudit(Map<Long, Integer> actualCounts) {
        Map<Long, Integer> discrepancies = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : actualCounts.entrySet()) {
            Long productId = entry.getKey();
            Integer actualCount = entry.getValue();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

            int systemCount = product.getStockQuantity();
            int discrepancy = actualCount - systemCount;

            if (discrepancy != 0) {
                discrepancies.put(productId, discrepancy);
                // Adjust system count to match actual
                product.setStockQuantity(actualCount);
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }
        }

        return discrepancies;
    }
}
