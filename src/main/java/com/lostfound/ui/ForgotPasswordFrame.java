package com.lostfound.ui;

import com.lostfound.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class ForgotPasswordFrame extends JFrame {

    private final AuthService authService = new AuthService();

    public ForgotPasswordFrame() {
        setTitle("Forgot Password");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel emailLabel = new JLabel("Enter your registered email:");
        JTextField emailField = new JTextField();
        JButton submitBtn = new JButton("Send Reset Token");

        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(submitBtn);

        add(panel);

        submitBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your email.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String token = authService.initiatePasswordReset(email);
                if (token != null) {
                    // For now, just show the token in a popup (later: send via email)
                    JOptionPane.showMessageDialog(this,
                            "Reset token generated!\n\nToken: " + token + "\n(Valid for 15 minutes)",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new ResetPasswordFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No account found with that email.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error generating reset token: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}