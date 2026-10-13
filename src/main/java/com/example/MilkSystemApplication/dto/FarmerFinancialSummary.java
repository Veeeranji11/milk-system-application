package com.example.MilkSystemApplication.dto;

import com.example.MilkSystemApplication.model.Farmer;

public class FarmerFinancialSummary {
    private Farmer farmer;
    private double totalLitres;
    private double totalEarned;
    private double totalPaid;
    private double pendingBalance;
    private long totalSuppliesCount;

    public FarmerFinancialSummary() {}

    public FarmerFinancialSummary(Farmer farmer, double totalLitres, double totalEarned, double totalPaid, long totalSuppliesCount) {
        this.farmer = farmer;
        this.totalLitres = Math.round(totalLitres * 100.0) / 100.0;
        this.totalEarned = Math.round(totalEarned * 100.0) / 100.0;
        this.totalPaid = Math.round(totalPaid * 100.0) / 100.0;
        this.pendingBalance = Math.round((this.totalEarned - this.totalPaid) * 100.0) / 100.0;
        this.totalSuppliesCount = totalSuppliesCount;
    }

    public Farmer getFarmer() { return farmer; }
    public void setFarmer(Farmer farmer) { this.farmer = farmer; }

    public Long getFarmerId() { return farmer != null ? farmer.getId() : null; }
    public String getFarmerName() { return farmer != null ? farmer.getName() : ""; }

    public double getTotalLitres() { return totalLitres; }
    public void setTotalLitres(double totalLitres) { this.totalLitres = totalLitres; }

    public double getTotalEarned() { return totalEarned; }
    public void setTotalEarned(double totalEarned) { this.totalEarned = totalEarned; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getPendingBalance() { return pendingBalance; }
    public void setPendingBalance(double pendingBalance) { this.pendingBalance = pendingBalance; }

    public long getTotalSuppliesCount() { return totalSuppliesCount; }
    public void setTotalSuppliesCount(long totalSuppliesCount) { this.totalSuppliesCount = totalSuppliesCount; }
}