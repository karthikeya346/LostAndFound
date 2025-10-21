package com.lostfound.controller;

import com.lostfound.dao.AuditLogDAO;
import model.AuditLog;

import java.util.List;

public class AuditLogController {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // ---------------- Logging ----------------

    /** Record a new action in the audit log with full context. */
    public boolean logAction(int userId, String action, Integer itemId, Integer claimId, String details) {
        return auditLogDAO.logAction(userId, action, itemId, claimId, details);
    }

    /** Convenience overload for simple actions without item/claim context. */
    public boolean logAction(int userId, String action, String details) {
        return auditLogDAO.logAction(userId, action, details);
    }

    // ---------------- Fetching ----------------

    /** Fetch recent logs (for admin dashboard). */
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogDAO.getRecentLogs(limit);
    }

    /** Fetch logs for a specific user. */
    public List<AuditLog> getLogsByUser(int userId) {
        return auditLogDAO.getLogsByUser(userId);
    }

    /** Fetch logs for a specific action type. */
    public List<AuditLog> getLogsByAction(String action) {
        return auditLogDAO.getLogsByAction(action);
    }
}