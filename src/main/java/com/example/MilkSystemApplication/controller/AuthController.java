package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.dto.AuthRequest;
import com.example.MilkSystemApplication.dto.AuthResponse;
import com.example.MilkSystemApplication.dto.RegisterRequest;
import com.example.MilkSystemApplication.model.AdminUser;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.AdminUserRepository;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.security.JwtTokenProvider;
import com.example.MilkSystemApplication.security.UserPrincipal;
import com.example.MilkSystemApplication.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<?> registerBusiness(@Valid @RequestBody RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (adminUserRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists"));
        }

        // Generate slug
        String rawSlug = request.getSlug() != null && !request.getSlug().trim().isEmpty()
                ? request.getSlug().trim().toLowerCase().replaceAll("[^a-z0-9-]", "-")
                : request.getBusinessName().trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");

        if (rawSlug.endsWith("-")) rawSlug = rawSlug.substring(0, rawSlug.length() - 1);
        String finalSlug = rawSlug;
        if (tenantRepository.existsBySlug(finalSlug)) {
            finalSlug = rawSlug + "-" + System.currentTimeMillis() % 10000;
        }

        // 1. Create Tenant with 15-Day Free Trial
        String tenantId = "dairy-" + UUID.randomUUID().toString().substring(0, 8);
        Tenant tenant = new Tenant(tenantId, request.getBusinessName().trim(), finalSlug, email, request.getPhone().trim());
        tenant.setAddress(request.getAddress());
        tenant.setVillage(request.getVillage());
        tenant.setDistrict(request.getDistrict());
        tenant.setState(request.getState());
        tenant.setSubscriptionStatus("TRIAL");
        tenant.setSubscriptionPlan("FREE_TRIAL");
        tenant.setTrialEndsAt(LocalDate.now().plusDays(15));
        tenantRepository.save(tenant);

        // 2. Create Owner Admin User
        String username = email.split("@")[0] + "_" + (System.currentTimeMillis() % 1000);
        AdminUser admin = new AdminUser();
        admin.setTenantId(tenantId);
        admin.setUsername(username.toLowerCase());
        admin.setEmail(email);
        admin.setFullName(request.getFullName().trim());
        admin.setPhone(request.getPhone().trim());
        admin.setSocietyName(request.getBusinessName().trim());
        admin.setRole("ROLE_ADMIN");
        admin.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        admin.setTrialExpiryDate(LocalDate.now().plusDays(15));
        adminUserRepository.save(admin);

        // 3. Generate JWT Token
        UserPrincipal principal = new UserPrincipal(
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getPassword(),
                tenantId,
                admin.getFullName(),
                admin.getRole()
        );
        String token = tokenProvider.generateToken(principal);

        AuthResponse response = new AuthResponse(
                token,
                admin.getId(),
                admin.getUsername(),
                admin.getEmail(),
                admin.getFullName(),
                admin.getRole(),
                tenant.getId(),
                tenant.getBusinessName(),
                tenant.getSubscriptionStatus(),
                tenant.getSubscriptionPlan(),
                tenant.getTrialEndsAt(),
                tenant.getSubscriptionEndsAt(),
                tenant.getDaysRemaining(),
                tenant.isAccessAllowed()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        String identifier = request.getUsername().trim().toLowerCase();
        String password = request.getPassword().trim();

        // 1. Try Admin Login (by username, email, or phone)
        Optional<AdminUser> adminOpt = adminUserRepository.findByUsername(identifier);
        if (adminOpt.isEmpty()) adminOpt = adminUserRepository.findByEmail(identifier);
        if (adminOpt.isEmpty()) adminOpt = adminUserRepository.findByPhone(identifier);

        if (adminOpt.isPresent()) {
            AdminUser admin = adminOpt.get();
            if (passwordEncoder.matches(password, admin.getPassword()) || password.equals(admin.getPassword())) {
                // Fetch tenant
                String tenantId = admin.getTenantId() != null ? admin.getTenantId() : "default-tenant";
                Tenant tenant = tenantRepository.findById(tenantId)
                        .orElseGet(() -> {
                            Tenant def = new Tenant(tenantId, admin.getSocietyName() != null ? admin.getSocietyName() : "Dairy Co-op", "dairy", admin.getEmail(), admin.getPhone());
                            return tenantRepository.save(def);
                        });

                UserPrincipal principal = new UserPrincipal(
                        admin.getId(),
                        admin.getUsername(),
                        admin.getEmail(),
                        admin.getPassword(),
                        tenant.getId(),
                        admin.getFullName(),
                        admin.getRole()
                );
                String token = tokenProvider.generateToken(principal);

                return ResponseEntity.ok(new AuthResponse(
                        token,
                        admin.getId(),
                        admin.getUsername(),
                        admin.getEmail(),
                        admin.getFullName(),
                        admin.getRole(),
                        tenant.getId(),
                        tenant.getBusinessName(),
                        tenant.getSubscriptionStatus(),
                        tenant.getSubscriptionPlan(),
                        tenant.getTrialEndsAt(),
                        tenant.getSubscriptionEndsAt(),
                        tenant.getDaysRemaining(),
                        tenant.isAccessAllowed()
                ));
            }
        }

        // 2. Try Farmer Login (by Farmer ID or Phone)
        Optional<Farmer> farmerOpt = Optional.empty();
        try {
            Long farmerId = Long.parseLong(identifier);
            farmerOpt = farmerRepository.findById(farmerId);
        } catch (NumberFormatException ignored) {}

        if (farmerOpt.isEmpty()) {
            farmerOpt = farmerRepository.findByPhone(identifier);
        }

        if (farmerOpt.isPresent()) {
            Farmer farmer = farmerOpt.get();
            String farmerPass = farmer.getPassword();
            boolean matched = passwordEncoder.matches(password, farmerPass) ||
                    password.equals(farmerPass) ||
                    (password.equals("farmer123") && (farmerPass == null || farmerPass.isEmpty()));

            if (matched) {
                String tenantId = farmer.getTenantId() != null ? farmer.getTenantId() : "default-tenant";
                Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

                UserPrincipal principal = new UserPrincipal(
                        farmer.getId(),
                        String.valueOf(farmer.getId()),
                        "",
                        farmer.getPassword(),
                        tenantId,
                        farmer.getName(),
                        "ROLE_FARMER"
                );
                String token = tokenProvider.generateToken(principal);

                return ResponseEntity.ok(new AuthResponse(
                        token,
                        farmer.getId(),
                        String.valueOf(farmer.getId()),
                        "",
                        farmer.getName(),
                        "ROLE_FARMER",
                        tenantId,
                        tenant != null ? tenant.getBusinessName() : "Dairy Society",
                        tenant != null ? tenant.getSubscriptionStatus() : "ACTIVE",
                        tenant != null ? tenant.getSubscriptionPlan() : "BASIC",
                        tenant != null ? tenant.getTrialEndsAt() : LocalDate.now().plusDays(15),
                        tenant != null ? tenant.getSubscriptionEndsAt() : null,
                        tenant != null ? tenant.getDaysRemaining() : 15,
                        true
                ));
            }
        }

        // Bootstrap Admin Fallback
        if ("admin".equalsIgnoreCase(identifier) && "admin123".equals(password)) {
            Tenant defTenant = tenantRepository.findById("default-tenant")
                    .orElseGet(() -> {
                        Tenant t = new Tenant("default-tenant", "Milk Society Admin", "demo", "admin@milksystem.com", "9876543210");
                        t.setSubscriptionStatus("ACTIVE");
                        return tenantRepository.save(t);
                    });

            UserPrincipal principal = new UserPrincipal(
                    1L,
                    "admin",
                    "admin@milksystem.com",
                    passwordEncoder.encode("admin123"),
                    "default-tenant",
                    "System Administrator",
                    "ROLE_ADMIN"
            );
            String token = tokenProvider.generateToken(principal);

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    1L,
                    "admin",
                    "admin@milksystem.com",
                    "System Administrator",
                    "ROLE_ADMIN",
                    defTenant.getId(),
                    defTenant.getBusinessName(),
                    defTenant.getSubscriptionStatus(),
                    defTenant.getSubscriptionPlan(),
                    defTenant.getTrialEndsAt(),
                    defTenant.getSubscriptionEndsAt(),
                    365,
                    true
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid credentials. Please check your username/phone and password."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        return ResponseEntity.ok(Map.of(
                "tenantId", tenantId,
                "tenant", tenantOpt.orElse(null),
                "isAccessAllowed", tenantOpt.map(Tenant::isAccessAllowed).orElse(false),
                "daysRemaining", tenantOpt.map(Tenant::getDaysRemaining).orElse(0L)
        ));
    }
}
