
package dao;

import java.math.BigDecimal;
import java.sql.*;

public class HoaDon_Dao {

    /** Tạo hóa đơn mới, trả về maHoaDon đã tạo */
    public String createHoaDon(Connection cn, String maNV, String maHK, BigDecimal vat, String maKM) throws SQLException {
        String maHD = "HD" + System.currentTimeMillis();
        String sql = "INSERT INTO HoaDon(maHoaDon, ngayLapHoaDon, VAT, maNV, maHK, maKhuyenMai) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(3, vat == null ? new BigDecimal("0.00") : vat);
            ps.setString(4, maNV);
            ps.setString(5, maHK);
            ps.setString(6, maKM);
            ps.executeUpdate();
            return maHD;
        }
    }
}
