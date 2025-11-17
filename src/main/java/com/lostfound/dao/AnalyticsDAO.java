package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

@Component
public class AnalyticsDAO {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDAO.class);

    // 📊 Claims grouped by status (Pending, Approved, Rejected, Returned)
    public Map<String, Integer> getClaimStatsByStatus() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) AS total FROM claims GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("total"));
            }
        } catch (Exception e) {
            logger.error("Error fetching claim stats by status", e);
        }
        return stats;
    }

    // 📊 Items reported grouped by month (YYYY-MM)
    public Map<String, Integer> getItemsReportedByMonth() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(date_reported, '%Y-%m') AS month, COUNT(*) AS total " +
                     "FROM items GROUP BY month ORDER BY month ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("month"), rs.getInt("total"));
            }
        } catch (Exception e) {
            logger.error("Error fetching items reported by month", e);
        }
        return stats;
    }

    // 📊 Top locations where items are reported
    public Map<String, Integer> getTopLocations(int limit) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT location, COUNT(*) AS total FROM items " +
                     "GROUP BY location ORDER BY total DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("location"), rs.getInt("total"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }

    // 📊 Users registered by month (extra useful for growth charts)
    public Map<String, Integer> getUserRegistrationsByMonth() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS total " +
                     "FROM users GROUP BY month ORDER BY month ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                stats.put(rs.getString("month"), rs.getInt("total"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stats;
    }
}