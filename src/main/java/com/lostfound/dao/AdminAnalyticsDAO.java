package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import java.sql.*;
import java.util.*;

public class AdminAnalyticsDAO {

    // ---------- Basic Counts ----------
    private int getCount(String sql) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getUserCount() { return getCount("SELECT COUNT(*) FROM users"); }
    public int getItemCount() { return getCount("SELECT COUNT(*) FROM items"); }
    public int getClaimCount() { return getCount("SELECT COUNT(*) FROM claims"); }
    public int getChatCount() { return getCount("SELECT COUNT(*) FROM chats"); }
    public int getAuditLogCount() { return getCount("SELECT COUNT(*) FROM audit_logs"); }

    // ---------- Time-based Trends ----------
    public Map<String, Integer> getClaimsPerMonth(int months) {
        String sql = "SELECT DATE_FORMAT(claim_date, '%Y-%m') AS ym, COUNT(*) " +
                     "FROM claims WHERE claim_date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                     "GROUP BY ym ORDER BY ym ASC";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, months);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getItemsPerWeek(int weeks) {
        String sql = "SELECT DATE_FORMAT(date_reported, '%x-%v') AS yw, COUNT(*) " +
                     "FROM items WHERE date_reported >= DATE_SUB(CURDATE(), INTERVAL ? WEEK) " +
                     "GROUP BY yw ORDER BY yw ASC";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, weeks);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString(1), rs.getInt(2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // ---------- Breakdowns ----------
    public Map<String, Integer> getClaimStatusBreakdown() {
        String sql = "SELECT status, COUNT(*) FROM claims GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Integer> getItemStatusBreakdown() {
        String sql = "SELECT status, COUNT(*) FROM items GROUP BY status";
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // ---------- Top Insights ----------
    public List<Map<String, Object>> getTopItemLocations(int limit) {
        String sql = "SELECT location, COUNT(*) AS cnt FROM items " +
                     "GROUP BY location ORDER BY cnt DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("location", rs.getString("location"));
                    row.put("count", rs.getInt("cnt"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getMostActiveUsers(int limit) {
        String sql = "SELECT u.username, COUNT(i.id) + COUNT(c.id) AS activity " +
                     "FROM users u " +
                     "LEFT JOIN items i ON u.id = i.user_id " +
                     "LEFT JOIN claims c ON u.id = c.user_id " +
                     "GROUP BY u.id ORDER BY activity DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("username", rs.getString("username"));
                    row.put("activity", rs.getInt("activity"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}