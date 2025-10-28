package dao;

import connectDB.ConnectDB;
import entity.TicketExchangeInfo;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * DAO phục vụ chức năng tra cứu vé cũ khi đổi vé.
 */
public class DoiVe_Dao {

    public TicketExchangeInfo findByMaVe(String maVe) throws SQLException {
        try {
            return queryTicket(maVe, true);
        } catch (SQLException ex) {
            if (isColumnMissing(ex, "namsinh")) {
                return queryTicket(maVe, false);
            }
            throw ex;
        }
    }

    private TicketExchangeInfo queryTicket(String maVe, boolean includeNamSinh) throws SQLException {
        String sql = buildSql(includeNamSinh);
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maVe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, includeNamSinh);
                }
            }
        }
        return null;
    }

    private TicketExchangeInfo mapRow(ResultSet rs, boolean includeNamSinh) throws SQLException {
        String namSinh = null;
        if (includeNamSinh) {
            try {
                namSinh = rs.getString("namSinh");
            } catch (SQLException ignore) {
                namSinh = null;
            }
        }
        return new TicketExchangeInfo(
                rs.getString("maVe"),
                rs.getString("tenHK"),
                rs.getString("soDienThoai"),
                namSinh,
                rs.getString("cccd"),
                rs.getString("maHK"),
                rs.getString("maGT"),
                rs.getString("tenGT"),
                rs.getString("maLoaiVe"),
                rs.getString("tenLoaiVe"),
                rs.getString("tenLoaiGhe"),
                rs.getString("soGhe"),
                rs.getInt("soToa"),
                rs.getString("tenKhoangTau"),
                rs.getBigDecimal("giaVe"),
                rs.getString("maChuyenTau"),
                rs.getString("maTau"),
                rs.getString("tenTau"),
                rs.getString("gaDi"),
                rs.getString("gaDen"),
                toLocalDateTime(rs.getTimestamp("thoiGianKhoiHanh")),
                toLocalDateTime(rs.getTimestamp("thoiGianKetThuc")),
                rs.getString("trangThai"),
                rs.getString("maGhe")
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    private String buildSql(boolean includeNamSinh) {
        String namSinhColumn = includeNamSinh ? "hk.namSinh," : "";
        return "SELECT v.maVe, v.giaVe, v.maLoaiVe, v.ngayDat, v.maGhe, v.maChuyenTau, v.maHK, v.trangThai, " +
                "hk.tenHK, hk.soDienThoai, hk.cccd, hk.maGT, gt.tenGT, " +
                namSinhColumn +
                "ct.thoiGianKhoiHanh, ct.thoiGianKetThuc, ct.maTau, tau.tenTau, " +
                "gaDi.tenGa AS gaDi, gaDen.tenGa AS gaDen, " +
                "tt.soToa, kt.tenKhoangTau, g.soGhe, lg.tenLoaiGhe, lv.tenLoaiVe " +
                "FROM Ve v " +
                "JOIN HanhKhach hk ON hk.maHK = v.maHK " +
                "LEFT JOIN GioiTinh gt ON gt.maGT = hk.maGT " +
                "JOIN ChuyenTau ct ON ct.maChuyenTau = v.maChuyenTau " +
                "JOIN Tau tau ON tau.maTau = ct.maTau " +
                "JOIN LichTrinh lt ON lt.maLichTrinh = ct.maLichTrinh " +
                "JOIN Ga gaDi ON gaDi.maGa = lt.maGaDi " +
                "JOIN Ga gaDen ON gaDen.maGa = lt.maGaDen " +
                "JOIN Ghe g ON g.maGhe = v.maGhe " +
                "JOIN KhoangTau kt ON kt.maKhoangTau = g.maKhoangTau " +
                "JOIN ToaTau tt ON tt.maToa = kt.maToa " +
                "JOIN LoaiGhe lg ON lg.maLoaiGhe = g.maLoaiGhe " +
                "JOIN LoaiVe lv ON lv.maLoaiVe = v.maLoaiVe " +
                "WHERE v.maVe = ?";
    }

    private boolean isColumnMissing(SQLException ex, String columnNameLowerCase) {
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        return message.toLowerCase().contains("invalid column") && message.toLowerCase().contains(columnNameLowerCase);
    }
}

