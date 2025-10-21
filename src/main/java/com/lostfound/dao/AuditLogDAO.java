package com.lostfound.dao;

import model.AuditLog;
import com.lostfound.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for audit_logs table.
 * Handles inserting new logs and fetching logs for dashboard/traceability.
 */
public class AuditLogDAO {

    // ---------------- Insert Logs ----------------

    /**
     * Insert a new log entry with full context.
     * Logging failures are caught and never allowed to crash the app.
     */
    public boolean logAction(int userId, String action, Integer itemId, Integer claimId, String details) {
        String sql = "INSERT INTO audit_logs (user_id, action, item_id, claim_id, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, action);

            if (itemId != null) ps.setInt(3, itemId);
            else ps.setNull(3, Types.INTEGER);

            if (claimId != null) ps.setInt(4, claimId);
            else ps.setNull(4, Types.INTEGER);

            ps.setString(5, details);

            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Failed to log action '" + action + "': " + e.getMessage());
            return false; // swallow error so app continues
        }
    }

    /** Convenience overload for actions without item/claim context. */
    public boolean logAction(int userId, String action, String details) {
        return logAction(userId, action, null, null, details);
    }

    // ---------------- Fetch Logs ----------------

    /** Fetch recent logs (for admin dashboard). */
    public List<AuditLog> getRecentLogs(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT id, user_id, action, item_id, claim_id, details, created_at " +
                     "FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Error fetching recent logs: " + e.getMessage());
        }
        return logs;
    }

    /** Fetch all logs for a specific user. */
    public List<AuditLog> getLogsByUser(int userId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Error fetching logs by user: " + e.getMessage());
        }
        return logs;
    }

    /** Fetch all logs for a specific action type. */
    public List<AuditLog> getLogsByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, action);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRow(rs));
                }
            }
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Error fetching logs by action: " + e.getMessage());
        }
        return logs;
    }

    // ---------------- Helpers ----------------

    /** Helper to map a ResultSet row into an AuditLog object. */
    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setAction(rs.getString("action"));
        log.setItemId(rs.getInt("item_id"));
        log.setClaimId(rs.getInt("claim_id"));
        log.setDetails(rs.getString("details"));
        log.setCreatedAt(rs.getTimestamp("created_at"));
        return log;
    }
}