package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.Invoice;
import com.example.MilkSystemApplication.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices() {
        return billingService.getInvoices();
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        return billingService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/farmer/{farmerId}")
    public List<Invoice> getFarmerInvoices(@PathVariable Long farmerId) {
        return billingService.getInvoicesByFarmer(farmerId);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateFarmerBill(
            @RequestParam Long farmerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0.0") double deductions) {
        try {
            Invoice invoice = billingService.generateFarmerBill(farmerId, startDate, endDate, deductions);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/generate-all")
    public ResponseEntity<?> generateAllBills(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0.0") double deductionsPerFarmer) {
        try {
            List<Invoice> invoices = billingService.generateAllFarmerBills(startDate, endDate, deductionsPerFarmer);
            return ResponseEntity.ok(Map.of(
                    "generatedCount", invoices.size(),
                    "invoices", invoices
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<?> markBillPaid(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String paymentMethod = body.getOrDefault("paymentMethod", "BANK_TRANSFER");
            String ref = body.getOrDefault("transactionRef", "PAY-" + System.currentTimeMillis());
            Invoice updated = billingService.markBillAsPaid(id, paymentMethod, ref);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
