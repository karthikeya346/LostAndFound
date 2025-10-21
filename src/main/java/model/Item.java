package model;

import java.sql.Date;

public class Item {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String location;
    private Date dateReported;
    private String type;       // LOST or FOUND (user input)
    private String status;     // UNDER/APPROVED/REJECTED (moderation workflow)
    private String reportedBy; // optional for admin view
    private String imagePath;  // optional image path

    // Constructor for user reporting
    public Item(int userId, String title, String description, String location,
                Date dateReported, String type, String imagePath) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateReported = dateReported;
        this.type = type;          // LOST/FOUND
        this.status = "UNDER";     // default moderation state
        this.imagePath = imagePath;
    }

    // Constructor for admin listing
    public Item(int id, String title, String description, String location,
                Date dateReported, String type, String status, String reportedBy, String imagePath) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.dateReported = dateReported;
        this.type = type;
        this.status = status;
        this.reportedBy = reportedBy;
        this.imagePath = imagePath;
    }

    // No-arg constructor
    public Item() {}

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public Date getDateReported() { return dateReported; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getReportedBy() { return reportedBy; }
    public String getImagePath() { return imagePath; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setDateReported(Date dateReported) { this.dateReported = dateReported; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", dateReported=" + dateReported +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", reportedBy='" + reportedBy + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}