package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.dto.FarmerFinancialSummary;
import com.example.MilkSystemApplication.model.Farmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SmsNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationService.class);

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private MilkCollectionService milkService;

    public String generateMonthlyStatementMessage(Long farmerId) {
        FarmerFinancialSummary summary = farmerService.getFarmerSummary(farmerId);
        Farmer farmer = summary.getFarmer();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        return String.format(
                "🥛 Sri Lakshmi Milk Society - %s Statement\n" +
                "Dear %s (ID: #%d),\n" +
                "• Total Milk Supplied: %.1f Litres\n" +
                "• Gross Amount Earned: ₹%.2f\n" +
                "• Total Paid to Date: ₹%.2f\n" +
                "• Net Pending Balance: ₹%.2f\n" +
                "Thank you for your partnership! For queries: tnagaveeranjineylu@gmail.com",
                currentMonth,
                farmer.getName(),
                farmer.getId(),
                summary.getTotalLitres(),
                summary.getTotalEarned(),
                summary.getTotalPaid(),
                summary.getPendingBalance()
        );
    }

    public boolean sendSms(Long farmerId) {
        String msg = generateMonthlyStatementMessage(farmerId);
        Farmer farmer = farmerService.getFarmerById(farmerId).orElse(null);
        if (farmer == null || farmer.getPhone() == null) return false;

        // Simulate SMS gateway dispatch
        log.info("[SMS GATEWAY DISPATCH] To Phone: {} | Message: {}", farmer.getPhone(), msg.replace("\n", " "));
        return true;
    }

    public List<String> broadcastMonthlyStatementsToAll() {
        List<FarmerFinancialSummary> summaries = farmerService.getAllFarmerSummaries();
        List<String> results = new ArrayList<>();

        for (FarmerFinancialSummary s : summaries) {
            boolean sent = sendSms(s.getFarmerId());
            if (sent) {
                results.add("SMS statement sent to " + s.getFarmerName() + " (" + s.getFarmer().getPhone() + ") - Net Balance: ₹" + s.getPendingBalance());
            }
        }
        return results;
    }

    public String generateWhatsAppUrl(Long farmerId) {
        Farmer farmer = farmerService.getFarmerById(farmerId).orElse(null);
        if (farmer == null || farmer.getPhone() == null) return "#";

        String cleanPhone = farmer.getPhone().replaceAll("[^0-9]", "");
        if (cleanPhone.length() == 10) {
            cleanPhone = "91" + cleanPhone;
        }

        String msg = generateMonthlyStatementMessage(farmerId);
        String encoded = URLEncoder.encode(msg, StandardCharsets.UTF_8);

        return "https://wa.me/" + cleanPhone + "?text=" + encoded;
    }
}
