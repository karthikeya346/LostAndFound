package com.lostfound.ui;

import model.User;
import com.lostfound.service.AuthService;
import com.lostfound.service.OTPService;
import com.lostfound.dao.NotificationDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Login and Register frame for Lost & Found system.
 * Includes Login, Register, OTP verification, and Forgot Password flow.
 */
public class LoginFrame extends JFrame {

    // ---------------- UI Components ----------------
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JButton forgotBtn;   // ✅ new
    private JLabel statusLabel;

    // ---------------- Services ----------------
    private final AuthService authService = new AuthService();
    private final OTPService otpService = new OTPService();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    // ---------------- Constructor ----------------
    public LoginFrame() {
        setTitle("Lost & Found - Login");
        setSize(500, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Build UI
        initComponents();
        layoutComponents();
        attachListeners();
    }

    // ---------------- UI Setup ----------------
    private void initComponents() {
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        emailField = new JTextField();

        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register");
        forgotBtn = new JButton("Forgot Password?");   // ✅ new

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.DARK_GRAY);
    }

    private void layoutComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        // Email (for registration)
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email (for Register):"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);
        buttonPanel.add(forgotBtn);   // ✅ added here

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        // Status label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        add(formPanel, BorderLayout.CENTER);
    }

    private void attachListeners() {
        loginBtn.addActionListener(e -> onLogin());
        registerBtn.addActionListener(e -> onRegister());
        forgotBtn.addActionListener(e -> onForgotPassword());   // ✅ new
    }

    // ---------------- Login Flow ----------------
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = authService.login(username, password);
        if (user == null) {
            showError("Invalid credentials.");
            return;
        }

        // Step 1: Generate OTP
        String otp = otpService.generateOTP(user.getId(), user.getEmail());
        if (otp == null) {
            showError("Login failed: Only SRM email addresses are allowed.");
            return;
        }

        // Step 2: Show OTP to user
        JOptionPane.showMessageDialog(this,
                "Your OTP is: " + otp,
                "OTP Generated",
                JOptionPane.INFORMATION_MESSAGE);

        // Step 3: Send OTP as notification
        notificationDAO.createNotification(user.getId(), "Your OTP is: " + otp);

        // Step 4: Prompt for OTP
        String inputOtp = JOptionPane.showInputDialog(this,
                "Enter the OTP sent to your account:",
                "OTP Verification",
                JOptionPane.PLAIN_MESSAGE);

        // Step 5: Verify OTP
        if (inputOtp != null && otpService.verifyOTP(user.getId(), inputOtp.trim())) {
            JOptionPane.showMessageDialog(this,
                    "Login successful! Role: " + user.getRole(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboard(user.getId()).setVisible(true);
            } else {
                new UserDashboard(user.getId()).setVisible(true);
            }
        } else {
            showError("Invalid or expired OTP.");
        }
    }

    // ---------------- Register Flow ----------------
    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        boolean ok = authService.register(username, password, email);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                    "Registered successfully. You can now log in.",
                    "Registration Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            showError("Registration failed (username/email may already exist or invalid domain).");
        }
    }

    // ---------------- Forgot Password Flow ----------------
    private void onForgotPassword() {
        SwingUtilities.invokeLater(() -> {
            new ForgotPasswordFrame().setVisible(true);
        });
    }

    // ---------------- Helpers ----------------
    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        statusLabel.setText(msg);
        statusLabel.setForeground(Color.RED);
    }

    // ---------------- Main ----------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        LoginFrame frame = new LoginFrame();
          frame.setVisible(true);
        });
    }
}