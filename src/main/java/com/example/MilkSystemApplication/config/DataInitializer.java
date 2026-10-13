package com.example.MilkSystemApplication.config;

import com.example.MilkSystemApplication.model.*;
import com.example.MilkSystemApplication.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private TenantRepository tenantRepo;

    @Autowired
    private AdminUserRepository adminUserRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private MilkCollectionRepository milkRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private MilkSaleRepository saleRepo;

    @Autowired
    private AppSettingRepository settingRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String defaultTenantId = "default-tenant";

        // 1. Initialize Default Tenant
        if (!tenantRepo.existsById(defaultTenantId)) {
            Tenant defaultTenant = new Tenant(
                    defaultTenantId,
                    "Sri Lakshmi Village Milk Producers Society",
                    "sri-lakshmi",
                    "admin@milksystem.com",
                    "9848012345"
            );
            defaultTenant.setVillage("Gollapudi");
            defaultTenant.setDistrict("Krishna");
            defaultTenant.setState("Andhra Pradesh");
            defaultTenant.setSubscriptionStatus("ACTIVE");
            defaultTenant.setSubscriptionPlan("PRO");
            defaultTenant.setTrialEndsAt(LocalDate.now().plusDays(15));
            defaultTenant.setSubscriptionEndsAt(LocalDate.now().plusYears(1));
            defaultTenant.setBaseFatRate(7.50);
            tenantRepo.save(defaultTenant);
        }

        // 2. Initialize settings if empty
        if (settingRepo.count() == 0) {
            AppSetting setting = new AppSetting(7.50, 50.00, "Sri Lakshmi Village Milk Producers Society");
            setting.setSocietyContact("9848012345");
            setting.setSocietyAddress("Main Road, Gollapudi Village, Krishna Dist");
            setting.setSocietyUpiId("tnagaveeranjineyulu@oksbi");
            setting.setSocietyUpiName("T Naga veeranjineyulu");
            setting.setSocietyQrImage("/images/payment_qr.jpg");
            settingRepo.save(setting);
        }

        // 3. Initialize Super Admin in SQL if empty
        if (adminUserRepo.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setTenantId(defaultTenantId);
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("T Naga veeranjineyulu");
            admin.setEmail("admin@milksystem.com");
            admin.setPhone("9848012345");
            admin.setSocietyName("Sri Lakshmi Village Milk Producers Society");
            admin.setRole("ROLE_ADMIN");
            admin.setRegistrationDate(LocalDate.now().minusDays(3));
            admin.setTrialExpiryDate(LocalDate.now().plusDays(12));
            admin.setPremium(true);
            admin.setPremiumExpiryDate(LocalDate.now().plusYears(10));
            admin.setPremiumTransactionRef("SYS-ROOT-PREMIUM");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            adminUserRepo.save(admin);
        }

        // 4. Initialize farmers if empty
        if (farmerRepo.count() == 0) {
            String defaultEncodedPass = passwordEncoder.encode("farmer123");

            Farmer f1 = new Farmer("Ramesh Reddy", "9848011111", "Gollapudi", "House No. 2-14, Gollapudi", 5,
                    "309823487123", "SBIN0001234", "ramesh@upi", defaultEncodedPass);
            f1.setTenantId(defaultTenantId);
            f1.setCode("F-001");
            f1.setCowCount(2);

            Farmer f2 = new Farmer("Lakshmi Devi", "9848022222", "Kanchikacherla", "Near Temple, Kanchikacherla", 3,
                    "408192837192", "ANDB0000456", "lakshmi@paytm", defaultEncodedPass);
            f2.setTenantId(defaultTenantId);
            f2.setCode("F-002");
            f2.setCowCount(1);

            Farmer f3 = new Farmer("Venkat Rao", "9848033333", "Ibrahimpatnam", "Plot 45, Ibrahimpatnam", 8,
                    "501928374619", "HDFC0000789", "venkatrao@oksbi", defaultEncodedPass);
            f3.setTenantId(defaultTenantId);
            f3.setCode("F-003");
            f3.setCowCount(4);

            Farmer f4 = new Farmer("Suresh Kumar", "9848044444", "Gollapudi", "Old Panchayat Street, Gollapudi", 4,
                    "602938475819", "ICIC0000321", "suresh@ybl", defaultEncodedPass);
            f4.setTenantId(defaultTenantId);
            f4.setCode("F-004");
            f4.setCowCount(2);

            Farmer f5 = new Farmer("Anjaiah Yadav", "9848055555", "Kondapalli", "Dairy Colony, Kondapalli", 6,
                    "703948572819", "UBIN0000654", "anjaiah@upi", defaultEncodedPass);
            f5.setTenantId(defaultTenantId);
            f5.setCode("F-005");
            f5.setCowCount(3);

            List<Farmer> savedFarmers = farmerRepo.saveAll(List.of(f1, f2, f3, f4, f5));

            // Seed milk collections
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            LocalDate dayBefore = today.minusDays(2);

            // Today Morning
            MilkCollection c1 = new MilkCollection(savedFarmers.get(0).getId(), today, "Morning", 12.5, 6.5, 48.75, 609.38);
            c1.setTenantId(defaultTenantId);
            c1.setQuality("Good");
            c1.setCollectionTime(java.time.LocalTime.of(6, 30));

            MilkCollection c2 = new MilkCollection(savedFarmers.get(1).getId(), today, "Morning", 8.0, 7.0, 52.50, 420.00);
            c2.setTenantId(defaultTenantId);
            c2.setQuality("Excellent (Grade A)");
            c2.setCollectionTime(java.time.LocalTime.of(7, 0));

            MilkCollection c3 = new MilkCollection(savedFarmers.get(2).getId(), today, "Morning", 20.0, 6.2, 46.50, 930.00);
            c3.setTenantId(defaultTenantId);
            c3.setQuality("Good");
            c3.setCollectionTime(java.time.LocalTime.of(7, 15));

            MilkCollection c4 = new MilkCollection(savedFarmers.get(3).getId(), today, "Morning", 9.5, 6.8, 51.00, 484.50);
            c4.setTenantId(defaultTenantId);
            c4.setQuality("Excellent (Grade A)");
            c4.setCollectionTime(java.time.LocalTime.of(7, 30));

            MilkCollection c5 = new MilkCollection(savedFarmers.get(4).getId(), today, "Morning", 15.0, 6.4, 48.00, 720.00);
            c5.setTenantId(defaultTenantId);
            c5.setQuality("Good");
            c5.setCollectionTime(java.time.LocalTime.of(7, 45));

            // Yesterday
            MilkCollection c6 = new MilkCollection(savedFarmers.get(0).getId(), yesterday, "Morning", 13.0, 6.4, 48.00, 624.00);
            c6.setTenantId(defaultTenantId);
            c6.setQuality("Good");
            c6.setCollectionTime(java.time.LocalTime.of(6, 45));

            MilkCollection c7 = new MilkCollection(savedFarmers.get(0).getId(), yesterday, "Evening", 11.5, 6.6, 49.50, 569.25);
            c7.setTenantId(defaultTenantId);
            c7.setQuality("Good");
            c7.setCollectionTime(java.time.LocalTime.of(18, 15));

            MilkCollection c8 = new MilkCollection(savedFarmers.get(1).getId(), yesterday, "Morning", 8.5, 6.9, 51.75, 439.88);
            c8.setTenantId(defaultTenantId);
            c8.setQuality("Excellent (Grade A)");
            c8.setCollectionTime(java.time.LocalTime.of(7, 10));

            MilkCollection c9 = new MilkCollection(savedFarmers.get(2).getId(), yesterday, "Morning", 19.5, 6.3, 47.25, 921.38);
            c9.setTenantId(defaultTenantId);
            c9.setQuality("Good");
            c9.setCollectionTime(java.time.LocalTime.of(7, 20));

            MilkCollection c10 = new MilkCollection(savedFarmers.get(2).getId(), yesterday, "Evening", 18.0, 6.5, 48.75, 877.50);
            c10.setTenantId(defaultTenantId);
            c10.setQuality("Good");
            c10.setCollectionTime(java.time.LocalTime.of(18, 30));

            milkRepo.saveAll(List.of(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10));

            // Payments
            Payment p1 = new Payment(savedFarmers.get(0).getId(), 1000.00, yesterday, "UPI", "UPI-REF-902341", "Weekly advance");
            p1.setTenantId(defaultTenantId);
            Payment p2 = new Payment(savedFarmers.get(1).getId(), 500.00, yesterday, "CASH", "CASH-REC-012", "Direct cash payout");
            p2.setTenantId(defaultTenantId);
            Payment p3 = new Payment(savedFarmers.get(2).getId(), 1500.00, dayBefore, "BANK_TRANSFER", "NEFT-HDFC-8821", "Bank transfer");
            p3.setTenantId(defaultTenantId);
            Payment p4 = new Payment(savedFarmers.get(4).getId(), 700.00, dayBefore, "UPI", "UPI-GPAY-7712", "UPI payment");
            p4.setTenantId(defaultTenantId);

            paymentRepo.saveAll(List.of(p1, p2, p3, p4));
        }

        // 5. Initialize Customers if empty
        if (customerRepo.count() == 0) {
            Customer cust1 = new Customer(defaultTenantId, "Sri Krishna Sweets & Bakery", "9848099881", "Main Bazaar, Gollapudi", 25.0, 58.0);
            cust1.setCustomerType("COMMERCIAL");
            cust1.setCurrentBalance(1450.0);

            Customer cust2 = new Customer(defaultTenantId, "Subba Rao (School Teacher)", "9848099882", "Teacher Colony, Gollapudi", 2.0, 60.0);
            cust2.setCustomerType("RETAIL");

            Customer cust3 = new Customer(defaultTenantId, "Hotel Anand Grand", "9848099883", "Highway Road, Ibrahimpatnam", 50.0, 56.0);
            cust3.setCustomerType("WHOLESALE");
            cust3.setCurrentBalance(2800.0);

            customerRepo.saveAll(List.of(cust1, cust2, cust3));
        }

        // 6. Initialize Milk Sales if empty
        if (saleRepo.count() == 0) {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            MilkSale s1 = new MilkSale("Sri Krishna Sweets", "9848099881", today, "Morning", 25.0, 58.00, "CASH", "PAID");
            s1.setTenantId(defaultTenantId);
            MilkSale s2 = new MilkSale("Subba Rao (Teacher)", "9848099882", today, "Morning", 2.0, 60.00, "UPI", "PAID");
            s2.setTenantId(defaultTenantId);
            MilkSale s3 = new MilkSale("Hotel Anand Grand", "9848099883", yesterday, "Morning", 50.0, 56.00, "BANK_TRANSFER", "PAID");
            s3.setTenantId(defaultTenantId);
            saleRepo.saveAll(List.of(s1, s2, s3));
        }
    }
}