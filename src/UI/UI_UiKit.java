
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Date;

class Ui {
    static final Color BLUE = new Color(0x2474FF);
    static final Color SOFT_BLUE = new Color(0xE9F2FF);
    static final Color BORDER = new Color(0xC9D6FF);

    static JLabel banner(String text){
        JLabel lb = new JLabel(text, SwingConstants.LEFT);
        lb.setOpaque(true);
        lb.setBackground(new Color(0xD8E8FF));
        lb.setBorder(new EmptyBorder(10,12,10,12));
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 18f));
        return lb;
    }
    static JPanel card(JComponent content, String title){
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(true);
        wrap.setBackground(Color.WHITE);
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(new Color(0xAFC6FF)), title);
        tb.setTitleFont(wrap.getFont().deriveFont(Font.BOLD, 13f));
        wrap.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(10,10,10,10)));
        wrap.add(content, BorderLayout.CENTER);
        return wrap;
    }
    static JPanel infoBox(String... lines){
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SOFT_BLUE);
        p.setBorder(new EmptyBorder(12,12,12,12));
        for (String s: lines){
            JLabel l = new JLabel("• " + s);
            l.setBorder(new EmptyBorder(2,2,2,2));
            p.add(l);
        }
        return p;
    }
    static JButton primary(String text) {
        JButton b = new JButton(text);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI()); // tránh lớp trắng của Nimbus
        b.setBackground(new Color(0x1976D2)); // xanh dương chính
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8,16,8,16));

        // Hiệu ứng hover
        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (!b.isEnabled()) {
                b.setBackground(new Color(0xBBDEFB)); // nền nhạt khi disable
                b.setForeground(new Color(0xE0E0E0));
            } else if (m.isRollover()) {
                b.setBackground(new Color(0x42A5F5)); // sáng hơn khi hover
                b.setForeground(Color.WHITE);
            } else if (m.isPressed()) {
                b.setBackground(new Color(0x1565C0)); // đậm khi nhấn
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(new Color(0x1976D2)); // mặc định
                b.setForeground(Color.WHITE);
            }
        });

        return b;
    }
    static JTextField field(){
        JTextField t = new JTextField();
        t.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                new EmptyBorder(6,8,6,8)
        ));
        return t;
    }
    static JComponent labeled(String name, JComponent c){
        JPanel p = new JPanel(new BorderLayout(6,4));
        p.setOpaque(false);
        JLabel lb = new JLabel(name);
        p.add(lb, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
