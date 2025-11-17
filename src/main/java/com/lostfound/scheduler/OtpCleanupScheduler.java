package com.lostfound.scheduler;

import com.lostfound.dao.NotificationDAO;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Background scheduler to clear expired OTP notifications every 5 minutes.
 */
public class OtpCleanupScheduler {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final Timer timer = new Timer(true); // daemon thread

    /** Start the scheduler. */
    public void start() {
        // Run immediately, then every 5 minutes (5 * 60 * 1000 ms)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    int cleared = notificationDAO.clearExpiredOtps();
                    if (cleared > 0) {
                        System.out.println("[Scheduler] Cleared " + cleared + " expired OTP notifications.");
                    }
                } catch (Exception e) {
                    System.err.println("[Scheduler] Error clearing expired OTPs: " + e.getMessage());
                }
            }
        }, 0, 5 * 60 * 1000);
    }
}