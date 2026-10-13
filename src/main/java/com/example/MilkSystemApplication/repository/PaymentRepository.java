package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByFarmerIdOrderByPaymentDateDescIdDesc(Long farmerId);

    List<Payment> findAllByOrderByPaymentDateDescIdDesc();

    List<Payment> findByPaymentDateOrderByCreatedAtDesc(LocalDate date);

    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.status = 'PAID'")
    double getTotalPaidAmount();

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.farmerId = :farmerId AND p.status = 'PAID'")
    double getTotalPaidByFarmer(@Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.paymentDate = :date AND p.status = 'PAID'")
    double getPaidAmountByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.status = 'PAID'")
    double getPaidAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Multi-tenant queries
    List<Payment> findByTenantIdOrderByPaymentDateDescIdDesc(String tenantId);
    List<Payment> findByTenantIdAndFarmerIdOrderByPaymentDateDescIdDesc(String tenantId, Long farmerId);
    List<Payment> findByTenantIdAndCustomerIdOrderByPaymentDateDescIdDesc(String tenantId, Long customerId);
    List<Payment> findByTenantIdAndPaymentTypeOrderByPaymentDateDescIdDesc(String tenantId, String paymentType);
    List<Payment> findByTenantIdAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(String tenantId, LocalDate startDate, LocalDate endDate);
    Optional<Payment> findByIdAndTenantId(Long id, String tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.tenantId = :tenantId AND p.status = 'PAID'")
    double getTotalPaidAmountByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.tenantId = :tenantId AND p.farmerId = :farmerId AND p.status = 'PAID'")
    double getTotalPaidByTenantAndFarmer(@Param("tenantId") String tenantId, @Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.tenantId = :tenantId AND p.paymentDate = :date AND p.status = 'PAID'")
    double getPaidAmountByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);
}