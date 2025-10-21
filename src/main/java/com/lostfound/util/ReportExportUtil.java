package com.lostfound.util;

import model.AuditLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

// For PDF export
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class ReportExportUtil {

    /**
     * Export analytics summary (counts, charts data) to CSV.
     */
    public static void exportAnalyticsToCSV(String filePath,
                                            Map<String, Integer> claimStats,
                                            Map<String, Integer> itemsByMonth,
                                            Map<String, Integer> topLocations,
                                            Map<String, Integer> userGrowth) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("=== Claim Stats by Status ===\n");
            for (Map.Entry<String, Integer> entry : claimStats.entrySet()) {
                writer.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            writer.append("\n=== Items Reported by Month ===\n");
            for (Map.Entry<String, Integer> entry : itemsByMonth.entrySet()) {
                writer.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            writer.append("\n=== Top Locations ===\n");
            for (Map.Entry<String, Integer> entry : topLocations.entrySet()) {
                writer.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            writer.append("\n=== User Growth by Month ===\n");
            for (Map.Entry<String, Integer> entry : userGrowth.entrySet()) {
                writer.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
        }
    }

    /**
     * Export audit logs to CSV.
     */
    public static void exportAuditLogsToCSV(String filePath, List<AuditLog> logs) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("ID,UserID,Action,ItemID,ClaimID,Details,CreatedAt\n");
            for (AuditLog log : logs) {
                writer.append(String.valueOf(log.getId())).append(",")
                      .append(String.valueOf(log.getUserId())).append(",")
                      .append(log.getAction()).append(",")
                      .append(String.valueOf(log.getItemId())).append(",")
                      .append(String.valueOf(log.getClaimId())).append(",")
                      .append(log.getDetails() == null ? "" : log.getDetails()).append(",")
                      .append(String.valueOf(log.getCreatedAt())).append("\n");
            }
        }
    }

    /**
     * Export audit logs to PDF (using iText).
     */
    public static void exportAuditLogsToPDF(String filePath, List<AuditLog> logs) throws Exception {
        Document document = new Document();
        PdfWriter.getInstance(document, new java.io.FileOutputStream(filePath));
        document.open();

        document.add(new Paragraph("Audit Logs Report"));
        document.add(new Paragraph("Generated on: " + new java.util.Date()));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.addCell("ID");
        table.addCell("UserID");
        table.addCell("Action");
        table.addCell("ItemID");
        table.addCell("ClaimID");
        table.addCell("Details");

        for (AuditLog log : logs) {
            table.addCell(String.valueOf(log.getId()));
            table.addCell(String.valueOf(log.getUserId()));
            table.addCell(log.getAction());
            table.addCell(String.valueOf(log.getItemId()));
            table.addCell(String.valueOf(log.getClaimId()));
            table.addCell(log.getDetails() == null ? "" : log.getDetails());
        }

        document.add(table);
        document.close();
    }
}