package UI;

import dao.NhanVien_Dao;
import model.NhanVienThongTin;

import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuanLyNhanVienPanel extends JPanel {
    private final NhanVien_Dao nvDao = new NhanVien_Dao();

    private final List<NhanVienThongTin> currentData = new ArrayList<>();

    private JTable tbl;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;

    // --- Form Thêm ---
    private JTextField txtMa_Add, txtTen_Add, txtSDT_Add, txtEmail_Add;
    private JSpinner spNgaySinh_Add;
    private JComboBox<String> cboLoai_Add;
    private JButton btnThem;

    // --- Form Sửa ---
    private JTextField txtMa_Edit, txtTen_Edit, txtSDT_Edit, txtEmail_Edit;
    private JSpinner spNgaySinh_Edit;
    private JComboBox<String> cboLoai_Edit;
    private JButton btnCapNhat;

    // --- Form Xóa ---
    private JTextField txtMa_Del, txtTen_Del, txtNgaySinh_Del, txtSDT_Del, txtEmail_Del, txtLoai_Del;
    private JButton btnXoa;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public QuanLyNhanVienPanel() {
        setLayout(new BorderLayout(10, 10));
        initUI();
        loadData();
        bindEvents();
        refreshAddId();
    }

    private void initUI() {
        JPanel left = new JPanel(new BorderLayout(5, 5));
        left.setBorder(new TitledBorder("Danh sách nhân viên"));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        txtSearch = new JTextField();
        JButton btnSearch = new JButton("Tìm");
        searchPanel.add(new JLabel("Từ khóa:"), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearch, BorderLayout.EAST);
        left.add(searchPanel, BorderLayout.NORTH);

        String[] cols = {"Mã nhân viên", "Tên nhân viên", "Ngày sinh", "Số điện thoại", "Email", "Loại"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tbl = new JTable(model);
        tbl.setRowHeight(26);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        left.add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnSearch.addActionListener(e -> applyFilter());
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                applyFilter();
            }
        });

        // Right panel - form tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Thêm", buildAddTab());
        tabs.addTab("Sửa", buildEditTab());
        tabs.addTab("Xóa", buildDeleteTab());
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) {
                refreshAddId();
            }
        });

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new TitledBorder("Thông tin nhân viên"));
        right.add(tabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.6);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildAddTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtMa_Add = roField();
        txtTen_Add = new JTextField();
        spNgaySinh_Add = dateSpinner();
        txtSDT_Add = new JTextField();
        txtEmail_Add = new JTextField();
        cboLoai_Add = new JComboBox<>(new String[]{"Quản trị", "Nhân viên bán vé"});

        int r = 0;
        addLine(p, gc, r++, "Mã nhân viên", txtMa_Add);
        addLine(p, gc, r++, "Tên nhân viên", txtTen_Add);
        addLine(p, gc, r++, "Ngày sinh", spNgaySinh_Add);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Add);
        addLine(p, gc, r++, "Email", txtEmail_Add);
        addLine(p, gc, r++, "Loại nhân viên", cboLoai_Add);

        btnThem = new JButton("Thêm");
        addFull(p, gc, r, btnThem);
        return p;
    }

    private JPanel buildEditTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtMa_Edit = roField();
        txtTen_Edit = new JTextField();
        spNgaySinh_Edit = dateSpinner();
        txtSDT_Edit = new JTextField();
        txtEmail_Edit = new JTextField();
        cboLoai_Edit = new JComboBox<>(new String[]{"Quản trị", "Nhân viên bán vé"});

        int r = 0;
        addLine(p, gc, r++, "Mã nhân viên", txtMa_Edit);
        addLine(p, gc, r++, "Tên nhân viên", txtTen_Edit);
        addLine(p, gc, r++, "Ngày sinh", spNgaySinh_Edit);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Edit);
        addLine(p, gc, r++, "Email", txtEmail_Edit);
        addLine(p, gc, r++, "Loại nhân viên", cboLoai_Edit);

        btnCapNhat = new JButton("Cập nhật");
        addFull(p, gc, r, btnCapNhat);
        return p;
    }

    private JPanel buildDeleteTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtMa_Del = roField();
        txtTen_Del = roField();
        txtNgaySinh_Del = roField();
        txtSDT_Del = roField();
        txtEmail_Del = roField();
        txtLoai_Del = roField();

        int r = 0;
        addLine(p, gc, r++, "Mã nhân viên", txtMa_Del);
        addLine(p, gc, r++, "Tên nhân viên", txtTen_Del);
        addLine(p, gc, r++, "Ngày sinh", txtNgaySinh_Del);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Del);
        addLine(p, gc, r++, "Email", txtEmail_Del);
        addLine(p, gc, r++, "Loại nhân viên", txtLoai_Del);

        btnXoa = new JButton("Xóa");
        addFull(p, gc, r, btnXoa);
        return p;
    }

    private void loadData() {
        try {
            currentData.clear();
            model.setRowCount(0);
            List<NhanVienThongTin> list = nvDao.findAll();
            currentData.addAll(list);
            for (NhanVienThongTin nv : list) {
                model.addRow(new Object[]{
                        nv.getMaNV(),
                        nv.getTenNV(),
                        formatDate(nv.getNgaySinh()),
                        nv.getSoDienThoai(),
                        nv.getEmail(),
                        nv.getLoaiNV()
                });
            }
            model.fireTableDataChanged();
            clearSelection();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bindEvents() {
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || tbl.getSelectedRow() == -1) {
                return;
            }
            int viewRow = tbl.getSelectedRow();
            int modelRow = tbl.convertRowIndexToModel(viewRow);
            if (modelRow >= 0 && modelRow < currentData.size()) {
                fillForms(currentData.get(modelRow));
            }
        });

        btnThem.addActionListener(e -> handleAdd());
        btnCapNhat.addActionListener(e -> handleUpdate());
        btnXoa.addActionListener(e -> handleDelete());
    }

    private void handleAdd() {
        String ma = txtMa_Add.getText().trim();
        String ten = txtTen_Add.getText().trim();
        LocalDate ngaySinh = getSpinnerDate(spNgaySinh_Add);
        String sdt = txtSDT_Add.getText().trim();
        String email = txtEmail_Add.getText().trim();
        String loai = (String) cboLoai_Add.getSelectedItem();

        if (!validateInput(ten, sdt, email, ngaySinh)) {
            return;
        }

        NhanVienThongTin nv = new NhanVienThongTin(ma, ten, ngaySinh, sdt, email, loai);
        try {
            int affected = nvDao.insert(nv);
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                resetAddForm();
                loadData();
                refreshAddId();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể thêm nhân viên: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        String ma = txtMa_Edit.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần sửa.");
            return;
        }
        String ten = txtTen_Edit.getText().trim();
        LocalDate ngaySinh = getSpinnerDate(spNgaySinh_Edit);
        String sdt = txtSDT_Edit.getText().trim();
        String email = txtEmail_Edit.getText().trim();
        String loai = (String) cboLoai_Edit.getSelectedItem();

        if (!validateInput(ten, sdt, email, ngaySinh)) {
            return;
        }

        NhanVienThongTin nv = new NhanVienThongTin(ma, ten, ngaySinh, sdt, email, loai);
        try {
            int affected = nvDao.update(nv);
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên để cập nhật.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        String ma = txtMa_Del.getText().trim();
        if (ma.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa nhân viên " + ma + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int affected = nvDao.deactivateById(ma);
            if (affected > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa nhân viên.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearSelection();
                loadData();
                refreshAddId();
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không thể xóa: " + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillForms(NhanVienThongTin nv) {
        txtMa_Edit.setText(nv.getMaNV());
        txtTen_Edit.setText(nv.getTenNV());
        setSpinnerDate(spNgaySinh_Edit, nv.getNgaySinh());
        txtSDT_Edit.setText(nv.getSoDienThoai());
        txtEmail_Edit.setText(nv.getEmail());
        cboLoai_Edit.setSelectedItem(nv.getLoaiNV() != null ? nv.getLoaiNV() : "Quản trị");

        txtMa_Del.setText(nv.getMaNV());
        txtTen_Del.setText(nv.getTenNV());
        txtNgaySinh_Del.setText(formatDate(nv.getNgaySinh()));
        txtSDT_Del.setText(nv.getSoDienThoai());
        txtEmail_Del.setText(nv.getEmail());
        txtLoai_Del.setText(nv.getLoaiNV());
    }

    private void clearSelection() {
        tbl.clearSelection();
        txtMa_Edit.setText("");
        txtTen_Edit.setText("");
        setSpinnerDate(spNgaySinh_Edit, LocalDate.now());
        txtSDT_Edit.setText("");
        txtEmail_Edit.setText("");
        cboLoai_Edit.setSelectedIndex(0);

        txtMa_Del.setText("");
        txtTen_Del.setText("");
        txtNgaySinh_Del.setText("");
        txtSDT_Del.setText("");
        txtEmail_Del.setText("");
        txtLoai_Del.setText("");
    }

    private void resetAddForm() {
        txtTen_Add.setText("");
        setSpinnerDate(spNgaySinh_Add, LocalDate.now());
        txtSDT_Add.setText("");
        txtEmail_Add.setText("");
        cboLoai_Add.setSelectedIndex(0);
    }

    private void refreshAddId() {
        try {
            String nextId = nvDao.generateNextId();
            txtMa_Add.setText(nextId);
        } catch (SQLException ex) {
            txtMa_Add.setText("Không lấy được mã");
        }
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    private boolean validateInput(String ten, String sdt, String email, LocalDate ngaySinh) {
        if (ten.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên nhân viên không được để trống.");
            return false;
        }
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống.");
            return false;
        }
        if (!sdt.matches("\\d{9,11}")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ.");
            return false;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được để trống.");
            return false;
        }
        if (!email.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ.");
            return false;
        }
        if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Ngày sinh không được vượt quá hiện tại.");
            return false;
        }
        return true;
    }

    private static GridBagConstraints baseGC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        return gc;
    }

    private static void addLine(JPanel p, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        p.add(new JLabel(label), gc);

        gc.gridx = 1;
        gc.weightx = 1;
        p.add(comp, gc);
    }

    private static void addFull(JPanel p, GridBagConstraints gc, int row, JComponent comp) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.gridwidth = 2;
        gc.weightx = 1;
        p.add(comp, gc);
        gc.gridwidth = 1;
    }

    private static JTextField roField() {
        JTextField t = new JTextField();
        t.setEditable(false);
        return t;
    }

    private static JSpinner dateSpinner() {
        SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "dd/MM/yyyy");
        sp.setEditor(editor);
        return sp;
    }

    private static LocalDate getSpinnerDate(JSpinner spinner) {
        Object value = spinner.getValue();
        if (value instanceof Date) {
            return ((Date) value).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private static void setSpinnerDate(JSpinner spinner, LocalDate date) {
        Date value;
        if (date != null) {
            value = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else {
            value = new Date();
        }
        spinner.setValue(value);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? "" : DF.format(date);
    }
}