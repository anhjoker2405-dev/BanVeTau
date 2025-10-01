package model;

public class NhanVienBanVe extends NhanVien {
    public NhanVienBanVe(String maNV, String tenNV, String soDienThoai, String email) {
        super(maNV, tenNV, soDienThoai, email);
    }
    @Override public String getLoaiHienThi() { return "Bán Vé"; }
}
