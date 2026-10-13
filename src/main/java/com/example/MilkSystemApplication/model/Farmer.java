package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "farmers", indexes = {
        @Index(name = "idx_farmer_tenant_phone", columnList = "tenantId, phone"),
        @Index(name = "idx_farmer_tenant_code", columnList = "tenantId, code")
})
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId = "default-tenant";

    private String code; // Farmer unique code inside dairy e.g. "F-001"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String village;
    private String address;
    private int buffaloCount;
    private int cowCount;

    // Banking & Payment Details
    private String bankAccountNo;
    private String ifscCode;
    private String upiId;

    // Authentication for Farmer Login
    @Column(nullable = false)
    private String password = "farmer123";

    private String role = "ROLE_FARMER";
    private boolean active = true;

    private LocalDate registrationDate;
    private LocalDate trialExpiryDate;
    private boolean isPremium = false;
    private LocalDate premiumExpiryDate;
    private String premiumTransactionRef;

    public Farmer() {
        this.tenantId = "default-tenant";
        this.registrationDate = LocalDate.now();
        this.trialExpiryDate = LocalDate.now().plusDays(15);
        this.password = "farmer123";
        this.role = "ROLE_FARMER";
        this.active = true;
    }

    public Farmer(String name, String phone, String village, int buffaloCount) {
        this();
        this.name = name;
        this.phone = phone;
        this.village = village;
        this.buffaloCount = buffaloCount;
    }

    public Farmer(String name, String phone, String village, String address, int buffaloCount,
                  String bankAccountNo, String ifscCode, String upiId, String password) {
        this();
        this.name = name;
        this.phone = phone;
        this.village = village;
        this.address = address;
        this.buffaloCount = buffaloCount;
        this.bankAccountNo = bankAccountNo;
        this.ifscCode = ifscCode;
        this.upiId = upiId;
        if (password != null && !password.trim().isEmpty()) {
            this.password = password;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getBuffaloCount() { return buffaloCount; }
    public void setBuffaloCount(int buffaloCount) { this.buffaloCount = buffaloCount; }

    public String getBankAccountNo() { return bankAccountNo; }
    public void setBankAccountNo(String bankAccountNo) { this.bankAccountNo = bankAccountNo; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public LocalDate getTrialExpiryDate() { return trialExpiryDate; }
    public void setTrialExpiryDate(LocalDate trialExpiryDate) { this.trialExpiryDate = trialExpiryDate; }

    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }

    public LocalDate getPremiumExpiryDate() { return premiumExpiryDate; }
    public void setPremiumExpiryDate(LocalDate premiumExpiryDate) { this.premiumExpiryDate = premiumExpiryDate; }

    public String getPremiumTransactionRef() { return premiumTransactionRef; }
    public void setPremiumTransactionRef(String premiumTransactionRef) { this.premiumTransactionRef = premiumTransactionRef; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public int getCowCount() { return cowCount; }
    public void setCowCount(int cowCount) { this.cowCount = cowCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}