package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.text.NumberFormat;
import com.toedter.calendar.JDateChooser;

public class BanVe extends JPanel {

    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color BLUE_HOVER = new Color(47, 107, 255, 30);
    private static final Color PANEL_BG = new Color(245, 248, 253);

    private final CardLayout wizard = new CardLayout();
    private final JPanel cards = new JPanel(wizard);

    private final ChooseTripPage page1 = new ChooseTripPage();
    private final TicketDetailPage page2 = new TicketDetailPage();
    private final PaymentPage page3 = new PaymentPage();

    private TrainInfo currentTrain;
    private int currentCarIndex = 1;
    private final List<TicketSelection> selections = new ArrayList<>();

    public BanVe() {
        setLayout(new BorderLayout());
        setBackground(PANEL_BG);

        // BỎ THANH 1-2-3 (Step Bar)
        // add(stepBar, BorderLayout.NORTH);

        cards.add(page1, "p1");
        cards.add(page2, "p2");
        cards.add(page3, "p3");
        add(cards, BorderLayout.CENTER);

        showStep(1);
    }

    private void showStep(int step) {
        switch (step) {
            case 1 -> wizard.show(cards, "p1");
            case 2 -> { page2.refresh(); wizard.show(cards, "p2"); }
            case 3 -> { page3.refresh(); wizard.show(cards, "p3"); }
        }
    }

    // ======================= PAGE 1 =======================
    private class ChooseTripPage extends JPanel {
        private final CardLayout subCards = new CardLayout();
        private final JPanel subPanel = new JPanel(subCards);

        private final JComboBox<String> cbFrom = new JComboBox<>(new String[]{
                "Sài Gòn", "Biên Hòa", "Nha Trang", "Đà Nẵng", "Huế", "Vinh", "Hà Nội"
        });
        private final JComboBox<String> cbTo = new JComboBox<>(new String[]{
                "Hà Nội", "Vinh", "Huế", "Đà Nẵng", "Nha Trang", "Biên Hòa", "Sài Gòn"
        });
        private final JRadioButton rdOneWay = new JRadioButton("Một chiều", true);
        private final JRadioButton rdRound = new JRadioButton("Khứ hồi");
        private final JDateChooser dcStart = new JDateChooser(new Date());
        private final JDateChooser dcReturn = new JDateChooser(new Date());
        private final JButton btnSearch = new JButton("Tìm kiếm");

        private final JPanel trainsBar = new JPanel();
        private final JPanel seatArea = new JPanel(new BorderLayout());
        private final JButton btnChangeSearch = new JButton("Quay lại tìm chuyến");
        private final JButton btnNext = new JButton("Tiếp tục");

        ChooseTripPage() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel searchForm = buildSearchForm();
            JPanel resultView = buildResultView();

            subPanel.add(searchForm, "form");
            subPanel.add(resultView, "result");
            add(subPanel, BorderLayout.CENTER);

            subCards.show(subPanel, "form");

            rdOneWay.addActionListener(e -> dcReturn.setEnabled(false));
            rdRound.addActionListener(e -> dcReturn.setEnabled(true));

            btnSearch.addActionListener(e -> {
                List<TrainInfo> data = mockTrains();
                fillTrains(data);
                seatArea.removeAll();
                seatArea.add(centerMsg("Chọn một tàu để xem chỗ ngồi."), BorderLayout.CENTER);
                seatArea.revalidate(); seatArea.repaint();
                selections.clear();
                btnNext.setEnabled(false);
                subCards.show(subPanel, "result");
            });

            btnChangeSearch.addActionListener(e -> {
                selections.clear();
                subCards.show(subPanel, "form");
            });

            btnNext.addActionListener(e -> showStep(2));
        }

