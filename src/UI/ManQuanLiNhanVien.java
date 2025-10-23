package ui;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import dao.NhanVien_Dao;
import entity.LoaiNhanVien;
import entity.NhanVienThongTin;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManQuanLiNhanVien extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(47, 107, 255);
    private static final Color BLUE_SOFT = new Color(230, 240, 255);
    private static final Color BORDER_SOFT = new Color(200, 220, 255);
    private static final Color TEXT_DARK = new Color(30, 35, 45);
    private static final Color TABLE_HEADER_BG = new Color(27, 38, 77);
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NhanVien_Dao nhanVienDao = new NhanVien_Dao();

    private final JTextField txtMaNV = new JTextField();
    private final JTextField txtTenNV = new JTextField();
    private final JDateChooser dcNgaySinh = new JDateChooser();
    
    private final JTextField txtSDT = new JTextField();
    private final JTextField txtEmail = new JTextField();
    private final JTextField txtCCCD = new JTextField();
    private final JComboBox<String> cboLoaiNV = new JComboBox<>();
    private final JTextField txtNgayBatDau = new JTextField();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Mã nhân viên", "Tên nhân viên", "Ngày sinh", "Số điện thoại",
            "Email", "CCCD", "Loại nhân viên", "Ngày bắt đầu làm việc"
    }, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    private final JButton btnThem = new JButton("Thêm");
    private final JButton btnSua = new JButton("Cập nhật");
    private final JButton btnXoa = new JButton("Xóa");

    private final List<NhanVienThongTin> currentData = new ArrayList<>();
    private final Map<String, String> loaiNvByMoTa = new LinkedHashMap<>();

    public ManQuanLiNhanVien() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel leftForm = buildFormPanel();
        JPanel rightTable = buildTablePanel();

        JPanel leftHolder = new JPanel(new BorderLayout());
        leftHolder.setOpaque(false);
        leftForm.setPreferredSize(new Dimension(520, 0));
        leftForm.setMinimumSize(new Dimension(520, 0));
        leftForm.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
        leftHolder.setPreferredSize(new Dimension(520, 0));
        leftHolder.add(leftForm, BorderLayout.CENTER);
        
        java.awt.event.MouseAdapter formBackgroundClick = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Component source = e.getComponent();
                if (!(source instanceof Container)) {
                    return;
                }
                Component deepest = SwingUtilities.getDeepestComponentAt((Container) source, e.getX(), e.getY());
                if (deepest != source) {
                    return;
                }
                resetFormForNewEntry();
            }
        };
        leftHolder.addMouseListener(formBackgroundClick);
        leftForm.addMouseListener(formBackgroundClick);


        add(leftHolder, BorderLayout.WEST);
        add(rightTable, BorderLayout.CENTER);

        wireEvents();
        loadInitialData();
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                "THÔNG TIN NHÂN VIÊN",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(70, 130, 180)
        ));

        GridBagConstraints l = new GridBagConstraints();
        l.insets = new Insets(18, 14, 12, 8);
        l.anchor = GridBagConstraints.EAST;
        l.gridx = 0;
        l.gridy = 0;

        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(18, 0, 12, 14);
        f.fill = GridBagConstraints.HORIZONTAL;
        f.weightx = 1;
        f.gridx = 1;
        f.gridy = 0;

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        java.util.function.Function<String, JLabel> L = (text) -> {
            JLabel lb = new JLabel(text, SwingConstants.RIGHT);
            lb.setFont(labelFont);
            lb.setForeground(TEXT_DARK);
            lb.setPreferredSize(new Dimension(130, 28));
            return lb;
        };
        java.util.function.Consumer<JComponent> styleField = (comp) -> {
            comp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            comp.setPreferredSize(new Dimension(420, 34));
            comp.setMinimumSize(new Dimension(380, 34));
            comp.setBackground(Color.WHITE);
            comp.setBorder(new CompoundBorder(new LineBorder(BORDER_SOFT), new EmptyBorder(6, 8, 6, 8)));
        };

        // Style cho combobox và datepicker
        cboLoaiNV.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        dcNgaySinh.setDateFormatString("dd/MM/yyyy");
        dcNgaySinh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dcNgaySinh.setOpaque(true);
        JTextFieldDateEditor editor = (JTextFieldDateEditor) dcNgaySinh.getDateEditor();
        editor.setEditable(true);
        editor.setColumns(20);
        Dimension fieldSize = new Dimension(420, 34);
        dcNgaySinh.setPreferredSize(fieldSize);
        dcNgaySinh.setMinimumSize(new Dimension(380, 34));
        int calBtnW = 34;
        editor.setPreferredSize(new Dimension(fieldSize.width - calBtnW, fieldSize.height));
        editor.setBorder(new CompoundBorder(new LineBorder(BORDER_SOFT), new EmptyBorder(6, 8, 6, 8)));
        JButton calBtn = dcNgaySinh.getCalendarButton();
        calBtn.setText("...");
        calBtn.setPreferredSize(new Dimension(calBtnW, fieldSize.height));

        p.add(L.apply("Mã nhân viên"), l);
        p.add(txtMaNV, f);
        styleField.accept(txtMaNV);
        txtMaNV.setEditable(false);
        txtMaNV.setBackground(new Color(245, 248, 255));

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Tên nhân viên"), l);
        p.add(txtTenNV, f);
        styleField.accept(txtTenNV);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Ngày sinh"), l);
        p.add(dcNgaySinh, f);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Số điện thoại"), l);
        p.add(txtSDT, f);
        styleField.accept(txtSDT);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Email"), l);
        p.add(txtEmail, f);
        styleField.accept(txtEmail);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("CCCD"), l);
        p.add(txtCCCD, f);
        styleField.accept(txtCCCD);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Loại nhân viên"), l);
        p.add(cboLoaiNV, f);
        styleField.accept(cboLoaiNV);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Ngày bắt đầu"), l);
        p.add(txtNgayBatDau, f);
        styleField.accept(txtNgayBatDau);
        txtNgayBatDau.setEditable(false);
        txtNgayBatDau.setBackground(new Color(245, 248, 255));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 15));
        btnBar.setOpaque(false);
        for (JButton b : new JButton[]{btnThem, btnSua, btnXoa}) {
            styleButton(b);
            btnBar.add(b);
        }
        GridBagConstraints gBtn = new GridBagConstraints();
        gBtn.gridx = 0;
        gBtn.gridy = ++l.gridy;
        gBtn.gridwidth = 2;
        gBtn.insets = new Insets(30, 14, 30, 14);
        gBtn.fill = GridBagConstraints.HORIZONTAL;
        p.add(btnBar, gBtn);

        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_SOFT),
                "DANH SÁCH NHÂN VIÊN",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(70, 130, 180)
        ));

        table.setFillsViewportHeight(true);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 242, 255));
        table.setSelectionBackground(BLUE_SOFT);
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setDefaultRenderer(new TableCellRenderer() {
            private final JLabel lbl = new JLabel();
            {
                lbl.setOpaque(true);
                lbl.setBackground(TABLE_HEADER_BG);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(new MatteBorder(0, 0, 1, 1, BORDER_SOFT));
            }
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                        boolean isSelected, boolean hasFocus, int row, int column) {
                lbl.setText(value == null ? "" : value.toString());
                return lbl;
            }
        });

        int[] widths = {110, 160, 110, 120, 180, 130, 160, 160};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = table.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_SOFT));

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(BLUE_PRIMARY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorder(new EmptyBorder(8, 22, 8, 22));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Màu hover và nhấn
        Color hoverBg  = BLUE_PRIMARY.darker();
        Color pressBg  = hoverBg.darker();

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBg);      // hover → xanh đậm
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(BLUE_PRIMARY); // rời chuột → xanh gốc
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(pressBg);      // nhấn → xanh rất đậm
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                // nếu vẫn trong vùng → hover, ngược lại → xanh gốc
                if (b.getBounds().contains(e.getPoint())) {
                    b.setBackground(hoverBg);
                } else {
                    b.setBackground(BLUE_PRIMARY);
                }
            }
        });
    }




    private void wireEvents() {
        btnThem.addActionListener(e -> handleAdd());
        btnSua.addActionListener(e -> handleUpdate());
        btnXoa.addActionListener(e -> handleDeactivate());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelection();
            }
        });

    }

    private void loadInitialData() {
        loadLoaiNhanVienOptions();
        refreshTable();
        resetFormForNewEntry();
    }

    private void loadLoaiNhanVienOptions() {
        try {
            loaiNvByMoTa.clear();
            cboLoaiNV.removeAllItems();
            List<LoaiNhanVien> options = nhanVienDao.findAllLoaiNhanVien();
            for (LoaiNhanVien loai : options) {
                String moTa = loai.getMoTa();
                if (moTa == null || moTa.isBlank()) {
                    moTa = loai.getMaLoaiNV();
                }
                cboLoaiNV.addItem(moTa);
                loaiNvByMoTa.put(moTa, loai.getMaLoaiNV());
            }
        } catch (SQLException ex) {
            showError("Không thể tải danh sách loại nhân viên", ex);
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        currentData.clear();
        try {
            List<NhanVienThongTin> list = nhanVienDao.findAll();
            currentData.addAll(list);
            for (NhanVienThongTin nv : list) {
                model.addRow(new Object[]{
                        nv.getMaNV(),
                        nv.getTenNV(),
                        formatDate(nv.getNgaySinh()),
                        nv.getSoDienThoai(),
                        nv.getEmail(),
                        nv.getCccd(),
                        nv.getLoaiNV(),
                        formatDate(nv.getNgayBatDauLamViec())
                });
            }
            model.fireTableDataChanged();
        } catch (SQLException ex) {
            showError("Không thể tải danh sách nhân viên", ex);;
        }
    }

    private void prepareForNewEntry() {
        txtTenNV.setText("");
        dcNgaySinh.setDate(null);
        txtSDT.setText("");
        txtEmail.setText("");
        txtCCCD.setText("");
        if (cboLoaiNV.getItemCount() > 0) {
            cboLoaiNV.setSelectedIndex(0);
        }
        LocalDate today = LocalDate.now();
        txtNgayBatDau.setText(DF.format(today));
        generateNextId();
//        btnSua.setEnabled(false);
//        btnXoa.setEnabled(false);
    }

    private void resetFormForNewEntry() {
        if (!table.getSelectionModel().isSelectionEmpty()) {
            table.clearSelection();
            return;
        }
        prepareForNewEntry();
    }

    private void generateNextId() {
        try {
            txtMaNV.setText(nhanVienDao.generateNextId());
        } catch (SQLException ex) {
            txtMaNV.setText("");
            showError("Không thể sinh mã nhân viên mới", ex);
        }
    }

    private void onTableSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            prepareForNewEntry();
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String maNV = (String) model.getValueAt(modelRow, 0);
        NhanVienThongTin nv = findEmployeeById(maNV);
        if (nv != null) {
            fillForm(nv);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
    }

    private void fillForm(NhanVienThongTin nv) {
        txtMaNV.setText(nv.getMaNV());
        txtTenNV.setText(nv.getTenNV());
        if (nv.getNgaySinh() != null) {
            dcNgaySinh.setDate(java.util.Date.from(nv.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            dcNgaySinh.setDate(null);
        }
        txtSDT.setText(nv.getSoDienThoai());
        txtEmail.setText(nv.getEmail());
        txtCCCD.setText(nv.getCccd());
        txtNgayBatDau.setText(formatDate(nv.getNgayBatDauLamViec()));
        ensureLoaiExistsInCombo(nv.getLoaiNV(), nv.getMaLoaiNV());
    }

    private void ensureLoaiExistsInCombo(String display, String maLoai) {
        if (display == null || display.isBlank()) {
            return;
        }
        boolean found = false;
        for (int i = 0; i < cboLoaiNV.getItemCount(); i++) {
            if (display.equals(cboLoaiNV.getItemAt(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            cboLoaiNV.addItem(display);
        }
        if (maLoai != null && !maLoai.isBlank()) {
            loaiNvByMoTa.putIfAbsent(display, maLoai);
        }
        cboLoaiNV.setSelectedItem(display);
    }

    private NhanVienThongTin findEmployeeById(String maNV) {
        return currentData.stream()
                .filter(item -> item.getMaNV().equals(maNV))
                .findFirst()
                .orElse(null);
    }

    private void handleAdd() {
        if (!validateForm()) {
            return;
        }
        String maNV = txtMaNV.getText().trim();
        String tenNV = txtTenNV.getText().trim();
        LocalDate ngaySinh = getNgaySinh();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String moTaLoai = (String) cboLoaiNV.getSelectedItem();
        String maLoaiNV = loaiNvByMoTa.get(moTaLoai);
        if (maLoaiNV == null || maLoaiNV.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã loại nhân viên", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate ngayBatDau = LocalDate.now();
        txtNgayBatDau.setText(DF.format(ngayBatDau));

        NhanVienThongTin nv = new NhanVienThongTin(maNV, tenNV, ngaySinh, sdt, email, moTaLoai, maLoaiNV, cccd, ngayBatDau);
        try {
            int rows = nhanVienDao.insert(nv);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Thêm nhân viên thành công. Nhân viên đã được hiển thị trong danh sách.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                resetFormForNewEntry();
            }
        } catch (SQLException ex) {
            showError("Không thể thêm nhân viên", ex);
        }
    }

    private void handleUpdate() {
        if (txtMaNV.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần cập nhật", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) {
            return;
        }
        String maNV = txtMaNV.getText().trim();
        String tenNV = txtTenNV.getText().trim();
        LocalDate ngaySinh = getNgaySinh();
        String sdt = txtSDT.getText().trim();
        String email = txtEmail.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String moTaLoai = (String) cboLoaiNV.getSelectedItem();
        String maLoaiNV = loaiNvByMoTa.get(moTaLoai);
        if (maLoaiNV == null || maLoaiNV.isBlank()) {
            NhanVienThongTin current = findEmployeeById(maNV);
            if (current != null) {
                maLoaiNV = current.getMaLoaiNV();
            }
        }
        if (maLoaiNV == null || maLoaiNV.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã loại nhân viên", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate ngayBatDau = parseDate(txtNgayBatDau.getText());

        NhanVienThongTin nv = new NhanVienThongTin(maNV, tenNV, ngaySinh, sdt, email, moTaLoai, maLoaiNV, cccd, ngayBatDau);
        try {
            int rows = nhanVienDao.update(nv);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                resetFormForNewEntry();
            }
        } catch (SQLException ex) {
            showError("Không thể cập nhật nhân viên", ex);
        }
    }

    private void handleDeactivate() {
        String maNV = txtMaNV.getText().trim();
        if (maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần ẩn", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Ẩn nhân viên này khỏi danh sách? (tài khoản sẽ chuyển sang trạng thái Vô hiệu hóa)",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int rows = nhanVienDao.deactivateById(maNV);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Đã ẩn nhân viên khỏi danh sách", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nhân viên hiện chưa có tài khoản hoặc tài khoản đã bị vô hiệu hóa.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshTable();
            resetFormForNewEntry();
        } catch (SQLException ex) {
            showError("Không thể cập nhật trạng thái tài khoản", ex);
        }
    }

    private boolean validateForm() {
        if (txtTenNV.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtTenNV.requestFocus();
            return false;
        }
        if (txtSDT.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        if (txtCCCD.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }
        if (cboLoaiNV.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại nhân viên", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboLoaiNV.requestFocus();
            return false;
        }
        return true;
    }

    private LocalDate getNgaySinh() {
        java.util.Date date = dcNgaySinh.getDate();
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, DF);
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DF.format(date);
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}