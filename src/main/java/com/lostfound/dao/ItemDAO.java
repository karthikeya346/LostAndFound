package com.lostfound.dao;

import model.Item;
import model.Claim;
import com.lostfound.util.DBConnection;

import java.sql.*;
import java.util.*;

public class ItemDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Insert new item (user reporting)
    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (user_id, title, description, location, date_reported, type, status, image_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getLocation());
            ps.setDate(5, item.getDateReported());
            ps.setString(6, item.getType());   // LOST/FOUND
            ps.setString(7, "UNDER");          // default moderation
            ps.setString(8, item.getImagePath());

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int itemId = rs.getInt(1);
                        // Audit log
                        auditLogDAO.logAction(item.getUserId(), "ITEM_REPORTED", itemId, 0,
                                "User reported new item: " + item.getTitle());
                        // Notification
                        notificationDAO.saveNotification(
                                item.getUserId(),
                                "ITEM",
                                "Your item '" + item.getTitle() + "' has been reported and is pending review."
                        );
                    }
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error adding item: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // Fetch all items for admin (with usernames)
    public List<Item> getAllItemsForAdmin() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.id, i.user_id, i.title, i.description, i.location, i.date_reported, " +
                     "i.type, i.status, i.image_path, u.username " +
                     "FROM items i JOIN users u ON i.user_id = u.id " +
                     "ORDER BY i.date_reported DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDate("date_reported"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getString("username"), // reportedBy
                        rs.getString("image_path")
                );
                item.setUserId(rs.getInt("user_id"));
                items.add(item);
            }
        } catch (Exception e) {
            System.err.println("Error fetching admin items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    // Fetch all items for user (no usernames needed)
    // ✅ Filter out CLAIMED items so users don’t see them
    public List<Item> getAllItemsForUserView() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT id, user_id, title, description, location, date_reported, type, status, image_path " +
                     "FROM items WHERE status != 'CLAIMED' ORDER BY date_reported DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Item item = new Item(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("location"),
                        rs.getDate("date_reported"),
                        rs.getString("type"),
                        rs.getString("status"),
                        null, // reportedBy not needed for user view
                        rs.getString("image_path")
                );
                item.setUserId(rs.getInt("user_id"));
                items.add(item);
            }
        } catch (Exception e) {
            System.err.println("Error fetching user items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    // Helper: fetch item by ID
    public Item getItemById(int itemId) {
        String sql = "SELECT id, user_id, title, description, location, date_reported, type, status, image_path " +
                     "FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Item item = new Item(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("location"),
                            rs.getDate("date_reported"),
                            rs.getString("type"),
                            rs.getString("status"),
                            null,
                            rs.getString("image_path")
                    );
                    item.setUserId(rs.getInt("user_id"));
                    return item;
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching item by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Smart matching: find potential matches for a claim (search only APPROVED items)
    public List<Item> findPotentialMatches(Claim claim) {
        List<Item> matches = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();

        Item claimedItem = getItemById(claim.getItemId());
        if (claimedItem == null) {
            return matches;
        }

        String combined = (claimedItem.getTitle() + " " + claimedItem.getDescription()).toLowerCase();
        String[] keywords = combined.split("\\s+");

        String sql = "SELECT id, user_id, title, description, location, date_reported, type, status, image_path " +
                     "FROM items WHERE status = 'APPROVED' AND id <> ? " +
                     "AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(location) LIKE ?) " +
                     "ORDER BY date_reported DESC";

        try (Connection conn = DBConnection.getConnection()) {
            for (String keyword : keywords) {
                if (keyword.length() < 3) continue;

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, claimedItem.getId());
                    String like = "%" + keyword + "%";
                    ps.setString(2, like);
                    ps.setString(3, like);
                    ps.setString(4, like);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            if (!seen.contains(id)) {
                                Item item = new Item(
                                        rs.getInt("id"),
                                        rs.getString("title"),
                                        rs.getString("description"),
                                        rs.getString("location"),
                                        rs.getDate("date_reported"),
                                        rs.getString("type"),
                                        rs.getString("status"),
                                        null,
                                        rs.getString("image_path")
                                );
                                item.setUserId(rs.getInt("user_id"));
                                matches.add(item);
                                seen.add(id);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding potential matches: " + e.getMessage());
            e.printStackTrace();
        }

        return matches;
    }

    // Update item status (generic moderation update)
    // ✅ Prevent duplicate approvals
    public boolean updateStatus(int itemId, String status) {
        if (!Arrays.asList("UNDER", "APPROVED", "REJECTED", "CLAIMED").contains(status)) {
            System.err.println("Invalid status value: " + status);
            return false;
        }

        String sql = "UPDATE items SET status=? WHERE id=? " +
                     (status.equals("APPROVED") ? "AND status!='APPROVED'" : "");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, itemId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                auditLogDAO.logAction(0, "ITEM_STATUS_UPDATED", itemId, 0,
                        "Status changed to " + status);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error updating item status: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

        // Explicit admin actions
    public boolean approveItem(int itemId, int adminId) {
        boolean ok = updateStatus(itemId, "APPROVED");
        if (ok) {
            try {
                auditLogDAO.logAction(adminId, "ITEM_APPROVED", itemId, 0,
                        "Admin approved reported item");
            } catch (Exception logEx) {
                System.err.println("[AuditLogDAO] Failed to log ITEM_APPROVED: " + logEx.getMessage());
            }
            Item item = getItemById(itemId);
            if (item != null) {
                try {
                    notificationDAO.saveNotification(
                            item.getUserId(),
                            "ITEM",
                            "Your item '" + item.getTitle() + "' has been approved."
                    );
                } catch (Exception notifEx) {
                    System.err.println("[NotificationDAO] Failed to save notification: " + notifEx.getMessage());
                }
            }
        }
        return ok;
    }

    public boolean rejectItem(int itemId, int adminId) {
        boolean ok = updateStatus(itemId, "REJECTED");
        if (ok) {
            try {
                auditLogDAO.logAction(adminId, "ITEM_REJECTED", itemId, 0,
                        "Admin rejected reported item");
            } catch (Exception logEx) {
                System.err.println("[AuditLogDAO] Failed to log ITEM_REJECTED: " + logEx.getMessage());
            }
            Item item = getItemById(itemId);
            if (item != null) {
                try {
                    notificationDAO.saveNotification(
                            item.getUserId(),
                            "ITEM",
                            "Your item '" + item.getTitle() + "' has been rejected."
                    );
                } catch (Exception notifEx) {
                    System.err.println("[NotificationDAO] Failed to save notification: " + notifEx.getMessage());
                }
            }
        }
        return ok;
    }

    // Delete item (by user or admin)
    public boolean deleteItem(int itemId, int actorId) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try {
                    auditLogDAO.logAction(actorId, "ITEM_DELETED", itemId, 0,
                            "Item deleted by actor ID: " + actorId);
                } catch (Exception logEx) {
                    System.err.println("[AuditLogDAO] Failed to log ITEM_DELETED: " + logEx.getMessage());
                }
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error deleting item: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}