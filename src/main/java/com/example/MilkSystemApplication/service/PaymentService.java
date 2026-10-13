package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.MilkCollectionRepository;
import com.example.MilkSystemApplication.repository.PaymentRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private MilkCollectionRepository milkRepo;

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }

    public Payment addPayment(Long farmerId, double amount, LocalDate paymentDate, String paymentMethod, String transactionRef, String notes) {
        if (farmerId == null) {
            throw new IllegalArgumentException("Farmer ID must be provided.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        Payment payment = new Payment();
        payment.setTenantId(getTenantId());
        payment.setFarmerId(farmerId);
        payment.setAmount(Math.round(amount * 100.0) / 100.0);
        payment.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        payment.setPaymentMethod(paymentMethod != null && !paymentMethod.trim().isEmpty() ? paymentMethod.toUpperCase() : "CASH");
        payment.setTransactionRef(transactionRef != null ? transactionRef.trim() : null);
        payment.setNotes(notes != null ? notes.trim() : null);
        payment.setStatus("PAID");
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepo.save(payment);
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = paymentRepo.findByTenantIdOrderByPaymentDateDescIdDesc(getTenantId());
        populateFarmerNames(list);
        return list;
    }

    public List<Payment> getPaymentsByFarmer(Long farmerId) {
        List<Payment> list = paymentRepo.findByTenantIdAndFarmerIdOrderByPaymentDateDescIdDesc(getTenantId(), farmerId);
        populateFarmerNames(list);
        return list;
    }

    public List<Payment> getPaymentsBetweenDates(LocalDate start, LocalDate end) {
        List<Payment> list = paymentRepo.findByTenantIdAndPaymentDateBetweenOrderByPaymentDateDescIdDesc(getTenantId(), start, end);
        populateFarmerNames(list);
        return list;
    }

    public List<Payment> getPaymentsByDate(LocalDate date) {
        List<Payment> list = paymentRepo.findByPaymentDateOrderByCreatedAtDesc(date);
        populateFarmerNames(list);
        return list;
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepo.findByIdAndTenantId(id, getTenantId());
    }

    public void deletePayment(Long id) {
        paymentRepo.findByIdAndTenantId(id, getTenantId()).ifPresent(paymentRepo::delete);
    }

    public double getTotalPaid() {
        return Math.round(paymentRepo.getTotalPaidAmountByTenant(getTenantId()) * 100.0) / 100.0;
    }

    public double getTotalPaidByFarmer(Long farmerId) {
        return Math.round(paymentRepo.getTotalPaidByTenantAndFarmer(getTenantId(), farmerId) * 100.0) / 100.0;
    }

    public double getPendingAmountByFarmer(Long farmerId) {
        double earned = milkRepo.getTotalAmountByTenantAndFarmer(getTenantId(), farmerId);
        double paid = paymentRepo.getTotalPaidByTenantAndFarmer(getTenantId(), farmerId);
        return Math.max(0.0, Math.round((earned - paid) * 100.0) / 100.0);
    }

    public double getTotalPendingAmount() {
        double totalEarned = milkRepo.getTotalAmount();
        double totalPaid = paymentRepo.getTotalPaidAmountByTenant(getTenantId());
        return Math.max(0.0, Math.round((totalEarned - totalPaid) * 100.0) / 100.0);
    }

    private void populateFarmerNames(List<Payment> payments) {
        if (payments == null || payments.isEmpty()) return;
        Map<Long, String> farmerMap = farmerRepo.findByTenantIdOrderByNameAsc(getTenantId()).stream()
                .collect(Collectors.toMap(Farmer::getId, Farmer::getName, (e1, e2) -> e1));
        for (Payment p : payments) {
            p.setFarmerName(farmerMap.getOrDefault(p.getFarmerId(), "Farmer #" + p.getFarmerId()));
        }
    }
}