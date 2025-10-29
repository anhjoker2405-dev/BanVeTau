package ui;

import dao.RolePermissionDao;
import dao.Ve_Dao;
import entity.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {
    
    // Cards for content
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    {
    content.setOpaque(true);
    content.setBackground(Color.WHITE);      // <<< đổi nền trắng để hết viền xám
    content.setBorder(new EmptyBorder(0,0,0,0));
    }
    
    //CAYVL
    private final BanVe banVePanel = new BanVe();
    private final QuanLyChuyenTau quanLyChuyenTauPanel = new QuanLyChuyenTau();
    

    // Current user
    private final TaiKhoan tk;
    private final RolePermissionDao rolePermissionDao = new RolePermissionDao();
    private final RolePermissionDao.RolePermission permission;

    // Menu items cần ẩn/hiện theo quyền
    private JToggleButton danhMucToggle;
    private JPanel danhMucGroup;
    private JButton menuQuanLyTaiKhoanBtn;
    private JButton menuQuanLyNhanVienBtn;
    private JButton menuQuanLyKhuyenMaiBtn;
    private JButton menuQuanLyChuyenTauBtn;
    private JButton menuQuanLyHanhKhachBtn;

    // ===================== Colors ======================
    // Sidebar nền sáng, chữ tối
    private static final Color SIDEBAR_BG = new Color(248, 250, 252); // #F8FAFC

    // Giữ các màu cũ vì vẫn dùng trong button/toggle con
    private static final Color SIDEBAR_TOP = new Color(27, 38, 77);
    private static final Color SIDEBAR_BOTTOM = new Color(72, 132, 255);
    private static final Color ACCENT_PRIMARY = new Color(96, 140, 255);
    private static final Color ACCENT_SECONDARY = new Color(158, 110, 255);
    private static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private static final Color TEXT_MUTED = new Color(205, 215, 255);

    private static final int NAV_RADIUS = 14;
    // ===================================================


    // Constructor
    public MainFrame(TaiKhoan tk) {
        this.tk = tk;
        this.permission = rolePermissionDao.resolve(tk);
        setTitle("Đường sắt Sài Gòn - Hệ thống bán vé");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar();      // << menu mới nền sáng, không header admin
        JPanel topbar = buildTopbar();
        buildCards();   
        banVePanel.setBookingCompletionListener(() -> quanLyChuyenTauPanel.reloadData());
        

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);             // nhất quán nền
        root.add(sidebar, BorderLayout.WEST);
        root.add(topbar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);
        
        applyPermissions();
        cardLayout.show(content, "home");
    }
    
    private void openBanVe() {
        try {
            Ve_Dao.refreshExpiredTickets(0);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể cập nhật trạng thái vé đã hết hạn.\nVui lòng thử lại." +
                            System.lineSeparator() + ex.getMessage(),
                    "Lỗi cập nhật vé",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        cardLayout.show(content, "banve");
    }

    // Build Topbar
    private JPanel buildTopbar() {
        
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(101, 150, 248));
        top.setBorder(new EmptyBorder(8, 12, 8, 12));

        String role;
        if (permission != null) {
            if (permission.getTenLoaiTK() != null) {
                role = permission.getTenLoaiTK();
            } else if (permission.getMaLoaiTK() != null) {
                role = permission.getMaLoaiTK();
            } else {
                role = "Nhân viên";
            }
        } else {
            role = "Nhân viên";
        }
        String name = tk != null && tk.getTenDangNhap() != null ? tk.getTenDangNhap() : "User";

        JLabel title = new JLabel("Đường sắt Sài Gòn");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 6));
        right.setOpaque(false);

        JLabel lblHello = new JLabel("Chào " + role + " " + name);
        lblHello.setForeground(Color.WHITE);

        JLabel lblDate = new JLabel("Ngày " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        lblDate.setForeground(Color.WHITE);

        right.add(lblDate);
        right.add(lblHello);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }
    // ====================== Sidebar (đã chỉnh) ======================
    private JPanel buildSidebar() {
        // Panel menu nền sáng, phẳng, không gradient, không avatar/admin, không viền
        JPanel side = new JPanel(new BorderLayout());
        side.setOpaque(true);
        side.setBackground(SIDEBAR_BG);
        side.setBorder(null);
        side.setPreferredSize(new Dimension(260, 750));

        // === MENU ITEMS ===
        JPanel menu = new JPanel();
        menu.setOpaque(true);
        menu.setBackground(SIDEBAR_BG);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(12, 16, 24, 16));
        menu.add(Box.createVerticalStrut(8));

        // Nút nhanh trên cùng
        menu.add(makeTopButton("⌂  Trang chủ", () -> cardLayout.show(content, "home")));
        menu.add(Box.createVerticalStrut(6));

        // --------- Danh mục ----------
        danhMucToggle = makeToggle("Danh mục");
        menuQuanLyKhuyenMaiBtn = makeChild("Quản lí khuyến mãi", () -> cardLayout.show(content, "khuyenmai"));
        menuQuanLyTaiKhoanBtn = makeChild("Quản lí tài khoản",  () -> cardLayout.show(content, "quanly_taikhoan"));
        menuQuanLyNhanVienBtn = makeChild("Quản lí Nhân viên",  () -> cardLayout.show(content, "quanly_nhanvien"));
        menuQuanLyChuyenTauBtn = makeChild("Quản lý chuyến tàu",  () -> cardLayout.show(content, "quanly_chuyentau"));
        menuQuanLyHanhKhachBtn = makeChild("Quản lý hành khách",  () -> cardLayout.show(content, "quanly_hành khách"));
        danhMucGroup = groupPanel(
            menuQuanLyKhuyenMaiBtn,
            menuQuanLyTaiKhoanBtn,
            menuQuanLyNhanVienBtn,
            menuQuanLyChuyenTauBtn,
            menuQuanLyHanhKhachBtn
        );
        danhMucToggle.addActionListener(e -> { danhMucGroup.setVisible(danhMucToggle.isSelected()); menu.revalidate(); menu.repaint(); });
        menu.add(danhMucToggle);
        menu.add(danhMucGroup);
        menu.add(Box.createVerticalStrut(6));

        // --------- Xử lý ----------
        JToggleButton btnXuLy = makeToggle("Xử lý");
        JPanel xlGroup = groupPanel(
//            makeChild("Bán vé",  () -> cardLayout.show(content, "banve")),
            makeChild("Bán vé",  this::openBanVe),
            makeChild("Đổi vé",  () -> cardLayout.show(content, "doive")),
            makeChild("Trả vé",  () -> cardLayout.show(content, "trave")),
            makeChild("Xuất hóa đơn",  () -> cardLayout.show(content, "xuathoadon"))
        );
        btnXuLy.addActionListener(e -> { xlGroup.setVisible(btnXuLy.isSelected()); menu.revalidate(); menu.repaint(); });
        menu.add(btnXuLy);
        menu.add(xlGroup);
        menu.add(Box.createVerticalStrut(6));

        // --------- Tìm kiếm ----------
        JToggleButton btnSearch = makeToggle("Tìm kiếm");
        JPanel searchGroup = groupPanel(
            makeChild("Tìm kiếm chuyến tàu",  () -> cardLayout.show(content, "timkiem_chuyendi")),
            makeChild("Tìm kiếm hành khách", () -> cardLayout.show(content, "timkiem_hanhkhach")),
            makeChild("Tìm kiếm nhân viên",  () -> cardLayout.show(content, "timkiem_nhanvien")),
            makeChild("Tìm kiếm hóa đơn",    () -> cardLayout.show(content, "timkiem_hoadon")),
            makeChild("Tìm kiếm khuyến mãi",   () -> cardLayout.show(content, "timkiem_khuyenmai"))
        );
        btnSearch.addActionListener(e -> { searchGroup.setVisible(btnSearch.isSelected()); menu.revalidate(); menu.repaint(); });
        menu.add(btnSearch);
        menu.add(searchGroup);
        menu.add(Box.createVerticalStrut(6));

        // --------- Thống kê ----------
        JToggleButton btnTK = makeToggle("Thống kê");
        JPanel tkGroup = groupPanel(
            makeChild("Doanh thu", () -> cardLayout.show(content, "thongke_doanhthu"))
        );
        btnTK.addActionListener(e -> { tkGroup.setVisible(btnTK.isSelected()); menu.revalidate(); menu.repaint(); });
        menu.add(btnTK);
        menu.add(tkGroup);
        menu.add(Box.createVerticalStrut(6));

        // Logout
        menu.add(makeTopButton("⎋  Đăng xuất", () -> {
            int r = JOptionPane.showConfirmDialog(this, "Đăng xuất?", "Xác nhận",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        }));
        menu.add(Box.createVerticalStrut(20));

        // Scroll (không viền, nền sáng)
        JScrollPane sp = new JScrollPane(menu);
        sp.setBorder(null);
        sp.getViewport().setBackground(SIDEBAR_BG);
        sp.setBackground(SIDEBAR_BG);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        side.add(sp, BorderLayout.CENTER);
        return side;
    }
    // ================================================================

    // Create top buttons
    private JButton makeTopButton(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                float alpha = hover ? 0.75f : 0.55f;
                g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
                g2.setPaint(new GradientPaint(0, 0, Color.WHITE, getWidth(), getHeight(), new Color(240, 244, 255)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), NAV_RADIUS + 6, NAV_RADIUS + 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(14, 20, 14, 18));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 15f));
        b.setForeground(TEXT_PRIMARY);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> onClick.run());

        return b;
    }

    private JToggleButton makeToggle(String text) {
        final String base = text;

        JToggleButton b = new JToggleButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = isSelected();
                boolean hover = getModel().isRollover();
                if (sel) {
                    GradientPaint gp = new GradientPaint(0, 0, ACCENT_PRIMARY, getWidth(), getHeight(), ACCENT_SECONDARY);
                    g2.setPaint(gp);
                } else if (hover) {
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
                    g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 200), getWidth(), getHeight(), new Color(245, 248, 255, 120)));
                } else {
                    g2.setComposite(AlphaComposite.SrcOver.derive(0.45f));
                    g2.setColor(new Color(255, 255, 255));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), NAV_RADIUS, NAV_RADIUS);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override public void setContentAreaFilled(boolean filled) { /* tránh nền mặc định */ }
        };

        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(14, 18, 14, 18));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 15f));

        // Cập nhật mũi tên + màu theo trạng thái
        Runnable update = () -> {
            boolean sel = b.isSelected();
            boolean hover = b.getModel().isRollover();
            b.setText((sel ? "⌄  " : "›  ") + base);
            if (sel) { b.setForeground(Color.WHITE); }
            else if (hover) { b.setForeground(new Color(70, 82, 130)); }
            else { b.setForeground(TEXT_PRIMARY); }
        };

        b.addChangeListener(e -> update.run());
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { update.run(); }
            public void mouseExited (java.awt.event.MouseEvent e) { update.run(); }
            public void mousePressed(java.awt.event.MouseEvent e) { update.run(); }
            public void mouseReleased(java.awt.event.MouseEvent e) { update.run(); }
        });

        b.setSelected(false); // mặc định đóng
        update.run();
        return b;
    }

    private JPanel groupPanel(JButton... btns) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (JButton x : btns) p.add(x);
        p.setVisible(false); // mặc định thu gọn
        p.setBorder(new EmptyBorder(4, 10, 8, 0));
        return p;
    }

    private JButton makeChild(String text, Runnable onClick) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                Color start = hover ? new Color(245, 249, 255) : new Color(255, 255, 255, 235);
                Color end = hover ? new Color(232, 239, 255) : new Color(248, 250, 255, 215);
                g2.setComposite(AlphaComposite.SrcOver.derive(0.95f));
                g2.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
                g2.fillRoundRect(6, 0, getWidth() - 6, getHeight(), NAV_RADIUS, NAV_RADIUS);

                g2.setComposite(AlphaComposite.SrcOver);
                g2.setPaint(new GradientPaint(0, 0, ACCENT_PRIMARY, 0, getHeight(), ACCENT_SECONDARY));
                g2.fillRoundRect(0, getHeight() / 2 - 14, 6, 28, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(10, 26, 10, 16));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 14f));
        b.setForeground(TEXT_PRIMARY);
        b.setIcon(createDotIcon());
        b.setIconTextGap(12);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> onClick.run());

        return b;
    }

    private Icon createDotIcon() {
        return new Icon() {
            @Override public int getIconWidth() { return 10; }
            @Override public int getIconHeight() { return 10; }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(x, y, ACCENT_PRIMARY, x + getIconWidth(), y + getIconHeight(), ACCENT_SECONDARY);
                g2.setPaint(gp);
                g2.fillOval(x, y, getIconWidth(), getIconHeight());
                g2.dispose();
            }
        };
    }

    private void buildCards() {
        content.add(buildHomePanel(), "home");

//        content.add(new BanVe(), "banve");
        content.add(banVePanel, "banve");
        content.add(new DoiVe(), "doive");
        content.add(new TraVe(), "trave");
        content.add(new XuatHoaDonPanel(),      "xuathoadon");

        content.add(new QuanLyKhuyenMaiPanel(),            "khuyenmai");
        content.add(new ManQuanLiHanhKhach(),                 "quanly_hành khách");
        content.add(new QuanLyTaiKhoanPanel(),     "quanly_taikhoan");
        content.add(new QuanLyNhanVien(),            "quanly_nhanvien");
//        content.add(simplePanel("Quản lý chuyến tàu"),     "quanly_chuyentau");
//        content.add(new ManQuanLiChuyenTau(),             "quanly_chuyentau");
        content.add(quanLyChuyenTauPanel,             "quanly_chuyentau");

        content.add(new TimKiemChuyenDiPanel(),           "timkiem_chuyendi");
        content.add(new TimKiemHanhKhachPanel(),   "timkiem_hanhkhach");
        content.add(new TimKiemNhanVienPanel(),    "timkiem_nhanvien");
        content.add(new TimKiemHoaDonPanel(),      "timkiem_hoadon");
        content.add(new TimKiemKhuyenMaiPanel(),    "timkiem_khuyenmai");

        content.add(new ThongKeDoanhThuPanel(),  "thongke_doanhthu");
    }
    
    private void applyPermissions() {
        if (permission == null) {
            return;
        }

        if (permission.hideManagementMenus()) {
            hideMenuItem(menuQuanLyKhuyenMaiBtn);
            hideMenuItem(menuQuanLyTaiKhoanBtn);
            hideMenuItem(menuQuanLyNhanVienBtn);
            hideMenuItem(menuQuanLyChuyenTauBtn);
            hideMenuItem(menuQuanLyHanhKhachBtn);
        }

        updateDanhMucToggleState();
    }

    private void hideMenuItem(AbstractButton button) {
        if (button == null) {
            return;
        }
        button.setVisible(false);
        button.setEnabled(false);
    }

    private void updateDanhMucToggleState() {
        if (danhMucGroup == null || danhMucToggle == null) {
            return;
        }
        boolean hasVisibleChild = false;
        for (Component component : danhMucGroup.getComponents()) {
            if (component.isVisible()) {
                hasVisibleChild = true;
                break;
            }
        }

        if (!hasVisibleChild) {
            danhMucGroup.setVisible(false);
            danhMucToggle.setVisible(false);
            danhMucToggle.setEnabled(false);
        }
    }

    private JPanel buildHomePanel() {
        return new HomePanel();
    }

    /** Panel Trang chủ: nền ảnh, không có card trắng */
    static class HomePanel extends JPanel {
        private final Image bg;

        HomePanel() {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(16, 16, 16, 16));
            Image tmp;
            try {
                tmp = new ImageIcon(getClass().getResource("/img/gatau1.jpeg")).getImage();
            } catch (Exception ex) {
                tmp = null;
            }
            bg = tmp;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setColor(new Color(245, 245, 245));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

//    private JPanel simplePanel(String title) {
//        JPanel p = new JPanel(new BorderLayout());
//        p.setBorder(new EmptyBorder(16, 16, 16, 16));
//
//        JLabel lb = new JLabel(title, SwingConstants.LEFT);
//        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 20f));
//        p.add(lb, BorderLayout.NORTH);
//
//        p.add(new JLabel("Nội dung sẽ được phát triển ở đây...", SwingConstants.CENTER), BorderLayout.CENTER);
//        return p;
//    }
}
