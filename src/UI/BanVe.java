package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.NumberFormat;
import util.AppSession;
import dao.ChuyenDi_Dao;
import dao.HanhKhach_Dao;
import dao.NhanVien_Dao;
import dao.ThanhToan_Dao;
import dao.SeatAvailabilityDao;
import entity.ChuyenTau;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BanVe extends JPanel {

    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color BLUE_HOVER = new Color(47, 107, 255, 30);
    private static final Color PANEL_BG = new Color(245, 248, 253);

    private final CardLayout wizard = new CardLayout();
    private final JPanel cards = new JPanel(wizard);

    private final ChooseTripPage page1 = new ChooseTripPage();
    private final ManChonGheNgoi page2 = new ManChonGheNgoi();
    private final ManThanhToan page3 = new ManThanhToan();

    private TrainInfo currentTrain;
    private final List<TicketSelection> selections = new ArrayList<>();
    private TripSelectPanel.Trip selectedTrip;
    private Runnable bookingCompletionListener;

    public BanVe() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        // BỎ THANH 1-2-3 (Step Bar)
        // add(stepBar, BorderLayout.NORTH);

        cards.add(page1, "p1");
        cards.add(page2, "p2");
        cards.add(page3, "p3");
        add(cards, BorderLayout.CENTER);
        
        page3.setMode(ManThanhToan.Mode.BOOKING);
        
        page2.addBackActionListener(e -> showStep(1));
        page2.addNextActionListener(e -> handleSeatSelectionNext());
        
        page3.setBackAction(() -> showStep(2));
        page3.setEditTicketsAction(() -> showStep(2));
        page3.setConfirmAction(this::thucHienThanhToan);

        showStep(1);
    }

    private void showStep(int step) {
        switch (step) {
            case 1 -> {
                page1.onShow();
                wizard.show(cards, "p1");
            }
            case 2 -> wizard.show(cards, "p2");
            case 3 -> goToPaymentStep();
        }
    }
    public void setBookingCompletionListener(Runnable listener) {
        this.bookingCompletionListener = listener;
    }

    // ======================= PAGE 1 =======================
    
    private class ChooseTripPage extends JPanel {
        private final CardLayout subCards = new CardLayout();
        private final JPanel subPanel = new JPanel(subCards);

        private final SearchTripPanel searchPanel = new SearchTripPanel();
        private final TripSelectPanel resultPanel = new TripSelectPanel();
        
        private String lastGaDi;
        private String lastGaDen;
        private LocalDate lastNgay;
        private boolean showingResults;

        ChooseTripPage() {
            setOpaque(false);
            setLayout(new BorderLayout());
            add(subPanel, BorderLayout.CENTER);

            subPanel.add(searchPanel, "search");
            subPanel.add(resultPanel, "result");
            subCards.show(subPanel, "search");
            
            resultPanel.onBack(evt -> showSearchForm());
            resultPanel.setTripSelectionListener(trip -> handleTripSelection(trip));

            // Load ga đi / ga đến từ SQL
            try {
                dao.ChuyenDi_Dao dao = new dao.ChuyenDi_Dao();
                java.util.List<String> gaDi = dao.getAllGaDi();
                java.util.List<String> gaDen = dao.getAllGaDen();
                searchPanel.setStations(gaDi, gaDen);
            } catch (Exception ex) {
                ex.printStackTrace();
                // fallback: để trống, người dùng tự gõ
            }

            // Sự kiện tìm kiếm
            searchPanel.onSearch(e -> {
                executeSearch(searchPanel.getGaDi(), searchPanel.getGaDen(), searchPanel.getNgayDi());
            });
        }

        private void executeSearch(String gaDi, String gaDen, LocalDate ngay) {
            if (ngay == null) {
                ngay = LocalDate.now();
            }
            lastGaDi = gaDi;
            lastGaDen = gaDen;
            lastNgay = ngay;

            List<TripSelectPanel.Trip> trips = queryTrips(gaDi, gaDen, ngay);
            resultPanel.setContext(gaDi != null ? gaDi : "", gaDen != null ? gaDen : "", ngay);
            resultPanel.setTrips(trips);
            subCards.show(subPanel, "result");
            showingResults = true;
        }

        private List<TripSelectPanel.Trip> queryTrips(String gaDi, String gaDen, LocalDate ngay) {
            if (ngay == null) {
                return java.util.Collections.emptyList();
            }

            java.time.LocalDateTime from = ngay.atStartOfDay();
            java.time.LocalDateTime to = ngay.atTime(23, 59, 59);
            List<TripSelectPanel.Trip> trips = new ArrayList<>();
            try {
                dao.ChuyenDi_Dao dao = new dao.ChuyenDi_Dao();
                java.util.Date dFrom = java.util.Date.from(from.atZone(java.time.ZoneId.systemDefault()).toInstant());
                java.util.Date dTo = java.util.Date.from(to.atZone(java.time.ZoneId.systemDefault()).toInstant());
                List<ChuyenTau> rs = dao.search(null, gaDi, gaDen, dFrom, dTo);
                for (ChuyenTau cd : rs) {
                    trips.add(new TripSelectPanel.Trip(
                            cd.getMaChuyenTau(),
                            cd.getMaTau(),
                            cd.getTenTau(),
                            cd.getGaDi(),
                            cd.getGaDen(),
                            cd.getThoiGianKhoiHanh(),
                            cd.getThoiGianKetThuc(),
                            cd.getSoGheTrong(),
                            cd.getGiaVe()
                    ));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return trips;
        }

        private void showSearchForm() {
            subCards.show(subPanel, "search");
            showingResults = false;
        }

        void refreshLatestResults() {
            if (showingResults && lastNgay != null) {
                executeSearch(lastGaDi, lastGaDen, lastNgay);
            }
        }

        void onShow() {
            refreshLatestResults();
        }
    }


    // ======================= PAGE 2 =======================
    private void handleTripSelection(TripSelectPanel.Trip trip) {
        if (trip == null) {
            return;
        }
        selectedTrip = trip;
        selections.clear();
        page2.clearSelection();

        LocalDate ngayDi = trip.depart != null ? trip.depart.toLocalDate() : null;
        page2.setRoute(trip.departStation, trip.arriveStation, ngayDi);

        boolean loaded = page2.loadSeatMap(trip.code);
        if (!loaded) {
            return;
        }

        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        currentTrain = new TrainInfo(
                trip.code,
                trip.depart != null ? trip.depart.format(timeFmt) : "",
                trip.arrive != null ? trip.arrive.format(timeFmt) : "",
                trip.departStation + " -> " + trip.arriveStation,
                page2.getCarCount()
        );

        showStep(2);
    }

    private void handleSeatSelectionNext() {
        goToPaymentStep();
    }

    private void goToPaymentStep() {
        if (!syncSelectionsFromSeatPage()) {
            return;
        }
        page3.setTrainInfo(currentTrain);
        page3.setSelections(selections);
        wizard.show(cards, "p3");
    }

    private boolean syncSelectionsFromSeatPage() {
        List<ManChonGheNgoi.SeatSelection> seats = page2.getSelectedSeats();
        if (seats.isEmpty()) {
            JOptionPane.showMessageDialog(BanVe.this, "Chưa có ghế nào được chọn.");
            return false;
        }

        if (currentTrain == null && selectedTrip != null) {
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
            currentTrain = new TrainInfo(
                    selectedTrip.code,
                    selectedTrip.depart != null ? selectedTrip.depart.format(timeFmt) : "",
                    selectedTrip.arrive != null ? selectedTrip.arrive.format(timeFmt) : "",
                    selectedTrip.departStation + " -> " + selectedTrip.arriveStation,
                    page2.getCarCount()
            );
        }

        if (currentTrain == null) {
            JOptionPane.showMessageDialog(BanVe.this, "Không xác định được thông tin chuyến tàu.");
            return false;
        }
        selections.clear();
        
        BigDecimal defaultFare = page2.getFarePerSeat();
        List<ManChonGheNgoi.PassengerInfo> infoList = page2.collectPassengerInfos();
        Map<ManChonGheNgoi.SeatSelection, ManChonGheNgoi.PassengerInfo> infoBySeat = new LinkedHashMap<>();
        for (ManChonGheNgoi.PassengerInfo info : infoList) {
            if (info != null && info.getSeat() != null) {
                infoBySeat.put(info.getSeat(), info);
            }
        }

        for (ManChonGheNgoi.SeatSelection seat : seats) {
            ManChonGheNgoi.PassengerInfo info = infoBySeat.get(seat);
            if (info != null) {
                selections.add(new TicketSelection(currentTrain, info));
            } else {
                selections.add(new TicketSelection(currentTrain, seat, defaultFare));
            }
        }
        return !selections.isEmpty();
    }
    //Xử lý thanh toán & lưu SQL
    /** ĐÃ SỬA: không hard-code NV/HK; lấy đúng từ SQL */
    private void thucHienThanhToan() {
        if (selections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có ghế nào được chọn.");
            return;
        }

        // 1) Lấy mã NV từ phiên đăng nhập (hoặc rơi về NV đầu tiên trong bảng)
        String maNV;
        try {
            NhanVien_Dao nvDao = new NhanVien_Dao();
            String fromSession = AppSession.getMaNV();
            if (fromSession != null && nvDao.exists(fromSession)) {
                maNV = fromSession;
            } else {
                maNV = nvDao.getAnyActiveMaNV();
                if (maNV == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên hợp lệ trong CSDL.");
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra nhân viên: " + ex.getMessage());
            return;
        }

        // 2) Lấy thông tin khách từ vé đầu tiên
        TicketSelection first = selections.get(0);
        String tenHK  = first.getHoTen();
        String sdtHK  = first.getSoDienThoai();
        String cccdHK = first.getCccd();

        if (tenHK == null || tenHK.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Họ tên khách ở phần Chi tiết vé.");
            return;
        }

        String maHK;
        try {
            HanhKhach_Dao hkDao = new HanhKhach_Dao();
            maHK = hkDao.ensure(tenHK, sdtHK, cccdHK);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi tạo/kiểm tra hành khách: " + ex.getMessage());
            return;
        }

        // 3) Danh sách mã ghế
        java.util.List<String> maGheList = new java.util.ArrayList<>();
        for (TicketSelection sel : selections) {
            if (sel.getSeatId() != null) {
                maGheList.add(sel.getSeatId());
            }
        }
        if (maGheList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không xác định được thông tin ghế ngồi để lưu vé.");
            return;
        }

        // 4) Mã chuyến tàu
        String maChuyenTau = selectedTrip != null ? selectedTrip.code
                : (currentTrain != null ? currentTrain.code : null);
        if (maChuyenTau == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã chuyến tàu.");
            return;
        }

        // 5) Lưu hóa đơn + vé
        int tong = page3.getTotalAmount();
        try {
            ThanhToan_Dao service = new ThanhToan_Dao();
            BigDecimal vat = page3.getVatRateDecimal();
            String maKM = page3.getSelectedPromotionId();
            BigDecimal unit = page3.getUnitPriceAfterAdjustments(); // đơn giá/ve sau KM+VAT
            if (unit == null) {
                unit = BigDecimal.ZERO;
            }

            String maHD = service.luuHoaDonVaVe(maNV, maHK, maChuyenTau, maGheList, unit, vat, maKM);
            new SeatAvailabilityDao().refreshForTrip(maChuyenTau);

            JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nMã HĐ: " + maHD + "\nTổng tiền: " + formatVND(tong));
            selections.clear();
            page3.setSelections(java.util.Collections.emptyList());
            showStep(1);
            if (bookingCompletionListener != null) {
                SwingUtilities.invokeLater(bookingCompletionListener);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi khi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================END PAGE 3 =======================
    
    // ======= Common helpers =======
    public static class TrainInfo {
        private final String code;
        private final String depart;
        private final String arrive;
        private final String route;
        private final int carCount;

        TrainInfo(String code, String depart, String arrive, String route, int carCount) {
            this.code = code;
            this.depart = depart;
            this.arrive = arrive;
            this.route = route;
            this.carCount = carCount;
        }

        public String getCode() {
            return code;
        }

        public String getDepart() {
            return depart;
        }

        public String getArrive() {
            return arrive;
        }

        public String getRoute() {
            return route;
        }

        public int getCarCount() {
            return carCount;
        }
    }
    public static class TicketSelection {
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

        TicketSelection(TrainInfo train, ManChonGheNgoi.PassengerInfo info) {
            this.train = train;
            ManChonGheNgoi.SeatSelection seat = info != null ? info.getSeat() : null;
            this.car = seat != null ? seat.getSoToa() : 0;
            this.seatNumber = seat != null ? seat.getSeatDisplayNumber() : 0;
            this.seatId = seat != null ? seat.getMaGhe() : null;
            this.basePrice = info != null && info.getGiaVe() != null ? info.getGiaVe() : BigDecimal.ZERO;
            this.hoTen = trim(info != null ? info.getHoTen() : null);
            this.soDienThoai = trim(info != null ? info.getSoDienThoai() : null);
            this.cccd = trim(info != null ? info.getCccd() : null);
            this.namSinh = trim(info != null ? info.getNamSinh() : null);
            this.maLoaiVe = info != null ? info.getMaLoaiVe() : null;
            this.tenLoaiVe = info != null ? info.getTenLoaiVe() : null;
            this.maGioiTinh = info != null ? info.getMaGioiTinh() : null;
            this.tenGioiTinh = info != null ? info.getTenGioiTinh() : null;
        }

        TicketSelection(TrainInfo train, ManChonGheNgoi.SeatSelection seat, BigDecimal basePrice) {
            this.train = train;
            this.car = seat != null ? seat.getSoToa() : 0;
            this.seatNumber = seat != null ? seat.getSeatDisplayNumber() : 0;
            this.seatId = seat != null ? seat.getMaGhe() : null;
            this.basePrice = basePrice != null ? basePrice : BigDecimal.ZERO;
            this.hoTen = null;
            this.soDienThoai = null;
            this.cccd = null;
            this.namSinh = null;
            this.maLoaiVe = null;
            this.tenLoaiVe = null;
            this.maGioiTinh = null;
            this.tenGioiTinh = null;
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

    private static String formatVND(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        return nf.format(amount) + "₫";
    }
}