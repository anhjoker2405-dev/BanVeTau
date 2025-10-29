package ui;

import dao.TaiKhoan_Dao;
import dao.NhanVien_Dao;
import entity.TaiKhoan;
import entity.NhanVienThongTin;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * QuanLyTaiKhoanPanel — UI card bo góc + primary button + bảng zebra
 * CẬP NHẬT: Không nhập tay Mã NV nữa — chọn từ ComboBox nhân viên chưa có tài khoản.
 *           Txt Mã NV chỉ hiển thị (read-only) để người dùng nhìn thấy.
 */
public class QuanLyTaiKhoanPanel extends JPanel {

    // ===== DAO =====
    private final TaiKhoan_Dao dao = new TaiKhoan_Dao();
    private final NhanVien_Dao nvDao = new NhanVien_Dao();

    // ===== Model/Table =====
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Mã TK", "Tên đăng nhập", "Mã NV", "Loại TK", "Trạng thái"}, 0
    ) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    // ===== Search =====
    private final JTextField txtSearch = new JTextField();

    // ===== Add form =====
    private final JTextField     txtUser   = new JTextField();
    private final JPasswordField txtPass   = new JPasswordField();
    private final JTextField     txtMaNV   = new JTextField(); // hiển thị mã NV (readonly)
    private final JComboBox<NhanVienThongTin> cboNhanVien = new JComboBox<>(); // chọn NV để lấy mã
    private final JComboBox<LoaiTKItem> cboLoaiTK = new JComboBox<>();
    private final JCheckBox      chkActive = new JCheckBox("Kích hoạt");

    // ===== Edit form =====
    private final JTextField txtMaTK_Edit   = new JTextField();
    private final JComboBox<LoaiTKItem> cboLoaiTK_Edit = new JComboBox<>();

    // ===== Buttons =====
    private final PrimaryButton btnRefreshTop = new PrimaryButton("Tải lại");

    public QuanLyTaiKhoanPanel() {
        // Layout + nền gradient
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tắt viền focus rối mắt
        UIManager.put("TextField.focusHighlight", Color.WHITE);
        UIManager.put("ComboBox.focus", new Color(0,0,0,0));
        UIManager.put("TextField.focus", new Color(0,0,0,0));
        UIManager.put("FormattedTextField.focus", new Color(0,0,0,0));
        UIManager.put("PasswordField.focus", new Color(0,0,0,0));
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());

        // Options cho Loại TK
        populateLoaiTKOptions(cboLoaiTK);
        populateLoaiTKOptions(cboLoaiTK_Edit);

        styleInputs();

        // UI
        add(buildFilterCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildBottomCards(), BorderLayout.SOUTH);

        // Events
        wireEvents();

        // load lần đầu
        reload();
        loadNhanVienCombo();
    }

    // ===== UI: Filter Card =====
    private JComponent buildFilterCard() {
        CardPanel card = new CardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(16, 20, 12, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lb = new JLabel("Từ khóa:");
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(lb, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        card.add(txtSearch, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        btnRefreshTop.addActionListener(this::onReload);
        card.add(btnRefreshTop, gbc);
        return card;
    }

    // ===== UI: Table Card =====
    private JComponent buildTableCard() {
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false); sp.getViewport().setOpaque(false);

        TableStyles.applyTimKiemStyle(table, sp);

        // set preferred widths
        int[] widths = {110, 160, 110, 140, 110};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        CardPanel card = new CardPanel(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Danh sách tài khoản");
        title.setFont(getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(45, 70, 120));
        card.add(title, BorderLayout.NORTH);

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(false);
        inner.setBorder(new EmptyBorder(8, 12, 12, 12));
        inner.add(sp, BorderLayout.CENTER);
        card.add(inner, BorderLayout.CENTER);

        return card;
    }

    // ===== UI: Bottom Cards (Add + Edit) =====
    private JComponent buildBottomCards() {
        JPanel wrap = new JPanel(new GridLayout(1, 2, 12, 12));
        wrap.setOpaque(false);
        wrap.add(buildAddCard());
        wrap.add(buildEditCard());
        return wrap;
    }

    private JComponent buildAddCard() {
        CardPanel p = new CardPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,8,6,8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JLabel title = sectionTitle("Thêm tài khoản");
        gc.gridwidth = 2; p.add(title, gc); gc.gridwidth = 1; gc.gridy++;

        p.add(new JLabel("Tên đăng nhập"), gc); gc.gridx=1; p.add(txtUser, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Mật khẩu"), gc); gc.gridx=1; p.add(txtPass, gc);

        // Hàng NV: chọn NV -> hiển thị mã NV
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Nhân viên"), gc); gc.gridx=1; p.add(cboNhanVien, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Mã NV"), gc);
        txtMaNV.setEditable(false);
        gc.gridx=1; p.add(txtMaNV, gc);

        gc.gridx=0; gc.gridy++; p.add(new JLabel("Loại TK"), gc); gc.gridx=1; p.add(cboLoaiTK, gc);
        gc.gridx=1; gc.gridy++; p.add(chkActive, gc);

        gc.gridx=0; gc.gridy++; gc.gridwidth=2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        PrimaryButton btnAdd = new PrimaryButton("Thêm");
        btnAdd.addActionListener(this::onAdd);
        PrimaryButton btnClear = new PrimaryButton("Xóa trắng");
        btnClear.addActionListener(e -> { clearAddForm(); });
        btns.add(btnClear); btns.add(btnAdd);
        p.add(btns, gc);
        return p;
    }

    private void clearAddForm(){
        txtUser.setText(""); txtPass.setText("");
        txtMaNV.setText("");
        cboLoaiTK.setSelectedIndex(0);
        if (cboNhanVien.getItemCount() > 0) cboNhanVien.setSelectedIndex(-1);
        chkActive.setSelected(false);
    }

    private JComponent buildEditCard() {
        CardPanel p = new CardPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(16, 20, 16, 20));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,8,6,8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JLabel title = sectionTitle("Sửa / Xử lý");
        gc.gridwidth = 2; p.add(title, gc); gc.gridwidth = 1; gc.gridy++;

        txtMaTK_Edit.setEditable(false);
        p.add(new JLabel("Mã TK"), gc); gc.gridx=1; p.add(txtMaTK_Edit, gc);
        gc.gridx=0; gc.gridy++; p.add(new JLabel("Loại TK"), gc); gc.gridx=1; p.add(cboLoaiTK_Edit, gc);

        gc.gridx=0; gc.gridy++; gc.gridwidth=2;
        JPanel btns = new JPanel(new GridLayout(2, 3, 10, 10));
        btns.setOpaque(false);

        PrimaryButton btnActivate   = new PrimaryButton("Kích hoạt");   btnActivate.addActionListener(e -> updateStatus(true));
        PrimaryButton btnDeactivate = new PrimaryButton("Vô hiệu");     btnDeactivate.addActionListener(e -> updateStatus(false));
        PrimaryButton btnReset      = new PrimaryButton("Đặt lại MK…"); btnReset.addActionListener(this::onResetPassword);
        PrimaryButton btnUpdateRole = new PrimaryButton("Cập nhật quyền"); btnUpdateRole.addActionListener(this::onUpdateRole);
        PrimaryButton btnDelete     = new PrimaryButton("Xóa");         btnDelete.addActionListener(this::onDelete);
        PrimaryButton btnReload     = new PrimaryButton("Tải lại");     btnReload.addActionListener(this::onReload);

        btns.add(btnActivate); btns.add(btnDeactivate); btns.add(btnReset);
        btns.add(btnUpdateRole); btns.add(btnDelete); btns.add(btnReload);
        p.add(btns, gc);
        return p;
    }

    private JLabel sectionTitle(String text) {
        JLabel t = new JLabel(text);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(45, 70, 120));
        return t;
    }

    // ===== Styles =====
    private void styleInputs() {
        int h = 28;
        Font f = new Font("Segoe UI", Font.PLAIN, 13);
        for (JComponent c : new JComponent[]{txtSearch, txtUser, txtPass, txtMaNV, txtMaTK_Edit}) {
            styleInputComponent(c);
            c.setPreferredSize(new Dimension(260, h));
            c.setFont(f);
        }
        // style cho ComboBox
        for (JComponent c : new JComponent[]{cboLoaiTK, cboLoaiTK_Edit, cboNhanVien}) {
            styleInputComponent(c);
            c.setPreferredSize(new Dimension(260, h));
            c.setFont(f);
        }
        txtSearch.setPreferredSize(new Dimension(360, h));
    }

    private static void styleInputComponent(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196,210,237), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        c.setBackground(Color.WHITE);
        c.setForeground(new Color(35,48,74));
    }

    private void populateLoaiTKOptions(JComboBox<LoaiTKItem> combo) {
        combo.removeAllItems();
        combo.addItem(new LoaiTKItem("LTK-02", "TK_NhanVien")); // Nhân viên
        combo.addItem(new LoaiTKItem("LTK-01", "TK_QuanTri"));  // Quản trị
    }

    private static void selectLoaiTrongCombo(JComboBox<LoaiTKItem> combo, String value) {
        if (value == null) return;
        String v = value.trim();
        for (int i = 0; i < combo.getItemCount(); i++) {
            LoaiTKItem it = combo.getItemAt(i);
            if (it.ma.equalsIgnoreCase(v) || it.ten.equalsIgnoreCase(v)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void loadNhanVienCombo() {
        try {
            DefaultComboBoxModel<NhanVienThongTin> m = new DefaultComboBoxModel<>();
            List<NhanVienThongTin> list = nvDao.findNhanVienChuaCoTaiKhoan();
            for (NhanVienThongTin nv : list) m.addElement(nv);
            cboNhanVien.setModel(m);
            cboNhanVien.setSelectedIndex(list.isEmpty() ? -1 : 0);
            updateMaNVFromCombo();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Không tải được danh sách nhân viên: " + ex.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateMaNVFromCombo() {
        NhanVienThongTin nv = (NhanVienThongTin) cboNhanVien.getSelectedItem();
        txtMaNV.setText(nv == null ? "" : nv.getMaNV());
    }

    // ===== Events / DAO glue =====
    private void wireEvents() {
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String key = txtSearch.getText();
                if (key == null || key.isBlank()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(key)));
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
                    txtMaTK_Edit.setText(String.valueOf(model.getValueAt(modelRow, 0)));
                    String loaiDisplay = String.valueOf(model.getValueAt(modelRow, 3)); // có thể là mã hoặc tên
                    selectLoaiTrongCombo(cboLoaiTK_Edit, loaiDisplay);
                }
            }
        });

        cboNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVienThongTin) {
                    NhanVienThongTin nv = (NhanVienThongTin) value;
                    setText(nv.getMaNV() + " - " + nv.getTenNV());
                }
                return this;
            }
        });
        cboNhanVien.addActionListener(e -> updateMaNVFromCombo());
    }

    private void onAdd(ActionEvent e) {
        String u = txtUser.getText().trim();
        String p = new String(txtPass.getPassword());
        NhanVienThongTin nv = (NhanVienThongTin) cboNhanVien.getSelectedItem();
        String maNV = nv != null ? nv.getMaNV() : "";
        LoaiTKItem item = (LoaiTKItem) cboLoaiTK.getSelectedItem();
        String loaiCode = item != null ? item.ma : null;
        boolean active = chkActive.isSelected();

        if (u.isEmpty() || p.isEmpty() || maNV.isEmpty() || loaiCode == null || loaiCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ thông tin.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            TaiKhoan created = dao.create(u, p, maNV, loaiCode, active);
            JOptionPane.showMessageDialog(this, "Đã tạo tài khoản #" + created.getMaTK());
            reload();
            loadNhanVienCombo(); // NV vừa được gắn TK -> bỏ khỏi danh sách
            clearAddForm();
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
        LoaiTKItem item = (LoaiTKItem) cboLoaiTK_Edit.getSelectedItem();
        String loai = (item != null) ? item.ma : null;
        if (maTK.isEmpty()) { warnSelect(); return; }
        if (loai == null || loai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại tài khoản.", "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
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
            loadNhanVienCombo();
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

    private void onReload(ActionEvent e) { reload(); loadNhanVienCombo(); }

    private void reload() {
        try {
            List<TaiKhoan> list = dao.findAll();
            model.setRowCount(0);
            for (TaiKhoan tk : list) {
                model.addRow(new Object[]{ tk.getMaTK(), tk.getTenDangNhap(), tk.getMaNV(), tk.getLoaiTK(), tk.getTrangThai() });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void warnSelect() {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn một hàng trong bảng.", "Chưa chọn", JOptionPane.WARNING_MESSAGE);
    }

    // ====== Paint gradient background ======
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(242, 247, 255), 0, getHeight(), Color.WHITE));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    // ====== Shared styled components ======
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
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(getText());
            int textHeight = fm.getAscent();
            int x = (w - textWidth) / 2;
            int y = (h + textHeight) / 2 - 2;
            g.drawString(getText(), x, y);
        }
    }

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

    // ====== item cho ComboBox Loại TK ======
    private static class LoaiTKItem {
        final String ma;  // ví dụ LTK-01
        final String ten; // ví dụ TK_QuanTri
        LoaiTKItem(String ma, String ten) { this.ma = ma; this.ten = ten; }
        @Override public String toString() {
            return ten;
        } // hiển thị tên
    }
}
