package dao;

import connectDB.ConnectDB;
import model.TaiKhoan;

import java.sql.*;

public class TaiKhoan_Dao {

    public TaiKhoan authenticate(String username, String password) {
        final String sql = """
            SELECT maTK, tenDangNhap, matKhau, maNV, trangThai, loaiTK
            FROM TaiKhoan
            WHERE tenDangNhap = ? AND trangThai = N'active'
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String stored = rs.getString("matKhau");
                if (stored != null && stored.equals(password)) {
                    return new TaiKhoan(
                            rs.getString("maTK"),
                            rs.getString("tenDangNhap"),
                            stored,
                            rs.getString("maNV"),
                            rs.getString("trangThai"),
                            rs.getString("loaiTK")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi CSDL: " + e.getMessage(), e);
        }
        return null;
    }
}
