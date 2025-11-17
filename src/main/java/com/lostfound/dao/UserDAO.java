package com.lostfound.dao;

import model.User;
import com.lostfound.util.DBConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // ---------------- User Management ----------------

    /** Create a new user (registration). */
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getEmail());
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- Login OTP helpers ----------------

    /** Set a short-lived OTP for login on the user row. */
    public boolean setLoginOtp(int userId, String otp, Timestamp expiry) {
        String sql = "UPDATE users SET reset_token=?, reset_token_expiry=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setTimestamp(2, expiry);
            ps.setInt(3, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("Error setting login OTP: " + e.getMessage());
            return false;
        }
    }

    /** Verify OTP for a user (must match and be unexpired). */
public boolean verifyLoginOtp(int userId, String otp) {
    String sql = "SELECT reset_token, reset_token_expiry FROM users WHERE id=?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String dbToken = rs.getString("reset_token");
                Timestamp expiry = rs.getTimestamp("reset_token_expiry");

                System.out.println("[verifyLoginOtp] DB token: " + dbToken);
                System.out.println("[verifyLoginOtp] Entered token: " + otp);
                System.out.println("[verifyLoginOtp] Expiry: " + expiry + ", Now: " + new Timestamp(System.currentTimeMillis()));

                if (dbToken != null && otp != null &&
                    dbToken.trim().equals(otp.trim()) &&
                    (expiry == null || expiry.after(new Timestamp(System.currentTimeMillis())))) {
                    System.out.println("[verifyLoginOtp] ✅ OTP verified successfully");
                    return true;
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error verifying login OTP: " + e.getMessage());
    }
    return false;
}

    /** Clear OTP after successful verification. */
    public boolean clearLoginOtp(int userId) {
        String sql = "UPDATE users SET reset_token=NULL, reset_token_expiry=NULL WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            System.err.println("Error clearing login OTP: " + e.getMessage());
            return false;
        }
    }

    /** Fetch user by username (for login). */
    public User getByUsername(String username) {
        String sql = "SELECT id, username, password AS password_hash, role, email, status FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("status")
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching user by username: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /** Fetch user by ID. */
    public User getById(int id) {
        String sql = "SELECT id, username, password AS password_hash, role, email, status FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("email"),
                        rs.getString("status")
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /** Check if a username already exists. */
    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            e.printStackTrace();
            return true; // conservative: treat as exists on error
        }
    }

    /** Check if an email already exists. */
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }

    // ---------------- Session / Audit ----------------

    /** Log login event (non-fatal if audit fails). */
    public void logLogin(int userId) {
        try {
            auditLogDAO.logAction(userId, "LOGIN", 0, 0, "User logged in");
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Failed to log LOGIN: " + e.getMessage());
        }
    }

    /** Log logout event (non-fatal if audit fails). */
    public void logout(int userId) {
        try {
            auditLogDAO.logAction(userId, "LOGOUT", 0, 0, "User logged out");
        } catch (Exception e) {
            System.err.println("[AuditLogDAO] Failed to log LOGOUT: " + e.getMessage());
        }
    }

    // ---------------- Forgot Password Support ----------------

    /** Create a password reset token for a user identified by email. */
    public boolean createPasswordResetToken(String email, String token, Timestamp expiry) {
        String sql = "UPDATE users SET reset_token=?, reset_token_expiry=? WHERE LOWER(email)=LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            ps.setTimestamp(2, expiry);
            ps.setString(3, email);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            System.err.println("Error creating password reset token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Validate a reset token (must not be expired). */
    public boolean validateResetToken(String token) {
        String sql = "SELECT id FROM users WHERE reset_token=? AND reset_token_expiry > CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            System.err.println("Error validating reset token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Update password by token and clear reset fields. */
    public boolean updatePassword(String token, String newPasswordHash) {
        String sql = "UPDATE users SET password=?, reset_token=NULL, reset_token_expiry=NULL WHERE reset_token=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setString(2, token);
            return ps.executeUpdate() == 1;

        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- Role Checks ----------------

    /** Check if a user is an admin. */
    public boolean isAdmin(int userId) {
        String sql = "SELECT role FROM users WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "ADMIN".equalsIgnoreCase(rs.getString("role"));
                }
            }

        } catch (Exception e) {
            System.err.println("Error checking admin role: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ---------------- Admin Utilities (safe, minimal) ----------------

    /** Fetch all users (no password hashes exposed). */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, role, email, status FROM users ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    null, // do not expose password hash
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("status")
                );
                users.add(u);
            }

        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
        /** Ban a user (requires 'status' column in users table). */
    public boolean banUser(int userId, int adminId) {
        String sql = "UPDATE users SET status='DISABLED' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                try {
                    auditLogDAO.logAction(adminId, "USER_BANNED", userId, 0, "Admin banned user");
                } catch (Exception e) {
                    System.err.println("[AuditLogDAO] Failed to log USER_BANNED: " + e.getMessage());
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error banning user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /** Unban a user (requires 'status' column in users table). */
    public boolean unbanUser(int userId, int adminId) {
        String sql = "UPDATE users SET status='ACTIVE' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                try {
                    auditLogDAO.logAction(adminId, "USER_UNBANNED", userId, 0, "Admin unbanned user");
                } catch (Exception e) {
                    System.err.println("[AuditLogDAO] Failed to log USER_UNBANNED: " + e.getMessage());
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error unbanning user: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}