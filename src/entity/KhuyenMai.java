package entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class KhuyenMai {
	
	private String maKhuyenMai;
	private String tenKhuyenMai;
	private BigDecimal giamGia;
	private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String moTa;
    
    //Constructor
	public KhuyenMai(String maKhuyenMai, String tenKhuyenMai, BigDecimal giamGia, LocalDateTime ngayBatDau,
			LocalDateTime ngayKetThuc, String moTa) {
		super();
		this.maKhuyenMai = maKhuyenMai;
		this.tenKhuyenMai = tenKhuyenMai;
		this.giamGia = giamGia;
		this.ngayBatDau = ngayBatDau;
		this.ngayKetThuc = ngayKetThuc;
		this.moTa = moTa;
	}

	public KhuyenMai() {
		super();
	}

	//Get & Set
	public String getMaKhuyenMai() {
		return maKhuyenMai;
	}

	public void setMaKhuyenMai(String maKhuyenMai) {
		this.maKhuyenMai = maKhuyenMai;
	}

	public String getTenKhuyenMai() {
		return tenKhuyenMai;
	}

	public void setTenKhuyenMai(String tenKhuyenMai) {
		this.tenKhuyenMai = tenKhuyenMai;
	}

	public BigDecimal getGiamGia() {
		return giamGia;
	}

	public void setGiamGia(BigDecimal giamGia) {
		this.giamGia = giamGia;
	}

	public LocalDateTime getNgayBatDau() {
		return ngayBatDau;
	}

	public void setNgayBatDau(LocalDateTime ngayBatDau) {
		this.ngayBatDau = ngayBatDau;
	}

	public LocalDateTime getNgayKetThuc() {
		return ngayKetThuc;
	}

	public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
		this.ngayKetThuc = ngayKetThuc;
	}

	public String getMoTa() {
		return moTa;
	}

	public void setMoTa(String moTa) {
		this.moTa = moTa;
	}
}
