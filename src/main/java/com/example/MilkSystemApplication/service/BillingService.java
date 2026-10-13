package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.Invoice;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Payment;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.InvoiceRepository;
import com.example.MilkSystemApplication.repository.MilkCollectionRepository;
import com.example.MilkSystemApplication.repository.PaymentRepository;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BillingService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private FarmerRepository farmerRepository;

    @Autowired
    private MilkCollectionRepository collectionRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private CloudStorageService cloudStorageService;

    @Transactional
    public Invoice generateFarmerBill(Long farmerId, LocalDate startDate, LocalDate endDate, double deductions) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";

        final String activeTenantId = tenantId;
        Farmer farmer = farmerRepository.findByIdAndTenantId(farmerId, activeTenantId)
                .orElseThrow(() -> new RuntimeException("Farmer not found for ID: " + farmerId));

        Tenant tenant = tenantRepository.findById(activeTenantId)
                .orElseGet(() -> new Tenant(activeTenantId, "Dairy Co-operative", "dairy", "info@dairy.com", ""));

        List<MilkCollection> collections = collectionRepository
                .findByTenantIdAndFarmerIdAndDateBetweenOrderByDateDescIdDesc(activeTenantId, farmerId, startDate, endDate);

        if (collections.isEmpty()) {
            throw new RuntimeException("No milk collections found for farmer " + farmer.getName() + " in the selected date range.");
        }

        double totalLitres = collections.stream().mapToDouble(MilkCollection::getLitres).sum();
        double totalAmount = collections.stream().mapToDouble(MilkCollection::getAmount).sum();
        double avgFat = collections.stream().mapToDouble(MilkCollection::getFatPercentage).average().orElse(0.0);
        double netAmount = Math.max(0.0, totalAmount - deductions);

        String invoiceNumber = "INV-F-" + LocalDate.now().toString().replace("-", "") + "-" + farmer.getId() + "-" + (System.currentTimeMillis() % 1000);

        // Generate PDF
        byte[] pdfBytes = pdfService.generateFarmerSettlementBill(tenant, farmer, collections, startDate, endDate, deductions, invoiceNumber);

        // Upload to Cloud Storage
        String pdfUrl = cloudStorageService.uploadFile("invoices/" + activeTenantId, invoiceNumber + ".pdf", pdfBytes, "application/pdf");

        // Save Invoice entity
        Invoice invoice = new Invoice();
        invoice.setTenantId(activeTenantId);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceType("FARMER_SETTLEMENT");
        invoice.setRecipientId(farmer.getId());
        invoice.setRecipientName(farmer.getName());
        invoice.setRecipientPhone(farmer.getPhone());
        invoice.setPeriodStartDate(startDate);
        invoice.setPeriodEndDate(endDate);
        invoice.setTotalLitres(Math.round(totalLitres * 100.0) / 100.0);
        invoice.setAverageFat(Math.round(avgFat * 100.0) / 100.0);
        invoice.setSubtotal(Math.round(totalAmount * 100.0) / 100.0);
        invoice.setDeductions(Math.round(deductions * 100.0) / 100.0);
        invoice.setNetAmount(Math.round(netAmount * 100.0) / 100.0);
        invoice.setStatus("PENDING");
        invoice.setPdfUrl(pdfUrl);
        invoice.setBillDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7));

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public List<Invoice> generateAllFarmerBills(LocalDate startDate, LocalDate endDate, double deductionsPerFarmer) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";

        List<Farmer> farmers = farmerRepository.findByTenantIdOrderByNameAsc(tenantId);
        List<Invoice> generatedInvoices = new ArrayList<>();

        for (Farmer farmer : farmers) {
            List<MilkCollection> colls = collectionRepository
                    .findByTenantIdAndFarmerIdAndDateBetweenOrderByDateDescIdDesc(tenantId, farmer.getId(), startDate, endDate);
            if (!colls.isEmpty()) {
                try {
                    Invoice inv = generateFarmerBill(farmer.getId(), startDate, endDate, deductionsPerFarmer);
                    generatedInvoices.add(inv);
                } catch (Exception ignored) {}
            }
        }

        return generatedInvoices;
    }

    @Transactional
    public Invoice markBillAsPaid(Long invoiceId, String paymentMethod, String ref) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";

        Invoice invoice = invoiceRepository.findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() -> new RuntimeException("Invoice not found for ID: " + invoiceId));

        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaymentReference(ref != null ? ref : "PAY-" + System.currentTimeMillis() % 10000);

        // Record a Payment entry automatically
        Payment payment = new Payment();
        payment.setTenantId(tenantId);
        payment.setFarmerId(invoice.getRecipientId());
        payment.setAmount(invoice.getNetAmount());
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentMethod != null ? paymentMethod : "BANK_TRANSFER");
        payment.setTransactionRef(invoice.getPaymentReference());
        payment.setNotes("Settlement against Bill " + invoice.getInvoiceNumber());
        payment.setStatus("PAID");
        paymentRepository.save(payment);

        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoices() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";
        return invoiceRepository.findByTenantIdOrderByBillDateDesc(tenantId);
    }

    public Optional<Invoice> getInvoiceById(Long id) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";
        return invoiceRepository.findByIdAndTenantId(id, tenantId);
    }

    public List<Invoice> getInvoicesByFarmer(Long farmerId) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isEmpty()) tenantId = "default-tenant";
        return invoiceRepository.findByTenantIdAndRecipientIdAndInvoiceType(tenantId, farmerId, "FARMER_SETTLEMENT");
    }
}
