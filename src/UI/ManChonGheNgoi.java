package ui;
// ================== CHỨC NĂNG BÁN VÉ · MÀN CHỌN GHẾ ==================
import dao.FormValidator;
import dao.GiaVe_Dao;
import dao.HanhKhach_Dao;
import dao.LoaiVe_Dao;
import dao.SeatMapDao;
import entity.ComboItem;
import entity.Ghe;
import entity.KhoangTau;
import entity.ToaTau;
import entity.LoaiVe;
import entity.PassengerInfo;
import entity.SeatSelection;
import entity.ToaTau;

import java.sql.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Locale;
import java.math.RoundingMode;

public class ManChonGheNgoi extends JPanel {

    // ==== Màu dùng chung ====
    private static final Color BLUE_PRIMARY   = new Color(0x1976D2);
    private static final Color BLUE_LIGHT     = new Color(0xE3F2FD);
    private static final Color BLUE_BORDER    = new Color(0x90CAF9);
    private static final Color GREEN_PRIMARY  = new Color(0x2E7D32);
    private static final Color GREEN_SOFT     = new Color(0x43A047);
    private static final Color RED_PRIMARY    = new Color(0xE53935);
    private static final Color RED_SOFT       = new Color(0xEF9A9A);
    private static final Color SEAT_FREE      = new Color(0xBFE3FF);
    private static final Color SEAT_SELECTED  = new Color(0xFF6F61);
    private static final Color SEAT_SOLD      = new Color(0xBDBDBD);
    private static final Color SEAT_TEXT      = new Color(0x174A7A);
    private static final Color COMPART_BAR    = new Color(0x214A7A); // thanh ngăn khoang (đậm)
    private static final Color TOA_BORDER     = new Color(0x205A9B); // viền toa

    // == Seat size tuning (nút nhỏ) ==
    private static final int   SEAT_W = 48;
    private static final int   SEAT_H = 44;
    private static final float SEAT_FONT_PT = 10.5f;
    private static final int   SEAT_GAP = 8;          // khoảng cách giữa các nút
    private static final int   SEAT_INNER_PAD = 1;    // đệm trong
    
    private final JLabel routeLabel = new JLabel();
    private final JPanel carListPanel = new JPanel();
    private final SeatMapDao seatMapDao = new SeatMapDao();
        private final GiaVe_Dao giaVeDao = new GiaVe_Dao();
    private final HanhKhach_Dao hanhKhachDao = new HanhKhach_Dao();
    private final LoaiVe_Dao loaiVeDao = new LoaiVe_Dao();
    private final Map<JToggleButton, SeatSelection> seatBinding = new HashMap<>();
    private final Set<SeatSelection> selectedSeats = new LinkedHashSet<>();
        private final Map<SeatSelection, TicketForm> ticketForms = new LinkedHashMap<>();
    private final List<ComboItem> genderOptions = new ArrayList<>();
    private final List<ComboItem> ticketTypeOptions = new ArrayList<>();
    private JPanel ticketListPanel;
    private List<ToaTau> currentCars = Collections.emptyList();
    private JButton btnBack;
    private JButton btnNext;
    private JTextField commonNameField;
    private JComboBox<ComboItem> commonGenderCombo;
    private JTextField commonPhoneField;
    private JTextField commonCccdField;
    private String currentMaChuyenTau;
    private BigDecimal currentFare;
    private static final BigDecimal DISCOUNT_NONE = BigDecimal.ZERO;
    private static final BigDecimal DISCOUNT_STUDENT = new BigDecimal("0.10");
    private static final BigDecimal DISCOUNT_SENIOR = new BigDecimal("0.15");
    private final DecimalFormat currencyFormat;
    private SeatSelectionListener seatSelectionListener;
    private boolean updatingSelection;


    public ManChonGheNgoi() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        currencyFormat = new DecimalFormat("#,##0", symbols);
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        loadReferenceData();
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(Color.WHITE);

        root.add(buildStepBar(), BorderLayout.NORTH);

        // === Center: cố định 70/30, không có thanh kéo ===
        JComponent left  = buildLeft();
        JComponent right = buildRight();

        left.setMinimumSize(new Dimension(0, 0));
        right.setMinimumSize(new Dimension(0, 0));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBorder(new LineBorder(new Color(230,230,230)));
        GridBagConstraints cc = new GridBagConstraints();
        cc.fill = GridBagConstraints.BOTH;
        cc.gridy = 0;
        cc.weighty = 1.0;

        // Trái 70%
        cc.gridx = 0;
        cc.weightx = 0.70;
        center.add(left, cc);

        // Phải 30%
        cc.gridx = 1;
        cc.weightx = 0.30;
        center.add(right, cc);

        root.add(center, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }

