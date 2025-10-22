package com.retail.controller;

import com.retail.model.Product;
import com.retail.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for product management endpoints
 */
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Creates a new product
     */
    public Product createProduct(String sku, String name, BigDecimal price, Integer stockQuantity) {
        Product product = new Product(sku, name, price, stockQuantity);
        return productRepository.save(product);
    }

    /**
     * Gets product by ID
     */
    public Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
    }

    /**
     * Gets product by SKU
     */
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
    }

    /**
     * Gets all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Gets products by category
     */
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * Updates product price
     */
    public Product updateProductPrice(Long productId, BigDecimal newPrice) {
        Product product = getProduct(productId);
        product.updatePrice(newPrice);
        return productRepository.save(product);
    }

    /**
     * Updates product stock
     */
    public Product updateProductStock(Long productId, Integer newStock) {
        Product product = getProduct(productId);
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    /**
     * Activates a product
     */
    public Product activateProduct(Long productId) {
        Product product = getProduct(productId);
        product.setStatus(Product.ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    /**
     * Deactivates a product
     */
    public Product deactivateProduct(Long productId) {
        Product product = getProduct(productId);
        product.setStatus(Product.ProductStatus.INACTIVE);
        return productRepository.save(product);
    }
}
