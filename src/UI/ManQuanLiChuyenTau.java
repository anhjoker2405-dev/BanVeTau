package ui;

import com.toedter.calendar.JDateChooser;
import dao.ChuyenTauDao;
import entity.ChuyenTauThongTin;
import entity.Ga;
import entity.Tau;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.Rectangle;
import java.util.Calendar;
import java.util.Date;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ManQuanLiChuyenTau extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== Inputs =====
    private final JTextField txtMa = new JTextField();

    // ComboBox KHÔNG có dữ liệu demo
//    private final JComboBox<String> cboGaDi    = new JComboBox<>();
//    private final JComboBox<String> cboGaDen   = new JComboBox<>();
//    private final JComboBox<String> cboTau     = new JComboBox<>();
//    private final JComboBox<String> cboLoaiGhe = new JComboBox<>();
    // ComboBox lấy dữ liệu từ database
    private final JComboBox<Ga>  cboGaDi  = new JComboBox<>();
    private final JComboBox<Ga>  cboGaDen = new JComboBox<>();
    private final JComboBox<Tau> cboTau   = new JComboBox<>();

    // JCalendar + Spinner thời gian
    private final DateTimePicker dtKH;   // Thời Gian Khởi Hành
    private final DateTimePicker dtDT;   // Thời Gian Dự Tính

    // ===== Buttons (primary blue) =====
    private final PrimaryButton btnThem    = new PrimaryButton("Thêm Chuyến Tàu");
    private final PrimaryButton btnXoa     = new PrimaryButton("Xóa Chuyến Tàu");
    private final PrimaryButton btnCapNhat = new PrimaryButton("Cập Nhật Chuyến Tàu");
    private final ChuyenTauDao chuyenTauDao = new ChuyenTauDao();

    // ===== Table =====
    // ĐÃ XOÁ “Số Ghế” và “Số Ghế Đã Đặt”
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Mã Chuyến Tàu","Ga Đi","Ga Đến","Thời Gian Đi",
            "Thời Gian Khởi Hành","Thời Gian Dự Tính","Tàu Di Chuyển",
            "Toa","Khoang","Loại Ghế","Số Ghế Còn Trống"
    }, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };

    private final JTable table = new JTable(model);

    public ManQuanLiChuyenTau() {
        // Nền gradient + layout
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // TẮT viền chấm focus
        UIManager.put("TextField.focusHighlight", Color.WHITE);
        UIManager.put("ComboBox.focus", new Color(0,0,0,0));
        UIManager.put("TextField.focus", new Color(0,0,0,0));
        UIManager.put("FormattedTextField.focus", new Color(0,0,0,0));
        UIManager.put("PasswordField.focus", new Color(0,0,0,0));
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());

        // Khởi tạo DateTimePicker (rỗng)
        dtKH = new DateTimePicker();
        dtDT = new DateTimePicker();

        styleInputs();
        styleButtons();
        
        initActions();
        loadComboData();
        prepareNextId();
        loadTableData();

        // Đảm bảo combo KHÔNG chọn gì (đều rỗng)
        cboGaDi.setSelectedIndex(-1);
        cboGaDen.setSelectedIndex(-1);
        cboTau.setSelectedIndex(-1);
