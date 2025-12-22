package dao;

import connectDB.ConnectDB;
import entity.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NhanVien_Dao {

    /** Tìm nhân viên theo username, chỉ lấy tài khoản đang kích hoạt để phục vụ đăng nhập. */
    public NhanVien findByUsername(String username) {
        String sql = """
            SELECT
                nv.maNV,
                nv.tenNV,
                nv.soDienThoai,
                nv.email,
                COALESCE(lnv.moTa, nv.maLoaiNV) AS loaiNV,
                COALESCE(ltk.moTa, tk.maLoaiTK) AS loaiTK
            FROM TaiKhoan tk
            JOIN NhanVien nv           ON nv.maNV     = tk.maNV
            LEFT JOIN LoaiNhanVien  lnv ON lnv.maLoaiNV = nv.maLoaiNV
            LEFT JOIN LoaiTaiKhoan  ltk ON ltk.maLoaiTK = tk.maLoaiTK
            WHERE tk.tenDangNhap = ?
              AND UPPER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'KICH_HOAT'
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String maNV  = rs.getString("maNV");
                String tenNV = rs.getString("tenNV");
                String sdt   = rs.getString("soDienThoai");
                String email = rs.getString("email");
                String loaiNV = rs.getString("loaiNV");
                String loaiTK = rs.getString("loaiTK");

                String role = (loaiNV != null && !loaiNV.isBlank()) ? loaiNV
                             : (loaiTK == null ? "" : loaiTK);

                if (role.toLowerCase().contains("quản")
                        || role.equalsIgnoreCase("admin")
                        || role.toLowerCase().contains("manager")) {
                    return new NhanVienQuanLy(maNV, tenNV, sdt, email);
                } else {
                    return new NhanVienBanVe(maNV, tenNV, sdt, email);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi NhanVien_Dao.findByUsername: " + e.getMessage(), e);
        }
    }

    // =================== CRUD phục vụ màn quản lý nhân viên ===================

    /**
     * Danh sách nhân viên hiển thị trên màn quản lý:
     * gồm NV chưa có tài khoản và tài khoản trạng thái KICH_HOAT.
     */
    // -- Lấy danh sách nhân viên hiển thị trên màn quản lý nhân viên --
    public List<NhanVienThongTin> findAll() throws SQLException {
        List<NhanVienThongTin> list = new ArrayList<>();
        String sql = """
            SELECT
                nv.maNV,
                nv.tenNV,
                nv.ngaySinh,
                nv.soDienThoai,
                nv.email,
                nv.cccd,
                nv.ngayBatDauLamViec,
                nv.maLoaiNV,
                lnv.moTa AS moTaLoaiNV
            FROM NhanVien nv
            LEFT JOIN TaiKhoan tk ON tk.maNV = nv.maNV
            LEFT JOIN LoaiNhanVien lnv ON lnv.maLoaiNV = nv.maLoaiNV
            WHERE tk.maNV IS NULL
               OR UPPER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'KICH_HOAT'
            ORDER BY nv.maNV
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDate dob = toLocalDate(rs.getDate("ngaySinh"));
                LocalDate startDate = toLocalDate(rs.getDate("ngayBatDauLamViec"));
                String maLoaiNV = rs.getString("maLoaiNV");
                String loaiNV = rs.getString("moTaLoaiNV");
                if (loaiNV == null || loaiNV.isBlank()) {
                    loaiNV = maLoaiNV;
                }
                NhanVienThongTin nv = new NhanVienThongTin(
                        rs.getString("maNV"),
                        rs.getString("tenNV"),
                        dob,
                        rs.getString("soDienThoai"),
                        rs.getString("email"),
                        loaiNV,
                        maLoaiNV,
                        rs.getString("cccd"),
                        startDate
                );
                list.add(nv);
            }
        }
        return list;
    }

    // -- Lấy danh sách loại nhân viên phục vụ combobox phân quyền --
    public List<LoaiNhanVien> findAllLoaiNhanVien() throws SQLException {
        List<LoaiNhanVien> list = new ArrayList<>();
        String sql = "SELECT maLoaiNV, moTa FROM LoaiNhanVien ORDER BY moTa";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ma = rs.getString("maLoaiNV");
                String moTa = rs.getString("moTa");
                list.add(new LoaiNhanVien(ma, moTa));
            }
        }
        return list;
    }

    // -- Kiểm tra nhân viên có tồn tại bằng mã --
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
    
    // -- Sinh mã nhân viên tiếp theo ở định dạng NVxxx --
    public String generateNextId() throws SQLException {
        try (Connection con = ConnectDB.getConnection()) {
            return nextId(con);
        }
    }
    
    // -- Sinh mã nhân viên mới bằng kết nối có sẵn (dùng trong transaction) --
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
                    try { n = Integer.parseInt(numberPart); }
                    catch (NumberFormatException ex) { n = 0; }
                    return "NV" + String.format("%03d", n + 1);
                }
            }
        }
        return "NV001";
