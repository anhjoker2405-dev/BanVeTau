package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;

import java.sql.*;

public class TaiKhoan_Dao {

    public TaiKhoan authenticate(String username, String password) {
        final String sql = """
            SELECT
                tk.maTK,
                tk.tenDangNhap,
                tk.matKhau,
                tk.maNV,
                tk.trangThai,
                -- Lấy tên loại TK nếu có; alias về loaiTK để khớp model cũ
                COALESCE(ltk.tenLoaiTK, tk.maLoaiTK) AS loaiTK
            FROM TaiKhoan tk
            LEFT JOIN LoaiTaiKhoan ltk ON ltk.maLoaiTK = tk.maLoaiTK
            WHERE LTRIM(RTRIM(LOWER(tk.tenDangNhap))) = LOWER(?)
              AND LOWER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'Kich_Hoat'
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String stored = rs.getString("matKhau");
                // So sánh đơn giản (nếu dùng hash thì thay bằng verify hash)
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
            throw new RuntimeException("Lỗi CSDL (authenticate): " + e.getMessage(), e);
        }
        return null;
    }
}
