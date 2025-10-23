package ui;

import dao.ChuyenDi_Dao;
import entity.ChuyenTau;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimKiemChuyenDiPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACCENT_COLOR = new Color(66, 133, 244);
    private static final Color ACCENT_DARKER = new Color(52, 103, 188);

    private final ChuyenDi_Dao dao = new ChuyenDi_Dao();
    private final JTextField txtMaChuyenDi = new JTextField();
    private final JComboBox<String> cboGaDi = new JComboBox<>();
    private final JComboBox<String> cboGaDen = new JComboBox<>();

    private final JCheckBox chkKhoiHanhTu = new JCheckBox("Từ");
    private final JDateChooser dcKhoiHanhTu = makeDateChooser();
    private final JSpinner spTimeTu = makeTimeSpinner();

    private final JButton btnTim = new JButton("Tìm chuyến đi"); // (đừng setFont ở đây)
    private final JButton btnLamMoi = new JButton("Làm mới");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{ "Mã chuyến đi", "Ga đi", "Ga đến", "Thời gian khởi hành", "Thời gian đến dự kiến", "Tên tàu", "Số ghế Trống" }, 0
    ){
        @Override public boolean isCellEditable(int r, int c){ return false; }
    };
    private final JTable tblKetQua = new JTable(tableModel);

    public TimKiemChuyenDiPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(24, 24, 24, 24));

        styleInputs();
        styleTable();

        JPanel north = new JPanel(new BorderLayout(0, 16));
        north.setOpaque(false);
        north.add(buildHeaderPanel(), BorderLayout.NORTH);
        north.add(buildFilterPanel(), BorderLayout.CENTER);

        add(north, BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadStations();
        performSearch();
    }

    private JPanel buildHeaderPanel() {
        CardPanel header = new CardPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Tìm kiếm chuyến đi");
        title.setFont(TITLE_FONT.deriveFont(20f));
        title.setForeground(new Color(33, 56, 110));

        JLabel subtitle = new JLabel("Lọc nhanh chuyến tàu phù hợp với lịch trình của bạn");
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

    private JPanel buildFilterPanel() {
        CardPanel panel = new CardPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int col = 0;
        addFilter(panel, gbc, 0, col++, new JLabel("Mã chuyến đi:"), txtMaChuyenDi);
        addFilter(panel, gbc, 0, col++, new JLabel("Ga đi:"), cboGaDi);
        addFilter(panel, gbc, 0, col++, new JLabel("Ga đến:"), cboGaDen);

        addDateTimeFilter(panel, gbc, 1, 0, chkKhoiHanhTu, dcKhoiHanhTu, spTimeTu);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnLamMoi);
        actionPanel.add(btnTim);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 6; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(actionPanel, gbc);

        btnTim.addActionListener(this::onSearch);
        btnLamMoi.addActionListener(this::onReset);

        chkKhoiHanhTu.addActionListener(e -> setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, chkKhoiHanhTu.isSelected()));
        setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, false);
        return panel;
    }

    private JPanel buildTablePanel() {
        CardPanel panel = new CardPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Kết quả tra cứu");
        title.setFont(SECTION_FONT);
        title.setForeground(new Color(45, 70, 120));

        JScrollPane scrollPane = new JScrollPane(tblKetQua);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void onSearch(ActionEvent e) { performSearch(); }

    private void onReset(ActionEvent e) {
        txtMaChuyenDi.setText("");
        if (cboGaDi.getItemCount() > 0) cboGaDi.setSelectedIndex(0);
        if (cboGaDen.getItemCount() > 0) cboGaDen.setSelectedIndex(0);

        chkKhoiHanhTu.setSelected(false);

        Date now = new Date();
        dcKhoiHanhTu.setDate(now);
        spTimeTu.setValue(now);
        setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, false);

        performSearch();
    }

    private void performSearch() {
        Date from = null;
        if (chkKhoiHanhTu.isSelected() && dcKhoiHanhTu.getDate() != null) {
            from = mergeDateAndTime(dcKhoiHanhTu.getDate(), (Date) spTimeTu.getValue());
        }

        try {
            List<ChuyenTau> data = dao.search(
                    txtMaChuyenDi.getText(),
                    (String) cboGaDi.getSelectedItem(),
                    (String) cboGaDen.getSelectedItem(),
                    from,
                    null
            );
            tableModel.setRowCount(0);
            for (ChuyenTau cd : data) {
                tableModel.addRow(new Object[]{
                        cd.getMaChuyenTau(),
                        safeString(cd.getGaDi()),
                        safeString(cd.getGaDen()),
                        formatTime(cd.getThoiGianKhoiHanh()),
                        formatTime(cd.getThoiGianKetThuc()),
                        safeString(cd.getTenTau()),
                        cd.getSoGheTrong()
                });
            }
            tableModel.fireTableDataChanged();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu chuyến đi: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================== Helpers (UI) ==================
    private static JDateChooser makeDateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setDate(new Date());
        dc.setFocusable(false);
        return dc;
    }

    private static JSpinner makeTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "HH:mm");
        ((DateFormatter) editor.getTextField().getFormatter()).setAllowsInvalid(false);
        sp.setEditor(editor);
        return sp;
    }

    private static void setDateTimeEnabled(JDateChooser dc, JSpinner time, boolean enabled) {
        dc.setEnabled(enabled);
        time.setEnabled(enabled);
    }

    private void styleInputs() {
        styleField(txtMaChuyenDi);
        styleCombo(cboGaDi);
        styleCombo(cboGaDen);
        styleDateChooser(dcKhoiHanhTu);
        styleSpinner(spTimeTu);
        styleCheckbox(chkKhoiHanhTu);
        styleButtonPrimary(btnTim);
        styleButtonSecondary(btnLamMoi);
    }

    private void styleTable() {
        tblKetQua.setRowHeight(28);
        tblKetQua.setAutoCreateRowSorter(true);
        tblKetQua.setIntercellSpacing(new Dimension(0, 0));
        tblKetQua.setShowGrid(false);
        tblKetQua.setFillsViewportHeight(true);
        tblKetQua.setSelectionBackground(new Color(91, 137, 255));
        tblKetQua.setSelectionForeground(Color.WHITE);
        tblKetQua.setBackground(Color.WHITE);
        tblKetQua.setForeground(new Color(35, 48, 74));
        tblKetQua.setRowSelectionAllowed(true);

        JTableHeader header = tblKetQua.getTableHeader();
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

    private static void addFilter(JPanel panel, GridBagConstraints gbc, int row, int col, JComponent label, JComponent field) {
        gbc.gridx = col * 2; gbc.gridy = row; gbc.weightx = 0;
        panel.add(label, gbc);
        gbc.gridx = col * 2 + 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private static void addDateTimeFilter(JPanel panel, GridBagConstraints gbc, int row, int col,
                                          JCheckBox checkbox, JDateChooser dateChooser, JSpinner timeSpinner) {
        gbc.gridx = col * 2; gbc.gridy = row; gbc.weightx = 0;
        panel.add(checkbox, gbc);

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        p.add(dateChooser, BorderLayout.CENTER);
        p.add(timeSpinner, BorderLayout.EAST);

        gbc.gridx = col * 2 + 1; gbc.weightx = 1;
        panel.add(p, gbc);
    }

    private static void styleField(JTextField field) { styleInputComponent(field); }
    private static void styleCombo(JComboBox<?> comboBox) {
        styleInputComponent(comboBox);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(new Color(35, 48, 74));
    }
    private static void styleDateChooser(JDateChooser chooser) {
        Component ui = chooser.getDateEditor().getUiComponent();
        JComponent editor = (ui instanceof JComponent) ? (JComponent) ui : null;
        if (editor != null) styleInputComponent(editor);
        chooser.setBorder(BorderFactory.createEmptyBorder());
        chooser.setBackground(Color.WHITE);
    }
    private static void styleSpinner(JSpinner spinner) {
        JComponent editor = (spinner.getEditor() instanceof JComponent) ? (JComponent) spinner.getEditor() : null;
        if (editor != null) styleInputComponent(editor);
        spinner.setBorder(BorderFactory.createEmptyBorder());
        spinner.setBackground(Color.WHITE);
    }
    private static void styleCheckbox(JCheckBox checkBox) {
        checkBox.setOpaque(false);
        checkBox.setForeground(new Color(45, 70, 120));
        checkBox.setFont(checkBox.getFont().deriveFont(Font.BOLD));
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
    private static void styleInputComponent(JComponent component) {
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 210, 237), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        component.setBackground(Color.WHITE);
        component.setForeground(new Color(35, 48, 74));
    }

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

    // === Helpers (data/format) =====
    private void loadStations() {
        cboGaDi.removeAllItems(); cboGaDen.removeAllItems();
        cboGaDi.addItem("Tất cả"); cboGaDen.addItem("Tất cả");
        try {
            for (String ga : dao.getAllGaDi())  cboGaDi.addItem(ga);
            for (String ga : dao.getAllGaDen()) cboGaDen.addItem(ga);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách ga: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Date mergeDateAndTime(Date dateOnly, Date timeOnly) {
        Calendar cDate = Calendar.getInstance(); cDate.setTime(dateOnly);
        Calendar cTime = Calendar.getInstance(); cTime.setTime(timeOnly);
        Calendar out = Calendar.getInstance(); out.clear();
        out.set(cDate.get(Calendar.YEAR), cDate.get(Calendar.MONTH), cDate.get(Calendar.DAY_OF_MONTH),
                cTime.get(Calendar.HOUR_OF_DAY), cTime.get(Calendar.MINUTE), 0);
        out.set(Calendar.MILLISECOND, 0);
        return out.getTime();
    }

    private static String formatTime(LocalDateTime time) { return time != null ? DATE_TIME_FMT.format(time) : ""; }
    private static String formatCurrency(BigDecimal v) {
        if (v == null) return "";
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(v);
    }
    private static String safeString(String v) { return v != null ? v : ""; }
}
