package ui.charts;

import entity.MonthlyRevenue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bar chart đơn giản – có grid, nhãn trục Y, value label & tooltip */
public class SimpleBarChartPanel extends JPanel {
    private List<MonthlyRevenue> data;
    private final List<Rectangle> barRects = new ArrayList<>();
    private final List<String> barTips  = new ArrayList<>();

    private static final int TICK_COUNT = 5; // số vạch ngang

    public SimpleBarChartPanel(List<MonthlyRevenue> data) {
        this.data = data;
        setPreferredSize(new Dimension(640, 280));
        setBackground(Color.WHITE);

        // Tooltip theo cột
        setToolTipText(""); // enable tooltip
        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < barRects.size(); i++) {
                    if (barRects.get(i).contains(e.getPoint())) {
                        setToolTipText(barTips.get(i));
                        return;
                    }
                }
                setToolTipText(null);
            }
        });
    }

    public void setData(List<MonthlyRevenue> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (data == null || data.isEmpty()) {
            g2.setColor(new Color(120,120,120));
            g2.drawString("Không có dữ liệu", getWidth()/2 - 40, getHeight()/2);
            g2.dispose();
            return;
        }

        // Tìm max và làm tròn mốc đẹp cho trục Y
        BigDecimal max = BigDecimal.ZERO;
        for (MonthlyRevenue r : data) if (r.getRevenue().compareTo(max) > 0) max = r.getRevenue();
        double niceMax = niceUpper(max.doubleValue());

        // Lề trái động theo nhãn lớn nhất để không bị cắt
        FontMetrics fm = g2.getFontMetrics();
        String yMaxLabel = formatShort((long)Math.round(niceMax));
        int leftPad = Math.max(64, 12 + fm.stringWidth(yMaxLabel));

        // vùng vẽ
        Insets pad = new Insets(28, leftPad, 40, 24);
        int W = Math.max(0, getWidth()  - pad.left - pad.right);
        int H = Math.max(0, getHeight() - pad.top  - pad.bottom);
        int X0 = pad.left;
        int Y0 = getHeight() - pad.bottom;

        // Trục
        g2.setColor(new Color(225,225,225));
        g2.drawLine(X0, Y0, X0 + W, Y0);     // X-axis
        g2.drawLine(X0, Y0, X0, Y0 - H);     // Y-axis

        // Vạch ngang & nhãn Y
        g2.setColor(new Color(235,235,235));
        for (int i = 1; i <= TICK_COUNT; i++) {
            int y = Y0 - i * H / TICK_COUNT;
            g2.drawLine(X0, y, X0 + W, y);  // grid line
        }
        g2.setColor(new Color(120,120,120));
        for (int i = 0; i <= TICK_COUNT; i++) {
            double v = niceMax * i / TICK_COUNT;
            String label = formatShort((long) Math.round(v)); // dùng dạng ngắn
            int y = Y0 - i * H / TICK_COUNT + fm.getAscent()/2;
            int x = X0 - 10 - fm.stringWidth(label);
            g2.drawString(label, x, y);
        }

        // Cột
        barRects.clear(); barTips.clear();
        int n = data.size();
        int barW = Math.max(10, W / Math.max(1, n) - 16);
        int step = Math.max(barW + 16, W / Math.max(1, n));
        int x = X0 + (step - barW) / 2;

        for (int i = 0; i < n; i++) {
            MonthlyRevenue r = data.get(i);
            double ratio = niceMax <= 0 ? 0 : r.getRevenue().doubleValue() / niceMax;
            int barH = (int) Math.round(ratio * (H - 6));
            int yTop = Y0 - barH;

            // cột
            g2.setColor(new Color(101,150,248));
            g2.fillRoundRect(x, yTop, barW, barH, 8, 8);

            // nhãn tháng
            String mLabel = r.getMonthLabel();
            g2.setColor(new Color(80,80,80));
            int lw = fm.stringWidth(mLabel);
            g2.drawString(mLabel, x + barW/2 - lw/2, Y0 + fm.getAscent() + 4);

            // value label (trên cột, hoặc bên trong nếu thiếu chỗ)
            String vLabel = formatCurrency(r.getRevenue().longValue());
            int vw = fm.stringWidth(vLabel);
            int vy = yTop - 6;
            g2.setColor(new Color(60,60,60));
            if (vy - fm.getAscent() < pad.top) {
                // nếu không đủ chỗ phía trên, vẽ trong cột với màu trắng
                g2.setColor(Color.WHITE);
                vy = yTop + fm.getAscent() + 6;
            }
            g2.drawString(vLabel, x + barW/2 - vw/2, vy);

            // tooltip + hitbox
            barRects.add(new Rectangle(x, yTop, barW, barH));
            barTips.add(mLabel + ": " + vLabel);

            x += step;
        }

        g2.dispose();
    }

    /** Làm tròn mốc tối đa thành số "đẹp" (1, 2, 5, 10 * 10^k) */
    private static double niceUpper(double max) {
        if (max <= 0) return 1;
        double exp = Math.floor(Math.log10(max));
        double frac = max / Math.pow(10, exp);
        double niceFrac;
        if (frac <= 1)      niceFrac = 1;
        else if (frac <= 2) niceFrac = 2;
        else if (frac <= 5) niceFrac = 5;
        else                niceFrac = 10;
        return niceFrac * Math.pow(10, exp);
    }

    /** Nhãn ngắn gọn cho trục Y */
    private static String formatShort(long v) {
        if (v >= 1_000_000_000) return String.format(Locale.US, "%.1f tỷ", v / 1_000_000_000.0);
        if (v >= 1_000_000)     return String.format(Locale.US, "%.1f triệu", v / 1_000_000.0);
        if (v >= 1_000)         return String.format(Locale.US, "%.1f nghìn", v / 1_000.0);
        return String.format("%,d đ", v).replace(',', '.');
    }

    /** Số tiền đầy đủ dùng cho nhãn ngay trên cột */
    private static String formatCurrency(long v) {
        String s = String.format("%,d", v).replace(',', '.');
        return s + " đ";
    }
}
