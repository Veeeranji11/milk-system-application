package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milk_sales", indexes = {
        @Index(name = "idx_sale_tenant_date", columnList = "tenantId, saleDate")
})
public class MilkSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId = "default-tenant";

    private Long customerId;

    @Column(nullable = false)
    private String buyerName;

    private String buyerPhone;
    private String buyerAddress;

    @Column(nullable = false)
    private LocalDate saleDate;

    private String shift = "Morning";

    @Column(nullable = false)
    private double litres;

    @Column(nullable = false)
    private double ratePerLitre = 60.00;

    @Column(nullable = false)
    private double totalAmount;

    private String paymentMethod = "CASH"; // CASH, UPI
    private String paymentStatus = "PAID"; // PAID, PENDING
    private String transactionRef;
    private String notes;

    private LocalDateTime createdAt;

    public MilkSale() {
        this.saleDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.paymentStatus = "PAID";
        this.paymentMethod = "CASH";
    }

    public MilkSale(String buyerName, String buyerPhone, LocalDate saleDate, String shift, double litres, double ratePerLitre, String paymentMethod, String paymentStatus) {
        this();
        this.buyerName = buyerName;
        this.buyerPhone = buyerPhone;
        this.saleDate = saleDate != null ? saleDate : LocalDate.now();
        this.shift = shift != null ? shift : "Morning";
        this.litres = Math.round(litres * 100.0) / 100.0;
        this.ratePerLitre = Math.round(ratePerLitre * 100.0) / 100.0;
        this.totalAmount = Math.round(this.litres * this.ratePerLitre * 100.0) / 100.0;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PAID";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerPhone() { return buyerPhone; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }

    public String getBuyerAddress() { return buyerAddress; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }

    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public double getLitres() { return litres; }
    public void setLitres(double litres) {
        this.litres = Math.round(litres * 100.0) / 100.0;
        if (this.ratePerLitre > 0) {
            this.totalAmount = Math.round(this.litres * this.ratePerLitre * 100.0) / 100.0;
        }
    }

    public double getRatePerLitre() { return ratePerLitre; }
    public void setRatePerLitre(double ratePerLitre) {
        this.ratePerLitre = Math.round(ratePerLitre * 100.0) / 100.0;
        if (this.litres > 0) {
            this.totalAmount = Math.round(this.litres * this.ratePerLitre * 100.0) / 100.0;
        }
    }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = Math.round(totalAmount * 100.0) / 100.0; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
}
