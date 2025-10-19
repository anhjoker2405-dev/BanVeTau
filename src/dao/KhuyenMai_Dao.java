package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import entity.KhuyenMai;
import connectDB.ConnectDB;

public class KhuyenMai_Dao {

    /**
     * Phương thức lấy tất cả danh sách khuyến mãi từ CSDL
     * @return List<KhuyenMai>
     */
    public List<KhuyenMai> getAllKhuyenMai() {
        List<KhuyenMai> dsKhuyenMai = new ArrayList<>();
        String sql = "SELECT maKhuyenMai, tenKhuyenMai, giamGia, ngayBatDau, ngayKetThuc, moTa FROM KhuyenMai"; 
        
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai();
                
                km.setMaKhuyenMai(rs.getString("maKhuyenMai"));
                km.setTenKhuyenMai(rs.getString("tenKhuyenMai"));
                km.setGiamGia(rs.getBigDecimal("giamGia"));
                km.setNgayBatDau(rs.getTimestamp("ngayBatDau").toLocalDateTime());
                km.setNgayKetThuc(rs.getTimestamp("ngayKetThuc").toLocalDateTime());
                km.setMoTa(rs.getString("moTa"));
                
                dsKhuyenMai.add(km);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return dsKhuyenMai;
    }

    /**
     * Phương thức thêm một khuyến mãi mới vào CSDL
     * @param km Đối tượng KhuyenMai chứa thông tin cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean addKhuyenMai(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (maKhuyenMai, tenKhuyenMai, giamGia, ngayBatDau, ngayKetThuc, moTa) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, km.getMaKhuyenMai());
            pstmt.setString(2, km.getTenKhuyenMai());
            pstmt.setBigDecimal(3, km.getGiamGia());
            pstmt.setTimestamp(4, Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(5, Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(6, km.getMoTa());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Phương thức cập nhật thông tin một khuyến mãi
     * @param km Đối tượng KhuyenMai chứa thông tin cần cập nhật
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean updateKhuyenMai(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKhuyenMai = ?, giamGia = ?, ngayBatDau = ?, " +
                     "ngayKetThuc = ?, moTa = ? " +
                     "WHERE maKhuyenMai = ?";
        
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, km.getTenKhuyenMai());
            pstmt.setBigDecimal(2, km.getGiamGia());
            pstmt.setTimestamp(3, Timestamp.valueOf(km.getNgayBatDau()));
            pstmt.setTimestamp(4, Timestamp.valueOf(km.getNgayKetThuc()));
            pstmt.setString(5, km.getMoTa());
            pstmt.setString(6, km.getMaKhuyenMai()); // Điều kiện WHERE

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Phương thức xóa một khuyến mãi khỏi CSDL
     * @param maKM Mã khuyến mãi cần xóa
     * @return true nếu xóa thành công, false nếu thất bại
     */
    public boolean deleteKhuyenMai(String maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKhuyenMai = ?";
        
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maKM);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}