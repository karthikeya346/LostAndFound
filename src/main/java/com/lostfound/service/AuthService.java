package com.lostfound.service;

import model.User;
import org.springframework.stereotype.Service;

/**
 * AuthService acts as a thin wrapper around UserService.
 * Handles login, registration, and password reset logic for the UI layer.
 */
@Service
public class AuthService {

    private final UserService userService = new UserService();

    /**
     * Register a new user with default role = "USER".
     * SRM email restriction is enforced inside UserService.
     */
    public boolean register(String username, String plainPassword, String email) {
        return userService.registerUser(username, plainPassword, "USER", email);
    }

    /**
     * Authenticate a user by username and password.
     */
    public User login(String username, String plainPassword) {
        return userService.login(username, plainPassword);
    }

    // ---------------- Forgot Password Support ----------------

    /**
     * Initiate password reset: generates a token and saves it with expiry.
     * Returns the token if successful, null otherwise.
     */
    public String initiatePasswordReset(String email) {
        return userService.initiatePasswordReset(email);
    }

    /**
     * Validate a reset token.
     */
    public boolean validateResetToken(String token) {
        return userService.validateResetToken(token);
    }

    /**
     * Reset password using a valid token.
     */
    public boolean resetPassword(String token, String newPassword) {
        return userService.resetPassword(token, newPassword);
    }
}