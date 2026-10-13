package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_tenant_date", columnList = "tenantId, paymentDate"),
        @Index(name = "idx_pay_tenant_farmer", columnList = "tenantId, farmerId")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId = "default-tenant";

    private Long farmerId;
    private Long customerId;

    @Column(nullable = false, length = 32)
    private String paymentType = "FARMER_PAYOUT"; // FARMER_PAYOUT, CUSTOMER_RECEIPT

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    // CASH, BANK_TRANSFER, UPI
    @Column(nullable = false)
    private String paymentMethod = "CASH";

    private String transactionRef;
    private String notes;

    // PAID, PENDING, CANCELLED
    private String status = "PAID";

    private LocalDateTime createdAt;

    @Transient
    private String farmerName;

    @Transient
    private String customerName;

    public Payment() {
        this.tenantId = "default-tenant";
        this.paymentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.status = "PAID";
        this.paymentMethod = "CASH";
        this.paymentType = "FARMER_PAYOUT";
    }

    public Payment(Long farmerId, double amount, LocalDate paymentDate, String paymentMethod, String transactionRef, String notes) {
        this();
        this.farmerId = farmerId;
        this.amount = amount;
        this.paymentDate = paymentDate != null ? paymentDate : LocalDate.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.transactionRef = transactionRef;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
}