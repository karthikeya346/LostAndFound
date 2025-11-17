package com.lostfound.controller;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.NotificationDAO;
import model.Item;
import model.Claim;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ClaimController {

    @Autowired
    private ClaimDAO claimDAO;
    @Autowired
    private NotificationDAO notificationDAO;
    @Autowired
    private ItemDAO itemDAO;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createClaim(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Claim claim = new Claim(
                (Integer) request.get("itemId"),
                (Integer) request.get("userId")
            );
            // Rule: reporter of a FOUND item cannot claim their own item
            Item item = itemDAO.getItemById(claim.getItemId());
            if (item == null) {
                response.put("success", false);
                response.put("message", "Item not found");
                return ResponseEntity.badRequest().body(response);
            }
            if (item.getUserId() == claim.getUserId() && "FOUND".equalsIgnoreCase(item.getType())) {
                response.put("success", false);
                response.put("message", "You cannot claim an item you reported as FOUND");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = claimDAO.createClaim(claim);
            if (success) {
                try { notificationDAO.saveNotification(item.getUserId(), "CLAIM_CREATED", "Your item '" + item.getTitle() + "' has a new claim"); } catch (Exception ignored) {}
                response.put("success", true);
                response.put("message", "Claim created successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to create claim");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingClaims() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Claim> claims = claimDAO.getPendingClaims();
            response.put("success", true);
            response.put("claims", claims);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getClaimsByUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Claim> claims = claimDAO.getClaimsByUser(userId);
            response.put("success", true);
            response.put("claims", claims != null ? claims : Collections.emptyList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveClaim(@PathVariable int id, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = claimDAO.approveClaim(id, adminId);
            if (success) {
                response.put("success", true);
                response.put("message", "Claim approved successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to approve claim");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectClaim(@PathVariable int id, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = claimDAO.rejectClaim(id, adminId);
            if (success) {
                response.put("success", true);
                response.put("message", "Claim rejected successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to reject claim");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelClaim(@PathVariable int id, @RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = claimDAO.cancelClaim(id, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "Claim cancelled successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to cancel claim");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/mark-returned")
    public ResponseEntity<Map<String, Object>> markReturned(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean ok = claimDAO.markReturned(id);
            if (ok) {
                response.put("success", true);
                response.put("message", "Claim marked as RETURNED");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update claim status");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}