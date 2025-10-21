package com.lostfound.controller;

import com.lostfound.service.UserService;
import model.User;

public class AuthController {

    private final UserService userService = new UserService();

    // ---------------- Registration ----------------
    public boolean register(String username, String password, String role, String email) {
        return userService.registerUser(username, password, role, email);
    }

    // ---------------- Login ----------------
    public User login(String username, String password) {
        return userService.login(username, password);
    }
}