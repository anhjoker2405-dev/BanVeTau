
package dao;

import connectDB.ConnectDB;
import entity.HoaDonView;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HoaDon_Dao {

    /** Tạo hóa đơn mới, trả về maHoaDon đã tạo */
    public String createHoaDon(Connection cn, String maNV, String maHK, BigDecimal vat, String maKM) throws SQLException {
        String maHD = "HD" + System.currentTimeMillis();
        String sql = "INSERT INTO HoaDon(maHoaDon, ngayLapHoaDon, VAT, maNV, maHK, maKhuyenMai) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setBigDecimal(3, vat == null ? new BigDecimal("0.00") : vat);
            ps.setString(4, maNV);
            ps.setString(5, maHK);
            ps.setString(6, maKM);
            ps.executeUpdate();
            return maHD;
        }
    }

    /**
     * Tìm kiếm hoá đơn theo mã, ngày lập từ/đến, theo mã NV hoặc mã HK (tuỳ chọn).
     * Các tham số null/chuỗi rỗng sẽ được bỏ qua trong WHERE.
     */
    public List<HoaDonView> search(String maHoaDon, Timestamp from, Timestamp to, String maNV, String maHK) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT hd.maHoaDon, hd.ngayLapHoaDon, nv.tenNV, hk.tenHK, hk.soDienThoai, hd.VAT, ");
        sql.append("CAST(ISNULL(SUM(ct.soLuongVe * ct.donGia), 0) AS DECIMAL(18,2)) AS tongTien ");
        sql.append("FROM HoaDon hd ");
        sql.append("LEFT JOIN ChiTietHoaDon ct ON ct.maHoaDon = hd.maHoaDon ");
        sql.append("LEFT JOIN NhanVien nv ON nv.maNV = hd.maNV ");
        sql.append("LEFT JOIN HanhKhach hk ON hk.maHK = hd.maHK ");

        List<Object> params = new ArrayList<>();
        List<Integer> types = new ArrayList<>();
        List<String> where = new ArrayList<>();

        if (maHoaDon != null && !maHoaDon.isBlank()) {
            where.add("hd.maHoaDon LIKE ?");
            params.add("%" + maHoaDon.trim() + "%");
            types.add(Types.NVARCHAR);
        }
        if (from != null) {
            where.add("hd.ngayLapHoaDon >= ?");
            params.add(from);
            types.add(Types.TIMESTAMP);
        }
        if (to != null) {
            where.add("hd.ngayLapHoaDon <= ?");
            params.add(to);
            types.add(Types.TIMESTAMP);
        }
        if (maNV != null && !maNV.isBlank()) {
            where.add("hd.maNV = ?");
            params.add(maNV.trim());
            types.add(Types.NVARCHAR);
        }
        if (maHK != null && !maHK.isBlank()) {
            where.add("hd.maHK = ?");
            params.add(maHK.trim());
            types.add(Types.NVARCHAR);
        }

        if (!where.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", where));
        }
        sql.append(" GROUP BY hd.maHoaDon, hd.ngayLapHoaDon, nv.tenNV, hk.tenHK, hk.soDienThoai, hd.VAT ");
        sql.append(" ORDER BY hd.ngayLapHoaDon DESC, hd.maHoaDon DESC ");

        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                int t = types.get(i);
                if (t == Types.NVARCHAR) ps.setString(i + 1, (String) v);
                else if (t == Types.TIMESTAMP) ps.setTimestamp(i + 1, (Timestamp) v);
                else ps.setObject(i + 1, v);
            }

            List<HoaDonView> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDonView r = new HoaDonView(
                            rs.getString(1),      // maHoaDon
                            rs.getTimestamp(2),   // ngayLap
                            rs.getString(3),      // tenNV
                            rs.getString(4),      // tenHK
                            rs.getString(5),      // sdtHK
                            rs.getBigDecimal(6),  // VAT
                            rs.getBigDecimal(7)   // tongTien
                    );
                    list.add(r);
                }
            }
            return list;
        }
    }
}
