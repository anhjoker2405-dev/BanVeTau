package UI;

import dao.ChuyenDi_Dao;
import model.ChuyenDi;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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

    private final ChuyenDi_Dao dao = new ChuyenDi_Dao();
    private final JTextField txtMaChuyenDi = new JTextField();
    private final JComboBox<String> cboGaDi = new JComboBox<>();
    private final JComboBox<String> cboGaDen = new JComboBox<>();

    private final JCheckBox chkKhoiHanhTu = new JCheckBox("Từ");
    private final JCheckBox chkKhoiHanhDen = new JCheckBox("Đến");

    // ==== Date & Time pickers (JCalendar + Spinner) ====
    private final JDateChooser dcKhoiHanhTu = makeDateChooser();
    private final JDateChooser dcKhoiHanhDen = makeDateChooser();
    private final JSpinner spTimeTu = makeTimeSpinner();     // HH:mm
    private final JSpinner spTimeDen = makeTimeSpinner();    // HH:mm
    

    private final JButton btnTim = new JButton("Tìm chuyến đi");
    private final JButton btnLamMoi = new JButton("Làm mới");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                    "Mã chuyến đi",
                    "Ga đi",
                    "Ga đến",
                    "Thời gian khởi hành",
                    "Thời gian đến dự kiến",
                    "Tên tàu",
                    "Số ghế Trống",
            }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable tblKetQua = new JTable(tableModel);

    public TimKiemChuyenDiPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(buildFilterPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadStations();
        performSearch();
    }

    private JPanel buildFilterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bộ lọc"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int col = 0;
        addFilter(panel, gbc, 0, col++, new JLabel("Mã chuyến đi:"), txtMaChuyenDi);
        addFilter(panel, gbc, 0, col++, new JLabel("Ga đi:"), cboGaDi);
        addFilter(panel, gbc, 0, col++, new JLabel("Ga đến:"), cboGaDen);

        // Hàng chọn ngày/giờ "Từ" và "Đến"
        addDateTimeFilter(panel, gbc, 1, 0, chkKhoiHanhTu, dcKhoiHanhTu, spTimeTu);
        addDateTimeFilter(panel, gbc, 1, 1, chkKhoiHanhDen, dcKhoiHanhDen, spTimeDen);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(btnLamMoi);
        actionPanel.add(btnTim);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(actionPanel, gbc);

        btnTim.addActionListener(this::onSearch);
        btnLamMoi.addActionListener(this::onReset);

        // Enable/disable theo checkbox
        chkKhoiHanhTu.addActionListener(e -> setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, chkKhoiHanhTu.isSelected()));
        chkKhoiHanhDen.addActionListener(e -> setDateTimeEnabled(dcKhoiHanhDen, spTimeDen, chkKhoiHanhDen.isSelected()));
        setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, false);
        setDateTimeEnabled(dcKhoiHanhDen, spTimeDen, false);

        return panel;
    }

    private JPanel buildTablePanel() {
        tblKetQua.setRowHeight(26);
        tblKetQua.setAutoCreateRowSorter(true);
        tblKetQua.getTableHeader().setReorderingAllowed(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Kết quả tra cứu"));
        panel.add(new JScrollPane(tblKetQua), BorderLayout.CENTER);
        return panel;
    }

    private void onSearch(ActionEvent event) {
        performSearch();
    }

    private void onReset(ActionEvent event) {
        txtMaChuyenDi.setText("");
        if (cboGaDi.getItemCount() > 0) cboGaDi.setSelectedIndex(0);
        if (cboGaDen.getItemCount() > 0) cboGaDen.setSelectedIndex(0);

        chkKhoiHanhTu.setSelected(false);
        chkKhoiHanhDen.setSelected(false);

        Date now = new Date();
        dcKhoiHanhTu.setDate(now);
        dcKhoiHanhDen.setDate(now);
        spTimeTu.setValue(now);
        spTimeDen.setValue(now);

        setDateTimeEnabled(dcKhoiHanhTu, spTimeTu, false);
        setDateTimeEnabled(dcKhoiHanhDen, spTimeDen, false);

        performSearch();
    }

    private void performSearch() {
        Date from = null;
        Date to = null;

        if (chkKhoiHanhTu.isSelected() && dcKhoiHanhTu.getDate() != null) {
            from = mergeDateAndTime(dcKhoiHanhTu.getDate(), (Date) spTimeTu.getValue());
        }
        if (chkKhoiHanhDen.isSelected() && dcKhoiHanhDen.getDate() != null) {
            to = mergeDateAndTime(dcKhoiHanhDen.getDate(), (Date) spTimeDen.getValue());
        }

        try {
            List<ChuyenDi> data = dao.search(
                    txtMaChuyenDi.getText(),
                    (String) cboGaDi.getSelectedItem(),
                    (String) cboGaDen.getSelectedItem(),
                    from,
                    to
            );

            tableModel.setRowCount(0);
            for (ChuyenDi cd : data) {
                tableModel.addRow(new Object[]{
                        cd.getMaChuyenTau(),
                        safeString(cd.getGaDi()),
                        safeString(cd.getGaDen()),
                        formatTime(cd.getThoiGianKhoiHanh()),
                        formatTime(cd.getThoiGianKetThuc()),
                        safeString(cd.getTenTau()),
                        cd.getSoGheTrong()
                        // add getter
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
        dc.setDateFormatString("dd/MM/yyyy");  // hiển thị 12/10/2025
        dc.setDate(new Date());
        dc.setFocusable(false);
        return dc;
    }

    private static JSpinner makeTimeSpinner() {
        // Spinner chỉ hiện GIỜ:PHÚT (HH:mm)
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "HH:mm");
        // chặn nhập ký tự tự do (vẫn cho phép tăng/giảm bằng mũi tên)
        ((DateFormatter) editor.getTextField().getFormatter()).setAllowsInvalid(false);
        sp.setEditor(editor);
        return sp;
    }

    private static void setDateTimeEnabled(JDateChooser dc, JSpinner time, boolean enabled) {
        dc.setEnabled(enabled);
        time.setEnabled(enabled);
    }

    private static void addFilter(JPanel panel, GridBagConstraints gbc, int row, int col, JComponent label, JComponent field) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = col * 2 + 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private static void addDateTimeFilter(JPanel panel, GridBagConstraints gbc, int row, int col,
                                          JCheckBox checkbox, JDateChooser dateChooser, JSpinner timeSpinner) {
        gbc.gridx = col * 2;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(checkbox, gbc);

        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.add(dateChooser, BorderLayout.CENTER);
        p.add(timeSpinner, BorderLayout.EAST);

        gbc.gridx = col * 2 + 1;
        gbc.weightx = 1;
        panel.add(p, gbc);
    }

    // ================ Helpers (data/format) ===========
    
    private void loadStations() {
        cboGaDi.removeAllItems();
        cboGaDen.removeAllItems();
        cboGaDi.addItem("Tất cả");
        cboGaDen.addItem("Tất cả");
        try {
            for (String ga : dao.getAllGaDi()) {
                cboGaDi.addItem(ga);
            }
            for (String ga : dao.getAllGaDen()) {
                cboGaDen.addItem(ga);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách ga: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private static Date mergeDateAndTime(Date dateOnly, Date timeOnly) {
        Calendar cDate = Calendar.getInstance();
        cDate.setTime(dateOnly);

        Calendar cTime = Calendar.getInstance();
        cTime.setTime(timeOnly);

        Calendar out = Calendar.getInstance();
        out.clear();
        out.set(
                cDate.get(Calendar.YEAR),
                cDate.get(Calendar.MONTH),
                cDate.get(Calendar.DAY_OF_MONTH),
                cTime.get(Calendar.HOUR_OF_DAY),
                cTime.get(Calendar.MINUTE),
                0
        );
        out.set(Calendar.MILLISECOND, 0);
        return out.getTime();
    }

    private static String formatTime(LocalDateTime time) {
        return time != null ? DATE_TIME_FMT.format(time) : "";
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return nf.format(value);
    }

    private static String safeString(String value) {
        return value != null ? value : "";
    }
}
