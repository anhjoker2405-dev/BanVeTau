package dao;

import connectDB.ConnectDB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import entity.ChuyenTauThongTin;
import entity.Ga;
import entity.Tau;

public class ChuyenTauDao {
    
    private final SeatAvailabilityDao seatAvailabilityDao = new SeatAvailabilityDao();

    public List<Ga> fetchGaOptions() throws SQLException {
        String sql = "SELECT maGa, tenGa FROM Ga ORDER BY tenGa";
        List<Ga> list = new ArrayList<>();
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Ga(rs.getString("maGa"), rs.getString("tenGa")));
            }
        }
        return list;
    }

    public List<Tau> fetchTauOptions() throws SQLException {
        String sql = "SELECT maTau, tenTau FROM Tau ORDER BY tenTau";
        List<Tau> list = new ArrayList<>();
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Tau(rs.getString("maTau"), rs.getString("tenTau")));
            }
        }
        return list;
    }

     public String generateMaChuyenTau() throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            return generateMaChuyenTau(cn);
        }
    }

    private String generateMaChuyenTau(Connection cn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(maChuyenTau, 3, 50) AS INT)) FROM ChuyenTau WHERE ISNUMERIC(SUBSTRING(maChuyenTau, 3, 50)) = 1";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int next = 1;
            if (rs.next() && rs.getObject(1) != null) {
                next = rs.getInt(1) + 1;
            }
            return formatId("CT", next, 6);
        }
    }

    public String createChuyenTau(String maChuyenTau,
                                  String maGaDi,
                                  String maGaDen,
                                  String maTau,
                                  LocalDateTime thoiGianKhoiHanh,
                                  LocalDateTime thoiGianKetThuc) throws SQLException {
        String sql = "{call dbo.usp_TaoChuyenTau(?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection cn = ConnectDB.getConnection();
             CallableStatement cs = cn.prepareCall(sql)) {
            cs.setString(1, maChuyenTau);
            cs.setString(2, maGaDi);
            cs.setString(3, maGaDen);
            cs.setString(4, maTau);
            cs.setTimestamp(5, Timestamp.valueOf(thoiGianKhoiHanh));
            cs.setTimestamp(6, Timestamp.valueOf(thoiGianKetThuc));
            cs.setString(7, "Đang lập");
            cs.registerOutParameter(8, Types.NVARCHAR);
            cs.execute();
            return cs.getString(8);
        }
    }

    public boolean existsById(String maChuyenTau) throws SQLException {
        String sql = "SELECT 1 FROM ChuyenTau WHERE maChuyenTau = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int deleteById(String maChuyenTau) throws SQLException {
        String sql = "DELETE FROM ChuyenTau WHERE maChuyenTau = ?";
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, maChuyenTau);
            return ps.executeUpdate();
        }
    }

    public int update(String maChuyenTau,
                      String maGaDi,
                      String maGaDen,
                      String maTau,
                      LocalDateTime thoiGianKhoiHanh,
                      LocalDateTime thoiGianKetThuc) throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String maLichTrinh = ensureLichTrinh(cn, maGaDi, maGaDen);
                String sql = "UPDATE ChuyenTau SET maLichTrinh = ?, maTau = ?, thoiGianKhoiHanh = ?, thoiGianKetThuc = ? WHERE maChuyenTau = ?";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setString(1, maLichTrinh);
                    ps.setString(2, maTau);
                    ps.setTimestamp(3, Timestamp.valueOf(thoiGianKhoiHanh));
                    ps.setTimestamp(4, Timestamp.valueOf(thoiGianKetThuc));
                    ps.setString(5, maChuyenTau);
                    int updated = ps.executeUpdate();
                    cn.commit();
                    return updated;
                }
            } catch (Exception ex) {
                cn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException(ex);
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private String ensureLichTrinh(Connection cn, String maGaDi, String maGaDen) throws SQLException {
        String select = "SELECT TOP 1 maLichTrinh FROM LichTrinh WHERE maGaDi = ? AND maGaDen = ? ORDER BY maLichTrinh";
        try (PreparedStatement ps = cn.prepareStatement(select)) {
            ps.setString(1, maGaDi);
            ps.setString(2, maGaDen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        String newId = nextLichTrinhId(cn);
        String insert = "INSERT INTO LichTrinh(maLichTrinh, maGaDi, maGaDen, soKMDiChuyen, thoiGianDi, soTienMotKm) VALUES(?, ?, ?, NULL, NULL, DEFAULT)";
        try (PreparedStatement ps = cn.prepareStatement(insert)) {
            ps.setString(1, newId);
            ps.setString(2, maGaDi);
            ps.setString(3, maGaDen);
            ps.executeUpdate();
        }
        return newId;
    }

    private String nextLichTrinhId(Connection cn) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(maLichTrinh, 3, 50) AS INT)) FROM LichTrinh WHERE ISNUMERIC(SUBSTRING(maLichTrinh, 3, 50)) = 1";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            int next = 1;
            if (rs.next() && rs.getObject(1) != null) {
                next = rs.getInt(1) + 1;
            }
            return formatId("LT", next, 6);
        }
    }

    private String formatId(String prefix, int value, int width) {
        return prefix + String.format("%0" + width + "d", value);
    }

    public List<ChuyenTauThongTin> fetchDanhSachChuyenTau() throws SQLException {
        String sql = "SELECT DISTINCT ct.maChuyenTau, "
                + "       gDi.tenGa AS gaDi, "
                + "       gDen.tenGa AS gaDen, "
                + "       lt.thoiGianDi, "
                + "       ct.thoiGianKhoiHanh, "
                + "       ct.thoiGianKetThuc, "
                + "       tau.maTau, "
                + "       tau.tenTau, "
                + "       toa.soToa, "
                + "       khoang.tenKhoangTau, "
                + "       lg.tenLoaiGhe, "
                + "       ct.soGheTrong "
                + "FROM   ChuyenTau ct "
                + "JOIN   LichTrinh lt   ON lt.maLichTrinh = ct.maLichTrinh "
                + "JOIN   Ga gDi         ON gDi.maGa = lt.maGaDi "
                + "JOIN   Ga gDen        ON gDen.maGa = lt.maGaDen "
                + "JOIN   Tau tau        ON tau.maTau = ct.maTau "
                + "LEFT JOIN ToaTau toa  ON toa.maTau = tau.maTau "
                + "LEFT JOIN KhoangTau khoang ON khoang.maToa = toa.maToa "
                + "LEFT JOIN Ghe ghe     ON ghe.maKhoangTau = khoang.maKhoangTau "
                + "LEFT JOIN LoaiGhe lg  ON lg.maLoaiGhe = ghe.maLoaiGhe "
                + "ORDER BY ct.maChuyenTau, toa.soToa, khoang.tenKhoangTau, lg.tenLoaiGhe";

        List<ChuyenTauThongTin> result = new ArrayList<>();
        try (Connection cn = ConnectDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDateTime khoiHanh = rs.getTimestamp("thoiGianKhoiHanh") != null
                        ? rs.getTimestamp("thoiGianKhoiHanh").toLocalDateTime()
                        : null;
                LocalDateTime ketThuc = rs.getTimestamp("thoiGianKetThuc") != null
                        ? rs.getTimestamp("thoiGianKetThuc").toLocalDateTime()
                        : null;
                Integer thoiGianDi = (Integer) rs.getObject("thoiGianDi");

                String soToa = rs.getString("soToa");
                if (soToa != null) {
                    soToa = soToa.trim();
                }

                result.add(new ChuyenTauThongTin(
                        rs.getString("maChuyenTau"),
                        rs.getString("gaDi"),
                        rs.getString("gaDen"),
                        thoiGianDi,
                        khoiHanh,
                        ketThuc,
                        rs.getString("tenTau"),
                        soToa,
                        rs.getString("tenKhoangTau"),
                        rs.getString("tenLoaiGhe"),
                        rs.getInt("soGheTrong")
                ));
            }
        }

        return result;
    }
}