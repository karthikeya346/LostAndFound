package com.lostfound.service;

import com.lostfound.dao.NotificationDAO;
import com.lostfound.util.EmailUtil;

public class NotificationService {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final EmailUtil emailUtil = new EmailUtil();

    public void sendNotification(int userId, String email, String type, String message) {
        // Save notification in DB
        notificationDAO.saveNotification(userId, type, message);

        // Send email (stubbed for now)
        String subject = "Lost & Found Update: " + type.replace("_", " ");
        emailUtil.sendEmail(email, subject, message);
    }
}