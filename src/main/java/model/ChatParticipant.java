package model;

/**
 * Represents a participant in a chat session with an alias.
 */
public class ChatParticipant {
    private int id;
    private int chatId;
    private int userId;
    private String alias; // "Owner" / "Claimant"
    private String role;  // OWNER / CLAIMANT

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChatId() { return chatId; }
    public void setChatId(int chatId) { this.chatId = chatId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}