package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thông tin một toa tàu và các khoang đi kèm.
 */
public class ToaTau {
    private final String maToa;
    private final int soToa;
    private final String maTau;
    private final int soKhoangTau;
    private final List<KhoangTau> danhSachKhoang = new ArrayList<>();

    public ToaTau(String maToa, int soToa, String maTau, int soKhoangTau) {
        this.maToa = maToa;
        this.soToa = soToa;
        this.maTau = maTau;
        this.soKhoangTau = soKhoangTau;
    }

    public String getMaToa() {
        return maToa;
    }

    public int getSoToa() {
        return soToa;
    }

    public String getMaTau() {
        return maTau;
    }

    public int getSoKhoangTau() {
        return soKhoangTau;
    }

    public List<KhoangTau> getDanhSachKhoang() {
        return Collections.unmodifiableList(danhSachKhoang);
    }

    public void addKhoang(KhoangTau khoang) {
        danhSachKhoang.add(khoang);
    }
}   