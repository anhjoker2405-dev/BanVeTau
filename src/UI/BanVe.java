package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.NumberFormat;
import java.math.BigDecimal;

import dao.ThanhToan_Dao;
import dao.GiaVe_Dao;
import dao.KhuyenMai_Dao;
import entity.KhuyenMai;
import dao.ChuyenDi_Dao;
import entity.ChuyenTau;

// >>> Thêm import lấy dữ liệu NV/HK từ SQL và phiên đăng nhập
import util.AppSession;
import dao.NhanVien_Dao;
import dao.HanhKhach_Dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BanVe extends JPanel {
    private static final int BASE_PRICE = 81060;

    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color BLUE_HOVER = new Color(47, 107, 255, 30);
    private static final Color PANEL_BG = new Color(245, 248, 253);

    private final CardLayout wizard = new CardLayout();
    private final JPanel cards = new JPanel(wizard);

    private final ChooseTripPage page1 = new ChooseTripPage();
    private final ManChonGheNgoi page2 = new ManChonGheNgoi();
    private final PaymentPage page3 = new PaymentPage();

    private TrainInfo currentTrain;
    private final List<TicketSelection> selections = new ArrayList<>();
    private TripSelectPanel.Trip selectedTrip;

    public BanVe() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        cards.add(page1, "p1");
        cards.add(page2, "p2");
        cards.add(page3, "p3");
        add(cards, BorderLayout.CENTER);

        page2.addBackActionListener(e -> showStep(1));
        page2.addNextActionListener(e -> handleSeatSelectionNext());

        showStep(1);
    }

    private void showStep(int step) {
        switch (step) {
            case 1 -> wizard.show(cards, "p1");
            case 2 -> wizard.show(cards, "p2");
            case 3 -> { page3.refresh(); wizard.show(cards, "p3"); }
        }
    }

    // ======================= PAGE 1 =======================
    private class ChooseTripPage extends JPanel {
        private final CardLayout subCards = new CardLayout();
        private final JPanel subPanel = new JPanel(subCards);

        private final SearchTripPanel searchPanel = new SearchTripPanel();
        private final TripSelectPanel resultPanel = new TripSelectPanel();

        ChooseTripPage() {
            setOpaque(false);
            setLayout(new BorderLayout());
            add(subPanel, BorderLayout.CENTER);

            subPanel.add(searchPanel, "search");
            subPanel.add(resultPanel, "result");
            subCards.show(subPanel, "search");

            // Load ga đi / ga đến từ SQL
            try {
                dao.ChuyenDi_Dao dao = new dao.ChuyenDi_Dao();
                java.util.List<String> gaDi = dao.getAllGaDi();
                java.util.List<String> gaDen = dao.getAllGaDen();
                searchPanel.setStations(gaDi, gaDen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Sự kiện tìm kiếm
            searchPanel.onSearch(e -> {
                String gaDi = searchPanel.getGaDi();
                String gaDen = searchPanel.getGaDen();
                java.time.LocalDate ngay = searchPanel.getNgayDi();

                java.time.LocalDateTime from = ngay.atStartOfDay();
                java.time.LocalDateTime to = ngay.atTime(23,59,59);

                java.util.List<TripSelectPanel.Trip> trips = new java.util.ArrayList<>();
                try {
                    dao.ChuyenDi_Dao dao = new dao.ChuyenDi_Dao();
                    java.util.Date dFrom = java.util.Date.from(from.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    java.util.Date dTo   = java.util.Date.from(to.atZone(java.time.ZoneId.systemDefault()).toInstant());
                    List<ChuyenTau> rs = dao.search(null, gaDi, gaDen, dFrom, dTo);
                    for (ChuyenTau cd : rs) {
                        trips.add(new TripSelectPanel.Trip(
                            cd.getMaChuyenTau(),
                            cd.getGaDi(),
                            cd.getGaDen(),
                            cd.getThoiGianKhoiHanh(),
                            cd.getThoiGianKetThuc(),
                            cd.getSoGheTrong(),
                            0.0 // giá vé (chưa có trong DB)
                        ));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                resultPanel.setContext(gaDi, gaDen, ngay);
                resultPanel.setTrips(trips);
                resultPanel.onBack(evt -> subCards.show(subPanel, "search"));
                resultPanel.setTripSelectionListener(trip -> {
                    handleTripSelection(trip);
                    subCards.show(subPanel, "result");
                });

                subCards.show(subPanel, "result");
            });
        }
    }

    // ======================= PAGE 2 =======================
    private void handleTripSelection(TripSelectPanel.Trip trip) {
        if (trip == null) return;

        selectedTrip = trip;
        selections.clear();
        page2.clearSelection();

        LocalDate ngayDi = trip.depart != null ? trip.depart.toLocalDate() : null;
        page2.setRoute(trip.departStation, trip.arriveStation, ngayDi);

        boolean loaded = page2.loadSeatMap(trip.code);
        if (!loaded) return;

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
        List<ManChonGheNgoi.SeatSelection> seats = page2.getSelectedSeats();
        if (seats.isEmpty()) {
            JOptionPane.showMessageDialog(BanVe.this, "Chưa có ghế nào được chọn.");
            return;
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
            return;
        }

        selections.clear();

        // --- lấy giá cơ sở từ DB (nếu có) ---
        int base = BASE_PRICE;
        try {
            BigDecimal bd = new GiaVe_Dao().getGiaCoSoByMaChuyenTau(currentTrain.code);
            if (bd != null) base = bd.intValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // set vào màn chi tiết vé (bên phải) để hiển thị đúng
        page2.setPerSeatPrice(base);

        
        
        for (ManChonGheNgoi.SeatSelection seat : seats) {
            int seatNumber = seat.getSeatDisplayNumber();
            TicketSelection ts = new TicketSelection(currentTrain, seat.getSoToa(), seatNumber, base, seat.getMaGhe());

            // Snapshot dữ liệu từ bước 2
            String name = "";
            String phone = "";
            String cccd = "";
            String yob = "";
            String typeText = "";

            try {
                java.util.Map<String, ManChonGheNgoi.FormRefs> map = page2.getFormBinding();
                ManChonGheNgoi.FormRefs r = (map != null) ? map.get(seat.getMaGhe()) : null;
                if (r != null) {
                    if (r.tfName != null)  name = r.tfName.getText();
                    if (r.tfPhone != null) phone = r.tfPhone.getText();
                    if (r.tfCCCD != null)  cccd = r.tfCCCD.getText();
                    if (r.cbYear != null && r.cbYear.getSelectedItem() != null) {
                        yob = r.cbYear.getSelectedItem().toString();
                    }
                    if (r.cbType != null && r.cbType.getSelectedItem() != null) {
                        typeText = r.cbType.getSelectedItem().toString();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Gán về TicketSelection bằng control MỚI (không phụ thuộc panel bước 2)
            ts.tfName  = new javax.swing.JTextField(name);
            ts.tfPhone = new javax.swing.JTextField(phone);
            ts.tfCCCD  = new javax.swing.JTextField(cccd);

            javax.swing.JTextField tfBirth = new javax.swing.JTextField(yob);
            ts.tfBirth = tfBirth;

            javax.swing.JComboBox<String> cbType = new javax.swing.JComboBox<>(new String[]{
                "Vé dành cho học sinh, sinh viên","Vé người lớn","Vé trẻ em"
            });
            cbType.setSelectedItem(typeText);
            ts.cbType = cbType;

            selections.add(ts);
        }



        showStep(3);
    }

    // ======================= PAGE 3 =======================
    private class PaymentPage extends JPanel {
        private final JLabel totalLabel = new JLabel("Tổng tiền: 0₫");
        private final JPanel summary = new JPanel();
        private final JRadioButton cash = new JRadioButton("Tiền mặt", true);

        // các field được outer class truy cập khi lưu
        int totalComputed = 0;
        String maKMComputed = null;
        BigDecimal unitPriceComputed = BigDecimal.ZERO;

        private final JComboBox<KhuyenMai> cbKhuyenMai = new JComboBox<>();
        final JSpinner spVat = new JSpinner(new SpinnerNumberModel(10, 0, 20, 1));

        private final JButton btnBack = new JButton("Quay Lại");
        private final JButton btnConfirm = new JButton("Xác Nhận");

        
        // === Customer info box ===
        private final JPanel customerBox = new JPanel(new GridBagLayout());
        private final JLabel lbCusName  = new JLabel("-");
        private final JLabel lbCusPhone = new JLabel("-");
        private final JLabel lbCusCCCD  = new JLabel("-");
PaymentPage() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel head = new JLabel("THANH TOÁN", SwingConstants.LEFT);
            head.setBorder(new EmptyBorder(12, 14, 12, 14));
            head.setOpaque(true);
            head.setBackground(new Color(220, 235, 255));
            head.setFont(head.getFont().deriveFont(Font.BOLD, 20f));
            add(head, BorderLayout.NORTH);

            // Summary list
            summary.setOpaque(false);
            summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
            JScrollPane summaryScroll = new JScrollPane(summary);
            summaryScroll.setBorder(new EmptyBorder(8, 12, 8, 12));
            summaryScroll.getVerticalScrollBar().setUnitIncrement(18);
            add(summaryScroll, BorderLayout.CENTER);

            // Payment box
            JPanel payBox = new JPanel(new GridBagLayout());
            payBox.setBorder(new EmptyBorder(8, 12, 12, 12));
            payBox.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6,6,6,6);
            gc.anchor = GridBagConstraints.WEST;

            ButtonGroup g = new ButtonGroup(); g.add(cash);
            gc.gridx = 0; gc.gridy = 0;
            payBox.add(new JLabel("Hình thức"), gc);
            gc.gridx = 1; JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            p.setOpaque(false); p.add(cash);
            payBox.add(p, gc);

            gc.gridx = 0; gc.gridy = 1;
            payBox.add(new JLabel("Số tiền cần thanh toán"), gc);
            gc.gridx = 1; totalLabel.setForeground(new Color(200,0,0));
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
            payBox.add(totalLabel, gc);

            // Khuyến mãi
            gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 1; payBox.add(new JLabel("Khuyến mãi"), gc);
            gc.gridx = 1; payBox.add(cbKhuyenMai, gc);

            // VAT
            gc.gridx = 0; gc.gridy = 3; payBox.add(new JLabel("VAT (%)"), gc);
            gc.gridx = 1; payBox.add(spVat, gc);

            gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 2;

            JPanel bottomArea = new JPanel(new BorderLayout());
            bottomArea.setOpaque(false);
            bottomArea.add(buildCustomerInfoBox(), BorderLayout.NORTH);
            bottomArea.add(payBox, BorderLayout.CENTER);
            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            foot.add(btnBack); foot.add(btnConfirm);
            bottomArea.add(foot, BorderLayout.SOUTH);
            add(bottomArea, BorderLayout.SOUTH);

            btnBack.addActionListener(e -> showStep(2));
            btnConfirm.addActionListener(e -> thucHienThanhToan());

            // Nạp danh sách khuyến mãi
            try {
                java.util.List<KhuyenMai> ds = new KhuyenMai_Dao().getAllKhuyenMai();
                cbKhuyenMai.addItem(null);
                for (KhuyenMai km : ds) cbKhuyenMai.addItem(km);
                cbKhuyenMai.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof KhuyenMai km) {
                            setText(km.getTenKhuyenMai() + " (" + km.getGiamGia().multiply(new BigDecimal(100)).intValue() + "%)");
                        } else if (value == null) {
                            setText("Không áp dụng");
                        }
                        return c;
                    }
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        // --- Helpers for ticket cards & customer box ---
        private static String safe(JTextField tf) {
            if (tf == null) return "-";
            String s = tf.getText() == null ? "" : tf.getText().trim();
            return s.isEmpty() ? "-" : s;
        }
        private static String safeCombo(JComboBox<?> cb) {
            return (cb != null && cb.getSelectedItem() != null) ? cb.getSelectedItem().toString() : "-";
        }
        private int addInfoRow(JPanel p, GridBagConstraints gc, int row, String label, String value) {
            gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.anchor = GridBagConstraints.WEST;
            p.add(new JLabel(label), gc);
            gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
            p.add(new JLabel(value), gc);
            return row + 1;
        }
        private JPanel buildTicketCard(int index, TicketSelection sel) {
            JPanel card = new JPanel(new GridBagLayout());
            card.setOpaque(false);
            card.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)),
                             new EmptyBorder(10, 12, 10, 12)));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(3, 6, 3, 6);
            gc.anchor = GridBagConstraints.WEST;

            JLabel lbSeat = new JLabel(String.format("Vé %d • Toa %d • Ghế %d", index, sel.car, sel.seat));
            lbSeat.setFont(lbSeat.getFont().deriveFont(Font.BOLD));
            JLabel lbPrice = new JLabel(formatVND(sel.price));
            lbPrice.setForeground(new Color(200,0,0));
            lbPrice.setFont(lbPrice.getFont().deriveFont(Font.BOLD, 13f));

            gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; card.add(lbSeat, gc);
            gc.gridx = 1; gc.gridy = 0; gc.weightx = 0; gc.anchor = GridBagConstraints.EAST; card.add(lbPrice, gc);
            gc.anchor = GridBagConstraints.WEST;
            int row = 1;

            row = addInfoRow(card, gc, row, "Họ tên",    safe(sel.tfName));
            row = addInfoRow(card, gc, row, "SĐT",       (sel.tfPhone != null ? safe(sel.tfPhone) : "-"));
            row = addInfoRow(card, gc, row, "CCCD",      safe(sel.tfCCCD));
            row = addInfoRow(card, gc, row, "Năm sinh",  (sel.tfBirth != null ? safe(sel.tfBirth) : "-"));
            row = addInfoRow(card, gc, row, "Loại vé",   safeCombo(sel.cbType));

            JButton btnEdit = new JButton("Sửa vé này");
            btnEdit.addActionListener(e -> {
                showStep(2);
                SwingUtilities.invokeLater(() -> { try { if (sel.tfName != null) sel.tfName.requestFocusInWindow(); } catch (Exception ignore) {} });
            });
            gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2; gc.anchor = GridBagConstraints.EAST;
            card.add(btnEdit, gc);

            return card;
        }
        private JPanel buildCustomerInfoBox() {
            JPanel box = new JPanel(new GridBagLayout());
            box.setOpaque(false);
            box.setBorder(new CompoundBorder(
                    new TitledBorder(new LineBorder(new Color(224,224,224)), "Thông tin khách hàng"),
                    new EmptyBorder(6, 10, 6, 10)
            ));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(4,6,4,6);
            gc.anchor = GridBagConstraints.WEST;

            gc.gridx = 0; gc.gridy = 0; box.add(new JLabel("Họ tên"), gc);
            gc.gridx = 1; box.add(lbCusName, gc);

            gc.gridx = 0; gc.gridy = 1; box.add(new JLabel("Số điện thoại"), gc);
            gc.gridx = 1; box.add(lbCusPhone, gc);

            gc.gridx = 0; gc.gridy = 2; box.add(new JLabel("CCCD"), gc);
            gc.gridx = 1; box.add(lbCusCCCD, gc);

            JButton btnEdit = new JButton("Sửa…");
            btnEdit.addActionListener(e -> showStep(2));
            gc.gridx = 2; gc.gridy = 0; gc.gridheight = 3; gc.weightx = 1; gc.anchor = GridBagConstraints.EAST;
            box.add(btnEdit, gc);
            return box;
        }
        private void updateCustomerInfoBox() {
            if (selections == null || selections.isEmpty()) {
                lbCusName.setText("-"); lbCusPhone.setText("-"); lbCusCCCD.setText("-"); return;
            }
            TicketSelection first = selections.get(0);
            lbCusName.setText(first.tfName != null ? safe(first.tfName) : "-");
            lbCusPhone.setText(first.tfPhone != null ? safe(first.tfPhone) : "-");
            lbCusCCCD.setText(first.tfCCCD != null ? safe(first.tfCCCD) : "-");
        }


        void refresh() {
            summary.removeAll();
            BigDecimal totalBD = BigDecimal.ZERO;

            if (selections.isEmpty()) {
                summary.add(centerMsg("Chưa có ghế nào được chọn."));
            } else {
                
            int _i = 1;
            for (TicketSelection sel : selections) {
                JPanel card = buildTicketCard(_i++, sel);
                summary.add(card);
                summary.add(Box.createVerticalStrut(6));
                totalBD = totalBD.add(new BigDecimal(sel.price));
            }

            }

            BigDecimal promoRate = BigDecimal.ZERO; String maKM = null;
            Object selKM = cbKhuyenMai.getSelectedItem();
            if (selKM instanceof KhuyenMai km && km.getGiamGia() != null) {
                promoRate = km.getGiamGia();
                maKM = km.getMaKhuyenMai();
            }

            BigDecimal afterPromo = totalBD.multiply(BigDecimal.ONE.subtract(promoRate));
            BigDecimal vatRate = new BigDecimal(((Integer)spVat.getValue())/100.0);
            BigDecimal grand = afterPromo.multiply(BigDecimal.ONE.add(vatRate));
            int grandInt = grand.setScale(0, java.math.RoundingMode.HALF_UP).intValue();

            int qty = Math.max(selections.size(), 1);
            BigDecimal unit = afterPromo
                    .divide(new BigDecimal(qty), java.math.RoundingMode.HALF_UP) // đơn giá sau KM
                    .multiply(BigDecimal.ONE.add(vatRate))                       // + VAT
                    .setScale(0, java.math.RoundingMode.HALF_UP);

            totalLabel.setText("Tổng tiền: " + formatVND(grandInt));

            // giữ lại để outer class lưu DB
            totalComputed = grandInt;
            maKMComputed = maKM;
            unitPriceComputed = unit;

            summary.revalidate();
            summary.repaint();
            updateCustomerInfoBox();
        }
    }

    // ======= Common helpers =======
    private static JPanel centerMsg(String s) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel("<html><div style='padding:8px;color:#666;'>" + s + "</div></html>", SwingConstants.CENTER);
        p.add(l, BorderLayout.CENTER); return p;
    }

    private static class TrainInfo {
        final String code, depart, arrive, coachLabel; final int carCount;
        TrainInfo(String code, String depart, String arrive, String coachLabel, int carCount) {
            this.code = code; this.depart = depart; this.arrive = arrive;
            this.coachLabel = coachLabel; this.carCount = carCount;
        }
    }
    private static class TicketSelection {
        final TrainInfo train; final int car; final int seat; final int price; final String seatId;
        JTextField tfName, tfPhone, tfCCCD, tfBirth; JComboBox<String> cbType;
        TicketSelection(TrainInfo t, int car, int seat, int price, String seatId) {
            this.train = t; this.car = car; this.seat = seat; this.price = price; this.seatId = seatId;
        }
    }
    private static class TrainCard extends JPanel {
        public final JToggleButton toggle = new JToggleButton();
        TrainCard(TrainInfo t, boolean showCaption) {
            setOpaque(false); setLayout(new BorderLayout());
            setPreferredSize(new Dimension(200, 140));

            JPanel inner = new JPanel(new BorderLayout());
            inner.setOpaque(true);
            inner.setBackground(Color.WHITE);
            inner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    new EmptyBorder(10, 10, 10, 10)
            ));

            if (showCaption) {
                JLabel cap = new JLabel("CHUYẾN TÀU");
                cap.setForeground(new Color(120,120,120));
                cap.setFont(cap.getFont().deriveFont(Font.BOLD, 11f));
                inner.add(cap, BorderLayout.NORTH);
            }

            String html = "<html><div style='text-align:center;'>"
                    + "<div style='font-weight:bold;font-size:16px;'>" + t.code + "</div>"
                    + "<div style='font-size:12px;margin-top:6px;'>TG đi " + t.depart + "<br/>TG đến " + t.arrive + "</div>"
                    + "</div></html>";
            toggle.setText(html);
            toggle.setUI(new BasicToggleButtonUI());
            toggle.setFocusPainted(false);
            toggle.setOpaque(false);
            toggle.setBorder(null);

            inner.add(toggle, BorderLayout.CENTER);
            add(inner, BorderLayout.CENTER);
        }
        void addActionListener(java.awt.event.ActionListener l) { toggle.addActionListener(l); }
    }
    private static class SeatButton extends JToggleButton {
        SeatButton(String text) {
            super(text);
            setUI(new BasicToggleButtonUI());
            setFocusPainted(false);
            setOpaque(true);
            setBackground(new Color(238, 243, 255));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 215, 255)),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            setPreferredSize(new Dimension(48, 36));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override public void setEnabled(boolean b) {
            super.setEnabled(b);
            if (!b) { setBackground(new Color(230, 230, 230)); setForeground(new Color(150,150,150)); }
        }
        { getModel().addChangeListener(e -> {
                ButtonModel m = getModel();
                if (!isEnabled()) return;
                if (m.isSelected())      setBackground(new Color(255, 120, 120));
                else if (m.isRollover()) setBackground(new Color(190, 215, 255));
                else                     setBackground(new Color(238, 243, 255));
                setForeground(isSelected() ? Color.WHITE : Color.BLACK);
        }); }
    }

    private int estimatePrice(TrainInfo t, int car, int seat) {
        int base = switch (t.coachLabel) {
            case "Ngồi mềm chất lượng cao" -> 350_000;
            case "Ngồi mềm" -> 300_000;
            default -> 250_000;
        };
        int extra = (seat % 2 == 0 ? 20_000 : 0) + (car <= 2 ? 30_000 : 0);
        return base + extra;
    }

    // ====== Xử lý thanh toán & lưu SQL ======
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

        // 2) Lấy thông tin khách từ thẻ 'Chi tiết vé' đầu tiên
        TicketSelection first = selections.get(0);
        String tenHK  = first.tfName  != null ? first.tfName.getText().trim()  : null;
        String sdtHK  = first.tfPhone != null ? first.tfPhone.getText().trim() : null;
        String cccdHK = first.tfCCCD  != null ? first.tfCCCD.getText().trim()  : null;

        if (tenHK == null || tenHK.isEmpty()) {
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
        for (TicketSelection sel : selections) maGheList.add(sel.seatId);

        // 4) Mã chuyến tàu
        String maChuyenTau = selectedTrip != null ? selectedTrip.code
                : (currentTrain != null ? currentTrain.code : null);
        if (maChuyenTau == null) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã chuyến tàu.");
            return;
        }

        // 5) Lưu hóa đơn + vé
        int tong = page3.totalComputed;
        try {
            ThanhToan_Dao service = new ThanhToan_Dao();
            BigDecimal vat = new BigDecimal(((Integer)page3.spVat.getValue())/100.0);
            String maKM = page3.maKMComputed;
            BigDecimal unit = page3.unitPriceComputed; // đơn giá/ve sau KM+VAT

            String maHD = service.luuHoaDonVaVe(maNV, maHK, maChuyenTau, maGheList, unit, vat, maKM);

            JOptionPane.showMessageDialog(this,
                "Thanh toán thành công!\nMã HĐ: " + maHD + "\nTổng tiền: " + formatVND(tong));
            selections.clear();
            showStep(1);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Có lỗi khi lưu dữ liệu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String formatVND(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        return nf.format(amount) + "₫";
    }
}
