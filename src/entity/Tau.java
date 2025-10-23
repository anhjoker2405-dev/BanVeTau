package entity;

/**
 * Đại diện thông tin tàu phục vụ việc chọn tàu chạy cho chuyến.
 */
public class Tau {
    private final String maTau;
    private final String tenTau;

    public Tau(String maTau, String tenTau) {
        this.maTau = maTau;
        this.tenTau = tenTau;
    }

    public String getMaTau() {
        return maTau;
    }

    public String getTenTau() {
        return tenTau;
    }

    @Override
    public String toString() {
        return tenTau;
    }
}