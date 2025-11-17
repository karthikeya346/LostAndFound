package com.lostfound.service;

import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.AuditLogDAO;
import org.springframework.stereotype.Service;

@Service
public class NotificationTriggerService {

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // 🔔 Claim created
    public void onClaimCreated(int userId, int itemId) {
        notificationDAO.saveNotification(1, "CLAIM_CREATED", 
            "User #" + userId + " submitted a claim for Item #" + itemId);
        auditLogDAO.logAction(userId, "CLAIM_CREATED", itemId, 0, "Claim submitted");
    }

    // 🔔 Claim approved
    public void onClaimApproved(int claimId, int userId, int itemId, int adminId) {
        notificationDAO.saveNotification(userId, "CLAIM_APPROVED", 
            "Your claim #" + claimId + " for Item #" + itemId + " has been approved.");
        auditLogDAO.logAction(adminId, "CLAIM_APPROVED", itemId, claimId, "Claim approved");
    }

    // 🔔 Claim rejected
    public void onClaimRejected(int claimId, int userId, int itemId, int adminId) {
        notificationDAO.saveNotification(userId, "CLAIM_REJECTED", 
            "Your claim #" + claimId + " for Item #" + itemId + " has been rejected.");
        auditLogDAO.logAction(adminId, "CLAIM_REJECTED", itemId, claimId, "Claim rejected");
    }

    // 🔔 Item reported
    public void onItemReported(int itemId, int userId) {
        notificationDAO.saveNotification(1, "ITEM_REPORTED", 
            "New item reported by User #" + userId + " (Item #" + itemId + ")");
        auditLogDAO.logAction(userId, "ITEM_REPORTED", itemId, 0, "Item reported");
    }

    // 🔔 Item claimed
    public void onItemClaimed(int itemId, int claimantId, int ownerId) {
        notificationDAO.saveNotification(ownerId, "ITEM_CLAIMED", 
            "Your item #" + itemId + " has been claimed by User #" + claimantId);
        auditLogDAO.logAction(claimantId, "ITEM_CLAIMED", itemId, 0, "Item claimed");
    }
}