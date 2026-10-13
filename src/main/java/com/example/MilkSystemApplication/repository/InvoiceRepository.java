package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTenantIdOrderByBillDateDesc(String tenantId);
    List<Invoice> findByTenantIdAndInvoiceTypeOrderByBillDateDesc(String tenantId, String invoiceType);
    Optional<Invoice> findByIdAndTenantId(Long id, String tenantId);
    Optional<Invoice> findByTenantIdAndInvoiceNumber(String tenantId, String invoiceNumber);
    List<Invoice> findByTenantIdAndRecipientIdAndInvoiceType(String tenantId, Long recipientId, String invoiceType);
    List<Invoice> findByTenantIdAndStatus(String tenantId, String status);
    long countByTenantId(String tenantId);
}
