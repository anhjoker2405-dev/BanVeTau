package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "TaiKhoan" đầy đủ CRUD cho màn Quản lý tài khoản.
 * Bảng: TaiKhoan(maTK, tenDangNhap, matKhau, maNV, maLoaiTK, trangThai)
 * Gợi ý thêm: LoaiTaiKhoan(maLoaiTK, tenLoaiTK)
 *
 * Lưu ý: Demo để plain password cho tương thích đăng nhập hiện có.
 * Khi triển khai thật nên hash password + salt (SHA-256/Bcrypt) và sửa authenticate().
 */
public class TaiKhoan_Dao {

    // ====== READ ======
    public List<TaiKhoan> findAll() throws SQLException {
        final String sql = """
            SELECT tk.maTK, tk.tenDangNhap, tk.matKhau, tk.maNV, tk.trangThai,
                   COALESCE(ltk.tenLoaiTK, tk.maLoaiTK) AS loaiTK
            FROM TaiKhoan tk
            LEFT JOIN LoaiTaiKhoan ltk ON ltk.maLoaiTK = tk.maLoaiTK
            ORDER BY tk.maTK
        """;
        List<TaiKhoan> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<TaiKhoan> search(String keyword) throws SQLException {
        final String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        final String sql = """
            SELECT tk.maTK, tk.tenDangNhap, tk.matKhau, tk.maNV, tk.trangThai,
                   COALESCE(ltk.tenLoaiTK, tk.maLoaiTK) AS loaiTK
            FROM TaiKhoan tk
            LEFT JOIN LoaiTaiKhoan ltk ON ltk.maLoaiTK = tk.maLoaiTK
            WHERE tk.tenDangNhap LIKE ? OR tk.maNV LIKE ?
            ORDER BY tk.maTK
        """;
        List<TaiKhoan> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean existsUsername(String username) throws SQLException {
        final String sql = "SELECT 1 FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ====== CREATE ======
    public TaiKhoan create(String tenDangNhap, String matKhau, String maNV, String maLoaiTK, boolean kichHoat) throws SQLException {
        if (existsUsername(tenDangNhap)) throw new SQLException("Tên đăng nhập đã tồn tại.");
        final String sql = """
            INSERT INTO TaiKhoan (tenDangNhap, matKhau, maNV, maLoaiTK, trangThai)
            VALUES (?, ?, ?, ?, ?);
            SELECT SCOPE_IDENTITY() AS newId;
        """;
        String status = kichHoat ? "Kich_Hoat" : "Vo_Hieu";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau);
            ps.setString(3, maNV);
            ps.setString(4, maLoaiTK);
            ps.setString(5, status);
            try (ResultSet rs = ps.executeQuery()) {
                String newId = null;
                if (rs.next()) newId = String.valueOf(rs.getObject("newId"));
                return new TaiKhoan(newId, tenDangNhap, matKhau, maNV, status, maLoaiTK);
            }
        }
    }

    // ====== UPDATE ======
    public int updateStatus(String maTK, boolean kichHoat) throws SQLException {
        final String sql = "UPDATE TaiKhoan SET trangThai = ? WHERE maTK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kichHoat ? "Kich_Hoat" : "Vo_Hieu");
            ps.setString(2, maTK);
            return ps.executeUpdate();
        }
    }

    public int updatePassword(String maTK, String newPassword) throws SQLException {
        final String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE maTK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, maTK);
            return ps.executeUpdate();
        }
    }

    public int updateRole(String maTK, String maLoaiTK) throws SQLException {
        final String sql = "UPDATE TaiKhoan SET maLoaiTK = ? WHERE maTK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLoaiTK);
            ps.setString(2, maTK);
            return ps.executeUpdate();
        }
    }

    // ====== DELETE ======
    public int delete(String maTK) throws SQLException {
        final String sql = "DELETE FROM TaiKhoan WHERE maTK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTK);
            return ps.executeUpdate();
        }
    }

    // ====== AUTH ======
    public TaiKhoan authenticate(String username, String password) {
        final String sql = """
            SELECT tk.maTK, tk.tenDangNhap, tk.matKhau, tk.maNV, tk.trangThai,
                   COALESCE(ltk.tenLoaiTK, tk.maLoaiTK) AS loaiTK
            FROM TaiKhoan tk
            LEFT JOIN LoaiTaiKhoan ltk ON ltk.maLoaiTK = tk.maLoaiTK
            WHERE LTRIM(RTRIM(LOWER(tk.tenDangNhap))) = LOWER(?)
              AND LOWER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'kich_hoat'
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("matKhau");
                    if (stored != null && stored.equals(password)) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi CSDL (authenticate): " + e.getMessage(), e);
        }
        return null;
    }

    private TaiKhoan mapRow(ResultSet rs) throws SQLException {
        return new TaiKhoan(
                String.valueOf(rs.getObject("maTK")),
                rs.getString("tenDangNhap"),
                rs.getString("matKhau"),
                rs.getString("maNV"),
                rs.getString("trangThai"),
                rs.getString("loaiTK")
        );
    }
}
