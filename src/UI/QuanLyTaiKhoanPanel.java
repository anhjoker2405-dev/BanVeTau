package ui;

import dao.TaiKhoan_Dao;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Giao diện "Quản lý tài khoản" style giống các panel quản lý khác.
 * Chức năng:
 *  - Xem danh sách tài khoản
 *  - Tìm kiếm theo tên đăng nhập / mã NV
 *  - Thêm tài khoản cho nhân viên
 *  - Kích hoạt / Vô hiệu hóa
 *  - Đặt lại mật khẩu
 *  - Chỉnh sửa quyền (loại TK)
 *  - Xóa tài khoản
 */
public class QuanLyTaiKhoanPanel extends JPanel {

    private final TaiKhoan_Dao dao = new TaiKhoan_Dao();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã TK", "Tên đăng nhập", "Mã NV", "Loại TK", "Trạng thái"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    // Tìm kiếm
    private final JTextField txtSearch = new JTextField();

    // Form thêm
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JTextField txtMaNV = new JTextField();
    private final JTextField txtLoaiTK = new JTextField();
    private final JCheckBox chkActive = new JCheckBox("Kích hoạt");

    // Form sửa
    private final JTextField txtMaTK_Edit = new JTextField();
    private final JTextField txtLoaiTK_Edit = new JTextField();

