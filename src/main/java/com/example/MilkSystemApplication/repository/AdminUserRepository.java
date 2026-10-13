package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
    Optional<AdminUser> findByEmail(String email);
    Optional<AdminUser> findByPhone(String phone);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    java.util.List<AdminUser> findByTenantId(String tenantId);
    Optional<AdminUser> findByUsernameAndTenantId(String username, String tenantId);
}
