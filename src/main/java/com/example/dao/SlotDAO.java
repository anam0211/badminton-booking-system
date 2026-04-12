package com.example.dao;

import com.example.model.Slot;
import com.example.utils.DBConnection;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class SlotDAO {

    public List<Slot> getAll() throws Exception {
        List<Slot> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = "SELECT * FROM time_slots";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Slot s = new Slot();
            s.setId(rs.getInt("id"));
            s.setStartTime(rs.getTime("start_time").toString());
            s.setEndTime(rs.getTime("end_time").toString());
            s.setSlotName(rs.getString("slot_name"));
            list.add(s);
        }

        return list;
    }

    public void create(Slot s) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = """
            INSERT INTO time_slots(start_time, end_time, slot_name)
            VALUES (?, ?, ?)
        """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, s.getStartTime());
        ps.setString(2, s.getEndTime());
        ps.setString(3, s.getSlotName());

        ps.executeUpdate();
    }

    public void delete(int id) throws Exception {
        Connection conn = DBConnection.getConnection();

        String sql = "DELETE FROM time_slots WHERE id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();
    }

    public int createAndReturnId(Slot s) throws Exception {

        Connection conn = DBConnection.getConnection(); // 🔥 thiếu dòng này là lỗi ngay

        String sql = "INSERT INTO time_slots(start_time, end_time, slot_name) VALUES (?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, s.getStartTime());
        ps.setString(2, s.getEndTime());
        ps.setString(3, s.getSlotName());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();

        if (rs.next()) {
            return rs.getInt(1);
        }

        return 0;
    }

}