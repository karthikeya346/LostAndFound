package model;

import java.sql.Timestamp;

/**
 * Represents a single message inside a chat session.
 */
public class ChatMessage {
    private int id;
    private int chatId;
    private int senderUserId;
    private String message;
    private Timestamp createdAt;   // ✅ standardized name

    // Constructors
    public ChatMessage() {}

    public ChatMessage(int id, int chatId, int senderUserId, String message, Timestamp createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.senderUserId = senderUserId;
        this.message = message;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public int getSenderUserId() { return senderUserId; }
    public void setSenderUserId(int senderUserId) { this.senderUserId = senderUserId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getCreatedAt() { return createdAt; }   // ✅ matches AdminChatViewerFrame
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}