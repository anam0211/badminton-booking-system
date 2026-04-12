package com.example.dao;

import com.example.model.*;
import com.example.utils.DBConnection;

import java.sql.*;
import java.util.*;

public class CourtDAO {

    public List<Court> getByBranch(Long branchId) throws Exception {
        List<Court> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = """
        SELECT c.*, b.name AS branch_name, b.address
        FROM courts c
        JOIN branches b ON c.branch_id = b.id
        WHERE c.branch_id = ? AND c.is_deleted = FALSE
    """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, branchId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Court c = new Court();
            c.setId(rs.getLong("id"));
            c.setName(rs.getString("name"));
            c.setType(rs.getString("type"));
            c.setStatus(rs.getString("status"));
            c.setBranchId(rs.getLong("branch_id"));


            Branch b = new Branch();
            b.setId(rs.getLong("branch_id"));
            b.setName(rs.getString("branch_name"));
            b.setAddress(rs.getString("address"));

            c.setBranch(b);

            list.add(c);
        }

        return list;
    }

    public Court getById(Long id) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = """
        SELECT c.*, b.name AS branch_name, b.address
        FROM courts c
        JOIN branches b ON c.branch_id = b.id
        WHERE c.id = ?
    """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            Court c = new Court();
            c.setId(id);
            c.setName(rs.getString("name"));
            c.setType(rs.getString("type"));
            c.setBranchId(rs.getLong("branch_id"));
            c.setStatus(rs.getString("status"));

            Branch b = new Branch();
            b.setId(rs.getLong("branch_id"));
            b.setName(rs.getString("branch_name"));
            b.setAddress(rs.getString("address"));

            c.setBranch(b);

            return c;
        }
        return null;
    }

    public void create(Court c) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = """
        INSERT INTO courts (name, type, branch_id, status)
        VALUES (?, ?, ?, 'AVAILABLE')
    """;

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, c.getName());
        ps.setString(2, c.getType());
        ps.setLong(3, c.getBranchId());

        ps.executeUpdate();
    }

    public void update(Court c) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = """
        UPDATE courts
        SET name = ?, type = ?, status = ?
        WHERE id = ?
    """;

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setString(1, c.getName());
        ps.setString(2, c.getType());
        ps.setString(3, c.getStatus());
        ps.setLong(4, c.getId());

        ps.executeUpdate();
    }

    public void delete(Long id) throws Exception {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE courts SET is_deleted=TRUE WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, id);
        ps.executeUpdate();
    }

    public List<Court> filter(Long branchId,
                              String slotStart, String slotEnd,
                              Double priceMin, Double priceMax) throws Exception {

        List<Court> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = """
        SELECT DISTINCT c.*
        FROM courts c
        JOIN prices p ON c.branch_id = p.branch_id
        JOIN time_slots t ON p.time_slot_id = t.id
        WHERE c.branch_id = ?
        
        AND (? IS NULL OR t.start_time >= ?)
        AND (? IS NULL OR t.end_time <= ?)

        AND (? IS NULL OR p.price >= ?)
        AND (? IS NULL OR p.price <= ?)

        AND c.is_deleted = FALSE
    """;

        PreparedStatement ps = conn.prepareStatement(sql);

        ps.setLong(1, branchId);

        ps.setString(2, slotStart);
        ps.setString(3, slotStart);

        ps.setString(4, slotEnd);
        ps.setString(5, slotEnd);

        ps.setObject(6, priceMin);
        ps.setObject(7, priceMin);

        ps.setObject(8, priceMax);
        ps.setObject(9, priceMax);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Court c = new Court();
            c.setId(rs.getLong("id"));
            c.setName(rs.getString("name"));
            c.setType(rs.getString("type"));
            list.add(c);
        }

        return list;
    }

    public Long createAndReturnId(Court c) throws Exception {

        Connection conn = DBConnection.getConnection();

        String sql = "INSERT INTO courts(name, type, branch_id) VALUES (?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, c.getName());
        ps.setString(2, c.getType());
        ps.setLong(3, c.getBranchId());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            return rs.getLong(1); // 🔥 ID vừa insert
        }

        return null;
    }


}