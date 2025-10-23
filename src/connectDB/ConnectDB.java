package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=RailwayBooking;encrypt=false;";

    private static final String USER = "sa";
    private static final String PASSWORD = "123";

    // Singleton instance
    private static final ConnectDB instance = new ConnectDB();

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    /**
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println(" Không tìm thấy driver SQL Server.");
            throw new SQLException("Driver not found", e);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi kết nối đến cơ sở dữ liệu: " + e.getMessage());
            throw e;
        }
    }


    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println(" Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
}