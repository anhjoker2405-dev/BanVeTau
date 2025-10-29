package dao;

import connectDB.ConnectDB;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DAO phục vụ thao tác cập nhật trạng thái khi trả vé.
 */
public class TraVe_Dao {

    /**
     * Cập nhật trạng thái của vé sang "Đã hủy".
     *
     * @param maVe mã vé cần hủy
     * @return true nếu cập nhật thành công, false nếu không có bản ghi nào bị ảnh hưởng
     */
    public boolean cancelTicket(String maVe) throws SQLException {
        try (Connection cn = ConnectDB.getConnection()) {
            Ve_Dao veDao = new Ve_Dao();
            return veDao.updateTrangThai(cn, maVe, "Đã hủy") > 0;
        }
    }
}