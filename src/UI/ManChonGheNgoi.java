package ui;

import dao.SeatMapDao;
import entity.Ghe;
import entity.KhoangTau;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ManChonGheNgoi extends JPanel {
    
    // ==== Quick-fill generators ====
    private static final java.util.Random RNG = new java.util.Random();
    private static final String[] LN = {"Nguyễn","Trần","Lê","Phạm","Hoàng","Huỳnh","Phan","Vũ","Võ","Đặng","Bùi","Đỗ","Hồ","Ngô","Dương","Lý"};
    private static final String[] MN = {"Văn","Thị","Hồng","Ngọc","Thanh","Minh","Anh","Quốc","Gia","Hữu","Trung"};
    private static final String[] FN = {"An","Anh","Bình","Châu","Dũng","Giang","Hiếu","Hòa","Hùng","Huy","Khang","Khôi","Lam","Lan","Linh","Long","Mai","Minh","My","Nam","Ngân","Ngọc","Nhi","Phát","Phúc","Quân","Quang","Quỳnh","Sơn","Thảo","Thành","Thắng","Trang","Trinh","Tú","Tùng","Vy","Yến"};
    private static final String[] PHONE_PREFIX = {"032","033","034","035","036","037","038","039","070","076","077","078","079","081","082","083","084","085","086","088","089","090","093","094","096","097","098","099"};

    private static String genName() {
        return LN[RNG.nextInt(LN.length)] + " " + MN[RNG.nextInt(MN.length)] + " " + FN[RNG.nextInt(FN.length)];
    }
    private static String genPhone() {
        String p = PHONE_PREFIX[RNG.nextInt(PHONE_PREFIX.length)];
        StringBuilder sb = new StringBuilder(p);
        while (sb.length() < 10) sb.append(RNG.nextInt(10));
        return sb.toString();
    }
    private static String genCCCD() {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<12;i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }
    private static int genYOBAndSetType(javax.swing.JComboBox<String> type) {
        int yearNow = java.time.LocalDate.now().getYear();
        int roll = RNG.nextInt(100);
        int yob;
        if (roll < 15) { // trẻ em 4-11
            yob = yearNow - (4 + RNG.nextInt(8));
            type.setSelectedItem("Vé trẻ em");
        } else if (roll < 55) { // học sinh/sinh viên 12-22
            yob = yearNow - (12 + RNG.nextInt(11));
            type.setSelectedItem("Vé dành cho học sinh, sinh viên");
        } else { // người lớn 23-65
            yob = yearNow - (23 + RNG.nextInt(43));
            type.setSelectedItem("Vé người lớn");
        }
        if (yob < 1950) yob = 1950;
        if (yob > 2025) yob = 2025;
        return yob;
    }


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
    private final Map<JToggleButton, SeatSelection> seatBinding = new HashMap<>();
    private final Set<SeatSelection> selectedSeats = new LinkedHashSet<>();
    
    
    
    // References to customer form (header) fields for 'Điền nhanh'
    private javax.swing.JTextField custNameField;
    private javax.swing.JTextField custPhoneField;
    private javax.swing.JTextField custCCCDField;
    // Bind form inputs with seatId so we can read them on the payment page
    public static class FormRefs {
        public javax.swing.JTextField tfName;
        public javax.swing.JTextField tfPhone;
        public javax.swing.JTextField tfCCCD;
        public javax.swing.JComboBox<String> cbYear;
        public javax.swing.JComboBox<String> cbType;
    }
    private final java.util.Map<String, FormRefs> formBinding = new java.util.LinkedHashMap<>();
private List<ToaTau> currentCars = Collections.emptyList();
    private JButton btnBack;
    private JButton btnNext;
    private SeatSelectionListener seatSelectionListener;
    private boolean updatingSelection;


    
    // Right side dynamic container for customer form + ticket cards
    private JPanel rightBody;
    private int perSeatPrice = 0;
public ManChonGheNgoi() {
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
                    b.setBackground(SEAT_SELECTED);
                    b.setForeground(Color.WHITE);
                    selectedSeats.add(seat);
                    notifySeatSelected(seat);
                } else {
                    b.setBackground(SEAT_FREE);
                    b.setForeground(SEAT_TEXT);
                    selectedSeats.remove(seat);
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

    
    /** Cập nhật giá cơ sở/ghế để hiển thị ở phần Chi Tiết Vé (bước 2) */
    public void setPerSeatPrice(int price) {
        this.perSeatPrice = price;
        updateRightContent();
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

        right.add(infoHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(8, 8, 8, 8));

        rightBody = body;
        updateRightContent();

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(new LineBorder(new Color(230,230,230)));
        sp.getVerticalScrollBar().setUnitIncrement(18);

        right.add(sp, BorderLayout.CENTER);
        right.add(bottomButtons(), BorderLayout.SOUTH);
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
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Họ tên
        c.gridx=0; c.gridy=0; c.weightx=0;
        g.add(new JLabel("Họ Tên"), c);
        c.gridx=1; c.weightx=1;
        custNameField = new JTextField(); g.add(custNameField, c);

        // SĐT
        c.gridx=0; c.gridy=1; c.weightx=0;
        g.add(new JLabel("Số Điện Thoại"), c);
        c.gridx=1; c.weightx=1;
        custPhoneField = new JTextField(); g.add(custPhoneField, c);

        // CCCD
        c.gridx=2; c.gridy=1; c.weightx=0;
        g.add(new JLabel("CCCD"), c);
        c.gridx=3; c.weightx=1;
        custCCCDField = new JTextField(); g.add(custCCCDField, c);

        // căn cột
        c.gridx=2; c.gridy=0; c.weightx=0; g.add(new JLabel(""), c);
        c.gridx=3; c.gridy=0; c.weightx=1; g.add(Box.createHorizontalStrut(10), c);

        
        // Nút Điền nhanh cho KH
        JButton quickCus = solidButton("Điền nhanh", GREEN_SOFT, Color.WHITE);
        quickCus.setBorder(new EmptyBorder(4,10,4,10));
        quickCus.addActionListener(e -> {
            if (custNameField != null)  custNameField.setText(genName());
            if (custPhoneField != null) custPhoneField.setText(genPhone());
            if (custCCCDField != null)  custCCCDField.setText(genCCCD());
        });
        GridBagConstraints c2 = new GridBagConstraints();
        c2.insets = new Insets(4,4,4,4);
        c2.gridx=3; c2.gridy=0; c2.weightx=0; c2.anchor=GridBagConstraints.EAST;
        g.add(quickCus, c2);
        return g;
    
    }

    
private JComponent ticketDetailCard(SeatSelection seat, String titleText) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(RED_SOFT), new EmptyBorder(8, 10, 10, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Chi Tiết Vé:  " + titleText);
        title.setForeground(RED_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13.5f));

        JButton quick = solidButton("Điền nhanh", GREEN_SOFT, Color.WHITE);
        quick.setFocusPainted(false);
        quick.setBackground(GREEN_SOFT);
        quick.setForeground(Color.WHITE);
        quick.setBorder(new EmptyBorder(4,10,4,10));

        c.gridx=0; c.gridy=0; c.gridwidth=3; c.weightx=1; card.add(title, c);
        c.gridx=3; c.gridy=0; c.gridwidth=1; c.weightx=0; card.add(quick, c);

        // Row 1: Họ tên + Năm sinh
        c.gridx=0; c.gridy=1; c.weightx=0; card.add(new JLabel("Họ Tên"), c);
        c.gridx=1; c.weightx=1; final JTextField tfName = new JTextField(16); card.add(tfName, c);
        c.gridx=2; c.gridy=1; c.weightx=0; card.add(new JLabel("Năm Sinh"), c);
        c.gridx=3; c.weightx=0.6; final JComboBox<String> year = new JComboBox<>();
        for (int y=1950; y<=2025; y++) year.addItem(String.valueOf(y));
        year.setSelectedItem("1990");
        card.add(year, c);

        // Row 2: CCCD + Loại vé
        c.gridx=0; c.gridy=2; c.weightx=0; card.add(new JLabel("CCCD"), c);
        c.gridx=1; c.weightx=1; final JTextField tfCCCD = new JTextField(16); card.add(tfCCCD, c);
        c.gridx=2; c.gridy=2; c.weightx=0; card.add(new JLabel("Loại Vé"), c);
        c.gridx=3; c.weightx=0.6; final JComboBox<String> type = new JComboBox<>(new String[]{
            "Vé dành cho học sinh, sinh viên","Vé người lớn","Vé trẻ em"
        });
        card.add(type, c);

        // Row 3: SĐT (từng vé)
        c.gridx=0; c.gridy=3; c.weightx=0; card.add(new JLabel("SĐT"), c);
        c.gridx=1; c.weightx=1; final JTextField tfPhone = new JTextField(16); card.add(tfPhone, c);

        // Row 4: Tiền vé
        c.gridx=0; c.gridy=4; c.weightx=0; JLabel priceLabel = new JLabel("Tiền Vé");
        priceLabel.setForeground(RED_PRIMARY);
        card.add(priceLabel, c);
        c.gridx=1; c.weightx=0.6; JTextField price = new JTextField(String.valueOf(perSeatPrice));
        price.setHorizontalAlignment(JTextField.RIGHT);
        price.setEditable(false);
        card.add(price, c);

        // Nút Điền nhanh sinh dữ liệu phù hợp
        quick.addActionListener(e -> { 
            String name  = genName();
            String phone = genPhone();
            String cccd  = genCCCD();
            int yob = genYOBAndSetType(type); // tự set loại vé theo tuổi
            tfName.setText(name);
            tfPhone.setText(phone);
            tfCCCD.setText(cccd);
            year.setSelectedItem(String.valueOf(yob));
        });

        // Bind để PaymentPage đọc lại
        FormRefs r = new FormRefs();
        r.tfName = tfName;
        r.tfPhone = tfPhone;
        r.tfCCCD = tfCCCD;
        r.cbYear = year;
        r.cbType = type;
        formBinding.put(seat.getMaGhe(), r);

        return card;
    }


    private JComponent bottomButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        p.setBackground(Color.WHITE);

        btnBack = solidButton("Quay Lại", new Color(0x64B5F6), Color.WHITE);
        btnBack.setBackground(new Color(0x64B5F6));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        btnBack.setBorder(new EmptyBorder(8, 20, 8, 20));

        btnNext = solidButton("Thanh Toán", GREEN_PRIMARY, Color.WHITE);
        btnNext.setBackground(GREEN_PRIMARY);
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.setBorder(new EmptyBorder(8, 20, 8, 20));
        btnNext.setEnabled(false);

        p.add(btnBack);
        p.add(btnNext);
        return p;
    }

    

    private void updateRightContent() {
        if (rightBody == null) return;
        rightBody.removeAll();

        if (selectedSeats.isEmpty()) {
            rightBody.add(emptyMessage("Chọn ghế để nhập thông tin khách hàng"));
        } else {
            rightBody.add(customerFormRow());
            rightBody.add(Box.createVerticalStrut(8));
            for (SeatSelection s : selectedSeats) {
                String title = String.format("Toa số: %d, Khoang: %s, Ghế: %s",
                        s.getSoToa(), s.getTenKhoang(), seatLabel(s));
                rightBody.add(ticketDetailCard(s, title));
                rightBody.add(Box.createVerticalStrut(8));
            }
        }

        rightBody.revalidate();
        rightBody.repaint();
    }
