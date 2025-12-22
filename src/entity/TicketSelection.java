package entity;

import java.math.BigDecimal;
// -- Dữ liệu vé đã chọn chuyển sang bước thanh toán và in ấn --
public class TicketSelection {
    private final TrainInfo train;
    private final int car;
    private final int seatNumber;
    private final String seatId;
    private final BigDecimal basePrice;
    private final String hoTen;
    private final String soDienThoai;
    private final String cccd;
    private final String namSinh;
    private final String maLoaiVe;
    private final String tenLoaiVe;
    private final String maGioiTinh;
    private final String tenGioiTinh;

    public TicketSelection(TrainInfo train, PassengerInfo info) {
        this(train,
                info != null ? info.getSeat() : null,
                info != null ? info.getGiaVe() : BigDecimal.ZERO,
                info != null ? info.getHoTen() : null,
                info != null ? info.getSoDienThoai() : null,
                info != null ? info.getCccd() : null,
                info != null ? info.getNamSinh() : null,
                info != null ? info.getMaLoaiVe() : null,
                info != null ? info.getTenLoaiVe() : null,
                info != null ? info.getMaGioiTinh() : null,
                info != null ? info.getTenGioiTinh() : null);
    }

    public TicketSelection(TrainInfo train, SeatSelection seat, BigDecimal basePrice) {
        this(train, seat, basePrice, null, null, null, null, null, null, null, null);
    }

    private TicketSelection(TrainInfo train, SeatSelection seat, BigDecimal basePrice,
                            String hoTen, String soDienThoai, String cccd, String namSinh,
                            String maLoaiVe, String tenLoaiVe, String maGioiTinh, String tenGioiTinh) {
        this.train = train;
        this.car = seat != null ? seat.getSoToa() : 0;
        this.seatNumber = seat != null ? seat.getSeatDisplayNumber() : 0;
        this.seatId = seat != null ? seat.getMaGhe() : null;
        this.basePrice = basePrice != null ? basePrice : BigDecimal.ZERO;
        this.hoTen = trim(hoTen);
        this.soDienThoai = trim(soDienThoai);
        this.cccd = trim(cccd);
        this.namSinh = trim(namSinh);
        this.maLoaiVe = maLoaiVe;
        this.tenLoaiVe = tenLoaiVe;
        this.maGioiTinh = maGioiTinh;
        this.tenGioiTinh = tenGioiTinh;
    }

    private static String trim(String value) {
        return value != null ? value.trim() : null;
    }

    public TrainInfo getTrain() {
        return train;
    }

    public int getCar() {
        return car;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getSeatId() {
        return seatId;
    }

    public BigDecimal getBasePrice() {
        return basePrice != null ? basePrice : BigDecimal.ZERO;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public String getCccd() {
        return cccd;
    }

    public String getNamSinh() {
        return namSinh;
    }

    public String getMaLoaiVe() {
        return maLoaiVe;
    }

    public String getTenLoaiVe() {
        return tenLoaiVe;
    }

    public String getMaGioiTinh() {
        return maGioiTinh;
    }

    public String getTenGioiTinh() {
        return tenGioiTinh;
    }
}