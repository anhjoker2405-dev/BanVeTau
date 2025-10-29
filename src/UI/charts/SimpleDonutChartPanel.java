package ui.charts;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Biểu đồ donut tối giản bằng Java2D (không cần thư viện ngoài) */
public class SimpleDonutChartPanel extends JPanel {

    /** Dữ liệu một lát (label + value) */
    public static class Slice {
        public final String label;
        public final double value;
        public Slice(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }

    private List<Slice> data;

    public SimpleDonutChartPanel(List<Slice> data) {
        this.data = data;
        setPreferredSize(new Dimension(360, 240));
        setBackground(Color.WHITE);
    }

    public void setData(List<Slice> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (data == null || data.isEmpty()) {
            g.setColor(new Color(120,120,120));
            g.drawString("Không có dữ liệu", getWidth()/2 - 40, getHeight()/2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight()) - 20;
        int x = (getWidth() - size) / 2;
        int y = (getHeight() - size) / 2;

        double total = data.stream().mapToDouble(s -> s.value).sum();
        double angle = 0;
        Color[] palette = {
            new Color(101,150,248), new Color(249,168,38), new Color(147,197,114),
            new Color(244,114,182), new Color(16,185,129), new Color(236,72,153)
        };

        int i = 0;
        for (Slice s : data) {
            double sweep = total == 0 ? 0 : (s.value / total) * 360.0;
            g2.setColor(palette[i % palette.length]);
            g2.fillArc(x, y, size, size, (int)Math.round(angle), (int)Math.round(sweep));
            angle += sweep;
            i++;
        }

        int inner = (int)(size * 0.55);
        g2.setColor(getBackground());
        g2.fillOval(x + (size - inner)/2, y + (size - inner)/2, inner, inner);

        int lx = 10, ly = 12;
        i = 0;
        for (Slice s : data) {
            g2.setColor(palette[i % palette.length]);
            g2.fillRect(lx, ly - 10, 12, 12);
            g2.setColor(new Color(60,60,60));
            String pct = total == 0 ? "0%" : String.format("%,.0f%%", (s.value/total)*100);
            g2.drawString(s.label + " (" + pct + ")", lx + 18, ly);
            ly += 16;
            i++;
        }

        g2.dispose();
    }
}
