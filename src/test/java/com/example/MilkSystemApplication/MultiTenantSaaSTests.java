package com.example.MilkSystemApplication;

import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.Invoice;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.security.JwtTokenProvider;
import com.example.MilkSystemApplication.security.UserPrincipal;
import com.example.MilkSystemApplication.service.BillingService;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentGatewayService;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
class MultiTenantSaaSTests {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private MilkCollectionService collectionService;

    @Autowired
    private BillingService billingService;

    @Autowired
    private PaymentGatewayService gatewayService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TENANT_A = "tenant-dairy-alpha";
    private static final String TENANT_B = "tenant-dairy-beta";

    @BeforeEach
    void setupTenants() {
        Tenant tenantA = new Tenant(TENANT_A, "Alpha Co-operative Dairy", "alpha", "contact@alpha.com", "9111111111");
        tenantA.setSubscriptionStatus("TRIAL");
        tenantA.setTrialEndsAt(LocalDate.now().plusDays(15));
        tenantRepository.save(tenantA);

        Tenant tenantB = new Tenant(TENANT_B, "Beta Modern Dairy", "beta", "contact@beta.com", "9222222222");
        tenantB.setSubscriptionStatus("ACTIVE");
        tenantB.setSubscriptionEndsAt(LocalDate.now().plusMonths(6));
        tenantRepository.save(tenantB);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testStrictTenantIsolation() {
        // 1. Under Tenant A: Add Farmer and Milk Collection
        TenantContext.setTenantId(TENANT_A);
        Farmer farmerA = new Farmer("Alpha Producer", "9111100001", "Alpha Village", 4);
        Farmer savedFarmerA = farmerService.addFarmer(farmerA);
        Assertions.assertEquals(TENANT_A, savedFarmerA.getTenantId());

        MilkCollection mcA = collectionService.addCollection(savedFarmerA.getId(), 15.0, 6.5, "Morning", LocalDate.now());
        Assertions.assertEquals(TENANT_A, mcA.getTenantId());

        // 2. Under Tenant B: Query farmers and collections
        TenantContext.setTenantId(TENANT_B);
        List<Farmer> farmersInB = farmerService.getAllFarmers();
        // Crucial verification: Tenant B MUST NOT see Tenant A's farmer
        Assertions.assertFalse(farmersInB.stream().anyMatch(f -> f.getId().equals(savedFarmerA.getId())));

        // Tenant B cannot retrieve Tenant A's farmer by ID
        Assertions.assertTrue(farmerService.getFarmerById(savedFarmerA.getId()).isEmpty());

        // 3. Add Farmer for Tenant B
        Farmer farmerB = new Farmer("Beta Producer", "9222200002", "Beta Village", 2);
        Farmer savedFarmerB = farmerService.addFarmer(farmerB);
        Assertions.assertEquals(TENANT_B, savedFarmerB.getTenantId());

        // 4. Switch back to Tenant A
        TenantContext.setTenantId(TENANT_A);
        List<Farmer> farmersInA = farmerService.getAllFarmers();
        Assertions.assertTrue(farmersInA.stream().anyMatch(f -> f.getId().equals(savedFarmerA.getId())));
        Assertions.assertFalse(farmersInA.stream().anyMatch(f -> f.getId().equals(savedFarmerB.getId())));

        // Clean up
        collectionService.deleteCollection(mcA.getId());
        farmerService.deleteFarmer(savedFarmerA.getId());
        TenantContext.setTenantId(TENANT_B);
        farmerService.deleteFarmer(savedFarmerB.getId());
    }

    @Test
    void testJwtGenerationAndTenantClaims() {
        UserPrincipal principal = new UserPrincipal(
                101L,
                "manager_alpha",
                "manager@alpha.com",
                "secret",
                TENANT_A,
                "Manager Alpha",
                "ROLE_ADMIN"
        );

        String token = jwtTokenProvider.generateToken(principal);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(jwtTokenProvider.validateToken(token));

        String extractedUsername = jwtTokenProvider.getUsernameFromJWT(token);
        String extractedTenant = jwtTokenProvider.getTenantIdFromJWT(token);

        Assertions.assertEquals("manager_alpha", extractedUsername);
        Assertions.assertEquals(TENANT_A, extractedTenant);
    }

    @Test
    void test15DayTrialAndSubscriptionLifecycle() {
        Tenant tenant = tenantRepository.findById(TENANT_A).orElseThrow();
        Assertions.assertEquals("TRIAL", tenant.getSubscriptionStatus());
        Assertions.assertTrue(tenant.isAccessAllowed());
        Assertions.assertTrue(tenant.getDaysRemaining() >= 14);

        // Test payment verification and upgrade to ACTIVE plan
        TenantContext.setTenantId(TENANT_A);
        var order = gatewayService.createSubscriptionOrder("PRO", 1);
        Assertions.assertNotNull(order.getOrderRef());

        boolean activated = gatewayService.verifyAndActivateSubscription(order.getOrderRef(), "pay_mock_123", "mock_signature");
        Assertions.assertTrue(activated);

        Tenant updatedTenant = tenantRepository.findById(TENANT_A).orElseThrow();
        Assertions.assertEquals("ACTIVE", updatedTenant.getSubscriptionStatus());
        Assertions.assertEquals("PRO", updatedTenant.getSubscriptionPlan());
    }

    @Test
    void testBillingAndSettlementPdfGeneration() {
        TenantContext.setTenantId(TENANT_A);
        Farmer farmer = new Farmer("Billing Test Farmer", "9333300003", "Test Nagar", 5);
        Farmer savedFarmer = farmerService.addFarmer(farmer);

        LocalDate today = LocalDate.now();
        MilkCollection mc = collectionService.addCollection(savedFarmer.getId(), 25.0, 6.0, "Morning", today);

        // Generate Settlement Invoice
        Invoice invoice = billingService.generateFarmerBill(savedFarmer.getId(), today.minusDays(1), today, 50.0);
        Assertions.assertNotNull(invoice.getId());
        Assertions.assertNotNull(invoice.getInvoiceNumber());
        Assertions.assertNotNull(invoice.getPdfUrl());
        Assertions.assertTrue(invoice.getPdfUrl().endsWith(".pdf"));
        Assertions.assertEquals("PENDING", invoice.getStatus());

        // Test marking as paid
        Invoice paidInvoice = billingService.markBillAsPaid(invoice.getId(), "BANK_TRANSFER", "REF-TEST-001");
        Assertions.assertEquals("PAID", paidInvoice.getStatus());

        // Clean up
        collectionService.deleteCollection(mc.getId());
        farmerService.deleteFarmer(savedFarmer.getId());
    }
}
