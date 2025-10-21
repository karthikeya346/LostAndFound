package com.lostfound.controller;

import com.lostfound.dao.UserDAO;
import model.User;

import java.sql.Timestamp;
import java.util.List;

public class UserController {

    private final UserDAO userDAO = new UserDAO();

    /** Register a new user. */
    public boolean registerUser(User user) {
        return userDAO.createUser(user);
    }

    /** Login: fetch user by username and verify password hash externally. */
    public User loginUser(String username) {
        User user = userDAO.getByUsername(username);
        if (user != null) {
            userDAO.logLogin(user.getId());
        }
        return user;
    }

    /** Logout a user. */
    public void logoutUser(int userId) {
        userDAO.logout(userId);
    }

    /** Fetch user by ID. */
    public User getUserById(int id) {
        return userDAO.getById(id);
    }

    /** Check if username exists. */
    public boolean usernameExists(String username) {
        return userDAO.existsByUsername(username);
    }

    /** Check if email exists. */
    public boolean emailExists(String email) {
        return userDAO.existsByEmail(email);
    }

    /** Create password reset token. */
    public boolean createPasswordResetToken(String email, String token, Timestamp expiry) {
        return userDAO.createPasswordResetToken(email, token, expiry);
    }

    /** Validate reset token. */
    public boolean validateResetToken(String token) {
        return userDAO.validateResetToken(token);
    }

    /** Update password using reset token. */
    public boolean updatePassword(String token, String newPasswordHash) {
        return userDAO.updatePassword(token, newPasswordHash);
    }

    /** Check if user is admin. */
    public boolean isAdmin(int userId) {
        return userDAO.isAdmin(userId);
    }

    /** Ban a user (admin action). */
    public boolean banUser(int userId, int adminId) {
        return userDAO.banUser(userId, adminId);
    }

    /** Unban a user (admin action). */
    public boolean unbanUser(int userId, int adminId) {
        return userDAO.unbanUser(userId, adminId);
    }

    /** Fetch all users (admin view). */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
}