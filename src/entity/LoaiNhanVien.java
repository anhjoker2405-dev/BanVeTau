package entity;

public class LoaiNhanVien {
    private String maLoaiNV;
    private String moTa;

    public LoaiNhanVien() {
    }

    public LoaiNhanVien(String maLoaiNV, String moTa) {
        this.maLoaiNV = maLoaiNV;
        this.moTa = moTa;
    }

    public String getMaLoaiNV() {
        return maLoaiNV;
    }

    public void setMaLoaiNV(String maLoaiNV) {
        this.maLoaiNV = maLoaiNV;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }
}