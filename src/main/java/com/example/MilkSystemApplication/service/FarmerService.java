package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.MilkCollectionRepository;
import com.example.MilkSystemApplication.repository.PaymentRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FarmerService {

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private MilkCollectionRepository milkRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }

    private synchronized String generateFarmerCode() {
        long count = farmerRepo.count() + 1;
        String candidate = String.format("FMR-%04d", count);
        int suffix = 1;
        while (farmerRepo.findByCode(candidate).isPresent()) {
            candidate = String.format("FMR-%04d", count + suffix);
            suffix++;
        }
        return candidate;
    }

    public Farmer addFarmer(Farmer farmer) {
        if (farmer.getTenantId() == null || farmer.getTenantId().equals("default-tenant")) {
            farmer.setTenantId(getTenantId());
        }
        if (farmer.getCode() == null || farmer.getCode().trim().isEmpty()) {
            farmer.setCode(generateFarmerCode());
        } else {
            farmer.setCode(farmer.getCode().trim().toUpperCase());
        }
        if (farmer.getRegistrationDate() == null) {
            farmer.setRegistrationDate(LocalDate.now());
        }
        if (farmer.getPassword() == null || farmer.getPassword().trim().isEmpty()) {
            farmer.setPassword("farmer123");
        }
        if (farmer.getRole() == null || farmer.getRole().trim().isEmpty()) {
            farmer.setRole("ROLE_FARMER");
        }
        return farmerRepo.save(farmer);
    }

    public List<Farmer> getAllFarmers() {
        return farmerRepo.findByTenantIdOrderByNameAsc(getTenantId());
    }

    public Optional<Farmer> getFarmerById(Long id) {
        return farmerRepo.findByIdAndTenantId(id, getTenantId());
    }

    public Optional<Farmer> getFarmerByPhone(String phone) {
        return farmerRepo.findByTenantIdAndPhone(getTenantId(), phone);
    }

    public Optional<Farmer> getFarmerByCode(String code) {
        if (code == null) return Optional.empty();
        String c = code.trim();
        Optional<Farmer> opt = farmerRepo.findByTenantIdAndCodeIgnoreCase(getTenantId(), c);
        if (opt.isEmpty()) {
            opt = farmerRepo.findByCodeIgnoreCase(c);
        }
        return opt;
    }

    public Farmer updateFarmer(Long id, Farmer updated) {
        return farmerRepo.findByIdAndTenantId(id, getTenantId()).map(farmer -> {
            farmer.setName(updated.getName());
            farmer.setPhone(updated.getPhone());
            farmer.setVillage(updated.getVillage());
            farmer.setAddress(updated.getAddress());
            farmer.setBuffaloCount(updated.getBuffaloCount());
            farmer.setCowCount(updated.getCowCount());
            farmer.setCode(updated.getCode());
            farmer.setBankAccountNo(updated.getBankAccountNo());
            farmer.setIfscCode(updated.getIfscCode());
            farmer.setUpiId(updated.getUpiId());
            if (updated.getPassword() != null && !updated.getPassword().trim().isEmpty()) {
                farmer.setPassword(updated.getPassword());
            }
            return farmerRepo.save(farmer);
        }).orElseThrow(() -> new RuntimeException("Farmer not found with id " + id));
    }

    public boolean changePassword(Long farmerId, String oldPassword, String newPassword, PasswordEncoder encoder) {
        Optional<Farmer> farmerOpt = farmerRepo.findById(farmerId);
        if (farmerOpt.isEmpty()) return false;
        Farmer farmer = farmerOpt.get();

        boolean match = encoder.matches(oldPassword, farmer.getPassword()) || oldPassword.equals(farmer.getPassword());
        if (!match) {
            return false;
        }

        farmer.setPassword(encoder.encode(newPassword));
        farmerRepo.save(farmer);
        return true;
    }

    public void resetPassword(Long farmerId, String newRawPassword, PasswordEncoder encoder) {
        farmerRepo.findById(farmerId).ifPresent(farmer -> {
            farmer.setPassword(encoder.encode(newRawPassword));
            farmerRepo.save(farmer);
        });
    }

    public void deleteFarmer(Long id) {
        farmerRepo.findByIdAndTenantId(id, getTenantId()).ifPresent(farmerRepo::delete);
    }

    public List<Farmer> searchFarmers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllFarmers();
        }
        String q = query.trim();
        return farmerRepo.findByTenantIdAndNameContainingIgnoreCaseOrTenantIdAndPhoneContaining(getTenantId(), q, getTenantId(), q);
    }

    public long getTotalFarmersCount() {
        return farmerRepo.countByTenantId(getTenantId());
    }

    public FarmerFinancialSummary getFarmerSummary(Long farmerId) {
        Farmer farmer = farmerRepo.findByIdAndTenantId(farmerId, getTenantId())
                .orElseThrow(() -> new RuntimeException("Farmer not found: " + farmerId));
        double litres = milkRepo.getTotalLitresByTenantAndFarmer(getTenantId(), farmerId);
        double earned = milkRepo.getTotalAmountByTenantAndFarmer(getTenantId(), farmerId);
        double paid = paymentRepo.getTotalPaidByTenantAndFarmer(getTenantId(), farmerId);
        long count = milkRepo.findByTenantIdAndFarmerIdOrderByDateDescIdDesc(getTenantId(), farmerId).size();

        return new FarmerFinancialSummary(farmer, litres, earned, paid, count);
    }

    public List<FarmerFinancialSummary> getAllFarmerSummaries() {
        List<Farmer> farmers = farmerRepo.findByTenantIdOrderByNameAsc(getTenantId());
        List<FarmerFinancialSummary> summaries = new ArrayList<>();
        for (Farmer f : farmers) {
            double litres = milkRepo.getTotalLitresByTenantAndFarmer(getTenantId(), f.getId());
            double earned = milkRepo.getTotalAmountByTenantAndFarmer(getTenantId(), f.getId());
            double paid = paymentRepo.getTotalPaidByTenantAndFarmer(getTenantId(), f.getId());
            long count = milkRepo.findByTenantIdAndFarmerIdOrderByDateDescIdDesc(getTenantId(), f.getId()).size();
            summaries.add(new FarmerFinancialSummary(f, litres, earned, paid, count));
        }
        return summaries;
    }
}