package entity;

import java.time.LocalDateTime;
/**
 * Đại diện dữ liệu hiển thị của một chuyến tàu kèm chi tiết toa, khoang, loại ghế.
 */
public class ChuyenTauThongTin {
    private final String maChuyenTau;
    private final String gaDi;
    private final String gaDen;
    private final Integer thoiGianDi;
    private final LocalDateTime thoiGianKhoiHanh;
    private final LocalDateTime thoiGianDuTinh;
    private final String tenTau;
    private final String soToa;
    private final String tenKhoang;
    private final String tenLoaiGhe;
    private final int soGheTrong;

    public ChuyenTauThongTin(String maChuyenTau,
                             String gaDi,
                             String gaDen,
                             Integer thoiGianDi,
                             LocalDateTime thoiGianKhoiHanh,
                             LocalDateTime thoiGianDuTinh,
                             String tenTau,
                             String soToa,
                             String tenKhoang,
                             String tenLoaiGhe,
                             int soGheTrong) {
        this.maChuyenTau = maChuyenTau;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.thoiGianDi = thoiGianDi;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianDuTinh = thoiGianDuTinh;
        this.tenTau = tenTau;
        this.soToa = soToa;
        this.tenKhoang = tenKhoang;
        this.tenLoaiGhe = tenLoaiGhe;
        this.soGheTrong = soGheTrong;
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

    public Integer getThoiGianDi() {
        return thoiGianDi;
    }

    public LocalDateTime getThoiGianKhoiHanh() {
        return thoiGianKhoiHanh;
    }

    public LocalDateTime getThoiGianDuTinh() {
        return thoiGianDuTinh;
    }

    public String getTenTau() {
        return tenTau;
    }

    public String getSoToa() {
        return soToa;
    }

    public String getTenKhoang() {
        return tenKhoang;
    }

    public String getTenLoaiGhe() {
        return tenLoaiGhe;
    }

    public int getSoGheTrong() {
        return soGheTrong;
    }
}