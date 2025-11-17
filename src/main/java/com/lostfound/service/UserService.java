package com.lostfound.service;

import com.lostfound.dao.UserDAO;
import com.lostfound.dao.AuditLogDAO;
import model.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Register a new user.
     * - Checks if username/email already exist
     * - Enforces SRM email domain restriction
     * - Stores password in plain text (for now)
     * - Logs registration attempts
     */
    public boolean registerUser(String username, String plainPassword, String role, String email) {
        if (!isValidSrmEmail(email)) {
            auditLogDAO.logAction(0, "REGISTRATION_FAILED", "Invalid email domain: " + email);
            return false;
        }

        if (userDAO.existsByUsername(username) || userDAO.existsByEmail(email)) {
            auditLogDAO.logAction(0, "REGISTRATION_FAILED", "Username or email already taken: " + username + " / " + email);
            return false;
        }

        User user = new User(0, username, plainPassword, role, email);
        boolean created = userDAO.createUser(user);

        if (created) {
            // Fetch back the saved user to get the generated ID
            User saved = userDAO.getByUsername(username);
            int uid = (saved != null) ? saved.getId() : 0;
            auditLogDAO.logAction(uid, "REGISTRATION_SUCCESS", "User registered: " + username);
        } else {
            auditLogDAO.logAction(0, "REGISTRATION_FAILED", "DB insert failed for: " + username);
        }

        return created;
    }

    /**
     * Authenticate a user by username and password.
     * Returns the User if credentials are valid, otherwise null.
     */
    public User login(String username, String plainPassword) {
        User user = userDAO.getByUsername(username);

        if (user == null) {
            auditLogDAO.logAction(0, "LOGIN_FAILED", "No such user: " + username);
            return null;
        }

        // Block banned users
        if (user.getStatus() != null && "DISABLED".equalsIgnoreCase(user.getStatus())) {
            auditLogDAO.logAction(user.getId(), "LOGIN_BLOCKED", "Banned user attempted login: " + username);
            return null;
        }

        if (plainPassword.equals(user.getPassword())) {
            auditLogDAO.logAction(user.getId(), "LOGIN_SUCCESS", "User logged in: " + username);
            return user;
        } else {
            auditLogDAO.logAction(user.getId(), "LOGIN_FAILED", "Invalid password for: " + username);
            return null;
        }
    }

    /** Fetch a user by ID. */
    public User getUserById(int id) {
        return userDAO.getById(id);
    }

    // ---------------- Forgot Password Support ----------------

    /** Generate a reset token for a given email. Returns the token if successful, null otherwise. */
    public String initiatePasswordReset(String email) {
        String token = UUID.randomUUID().toString();
        Timestamp expiry = new Timestamp(System.currentTimeMillis() + (15 * 60 * 1000)); // 15 minutes

        boolean ok = userDAO.createPasswordResetToken(email, token, expiry);
        if (ok) {
            auditLogDAO.logAction(0, "PASSWORD_RESET_REQUEST", "Reset token generated for email: " + email);
            return token;
        } else {
            auditLogDAO.logAction(0, "PASSWORD_RESET_FAILED", "No account found for email: " + email);
            return null;
        }
    }

    /** Validate a reset token. */
    public boolean validateResetToken(String token) {
        return userDAO.validateResetToken(token);
    }

    /** Reset password using a valid token. */
    public boolean resetPassword(String token, String newPassword) {
        boolean ok = userDAO.updatePassword(token, newPassword);
        if (ok) {
            auditLogDAO.logAction(0, "PASSWORD_RESET_SUCCESS", "Password reset with token: " + token);
        } else {
            auditLogDAO.logAction(0, "PASSWORD_RESET_FAILED", "Failed reset attempt with token: " + token);
        }
        return ok;
    }

    // ---------------- Helper methods ----------------

    /** Validate SRM email domain. */
    private boolean isValidSrmEmail(String email) {
        return email != null && email.toLowerCase().endsWith("@srmist.edu.in");
    }
}