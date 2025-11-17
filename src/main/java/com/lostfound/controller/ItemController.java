package com.lostfound.controller;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.NotificationDAO;
import model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class ItemController {

    @Autowired
    private ItemDAO itemDAO;
    @Autowired
    private NotificationDAO notificationDAO;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Item item = new Item(
                (Integer) request.get("userId"),
                (String) request.get("title"),
                (String) request.get("description"),
                (String) request.get("location"),
                new java.sql.Date(System.currentTimeMillis()),
                (String) request.get("type"),
                (String) request.get("imagePath")
            );

            // Enforce: photo is required
            String imagePath = (String) request.get("imagePath");
            if (imagePath == null || imagePath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Photo is required to report an item. Please attach an image.");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = itemDAO.addItem(item);
            if (success) {
                response.put("success", true);
                response.put("message", "Item added successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to add item");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addItemMultipart(
            @RequestPart("userId") String userIdStr,
            @RequestPart("title") String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "location", required = false) String location,
            @RequestPart("type") String type,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Title is required");
                return ResponseEntity.badRequest().body(response);
            }
            int userId;
            try { userId = Integer.parseInt(userIdStr); }
            catch (Exception ex) {
                response.put("success", false);
                response.put("message", "Invalid userId");
                return ResponseEntity.badRequest().body(response);
            }
            // Enforce: image file is required
            if (image == null || image.isEmpty()) {
                response.put("success", false);
                response.put("message", "Photo is required to report an item. Please attach an image.");
                return ResponseEntity.badRequest().body(response);
            }

            String storedPath = null;
            if (image != null && !image.isEmpty()) {
                String baseDir = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "items";
                java.nio.file.Path dir = java.nio.file.Paths.get(baseDir);
                java.nio.file.Files.createDirectories(dir);

                String original = image.getOriginalFilename();
                String ext = "";
                if (original != null && original.contains(".")) {
                    ext = original.substring(original.lastIndexOf('.'));
                } else {
                    String ct = image.getContentType();
                    if (ct != null && ct.contains("jpeg")) ext = ".jpg";
                    else if (ct != null && ct.contains("png")) ext = ".png";
                    else if (ct != null && ct.contains("webp")) ext = ".webp";
                    else ext = ".bin";
                }
                String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;
                java.nio.file.Path target = dir.resolve(filename);
                image.transferTo(target.toFile());
                storedPath = "/uploads/items/" + filename;
            }

            Item item = new Item(
                userId,
                title,
                description,
                location,
                new java.sql.Date(System.currentTimeMillis()),
                type,
                storedPath
            );

            boolean success = itemDAO.addItem(item);
            if (success) {
                response.put("success", true);
                response.put("message", "Item added successfully");
                response.put("imagePath", storedPath);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to add item");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getAllItemsForAdmin() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Item> items = itemDAO.getAllItemsForAdmin();
            response.put("success", true);
            response.put("items", items);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getAllItemsForUser() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Item> items = itemDAO.getAllItemsForUserView();
            response.put("success", true);
            response.put("items", items);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getItemById(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Item item = itemDAO.getItemById(id);
            if (item != null) {
                response.put("success", true);
                response.put("item", item);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Item not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getItemsByUser(@PathVariable int userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Item> items = itemDAO.getItemsByUser(userId);
            response.put("success", true);
            response.put("items", items);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateItemStatus(@PathVariable int id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = request.get("status");
            boolean success = itemDAO.updateStatus(id, status);
            if (success) {
                response.put("success", true);
                response.put("message", "Item status updated");
                try {
                    Item item = itemDAO.getItemById(id);
                    if (item != null) {
                        String type = "ITEM_" + ("APPROVED".equalsIgnoreCase(status) ? "APPROVED" : "REJECTED");
                        String msg = "Your item '" + item.getTitle() + "' was " + status.toLowerCase();
                        notificationDAO.saveNotification(item.getUserId(), type, msg);
                    }
                } catch (Exception ignored) {}
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to update status");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteItem(@PathVariable int id, @RequestParam int actorId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = itemDAO.deleteItem(id, actorId);
            if (success) {
                response.put("success", true);
                response.put("message", "Item deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete item");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}