//        cboLoaiGhe.setSelectedIndex(-1);

        add(buildFilterCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        // KHÔNG gọi mockData() => bảng trống
    }

    private JPanel buildFilterCard() {
        CardPanel card = new CardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 12, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(card, gbc, 0, new JLabel("Mã Chuyến Tàu :"), txtMa, 3);
        addPair(card, gbc, 1, new JLabel("Ga đi :"),  cboGaDi,  new JLabel("Ga đến :"), cboGaDen);
//        addPair(card, gbc, 2, new JLabel("Tàu Di Chuyển :"), cboTau, new JLabel("Loại Ghế :"), cboLoaiGhe);
//        addRow(card, gbc, 2, new JLabel("Tàu Di Chuyển :"), cboTau, 3);
        addPair(card, gbc, 2,
        new JLabel("Tàu Di Chuyển :"), cboTau,
        new JLabel(""), makeFiller()); // giữ layout 4 cột, bên phải là filler


        // Dùng DateTimePicker thay cho textfield
        addPair(card, gbc, 3,
                new JLabel("Thời Gian Khởi Hành :"), dtKH,
                new JLabel("Thời Gian Dự Tính :"),   dtDT);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints ab = new GridBagConstraints();
        ab.insets = new Insets(0, 8, 0, 8);
        actions.add(btnThem, ab); actions.add(btnXoa, ab);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; gbc.weightx = 1; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
        card.add(actions, gbc);
        gbc.gridx = 3; gbc.gridy = 4; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.EAST;
        card.add(btnCapNhat, gbc);

        // to hơn cho nhãn
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        for (Component c : card.getComponents()) if (c instanceof JLabel) c.setFont(labelFont);

        return card;
    }

    /** Viewport bảng: bo góc + padding + STYLE như TimKiemChuyenDiPanel */
    private JPanel buildTableCard() {
        // tỉ lệ cột ban đầu (đã giảm còn 11 cột)
        int[] widths = {120,110,120,100,140,140,150,70,80,110,120};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        TableStyles.applyTimKiemStyle(table, sp);

        CardPanel card = new CardPanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Danh sách chuyến tàu");
        title.setFont(getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(45, 70, 120));
        card.add(title, BorderLayout.NORTH);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 12, 12, 12));   // padding giữa viền bo góc và bảng
        inner.add(sp, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);

        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { table.doLayout(); }
        });

        return card;
    }

    // ===== helpers =====
        private JComponent makeFiller() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        // Cho phép giãn ra nhưng không thấy gì
        p.setMinimumSize(new Dimension(0, 0));
        p.setPreferredSize(new Dimension(0, 0));
        return p;
    }
    private void addRow(JPanel p, GridBagConstraints gbc, int row, JComponent label, JComponent field, int span) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; p.add(label, gbc);
        gbc.gridx = 1; gbc.gridwidth = span; gbc.weightx = 1; p.add(field, gbc);
    }
    private void addPair(JPanel p, GridBagConstraints gbc, int row, JComponent l1, JComponent c1, JComponent l2, JComponent c2) {
        gbc.gridy = row;
        gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; p.add(l1, gbc);
        gbc.gridx = 1; gbc.weightx = 1; p.add(c1, gbc);
        gbc.gridx = 2; gbc.weightx = 0; p.add(l2, gbc);
        gbc.gridx = 3; gbc.weightx = 1; p.add(c2, gbc);
    }

    // ===== Styles =====
    private void styleInputs() {
        int h = 28;
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);
        for (JComponent c : new JComponent[]{txtMa, cboGaDi, cboGaDen, cboTau/* , cboLoaiGhe */}) {
            styleInputComponent(c);
            c.setPreferredSize(new Dimension(260, h));
            c.setFont(inputFont);

            // Xoá viền chấm focus bên trong ComboBox
            if (c instanceof JComboBox<?> cb) stripComboFocus(cb);
        }
        txtMa.setPreferredSize(new Dimension(520, h));

        // Áp style cho DateTimePicker
        dtKH.applyInputStyle(h);
        dtDT.applyInputStyle(h);
    }

    private void styleButtons() {
        for (JButton b : new JButton[]{btnThem, btnXoa, btnCapNhat}) {
            b.setPreferredSize(new Dimension(160, 32)); // PrimaryButton tự vẽ nền xanh
        }
    }

    // ===== Utility: tắt focus border của JComboBox =====
    private static void stripComboFocus(JComboBox<?> cb) {
        cb.setUI(new BasicComboBoxUI() {
            @Override
            public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
                super.paintCurrentValue(g, bounds, false); // ép không vẽ viền chấm focus
            }
            @Override
            protected JButton createArrowButton() {
                JButton b = super.createArrowButton();
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                return b;
            }
        });
    }
