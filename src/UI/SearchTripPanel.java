package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import com.toedter.calendar.JDateChooser;


public class SearchTripPanel extends JPanel {

    // === Exposed fields ===
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
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 220, 255)),
                "THÔNG TIN HÀNH TRÌNH",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 18),
                new Color(70, 130, 180)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ========== Ga đi ==========
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblGaDi = new JLabel("Ga Đi");
        styleLabel(lblGaDi);
        leftPanel.add(lblGaDi, gbc);

        gbc.gridx = 1;
        cbGaDi = new JComboBox<>();
        cbGaDi.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbGaDi.setPreferredSize(new Dimension(160, 34));
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
        cbGaDen = new JComboBox<>();
        cbGaDen.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cbGaDen.setPreferredSize(new Dimension(160, 34));
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
        rbKhuHoi = new JRadioButton("Khứ Hồi");
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
        dcNgayDi.setDate(java.sql.Date.valueOf(LocalDate.now()));
        dcNgayDi.setDateFormatString("yyyy-MM-dd");
        dcNgayDi.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dcNgayDi.setPreferredSize(new Dimension(160, 34));
        leftPanel.add(dcNgayDi, gbc);

        // ========== Ngày về ==========
        gbc.gridx = 0; gbc.gridy++;
        JLabel lblNgayVe = new JLabel("Ngày Về (Khứ hồi)");
        styleLabel(lblNgayVe);
        leftPanel.add(lblNgayVe, gbc);

        gbc.gridx = 1;
        dcNgayVe = new JDateChooser();
        dcNgayVe.setDate(java.sql.Date.valueOf(LocalDate.now()));
        dcNgayVe.setDateFormatString("yyyy-MM-dd");
        dcNgayVe.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dcNgayVe.setPreferredSize(new Dimension(160, 34));
        dcNgayVe.setEnabled(false);
        leftPanel.add(dcNgayVe, gbc);

        // Bật/tắt ngày về khi chọn loại vé
        rbKhuHoi.addActionListener(e -> dcNgayVe.setEnabled(true));
        rbMotChieu.addActionListener(e -> dcNgayVe.setEnabled(false));

        // ========== Nút tìm kiếm ==========
        gbc.gridx = 1; gbc.gridy++;
        btnTimKiem = new JButton("Tìm Kiếm");
        btnTimKiem.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnTimKiem.setPreferredSize(new Dimension(160, 36));
        btnTimKiem.setBackground(new Color(90, 160, 230));
        btnTimKiem.setForeground(Color.WHITE);
        btnTimKiem.setFocusPainted(false);
        leftPanel.add(btnTimKiem, gbc);

        // ===== Panel phải (60%) =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new LineBorder(new Color(220, 220, 220)));

        JLabel mapLabel = new JLabel("", JLabel.CENTER);
        rightPanel.add(mapLabel, BorderLayout.CENTER);

        // Load ảnh gốc (tự động scale)
        java.net.URL mapUrl = getClass().getResource("/img/map.jpg");

        if (mapUrl != null) {
            ImageIcon originalMap = new ImageIcon(mapUrl);
            Image originalImage = originalMap.getImage();

            rightPanel.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int w = rightPanel.getWidth();
                    int h = rightPanel.getHeight();
                    if (w <= 0 || h <= 0) return;

                    double imgRatio = (double) originalImage.getWidth(null) / originalImage.getHeight(null);
                    double panelRatio = (double) w / h;

                    int newW = w;
                    int newH = h;
                    if (panelRatio > imgRatio) newW = (int) (h * imgRatio);
                    else newH = (int) (w / imgRatio);

                    Image scaled = originalImage.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                    mapLabel.setIcon(new ImageIcon(scaled));
                }
            });
        }

        // ===== Split theo tỷ lệ 40% - 60% =====
        
        // Padding cho panel trái (tạo khoảng cách giữa 2 panel)
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(200, 220, 255)),
                        "THÔNG TIN HÀNH TRÌNH",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 18),
                        new Color(70, 130, 180)
                ),
                new EmptyBorder(20, 20, 20, 40) // trái, trên, phải, dưới → cách map 40px
        ));

        // Padding nhẹ cho panel phải để không dính sát mép khung
        rightPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new LineBorder(new Color(220, 220, 220))
        ));

        // Tạo SplitPane 40/60
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(0); // ẩn hoàn toàn đường chia

        // Loại bỏ divider mặc định (ẩn đường đen)
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        // Không vẽ divider => không còn đường đen
                    }
                };
            }
        });

        // Giữ tỉ lệ 40/60 khi resize
        bindProportionalDivider(splitPane, 0.4);
        splitPane.setResizeWeight(0.0);
        
        // Giữ chiều cao 2 panel bằng nhau
        splitPane.setResizeWeight(0.0);
        splitPane.setOneTouchExpandable(false);

        // Thêm vào khung chính
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
        if (icon != null)
            lbl.setIcon(icon);
        else
            lbl.setText("🚆");
        return lbl;
    }

    /** Load ảnh an toàn (tránh NullPointerException) */
    private ImageIcon safeLoadImage(String path, int width, int height) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("️Không tìm thấy ảnh: " + path);
                return null;
            }
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

    // ======== MAIN TEST ========
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Bán Vé - Chọn Chuyến Đi");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // mở vừa màn hình ngay khi khởi động
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);   // ⬅️ quan trọng
            f.setLocationByPlatform(true);               // để hệ điều hành chọn vị trí hợp lý
            f.setContentPane(new SearchTripPanel());
            f.setVisible(true);
        });
    }

    // ===== Public API for integration with BanVe =====
    public void setStations(List<String> gaDiList, List<String> gaDenList) {
        DefaultComboBoxModel<String> m1 = new DefaultComboBoxModel<>();
        if (gaDiList != null) for (String s : gaDiList) m1.addElement(s);
        cbGaDi.setModel(m1);

        DefaultComboBoxModel<String> m2 = new DefaultComboBoxModel<>();
        if (gaDenList != null) for (String s : gaDenList) m2.addElement(s);
        cbGaDen.setModel(m2);
    }

    public String getGaDi() { Object v = cbGaDi.getSelectedItem(); return v==null? null : v.toString(); }
    public String getGaDen() { Object v = cbGaDen.getSelectedItem(); return v==null? null : v.toString(); }
    public LocalDate getNgayDi() {
        try {
            java.util.Date d = dcNgayDi.getDate();
            return d != null ? d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate() : LocalDate.now();
        } catch (Exception e) { return LocalDate.now(); }
    }
    public void onSearch(java.awt.event.ActionListener al) { btnTimKiem.addActionListener(al); }

}
