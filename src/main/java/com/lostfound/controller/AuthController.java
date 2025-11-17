package com.lostfound.controller;

import com.lostfound.service.UserService;
import com.lostfound.dao.UserDAO;
import model.User;
import com.lostfound.dao.NotificationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuthController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private NotificationDAO notificationDAO;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            String role = request.getOrDefault("role", "USER");

            boolean success = userService.registerUser(username, password, role, email);
            
            if (success) {
                response.put("success", true);
                response.put("message", "User registered successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Registration failed");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Prefer username to avoid client/userId mismatches
            String username = null;
            Object unameObj = request.get("username");
            if (unameObj != null) username = String.valueOf(unameObj).trim();

            Integer userId = null;
            if (username != null && !username.isBlank()) {
                User u = userDAO.getByUsername(username);
                if (u == null) {
                    response.put("success", false);
                    response.put("message", "User not found");
                    return ResponseEntity.status(404).body(response);
                }
                userId = u.getId();
            } else {
                Object uidObj = request.get("userId");
                if (uidObj instanceof Number) userId = ((Number) uidObj).intValue();
                else if (uidObj != null) userId = Integer.parseInt(String.valueOf(uidObj));
            }

            Object otpObj = request.get("otp");
            String otp = otpObj == null ? null : String.valueOf(otpObj).trim();
            if (userId == null || userId <= 0 || otp == null || otp.isBlank()) {
                response.put("success", false);
                response.put("message", "Invalid request");
                return ResponseEntity.badRequest().body(response);
            }
            System.out.println("[verifyLoginOtp] Method triggered for userId=" + userId + ", otp=" + otp);
            boolean ok = userDAO.verifyLoginOtp(userId, otp);
            if (ok) {
                userDAO.clearLoginOtp(userId);
                try { notificationDAO.saveNotification(userId, "OTP_VERIFIED", "Your OTP was verified successfully"); } catch (Exception ignored) {}
                response.put("success", true);
                response.put("message", "OTP verified");
                return ResponseEntity.ok(response);
            } else {
                System.err.println("[AuthController] OTP verify failed for userId=" + userId + ", otp=" + otp);
                response.put("success", false);
                response.put("message", "Invalid or expired OTP");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String username = request.get("username");
            String password = request.get("password");

            User user = userService.login(username, password);
            
            if (user != null) {
                // Generate a 6-digit OTP, persist with 5-minute expiry
                int otp = (int)(100000 + Math.random() * 900000);
                Timestamp expiry = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000);
                try { userDAO.setLoginOtp(user.getId(), String.valueOf(otp), expiry); } catch (Exception ignored) {}
                try { notificationDAO.saveNotification(user.getId(), "OTP_SENT", "Your one-time password has been generated"); } catch (Exception ignored) {}
                response.put("success", true);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ));
                response.put("otp", String.valueOf(otp));
                response.put("message", "Login successful");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userService.getUserById(userId);
            if (user != null) {
                response.put("success", true);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole()
                ));
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ---------------- Forgot Password ----------------

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = request.get("email");
            if (email == null || email.isBlank()) {
                response.put("success", false);
                response.put("message", "Email is required");
                return ResponseEntity.badRequest().body(response);
            }
            String token = userService.initiatePasswordReset(email.trim());
            if (token != null) {
                System.out.println("[ForgotPassword] Reset token for " + email + ": " + token);
                response.put("success", true);
                response.put("message", "Password reset token generated and sent");
                response.put("token", token);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No account found for this email");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
                response.put("success", false);
                response.put("message", "Token and newPassword are required");
                return ResponseEntity.badRequest().body(response);
            }
            if (!userService.validateResetToken(token.trim())) {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.badRequest().body(response);
            }
            boolean ok = userService.resetPassword(token.trim(), newPassword);
            if (ok) {
                response.put("success", true);
                response.put("message", "Password reset successful");
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("message", "Failed to reset password");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}