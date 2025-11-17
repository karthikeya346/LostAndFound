package com.lostfound.controller;

import com.lostfound.dao.AuditLogDAO;
import model.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AuditLogController {

    @Autowired
    private AuditLogDAO auditLogDAO;

    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getRecentLogs(@RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AuditLog> logs = auditLogDAO.getRecentLogs(limit);
            response.put("success", true);
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<Map<String, Object>> getLogsByUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AuditLog> logs = auditLogDAO.getLogsByUser(userId);
            response.put("success", true);
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/logs/action/{action}")
    public ResponseEntity<Map<String, Object>> getLogsByAction(@PathVariable String action) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AuditLog> logs = auditLogDAO.getLogsByAction(action);
            response.put("success", true);
            response.put("logs", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> logAction(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int userId = (Integer) request.get("userId");
            String action = (String) request.get("action");
            Integer itemId = (Integer) request.get("itemId");
            Integer claimId = (Integer) request.get("claimId");
            String details = (String) request.get("details");

            boolean success = auditLogDAO.logAction(userId, action, itemId, claimId, details);
            if (success) {
                response.put("success", true);
                response.put("message", "Action logged successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to log action");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/logs")
    public ResponseEntity<Map<String, Object>> clearAllLogs() {
        Map<String, Object> response = new HashMap<>();
        try {
            int deleted = auditLogDAO.clearAll();
            response.put("success", true);
            response.put("deleted", deleted);
            response.put("message", "All audit logs cleared");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Map<String, Object>> deleteLogById(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean ok = auditLogDAO.deleteById(id);
            if (ok) {
                response.put("success", true);
                response.put("message", "Audit log deleted");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete audit log");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}