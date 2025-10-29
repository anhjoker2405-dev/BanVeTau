package entity;

public class TaiKhoan {
    private final String maTK;
    private final String tenDangNhap;
    private final String matKhau;
    private final String maNV;
    private final String trangThai;
    private final String maLoaiTK;
    private final String tenLoaiTK;

    public TaiKhoan(String maTK, String tenDangNhap, String matKhau, String maNV,
                     String trangThai, String maLoaiTK, String tenLoaiTK) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.maNV = maNV;
        this.trangThai = trangThai;
        this.maLoaiTK = maLoaiTK;
        this.tenLoaiTK = tenLoaiTK;
    }

    public String getMaTK() { return maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public String getMaNV() { return maNV; }
    public String getTrangThai() { return trangThai; }
    public String getMaLoaiTK() { return maLoaiTK; }

    public String getTenLoaiTK() { return tenLoaiTK; }

    /**
     * Giữ lại getter cũ để tránh sửa toàn bộ code phía trên UI.
     * Nếu có tên loại thì ưu tiên trả về, ngược lại trả về mã loại.
     */
    public String getLoaiTK() {
        return tenLoaiTK != null && !tenLoaiTK.isBlank()
                ? tenLoaiTK
                : maLoaiTK;
    }
}
