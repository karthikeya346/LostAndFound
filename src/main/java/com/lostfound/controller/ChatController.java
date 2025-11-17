package com.lostfound.controller;

import com.lostfound.dao.ChatDAO;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.UserDAO;
import model.ChatSession;
import model.ChatParticipant;
import model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ChatController {

    @Autowired
    private ChatDAO chatDAO;
    @Autowired
    private ClaimDAO claimDAO;
    @Autowired
    private UserDAO userDAO;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createChatSession(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer itemId = parseInt(request.get("itemId"));
            if (itemId == null) itemId = parseInt(request.get("item_id"));
            Integer claimId = parseInt(request.get("claimId"));
            if (claimId == null) claimId = parseInt(request.get("claim_id"));
            Integer startedBy = parseInt(request.get("startedBy"));
            if (startedBy == null) startedBy = parseInt(request.get("started_by"));
            // Derive itemId from claimId if not provided
            if ((itemId == null || itemId <= 0) && claimId != null) {
                int derived = claimDAO.getItemIdForClaim(claimId);
                if (derived > 0) itemId = derived; else itemId = null;
            }

            System.out.println("[ChatController] createChat payload: itemId=" + itemId + ", claimId=" + claimId + ", startedBy=" + startedBy);
            if (claimId == null || claimId <= 0) {
                response.put("success", false);
                response.put("message", "Invalid payload: claimId is required");
                return ResponseEntity.badRequest().body(response);
            }
            if (itemId == null || itemId <= 0) {
                response.put("success", false);
                response.put("message", "Invalid payload: could not resolve itemId for claimId=" + claimId);
                return ResponseEntity.badRequest().body(response);
            }
            if (startedBy == null) startedBy = 0;

            int chatId = chatDAO.createChatSession(itemId, claimId, startedBy);
            response.put("success", true);
            response.put("chatId", chatId);
            response.put("message", "Chat session created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/by-claim/{claimId}")
    public ResponseEntity<Map<String, Object>> getChatIdByClaim(@PathVariable int claimId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int chatId = claimDAO.getChatIdForClaim(claimId);
            if (chatId > 0) {
                response.put("success", true);
                response.put("chatId", chatId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "No chat for this claim");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Integer parseInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) { return null; }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllChatSessions(@RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!userDAO.isAdmin(adminId)) {
                response.put("success", false);
                response.put("message", "Forbidden: admin only");
                return ResponseEntity.status(403).body(response);
            }
            List<ChatSession> sessions = chatDAO.getAllChatSessions();
            response.put("success", true);
            response.put("sessions", sessions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getChatsForUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChatSession> sessions = chatDAO.getChatsForUser(userId);
            response.put("success", true);
            response.put("sessions", sessions);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getChatSession(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChatMessage> messages = chatDAO.getMessages(id);
            List<ChatParticipant> participants = chatDAO.getParticipants(id);
            
            response.put("success", true);
            response.put("messages", messages);
            response.put("participants", participants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(@PathVariable int id, @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            int senderId = (Integer) request.get("senderId");
            String message = (String) request.get("message");
            
            boolean success = chatDAO.saveMessage(id, senderId, message);
            if (success) {
                response.put("success", true);
                response.put("message", "Message sent successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to send message");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<Map<String, Object>> getMessages(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChatMessage> messages = chatDAO.getMessages(id);
            response.put("success", true);
            response.put("messages", messages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Map<String, Object>> closeChat(@PathVariable int id, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = chatDAO.closeChat(id, adminId);
            if (success) {
                response.put("success", true);
                response.put("message", "Chat closed successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to close chat");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{id}/close-by-user")
    public ResponseEntity<Map<String, Object>> closeChatByUser(@PathVariable int id, @RequestParam int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = chatDAO.closeChatByUser(id, userId);
            if (success) {
                response.put("success", true);
                response.put("message", "Chat closed by user");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to close chat");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable int messageId, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = chatDAO.deleteMessage(messageId, adminId);
            if (success) {
                response.put("success", true);
                response.put("message", "Message deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete message");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteChat(@PathVariable int id, @RequestParam int adminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = chatDAO.deleteChat(id, adminId);
            if (success) {
                response.put("success", true);
                response.put("message", "Chat deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete chat");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}