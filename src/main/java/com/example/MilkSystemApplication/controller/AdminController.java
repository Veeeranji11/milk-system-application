package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.AppSetting;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AdminController {

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

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, java.security.Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("subscription", subscriptionService.getAdminSubscriptionStatus(username));
        model.addAttribute("totalFarmers", farmerService.getTotalFarmersCount());
        model.addAttribute("todayLitres", milkService.getTodayLitres());
        model.addAttribute("todayAmount", milkService.getTodayAmount());
        model.addAttribute("todayMorningLitres", milkService.getTodayMorningLitres());
        model.addAttribute("todayEveningLitres", milkService.getTodayEveningLitres());
        model.addAttribute("monthlyLitres", milkService.getMonthlyLitres());
        model.addAttribute("monthlyAmount", milkService.getMonthlyAmount());
        model.addAttribute("totalLitres", milkService.getTotalLitres());
        model.addAttribute("totalAmount", milkService.getTotalAmount());
        model.addAttribute("totalPaid", paymentService.getTotalPaid());
        model.addAttribute("pendingBalance", paymentService.getTotalPendingAmount());
        model.addAttribute("rateFactor", milkService.getRateFactor());
        model.addAttribute("settings", milkService.getSettings());

        List<MilkCollection> allCollections = milkService.getAllCollections();
        model.addAttribute("recentCollections", allCollections.stream().limit(10).toList());
        model.addAttribute("recentPayments", paymentService.getAllPayments().stream().limit(5).toList());
        model.addAttribute("farmers", farmerService.getAllFarmers());
        return "dashboard";
    }

    @PostMapping("/admin/subscription/renew")
    public String renewSubscription(@RequestParam(defaultValue = "1") int months,
                                    @RequestParam(required = false) String transactionRef,
                                    java.security.Principal principal,
                                    RedirectAttributes redirectAttributes) {
        String username = (principal != null) ? principal.getName() : "admin";
        String ref = (transactionRef != null && !transactionRef.trim().isEmpty()) ? transactionRef.trim() : "UPI-TXN-" + (System.currentTimeMillis() % 1000000);
        subscriptionService.activateAdminPremium(username, ref, months);
        redirectAttributes.addFlashAttribute("successMessage", "Monthly Subscription renewed successfully! Plan: ₹150/mo (" + months + " month(s)). Transaction Ref: " + ref);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/subscription/farmer-quota")
    public String activateFarmerQuotaPlan(@RequestParam(defaultValue = "30") int maxFarmers,
                                          @RequestParam(defaultValue = "1") int months,
                                          @RequestParam(required = false) String transactionRef,
                                          RedirectAttributes redirectAttributes) {
        String ref = (transactionRef != null && !transactionRef.trim().isEmpty()) ? transactionRef.trim() : "UPI-FMR-" + (System.currentTimeMillis() % 1000000);
        int count = subscriptionService.activateFarmerGroupQuota(maxFarmers, months, ref);
        redirectAttributes.addFlashAttribute("successMessage", "Farmer Subscription activated for " + count + " farmers (₹100 for 30 users plan). Transaction Ref: " + ref);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/settings/rate")
    public String updateRateSettings(@RequestParam(required = false) Double rateFactor, RedirectAttributes redirectAttributes) {
        if (rateFactor != null && rateFactor > 0) {
            milkService.setRateFactor(rateFactor);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Rate settings updated successfully (₹" + rateFactor + " per Fat %)");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/settings/society")
    public String updateSocietySettings(@ModelAttribute AppSetting setting, RedirectAttributes redirectAttributes) {
        milkService.updateSettings(setting);
        redirectAttributes.addFlashAttribute("successMessage", "Society settings updated successfully!");
        return "redirect:/admin/dashboard";
    }

    // Farmer Management
    @GetMapping("/admin/farmers")
    public String listFarmers(@RequestParam(value = "search", required = false) String search, Model model) {
        model.addAttribute("farmers", farmerService.searchFarmers(search));
        model.addAttribute("search", search);
        return "farmers";
    }

    @GetMapping("/admin/farmers/add")
    public String showAddFarmerForm(Model model) {
        model.addAttribute("farmer", new Farmer());
        return "addFarmer";
    }

    @PostMapping("/admin/farmers/add")
    public String addFarmer(@ModelAttribute Farmer farmer, RedirectAttributes redirectAttributes) {
        if (farmer.getPassword() != null && !farmer.getPassword().trim().isEmpty()) {
            farmer.setPassword(passwordEncoder.encode(farmer.getPassword().trim()));
        } else {
            farmer.setPassword(passwordEncoder.encode("farmer123"));
        }
        Farmer saved = farmerService.addFarmer(farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Farmer " + saved.getName() + " registered successfully! (Farmer ID: #" + saved.getId() + ")");
        return "redirect:/admin/farmers";
    }

    @GetMapping("/admin/farmers/edit/{id}")
    public String showEditFarmerForm(@PathVariable Long id, Model model) {
        farmerService.getFarmerById(id).ifPresent(f -> model.addAttribute("farmer", f));
        return "editFarmer";
    }

    @PostMapping("/admin/farmers/edit/{id}")
    public String updateFarmer(@PathVariable Long id, @ModelAttribute Farmer farmer, RedirectAttributes redirectAttributes) {
        farmerService.updateFarmer(id, farmer);
        redirectAttributes.addFlashAttribute("successMessage", "Farmer details updated successfully!");
        return "redirect:/admin/farmers";
    }

    @PostMapping("/admin/farmers/{id}/reset-password")
    public String resetFarmerPassword(@PathVariable Long id, @RequestParam String newPassword, RedirectAttributes redirectAttributes) {
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            farmerService.resetPassword(id, newPassword.trim(), passwordEncoder);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully for farmer #" + id);
        }
        return "redirect:/admin/farmers";
    }

    @GetMapping("/admin/farmers/delete/{id}")
    public String deleteFarmer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        farmerService.deleteFarmer(id);
        redirectAttributes.addFlashAttribute("successMessage", "Farmer deleted successfully!");
        return "redirect:/admin/farmers";
    }

    @GetMapping("/admin/farmers/{id}/statement")
    public String showFarmerStatement(@PathVariable Long id, Model model) {
        farmerService.getFarmerById(id).ifPresent(f -> {
            model.addAttribute("farmer", f);
            model.addAttribute("summary", farmerService.getFarmerSummary(id));
        });
        model.addAttribute("collections", milkService.getCollectionsByFarmer(id));
        model.addAttribute("payments", paymentService.getPaymentsByFarmer(id));
        model.addAttribute("totalLitres", milkService.getTotalLitresByFarmer(id));
        model.addAttribute("totalAmount", milkService.getTotalAmountByFarmer(id));
        model.addAttribute("totalPaid", paymentService.getTotalPaidByFarmer(id));
        model.addAttribute("pendingBalance", paymentService.getPendingAmountByFarmer(id));
        model.addAttribute("settings", milkService.getSettings());
        return "farmerStatement";
    }

    // Milk Collections
    @GetMapping("/admin/collections")
    public String listCollections(
            @RequestParam(value = "farmerId", required = false) Long farmerId,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "quality", required = false) String quality,
            Model model) {
        List<MilkCollection> list;
        if (farmerId != null || date != null || (quality != null && !quality.trim().isEmpty())) {
            list = milkService.searchCollections(farmerId, date, quality);
        } else {
            list = milkService.getAllCollections();
        }
        model.addAttribute("collections", list);
        model.addAttribute("filterDate", date);
        model.addAttribute("filterFarmerId", farmerId);
        model.addAttribute("filterQuality", quality);
        model.addAttribute("farmers", farmerService.getAllFarmers());
        model.addAttribute("totalLitres", Math.round(list.stream().mapToDouble(MilkCollection::getLitres).sum() * 100.0) / 100.0);
        model.addAttribute("totalAmount", Math.round(list.stream().mapToDouble(MilkCollection::getAmount).sum() * 100.0) / 100.0);
        return "collections";
    }

    @GetMapping("/admin/collections/add")
    public String showAddCollectionForm(Model model) {
        model.addAttribute("farmers", farmerService.getAllFarmers());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("currentTime", java.time.LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.MINUTES));
        model.addAttribute("rateFactor", milkService.getRateFactor());
        return "addMilkCollection";
    }

    @PostMapping("/admin/collections/add")
    public String addCollection(@RequestParam Long farmerId,
                                @RequestParam double litres,
                                @RequestParam double fatPercentage,
                                @RequestParam(required = false) Double ratePerLitre,
                                @RequestParam(defaultValue = "Morning") String shift,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(required = false) String quality,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) java.time.LocalTime collectionTime,
                                RedirectAttributes redirectAttributes) {
        LocalDate collectionDate = (date != null) ? date : LocalDate.now();
        java.time.LocalTime collTime = (collectionTime != null) ? collectionTime : java.time.LocalTime.now();
        MilkCollection mc = milkService.addCollection(farmerId, litres, fatPercentage, ratePerLitre, shift, collectionDate, quality, collTime);
        redirectAttributes.addFlashAttribute("successMessage", "Milk collection recorded: " + litres + "L (" + mc.getQuality() + ") @ ₹" + mc.getRatePerLitre() + "/L = ₹" + mc.getAmount() + " (" + shift + ")");
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/collections/edit/{id}")
    public String showEditCollectionForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        java.util.Optional<MilkCollection> opt = milkService.getCollectionById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Record #" + id + " not found!");
            return "redirect:/admin/collections";
        }
        MilkCollection mc = opt.get();
        if (mc.isMorningEditLocked()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Restricted: Morning milk collection data cannot be edited after morning hours (12:00 PM)!");
            return "redirect:/admin/collections";
        }
        model.addAttribute("collection", mc);
        model.addAttribute("farmers", farmerService.getAllFarmers());
        model.addAttribute("rateFactor", milkService.getRateFactor());
        return "editCollection";
    }

    @PostMapping("/admin/collections/edit/{id}")
    public String updateCollection(@PathVariable Long id,
                                   @RequestParam Long farmerId,
                                   @RequestParam double litres,
                                   @RequestParam double fatPercentage,
                                   @RequestParam(required = false) Double ratePerLitre,
                                   @RequestParam(defaultValue = "Morning") String shift,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam(required = false) String quality,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) java.time.LocalTime collectionTime,
                                   RedirectAttributes redirectAttributes) {
        try {
            milkService.updateCollection(id, farmerId, litres, fatPercentage, ratePerLitre, shift, date, quality, collectionTime);
            redirectAttributes.addFlashAttribute("successMessage", "Collection record #" + id + " updated successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/collections";
    }

    @GetMapping("/admin/collections/delete/{id}")
    public String deleteCollection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        milkService.deleteCollection(id);
        redirectAttributes.addFlashAttribute("successMessage", "Record deleted successfully!");
        return "redirect:/admin/collections";
    }
}