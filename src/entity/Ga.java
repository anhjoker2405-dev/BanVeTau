package entity;

/**
 * Đại diện thông tin ga tàu cơ bản dùng cho lựa chọn trong giao diện.
 */
public class Ga {
    private final String maGa;
    private final String tenGa;

    public Ga(String maGa, String tenGa) {
        this.maGa = maGa;
        this.tenGa = tenGa;
    }

    public String getMaGa() {
        return maGa;
    }

    public String getTenGa() {
        return tenGa;
    }

    @Override
    public String toString() {
        return tenGa;
    }
}