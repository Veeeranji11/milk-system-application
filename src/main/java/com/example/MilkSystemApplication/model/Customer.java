package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_cust_tenant_phone", columnList = "tenantId, phone")
})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;
    private String address;
    private String customerType = "RETAIL"; // RETAIL, WHOLESALE, COMMERCIAL

    private double dailyQuotaLitres = 1.0;
    private double pricePerLitre = 55.0;
    private double currentBalance = 0.0; // Positive means customer owes dairy money

    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Customer() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Customer(String tenantId, String name, String phone, String address, double dailyQuotaLitres, double pricePerLitre) {
        this();
        this.tenantId = tenantId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.dailyQuotaLitres = dailyQuotaLitres;
        this.pricePerLitre = pricePerLitre;
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
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

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCustomerType() { return customerType; }
    public void setCustomerType(String customerType) { this.customerType = customerType; }

    public double getDailyQuotaLitres() { return dailyQuotaLitres; }
    public void setDailyQuotaLitres(double dailyQuotaLitres) { this.dailyQuotaLitres = dailyQuotaLitres; }

    public double getPricePerLitre() { return pricePerLitre; }
    public void setPricePerLitre(double pricePerLitre) { this.pricePerLitre = pricePerLitre; }

    public double getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(double currentBalance) { this.currentBalance = currentBalance; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
