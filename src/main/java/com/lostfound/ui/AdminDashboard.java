package com.lostfound.ui;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.AnalyticsDAO;
import model.Item;
import model.Claim;
import model.Notification;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

// JFreeChart imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboard extends JFrame {
    private final int adminId;

    private JTable itemsTable;
    private DefaultTableModel itemsModel;

    private JTable claimsTable;
    private DefaultTableModel claimsModel;

    private JTable notifTable;
    private DefaultTableModel notifModel;

    public AdminDashboard(int adminId) {
        this.adminId = adminId;
        setTitle("Admin Dashboard");
        setSize(1300, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Top panel with welcome + logout ---
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, Admin #" + adminId);
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            // optional: new UserDAO().logout(adminId);
            dispose();
            new LoginFrame().setVisible(true);
        });
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        topPanel.add(logoutBtn, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();

        // ---------------- Reported Items ----------------
        JPanel itemsPanel = new JPanel(new BorderLayout());
        String[] itemCols = {"ID", "Title", "Description", "Location", "Date Reported", "Status", "Reported By"};
        itemsModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        JPanel itemButtons = new JPanel();
        JButton approveItemBtn = new JButton("Approve Item");
        JButton rejectItemBtn = new JButton("Reject Item");
        JButton refreshItemsBtn = new JButton("Refresh Items");

        approveItemBtn.addActionListener(e -> approveSelectedItem());
        rejectItemBtn.addActionListener(e -> rejectSelectedItem());
        refreshItemsBtn.addActionListener(e -> loadItems());

        itemButtons.add(approveItemBtn);
        itemButtons.add(rejectItemBtn);
        itemButtons.add(refreshItemsBtn);

        itemsPanel.add(itemButtons, BorderLayout.SOUTH);
        tabs.addTab("Reported Items", itemsPanel);

        // ---------------- Claims Management ----------------
        JPanel claimsPanel = new JPanel(new BorderLayout());
        String[] claimCols = {"Claim ID", "Item ID", "User ID", "Matched Item ID", "Matched Title", "Date", "Status"};
        claimsModel = new DefaultTableModel(claimCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        claimsTable = new JTable(claimsModel);
        claimsPanel.add(new JScrollPane(claimsTable), BorderLayout.CENTER);

        JPanel claimButtons = new JPanel();
        JButton approveBtn = new JButton("Approve Claim");
        JButton rejectBtn = new JButton("Reject Claim");
        JButton returnedBtn = new JButton("Mark Returned");
        JButton refreshClaimsBtn = new JButton("Refresh Claims");

        approveBtn.addActionListener(e -> approveSelectedClaim());
        rejectBtn.addActionListener(e -> rejectSelectedClaim());
        returnedBtn.addActionListener(e -> returnSelectedClaim());
        refreshClaimsBtn.addActionListener(e -> loadClaims());

        claimButtons.add(approveBtn);
        claimButtons.add(rejectBtn);
        claimButtons.add(returnedBtn);
        claimButtons.add(refreshClaimsBtn);

        claimsPanel.add(claimButtons, BorderLayout.SOUTH);
        tabs.addTab("Claims", claimsPanel);

        // ---------------- Notifications ----------------
        JPanel notifPanel = new JPanel(new BorderLayout());
        String[] notifCols = {"User ID", "Message", "Date", "Read"};
        notifModel = new DefaultTableModel(notifCols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        notifTable = new JTable(notifModel);
        notifPanel.add(new JScrollPane(notifTable), BorderLayout.CENTER);

        loadNotifications(); // initial load
        tabs.addTab("Notifications", notifPanel);

        // ---------------- Analytics ----------------
        JPanel analyticsPanel = new JPanel(new GridLayout(1, 3));
        AnalyticsDAO analyticsDAO = new AnalyticsDAO();

        // Claims by Status (Pie Chart)
        Map<String, Integer> claimStats = analyticsDAO.getClaimStatsByStatus();
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        for (Map.Entry<String, Integer> entry : claimStats.entrySet()) {
            pieDataset.setValue(entry.getKey(), entry.getValue());
        }
        JFreeChart pieChart = ChartFactory.createPieChart("Claims by Status", pieDataset, true, true, false);
        ChartPanel piePanel = new ChartPanel(pieChart);
        analyticsPanel.add(piePanel);

        // Items by Month (Bar Chart)
        Map<String, Integer> itemsByMonth = analyticsDAO.getItemsReportedByMonth();
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : itemsByMonth.entrySet()) {
            barDataset.addValue(entry.getValue(), "Items", entry.getKey());
        }
        JFreeChart barChart = ChartFactory.createBarChart("Items Reported by Month", "Month", "Count", barDataset);
        ChartPanel barPanel = new ChartPanel(barChart);
        analyticsPanel.add(barPanel);

        // Top Locations (Bar Chart)
        Map<String, Integer> topLocations = analyticsDAO.getTopLocations(5);
        DefaultCategoryDataset locDataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> entry : topLocations.entrySet()) {
            locDataset.addValue(entry.getValue(), "Reports", entry.getKey());
        }
        JFreeChart locChart = ChartFactory.createBarChart("Top Locations", "Location", "Reports", locDataset);
        ChartPanel locPanel = new ChartPanel(locChart);
        analyticsPanel.add(locPanel);

        tabs.addTab("Analytics", analyticsPanel);

        // Layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);

        loadItems();
        loadClaims();

        setVisible(true);
    }

    // ---------------- Loaders ----------------
    private void loadItems() {
        itemsModel.setRowCount(0);
        try {
            List<Item> items = new ItemDAO().getAllItemsForAdmin();
            for (Item item : items) {
                itemsModel.addRow(new Object[]{
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getLocation(),
                        item.getDateReported(),
                        item.getStatus(),
                        item.getReportedBy()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load items: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadClaims() {
        claimsModel.setRowCount(0);
        try {
            List<Claim> claims = new ClaimDAO().getPendingClaims();
            ItemDAO itemDAO = new ItemDAO();

            for (Claim claim : claims) {
                int matchedId = claim.getMatchedItemId();
                String matchedTitle = "—";
                if (matchedId > 0) {
                    Item matchedItem = itemDAO.getItemById(matchedId);
                    if (matchedItem != null) {
                        matchedTitle = matchedItem.getTitle();
                    }
                }

                claimsModel.addRow(new Object[]{
                        claim.getId(),
                        claim.getItemId(),
                        claim.getUserId(),
                        matchedId == 0 ? "—" : matchedId,
                        matchedTitle,
                        claim.getClaimDate(),
                        claim.getStatus()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load claims: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadNotifications() {
        notifModel.setRowCount(0);
        try {
            List<Notification> notifs = new NotificationDAO().getAllNotifications();
            for (Notification n : notifs) {
                notifModel.addRow(new Object[]{
                        n.getUserId(),
                        n.getMessage(),
                        n.getCreatedAt(),
                        n.isRead() ? "Yes" : "No"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load notifications: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------------- Item actions ----------------
        private void approveSelectedItem() {
    int row = itemsTable.getSelectedRow();
    if (row == -1) {
        JOptionPane.showMessageDialog(this, "Select an item first.");
        return;
    }
    int itemId = Integer.parseInt(itemsModel.getValueAt(row, 0).toString());
    ItemDAO itemDAO = new ItemDAO();
    boolean ok = itemDAO.approveItem(itemId, adminId);
    if (ok) {
        Item item = itemDAO.getItemById(itemId);
        if (item != null) {
            new NotificationDAO().createNotification(
                    item.getUserId(),
                    "Your reported item (ID: " + itemId + ") has been approved by admin."
            );
        }
        loadNotifications();
        JOptionPane.showMessageDialog(this, "Item approved successfully!");
        loadItems();
    } else {
        JOptionPane.showMessageDialog(this, "Failed to approve item.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private void rejectSelectedItem() {
        int row = itemsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }
        int itemId = Integer.parseInt(itemsModel.getValueAt(row, 0).toString());
        ItemDAO itemDAO = new ItemDAO();
        boolean ok = itemDAO.rejectItem(itemId, adminId);
        if (ok) {
            int userId = itemDAO.getItemById(itemId).getUserId();
            new NotificationDAO().saveNotification(
                    userId,
                    "ITEM",
                    "Your reported item (ID: " + itemId + ") has been rejected by admin."
            );
            loadNotifications();
            JOptionPane.showMessageDialog(this, "Item rejected successfully!");
            loadItems();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reject item.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void approveSelectedClaim() {
        int row = claimsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim first.");
            return;
        }
        int claimId = Integer.parseInt(claimsModel.getValueAt(row, 0).toString());
        ClaimDAO claimDAO = new ClaimDAO();
        boolean ok = claimDAO.approveClaim(claimId, adminId);
        if (ok) {
            int userId = claimDAO.getUserIdForClaim(claimId);
            new NotificationDAO().saveNotification(
                    userId,
                    "CLAIM",
                    "Your claim (ID: " + claimId + ") has been approved by admin."
            );
            loadNotifications();
            JOptionPane.showMessageDialog(this, "Claim approved successfully!");
            loadClaims();
            loadItems(); // keep items in sync
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve claim.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelectedClaim() {
        int row = claimsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim first.");
            return;
        }
        int claimId = Integer.parseInt(claimsModel.getValueAt(row, 0).toString());
        ClaimDAO claimDAO = new ClaimDAO();
        boolean ok = claimDAO.rejectClaim(claimId, adminId);
        if (ok) {
            int userId = claimDAO.getUserIdForClaim(claimId);
            new NotificationDAO().saveNotification(
                    userId,
                    "CLAIM",
                    "Your claim (ID: " + claimId + ") has been rejected by admin."
            );
            loadNotifications();
            JOptionPane.showMessageDialog(this, "Claim rejected successfully!");
            loadClaims();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to reject claim.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnSelectedClaim() {
        int row = claimsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a claim first.");
            return;
        }
        int claimId = Integer.parseInt(claimsModel.getValueAt(row, 0).toString());
        ClaimDAO claimDAO = new ClaimDAO();
        boolean ok = claimDAO.markReturned(claimId, adminId);
        if (ok) {
            int userId = claimDAO.getUserIdForClaim(claimId);
            new NotificationDAO().saveNotification(
                    userId,
                    "CLAIM",
                    "Your claim (ID: " + claimId + ") has been marked as returned."
            );
            loadNotifications();
            JOptionPane.showMessageDialog(this, "Claim marked as Returned!");
            loadClaims();
            loadItems();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to mark claim as Returned.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}