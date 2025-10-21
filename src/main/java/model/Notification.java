package model;

import java.sql.Timestamp;

public class Notification {
    private int id;
    private int userId;
    private String type;       // <-- NEW field for category (CLAIM_APPROVED, ITEM_ADDED, etc.)
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {}

    public Notification(int userId, String type, String message) {
        this.userId = userId;
        this.type = type;
        this.message = message;
        this.isRead = false;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}