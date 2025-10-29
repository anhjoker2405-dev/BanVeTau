package ui; // Hoặc package UI của bạn

// Import các lớp cần thiết
import dao.KhuyenMai_Dao; // Sửa tên này nếu file DAO của bạn khác
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
import java.util.List;

/**
 * TimKiemKhuyenMaiPanel
 * Giữ NGUYÊN CHỨC NĂNG (DAO, lọc bằng ô tìm kiếm, TableRowSorter),
 * CHỈ thay đổi STYLE để đồng bộ với TimKiemChuyenDiPanel
 * và bổ sung 2 nút: "Tìm khuyến mãi" (reload từ CSDL) & "Làm mới".
 */
public class TimKiemKhuyenMaiPanel extends JPanel {

    // ===== Style constants giống TimKiemChuyenDiPanel =====
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color ACCENT_DARKER = new Color(52, 103, 188);

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

        // Tổng thể đồng bộ với TimKiemChuyenDiPanel
        setOpaque(false);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header + Filter + Table đặt trong các "CardPanel" giống panel chuyến đi
        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(buildHeaderPanel(), BorderLayout.NORTH);
        north.add(buildFilterPanel(), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        // 3. Tải dữ liệu và Thêm sự kiện (GIỮ NGUYÊN + thêm click cho nút)
        loadDataToTable();
        addEvents();
    }

    // ===== Header giống chuyến đi =====
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

    // ===== Filter: label + textfield + 2 nút bên phải =====
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

        // Action buttons (canh phải như panel chuyến đi)
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
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho phép sửa trực tiếp trên bảng
            }
        };
        tblKetQua = new JTable(tableModel);
        tblKetQua.setRowHeight(28);

        // GIỮ: bộ lọc Sorter dựa trên model để applyFilter() hoạt động như cũ
        sorter = new TableRowSorter<>(tableModel);
        tblKetQua.setRowSorter(sorter);

        // ======= STYLE bảng giống TimKiemChuyenDiPanel =======
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
        // Reload từ CSDL rồi áp bộ lọc hiện tại
        loadDataToTable();
        applyFilter();
    }
    private void onResetClick(ActionEvent e) {
        txtSearch.setText("");
        sorter.setRowFilter(null);
        loadDataToTable();
        txtSearch.requestFocus();
    }

    // ================== STYLE HELPERS (copy tinh thần từ TimKiemChuyenDiPanel) ==================
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

    // CardPanel dùng lại phong cách nền trắng bo góc + viền như panel chuyến đi
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

    /**
     * Tải toàn bộ dữ liệu từ CSDL vào JTable
     */
    private void loadDataToTable() {
        tableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<KhuyenMai> ds = khuyenMaiDAO.getAllKhuyenMai();
        for (KhuyenMai km : ds) {
            Object[] row = {
                    km.getMaKhuyenMai(),
                    km.getTenKhuyenMai(),
                    km.getGiamGia(),
                    km.getNgayBatDau(),
                    km.getNgayKetThuc(),
                    km.getMoTa()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Thêm sự kiện cho ô tìm kiếm (gõ tới đâu lọc tới đó)
     */
    private void addEvents() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });
    }

    /**
     * Lọc dữ liệu JTable dựa trên nội dung ô tìm kiếm
     */
    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            // Nếu ô tìm kiếm rỗng, hiển thị tất cả
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }
}
