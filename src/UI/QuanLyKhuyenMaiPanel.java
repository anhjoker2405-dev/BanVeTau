package ui;

import com.toedter.calendar.JDateChooser;
import dao.KhuyenMai_Dao;
import entity.KhuyenMai;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Quản Lý Khuyến Mãi: nối DAO (add/update/delete, load bảng), auto sinh mã nếu txtMa trống.
 * Giữ nguyên style, bổ sung validate + định dạng hiển thị.
 */
public class QuanLyKhuyenMaiPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Đặt TRUE nếu DB của bạn lưu giảm giá dạng 0..1; FALSE nếu lưu 0..100 */
    private static final boolean STORE_GIAM_GIA_AS_FRACTION = false;

    // ===== DAO =====
    private final KhuyenMai_Dao kmDao = new KhuyenMai_Dao();

    // ===== Inputs =====
    private final JTextField txtMa  = new JTextField();      // để trống sẽ auto "KM-###"
    private final JTextField txtTen = new JTextField();
    private final JSpinner   spGiam = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1)); // % 0..100 hiển thị
    private final JTextArea  txtMoTa = new JTextArea(3, 20);

    // Ngày bắt đầu / kết thúc
    private final DateTimePicker dtBatDau = new DateTimePicker();
    private final DateTimePicker dtKetThuc = new DateTimePicker();

    // ===== Buttons =====
    private final PrimaryButton btnThem    = new PrimaryButton("Thêm Khuyến Mãi");
    private final PrimaryButton btnXoa     = new PrimaryButton("Xóa Khuyến Mãi");
    private final PrimaryButton btnCapNhat = new PrimaryButton("Cập Nhật Khuyến Mãi");

    // ===== Table =====
    private final DefaultTableModel model = new DefaultTableModel(new String[]{
            "Mã KM","Tên khuyến mãi","Giảm (%)","Ngày bắt đầu","Ngày kết thúc","Mô tả"
    }, 0) { @Override public boolean isCellEditable(int r,int c){ return false; } };

    private final JTable table = new JTable(model);

    public QuanLyKhuyenMaiPanel() {
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

        styleInputs();
        styleButtons();
        initActions();

        add(buildFilterCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadTable(); // nạp danh sách ban đầu
    }

    private JPanel buildFilterCard() {
        CardPanel card = new CardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 12, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addRow(card, gbc, 0, new JLabel("Mã khuyến mãi :"), txtMa, 3);
        addRow(card, gbc, 1, new JLabel("Tên khuyến mãi :"), txtTen, 3);

        // Mô tả dùng textarea (scroll) + Giảm giá (%)
        JScrollPane spMoTa = new JScrollPane(txtMoTa);
        styleTextArea(spMoTa, txtMoTa);
        addPair(card, gbc, 2,
                new JLabel("Giảm giá (%) :"), spGiam,
                new JLabel("Mô tả :"), spMoTa);

        // Dùng DateTimePicker
        addPair(card, gbc, 3,
                new JLabel("Ngày bắt đầu :"), dtBatDau,
                new JLabel("Ngày kết thúc :"),   dtKetThuc);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints ab = new GridBagConstraints();
        ab.insets = new Insets(0, 8, 0, 8);
        actions.add(btnThem, ab); actions.add(btnXoa, ab);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0; gbc2.gridy = 4; gbc2.gridwidth = 3; gbc2.weightx = 1; gbc2.fill = GridBagConstraints.NONE; gbc2.anchor = GridBagConstraints.WEST;
        card.add(actions, gbc2);
        gbc2 = new GridBagConstraints();
        gbc2.gridx = 3; gbc2.gridy = 4; gbc2.gridwidth = 1; gbc2.weightx = 0; gbc2.anchor = GridBagConstraints.EAST;
        card.add(btnCapNhat, gbc2);

        // to hơn cho nhãn
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        for (Component c : card.getComponents()) if (c instanceof JLabel) c.setFont(labelFont);

        return card;
    }

    /** Viewport bảng: bo góc + padding + STYLE như ManQuanLiChuyenTau */
    private JPanel buildTableCard() {
        int[] widths = {110, 220, 90, 140, 140, 260};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        TableStyles.applyTimKiemStyle(table, sp);

        CardPanel card = new CardPanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Danh sách khuyến mãi");
        title.setFont(getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(45, 70, 120));
        card.add(title, BorderLayout.NORTH);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 12, 12, 12));
        inner.add(sp, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);

        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { table.doLayout(); }
        });

        // Chọn dòng → đổ form
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) fillFormFromRow(r);
            }
        });

        return card;
    }

    // ===== helpers =====
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
        for (JComponent c : new JComponent[]{txtMa, txtTen}) {
            styleInputComponent(c);
            c.setPreferredSize(new Dimension(260, h));
            c.setFont(inputFont);
        }
        txtMa.setPreferredSize(new Dimension(520, h));
        // txtMa.setEditable(false); // bật nếu muốn cấm sửa tay mã (vì auto)

        // Spinner %
        styleInputComponent(spGiam);
        spGiam.setPreferredSize(new Dimension(120, h));
        JComponent ed = ((JSpinner.DefaultEditor) spGiam.getEditor());
        ed.setBorder(null);
        ed.getComponent(0).setFont(inputFont);

        // DateTimePicker
        dtBatDau.applyInputStyle(h);
        dtKetThuc.applyInputStyle(h);
    }

    private void styleButtons() {
        for (JButton b : new JButton[]{btnThem, btnXoa, btnCapNhat}) {
            b.setPreferredSize(new Dimension(190, 32));
        }
    }

    private void styleTextArea(JScrollPane sp, JTextArea ta) {
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196,210,237), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
    }

    // ===== Actions: ĐÃ GẮN DAO =====
    private void initActions() {
        btnThem.addActionListener(e -> {
            try {
                KhuyenMai km = buildEntityFromForm(/*forUpdate*/ false);
                if (km == null) return; // đã thông báo lỗi
                boolean ok = kmDao.addKhuyenMai(km); // auto sinh mã nếu txtMa trống
                if (ok) {
                    txtMa.setText(km.getMaKhuyenMai()); // DAO đã set lại mã
                    JOptionPane.showMessageDialog(this, "Thêm thành công: " + km.getMaKhuyenMai(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadTableAndSelect(km.getMaKhuyenMai());
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm KHÔNG thành công.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi thêm KM: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnXoa.addActionListener(e -> {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập/Chọn 'Mã khuyến mãi' để xoá.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int cf = JOptionPane.showConfirmDialog(this, "Xoá KM: " + ma + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (cf != JOptionPane.YES_OPTION) return;

            try {
                boolean ok = kmDao.deleteKhuyenMai(ma);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Đã xoá: " + ma, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    clearForm();
                    loadTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy/không xoá được: " + ma, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi xoá KM: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCapNhat.addActionListener(e -> {
            try {
                KhuyenMai km = buildEntityFromForm(/*forUpdate*/ true);
                if (km == null) return;
                boolean ok = kmDao.updateKhuyenMai(km);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadTableAndSelect(km.getMaKhuyenMai());
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật KHÔNG thành công.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi cập nhật KM: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ====== Load/Fill ======
    private void loadTable() {
        model.setRowCount(0);
        List<KhuyenMai> ds = kmDao.getAllKhuyenMai();
        for (KhuyenMai km : ds) {
            model.addRow(new Object[]{
                    safe(km.getMaKhuyenMai()),
                    safe(km.getTenKhuyenMai()),
                    formatPercent(km.getGiamGia()),
                    formatDateTime(km.getNgayBatDau()),
                    formatDateTime(km.getNgayKetThuc()),
                    safe(km.getMoTa())
            });
        }
    }

    private void loadTableAndSelect(String ma) {
        loadTable();
        if (ma == null) return;
        // chọn dòng có mã vừa thao tác
        for (int i = 0; i < model.getRowCount(); i++) {
            if (ma.equals(model.getValueAt(i, 0))) {
                int viewRow = (table.getRowSorter() != null)
                        ? table.convertRowIndexToView(i)
                        : i;
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                fillFormFromRow(viewRow);
                break;
            }
        }
    }

    /** Chọn 1 dòng trên bảng → đổ dữ liệu ra form */
    private void fillFormFromRow(int viewRow) {
        if (viewRow < 0) return;

        // Nếu bảng có sorter/filter, chuyển view index -> model index
        int r = (table.getRowSorter() != null)
                ? table.convertRowIndexToModel(viewRow)
                : viewRow;

        String ma   = getCell(r, 0);
        String ten  = getCell(r, 1);
        String giam = getCell(r, 2); // "15%"
        String bd   = getCell(r, 3); // "dd/MM/yyyy HH:mm"
        String kt   = getCell(r, 4);
        String moTa = getCell(r, 5);

        txtMa.setText(ma);
        txtTen.setText(ten);

        try {
            double p = Double.parseDouble(giam.replace("%", "").trim());
            spGiam.setValue((int) Math.round(p));
        } catch (Exception ignore) {
            spGiam.setValue(0);
        }

        dtBatDau.setLocalDateTime(parseDate(bd));
        dtKetThuc.setLocalDateTime(parseDate(kt));
        txtMoTa.setText(moTa);
    }

    /** Lấy text ô (model) an toàn */
    private String getCell(int modelRow, int col) {
        Object v = model.getValueAt(modelRow, col);
        return v == null ? "" : v.toString();
    }

    // ===== Build entity & validate =====
    private KhuyenMai buildEntityFromForm(boolean forUpdate) {
        String ma  = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        int giamInt = (Integer) spGiam.getValue();
        LocalDateTime nbd = dtBatDau.getLocalDateTime();
        LocalDateTime nkt = dtKetThuc.getLocalDateTime();
        String moTa = txtMoTa.getText().trim();

        if (forUpdate && ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Thiếu 'Mã khuyến mãi' để cập nhật.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên khuyến mãi không được rỗng.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (giamInt < 0 || giamInt > 100) {
            JOptionPane.showMessageDialog(this, "Giảm giá phải trong khoảng 0..100%.", "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (nbd == null || nkt == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đầy đủ ngày bắt đầu/kết thúc.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (nkt.isBefore(nbd)) {
            JOptionPane.showMessageDialog(this, "Ngày kết thúc phải >= ngày bắt đầu.", "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        BigDecimal giamGia = BigDecimal.valueOf(giamInt);
        if (STORE_GIAM_GIA_AS_FRACTION) {
            giamGia = giamGia.divide(BigDecimal.valueOf(100)); // lưu dạng 0..1
        }

        KhuyenMai km = new KhuyenMai();
        km.setMaKhuyenMai(ma.isEmpty() ? null : ma);  // để null -> DAO tự sinh KM-###
        km.setTenKhuyenMai(ten);
        km.setGiamGia(giamGia);
        km.setNgayBatDau(nbd);
        km.setNgayKetThuc(nkt);
        km.setMoTa(moTa);
        return km;
    }

    private void clearForm() {
        txtMa.setText("");
        txtTen.setText("");
        spGiam.setValue(0);
        txtMoTa.setText("");
        dtBatDau.setLocalDateTime(null);
        dtKetThuc.setLocalDateTime(null);
    }

    // ===== Format/Parse helpers =====
    private static String safe(Object v){ return v==null? "": String.valueOf(v); }

    private static String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME_FMT);
    }
    private static LocalDateTime parseDate(String s) {
        try { return (s==null || s.isBlank()) ? null : LocalDateTime.parse(s, DATE_TIME_FMT); }
        catch (Exception e){ return null; }
    }

    private static String formatPercent(BigDecimal giamGia) {
        if (giamGia == null) return "";
        BigDecimal val = giamGia;
        // nếu DB lưu 0..1 thì hiển thị *100
        if (val.compareTo(BigDecimal.ZERO) >= 0 && val.compareTo(BigDecimal.ONE) < 0) {
            val = val.multiply(BigDecimal.valueOf(100));
        }
        val = val.stripTrailingZeros();
        if (val.scale() < 0) val = val.setScale(0);
        return val.toPlainString() + "%";
    }

    // ====== Vẽ nền gradient ======
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(242, 247, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    // ======= Các class style dùng chung =======

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

    // Primary blue button
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

    // TABLE STYLE (zebra, header, padding)
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
                setBorder(pad);
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
        private final JSpinner     timeSpinner = new JSpinner(new SpinnerDateModel()); // chọn giờ

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
        LocalDateTime getLocalDateTime() {
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

            return LocalDateTime.of(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH)+1,
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    cal.get(Calendar.SECOND)
            );
        }

        /** Set LocalDateTime (null => xóa) */
        void setLocalDateTime(LocalDateTime ldt) {
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

        /** Chuỗi dd/MM/yyyy HH:mm ("" nếu chưa chọn) */
        String getText() {
            LocalDateTime ldt = getLocalDateTime();
            if (ldt == null) return "";
            return ldt.format(DATE_TIME_FMT);
        }
    }

    // ===== Demo main (tuỳ chọn) =====
//    public static void main(String[] args) throws Exception {
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        JFrame f = new JFrame("Quản lý khuyến mãi");
//        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
//        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        f.setSize(1200, 700);
//        f.setLocationRelativeTo(null);
//        f.setContentPane(new QuanLyKhuyenMaiPanel());
//        f.setVisible(true);
//    }
}
