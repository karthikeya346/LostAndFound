package com.lostfound.ui;

import com.lostfound.controller.ChatController;
import model.ChatMessage;
import model.ChatParticipant;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Swing chat window for a given chat session.
 * Shows messages with aliases, allows sending new messages.
 */
public class ChatFrame extends JFrame {

    private final ChatController chatController = new ChatController();
    private final int chatId;
    private final int currentUserId;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendBtn;

    public ChatFrame(int chatId, int currentUserId) {
        this.chatId = chatId;
        this.currentUserId = currentUserId;

        setTitle("Anonymous Chat - Session " + chatId);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();
        attachListeners();

        loadMessages();

        // 🔄 Auto-refresh every 3 seconds
        new Timer(3000, e -> loadMessages()).start();

        setVisible(true);
    }

    private void initComponents() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);

        inputField = new JTextField();
        sendBtn = new JButton("Send");
    }

    private void layoutComponents() {
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendBtn, BorderLayout.EAST);

        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void attachListeners() {
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        boolean ok = chatController.sendMessage(chatId, currentUserId, text);
        if (ok) {
            inputField.setText("");
            loadMessages();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to send message.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMessages() {
        chatArea.setText("");

        List<ChatMessage> messages = chatController.getMessages(chatId);
        if (messages == null) messages = List.of();

        List<ChatParticipant> participants = chatController.getParticipants(chatId);
        if (participants == null) participants = List.of();

        StringBuilder sb = new StringBuilder();
        for (ChatMessage msg : messages) {
            String alias = participants.stream()
                    .filter(p -> p.getUserId() == msg.getSenderUserId())
                    .map(ChatParticipant::getAlias)
                    .findFirst()
                    .orElse("User");
            sb.append(alias).append(": ").append(msg.getMessage()).append("\n");
        }
        chatArea.setText(sb.toString());

        // 🔽 Auto-scroll to bottom
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    // For quick standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Example: open chatId=1 as userId=2
            new ChatFrame(1, 2).setVisible(true);
        });
    }
}