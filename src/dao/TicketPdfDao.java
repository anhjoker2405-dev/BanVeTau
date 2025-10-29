package dao;

import connectDB.ConnectDB;
import entity.TicketPdfInfo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Lấy dữ liệu đầy đủ của vé để in PDF.
 */
public class TicketPdfDao {

    private static final String SQL =
            "SELECT v.maVe, " +
            "       gd.tenGa   AS tenGaDi, " +
            "       ga.tenGa   AS tenGaDen, " +
            "       ct.maTau, " +
            "       tau.tenTau, " +
            "       tt.soToa, " +
            "       kt.tenKhoangTau, " +
            "       g.soGhe, " +
            "       g.maLoaiGhe, " +
            "       lg.tenLoaiGhe, " +
            "       v.maLoaiVe, " +
            "       lv.tenLoaiVe, " +
            "       hk.tenHK, " +
            "       hk.cccd, " +
            "       v.giaVe, " +
            "       ct.thoiGianKhoiHanh " +
            "FROM Ve v " +
            "JOIN ChuyenTau ct   ON v.maChuyenTau = ct.maChuyenTau " +
            "JOIN LichTrinh lt   ON ct.maLichTrinh = lt.maLichTrinh " +
            "JOIN Ga gd          ON lt.maGaDi = gd.maGa " +
            "JOIN Ga ga          ON lt.maGaDen = ga.maGa " +
            "JOIN Ghe g          ON v.maGhe = g.maGhe " +
            "JOIN KhoangTau kt   ON g.maKhoangTau = kt.maKhoangTau " +
            "JOIN ToaTau tt      ON kt.maToa = tt.maToa " +
            "JOIN Tau tau        ON tt.maTau = tau.maTau " +
            "JOIN HanhKhach hk   ON v.maHK = hk.maHK " +
            "JOIN LoaiVe lv      ON v.maLoaiVe = lv.maLoaiVe " +
            "LEFT JOIN LoaiGhe lg ON g.maLoaiGhe = lg.maLoaiGhe " +
            "WHERE v.maVe = ?";

    public Optional<TicketPdfInfo> findByMaVe(String maVe) throws SQLException {
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(SQL)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        }
        return Optional.empty();
    }

    private TicketPdfInfo map(ResultSet rs) throws SQLException {
        String maVe = rs.getString("maVe");
        String gaDi = rs.getString("tenGaDi");
        String gaDen = rs.getString("tenGaDen");
        String maTau = rs.getString("maTau");
        String tenTau = rs.getString("tenTau");
        int soToa = rs.getInt("soToa");
        String tenKhoang = rs.getString("tenKhoangTau");
        String soGhe = rs.getString("soGhe");
        String maLoaiGhe = rs.getString("maLoaiGhe");
        String tenLoaiGhe = rs.getString("tenLoaiGhe");
        String maLoaiVe = rs.getString("maLoaiVe");
        String tenLoaiVe = rs.getString("tenLoaiVe");
        String tenHK = rs.getString("tenHK");
        String cccd = rs.getString("cccd");
        BigDecimal giaVe = rs.getBigDecimal("giaVe");
        Timestamp tsKhoiHanh = rs.getTimestamp("thoiGianKhoiHanh");
        LocalDateTime khoiHanh = tsKhoiHanh != null ? tsKhoiHanh.toLocalDateTime() : null;

        return new TicketPdfInfo(
                maVe,
                gaDi,
                gaDen,
                maTau,
                tenTau,
                soToa,
                tenKhoang,
                soGhe,
                maLoaiGhe,
                tenLoaiGhe,
                maLoaiVe,
                tenLoaiVe,
                tenHK,
                cccd,
                giaVe,
                khoiHanh
        );
    }
}