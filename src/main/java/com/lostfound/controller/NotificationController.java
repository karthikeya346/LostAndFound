package com.lostfound.controller;

import com.lostfound.dao.NotificationDAO;
import model.Notification;

import java.util.List;

public class NotificationController {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    /** Create a new notification for a user. */
    public boolean createNotification(int userId, String message) {
        return notificationDAO.createNotification(userId, message);
    }

    /** Save a typed notification (preferred). */
    public boolean saveNotification(int userId, String type, String message) {
        return notificationDAO.saveNotification(userId, type, message);
    }

    /** Fetch all notifications for a specific user. */
    public List<Notification> getNotificationsByUser(int userId) {
        return notificationDAO.getNotificationsByUser(userId);
    }

    /** Fetch all notifications (admin view). */
    public List<Notification> getAllNotifications() {
        return notificationDAO.getAllNotifications();
    }

    /** Mark a notification as read. */
    public boolean markAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    /** Delete a notification (user/admin action). */
    public boolean deleteNotification(int notificationId, int userId) {
        return notificationDAO.deleteNotification(notificationId, userId);
    }

    /** Clear expired OTP notifications (older than 5 minutes). */
    public int clearExpiredOtps() {
        return notificationDAO.clearExpiredOtps();
    }
}