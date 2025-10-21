package com.lostfound.controller;

import com.lostfound.dao.ChatDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.UserDAO;
import model.ChatSession;
import model.ChatParticipant;
import model.ChatMessage;
import model.Notification;

import java.util.List;

public class ChatController {

    private final ChatDAO chatDAO = new ChatDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();

    // ---------------- Chat Session ----------------

    /** Create a new chat session (usually when a claim is approved). */
    public int createChatSession(int itemId, int claimId, int startedBy) {
        return chatDAO.createChatSession(itemId, claimId, startedBy);
    }

    /** Close a chat session (admin or system action). */
    public boolean closeChat(int chatId, int adminId) {
        return chatDAO.closeChat(chatId, adminId);
    }

    /** Fetch all chat sessions (admin monitoring). */
    public List<ChatSession> getAllChatSessions() {
        return chatDAO.getAllChatSessions();
    }

    /** Search chat sessions by status/item/claim. */
    public List<ChatSession> searchSessions(String status, Integer itemId, Integer claimId) {
        return chatDAO.searchSessions(status, itemId, claimId);
    }

    // ---------------- Participants ----------------

    /** Add a participant to a chat with alias + role. */
    public boolean addParticipant(int chatId, int userId, String alias, String role) {
        return chatDAO.addParticipant(chatId, userId, alias, role);
    }

    /** Get participants for a chat (for alias resolution). */
    public List<ChatParticipant> getParticipants(int chatId) {
        return chatDAO.getParticipants(chatId);
    }

    /** Remove a participant from a chat (admin action). */
    public boolean removeParticipant(int chatId, int userId, int adminId) {
        return chatDAO.removeParticipant(chatId, userId, adminId);
    }

    // ---------------- Messages ----------------

    /** Send a new message in a chat session. */
    public boolean sendMessage(int chatId, int senderId, String message) {
        return chatDAO.saveMessage(chatId, senderId, message);
    }

    /** Fetch all messages in a chat session. */
    public List<ChatMessage> getMessages(int chatId) {
        return chatDAO.getMessages(chatId);
    }

    /** Delete a message (admin moderation). */
    public boolean deleteMessage(int messageId, int adminId) {
        return chatDAO.deleteMessage(messageId, adminId);
    }

    /** Fetch all messages across all chats (admin monitoring). */
    public List<ChatMessage> getAllMessages() {
        return chatDAO.getAllMessages();
    }

    /** Search messages in a chat by keyword. */
    public List<ChatMessage> searchMessages(int chatId, String keyword) {
        return chatDAO.searchMessages(chatId, keyword);
    }

    // ---------------- Notifications ----------------

    /** Fetch notifications for a specific user. */
    public List<Notification> getNotifications(int userId) {
        return notificationDAO.getNotificationsByUser(userId);
    }

    /** Fetch all notifications (admin view). */
    public List<Notification> getAllNotifications() {
        return notificationDAO.getAllNotifications();
    }

    /** Mark a notification as read. */
    public boolean markNotificationAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    // ---------------- Role Checks ----------------

    /** Check if a user is an admin. */
    public boolean isAdmin(int userId) {
        return userDAO.isAdmin(userId);
    }
}