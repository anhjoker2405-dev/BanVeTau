package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** POJO HoaDon cho bước thanh toán */
public class HoaDon {
    private String maHoaDon;
    private Timestamp ngayLap;
    private BigDecimal vat;
    private String maNV;
    private String maHK;
    private String maKhuyenMai;

    private final List<ChiTietHoaDon> chiTiet = new ArrayList<>();

    public HoaDon() {}

    public HoaDon(String maHoaDon, Timestamp ngayLap, BigDecimal vat, String maNV, String maHK, String maKhuyenMai) {
        this.maHoaDon = maHoaDon; this.ngayLap = ngayLap; this.vat = vat; this.maNV = maNV; this.maHK = maHK; this.maKhuyenMai = maKhuyenMai;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }
    public BigDecimal getVat() { return vat; }
    public void setVat(BigDecimal vat) { this.vat = vat; }
    public String getMaNV() { return maNV; }
    public void setMaNV(String maNV) { this.maNV = maNV; }
    public String getMaHK() { return maHK; }
    public void setMaHK(String maHK) { this.maHK = maHK; }
    public String getMaKhuyenMai() { return maKhuyenMai; }
    public void setMaKhuyenMai(String maKhuyenMai) { this.maKhuyenMai = maKhuyenMai; }
    public List<ChiTietHoaDon> getChiTiet() { return chiTiet; }
}
