package model;

public abstract class NhanVien {
    protected String maNV;
    protected String tenNV;
    protected String soDienThoai;
    protected String email;

    public NhanVien(String maNV, String tenNV, String soDienThoai, String email) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.soDienThoai = soDienThoai;
        this.email = email;
    }
    public String getMaNV() { return maNV; }
    public String getTenNV() { return tenNV; }
    public String getSoDienThoai() { return soDienThoai; }
    public String getEmail() { return email; }

    public abstract String getLoaiHienThi(); 
}
