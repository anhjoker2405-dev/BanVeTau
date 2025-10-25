package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Thông tin vé khi tạo mới ở bước thanh toán */
public class VeMua {
    private String maVe;
    private BigDecimal giaVe;
    private String maLoaiVe;
    private Timestamp ngayDat;
    private String maGhe;
    private String maChuyenTau;
    private String maHK;

    public VeMua() {}

    public VeMua(String maVe, BigDecimal giaVe, String maLoaiVe, Timestamp ngayDat, String maGhe, String maChuyenTau, String maHK) {
        this.maVe = maVe; this.giaVe = giaVe; this.maLoaiVe = maLoaiVe; this.ngayDat = ngayDat; this.maGhe = maGhe; this.maChuyenTau = maChuyenTau; this.maHK = maHK;
    }

    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }
    public BigDecimal getGiaVe() { return giaVe; }
    public void setGiaVe(BigDecimal giaVe) { this.giaVe = giaVe; }
    public String getMaLoaiVe() { return maLoaiVe; }
    public void setMaLoaiVe(String maLoaiVe) { this.maLoaiVe = maLoaiVe; }
    public Timestamp getNgayDat() { return ngayDat; }
    public void setNgayDat(Timestamp ngayDat) { this.ngayDat = ngayDat; }
    public String getMaGhe() { return maGhe; }
    public void setMaGhe(String maGhe) { this.maGhe = maGhe; }
    public String getMaChuyenTau() { return maChuyenTau; }
    public void setMaChuyenTau(String maChuyenTau) { this.maChuyenTau = maChuyenTau; }
    public String getMaHK() { return maHK; }
    public void setMaHK(String maHK) { this.maHK = maHK; }
}
