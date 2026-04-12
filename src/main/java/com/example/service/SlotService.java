package com.example.service;

import com.example.dao.*;
import com.example.model.Slot;

import java.util.List;

public class SlotService {
    private SlotDAO dao = new SlotDAO();

    public List<Slot> getAll() throws Exception {
        return dao.getAll();
    }

    public void create(Slot s) throws Exception {
        dao.create(s);
    }

    public void delete(int id) throws Exception {
        dao.delete(id);
    }


    public int createAndReturnId(Slot s) throws Exception {
        return dao.createAndReturnId(s);
    }

}