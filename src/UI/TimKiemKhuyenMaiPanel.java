package ui;

import dao.KhuyenMai_Dao;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * TimKiemKhuyenMaiPanel
 * GIỮ NGUYÊN CHỨC NĂNG (DAO, lọc bằng ô tìm kiếm, TableRowSorter),
 * CHỈ thay đổi STYLE + hiển thị format + comparator sort.
 */
public class TimKiemKhuyenMaiPanel extends JPanel {

    // ===== Style constants giống TimKiemChuyenDiPanel =====
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color ACCENT_DARKER = new Color(52, 103, 188);

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== Fields giữ nguyên chức năng =====
    private JTable tblKetQua;
    private DefaultTableModel tableModel;
    private KhuyenMai_Dao khuyenMaiDAO;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;
    private JButton btnTim;
    private JButton btnLamMoi;

    public TimKiemKhuyenMaiPanel() {
        khuyenMaiDAO = new KhuyenMai_Dao();

        setOpaque(false);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(buildHeaderPanel(), BorderLayout.NORTH);
        north.add(buildFilterPanel(), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadDataToTable();
        addEvents();
    }

    // ===== Header =====
    private JPanel buildHeaderPanel() {
        CardPanel header = new CardPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Tìm kiếm khuyến mãi");
        title.setFont(TITLE_FONT.deriveFont(20f));
        title.setForeground(new Color(33, 56, 110));

        JLabel subtitle = new JLabel("Lọc nhanh chương trình giảm giá đang áp dụng");
        subtitle.setForeground(new Color(80, 102, 145));
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(title);
        texts.add(subtitle);

        header.add(texts, BorderLayout.CENTER);
        return header;
    }

    // ===== Filter =====
    private JPanel buildFilterPanel() {
        CardPanel panel = new CardPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lb = new JLabel("Nhập (Mã KM, Tên KM, Mô tả):");
        txtSearch = new JTextField();
        styleInputComponent(txtSearch);

        // label
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(lb, gbc);
        // field
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        panel.add(txtSearch, gbc);

        // Action buttons (canh phải)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        btnLamMoi = new JButton("Làm mới");
        btnTim    = new JButton("Tìm khuyến mãi");
        styleButtonSecondary(btnLamMoi);
        styleButtonPrimary(btnTim);
        actions.add(btnLamMoi);
        actions.add(btnTim);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(actions, gbc);

        // Events cho 2 nút
        btnTim.addActionListener(this::onSearchClick);
        btnLamMoi.addActionListener(this::onResetClick);

        // Phím tắt: Enter tìm, Esc làm mới, Ctrl+L focus ô tìm
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onSearchClick(null);
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) onResetClick(null);
                else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_L) txtSearch.requestFocus();
            }
        });

        return panel;
    }

    private JPanel buildTablePanel() {
        CardPanel panel = new CardPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Kết quả tra cứu");
        title.setFont(SECTION_FONT);
        title.setForeground(new Color(45, 70, 120));

        String[] columnNames = {"Mã KM", "Tên KM", "Giảm Giá", "Ngày Bắt Đầu", "Ngày Kết Thúc", "Mô Tả"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tblKetQua = new JTable(tableModel);
        tblKetQua.setRowHeight(28);

        sorter = new TableRowSorter<>(tableModel);
        tblKetQua.setRowSorter(sorter);
        configureSorters(); // sắp xếp “đúng số” & “đúng ngày”

        styleTable(tblKetQua);

        JScrollPane scrollPane = new JScrollPane(tblKetQua);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ================== EVENTS ==================
    private void onSearchClick(ActionEvent e) {
        loadDataToTable(); // reload từ DB
        applyFilter();     // áp bộ lọc hiện tại
    }
    private void onResetClick(ActionEvent e) {
        txtSearch.setText("");
        sorter.setRowFilter(null);
        loadDataToTable();
        txtSearch.requestFocus();
    }

    // ================== STYLE HELPERS ==================
    private static void styleTable(JTable table) {
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(91, 137, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(Color.WHITE);
        table.setForeground(new Color(35, 48, 74));
        table.setRowSelectionAllowed(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(new Color(227, 235, 255));
        header.setForeground(new Color(54, 76, 125));
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        if (header.getDefaultRenderer() instanceof DefaultTableCellRenderer) {
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
            renderer.setHorizontalAlignment(SwingConstants.LEFT);
            renderer.setBorder(new EmptyBorder(10, 12, 10, 12));
        }
    }

    private static void styleInputComponent(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 210, 237), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        component.setBackground(Color.WHITE);
        component.setForeground(new Color(35, 48, 74));
    }

    private static void styleButtonPrimary(JButton button) {
        styleButton(button, ACCENT_COLOR, ACCENT_DARKER, Color.WHITE);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 16f));
    }
    private static void styleButtonSecondary(JButton button) {
        styleButton(button, new Color(226, 232, 247), new Color(201, 210, 233), new Color(45, 70, 120));
    }
    private static void styleButton(JButton button, Color background, Color hover, Color foreground) {
        button.setForeground(foreground);
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        button.getModel().addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed())      button.setBackground(hover.darker());
            else if (m.isRollover())button.setBackground(hover);
            else                     button.setBackground(background);
        });
    }

    // CardPanel nền trắng bo góc
    private static class CardPanel extends JPanel {
        CardPanel(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 235));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.setColor(new Color(215, 225, 245));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ================== GIỮ NGUYÊN CHỨC NĂNG ==================

    /** Tải toàn bộ dữ liệu từ CSDL vào JTable */
    private void loadDataToTable() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai();
        for (KhuyenMai km : ds) {
            Object[] row = {
                    nullToBlank(km.getMaKhuyenMai()),
                    nullToBlank(km.getTenKhuyenMai()),
                    formatPercent(km.getGiamGia()),              // "15%"
                    formatDateTime(km.getNgayBatDau()),          // "dd/MM/yyyy HH:mm"
                    formatDateTime(km.getNgayKetThuc()),
                    nullToBlank(km.getMoTa())
            };
            tableModel.addRow(row);
        }
    }

    /** Thêm sự kiện cho ô tìm kiếm (gõ tới đâu lọc tới đó) */
    private void addEvents() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter(); }
        });
    }

    /** Lọc dữ liệu JTable dựa trên nội dung ô tìm kiếm */
    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            // Escape regex để không lỗi khi nhập ký tự đặc biệt
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword)));
        }
    }

    /** Sắp xếp đúng kiểu dữ liệu cho từng cột */
    private void configureSorters() {
        // Cột 0: Mã KM (KM-###) -> so sánh theo số
        sorter.setComparator(0, Comparator.comparingInt(TimKiemKhuyenMaiPanel::extractKmNumber));

        // Cột 2: Giảm Giá "15%" -> so sánh theo double
        sorter.setComparator(2, Comparator.comparingDouble(TimKiemKhuyenMaiPanel::parsePercent));

        // Cột 3,4: Ngày -> so sánh theo thời gian
        Comparator<String> dateCmp = Comparator.comparing(TimKiemKhuyenMaiPanel::parseDateSafe);
        sorter.setComparator(3, dateCmp);
        sorter.setComparator(4, dateCmp);
    }

    // ================== Format & Parse helpers ==================
    private static String nullToBlank(Object v) { return v == null ? "" : String.valueOf(v); }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FMT);
    }

    private static String formatPercent(BigDecimal giamGia) {
        if (giamGia == null) return "";
        BigDecimal val = giamGia;
        // nếu lưu 0..1 thì nhân 100
        if (val.compareTo(BigDecimal.ZERO) >= 0 && val.compareTo(BigDecimal.ONE) < 0) {
            val = val.multiply(BigDecimal.valueOf(100));
        }
        val = val.stripTrailingZeros();
        if (val.scale() < 0) val = val.setScale(0);
        return val.toPlainString() + "%";
    }

    private static int extractKmNumber(String ma) {
        if (ma == null) return -1;
        // Hỗ trợ cả "KM-001" lẫn "KM001"
        String digits = ma.replaceFirst("^KM-?", "");
        try { return Integer.parseInt(digits); }
        catch (Exception e) { return -1; }
    }

    private static double parsePercent(String s) {
        if (s == null || s.isBlank()) return -1;
        try { return Double.parseDouble(s.replace("%", "").trim()); }
        catch (Exception e) { return -1; }
    }

    private static long parseDateSafe(String s) {
        try {
            if (s == null || s.isBlank()) return Long.MIN_VALUE;
            return LocalDateTime.parse(s, DATE_TIME_FMT).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return Long.MIN_VALUE;
        }
    }
}
