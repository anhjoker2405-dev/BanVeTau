package dao;

import connectDB.ConnectDB;
import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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
    
    // =================== CRUD phục vụ màn quản lý nhân viên ===================

    public List<NhanVienThongTin> findAll() throws SQLException {
        List<NhanVienThongTin> list = new ArrayList<>(); //chỉ hiện nhân viên có tài khoản active
            String sql = """
                SELECT nv.maNV, nv.tenNV, nv.ngaySinh, nv.soDienThoai, nv.email, nv.loaiNV
                FROM NhanVien nv
                WHERE EXISTS (
                    SELECT 1 FROM TaiKhoan tk
                    WHERE tk.maNV = nv.maNV
                      AND LOWER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'active'
                )
                OR NOT EXISTS (SELECT 1 FROM TaiKhoan tk WHERE tk.maNV = nv.maNV)
                ORDER BY nv.maNV
            """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date ngaySinh = rs.getDate("ngaySinh");
                LocalDate dob = ngaySinh != null ? ngaySinh.toLocalDate() : null;
                list.add(new NhanVienThongTin(
                        rs.getString("maNV"),
                        rs.getString("tenNV"),
                        dob,
                        rs.getString("soDienThoai"),
                        rs.getString("email"),
                        rs.getString("loaiNV")
                ));
            }
        }
        return list;
    }

    public boolean existsById(String maNV) throws SQLException {
        String sql = "SELECT 1 FROM NhanVien WHERE maNV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String generateNextId() throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            return nextId(con);
        }
    }

    public String nextId(Connection con) throws SQLException {
        String sql = "SELECT MAX(maNV) FROM NhanVien WHERE maNV LIKE 'NV%'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString(1);
                if (last != null) {
                    last = last.trim();
                    String numberPart = last.length() > 2 ? last.substring(2) : "0";
                    int n;
                    try {
                        n = Integer.parseInt(numberPart);
                    } catch (NumberFormatException ex) {
                        n = 0;
                    }
                    return "NV" + String.format("%03d", n + 1);
                }
            }
        }
        return "NV001";
    }

    public int insert(NhanVienThongTin nv) throws SQLException {
        String sql = "INSERT INTO NhanVien(maNV, tenNV, ngaySinh, soDienThoai, email, loaiNV) VALUES(?,?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getTenNV());
            if (nv.getNgaySinh() != null) {
                ps.setDate(3, Date.valueOf(nv.getNgaySinh()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setString(4, nv.getSoDienThoai());
            ps.setString(5, nv.getEmail());
            ps.setString(6, nv.getLoaiNV());
            return ps.executeUpdate();
        }
    }

    public int update(NhanVienThongTin nv) throws SQLException {
        String sql = "UPDATE NhanVien SET tenNV=?, ngaySinh=?, soDienThoai=?, email=?, loaiNV=? WHERE maNV=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getTenNV());
            if (nv.getNgaySinh() != null) {
                ps.setDate(2, Date.valueOf(nv.getNgaySinh()));
            } else {
                ps.setNull(2, Types.DATE);
            }
            ps.setString(3, nv.getSoDienThoai());
            ps.setString(4, nv.getEmail());
            ps.setString(5, nv.getLoaiNV());
            ps.setString(6, nv.getMaNV());
            return ps.executeUpdate();
        }
    }
    
// ẩn nhân viên trong table
    public int deactivateById(String maNV) throws SQLException {
        String sql = """
            UPDATE TaiKhoan
               SET trangThai = 'inactive'
             WHERE maNV = ?
               AND LOWER(LTRIM(RTRIM(COALESCE(trangThai, '')))) <> 'inactive'
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate();
        }
    }
    
}
