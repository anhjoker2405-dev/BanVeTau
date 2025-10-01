package model;

public class NhanVienQuanLy extends NhanVien {
    public NhanVienQuanLy(String maNV, String tenNV, String soDienThoai, String email) {
        super(maNV, tenNV, soDienThoai, email);
    }
    @Override public String getLoaiHienThi() { return "Quản Lý"; }
}
