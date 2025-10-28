package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Đồng bộ lại số ghế trống của bảng ChuyenTau dựa trên dữ liệu vé và sơ đồ ghế.
 */
public class SeatAvailabilityDao {

    private static final String REFRESH_BASE_SQL =
        "WITH seatTotals AS ( " +
        "    SELECT ct.maChuyenTau, COUNT(g.maGhe) AS totalSeats " +
        "    FROM ChuyenTau ct " +
        "    JOIN ToaTau tt   ON tt.maTau = ct.maTau " +
        "    JOIN KhoangTau kt ON kt.maToa = tt.maToa " +
        "    JOIN Ghe g        ON g.maKhoangTau = kt.maKhoangTau " +
        "    GROUP BY ct.maChuyenTau " +
        "), sold AS ( " +
        "    SELECT maChuyenTau, COUNT(*) AS soldCount " +
        "    FROM Ve " +
        "    WHERE trangThai IN (N'Đã bán', N'Đã đổi') " +
        "    GROUP BY maChuyenTau " +
        ") " +
        "UPDATE ct " +
        "SET soGheTrong = CASE " +
        "    WHEN COALESCE(st.totalSeats, 252) - COALESCE(s.soldCount, 0) < 0 THEN 0 " +
        "    ELSE COALESCE(st.totalSeats, 252) - COALESCE(s.soldCount, 0) " +
        "END " +
        "FROM ChuyenTau ct " +
        "LEFT JOIN seatTotals st ON st.maChuyenTau = ct.maChuyenTau " +
        "LEFT JOIN sold s ON s.maChuyenTau = ct.maChuyenTau";

    public void refreshAll() throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            refreshAll(cn);
        }
    }

    public void refreshAll(Connection cn) throws SQLException {
        executeRefresh(cn, null);
    }
    
    public void refreshForTrip(String maChuyenTau) throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            refreshForTrip(cn, maChuyenTau);
        }
    }
    
    public void refreshForTrip(Connection cn, String maChuyenTau) throws SQLException {
        executeRefresh(cn, maChuyenTau);
    }

    private void executeRefresh(Connection cn, String maChuyenTau) throws SQLException {
        String sql = REFRESH_BASE_SQL + (maChuyenTau != null ? " WHERE ct.maChuyenTau = ?" : "");
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (maChuyenTau != null) {
                ps.setString(1, maChuyenTau);
            }
            ps.executeUpdate();
        }
    }
}