package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_settings")
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double rateFactor = 7.50;
    private double basePricePerLitre = 50.00;

    private String societyName = "Village Milk Collection Center";
    private String societyContact = "9876543210";
    private String societyAddress = "Main Road, Village Center";

    private String societyUpiId = "tnagaveeranjineyulu@oksbi";
    private String societyUpiName = "T Naga veeranjineyulu";
    private String societyQrImage = "/images/payment_qr.jpg";

    private LocalDateTime updatedAt;

    public AppSetting() {
        this.rateFactor = 7.50;
        this.basePricePerLitre = 50.00;
        this.societyName = "Village Milk Collection Center";
        this.societyUpiId = "tnagaveeranjineyulu@oksbi";
        this.societyUpiName = "T Naga veeranjineyulu";
        this.societyQrImage = "/images/payment_qr.jpg";
        this.updatedAt = LocalDateTime.now();
    }

    public AppSetting(double rateFactor, double basePricePerLitre, String societyName) {
        this();
        this.rateFactor = rateFactor;
        this.basePricePerLitre = basePricePerLitre;
        this.societyName = societyName;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getRateFactor() { return rateFactor; }
    public void setRateFactor(double rateFactor) { this.rateFactor = rateFactor; }

    public double getBasePricePerLitre() { return basePricePerLitre; }
    public void setBasePricePerLitre(double basePricePerLitre) { this.basePricePerLitre = basePricePerLitre; }

    public String getSocietyName() { return societyName; }
    public void setSocietyName(String societyName) { this.societyName = societyName; }

    public String getSocietyContact() { return societyContact; }
    public void setSocietyContact(String societyContact) { this.societyContact = societyContact; }

    public String getSocietyAddress() { return societyAddress; }
    public void setSocietyAddress(String societyAddress) { this.societyAddress = societyAddress; }

    public String getSocietyUpiId() { return societyUpiId; }
    public void setSocietyUpiId(String societyUpiId) { this.societyUpiId = societyUpiId; }

    public String getSocietyUpiName() { return societyUpiName; }
    public void setSocietyUpiName(String societyUpiName) { this.societyUpiName = societyUpiName; }

    public String getSocietyQrImage() { return societyQrImage; }
    public void setSocietyQrImage(String societyQrImage) { this.societyQrImage = societyQrImage; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}