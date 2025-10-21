package com.lostfound.dao;

import com.lostfound.util.DBConnection;
import model.ChatSession;
import model.ChatParticipant;
import model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for handling chat sessions, participants, and messages.
 */
public class ChatDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO(); // for role checks

    // ---------------- Chat Session ----------------

    public int createChatSession(int itemId, int claimId, int startedBy) {
        String sql = "INSERT INTO chats (item_id, claim_id, started_by) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimId);
            ps.setInt(3, startedBy);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int chatId = rs.getInt(1);
                    auditLogDAO.logAction(startedBy, "CHAT_CREATED", 0, chatId, "Chat session created");
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
            ps.setInt(1, chatId);
            ps.setInt(2, senderId);
            ps.setString(3, message);
            int rows = ps.executeUpdate();
            if (rows == 1) {
                auditLogDAO.logAction(senderId, "CHAT_MESSAGE", 0, chatId, "Message sent");

                // 🔔 Notify all other participants
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
}