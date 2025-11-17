package com.lostfound.controller;

import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.UserDAO;
import model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class NotificationController {

    @Autowired
    private NotificationDAO notificationDAO;
    @Autowired
    private UserDAO userDAO;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = (Integer) request.get("userId");
            String type = (String) request.get("type");
            String message = (String) request.get("message");
            
            boolean success = notificationDAO.saveNotification(userId, type, message);
            if (success) {
                response.put("success", true);
                response.put("message", "Notification created successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to create notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getNotificationsByUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
            response.put("success", true);
            response.put("notifications", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Notification> notifications = notificationDAO.getAllNotifications();
            response.put("success", true);
            response.put("notifications", notifications);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = notificationDAO.markAsRead(id);
            if (success) {
                response.put("success", true);
                response.put("message", "Notification marked as read");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to mark notification as read");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable int id, @RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = notificationDAO.deleteNotification(id, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "Notification deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}/admin")
    public ResponseEntity<Map<String, Object>> adminDeleteNotification(@PathVariable int id, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!userDAO.isAdmin(adminId)) {
                response.put("success", false);
                response.put("message", "Forbidden: admin only");
                return ResponseEntity.status(403).body(response);
            }
            boolean success = notificationDAO.adminDeleteById(id);
            if (success) {
                response.put("success", true);
                response.put("message", "Notification deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete notification");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/clear-expired")
    public ResponseEntity<Map<String, Object>> clearExpiredOtps() {
        Map<String, Object> response = new HashMap<>();
        try {
            int cleared = notificationDAO.clearExpiredOtps();
            response.put("success", true);
            response.put("cleared", cleared);
            response.put("message", "Expired OTPs cleared successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}