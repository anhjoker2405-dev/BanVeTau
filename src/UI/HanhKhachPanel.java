package UI;

import dao.HanhKhach_Dao;
import model.HanhKhach;
import connectDB.ConnectDB;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HanhKhachPanel extends JPanel {
    private final HanhKhach_Dao hkDao = new HanhKhach_Dao();

    // Table
    private JTable tbl;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtSearch;

    // --- Form Nhập ---
    private JTextField txtTen_Add, txtSDT_Add, txtCCCD_Add;
    private JComboBox<String> cboGT_Add;
    private JButton btnThem;

    // --- Form Sửa ---
    private JTextField txtMa_Edit, txtTen_Edit, txtSDT_Edit, txtCCCD_Edit;
    private JComboBox<String> cboGT_Edit;
    private JButton btnCapNhat;

    // --- Form Xóa (read-only) ---
    private JTextField txtMa_Del, txtTen_Del, txtSDT_Del, txtCCCD_Del, txtGT_Del;
    private JButton btnXoa;

    public HanhKhachPanel() {
        setLayout(new BorderLayout(10,10));
        initUI();
        loadData();
        bindEvents();
    }

    private void initUI() {
        // ===== Bên trái màn: TABLE =====
        JPanel left = new JPanel(new BorderLayout(5,5));
        left.setBorder(new TitledBorder("Danh sách hành khách"));

        JPanel top = new JPanel(new BorderLayout(5,5));
        txtSearch = new JTextField();
        JButton btnTim = new JButton("Tìm");
        top.add(new JLabel("Tìm nhanh: "), BorderLayout.WEST);
        top.add(txtSearch, BorderLayout.CENTER);
        top.add(btnTim, BorderLayout.EAST);
        left.add(top, BorderLayout.NORTH);

        String[] cols = {"Mã HK", "Tên HK", "SĐT", "CCCD", "Giới tính"};
        model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        tbl = new JTable(model);
        tbl.setRowHeight(26);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        tbl.setRowSorter(sorter);
        left.add(new JScrollPane(tbl), BorderLayout.CENTER);

        btnTim.addActionListener(e -> applyFilter());
        txtSearch.addKeyListener(new KeyAdapter() { @Override public void keyReleased(KeyEvent e) { applyFilter(); }});

        // ==== Bên phải màn: TABS ====
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Thêm", buildAddTab());
        tabs.addTab("Sửa",  buildEditTab());
        tabs.addTab("Xóa",  buildDeleteTab());

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new TitledBorder("Thông tin hành khách"));
        right.add(tabs, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.60);
        add(split, BorderLayout.CENTER);
    }

    // -------- TABS -------
    private JPanel buildAddTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtTen_Add  = new JTextField();
        txtSDT_Add  = new JTextField();
        txtCCCD_Add = new JTextField();
        cboGT_Add   = new JComboBox<>(new String[]{"Nam","Nữ"});

        int r=0;
        addLine(p, gc, r++, "Tên HK", txtTen_Add);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Add);
        addLine(p, gc, r++, "CCCD", txtCCCD_Add);
        addLine(p, gc, r++, "Giới tính", cboGT_Add);

        btnThem = new JButton("Thêm");
        addFull(p, gc, r, btnThem);
        return p;
    }

    private JPanel buildEditTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtMa_Edit   = new JTextField(); txtMa_Edit.setEnabled(false);
        txtTen_Edit  = new JTextField();
        txtSDT_Edit  = new JTextField();
        txtCCCD_Edit = new JTextField();
        cboGT_Edit   = new JComboBox<>(new String[]{"Nam","Nữ"});

        int r=0;
        addLine(p, gc, r++, "Mã HK", txtMa_Edit);
        addLine(p, gc, r++, "Tên HK", txtTen_Edit);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Edit);
        addLine(p, gc, r++, "CCCD", txtCCCD_Edit);
        addLine(p, gc, r++, "Giới tính", cboGT_Edit);

        btnCapNhat = new JButton("Cập nhật");
        addFull(p, gc, r, btnCapNhat);
        return p;
    }

    private JPanel buildDeleteTab() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = baseGC();

        txtMa_Del   = roField();
        txtTen_Del  = roField();
        txtSDT_Del  = roField();
        txtCCCD_Del = roField();
        txtGT_Del   = roField();

        int r=0;
        addLine(p, gc, r++, "Mã HK", txtMa_Del);
        addLine(p, gc, r++, "Tên HK", txtTen_Del);
        addLine(p, gc, r++, "Số điện thoại", txtSDT_Del);
        addLine(p, gc, r++, "CCCD", txtCCCD_Del);
        addLine(p, gc, r++, "Giới tính", txtGT_Del);

        btnXoa = new JButton("Xóa");
        addFull(p, gc, r, btnXoa);
        return p;
    }

    // ========== Helper layout ==========
    private static GridBagConstraints baseGC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        return gc;
    }
    private static JTextField roField() { JTextField t = new JTextField(); t.setEditable(false); return t; }
    private static void addLine(JPanel p, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx=0; gc.gridy=row; gc.weightx=0; gc.gridwidth=1;
        p.add(new JLabel(label), gc);
        gc.gridx=1; gc.weightx=1;
        p.add(comp, gc);
    }
    private static void addFull(JPanel p, GridBagConstraints gc, int row, JComponent comp) {
        gc.gridx=0; gc.gridy=row; gc.gridwidth=2; gc.weightx=1;
        p.add(comp, gc);
        gc.gridwidth=1;
    }

    // ========== Data ==========
    private void loadData() {
        try {
            model.setRowCount(0);
            List<HanhKhach> list = hkDao.findAll();
            for (HanhKhach hk : list) {
                model.addRow(new Object[]{ hk.getMaHK(), hk.getTenHK(), hk.getSoDienThoai(), hk.getCccd(), hk.getGioiTinh() });
            }
            model.fireTableDataChanged();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bindEvents() {
        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || tbl.getSelectedRow() == -1) return;
            int r = tbl.convertRowIndexToModel(tbl.getSelectedRow());
            String ma  = (String) model.getValueAt(r, 0);
            String ten = (String) model.getValueAt(r, 1);
            String sdt = (String) model.getValueAt(r, 2);
            String cccd= (String) model.getValueAt(r, 3);
            String gt  = (String) model.getValueAt(r, 4);

            txtMa_Edit.setText(ma);
            txtTen_Edit.setText(ten);
            txtSDT_Edit.setText(sdt);
            txtCCCD_Edit.setText(cccd);
            cboGT_Edit.setSelectedItem(gt);

            txtMa_Del.setText(ma);
            txtTen_Del.setText(ten);
            txtSDT_Del.setText(sdt);
            txtCCCD_Del.setText(cccd);
            txtGT_Del.setText(gt);
        });

        // Thêm
        btnThem.addActionListener(e -> {
            try (Connection cn = ConnectDB.getConnection()) {
                String maMoi = hkDao.nextId(cn);
                HanhKhach hk = readFormAdd(maMoi);
                int n = hkDao.insert(hk);
                if (n > 0) {
                    loadData();
                    clearAddForm();
                    JOptionPane.showMessageDialog(this, "Đã thêm hành khách " + maMoi);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không thêm được: " + ex.getMessage());
            }
        });

        // Cập nhật
        btnCapNhat.addActionListener(e -> {
            try {
                HanhKhach hk = readFormEdit();
                int n = hkDao.update(hk);
                if (n > 0) { loadData(); JOptionPane.showMessageDialog(this, "Đã cập nhật!"); }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Không sửa được: " + ex.getMessage());
            }
        });

        // Xóa
        btnXoa.addActionListener(e -> {
            String ma = txtMa_Del.getText().trim();
            if (ma.isEmpty()) { JOptionPane.showMessageDialog(this, "Chọn hành khách để xóa!"); return; }
            int opt = JOptionPane.showConfirmDialog(this, "Xóa hành khách " + ma + "?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    int n = hkDao.deleteById(ma);
                    if (n > 0) { loadData(); clearDeleteFields(); JOptionPane.showMessageDialog(this, "Đã xóa!"); }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Không xóa được: " + ex.getMessage());
                }
            }
        });
    }

    private void applyFilter() {
        String kw = txtSearch.getText().trim();
        sorter.setRowFilter(kw.isEmpty() ? null : RowFilter.regexFilter("(?i)" + kw));
    }

    // ========== Forms ==========
    private HanhKhach readFormAdd(String maHK) {
        String ten  = txtTen_Add.getText().trim();
        String sdt  = txtSDT_Add.getText().trim();
        String cccd = txtCCCD_Add.getText().trim();
        String gt   = (String) cboGT_Add.getSelectedItem();

        if (ten.isEmpty()) throw new IllegalArgumentException("Tên HK không được rỗng");
        if (!sdt.matches("^0\\d{9,10}$")) throw new IllegalArgumentException("SĐT không hợp lệ (10–11 số)");
        if (!cccd.matches("^(\\d{9}|\\d{12})$")) throw new IllegalArgumentException("CCCD/CMND phải 9 hoặc 12 số");

        return new HanhKhach(maHK, ten, sdt, cccd, gt);
    }

    private HanhKhach readFormEdit() {
        String ma  = txtMa_Edit.getText().trim();
        String ten = txtTen_Edit.getText().trim();
        String sdt = txtSDT_Edit.getText().trim();
        String cccd= txtCCCD_Edit.getText().trim();
        String gt  = (String) cboGT_Edit.getSelectedItem();

        if (ma.isEmpty()) throw new IllegalArgumentException("Chưa chọn hành khách để sửa");
        if (ten.isEmpty()) throw new IllegalArgumentException("Tên HK không được rỗng");
        if (!sdt.matches("^0\\d{9,10}$")) throw new IllegalArgumentException("SĐT không hợp lệ (10–11 số)");
        if (!cccd.matches("^(\\d{9}|\\d{12})$")) throw new IllegalArgumentException("CCCD/CMND phải 9 hoặc 12 số");

        return new HanhKhach(ma, ten, sdt, cccd, gt);
    }

    private void clearAddForm() {
        txtTen_Add.setText("");
        txtSDT_Add.setText("");
        txtCCCD_Add.setText("");
        cboGT_Add.setSelectedIndex(0);
        txtTen_Add.requestFocus();
    }

    private void clearDeleteFields() {
        txtMa_Del.setText(""); txtTen_Del.setText(""); txtSDT_Del.setText(""); txtCCCD_Del.setText(""); txtGT_Del.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Quản lý Hành Khách");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(1100, 650);
            f.setLocationRelativeTo(null);
            f.setContentPane(new HanhKhachPanel());
            f.setVisible(true);
        });
    }
}
