package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class GiaCoSoFetcher {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=RailwayBooking;encrypt=false";
        String user = "sa";
        String password = "123";

        String maLichTrinh = "LT000000"; // mã bạn muốn tra

        String sql = """
            SELECT CAST(ROUND(soKMDiChuyen * soTienMotKm, 0) AS decimal(12,0)) AS giaCoSo
            FROM dbo.LichTrinh
            WHERE maLichTrinh = ?
        """;

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maLichTrinh);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double giaCoSo = rs.getDouble("giaCoSo");
                    System.out.println("Giá cơ sở cho " + maLichTrinh + " là: " + giaCoSo + " VND");
                } else {
                    System.out.println("Không tìm thấy mã lịch trình " + maLichTrinh);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
