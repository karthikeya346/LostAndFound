package com.lostfound.service;

import com.lostfound.dao.ItemDAO;
import model.Claim;
import model.Item;

import java.util.List;

public class MatchingService {
    private final ItemDAO itemDAO = new ItemDAO();

    public List<Item> findMatchesForClaim(Claim claim) {
        return itemDAO.findPotentialMatches(claim);
    }
}