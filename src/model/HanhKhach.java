/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;


public class HanhKhach {
    private String maHK;
    private String tenHK;
    private String soDienThoai;
    private String cccd;
    private String gioiTinh; // "Nam" hoặc "Nữ"

    public HanhKhach() {}
    public HanhKhach(String maHK, String tenHK, String soDienThoai, String cccd, String gioiTinh) {
        this.maHK = maHK;
        this.tenHK = tenHK;
        this.soDienThoai = soDienThoai;
        this.cccd = cccd;
        this.gioiTinh = gioiTinh;
}
    public String getMaHK() { return maHK; }
    public void setMaHK(String maHK) { this.maHK = maHK; }
    public String getTenHK() { return tenHK; }
    public void setTenHK(String tenHK) { this.tenHK = tenHK; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getCccd() { return cccd; }
    public void setCccd(String cccd) { this.cccd = cccd; }
    public String getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(String gioiTinh) { this.gioiTinh = gioiTinh; }
}
