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
import java.util.Optional;
import java.text.NumberFormat;
import util.AppSession;
import dao.ChuyenDi_Dao;
import dao.HanhKhach_Dao;
import dao.HoaDonPdfDao;
import dao.NhanVien_Dao;
import dao.ThanhToan_Dao;
import dao.SeatAvailabilityDao;
import dao.TicketPdfDao;
import entity.ChuyenTau;
import entity.PassengerInfo;
import entity.SeatSelection;
import entity.TicketSelection;
import entity.TrainInfo;
import entity.InvoicePdfInfo;
import entity.TicketPdfInfo;
import java.math.BigDecimal;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import util.TicketPdfExporter;
import util.HDPdfExporter;

public class BanVe extends JPanel {

    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color BLUE_HOVER = new Color(47, 107, 255, 30);
    private static final Color PANEL_BG = new Color(245, 248, 253);

    private final CardLayout wizard = new CardLayout();
    private final JPanel cards = new JPanel(wizard);

    private final ChooseTripPage page1 = new ChooseTripPage();
    private final ManChonGheNgoi page2 = new ManChonGheNgoi();
    private final ManThanhToan page3 = new ManThanhToan();
    private final ManHinhXuatPDF exportPanel = new ManHinhXuatPDF();

    private TrainInfo currentTrain;
    private final List<TicketSelection> selections = new ArrayList<>();
    private ManChonChuyen.Trip selectedTrip;
    private Runnable bookingCompletionListener;

    public BanVe() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        // BỎ THANH 1-2-3 (Step Bar)
        // add(stepBar, BorderLayout.NORTH);

        cards.add(page1, "p1");
        cards.add(page2, "p2");
        cards.add(page3, "p3");
        cards.add(exportPanel, "p4");
        add(cards, BorderLayout.CENTER);
        
        page3.setMode(ManThanhToan.Mode.BOOKING);
        
        page2.addBackActionListener(e -> showStep(1));
        page2.addNextActionListener(e -> handleSeatSelectionNext());
        
        page3.setBackAction(() -> showStep(2));
        page3.setEditTicketsAction(() -> showStep(2));
        page3.setConfirmAction(this::thucHienThanhToan);
        
        exportPanel.getBtnInVe().addActionListener(e -> handleExportTicket(exportPanel, pendingTicketIds));
        exportPanel.getBtnInHoaDon().addActionListener(e ->
                handleExportInvoice(exportPanel, pendingInvoiceId));
        exportPanel.getBtnBackHome().addActionListener(e -> {
            showStep(1);
            pendingTicketIds = java.util.Collections.emptyList();
            exportPanel.setInfoMessage(" ");
            pendingInvoiceId = null;
        });

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

        private final ManTimChuyen searchPanel = new ManTimChuyen();
        private final ManChonChuyen resultPanel = new ManChonChuyen();
        
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

