package com.lostfound.controller;

import com.lostfound.service.AdminAnalyticsService;
import com.lostfound.dao.AnalyticsDAO;

import java.util.HashMap;
import java.util.Map;

public class AdminDashboardController {

    private final AdminAnalyticsService analyticsService = new AdminAnalyticsService();
    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public Map<String, Object> getDashboardData() {
        Map<String, Object> response = new HashMap<>();

        // ✅ Summary counts (tiles)
        response.put("totalUsers", analyticsService.getTotalUsers());
        response.put("totalItems", analyticsService.getTotalItems());
        response.put("totalClaims", analyticsService.getTotalClaims());
        response.put("totalChats", analyticsService.getTotalChats());
        response.put("totalLogs", analyticsService.getTotalAuditLogs());

        // ✅ Charts & trends
        Map<String, Integer> claimStats = analyticsDAO.getClaimStatsByStatus();
        Map<String, Integer> itemsByMonth = analyticsDAO.getItemsReportedByMonth();
        Map<String, Integer> topLocations = analyticsDAO.getTopLocations(5);
        Map<String, Integer> userGrowth = analyticsDAO.getUserRegistrationsByMonth();

        response.put("claimStats", claimStats);
        response.put("itemsByMonth", itemsByMonth);
        response.put("topLocations", topLocations);
        response.put("userGrowth", userGrowth);

        return response; // In Swing, you’d pass this Map back to the UI layer
    }
}