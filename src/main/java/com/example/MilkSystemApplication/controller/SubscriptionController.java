package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.SubscriptionOrder;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.service.PaymentGatewayService;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private PaymentGatewayService gatewayService;

    @Autowired
    private TenantRepository tenantRepository;

    @GetMapping("/plans")
    public List<Map<String, Object>> getPlans() {
        return List.of(
                Map.of(
                        "id", "STARTER",
                        "name", "Starter Society",
                        "price", 499.00,
                        "interval", "month",
                        "farmerLimit", 50,
                        "description", "Ideal for small village milk centers and family dairies.",
                        "features", List.of(
                                "Up to 50 Registered Farmers",
                                "Morning & Evening Collection",
                                "Automated FAT & SNF Calculator",
                                "PDF Settlement Bill Generation",
                                "Cloud Document Storage",
                                "Email Support"
                        )
                ),
                Map.of(
                        "id", "PRO",
                        "name", "Professional Dairy",
                        "price", 999.00,
                        "interval", "month",
                        "farmerLimit", 500,
                        "popular", true,
                        "description", "Most popular for growing co-operatives and bulk milk collection centers.",
                        "features", List.of(
                                "Up to 500 Registered Farmers",
                                "Customer Milk Sales & Delivery Ledger",
                                "Batch 10-Day Settlement Generation",
                                "Automated Cloud Storage & PDF Invoices",
                                "Farmer Passbook & Ledger Self-Service",
                                "SMS / WhatsApp Slip Dispatch Integration",
                                "Priority Phone & Email Support"
                        )
                ),
                Map.of(
                        "id", "ENTERPRISE",
                        "name", "Enterprise Union",
                        "price", 9999.00,
                        "interval", "year",
                        "farmerLimit", 5000,
                        "description", "For multi-village unions and commercial dairy plants.",
                        "features", List.of(
                                "Unlimited Farmers & Customers",
                                "Multi-Branch & Center Aggregation",
                                "Custom Rate Chart Matrices (FAT x SNF)",
                                "Automated Daily Database Backups",
                                "Dedicated Account Manager & SLA",
                                "Zero Downtime Disaster Recovery"
                        )
                )
        );
    }

    @GetMapping("/status")
    public ResponseEntity<?> getSubscriptionStatus() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";

        Optional<Tenant> tenantOpt = tenantRepository.findById(tenantId);
        if (tenantOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "TRIAL",
                    "plan", "FREE_TRIAL",
                    "daysRemaining", 15,
                    "isAccessAllowed", true
            ));
        }

        Tenant tenant = tenantOpt.get();
        return ResponseEntity.ok(Map.of(
                "tenantId", tenant.getId(),
                "businessName", tenant.getBusinessName(),
                "status", tenant.getSubscriptionStatus(),
                "plan", tenant.getSubscriptionPlan(),
                "trialEndsAt", tenant.getTrialEndsAt() != null ? tenant.getTrialEndsAt().toString() : "",
                "subscriptionEndsAt", tenant.getSubscriptionEndsAt() != null ? tenant.getSubscriptionEndsAt().toString() : "",
                "daysRemaining", tenant.getDaysRemaining(),
                "isAccessAllowed", tenant.isAccessAllowed(),
                "currency", tenant.getCurrency()
        ));
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> body) {
        String planName = (String) body.getOrDefault("plan", "PRO");
        int months = body.get("months") != null ? Integer.parseInt(body.get("months").toString()) : 1;

        SubscriptionOrder order = gatewayService.createSubscriptionOrder(planName, months);

        return ResponseEntity.ok(Map.of(
                "orderRef", order.getOrderRef(),
                "gatewayOrderId", order.getGatewayOrderId(),
                "amount", order.getAmount(),
                "currency", order.getCurrency(),
                "keyId", gatewayService.getGatewayKeyId(),
                "provider", gatewayService.getGatewayProvider()
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> body) {
        String orderRef = body.get("orderRef");
        String paymentId = body.get("paymentId");
        String signature = body.get("signature");

        boolean success = gatewayService.verifyAndActivateSubscription(orderRef, paymentId, signature);
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment verified and subscription activated successfully!"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Payment signature verification failed."
            ));
        }
    }

    @PostMapping("/quick-upgrade")
    public ResponseEntity<?> quickUpgrade(@RequestBody Map<String, String> body) {
        String plan = body.getOrDefault("plan", "PRO");
        SubscriptionOrder order = gatewayService.createSubscriptionOrder(plan, 1);
        boolean success = gatewayService.verifyAndActivateSubscription(order.getOrderRef(), "sim_pay_" + System.currentTimeMillis(), "mock_signature");
        return ResponseEntity.ok(Map.of("success", success, "plan", plan));
    }
}
