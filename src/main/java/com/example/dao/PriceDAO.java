package com.example.dao;

import com.example.model.Price;
import com.example.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceDAO {

    public List<Price> getByBranch(Long branchId) throws Exception {

        List<Price> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = """
        SELECT p.*, t.slot_name
        FROM prices p
        JOIN time_slots t ON p.time_slot_id = t.id
        WHERE p.branch_id = ?
    """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, branchId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Price p = new Price();

            p.setId(rs.getLong("id"));
            p.setBranchId(rs.getLong("branch_id"));
            p.setTimeSlotId(rs.getInt("time_slot_id"));
            p.setPrice(rs.getDouble("price"));

            p.setSlotName(rs.getString("slot_name"));

            list.add(p);
        }

        return list;
    }

    public void create(Price p) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = """
            INSERT INTO prices(branch_id, time_slot_id, price)
            VALUES (?, ?, ?)
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, p.getBranchId());
        ps.setInt(2, p.getTimeSlotId());
        ps.setDouble(3, p.getPrice());

        ps.executeUpdate();
    }

    public void update(Price p) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = "UPDATE prices SET price=? WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setDouble(1, p.getPrice());
        ps.setLong(2, p.getId());

        ps.executeUpdate();
    }

    public void delete(Long id) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = "DELETE FROM prices WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);

        ps.executeUpdate();
    }
}