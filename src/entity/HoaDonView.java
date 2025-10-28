package entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** DTO/POJO dùng cho màn hình tìm kiếm hóa đơn */
public class HoaDonView {
    private String maHoaDon;
    private Timestamp ngayLap;
    private String tenNhanVien;
    private String tenHanhKhach;
    private String sdtHanhKhach;
    private BigDecimal vat;
    private BigDecimal tongTien;

    public HoaDonView() {}

    public HoaDonView(String maHoaDon, Timestamp ngayLap, String tenNhanVien,
                      String tenHanhKhach, String sdtHanhKhach, BigDecimal vat, BigDecimal tongTien) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.tenNhanVien = tenNhanVien;
        this.tenHanhKhach = tenHanhKhach;
        this.sdtHanhKhach = sdtHanhKhach;
        this.vat = vat;
        this.tongTien = tongTien;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }

    public Timestamp getNgayLap() { return ngayLap; }
    public void setNgayLap(Timestamp ngayLap) { this.ngayLap = ngayLap; }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }

    public String getTenHanhKhach() { return tenHanhKhach; }
    public void setTenHanhKhach(String tenHanhKhach) { this.tenHanhKhach = tenHanhKhach; }

    public String getSdtHanhKhach() { return sdtHanhKhach; }
    public void setSdtHanhKhach(String sdtHanhKhach) { this.sdtHanhKhach = sdtHanhKhach; }

    public BigDecimal getVat() { return vat; }
    public void setVat(BigDecimal vat) { this.vat = vat; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }
}