            List<ManChonChuyen.Trip> trips = queryTrips(gaDi, gaDen, ngay);
            resultPanel.setContext(gaDi != null ? gaDi : "", gaDen != null ? gaDen : "", ngay);
            resultPanel.setTrips(trips);
            subCards.show(subPanel, "result");
            showingResults = true;
        }

        private List<ManChonChuyen.Trip> queryTrips(String gaDi, String gaDen, LocalDate ngay) {
            if (ngay == null) {
                return java.util.Collections.emptyList();
            }

            java.time.LocalDateTime from = ngay.atStartOfDay();
            java.time.LocalDateTime to = ngay.atTime(23, 59, 59);
            List<ManChonChuyen.Trip> trips = new ArrayList<>();
            try {
                dao.ChuyenDi_Dao dao = new dao.ChuyenDi_Dao();
                java.util.Date dFrom = java.util.Date.from(from.atZone(java.time.ZoneId.systemDefault()).toInstant());
                java.util.Date dTo = java.util.Date.from(to.atZone(java.time.ZoneId.systemDefault()).toInstant());
                List<ChuyenTau> rs = dao.search(null, gaDi, gaDen, dFrom, dTo);
                for (ChuyenTau cd : rs) {
                    trips.add(new ManChonChuyen.Trip(
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
    private void handleTripSelection(ManChonChuyen.Trip trip) {
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
        List<SeatSelection> seats = page2.getSelectedSeats();
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
        List<PassengerInfo> infoList = page2.collectPassengerInfos();
        Map<SeatSelection, PassengerInfo> infoBySeat = new LinkedHashMap<>();
        for (PassengerInfo info : infoList) {
            if (info != null && info.getSeat() != null) {
                infoBySeat.put(info.getSeat(), info);
            }
        }

        for (SeatSelection seat : seats) {
            PassengerInfo info = infoBySeat.get(seat);
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
                : (currentTrain != null ? currentTrain.getCode() : null);
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

            ThanhToan_Dao.PaymentResult paymentResult = service.luuHoaDonVaVe(
                    maNV, maHK, maChuyenTau, maGheList, unit, vat, maKM);
            new SeatAvailabilityDao().refreshForTrip(maChuyenTau);

            selections.clear();
            page3.setSelections(java.util.Collections.emptyList());
            if (bookingCompletionListener != null) {
                SwingUtilities.invokeLater(bookingCompletionListener);
            }
            
            SwingUtilities.invokeLater(() -> showTicketExportScreen(paymentResult, tong));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi khi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private java.util.List<String> pendingTicketIds = java.util.Collections.emptyList();
    private String pendingInvoiceId;

    private void showTicketExportScreen(ThanhToan_Dao.PaymentResult paymentResult, int tongTien) {
        pendingInvoiceId = paymentResult != null ? paymentResult.getMaHoaDon() : null;
        if (paymentResult == null || paymentResult.getMaVeList().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Thanh toán thành công!\nTổng tiền: " + formatVND(tongTien));
            showStep(1);
            return;
        }

        List<String> maVeList = paymentResult.getMaVeList();
        String maHD = paymentResult.getMaHoaDon();
        pendingInvoiceId = maHD;
        String summary = String.format(
                "Đã tạo %d vé. Mã HĐ: %s. Tổng tiền: %s",
                maVeList.size(),
                maHD != null ? maHD : "-",
                formatVND(tongTien));

        pendingTicketIds = maVeList;
        exportPanel.setInfoMessage(summary);
        wizard.show(cards, "p4");
    }
    
    private void handleExportInvoice(java.awt.Component parent, String maHoaDon) {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Không có mã hóa đơn để in.");
            return;
        }

        try {
            HoaDonPdfDao dao = new HoaDonPdfDao();
            Optional<InvoicePdfInfo> infoOpt = dao.findByMaHoaDon(maHoaDon);
            if (infoOpt.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Không tìm thấy dữ liệu cho hóa đơn " + maHoaDon,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Path exportDir = Paths.get("/BanVeTauv2/HoaDon");
            Files.createDirectories(exportDir);
            Path output = exportDir.resolve(maHoaDon + ".pdf");

            HDPdfExporter.export(infoOpt.get(), output.toString());

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(output.toFile());
                }
            } catch (Exception ioe) {
                JOptionPane.showMessageDialog(parent,
                        "Không thể mở tệp vừa lưu: " + ioe.getMessage(),
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(parent,
                    "Đã xuất hóa đơn: " + output.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Có lỗi khi xuất hóa đơn: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportTicket(java.awt.Component parent, List<String> maVeList) {
        if (maVeList == null || maVeList.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "Không có vé nào để in.");
            return;
        }

        String selectedTicket = maVeList.get(0);
        if (maVeList.size() > 1) {
            Object choice = JOptionPane.showInputDialog(parent,
                    "Chọn mã vé cần in",
                    "In vé",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    maVeList.toArray(),
                    selectedTicket);
            if (choice == null) {
                return;
            }
            selectedTicket = choice.toString();
        }

        try {
            TicketPdfDao dao = new TicketPdfDao();
            Optional<TicketPdfInfo> infoOpt = dao.findByMaVe(selectedTicket);
            if (infoOpt.isEmpty()) {
                JOptionPane.showMessageDialog(parent,
                        "Không tìm thấy dữ liệu cho mã vé " + selectedTicket,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Path exportDir = Paths.get("/BanVeTauv2/Ve");
            Files.createDirectories(exportDir);
            Path output = exportDir.resolve(selectedTicket + ".pdf");

            TicketPdfExporter.export(infoOpt.get(), output.toString());

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(output.toFile());
                }
            } catch (Exception ioe) {
                // Thông báo nhưng không chặn quy trình lưu
                JOptionPane.showMessageDialog(parent,
                        "Không thể mở tệp vừa lưu: " + ioe.getMessage(),
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(parent,
                    "Đã xuất vé: " + output.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Có lỗi khi xuất vé: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =======================END PAGE =======================

    private static String formatVND(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        return nf.format(amount) + "₫";
    }
}