        private JPanel buildSearchForm() {
            JPanel wrap = new JPanel(new BorderLayout());
            wrap.setOpaque(false);

            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBorder(new EmptyBorder(12, 12, 12, 12));
            form.setPreferredSize(new Dimension(420, 10));

            Dimension field = new Dimension(260, 32);
            setFixedSize(cbFrom, field); setFixedSize(cbTo, field);
            dcStart.setDateFormatString("dd/MM/yyyy");
            dcReturn.setDateFormatString("dd/MM/yyyy");
            dcReturn.setEnabled(false);
            dcStart.setPreferredSize(field); dcReturn.setPreferredSize(field);

            JLabel title = new JLabel("Tìm chuyến");
            title.setForeground(new Color(18, 74, 147));
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(title);
            form.add(Box.createVerticalStrut(8));

            form.add(labeled("Ga đi", cbFrom));
            form.add(Box.createVerticalStrut(10));
            form.add(labeled("Ga đến", cbTo));
            form.add(Box.createVerticalStrut(12));

            ButtonGroup g = new ButtonGroup(); g.add(rdOneWay); g.add(rdRound);
            JPanel trip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            trip.setOpaque(false); rdOneWay.setOpaque(false); rdRound.setOpaque(false);
            trip.add(rdOneWay); trip.add(rdRound);
            form.add(labeled("Loại hành trình", trip));
            form.add(Box.createVerticalStrut(12));

            form.add(labeled("Ngày đi", dcStart));
            form.add(Box.createVerticalStrut(10));
            form.add(labeled("Ngày về", dcReturn));
            form.add(Box.createVerticalStrut(14));

            stylePrimary(btnSearch);
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            btnWrap.setOpaque(false);
            btnWrap.add(btnSearch);
            form.add(btnWrap);
            form.add(Box.createVerticalStrut(10));

            wrap.add(centerMsg("Nhập điều kiện tìm kiếm bên trái rồi bấm Tìm kiếm."), BorderLayout.CENTER);
            wrap.add(form, BorderLayout.WEST);
            return wrap;
        }

