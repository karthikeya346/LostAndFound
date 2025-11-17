package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import model.ChatSession;
import model.ChatParticipant;
import model.ChatMessage;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for handling chat sessions, participants, and messages.
 */
@Component
public class ChatDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO(); // for role checks
    private final ClaimDAO claimDAO = new ClaimDAO();

    // ---------------- Chat Session ----------------

    public int createChatSession(int itemId, int claimId, int startedBy) {
        String sql = "INSERT INTO chats (item_id, claim_id, started_by) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Enforce: only one open chat per item at a time
            try (PreparedStatement chk = conn.prepareStatement("SELECT id FROM chats WHERE item_id=? AND UPPER(status) <> 'CLOSED' LIMIT 1")) {
                chk.setInt(1, itemId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        System.err.println("Refusing to create chat: open chat already exists for item " + itemId);
                        return -1;
                    }
                }
            }
            ps.setInt(1, itemId);
            ps.setInt(2, claimId);
            ps.setInt(3, startedBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int chatId = rs.getInt(1);
                    auditLogDAO.logAction(startedBy, "CHAT_CREATED", 0, chatId, "Chat session created");
                    try {
                        int ownerId = claimDAO.getOwnerIdForItem(itemId);
                        int claimantId = claimDAO.getUserIdForClaim(claimId);
                        if (ownerId > 0) addParticipant(chatId, ownerId, "User A", "OWNER");
                        if (claimantId > 0) addParticipant(chatId, claimantId, "User B", "CLAIMANT");
                        if (ownerId > 0) notificationDAO.saveNotification(ownerId, "CHAT", "A private chat has been opened for claim #" + claimId + ".");
                        if (claimantId > 0) notificationDAO.saveNotification(claimantId, "CHAT", "A private chat has been opened for your claim #" + claimId + ".");
                    } catch (Exception ignored) {}
                    return chatId;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    public boolean closeChat(int chatId, int adminId) {
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Unauthorized attempt to close chat by user " + adminId);
            return false;
        }
        String sql = "UPDATE chats SET status='CLOSED', closed_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(adminId, "CHAT_CLOSED", 0, chatId, "Chat session closed");
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ---------------- Participants ----------------

    public boolean addParticipant(int chatId, int userId, String alias, String role) {
        String sql = "INSERT INTO chat_participants (chat_id, user_id, alias, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setInt(2, userId);
            ps.setString(3, alias);
            ps.setString(4, role);
            return ps.executeUpdate() == 1;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<ChatParticipant> getParticipants(int chatId) {
        List<ChatParticipant> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_participants WHERE chat_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatParticipant p = new ChatParticipant();
                    p.setId(rs.getInt("id"));
                    p.setChatId(rs.getInt("chat_id"));
                    p.setUserId(rs.getInt("user_id"));
                    p.setAlias(rs.getString("alias"));
                    p.setRole(rs.getString("role"));
                    list.add(p);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------------- Helpers & User Actions ----------------

    /** Check if a user is a participant of a chat */
    public boolean isParticipant(int chatId, int userId) {
        String sql = "SELECT 1 FROM chat_participants WHERE chat_id=? AND user_id=? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Allow a participant to close a chat */
    public boolean closeChatByUser(int chatId, int userId) {
        if (!isParticipant(chatId, userId) && !userDAO.isAdmin(userId)) {
            System.err.println("User " + userId + " is not participant/admin, cannot close chat " + chatId);
            return false;
        }
        String sql = "UPDATE chats SET status='CLOSED', closed_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(userId, "CHAT_CLOSED", 0, chatId, "Chat closed by user");
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    /** Remove a participant from a chat (admin action). */
    public boolean removeParticipant(int chatId, int userId, int adminId) {
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Unauthorized attempt to remove participant by user " + adminId);
            return false;
        }
        String sql = "DELETE FROM chat_participants WHERE chat_id=? AND user_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(adminId, "REMOVE_CHAT_PARTICIPANT", userId, chatId,
                        "Admin removed participant from chat");
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ---------------- Messages ----------------

    public boolean saveMessage(int chatId, int senderId, String message) {
        String sql = "INSERT INTO chat_messages (chat_id, sender_user_id, message) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // Prevent messaging in CLOSED chats
            try (PreparedStatement chk = conn.prepareStatement("SELECT status FROM chats WHERE id=?")) {
                chk.setInt(1, chatId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (status != null && status.equalsIgnoreCase("CLOSED")) {
                            System.err.println("Chat " + chatId + " is CLOSED; refusing to send message");
                            return false;
                        }
                    }
                }
            }
            ps.setInt(1, chatId);
            ps.setInt(2, senderId);
            ps.setString(3, message);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(senderId, "CHAT_MESSAGE", 0, chatId, "Message sent");

                // Notify all other participants
                List<ChatParticipant> participants = getParticipants(chatId);
                for (ChatParticipant p : participants) {
                    if (p.getUserId() != senderId) {
                        notificationDAO.saveNotification(
                                p.getUserId(),
                                "CHAT_MESSAGE",
                                "New message in chat " + chatId
                        );
                    }
                }
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public List<ChatMessage> getMessages(int chatId) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE chat_id=? ORDER BY sent_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage();
                    msg.setId(rs.getInt("id"));
                    msg.setChatId(rs.getInt("chat_id"));
                    msg.setSenderUserId(rs.getInt("sender_user_id"));
                    msg.setMessage(rs.getString("message"));
                    msg.setCreatedAt(rs.getTimestamp("sent_at"));
                    list.add(msg);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public boolean deleteMessage(int messageId, int adminId) {
        if (!userDAO.isAdmin(adminId)) {
            System.err.println("Unauthorized attempt to delete message by user " + adminId);
            return false;
        }
        String sql = "DELETE FROM chat_messages WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(adminId, "DELETE_CHAT_MESSAGE", 0, messageId,
                        "Admin deleted chat message");
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // ---------------- Admin Utilities ----------------

    public List<ChatSession> getAllChatSessions() {
        List<ChatSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM chats ORDER BY started_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChatSession cs = new ChatSession();
                cs.setId(rs.getInt("id"));
                cs.setItemId(rs.getInt("item_id"));
                cs.setClaimId(rs.getInt("claim_id"));
                cs.setStartedBy(rs.getInt("started_by"));
                cs.setStatus(rs.getString("status"));
                cs.setStartedAt(rs.getTimestamp("started_at"));
                cs.setClosedAt(rs.getTimestamp("closed_at"));
                sessions.add(cs);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sessions;
    }

    public List<ChatMessage> getAllMessages() {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages ORDER BY sent_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setId(rs.getInt("id"));
                msg.setChatId(rs.getInt("chat_id"));
                msg.setSenderUserId(rs.getInt("sender_user_id"));
                msg.setMessage(rs.getString("message"));
                msg.setCreatedAt(rs.getTimestamp("sent_at"));
                list.add(msg);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ---------------- Search & Filter ----------------

    /** Get chat sessions visible to a user: strictly participant-based. */
    public List<ChatSession> getChatsForUser(int userId) {
        List<ChatSession> sessions = new ArrayList<>();
        String sql = "SELECT c.* FROM chats c JOIN chat_participants p ON p.chat_id = c.id WHERE p.user_id = ? ORDER BY c.started_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatSession cs = new ChatSession();
                    cs.setId(rs.getInt("id"));
                    cs.setItemId(rs.getInt("item_id"));
                    cs.setClaimId(rs.getInt("claim_id"));
                    cs.setStartedBy(rs.getInt("started_by"));
                    cs.setStatus(rs.getString("status"));
                    cs.setStartedAt(rs.getTimestamp("started_at"));
                    cs.setClosedAt(rs.getTimestamp("closed_at"));
                    sessions.add(cs);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sessions;
    }

    /** Search sessions by optional filters (status, itemId, claimId). */
    public List<ChatSession> searchSessions(String status, Integer itemId, Integer claimId) {
        List<ChatSession> sessions = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM chats WHERE 1=1");
        if (status != null) sql.append(" AND status=?");
        if (itemId != null) sql.append(" AND item_id=?");
        if (claimId != null) sql.append(" AND claim_id=?");
        sql.append(" ORDER BY started_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (status != null) ps.setString(idx++, status);
            if (itemId != null) ps.setInt(idx++, itemId);
            if (claimId != null) ps.setInt(idx++, claimId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatSession cs = new ChatSession();
                    cs.setId(rs.getInt("id"));
                    cs.setItemId(rs.getInt("item_id"));
                    cs.setClaimId(rs.getInt("claim_id"));
                    cs.setStartedBy(rs.getInt("started_by"));
                    cs.setStatus(rs.getString("status"));
                    cs.setStartedAt(rs.getTimestamp("started_at"));
                    cs.setClosedAt(rs.getTimestamp("closed_at"));
                    sessions.add(cs);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return sessions;
    }

    /** Search messages in a chat by keyword. */
    public List<ChatMessage> searchMessages(int chatId, String keyword) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_messages WHERE chat_id=? AND message LIKE ? ORDER BY sent_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chatId);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage();
                    msg.setId(rs.getInt("id"));
                    msg.setChatId(rs.getInt("chat_id"));
                    msg.setSenderUserId(rs.getInt("sender_user_id"));
                    msg.setMessage(rs.getString("message"));
                    msg.setCreatedAt(rs.getTimestamp("sent_at"));
                    list.add(msg);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    /** Delete an entire chat (admin only). Removes messages and participants, then chat row. */
public boolean deleteChat(int chatId, int adminId) {
    if (!userDAO.isAdmin(adminId)) {
        System.err.println("Unauthorized attempt to delete chat by user " + adminId);
        return false;
    }
    String delMsgs = "DELETE FROM chat_messages WHERE chat_id=?";
    String delParts = "DELETE FROM chat_participants WHERE chat_id=?";
    String delChat = "DELETE FROM chats WHERE id=?";
    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false);
        try (PreparedStatement ps1 = conn.prepareStatement(delMsgs);
             PreparedStatement ps2 = conn.prepareStatement(delParts);
             PreparedStatement ps3 = conn.prepareStatement(delChat)) {

            ps1.setInt(1, chatId);
            ps1.executeUpdate();

            ps2.setInt(1, chatId);
            ps2.executeUpdate();

            ps3.setInt(1, chatId);
            int rows = ps3.executeUpdate();

            conn.commit();
            if (rows == 1) {
                auditLogDAO.logAction(adminId, "DELETE_CHAT", 0, chatId, "Admin deleted chat session");
                return true;
            }
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return false;
}
}