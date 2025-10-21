package com.lostfound.service;

import com.lostfound.dao.AdminAnalyticsDAO;

public class AdminAnalyticsService {
    private final AdminAnalyticsDAO dao = new AdminAnalyticsDAO();

    public int getTotalUsers() { return dao.getUserCount(); }
    public int getTotalItems() { return dao.getItemCount(); }
    public int getTotalClaims() { return dao.getClaimCount(); }
    public int getTotalChats() { return dao.getChatCount(); }
    public int getTotalAuditLogs() { return dao.getAuditLogCount(); }
}