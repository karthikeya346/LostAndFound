package model;

import java.sql.Timestamp;

/**
 * Represents a single audit log entry.
 * Stores who performed the action, what action, and when.
 */
public class AuditLog {
    private int id;              // Primary key
    private int userId;          // Actor (admin or user)
    private String action;       // e.g. CLAIM_CREATED, CLAIM_APPROVED
    private int itemId;          // Related item
    private int claimId;         // Related claim
    private Timestamp createdAt; // When it happened
    private String details;      // Extra info

    // Constructor for creating new log entries (before DB assigns id/createdAt)
    public AuditLog(int userId, String action, int itemId, int claimId, String details) {
        this.userId = userId;
        this.action = action;
        this.itemId = itemId;
        this.claimId = claimId;
        this.details = details;
    }

    // Empty constructor (useful for DAO population)
    public AuditLog() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}