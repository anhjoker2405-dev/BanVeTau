package model;

import java.time.LocalDate;

/**
 * POJO chứa đầy đủ thông tin nhân viên để sử dụng cho các màn quản lý.
 */
public class NhanVienThongTin {
    private String maNV;
    private String tenNV;
    private LocalDate ngaySinh;
    private String soDienThoai;
    private String email;
    private String loaiNV;

    public NhanVienThongTin() {
    }

    public NhanVienThongTin(String maNV, String tenNV, LocalDate ngaySinh,
                             String soDienThoai, String email, String loaiNV) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.loaiNV = loaiNV;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoaiNV() {
        return loaiNV;
    }

    public void setLoaiNV(String loaiNV) {
        this.loaiNV = loaiNV;
    }
}