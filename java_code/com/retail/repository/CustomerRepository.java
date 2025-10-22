package com.retail.repository;

import com.retail.model.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer data access
 */
public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(Long customerId);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    List<Customer> findByStatus(Customer.CustomerStatus status);
    List<Customer> findByTier(Customer.CustomerTier tier);
    void deleteById(Long customerId);
}
