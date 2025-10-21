package com.lostfound.controller;

import com.lostfound.dao.ClaimDAO;
import com.lostfound.dao.ItemDAO;
import model.Claim;
import model.Item;

import java.util.Collections;
import java.util.List;

public class ClaimController {

    private final ClaimDAO claimDAO = new ClaimDAO();
    private final ItemDAO itemDAO = new ItemDAO();

    /** Create a new claim (user-initiated). */
    public boolean createClaim(Claim claim) {
        try {
            return claimDAO.createClaim(claim);
        } catch (Exception e) {
            e.printStackTrace(); // would print red text in console
            return false;
        }
    }

    /** Create a new claim and immediately fetch suggested matches. */
    public List<Item> createClaimWithSuggestions(Claim claim) {
        boolean saved = claimDAO.createClaim(claim);
        if (!saved) {
            return Collections.emptyList();
        }
        return itemDAO.findPotentialMatches(claim);
    }

    /** Link a claim to a matched item (user confirms suggestion). */
    public boolean linkClaimToItem(int claimId, int itemId, int userId) {
        return claimDAO.linkClaimToItem(claimId, itemId, userId);
    }

    /** Unlink a claim from an item (reset to pending). */
    public boolean unlinkClaim(int claimId, int userId) {
        return claimDAO.unlinkClaim(claimId, userId);
    }

    /** Fetch all pending claims (for admin dashboard). */
    public List<Claim> getPendingClaims() {
        return claimDAO.getPendingClaims();
    }

    /** Fetch all claims submitted by a specific user. */
    public List<Claim> getClaimsByUser(int userId) {
        List<Claim> claims = claimDAO.getClaimsByUser(userId);
        return claims != null ? claims : Collections.emptyList();
    }

    /** Cancel a claim (user action). */
    public boolean cancelClaim(int claimId, int userId) {
        return claimDAO.cancelClaim(claimId, userId);
    }

    /** Approve a claim (admin action). */
    public boolean approveClaim(int claimId, int adminId) {
        return claimDAO.approveClaim(claimId, adminId);
    }

    /** Reject a claim (admin action). */
    public boolean rejectClaim(int claimId, int adminId) {
        return claimDAO.rejectClaim(claimId, adminId);
    }

    /** Mark a claim as returned (admin action). */
    public boolean markReturned(int claimId, int adminId) {
        return claimDAO.markReturned(claimId, adminId);
    }

    /** Fetch chatId for a claim (for user dashboard chat button). */
    public int getChatIdForClaim(int claimId) {
        return claimDAO.getChatIdForClaim(claimId);
    }

    /** Fetch ownerId for an item (optional helper). */
    public int getOwnerIdForItem(int itemId) {
        return claimDAO.getOwnerIdForItem(itemId);
    }
}