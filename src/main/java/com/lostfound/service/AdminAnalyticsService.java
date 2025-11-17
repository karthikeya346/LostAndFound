package com.lostfound.service;

import com.lostfound.dao.AdminAnalyticsDAO;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAnalyticsService {
    private final AdminAnalyticsDAO dao = new AdminAnalyticsDAO();

    public int getTotalUsers() { return dao.getUserCount(); }
    public int getTotalItems() { return dao.getItemCount(); }
    public int getTotalClaims() { return dao.getClaimCount(); }
    public int getTotalChats() { return dao.getChatCount(); }
    public int getTotalAuditLogs() { return dao.getAuditLogCount(); }
    public int getActiveClaims() { return dao.getActiveClaimCount(); }
    public int getOpenChats() { return dao.getChatOpenCount(); }
    public int getClosedChats() { return dao.getChatClosedCount(); }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", getTotalUsers());
        data.put("totalItems", getTotalItems());
        data.put("activeClaims", getActiveClaims());
        data.put("activeChats", getOpenChats());
        return data;
    }

    public Map<String, Object> getItemsOverTime(int days) {
        // Approximate weeks from days for existing DAO
        int weeks = Math.max(1, days / 7);
        Map<String, Integer> perWeek = dao.getItemsPerWeek(weeks);
        Map<String, Object> out = new HashMap<>();
        out.put("labels", perWeek.keySet().toArray(new String[0]));
        out.put("items", perWeek.values().stream().mapToInt(Integer::intValue).toArray());
        return out;
    }

    public Map<String, Object> getClaimStatusBreakdown() {
        Map<String, Integer> map = dao.getClaimStatusBreakdown();
        return new HashMap<>(map);
    }

    public Map<String, Object> getUserActivity() {
        // If you maintain a 'status' on users, wire here. For now return zeros to avoid schema assumptions.
        Map<String, Object> data = new HashMap<>();
        data.put("active", 0);
        data.put("banned", 0);
        return data;
    }

    public Map<String, Object> getChatActivity() {
        Map<String, Object> data = new HashMap<>();
        data.put("open", getOpenChats());
        data.put("closed", getClosedChats());
        return data;
    }

    public Map<String, Integer> getItemTypeBreakdown() {
        return dao.getItemTypeBreakdown();
    }

    public String exportAnalytics(String format) {
        return "Analytics data exported in " + format + " format";
    }
}