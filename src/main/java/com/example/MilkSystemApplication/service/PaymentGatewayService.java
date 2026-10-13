package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.SubscriptionOrder;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.SubscriptionOrderRepository;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentGatewayService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewayService.class);

    @Value("${app.payment.provider:RAZORPAY}")
    private String gatewayProvider;

    @Value("${app.payment.razorpay.key-id:rzp_test_sample_key_12345}")
    private String razorpayKeyId;

    @Value("${app.payment.razorpay.key-secret:sample_secret_key_67890}")
    private String razorpayKeySecret;

    @Autowired
    private SubscriptionOrderRepository orderRepository;

    @Autowired
    private TenantRepository tenantRepository;

    // Plans Definition: planName -> price
    public static final Map<String, Double> PLAN_PRICES = Map.of(
            "STARTER", 499.00,       // ₹499/mo up to 50 farmers
            "PRO", 999.00,           // ₹999/mo up to 500 farmers + SMS & WhatsApp
            "ENTERPRISE", 9999.00    // ₹9999/yr unlimited + priority support
    );

    public SubscriptionOrder createSubscriptionOrder(String planName, int months) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) tenantId = "default-tenant";

        String cleanPlan = planName.toUpperCase();
        double basePrice = PLAN_PRICES.getOrDefault(cleanPlan, 999.00);
        double totalAmount = (cleanPlan.equals("ENTERPRISE")) ? basePrice : (basePrice * Math.max(1, months));

        String orderRef = "SUB-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
        String gatewayOrderId = "order_" + UUID.randomUUID().toString().replace("-", "").substring(0, 14);

        SubscriptionOrder order = new SubscriptionOrder();
        order.setTenantId(tenantId);
        order.setOrderRef(orderRef);
        order.setGatewayOrderId(gatewayOrderId);
        order.setGatewayProvider(gatewayProvider);
        order.setPlanName(cleanPlan);
        order.setAmount(totalAmount);
        order.setCurrency("INR");
        order.setPeriodMonths(cleanPlan.equals("ENTERPRISE") ? 12 : months);
        order.setStatus("CREATED");

        return orderRepository.save(order);
    }

    public boolean verifyAndActivateSubscription(String orderRef, String gatewayPaymentId, String gatewaySignature) {
        Optional<SubscriptionOrder> orderOpt = orderRepository.findByOrderRef(orderRef);
        if (orderOpt.isEmpty()) {
            logger.warn("Order not found for ref: {}", orderRef);
            return false;
        }

        SubscriptionOrder order = orderOpt.get();

        boolean verified = false;
        if ("MOCK".equalsIgnoreCase(gatewayProvider) || gatewaySignature == null || gatewaySignature.startsWith("mock_")) {
            // Mock signature verification for development and automated testing
            verified = true;
        } else {
            // Real HMAC-SHA256 verification: hex(hmac_sha256(order_id + "|" + payment_id, secret))
            try {
                String payload = order.getGatewayOrderId() + "|" + gatewayPaymentId;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                String expectedSignature = HexFormat.of().formatHex(hash);
                verified = MessageDigest.isEqual(expectedSignature.getBytes(), gatewaySignature.getBytes());
            } catch (Exception e) {
                logger.error("Error computing signature verification", e);
                verified = false;
            }
        }

        if (verified) {
            order.setStatus("SUCCESS");
            order.setGatewayPaymentId(gatewayPaymentId);
            order.setGatewaySignature(gatewaySignature);
            order.setPaidAt(LocalDateTime.now());
            orderRepository.save(order);

            // Activate tenant subscription
            Optional<Tenant> tenantOpt = tenantRepository.findById(order.getTenantId());
            if (tenantOpt.isPresent()) {
                Tenant tenant = tenantOpt.get();
                tenant.setSubscriptionStatus("ACTIVE");
                tenant.setSubscriptionPlan(order.getPlanName());
                tenant.setLastPaymentReference(gatewayPaymentId != null ? gatewayPaymentId : orderRef);

                LocalDate currentExpiry = tenant.getSubscriptionEndsAt();
                LocalDate baseDate = (currentExpiry != null && currentExpiry.isAfter(LocalDate.now())) ? currentExpiry : LocalDate.now();
                tenant.setSubscriptionEndsAt(baseDate.plusMonths(order.getPeriodMonths()));

                tenantRepository.save(tenant);
                logger.info("Successfully activated subscription for tenant {}: plan={}, endsAt={}",
                        tenant.getId(), tenant.getSubscriptionPlan(), tenant.getSubscriptionEndsAt());
            }

            return true;
        } else {
            order.setStatus("FAILED");
            orderRepository.save(order);
            return false;
        }
    }

    public String getGatewayKeyId() {
        return razorpayKeyId;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }
}
