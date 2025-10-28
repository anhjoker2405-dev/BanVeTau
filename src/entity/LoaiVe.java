package entity;

public class LoaiVe {
    private String maLoaiVe;
    private String tenLoaiVe;
    private String moTaLoaiVe;

    public LoaiVe() {
    }

    public LoaiVe(String maLoaiVe, String tenLoaiVe, String moTaLoaiVe) {
        this.maLoaiVe = maLoaiVe;
        this.tenLoaiVe = tenLoaiVe;
        this.moTaLoaiVe = moTaLoaiVe;
    }

    public String getMaLoaiVe() {
        return maLoaiVe;
    }

    public void setMaLoaiVe(String maLoaiVe) {
        this.maLoaiVe = maLoaiVe;
    }

    public String getTenLoaiVe() {
        return tenLoaiVe;
    }

    public void setTenLoaiVe(String tenLoaiVe) {
        this.tenLoaiVe = tenLoaiVe;
    }

    public String getMoTaLoaiVe() {
        return moTaLoaiVe;
    }

    public void setMoTaLoaiVe(String moTaLoaiVe) {
        this.moTaLoaiVe = moTaLoaiVe;
    }
}