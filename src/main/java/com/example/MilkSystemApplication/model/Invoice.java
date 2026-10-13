package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_inv_tenant_num", columnList = "tenantId, invoiceNumber", unique = true),
        @Index(name = "idx_inv_tenant_recipient", columnList = "tenantId, recipientId, invoiceType")
})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 64)
    private String invoiceNumber;

    @Column(nullable = false, length = 32)
    private String invoiceType; // FARMER_SETTLEMENT, CUSTOMER_BILL

    @Column(nullable = false)
    private Long recipientId; // Farmer ID or Customer ID

    @Column(nullable = false)
    private String recipientName;
    private String recipientPhone;

    private LocalDate periodStartDate;
    private LocalDate periodEndDate;

    private double totalLitres;
    private double averageFat;
    private double subtotal;
    private double bonusOrIncentive = 0.0;
    private double deductions = 0.0; // Cattle feed, advances, etc.
    private double netAmount;

    @Column(nullable = false, length = 32)
    private String status = "PENDING"; // PENDING, PAID, CANCELLED

    @Column(length = 512)
    private String pdfUrl; // Cloud object storage or public URL

    private LocalDate billDate;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
    private String paymentReference;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Invoice() {
        this.billDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (billDate == null) billDate = LocalDate.now();
        if (status == null) status = "PENDING";
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }

    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public void setPeriodEndDate(LocalDate periodEndDate) { this.periodEndDate = periodEndDate; }

    public double getTotalLitres() { return totalLitres; }
    public void setTotalLitres(double totalLitres) { this.totalLitres = totalLitres; }

    public double getAverageFat() { return averageFat; }
    public void setAverageFat(double averageFat) { this.averageFat = averageFat; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getBonusOrIncentive() { return bonusOrIncentive; }
    public void setBonusOrIncentive(double bonusOrIncentive) { this.bonusOrIncentive = bonusOrIncentive; }

    public double getDeductions() { return deductions; }
    public void setDeductions(double deductions) { this.deductions = deductions; }

    public double getNetAmount() { return netAmount; }
    public void setNetAmount(double netAmount) { this.netAmount = netAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public LocalDate getBillDate() { return billDate; }
    public void setBillDate(LocalDate billDate) { this.billDate = billDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
