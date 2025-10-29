package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Thông tin đầy đủ của 1 vé cần để in PDF.
 */
public class TicketPdfInfo {

    private final String maVe;
    private final String gaDi;
    private final String gaDen;
    private final String maTau;
    private final String tenTau;
    private final int soToa;
    private final String tenKhoang;
    private final String soGhe;
    private final String maLoaiGhe;
    private final String tenLoaiGhe;
    private final String maLoaiVe;
    private final String tenLoaiVe;
    private final String tenHanhKhach;
    private final String cccd;
    private final BigDecimal giaVe;
    private final LocalDateTime thoiGianKhoiHanh;

    public TicketPdfInfo(String maVe,
                         String gaDi,
                         String gaDen,
                         String maTau,
                         String tenTau,
                         int soToa,
                         String tenKhoang,
                         String soGhe,
                         String maLoaiGhe,
                         String tenLoaiGhe,
                         String maLoaiVe,
                         String tenLoaiVe,
                         String tenHanhKhach,
                         String cccd,
                         BigDecimal giaVe,
                         LocalDateTime thoiGianKhoiHanh) {
        this.maVe = maVe;
        this.gaDi = gaDi;
        this.gaDen = gaDen;
        this.maTau = maTau;
        this.tenTau = tenTau;
        this.soToa = soToa;
        this.tenKhoang = tenKhoang;
        this.soGhe = soGhe;
        this.maLoaiGhe = maLoaiGhe;
        this.tenLoaiGhe = tenLoaiGhe;
        this.maLoaiVe = maLoaiVe;
        this.tenLoaiVe = tenLoaiVe;
        this.tenHanhKhach = tenHanhKhach;
        this.cccd = cccd;
        this.giaVe = giaVe;
        this.thoiGianKhoiHanh = thoiGianKhoiHanh;
    }

    public String getMaVe() {
        return maVe;
    }

    public String getGaDi() {
        return gaDi;
    }

    public String getGaDen() {
        return gaDen;
    }

    public String getMaTau() {
        return maTau;
    }

    public String getTenTau() {
        return tenTau;
    }

    public int getSoToa() {
        return soToa;
    }

    public String getTenKhoang() {
        return tenKhoang;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public String getMaLoaiGhe() {
        return maLoaiGhe;
    }

    public String getTenLoaiGhe() {
        return tenLoaiGhe;
    }

    public String getMaLoaiVe() {
        return maLoaiVe;
    }

    public String getTenLoaiVe() {
        return tenLoaiVe;
    }

    public String getTenHanhKhach() {
        return tenHanhKhach;
    }

    public String getCccd() {
        return cccd;
    }

    public BigDecimal getGiaVe() {
        return giaVe;
    }

    public LocalDateTime getThoiGianKhoiHanh() {
        return thoiGianKhoiHanh;
    }

    public String getTrainDisplay() {
        if (tenTau != null && !tenTau.isBlank()) {
            return tenTau;
        }
        return maTau != null ? maTau : "";
    }

    public String getCoachDisplay() {
        return soToa > 0 ? String.valueOf(soToa) : "";
    }

    public String getSeatDisplay() {
        String seat = soGhe != null ? soGhe.trim() : "";
        String khoang = tenKhoang != null ? tenKhoang.trim() : "";

        boolean khoangHasLabel = !khoang.isEmpty() && khoang.toLowerCase(Locale.ROOT).contains("khoang");
        StringBuilder sb = new StringBuilder();
        if (!seat.isEmpty()) {
            sb.append("Số ").append(seat);
        }
        if (!khoang.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            if (khoangHasLabel) {
                sb.append(khoang);
            } else {
                sb.append("Khoang ").append(khoang);
            }
        }
        return sb.toString();
    }

    public String getSeatClassDisplay() {
        if (maLoaiGhe != null) {
            if (maLoaiGhe.equalsIgnoreCase("LG-01")) {
                return "Ngồi mềm điều hòa";
            }
            if (maLoaiGhe.equalsIgnoreCase("LG-02")) {
                return "Giường nằm điều hòa";
            }
        }

        if (soToa > 0) {
            if (soToa % 2 != 0) {
                return "Ngồi mềm điều hòa";
            }
            if (soToa % 2 == 0) {
                return "Giường nằm điều hòa";
            }
        }

        if (tenLoaiGhe != null && !tenLoaiGhe.isBlank()) {
            return tenLoaiGhe;
        }
        return "";
    }

    public String getNgayDiDisplay() {
        if (thoiGianKhoiHanh == null) {
            return "";
        }
        return thoiGianKhoiHanh.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getGioDiDisplay() {
        if (thoiGianKhoiHanh == null) {
            return "";
        }
        return thoiGianKhoiHanh.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}