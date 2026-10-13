package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_slug", columnList = "slug", unique = true)
})
public class Tenant {

    @Id
    @Column(length = 64, nullable = false, updatable = false)
    private String id; // Unique Tenant Identifier (e.g. "tenant-abc12345")

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false, unique = true, length = 64)
    private String slug;

    @Column(nullable = false)
    private String contactEmail;

    private String phone;
    private String address;
    private String village;
    private String district;
    private String state;

    // SaaS Subscription & Lifecycle
    @Column(nullable = false)
    private String subscriptionStatus; // TRIAL, ACTIVE, EXPIRED, CANCELLED

    @Column(nullable = false)
    private String subscriptionPlan; // FREE_TRIAL, BASIC, PRO, ENTERPRISE

    private LocalDate trialEndsAt;
    private LocalDate subscriptionEndsAt;
    private String lastPaymentReference;

    // Pricing & Dairy Settings
    private double baseFatRate = 7.5; // Rate per FAT point (e.g. ₹7.5 / fat %)
    private double standardCowRate = 42.0;
    private double standardBuffaloRate = 65.0;
    private String currency = "INR";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Tenant() {
        this.subscriptionStatus = "TRIAL";
        this.subscriptionPlan = "FREE_TRIAL";
        this.trialEndsAt = LocalDate.now().plusDays(15);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Tenant(String id, String businessName, String slug, String contactEmail, String phone) {
        this();
        this.id = id;
        this.businessName = businessName;
        this.slug = slug != null ? slug.trim().toLowerCase() : id;
        this.contactEmail = contactEmail;
        this.phone = phone;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
        if (this.trialEndsAt == null) this.trialEndsAt = LocalDate.now().plusDays(15);
        if (this.subscriptionStatus == null) this.subscriptionStatus = "TRIAL";
        if (this.subscriptionPlan == null) this.subscriptionPlan = "FREE_TRIAL";
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAccessAllowed() {
        LocalDate today = LocalDate.now();
        if ("ACTIVE".equalsIgnoreCase(subscriptionStatus)) {
            return subscriptionEndsAt == null || !subscriptionEndsAt.isBefore(today);
        }
        if ("TRIAL".equalsIgnoreCase(subscriptionStatus)) {
            return trialEndsAt == null || !trialEndsAt.isBefore(today);
        }
        return false;
    }

    public long getDaysRemaining() {
        LocalDate today = LocalDate.now();
        if ("ACTIVE".equalsIgnoreCase(subscriptionStatus) && subscriptionEndsAt != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(today, subscriptionEndsAt);
        }
        if (trialEndsAt != null) {
            return java.time.temporal.ChronoUnit.DAYS.between(today, trialEndsAt);
        }
        return 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public LocalDate getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(LocalDate trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public LocalDate getSubscriptionEndsAt() { return subscriptionEndsAt; }
    public void setSubscriptionEndsAt(LocalDate subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public String getLastPaymentReference() { return lastPaymentReference; }
    public void setLastPaymentReference(String lastPaymentReference) { this.lastPaymentReference = lastPaymentReference; }

    public double getBaseFatRate() { return baseFatRate; }
    public void setBaseFatRate(double baseFatRate) { this.baseFatRate = baseFatRate; }

    public double getStandardCowRate() { return standardCowRate; }
    public void setStandardCowRate(double standardCowRate) { this.standardCowRate = standardCowRate; }

    public double getStandardBuffaloRate() { return standardBuffaloRate; }
    public void setStandardBuffaloRate(double standardBuffaloRate) { this.standardBuffaloRate = standardBuffaloRate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
