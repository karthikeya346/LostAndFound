package com.lostfound.service;

import com.lostfound.dao.OTPDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.AuditLogDAO;

import java.util.Random;

public class OTPService {
    private final OTPDAO otpDAO = new OTPDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final Random random = new Random();

    // Generate and store a new OTP for a user
    public String generateOTP(int userId, String email) {
        if (!isValidSrmEmail(email)) {
            try {
                auditLogDAO.logAction(userId, "OTP_GENERATION_FAILED", "Invalid email domain: " + email);
            } catch (Exception ignore) {}
            return null;
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        boolean saved = otpDAO.saveOTP(userId, otp);

        if (saved) {
            notificationDAO.saveNotification(userId, "OTP", "Your OTP is: " + otp);
            try {
                auditLogDAO.logAction(userId, "OTP_GENERATED", "OTP generated and notification sent");
            } catch (Exception ignore) {}
            return otp;
        } else {
            try {
                auditLogDAO.logAction(userId, "OTP_GENERATION_FAILED", "Failed to save OTP in DB");
            } catch (Exception ignore) {}
            return null;
        }
    }

    // Verify an OTP for a user
    public boolean verifyOTP(int userId, String inputOtp) {
        boolean valid = otpDAO.verifyOTP(userId, inputOtp);

        try {
            if (valid) {
                auditLogDAO.logAction(userId, "OTP_VERIFIED", "OTP verified successfully");
            } else {
                auditLogDAO.logAction(userId, "OTP_FAILED", "Invalid or expired OTP entered: " + inputOtp);
            }
        } catch (Exception ignore) {}

        return valid;
    }

    // Helper
    private boolean isValidSrmEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@srmist.edu.in");
    }
}