    public QuanLyTaiKhoanPanel() {
        setLayout(new BorderLayout(12, 12));
        setOpaque(true);

        // ===== NORTH: Thanh tìm kiếm
        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.setBorder(new TitledBorder("Tìm kiếm"));
        north.add(new JLabel("Từ khóa: "), BorderLayout.WEST);
        north.add(txtSearch, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Tải lại");
        btnRefresh.addActionListener(this::onReload);
        north.add(btnRefresh, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);

        // ===== CENTER: Bảng
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new TitledBorder("Danh sách tài khoản"));
        add(sp, BorderLayout.CENTER);

        // ===== SOUTH: 2 hộp — Thêm & Sửa/Xử lý
        JPanel south = new JPanel(new GridLayout(1, 2, 12, 12));
        south.add(buildAddPanel());
        south.add(buildEditPanel());
        add(south, BorderLayout.SOUTH);

        // ===== Sự kiện
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String key = txtSearch.getText();
                if (key == null || key.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(key)));
                }
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                txtMaTK_Edit.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                txtLoaiTK_Edit.setText(String.valueOf(model.getValueAt(modelRow, 3)));
            }
        });

        // load lần đầu
        reload();
    }

    private JPanel buildAddPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Thêm tài khoản"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        p.add(new JLabel("Tên đăng nhập"), gc);
        gc.gridx=1; p.add(txtUser, gc);

        gc.gridx=0; gc.gridy++; p.add(new JLabel("Mật khẩu"), gc);
        gc.gridx=1; p.add(txtPass, gc);

        gc.gridx=0; gc.gridy++; p.add(new JLabel("Mã nhân viên"), gc);
        gc.gridx=1; p.add(txtMaNV, gc);

        gc.gridx=0; gc.gridy++; p.add(new JLabel("Loại TK (mã/tên)"), gc);
        gc.gridx=1; p.add(txtLoaiTK, gc);

        gc.gridx=1; gc.gridy++; p.add(chkActive, gc);

        gc.gridx=0; gc.gridy++; gc.gridwidth=2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Thêm");
        btnAdd.addActionListener(this::onAdd);
        JButton btnClear = new JButton("Xóa trắng");
        btnClear.addActionListener(e -> {
            txtUser.setText(""); txtPass.setText("");
            txtMaNV.setText(""); txtLoaiTK.setText(""); chkActive.setSelected(false);
        });
        btns.add(btnClear);
        btns.add(btnAdd);
        p.add(btns, gc);

        return p;
    }

    private JPanel buildEditPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder("Sửa / Xử lý"));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4,4,4,4);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        txtMaTK_Edit.setEditable(false);

        p.add(new JLabel("Mã TK"), gc);
        gc.gridx=1; p.add(txtMaTK_Edit, gc);

        gc.gridx=0; gc.gridy++; p.add(new JLabel("Loại TK"), gc);
        gc.gridx=1; p.add(txtLoaiTK_Edit, gc);

        gc.gridx=0; gc.gridy++; gc.gridwidth=2;
        JPanel btns = new JPanel(new GridLayout(2, 3, 6, 6));

        JButton btnActivate = new JButton("Kích hoạt");
        btnActivate.addActionListener(e -> updateStatus(true));

        JButton btnDeactivate = new JButton("Vô hiệu");
        btnDeactivate.addActionListener(e -> updateStatus(false));

        JButton btnReset = new JButton("Đặt lại MK…");
        btnReset.addActionListener(this::onResetPassword);

        JButton btnUpdateRole = new JButton("Cập nhật quyền");
        btnUpdateRole.addActionListener(this::onUpdateRole);

        JButton btnDelete = new JButton("Xóa");
        btnDelete.addActionListener(this::onDelete);

        JButton btnReload = new JButton("Tải lại");
        btnReload.addActionListener(this::onReload);

        btns.add(btnActivate);
        btns.add(btnDeactivate);
        btns.add(btnReset);
        btns.add(btnUpdateRole);
        btns.add(btnDelete);
        btns.add(btnReload);

        p.add(btns, gc);

        return p;
    }

    private void onAdd(ActionEvent e) {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        String maNV = txtMaNV.getText().trim();
        String loai = txtLoaiTK.getText().trim();
        boolean active = chkActive.isSelected();

        if (u.isEmpty() || p.isEmpty() || maNV.isEmpty() || loai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            TaiKhoan created = dao.create(u, p, maNV, loai, active);
            JOptionPane.showMessageDialog(this, "Đã tạo tài khoản #" + created.getMaTK());
            reload();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tạo tài khoản: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onResetPassword(ActionEvent e) {
        String maTK = txtMaTK_Edit.getText().trim();
        if (maTK.isEmpty()) { warnSelect(); return; }
        String newPass = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:", "Đặt lại mật khẩu", JOptionPane.PLAIN_MESSAGE);
        if (newPass == null || newPass.isBlank()) return;
        try {
            int n = dao.updatePassword(maTK, newPass.trim());
            if (n > 0) JOptionPane.showMessageDialog(this, "Đã đặt lại mật khẩu.");
            reload();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi đặt lại mật khẩu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onUpdateRole(ActionEvent e) {
        String maTK = txtMaTK_Edit.getText().trim();
        String loai = txtLoaiTK_Edit.getText().trim();
        if (maTK.isEmpty()) { warnSelect(); return; }
        if (loai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập loại tài khoản.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            int n = dao.updateRole(maTK, loai);
            if (n > 0) JOptionPane.showMessageDialog(this, "Đã cập nhật quyền.");
            reload();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật quyền: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete(ActionEvent e) {
        String maTK = txtMaTK_Edit.getText().trim();
        if (maTK.isEmpty()) { warnSelect(); return; }
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa tài khoản #" + maTK + " ?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            int n = dao.delete(maTK);
            if (n > 0) JOptionPane.showMessageDialog(this, "Đã xóa.");
            reload();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatus(boolean active) {
        String maTK = txtMaTK_Edit.getText().trim();
        if (maTK.isEmpty()) { warnSelect(); return; }
        try {
            int n = dao.updateStatus(maTK, active);
            if (n > 0) JOptionPane.showMessageDialog(this, active ? "Đã kích hoạt." : "Đã vô hiệu.");
            reload();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi cập nhật trạng thái: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onReload(ActionEvent e) {
        reload();
    }

    private void reload() {
        try {
            List<TaiKhoan> list = dao.findAll();
            model.setRowCount(0);
            for (TaiKhoan tk : list) {
                model.addRow(new Object[]{
                        tk.getMaTK(), tk.getTenDangNhap(), tk.getMaNV(), tk.getLoaiTK(), tk.getTrangThai()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warnSelect() {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một hàng trong bảng.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
    }
}
