package com.example.MilkSystemApplication;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentService;
import com.example.MilkSystemApplication.service.ReportService;
import com.example.MilkSystemApplication.service.SubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
class MilkSystemApplicationTests {

	@Autowired
	private FarmerService farmerService;

	@Autowired
	private MilkCollectionService milkService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private ReportService reportService;

	@Autowired
	private SubscriptionService subscriptionService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(farmerService);
		Assertions.assertNotNull(milkService);
		Assertions.assertNotNull(paymentService);
		Assertions.assertNotNull(reportService);
		Assertions.assertNotNull(passwordEncoder);
	}

	@Test
	void testFarmerLifecycleAndSearch() {
		// 1. Create Farmer
		Farmer farmer = new Farmer("Suresh Patel", "9876500001", "Anand Village", 5);
		farmer.setAddress("D.No 4-21, Main Road");
		farmer.setBankAccountNo("987654321012");
		farmer.setIfscCode("SBIN0001234");
		farmer.setUpiId("suresh@upi");
		farmer.setPassword(passwordEncoder.encode("farmer123"));

		Farmer saved = farmerService.addFarmer(farmer);
		Assertions.assertNotNull(saved.getId());
		Assertions.assertEquals("Suresh Patel", saved.getName());
		Assertions.assertEquals("987654321012", saved.getBankAccountNo());

		// 2. Fetch by ID
		Optional<Farmer> fetched = farmerService.getFarmerById(saved.getId());
		Assertions.assertTrue(fetched.isPresent());
		Assertions.assertEquals("9876500001", fetched.get().getPhone());

		// 3. Update Farmer
		saved.setBuffaloCount(7);
		saved.setVillage("Anand East");
		Farmer updated = farmerService.updateFarmer(saved.getId(), saved);
		Assertions.assertEquals(7, updated.getBuffaloCount());
		Assertions.assertEquals("Anand East", updated.getVillage());

		// 4. Search Farmer
		List<Farmer> searchResults = farmerService.searchFarmers("Suresh");
		Assertions.assertFalse(searchResults.isEmpty());
		Assertions.assertTrue(searchResults.stream().anyMatch(f -> f.getName().contains("Suresh")));

		// 5. Password Reset
		farmerService.resetPassword(saved.getId(), "newSecret123", passwordEncoder);
		Optional<Farmer> afterReset = farmerService.getFarmerById(saved.getId());
		Assertions.assertTrue(afterReset.isPresent());
		Assertions.assertTrue(passwordEncoder.matches("newSecret123", afterReset.get().getPassword()));

		// 6. Delete Farmer
		farmerService.deleteFarmer(saved.getId());
		Optional<Farmer> deleted = farmerService.getFarmerById(saved.getId());
		Assertions.assertFalse(deleted.isPresent());
	}

	@Test
	void testMilkRateCalculationAndCollection() {
		// Create a test farmer
		Farmer farmer = new Farmer("Rajesh Milkman", "9123456780", "Green Valley", 3);
		farmer.setPassword(passwordEncoder.encode("farmer123"));
		Farmer savedFarmer = farmerService.addFarmer(farmer);

		// Test Rate Calculation: 6.0% fat * 7.5 factor = ₹45.00
		double rate = milkService.calculateRatePerLitre(6.0);
		Assertions.assertEquals(45.0, rate);

		// Test 6.5% fat * 7.5 factor = ₹48.75
		double rate65 = milkService.calculateRatePerLitre(6.5);
		Assertions.assertEquals(48.75, rate65);

		// Record Collection: 10 Litres at 6.0% Fat -> Expected Amount = 10 * 45.0 = 450.0
		MilkCollection mc = milkService.addCollection(
				savedFarmer.getId(),
				10.0,
				6.0,
				"Morning",
				LocalDate.now()
		);

		Assertions.assertNotNull(mc.getId());
		Assertions.assertEquals(45.0, mc.getRatePerLitre());
		Assertions.assertEquals(450.0, mc.getAmount());
		Assertions.assertEquals("Morning", mc.getShift());

		// Test custom rate per litre override: 12 Litres with custom rate ₹52.00/L -> Expected Amount = 624.0
		MilkCollection mcCustom = milkService.addCollection(
				savedFarmer.getId(),
				12.0,
				6.0,
				52.0,
				"Evening",
				LocalDate.now()
		);
		Assertions.assertEquals(52.0, mcCustom.getRatePerLitre());
		Assertions.assertEquals(624.0, mcCustom.getAmount());

		// Test Updating Collection Record: update rate to ₹55.00/L and 10 Litres -> Expected Amount = 550.0
		MilkCollection updated = milkService.updateCollection(
				mc.getId(),
				savedFarmer.getId(),
				10.0,
				6.0,
				55.0,
				"Morning",
				LocalDate.now()
		);
		Assertions.assertEquals(55.0, updated.getRatePerLitre());
		Assertions.assertEquals(550.0, updated.getAmount());

		// Verify collection can be retrieved
		List<MilkCollection> farmerCollections = milkService.getCollectionsByFarmer(savedFarmer.getId());
		Assertions.assertFalse(farmerCollections.isEmpty());

		// Clean up
		milkService.deleteCollection(mc.getId());
		milkService.deleteCollection(mcCustom.getId());
		farmerService.deleteFarmer(savedFarmer.getId());
	}

	@Test
	void testPaymentDisbursementAndFinancialLedger() {
		// 1. Create a test farmer
		Farmer farmer = new Farmer("Narayana Rao", "9949012345", "Kavali", 4);
		farmer.setPassword(passwordEncoder.encode("farmer123"));
		Farmer saved = farmerService.addFarmer(farmer);

		// 2. Supply milk: 20 Litres @ 6.0% fat (₹45/L) = ₹900.0
		MilkCollection mc1 = milkService.addCollection(saved.getId(), 20.0, 6.0, "Morning", LocalDate.now());
		Assertions.assertEquals(900.0, mc1.getAmount());

		// Supply milk: 10 Litres @ 7.0% fat (₹52.5/L) = ₹525.0
		MilkCollection mc2 = milkService.addCollection(saved.getId(), 10.0, 7.0, "Evening", LocalDate.now());
		Assertions.assertEquals(525.0, mc2.getAmount());

		// Total Earned = 900 + 525 = 1425.0
		double totalEarned = milkService.getTotalAmountByFarmer(saved.getId());
		Assertions.assertEquals(1425.0, totalEarned);

		// Initial Pending Balance before payment = 1425.0
		double pendingBefore = paymentService.getPendingAmountByFarmer(saved.getId());
		Assertions.assertEquals(1425.0, pendingBefore);

		// 3. Make partial disbursement of ₹1000.0 via UPI
		Payment payment = paymentService.addPayment(saved.getId(), 1000.0, LocalDate.now(), "UPI", "UPI-TEST-9988", "Advance Weekly Payout");
		Assertions.assertNotNull(payment.getId());
		Assertions.assertEquals(1000.0, payment.getAmount());

		// 4. Verify updated balance
		double totalPaid = paymentService.getTotalPaidByFarmer(saved.getId());
		Assertions.assertEquals(1000.0, totalPaid);

		double pendingAfter = paymentService.getPendingAmountByFarmer(saved.getId());
		Assertions.assertEquals(425.0, pendingAfter);

		// 5. Verify FarmerFinancialSummary DTO
		FarmerFinancialSummary summary = farmerService.getFarmerSummary(saved.getId());
		Assertions.assertEquals(30.0, summary.getTotalLitres());
		Assertions.assertEquals(1425.0, summary.getTotalEarned());
		Assertions.assertEquals(1000.0, summary.getTotalPaid());
		Assertions.assertEquals(425.0, summary.getPendingBalance());

		// 6. Test Reports
		Map<String, Object> dailyReport = reportService.getDailyReport(LocalDate.now());
		Assertions.assertNotNull(dailyReport);
		Assertions.assertTrue(((Number) dailyReport.get("totalLitres")).doubleValue() >= 30.0);

		List<FarmerFinancialSummary> pendingReport = reportService.getPendingPaymentsReport();
		Assertions.assertFalse(pendingReport.isEmpty());
		Assertions.assertTrue(pendingReport.stream().anyMatch(p -> p.getFarmerId().equals(saved.getId()) && p.getPendingBalance() == 425.0));

		// Clean up
		paymentService.deletePayment(payment.getId());
		milkService.deleteCollection(mc1.getId());
		milkService.deleteCollection(mc2.getId());
		farmerService.deleteFarmer(saved.getId());
	}

	@Test
	void testAutoGeneratedFarmerUniqueCode() {
		// Create a new farmer with null code
		Farmer f = new Farmer("Ramesh Goud", "9848099887", "Nellore", 6);
		Farmer saved = farmerService.addFarmer(f);

		Assertions.assertNotNull(saved.getCode(), "Farmer code should be auto-generated");
		Assertions.assertTrue(saved.getCode().startsWith("FMR-"), "Farmer code should start with FMR-");

		// Fetch farmer by unique code
		Optional<Farmer> byCode = farmerService.getFarmerByCode(saved.getCode());
		Assertions.assertTrue(byCode.isPresent());
		Assertions.assertEquals(saved.getId(), byCode.get().getId());
		Assertions.assertEquals("Ramesh Goud", byCode.get().getName());

		// Clean up
		farmerService.deleteFarmer(saved.getId());
	}

	@Test
	void testMilkCollectionWithQualityAndMorningLockout() {
		Farmer f = new Farmer("Anji Reddy", "9848055443", "Guntur", 4);
		Farmer saved = farmerService.addFarmer(f);

		// Record milk collection with quality and time
		LocalTime colTime = LocalTime.of(7, 30);
		MilkCollection mc = milkService.addCollection(
				saved.getId(),
				12.5,
				6.2,
				"Morning",
				LocalDate.now(),
				"Good (Standard)",
				colTime
		);

		Assertions.assertNotNull(mc.getId());
		Assertions.assertEquals("Good (Standard)", mc.getQuality());
		Assertions.assertEquals(colTime, mc.getCollectionTime());

		// Test morning lockout: past date morning records cannot be edited
		MilkCollection pastMorning = new MilkCollection(saved, 10.0, 6.0, 45.0, 450.0, "Morning", LocalDate.now().minusDays(1));
		pastMorning.setCollectionTime(LocalTime.of(7, 0));
		Assertions.assertTrue(pastMorning.isMorningEditLocked(), "Past morning collections must be locked for editing");

		// Update past locked collection should throw IllegalStateException
		MilkCollection savedPast = milkService.addCollection(
				saved.getId(),
				10.0,
				6.0,
				"Morning",
				LocalDate.now().minusDays(1),
				"Standard",
				LocalTime.of(7, 0)
		);

		Assertions.assertThrows(IllegalStateException.class, () -> {
			milkService.updateCollection(
					savedPast.getId(),
					saved.getId(),
					15.0,
					6.0,
					45.0,
					"Morning",
					LocalDate.now().minusDays(1),
					"Good",
					LocalTime.of(7, 0)
			);
		});

		// Clean up
		milkService.deleteCollection(mc.getId());
		milkService.deleteCollection(savedPast.getId());
		farmerService.deleteFarmer(saved.getId());
	}

	@Test
	void testMultiCriteriaSearchCollections() {
		Farmer f = new Farmer("Koteswara Rao", "9848033221", "Vijayawada", 5);
		Farmer saved = farmerService.addFarmer(f);

		LocalDate testDate = LocalDate.now();
		MilkCollection mc1 = milkService.addCollection(saved.getId(), 8.0, 6.5, "Morning", testDate, "Grade A", LocalTime.of(7, 15));
		MilkCollection mc2 = milkService.addCollection(saved.getId(), 9.0, 6.0, "Evening", testDate, "Standard", LocalTime.of(18, 30));

		// Search by Farmer ID, Date and Quality
		List<MilkCollection> found = milkService.searchCollections(null, saved.getId(), testDate, "Grade A");
		Assertions.assertEquals(1, found.size());
		Assertions.assertEquals(mc1.getId(), found.get(0).getId());

		// Search by Quality only
		List<MilkCollection> gradeA = milkService.searchCollections(null, null, null, "Grade A");
		Assertions.assertTrue(gradeA.stream().anyMatch(c -> c.getId().equals(mc1.getId())));

		// Farmer self-search
		List<MilkCollection> farmerSelfSearch = milkService.searchFarmerCollections(null, saved.getId(), testDate.minusDays(1), testDate.plusDays(1), "Evening", "Standard");
		Assertions.assertEquals(1, farmerSelfSearch.size());
		Assertions.assertEquals(mc2.getId(), farmerSelfSearch.get(0).getId());

		// Clean up
		milkService.deleteCollection(mc1.getId());
		milkService.deleteCollection(mc2.getId());
		farmerService.deleteFarmer(saved.getId());
	}

	@Test
	void testSubscriptionPricingAndActivation() {
		// Verify pricing configuration: Admin = ₹150/mo, Farmer 30-user quota = ₹100
		Assertions.assertEquals(150.00, SubscriptionService.ADMIN_PREMIUM_PRICE);
		Assertions.assertEquals(100.00, SubscriptionService.FARMER_GROUP_30_PRICE);

		// Admin subscription activation
		boolean activated = subscriptionService.activateAdminPremium("admin", "TXN-ADMIN-TEST-150", 1);
		Assertions.assertTrue(activated);

		Map<String, Object> adminStatus = subscriptionService.getAdminSubscriptionStatus("admin");
		Assertions.assertEquals(true, adminStatus.get("isPremium"));
		Assertions.assertEquals(150.00, adminStatus.get("premiumPrice"));
		Assertions.assertEquals(100.00, adminStatus.get("farmerGroupPrice"));

		// Activate 30-farmer group quota
		int activatedFarmers = subscriptionService.activateFarmerGroupQuota(30, 1, "TXN-GROUP-30-TEST");
		Assertions.assertTrue(activatedFarmers >= 0);
	}
}