    // ---------------- Step Bar (full width 3 cột) ----------------
    private JComponent buildStepBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, 8, 0));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new EmptyBorder(0, 0, 8, 0));

        bar.add(makeStep("1  CHỌN CHUYẾN", false));
        bar.add(makeStep("2  CHI TIẾT VÉ", true));   // xanh - active
        bar.add(makeStep("3  THANH TOÁN", false));
        return bar;
    }

    private JComponent makeStep(String text, boolean active) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true); // phải có để background hiển thị
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
        l.setBorder(new CompoundBorder(
                new LineBorder(new Color(210,210,210)),
                new EmptyBorder(12, 0, 12, 0)
        ));
        if (active) {
            l.setBackground(BLUE_PRIMARY);
            l.setForeground(Color.WHITE);
        } else {
            l.setBackground(new Color(0xE0E0E0));
            l.setForeground(Color.DARK_GRAY);
        }
        return l;
    }

    // ---------------- LEFT: Chọn vị trí ----------------
    private JComponent buildLeft() {
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(Color.WHITE);

        // Tiêu đề tuyến
        routeLabel.setOpaque(true);
        routeLabel.setBackground(new Color(0xE8F0FE));
        routeLabel.setForeground(new Color(0x1565C0));
        routeLabel.setFont(routeLabel.getFont().deriveFont(Font.BOLD, 14f));
        routeLabel.setBorder(new CompoundBorder(new LineBorder(BLUE_BORDER),
                new EmptyBorder(10, 10, 10, 10)));
        routeLabel.setText("  Tuyến ...");
        left.add(routeLabel, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Color.WHITE);
        center.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lbl = new JLabel("CHỌN VỊ TRÍ", SwingConstants.CENTER);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 16f));
        lbl.setForeground(new Color(0x1976D2));
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        center.add(lbl);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        legend.setBackground(Color.WHITE);
        legend.add(legendItem(new Color(0x4DB6AC), "Giường Nằm Khoang 6 Điều Hòa"));
        legend.add(legendItem(new Color(0x81C784), "Giường Nằm Khoang 4 Điều Hòa"));
        legend.add(legendItem(SEAT_FREE, "Ghế Ngồi Mềm Điều Hòa"));
        legend.add(legendItem(SEAT_SELECTED, "Ghế Đang Chọn"));
        legend.add(legendItem(SEAT_SOLD, "Ghế Đã Bán"));
        legend.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)),
                new EmptyBorder(8, 8, 8, 8)));
        center.add(legend);

        // ======= Danh sách toa (cuộn) =======
        carListPanel.setLayout(new BoxLayout(carListPanel, BoxLayout.Y_AXIS));
        carListPanel.setBackground(Color.WHITE);
        carListPanel.add(emptyMessage("Chưa có dữ liệu. Vui lòng chọn chuyến tàu."));

        JScrollPane sp = new JScrollPane(carListPanel);
        sp.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)), new EmptyBorder(0,0,0,0)));
        sp.getVerticalScrollBar().setUnitIncrement(18);

        center.add(Box.createVerticalStrut(6));
        center.add(sp);

        left.add(center, BorderLayout.CENTER);
        return left;
    }

    private JComponent legendItem(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(color);
        box.setPreferredSize(new Dimension(24, 18));
        box.setBorder(new LineBorder(new Color(200,200,200)));
        JLabel t = new JLabel(text);
        t.setForeground(new Color(0x455A64));
        p.add(box);
        p.add(t);
        return p;
    }

    private JComponent emptyMessage(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(new Color(120, 120, 120));
        label.setBorder(new EmptyBorder(20, 0, 20, 0));
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(label, BorderLayout.CENTER);
        return wrapper;
    }

    private void renderSeatMap(List<ToaTau> cars) {
        seatBinding.clear();
        selectedSeats.clear();
        ticketForms.clear();
        refreshTicketCards();
        carListPanel.removeAll();

        if (cars == null || cars.isEmpty()) {
            carListPanel.add(emptyMessage("Không tìm thấy dữ liệu toa tàu."));
        } else {
            for (int i = 0; i < cars.size(); i++) {
                carListPanel.add(new ToaPanel(cars.get(i)));
                if (i < cars.size() - 1) {
                    carListPanel.add(Box.createVerticalStrut(14));
                }
            }
        }

        carListPanel.revalidate();
        carListPanel.repaint();
        updateNextButtonState();
    }

    // ================== TOA PANEL (6/7 khoang) ==================
    private class ToaPanel extends JPanel {
        ToaPanel(ToaTau toa) {
            setOpaque(false);
            setLayout(new BorderLayout());

            String type = toa.getSoToa() % 2 == 0 ? "Giường nằm điều hòa" : "Ngồi mềm điều hòa";
            JLabel t = new JLabel("Toa số " + toa.getSoToa() + ": " + type, SwingConstants.CENTER);
            t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
            t.setForeground(new Color(0x1E88E5));
            t.setBorder(new EmptyBorder(4, 0, 8, 0));
            add(t, BorderLayout.NORTH);


            JPanel rounded = new RoundedBorderPanel();
            rounded.setLayout(new BorderLayout());
            rounded.setOpaque(false);
            rounded.setBorder(new EmptyBorder(8, 10, 8, 10));

            // Hàng khoang: 6 khoang nằm ngang
            JPanel row = new JPanel();
            row.setOpaque(false);
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
            row.setAlignmentX(Component.CENTER_ALIGNMENT);

            // sinh 6 khoang; mỗi khoang 6 ghế (2x3)
            List<KhoangTau> khoangs = toa.getDanhSachKhoang();
            for (int i = 0; i < khoangs.size(); i++) {
                if (i > 0) {
                    row.add(Box.createHorizontalStrut(6));
                    row.add(new CompartmentSeparator());
                    row.add(Box.createHorizontalStrut(6));
                }
                row.add(new KhoangPanel(toa, khoangs.get(i)));
            }

            rounded.add(row, BorderLayout.CENTER);
            add(rounded, BorderLayout.CENTER);
        }
    }

    // Panel vẽ đường viền bo góc dày cho TOA
    private static class RoundedBorderPanel extends JPanel {
        RoundedBorderPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = 20;
            int pad = 3;
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2, arc, arc);
            g2.setColor(TOA_BORDER);
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawRoundRect(pad, pad, getWidth() - pad * 2, getHeight() - pad * 2, arc, arc);
            g2.dispose();
        }
    }

    // Thanh ngăn cách giữa các khoang (cao ~ ngang ma trận ghế)
    private class CompartmentSeparator extends JComponent {
        private static final int BAR_W = 6;

        private int barHeight() {
            return 3 * SEAT_H + 6 * SEAT_GAP + 4;
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(BAR_W, barHeight());
        }

        @Override public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COMPART_BAR);
            g2.fillRoundRect(0, 0, BAR_W, getHeight(), 6, 6);
            g2.dispose();
        }
    }
    // Một khoang: nhãn + lưới 3x2 (6 ghế)
    private class KhoangPanel extends JPanel {
        KhoangPanel(ToaTau toa, KhoangTau khoang) {
            setOpaque(false);
            setLayout(new BorderLayout(4, 4));

            JLabel l = new JLabel("Khoang " + khoang.getTenKhoangTau(), SwingConstants.CENTER);
            l.setForeground(new Color(0x607D8B));
            l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
            add(l, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridBagLayout());
            grid.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(SEAT_GAP, SEAT_GAP, SEAT_GAP, SEAT_GAP);
            gc.anchor = GridBagConstraints.CENTER;
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0; gc.weighty = 0;

            List<Ghe> gheList = khoang.getDanhSachGhe();
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 2; c++) {
                    int idx = r * 2 + c;
                    if (idx >= gheList.size()) {
                        break;
                    }
                    Ghe ghe = gheList.get(idx);
                    SeatSelection seat = new SeatSelection(toa, khoang, ghe);
                    gc.gridx = c; gc.gridy = r;
                    grid.add(seatButton(seat), gc);
                }
            }
            add(grid, BorderLayout.CENTER);

            setBorder(new EmptyBorder(6, 6, 6, 6));
        }
    }

    // Nút ghế bo góc, không vẽ khung stroke
    private static class SeatButton extends JToggleButton {
        private final int arc;
        SeatButton(String text, boolean selected, int arc) {
            super(text, selected);
            this.arc = arc;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Nền bo góc
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            // Viền mảnh 1px (tuỳ chọn)
            g2.setStroke(new BasicStroke(1f));
            g2.setColor(new Color(255,255,255,60));
            g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JToggleButton seatButton(SeatSelection seat) {
        String label = seatLabel(seat);
        JToggleButton b = new SeatButton(label, false, 12);
        Dimension d = new Dimension(SEAT_W, SEAT_H);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        b.setFont(b.getFont().deriveFont(Font.BOLD, SEAT_FONT_PT));
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // tắt UI mặc định để không có hiệu ứng lạ
        b.setFocusable(false);
        b.setRolloverEnabled(false);
        b.setUI(new BasicToggleButtonUI());

        // đệm nhẹ bên trong
        b.setBorder(BorderFactory.createEmptyBorder(SEAT_INNER_PAD, SEAT_INNER_PAD,
                                                   SEAT_INNER_PAD, SEAT_INNER_PAD));

        seatBinding.put(b, seat);

        if (seat.getGhe().isDaDat()) {
            b.setBackground(SEAT_SOLD);
            b.setForeground(Color.DARK_GRAY);
            b.setEnabled(false);

        } else {
            b.setBackground(SEAT_FREE);
            b.setForeground(SEAT_TEXT);
            b.addItemListener(e -> {
                if (updatingSelection) return;
                if (b.isSelected()) {
                    if (!canSelectSeat(seat)) {
                        updatingSelection = true;
                        b.setSelected(false);
                        updatingSelection = false;
                        b.setBackground(SEAT_FREE);
                        b.setForeground(SEAT_TEXT);
                        updateNextButtonState();
                        return;
                    }
                    b.setBackground(SEAT_SELECTED);
                    b.setForeground(Color.WHITE);
                    selectedSeats.add(seat);
                    addTicketForm(seat);
                    notifySeatSelected(seat);
                } else {
                    b.setBackground(SEAT_FREE);
                    b.setForeground(SEAT_TEXT);
                    selectedSeats.remove(seat);
                    removeTicketForm(seat);
                    notifySeatDeselected(seat);
                }
                updateNextButtonState();
            });
        }
        return b;
    }
    
    private String seatLabel(SeatSelection seat) {
        int display = seat.getSeatDisplayNumber();
        if (display > 0) {
            return String.valueOf(display);
        }
        return seat.getGhe().getSoGhe();
    }

    private void notifySeatSelected(SeatSelection seat) {
            if (seatSelectionListener != null) {
                seatSelectionListener.seatSelected(seat);
            }
    }

    private void notifySeatDeselected(SeatSelection seat) {
            if (seatSelectionListener != null) {
                seatSelectionListener.seatDeselected(seat);
            }
    }

    // ---------------- RIGHT: Thông tin khách hàng ----------------
    
    private JButton solidButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 20, 8, 20));

        // hover/press nhẹ
        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed())      b.setBackground(bg.darker());
            else if (m.isRollover()) b.setBackground(bg.brighter());
            else                     b.setBackground(bg);
        });
        return b;
    }
    
    private JComponent buildRight() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(Color.WHITE);
        right.setPreferredSize(new Dimension(430, 0));
        right.setPreferredSize(new Dimension(430, 0));

        right.add(infoHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(8, 8, 8, 8));

        body.add(customerFormRow());
        body.add(Box.createVerticalStrut(8));

        ticketListPanel = new JPanel();
        ticketListPanel.setLayout(new BoxLayout(ticketListPanel, BoxLayout.Y_AXIS));
        ticketListPanel.setBackground(Color.WHITE);
        ticketListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(ticketListPanel);
        
        body.add(Box.createVerticalStrut(8));
        body.add(bottomButtons());

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(new LineBorder(new Color(230,230,230)));
        sp.getVerticalScrollBar().setUnitIncrement(18);

        right.add(sp, BorderLayout.CENTER);
        refreshTicketCards();
        return right;
    }

    private JComponent infoHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BLUE_LIGHT);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, BLUE_BORDER),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel icon = new JLabel("\u2139");
        icon.setFont(icon.getFont().deriveFont(Font.BOLD, 16f));
        icon.setForeground(BLUE_PRIMARY);

        JLabel t = new JLabel("THÔNG TIN KHÁCH HÀNG");
        t.setFont(t.getFont().deriveFont(Font.BOLD, 15f));
        t.setForeground(BLUE_PRIMARY);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(icon);
        left.add(t);

        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JComponent customerFormRow() {
        JPanel g = new JPanel(new GridBagLayout());
        g.setBackground(Color.WHITE);
        g.setBorder(new CompoundBorder(new LineBorder(new Color(235,235,235)),
                new EmptyBorder(10,10,10,10)));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        g.setAlignmentX(Component.LEFT_ALIGNMENT);
        g.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        // Họ tên
        if (commonNameField == null) {
            commonNameField = new JTextField();
        }
        c.gridx=0; c.gridy=0; c.weightx=0;
        g.add(new JLabel("Họ Tên"), c);
        c.gridx=1; c.weightx=1;
        g.add(commonNameField, c);
        
        // Giới tính
        if (commonGenderCombo == null) {
            commonGenderCombo = createGenderComboBox();
        }
        c.gridx=2; c.weightx=0;
        g.add(new JLabel("Giới Tính"), c);
        c.gridx=3; c.weightx=0.6;
        g.add(commonGenderCombo, c);

        // SĐT
        if (commonPhoneField == null) {
            commonPhoneField = new JTextField();
        }
        c.gridx=0; c.gridy=1; c.weightx=0;
        g.add(new JLabel("Số Điện Thoại"), c);
        c.gridx=1; c.weightx=1;
        g.add(commonPhoneField, c);

        // CCCD
        if (commonCccdField == null) {
            commonCccdField = new JTextField();
        }
        c.gridx=2; c.gridy=1; c.weightx=0;
        g.add(new JLabel("CCCD"), c);
        c.gridx=3; c.weightx=0.6;
        g.add(commonCccdField, c);  

        return g;
    }

    private TicketForm createTicketForm(SeatSelection seat) {
        TicketForm form = new TicketForm();
        form.seat = seat;
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(RED_SOFT),
                new EmptyBorder(8, 10, 10, 10)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        String tenKhoang = seat.getTenKhoang() != null ? seat.getTenKhoang() : seat.getMaKhoang();
        String seatDisplayLabel = seatLabel(seat);
        String titleText = String.format("Chi Tiết Vé:  Toa số: %d, Khoang: %s, Ghế: %s",
                seat.getSoToa(), tenKhoang, seatDisplayLabel);
        JLabel title = new JLabel(titleText);
        title.setForeground(RED_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13.5f));

        JButton quick = solidButton("Điền nhanh", GREEN_SOFT, Color.WHITE);
        quick.setFocusPainted(false);
        quick.setBackground(GREEN_SOFT);
        quick.setForeground(Color.WHITE);
        quick.setBorder(new EmptyBorder(4,10,4,10));
        quick.addActionListener(e -> fillTicketFormFromCommon(form));

        c.gridx=0; c.gridy=0; c.gridwidth=3; c.weightx=1;
        card.add(title, c);
        c.gridx=3; c.gridy=0; c.gridwidth=1; c.weightx=0; c.anchor = GridBagConstraints.EAST;
        card.add(quick, c);
        c.anchor = GridBagConstraints.WEST;
        
        JTextField nameField = new JTextField(16);
        form.nameField = nameField;
        c.gridx=0; c.gridy=1; c.weightx=0; card.add(new JLabel("Họ Tên"), c);
        c.gridx=1; c.weightx=1; card.add(nameField, c);

        JComboBox<ComboItem> genderCombo = createGenderComboBox();
        form.genderCombo = genderCombo;
        c.gridx=2; c.weightx=0; card.add(new JLabel("Giới Tính"), c);
        c.gridx=3; c.weightx=0.6; card.add(genderCombo, c);

        JTextField phoneField = new JTextField(16);
        form.phoneField = phoneField;
        c.gridx=0; c.gridy=2; c.weightx=0; card.add(new JLabel("Số Điện Thoại"), c);
        c.gridx=1; c.weightx=1; card.add(phoneField, c);

        JTextField cccdField = new JTextField(16);
        form.cccdField = cccdField;
        c.gridx=2; c.weightx=0; card.add(new JLabel("CCCD"), c);
        c.gridx=3; c.weightx=0.6; card.add(cccdField, c);

        c.gridx=0; c.gridy=3; c.weightx=0; card.add(new JLabel("Năm Sinh"), c);
        JComboBox<String> yearCombo = createYearComboBox();
        form.yearCombo = yearCombo;
        c.gridx=1; c.weightx=0.6; card.add(yearCombo, c);
        
        c.gridx=0; c.gridy=4; c.weightx=0; card.add(new JLabel("Loại Vé"), c);
        c.gridx=1; c.gridwidth=3; c.weightx=1;
        JComboBox<ComboItem> ticketTypeCombo = createTicketTypeComboBox();
        form.ticketTypeCombo = ticketTypeCombo;
        card.add(ticketTypeCombo, c);
        c.gridwidth=1;

        c.gridx=0; c.gridy=5; c.weightx=0;
        JLabel discountLabelTitle = new JLabel("Giảm Giá");
        card.add(discountLabelTitle, c);
        c.gridx=1; c.weightx=1; c.gridwidth=3;
        JLabel discountValueLabel = new JLabel();
        discountValueLabel.setForeground(new Color(0x2E7D32));
        form.discountValueLabel = discountValueLabel;
        card.add(discountValueLabel, c);
        c.gridwidth=1;

        c.gridx=0; c.gridy=6; c.weightx=0;
        JLabel priceLabel = new JLabel("Tiền Vé");
        priceLabel.setForeground(RED_PRIMARY);
        card.add(priceLabel, c);
        c.gridx=1; c.weightx=1; c.gridwidth=3;

        JTextField priceField = new JTextField();
        priceField.setHorizontalAlignment(JTextField.RIGHT);
        priceField.setEditable(false);
        priceField.setFocusable(false);
        priceField.setBackground(new Color(0xFAFAFA));
        form.priceField = priceField;
        card.add(priceField, c);
        c.gridwidth=1;
        
        ticketTypeCombo.addActionListener(e -> updateTicketPriceForForm(form));
        updateTicketPriceForForm(form);
        
        form.container = card;
        return form;
    }

    private void addTicketForm(SeatSelection seat) {
        if (seat == null || ticketForms.containsKey(seat)) {
            return;
        }
        TicketForm form = createTicketForm(seat);
        form.seat = seat;
        ticketForms.put(seat, form);
        refreshTicketCards();
        updateTicketPriceFields();
    }

    private void removeTicketForm(SeatSelection seat) {
        if (seat == null) {
            return;
        }
        if (ticketForms.remove(seat) != null) {
            refreshTicketCards();
        }
    }

    private void refreshTicketCards() {
        if (ticketListPanel == null) {
            return;
        }
        ticketListPanel.removeAll();
        if (ticketForms.isEmpty()) {
            ticketListPanel.add(emptyMessage("Chưa chọn ghế. Vui lòng chọn ghế để nhập thông tin hành khách."));
        } else {
            boolean first = true;
            for (TicketForm form : ticketForms.values()) {
                if (!first) {
                    ticketListPanel.add(Box.createVerticalStrut(8));
                }
                ticketListPanel.add(form.container);
                first = false;
            }
        }
        ticketListPanel.revalidate();
        ticketListPanel.repaint();
    }
    // -- Cập nhật lại giá vé hiển thị khi có thay đổi loại vé hoặc khuyến mãi --
    private void updateTicketPriceFields() {
        for (TicketForm form : ticketForms.values()) {
            updateTicketPriceForForm(form);
        }
    }
    
    // -- Tính toán lại giá tiền cụ thể cho một form hành khách --
    private void updateTicketPriceForForm(TicketForm form) {
        if (form == null) {
            return;
        }
        ComboItem ticketTypeItem = form.ticketTypeCombo != null
                ? (ComboItem) form.ticketTypeCombo.getSelectedItem()
                : null;
        BigDecimal discountRate = getDiscountRate(ticketTypeItem);
        BigDecimal discountedFare = calculateDiscountedFare(currentFare, discountRate);
        form.discountedFare = discountedFare;

        if (form.discountValueLabel != null) {
            form.discountValueLabel.setText(formatDiscountText(discountRate));
        }

        if (form.priceField != null) {
            if (currentFare == null) {
                form.priceField.setText("");
            } else {
                form.priceField.setText(formatCurrency(discountedFare));
            }
        }
    }
    // -- Áp dụng tỷ lệ giảm giá lên giá gốc để ra giá cuối cùng --    
    private BigDecimal calculateDiscountedFare(BigDecimal baseFare, BigDecimal discountRate) {
        if (baseFare == null) {
            return null;
        }
        BigDecimal effectiveRate = discountRate != null ? discountRate : DISCOUNT_NONE;
        BigDecimal multiplier = BigDecimal.ONE.subtract(effectiveRate);
        return baseFare.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }
    
    // -- Hiển thị phần trăm giảm giá dễ đọc cho người dùng --
    private String formatDiscountText(BigDecimal discountRate) {
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0) {
            return "Không áp dụng";
        }
        BigDecimal percentage = discountRate.multiply(BigDecimal.valueOf(100));
        String text = percentage.stripTrailingZeros().toPlainString();
        return text + "%";
    }

    private BigDecimal getDiscountRate(ComboItem ticketTypeItem) {
        if (ticketTypeItem == null) {
            return DISCOUNT_NONE;
        }

        String value = ticketTypeItem.getValue() != null
                ? ticketTypeItem.getValue().trim().toLowerCase(Locale.ROOT)
                : "";
        switch (value) {
            case "lv-002":
                return DISCOUNT_STUDENT;
            case "lv-003":
                return DISCOUNT_SENIOR;
        }

        String label = ticketTypeItem.getLabel() != null
                ? ticketTypeItem.getLabel().toLowerCase(Locale.ROOT)
                : "";
        if (label.contains("học sinh") || label.contains("sinh viên")) {
            return DISCOUNT_STUDENT;
        }
        if (label.contains("cao tuổi") || label.contains("người cao tuổi")) {
            return DISCOUNT_SENIOR;
        }
        return DISCOUNT_NONE;
    }
    // -- Lấy giá vé hiện tại của chuyến để chia sẻ cho bước thanh toán --    
    public BigDecimal getFarePerSeat() {
        return currentFare;
    }
    // -- Gom toàn bộ thông tin hành khách theo từng ghế đã chọn --
    public List<PassengerInfo> collectPassengerInfos() {
        List<PassengerInfo> result = new ArrayList<>();
        for (Map.Entry<SeatSelection, TicketForm> entry : ticketForms.entrySet()) {
            TicketForm form = entry.getValue();
            SeatSelection seat = entry.getKey();
            if (form == null || seat == null) {
                continue;
            }

            String hoTen = form.nameField != null ? form.nameField.getText().trim() : "";
            String soDienThoai = form.phoneField != null ? form.phoneField.getText().trim() : "";
            String cccd = form.cccdField != null ? form.cccdField.getText().trim() : "";
            String namSinh = form.yearCombo != null ? (String) form.yearCombo.getSelectedItem() : null;

            ComboItem genderItem = form.genderCombo != null ? (ComboItem) form.genderCombo.getSelectedItem() : null;
            String maGioiTinh = genderItem != null ? genderItem.getValue() : null;
            String tenGioiTinh = genderItem != null ? genderItem.getLabel() : null;

            ComboItem ticketTypeItem = form.ticketTypeCombo != null ? (ComboItem) form.ticketTypeCombo.getSelectedItem() : null;
            String maLoaiVe = ticketTypeItem != null ? ticketTypeItem.getValue() : null;
            String tenLoaiVe = ticketTypeItem != null ? ticketTypeItem.getLabel() : null;

            BigDecimal giaVe;
            if (form.discountedFare != null) {
                giaVe = form.discountedFare;
            } else if (currentFare != null) {
                giaVe = currentFare;
            } else {
                giaVe = BigDecimal.ZERO;
            }

            result.add(new PassengerInfo(seat, hoTen, soDienThoai, cccd, namSinh,
                    maGioiTinh, tenGioiTinh, maLoaiVe, tenLoaiVe, giaVe));
        }
        return result;
    }
    
    // -- Kiểm tra dữ liệu nhập trên các form hành khách trước khi sang bước thanh toán --    
    public boolean validatePassengerForms() {
        if (ticketForms.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Chưa có ghế nào được chọn.",
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        for (Map.Entry<SeatSelection, TicketForm> entry : ticketForms.entrySet()) {
            SeatSelection seat = entry.getKey();
            TicketForm form = entry.getValue();
            if (form == null) {
                continue;
            }
            String seatInfo = seat != null
                    ? String.format("Toa %d, Ghế %s", seat.getSoToa(), seatLabel(seat))
                    : "ghế đã chọn";

            String hoTen = form.nameField != null ? form.nameField.getText().trim() : "";
            if (!FormValidator.isValidPersonName(hoTen)) {
                JOptionPane.showMessageDialog(this,
                        "Họ tên hành khách tại " + seatInfo +
                                " không hợp lệ (chỉ bao gồm chữ, khoảng trắng và dài 2-50 ký tự).",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                if (form.nameField != null) {
                    form.nameField.requestFocus();
                }
                return false;
            }

            String soDienThoai = form.phoneField != null ? form.phoneField.getText().trim() : "";
            if (!FormValidator.isValidPhoneNumber(soDienThoai)) {
                JOptionPane.showMessageDialog(this,
                        "Số điện thoại tại " + seatInfo +
                                " không hợp lệ (bắt đầu bằng 0 và gồm 10-11 số).",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                if (form.phoneField != null) {
                    form.phoneField.requestFocus();
                }
                return false;
            }

            String cccd = form.cccdField != null ? form.cccdField.getText().trim() : "";
            if (!FormValidator.isValidCccd(cccd)) {
                JOptionPane.showMessageDialog(this,
                        "CCCD/CMND tại " + seatInfo + " phải gồm 9 hoặc 12 chữ số.",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                if (form.cccdField != null) {
                    form.cccdField.requestFocus();
                }
                return false;
            }
        }
        return true;
    }
    
    // -- Sao chép nhanh thông tin nhập chung sang từng form ghế --
    private void fillTicketFormFromCommon(TicketForm form) {
        if (form == null) return;

        if (commonNameField != null) {
            form.nameField.setText(commonNameField.getText().trim());
        }
        if (commonGenderCombo != null && form.genderCombo != null) {
            ComboItem selected = (ComboItem) commonGenderCombo.getSelectedItem();
            if (selected != null) {
                form.genderCombo.setSelectedItem(selected);
            }
        }
        if (commonPhoneField != null) {
            form.phoneField.setText(commonPhoneField.getText().trim());
        }
        if (commonCccdField != null) {
            form.cccdField.setText(commonCccdField.getText().trim());
        }
    }
    
    // -- Tạo combo box giới tính dùng chung cho các form hành khách --
    private JComboBox<ComboItem> createGenderComboBox() {
        JComboBox<ComboItem> combo = new JComboBox<>();
        for (ComboItem item : genderOptions) {
            combo.addItem(item);
        }
        combo.setFocusable(false);
        return combo;
    }
    // -- Tạo combo box loại vé dựa trên dữ liệu đã tải --  
    private JComboBox<ComboItem> createTicketTypeComboBox() {
        JComboBox<ComboItem> combo = new JComboBox<>();
        for (ComboItem item : ticketTypeOptions) {
            combo.addItem(item);
        }
        combo.setFocusable(false);
        return combo;
    }
    // -- Sinh danh sách năm sinh để người dùng chọn nhanh --
    private JComboBox<String> createYearComboBox() {
        JComboBox<String> combo = new JComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear; y >= 1950; y--) {
            combo.addItem(String.valueOf(y));
        }
        if (combo.getItemCount() > 0) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if ("1990".equals(combo.getItemAt(i))) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
        combo.setFocusable(false);
        return combo;
    }
    
    private static class TicketForm {
        SeatSelection seat;
        JPanel container;
        JTextField nameField;
        JComboBox<ComboItem> genderCombo;
        JTextField phoneField;
        JTextField cccdField;
        JComboBox<String> yearCombo;
        JComboBox<ComboItem> ticketTypeCombo;
        JTextField priceField;
        JLabel discountValueLabel;
        BigDecimal discountedFare;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        synchronized (currencyFormat) {
            return currencyFormat.format(amount) + " đ";
        }
    }
    // -- Tải dữ liệu danh mục (giới tính, loại vé) phục vụ bước chọn ghế --
    private void loadReferenceData() {
        genderOptions.clear();
        ticketTypeOptions.clear();

        try {
            Map<String, String> genderMap = hanhKhachDao.findAllGioiTinh();
            for (Map.Entry<String, String> entry : genderMap.entrySet()) {
                genderOptions.add(new ComboItem(entry.getKey(), entry.getValue()));
            }
        } catch (SQLException ex) {
            showReferenceDataWarning("Không thể tải danh sách giới tính từ cơ sở dữ liệu. Sẽ sử dụng dữ liệu mặc định.", ex);
        }
        if (genderOptions.isEmpty()) {
            genderOptions.add(new ComboItem("GT-001", "Nam"));
            genderOptions.add(new ComboItem("GT-002", "Nữ"));
            genderOptions.add(new ComboItem("GT-003", "Khác"));
        }

        try {
            List<LoaiVe> loaiVes = loaiVeDao.findAll();
            for (LoaiVe loaiVe : loaiVes) {
                ticketTypeOptions.add(new ComboItem(loaiVe.getMaLoaiVe(), loaiVe.getTenLoaiVe()));
            }
        } catch (SQLException ex) {
            showReferenceDataWarning("Không thể tải danh sách loại vé từ cơ sở dữ liệu. Sẽ sử dụng dữ liệu mặc định.", ex);
        }
        if (ticketTypeOptions.isEmpty()) {
            ticketTypeOptions.add(new ComboItem("LV-001", "Vé Thường"));
            ticketTypeOptions.add(new ComboItem("LV-002", "Vé học sinh, sinh viên"));
            ticketTypeOptions.add(new ComboItem("LV-003", "Vé cho người cao tuổi"));
        }
    }
    // -- Thông báo khi không thể tải dữ liệu danh mục và dùng dữ liệu mặc định --
    private void showReferenceDataWarning(String message, Exception ex) {
        System.err.println(message + " Chi tiết: " + ex.getMessage());
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE));
    }
    // -- Ràng buộc việc chọn ghế chỉ trong cùng một khoang để tránh nhầm lẫn --
    private boolean canSelectSeat(SeatSelection seat) {
        if (selectedSeats.isEmpty()) {
            return true;
        }
        SeatSelection first = selectedSeats.iterator().next();
        if (Objects.equals(first.getMaKhoang(), seat.getMaKhoang())) {
            return true;
        }
        SwingUtilities.invokeLater(() -> {
            String khoangDangChon = first.getTenKhoang() != null ? first.getTenKhoang() : first.getMaKhoang();
            String khoangMoi = seat.getTenKhoang() != null ? seat.getTenKhoang() : seat.getMaKhoang();
            JOptionPane.showMessageDialog(this,
                    "Không thể chọn ghế ở khoang " + khoangMoi + " vì bạn đang chọn khoang " + khoangDangChon + ".",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
        });
        return false;
    }
    
    // -- Tải giá vé tương ứng với chuyến tàu hiện hành từ cơ sở dữ liệu --
    private void loadFareForCurrentTrip() {
        currentFare = null;
        if (currentMaChuyenTau == null || currentMaChuyenTau.isBlank()) {
            updateTicketPriceFields();
            return;
        }
        try {
            currentFare = giaVeDao.getGiaVeTheoKhoangCach(currentMaChuyenTau);
            if (currentFare == null) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Không tìm thấy giá vé cho chuyến tàu đã chọn.",
                                "Cảnh báo", JOptionPane.WARNING_MESSAGE));
            }
        } catch (SQLException ex) {
            System.err.println("Không thể lấy giá vé: " + ex.getMessage());
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                            "Không thể lấy giá vé cho chuyến tàu đã chọn. Vui lòng kiểm tra kết nối cơ sở dữ liệu.",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE));
        }
        updateTicketPriceFields();
    }
    // -- Thanh nút điều hướng giữa các bước của quy trình bán vé --
    private JComponent bottomButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        btnBack = solidButton("Quay Lại", new Color(0x64B5F6), Color.WHITE);
        btnBack.setBackground(new Color(0x64B5F6));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setBorder(new EmptyBorder(8, 20, 8, 20));

        btnNext = solidButton("Tiếp Tục", GREEN_PRIMARY, Color.WHITE);
        btnNext.setBackground(GREEN_PRIMARY);
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnNext.setEnabled(false);

        p.add(btnBack);
        p.add(btnNext);
        return p;
    }
    // -- Bật/tắt nút Tiếp Tục dựa trên việc đã chọn ghế hay chưa --
    private void updateNextButtonState() {
        if (btnNext != null) {
            btnNext.setEnabled(!selectedSeats.isEmpty());
        }
    }
    // ===================== Public API =====================
    
    // -- Cập nhật thông tin tuyến hiển thị phía trên màn hình --
    public void setRoute(String gaDi, String gaDen, LocalDate ngay) {
        if (gaDi == null || gaDen == null) {
            routeLabel.setText("  Tuyến ...");
            return;
        }
        String dateText = ngay != null ? ngay.format(DateTimeFormatter.ofPattern("dd/MM")) : "";
        routeLabel.setText("  Tuyến " + gaDi + " -> " + gaDen + (dateText.isEmpty() ? "" : ", Ngày " + dateText));
    }
    
    // -- Lấy sơ đồ ghế cho chuyến tàu và vẽ lại toàn bộ giao diện --
    public boolean loadSeatMap(String maChuyenTau) {
        currentMaChuyenTau = maChuyenTau;
        try {
            currentCars = seatMapDao.loadSeatMap(maChuyenTau);
            renderSeatMap(currentCars);
            loadFareForCurrentTrip();
            return true;
        } catch (SQLException ex) {
            currentCars = Collections.emptyList();
            renderSeatMap(currentCars);
            currentFare = null;
            updateTicketPriceFields();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải sơ đồ ghế từ cơ sở dữ liệu. Vui lòng kiểm tra kết nối.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    // -- Trả về số lượng toa hiện có để các bước sau kiểm tra ràng buộc -- 
    public int getCarCount() {
        return currentCars != null ? currentCars.size() : 0;
    }
    // -- Cho phép màn hình cha đăng ký lắng nghe sự kiện chọn ghế --
    public void setSeatSelectionListener(SeatSelectionListener listener) {
        this.seatSelectionListener = listener;
    }
    // -- Đổ thông tin mặc định vào các trường dùng chung khi quay lại bước này --
    public void setCommonPassengerInfo(String hoTen, String soDienThoai, String cccd, String maGioiTinh) {
        if (commonNameField != null) {
            commonNameField.setText(hoTen != null ? hoTen : "");
        }
        if (commonPhoneField != null) {
            commonPhoneField.setText(soDienThoai != null ? soDienThoai : "");
        }
        if (commonCccdField != null) {
            commonCccdField.setText(cccd != null ? cccd : "");
        }
        if (commonGenderCombo != null && maGioiTinh != null) {
            for (int i = 0; i < commonGenderCombo.getItemCount(); i++) {
                ComboItem item = commonGenderCombo.getItemAt(i);
                if (item != null && maGioiTinh.equalsIgnoreCase(item.getValue())) {
                    commonGenderCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        for (TicketForm form : ticketForms.values()) {
            fillTicketFormFromCommon(form);
        }
    }
// -- Trả về danh sách ghế đã chọn để truyền sang bước thanh toán --
    public List<SeatSelection> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }
// -- Dọn dẹp toàn bộ lựa chọn khi người dùng quay lại từ bước sau --
    public void clearSelection() {
        updatingSelection = true;
        for (Map.Entry<JToggleButton, SeatSelection> entry : seatBinding.entrySet()) {
            JToggleButton btn = entry.getKey();
            if (btn.isEnabled()) {
                btn.setSelected(false);
                btn.setBackground(SEAT_FREE);
                btn.setForeground(SEAT_TEXT);
            }
        }
        selectedSeats.clear();
        ticketForms.clear();
        updatingSelection = false;
        refreshTicketCards();
        updateTicketPriceFields();
        updateNextButtonState();
    }
    // -- Liên kết nút Quay Lại với luồng điều hướng bên ngoài --
    public void addBackActionListener(ActionListener listener) {
        if (btnBack != null) {
            btnBack.addActionListener(listener);
        }
    }
    // -- Liên kết nút Tiếp Tục với bước thanh toán tiếp theo --
    public void addNextActionListener(ActionListener listener) {
        if (btnNext != null) {
            btnNext.addActionListener(listener);
        }
    }

    // ===================== Seat Selection model =====================
    public interface SeatSelectionListener {
        void seatSelected(SeatSelection seat);
        void seatDeselected(SeatSelection seat);
    }

//    public static void main(String[] args) {
//        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Chi tiết vé tàu");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setContentPane(new ManChonGheNgoi());
//            frame.setMinimumSize(new Dimension(1200, 720));
//            frame.setSize(new Dimension(1200, 720));
//            frame.setLocationRelativeTo(null);
//            frame.setVisible(true);
//            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        });
//    }
}