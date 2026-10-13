package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.MilkSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface MilkSaleRepository extends JpaRepository<MilkSale, Long> {

    List<MilkSale> findAllByOrderBySaleDateDescIdDesc();

    List<MilkSale> findBySaleDateOrderByCreatedAtDesc(LocalDate date);

    List<MilkSale> findBySaleDateBetweenOrderBySaleDateDescIdDesc(LocalDate start, LocalDate end);

    List<MilkSale> findByBuyerNameContainingIgnoreCaseOrBuyerPhoneContaining(String name, String phone);

    @Query("SELECT COALESCE(SUM(s.litres), 0.0) FROM MilkSale s WHERE s.saleDate = :date")
    Double getTotalSoldLitresByDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0.0) FROM MilkSale s WHERE s.saleDate = :date")
    Double getTotalSalesRevenueByDate(LocalDate date);

    @Query("SELECT COALESCE(SUM(s.litres), 0.0) FROM MilkSale s")
    Double getTotalSoldLitres();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0.0) FROM MilkSale s")
    Double getTotalSalesRevenue();

    // Multi-tenant queries
    List<MilkSale> findByTenantIdOrderBySaleDateDescIdDesc(String tenantId);
    List<MilkSale> findByTenantIdAndSaleDateOrderByCreatedAtDesc(String tenantId, LocalDate date);
    List<MilkSale> findByTenantIdAndSaleDateBetweenOrderBySaleDateDescIdDesc(String tenantId, LocalDate start, LocalDate end);
    Optional<MilkSale> findByIdAndTenantId(Long id, String tenantId);

    @Query("SELECT COALESCE(SUM(s.litres), 0.0) FROM MilkSale s WHERE s.tenantId = :tenantId AND s.saleDate = :date")
    Double getTotalSoldLitresByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0.0) FROM MilkSale s WHERE s.tenantId = :tenantId AND s.saleDate = :date")
    Double getTotalSalesRevenueByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(s.litres), 0.0) FROM MilkSale s WHERE s.tenantId = :tenantId")
    Double getTotalSoldLitresByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0.0) FROM MilkSale s WHERE s.tenantId = :tenantId")
    Double getTotalSalesRevenueByTenant(@Param("tenantId") String tenantId);
}
