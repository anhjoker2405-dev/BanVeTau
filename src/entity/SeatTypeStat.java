package entity;

import java.math.BigDecimal;

/** Thống kê theo loại ghế (số vé + doanh thu) */
public class SeatTypeStat {
    private final String tenLoaiGhe;
    private final int soVe;
    private final BigDecimal doanhThu;

    public SeatTypeStat(String tenLoaiGhe, int soVe, BigDecimal doanhThu) {
        this.tenLoaiGhe = tenLoaiGhe;
        this.soVe = soVe;
        this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
    }

    public String getTenLoaiGhe() { return tenLoaiGhe; }
    public int getSoVe()          { return soVe; }
    public BigDecimal getDoanhThu(){ return doanhThu; }
}
