
package dao;

import connectDB.ConnectDB;

import java.math.BigDecimal;
import java.sql.*;

public class Ve_Dao {
    
    public static void refreshExpiredTickets(int graceMinutes) throws SQLException {
        try (Connection con = ConnectDB.getConnection();
             CallableStatement cs = con.prepareCall("{ call dbo.MarkTicketsExpired(?) }")
        ) {
            cs.setInt(1, graceMinutes);
            cs.executeUpdate();
        }
    }

    public boolean isSeatSold(Connection cn, String maChuyenTau, String maGhe) throws SQLException {
        String sql = "SELECT 1 FROM Ve WHERE maChuyenTau=? AND maGhe=? AND trangThai IN (N'Đã bán', N'Đã đổi')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            ps.setString(2, maGhe);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Map (chuyến, toa, số ghế) -> maGhe trong DB */
    public String resolveMaGhe(Connection cn, String maChuyenTau, int soToa, String soGhe) throws SQLException {
        final String sql = 
            "SELECT TOP 1 g.maGhe " +
            "FROM ChuyenTau ct " +
            "JOIN ToaTau tt   ON tt.maTau = ct.maTau " +
            "JOIN KhoangTau kt ON kt.maToa = tt.maToa " +
            "JOIN Ghe g       ON g.maKhoangTau = kt.maKhoangTau " +
            "WHERE ct.maChuyenTau = ? AND tt.soToa = ? AND g.soGhe = ? " +
            "ORDER BY g.maGhe";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            ps.setInt(2, soToa);
            ps.setString(3, soGhe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    public int insertVe(Connection cn, String maVe, BigDecimal giaVe, String maLoaiVe,
                        Timestamp ngayDat, String maGhe, String maChuyenTau, String maHK, String trangThai) throws SQLException {
        String status = trangThai != null ? trangThai : "Đã bán";
        String sql = "INSERT INTO Ve(maVe, giaVe, maLoaiVe, ngayDat, maGhe, maChuyenTau, maHK, trangThai) " +
                     "VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            ps.setBigDecimal(2, giaVe);
            ps.setString(3, maLoaiVe);
            ps.setTimestamp(4, ngayDat);
            ps.setString(5, maGhe);
            ps.setString(6, maChuyenTau);
            ps.setString(7, maHK);
            ps.setString(8, status);
            return ps.executeUpdate();
        }
    }

    public int updateTrangThai(Connection cn, String maVe, String trangThai) throws SQLException {
        String sql = "UPDATE Ve SET trangThai = ? WHERE maVe = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maVe);
            return ps.executeUpdate();
        }
    }
}
