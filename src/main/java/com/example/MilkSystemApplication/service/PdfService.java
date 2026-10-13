package com.example.MilkSystemApplication.service;

import com.example.MilkSystemApplication.model.Farmer;
import com.example.MilkSystemApplication.model.MilkCollection;
import com.example.MilkSystemApplication.model.Tenant;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public byte[] generateFarmerSettlementBill(Tenant tenant,
                                              Farmer farmer,
                                              List<MilkCollection> collections,
                                              LocalDate startDate,
                                              LocalDate endDate,
                                              double deductions,
                                              String invoiceNumber) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors & Fonts
            Color primaryColor = new Color(24, 76, 120); // Deep Navy
            Color headerBg = new Color(240, 244, 248);
            Color altRowBg = new Color(248, 250, 252);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, primaryColor);
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

            // 1. Dairy Header
            Paragraph dairyName = new Paragraph(tenant != null ? tenant.getBusinessName().toUpperCase() : "DAIRY CO-OPERATIVE SOCIETY", titleFont);
            dairyName.setAlignment(Element.ALIGN_CENTER);
            document.add(dairyName);

            String addressStr = (tenant != null && tenant.getVillage() != null ? tenant.getVillage() + ", " : "") +
                    (tenant != null && tenant.getState() != null ? tenant.getState() : "India") +
                    (tenant != null && tenant.getPhone() != null ? " | Phone: " + tenant.getPhone() : "");
            Paragraph dairyAddress = new Paragraph(addressStr, subTitleFont);
            dairyAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(dairyAddress);

            Paragraph docType = new Paragraph("MILK PRODUCER PAYMENT BILL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY));
            docType.setAlignment(Element.ALIGN_CENTER);
            docType.setSpacingBefore(8);
            docType.setSpacingAfter(10);
            document.add(docType);

            // 2. Info Box (2 columns: Bill Info & Farmer Info)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});
            infoTable.setSpacingAfter(12);

            // Left: Farmer Details
            PdfPCell farmerCell = new PdfPCell();
            farmerCell.setBorder(Rectangle.BOX);
            farmerCell.setBorderColor(new Color(220, 224, 230));
            farmerCell.setPadding(8);
            farmerCell.setBackgroundColor(headerBg);
            farmerCell.addElement(new Paragraph("Farmer Name: " + farmer.getName(), boldFont));
            farmerCell.addElement(new Paragraph("Farmer ID: #" + farmer.getId() + (farmer.getCode() != null ? " (" + farmer.getCode() + ")" : ""), regularFont));
            farmerCell.addElement(new Paragraph("Phone: " + farmer.getPhone() + " | Village: " + (farmer.getVillage() != null ? farmer.getVillage() : "N/A"), regularFont));
            if (farmer.getBankAccountNo() != null && !farmer.getBankAccountNo().isEmpty()) {
                farmerCell.addElement(new Paragraph("Bank A/C: " + farmer.getBankAccountNo() + " (IFSC: " + farmer.getIfscCode() + ")", regularFont));
            }
            infoTable.addCell(farmerCell);

            // Right: Bill Details
            PdfPCell billCell = new PdfPCell();
            billCell.setBorder(Rectangle.BOX);
            billCell.setBorderColor(new Color(220, 224, 230));
            billCell.setPadding(8);
            billCell.setBackgroundColor(headerBg);
            billCell.addElement(new Paragraph("Bill No: " + invoiceNumber, boldFont));
            billCell.addElement(new Paragraph("Date: " + LocalDate.now().format(DATE_FORMAT), regularFont));
            billCell.addElement(new Paragraph("Period: " + startDate.format(DATE_FORMAT) + " to " + endDate.format(DATE_FORMAT), regularFont));
            billCell.addElement(new Paragraph("Status: READY FOR PAYOUT", boldFont));
            infoTable.addCell(billCell);

            document.add(infoTable);

            // 3. Collection Breakdown Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.4f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.2f});
            table.setHeaderRows(1);

            String[] headers = {"Date", "Shift", "Type", "Litres", "FAT %", "Rate (₹)", "Amount (₹)"};
            for (String h : headers) {
                PdfPCell c = new PdfPCell(new Phrase(h, tableHeaderFont));
                c.setBackgroundColor(primaryColor);
                c.setHorizontalAlignment(Element.ALIGN_CENTER);
                c.setPadding(5);
                table.addCell(c);
            }

            double totalLitres = 0;
            double totalAmount = 0;
            double sumFat = 0;
            int count = 0;

            for (MilkCollection mc : collections) {
                totalLitres += mc.getLitres();
                totalAmount += mc.getAmount();
                sumFat += mc.getFatPercentage();
                count++;

                Color bg = (count % 2 == 0) ? altRowBg : Color.WHITE;

                addCell(table, mc.getDate().format(DATE_FORMAT), Element.ALIGN_CENTER, regularFont, bg);
                addCell(table, mc.getShift(), Element.ALIGN_CENTER, regularFont, bg);
                addCell(table, mc.getMilkType() != null ? mc.getMilkType() : "BUFFALO", Element.ALIGN_CENTER, regularFont, bg);
                addCell(table, String.format("%.2f", mc.getLitres()), Element.ALIGN_RIGHT, regularFont, bg);
                addCell(table, String.format("%.1f%%", mc.getFatPercentage()), Element.ALIGN_RIGHT, regularFont, bg);
                addCell(table, String.format("%.2f", mc.getRatePerLitre()), Element.ALIGN_RIGHT, regularFont, bg);
                addCell(table, String.format("%.2f", mc.getAmount()), Element.ALIGN_RIGHT, regularFont, bg);
            }

            document.add(table);

            // 4. Summary & Totals
            double avgFat = count > 0 ? (sumFat / count) : 0.0;
            double netAmount = Math.max(0, totalAmount - deductions);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(10);

            addSummaryRow(summaryTable, "Total Litres:", String.format("%.2f L", totalLitres), boldFont);
            addSummaryRow(summaryTable, "Average FAT:", String.format("%.2f%%", avgFat), boldFont);
            addSummaryRow(summaryTable, "Gross Amount:", String.format("₹ %.2f", totalAmount), boldFont);
            if (deductions > 0) {
                addSummaryRow(summaryTable, "Deductions (Feed/Adv):", String.format("- ₹ %.2f", deductions), boldFont);
            }
            addSummaryRow(summaryTable, "Net Payable Amount:", String.format("₹ %.2f", netAmount), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, primaryColor));

            document.add(summaryTable);

            // 5. Signature and Notes
            Paragraph footerNotes = new Paragraph(
                    "Note: This is a computer-generated settlement invoice certified by " +
                            (tenant != null ? tenant.getBusinessName() : "the dairy society") + ".\n" +
                            "For any discrepancies, kindly contact the dairy administrator within 2 business days.",
                    regularFont);
            footerNotes.setSpacingBefore(20);
            document.add(footerNotes);

            PdfPTable signTable = new PdfPTable(2);
            signTable.setWidthPercentage(100);
            signTable.setSpacingBefore(35);
            
            PdfPCell signFarmer = new PdfPCell(new Paragraph("Farmer Signature", regularFont));
            signFarmer.setBorder(Rectangle.TOP);
            signFarmer.setHorizontalAlignment(Element.ALIGN_CENTER);
            signFarmer.setPaddingTop(5);
            signTable.addCell(signFarmer);

            PdfPCell signDairy = new PdfPCell(new Paragraph("Authorized Secretary / Manager", regularFont));
            signDairy.setBorder(Rectangle.TOP);
            signDairy.setHorizontalAlignment(Element.ALIGN_CENTER);
            signDairy.setPaddingTop(5);
            signTable.addCell(signDairy);

            document.add(signTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating PDF bill for farmer ID {}", farmer.getId(), e);
            throw new RuntimeException("Failed to generate PDF bill: " + e.getMessage(), e);
        }
    }

    private void addCell(PdfPTable table, String text, int align, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(4);
        cell.setBorderColor(new Color(230, 235, 240));
        table.addCell(cell);
    }

    private void addSummaryRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPadding(3);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setPadding(3);
        table.addCell(c2);
    }
}
