package entity;
/**
 * Đại diện một ghế cụ thể trong khoang tàu.
 */
public class Ghe {
    private final String maGhe;
    private final String soGhe;
    private final String maLoaiGhe;
    private final String maKhoangTau;
    private final boolean daDat;
    private int thuTuHienThi;

    public Ghe(String maGhe, String soGhe, String maLoaiGhe, String maKhoangTau, boolean daDat) {
        this.maGhe = maGhe;
        this.soGhe = soGhe;
        this.maLoaiGhe = maLoaiGhe;
        this.maKhoangTau = maKhoangTau;
        this.daDat = daDat;
    }

    public String getMaGhe() {
        return maGhe;
    }

    public String getSoGhe() {
        return soGhe;
    }

    public String getMaLoaiGhe() {
        return maLoaiGhe;
    }

    public String getMaKhoangTau() {
        return maKhoangTau;
    }
    
    public boolean isDaDat() {
        return daDat;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }
    
    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public int getSoGheAsInt() {
        try {
            return Integer.parseInt(soGhe.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
