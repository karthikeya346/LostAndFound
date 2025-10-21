package com.lostfound.ui;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.UserDAO;
import model.Item;
import model.Claim;
import model.Notification;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class UserDashboard extends JFrame {
    private final int userId;

    public UserDashboard(int userId) {
        this.userId = userId;
        setTitle("User Dashboard");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ---------------- Top Bar with Logout ----------------
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, User #" + userId);
        JButton logoutBtn = new JButton("Logout");
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    new UserDAO().logout(userId);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Logout log failed: " + ex.getMessage(),
                            "Warning", JOptionPane.WARNING_MESSAGE
                    );
                }
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        // ---------------- Tabs ----------------
        JTabbedPane tabs = new JTabbedPane();

        // ---------------- Report Item ----------------
        JPanel reportPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        reportPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField dateField = new JTextField("YYYY-MM-DD");
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"LOST", "FOUND"});
        JButton uploadBtn = new JButton("Upload Image");
        JLabel imageLabel = new JLabel("No file chosen");
        JButton submitBtn = new JButton("Report Item");

        final String[] imagePath = {null};

        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int option = chooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                imagePath[0] = chooser.getSelectedFile().getAbsolutePath();
                imageLabel.setText(chooser.getSelectedFile().getName());
            }
        });

        reportPanel.add(new JLabel("Title:")); reportPanel.add(titleField);
        reportPanel.add(new JLabel("Description:")); reportPanel.add(descField);
        reportPanel.add(new JLabel("Location:")); reportPanel.add(locationField);
        reportPanel.add(new JLabel("Date Reported:")); reportPanel.add(dateField);
        reportPanel.add(new JLabel("Type:")); reportPanel.add(typeBox);
        reportPanel.add(uploadBtn); reportPanel.add(imageLabel);
        reportPanel.add(new JLabel("")); reportPanel.add(submitBtn);

        submitBtn.addActionListener(e -> {
            try {
                String title = titleField.getText().trim();
                String desc = descField.getText().trim();
                String loc = locationField.getText().trim();
                String dateStr = dateField.getText().trim();

                if (title.isEmpty() || desc.isEmpty() || loc.isEmpty() || dateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Date date = Date.valueOf(dateStr);
                Item item = new Item(
                        userId,
                        title,
                        desc,
                        loc,
                        date,
                        (String) typeBox.getSelectedItem(),
                        imagePath[0]
                );
                boolean ok = new ItemDAO().addItem(item);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Item reported successfully!");
                    titleField.setText("");
                    descField.setText("");
                    locationField.setText("");
                    dateField.setText("YYYY-MM-DD");
                    typeBox.setSelectedIndex(0);
                    imagePath[0] = null;
                    imageLabel.setText("No file chosen");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to report item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Please enter the date in YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabs.addTab("Report Item", reportPanel);

        // ---------------- Claim Items ----------------
        JPanel claimPanel = new JPanel(new BorderLayout());
        String[] claimCols = {"ID", "Title", "Description", "Location", "Date", "Type", "Status", "Photo", "OwnerId"};
        DefaultTableModel claimModel = new DefaultTableModel(claimCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable claimTable = new JTable(claimModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 7) return ImageIcon.class; // Photo column
                return Object.class;
            }
        };
        claimTable.setRowHeight(50);
        claimPanel.add(new JScrollPane(claimTable), BorderLayout.CENTER);

        JButton claimBtn = new JButton("Claim Selected Item");
        JPanel btnPanel = new JPanel();
        btnPanel.add(claimBtn);
        claimPanel.add(btnPanel, BorderLayout.SOUTH);

        // Load items into table
        List<Item> claimItems;
        try {
            claimItems = new ItemDAO().getAllItemsForUserView();
            for (Item item : claimItems) {
                ImageIcon thumb = null;
                String imgPath = item.getImagePath();
                if (imgPath != null && !imgPath.isEmpty()) {
                    try {
                        Image scaled = new ImageIcon(imgPath).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                        thumb = new ImageIcon(scaled);
                    } catch (Exception ignore) { thumb = null; }
                }
                claimModel.addRow(new Object[]{
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getDateReported(),
                        item.getType(),
                        item.getStatus(),
                        thumb,
                        item.getUserId()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            claimItems = new ArrayList<>();
        }

        // Preview image on click
        final List<Item> finalClaimItems = claimItems;
        claimTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = claimTable.rowAtPoint(e.getPoint());
                int col = claimTable.columnAtPoint(e.getPoint());
                if (col == 7 && row >= 0 && row < finalClaimItems.size()) {
                    String path = finalClaimItems.get(row).getImagePath();
                    if (path != null && !path.isEmpty()) {
                        try {
                            ImageIcon full = new ImageIcon(path);
                            JLabel imgLabel = new JLabel(new ImageIcon(
                                    full.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH)
                            ));
                            JOptionPane.showMessageDialog(null, imgLabel, "Image Preview", JOptionPane.PLAIN_MESSAGE);
                        } catch (Exception ignore) {
                            JOptionPane.showMessageDialog(null, "Unable to preview image.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        // Claim button action
        claimBtn.addActionListener(e -> {
            int row = claimTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an item first.");
                return;
            }
            try {
                Object idObj = claimModel.getValueAt(row, 0);
                int itemId = (idObj instanceof Number)
                        ? ((Number) idObj).intValue()
                        : Integer.parseInt(idObj.toString());

                Claim claim = new Claim(itemId, userId);
                boolean ok = new ClaimDAO().createClaim(claim);

                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Claim submitted! Check 'My Claims' tab.");
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to submit claim.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
                        } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error creating claim: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        tabs.addTab("Claim Items", claimPanel);

        // ---------------- My Claims ----------------
        JPanel myClaimsPanel = new JPanel(new BorderLayout());
        String[] myCols = {"Claim ID", "Item ID", "Date", "Status"};
        DefaultTableModel myModel = new DefaultTableModel(myCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable myTable = new JTable(myModel);
        myClaimsPanel.add(new JScrollPane(myTable), BorderLayout.CENTER);

        JPanel myBtnPanel = new JPanel();
        JButton cancelBtn = new JButton("Cancel Selected Claim");
        JButton chatBtn = new JButton("Open Chat");
        chatBtn.setEnabled(false);

        myBtnPanel.add(cancelBtn);
        myBtnPanel.add(chatBtn);
        myClaimsPanel.add(myBtnPanel, BorderLayout.SOUTH);

        // Enable chat button only for approved claims
        myTable.getSelectionModel().addListSelectionListener(e -> {
            int row = myTable.getSelectedRow();
            if (row >= 0) {
                String status = String.valueOf(myModel.getValueAt(row, 3));
                chatBtn.setEnabled("APPROVED".equalsIgnoreCase(status));
            }
        });

        // Cancel claim action
        cancelBtn.addActionListener(e -> {
            int row = myTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a claim to cancel.");
                return;
            }
            int claimId = Integer.parseInt(myModel.getValueAt(row, 0).toString());
            String status = String.valueOf(myModel.getValueAt(row, 3));
            if (!"PENDING".equalsIgnoreCase(status)) {
                JOptionPane.showMessageDialog(this, "Only pending claims can be cancelled.");
                return;
            }
            try {
                boolean ok = new ClaimDAO().cancelClaim(claimId, userId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Claim cancelled successfully!");
                    refreshMyClaimsTable(myModel);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel claim.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error cancelling claim: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Chat action
        chatBtn.addActionListener(e -> {
            int row = myTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an approved claim first.");
                return;
            }
            int claimId = Integer.parseInt(myModel.getValueAt(row, 0).toString());
            try {
                int chatId = new ClaimDAO().getChatIdForClaim(claimId);
                if (chatId > 0) {
                    new ChatFrame(chatId, userId).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "No chat session found for this claim yet.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error opening chat: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        try {
            refreshMyClaimsTable(myModel);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load claims: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        tabs.addTab("My Claims", myClaimsPanel);

        // ---------------- Notifications ----------------
        JPanel notifPanel = new JPanel(new BorderLayout());
        String[] notifCols = {"Message", "Date", "Read"};
        DefaultTableModel notifModel = new DefaultTableModel(notifCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable notifTable = new JTable(notifModel);
        notifPanel.add(new JScrollPane(notifTable), BorderLayout.CENTER);

        try {
            List<Notification> notifs = new NotificationDAO().getNotificationsByUser(userId);
            for (Notification n : notifs) {
                notifModel.addRow(new Object[]{
                        n.getMessage(),
                        n.getCreatedAt(),
                        n.isRead() ? "Yes" : "No"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load notifications: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        tabs.addTab("Notifications", notifPanel);

        // ---------------- Add everything to frame ----------------
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);

        setVisible(true);
    }

    // Helper: refresh "My Claims" table
    private void refreshMyClaimsTable(DefaultTableModel myModel) {
        myModel.setRowCount(0);
        try {
            List<Claim> myClaims = new ClaimDAO().getClaimsByUser(userId);
            for (Claim claim : myClaims) {
                myModel.addRow(new Object[]{
                        claim.getId(),
                        claim.getItemId(),
                        claim.getClaimDate(),
                        claim.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading claims: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}