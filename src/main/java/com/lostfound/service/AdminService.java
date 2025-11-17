package com.lostfound.service;

import com.lostfound.dao.ItemDAO;
import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.NotificationDAO;
import com.lostfound.dao.AuditLogDAO;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final ItemDAO itemDAO;
    private final ClaimDAO claimDAO;
    private final NotificationDAO notificationDAO;
    private final AuditLogDAO auditLogDAO;

    public AdminService(ItemDAO itemDAO, ClaimDAO claimDAO,
                        NotificationDAO notificationDAO, AuditLogDAO auditLogDAO) {
        this.itemDAO = itemDAO;
        this.claimDAO = claimDAO;
        this.notificationDAO = notificationDAO;
        this.auditLogDAO = auditLogDAO;
    }

    // ---------------- Items ----------------
    public boolean approveItem(int itemId, int adminId) {
        boolean ok = itemDAO.updateStatus(itemId, "APPROVED");
        if (ok) {
            auditLogDAO.logAction(adminId, "ITEM_APPROVED", itemId, 0, "Item approved by admin");
        }
        return ok;
    }

    public boolean rejectItem(int itemId, int adminId, String reason) {
        boolean ok = itemDAO.updateStatus(itemId, "REJECTED");
        if (ok) {
            auditLogDAO.logAction(adminId, "ITEM_REJECTED", itemId, 0, reason != null ? reason : "Item rejected");
        }
        return ok;
    }

    // ---------------- Claims ----------------
    public boolean approveClaim(int claimId, int adminId) {
        boolean ok = claimDAO.approveClaim(claimId, adminId);
        if (ok) {
            auditLogDAO.logAction(adminId, "CLAIM_APPROVED", 0, claimId, "Claim approved by admin");
            int userId = claimDAO.getUserIdForClaim(claimId);
            if (userId != -1) {
                notificationDAO.createNotification(userId, "Your claim #" + claimId + " has been approved.");
            }
        }
        return ok;
    }

    public boolean rejectClaim(int claimId, int adminId) {
        boolean ok = claimDAO.rejectClaim(claimId, adminId);
        if (ok) {
            auditLogDAO.logAction(adminId, "CLAIM_REJECTED", 0, claimId, "Claim rejected by admin");
            int userId = claimDAO.getUserIdForClaim(claimId);
            if (userId != -1) {
                notificationDAO.createNotification(userId, "Your claim #" + claimId + " has been rejected.");
            }
        }
        return ok;
    }

    public boolean finalizeHandover(int claimId, int itemId, int adminId) {
        boolean okClaim = claimDAO.markReturned(claimId, adminId);
        boolean okItem = itemDAO.updateStatus(itemId, "RETURNED");

        if (okClaim && okItem) {
            auditLogDAO.logAction(adminId, "FINAL_HANDOVER", itemId, claimId, "Item returned to owner, claim completed");
            int userId = claimDAO.getUserIdForClaim(claimId);
            if (userId != -1) {
                notificationDAO.createNotification(userId, "Your claim #" + claimId + " has been completed. Item returned.");
            }
            return true;
        }
        return false;
    }
}