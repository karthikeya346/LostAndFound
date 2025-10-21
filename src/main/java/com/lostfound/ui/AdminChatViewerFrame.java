package com.lostfound.ui;

import com.lostfound.controller.ChatController;
import model.ChatSession;
import model.ChatMessage;
import model.ChatParticipant;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Admin UI to view all chat sessions and their messages in a table.
 */
public class AdminChatViewerFrame extends JFrame {

    private final ChatController chatController = new ChatController();

    private JTable sessionTable;
    private JTable messageTable;
    private JButton refreshBtn;
    private JButton deleteMsgBtn;
    private JButton closeChatBtn;
    private JButton banUserBtn;

    private int selectedChatId = -1;
    private final int adminId; // current admin userId for audit logging

    public AdminChatViewerFrame(int adminId) {
        this.adminId = adminId;

        setTitle("Admin - Chat Viewer");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        attachListeners();

        loadSessions();
    }

    private void initComponents() {
        sessionTable = new JTable();
        messageTable = new JTable();

        refreshBtn = new JButton("Refresh Sessions");
        deleteMsgBtn = new JButton("Delete Selected Message");
        closeChatBtn = new JButton("Close Chat");
        banUserBtn = new JButton("Ban Selected User");
    }

    private void layoutComponents() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Left: sessions
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);
        leftPanel.add(refreshBtn, BorderLayout.SOUTH);

        // Right: messages + actions
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(new JScrollPane(messageTable), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.add(deleteMsgBtn);
        actionPanel.add(banUserBtn);
        actionPanel.add(closeChatBtn);

        rightPanel.add(actionPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerLocation(350);

        add(splitPane, BorderLayout.CENTER);
    }

    private void attachListeners() {
        refreshBtn.addActionListener(e -> loadSessions());

        sessionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = sessionTable.getSelectedRow();
                if (row >= 0) {
                    selectedChatId = (int) sessionTable.getValueAt(row, 0);
                    loadMessages(selectedChatId);
                }
            }
        });

        deleteMsgBtn.addActionListener(e -> {
            int row = messageTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a message to delete.");
                return;
            }
            int messageId = (int) messageTable.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete message ID " + messageId + "?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = chatController.deleteMessage(messageId, adminId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Message deleted.");
                    loadMessages(selectedChatId);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete message.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        closeChatBtn.addActionListener(e -> {
            if (selectedChatId > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Close chat ID " + selectedChatId + "?",
                        "Confirm Close", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    boolean ok = chatController.closeChat(selectedChatId, adminId);
                    if (ok) {
                        JOptionPane.showMessageDialog(this, "Chat closed.");
                        loadSessions();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to close chat.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        banUserBtn.addActionListener(e -> {
            int row = messageTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a message to identify the user.");
                return;
            }
            int userId = chatController.getMessages(selectedChatId)
                    .get(row).getSenderUserId();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Remove user " + userId + " from chat " + selectedChatId + "?",
                    "Confirm Ban", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = chatController.removeParticipant(selectedChatId, userId, adminId);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "User removed from chat.");
                    loadMessages(selectedChatId);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to remove user.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadSessions() {
        List<ChatSession> sessions = chatController.getAllChatSessions();
        if (sessions == null) sessions = List.of();

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Chat ID", "Item ID", "Claim ID", "Status"}, 0);
        for (ChatSession cs : sessions) {
            model.addRow(new Object[]{cs.getId(), cs.getItemId(), cs.getClaimId(), cs.getStatus()});
        }
        sessionTable.setModel(model);
    }

    private void loadMessages(int chatId) {
        List<ChatMessage> messages = chatController.getMessages(chatId);
        if (messages == null) messages = List.of();

        List<ChatParticipant> participants = chatController.getParticipants(chatId);
        if (participants == null) participants = List.of();

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Message ID", "Alias", "Message", "Timestamp"}, 0);

        for (ChatMessage msg : messages) {
            String alias = participants.stream()
                    .filter(p -> p.getUserId() == msg.getSenderUserId())
                    .map(ChatParticipant::getAlias)
                    .findFirst()
                    .orElse("User");
            model.addRow(new Object[]{
                    msg.getId(),
                    alias,
                    msg.getMessage(),
                    msg.getCreatedAt()
            });
        }
        messageTable.setModel(model);
        messageTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    // For quick testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminChatViewerFrame(999).setVisible(true));
    }
}