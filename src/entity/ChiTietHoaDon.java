package entity;

import java.math.BigDecimal;

/** Chi tiết hóa đơn (mỗi vé 1 dòng) */
public class ChiTietHoaDon {
    private String maHoaDon;
    private String maVe;
    private int soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {}
    public ChiTietHoaDon(String maHoaDon, String maVe, int soLuong, BigDecimal donGia) {
        this.maHoaDon = maHoaDon; this.maVe = maVe; this.soLuong = soLuong; this.donGia = donGia;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { this.maHoaDon = maHoaDon; }
    public String getMaVe() { return maVe; }
    public void setMaVe(String maVe) { this.maVe = maVe; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }
}
