package com.lostfound.ui;

import com.lostfound.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ResetPasswordFrame extends JFrame {

    private final AuthService authService = new AuthService();

    public ResetPasswordFrame() {
        setTitle("Reset Password");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel tokenLabel = new JLabel("Reset Token:");
        JTextField tokenField = new JTextField();

        JLabel passLabel = new JLabel("New Password:");
        JPasswordField passField = new JPasswordField();

        JLabel confirmLabel = new JLabel("Confirm Password:");
        JPasswordField confirmField = new JPasswordField();

        JButton resetBtn = new JButton("Reset Password");

        panel.add(tokenLabel); panel.add(tokenField);
        panel.add(passLabel); panel.add(passField);
        panel.add(confirmLabel); panel.add(confirmField);
        panel.add(new JLabel("")); panel.add(resetBtn);

        add(panel);

        resetBtn.addActionListener(e -> {
            String token = tokenField.getText().trim();
            String newPass = new String(passField.getPassword());
            String confirmPass = new String(confirmField.getPassword());

            if (token.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                boolean valid = authService.validateResetToken(token);
                if (!valid) {
                    JOptionPane.showMessageDialog(this, "Invalid or expired token.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean ok = authService.resetPassword(token, newPass);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Password reset successful! You can now log in.");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error resetting password: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}