private void updateNextButtonState() {
        if (btnNext != null) {
            btnNext.setEnabled(!selectedSeats.isEmpty());
        }
        updateRightContent();
    }
    // ===================== Public API =====================

    public void setRoute(String gaDi, String gaDen, LocalDate ngay) {
        if (gaDi == null || gaDen == null) {
            routeLabel.setText("  Tuyến ...");
            return;
        }
        String dateText = ngay != null ? ngay.format(DateTimeFormatter.ofPattern("dd/MM")) : "";
        routeLabel.setText("  Tuyến " + gaDi + " -> " + gaDen + (dateText.isEmpty() ? "" : ", Ngày " + dateText));
    }

    public boolean loadSeatMap(String maChuyenTau) {
        try {
            currentCars = seatMapDao.loadSeatMap(maChuyenTau);
            renderSeatMap(currentCars);
            return true;
        } catch (SQLException ex) {
            currentCars = Collections.emptyList();
            renderSeatMap(currentCars);
            JOptionPane.showMessageDialog(this,
                    "Không thể tải sơ đồ ghế từ cơ sở dữ liệu. Vui lòng kiểm tra kết nối.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public int getCarCount() {
        return currentCars != null ? currentCars.size() : 0;
    }

    public void setSeatSelectionListener(SeatSelectionListener listener) {
        this.seatSelectionListener = listener;
    }

    public List<SeatSelection> getSelectedSeats() {
        return new ArrayList<>(selectedSeats);
    }

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
        updatingSelection = false;
        updateNextButtonState();
        updateRightContent();
}

    public void addBackActionListener(ActionListener listener) {
        if (btnBack != null) {
            btnBack.addActionListener(listener);
        }
    }

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

    public static class SeatSelection {
        private final int soToa;
        private final String maToa;
        private final String tenKhoang;
        private final String maKhoang;
        private final Ghe ghe;

        SeatSelection(ToaTau toa, KhoangTau khoang, Ghe ghe) {
            this.soToa = toa.getSoToa();
            this.maToa = toa.getMaToa();
            this.tenKhoang = khoang.getTenKhoangTau();
            this.maKhoang = khoang.getMaKhoangTau();
            this.ghe = ghe;
        }

        public int getSoToa() {
            return soToa;
        }

        public String getMaToa() {
            return maToa;
        }

        public String getTenKhoang() {
            return tenKhoang;
        }

        public String getMaKhoang() {
            return maKhoang;
        }

        public Ghe getGhe() {
            return ghe;
        }

        public int getSeatDisplayNumber() {
            int order = ghe.getThuTuHienThi();
            if (order > 0) {
                return order;
            }
            return ghe.getSoGheAsInt();
        }

        public String getMaGhe() {
            return ghe.getMaGhe();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeatSelection)) return false;
            SeatSelection that = (SeatSelection) o;
            return Objects.equals(ghe.getMaGhe(), that.ghe.getMaGhe());
        }

        @Override
        public int hashCode() {
            return Objects.hash(ghe.getMaGhe());
        }
    }
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chi tiết vé tàu");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ManChonGheNgoi());
            frame.setMinimumSize(new Dimension(1200, 720));
            frame.setSize(new Dimension(1200, 720));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
    }

    
    public java.util.Map<String, FormRefs> getFormBinding() {
        return formBinding;
    }
}