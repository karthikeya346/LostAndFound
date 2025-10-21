package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import java.sql.*;

public class AdminDAO {

    /**
     * Validate login for the single admin account.
     */
    public boolean validateAdmin(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password_hash = ? AND role = 'ADMIN'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password); // ⚠️ in production, compare hashed values
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if admin exists
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the admin ID (useful for audit logs).
     */
    public int getAdminId(String username) {
        String sql = "SELECT id FROM users WHERE username = ? AND role = 'ADMIN'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}