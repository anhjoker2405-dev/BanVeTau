package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.math.BigDecimal;

public class GiaVe_Dao {
    /** Lấy giá cơ sở theo mã chuyến tàu (từ LichTrinh) */
    public BigDecimal getGiaCoSoByMaChuyenTau(String maChuyenTau) throws SQLException {
        String sql = "SELECT CAST(ROUND(lt.soKMDiChuyen * lt.soTienMotKm, 0) AS decimal(12,0)) AS giaCoSo " +
                     "FROM ChuyenTau ct JOIN LichTrinh lt ON lt.maLichTrinh = ct.maLichTrinh " +
                     "WHERE ct.maChuyenTau = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaCoSo");
                }
                return null;
            }
        }
    }
    
     /**
     * Tính giá vé dựa trên khoảng cách giữa ga đi và ga đến sử dụng hàm dbo.fn_KhoangCachKM.
     * Công thức: khoảng_cách_km * đơn giá trên một km được cấu hình trong bảng LichTrinh.
     */
    public BigDecimal getGiaVeTheoKhoangCach(String maChuyenTau) throws SQLException {
        String sql = """
                SELECT CAST(ROUND(dbo.fn_KhoangCachKM(lt.maGaDi, lt.maGaDen) * lt.soTienMotKm, 0) AS decimal(12,0)) AS giaVe
                FROM ChuyenTau ct
                JOIN LichTrinh lt ON lt.maLichTrinh = ct.maLichTrinh
                WHERE ct.maChuyenTau = ?
                """;
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("giaVe");
                }
                return null;
            }
        }
    }
}
    