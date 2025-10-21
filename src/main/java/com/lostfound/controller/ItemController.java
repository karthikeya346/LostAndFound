package com.lostfound.controller;

import com.lostfound.dao.ItemDAO;
import model.Item;
import model.Claim;

import java.util.List;

public class ItemController {

    private final ItemDAO itemDAO = new ItemDAO();

    /** Add a new item (user reporting lost/found). */
    public boolean addItem(Item item) {
        return itemDAO.addItem(item);
    }

    /** Fetch all items for admin view (includes usernames). */
    public List<Item> getAllItemsForAdmin() {
        return itemDAO.getAllItemsForAdmin();
    }

    /** Fetch all items for user view (excludes CLAIMED). */
    public List<Item> getAllItemsForUser() {
        return itemDAO.getAllItemsForUserView();
    }

    /** Fetch a single item by ID. */
    public Item getItemById(int itemId) {
        return itemDAO.getItemById(itemId);
    }

    /** Find potential matches for a claim (smart matching). */
    public List<Item> findPotentialMatches(Claim claim) {
        return itemDAO.findPotentialMatches(claim);
    }

    /** Update item status (generic moderation). */
    public boolean updateItemStatus(int itemId, String status) {
        return itemDAO.updateStatus(itemId, status);
    }

    /** Approve an item (admin action). */
    public boolean approveItem(int itemId, int adminId) {
        return itemDAO.approveItem(itemId, adminId);
    }

    /** Reject an item (admin action). */
    public boolean rejectItem(int itemId, int adminId) {
        return itemDAO.rejectItem(itemId, adminId);
    }

    /** Delete an item (user or admin action). */
    public boolean deleteItem(int itemId, int actorId) {
        return itemDAO.deleteItem(itemId, actorId);
    }
}