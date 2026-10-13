package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "milk_collections", indexes = {
        @Index(name = "idx_coll_tenant_date", columnList = "tenantId, date"),
        @Index(name = "idx_coll_tenant_farmer", columnList = "tenantId, farmerId")
})
public class MilkCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId = "default-tenant";

    @Column(nullable = false)
    private Long farmerId;

    @Column(nullable = false)
    private LocalDate date;

    private String shift = "Morning"; // "Morning" or "Evening"
    private String milkType = "BUFFALO"; // "COW" or "BUFFALO"
    private double litres;
    private double fatPercentage;
    private double snfPercentage = 8.5;
    private double ratePerLitre;
    private double amount;

    private String quality = "Good"; // e.g. "Standard", "Good", "Excellent / Grade A"
    private LocalTime collectionTime = LocalTime.now();

    @Transient
    private String farmerName;

    public MilkCollection() {
        this.tenantId = "default-tenant";
        this.date = LocalDate.now();
        this.shift = "Morning";
        this.milkType = "BUFFALO";
        this.quality = "Good";
        this.collectionTime = LocalTime.now();
    }

    public MilkCollection(Long farmerId, LocalDate date, String shift, double litres, double fatPercentage, double ratePerLitre, double amount) {
        this();
        this.farmerId = farmerId;
        this.date = date != null ? date : LocalDate.now();
        this.shift = shift != null ? shift : "Morning";
        this.litres = litres;
        this.fatPercentage = fatPercentage;
        this.ratePerLitre = ratePerLitre;
        this.amount = amount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public double getLitres() { return litres; }
    public void setLitres(double litres) { this.litres = litres; }
    public double getFatPercentage() { return fatPercentage; }
    public void setFatPercentage(double fatPercentage) { this.fatPercentage = fatPercentage; }
    public double getRatePerLitre() { return ratePerLitre; }
    public void setRatePerLitre(double ratePerLitre) { this.ratePerLitre = ratePerLitre; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMilkType() { return milkType; }
    public void setMilkType(String milkType) { this.milkType = milkType; }
    public double getSnfPercentage() { return snfPercentage; }
    public void setSnfPercentage(double snfPercentage) { this.snfPercentage = snfPercentage; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public LocalTime getCollectionTime() { return collectionTime; }
    public void setCollectionTime(LocalTime collectionTime) { this.collectionTime = collectionTime; }

    public boolean isMorningEditLocked() {
        if (!"Morning".equalsIgnoreCase(this.shift)) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (this.date == null || this.date.isBefore(today)) {
            return true; // Past morning records are permanently locked
        }
        if (this.date.equals(today)) {
            // After 12:00 PM (noon), morning records cannot be edited
            return LocalTime.now().isAfter(LocalTime.of(12, 0));
        }
        return false;
    }
}