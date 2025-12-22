package entity;

import java.util.Objects;
// -- Mô tả một ghế được chọn trên màn hình bán vé, bao gồm thông tin toa/khoang --
public class SeatSelection {
    private final int soToa;
    private final String maToa;
    private final String tenKhoang;
    private final String maKhoang;
    private final Ghe ghe;

    public SeatSelection(ToaTau toa, KhoangTau khoang, Ghe ghe) {
        this.soToa = toa.getSoToa();
        this.maToa = toa.getMaToa();
        this.tenKhoang = khoang.getTenKhoangTau();
        this.maKhoang = khoang.getMaKhoangTau();
        this.ghe = ghe;
    }

    public int getSoToa() {
        return soToa;
    }

    public String getMaToa() {
        return maToa;
    }

    public String getTenKhoang() {
        return tenKhoang;
    }

    public String getMaKhoang() {
        return maKhoang;
    }

    public Ghe getGhe() {
        return ghe;
    }

    public int getSeatDisplayNumber() {
        int order = ghe.getThuTuHienThi();
        if (order > 0) {
            return order;
        }
        return ghe.getSoGheAsInt();
    }

    public String getMaGhe() {
        return ghe.getMaGhe();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeatSelection)) return false;
        SeatSelection that = (SeatSelection) o;
        return Objects.equals(ghe.getMaGhe(), that.ghe.getMaGhe());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ghe.getMaGhe());
    }
}