package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

public class ThanhToan_Dao {
    private final HoaDon_Dao hoaDonDao = new HoaDon_Dao();
    private final ChiTietHoaDon_Dao cthdDao = new ChiTietHoaDon_Dao();
    private final Ve_Dao veDao = new Ve_Dao();
    private final SeatAvailabilityDao seatAvailabilityDao = new SeatAvailabilityDao();

    /** Lưu 1 hóa đơn + danh sách vé (mỗi vé 1 chi tiết), thực hiện trong TRANSACTION */
    public String luuHoaDonVaVe(String maNV, String maHK, String maChuyenTau, List<String> danhSachMaGhe, BigDecimal donGiaMoiVe, BigDecimal vat, String maKM) throws SQLException {
        try (Connection cn = ConnectDB.getInstance().getConnection()) {
            try {
                cn.setAutoCommit(false);

                // 1) Tạo hóa đơn
                String maHD = hoaDonDao.createHoaDon(cn, maNV, maHK, vat, maKM);

                // 2) Lưu từng vé và chi tiết
                for (String maGhe : danhSachMaGhe) {
                    // tạo maVe (đơn giản theo time + ghe)
                    String maVe = "VE" + (System.currentTimeMillis() % 1000000000L);
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    // mặc định maLoaiVe = LV01 (có thể đổi theo UI)
//                    veDao.insertVe(cn, maVe, donGiaMoiVe, "LV01", now, maGhe, maChuyenTau, maHK);
                    veDao.insertVe(cn, maVe, donGiaMoiVe, "LV01", now, maGhe, maChuyenTau, maHK, "Đã bán");
                    cthdDao.insertCT(cn, maHD, maVe, 1, donGiaMoiVe);
                }
                seatAvailabilityDao.refreshForTrip(cn, maChuyenTau);
                
                cn.commit();
                return maHD;
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
    public ExchangeResult luuDoiVe(String maNV, String maHK, String maVeCu, String maChuyenTauCu,
                                   String maChuyenTauMoi, String maGheMoi, String maLoaiVeMoi,
                                   BigDecimal giaVeMoi, BigDecimal tongThanhToan) throws SQLException {
        try (Connection cn = ConnectDB.getInstance().getConnection()) {
            try {
                cn.setAutoCommit(false);

                veDao.updateTrangThai(cn, maVeCu, "Đã hủy");

                String maHD = hoaDonDao.createHoaDon(cn, maNV, maHK, BigDecimal.ZERO, null);
                String maVeMoi = "VE" + (System.currentTimeMillis() % 1000000000L);
                Timestamp now = new Timestamp(System.currentTimeMillis());
                String maLoaiVe = maLoaiVeMoi != null ? maLoaiVeMoi : "LV01";

                veDao.insertVe(cn, maVeMoi, giaVeMoi, maLoaiVe, now, maGheMoi, maChuyenTauMoi, maHK, "Đã đổi");
                cthdDao.insertCT(cn, maHD, maVeMoi, 1, tongThanhToan);

                seatAvailabilityDao.refreshForTrip(cn, maChuyenTauMoi);
                if (maChuyenTauCu != null && !maChuyenTauCu.equals(maChuyenTauMoi)) {
                    seatAvailabilityDao.refreshForTrip(cn, maChuyenTauCu);
                }

                cn.commit();
                return new ExchangeResult(maHD, maVeMoi);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public static class ExchangeResult {
        private final String maHoaDon;
        private final String maVeMoi;

        public ExchangeResult(String maHoaDon, String maVeMoi) {
            this.maHoaDon = maHoaDon;
            this.maVeMoi = maVeMoi;
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public String getMaVeMoi() {
            return maVeMoi;
        }
    }
}
