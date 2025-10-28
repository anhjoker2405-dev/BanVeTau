package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Mô hình dữ liệu đại diện một chuyến tàu dùng cho màn hình tra cứu.
 */
public class ChuyenTau {
    private final String maChuyenTau;
    private final String gaDi;
    private final String gaDen;
    private final LocalDateTime thoiGianKhoiHanh;
    private final LocalDateTime thoiGianKetThuc;
    private final String maTau;
    private final String tenTau;
    private final BigDecimal giaVe;
    private int soGheTrong;
    private int quangDuongDiChuyen;

    // constructor
    public ChuyenTau(String maChuyenTau, String gaDi, String gaDen,
                     LocalDateTime thoiGianKhoiHanh, LocalDateTime thoiGianKetThuc,
                     String maTau, String tenTau, int soGheTrong, BigDecimal giaVe) {
        this.maChuyenTau = maChuyenTau;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.soGheTrong = soGheTrong;
        this.giaVe = giaVe;
        this.quangDuongDiChuyen = quangDuongDiChuyen;
        
    }

    public String getMaChuyenTau() {
        return maChuyenTau;
    }

    public String getGaDi() {
        return gaDi;
    }

    public String getGaDen() {
        return gaDen;
    }

    public LocalDateTime getThoiGianKhoiHanh() {
        return thoiGianKhoiHanh;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }
    
    public String getMaTau() {
        return maTau;
    }

    public String getTenTau() {
        return tenTau;
    }
    
    public BigDecimal getGiaVe() {
        return giaVe;
    }
    
    public int getSoGheTrong() {
        return soGheTrong;
    }

    public void setSoGheTrong(int soGheTrong) {
        this.soGheTrong = soGheTrong;
    }
    
    public int getQuangDuongDiChuyen(){
        return quangDuongDiChuyen;
    }
    
    public void setQuangDuongDiChuyen(int quangDuongDiChuyen){
        this.quangDuongDiChuyen = quangDuongDiChuyen;
    }
}