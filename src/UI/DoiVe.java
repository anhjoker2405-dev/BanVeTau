package ui;

import dao.ChuyenDi_Dao;
import dao.DoiVe_Dao;
import dao.NhanVien_Dao;
import dao.ThanhToan_Dao;
import entity.ChuyenTau;
import entity.TicketExchangeInfo;
import util.AppSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Luồng đổi vé: tra cứu vé cũ → chọn chuyến/ghế mới → thanh toán phần chênh lệch.
 */
public class DoiVe extends JPanel {

    private static final String CARD_SEARCH = "search";
    private static final String CARD_TRIP = "trip";
    private static final String CARD_SEAT = "seat";
    private static final String CARD_PAY = "pay";

    private final CardLayout wizard = new CardLayout();
    private final JPanel cards = new JPanel(wizard);

    private final SearchSection searchSection = new SearchSection();
    private final ChooseTripPage chooseTripPage = new ChooseTripPage();
    private final ManChonGheNgoi seatPage = new ManChonGheNgoi();
    private final ManThanhToan paymentPage = new ManThanhToan();

    private final List<BanVe.TicketSelection> selections = new ArrayList<>();
    private final NumberFormat currencyFormat;

    private TicketExchangeInfo currentTicket;
    private TripSelectPanel.Trip selectedTrip;
    private BanVe.TrainInfo currentTrain;
    private BigDecimal exchangeFeeRate;

