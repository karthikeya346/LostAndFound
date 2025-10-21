package model;

import java.sql.Timestamp;

/**
 * Represents a chat session tied to an item/claim.
 */
public class ChatSession {
    private int id;
    private int itemId;
    private int claimId;
    private int startedBy;
    private String status; // OPEN / CLOSED
    private Timestamp startedAt;
    private Timestamp closedAt;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getStartedBy() { return startedBy; }
    public void setStartedBy(int startedBy) { this.startedBy = startedBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getStartedAt() { return startedAt; }
    public void setStartedAt(Timestamp startedAt) { this.startedAt = startedAt; }

    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }
}