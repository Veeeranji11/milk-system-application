package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.MilkSale;
import com.example.MilkSystemApplication.repository.MilkSaleRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MilkSaleService {

    @Autowired
    private MilkSaleRepository saleRepo;

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }

    public MilkSale recordSale(String buyerName, String buyerPhone, String buyerAddress, LocalDate saleDate,
                               String shift, double litres, double ratePerLitre, String paymentMethod,
                               String paymentStatus, String transactionRef, String notes) {
        if (buyerName == null || buyerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Buyer / Customer Name is required.");
        }
        if (litres <= 0) {
            throw new IllegalArgumentException("Milk litres must be greater than zero.");
        }
        if (ratePerLitre <= 0) {
            ratePerLitre = 60.00;
        }

        MilkSale sale = new MilkSale();
        sale.setTenantId(getTenantId());
        sale.setBuyerName(buyerName.trim());
        sale.setBuyerPhone(buyerPhone != null ? buyerPhone.trim() : null);
        sale.setBuyerAddress(buyerAddress != null ? buyerAddress.trim() : null);
        sale.setSaleDate(saleDate != null ? saleDate : LocalDate.now());
        sale.setShift(shift != null ? shift : "Morning");
        sale.setLitres(Math.round(litres * 100.0) / 100.0);
        sale.setRatePerLitre(Math.round(ratePerLitre * 100.0) / 100.0);
        sale.setTotalAmount(Math.round(sale.getLitres() * sale.getRatePerLitre() * 100.0) / 100.0);
        sale.setPaymentMethod(paymentMethod != null ? paymentMethod.toUpperCase() : "CASH");
        sale.setPaymentStatus(paymentStatus != null ? paymentStatus.toUpperCase() : "PAID");
        sale.setTransactionRef(transactionRef != null ? transactionRef.trim() : null);
        sale.setNotes(notes != null ? notes.trim() : null);

        return saleRepo.save(sale);
    }

    public List<MilkSale> getAllSales() {
        return saleRepo.findByTenantIdOrderBySaleDateDescIdDesc(getTenantId());
    }

    public List<MilkSale> getSalesByDate(LocalDate date) {
        return saleRepo.findByTenantIdAndSaleDateOrderByCreatedAtDesc(getTenantId(), date);
    }

    public Optional<MilkSale> getSaleById(Long id) {
        return saleRepo.findByIdAndTenantId(id, getTenantId());
    }

    public void deleteSale(Long id) {
        saleRepo.findByIdAndTenantId(id, getTenantId()).ifPresent(saleRepo::delete);
    }

    public double getTodaySoldLitres() {
        Double val = saleRepo.getTotalSoldLitresByTenantAndDate(getTenantId(), LocalDate.now());
        return val != null ? Math.round(val * 100.0) / 100.0 : 0.0;
    }

    public double getTodaySalesRevenue() {
        Double val = saleRepo.getTotalSalesRevenueByTenantAndDate(getTenantId(), LocalDate.now());
        return val != null ? Math.round(val * 100.0) / 100.0 : 0.0;
    }

    public double getTotalSoldLitres() {
        Double val = saleRepo.getTotalSoldLitresByTenant(getTenantId());
        return val != null ? Math.round(val * 100.0) / 100.0 : 0.0;
    }

    public double getTotalSalesRevenue() {
        Double val = saleRepo.getTotalSalesRevenueByTenant(getTenantId());
        return val != null ? Math.round(val * 100.0) / 100.0 : 0.0;
    }
}
