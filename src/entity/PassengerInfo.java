package entity;

import java.math.BigDecimal;

public class PassengerInfo {
    private final SeatSelection seat;
    private final String hoTen;
    private final String soDienThoai;
    private final String cccd;
    private final String namSinh;
    private final String maGioiTinh;
    private final String tenGioiTinh;
    private final String maLoaiVe;
    private final String tenLoaiVe;
    private final BigDecimal giaVe;

    public PassengerInfo(SeatSelection seat, String hoTen, String soDienThoai, String cccd, String namSinh,
                         String maGioiTinh, String tenGioiTinh, String maLoaiVe, String tenLoaiVe, BigDecimal giaVe) {
        this.seat = seat;
        this.hoTen = hoTen;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.namSinh = namSinh;
        this.maGioiTinh = maGioiTinh;
        this.tenGioiTinh = tenGioiTinh;
        this.maLoaiVe = maLoaiVe;
        this.tenLoaiVe = tenLoaiVe;
        this.giaVe = giaVe;
    }

    public SeatSelection getSeat() {
        return seat;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public String getNamSinh() {
        return namSinh;
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

    public BigDecimal getGiaVe() {
        return giaVe;
    }
}