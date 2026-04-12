package com.example.service;

import com.example.dao.PriceDAO;
import com.example.model.Price;

import java.util.List;

public class PriceService {

    private PriceDAO dao = new PriceDAO();

    public List<Price> getByBranch(Long branchId) throws Exception {
        return dao.getByBranch(branchId);
    }

    public void create(Price p) throws Exception {
        if (p.getPrice() <= 0) {
            throw new Exception("Price must > 0");
        }
        dao.create(p);
    }

    public void update(Price p) throws Exception {
        dao.update(p);
    }

    public void delete(Long id) throws Exception {
        dao.delete(id);
    }
}