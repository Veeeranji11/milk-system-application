package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findByContactEmail(String contactEmail);
    boolean existsBySlug(String slug);
    boolean existsByContactEmail(String contactEmail);
}
