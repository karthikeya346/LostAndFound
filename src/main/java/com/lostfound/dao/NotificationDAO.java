package com.lostfound.dao;

import model.Notification;
import com.lostfound.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for handling user notifications.
 */
public class NotificationDAO {

    /** Legacy method: stores only userId + message. */
    public boolean createNotification(int userId, String message) {
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Preferred method: stores userId + type + message. */
    public boolean saveNotification(int userId, String type, String message) {
        String sql = "INSERT INTO notifications (user_id, type, message) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, type);
            ps.setString(3, message);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            // Fallback if 'type' column missing
            System.err.println("[NotificationDAO] 'type' column missing? Falling back. Error: " + e.getMessage());
            return createNotification(userId, "[" + type + "] " + message);
        } catch (Exception e) {
            System.err.println("Error saving notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Fetch notifications for a specific user. */
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setId(rs.getInt("id"));
                    n.setUserId(rs.getInt("user_id"));
                    try { n.setType(rs.getString("type")); } catch (SQLException ignore) {}
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching notifications by user: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /** Fetch all notifications (for admin view). */
    public List<Notification> getAllNotifications() {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setUserId(rs.getInt("user_id"));
                try { n.setType(rs.getString("type")); } catch (SQLException ignore) {}
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        } catch (Exception e) {
            System.err.println("Error fetching all notifications: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /** Mark a notification as read. */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** ✅ Delete a notification (user/admin action). */
    public boolean deleteNotification(int notificationId, int userId) {
        String sql = "DELETE FROM notifications WHERE id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** ✅ Auto-clear expired OTP notifications (older than 5 minutes). */
    public int clearExpiredOtps() {
        String sql = "DELETE FROM notifications WHERE type='OTP' AND created_at < NOW() - INTERVAL 5 MINUTE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error clearing expired OTP notifications: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}