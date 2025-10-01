package UI;

import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFrame extends JFrame {
    // Cards for content
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);

    // Current user
    private final TaiKhoan tk;

    // Colors
    private static final Color BLUE = new Color(47, 107, 255);
    private static final Color BLUE_HOVER = new Color(47, 107, 255, 30);
    private static final Color SIDEBAR_BG = new Color(245, 248, 253);

    private static final int NAV_RADIUS = 12;
    private static final int NAV_GAP = 10;

    
    // Helper: kiểm tra quyền quản lý
    private boolean isManager() {
        String role = (tk != null ? tk.getLoaiTK() : null);
        return role != null && role.equalsIgnoreCase("NhanVienQuanLy");
    }
    
    

    // Constructor
    public MainFrame(TaiKhoan tk) {
        this.tk = tk;
        setTitle("Đường sắt Sài Gòn - Hệ thống bán vé");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        JPanel sidebar = buildSidebar();
        JPanel topbar = buildTopbar();
        buildCards();

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar, BorderLayout.WEST);
        root.add(topbar, BorderLayout.NORTH);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        cardLayout.show(content, "home");
    }

    // Build Topbar
    private JPanel buildTopbar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(101, 150, 248));
        top.setBorder(new EmptyBorder(8, 12, 8, 12));

        String role = tk != null && tk.getLoaiTK() != null ? tk.getLoaiTK() : "Nhân Viên";
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

    // Build Sidebar
    private JPanel buildSidebar() {
    JPanel side = new JPanel(new BorderLayout());
    side.setPreferredSize(new Dimension(260, 750));
    side.setBackground(SIDEBAR_BG);

    JPanel menu = new JPanel();
    menu.setOpaque(false);
    menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

    // 1) Trang chủ
    menu.add(makeTopButton("Trang chủ", () -> cardLayout.show(content, "home")));
    menu.add(Box.createVerticalStrut(6));

    // 2) Vé (xổ xuống)
    JToggleButton btnVe = makeToggle("Vé");
    JPanel veGroup = groupPanel(
            makeChild("Bán vé", () -> cardLayout.show(content, "banve")),
            makeChild("Đổi vé", () -> cardLayout.show(content, "doive")),
            makeChild("Trả vé", () -> cardLayout.show(content, "trave"))
    );
    btnVe.addActionListener(e -> { veGroup.setVisible(btnVe.isSelected()); menu.revalidate(); menu.repaint(); });
    menu.add(btnVe);
    menu.add(veGroup);
    menu.add(Box.createVerticalStrut(6));

    // 3) Tra cứu (xổ xuống)
    JToggleButton btnTra = makeToggle("Tra cứu");
    JPanel traGroup = groupPanel(
            makeChild("Khuyến mãi", () -> cardLayout.show(content, "khuyenmai")),
            makeChild("Danh sách chuyến đi", () -> cardLayout.show(content, "chuyendi")),
            makeChild("Tàu", () -> cardLayout.show(content, "tau")),
            makeChild("Khách hàng", () -> cardLayout.show(content, "khachhang"))
    );
    btnTra.addActionListener(e -> { traGroup.setVisible(btnTra.isSelected()); menu.revalidate(); menu.repaint(); });
    menu.add(btnTra);
    menu.add(traGroup);
    menu.add(Box.createVerticalStrut(6));

    
        if (isManager()) {
            JToggleButton btnQL = makeToggle("Quản lý");
            JPanel qlGroup = groupPanel(
                    makeChild("Quản lý tàu", () -> cardLayout.show(content, "quanly_tau")),
                    makeChild("Quản lý nhân viên", () -> cardLayout.show(content, "quanly_nhanvien")),
                    makeChild("Quản lý khuyến mãi", () -> cardLayout.show(content, "quanly_khuyenmai")),
                    makeChild("Quản lý chuyến đi", () -> cardLayout.show(content, "quanly_chuyendi"))
            );
            btnQL.addActionListener(e -> { qlGroup.setVisible(btnQL.isSelected()); menu.revalidate(); menu.repaint(); });
            menu.add(btnQL);
            menu.add(qlGroup);
            menu.add(Box.createVerticalStrut(NAV_GAP));
        }
// 4) Tài khoản
    menu.add(makeTopButton("Tài khoản", () -> cardLayout.show(content, "taikhoan")));
    menu.add(Box.createVerticalStrut(6));

    // 5) Thống kê
    menu.add(makeTopButton("Thống kê", () -> cardLayout.show(content, "thongke")));
    menu.add(Box.createVerticalStrut(6));

    // 6) Đăng xuất
    menu.add(makeTopButton("Đăng xuất", () -> {
        int r = JOptionPane.showConfirmDialog(this, "Đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }));

    // Cho phép cuộn nếu thiếu chỗ
    JScrollPane sp = new JScrollPane(menu);
    sp.setBorder(null);
    sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    sp.getVerticalScrollBar().setUnitIncrement(16);

    side.add(sp, BorderLayout.CENTER);
    return side;
}
    

    // Create top buttons
    private JButton makeTopButton(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 1, 0, new Color(235, 235, 235)),
                new EmptyBorder(12, 16, 12, 16)
        ));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> onClick.run());

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(BLUE_HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(Color.WHITE);
            }
        });

        return b;
    }
    
    private JToggleButton makeToggle(String text) {
        final String base = text;

        JToggleButton b = new JToggleButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                int r = 12; try { r = NAV_RADIUS; } catch (Throwable ignored) {}
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
                super.paintComponent(g2);
                g2.dispose();
            }
            @Override public void setContentAreaFilled(boolean filled) { /* tránh nền mặc định */ }
        };

        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(12, 16, 12, 16));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.setOpaque(false);
        b.setBackground(Color.WHITE);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Cập nhật mũi tên + màu theo trạng thái
        Runnable update = () -> {
            boolean sel = b.isSelected();
            boolean hover = b.getModel().isRollover();
            b.setText((sel ? "▾ " : "▸ ") + base); // ← mũi tên
            if (sel) { b.setBackground(BLUE); b.setForeground(Color.WHITE); }
            else if (hover) { b.setBackground(BLUE_HOVER); b.setForeground(Color.BLACK); }
            else { b.setBackground(Color.WHITE); b.setForeground(Color.BLACK); }
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
        return p;
    }
    
    private JButton makeChild(String text, Runnable onClick) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(10, 32, 10, 16));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setBackground(new Color(230, 240, 255));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> onClick.run());
        b.addChangeListener(e -> {
            if (b.getModel().isRollover()) b.setBackground(new Color(210, 230, 255));
            else b.setBackground(new Color(230, 240, 255));
        });
        return b;
    }
    // Build the content panels for each section
    private void buildCards() {
        content.add(buildHomePanel(), "home");
        // Vé
        content.add(new BanVe(), "banve");
        content.add(new DoiVe(), "doive");
        content.add(new TraVe(), "trave");
        // Tra cứu
        content.add(simplePanel("Khuyến mãi"), "khuyenmai");
        content.add(simplePanel("Danh sách chuyến đi"), "chuyendi");
        content.add(simplePanel("Tàu"), "tau");
        content.add(simplePanel("Khách hàng"), "khachhang");
        // Tài khoản & Thống kê
        content.add(simplePanel("Tài khoản"), "taikhoan");
        content.add(simplePanel("Thống kê"), "thongke");
        // Quản lý
        content.add(simplePanel("Quản lý tàu"),        "quanly_tau");
        content.add(simplePanel("Quản lý nhân viên"),  "quanly_nhanvien");
        content.add(simplePanel("Quản lý khuyến mãi"), "quanly_khuyenmai");
        content.add(simplePanel("Quản lý chuyến đi"),  "quanly_chuyendi");

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
                tmp = new ImageIcon(getClass().getResource("/img/home.jpg")).getImage();
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

    private JPanel simplePanel(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lb = new JLabel(title, SwingConstants.LEFT);
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 20f));
        p.add(lb, BorderLayout.NORTH);

        p.add(new JLabel("Nội dung sẽ được phát triển ở đây...", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    /** Section xổ xuống trong sidebar */
    private class Section extends JPanel {
        private final JToggleButton header = new JToggleButton();
        private final JPanel items = new JPanel();

        Section(String title) {
            setLayout(new BorderLayout());
            setOpaque(false);

            header.setText(" " + title);
            header.setFocusPainted(false);
            header.setHorizontalAlignment(SwingConstants.LEFT);
            header.setBackground(Color.WHITE);
            header.setOpaque(true);
            header.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(1, 0, 1, 0, new Color(235, 235, 235)),
                    new EmptyBorder(12, 16, 12, 16)
            ));
            header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            header.addActionListener(e -> toggle());
            header.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!header.isSelected()) header.setBackground(BLUE_HOVER);
                }

                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!header.isSelected()) header.setBackground(Color.WHITE);
                }
            });
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            items.setOpaque(false);
            items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
            items.setVisible(false);

            add(header, BorderLayout.NORTH);
            add(items, BorderLayout.CENTER);
            updateSizes();
        }

        void addItem(String text, Runnable onClick) {
            JButton b = new JButton(text);
            b.setFocusPainted(false);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(new EmptyBorder(10, 32, 10, 16));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setBackground(new Color(230, 240, 255));
            b.setOpaque(true);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.addActionListener(e -> onClick.run());
            b.addChangeListener(e -> {
                if (b.getModel().isRollover()) {
                    b.setBackground(new Color(210, 230, 255));
                } else {
                    b.setBackground(new Color(230, 240, 255));
                }
            });
            items.add(b);
            updateSizes();
        }

        void toggle() {
            boolean expand = header.isSelected();
            header.setBackground(expand ? BLUE : Color.WHITE);
            header.setForeground(expand ? Color.WHITE : Color.BLACK);
            items.setVisible(expand);
            updateSizes();
        }

        private void updateSizes() {
            int h = header.getPreferredSize().height;
            if (items.isVisible()) h += items.getPreferredSize().height;
            setPreferredSize(new Dimension(Integer.MAX_VALUE, h + 2));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h + 2));
            revalidate();
        }
    }
}