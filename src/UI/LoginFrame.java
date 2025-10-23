package ui;

import dao.TaiKhoan_Dao;
import dao.NhanVien_Dao;
import entity.TaiKhoan;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LoginFrame extends JFrame {
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JCheckBox chkShow = new JCheckBox();
    private final JButton btnLogin = new JButton("Đăng Nhập");
    private final JLabel lblForgot = new JLabel("Quên mật khẩu?");
    private final TaiKhoan_Dao userDAO = new TaiKhoan_Dao();

    private final char defaultEchoChar;

    public LoginFrame() {
        setTitle("Hệ thống bán vé tàu đường sắt Sài Gòn");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        defaultEchoChar = txtPass.getEchoChar();

        // Background
        setContentPane(new BgPanel());

        // LEFT: train graphic
        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(380, 600));
        JLabel train = new JLabel(loadIcon("/img/LOGO.png", 260, 220));
        left.add(train);

        // RIGHT: header + card
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel bigTitle = new JLabel("Hệ thống bán vé tàu đường sắt Sài Gòn");
        bigTitle.setFont(bigTitle.getFont().deriveFont(Font.BOLD, 22f));
        bigTitle.setForeground(new Color(30,30,30));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        header.setOpaque(false);
        header.add(bigTitle);

        gbc.gridx=0; gbc.gridy=0; gbc.anchor=GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(10,0,0,0);
        right.add(header, gbc);

        JPanel card = buildLoginCard();
        gbc.gridy=1; gbc.insets = new Insets(10, 0, 60, 60);
        right.add(card, gbc);

        getContentPane().setLayout(new BorderLayout());
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
    }

    private JPanel buildLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.gray);
        card.setBorder(new EmptyBorder(22, 26, 22, 26));
        card.setPreferredSize(new Dimension(420, 320));

        JLabel title = new JLabel("Đăng Nhập", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        card.add(labeledField("Tên đăng nhập", txtUser, "/img/username.png"));
        card.add(Box.createVerticalStrut(10));
        card.add(labeledField("Mật khẩu", txtPass, "/img/password.png"));

        JPanel util = new JPanel(new BorderLayout());
        util.setOpaque(false);
        chkShow.setOpaque(false);
chkShow.setToolTipText("Hiện/ẩn mật khẩu");
chkShow.setText("Hiện mật khẩu");
// Toggle echo char & đổi nhãn cho rõ ràng
chkShow.addActionListener(e -> {
    boolean show = chkShow.isSelected();
    txtPass.setEchoChar(show ? (char)0 : defaultEchoChar);
    chkShow.setText(show ? "Ẩn mật khẩu" : "Hiện mật khẩu");
});
util.add(chkShow, BorderLayout.EAST);
lblForgot.setForeground(new Color(18, 74, 147));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "Liên hệ quản trị viên để đặt lại mật khẩu.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        util.add(lblForgot, BorderLayout.WEST);
        card.add(Box.createVerticalStrut(6));
        card.add(util);

        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(102,255,153));
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 14f));
        btnLogin.addActionListener(e -> doLogin());
        card.add(Box.createVerticalStrut(14));
        card.add(btnLogin);

        return card;
    }

    private JPanel labeledField(String label, JComponent field, String iconPath) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel lb = new JLabel(label);
        lb.setForeground(Color.WHITE);
        lb.setBorder(new EmptyBorder(0, 4, 4, 4));
        wrap.add(lb);

        JPanel row = new JPanel(new BorderLayout(8,8));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));

        JLabel icon = new JLabel(loadIcon(iconPath, 20, 20));
        row.add(icon, BorderLayout.WEST);

        field.setBorder(BorderFactory.createEmptyBorder(4,6,4,6));
        row.add(field, BorderLayout.CENTER);

        wrap.add(row);
        return wrap;
    }

    private void doLogin() {
        String u = txtUser.getText().trim();
        String p = String.valueOf(txtPass.getPassword());
        if (u.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập tên đăng nhập."); txtUser.requestFocus(); return; }
        if (p.isEmpty()) { JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu."); txtPass.requestFocus(); return; }

        TaiKhoan tk = userDAO.authenticate(u, p);
        if (tk != null) {
            // lấy thông tin nhân viên (nếu cần hiển thị sâu hơn)
            NhanVien_Dao nvDao = new NhanVien_Dao();
            NhanVien nv = nvDao.findByUsername(u); // có thể null nếu chưa mapping
            new MainFrame(tk).setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc tài khoản không hoạt động!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ImageIcon loadIcon(String path, int w, int h) {
        java.net.URL url = getClass().getResource(path);
        if (url == null) return new ImageIcon(new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
        Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }

    static class BgPanel extends JPanel {
        private Image bg;
        public BgPanel() {
            try { bg = new ImageIcon(getClass().getResource("/img/background.jpg")).getImage(); }
            catch (Exception ignore) {}
        }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(255,255,255,160));
                g2.fillRoundRect(8, 8, getWidth()-16, getHeight()-16, 30, 30);
                g2.dispose();
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(0,0,getWidth(),getHeight());
            }
        }
    }
}
