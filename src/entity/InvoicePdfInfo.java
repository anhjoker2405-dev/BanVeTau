package entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thông tin tổng hợp để in hóa đơn PDF.
 */
public class InvoicePdfInfo {
    private String maHoaDon;
    private LocalDate ngayLap;
    private String nhanVienLap;
    private String dienThoaiNhanVien;
    private String tenKhachHang;
    private String dienThoaiKhachHang;
    private BigDecimal vatRate = BigDecimal.ZERO;         // dạng thập phân, ví dụ 0.10
    private BigDecimal vatRatePercent = BigDecimal.ZERO;  // dạng phần trăm, ví dụ 10.00
    private String maKhuyenMai;
    private BigDecimal promotionRate = BigDecimal.ZERO;        // dạng thập phân, ví dụ 0.05
    private BigDecimal promotionRatePercent = BigDecimal.ZERO; // dạng phần trăm, ví dụ 5.00

    private final List<InvoicePdfItem> items = new ArrayList<>();

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getNhanVienLap() {
        return nhanVienLap;
    }

    public void setNhanVienLap(String nhanVienLap) {
        this.nhanVienLap = nhanVienLap;
    }

    public String getDienThoaiNhanVien() {
        return dienThoaiNhanVien;
    }

    public void setDienThoaiNhanVien(String dienThoaiNhanVien) {
        this.dienThoaiNhanVien = dienThoaiNhanVien;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getDienThoaiKhachHang() {
        return dienThoaiKhachHang;
    }

    public void setDienThoaiKhachHang(String dienThoaiKhachHang) {
        this.dienThoaiKhachHang = dienThoaiKhachHang;
    }

    public BigDecimal getVatRate() {
        return vatRate != null ? vatRate : BigDecimal.ZERO;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate != null ? vatRate : BigDecimal.ZERO;
    }

    public BigDecimal getVatRatePercent() {
        return vatRatePercent != null ? vatRatePercent : BigDecimal.ZERO;
    }

    public void setVatRatePercent(BigDecimal vatRatePercent) {
        this.vatRatePercent = vatRatePercent != null ? vatRatePercent : BigDecimal.ZERO;
    }
    
    public String getMaKhuyenMai() {
        return maKhuyenMai;
    }

    public void setMaKhuyenMai(String maKhuyenMai) {
        this.maKhuyenMai = maKhuyenMai;
    }

    public BigDecimal getPromotionRate() {
        return promotionRate != null ? promotionRate : BigDecimal.ZERO;
    }

    public void setPromotionRate(BigDecimal promotionRate) {
        this.promotionRate = promotionRate != null ? promotionRate : BigDecimal.ZERO;
    }

    public BigDecimal getPromotionRatePercent() {
        return promotionRatePercent != null ? promotionRatePercent : BigDecimal.ZERO;
    }

    public void setPromotionRatePercent(BigDecimal promotionRatePercent) {
        this.promotionRatePercent = promotionRatePercent != null ? promotionRatePercent : BigDecimal.ZERO;
    }

    public List<InvoicePdfItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<InvoicePdfItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
    }

    public void addItem(InvoicePdfItem item) {
        if (item != null) {
            items.add(item);
        }
    }
}