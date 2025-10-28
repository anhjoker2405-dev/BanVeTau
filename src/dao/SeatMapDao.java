package dao;

import connectDB.ConnectDB;
import entity.Ghe;
import entity.KhoangTau;
import entity.ToaTau;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO tải dữ liệu sơ đồ ghế cho màn hình chọn ghế.
 */
public class SeatMapDao {

    private static final String SEAT_MAP_SQL =
        "SELECT tt.maToa, tt.soToa, tt.maTau, tt.soKhoangTau, " +
        "       kt.maKhoangTau, kt.tenKhoangTau, kt.soLuongGhe, " +
        "       g.maGhe, g.soGhe, g.maLoaiGhe, " +
        "       CASE WHEN EXISTS (SELECT 1 FROM Ve v WHERE v.maChuyenTau = ct.maChuyenTau AND v.maGhe = g.maGhe AND v.trangThai IN (N'Đã bán', N'Đã đổi')) " +
        "            THEN 1 ELSE 0 END AS daBan " +
        "FROM ChuyenTau ct " +
        "JOIN ToaTau tt   ON tt.maTau = ct.maTau " +
        "JOIN KhoangTau kt ON kt.maToa = tt.maToa " +
        "JOIN Ghe g       ON g.maKhoangTau = kt.maKhoangTau " +
        "WHERE ct.maChuyenTau = ? " +
        "ORDER BY tt.soToa, TRY_CAST(kt.tenKhoangTau AS INT), TRY_CAST(g.soGhe AS INT), g.soGhe";

    public List<ToaTau> loadSeatMap(String maChuyenTau) throws SQLException {
        Map<String, ToaTau> toaMap = new LinkedHashMap<>();
        Map<String, KhoangTau> khoangMap = new LinkedHashMap<>();
        Map<String, Integer> seatCounter = new LinkedHashMap<>();

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(SEAT_MAP_SQL)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maToa = rs.getString("maToa");
                    ToaTau toa = toaMap.get(maToa);
                    if (toa == null) {
                        toa = new ToaTau(
                                maToa,
                                rs.getInt("soToa"),
                                rs.getString("maTau"),
                                rs.getInt("soKhoangTau"));
                        toaMap.put(maToa, toa);
                    }

                    String maKhoang = rs.getString("maKhoangTau");
                    String khoangKey = maToa + "|" + maKhoang;
                    KhoangTau khoang = khoangMap.get(khoangKey);
                    if (khoang == null) {
                        khoang = new KhoangTau(
                                maKhoang,
                                rs.getString("tenKhoangTau"),
                                rs.getInt("soLuongGhe"),
                                maToa);
                        khoangMap.put(khoangKey, khoang);
                        toa.addKhoang(khoang);
                    }

                    Ghe ghe = new Ghe(
                            rs.getString("maGhe"),
                            rs.getString("soGhe"),
                            rs.getString("maLoaiGhe"),
                            maKhoang,
                            rs.getBoolean("daBan"));
                    int order = seatCounter.merge(maToa, 1, Integer::sum);
                    ghe.setThuTuHienThi(order);
                    khoang.addGhe(ghe);
                }
            }
        }

        return new ArrayList<>(toaMap.values());
    }
}