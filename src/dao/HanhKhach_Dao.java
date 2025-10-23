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
    
    public String generateNextId() throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            return nextId(cn);
        }
    }

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
}
