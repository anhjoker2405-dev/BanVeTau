package dao;

import connectDB.ConnectDB;
import entity.MonthlyRevenue;
import entity.TripCount;
import entity.SeatTypeStat;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO cho các thống kê tổng hợp */
public class ThongKe_Dao {

    /** Lấy doanh thu theo tháng. Nếu year == null => lấy tất cả các năm có dữ liệu. */
    public List<MonthlyRevenue> getRevenueByMonth(Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT YEAR(hd.ngayLapHoaDon) AS y, MONTH(hd.ngayLapHoaDon) AS m, ");
        sql.append("       SUM(ct.soLuongVe * ct.donGia) AS revenue ");
        sql.append("FROM HoaDon hd ");
        sql.append("JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon ");
        if (year != null) {
            sql.append("WHERE YEAR(hd.ngayLapHoaDon) = ? ");
        }
        sql.append("GROUP BY YEAR(hd.ngayLapHoaDon), MONTH(hd.ngayLapHoaDon) ");
        sql.append("ORDER BY y, m");

        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            if (year != null) ps.setInt(1, year);

            List<MonthlyRevenue> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int y = rs.getInt("y");
                    int m = rs.getInt("m");
                    BigDecimal rev = rs.getBigDecimal("revenue");
                    out.add(new MonthlyRevenue(y, m, rev));
                }
            }
            return out;
        }
    }

    /** Top chuyến tàu có số vé nhiều nhất (lọc theo năm nếu truyền). */
    public List<TripCount> getTopTrips(Integer year, int limit) throws SQLException {
    int lim = limit <= 0 ? 5 : limit;

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");
    sql.append("       CONCAT(gDi.tenGa, N' đến ', gDen.tenGa) AS tuyen, ");
    sql.append("       COUNT(v.maVe) AS soVe ");
    sql.append("FROM Ve v ");
    sql.append("JOIN ChuyenTau ct ON ct.maChuyenTau = v.maChuyenTau ");
    sql.append("JOIN LichTrinh lt ON lt.maLichTrinh = ct.maLichTrinh ");
    sql.append("JOIN Ga gDi ON gDi.maGa = lt.maGaDi ");
    sql.append("JOIN Ga gDen ON gDen.maGa = lt.maGaDen ");
    sql.append("WHERE (v.trangThai IS NULL OR v.trangThai <> N'Đã hủy') ");
    if (year != null) sql.append("AND YEAR(v.ngayDat) = ? ");
    sql.append("GROUP BY gDi.tenGa, gDen.tenGa ");
    sql.append("ORDER BY soVe DESC ");
    String finalSql = sql.toString() + "OFFSET 0 ROWS FETCH NEXT " + lim + " ROWS ONLY";

    try (Connection cn = ConnectDB.getConnection();
         PreparedStatement ps = cn.prepareStatement(finalSql)) {
        int i = 1;
        if (year != null) ps.setInt(i++, year);
        List<TripCount> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new TripCount(
                    null,                               // không còn dùng mã chuyến
                    rs.getString("tuyen"),
                    rs.getInt("soVe")
                ));
            }
        }
        return out;
    }
}

    /** Thống kê số vé & doanh thu theo loại ghế (lọc theo năm nếu truyền). */
    public List<SeatTypeStat> getSeatTypeStats(Integer year) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT lg.tenLoaiGhe, COUNT(*) AS soVe, SUM(v.giaVe) AS doanhThu ");
        sql.append("FROM Ve v ");
        sql.append("JOIN Ghe g      ON g.maGhe = v.maGhe ");
        sql.append("JOIN LoaiGhe lg ON lg.maLoaiGhe = g.maLoaiGhe ");
        sql.append("WHERE (v.trangThai IS NULL OR v.trangThai <> N'Đã hủy') ");
        if (year != null) {
            sql.append("AND YEAR(v.ngayDat) = ? ");
        }
        sql.append("GROUP BY lg.tenLoaiGhe ");
        sql.append("ORDER BY soVe DESC");

        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            if (year != null) ps.setInt(1, year);

            List<SeatTypeStat> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new SeatTypeStat(
                            rs.getString("tenLoaiGhe"),
                            rs.getInt("soVe"),
                            rs.getBigDecimal("doanhThu")
                    ));
                }
            }
            return out;
        }
    }
}
