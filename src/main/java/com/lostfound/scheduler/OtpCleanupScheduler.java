package com.lostfound.scheduler;

import com.lostfound.controller.NotificationController;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Background scheduler to clear expired OTP notifications every 5 minutes.
 */
public class OtpCleanupScheduler {

    private final NotificationController notificationController = new NotificationController();
    private final Timer timer = new Timer(true); // daemon thread

    /** Start the scheduler. */
    public void start() {
        // Run immediately, then every 5 minutes (5 * 60 * 1000 ms)
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int cleared = notificationController.clearExpiredOtps();
                if (cleared > 0) {
                    System.out.println("[Scheduler] Cleared " + cleared + " expired OTP notifications.");
                }
            }
        }, 0, 5 * 60 * 1000);
    }
}