package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import org.springframework.stereotype.Component;
import java.sql.*;

@Component
public class OTPDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Save a new OTP for a user (valid for 5 minutes)
    public boolean saveOTP(int userId, String otp) {
        String sql = "INSERT INTO otps (user_id, otp_code, expires_at, is_used) " +
                     "VALUES (?, ?, NOW() + INTERVAL 5 MINUTE, FALSE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, otp);
            boolean ok = ps.executeUpdate() == 1;

            if (ok) {
                try {
                    auditLogDAO.logAction(userId, "OTP_CREATED", 0, 0, "OTP generated for user");
                } catch (Exception logEx) {
                    System.err.println("[AuditLogDAO] Failed to log OTP_CREATED: " + logEx.getMessage());
                }
            }
            return ok;
        } catch (Exception e) {
            System.err.println("Error saving OTP: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Verify OTP for a user
    public boolean verifyOTP(int userId, String inputOtp) {
        String sql = "SELECT id, otp_code FROM otps " +
                     "WHERE user_id = ? AND expires_at > NOW() AND is_used = FALSE " +
                     "ORDER BY expires_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storedOtp = rs.getString("otp_code");
                    int otpId = rs.getInt("id");

                    if (storedOtp.trim().equals(inputOtp.trim())) {
                        markUsed(otpId);
                        try {
                            auditLogDAO.logAction(userId, "OTP_SUCCESS", 0, 0, "OTP verified successfully");
                        } catch (Exception logEx) {
                            System.err.println("[AuditLogDAO] Failed to log OTP_SUCCESS: " + logEx.getMessage());
                        }
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error verifying OTP: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            auditLogDAO.logAction(userId, "OTP_FAILED", 0, 0, "Invalid OTP attempt");
        } catch (Exception ignore) {}
        return false;
    }

    // Mark OTP as used
    private void markUsed(int otpId) {
        String sql = "UPDATE otps SET is_used = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error marking OTP as used: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Cleanup expired or used OTPs
    public void deleteExpiredOTPs() {
        String sql = "DELETE FROM otps WHERE expires_at < NOW() OR is_used = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[OTP Cleanup] Removed " + rows + " expired/used OTPs.");
                try {
                    auditLogDAO.logAction(0, "OTP_CLEANUP", 0, 0,
                            "Expired/used OTPs cleaned up. Count removed: " + rows);
                } catch (Exception logEx) {
                    System.err.println("[AuditLogDAO] Failed to log OTP_CLEANUP: " + logEx.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error cleaning up OTPs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}