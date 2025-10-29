package dao;

import connectDB.ConnectDB;
import entity.TaiKhoan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Xử lý phân quyền dựa trên bảng LoaiTaiKhoan.
 *
 *  - LTK-01 (TK_QuanTri)   : toàn quyền, hiển thị toàn bộ chức năng.
 *  - LTK-02 (TK_NhanVien)  : ẩn các chức năng quản lý trong menu Danh mục.
 *
 * Có thể mở rộng thêm nếu sau này bổ sung loại tài khoản mới.
 */
public class RolePermissionDao {

    public RolePermission resolve(TaiKhoan account) {
        if (account == null) {
            return RolePermission.unknown();
        }

        RolePermission permission = null;
        String maLoai = trim(account.getMaLoaiTK());

        if (maLoai != null) {
            permission = fetchByRoleCode(maLoai);
        }

        if (permission == null) {
            permission = fetchByAccountId(trim(account.getMaTK()));
        }

        if (permission == null) {
            permission = new RolePermission(maLoai, account.getTenLoaiTK(), null, null);
        }

        return permission != null ? permission : RolePermission.unknown();
    }

    private RolePermission fetchByRoleCode(String maLoai) {
        if (maLoai == null) {
            return null;
        }
        final String sql = "SELECT maLoaiTK, tenLoaiTK, moTa, quyenHan FROM LoaiTaiKhoan WHERE maLoaiTK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không lấy được thông tin loại tài khoản: " + e.getMessage(), e);
        }
        return null;
    }

    private RolePermission fetchByAccountId(String maTk) {
        if (maTk == null) {
            return null;
        }
        final String sql = """
            ELECT tk.maLoaiTK, ltk.tenLoaiTK, ltk.moTa, ltk.quyenHan
            FROM TaiKhoan tk
            LEFT JOIN LoaiTaiKhoan ltk ON ltk.maLoaiTK = tk.maLoaiTK
            WHERE tk.maTK = ?
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maTk);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không lấy được phân quyền tài khoản: " + e.getMessage(), e);
        }
        return null;
    }

    private RolePermission map(ResultSet rs) throws SQLException {
        return new RolePermission(
                trim(rs.getString("maLoaiTK")),
                trim(rs.getString("tenLoaiTK")),
                trim(rs.getString("moTa")),
                trim(rs.getString("quyenHan"))
        );
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * DTO đơn giản chứa thông tin phân quyền.
     */
    public static class RolePermission {
        private final String maLoaiTK;
        private final String tenLoaiTK;
        private final String moTa;
        private final String quyenHan;

        private RolePermission(String maLoaiTK, String tenLoaiTK, String moTa, String quyenHan) {
            this.maLoaiTK = maLoaiTK;
            this.tenLoaiTK = tenLoaiTK;
            this.moTa = moTa;
            this.quyenHan = quyenHan;
        }

        public static RolePermission unknown() {
            return new RolePermission(null, null, null, null);
        }

        public String getMaLoaiTK() {
            return maLoaiTK;
        }

        public String getTenLoaiTK() {
            return tenLoaiTK;
        }
        
        public String getMoTa() {
            return moTa;
        }

        public String getQuyenHan() {
            return quyenHan;
        }

        public boolean isAdmin() {
            return "LTK-01".equalsIgnoreCase(maLoaiTK);
        }

        public boolean hideManagementMenus() {
            if (maLoaiTK == null) {
                // Nếu không xác định rõ loại thì chọn phương án an toàn: ẩn nhóm quản lý.
                return true;
            }
            if ("LTK-02".equalsIgnoreCase(maLoaiTK)) {
                return true;
            }
            return false;
        }
    }
}