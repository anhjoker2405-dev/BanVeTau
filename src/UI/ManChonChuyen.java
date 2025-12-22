package ui;

// ================== CHỨC NĂNG BÁN VÉ · MÀN CHỌN CHUYẾN ==================
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.time.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;

public class ManChonChuyen extends JPanel {

    // ====== Style ======
    private static final Color STEP_ACTIVE    = new Color(31, 133, 230);
    private static final Color STEP_INACTIVE  = new Color(210, 210, 210);
    private static final Color STRIPE_BG      = new Color(226, 238, 255);
    private static final Color CARD_BORDER    = new Color(224, 230, 236);
    private static final Color TEXT_BLUE      = new Color(35, 125, 214);
    private static final Color TEXT_RED       = new Color(212, 39, 39);
    private static final Color BTN_BLUE       = new Color(31, 133, 230);
    private static final Font  TITLE_FONT     = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font  BIG_FONT       = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font  NORM_FONT      = new Font("Segoe UI", Font.PLAIN, 14);

    // ====== Top context ======
    private JLabel lbStep1; private JLabel lbStep2; private JLabel lbStep3;
    private JLabel lbRoute;

    // ====== Center list ======
    private JPanel listPanel;    // chứa các TripRow
    private JButton btnBack;
    private Consumer<Trip> chooseTripListener;

    public ManChonChuyen() {
        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(10,12,10,12));
        setBackground(Color.WHITE);

        add(buildStepper(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // (demo removed) - context & trips will be set from BanVe integration
    }

    // ---------- Top: stepper + stripe route ----------
    private JComponent buildStepper() {
        JPanel root = new JPanel(new BorderLayout(0,8));
        root.setOpaque(false);

        JPanel steps = new JPanel(new GridLayout(1,3,12,0));
        steps.setOpaque(false);

        lbStep1 = pill("1  CHỌN CHUYẾN", true);
        lbStep2 = pill("2  CHI TIẾT VÉ", false);
        lbStep3 = pill("3  THANH TOÁN", false);

        steps.add(wrapPill(lbStep1, true));
        steps.add(wrapPill(lbStep2, false));
        steps.add(wrapPill(lbStep3, false));

        // Stripe tiêu đề tuyến
        JPanel stripe = new JPanel(new BorderLayout());
        stripe.setBackground(STRIPE_BG);
        stripe.setBorder(new CompoundBorder(new LineBorder(new Color(210,223,243),1),
                                            new EmptyBorder(8,10,8,10)));
        lbRoute = new JLabel();
        lbRoute.setFont(TITLE_FONT);
        stripe.add(lbRoute, BorderLayout.WEST);

        root.add(steps, BorderLayout.NORTH);
        root.add(stripe, BorderLayout.CENTER);
        return root;
    }

    private JLabel pill(String text, boolean active) {
        JLabel lb = new JLabel(text, SwingConstants.CENTER);
        lb.setOpaque(true);
        lb.setForeground(active ? Color.WHITE : Color.DARK_GRAY);
        lb.setBackground(active ? STEP_ACTIVE : STEP_INACTIVE);
        lb.setFont(BIG_FONT);
        lb.setBorder(new EmptyBorder(8,12,8,12));
        return lb;
    }
    private JComponent wrapPill(JLabel pill, boolean active) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(pill, BorderLayout.CENTER);
        pill.setBorder(new CompoundBorder(new LineBorder(new Color(190,190,190),1,true),
                                          new EmptyBorder(8,12,8,12)));
        pill.setBackground(active ? STEP_ACTIVE : STEP_INACTIVE);
        pill.setForeground(active ? Color.WHITE : Color.DARK_GRAY);
        return p;
    }

    // ---------- Center: list ----------
    private JComponent buildCenter() {
    // Màu nền (vùng bao quanh card)
    Color BG = new Color(245, 245, 245);

    // === Panel chứa các TripRow ===
    listPanel = new JPanel();
    listPanel.setOpaque(false);
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    listPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

    // Card trắng bọc listPanel
    JPanel tripsCard = new JPanel(new BorderLayout());
    tripsCard.setBackground(Color.WHITE);
    tripsCard.setBorder(new CompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            new EmptyBorder(10, 10, 10, 10)
    ));
    tripsCard.add(listPanel, BorderLayout.NORTH);