//      return "NV" + String.format("%04d", n + 1);
    }
    
    // -- Thêm mới nhân viên vào cơ sở dữ liệu --
    public int insert(NhanVienThongTin nv) throws SQLException {
        String sql = """
            INSERT INTO NhanVien(maNV, tenNV, ngaySinh, soDienThoai, email, cccd, maLoaiNV, ngayBatDauLamViec)
            VALUES (?,?,?,?,?,?,?,?)
        """;
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
            ps.setString(6, nv.getCccd());
            String maLoaiNV = nv.getMaLoaiNV();
            if (maLoaiNV == null || maLoaiNV.isBlank()) {
                maLoaiNV = nv.getLoaiNV();
            }
            ps.setString(7, maLoaiNV);
            if (nv.getNgayBatDauLamViec() != null) {
                ps.setDate(8, Date.valueOf(nv.getNgayBatDauLamViec()));
            } else {
                ps.setNull(8, Types.DATE);
            }
            return ps.executeUpdate();
        }
    }
    
    // -- Cập nhật thông tin nhân viên hiện hữu --
    public int update(NhanVienThongTin nv) throws SQLException {
        String sql = """
            UPDATE NhanVien
               SET tenNV = ?,
                   ngaySinh = ?,
                   soDienThoai = ?,
                   email = ?,
                   cccd = ?,
                   maLoaiNV = ?
             WHERE maNV = ?
        """;
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
            ps.setString(5, nv.getCccd());
            String maLoaiNV = nv.getMaLoaiNV();
            if (maLoaiNV == null || maLoaiNV.isBlank()) {
                maLoaiNV = nv.getLoaiNV();
            }
            ps.setString(6, maLoaiNV);
            ps.setString(7, nv.getMaNV());
            return ps.executeUpdate();
        }
    }
    

    /** Đưa tài khoản NV về trạng thái VO_HIEU_HOA (nếu đang active). */
    // -- Vô hiệu hóa tài khoản nhân viên (ẩn khỏi hệ thống) --
    public int deactivateById(String maNV) throws SQLException {
        String sql = """
            UPDATE TaiKhoan
               SET trangThai = 'Vo_Hieu_Hoa'
             WHERE maNV = ?
               AND UPPER(LTRIM(RTRIM(COALESCE(trangThai, '')))) <> 'VO_HIEU_HOA'
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate();
        }
    }
    
    // -- Chuyển đổi java.sql.Date sang LocalDate --
    private LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    // =================== BỔ SUNG DÙNG CHO BÁN VÉ ===================

    /** Lấy maNV đầu tiên hợp lệ (chưa có TK hoặc TK KICH_HOAT). */
    // -- Lấy mã nhân viên đầu tiên còn hoạt động (dùng fallback cho bán vé) --
    public String findFirstActiveMaNV() throws SQLException {
        String sql = """
            SELECT TOP 1 nv.maNV
            FROM NhanVien nv
            LEFT JOIN TaiKhoan tk ON tk.maNV = nv.maNV
            WHERE tk.maNV IS NULL
               OR UPPER(LTRIM(RTRIM(COALESCE(tk.trangThai, '')))) = 'KICH_HOAT'
            ORDER BY nv.maNV
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString(1);
            return null;
        }
    }

    /**
     * Giải quyết maNV cho thanh toán:
     * - Nếu có username -> tìm NV đang KICH_HOAT.
     * - Nếu không thấy -> fallback maNV đầu tiên hợp lệ.
     */
    // -- Alias cho existsById để giữ API cũ --
    public String resolveMaNVForPayment(String username) throws SQLException {
        if (username != null && !username.isBlank()) {
            NhanVien nv = findByUsername(username);
            if (nv != null) return nv.getMaNV();
        }
        return findFirstActiveMaNV();
    }
    // -- Alias cho existsById để giữ API cũ --
    public boolean exists(String maNV) throws SQLException {
        return existsById(maNV);        // đã có sẵn trong DAO
    }
    // -- Lấy một mã nhân viên hợp lệ bất kỳ --
    public String getAnyActiveMaNV() throws SQLException {
        // nếu bạn đã có findFirstActiveMaNV() thì gọi lại
        return findFirstActiveMaNV();
    }
    // =================== TÌM KIẾM NHÂN VIÊN ===================
    // -- Tìm kiếm nhân viên theo nhiều tiêu chí cho màn quản lý --
    public List<NhanVienThongTin> search(String maNV, String tenNV, String sdt, String email, String cccd, String loaiNV) throws SQLException {
        List<NhanVienThongTin> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT nv.maNV, nv.tenNV, nv.soDienThoai, nv.email, nv.cccd, nv.maLoaiNV, lnv.moTa AS loaiNV, " +
            "nv.ngaySinh, nv.ngayBatDauLamViec " +
            "FROM NhanVien nv " +
            "LEFT JOIN LoaiNhanVien lnv ON lnv.maLoaiNV = nv.maLoaiNV " +
            "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (maNV != null && !maNV.isBlank()) {
            sql.append(" AND nv.maNV LIKE ?");
            params.add("%" + maNV + "%");
        }
        if (tenNV != null && !tenNV.isBlank()) {
            sql.append(" AND nv.tenNV LIKE ?");
            params.add("%" + tenNV + "%");
        }
        if (sdt != null && !sdt.isBlank()) {
            sql.append(" AND nv.soDienThoai LIKE ?");
            params.add("%" + sdt + "%");
        }
        if (email != null && !email.isBlank()) {
            sql.append(" AND nv.email LIKE ?");
            params.add("%" + email + "%");
        }
        if (cccd != null && !cccd.isBlank()) {
            sql.append(" AND nv.cccd LIKE ?");
            params.add("%" + cccd + "%");
        }
        if (loaiNV != null && !loaiNV.isBlank()) {
            sql.append(" AND (lnv.moTa LIKE ? OR nv.maLoaiNV LIKE ?)");
            params.add("%" + loaiNV + "%");
            params.add("%" + loaiNV + "%");
        }

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new NhanVienThongTin(
                            rs.getString("maNV"),
                            rs.getString("tenNV"),
                            rs.getDate("ngaySinh") == null ? null : rs.getDate("ngaySinh").toLocalDate(),
                            rs.getString("soDienThoai"),
                            rs.getString("email"),
                            rs.getString("loaiNV"),
                            rs.getString("maLoaiNV"),
                            rs.getString("cccd"),
                            rs.getDate("ngayBatDauLamViec") == null ? null : rs.getDate("ngayBatDauLamViec").toLocalDate()
                    ));
                }
            }
        }
        return list;
    }

    // -- Danh sách nhân viên chưa có tài khoản để hỗ trợ tạo tài khoản mới --
    public List<NhanVienThongTin> findNhanVienChuaCoTaiKhoan() throws SQLException {
        List<NhanVienThongTin> list = new ArrayList<>();
        String sql = """
            SELECT nv.maNV, nv.tenNV, nv.ngaySinh, nv.soDienThoai, nv.email,
                   nv.cccd, nv.ngayBatDauLamViec, nv.maLoaiNV,
                   COALESCE(lnv.moTa, nv.maLoaiNV) AS loaiNV
            FROM NhanVien nv
            LEFT JOIN TaiKhoan tk ON tk.maNV = nv.maNV
            LEFT JOIN LoaiNhanVien lnv ON lnv.maLoaiNV = nv.maLoaiNV
            WHERE tk.maNV IS NULL
            ORDER BY nv.tenNV
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new NhanVienThongTin(
                        rs.getString("maNV"),
                        rs.getString("tenNV"),
                        rs.getDate("ngaySinh") == null ? null : rs.getDate("ngaySinh").toLocalDate(),
                        rs.getString("soDienThoai"),
                        rs.getString("email"),
                        rs.getString("loaiNV"),
                        rs.getString("maLoaiNV"),
                        rs.getString("cccd"),
                        rs.getDate("ngayBatDauLamViec") == null ? null : rs.getDate("ngayBatDauLamViec").toLocalDate()
                ));
            }
        }
        return list;
    }
}