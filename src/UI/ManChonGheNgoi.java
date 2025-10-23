package ui;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.HashSet;

public class ManChonGheNgoi extends JFrame {

    // ==== Màu dùng chung ====
    private static final Color BLUE_PRIMARY   = new Color(0x1976D2);
    private static final Color BLUE_LIGHT     = new Color(0xE3F2FD);
    private static final Color BLUE_BORDER    = new Color(0x90CAF9);
    private static final Color GREEN_PRIMARY  = new Color(0x2E7D32);
    private static final Color GREEN_SOFT     = new Color(0x43A047);
    private static final Color RED_PRIMARY    = new Color(0xE53935);
    private static final Color RED_SOFT       = new Color(0xEF9A9A);
    private static final Color GRAY_BG        = new Color(0xF7F7F7);
    private static final Color GRAY_TEXT      = new Color(0x757575);
    private static final Color SEAT_FREE      = new Color(0xBFE3FF);
    private static final Color SEAT_SELECTED  = new Color(0xFF6F61);
    private static final Color SEAT_SOLD      = new Color(0xBDBDBD);

    public ManChonGheNgoi() {
        super("Chi tiết vé tàu");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 720));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(Color.WHITE);

        // ===== Top Step Bar =====
        root.add(buildStepBar(), BorderLayout.NORTH);

        // ===== Split trái/phải =====
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        split.setDividerSize(8);
        split.setResizeWeight(0.68); // ~ 70/30
        split.setBorder(new LineBorder(new Color(230, 230, 230)));
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ---------------- Step Bar ----------------
    private JComponent buildStepBar() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 0, 8);

        p.add(stepButton("1", "CHỌN CHUYẾN", false), gbc);
        p.add(stepButton("2", "CHI TIẾT VÉ", true), gbc);
        p.add(stepButton("3", "THANH TOÁN", false), gbc);

        return p;
    }

    private JComponent stepButton(String index, String text, boolean active) {
        JButton b = new JButton(index + "  " + text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setEnabled(false); // chỉ hiển thị, không cho bấm
        if (active) {
            b.setBackground(BLUE_PRIMARY);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(new Color(0xEEEEEE));
            b.setForeground(Color.DARK_GRAY);
        }
        b.setBorder(new CompoundBorder(new LineBorder(new Color(210,210,210)), b.getBorder()));
        return b;
    }

    // ---------------- LEFT: Chọn vị trí ----------------
    private JComponent buildLeft() {
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(Color.WHITE);

        // Tiêu đề tuyến
        JLabel route = new JLabel("  Tuyến An Hòa -> Bảo Sơn, Ngày 14/06 16:47");
        route.setOpaque(true);
        route.setBackground(new Color(0xE8F0FE));
        route.setForeground(new Color(0x1565C0));
        route.setFont(route.getFont().deriveFont(Font.BOLD, 14f));
        route.setBorder(new CompoundBorder(new LineBorder(BLUE_BORDER),
                new EmptyBorder(10, 10, 10, 10)));
        left.add(route, BorderLayout.NORTH);

        // Trung tâm: tiêu đề CHỌN VỊ TRÍ + legend + danh sách toa
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
        legend.add(legendItem(SEAT_SOLD, "Ghế Đã Chọn"));
        legend.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)),
                new EmptyBorder(8, 8, 8, 8)));
        center.add(legend);

        // Container Toa (cuộn)
        JPanel toaContainer = new JPanel();
        toaContainer.setLayout(new BoxLayout(toaContainer, BoxLayout.Y_AXIS));
        toaContainer.setBackground(Color.WHITE);

        // Demo: các ghế đang chọn là 9 và 13 (giống ảnh)
        Set<Integer> selected = new HashSet<>();
        selected.add(9);
        selected.add(13);

        toaContainer.add(buildToaPanel("Toa số 1: Ngồi mềm điều hòa", 1, selected));
        toaContainer.add(Box.createVerticalStrut(10));
        toaContainer.add(buildToaPanel("Toa số 2: Ngồi mềm điều hòa", 49, new HashSet<Integer>()));

        JScrollPane sp = new JScrollPane(toaContainer);
        sp.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)),
                new EmptyBorder(0, 0, 0, 0)));
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

    private JComponent buildToaPanel(String title, int startSeatNumber, Set<Integer> selectedSeats) {
        JPanel wrapper = new JPanel(new BorderLayout(8, 8));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)),
                new EmptyBorder(8, 8, 8, 8)));

        JButton selectAll = new JButton("Chọn Tất Cả Ghế");
        selectAll.setFocusPainted(false);
        selectAll.setBackground(GREEN_SOFT);
        selectAll.setForeground(Color.WHITE);
        selectAll.setBorder(new EmptyBorder(6,12,6,12));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(selectAll, BorderLayout.WEST);

        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setFont(t.getFont().deriveFont(Font.BOLD, 14f));
        t.setForeground(new Color(0x455A64));
        top.add(t, BorderLayout.CENTER);

        wrapper.add(top, BorderLayout.NORTH);

        // Vùng khoang ghế
        JPanel car = new JPanel();
        car.setLayout(new GridLayout(2, 1, 0, 12)); // 2 hàng khoang (giống ảnh)
        car.setOpaque(false);

        // Mỗi hàng là 6 khoang
        car.add(buildRowKhoang("Khoang", startSeatNumber, selectedSeats));
        car.add(buildRowKhoang("Khoang", startSeatNumber + 48, new HashSet<Integer>())); // hàng 2 để minh họa tiếp

        wrapper.add(car, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildRowKhoang(String labelPrefix, int start, Set<Integer> selectedSeats) {
        JPanel row = new JPanel(new GridLayout(1, 6, 10, 0));
        row.setOpaque(false);

        for (int k = 0; k < 6; k++) {
            int khoangIndex = k + 1;
            JPanel khoang = new JPanel(new BorderLayout(4, 4));
            khoang.setBackground(Color.WHITE);
            khoang.setBorder(new CompoundBorder(
                    new LineBorder(new Color(210, 225, 245)),
                    new EmptyBorder(6, 6, 6, 6)
            ));

            JLabel title = new JLabel(labelPrefix + " " + khoangIndex);
            title.setForeground(new Color(0x607D8B));
            title.setFont(title.getFont().deriveFont(Font.PLAIN, 12f));
            khoang.add(title, BorderLayout.NORTH);

            // Ma trận 6 ghế (2 x 3)
            JPanel grid = new JPanel(new GridLayout(2, 3, 6, 6));
            grid.setOpaque(false);

            for (int i = 0; i < 6; i++) {
                int seatNumber = start + (k * 8) + seatOffset(i); // sắp theo thứ tự giống hình
                JButton seat = seatButton(seatNumber,
                        selectedSeats.contains(seatNumber),
                        false); // có thể set "sold" = true nếu muốn xám
                grid.add(seat);
            }

            khoang.add(grid, BorderLayout.CENTER);
            row.add(khoang);
        }
        return row;
    }

    // sắp ghế để tạo cảm giác đếm 1..48 theo cột trong ảnh
    private int seatOffset(int indexInKhoang) {
        // ánh xạ 6 ghế trong khoang vào chuỗi số hợp lý
        int[] order = {0, 1, 8, 9, 16, 17}; // mỗi khoang cách nhau 8 số
        return order[indexInKhoang];
    }

    private JButton seatButton(int number, boolean selected, boolean sold) {
        JButton b = new JButton(String.valueOf(number));
        b.setFocusPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
        b.setPreferredSize(new Dimension(40, 36));
        b.setBorder(new LineBorder(new Color(160, 190, 220), 2, true));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);

        if (sold) {
            b.setBackground(SEAT_SOLD);
            b.setForeground(Color.DARK_GRAY);
        } else if (selected) {
            b.setBackground(SEAT_SELECTED);
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(SEAT_FREE);
            b.setForeground(new Color(0x174A7A));
        }

        // chỉ minh họa; bạn có thể gắn ActionListener thực sự
        b.addActionListener(e -> {
            if (b.getBackground().equals(SEAT_SELECTED)) {
                b.setBackground(SEAT_FREE);
                b.setForeground(new Color(0x174A7A));
            } else if (b.getBackground().equals(SEAT_FREE)) {
                b.setBackground(SEAT_SELECTED);
                b.setForeground(Color.WHITE);
            }
        });
        return b;
    }

    // ---------------- RIGHT: Thông tin khách hàng ----------------
    private JComponent buildRight() {
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(Color.WHITE);

        right.add(infoHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(8, 8, 8, 8));

        body.add(customerFormRow());
        body.add(Box.createVerticalStrut(8));
        body.add(ticketDetailCard("Toa số: 1, Khoang: 2, Ghế: 9"));
        body.add(Box.createVerticalStrut(8));
        body.add(ticketDetailCard("Toa số: 1, Khoang: 2, Ghế: 13"));
        body.add(Box.createVerticalStrut(8));
        body.add(bottomButtons());

        JScrollPane sp = new JScrollPane(body);
        sp.setBorder(new LineBorder(new Color(230,230,230)));
        sp.getVerticalScrollBar().setUnitIncrement(18);

        right.add(sp, BorderLayout.CENTER);
        return right;
    }

    private JComponent infoHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BLUE_LIGHT);
        p.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0, BLUE_BORDER),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel icon = new JLabel("\u2139"); // ℹ
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
        g.add(new JTextField(), c);

        // SĐT
        c.gridx=0; c.gridy=1; c.weightx=0;
        g.add(new JLabel("Số Điện Thoại"), c);
        c.gridx=1; c.weightx=1;
        g.add(new JTextField(), c);

        // CCCD
        c.gridx=2; c.gridy=1; c.weightx=0;
        g.add(new JLabel("CCCD"), c);
        c.gridx=3; c.weightx=1;
        g.add(new JTextField(), c);

        // Căn cột
        c.gridx=2; c.gridy=0; c.weightx=0; g.add(new JLabel(""), c);
        c.gridx=3; c.gridy=0; c.weightx=1; g.add(Box.createHorizontalStrut(10), c);

        return g;
    }

    private JComponent ticketDetailCard(String titleText) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(new LineBorder(RED_SOFT),
                new EmptyBorder(8, 10, 10, 10)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        JLabel title = new JLabel("Chi Tiết Vé:  " + titleText);
        title.setForeground(RED_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13.5f));

        JButton quick = new JButton("Điền nhanh");
        quick.setFocusPainted(false);
        quick.setBackground(GREEN_SOFT);
        quick.setForeground(Color.WHITE);
        quick.setBorder(new EmptyBorder(4,10,4,10));

        c.gridx=0; c.gridy=0; c.gridwidth=3; c.weightx=1;
        card.add(title, c);
        c.gridx=3; c.gridy=0; c.gridwidth=1; c.weightx=0;
        card.add(quick, c);

        // Họ tên
        c.gridx=0; c.gridy=1; c.weightx=0;
        card.add(new JLabel("Họ Tên"), c);
        c.gridx=1; c.weightx=1;
        card.add(new JTextField(16), c);

        // Năm sinh
        c.gridx=2; c.gridy=1; c.weightx=0;
        card.add(new JLabel("Năm Sinh"), c);
        c.gridx=3; c.weightx=0.6;
        JComboBox<String> year = new JComboBox<>();
        for (int y=1950; y<=2025; y++) year.addItem(String.valueOf(y));
        year.setSelectedItem("1990");
        card.add(year, c);

        // CCCD
        c.gridx=0; c.gridy=2; c.weightx=0;
        card.add(new JLabel("CCCD"), c);
        c.gridx=1; c.weightx=1;
        card.add(new JTextField(16), c);

        // Loại vé
        c.gridx=2; c.gridy=2; c.weightx=0;
        card.add(new JLabel("Loại Vé"), c);
        c.gridx=3; c.weightx=0.6;
        JComboBox<String> type = new JComboBox<>(new String[]{
                "Vé dành cho học sinh, sinh viên",
                "Vé người lớn",
                "Vé trẻ em"
        });
        card.add(type, c);

        // Tiền vé
        c.gridx=0; c.gridy=3; c.weightx=0;
        JLabel priceLabel = new JLabel("Tiền Vé");
        priceLabel.setForeground(RED_PRIMARY);
        card.add(priceLabel, c);
        c.gridx=1; c.weightx=0.6;
        JTextField price = new JTextField("81060.0");
        price.setHorizontalAlignment(JTextField.RIGHT);
        card.add(price, c);

        return card;
    }

    private JComponent bottomButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        p.setBackground(Color.WHITE);

        JButton back = new JButton("Quay Lại");
        back.setBackground(new Color(0x64B5F6));
        back.setForeground(Color.WHITE);
        back.setFocusPainted(false);
        back.setBorder(new EmptyBorder(8, 20, 8, 20));

        JButton next = new JButton("Tiếp Tục");
        next.setBackground(GREEN_PRIMARY);
        next.setForeground(Color.WHITE);
        next.setFocusPainted(false);
        next.setBorder(new EmptyBorder(8, 20, 8, 20));

        p.add(back);
        p.add(next);
        return p;
    }

    // ---------------- main ----------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> new ManChonGheNgoi().setVisible(true));
    }
}
