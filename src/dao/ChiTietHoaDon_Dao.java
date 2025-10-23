
package dao;

import java.math.BigDecimal;
import java.sql.*;

public class ChiTietHoaDon_Dao {
    public int insertCT(Connection cn, String maHoaDon, String maVe, int soLuong, BigDecimal donGia) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon(maHoaDon, maVe, soLuongVe, donGia) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maVe);
            ps.setInt(3, soLuong);
            ps.setBigDecimal(4, donGia);
            return ps.executeUpdate();
        }
    }
}
