package com.retail.repository;

import com.retail.model.Product;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product data access
 */
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(Long productId);
    Optional<Product> findBySku(String sku);
    List<Product> findAll();
    List<Product> findByCategory(String category);
    List<Product> findByStatus(Product.ProductStatus status);
    void deleteById(Long productId);
}