    // Padding ngoài cho card
    JPanel outerPadding = new JPanel(new BorderLayout());
    outerPadding.setOpaque(false);                      // để lộ màu BG của viewport
    outerPadding.setBorder(new EmptyBorder(9, 9, 9, 9));
    outerPadding.add(tripsCard, BorderLayout.NORTH);    // giữ BoxLayout theo trục Y

    // Wrapper để card co giãn ngang trong viewport
    JPanel wrapper = new JPanel(new GridBagLayout());
    wrapper.setOpaque(false);
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0; c.gridy = 0; c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    wrapper.add(outerPadding, c);

    // ScrollPane: đưa wrapper trực tiếp vào viewport
    JScrollPane sp = new JScrollPane(wrapper,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setBorder(null);
    sp.getVerticalScrollBar().setUnitIncrement(16);
    sp.getViewport().setOpaque(true);
    sp.getViewport().setBackground(BG);                 // màu nền viewport

    return sp;
}


    // ---------- Footer ----------
    private JComponent buildFooter() {
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        btnBack = new JButton("Quay Lại");
        styleBlueButton(btnBack, false);
        btnBack.setPreferredSize(new Dimension(120, 32));
        south.add(btnBack);
        return south;
    }

    // ============ Public API ============
    public void onBack(ActionListener al) { btnBack.addActionListener(al); }

    public void onChooseTrip(ActionListener al) {
        setTripSelectionListener(trip -> al.actionPerformed(new ActionEvent(trip, ActionEvent.ACTION_PERFORMED, trip.code)));
    }

    public void setTripSelectionListener(Consumer<Trip> listener) {
        this.chooseTripListener = listener;
    }

    private void fireTripChosen(Trip trip) {
        if (chooseTripListener != null) {
            chooseTripListener.accept(trip);
        }
    }

    public void setContext(String gaDi, String gaDen, LocalDate ngay) {
        String dateText = ngay.format(DateTimeFormatter.ofPattern("dd/MM"));
        lbRoute.setText("Tuyến " + gaDi + " -> " + gaDen + ", Ngày " + dateText);
    }

