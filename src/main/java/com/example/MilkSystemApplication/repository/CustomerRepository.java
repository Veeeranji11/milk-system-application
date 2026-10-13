package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTenantId(String tenantId);
    Optional<Customer> findByIdAndTenantId(Long id, String tenantId);
    List<Customer> findByTenantIdAndNameContainingIgnoreCase(String tenantId, String name);
    List<Customer> findByTenantIdAndPhone(String tenantId, String phone);
    long countByTenantId(String tenantId);
}
