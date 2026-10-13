package com.example.MilkSystemApplication.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String tenantId = "default-tenant";

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String phone;
    private String societyName;
    private String role = "ROLE_ADMIN";

    private LocalDate registrationDate;
    private LocalDate trialExpiryDate;
    private boolean isPremium = false;
    private LocalDate premiumExpiryDate;
    private String premiumTransactionRef;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminUser() {
        this.registrationDate = LocalDate.now();
        this.trialExpiryDate = LocalDate.now().plusDays(15);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public AdminUser(String username, String password, String fullName, String email, String phone, String societyName) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.societyName = societyName;
        this.role = "ROLE_ADMIN";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSocietyName() { return societyName; }
    public void setSocietyName(String societyName) { this.societyName = societyName; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
}
