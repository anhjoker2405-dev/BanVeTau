package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thông tin chi tiết của một vé đã mua, phục vụ chức năng đổi vé.
 */
public class TicketExchangeInfo {
    private final String maVe;
    private final String hoTen;
    private final String soDienThoai;
    private final String namSinh;
    private final String cccd;
    private final String maHK;
    private final String maGioiTinh;
    private final String tenGioiTinh;
    private final String maLoaiVe;
    private final String tenLoaiVe;
    private final String loaiGhe;
    private final String soGhe;
    private final int soToa;
    private final String tenKhoang;
    private final BigDecimal giaVe;
    private final String maChuyenTau;
    private final String maTau;
    private final String tenTau;
    private final String gaDi;
    private final String gaDen;
    private final LocalDateTime thoiGianKhoiHanh;
    private final LocalDateTime thoiGianKetThuc;
    private final String trangThai;
    private final String maGhe;

    public TicketExchangeInfo(String maVe, String hoTen, String soDienThoai, String namSinh,
                              String cccd, String maHK, String maGioiTinh, String tenGioiTinh,
                              String maLoaiVe, String tenLoaiVe, String loaiGhe, String soGhe,
                              int soToa, String tenKhoang, BigDecimal giaVe,
                              String maChuyenTau, String maTau, String tenTau,
                              String gaDi, String gaDen,
                              LocalDateTime thoiGianKhoiHanh, LocalDateTime thoiGianKetThuc,
                              String trangThai, String maGhe) {
        this.maVe = maVe;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.namSinh = namSinh;
        this.cccd = cccd;
        this.maHK = maHK;
        this.maGioiTinh = maGioiTinh;
        this.tenGioiTinh = tenGioiTinh;
        this.maLoaiVe = maLoaiVe;
        this.tenLoaiVe = tenLoaiVe;
        this.loaiGhe = loaiGhe;
        this.soGhe = soGhe;
        this.soToa = soToa;
        this.tenKhoang = tenKhoang;
        this.giaVe = giaVe;
        this.maChuyenTau = maChuyenTau;
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.trangThai = trangThai;
        this.maGhe = maGhe;
    }

    public String getMaVe() {
        return maVe;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getNamSinh() {
        return namSinh;
    }

    public String getCccd() {
        return cccd;
    }

    public String getMaHK() {
        return maHK;
    }

    public String getMaGioiTinh() {
        return maGioiTinh;
    }

    public String getTenGioiTinh() {
        return tenGioiTinh;
    }

    public String getMaLoaiVe() {
        return maLoaiVe;
    }

    public String getTenLoaiVe() {
        return tenLoaiVe;
    }

    public String getLoaiGhe() {
        return loaiGhe;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public int getSoToa() {
        return soToa;
    }

    public String getTenKhoang() {
        return tenKhoang;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public String getMaChuyenTau() {
        return maChuyenTau;
    }

    public String getMaTau() {
        return maTau;
    }

    public String getTenTau() {
        return tenTau;
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

    public String getTrangThai() {
        return trangThai;
    }

    public String getMaGhe() {
        return maGhe;
    }
}