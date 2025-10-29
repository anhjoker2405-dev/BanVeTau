package dao;

import connectDB.ConnectDB;
import entity.InvoicePdfInfo;
import entity.InvoicePdfItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Truy vấn dữ liệu hóa đơn để in PDF.
 */
public class HoaDonPdfDao {

    private static final String SQL_HEADER =
            "SELECT hd.maHoaDon, hd.ngayLapHoaDon, hd.VAT, " +
            "       nv.tenNV, nv.soDienThoai AS soDienThoaiNV, " +
            "       hk.tenHK, hk.soDienThoai AS soDienThoaiHK " +
            "FROM HoaDon hd " +
            "JOIN NhanVien nv ON nv.maNV = hd.maNV " +
            "JOIN HanhKhach hk ON hk.maHK = hd.maHK " +
            "WHERE hd.maHoaDon = ?";

    private static final String SQL_ITEMS =
            "SELECT ct.maVe, ct.soLuongVe, ct.donGia, " +
            "       CAST(ROUND(dbo.fn_KhoangCachKM(lt.maGaDi, lt.maGaDen) * lt.soTienMotKm, 0) AS decimal(18,0)) AS giaCoSo, " +
            "       tau.maTau, tau.tenTau, " +
            "       tt.soToa, g.soGhe, kt.tenKhoangTau, " +
            "       gd.tenGa AS tenGaDi, ga.tenGa AS tenGaDen " +
            "FROM ChiTietHoaDon ct " +
            "JOIN Ve v            ON ct.maVe = v.maVe " +
            "JOIN ChuyenTau ch    ON v.maChuyenTau = ch.maChuyenTau " +
            "JOIN LichTrinh lt    ON ch.maLichTrinh = lt.maLichTrinh " +
            "JOIN Ga gd           ON lt.maGaDi = gd.maGa " +
            "JOIN Ga ga           ON lt.maGaDen = ga.maGa " +
            "JOIN Ghe g           ON v.maGhe = g.maGhe " +
            "JOIN KhoangTau kt    ON g.maKhoangTau = kt.maKhoangTau " +
            "JOIN ToaTau tt       ON kt.maToa = tt.maToa " +
            "JOIN Tau tau         ON tt.maTau = tau.maTau " +
            "WHERE ct.maHoaDon = ? " +
            "ORDER BY ct.maVe";

    public Optional<InvoicePdfInfo> findByMaHoaDon(String maHoaDon) throws SQLException {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return Optional.empty();
        }

