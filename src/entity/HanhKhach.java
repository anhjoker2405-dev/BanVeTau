package entity;


public class HanhKhach {
    private String maHK;
    private String tenHK;
    private String soDienThoai;
    private String cccd;
    private String maGT;
    private String tenGT;

    public HanhKhach() {
    }

    public HanhKhach(String maHK, String tenHK, String soDienThoai, String cccd, String gioiTinh) {
        this(maHK, tenHK, soDienThoai, cccd, null, gioiTinh);
    }
    public HanhKhach(String maHK, String tenHK, String soDienThoai, String cccd, String maGT, String tenGT) {
        this.maHK = maHK;
        this.tenHK = tenHK;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.maGT = maGT;
        this.tenGT = tenGT;
    }

    public String getMaHK() {
        return maHK;
    }

    public void setMaHK(String maHK) {
        this.maHK = maHK;
    }

    public String getTenHK() {
        return tenHK;
    }

    public void setTenHK(String tenHK) {
        this.tenHK = tenHK;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getMaGT() {
        return maGT;
    }

    public void setMaGT(String maGT) {
        this.maGT = maGT;
    }

    public String getTenGT() {
        return tenGT;
    }

    public void setTenGT(String tenGT) {
        this.tenGT = tenGT;
    }

    /**
     * Getter giữ tương thích ngược với mã nguồn cũ sử dụng "gioiTinh".
     */
    public String getGioiTinh() {
        return tenGT;
    }

    public void setGioiTinh(String gioiTinh) {
        this.tenGT = gioiTinh;
    }
}