        private JPanel buildResultView() {
            JPanel container = new JPanel(new BorderLayout());
            container.setOpaque(false);

            // Top-right actions
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
            actions.setOpaque(false);
            actions.add(btnChangeSearch);
            btnNext.setEnabled(false);
            actions.add(btnNext);
            container.add(actions, BorderLayout.NORTH);

            // Train cards (scroll horizontal)
            trainsBar.setOpaque(false);
            trainsBar.setLayout(new BoxLayout(trainsBar, BoxLayout.X_AXIS));
            trainsBar.setBorder(new EmptyBorder(0, 12, 8, 12));
            JScrollPane trainScroll = new JScrollPane(trainsBar,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            trainScroll.setPreferredSize(new Dimension(10, 170)); // cao hơn để không bị "che"
            trainScroll.setBorder(BorderFactory.createEmptyBorder());
            trainScroll.getHorizontalScrollBar().setUnitIncrement(16);
            container.add(trainScroll, BorderLayout.CENTER);

            // Seat area + legend (bottom-right)
            JPanel centerWrap = new JPanel(new BorderLayout());
            centerWrap.setOpaque(false);
            centerWrap.setBorder(new EmptyBorder(8, 0, 0, 0)); // tách khỏi train cards
            seatArea.setOpaque(false);
            seatArea.add(centerMsg("Chọn tàu ở trên để xem toa & chỗ ngồi."), BorderLayout.CENTER);
            centerWrap.add(seatArea, BorderLayout.CENTER);

            JPanel legendRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
            legendRight.setOpaque(false);
            legendRight.add(tag(new Color(184, 231, 255), "Ghế Ngồi Mềm Điều Hòa"));
            legendRight.add(tag(new Color(140, 200, 140), "Giường Nằm Khoang "));
            legendRight.add(tag(new Color(255, 120, 120), "Ghế Đang Chọn"));
            legendRight.add(tag(new Color(180, 180, 180), "Ghế Đã Đặt")); // thêm chú thích ghế đã đặt (booked)
            centerWrap.add(legendRight, BorderLayout.SOUTH);

            container.add(centerWrap, BorderLayout.SOUTH);
            return container;
        }

        private void fillTrains(List<TrainInfo> trains) {
            trainsBar.removeAll();
            ButtonGroup g = new ButtonGroup();
            for (TrainInfo t : trains) {
                TrainCard card = new TrainCard(t, true);
                card.addActionListener(e -> {
                    currentTrain = t;
                    currentCarIndex = 1;
                    showTrain(t);
                });
                g.add(card.toggle);
                trainsBar.add(card);
                trainsBar.add(Box.createHorizontalStrut(12));
            }
            trainsBar.add(Box.createHorizontalStrut(4));
            trainsBar.revalidate(); trainsBar.repaint();
        }

        private void showTrain(TrainInfo t) {
            seatArea.removeAll();
            seatArea.add(buildCarAndSeat(t), BorderLayout.CENTER);
            seatArea.revalidate(); seatArea.repaint();
            selections.clear();
            btnNext.setEnabled(false);
        }

        private JPanel buildCarAndSeat(TrainInfo t) {
            JPanel root = new JPanel(new BorderLayout());
            root.setOpaque(false);

            JPanel cars = new JPanel();
            cars.setOpaque(false);
            cars.setLayout(new BoxLayout(cars, BoxLayout.X_AXIS));
            cars.setBorder(new EmptyBorder(10, 12, 10, 12));

            CardLayout seatCards = new CardLayout();
            JPanel seatCardsPanel = new JPanel(seatCards);
            seatCardsPanel.setOpaque(false);

            ButtonGroup g = new ButtonGroup();
            int carCount = t.carCount;
            for (int i = 1; i <= carCount; i++) {
                final int carIndex = i;
                JToggleButton btn = new JToggleButton("Toa " + i);
                stylePill(btn);
                g.add(btn);
                cars.add(btn);
                cars.add(Box.createHorizontalStrut(6));

                JPanel seatMap = buildSeatMap(t, carIndex);
                seatCardsPanel.add(seatMap, "car" + carIndex);

                btn.addActionListener(e -> {
                    currentCarIndex = carIndex;
                    seatCards.show(seatCardsPanel, "car" + carIndex);
                });
                if (i == 1) btn.setSelected(true);
            }

            root.add(cars, BorderLayout.NORTH);
            root.add(seatCardsPanel, BorderLayout.CENTER);
            return root;
        }

        private JPanel buildSeatMap(TrainInfo t, int carIndex) {
            int rows = 14;
            JPanel map = new JPanel(new GridBagLayout());
            map.setOpaque(false);
            map.setBorder(new EmptyBorder(8, 12, 12, 12));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6,6,6,6);
            gc.fill = GridBagConstraints.BOTH;

            int seatNo = 1;
            Random rnd = new Random(1000 * carIndex + t.code.hashCode());

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < 2; c++) {
                    gc.gridx = c; gc.gridy = r;
                    SeatButton seat = makeSeat(seatNo++, t, carIndex, rnd);
                    map.add(seat, gc);
                }
                gc.gridx = 2; gc.gridy = r;
                JPanel aisle = new JPanel(); aisle.setOpaque(false); aisle.setPreferredSize(new Dimension(12, 36));
                map.add(aisle, gc);
                for (int c = 3; c < 5; c++) {
                    gc.gridx = c; gc.gridy = r;
                    SeatButton seat = makeSeat(seatNo++, t, carIndex, rnd);
                    map.add(seat, gc);
                }
            }

            JLabel lab = new JLabel("Toa số " + carIndex + " · " + t.coachLabel, SwingConstants.CENTER);
            lab.setBorder(new EmptyBorder(8, 0, 8, 0));
            lab.setFont(lab.getFont().deriveFont(Font.BOLD));
            JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false); top.add(lab, BorderLayout.CENTER);

            JPanel wrap = new JPanel(new BorderLayout()); wrap.setOpaque(false);
            wrap.add(top, BorderLayout.NORTH); wrap.add(map, BorderLayout.CENTER);
            return wrap;
        }

        private SeatButton makeSeat(int number, TrainInfo t, int carIndex, Random rnd) {
            SeatButton b = new SeatButton(String.valueOf(number));
            boolean booked = rnd.nextInt(100) < 15;
            b.setEnabled(!booked);
            if (booked) {
                b.setToolTipText("Ghế đã được đặt");
            } else {
                b.addActionListener(e -> {
                    boolean selected = b.getModel().isSelected();
                    if (selected) selections.add(new TicketSelection(t, carIndex, number, estimatePrice(t, carIndex, number)));
                    else selections.removeIf(s -> s.train.code.equals(t.code) && s.car == carIndex && s.seat == number);
                    btnNext.setEnabled(!selections.isEmpty());
                });
            }
            return b;
        }
    }

    // ======================= PAGE 2 =======================
    private class TicketDetailPage extends JPanel {
        private final JPanel list = new JPanel();
        private final JButton btnBack = new JButton("Quay lại");
        private final JButton btnNext = new JButton("Tiếp tục");

        TicketDetailPage() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel head = new JLabel("CHI TIẾT VÉ", SwingConstants.LEFT);
            head.setBorder(new EmptyBorder(12, 14, 12, 14));
            head.setOpaque(true);
            head.setBackground(new Color(220, 235, 255));
            head.setFont(head.getFont().deriveFont(Font.BOLD, 20f));
            add(head, BorderLayout.NORTH);

            list.setOpaque(false);
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(new EmptyBorder(0,0,0,0));
            add(sp, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            foot.add(btnBack); foot.add(btnNext);
            add(foot, BorderLayout.SOUTH);

            btnBack.addActionListener(e -> showStep(1));
            btnNext.addActionListener(e -> {
                if (selections.isEmpty()) {
                    JOptionPane.showMessageDialog(BanVe.this, "Chưa có ghế nào được chọn.");
                    return;
                }
                showStep(3);
            });
        }

        void refresh() {
            list.removeAll();
            int idx = 1;
            for (TicketSelection s : selections) {
                list.add(ticketCard("Chi Tiết Vé " + idx++, s));
                list.add(Box.createVerticalStrut(10));
            }
            if (selections.isEmpty()) {
                list.add(centerMsg("Chưa chọn ghế nào. Vui lòng quay lại Bước 1."));
            }
            revalidate(); repaint();
        }

        private JPanel ticketCard(String title, TicketSelection sel) {
            JPanel card = new JPanel(new BorderLayout());
            TitledBorder tb = BorderFactory.createTitledBorder(title + " – Tàu " + sel.train.code + " · Toa " + sel.car + " · Ghế " + sel.seat);
            tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD, 16f));
            card.setBorder(BorderFactory.createCompoundBorder(
                    tb,
                    new EmptyBorder(10, 10, 10, 10)
            ));

            JPanel grid = new JPanel(new GridLayout(0, 2, 10, 8));
            Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);

            JLabel lbName = new JLabel("Họ Tên"); lbName.setFont(labelFont);
            JLabel lbPhone = new JLabel("SĐT"); lbPhone.setFont(labelFont);
            JLabel lbCCCD = new JLabel("CCCD"); lbCCCD.setFont(labelFont);
            JLabel lbBirth = new JLabel("Năm sinh"); lbBirth.setFont(labelFont);
            JLabel lbType = new JLabel("Loại vé"); lbType.setFont(labelFont);

            JTextField tfName = new JTextField();
            JTextField tfPhone = new JTextField();
            JTextField tfCCCD = new JTextField();
            JTextField tfBirth = new JTextField();
            JComboBox<String> cbType = new JComboBox<>(new String[]{
                    "Vé thường", "Vé dành cho học sinh, sinh viên", "Vé người cao tuổi"
            });
            Dimension f = new Dimension(220, 32);
            tfName.setPreferredSize(f); tfPhone.setPreferredSize(f);
            tfCCCD.setPreferredSize(f); tfBirth.setPreferredSize(f); cbType.setPreferredSize(f);

            sel.tfName = tfName; sel.tfPhone = tfPhone; sel.tfCCCD = tfCCCD; sel.tfBirth = tfBirth; sel.cbType = cbType;

            grid.add(lbName); grid.add(tfName);
            grid.add(lbPhone); grid.add(tfPhone);
            grid.add(lbCCCD); grid.add(tfCCCD);
            grid.add(lbBirth); grid.add(tfBirth);
            grid.add(lbType); grid.add(cbType);

            JPanel priceRow = new JPanel(new BorderLayout());
            JLabel price = new JLabel("Giá vé: " + formatVND(sel.price));
            price.setForeground(new Color(200,0,0));
            price.setFont(price.getFont().deriveFont(Font.BOLD, 14f));
            JButton remove = new JButton("Xóa ghế");
            remove.addActionListener(e -> { selections.remove(sel); refresh(); });
            priceRow.add(price, BorderLayout.WEST);
            priceRow.add(remove, BorderLayout.EAST);

            JPanel center = new JPanel(new BorderLayout());
            center.add(grid, BorderLayout.CENTER);
            center.add(priceRow, BorderLayout.SOUTH);

            card.add(center, BorderLayout.CENTER);
            return card;
        }
    }

    // ======================= PAGE 3 =======================
    private class PaymentPage extends JPanel {
        private final JLabel totalLabel = new JLabel("Tổng tiền: 0₫");
        private final JRadioButton cash = new JRadioButton("Tiền mặt");
        private final JRadioButton bank = new JRadioButton("Chuyển khoản", true);
        private final JButton btnBack = new JButton("Quay Lại");
        private final JButton btnConfirm = new JButton("Xác Nhận");

        PaymentPage() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JLabel head = new JLabel("THANH TOÁN", SwingConstants.LEFT);
            head.setBorder(new EmptyBorder(12, 14, 12, 14));
            head.setOpaque(true);
            head.setBackground(new Color(220, 235, 255));
            head.setFont(head.getFont().deriveFont(Font.BOLD, 20f));
            add(head, BorderLayout.NORTH);

            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(6,6,6,6);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 0; gc.gridy = 0;
            ButtonGroup g = new ButtonGroup(); g.add(cash); g.add(bank);
            center.add(new JLabel("Hình thức"), gc);
            gc.gridx = 1; JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            p.setOpaque(false); p.add(cash); p.add(bank);
            center.add(p, gc);

            gc.gridx = 0; gc.gridy = 1;
            center.add(new JLabel("Số tiền cần thanh toán"), gc);
            gc.gridx = 1; totalLabel.setForeground(new Color(200,0,0));
            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
            center.add(totalLabel, gc);

            add(center, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            foot.add(btnBack); foot.add(btnConfirm);
            add(foot, BorderLayout.SOUTH);

            btnBack.addActionListener(e -> showStep(2));
            btnConfirm.addActionListener(e -> JOptionPane.showMessageDialog(BanVe.this,
                    "Demo: sau này sẽ tạo hóa đơn & vé."));
        }

        void refresh() {
            int total = selections.stream().mapToInt(s -> s.price).sum();
            totalLabel.setText("Tổng tiền: " + formatVND(total));
        }
    }

    // ======= Common helpers =======
    private static JPanel labeled(String label, JComponent comp) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        JLabel lb = new JLabel(label);
        lb.setBorder(new EmptyBorder(0, 2, 4, 2));
        wrap.add(lb);

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        row.add(comp, BorderLayout.CENTER);
        wrap.add(row);
        return wrap;
    }
    private static void stylePrimary(AbstractButton b) {
        b.setBackground(BLUE); b.setForeground(Color.WHITE); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    private static Component tag(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setBackground(c);
        p.setBorder(new EmptyBorder(4,8,4,8));
        p.add(new JLabel(text));
        return p;
    }
    private static JPanel centerMsg(String s) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel("<html><div style='padding:8px;color:#666;'>" + s + "</div></html>", SwingConstants.CENTER);
        p.add(l, BorderLayout.CENTER); return p;
    }
    private static void setFixedSize(JComponent c, Dimension d) {
        c.setMinimumSize(d); c.setPreferredSize(d); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
    }

    private static void stylePill(AbstractButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addChangeListener(e -> {
            if (b.isSelected()) { b.setBackground(BLUE); b.setForeground(Color.WHITE); }
            else if (b.getModel().isRollover()) { b.setBackground(BLUE_HOVER); b.setForeground(Color.BLACK); }
            else { b.setBackground(Color.WHITE); b.setForeground(Color.BLACK); }
        });
    }

    private static class TrainInfo {
        final String code, depart, arrive, coachLabel; final int carCount;
        TrainInfo(String code, String depart, String arrive, String coachLabel, int carCount) {
            this.code = code; this.depart = depart; this.arrive = arrive;
            this.coachLabel = coachLabel; this.carCount = carCount;
        }
    }
    private static class TicketSelection {
        final TrainInfo train; final int car; final int seat; final int price;
        JTextField tfName, tfPhone, tfCCCD, tfBirth; JComboBox<String> cbType;
        TicketSelection(TrainInfo t, int car, int seat, int price) {
            this.train = t; this.car = car; this.seat = seat; this.price = price;
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
            toggle.addChangeListener(e -> {
                ButtonModel m = toggle.getModel();
                if (m.isSelected()) { inner.setBackground(new Color(47,107,255)); toggle.setForeground(Color.WHITE); }
                else if (m.isRollover()) { inner.setBackground(new Color(47,107,255,30)); toggle.setForeground(Color.BLACK); }
                else { inner.setBackground(Color.WHITE); toggle.setForeground(Color.BLACK); }
            });

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

    private List<TrainInfo> mockTrains() {
        return Arrays.asList(
                new TrainInfo("SE7", "06:00", "17:35", "Ngồi mềm điều hòa", 6),
                new TrainInfo("SE5", "08:55", "20:20", "Ngồi mềm", 6),
                new TrainInfo("SE9", "12:50", "03:40", "Ngồi mềm", 6),
                new TrainInfo("SE3", "19:20", "05:15", "Ngồi mềm", 6),
                new TrainInfo("SE1", "20:50", "05:45", "Ngồi mềm", 6)
        );
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

    private static String formatVND(int amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi","VN"));
        return nf.format(amount) + "₫";
    }
}
