package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private com.example.MilkSystemApplication.service.MilkCollectionService milkService;

    @GetMapping
    public String listPayments(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "farmerId", required = false) Long farmerId,
            Model model) {

        List<Payment> payments;
        if (farmerId != null) {
            payments = paymentService.getPaymentsByFarmer(farmerId);
        } else if (date != null) {
            payments = paymentService.getPaymentsByDate(date);
        } else {
            payments = paymentService.getAllPayments();
        }

        model.addAttribute("payments", payments);
        model.addAttribute("filterDate", date);
        model.addAttribute("filterFarmerId", farmerId);
        model.addAttribute("farmers", farmerService.getAllFarmers());
        model.addAttribute("totalPaid", paymentService.getTotalPaid());
        model.addAttribute("totalPending", paymentService.getTotalPendingAmount());
        model.addAttribute("settings", milkService.getSettings());

        return "payments";
    }

    @GetMapping("/add")
    public String showAddPaymentForm(@RequestParam(value = "farmerId", required = false) Long farmerId, Model model) {
        model.addAttribute("farmers", farmerService.getAllFarmers());
        model.addAttribute("selectedFarmerId", farmerId);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("settings", milkService.getSettings());

        if (farmerId != null) {
            double pending = paymentService.getPendingAmountByFarmer(farmerId);
            model.addAttribute("farmerPending", pending);
            farmerService.getFarmerById(farmerId).ifPresent(f -> model.addAttribute("selectedFarmer", f));
        }

        return "addPayment";
    }

    @PostMapping("/add")
    public String addPayment(
            @RequestParam Long farmerId,
            @RequestParam double amount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            @RequestParam(defaultValue = "CASH") String paymentMethod,
            @RequestParam(required = false) String transactionRef,
            @RequestParam(required = false) String notes,
            RedirectAttributes redirectAttributes) {

        try {
            LocalDate date = paymentDate != null ? paymentDate : LocalDate.now();
            Payment saved = paymentService.addPayment(farmerId, amount, date, paymentMethod, transactionRef, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Payment of ₹" + saved.getAmount() + " recorded via " + saved.getPaymentMethod() + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording payment: " + e.getMessage());
        }

        return "redirect:/admin/payments";
    }

    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        paymentService.deletePayment(id);
        redirectAttributes.addFlashAttribute("successMessage", "Payment record removed successfully!");
        return "redirect:/admin/payments";
    }

    @GetMapping("/pending")
    public String showPendingPayouts(Model model) {
        List<FarmerFinancialSummary> summaries = farmerService.getAllFarmerSummaries();
        List<FarmerFinancialSummary> pendingList = summaries.stream()
                .filter(s -> s.getPendingBalance() > 0)
                .toList();

        model.addAttribute("pendingList", pendingList);
        model.addAttribute("totalPending", paymentService.getTotalPendingAmount());
        return "reports";
    }
}