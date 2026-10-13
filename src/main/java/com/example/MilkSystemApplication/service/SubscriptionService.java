package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.AdminUser;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.repository.AdminUserRepository;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    public static final double ADMIN_PREMIUM_PRICE = 150.00; // ₹150 monthly subscription for dairy system
    public static final double FARMER_GROUP_30_PRICE = 100.00; // ₹100 for 30 users plan
    public static final double FARMER_PREMIUM_PRICE = 10.00;   // ₹10 per individual Farmer / month

    @Autowired
    private AdminUserRepository adminUserRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    public Map<String, Object> getAdminSubscriptionStatus(String username) {
        Map<String, Object> status = new HashMap<>();
        Optional<AdminUser> adminOpt = Optional.empty();
        if (username != null && !username.trim().isEmpty()) {
            adminOpt = adminUserRepo.findByUsername(username.trim().toLowerCase());
        }
        if (adminOpt.isEmpty()) {
            adminOpt = adminUserRepo.findAll().stream().findFirst();
        }

        if (adminOpt.isEmpty()) {
            status.put("isFound", true);
            status.put("username", "admin");
            status.put("fullName", "Dairy System Administrator");
            status.put("isPremium", true);
            status.put("planName", "Active Dairy Premium");
            status.put("statusLabel", "ACTIVE");
            status.put("daysRemaining", 30L);
            status.put("isTrialActive", true);
            status.put("isAccessAllowed", true);
            status.put("premiumPrice", ADMIN_PREMIUM_PRICE);
            status.put("farmerGroupPrice", FARMER_GROUP_30_PRICE);
            status.put("expiryDate", LocalDate.now().plusDays(30));
            return status;
        }

        AdminUser admin = adminOpt.get();
        LocalDate now = LocalDate.now();
        LocalDate expiry;
        boolean isPremiumActive = false;

        if (admin.isPremium() && admin.getPremiumExpiryDate() != null) {
            expiry = admin.getPremiumExpiryDate();
            isPremiumActive = !expiry.isBefore(now);
        } else {
            expiry = admin.getTrialExpiryDate() != null ? admin.getTrialExpiryDate() : admin.getRegistrationDate().plusDays(15);
        }

        long daysRemaining = ChronoUnit.DAYS.between(now, expiry);
        boolean isTrialActive = !admin.isPremium() && daysRemaining >= 0;
        boolean isAccessAllowed = isPremiumActive || isTrialActive;

        String statusLabel;
        if (isPremiumActive) {
            statusLabel = "ACTIVE (Monthly Plan)";
        } else if (isTrialActive) {
            statusLabel = "FREE TRIAL";
        } else {
            statusLabel = "EXPIRED (Payment Required)";
        }

        status.put("isFound", true);
        status.put("username", admin.getUsername());
        status.put("fullName", admin.getFullName() != null ? admin.getFullName() : admin.getUsername());
        status.put("isPremium", admin.isPremium());
        status.put("isPremiumActive", isPremiumActive);
        status.put("statusLabel", statusLabel);
        status.put("registrationDate", admin.getRegistrationDate());
        status.put("trialExpiryDate", admin.getTrialExpiryDate());
        status.put("premiumExpiryDate", admin.getPremiumExpiryDate());
        status.put("expiryDate", expiry);
        status.put("daysRemaining", Math.max(0, daysRemaining));
        status.put("isTrialActive", isTrialActive);
        status.put("isAccessAllowed", isAccessAllowed);
        status.put("premiumPrice", ADMIN_PREMIUM_PRICE);
        status.put("farmerGroupPrice", FARMER_GROUP_30_PRICE);
        status.put("lastPaymentRef", admin.getPremiumTransactionRef() != null ? admin.getPremiumTransactionRef() : "N/A");
        status.put("totalFarmers", farmerRepo.count());
        status.put("farmerQuota", 30);

        return status;
    }

    public Map<String, Object> getFarmerSubscriptionStatus(Long farmerId) {
        Map<String, Object> status = new HashMap<>();
        Optional<Farmer> farmerOpt = farmerRepo.findById(farmerId);

        if (farmerOpt.isEmpty()) {
            status.put("isFound", false);
            status.put("isPremium", false);
            status.put("daysRemaining", 0L);
            return status;
        }

        Farmer farmer = farmerOpt.get();
        LocalDate now = LocalDate.now();
        LocalDate expiry;
        boolean isPremiumActive = false;

        if (farmer.isPremium() && farmer.getPremiumExpiryDate() != null) {
            expiry = farmer.getPremiumExpiryDate();
            isPremiumActive = !expiry.isBefore(now);
        } else {
            expiry = farmer.getTrialExpiryDate() != null ? farmer.getTrialExpiryDate() : farmer.getRegistrationDate().plusDays(15);
        }

        long daysRemaining = ChronoUnit.DAYS.between(now, expiry);
        boolean isTrialActive = !farmer.isPremium() && daysRemaining >= 0;
        boolean isAccessAllowed = isPremiumActive || isTrialActive;

        status.put("isFound", true);
        status.put("farmerId", farmer.getId());
        status.put("farmerCode", farmer.getCode());
        status.put("farmerName", farmer.getName());
        status.put("isPremium", farmer.isPremium());
        status.put("isPremiumActive", isPremiumActive);
        status.put("registrationDate", farmer.getRegistrationDate());
        status.put("expiryDate", expiry);
        status.put("daysRemaining", Math.max(0, daysRemaining));
        status.put("isTrialActive", isTrialActive);
        status.put("isAccessAllowed", isAccessAllowed);
        status.put("premiumPrice", FARMER_PREMIUM_PRICE);
        status.put("groupPlanPrice", FARMER_GROUP_30_PRICE);

        return status;
    }

    public boolean activateAdminPremium(String username, String transactionRef, int months) {
        if (months <= 0) months = 1;
        Optional<AdminUser> adminOpt = Optional.empty();
        if (username != null && !username.trim().isEmpty()) {
            adminOpt = adminUserRepo.findByUsername(username.trim().toLowerCase());
        }
        if (adminOpt.isEmpty()) {
            adminOpt = adminUserRepo.findAll().stream().findFirst();
        }
        if (adminOpt.isEmpty()) return false;

        AdminUser admin = adminOpt.get();
        admin.setPremium(true);
        LocalDate baseDate = (admin.getPremiumExpiryDate() != null && admin.getPremiumExpiryDate().isAfter(LocalDate.now()))
                ? admin.getPremiumExpiryDate()
                : LocalDate.now();
        admin.setPremiumExpiryDate(baseDate.plusMonths(months));
        admin.setPremiumTransactionRef(transactionRef != null && !transactionRef.trim().isEmpty() ? transactionRef.trim() : "UPI-SUB-150");
        adminUserRepo.save(admin);
        return true;
    }

    public boolean activateAdminPremium(String username, String transactionRef) {
        return activateAdminPremium(username, transactionRef, 1);
    }

    public boolean activateFarmerPremium(Long farmerId, String transactionRef, int months) {
        if (months <= 0) months = 1;
        Optional<Farmer> farmerOpt = farmerRepo.findById(farmerId);
        if (farmerOpt.isEmpty()) return false;

        Farmer farmer = farmerOpt.get();
        farmer.setPremium(true);
        LocalDate baseDate = (farmer.getPremiumExpiryDate() != null && farmer.getPremiumExpiryDate().isAfter(LocalDate.now()))
                ? farmer.getPremiumExpiryDate()
                : LocalDate.now();
        farmer.setPremiumExpiryDate(baseDate.plusMonths(months));
        farmer.setPremiumTransactionRef(transactionRef != null && !transactionRef.trim().isEmpty() ? transactionRef.trim() : "UPI-FARMER-PLAN");
        farmerRepo.save(farmer);
        return true;
    }

    public boolean activateFarmerPremium(Long farmerId, String transactionRef) {
        return activateFarmerPremium(farmerId, transactionRef, 1);
    }

    public int activateFarmerGroupQuota(int maxFarmers, int months, String transactionRef) {
        if (months <= 0) months = 1;
        List<Farmer> farmers = farmerRepo.findAll().stream().limit(maxFarmers).toList();
        int count = 0;
        for (Farmer f : farmers) {
            f.setPremium(true);
            LocalDate baseDate = (f.getPremiumExpiryDate() != null && f.getPremiumExpiryDate().isAfter(LocalDate.now()))
                    ? f.getPremiumExpiryDate()
                    : LocalDate.now();
            f.setPremiumExpiryDate(baseDate.plusMonths(months));
            f.setPremiumTransactionRef(transactionRef != null && !transactionRef.trim().isEmpty() ? transactionRef.trim() : "UPI-GROUP-30");
            farmerRepo.save(f);
            count++;
        }
        return count;
    }
}
