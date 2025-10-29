package ui;

import dao.HoaDonPdfDao;
import dao.HoaDon_Dao;
import entity.HoaDonView;
import entity.InvoicePdfInfo;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import util.HDPdfExporter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class XuatHoaDonPanel extends JPanel {
    // ======= Giữ nguyên style như TimKiemChuyenDiPanel =======
    private static final Font TITLE_FONT   = new Font("SansSerif", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Color ACCENT_COLOR  = new Color(66, 133, 244);
    private static final Color ACCENT_DARKER = new Color(52, 103, 188);

    // ======= DAO =======
    private final HoaDon_Dao dao = new HoaDon_Dao();

    // ======= Filters =======
    private final JTextField txtMaHD = new JTextField();
    private final JCheckBox chkTu   = new JCheckBox("Từ");
    private final JDateChooser dcTu = makeDateChooser();
    private final JSpinner spTu     = makeTimeSpinner();

    private final JCheckBox chkDen  = new JCheckBox("Đến");
    private final JDateChooser dcDen = makeDateChooser();
    private final JSpinner spDen     = makeTimeSpinner();

    private final JTextField txtMaNV = new JTextField();
    private final JTextField txtMaHK = new JTextField();

    private final JButton btnLamMoi = new JButton("Làm mới");
    private final JButton btnTim    = new JButton("Tìm hoá đơn");
    private final JButton btnExport = new JButton("Xuất hoá đơn");

    // ======= Table =======
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Mã hóa đơn","Ngày lập","Khách hàng","SĐT","Nhân viên","VAT","Tổng tiền"}, 0){
        @Override public boolean isCellEditable(int r, int c){ return false; }
    };
    private final JTable table = new JTable(model);
    private final List<HoaDonView> currentData = new ArrayList<>();

    public XuatHoaDonPanel() {
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

        performSearch(); // load mặc định
    }

    // ======= Header =======
    private JPanel buildHeaderPanel() {
        CardPanel header = new CardPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Xuất hóa đơn");
        title.setFont(TITLE_FONT.deriveFont(20f));
        title.setForeground(new Color(33, 56, 110));

        JLabel subtitle = new JLabel("Lọc nhanh hoá đơn theo mã và ngày lập");
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

    // ======= Filters =======
    private JPanel buildFilterPanel() {
        CardPanel panel = new CardPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 24, 12, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int col = 0;
        addFilter(panel, gbc, 0, col++, new JLabel("Mã hóa đơn:"), txtMaHD);
        addDateTimeFilter(panel, gbc, 1, 0, chkTu,  dcTu,  spTu);
        addDateTimeFilter(panel, gbc, 1, 1, chkDen, dcDen, spDen);
        addFilter(panel, gbc, 2, 0, new JLabel("Mã NV:"), txtMaNV);
        addFilter(panel, gbc, 2, 1, new JLabel("Mã HK:"), txtMaHK);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnLamMoi);
        actionPanel.add(btnTim);
        actionPanel.add(btnExport);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 6; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(actionPanel, gbc);

        // events
        btnTim.addActionListener(this::onSearch);
        btnLamMoi.addActionListener(this::onReset);
        btnExport.addActionListener(e -> onExportInvoice());
        chkTu.addActionListener(e -> setDateTimeEnabled(dcTu, spTu, chkTu.isSelected()));
        chkDen.addActionListener(e -> setDateTimeEnabled(dcDen, spDen, chkDen.isSelected()));

        setDateTimeEnabled(dcTu, spTu, false);
        setDateTimeEnabled(dcDen, spDen, false);
        return panel;
    }

    private JPanel buildTablePanel() {
        CardPanel panel = new CardPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Kết quả tra cứu");
        title.setFont(SECTION_FONT);
        title.setForeground(new Color(45, 70, 120));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ======= Actions =======
    private void onSearch(ActionEvent e) { performSearch(); }

    private void onReset(ActionEvent e) {
        txtMaHD.setText("");
        txtMaNV.setText("");
        txtMaHK.setText("");

        chkTu.setSelected(false);
        chkDen.setSelected(false);

        Date now = new Date();
        dcTu.setDate(now);   spTu.setValue(now);
        dcDen.setDate(now);  spDen.setValue(now);

        setDateTimeEnabled(dcTu, spTu, false);
        setDateTimeEnabled(dcDen, spDen, false);

        performSearch();
    }

    private void performSearch() {
        try {
            Timestamp from = null, to = null;
            if (chkTu.isSelected()  && dcTu.getDate()  != null) from = toTimestamp(dcTu.getDate(),  (Date) spTu.getValue());
            if (chkDen.isSelected() && dcDen.getDate() != null) to   = toTimestamp(dcDen.getDate(), (Date) spDen.getValue());

            List<HoaDonView> data = dao.search(
                    txtMaHD.getText().trim(),
                    from, to,
                    txtMaNV.getText().trim(),
                    txtMaHK.getText().trim()
            );

            NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
            model.setRowCount(0);
            currentData.clear();
            currentData.addAll(data);
            for (HoaDonView hd : data) {
                model.addRow(new Object[]{
                        nullToDash(hd.getMaHoaDon()),
                        formatDateTime(hd.getNgayLap()),
                        nullToDash(hd.getTenHanhKhach()),
                        nullToDash(hd.getSdtHanhKhach()),
                        nullToDash(hd.getTenNhanVien()),
                        hd.getVat() == null ? "-" : hd.getVat().toPlainString() + "%",
                        vnd.format(hd.getTongTien() == null ? BigDecimal.ZERO : hd.getTongTien())
                });
            }
            model.fireTableDataChanged();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tìm kiếm hoá đơn: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ======= UI Helpers (giống panel chuyến đi) =======
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
        styleField(txtMaHD);
        styleField(txtMaNV);
        styleField(txtMaHK);
        styleDateChooser(dcTu);
        styleDateChooser(dcDen);
        styleSpinner(spTu);
        styleSpinner(spDen);
        styleCheckbox(chkTu);
        styleCheckbox(chkDen);
        styleButtonPrimary(btnTim);
        styleButtonSecondary(btnLamMoi);
        styleButtonThird(btnExport);
        
    }
    private void styleTable() {
        table.setRowHeight(28);
        table.setAutoCreateRowSorter(true);
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
    private static void styleButtonThird(JButton button) {
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

    // ======= Data helpers =======
    private static Timestamp toTimestamp(Date dateOnly, Date timeOnly) {
        Calendar d = Calendar.getInstance(); d.setTime(dateOnly);
        Calendar t = Calendar.getInstance(); t.setTime(timeOnly);
        Calendar out = Calendar.getInstance(); out.clear();
        out.set(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH),
                t.get(Calendar.HOUR_OF_DAY), t.get(Calendar.MINUTE), 0);
        out.set(Calendar.MILLISECOND, 0);
        return new Timestamp(out.getTimeInMillis());
    }
    private static String formatDateTime(java.sql.Timestamp ts) {
        if (ts == null) return "";
        Calendar c = Calendar.getInstance(); c.setTimeInMillis(ts.getTime());
        return String.format("%02d/%02d/%04d %02d:%02d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH)+1, c.get(Calendar.YEAR),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }
    private static String nullToDash(String s){ return (s == null || s.isBlank()) ? "-" : s; }

    private void onExportInvoice() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 hoá đơn trong bảng.");
            return;
        }
        int row = table.convertRowIndexToModel(viewRow);
        if (row < 0 || row >= currentData.size()) {
            JOptionPane.showMessageDialog(this, "Không thể xác định dữ liệu hoá đơn đã chọn.");
            return;
        }

        HoaDonView selected = currentData.get(row);
        String maHD = selected != null ? selected.getMaHoaDon() : null;
        if (maHD == null || maHD.isBlank() || "-".equals(maHD)) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy mã hoá đơn hợp lệ để xuất.");
            return;
        }

        try {
            HoaDonPdfDao pdfDao = new HoaDonPdfDao();
            Optional<InvoicePdfInfo> infoOpt = pdfDao.findByMaHoaDon(maHD);
            if (infoOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy dữ liệu chi tiết cho hoá đơn " + maHD,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Path exportDir = Paths.get("/BanVeTauv2/HoaDon");
            Files.createDirectories(exportDir);
            Path output = exportDir.resolve(maHD + ".pdf");

            HDPdfExporter.export(infoOpt.get(), output.toString());

            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(output.toFile());
                }
            } catch (Exception openEx) {
                JOptionPane.showMessageDialog(this,
                        "Đã lưu hoá đơn nhưng không thể mở tệp: " + openEx.getMessage(),
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE);
            }

            JOptionPane.showMessageDialog(this,
                    "Đã xuất hoá đơn: " + output.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Có lỗi khi xuất hoá đơn: " + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}
