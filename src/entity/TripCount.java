package entity;

/** Số lượt vé theo chuyến tàu (dùng cho Top chuyến đi nhiều nhất) */
public class TripCount {
    private final String maChuyenTau;
    private final String tuyen;  // "Ga A đến Ga B"
    private final int soVe;

    public TripCount(String maChuyenTau, String tuyen, int soVe) {
        this.maChuyenTau = maChuyenTau;
        this.tuyen = tuyen;
        this.soVe = soVe;
    }

    public String getMaChuyenTau() { return maChuyenTau; }
    public String getTuyen()       { return tuyen; }
    public int getSoVe()           { return soVe; }
}
