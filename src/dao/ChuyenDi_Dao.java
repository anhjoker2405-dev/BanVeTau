package dao;

import connectDB.ConnectDB;
import entity.ChuyenTau;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChuyenDi_Dao {
    
    private final SeatAvailabilityDao seatAvailabilityDao = new SeatAvailabilityDao();

    public List<String> getAllGaDi() throws SQLException {
        String sql = "SELECT tenGa FROM Ga ORDER BY tenGa";
        return loadStations(sql);
    }

    public List<String> getAllGaDen() throws SQLException {
        String sql = "SELECT tenGa FROM Ga ORDER BY tenGa";
        return loadStations(sql);
    }

    private List<String> loadStations(String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null && !value.isBlank()) list.add(value.trim());
            }
        }
        return list;
    }

    private static boolean isAll(String s) {
        if (s == null) return true;
        String norm = s.trim().toLowerCase();
        // chấp nhận vài biến thể phổ biến
        return norm.isBlank() || "tất cả".equals(norm) || "tat ca".equals(norm) || "all".equals(norm);
    }

    // Nếu chỉ có date (00:00), đẩy tới 23:59:59.997 để bao phủ cả ngày (SQL Server millisecond)
    private static Timestamp endOfDay(Timestamp ts) {
        if (ts == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 997);
        return new Timestamp(cal.getTimeInMillis());
    }

    public List<ChuyenTau> search(String maChuyen,
                                 String gaDi,
                                 String gaDen,
                                 Date khoiHanhTu,
                                 Date khoiHanhDen /* không còn dùng */) throws SQLException {

        StringBuilder where = new StringBuilder(" WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (maChuyen != null && !maChuyen.isBlank()) {
            where.append(" AND ct.maChuyenTau LIKE ?");
            params.add("%" + maChuyen.trim() + "%");
        }
        if (!isAll(gaDi)) {
            where.append(" AND gDi.tenGa = ?");
            params.add(gaDi.trim());
        }
        if (!isAll(gaDen)) {
            where.append(" AND gDen.tenGa = ?");
            params.add(gaDen.trim());
        }
        if (khoiHanhTu != null) {
            where.append(" AND ct.thoiGianKhoiHanh >= ?");
            params.add(new Timestamp(khoiHanhTu.getTime()));
        }
        
        String selectSql =
            "SELECT ct.maChuyenTau, gDi.tenGa AS gaDi, gDen.tenGa AS gaDen, " +
            "       ct.thoiGianKhoiHanh, ct.thoiGianKetThuc, ct.maTau, t.tenTau, " +
            "       CAST(ROUND(dbo.fn_KhoangCachKM(lt.maGaDi, lt.maGaDen) * lt.soTienMotKm, 0) AS decimal(12,0)) AS giaVe, " +
            "       ct.soGheTrong " +
            "FROM ChuyenTau ct " +
            "JOIN LichTrinh lt ON lt.maLichTrinh = ct.maLichTrinh " +
            "JOIN Ga gDi  ON gDi.maGa  = lt.maGaDi " +
            "JOIN Ga gDen ON gDen.maGa = lt.maGaDen " +
            "JOIN Tau t   ON t.maTau   = ct.maTau " +
            where +
            " ORDER BY ct.thoiGianKhoiHanh";

        List<ChuyenTau> out = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection()) {
            seatAvailabilityDao.refreshAll(con);
            try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new ChuyenTau(
                            rs.getString("maChuyenTau"),
                            rs.getString("gaDi"),
                            rs.getString("gaDen"),
                            rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime(),
                            rs.getTimestamp("thoiGianKetThuc").toLocalDateTime(),
                            rs.getString("maTau"),
                            rs.getString("tenTau"),
                            rs.getInt("soGheTrong"),
                            rs.getBigDecimal("giaVe")
                        ));
                    }
                }
            }
        }
        return out;
    }
}
