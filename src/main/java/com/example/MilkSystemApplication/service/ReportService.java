package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.MilkCollectionRepository;
import com.example.MilkSystemApplication.repository.PaymentRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private MilkCollectionRepository milkRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private FarmerService farmerService;

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }

    public Map<String, Object> getDailyReport(LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<MilkCollection> collections = milkRepo.findByTenantIdAndDateOrderByShiftDesc(getTenantId(), queryDate);

        // Populate farmer names
        Map<Long, String> farmerMap = farmerRepo.findByTenantIdOrderByNameAsc(getTenantId()).stream()
                .collect(Collectors.toMap(Farmer::getId, Farmer::getName, (e1, e2) -> e1));
        for (MilkCollection mc : collections) {
            mc.setFarmerName(farmerMap.getOrDefault(mc.getFarmerId(), "Farmer #" + mc.getFarmerId()));
        }

        double morningLitres = collections.stream().filter(c -> "Morning".equalsIgnoreCase(c.getShift())).mapToDouble(MilkCollection::getLitres).sum();
        double eveningLitres = collections.stream().filter(c -> "Evening".equalsIgnoreCase(c.getShift())).mapToDouble(MilkCollection::getLitres).sum();
        double totalLitres = collections.stream().mapToDouble(MilkCollection::getLitres).sum();
        double totalAmount = collections.stream().mapToDouble(MilkCollection::getAmount).sum();
        double avgFat = collections.isEmpty() ? 0.0 : collections.stream().mapToDouble(MilkCollection::getFatPercentage).average().orElse(0.0);

        Map<String, Object> report = new HashMap<>();
        report.put("date", queryDate);
        report.put("collections", collections);
        report.put("morningLitres", Math.round(morningLitres * 100.0) / 100.0);
        report.put("eveningLitres", Math.round(eveningLitres * 100.0) / 100.0);
        report.put("totalLitres", Math.round(totalLitres * 100.0) / 100.0);
        report.put("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        report.put("avgFat", Math.round(avgFat * 100.0) / 100.0);
        report.put("entriesCount", collections.size());

        return report;
    }

    public Map<String, Object> getMonthlyReport(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        List<MilkCollection> collections = milkRepo.findByTenantIdAndDateBetweenOrderByDateDescIdDesc(getTenantId(), startDate, endDate);
        List<Payment> payments = paymentRepo.findByTenantIdAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(getTenantId(), startDate, endDate);

        double totalLitres = collections.stream().mapToDouble(MilkCollection::getLitres).sum();
        double totalEarned = collections.stream().mapToDouble(MilkCollection::getAmount).sum();
        double totalPaid = payments.stream().mapToDouble(Payment::getAmount).sum();
        double avgFat = collections.isEmpty() ? 0.0 : collections.stream().mapToDouble(MilkCollection::getFatPercentage).average().orElse(0.0);

        Map<String, Object> report = new HashMap<>();
        report.put("yearMonth", ym.toString());
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalLitres", Math.round(totalLitres * 100.0) / 100.0);
        report.put("totalEarned", Math.round(totalEarned * 100.0) / 100.0);
        report.put("totalPaid", Math.round(totalPaid * 100.0) / 100.0);
        report.put("netMonthlyBalance", Math.round((totalEarned - totalPaid) * 100.0) / 100.0);
        report.put("avgFat", Math.round(avgFat * 100.0) / 100.0);
        report.put("collectionsCount", collections.size());
        report.put("paymentsCount", payments.size());

        return report;
    }

    public List<FarmerFinancialSummary> getFarmerWiseReport() {
        return farmerService.getAllFarmerSummaries();
    }

    public List<FarmerFinancialSummary> getPendingPaymentsReport() {
        return farmerService.getAllFarmerSummaries().stream()
                .filter(summary -> summary.getPendingBalance() > 0)
                .collect(Collectors.toList());
    }
}