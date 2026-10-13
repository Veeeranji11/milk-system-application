package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/daily")
    public Map<String, Object> getDailyReport(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reportService.getDailyReport(date != null ? date : LocalDate.now());
    }

    @GetMapping("/monthly")
    public Map<String, Object> getMonthlyReport(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month) {
        int y = year != null ? year : LocalDate.now().getYear();
        int m = month != null ? month : LocalDate.now().getMonthValue();
        return reportService.getMonthlyReport(y, m);
    }

    @GetMapping("/farmers")
    public List<FarmerFinancialSummary> getFarmerWiseReport() {
        return reportService.getFarmerWiseReport();
    }

    @GetMapping("/pending")
    public List<FarmerFinancialSummary> getPendingPaymentsReport() {
        return reportService.getPendingPaymentsReport();
    }
}