package entity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Một dòng chi tiết trên hóa đơn PDF.
 */
public class InvoicePdfItem {
    private final String maVe;
    private final String tenDichVu;
    private final int soLuong;
    private final BigDecimal donGiaChuaThue;
    private final BigDecimal thanhTienChuaThue;
    private final BigDecimal thueGTGT;
    private final BigDecimal thanhTienCoThue;

    public InvoicePdfItem(String maVe,
                          String tenDichVu,
                          int soLuong,
                          BigDecimal donGiaChuaThue,
                          BigDecimal thanhTienChuaThue,
                          BigDecimal thueGTGT,
                          BigDecimal thanhTienCoThue) {
        this.maVe = maVe;
        this.tenDichVu = tenDichVu;
        this.soLuong = soLuong;
        this.donGiaChuaThue = sanitize(donGiaChuaThue);
        this.thanhTienChuaThue = sanitize(thanhTienChuaThue);
        this.thueGTGT = sanitize(thueGTGT);
        this.thanhTienCoThue = sanitize(thanhTienCoThue);
    }

    private static BigDecimal sanitize(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public String getMaVe() {
        return maVe;
    }

    public String getTenDichVu() {
        return tenDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public BigDecimal getDonGiaChuaThue() {
        return donGiaChuaThue;
    }

    public BigDecimal getThanhTienChuaThue() {
        return thanhTienChuaThue;
    }

    public BigDecimal getThueGTGT() {
        return thueGTGT;
    }

    public BigDecimal getThanhTienCoThue() {
        return thanhTienCoThue;
    }

    @Override
    public String toString() {
        return "InvoicePdfItem{" +
                "maVe='" + maVe + '\'' +
                ", tenDichVu='" + tenDichVu + '\'' +
                ", soLuong=" + soLuong +
                ", donGiaChuaThue=" + donGiaChuaThue +
                ", thanhTienChuaThue=" + thanhTienChuaThue +
                ", thueGTGT=" + thueGTGT +
                ", thanhTienCoThue=" + thanhTienCoThue +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(maVe, tenDichVu, soLuong, donGiaChuaThue, thanhTienChuaThue, thueGTGT, thanhTienCoThue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InvoicePdfItem other)) return false;
        return soLuong == other.soLuong
                && Objects.equals(maVe, other.maVe)
                && Objects.equals(tenDichVu, other.tenDichVu)
                && Objects.equals(donGiaChuaThue, other.donGiaChuaThue)
                && Objects.equals(thanhTienChuaThue, other.thanhTienChuaThue)
                && Objects.equals(thueGTGT, other.thueGTGT)
                && Objects.equals(thanhTienCoThue, other.thanhTienCoThue);
    }
}