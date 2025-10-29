package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO "TaiKhoan" đầy đủ CRUD cho màn Quản lý tài khoản.
 * Bảng: TaiKhoan(maTK, tenDangNhap, matKhau, maNV, maLoaiTK, trangThai)
 * LoaiTaiKhoan(maLoaiTK, tenLoaiTK, moTa, quyenHan)
 *
 * Lưu ý: Demo để plain password cho tương thích đăng nhập hiện có.
 * Khi triển khai thật nên hash password + salt (SHA-256/Bcrypt) và sửa authenticate().
 */
public class TaiKhoan_Dao {

    // ====== READ ======
    public List<TaiKhoan> findAll() throws SQLException {
        final String sql = """
            SELECT tk.maTK, tk.tenDangNhap, tk.matKhau, tk.maNV, tk.trangThai,
                   tk.maLoaiTK, ltk.tenLoaiTK
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
                   tk.maLoaiTK, ltk.tenLoaiTK
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

    // ===== Helper: sinh maTK TK-001, TK-002... (gọi trong transaction) =====
    private String generateNextId(Connection con) throws SQLException {
        final String sql = """
            SELECT 'TK-' + RIGHT('000' + CAST(
                     ISNULL(MAX(TRY_CAST(REPLACE(maTK,'TK-','') AS INT)), 0) + 1
                   AS VARCHAR(3)), 3)
            FROM TaiKhoan WITH (UPDLOCK, HOLDLOCK)
        """;
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
            return "TK-001";
        }
    }

    // ===== Helper: chuẩn hoá maLoaiTK (nhập mã hoặc tên đều OK) =====
    private String normalizeMaLoaiTK(Connection con, String input) throws SQLException {
        if (input == null || input.isBlank()) {
            throw new SQLException("Loại tài khoản không được trống.");
        }
        final String sql = """
            SELECT TOP 1 maLoaiTK
            FROM LoaiTaiKhoan
            WHERE maLoaiTK = ? OR LTRIM(RTRIM(LOWER(tenLoaiTK))) = LOWER(?)
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String s = input.trim();
            ps.setString(1, s);
            ps.setString(2, s);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        throw new SQLException("Loại tài khoản không hợp lệ: " + input);
    }

    // ====== CREATE ======
    public TaiKhoan create(String tenDangNhap, String matKhau, String maNV, String maLoaiTK, boolean kichHoat) throws SQLException {
        if (existsUsername(tenDangNhap)) throw new SQLException("Tên đăng nhập đã tồn tại.");

        final String insert = "INSERT INTO TaiKhoan (maTK, tenDangNhap, matKhau, maNV, maLoaiTK, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        final String status = kichHoat ? "Kich_Hoat" : "Vo_Hieu";

        try (Connection con = ConnectDB.getConnection()) {
            con.setAutoCommit(false);
            try {
                String maTKNew = generateNextId(con);
                String maLoaiMapped = normalizeMaLoaiTK(con, maLoaiTK);

                try (PreparedStatement ps = con.prepareStatement(insert)) {
                    ps.setString(1, maTKNew);
                    ps.setString(2, tenDangNhap);
                    ps.setString(3, matKhau);
                    ps.setString(4, maNV);
                    ps.setString(5, maLoaiMapped);
                    ps.setString(6, status);
                    ps.executeUpdate();
                }

                con.commit();
                String tenLoai = fetchTenLoai(con, maLoaiMapped);
                return new TaiKhoan(maTKNew, tenDangNhap, matKhau, maNV, status, maLoaiMapped, tenLoai);
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
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
        try (Connection con = ConnectDB.getConnection()) {
            String mapped = normalizeMaLoaiTK(con, maLoaiTK);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, mapped);
                ps.setString(2, maTK);
                return ps.executeUpdate();
            }
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
                   tk.maLoaiTK, ltk.tenLoaiTK
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
                rs.getString("maLoaiTK"),
                rs.getString("tenLoaiTK")
        );
    }
    private String fetchTenLoai(Connection con, String maLoaiTK) throws SQLException {
        if (maLoaiTK == null) {
            return null;
        }
        final String sql = "SELECT tenLoaiTK FROM LoaiTaiKhoan WHERE maLoaiTK = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLoaiTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }
}
