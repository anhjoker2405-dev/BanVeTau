package dao;

import connectDB.ConnectDB;
import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

public class ThanhToan_Dao {
    private final HoaDon_Dao hoaDonDao = new HoaDon_Dao();
    private final ChiTietHoaDon_Dao cthdDao = new ChiTietHoaDon_Dao();
    private final Ve_Dao veDao = new Ve_Dao();

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
                    String maVe = "VE" + System.currentTimeMillis() + maGhe.hashCode();
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    // mặc định maLoaiVe = LV01 (có thể đổi theo UI)
                    veDao.insertVe(cn, maVe, donGiaMoiVe, "LV01", now, maGhe, maChuyenTau, maHK);
                    cthdDao.insertCT(cn, maHD, maVe, 1, donGiaMoiVe);
                }

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
}
