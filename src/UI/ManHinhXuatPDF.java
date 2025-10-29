package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Màn hình xuất PDF sau khi thanh toán thành công – bản phóng to và có ảnh trung tâm.
 */
public class ManHinhXuatPDF extends JPanel {

    // ==== Màu sắc ====
    private static final Color GREEN_HEADER   = new Color(0x2E7D32);
    private static final Color GREEN_BUTTON   = new Color(0x2E7D32);
    private static final Color BLUE_BUTTON    = new Color(0x1E88E5);
    private static final Color PINK_BUTTON    = new Color(0xF48FB1);
    private static final Color CARD_BG        = new Color(0xECECEC);
    private static final Color CONTENT_BG     = new Color(0xE3F2FD);
    private static final Color BORDER_COLOR   = new Color(0xCFCFCF);

    // ==== Kích thước phóng to ====
    private static final int CARD_W = 680;
    private static final int CARD_H = 600;  // cao hơn
    
    // Đường dẫn ảnh trong classpath (đặt file tại: /img/image_payment.png)
    private static final String PAYMENT_IMG = "/img/image_payment.png";
    


    // ==== Nút & thông tin ====
    private final JButton btnInVe       = new JButton("In Vé");
    private final JButton btnInHoaDon   = new JButton("In Hóa Đơn");
    private final JButton btnBackHome   = new JButton("Trở Về Trang Chủ");
    private final JLabel infoLabel      = new JLabel(" ", SwingConstants.CENTER);

    public ManHinhXuatPDF() {
        setLayout(new GridBagLayout()); // để panel trung tâm nằm giữa
        setBackground(Color.WHITE);

        JPanel card = buildCenterCard();
        add(card, new GridBagConstraints());
    }

