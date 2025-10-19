package entity;

public class Ghe {
    private String maGhe;
    private int soGhe;
    private String loaiGhe;
    private String maKhoangTau;
    private boolean booked;

    public Ghe(String maGhe, int soGhe, String loaiGhe, String maKhoangTau, boolean booked) {
        this.maGhe = maGhe;
        this.soGhe = soGhe;
        this.loaiGhe = loaiGhe;
        this.maKhoangTau = maKhoangTau;
        this.booked = booked;
    }

    
    
    
    public void setMaGhe(String maGhe) {
		this.maGhe = maGhe;
	}




	public void setSoGhe(int soGhe) {
		this.soGhe = soGhe;
	}




	public void setLoaiGhe(String loaiGhe) {
		this.loaiGhe = loaiGhe;
	}




	public void setMaKhoangTau(String maKhoangTau) {
		this.maKhoangTau = maKhoangTau;
	}

	


	public String getMaGhe() { return maGhe; }
    public int getSoGhe() { return soGhe; }
    public String getLoaiGhe() { return loaiGhe; }
    public String getMaKhoangTau() { return maKhoangTau; }
    public boolean isBooked() { return booked; }

    public void setBooked(boolean b) { 
    	this.booked = b; }
}