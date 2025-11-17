package model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String status;

    public User(int id, String username, String password, String role, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.status = "ACTIVE";
    }

    public User(String username, String password, String role, String email) {
        this(0, username, password, role, email);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }

    public User(int id, String username, String password, String role, String email, String status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.status = status;
    }
}