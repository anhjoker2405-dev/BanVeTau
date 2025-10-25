package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thông tin một khoang tàu và danh sách ghế thuộc khoang đó.
 */
public class KhoangTau {
    private final String maKhoangTau;
    private final String tenKhoangTau;
    private final int soLuongGhe;
    private final String maToa;
    private final List<Ghe> danhSachGhe = new ArrayList<>();

    public KhoangTau(String maKhoangTau, String tenKhoangTau, int soLuongGhe, String maToa) {
        this.maKhoangTau = maKhoangTau;
        this.tenKhoangTau = tenKhoangTau;
        this.soLuongGhe = soLuongGhe;
        this.maToa = maToa;
    }

    public String getMaKhoangTau() {
        return maKhoangTau;
    }

    public String getTenKhoangTau() {
        return tenKhoangTau;
    }

    public int getSoLuongGhe() {
        return soLuongGhe;
    }

    public String getMaToa() {
        return maToa;
    }

    public List<Ghe> getDanhSachGhe() {
        return Collections.unmodifiableList(danhSachGhe);
    }

    public void addGhe(Ghe ghe) {
        danhSachGhe.add(ghe);
    }
}