package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ThanhToan_Dao {
    private final HoaDon_Dao hoaDonDao = new HoaDon_Dao();
    private final ChiTietHoaDon_Dao cthdDao = new ChiTietHoaDon_Dao();
    private final Ve_Dao veDao = new Ve_Dao();
    private final SeatAvailabilityDao seatAvailabilityDao = new SeatAvailabilityDao();

    /** Lưu 1 hóa đơn + danh sách vé (mỗi vé 1 chi tiết), thực hiện trong TRANSACTION */
    public PaymentResult luuHoaDonVaVe(String maNV, String maHK, String maChuyenTau, List<String> danhSachMaGhe,
                                       BigDecimal donGiaMoiVe, BigDecimal vat, String maKM) throws SQLException {
        try (Connection cn = ConnectDB.getInstance().getConnection()) {
            try {
                cn.setAutoCommit(false);

                // 1) Tạo hóa đơn
                String maHD = hoaDonDao.createHoaDon(cn, maNV, maHK, vat, maKM);
                
                List<String> maVeList = new ArrayList<>();

                // 2) Lưu từng vé và chi tiết
                for (int i = 0; i < danhSachMaGhe.size(); i++) {
                    String maGhe = danhSachMaGhe.get(i);
                    // tạo maVe (đơn giản theo time + ghe)
                    String maVe = generateTicketId(i);
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    // mặc định maLoaiVe = LV01 (có thể đổi theo UI)
//                    veDao.insertVe(cn, maVe, donGiaMoiVe, "LV01", now, maGhe, maChuyenTau, maHK);
                    veDao.insertVe(cn, maVe, donGiaMoiVe, "LV01", now, maGhe, maChuyenTau, maHK, "Đã bán");
                    cthdDao.insertCT(cn, maHD, maVe, 1, donGiaMoiVe);
                    maVeList.add(maVe);
                }
                seatAvailabilityDao.refreshForTrip(cn, maChuyenTau);
                
                cn.commit();
                return new PaymentResult(maHD, maVeList);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }
    
    private String generateTicketId(int index) {
        long nano = System.nanoTime();
        long base = Math.abs(nano % 1_000_000_0000L); // 10 chữ số
        int suffix = Math.abs(index % 100);
        return String.format("VE%010d%02d", base, suffix);
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
    
    public static class PaymentResult {
        private final String maHoaDon;
        private final List<String> maVeList;

        public PaymentResult(String maHoaDon, List<String> maVeList) {
            this.maHoaDon = maHoaDon;
            this.maVeList = maVeList != null ? List.copyOf(maVeList) : List.of();
        }

        public String getMaHoaDon() {
            return maHoaDon;
        }

        public List<String> getMaVeList() {
            return maVeList;
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
