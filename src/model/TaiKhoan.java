package model;

public class TaiKhoan {
    private String maTK;
    private String tenDangNhap;
    private String matKhau;
    private String maNV;
    private String trangThai;
    private String loaiTK;

    public TaiKhoan(String maTK, String tenDangNhap, String matKhau, String maNV, String trangThai, String loaiTK) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.maNV = maNV;
        this.trangThai = trangThai;
        this.loaiTK = loaiTK;
    }

    public String getMaTK() { return maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getMaNV() { return maNV; }
    public String getTrangThai() { return trangThai; }
    public String getLoaiTK() { return loaiTK; }
}