    public void setTrips(List<Trip> trips) {
        listPanel.removeAll();
        if (trips == null || trips.isEmpty()) {
            JLabel empty = new JLabel("Không có chuyến phù hợp.", SwingConstants.CENTER);
            empty.setFont(NORM_FONT);
            empty.setBorder(new EmptyBorder(40,0,40,0));
            listPanel.add(empty);
        } else {
            for (Trip t : trips) {
                listPanel.add(new TripRow(t));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    // ============ Inner: TripRow ============
    private class TripRow extends JPanel {
        private final Trip trip;
        private final JButton btnChoose;

        TripRow(Trip t) {
            this.trip = t;
            setLayout(new BorderLayout());
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(new CompoundBorder(new LineBorder(CARD_BORDER,1,true),
                                         new EmptyBorder(10,12,10,12)));
            // Giãn full theo chiều ngang khi cửa sổ rộng
            // Giãn full theo chiều ngang khi cửa sổ rộng 
// setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height)); //  gây co về 0 khi pref chưa tính


            // Left: icon + code (link style)
            JPanel left = new JPanel(new BorderLayout(8,0));
            left.setOpaque(false);
            JLabel icon = new JLabel("\uD83D\uDE86"); // unicode 🚆
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            icon.setBorder(new EmptyBorder(4, 0, 0, 0)); // đẩy icon xuống 4px
            
            JPanel trainInfo = new JPanel();
            trainInfo.setLayout(new BoxLayout(trainInfo, BoxLayout.Y_AXIS));
            trainInfo.setOpaque(false);

            String trainCodeText = Optional.ofNullable(t.trainCode).filter(s -> !s.isBlank()).orElse("—");
            JLabel trainCode = new JLabel(trainCodeText);
            trainCode.setForeground(TEXT_BLUE);
            trainCode.setFont(BIG_FONT);

            String trainNameText = Optional.ofNullable(t.trainName).orElse("").trim();
            JLabel trainName = new JLabel(trainNameText);
            trainName.setFont(NORM_FONT);

            JLabel tripCode = new JLabel("Mã chuyến: " + Optional.ofNullable(t.code).orElse(""));
            tripCode.setFont(NORM_FONT.deriveFont(Font.PLAIN, 12f));
            tripCode.setForeground(Color.DARK_GRAY);

            trainInfo.add(trainCode);
            trainInfo.add(Box.createVerticalStrut(2));
            if (!trainNameText.isEmpty()) {
                trainInfo.add(trainName);
                trainInfo.add(Box.createVerticalStrut(2));
            }
            trainInfo.add(tripCode);

            left.add(icon, BorderLayout.WEST);
            left.add(trainInfo, BorderLayout.CENTER);


            // Center: timeline with arrow, stations, duration
            JComponent center = new Timeline(t);

            // Right: seats + price + choose button (cùng hàng, nút bám mép phải)
            JPanel right = new JPanel(new BorderLayout(12,0));
            right.setOpaque(false);
            right.setBorder(new EmptyBorder(0, 12, 0, 6));

            JPanel cols = new JPanel(new GridLayout(1,4,10,0));
            cols.setOpaque(false);
            
            JLabel lbSeat = new JLabel("Ghế Trống", SwingConstants.RIGHT);
            lbSeat.setFont(NORM_FONT.deriveFont(Font.BOLD));   // kế thừa font cũ và chỉ đổi sang BOLD
            lbSeat.setForeground(TEXT_BLUE);
            JLabel valSeat = new JLabel(String.valueOf(t.availableSeats), SwingConstants.LEFT);
            valSeat.setFont(BIG_FONT); valSeat.setForeground(TEXT_RED);

            JLabel lbPrice = new JLabel("Giá Vé", SwingConstants.RIGHT);
            lbPrice.setFont(NORM_FONT.deriveFont(Font.BOLD));  // kế thừa font cũ và chỉ đổi sang BOLD
            lbPrice.setForeground(TEXT_BLUE);
            JLabel valPrice = new JLabel(formatMoney(t.price), SwingConstants.LEFT);
            valPrice.setFont(BIG_FONT); valPrice.setForeground(TEXT_RED);

            cols.add(lbSeat); cols.add(valSeat);
            cols.add(lbPrice); cols.add(valPrice);

            btnChoose = new JButton("Chọn");
//            styleBlueButton(btnChoose, true);
            stylePrimaryButton(btnChoose);
            btnChoose.putClientProperty("trip", t);
            btnChoose.setPreferredSize(new Dimension(120, 34));
            btnChoose.addActionListener(e -> fireTripChosen(trip));

            JPanel btnWrap = new JPanel(new GridBagLayout());
            btnWrap.setOpaque(false);
            btnWrap.add(btnChoose, new GridBagConstraints());

            right.add(cols, BorderLayout.CENTER);
            right.add(btnWrap, BorderLayout.EAST);

            add(left, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
            add(right, BorderLayout.EAST);
        }

        // Cho phép hàng giãn hết bề rộng nhưng giữ chiều cao theo preferred
        @Override public Dimension getMaximumSize() {
            Dimension pref = getPreferredSize();
            return new Dimension(Integer.MAX_VALUE, pref.height);
        }
    }

    // ---- Timeline: icon trên line, không viền; time/date trên line; duration dưới line ----
    private static class Timeline extends JComponent {
        private final Trip trip;
        private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
        private static final DateTimeFormatter DM = DateTimeFormatter.ofPattern("dd/MM");

        Timeline(Trip t) {
            this.trip = t;
            setOpaque(false);
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(10, 84); // width sẽ do layout kéo giãn, chỉ cố định height
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            // Thanh + mũi tên
            int padLeft = 90, padRight = 90;
            int lineY = h / 2;
            int arrowLen = 18, arrowHalf = 6;
            int lineStart = padLeft;
            int lineEnd   = w - padRight - arrowLen;
            int tipX      = w - padRight; // đỉnh mũi tên

            // 1) Đường timeline
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(Color.DARK_GRAY);
            g2.drawLine(lineStart, lineY, lineEnd, lineY);

            // 2) Mũi tên
            Polygon arrow = new Polygon();
            arrow.addPoint(lineEnd, lineY - arrowHalf);
            arrow.addPoint(lineEnd, lineY + arrowHalf);
            arrow.addPoint(tipX,   lineY);
            g2.fill(arrow);

            // 3) Icon 🚆 trên thanh (không vòng tròn)
            String train = "\uD83D\uDE86";
            int trainFontSize = 30;
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, trainFontSize));
            FontMetrics fmTrain = g2.getFontMetrics();
            int trainX = (w - fmTrain.stringWidth(train)) / 2;
            int trainY = lineY - 8;
            g2.setColor(Color.BLACK);
            g2.drawString(train, trainX, trainY);

            // 4) Giờ/Ngày: canh giữa ở đầu line và tại mũi tên, đặt trên line
            String tDepart = HM.format(trip.depart);
            String dDepart = DM.format(trip.depart.toLocalDate());
            String tArrive = HM.format(trip.arrive);
            String dArrive = DM.format(trip.arrive.toLocalDate());

            int lift = 10; // nâng cụm time/date lên cao
            int groupTopY  = lineY - 18 - lift; // time
            int groupBotY  = lineY -  2 - lift; // date

            g2.setFont(BIG_FONT);
            FontMetrics fmBig = g2.getFontMetrics();
            g2.setFont(new Font(BIG_FONT.getName(), Font.PLAIN, BIG_FONT.getSize()-4));
            FontMetrics fmSmall = g2.getFontMetrics();

            int leftGroupW  = Math.max(fmBig.stringWidth(tDepart),  fmSmall.stringWidth(dDepart));
            int rightGroupW = Math.max(fmBig.stringWidth(tArrive),  fmSmall.stringWidth(dArrive));

            int leftX  = lineStart - leftGroupW/2;
            int rightX = tipX      - rightGroupW/2;

            // vẽ trái
            g2.setFont(BIG_FONT);
            g2.setColor(TEXT_RED);
            g2.drawString(tDepart, leftX + (leftGroupW - fmBig.stringWidth(tDepart))/2, groupTopY);
            g2.setFont(new Font(BIG_FONT.getName(), Font.PLAIN, BIG_FONT.getSize()-4));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(dDepart, leftX + (leftGroupW - fmSmall.stringWidth(dDepart))/2, groupBotY);

            // vẽ phải
            g2.setFont(BIG_FONT);
            g2.setColor(TEXT_RED);
            g2.drawString(tArrive, rightX + (rightGroupW - fmBig.stringWidth(tArrive))/2, groupTopY);
            g2.setFont(new Font(BIG_FONT.getName(), Font.PLAIN, BIG_FONT.getSize()-4));
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(dArrive, rightX + (rightGroupW - fmSmall.stringWidth(dArrive))/2, groupBotY);

            // 5) Thời lượng: DƯỚI thanh, giữa
            g2.setFont(NORM_FONT);
            g2.setColor(TEXT_BLUE);
            String dur = formatDuration(trip.depart, trip.arrive);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(dur, (w - fm.stringWidth(dur))/2, lineY + 22);

            // 6) Tên ga dưới
            g2.setFont(NORM_FONT);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(trip.departStation, lineStart - 35, lineY + 26);
            int toW = g2.getFontMetrics().stringWidth(trip.arriveStation);
            g2.drawString(trip.arriveStation, w - padRight - toW + 6, lineY + 26);

            g2.dispose();
        }

        private static String formatDuration(LocalDateTime start, LocalDateTime end) {
            long mins = java.time.temporal.ChronoUnit.MINUTES.between(start, end);
            long h = mins / 60, m = mins % 60;
            if (h > 0) return h + " giờ " + (m > 0 ? m + " phút" : "");
            return m + " phút";
        }
    }

    private static void styleBlueButton(AbstractButton b, boolean solid) {
        b.setFont(BIG_FONT);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (solid) {
            b.setBackground(BTN_BLUE);
            b.setForeground(Color.WHITE);
            b.setBorder(new CompoundBorder(new LineBorder(new Color(0,0,0,30),1,true),
                                           new EmptyBorder(6,12,6,12)));
        } else {
            b.setBackground(new Color(245, 248, 255));
            b.setForeground(BTN_BLUE.darker());
            b.setBorder(new CompoundBorder(new LineBorder(BTN_BLUE,1,true),
                                           new EmptyBorder(6,18,6,18)));
        }
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) {
            return "—";
        }
        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        fmt.setMaximumFractionDigits(0);
        fmt.setMinimumFractionDigits(0);
        return fmt.format(value) + "đ";
    }

    // ======= Model =======
    public static class Trip {
        public final String code;
        public final String trainCode;
        public final String trainName;
        public final String departStation, arriveStation;
        public final LocalDateTime depart, arrive;
        public final int availableSeats;
        public final BigDecimal price;

        public Trip(String code, String trainCode, String trainName,
                    String departStation, String arriveStation,
                    LocalDateTime depart, LocalDateTime arrive,
                    int availableSeats, BigDecimal price) {
            this.code = code;
            this.trainCode = trainCode;
            this.trainName = trainName;
            this.departStation = departStation;
            this.arriveStation = arriveStation;
            this.depart = depart;
            this.arrive = arrive;
            this.availableSeats = availableSeats;
            this.price = price;
        }
    }

    // ======= Demo & helpers =======
    private static List<Trip> sampleTrips() {
        LocalDate d = LocalDate.of(2025,6,14);
        return Arrays.asList(
            new Trip("CT-0001", "TAU-014", "Tàu Bắc Nam", "An Hòa", "Bảo Sơn",
                LocalDateTime.of(d, LocalTime.of(16,47)),
                LocalDateTime.of(d, LocalTime.of(19,42)),
                100, BigDecimal.valueOf(115800)),
            new Trip("CT-0002", "TAU-021", "Tàu SE1", "An Hòa", "Bảo Sơn",
                LocalDateTime.of(d, LocalTime.of(18,10)),
                LocalDateTime.of(d, LocalTime.of(21,5)),
                62, BigDecimal.valueOf(129000)),
            new Trip("CT-0003", "TAU-035", "Tàu SE2", "An Hòa", "Bảo Sơn",
                LocalDateTime.of(d, LocalTime.of(19,30)),
                LocalDateTime.of(d, LocalTime.of(22,15)),
                80, BigDecimal.valueOf(158000))
            
        );
    }
    
    private void stylePrimaryButton(JButton btn) {
        // Ép dùng BasicButtonUI để Windows L&F không ghi đè màu nền
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());

        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);

        btn.setBackground(new Color(0x1976D2)); // xanh đậm
        btn.setForeground(Color.WHITE);         // chữ trắng
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBorder(BorderFactory.createLineBorder(new Color(0x1565C0)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Giữ màu khi trạng thái enable/disable thay đổi
        btn.addChangeListener(e -> {
            if (btn.isEnabled()) {
                if (!btn.getModel().isRollover()) {
                    btn.setBackground(new Color(0x1976D2));
                }
                btn.setForeground(Color.WHITE);
            } else {
                // Nếu muốn khi disable vẫn xanh: giữ nguyên; 
                // còn nếu muốn xám thì có thể setForeground(new Color(180,180,180));
                btn.setBackground(new Color(0x1976D2));
                btn.setForeground(new Color(255,255,255,180));
            }
        });

        // Hiệu ứng hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(0x2196F3));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(new Color(0x1976D2));
            }
        });
    }

    // ======= Public getters để tích hợp tầng Controller =======
    public JButton getBackButton(){ return btnBack; }
    
    

    // ======= Quick run =======
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Chọn chuyến");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setContentPane(new ManChonChuyen());
            // mở toàn màn hình
            f.pack();
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setVisible(true);
        });
    }
}