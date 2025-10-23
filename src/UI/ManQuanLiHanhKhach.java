package ui;

import dao.HanhKhach_Dao;
import entity.HanhKhach;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManQuanLiHanhKhach extends JPanel {

    private static final Color BLUE_PRIMARY = new Color(47, 107, 255);
    private static final Color BLUE_SOFT = new Color(230, 240, 255);
    private static final Color BORDER_SOFT = new Color(200, 220, 255);
    private static final Color TEXT_DARK = new Color(30, 35, 45);
    private static final Color TABLE_HEADER_BG = new Color(27, 38, 77);

    private final HanhKhach_Dao hanhKhachDao = new HanhKhach_Dao();

    private final JTextField txtMaHK = new JTextField();
    private final JTextField txtTenHK = new JTextField();
    private final JTextField txtSDT = new JTextField();
    private final JTextField txtCCCD = new JTextField();
    private final JComboBox<String> cboGioiTinh = new JComboBox<>();

    private final DefaultTableModel model = new DefaultTableModel(new Object[]{
            "Mã hành khách", "Tên hành khách", "Số điện thoại", "CCCD", "Giới tính"
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

    private final List<HanhKhach> currentData = new ArrayList<>();
    private final Map<String, String> gioiTinhByTen = new LinkedHashMap<>();

    public ManQuanLiHanhKhach() {
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
                "THÔNG TIN HÀNH KHÁCH",
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

        cboGioiTinh.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());

        p.add(L.apply("Mã hành khách"), l);
        p.add(txtMaHK, f);
        styleField.accept(txtMaHK);
        txtMaHK.setEditable(false);
        txtMaHK.setBackground(new Color(245, 248, 255));

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Tên hành khách"), l);
        p.add(txtTenHK, f);
        styleField.accept(txtTenHK);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Số điện thoại"), l);
        p.add(txtSDT, f);
        styleField.accept(txtSDT);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("CCCD"), l);
        p.add(txtCCCD, f);
        styleField.accept(txtCCCD);

        l.gridy++;
        f.gridy++;
        p.add(L.apply("Giới tính"), l);
        p.add(cboGioiTinh, f);
        styleField.accept(cboGioiTinh);

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
                "DANH SÁCH HÀNH KHÁCH",
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

        int[] widths = {120, 200, 140, 160, 120};
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

        Color hoverBg = BLUE_PRIMARY.darker();
        Color pressBg = hoverBg.darker();

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(BLUE_PRIMARY);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(pressBg);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
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
        btnXoa.addActionListener(e -> handleDelete());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTableSelection();
            }
        });
    }

    private void loadInitialData() {
        loadGioiTinhOptions();
        refreshTable();
        resetFormForNewEntry();
    }

    private void loadGioiTinhOptions() {
        try {
            gioiTinhByTen.clear();
            cboGioiTinh.removeAllItems();
            Map<String, String> gioiTinhs = hanhKhachDao.findAllGioiTinh();
            for (Map.Entry<String, String> entry : gioiTinhs.entrySet()) {
                String ma = entry.getKey();
                String ten = entry.getValue();
                cboGioiTinh.addItem(ten);
                gioiTinhByTen.put(ten, ma);
            }
        } catch (SQLException ex) {
            showError("Không thể tải danh sách giới tính", ex);
        }
    }

    private void refreshTable() {
        model.setRowCount(0);
        currentData.clear();
        try {
            List<HanhKhach> list = hanhKhachDao.findAll();
            currentData.addAll(list);
            for (HanhKhach hk : list) {
                model.addRow(new Object[]{
                        hk.getMaHK(),
                        hk.getTenHK(),
                        hk.getSoDienThoai(),
                        hk.getCccd(),
                        hk.getTenGT()
                });
            }
            model.fireTableDataChanged();
        } catch (SQLException ex) {
            showError("Không thể tải danh sách hành khách", ex);
        }
    }

    private void prepareForNewEntry() {
        txtTenHK.setText("");
        txtSDT.setText("");
        txtCCCD.setText("");
        if (cboGioiTinh.getItemCount() > 0) {
            cboGioiTinh.setSelectedIndex(0);
        }
        generateNextId();
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
            txtMaHK.setText(hanhKhachDao.generateNextId());
        } catch (SQLException ex) {
            txtMaHK.setText("");
            showError("Không thể sinh mã hành khách mới", ex);
        }
    }

    private void onTableSelection() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            prepareForNewEntry();
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String maHK = (String) model.getValueAt(modelRow, 0);
        HanhKhach hk = findPassengerById(maHK);
        if (hk != null) {
            fillForm(hk);
            btnSua.setEnabled(true);
            btnXoa.setEnabled(true);
        }
    }

    private void fillForm(HanhKhach hk) {
        txtMaHK.setText(hk.getMaHK());
        txtTenHK.setText(hk.getTenHK());
        txtSDT.setText(hk.getSoDienThoai());
        txtCCCD.setText(hk.getCccd());
        ensureGenderExistsInCombo(hk.getTenGT(), hk.getMaGT());
    }

    private void ensureGenderExistsInCombo(String display, String maGT) {
        if (display == null || display.isBlank()) {
            cboGioiTinh.setSelectedItem(null);
            return;
        }
        boolean found = false;
        for (int i = 0; i < cboGioiTinh.getItemCount(); i++) {
            if (display.equals(cboGioiTinh.getItemAt(i))) {
                found = true;
                break;
            }
        }
        if (!found) {
            cboGioiTinh.addItem(display);
        }
        if (maGT != null && !maGT.isBlank()) {
            gioiTinhByTen.put(display, maGT);
        }
        cboGioiTinh.setSelectedItem(display);
    }

    private HanhKhach findPassengerById(String maHK) {
        return currentData.stream()
                .filter(item -> item.getMaHK().equals(maHK))
                .findFirst()
                .orElse(null);
    }

    private void handleAdd() {
        if (!validateForm()) {
            return;
        }
        String maHK = txtMaHK.getText().trim();
        String tenHK = txtTenHK.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String maGT = getSelectedGenderCode();
        String tenGT = (String) cboGioiTinh.getSelectedItem();

        HanhKhach hk = new HanhKhach(maHK, tenHK, sdt, cccd, maGT, tenGT);
        try {
            int rows = hanhKhachDao.insert(hk);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Thêm hành khách thành công. Hành khách đã được hiển thị trong danh sách.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                resetFormForNewEntry();
            }
        } catch (SQLException ex) {
            showError("Không thể thêm hành khách", ex);
        }
    }

    private void handleUpdate() {
        if (txtMaHK.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hành khách cần cập nhật", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateForm()) {
            return;
        }
        String maHK = txtMaHK.getText().trim();
        String tenHK = txtTenHK.getText().trim();
        String sdt = txtSDT.getText().trim();
        String cccd = txtCCCD.getText().trim();
        String tenGT = (String) cboGioiTinh.getSelectedItem();
        String maGT = getSelectedGenderCode();
        if ((maGT == null || maGT.isBlank()) && tenGT != null) {
            HanhKhach current = findPassengerById(maHK);
            if (current != null) {
                maGT = current.getMaGT();
            }
        }
        if (maGT == null || maGT.isBlank()) {
            JOptionPane.showMessageDialog(this, "Không xác định được mã giới tính", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HanhKhach hk = new HanhKhach(maHK, tenHK, sdt, cccd, maGT, tenGT);
        try {
            int rows = hanhKhachDao.update(hk);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Cập nhật hành khách thành công", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                resetFormForNewEntry();
            }
        } catch (SQLException ex) {
            showError("Không thể cập nhật hành khách", ex);
        }
    }

    private void handleDelete() {
        String maHK = txtMaHK.getText().trim();
        if (maHK.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hành khách cần xóa", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa hành khách này khỏi danh sách?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            int rows = hanhKhachDao.deleteById(maHK);
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Đã xóa hành khách khỏi danh sách", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshTable();
            resetFormForNewEntry();
        } catch (SQLException ex) {
            showError("Không thể xóa hành khách", ex);
        }
    }

    private boolean validateForm() {
        if (txtTenHK.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên hành khách không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtTenHK.requestFocus();
            return false;
        }
        String sdt = txtSDT.getText().trim();
        if (sdt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }
        if (!sdt.matches("^0\\d{9,10}$")) {
            JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ (10-11 số)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtSDT.requestFocus();
            return false;
        }
        String cccd = txtCCCD.getText().trim();
        if (cccd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "CCCD không được để trống", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }
        if (!cccd.matches("^(\\d{9}|\\d{12})$")) {
            JOptionPane.showMessageDialog(this, "CCCD/CMND phải gồm 9 hoặc 12 số", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtCCCD.requestFocus();
            return false;
        }
        if (cboGioiTinh.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giới tính", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            cboGioiTinh.requestFocus();
            return false;
        }
        return true;
    }

    private String getSelectedGenderCode() {
        String ten = (String) cboGioiTinh.getSelectedItem();
        if (ten == null) {
            return null;
        }
        return gioiTinhByTen.get(ten);
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(this, message + "\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}