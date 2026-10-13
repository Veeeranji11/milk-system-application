package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.AdminUser;
import com.example.MilkSystemApplication.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminUserService {

    @Autowired
    private AdminUserRepository adminUserRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminUser registerAdmin(String username, String rawPassword, String fullName, String email, String phone, String societyName) {
        if (adminUserRepo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (email != null && !email.trim().isEmpty() && adminUserRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        AdminUser admin = new AdminUser();
        admin.setUsername(username.trim().toLowerCase());
        admin.setPassword(passwordEncoder.encode(rawPassword.trim()));
        admin.setFullName(fullName != null ? fullName.trim() : username);
        admin.setEmail(email != null ? email.trim() : null);
        admin.setPhone(phone != null ? phone.trim() : null);
        admin.setSocietyName(societyName != null && !societyName.trim().isEmpty() ? societyName.trim() : "Village Milk Producers Society");
        admin.setRole("ROLE_ADMIN");
        admin.setRegistrationDate(LocalDate.now());
        admin.setTrialExpiryDate(LocalDate.now().plusDays(15));
        admin.setPremium(false);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        return adminUserRepo.save(admin);
    }

    public Optional<AdminUser> findByUsername(String username) {
        return adminUserRepo.findByUsername(username.trim().toLowerCase());
    }

    public Optional<AdminUser> findByEmailOrUsername(String identifier) {
        if (identifier == null) return Optional.empty();
        String id = identifier.trim().toLowerCase();
        Optional<AdminUser> byUser = adminUserRepo.findByUsername(id);
        if (byUser.isPresent()) return byUser;
        return adminUserRepo.findByEmail(id);
    }

    public boolean changeAdminPassword(String username, String oldPassword, String newPassword) {
        Optional<AdminUser> adminOpt = adminUserRepo.findByUsername(username.trim().toLowerCase());
        if (adminOpt.isEmpty()) return false;

        AdminUser admin = adminOpt.get();
        if (!passwordEncoder.matches(oldPassword, admin.getPassword()) && !oldPassword.equals(admin.getPassword())) {
            return false;
        }

        admin.setPassword(passwordEncoder.encode(newPassword.trim()));
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserRepo.save(admin);
        return true;
    }

    public AdminUser updateProfile(String username, String fullName, String email, String phone, String societyName) {
        AdminUser admin = adminUserRepo.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + username));

        if (fullName != null) admin.setFullName(fullName.trim());
        if (email != null) admin.setEmail(email.trim());
        if (phone != null) admin.setPhone(phone.trim());
        if (societyName != null) admin.setSocietyName(societyName.trim());
        admin.setUpdatedAt(LocalDateTime.now());

        return adminUserRepo.save(admin);
    }

    public void upgradeToPremium(String username, String txnRef) {
        AdminUser admin = adminUserRepo.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found: " + username));

        admin.setPremium(true);
        admin.setPremiumExpiryDate(LocalDate.now().plusYears(1));
        admin.setPremiumTransactionRef(txnRef != null ? txnRef.trim() : "UPI-DIRECT");
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserRepo.save(admin);
    }
}
