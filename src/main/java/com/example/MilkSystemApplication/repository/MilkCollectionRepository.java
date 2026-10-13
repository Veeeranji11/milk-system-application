package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.MilkCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilkCollectionRepository extends JpaRepository<MilkCollection, Long> {
    List<MilkCollection> findByFarmerId(Long farmerId);

    List<MilkCollection> findByFarmerIdOrderByDateDescIdDesc(Long farmerId);

    List<MilkCollection> findByDateOrderByShiftDesc(LocalDate date);

    List<MilkCollection> findAllByOrderByDateDescIdDesc();

    List<MilkCollection> findByDateBetweenOrderByDateDescIdDesc(LocalDate startDate, LocalDate endDate);

    List<MilkCollection> findByFarmerIdAndDateBetweenOrderByDateDescIdDesc(Long farmerId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m")
    double getTotalLitres();

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m")
    double getTotalAmount();

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.date = :date")
    double getLitresByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.date = :date")
    double getAmountByDate(@Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.farmerId = :farmerId")
    double getTotalLitresByFarmer(@Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.farmerId = :farmerId")
    double getTotalAmountByFarmer(@Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.date BETWEEN :startDate AND :endDate")
    double getLitresBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.date BETWEEN :startDate AND :endDate")
    double getAmountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.date = :date AND m.shift = :shift")
    double getLitresByDateAndShift(@Param("date") LocalDate date, @Param("shift") String shift);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.date = :date AND m.shift = :shift")
    double getAmountByDateAndShift(@Param("date") LocalDate date, @Param("shift") String shift);

    // Multi-tenant scoped queries
    List<MilkCollection> findByTenantIdOrderByDateDescIdDesc(String tenantId);
    List<MilkCollection> findByTenantIdAndDateOrderByShiftDesc(String tenantId, LocalDate date);
    List<MilkCollection> findByTenantIdAndFarmerIdOrderByDateDescIdDesc(String tenantId, Long farmerId);
    List<MilkCollection> findByTenantIdAndDateBetweenOrderByDateDescIdDesc(String tenantId, LocalDate startDate, LocalDate endDate);
    List<MilkCollection> findByTenantIdAndFarmerIdAndDateBetweenOrderByDateDescIdDesc(String tenantId, Long farmerId, LocalDate startDate, LocalDate endDate);
    Optional<MilkCollection> findByIdAndTenantId(Long id, String tenantId);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.date = :date")
    double getLitresByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.date = :date")
    double getAmountByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.date BETWEEN :startDate AND :endDate")
    double getLitresBetweenDatesByTenant(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.date BETWEEN :startDate AND :endDate")
    double getAmountBetweenDatesByTenant(@Param("tenantId") String tenantId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(AVG(m.fatPercentage), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.date = :date")
    double getAvgFatByTenantAndDate(@Param("tenantId") String tenantId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(m.litres), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.farmerId = :farmerId")
    double getTotalLitresByTenantAndFarmer(@Param("tenantId") String tenantId, @Param("farmerId") Long farmerId);

    @Query("SELECT COALESCE(SUM(m.amount), 0.0) FROM MilkCollection m WHERE m.tenantId = :tenantId AND m.farmerId = :farmerId")
    double getTotalAmountByTenantAndFarmer(@Param("tenantId") String tenantId, @Param("farmerId") Long farmerId);

    long countByTenantId(String tenantId);

    @Query("SELECT m FROM MilkCollection m WHERE m.tenantId = :tenantId " +
           "AND (:farmerId IS NULL OR m.farmerId = :farmerId) " +
           "AND (:date IS NULL OR m.date = :date) " +
           "AND (:quality IS NULL OR :quality = '' OR LOWER(m.quality) = LOWER(:quality)) " +
           "ORDER BY m.date DESC, m.id DESC")
    List<MilkCollection> searchCollections(
            @Param("tenantId") String tenantId,
            @Param("farmerId") Long farmerId,
            @Param("date") LocalDate date,
            @Param("quality") String quality);

    @Query("SELECT m FROM MilkCollection m WHERE m.tenantId = :tenantId " +
           "AND m.farmerId = :farmerId " +
           "AND (:startDate IS NULL OR m.date >= :startDate) " +
           "AND (:endDate IS NULL OR m.date <= :endDate) " +
           "AND (:shift IS NULL OR :shift = '' OR LOWER(m.shift) = LOWER(:shift)) " +
           "AND (:quality IS NULL OR :quality = '' OR LOWER(m.quality) = LOWER(:quality)) " +
           "ORDER BY m.date DESC, m.id DESC")
    List<MilkCollection> searchFarmerCollections(
            @Param("tenantId") String tenantId,
            @Param("farmerId") Long farmerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("shift") String shift,
            @Param("quality") String quality);
}