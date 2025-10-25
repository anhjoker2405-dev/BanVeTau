package dao;

import connectDB.ConnectDB;
import entity.HanhKhach;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HanhKhach_Dao {

    // =================== CRUD cơ bản ===================

    public List<HanhKhach> findAll() throws SQLException {
        List<HanhKhach> list = new ArrayList<>();
        String sql = """
                SELECT hk.maHK, hk.tenHK, hk.soDienThoai, hk.cccd, hk.maGT, gt.tenGT
                FROM HanhKhach hk
                LEFT JOIN GioiTinh gt ON hk.maGT = gt.maGT
                ORDER BY hk.maHK
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public boolean existsById(String maHK) throws SQLException {
        String sql = "SELECT 1 FROM HanhKhach WHERE maHK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHK);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Phát sinh mã tiếp theo định dạng HK000001,... */
    public String nextId(Connection cn) throws SQLException {
        String sql = "SELECT MAX(maHK) FROM HanhKhach WHERE maHK LIKE 'HK%'";
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            String base = "HK000001";
            if (rs.next() && rs.getString(1) != null) {
                String last = rs.getString(1);               // HK000123
                int n = Integer.parseInt(last.substring(2)); // 123
                return "HK" + String.format("%06d", n + 1);
            }
            return base;
        }
    }

    public int insert(HanhKhach hk) throws SQLException {
        String sql = "INSERT INTO HanhKhach(maHK, tenHK, soDienThoai, cccd, maGT) VALUES(?,?,?,?,?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hk.getMaHK());
            ps.setString(2, hk.getTenHK());
            ps.setString(3, hk.getSoDienThoai());
            ps.setString(4, hk.getCccd());
            ps.setString(5, hk.getMaGT());
            return ps.executeUpdate();
        }
    }

    public int update(HanhKhach hk) throws SQLException {
        String sql = "UPDATE HanhKhach SET tenHK=?, soDienThoai=?, cccd=?, maGT=? WHERE maHK=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hk.getTenHK());
            ps.setString(2, hk.getSoDienThoai());
            ps.setString(3, hk.getCccd());
            ps.setString(4, hk.getMaGT());
            ps.setString(5, hk.getMaHK());
            return ps.executeUpdate();
        }
    }

    public int deleteById(String maHK) throws SQLException {
        String sql = "DELETE FROM HanhKhach WHERE maHK = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHK);
            return ps.executeUpdate();
        }
    }

    public List<HanhKhach> search(String keyword) throws SQLException {
        List<HanhKhach> list = new ArrayList<>();
        String sql = """
                SELECT hk.maHK, hk.tenHK, hk.soDienThoai, hk.cccd, hk.maGT, gt.tenGT
                FROM HanhKhach hk
                LEFT JOIN GioiTinh gt ON hk.maGT = gt.maGT
                WHERE hk.maHK LIKE ? OR hk.tenHK LIKE ? OR hk.soDienThoai LIKE ? OR hk.cccd LIKE ?
                ORDER BY hk.maHK
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String kw = "%" + keyword.trim() + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // ======= BỔ SUNG CHO BÁN VÉ =======

    /** Tồn tại CCCD? (dùng nội bộ trong cùng transaction) */
    public boolean existsByCCCD(Connection cn, String cccd) throws SQLException {
        String sql = "SELECT 1 FROM HanhKhach WHERE cccd = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Lấy maHK theo CCCD (ngoài transaction). */
    public String findMaHKByCCCD(String cccd) throws SQLException {
        String sql = "SELECT maHK FROM HanhKhach WHERE cccd = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, cccd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /**
     * Upsert theo CCCD: nếu đã có → trả về maHK hiện có; nếu chưa có → tạo mới và trả về maHK mới.
     * Toàn bộ chạy trong 1 transaction.
     */
    public String upsertAndGetMaHK(String tenHK, String sdt, String cccd, String maGT) throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            cn.setAutoCommit(false);
            try {
                // 1) Tìm theo CCCD
                String q = "SELECT maHK FROM HanhKhach WHERE cccd = ?";
                try (PreparedStatement ps = cn.prepareStatement(q)) {
                    ps.setString(1, cccd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String ma = rs.getString(1);
                            cn.commit();
                            return ma;
                        }
                    }
                }

                // 2) Chưa có → phát sinh mã & insert
                String maHKMoi = nextId(cn);
                String ins = "INSERT INTO HanhKhach(maHK, tenHK, soDienThoai, cccd, maGT) VALUES(?,?,?,?,?)";
                try (PreparedStatement ps = cn.prepareStatement(ins)) {
                    ps.setString(1, maHKMoi);
                    ps.setString(2, tenHK);
                    ps.setString(3, sdt);
                    ps.setString(4, cccd);
                    ps.setString(5, maGT);
                    ps.executeUpdate();
                }

                cn.commit();
                return maHKMoi;
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    /**
     * Upsert + cập nhật thông tin nếu đã tồn tại:
     * - Nếu tìm thấy theo CCCD: cập nhật tenHK/sdt/maGT (nếu truyền vào khác null & khác dữ liệu hiện tại).
     * - Nếu chưa có: chèn mới.
     */
    public String upsertAndGetMaHKWithUpdate(String tenHK, String sdt, String cccd, String maGT) throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String select = "SELECT maHK, tenHK, soDienThoai, maGT FROM HanhKhach WHERE cccd = ?";
                String maHK = null;
                String oldTen = null, oldSdt = null, oldMaGT = null;

                try (PreparedStatement ps = cn.prepareStatement(select)) {
                    ps.setString(1, cccd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            maHK   = rs.getString("maHK");
                            oldTen = rs.getString("tenHK");
                            oldSdt = rs.getString("soDienThoai");
                            oldMaGT= rs.getString("maGT");
                        }
                    }
                }

                if (maHK != null) {
                    boolean needUpdate = false;
                    String newTen = oldTen, newSdt = oldSdt, newMaGT = oldMaGT;

                    if (tenHK != null && !tenHK.isBlank() && !tenHK.equals(oldTen)) {
                        newTen = tenHK; needUpdate = true;
                    }
                    if (sdt != null && !sdt.isBlank() && !sdt.equals(oldSdt)) {
                        newSdt = sdt; needUpdate = true;
                    }
                    if (maGT != null && !maGT.isBlank() && !maGT.equals(oldMaGT)) {
                        newMaGT = maGT; needUpdate = true;
                    }

                    if (needUpdate) {
                        String upd = "UPDATE HanhKhach SET tenHK=?, soDienThoai=?, maGT=? WHERE maHK=?";
                        try (PreparedStatement ps = cn.prepareStatement(upd)) {
                            ps.setString(1, newTen);
                            ps.setString(2, newSdt);
                            ps.setString(3, newMaGT);
                            ps.setString(4, maHK);
                            ps.executeUpdate();
                        }
                    }

                    cn.commit();
                    return maHK;
                } else {
                    // chèn mới
                    String maHKMoi = nextId(cn);
                    String ins = "INSERT INTO HanhKhach(maHK, tenHK, soDienThoai, cccd, maGT) VALUES(?,?,?,?,?)";
                    try (PreparedStatement ps = cn.prepareStatement(ins)) {
                        ps.setString(1, maHKMoi);
                        ps.setString(2, tenHK);
                        ps.setString(3, sdt);
                        ps.setString(4, cccd);
                        ps.setString(5, maGT);
                        ps.executeUpdate();
                    }
                    cn.commit();
                    return maHKMoi;
                }
            } catch (Exception ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    /** Lấy map GioiTinh (maGT -> tenGT). */
    public Map<String, String> findAllGioiTinh() throws SQLException {
        Map<String, String> map = new LinkedHashMap<>();
        String sql = "SELECT maGT, tenGT FROM GioiTinh ORDER BY maGT";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("maGT"), rs.getString("tenGT"));
            }
        }
        return map;
    }

    /** Lấy maGT mặc định (dòng đầu tiên bảng GioiTinh), có thể trả null nếu bảng rỗng. */
    public String getDefaultMaGT() throws SQLException {
        String sql = "SELECT TOP 1 maGT FROM GioiTinh ORDER BY maGT";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    /**
     * Tiện ích: đảm bảo có maHK cho thanh toán.
     * - Nếu maGT null/blank sẽ tự lấy mặc định từ GioiTinh.
     * - Thực hiện upsert theo CCCD, có cập nhật nếu đã tồn tại.
     */
    public String ensureMaHK(String tenHK, String sdt, String cccd, String maGT) throws SQLException {
        String gt = (maGT == null || maGT.isBlank()) ? getDefaultMaGT() : maGT;
        return upsertAndGetMaHKWithUpdate(tenHK, sdt, cccd, gt);
    }

    public String generateNextId() throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            return nextId(cn);
        }
    }

    private HanhKhach mapRow(ResultSet rs) throws SQLException {
        return new HanhKhach(
                rs.getString("maHK"),
                rs.getString("tenHK"),
                rs.getString("soDienThoai"),
                rs.getString("cccd"),
                rs.getString("maGT"),
                rs.getString("tenGT")
        );
    }
    
    public String ensure(String tenHK, String sdt, String cccd) throws SQLException {
        // lấy giới tính mặc định nếu chưa chọn
        String maGT = getDefaultMaGT();            // đã có ở DAO (nếu chưa có, thêm ở dưới)
        // upsert + update nếu đã tồn tại CCCD
        return upsertAndGetMaHKWithUpdate(tenHK, sdt, cccd, maGT);
    }
}
