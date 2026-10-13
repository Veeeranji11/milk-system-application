package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/farmer/{farmerId}")
    public List<Payment> getPaymentsByFarmer(@PathVariable Long farmerId) {
        return paymentService.getPaymentsByFarmer(farmerId);
    }

    @PostMapping
    public ResponseEntity<Payment> recordPayment(@RequestBody Payment request) {
        Payment saved = paymentService.addPayment(
                request.getFarmerId(),
                request.getAmount(),
                request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now(),
                request.getPaymentMethod(),
                request.getTransactionRef(),
                request.getNotes()
        );
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public Map<String, Double> getPaymentSummary() {
        return Map.of(
                "totalPaid", paymentService.getTotalPaid(),
                "totalPending", paymentService.getTotalPendingAmount()
        );
    }
}