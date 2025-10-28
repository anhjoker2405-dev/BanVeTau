package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.ZoneId;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import com.toedter.calendar.JDateChooser;

public class SearchTripPanel extends JPanel {

    // Exposed form fields for integration
    private JComboBox<String> cbGaDi;
    private JComboBox<String> cbGaDen;
    private JRadioButton rbMotChieu;
    private JRadioButton rbKhuHoi;
    private JDateChooser dcNgayDi;
    private JDateChooser dcNgayVe;
    private JButton btnTimKiem;

    public SearchTripPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ===== Panel trái (40%) =====
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Tiêu đề khung
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 255)),
                        "THÔNG TIN HÀNH TRÌNH",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 16),
                        new Color(70, 130, 180)
                ),
                new EmptyBorder(20, 20, 20, 40)
        ));

        // ========== Ga đi ==========
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblGaDi = new JLabel("Ga Đi");
        styleLabel(lblGaDi);
        leftPanel.add(lblGaDi, gbc);

        gbc.gridx = 1;
        cbGaDi = new JComboBox<>(new String[]{"An Hòa", "Hà Nội", "Đà Nẵng", "Sài Gòn"});
        styleField(cbGaDi);
        cbGaDi.setPreferredSize(new Dimension(160, 32));
        leftPanel.add(cbGaDi, gbc);

        gbc.gridx = 2;
        JLabel iconTrain1 = createScaledIcon("/img/train.png", 24, 24);
        leftPanel.add(iconTrain1, gbc);

        // ========== Ga đến ==========
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblGaDen = new JLabel("Ga Đến");
        styleLabel(lblGaDen);
        leftPanel.add(lblGaDen, gbc);

        gbc.gridx = 1;
        cbGaDen = new JComboBox<>(new String[]{"An Hòa", "Hà Nội", "Đà Nẵng", "Sài Gòn"});
        styleField(cbGaDen);
        cbGaDen.setPreferredSize(new Dimension(160, 32));
        leftPanel.add(cbGaDen, gbc);

        gbc.gridx = 2;
        JLabel iconTrain2 = createScaledIcon("/img/train.png", 24, 24);
        leftPanel.add(iconTrain2, gbc);

        // ========== Loại vé ==========
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblLoaiVe = new JLabel("Loại Vé");
        styleLabel(lblLoaiVe);
        leftPanel.add(lblLoaiVe, gbc);

        gbc.gridx = 1;
        rbMotChieu = new JRadioButton("Một Chiều");
        rbKhuHoi    = new JRadioButton("Khứ Hồi");
        rbMotChieu.setFont(rbMotChieu.getFont().deriveFont(16f));
        rbKhuHoi.setFont(rbKhuHoi.getFont().deriveFont(16f));
        rbMotChieu.setBackground(Color.WHITE);
        rbKhuHoi.setBackground(Color.WHITE);
        rbMotChieu.setForeground(new Color(60, 120, 200));
        rbKhuHoi.setForeground(new Color(60, 120, 200));
        rbMotChieu.setSelected(true);
        ButtonGroup bgLoaiVe = new ButtonGroup();
        bgLoaiVe.add(rbMotChieu);
        bgLoaiVe.add(rbKhuHoi);

        JPanel loaiVePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        loaiVePanel.setBackground(Color.WHITE);
        loaiVePanel.add(rbMotChieu);
        loaiVePanel.add(rbKhuHoi);
        leftPanel.add(loaiVePanel, gbc);

        // ========== Ngày đi ==========
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblNgayDi = new JLabel("Ngày Đi");
        styleLabel(lblNgayDi);
        leftPanel.add(lblNgayDi, gbc);

        gbc.gridx = 1;
        dcNgayDi = new JDateChooser();
        dcNgayDi.setDateFormatString("yyyy-MM-dd");
        dcNgayDi.setDate(new java.util.Date());
        dcNgayDi.setPreferredSize(new Dimension(160, 32));
        leftPanel.add(dcNgayDi, gbc);

        // ========== Ngày về ==========
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblNgayVe = new JLabel("Ngày Về (Khứ hồi)");
        styleLabel(lblNgayVe);
        leftPanel.add(lblNgayVe, gbc);

        gbc.gridx = 1;
        dcNgayVe = new JDateChooser();
        dcNgayVe.setDateFormatString("yyyy-MM-dd");
        dcNgayVe.setDate(new java.util.Date());
        dcNgayVe.setEnabled(false);
        dcNgayVe.setPreferredSize(new Dimension(160, 32));
        leftPanel.add(dcNgayVe, gbc);

        // Bật/tắt ngày về khi chọn loại vé
        rbKhuHoi.addActionListener(e -> dcNgayVe.setEnabled(true));
        rbMotChieu.addActionListener(e -> dcNgayVe.setEnabled(false));

        // ========== Nút tìm kiếm ==========
        gbc.gridx = 1; gbc.gridy++;
        btnTimKiem = new JButton("Tìm Kiếm");
        stylePrimaryButton(btnTimKiem);
        btnTimKiem.setFont(btnTimKiem.getFont().deriveFont(Font.BOLD, 16f));
        btnTimKiem.setFocusable(false);
        btnTimKiem.setBackground(new Color(90, 160, 230));
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setFocusPainted(false);
        leftPanel.add(btnTimKiem, gbc);

        // ===== Panel phải (60%) =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new LineBorder(new Color(220, 220, 220))
        ));

        JLabel mapLabel = new JLabel("", JLabel.CENTER);
        rightPanel.add(mapLabel, BorderLayout.CENTER);

        // Load ảnh gốc (tự động scale)
        java.net.URL mapUrl = getClass().getResource("/img/map.jpg");
        if (mapUrl != null) {
            ImageIcon originalMap = new ImageIcon(mapUrl);
            Image originalImage = originalMap.getImage();
            rightPanel.addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) {
                    int w = rightPanel.getWidth(), h = rightPanel.getHeight();
                    if (w <= 0 || h <= 0) return;
                    double imgRatio = (double) originalImage.getWidth(null) / originalImage.getHeight(null);
                    double panelRatio = (double) w / h;
                    int newW = panelRatio > imgRatio ? (int) (h * imgRatio) : w;
                    int newH = panelRatio > imgRatio ? h : (int) (w / imgRatio);
                    Image scaled = originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    mapLabel.setIcon(new ImageIcon(scaled));
                }
            });
        }

        // ===== Split 40/60 =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0);
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override public void paint(Graphics g) { /* ẩn divider */ }
                };
            }
        });
        bindProportionalDivider(splitPane, 0.4);
        splitPane.setResizeWeight(0.0);

        add(splitPane, BorderLayout.CENTER);
    }

    /** Giữ divider theo tỉ lệ (0..1) khi resize */
    private static void bindProportionalDivider(JSplitPane sp, double proportion) {
        sp.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && sp.isShowing()) {
                SwingUtilities.invokeLater(() -> {
                    int total = sp.getWidth() - sp.getDividerSize();
                    sp.setDividerLocation((int) Math.round(total * proportion));
                });
            }
        });
        sp.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int total = sp.getWidth() - sp.getDividerSize();
                sp.setDividerLocation((int) Math.round(total * proportion));
            }
        });
    }

    /** Tạo icon với kích thước cụ thể */
    private JLabel createScaledIcon(String path, int width, int height) {
        ImageIcon icon = safeLoadImage(path, width, height);
        JLabel lbl = new JLabel();
        if (icon != null) lbl.setIcon(icon); else lbl.setText("🚆");
        return lbl;
    }

    /** Load ảnh an toàn */
    private ImageIcon safeLoadImage(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon raw = new ImageIcon(url);
            Image scaled = raw.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            System.err.println("Lỗi load ảnh " + path + ": " + ex.getMessage());
            return null;
        }
    }

    /** Tô màu cho nhãn tiêu đề */
    private void styleLabel(JLabel lbl) {
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(new Color(60, 120, 200));
    }
    /** Style chung cho input (combo, text, date…) */
    private void styleField(JComponent c) {
        c.setFont(c.getFont().deriveFont(16f));
        c.setForeground(new Color(35, 48, 74));
    }

    // ===== Public API =====
    public void setStations(java.util.List<String> gaDi, java.util.List<String> gaDen) {
        if (gaDi != null) { cbGaDi.removeAllItems(); for (String s : gaDi) cbGaDi.addItem(s); }
        if (gaDen != null) { cbGaDen.removeAllItems(); for (String s : gaDen) cbGaDen.addItem(s); }
    }
    public void setSelectedStations(String gaDi, String gaDen) {
        selectStation(cbGaDi, gaDi);
        selectStation(cbGaDen, gaDen);
    }
    public String getGaDi() { return cbGaDi.getSelectedItem() == null ? null : cbGaDi.getSelectedItem().toString(); }
    public String getGaDen() { return cbGaDen.getSelectedItem() == null ? null : cbGaDen.getSelectedItem().toString(); }
    public boolean isKhuHoi() { return rbKhuHoi.isSelected(); }

    public java.time.LocalDate getNgayDi() {
        java.util.Date d = dcNgayDi.getDate();
        if (d == null) return java.time.LocalDate.now();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
    public java.time.LocalDate getNgayVe() {
        java.util.Date d = dcNgayVe.getDate();
        if (d == null) return getNgayDi();
        return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
    
    public void setNgayDi(LocalDate ngay) {
        if (ngay == null) return;
        java.util.Date date = java.util.Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dcNgayDi.setDate(date);
    }

    public void setNgayVe(LocalDate ngay) {
        if (ngay == null) return;
        java.util.Date date = java.util.Date.from(ngay.atStartOfDay(ZoneId.systemDefault()).toInstant());
        dcNgayVe.setDate(date);
    }

    public void onSearch(java.awt.event.ActionListener l) { btnTimKiem.addActionListener(l); }

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
    
    private void selectStation(JComboBox<String> comboBox, String value) {
        if (value == null || comboBox == null) {
            return;
        }
        ComboBoxModel<String> model = comboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            String item = model.getElementAt(i);
            if (item != null && item.equalsIgnoreCase(value)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        comboBox.addItem(value);
        comboBox.setSelectedItem(value);
    }

    // Demo nhanh
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Bán Vé - Chọn Chuyến Đi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setLocationByPlatform(true);
            f.setContentPane(new SearchTripPanel());
            f.setVisible(true);
        });
    }
}
