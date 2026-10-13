package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.AppSetting;
import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Tenant;
import com.example.MilkSystemApplication.repository.AppSettingRepository;
import com.example.MilkSystemApplication.repository.FarmerRepository;
import com.example.MilkSystemApplication.repository.MilkCollectionRepository;
import com.example.MilkSystemApplication.repository.TenantRepository;
import com.example.MilkSystemApplication.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MilkCollectionService {

    @Autowired
    private MilkCollectionRepository milkRepo;

    @Autowired
    private FarmerRepository farmerRepo;

    @Autowired
    private AppSettingRepository settingRepo;

    @Autowired
    private TenantRepository tenantRepo;

    private double rateFactor = 7.5;
    private double basePricePerLitre = 50.0;

    private String getTenantId() {
        String tenantId = TenantContext.getTenantId();
        return (tenantId != null && !tenantId.isEmpty()) ? tenantId : "default-tenant";
    }

    public double getRateFactor() {
        String tenantId = getTenantId();
        return tenantRepo.findById(tenantId)
                .map(Tenant::getBaseFatRate)
                .orElse(rateFactor);
    }

    public void setRateFactor(double newFactor) {
        if (newFactor > 0) {
            this.rateFactor = newFactor;
            String tenantId = getTenantId();
            tenantRepo.findById(tenantId).ifPresent(t -> {
                t.setBaseFatRate(newFactor);
                tenantRepo.save(t);
            });
        }
    }

    public AppSetting getSettings() {
        return settingRepo.findAll().stream().findFirst().orElseGet(() -> {
            AppSetting def = new AppSetting(rateFactor, basePricePerLitre, "Village Milk Collection Center");
            return settingRepo.save(def);
        });
    }

    public void updateSettings(AppSetting updated) {
        AppSetting current = getSettings();
        current.setRateFactor(updated.getRateFactor());
        current.setBasePricePerLitre(updated.getBasePricePerLitre());
        current.setSocietyName(updated.getSocietyName());
        current.setSocietyContact(updated.getSocietyContact());
        current.setSocietyAddress(updated.getSocietyAddress());
        if (updated.getSocietyUpiId() != null && !updated.getSocietyUpiId().trim().isEmpty()) {
            current.setSocietyUpiId(updated.getSocietyUpiId().trim());
        }
        if (updated.getSocietyUpiName() != null && !updated.getSocietyUpiName().trim().isEmpty()) {
            current.setSocietyUpiName(updated.getSocietyUpiName().trim());
        }
        if (updated.getSocietyQrImage() != null && !updated.getSocietyQrImage().trim().isEmpty()) {
            current.setSocietyQrImage(updated.getSocietyQrImage().trim());
        }
        current.setUpdatedAt(LocalDateTime.now());
        settingRepo.save(current);
        this.rateFactor = current.getRateFactor();
    }

    public double calculateRatePerLitre(double fatPercentage) {
        return Math.round(fatPercentage * getRateFactor() * 100.0) / 100.0;
    }

    public String determineQuality(double fatPercentage) {
        if (fatPercentage >= 7.0) return "Excellent (Grade A)";
        if (fatPercentage >= 6.0) return "Good";
        return "Standard";
    }

    public boolean canEditCollection(MilkCollection mc) {
        if (mc == null) return false;
        return !mc.isMorningEditLocked();
    }

    public MilkCollection addCollection(Long farmerId, double litres, double fatPercentage, Double customRate, String shift, LocalDate date, String quality, java.time.LocalTime collectionTime) {
        double ratePerLitre = (customRate != null && customRate > 0)
                ? Math.round(customRate * 100.0) / 100.0
                : calculateRatePerLitre(fatPercentage);

        double amount = Math.round(litres * ratePerLitre * 100.0) / 100.0;

        MilkCollection mc = new MilkCollection();
        mc.setTenantId(getTenantId());
        mc.setFarmerId(farmerId);
        mc.setLitres(Math.round(litres * 100.0) / 100.0);
        mc.setFatPercentage(Math.round(fatPercentage * 100.0) / 100.0);
        mc.setRatePerLitre(ratePerLitre);
        mc.setAmount(amount);
        mc.setShift(shift != null && !shift.trim().isEmpty() ? shift : "Morning");
        mc.setDate(date != null ? date : LocalDate.now());
        mc.setQuality((quality != null && !quality.trim().isEmpty()) ? quality.trim() : determineQuality(fatPercentage));
        mc.setCollectionTime(collectionTime != null ? collectionTime : java.time.LocalTime.now());

        return milkRepo.save(mc);
    }

    public MilkCollection addCollection(Long farmerId, double litres, double fatPercentage, Double customRate, String shift, LocalDate date) {
        return addCollection(farmerId, litres, fatPercentage, customRate, shift, date, null, null);
    }

    public MilkCollection addCollection(Long farmerId, double litres, double fatPercentage, String shift, LocalDate date) {
        return addCollection(farmerId, litres, fatPercentage, null, shift, date, null, null);
    }

    public MilkCollection addCollection(Long farmerId, double litres, double fatPercentage) {
        return addCollection(farmerId, litres, fatPercentage, null, "Morning", LocalDate.now(), null, null);
    }

    public MilkCollection updateCollection(Long id, Long farmerId, double litres, double fatPercentage, String shift, LocalDate date) {
        return updateCollection(id, farmerId, litres, fatPercentage, null, shift, date, null, null);
    }

    public MilkCollection updateCollection(Long id, Long farmerId, double litres, double fatPercentage, Double customRate, String shift, LocalDate date) {
        return updateCollection(id, farmerId, litres, fatPercentage, customRate, shift, date, null, null);
    }

    public MilkCollection updateCollection(Long id, Long farmerId, double litres, double fatPercentage, Double customRate, String shift, LocalDate date, String quality, java.time.LocalTime collectionTime) {
        return milkRepo.findByIdAndTenantId(id, getTenantId()).map(mc -> {
            if (mc.isMorningEditLocked()) {
                throw new IllegalStateException("Morning milk data cannot be edited after morning hours (12:00 PM)!");
            }

            double ratePerLitre = (customRate != null && customRate > 0)
                    ? Math.round(customRate * 100.0) / 100.0
                    : calculateRatePerLitre(fatPercentage);

            double amount = Math.round(litres * ratePerLitre * 100.0) / 100.0;

            mc.setFarmerId(farmerId);
            mc.setLitres(Math.round(litres * 100.0) / 100.0);
            mc.setFatPercentage(Math.round(fatPercentage * 100.0) / 100.0);
            mc.setRatePerLitre(ratePerLitre);
            mc.setAmount(amount);
            if (shift != null) mc.setShift(shift);
            if (date != null) mc.setDate(date);
            if (quality != null && !quality.trim().isEmpty()) {
                mc.setQuality(quality.trim());
            } else {
                mc.setQuality(determineQuality(fatPercentage));
            }
            if (collectionTime != null) mc.setCollectionTime(collectionTime);

            return milkRepo.save(mc);
        }).orElseThrow(() -> new RuntimeException("Record not found: " + id));
    }

    public List<MilkCollection> searchCollections(Long farmerId, LocalDate date, String quality) {
        String q = (quality != null && !quality.trim().isEmpty() && !quality.equalsIgnoreCase("ALL")) ? quality.trim() : null;
        List<MilkCollection> list = milkRepo.searchCollections(getTenantId(), farmerId, date, q);
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> searchFarmerCollections(Long farmerId, LocalDate startDate, LocalDate endDate, String shift, String quality) {
        String s = (shift != null && !shift.trim().isEmpty() && !shift.equalsIgnoreCase("ALL")) ? shift.trim() : null;
        String q = (quality != null && !quality.trim().isEmpty() && !quality.equalsIgnoreCase("ALL")) ? quality.trim() : null;
        List<MilkCollection> list = milkRepo.searchFarmerCollections(getTenantId(), farmerId, startDate, endDate, s, q);
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> getAllCollections() {
        List<MilkCollection> list = milkRepo.findByTenantIdOrderByDateDescIdDesc(getTenantId());
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> getCollectionsByFarmer(Long farmerId) {
        List<MilkCollection> list = milkRepo.findByTenantIdAndFarmerIdOrderByDateDescIdDesc(getTenantId(), farmerId);
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> getCollectionsByFarmerAndDates(Long farmerId, LocalDate startDate, LocalDate endDate) {
        List<MilkCollection> list = milkRepo.findByTenantIdAndFarmerIdAndDateBetweenOrderByDateDescIdDesc(getTenantId(), farmerId, startDate, endDate);
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> getCollectionsByDate(LocalDate date) {
        List<MilkCollection> list = milkRepo.findByTenantIdAndDateOrderByShiftDesc(getTenantId(), date);
        populateFarmerNames(list);
        return list;
    }

    public List<MilkCollection> getCollectionsBetweenDates(LocalDate start, LocalDate end) {
        List<MilkCollection> list = milkRepo.findByTenantIdAndDateBetweenOrderByDateDescIdDesc(getTenantId(), start, end);
        populateFarmerNames(list);
        return list;
    }

    public Optional<MilkCollection> getCollectionById(Long id) {
        return milkRepo.findByIdAndTenantId(id, getTenantId());
    }

    public void deleteCollection(Long id) {
        milkRepo.findByIdAndTenantId(id, getTenantId()).ifPresent(milkRepo::delete);
    }

    public double getTodayLitres() {
        return Math.round(milkRepo.getLitresByTenantAndDate(getTenantId(), LocalDate.now()) * 100.0) / 100.0;
    }

    public double getTodayAmount() {
        return Math.round(milkRepo.getAmountByTenantAndDate(getTenantId(), LocalDate.now()) * 100.0) / 100.0;
    }

    public double getTodayMorningLitres() {
        return Math.round(milkRepo.getLitresByDateAndShift(LocalDate.now(), "Morning") * 100.0) / 100.0;
    }

    public double getTodayEveningLitres() {
        return Math.round(milkRepo.getLitresByDateAndShift(LocalDate.now(), "Evening") * 100.0) / 100.0;
    }

    public double getMonthlyLitres() {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return Math.round(milkRepo.getLitresBetweenDatesByTenant(getTenantId(), start, end) * 100.0) / 100.0;
    }

    public double getMonthlyAmount() {
        YearMonth ym = YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return Math.round(milkRepo.getAmountBetweenDatesByTenant(getTenantId(), start, end) * 100.0) / 100.0;
    }

    public double getTotalLitres() {
        YearMonth ym = YearMonth.now();
        return Math.round(milkRepo.getLitresBetweenDatesByTenant(getTenantId(), ym.atDay(1).minusYears(1), ym.atEndOfMonth()) * 100.0) / 100.0;
    }

    public double getTotalAmount() {
        YearMonth ym = YearMonth.now();
        return Math.round(milkRepo.getAmountBetweenDatesByTenant(getTenantId(), ym.atDay(1).minusYears(1), ym.atEndOfMonth()) * 100.0) / 100.0;
    }

    public double getTotalLitresByFarmer(Long id) {
        return Math.round(milkRepo.getTotalLitresByTenantAndFarmer(getTenantId(), id) * 100.0) / 100.0;
    }

    public double getTotalAmountByFarmer(Long id) {
        return Math.round(milkRepo.getTotalAmountByTenantAndFarmer(getTenantId(), id) * 100.0) / 100.0;
    }

    public double getTodayAvgFat() {
        return Math.round(milkRepo.getAvgFatByTenantAndDate(getTenantId(), LocalDate.now()) * 100.0) / 100.0;
    }

    private void populateFarmerNames(List<MilkCollection> collections) {
        if (collections == null || collections.isEmpty()) return;
        Map<Long, String> farmerMap = farmerRepo.findByTenantIdOrderByNameAsc(getTenantId()).stream()
                .collect(Collectors.toMap(Farmer::getId, Farmer::getName, (e1, e2) -> e1));
        for (MilkCollection mc : collections) {
            mc.setFarmerName(farmerMap.getOrDefault(mc.getFarmerId(), "Farmer #" + mc.getFarmerId()));
        }
    }
}