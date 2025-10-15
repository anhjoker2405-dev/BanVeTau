package dao;

import connectDB.ConnectDB;
import model.ChuyenDi;

//import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * tra cứu dữ liệu chuyến tàu.
 */
public class ChuyenDi_Dao {

    public List<String> getAllGaDi() throws SQLException {
        String sql = "SELECT DISTINCT gaDi FROM LichTrinh ORDER BY gaDi";
        return loadStations(sql);
    }

    public List<String> getAllGaDen() throws SQLException {
        String sql = "SELECT DISTINCT gaDen FROM LichTrinh ORDER BY gaDen";
        return loadStations(sql);
    }

    private List<String> loadStations(String sql) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null && !value.isBlank()) {
                    list.add(value.trim());
                }
            }
        }
        return list;
    }

    public List<ChuyenDi> search(String maChuyen,
                                 String gaDi,
                                 String gaDen,
                                 Date khoiHanhTu,
                                 Date khoiHanhDen) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT ct.maChuyenTau, lt.gaDi, lt.gaDen, " +
                "       ct.thoiGianKhoiHanh, ct.thoiGianKetThuc, " +
                "       t.tenTau, ct.soGheTrong " +            // <- có khoảng trắng cuối dòng
                "FROM ChuyenTau ct " +
                "JOIN LichTrinh lt ON ct.maLichTrinh = lt.maLichTrinh " +
                "LEFT JOIN Tau t ON ct.maTau = t.maTau " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (maChuyen != null && !maChuyen.isBlank()) {
            sql.append(" AND ct.maChuyenTau LIKE ?");
            params.add('%' + maChuyen.trim() + '%');
        }
        if (gaDi != null && !gaDi.isBlank() && !"Tất cả".equalsIgnoreCase(gaDi)) {
            sql.append(" AND lt.gaDi = ?");
            params.add(gaDi);
        }
        if (gaDen != null && !gaDen.isBlank() && !"Tất cả".equalsIgnoreCase(gaDen)) {
            sql.append(" AND lt.gaDen = ?");
            params.add(gaDen);
        }
        if (khoiHanhTu != null) {
            sql.append(" AND ct.thoiGianKhoiHanh >= ?");
            params.add(new Timestamp(khoiHanhTu.getTime()));
        }
        if (khoiHanhDen != null) {
            sql.append(" AND ct.thoiGianKhoiHanh <= ?");
            params.add(new Timestamp(khoiHanhDen.getTime()));
        }
        

        sql.append(" ORDER BY ct.thoiGianKhoiHanh");

        List<ChuyenDi> result = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ma = rs.getString("maChuyenTau");
                    String gaDiVal = rs.getString("gaDi");
                    String gaDenVal = rs.getString("gaDen");
                    Timestamp gioDi = rs.getTimestamp("thoiGianKhoiHanh");
                    Timestamp gioDen = rs.getTimestamp("thoiGianKetThuc");
                    String tenTau = rs.getString("tenTau");
                    int soGheTrong = rs.getInt("soGheTrong");

                    LocalDateTime khoiHanh = gioDi != null
                            ? LocalDateTime.ofInstant(gioDi.toInstant(), ZoneId.systemDefault())
                            : null;
                    LocalDateTime ketThuc = gioDen != null
                            ? LocalDateTime.ofInstant(gioDen.toInstant(), ZoneId.systemDefault())
                            : null;

                    result.add(new ChuyenDi(ma,
                            gaDiVal,
                            gaDenVal,
                            khoiHanh,
                            ketThuc,
                            tenTau,
                            soGheTrong
                            ));
                }
            }   
        }
        return result;
    }
}