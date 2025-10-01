package dao;

import connectDB.ConnectDB;
import model.*;

import java.sql.*;

public class NhanVien_Dao {
    public NhanVien findByUsername(String username) {
        String sql = "SELECT nv.maNV, nv.tenNV, nv.soDienThoai, nv.email, nv.loaiNV, tk.loaiTK " +
                     "FROM TaiKhoan tk JOIN NhanVien nv ON nv.maNV = tk.maNV " +
                     "WHERE tk.tenDangNhap = ? AND tk.trangThai = N'active'";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String maNV = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String sdt   = rs.getString("soDienThoai");
                String email = rs.getString("email");
                String loaiNV = rs.getString("loaiNV");
                String loaiTK = rs.getString("loaiTK");
                String role = (loaiNV != null && !loaiNV.isBlank()) ? loaiNV : (loaiTK == null ? "" : loaiTK);
                if (role.toLowerCase().contains("quản") || role.equalsIgnoreCase("admin") || role.toLowerCase().contains("manager")) {
                    return new NhanVienQuanLy(maNV, tenNV, sdt, email);
                } else {
                    return new NhanVienBanVe(maNV, tenNV, sdt, email);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi NhanVien_Dao: " + e.getMessage(), e);
        }
    }
}
