package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    List<Farmer> findByNameContainingIgnoreCaseOrPhoneContainingOrVillageContaining(String name, String phone, String village);
    List<Farmer> findByNameContainingIgnoreCase(String name);
    Optional<Farmer> findByPhone(String phone);
    
    @Query("SELECT f FROM Farmer f ORDER BY f.name ASC")
    List<Farmer> findAllOrderByName();

    // Tenant-isolated queries
    List<Farmer> findByTenantIdOrderByNameAsc(String tenantId);
    Optional<Farmer> findByIdAndTenantId(Long id, String tenantId);
    Optional<Farmer> findByTenantIdAndPhone(String tenantId, String phone);
    Optional<Farmer> findByTenantIdAndCode(String tenantId, String code);
    Optional<Farmer> findByTenantIdAndCodeIgnoreCase(String tenantId, String code);
    Optional<Farmer> findByCode(String code);
    Optional<Farmer> findByCodeIgnoreCase(String code);
    List<Farmer> findByTenantIdAndNameContainingIgnoreCaseOrTenantIdAndPhoneContaining(String tenantId, String name, String tenantId2, String phone);
    long countByTenantId(String tenantId);
}