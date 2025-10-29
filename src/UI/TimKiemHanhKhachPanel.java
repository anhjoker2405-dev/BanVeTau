package ui;

import dao.HanhKhach_Dao;
import entity.HanhKhach;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.border.Border;

public class TimKiemHanhKhachPanel extends JPanel {
    private final HanhKhach_Dao dao = new HanhKhach_Dao();

    private final JTextField txtMaHK = new JTextField();
    private final JTextField txtTenHK = new JTextField();
    private final JTextField txtSDT = new JTextField();
    private final JTextField txtCCCD = new JTextField();

    private final JButton btnTim = new JButton("Tìm hành khách");
    private final JButton btnLamMoi = new JButton("Làm mới");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Mã HK", "Tên hành khách", "Giới tính", "Số điện thoại", "CCCD"}, 0
    ){
        @Override public boolean isCellEditable(int r, int c){ return false; }
    };
    private final JTable tblKetQua = new JTable(tableModel);

    public TimKiemHanhKhachPanel() {
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

        performSearch();
    }

    private JPanel buildHeaderPanel() {
        CardPanel header = new CardPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Tìm kiếm hành khách");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(33, 56, 110));

        JLabel subtitle = new JLabel("Tra cứu nhanh thông tin khách hàng trong hệ thống");
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

        int row = 0;
        addFilter(panel, gbc, row++, new JLabel("Mã HK:"), txtMaHK);
        addFilter(panel, gbc, row++, new JLabel("Tên hành khách:"), txtTenHK);
        addFilter(panel, gbc, row++, new JLabel("Số điện thoại:"), txtSDT);
        addFilter(panel, gbc, row++, new JLabel("CCCD:"), txtCCCD);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnLamMoi);
        actionPanel.add(btnTim);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        panel.add(actionPanel, gbc);

        btnTim.addActionListener(this::onSearch);
        btnLamMoi.addActionListener(this::onReset);

        return panel;
    }

    private JPanel buildTablePanel() {
        CardPanel panel = new CardPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));

        JLabel title = new JLabel("Kết quả tra cứu");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
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
        txtMaHK.setText("");
        txtTenHK.setText("");
        txtSDT.setText("");
        txtCCCD.setText("");
        performSearch();
    }

    private void performSearch() {
        try {
            List<HanhKhach> list = dao.search(
                    txtMaHK.getText().trim(),
                    txtTenHK.getText().trim(),
                    txtSDT.getText().trim(),
                    txtCCCD.getText().trim()
                );


            tableModel.setRowCount(0);
            for (HanhKhach hk : list) {
                tableModel.addRow(new Object[]{
                        hk.getMaHK(),
                        hk.getTenHK(),
                        hk.getGioiTinh() != null ? hk.getGioiTinh() : "",
                        hk.getSoDienThoai(),
                        hk.getCccd()
                });
            }
            tableModel.fireTableDataChanged();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu: " + ex.getMessage(),
                    "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==== style & helper ====
    private static void addFilter(JPanel panel, GridBagConstraints gbc, int row, JComponent label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        panel.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(field, gbc);
    }

    private void styleInputs() {
        for (JTextField f : new JTextField[]{txtMaHK, txtTenHK, txtSDT, txtCCCD}) styleInputComponent(f);
        styleButton(btnTim, new Color(66, 133, 244), new Color(52, 103, 188), Color.WHITE);
        styleButton(btnLamMoi, new Color(226, 232, 247), new Color(201, 210, 233), new Color(45, 70, 120));
    }

    private void styleTable() {
        tblKetQua.setRowHeight(28);
        tblKetQua.setAutoCreateRowSorter(true);
        tblKetQua.setShowGrid(false);
        tblKetQua.setBackground(Color.WHITE);
        tblKetQua.setForeground(new Color(35, 48, 74));

        JTableHeader header = tblKetQua.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(new Color(227, 235, 255));
        header.setForeground(new Color(54, 76, 125));
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private static void styleButton(JButton button, Color bg, Color hover, Color fg) {
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(10, 24, 10, 24));
        button.getModel().addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed()) button.setBackground(hover.darker());
            else if (m.isRollover()) button.setBackground(hover);
            else button.setBackground(bg);
        });
    }

    private static void styleInputComponent(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 210, 237), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        c.setBackground(Color.WHITE);
        c.setForeground(new Color(35, 48, 74));
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
    
}