        try (Connection cn = ConnectDB.getConnection()) {
            InvoicePdfInfo info = null;

            try (PreparedStatement ps = cn.prepareStatement(SQL_HEADER)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        info = new InvoicePdfInfo();
                        info.setMaHoaDon(rs.getString("maHoaDon"));

                        Timestamp ts = rs.getTimestamp("ngayLapHoaDon");
                        if (ts != null) {
                            LocalDate date = ts.toLocalDateTime().toLocalDate();
                            info.setNgayLap(date);
                        }

                        BigDecimal vatPercent = rs.getBigDecimal("VAT");
                        if (vatPercent != null) {
                            BigDecimal vatRate = vatPercent.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
                            info.setVatRatePercent(vatPercent);
                            info.setVatRate(vatRate);
                        } else {
                            info.setVatRate(BigDecimal.ZERO);
                            info.setVatRatePercent(BigDecimal.ZERO);
                        }

                        info.setNhanVienLap(rs.getString("tenNV"));
                        info.setDienThoaiNhanVien(rs.getString("soDienThoaiNV"));
                        info.setTenKhachHang(rs.getString("tenHK"));
                        info.setDienThoaiKhachHang(rs.getString("soDienThoaiHK"));
                    } else {
                        return Optional.empty();
                    }
                }
            }

            if (info == null) {
                return Optional.empty();
            }

            try (PreparedStatement ps = cn.prepareStatement(SQL_ITEMS)) {
                ps.setString(1, maHoaDon);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        info.addItem(mapItem(rs, info.getVatRate()));
                    }
                }
            }

            return Optional.of(info);
        }
    }

    private InvoicePdfItem mapItem(ResultSet rs, BigDecimal vatRate) throws SQLException {
        String maVe = rs.getString("maVe");
        int soLuong = rs.getInt("soLuongVe");
        if (soLuong <= 0) {
            soLuong = 1;
        }
        BigDecimal donGia = sanitizeMoney(rs.getBigDecimal("donGia"));
        BigDecimal giaCoSoRaw = rs.getBigDecimal("giaCoSo");
        BigDecimal giaCoSo = giaCoSoRaw != null ? sanitizeMoney(giaCoSoRaw) : null;

        BigDecimal qty = BigDecimal.valueOf(soLuong);
        BigDecimal thanhTienCoThue = donGia.multiply(qty);

        BigDecimal donGiaChuaThue = resolveDonGiaChuaThue(donGia, giaCoSo, vatRate);
        BigDecimal thanhTienChuaThue = donGiaChuaThue.multiply(qty);
        if (thanhTienChuaThue.compareTo(thanhTienCoThue) > 0 && thanhTienCoThue.signum() >= 0) {
            thanhTienChuaThue = thanhTienCoThue;
            donGiaChuaThue = thanhTienChuaThue.divide(qty, 0, RoundingMode.HALF_UP);
        }
        if (thanhTienChuaThue.compareTo(BigDecimal.ZERO) < 0) {
            thanhTienChuaThue = BigDecimal.ZERO;
            donGiaChuaThue = BigDecimal.ZERO;
        }

        BigDecimal thue = thanhTienCoThue.subtract(thanhTienChuaThue);
        if (thue.compareTo(BigDecimal.ZERO) < 0) {
            thue = BigDecimal.ZERO;
        }

        String tenDichVu = buildServiceDescription(rs);

        return new InvoicePdfItem(maVe, tenDichVu, soLuong,
                donGiaChuaThue, thanhTienChuaThue, thue, thanhTienCoThue);
    }
    
    private BigDecimal resolveDonGiaChuaThue(BigDecimal donGiaCoThue, BigDecimal giaCoSo, BigDecimal vatRate) {
        if (giaCoSo != null && giaCoSo.compareTo(BigDecimal.ZERO) > 0) {
            return giaCoSo;
        }
        if (vatRate != null && vatRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal divisor = BigDecimal.ONE.add(vatRate);
            if (divisor.compareTo(BigDecimal.ZERO) != 0) {
                return donGiaCoThue.divide(divisor, 0, RoundingMode.HALF_UP);
            }
        }
        return donGiaCoThue;
    }

    private BigDecimal sanitizeMoney(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    private String buildServiceDescription(ResultSet rs) throws SQLException {
        String chuyenTauCode = safe(getStringIfExists(rs, "maChuyenTauDisplay"));
        if (chuyenTauCode.isBlank()) {
            chuyenTauCode = safe(getStringIfExists(rs, "maChuyenTau"));
        }
        String maTau = safe(rs.getString("maTau"));
        String tenTau = safe(rs.getString("tenTau"));
        String tauDisplay = !tenTau.isBlank() ? tenTau : (!maTau.isBlank() ? maTau : chuyenTauCode);

        String gaDi = safe(rs.getString("tenGaDi"));
        String gaDen = safe(rs.getString("tenGaDen"));

        int soToa = rs.getInt("soToa");
        if (rs.wasNull()) {
            soToa = 0;
        }
        String soGhe = safe(rs.getString("soGhe"));
        String tenKhoang = safe(rs.getString("tenKhoangTau"));

        StringBuilder seat = new StringBuilder();
        if (soToa > 0) {
            seat.append("Toa ").append(String.format("%02d", soToa));
        }
        if (!soGhe.isBlank()) {
            if (seat.length() > 0) {
                seat.append(" / ");
            }
            seat.append("Ghế ").append(soGhe);
        }
        if (!tenKhoang.isBlank()) {
            if (seat.length() > 0) {
                seat.append(' ');
            }
            seat.append(tenKhoang);
        }

        StringBuilder service = new StringBuilder();
        if (!tauDisplay.isBlank()) {
            service.append(tauDisplay).append(' ');
        } else if (!chuyenTauCode.isBlank()) {
            service.append(chuyenTauCode).append(' ');
        }
        service.append(gaDi);
        if (!gaDi.isBlank() && !gaDen.isBlank()) {
            service.append(" → ");
        } else if (!gaDen.isBlank()) {
            service.append(' ');
        }
        service.append(gaDen);

        if (seat.length() > 0) {
            service.append(" (").append(seat).append(')');
        }

        return service.toString().trim();
    }

    private String safe(String value) {
        return value != null ? value.trim() : "";
    }
    private String getStringIfExists(ResultSet rs, String columnLabel) throws SQLException {
        try {
            return rs.getString(columnLabel);
        } catch (SQLException ex) {
            if (isMissingColumn(ex)) {
                return null;
            }
            throw ex;
        }
    }

    private boolean isMissingColumn(SQLException ex) {
        String state = ex.getSQLState();
        if (state != null && state.equalsIgnoreCase("S0022")) {
            return true;
        }
        String message = ex.getMessage();
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase();
        return normalized.contains("column") && normalized.contains("not valid");
    }
}