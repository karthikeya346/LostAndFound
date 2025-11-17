package com.lostfound.dao;

import model.Claim;
import com.lostfound.util.DBConnection;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for claims
 */
@Component
public class ClaimDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // Create a new claim (user-initiated)
    public boolean createClaim(Claim claim) {
        String insertSql = "INSERT INTO claims (item_id, user_id, status, claim_date) VALUES (?, ?, 'PENDING', NOW())";
        try (Connection conn = DBConnection.getConnection()) {

            // ✅ Prevent self-claim: user cannot claim their own reported item
            String checkSql = "SELECT COUNT(*) FROM items WHERE id=? AND user_id=?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, claim.getItemId());
                checkPs.setInt(2, claim.getUserId());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("User cannot claim their own reported item");
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, claim.getItemId());
                ps.setInt(2, claim.getUserId());
                int rows = ps.executeUpdate();

                if (rows == 1) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int claimId = rs.getInt(1);
                            auditLogDAO.logAction(
                                    claim.getUserId(),
                                    "CLAIM_CREATED",
                                    claim.getItemId(),
                                    claimId,
                                    "User created a new claim"
                            );
                            notificationDAO.saveNotification(
                                    claim.getUserId(),
                                    "CLAIM",
                                    "Your claim #" + claimId + " has been submitted and is pending review."
                            );
                        }
                    }
                }
                return rows == 1;
            }
        } catch (Exception e) {
            System.err.println("Error creating claim: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Fetch all pending claims (for admin view)
    public List<Claim> getPendingClaims() {
        List<Claim> claims = new ArrayList<>();
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_date, c.status, c.matched_item_id " +
                     "FROM claims c WHERE c.status = 'PENDING' ORDER BY c.claim_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int matchedId = rs.getInt("matched_item_id");
                if (rs.wasNull()) matchedId = -1;

                Claim claim = new Claim(
                        rs.getInt("id"),
                        rs.getInt("item_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("claim_date"),
                        rs.getString("status"),
                        matchedId
                );
                claims.add(claim);
            }
        } catch (Exception e) {
            System.err.println("Error fetching pending claims: " + e.getMessage());
            e.printStackTrace();
        }
        return claims;
    }

    // Fetch all claims submitted by a specific user
    public List<Claim> getClaimsByUser(int userId) {
        List<Claim> claims = new ArrayList<>();
        String sql = "SELECT c.id, c.item_id, c.user_id, c.claim_date, c.status, c.matched_item_id " +
                     "FROM claims c WHERE c.user_id = ? ORDER BY c.claim_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int matchedId = rs.getInt("matched_item_id");
                if (rs.wasNull()) matchedId = -1;

                Claim claim = new Claim(
                        rs.getInt("id"),
                        rs.getInt("item_id"),
                        rs.getInt("user_id"),
                        rs.getTimestamp("claim_date"),
                        rs.getString("status"),
                        matchedId
                );
                claims.add(claim);
            }
        } catch (Exception e) {
            System.err.println("Error fetching claims by user: " + e.getMessage());
            e.printStackTrace();
        }
        return claims;
    }

    // Generic status update method with notifications
    private boolean updateClaimStatus(int claimId, String status, int actorUserId,
                                      String details, String notifMessage, boolean isAdmin) {
        String sql = "UPDATE claims SET status = ? WHERE id = ? AND status!='APPROVED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, claimId);
            int rows = ps.executeUpdate();

            if (rows == 1) {
                auditLogDAO.logAction(actorUserId, "CLAIM_" + status, 0, claimId, details);
                int userId = getUserIdForClaim(claimId);
                if (userId != -1) {
                    notificationDAO.saveNotification(userId, "CLAIM", notifMessage);
                }
            }
            return rows == 1;
        } catch (Exception e) {
            System.err.println("Error updating claim status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Convenience wrappers
    public boolean cancelClaim(int claimId, int userId) {
        return updateClaimStatus(claimId, "CANCELLED", userId,
                "User cancelled claim",
                "Your claim #" + claimId + " has been cancelled.", false);
    }

    public boolean approveClaim(int claimId, int adminId) {
        boolean updated = updateClaimStatus(claimId, "APPROVED", adminId,
                "Admin approved claim",
                "Your claim #" + claimId + " has been approved.", true);

        if (updated) {
            try {
                int itemId = getItemIdForClaim(claimId);
                int claimantId = getUserIdForClaim(claimId);
                int ownerId = getOwnerIdForItem(itemId);

                // ✅ Mark item as CLAIMED
                String updateItem = "UPDATE items SET status='CLAIMED' WHERE id=?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(updateItem)) {
                    ps.setInt(1, itemId);
                    ps.executeUpdate();
                }

                // ✅ Create anonymous chat session between owner and claimant
                try {
                    ChatDAO chatDAO = new ChatDAO();
                    int chatId = chatDAO.createChatSession(itemId, claimId, adminId);
                    if (chatId > 0) {
                        chatDAO.addParticipant(chatId, ownerId, "User A", "OWNER");
                        chatDAO.addParticipant(chatId, claimantId, "User B", "CLAIMANT");
                        notificationDAO.saveNotification(ownerId, "CHAT",
                                "A private chat has been opened for claim #" + claimId + ".");
                        notificationDAO.saveNotification(claimantId, "CHAT",
                                "A private chat has been opened for your claim #" + claimId + ".");
                    }
                } catch (Exception chatEx) {
                    System.err.println("Error creating chat session: " + chatEx.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Error creating chat session for claim: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return updated;
    }

    public boolean rejectClaim(int claimId, int adminId) {
        return updateClaimStatus(claimId, "REJECTED", adminId,
                "Admin rejected claim",
                "Your claim #" + claimId + " has been rejected.", true);
    }

    public boolean markReturned(int claimId, int adminId) {
        boolean ok = updateClaimStatus(claimId, "RETURNED", adminId,
                "Admin marked claim as returned",
                "Your claimed item has been marked as returned.", true);
        if (ok) {
            // Also mark related item as RETURNED
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE items SET status='RETURNED' WHERE id=(SELECT item_id FROM claims WHERE id=?)")) {
                ps.setInt(1, claimId);
                ps.executeUpdate();
            } catch (Exception ignored) {}
        }
        return ok;
    }

    // Utility: get userId for a claim
    public int getUserIdForClaim(int claimId) {
        String sql = "SELECT user_id FROM claims WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching userId for claim: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // Mark a claim as returned/resolved
    public boolean markReturned(int claimId) {
        String sql = "UPDATE claims SET status='RETURNED' WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                // Also mark related item as RETURNED
                try (PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE items SET status='RETURNED' WHERE id=(SELECT item_id FROM claims WHERE id=?)")) {
                    ps2.setInt(1, claimId);
                    ps2.executeUpdate();
                } catch (Exception ignored) {}
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error marking claim returned: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Utility: get itemId for a claim
    public int getItemIdForClaim(int claimId) {
        String sql = "SELECT item_id FROM claims WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                    return rs.getInt("item_id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching itemId for claim: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // Utility: get ownerId for an item (the reporter of the item)
    public int getOwnerIdForItem(int itemId) {
        String sql = "SELECT user_id FROM items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching ownerId for item: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // Link a claim to a matched item
    public boolean linkClaimToItem(int claimId, int itemId, int userId) {
        String sql = "UPDATE claims SET matched_item_id = ?, status = 'LINKED' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimId);
            int rows = ps.executeUpdate();

            if (rows == 1) {
                auditLogDAO.logAction(userId, "CLAIM_LINKED", itemId, claimId,
                        "User linked claim to suggested item");
                notificationDAO.saveNotification(
                        userId,
                        "CLAIM",
                        "Your claim #" + claimId + " has been linked to item #" + itemId + " and is pending admin review."
                );
            }
            return rows == 1;
        } catch (Exception e) {
            System.err.println("Error linking claim to item: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Unlink a claim from a matched item (reset to PENDING)
    public boolean unlinkClaim(int claimId, int userId) {
        String sql = "UPDATE claims SET matched_item_id = NULL, status = 'PENDING' WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            int rows = ps.executeUpdate();

            if (rows == 1) {
                auditLogDAO.logAction(userId, "CLAIM_UNLINKED", 0, claimId,
                        "User/admin unlinked claim from matched item");
                notificationDAO.saveNotification(
                        userId,
                        "CLAIM",
                        "Your claim #" + claimId + " has been unlinked and set back to pending."
                );
            }
            return rows == 1;
        } catch (Exception e) {
            System.err.println("Error unlinking claim: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Fetch chatId for a claim (needed by UserDashboard Chat button)
    public int getChatIdForClaim(int claimId) {
        String sql = "SELECT id FROM chats WHERE claim_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching chatId for claim: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}