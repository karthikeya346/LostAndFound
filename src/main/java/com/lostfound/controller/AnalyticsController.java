package com.lostfound.controller;

import com.lostfound.service.AdminAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class AnalyticsController {

    @Autowired
    private AdminAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> dashboardData = analyticsService.getDashboardData();
            response.put("success", true);
            response.put("data", dashboardData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/items-over-time")
    public ResponseEntity<Map<String, Object>> getItemsOverTime(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getItemsOverTime(days);
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/claim-status")
    public ResponseEntity<Map<String, Object>> getClaimStatusBreakdown() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getClaimStatusBreakdown();
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user-activity")
    public ResponseEntity<Map<String, Object>> getUserActivity() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getUserActivity();
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/chat-activity")
    public ResponseEntity<Map<String, Object>> getChatActivity() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = analyticsService.getChatActivity();
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/item-type-breakdown")
    public ResponseEntity<Map<String, Object>> getItemTypeBreakdown() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Integer> data = analyticsService.getItemTypeBreakdown();
            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportAnalytics(@RequestParam String format) {
        Map<String, Object> response = new HashMap<>();
        try {
            String exportData = analyticsService.exportAnalytics(format);
            response.put("success", true);
            response.put("data", exportData);
            response.put("format", format);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
