package com.example.service;

import com.example.dao.*;
import com.example.model.Court;
import com.example.model.Slot;

import com.example.utils.DBConnection;
import java.sql.*;
import java.text.*;
import java.util.*;

public class CourtService {
    private CourtDAO dao = new CourtDAO();

    public List<Court> getByBranch(Long branchId) throws Exception {
        return dao.getByBranch(branchId);
    }

    public Court getById(Long id) throws Exception {
        return dao.getById(id);
    }

    public void create(Court c) throws Exception {
        dao.create(c);
    }

    public void update(Court c) throws Exception {
        dao.update(c);
    }

    public void delete(Long id) throws Exception {
        dao.delete(id);
    }


    public List<Court> filterCourts(Long branchId,
                                    String slotStart,
                                    String slotEnd,
                                    Double priceMin,
                                    Double priceMax) throws Exception {

        return dao.filter(branchId, slotStart, slotEnd, priceMin, priceMax);
    }

    public Long createAndReturnId(Court c) throws Exception {
        return dao.createAndReturnId(c);
    }

}