    public DoiVe() {
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);


        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7FB));

        paymentPage.setMode(ManThanhToan.Mode.EXCHANGE);

        cards.add(searchSection, CARD_SEARCH);
        cards.add(chooseTripPage, CARD_TRIP);
        cards.add(seatPage, CARD_SEAT);
        cards.add(paymentPage, CARD_PAY);
        add(cards, BorderLayout.CENTER);

        searchSection.getSearchButton().addActionListener(e -> handleSearchTicket());
        searchSection.addEnterKeyListener(e -> handleSearchTicket());
        searchSection.getSelectNewButton().addActionListener(e -> startExchangeFlow());

        seatPage.addBackActionListener(e -> showStep(CARD_TRIP));
        seatPage.addNextActionListener(e -> handleSeatSelectionNext());

        paymentPage.setBackAction(() -> showStep(CARD_SEAT));
        paymentPage.setEditTicketsAction(() -> showStep(CARD_SEAT));
        paymentPage.setConfirmAction(this::thucHienDoiVe);

        showStep(CARD_SEARCH);
    }

    private void showStep(String card) {
        if (CARD_TRIP.equals(card)) {
            chooseTripPage.onShow();
        }
        wizard.show(cards, card);
    }

    private void handleSearchTicket() {
        String maVe = searchSection.getMaVeInput();
        if (maVe == null || maVe.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã vé cần đổi.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            TicketExchangeInfo info = new DoiVe_Dao().findByMaVe(maVe.trim());
            if (info == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy vé với mã " + maVe + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                currentTicket = null;
                searchSection.clearTicket();
                searchSection.setSelectNewEnabled(false);
                return;
            }
            currentTicket = info;
            searchSection.setTicket(info);
            if (!"Đã bán".equalsIgnoreCase(info.getTrangThai())) {
                searchSection.setSelectNewEnabled(false);
                JOptionPane.showMessageDialog(this,
                        "Vé hiện tại không ở trạng thái 'Đã bán' nên không thể đổi.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            searchSection.setSelectNewEnabled(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tra cứu vé: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startExchangeFlow() {
        if (currentTicket == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng tra cứu vé cần đổi trước.");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        BigDecimal feeRate = determineFeeRate(now, currentTicket.getThoiGianKhoiHanh());
        if (feeRate == null) {
            JOptionPane.showMessageDialog(this,
                    "Vé không đạt điều kiện để đổi (phải đổi trước giờ khởi hành tối thiểu 4 giờ).",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        exchangeFeeRate = feeRate;

        selections.clear();
        selectedTrip = null;
        currentTrain = null;
        paymentPage.setExchangeBreakdown(null);
        paymentPage.setSelections(Collections.emptyList());

        seatPage.clearSelection();
        seatPage.setCommonPassengerInfo(
                currentTicket.getHoTen(),
                currentTicket.getSoDienThoai(),
                currentTicket.getCccd(),
                currentTicket.getMaGioiTinh()
        );

        chooseTripPage.prepareForTicket(currentTicket);
        showStep(CARD_TRIP);
    }

    private void handleSeatSelectionNext() {
        goToPaymentStep();
    }

    private void goToPaymentStep() {
        if (!syncSelectionsFromSeatPage()) {
            return;
        }
        paymentPage.setTrainInfo(currentTrain);
        paymentPage.setSelections(selections);
        paymentPage.setExchangeBreakdown(buildExchangeBreakdown());
        showStep(CARD_PAY);
    }

    private boolean syncSelectionsFromSeatPage() {
        if (currentTicket == null) {
            JOptionPane.showMessageDialog(this, "Chưa có vé cũ để đối chiếu.");
            return false;
        }
        List<ManChonGheNgoi.SeatSelection> seats = seatPage.getSelectedSeats();
        if (seats.size() != 1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đúng 1 ghế mới để đổi vé.");
            return false;
        }
        ManChonGheNgoi.SeatSelection seat = seats.get(0);
        BigDecimal fare = seatPage.getFarePerSeat();
        if (fare == null) {
            fare = BigDecimal.ZERO;
        }
        ManChonGheNgoi.PassengerInfo passengerInfo = new ManChonGheNgoi.PassengerInfo(
                seat,
                currentTicket.getHoTen(),
                currentTicket.getSoDienThoai(),
                currentTicket.getCccd(),
                currentTicket.getNamSinh(),
                currentTicket.getMaGioiTinh(),
                currentTicket.getTenGioiTinh(),
                currentTicket.getMaLoaiVe(),
                currentTicket.getTenLoaiVe(),
                fare
        );
        selections.clear();
        selections.add(new BanVe.TicketSelection(currentTrain, passengerInfo));
        return true;
    }

    private ManThanhToan.ExchangeBreakdown buildExchangeBreakdown() {
        if (currentTicket == null || selections.isEmpty()) {
            return null;
        }
        BigDecimal oldFare = safe(currentTicket.getGiaVe());
        BigDecimal newFare = safe(selections.get(0).getBasePrice());
        BigDecimal feeRate = exchangeFeeRate != null ? exchangeFeeRate : BigDecimal.ZERO;
        BigDecimal feeAmount = oldFare.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal differenceRaw = newFare.subtract(oldFare);
        BigDecimal difference = differenceRaw.setScale(0, RoundingMode.HALF_UP);
        BigDecimal payableDifference = difference.signum() > 0 ? difference : BigDecimal.ZERO;
        BigDecimal total = payableDifference.add(feeAmount);
        return new ManThanhToan.ExchangeBreakdown(oldFare, newFare, feeRate, feeAmount, difference, payableDifference, total);
    }

    private BigDecimal determineFeeRate(LocalDateTime requestTime, LocalDateTime departure) {
        if (requestTime == null || departure == null) {
            return null;
        }
        Duration duration = Duration.between(requestTime, departure);
        if (duration.isNegative()) {
            return null;
        }
        long minutes = duration.toMinutes();
        if (minutes < 4 * 60) {
            return null;
        }
        if (minutes >= 24 * 60) {
            return new BigDecimal("0.10");
        }
        return new BigDecimal("0.20");
    }

    private void thucHienDoiVe() {
        if (currentTicket == null || selections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có thông tin vé mới để thanh toán.");
            return;
        }
        BigDecimal recalculated = determineFeeRate(LocalDateTime.now(), currentTicket.getThoiGianKhoiHanh());
        if (recalculated == null) {
            JOptionPane.showMessageDialog(this,
                    "Thời gian đổi đã vượt quá giới hạn cho phép.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            resetFlow();
            return;
        }
        if (exchangeFeeRate == null || recalculated.compareTo(exchangeFeeRate) != 0) {
            exchangeFeeRate = recalculated;
            paymentPage.setExchangeBreakdown(buildExchangeBreakdown());
            JOptionPane.showMessageDialog(this,
                    "Phụ phí đổi vé đã thay đổi theo thời gian. Vui lòng kiểm tra lại số tiền và xác nhận lại.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ManThanhToan.ExchangeBreakdown breakdown = paymentPage.getExchangeBreakdown();
        if (breakdown == null) {
            JOptionPane.showMessageDialog(this, "Không thể xác định khoản thanh toán.");
            return;
        }
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
            JOptionPane.showMessageDialog(this, "Lỗi kiểm tra nhân viên: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maHK = currentTicket.getMaHK();
        if (maHK == null || maHK.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mã hành khách tương ứng với vé cũ.");
            return;
        }
        BanVe.TicketSelection selection = selections.get(0);
        String maGheMoi = selection.getSeatId();
        if (maGheMoi == null || maGheMoi.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được ghế mới.");
            return;
        }
        String maChuyenTauMoi = selectedTrip != null ? selectedTrip.code : (currentTrain != null ? currentTrain.getCode() : null);
        if (maChuyenTauMoi == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được chuyến tàu mới.");
            return;
        }
        BigDecimal giaVeMoi = safe(selection.getBasePrice());
        BigDecimal tongThanhToan = breakdown.getTotalPayable().setScale(0, RoundingMode.HALF_UP);
        try {
            ThanhToan_Dao service = new ThanhToan_Dao();
            ThanhToan_Dao.ExchangeResult result = service.luuDoiVe(
                    maNV,
                    maHK,
                    currentTicket.getMaVe(),
                    currentTicket.getMaChuyenTau(),
                    maChuyenTauMoi,
                    maGheMoi,
                    currentTicket.getMaLoaiVe(),
                    giaVeMoi,
                    tongThanhToan
            );
            JOptionPane.showMessageDialog(this,
                    "Đổi vé thành công!\nMã vé mới: " + result.getMaVeMoi() +
                            "\nMã hóa đơn: " + result.getMaHoaDon() +
                            "\nSố tiền đã thanh toán: " + formatCurrency(tongThanhToan));
            resetFlow();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Có lỗi khi lưu dữ liệu đổi vé: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFlow() {
        selections.clear();
        selectedTrip = null;
        currentTrain = null;
        exchangeFeeRate = null;
        paymentPage.setSelections(Collections.emptyList());
        paymentPage.setExchangeBreakdown(null);
        seatPage.clearSelection();
        chooseTripPage.reset();
        currentTicket = null;
        searchSection.clearTicket();
        searchSection.setSelectNewEnabled(false);
        searchSection.requestFocusInput();
        showStep(CARD_SEARCH);
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount != null ? amount : BigDecimal.ZERO) + " ₫";
    }

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
            resultPanel.setTripSelectionListener(DoiVe.this::handleTripSelection);

            try {
                ChuyenDi_Dao dao = new ChuyenDi_Dao();
                List<String> gaDi = dao.getAllGaDi();
                List<String> gaDen = dao.getAllGaDen();
                searchPanel.setStations(gaDi, gaDen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            searchPanel.onSearch(e -> executeSearch(searchPanel.getGaDi(), searchPanel.getGaDen(), searchPanel.getNgayDi()));
        }

        void prepareForTicket(TicketExchangeInfo ticket) {
            if (ticket == null) {
                return;
            }
            searchPanel.setSelectedStations(ticket.getGaDi(), ticket.getGaDen());
            LocalDate date = ticket.getThoiGianKhoiHanh() != null
                    ? ticket.getThoiGianKhoiHanh().toLocalDate()
                    : LocalDate.now();
            searchPanel.setNgayDi(date);
            searchPanel.setNgayVe(date);
            showSearchForm();
        }

        private void executeSearch(String gaDi, String gaDen, LocalDate ngay) {
            if (ngay == null) {
                ngay = LocalDate.now();
            }
            lastGaDi = gaDi;
            lastGaDen = gaDen;
            lastNgay = ngay;

            List<TripSelectPanel.Trip> trips = queryTrips(gaDi, gaDen, ngay);
            String di = gaDi != null ? gaDi : "";
            String den = gaDen != null ? gaDen : "";
            resultPanel.setContext(di, den, ngay);
            resultPanel.setTrips(trips);
            subCards.show(subPanel, "result");
            showingResults = true;
        }

        private List<TripSelectPanel.Trip> queryTrips(String gaDi, String gaDen, LocalDate ngay) {
            if (ngay == null) {
                return Collections.emptyList();
            }
            LocalDateTime from = ngay.atStartOfDay();
            LocalDateTime to = ngay.atTime(23, 59, 59);
            List<TripSelectPanel.Trip> trips = new ArrayList<>();
            try {
                ChuyenDi_Dao dao = new ChuyenDi_Dao();
                java.util.Date dFrom = java.util.Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
                java.util.Date dTo = java.util.Date.from(to.atZone(ZoneId.systemDefault()).toInstant());
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

        void onShow() {
            if (showingResults && lastNgay != null) {
                executeSearch(lastGaDi, lastGaDen, lastNgay);
            }
        }

        void reset() {
            showSearchForm();
            lastGaDi = null;
            lastGaDen = null;
            lastNgay = null;
        }
    }

    private void handleTripSelection(TripSelectPanel.Trip trip) {
        if (trip == null || currentTicket == null) {
            return;
        }
        if (!equalsIgnoreCase(currentTicket.getGaDi(), trip.departStation) ||
                !equalsIgnoreCase(currentTicket.getGaDen(), trip.arriveStation)) {
            JOptionPane.showMessageDialog(this,
                    "Vé mới phải có cùng ga đi và ga đến với vé cũ.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedTrip = trip;
        selections.clear();
        seatPage.clearSelection();

        LocalDate ngayDi = trip.depart != null ? trip.depart.toLocalDate() : null;
        seatPage.setRoute(trip.departStation, trip.arriveStation, ngayDi);
        boolean loaded = seatPage.loadSeatMap(trip.code);
        if (!loaded) {
            return;
        }
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        String departTime = trip.depart != null ? trip.depart.format(timeFmt) : "";
        String arriveTime = trip.arrive != null ? trip.arrive.format(timeFmt) : "";
        String route = trip.departStation + " -> " + trip.arriveStation;
        currentTrain = new BanVe.TrainInfo(
                trip.code,
                departTime,
                arriveTime,
                route,
                seatPage.getCarCount()
        );
        seatPage.setCommonPassengerInfo(
                currentTicket.getHoTen(),
                currentTicket.getSoDienThoai(),
                currentTicket.getCccd(),
                currentTicket.getMaGioiTinh()
        );
        showStep(CARD_SEAT);
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private class SearchSection extends JPanel {
        private final JTextField tfMaVeSearch = Ui.field();
        private final JButton btnTimVe = Ui.primary("Tìm Vé");
        private final Map<String, JTextField> fields = new LinkedHashMap<>();
        private final JButton btnChonVeMoi = Ui.primary("Chọn Vé Mới");

        SearchSection() {
            setLayout(new BorderLayout());
            setOpaque(false);

            for (JButton b : new JButton[]{btnTimVe, btnChonVeMoi}) {
                b.setUI(new BasicButtonUI());
                b.setContentAreaFilled(true);
                b.setOpaque(true);
                b.setFocusPainted(false);
            }

            add(Ui.banner("TÌM KIẾM VÉ ĐỔI"), BorderLayout.NORTH);

            JPanel body = new JPanel(new GridBagLayout());
            body.setOpaque(false);
            body.setBorder(new EmptyBorder(14, 14, 14, 14));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(0, 0, 0, 12);
            gc.gridx = 0;
            gc.gridy = 0;
            gc.fill = GridBagConstraints.BOTH;
            gc.weightx = 0.42;
            gc.weighty = 1;
            body.add(buildLeftColumn(), gc);

            gc.gridx = 1;
            gc.weightx = 0.58;
            body.add(buildRightColumn(), gc);

            add(body, BorderLayout.CENTER);
            setSelectNewEnabled(false);
        }

        private JComponent buildLeftColumn() {
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

            JPanel rules = Ui.card(Ui.infoBox(
                    "Thời điểm yêu cầu đổi: hành khách phải thực hiện đổi trước giờ tàu chạy ghi trên vé ít nhất thời gian tối thiểu quy định.",
                    "Ga đi và ga đến của vé mới phải giống hoặc nằm trong cùng tuyến/điểm xuất phát-đích với vé cũ theo quy định “cùng ga đi – cùng ga đến”.",
                    "Người yêu cầu đổi phải là người mua vé hoặc hành khách trên vé (hoặc được ủy quyền hợp pháp) và thông tin hành khách trên vé phải trùng với giấy tờ tùy thân.",
                    "Thông tin cá nhân hành khách không bị thay đổi khi đổi vé (không đổi tên, số giấy tờ tùy thân, số hành khách…).",
                    "--------------------",
                    "Phụ phí khi đổi vé:",
                    "Đổi ≥ 24 giờ: Phí 10% giá vé cũ.",
                    "Đổi 4–24 giờ: Phí 20% giá vé cũ.",
                    "Đổi < 4 giờ: Không được đổi vé."
            ), "QUY ĐỊNH ĐỔI VÉ");
            col.add(rules);
            col.add(Box.createVerticalStrut(16));

            JPanel search = new JPanel();
            search.setOpaque(false);
            search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));

            JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
            line.setOpaque(false);
            JLabel lb = new JLabel("Mã Vé");
            lb.setBorder(new EmptyBorder(0, 2, 4, 12));
            tfMaVeSearch.setPreferredSize(new Dimension(300, 34));
            line.add(lb);
            line.add(tfMaVeSearch);
            search.add(line);

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnRow.setOpaque(false);
            btnRow.add(btnTimVe);
            search.add(btnRow);

            col.add(Ui.card(search, ""));
            return col;
        }

        private JComponent buildRightColumn() {
            JPanel col = new JPanel(new BorderLayout());
            col.setOpaque(false);

            JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
            form.setOpaque(false);
            String[] names = {
                    "Mã Vé:", "Họ Tên Hành Khách:", "Năm Sinh:", "Số CCCD:",
                    "Chuyến Tàu:", "Tàu Di Chuyển:", "Số Toa:", "Số Khoang:",
                    "Loại Ghế:", "Số Ghế:", "Loại Vé:", "Tiền Vé:"
            };
            for (String n : names) {
                JTextField f = Ui.field();
                f.setEditable(false);
                fields.put(n, f);
                form.add(new JLabel(n));
                form.add(f);
            }

            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            btnRow.setOpaque(false);
            btnRow.add(btnChonVeMoi);

            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);
            wrap.add(form, BorderLayout.CENTER);
            wrap.add(btnRow, BorderLayout.SOUTH);

            col.add(Ui.card(wrap, "THÔNG TIN VÉ"), BorderLayout.CENTER);
            return col;
        }

        JButton getSearchButton() {
            return btnTimVe;
        }

        JButton getSelectNewButton() {
            return btnChonVeMoi;
        }

        void addEnterKeyListener(java.awt.event.ActionListener listener) {
            tfMaVeSearch.addActionListener(listener);
        }

        String getMaVeInput() {
            return tfMaVeSearch.getText();
        }

        void clearTicket() {
            fields.values().forEach(f -> f.setText(""));
        }

        void setTicket(TicketExchangeInfo info) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("Mã Vé:", nullToDash(info.getMaVe()));
            values.put("Họ Tên Hành Khách:", nullToDash(info.getHoTen()));
            values.put("Năm Sinh:", nullToDash(info.getNamSinh()));
            values.put("Số CCCD:", nullToDash(info.getCccd()));
            values.put("Chuyến Tàu:", buildRoute(info));
            values.put("Tàu Di Chuyển:", nullToDash(info.getTenTau() != null ? info.getTenTau() : info.getMaTau()));
            values.put("Số Toa:", info.getSoToa() > 0 ? String.valueOf(info.getSoToa()) : "-");
            values.put("Số Khoang:", nullToDash(info.getTenKhoang()));
            values.put("Loại Ghế:", nullToDash(info.getLoaiGhe()));
            values.put("Số Ghế:", nullToDash(info.getSoGhe()));
            values.put("Loại Vé:", nullToDash(info.getTenLoaiVe()));
            values.put("Tiền Vé:", formatCurrency(info.getGiaVe()));
            for (Map.Entry<String, String> entry : values.entrySet()) {
                JTextField field = fields.get(entry.getKey());
                if (field != null) {
                    field.setText(entry.getValue());
                }
            }
        }

        void setSelectNewEnabled(boolean enabled) {
            btnChonVeMoi.setEnabled(enabled);
        }

        void requestFocusInput() {
            SwingUtilities.invokeLater(tfMaVeSearch::requestFocusInWindow);
        }

        private String nullToDash(String value) {
            return value == null || value.isBlank() ? "-" : value;
        }

        private String buildRoute(TicketExchangeInfo info) {
            if (info == null) return "-";
            String gaDi = info.getGaDi() != null ? info.getGaDi() : "?";
            String gaDen = info.getGaDen() != null ? info.getGaDen() : "?";
            LocalDateTime dep = info.getThoiGianKhoiHanh();
            if (dep == null) {
                return gaDi + " → " + gaDen;
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return gaDi + " → " + gaDen + " (" + dep.format(fmt) + ")";
        }
    }
}
