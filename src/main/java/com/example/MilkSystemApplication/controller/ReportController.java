package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentService;
import com.example.MilkSystemApplication.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private MilkCollectionService milkService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public String reportsHub(
            @RequestParam(value = "tab", defaultValue = "daily") String tab,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            Model model) {

        LocalDate selectedDate = date != null ? date : LocalDate.now();
        int selectedYear = year != null ? year : LocalDate.now().getYear();
        int selectedMonth = month != null ? month : LocalDate.now().getMonthValue();

        // 1. Daily Report Data
        Map<String, Object> dailyReport = reportService.getDailyReport(selectedDate);
        model.addAttribute("dailyReport", dailyReport);
        model.addAttribute("selectedDate", selectedDate);

        // 2. Monthly Report Data
        Map<String, Object> monthlyReport = reportService.getMonthlyReport(selectedYear, selectedMonth);
        model.addAttribute("monthlyReport", monthlyReport);
        model.addAttribute("selectedYear", selectedYear);
        model.addAttribute("selectedMonth", selectedMonth);

        // 3. Farmer-wise Summary
        model.addAttribute("farmerSummaries", reportService.getFarmerWiseReport());

        // 4. Pending Payments
        model.addAttribute("pendingReports", reportService.getPendingPaymentsReport());

        // Summary Totals
        model.addAttribute("totalFarmers", farmerService.getTotalFarmersCount());
        model.addAttribute("totalLitres", milkService.getTotalLitres());
        model.addAttribute("totalAmount", milkService.getTotalAmount());
        model.addAttribute("totalPaid", paymentService.getTotalPaid());
        model.addAttribute("totalPending", paymentService.getTotalPendingAmount());
        model.addAttribute("activeTab", tab);
        model.addAttribute("settings", milkService.getSettings());

        return "reports";
    }
}