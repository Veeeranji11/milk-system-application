package com.example.MilkSystemApplication.dto;

import java.time.LocalDate;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;

    // Tenant & Subscription metadata
    private String tenantId;
    private String businessName;
    private String subscriptionStatus;
    private String subscriptionPlan;
    private LocalDate trialEndsAt;
    private LocalDate subscriptionEndsAt;
    private long daysRemaining;
    private boolean isAccessAllowed;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String username, String email, String fullName, String role,
                        String tenantId, String businessName, String subscriptionStatus, String subscriptionPlan,
                        LocalDate trialEndsAt, LocalDate subscriptionEndsAt, long daysRemaining, boolean isAccessAllowed) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.tenantId = tenantId;
        this.businessName = businessName;
        this.subscriptionStatus = subscriptionStatus;
        this.subscriptionPlan = subscriptionPlan;
        this.trialEndsAt = trialEndsAt;
        this.subscriptionEndsAt = subscriptionEndsAt;
        this.daysRemaining = daysRemaining;
        this.isAccessAllowed = isAccessAllowed;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public LocalDate getTrialEndsAt() { return trialEndsAt; }
    public void setTrialEndsAt(LocalDate trialEndsAt) { this.trialEndsAt = trialEndsAt; }

    public LocalDate getSubscriptionEndsAt() { return subscriptionEndsAt; }
    public void setSubscriptionEndsAt(LocalDate subscriptionEndsAt) { this.subscriptionEndsAt = subscriptionEndsAt; }

    public long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }

    public boolean isAccessAllowed() { return isAccessAllowed; }
    public void setAccessAllowed(boolean accessAllowed) { isAccessAllowed = accessAllowed; }
}