    private JPanel buildCenterCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xBDBDBD), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setPreferredSize(new Dimension(CARD_W, CARD_H));

        // ===== Header =====
        JLabel lbHeader = new JLabel("Thanh Toán Thành Công", SwingConstants.CENTER);
        lbHeader.setOpaque(true);
        lbHeader.setBackground(GREEN_HEADER);
        lbHeader.setForeground(Color.WHITE);
        lbHeader.setFont(lbHeader.getFont().deriveFont(Font.BOLD, 22f)); // to hơn
        lbHeader.setBorder(new EmptyBorder(8, 12, 8, 12));
        card.add(lbHeader, BorderLayout.NORTH);

        // ===== Nội dung trung tâm =====
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);

        infoLabel.setOpaque(false);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.PLAIN, 14f));
        infoLabel.setForeground(new Color(0x2F3A5A));
        infoLabel.setBorder(new EmptyBorder(16, 12, 16, 12));
        centerWrapper.add(infoLabel, BorderLayout.NORTH);

        // Panel phần xanh dùng BorderLayout
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CONTENT_BG);
        content.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // Panel vẽ ảnh “phủ kín” phần xanh
        ScaledImagePanel imgPanel = new ScaledImagePanel(PAYMENT_IMG, true); // true = COVER
        content.add(imgPanel, BorderLayout.CENTER); // <-- dùng String, KHÔNG dùng GridBagConstraints

        centerWrapper.add(content, BorderLayout.CENTER);
        card.add(centerWrapper, BorderLayout.CENTER);


        // ===== Hàng nút =====
        JPanel actions = new JPanel();
        actions.setOpaque(true);
        actions.setBackground(CARD_BG);
        actions.setBorder(new EmptyBorder(12, 0, 0, 0));
        actions.add(stylePink(btnInVe));
        actions.add(styleBlue(btnInHoaDon));
        actions.add(styleGreen(btnBackHome));
        card.add(actions, BorderLayout.SOUTH);

        return card;
    }

    // ===== Helpers =====
    private JButton styleBase(JButton b, Color bg) {
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setBorder(new CompoundBorder(
                new LineBorder(bg.darker(), 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final Color base = bg;
        final Color hover = mixColor(base, 0.15f);
        final Color pressed = mixColor(base, -0.1f);
        final Color disabledBg = new Color(0xB0BEC5);
        final Color disabledFg = new Color(0xECEFF1);

        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (!b.isEnabled()) {
                b.setBackground(disabledBg);
                b.setForeground(disabledFg);
            } else if (m.isPressed()) {
                b.setBackground(pressed);
                b.setForeground(Color.WHITE);
            } else if (m.isRollover()) {
                b.setBackground(hover);
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(base);
                b.setForeground(Color.WHITE);
            }
        });
        
        return b;
    }
    private Color mixColor(Color base, float delta) {
        float[] hsb = Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), null);
        hsb[2] = Math.max(0f, Math.min(1f, hsb[2] + delta));
        return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }
    private JButton styleGreen(JButton b) { return styleBase(b, GREEN_BUTTON); }
    private JButton styleBlue(JButton b)  { return styleBase(b, BLUE_BUTTON); }
    private JButton stylePink(JButton b)  { return styleBase(b, PINK_BUTTON); }

    private ImageIcon loadScaledIcon(String resourcePath, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(resourcePath);
            if (url != null) {
                Image raw = new ImageIcon(url).getImage();
                Image scaled = raw.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            } else {
                // fallback: cho phép truyền đường dẫn file tuyệt đối/relative ngoài classpath
                Image raw = Toolkit.getDefaultToolkit().getImage(resourcePath);
                Image scaled = raw.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            return null;
        }
    }

    // ==== Getter để gắn hành vi từ ngoài (giữ nguyên API cũ) ====
    public JButton getBtnInVe() { return btnInVe; }
    public JButton getBtnInHoaDon() { return btnInHoaDon; }
    public JButton getBtnBackHome() { return btnBackHome; }
    
    public void setInfoMessage(String message) {
        String text = (message != null && !message.isBlank()) ? message : " ";
        infoLabel.setText(text);
    }

    // ==== Demo chạy độc lập ====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("ManHinhXuatPDF (Demo)");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ManHinhXuatPDF panel = new ManHinhXuatPDF();
            panel.getBtnInVe().addActionListener(e ->
                    JOptionPane.showMessageDialog(f, "In Vé (PDF)…"));
            panel.getBtnInHoaDon().addActionListener(e ->
                    JOptionPane.showMessageDialog(f, "In Hóa Đơn (PDF)…"));
            panel.getBtnBackHome().addActionListener(e ->
                    JOptionPane.showMessageDialog(f, "Quay về Trang Chủ"));

            f.setContentPane(panel);
            f.setSize(1024, 640); // khung demo rộng rãi
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
    
    // Panel vẽ ảnh scale theo kích thước panel
    private static class ScaledImagePanel extends JPanel {
        private final Image img;
        private final boolean cover; // true: phủ kín (có thể crop). false: vừa khít (có viền trống)

        ScaledImagePanel(String resourcePath, boolean cover) {
            this.cover = cover;
            java.net.URL url = ManHinhXuatPDF.class.getResource(resourcePath);
            if (url != null) {
                img = new ImageIcon(url).getImage();
            } else {
                img = Toolkit.getDefaultToolkit().getImage(resourcePath); // fallback đường dẫn file
            }
            setOpaque(true);
            setBackground(new Color(0xE3F2FD));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;

            int pw = getWidth(), ph = getHeight();
            int iw = img.getWidth(this), ih = img.getHeight(this);
            if (iw <= 0 || ih <= 0) return;

            double sx = pw / (double) iw;
            double sy = ph / (double) ih;
            double s  = cover ? Math.max(sx, sy) : Math.min(sx, sy); // COVER or CONTAIN

            int w = (int) Math.round(iw * s);
            int h = (int) Math.round(ih * s);
            int x = (pw - w) / 2;
            int y = (ph - h) / 2;

            g.drawImage(img, x, y, w, h, this);
        }
    }

}
