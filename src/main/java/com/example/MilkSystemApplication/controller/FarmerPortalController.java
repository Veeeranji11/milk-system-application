package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/farmer")
public class FarmerPortalController {

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private MilkCollectionService milkService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private com.example.MilkSystemApplication.service.SubscriptionService subscriptionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Farmer getAuthenticatedFarmer(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No authenticated user found.");
        }
        String name = principal.getName().trim();
        try {
            Long farmerId = Long.parseLong(name);
            return farmerService.getFarmerById(farmerId)
                    .or(() -> farmerService.getFarmerByCode(name))
                    .orElseThrow(() -> new RuntimeException("Farmer profile not found for ID: " + farmerId));
        } catch (NumberFormatException e) {
            return farmerService.getFarmerByPhone(name)
                    .or(() -> farmerService.getFarmerByCode(name))
                    .orElseThrow(() -> new RuntimeException("Farmer profile not found for: " + name));
        }
    }

    @GetMapping("/dashboard")
    public String farmerDashboard(Principal principal, Model model) {
        Farmer farmer = getAuthenticatedFarmer(principal);
        Long id = farmer.getId();

        FarmerFinancialSummary summary = farmerService.getFarmerSummary(id);
        List<MilkCollection> recentCollections = milkService.getCollectionsByFarmer(id);
        List<Payment> recentPayments = paymentService.getPaymentsByFarmer(id);

        LocalDate today = LocalDate.now();
        List<MilkCollection> todayDeliveries = recentCollections.stream()
                .filter(c -> today.equals(c.getDate()))
                .toList();

        double todayLitres = todayDeliveries.stream().mapToDouble(MilkCollection::getLitres).sum();
        double todayAmount = todayDeliveries.stream().mapToDouble(MilkCollection::getAmount).sum();

        java.time.YearMonth currentYm = java.time.YearMonth.now();
        List<MilkCollection> monthlyCollections = milkService.getCollectionsByFarmerAndDates(id, currentYm.atDay(1), currentYm.atEndOfMonth());
        double monthlyLitres = monthlyCollections.stream().mapToDouble(MilkCollection::getLitres).sum();
        double monthlyAmount = monthlyCollections.stream().mapToDouble(MilkCollection::getAmount).sum();

        model.addAttribute("farmer", farmer);
        model.addAttribute("summary", summary);
        model.addAttribute("todayLitres", Math.round(todayLitres * 100.0) / 100.0);
        model.addAttribute("todayAmount", Math.round(todayAmount * 100.0) / 100.0);
        model.addAttribute("monthlyLitres", Math.round(monthlyLitres * 100.0) / 100.0);
        model.addAttribute("monthlyAmount", Math.round(monthlyAmount * 100.0) / 100.0);
        model.addAttribute("subscription", subscriptionService.getFarmerSubscriptionStatus(id));
        model.addAttribute("recentCollections", recentCollections.stream().limit(10).toList());
        model.addAttribute("recentPayments", recentPayments.stream().limit(5).toList());
        model.addAttribute("rateFactor", milkService.getRateFactor());
        model.addAttribute("settings", milkService.getSettings());

        return "farmerDashboard";
    }

    @GetMapping("/statement")
    public String farmerStatement(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "shift", required = false) String shift,
            @RequestParam(value = "quality", required = false) String quality,
            Principal principal,
            Model model) {

        Farmer farmer = getAuthenticatedFarmer(principal);
        Long id = farmer.getId();

        List<MilkCollection> collections = milkService.searchFarmerCollections(id, startDate, endDate, shift, quality);
        List<Payment> payments = paymentService.getPaymentsByFarmer(id);
        double totalLitres = collections.stream().mapToDouble(MilkCollection::getLitres).sum();
        double totalAmount = collections.stream().mapToDouble(MilkCollection::getAmount).sum();
        double totalPaid = paymentService.getTotalPaidByFarmer(id);
        double pending = paymentService.getPendingAmountByFarmer(id);

        model.addAttribute("farmer", farmer);
        model.addAttribute("collections", collections);
        model.addAttribute("payments", payments);
        model.addAttribute("totalLitres", Math.round(totalLitres * 100.0) / 100.0);
        model.addAttribute("totalAmount", Math.round(totalAmount * 100.0) / 100.0);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("pendingBalance", pending);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("shift", shift);
        model.addAttribute("quality", quality);
        model.addAttribute("subscription", subscriptionService.getFarmerSubscriptionStatus(id));
        model.addAttribute("settings", milkService.getSettings());

        return "farmerStatement";
    }

    @PostMapping("/subscription/pay")
    public String payFarmerSubscription(@RequestParam(defaultValue = "1") int months,
                                        @RequestParam(required = false) String transactionRef,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        Farmer farmer = getAuthenticatedFarmer(principal);
        String ref = (transactionRef != null && !transactionRef.trim().isEmpty()) ? transactionRef.trim() : "UPI-FARMER-" + (System.currentTimeMillis() % 1000000);
        subscriptionService.activateFarmerPremium(farmer.getId(), ref, months);
        redirectAttributes.addFlashAttribute("successMessage", "Farmer Subscription activated successfully for " + months + " month(s)! Transaction Ref: " + ref);
        return "redirect:/farmer/dashboard";
    }

    @GetMapping("/payments")
    public String farmerPayments(Principal principal, Model model) {
        Farmer farmer = getAuthenticatedFarmer(principal);
        Long id = farmer.getId();

        List<Payment> payments = paymentService.getPaymentsByFarmer(id);
        FarmerFinancialSummary summary = farmerService.getFarmerSummary(id);

        model.addAttribute("farmer", farmer);
        model.addAttribute("payments", payments);
        model.addAttribute("summary", summary);
        model.addAttribute("settings", milkService.getSettings());

        return "farmerPayments";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Principal principal, Model model) {
        Farmer farmer = getAuthenticatedFarmer(principal);
        model.addAttribute("farmer", farmer);
        return "farmerChangePassword";
    }

    @PostMapping("/change-password")
    public String processChangePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Farmer farmer = getAuthenticatedFarmer(principal);

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password and Confirm password do not match!");
            return "redirect:/farmer/change-password";
        }

        if (newPassword.trim().length() < 4) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 4 characters long.");
            return "redirect:/farmer/change-password";
        }

        boolean success = farmerService.changePassword(farmer.getId(), currentPassword, newPassword.trim(), passwordEncoder);
        if (!success) {
            redirectAttributes.addFlashAttribute("errorMessage", "Incorrect current password! Please try again.");
            return "redirect:/farmer/change-password";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully! Please use your new password for next login.");
        return "redirect:/farmer/dashboard";
    }
}