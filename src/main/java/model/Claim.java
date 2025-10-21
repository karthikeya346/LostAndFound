package model;

import java.sql.Timestamp;

public class Claim {
    private int id;
    private int itemId;
    private int userId;
    private Timestamp claimDate;
    private String status;

    // 🔑 Workflow fields
    private int matchedItemId;   // links to another item
    private int approvedBy;      // admin who approved/rejected

    // Extra fields for display
    private String itemTitle;
    private String username;

    // --- Constructors ---

    // Minimal constructor for creating a new claim
    public Claim(int itemId, int userId) {
        this.itemId = itemId;
        this.userId = userId;
        this.status = "PENDING";
        this.claimDate = new Timestamp(System.currentTimeMillis());
    }

    // Constructor for DB reads (basic)
    public Claim(int id, int itemId, int userId, Timestamp claimDate, String status) {
        this.id = id;
        this.itemId = itemId;
        this.userId = userId;
        this.claimDate = claimDate;
        this.status = status;
    }

    // Constructor for DB reads including matched item
    public Claim(int id, int itemId, int userId, Timestamp claimDate, String status, int matchedItemId) {
        this(id, itemId, userId, claimDate, status);
        this.matchedItemId = matchedItemId;
    }

    // Constructor for DB reads including matched item + approvedBy
    public Claim(int id, int itemId, int userId, Timestamp claimDate, String status, int matchedItemId, int approvedBy) {
        this(id, itemId, userId, claimDate, status, matchedItemId);
        this.approvedBy = approvedBy;
    }

    // Constructor for JOIN results (admin/user views)
    public Claim(int id, int itemId, int userId, Timestamp claimDate, String status, String itemTitle, String username) {
        this(id, itemId, userId, claimDate, status);
        this.itemTitle = itemTitle;
        this.username = username;
    }

    // --- Getters & Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Timestamp getClaimDate() { return claimDate; }
    public void setClaimDate(Timestamp claimDate) { this.claimDate = claimDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getMatchedItemId() { return matchedItemId; }
    public void setMatchedItemId(int matchedItemId) { this.matchedItemId = matchedItemId; }

    public int getApprovedBy() { return approvedBy; }
    public void setApprovedBy(int approvedBy) { this.approvedBy = approvedBy; }

    public String getItemTitle() { return itemTitle; }
    public void setItemTitle(String itemTitle) { this.itemTitle = itemTitle; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    @Override
    public String toString() {
        return "Claim{" +
                "id=" + id +
                ", itemId=" + itemId +
                ", userId=" + userId +
                ", claimDate=" + claimDate +
                ", status='" + status + '\'' +
                ", matchedItemId=" + matchedItemId +
                ", approvedBy=" + approvedBy +
                ", itemTitle='" + itemTitle + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}