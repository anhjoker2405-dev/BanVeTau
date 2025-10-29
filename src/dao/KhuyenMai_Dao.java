package dao;

import connectDB.ConnectDB;
import entity.KhuyenMai;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMai_Dao {

    /** ====== SINH MÃ TỰ ĐỘNG: KM + yyyyMMdd + ### ====== */
    private String nextMaKhuyenMai(Connection conn) throws SQLException {
    // SQL Server: lấy max phần số sau "KM-"
    String sql = """
        SELECT MAX(TRY_CONVERT(INT, SUBSTRING(maKhuyenMai, 4, 50)))
        FROM KhuyenMai
        WHERE maKhuyenMai LIKE 'KM-%'
    """;
    int next = 1;
    try (PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        if (rs.next() && !rs.wasNull()) {
            next = rs.getInt(1) + 1;
        }
    }
    return String.format("KM-%03d", next);
}


    /** ====== LẤY DANH SÁCH ====== */
    public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT maKhuyenMai, tenKhuyenMai, giamGia, ngayBatDau, ngayKetThuc, moTa FROM KhuyenMai";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setGiamGia(rs.getBigDecimal("giamGia"));
                Timestamp nbd = rs.getTimestamp("ngayBatDau");
                Timestamp nkt = rs.getTimestamp("ngayKetThuc");
                km.setNgayBatDau(nbd == null ? null : nbd.toLocalDateTime());
                km.setNgayKetThuc(nkt == null ? null : nkt.toLocalDateTime());
                km.setMoTa(rs.getString("moTa"));
                dsKhuyenMai.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsKhuyenMai;
    }

    /** ====== THÊM (TỰ SINH MÃ NẾU CHƯA CÓ) ====== */
    public boolean addKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giamGia, ngayBatDau, ngayKetThuc, moTa) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Nếu mã rỗng -> tự sinh
            String ma = (km.getMaKhuyenMai() == null || km.getMaKhuyenMai().isBlank())
                    ? nextMaKhuyenMai(conn)
                    : km.getMaKhuyenMai();

            pstmt.setString(1, ma);
            pstmt.setString(2, km.getTenKhuyenMai());
            pstmt.setBigDecimal(3, km.getGiamGia() == null ? BigDecimal.ZERO : km.getGiamGia());
            pstmt.setTimestamp(4, km.getNgayBatDau() == null ? null : Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(5, km.getNgayKetThuc() == null ? null : Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(6, km.getMoTa());

            int rows = pstmt.executeUpdate();
            // cập nhật ngược vào đối tượng nếu cần dùng tiếp
            km.setMaKhuyenMai(ma);
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** ====== BẢN THÊM TRẢ RA MÃ VỪA TẠO (nếu bạn muốn dùng) ====== */
    public String addKhuyenMaiAuto(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giamGia, ngayBatDau, ngayKetThuc, moTa) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String ma = nextMaKhuyenMai(conn);
            pstmt.setString(1, ma);
            pstmt.setString(2, km.getTenKhuyenMai());
            pstmt.setBigDecimal(3, km.getGiamGia() == null ? BigDecimal.ZERO : km.getGiamGia());
            pstmt.setTimestamp(4, km.getNgayBatDau() == null ? null : Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(5, km.getNgayKetThuc() == null ? null : Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(6, km.getMoTa());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                km.setMaKhuyenMai(ma);
                return ma;
            }
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** ====== CẬP NHẬT ====== */
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, giamGia = ?, ngayBatDau = ?, " +
                     "ngayKetThuc = ?, moTa = ? WHERE maKhuyenMai = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, km.getTenKhuyenMai());
            pstmt.setBigDecimal(2, km.getGiamGia() == null ? BigDecimal.ZERO : km.getGiamGia());
            pstmt.setTimestamp(3, km.getNgayBatDau() == null ? null : Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(4, km.getNgayKetThuc() == null ? null : Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(5, km.getMoTa());
            pstmt.setString(6, km.getMaKhuyenMai());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** ====== XOÁ ====== */
    public boolean deleteKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maKM);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
