package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.Customer;
import com.example.MilkSystemApplication.repository.CustomerRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        String tenantId = getTenantId();
        return customerRepository.findByTenantId(tenantId);
    }

    public Optional<Customer> getCustomerById(Long id) {
        String tenantId = getTenantId();
        return customerRepository.findByIdAndTenantId(id, tenantId);
    }

    public List<Customer> searchCustomers(String query) {
        String tenantId = getTenantId();
        return customerRepository.findByTenantIdAndNameContainingIgnoreCase(tenantId, query);
    }

    public Customer addCustomer(Customer customer) {
        customer.setTenantId(getTenantId());
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updated) {
        String tenantId = getTenantId();
        Customer customer = customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        customer.setName(updated.getName());
        customer.setPhone(updated.getPhone());
        customer.setEmail(updated.getEmail());
        customer.setAddress(updated.getAddress());
        customer.setCustomerType(updated.getCustomerType());
        customer.setDailyQuotaLitres(updated.getDailyQuotaLitres());
        customer.setPricePerLitre(updated.getPricePerLitre());
        customer.setActive(updated.isActive());

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        String tenantId = getTenantId();
        customerRepository.findByIdAndTenantId(id, tenantId).ifPresent(customerRepository::delete);
    }

    public long getCustomerCount() {
        return customerRepository.countByTenantId(getTenantId());
    }

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }
}