//------------------------------------------------------------------------   
        private void initActions() {
        btnThem.addActionListener(e -> handleAdd());
        btnXoa.addActionListener(e -> handleDelete());
        btnCapNhat.addActionListener(e -> handleUpdate());
    }

    private void loadComboData() {
        try {
            DefaultComboBoxModel<Ga> gaDiModel = new DefaultComboBoxModel<>();
            DefaultComboBoxModel<Ga> gaDenModel = new DefaultComboBoxModel<>();
            List<Ga> gaList = chuyenTauDao.fetchGaOptions();
            for (Ga ga : gaList) {
                gaDiModel.addElement(ga);
                gaDenModel.addElement(ga);
            }
            cboGaDi.setModel(gaDiModel);
            cboGaDen.setModel(gaDenModel);

            DefaultComboBoxModel<Tau> tauModel = new DefaultComboBoxModel<>();
            List<Tau> tauList = chuyenTauDao.fetchTauOptions();
            for (Tau tau : tauList) {
                tauModel.addElement(tau);
            }
            cboTau.setModel(tauModel);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu ga/tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prepareNextId() {
        try {
            String nextId = chuyenTauDao.generateMaChuyenTau();
            txtMa.setText(nextId);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể sinh mã chuyến tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
        private void loadTableData() {
        try {
            List<ChuyenTauThongTin> danhSach = chuyenTauDao.fetchDanhSachChuyenTau();
            model.setRowCount(0);
            for (ChuyenTauThongTin info : danhSach) {
                model.addRow(new Object[]{
                        info.getMaChuyenTau(),
                        info.getGaDi(),
                        info.getGaDen(),
                        formatDuration(info.getThoiGianDi()),
                        formatDateTime(info.getThoiGianKhoiHanh()),
                        formatDateTime(info.getThoiGianDuTinh()),
                        info.getTenTau(),
                        safe(info.getSoToa()),
                        safe(info.getTenKhoang()),
                        safe(info.getTenLoaiGhe()),
                        info.getSoGheTrong()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách chuyến tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    public void reloadData() {
        SwingUtilities.invokeLater(this::loadTableData);
    }

    private void handleAdd() {
        try {
            Ga gaDi = (Ga) cboGaDi.getSelectedItem();
            Ga gaDen = (Ga) cboGaDen.getSelectedItem();
            Tau tau = (Tau) cboTau.getSelectedItem();

            if (gaDi == null) throw new IllegalArgumentException("Vui lòng chọn ga đi.");
            if (gaDen == null) throw new IllegalArgumentException("Vui lòng chọn ga đến.");
            if (tau == null) throw new IllegalArgumentException("Vui lòng chọn tàu di chuyển.");

            LocalDateTime tgKhoiHanh = dtKH.getLocalDateTime();
            LocalDateTime tgKetThuc = dtDT.getLocalDateTime();
            validateThoiGian(tgKhoiHanh, tgKetThuc);

            String maMoi = txtMa.getText().trim();
            if (maMoi.isEmpty()) {
                maMoi = chuyenTauDao.generateMaChuyenTau();
            }
            chuyenTauDao.createChuyenTau(maMoi, gaDi.getMaGa(), gaDen.getMaGa(), tau.getMaTau(), tgKhoiHanh, tgKetThuc);

            JOptionPane.showMessageDialog(this, "Đã thêm chuyến tàu " + maMoi + " thành công.");
            clearForm();
            prepareNextId();
            loadTableData();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể thêm chuyến tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã chuyến tàu cần xóa.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "Bạn chắc chắn muốn xóa chuyến tàu " + ma + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (option != JOptionPane.YES_OPTION) return;

        try {
            int deleted = chuyenTauDao.deleteById(ma);
            if (deleted > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa chuyến tàu " + ma + ".");
                clearForm();
                prepareNextId();
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu " + ma + ".",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể xóa chuyến tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        String ma = txtMa.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã chuyến tàu cần cập nhật.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Ga gaDi = (Ga) cboGaDi.getSelectedItem();
            Ga gaDen = (Ga) cboGaDen.getSelectedItem();
            Tau tau = (Tau) cboTau.getSelectedItem();

            if (gaDi == null) throw new IllegalArgumentException("Vui lòng chọn ga đi.");
            if (gaDen == null) throw new IllegalArgumentException("Vui lòng chọn ga đến.");
            if (tau == null) throw new IllegalArgumentException("Vui lòng chọn tàu di chuyển.");

            LocalDateTime tgKhoiHanh = dtKH.getLocalDateTime();
            LocalDateTime tgKetThuc = dtDT.getLocalDateTime();
            validateThoiGian(tgKhoiHanh, tgKetThuc);

            int updated = chuyenTauDao.update(ma, gaDi.getMaGa(), gaDen.getMaGa(), tau.getMaTau(), tgKhoiHanh, tgKetThuc);
            if (updated > 0) {
                JOptionPane.showMessageDialog(this, "Đã cập nhật chuyến tàu " + ma + " thành công.");
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chuyến tàu " + ma + ".",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật chuyến tàu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validateThoiGian(LocalDateTime khoiHanh, LocalDateTime ketThuc) {
        if (khoiHanh == null) throw new IllegalArgumentException("Vui lòng chọn thời gian khởi hành.");
        if (ketThuc == null) throw new IllegalArgumentException("Vui lòng chọn thời gian dự tính.");
        if (!ketThuc.isAfter(khoiHanh)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian khởi hành.");
        }
    }

    private void clearForm() {
        cboGaDi.setSelectedIndex(-1);
        cboGaDen.setSelectedIndex(-1);
        cboTau.setSelectedIndex(-1);
        dtKH.setLocalDateTime(null);
        dtDT.setLocalDateTime(null);
    }
    
        private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FMT);
    }

    private String formatDuration(Integer minutes) {
        if (minutes == null) return "";
        if (minutes < 60) {
            return minutes + " phút";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return hours + " giờ";
        }
        return hours + " giờ " + mins + " phút";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

//    private static class ComboItem {
//        private final String id;
//        private final String label;
//
//        ComboItem(String id, String label) {
//            this.id = id;
//            this.label = label;
//        }
//
//        String id() { return id; }
//
//        @Override
//        public String toString() {
//            return label;
//        }
//    }
    
//--------------------------------------------------------
    // Nền gradient
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(242, 247, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    // Card bo góc
    private static class CardPanel extends JPanel {
        CardPanel(LayoutManager layout) { super(layout); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,235));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),24,24);
            g2.setColor(new Color(215,225,245));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,24,24);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ===== Primary blue button (luôn xanh cả khi disabled) =====
    private static class PrimaryButton extends JButton {
        private final Color base     = new Color(60, 120, 200);
        private final Color hover    = new Color(73, 137, 221);
        private final Color pressed  = new Color(48, 98, 165);
        private final Color disabled = new Color(60, 120, 200, 140);

        PrimaryButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(8, 18, 8, 18));
            addChangeListener(e -> repaint());
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color bg;
            if (!isEnabled())                 bg = disabled;
            else if (getModel().isPressed())  bg = pressed;
            else if (getModel().isRollover()) bg = hover;
            else                               bg = base;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w, h, 10, 10);
            g2.dispose();

            // chữ trắng
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getAscent();
            int x = (w - textWidth) / 2;
            int y = (h + textHeight) / 2 - 2;
            g.drawString(getText(), x, y);
        }
    }

    // ========= TABLE STYLE (nhái TimKiemChuyenDiPanel) =========
    private static class TableStyles {
        static void applyTimKiemStyle(JTable table, JScrollPane scrollPane) {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            table.setFillsViewportHeight(true);
            table.setRowHeight(28);
            table.setShowGrid(true);
            table.setGridColor(new Color(223, 230, 243));
            table.setIntercellSpacing(new Dimension(0, 1));
            table.setBorder(BorderFactory.createEmptyBorder());

            table.setFont(table.getFont().deriveFont(Font.PLAIN, 14f));
            table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));

            JTableHeader header = table.getTableHeader();
            header.setReorderingAllowed(false);
            header.setDefaultRenderer(new HeaderRenderer(table));

            DefaultTableCellRenderer cell = new ZebraCellRenderer();
            int cols = table.getColumnModel().getColumnCount();
            for (int c = 0; c < cols; c++)
                table.getColumnModel().getColumn(c).setCellRenderer(cell);

            table.setSelectionBackground(new Color(209, 227, 255));
            table.setSelectionForeground(new Color(35, 48, 74));

            if (scrollPane != null) {
                scrollPane.getViewport().setOpaque(false);
                scrollPane.setOpaque(false);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
            }
        }
        private static class HeaderRenderer extends DefaultTableCellRenderer {
            HeaderRenderer(JTable table) {
                setHorizontalAlignment(LEFT);
                setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
                setBorder(new EmptyBorder(6, 10, 6, 10));
            }
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(new Color(236, 242, 252));
                setForeground(new Color(45, 70, 120));
                setOpaque(true);
                return this;
            }
        }
        private static class ZebraCellRenderer extends DefaultTableCellRenderer {
            private final Color even = new Color(250, 252, 255);
            private final Color odd  = new Color(244, 248, 255);
            private final Border pad = new EmptyBorder(6, 10, 6, 10);
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground((row % 2 == 0) ? even : odd);
                    setForeground(new Color(35, 48, 74));
                }
                setBorder(pad); // padding + xoá viền focus
                setOpaque(true);
                return this;
            }
        }
    }

    private static void styleInputComponent(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196,210,237), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        c.setBackground(Color.WHITE);
        c.setForeground(new Color(35,48,74));
    }

    // ====== DateTimePicker dùng JCalendar + Spinner giờ ======
    /** Bộ chọn Ngày + Giờ dùng JCalendar + JSpinner, bo góc & padding như input */
    private static class DateTimePicker extends JPanel {
        private final JDateChooser dateChooser = new JDateChooser();      // chọn ngày
        private final JSpinner     timeSpinner = new JSpinner(             // chọn giờ
                new SpinnerDateModel());

        DateTimePicker() {
            super(new BorderLayout(6, 0));
            setOpaque(false);

            // Đặt định dạng hiển thị ngày: dd/MM/yyyy
            dateChooser.setDateFormatString("dd/MM/yyyy");

            // Spinner hiển thị HH:mm
            JSpinner.DateEditor ed = new JSpinner.DateEditor(timeSpinner, "HH:mm");
            timeSpinner.setEditor(ed);
            timeSpinner.setValue(new Date()); // có thể bỏ nếu muốn trống giờ mặc định

            add(dateChooser, BorderLayout.CENTER);
            add(timeSpinner, BorderLayout.EAST);
        }

        /** Áp style giống input: border bo góc + padding + chiều cao */
        void applyInputStyle(int height) {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(196,210,237), 1, true),
                    new EmptyBorder(2, 6, 2, 6)
            ));
            setPreferredSize(new Dimension(260, height));

            dateChooser.setBorder(null);
            timeSpinner.setBorder(null);
            dateChooser.setOpaque(false);
            timeSpinner.setOpaque(false);

            // Font đồng nhất
            Font f = new Font("Segoe UI", Font.PLAIN, 13);
            dateChooser.setFont(f);
            ((JSpinner.DefaultEditor) timeSpinner.getEditor()).getTextField().setFont(f);
        }

        /** Lấy LocalDateTime (null nếu chưa chọn ngày) */
        java.time.LocalDateTime getLocalDateTime() {
            Date d = dateChooser.getDate();
            if (d == null) return null;
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);

            Date t = (Date) timeSpinner.getValue();
            Calendar c2 = Calendar.getInstance();
            c2.setTime(t);

            cal.set(Calendar.HOUR_OF_DAY, c2.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE,     c2.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND,     c2.get(Calendar.SECOND));
            cal.set(Calendar.MILLISECOND,0);

            return java.time.LocalDateTime.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH)+1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND)
            );
        }

        /** Set LocalDateTime (null => xóa) */
        void setLocalDateTime(java.time.LocalDateTime ldt) {
            if (ldt == null) {
                dateChooser.setDate(null);
                timeSpinner.setValue(new Date());
                return;
            }
            Calendar cal = Calendar.getInstance();
            cal.set(ldt.getYear(), ldt.getMonthValue()-1, ldt.getDayOfMonth(),
                    ldt.getHour(), ldt.getMinute(), ldt.getSecond());
            dateChooser.setDate(cal.getTime());
            timeSpinner.setValue(cal.getTime());
        }

        /** Lấy chuỗi “dd-MM-yyyy HH:mm:ss” ("" nếu chưa chọn) */
        String getText() {
            java.time.LocalDateTime ldt = getLocalDateTime();
            if (ldt == null) return "";
            return ldt.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        }
    }

    // Demo
//    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        JFrame f = new JFrame("Quản lí chuyến tàu");
//        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.setSize(1700, 760);
//        f.setLocationRelativeTo(null);
//        f.setContentPane(new ManQuanLiChuyenTau());
//        f.setVisible(true);
//    }
}