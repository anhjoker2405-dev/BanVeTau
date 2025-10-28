package dao;

import connectDB.ConnectDB;
import entity.LoaiVe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoaiVe_Dao {

    public List<LoaiVe> findAll() throws SQLException {
        List<LoaiVe> list = new ArrayList<>();
        String sql = "SELECT maLoaiVe, tenLoaiVe, moTaLoaiVe FROM LoaiVe ORDER BY maLoaiVe";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LoaiVe(
                        rs.getString("maLoaiVe"),
                        rs.getString("tenLoaiVe"),
                        rs.getString("moTaLoaiVe")
                ));
            }
        }
        return list;
    }
}