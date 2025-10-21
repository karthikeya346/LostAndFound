package com.lostfound;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.lostfound.ui.LoginFrame;
import com.lostfound.scheduler.OtpCleanupScheduler; // ✅ import scheduler

public class MainApp {
    public static void main(String[] args) {
        try {
             //Set FlatLaf look and feel
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
            System.err.println("⚠️ Failed to set FlatLaf look and feel.");
        }

        // ✅ Start OTP cleanup scheduler
        new OtpCleanupScheduler().start();

        // ✅ Launch LoginFrame on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}