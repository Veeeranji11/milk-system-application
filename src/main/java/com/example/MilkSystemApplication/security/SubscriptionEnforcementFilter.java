package com.example.MilkSystemApplication.security;

import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class SubscriptionEnforcementFilter extends OncePerRequestFilter {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private com.example.MilkSystemApplication.repository.AdminUserRepository adminUserRepo;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Allow all auth, subscription management, health, static resources, and read-only GET calls
        if (path.startsWith("/api/auth/") ||
            path.startsWith("/api/subscriptions") ||
            path.startsWith("/admin/subscription") ||
            path.startsWith("/farmer/subscription") ||
            path.startsWith("/actuator") ||
            path.startsWith("/h2-console") ||
            path.equals("/") ||
            path.startsWith("/static/") ||
            HttpMethod.GET.matches(method) ||
            HttpMethod.OPTIONS.matches(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // For modifying requests (POST, PUT, DELETE), check tenant or admin subscription status
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null && !tenantId.equalsIgnoreCase("default-tenant")) {
            Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
            if (tenantOpt.isPresent()) {
                Tenant tenant = tenantOpt.get();
                if (!tenant.isAccessAllowed()) {
                    response.setStatus(402); // 402 Payment Required
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                    Map<String, Object> errorBody = new HashMap<>();
                    errorBody.put("error", "SUBSCRIPTION_EXPIRED");
                    errorBody.put("message", "Your 15-day free trial or active subscription has expired. Please upgrade your subscription to perform write operations.");
                    errorBody.put("subscriptionStatus", tenant.getSubscriptionStatus());
                    errorBody.put("trialEndsAt", tenant.getTrialEndsAt());
                    errorBody.put("subscriptionEndsAt", tenant.getSubscriptionEndsAt());
                    errorBody.put("upgradeUrl", "/subscription");

                    objectMapper.writeValue(response.getWriter(), errorBody);
                    return;
                }
            }
        } else {
            // Check default admin subscription
            Optional<com.example.MilkSystemApplication.model.AdminUser> adminOpt = adminUserRepo.findAll().stream().findFirst();
            if (adminOpt.isPresent()) {
                com.example.MilkSystemApplication.model.AdminUser admin = adminOpt.get();
                java.time.LocalDate now = java.time.LocalDate.now();
                boolean isPrem = admin.isPremium() && (admin.getPremiumExpiryDate() == null || !admin.getPremiumExpiryDate().isBefore(now));
                java.time.LocalDate trialExp = admin.getTrialExpiryDate() != null ? admin.getTrialExpiryDate() : admin.getRegistrationDate().plusDays(15);
                boolean isTrial = !admin.isPremium() && (trialExp == null || !trialExp.isBefore(now));

                if (!isPrem && !isTrial) {
                    if (request.getHeader("Accept") != null && request.getHeader("Accept").contains("text/html")) {
                        response.sendRedirect("/admin/dashboard?error=subscription_expired");
                        return;
                    }
                    response.setStatus(402);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    Map<String, Object> errorBody = new HashMap<>();
                    errorBody.put("error", "SUBSCRIPTION_EXPIRED");
                    errorBody.put("message", "Monthly subscription (₹150/mo) is required to keep system active. Please renew your subscription.");
                    errorBody.put("upgradeUrl", "/admin/dashboard");
                    objectMapper.writeValue(response.getWriter(), errorBody);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
