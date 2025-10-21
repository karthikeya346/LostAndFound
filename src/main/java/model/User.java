package model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private String email;

    public User(int id, String username, String passwordHash, String role, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.email = email;
    }

    public User(String username, String passwordHash, String role, String email) {
        this(0, username, passwordHash, role, email);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}