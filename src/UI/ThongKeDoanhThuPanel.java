package ui;

import dao.ThongKe_Dao;
import entity.MonthlyRevenue;
import entity.TripCount;
import entity.SeatTypeStat;

import ui.charts.SimpleBarChartPanel;
import ui.charts.SimpleDonutChartPanel;
import ui.charts.SimpleDonutChartPanel.Slice;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Dimension;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

/** Màn hình Doanh thu (không còn bảng) */
public class ThongKeDoanhThuPanel extends JPanel {
    private final JComboBox<String> cbYear;

    // KPI labels
    private final JLabel lblTotal;
    private final JLabel lblGrowth;
    private final JLabel lblMonths;

    // UI parts
    private final JList<TopTripItem> listTopTrips;   // <— custom item
    private final SimpleDonutChartPanel seatTypeChart;
    private final SimpleBarChartPanel chart;

    private final ThongKe_Dao dao = new ThongKe_Dao();

    public ThongKeDoanhThuPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Thống kê Doanh thu", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        int currentYear = LocalDate.now().getYear();
        cbYear = new JComboBox<>(new String[]{"Tất cả",
                Integer.toString(currentYear),
                Integer.toString(currentYear - 1),
                Integer.toString(currentYear - 2)});
        actions.add(new JLabel("Năm:"));
        actions.add(cbYear);
        JButton btnReload = new JButton("Tải dữ liệu");
        actions.add(btnReload);
        header.add(actions, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // KPI
        JPanel kpis = new JPanel(new GridLayout(1, 3, 12, 12));
        kpis.setBorder(new EmptyBorder(12, 0, 12, 0));
        kpis.setOpaque(false);

        lblTotal  = kpiCard("Tổng doanh thu", "0 đ");
        lblGrowth = kpiCard("Tăng trưởng so với tháng trước", "0%");
        lblMonths = kpiCard("Số tháng", "0");

        kpis.add(wrap(lblTotal,  "Tổng doanh thu"));
        kpis.add(wrap(lblGrowth, "Tăng trưởng so với tháng trước"));
        kpis.add(wrap(lblMonths, "Số tháng"));

        add(kpis, BorderLayout.CENTER);

        // Center: chart (trên) + bottom (dưới)
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 1; gc.weighty = 1; gc.fill = GridBagConstraints.BOTH;

        chart = new SimpleBarChartPanel(Collections.emptyList());
        chart.setBorder(new LineBorder(new Color(230,230,230), 1, true));
        center.add(chart, gc);

        // Bottom: Top trips (trái) + SeatType donut (phải)
        gc.gridy = 1; gc.weighty = 1;
        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 12));
        bottom.setOpaque(false);

        // ======= Top trips: custom JList với renderer 2 dòng + chấm màu =======
        JPanel topTripsCard = new JPanel(new BorderLayout());
        topTripsCard.setOpaque(true);
        topTripsCard.setBackground(Color.WHITE);
        topTripsCard.setBorder(new LineBorder(new Color(230,230,230), 1, true));

        JLabel topTripsTitle = new JLabel("Chuyến Đi Được Ưa Chuộng");
        topTripsTitle.setBorder(new EmptyBorder(10,12,0,12));
        topTripsTitle.setForeground(new Color(90,90,90));
        topTripsTitle.setFont(topTripsTitle.getFont().deriveFont(Font.BOLD, 13f));
        topTripsCard.add(topTripsTitle, BorderLayout.NORTH);

        listTopTrips = new JList<>(new DefaultListModel<>());
        listTopTrips.setCellRenderer(new TopTripRenderer());
        listTopTrips.setFixedCellHeight(56);
        listTopTrips.setSelectionBackground(new Color(245, 247, 255));
        listTopTrips.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        listTopTrips.setFocusable(false);
        topTripsCard.add(new JScrollPane(listTopTrips), BorderLayout.CENTER);

        bottom.add(topTripsCard);

        // ======= Donut loại ghế =======
        JPanel seatCard = new JPanel(new BorderLayout());
        seatCard.setOpaque(true);
        seatCard.setBackground(Color.WHITE);
        seatCard.setBorder(new LineBorder(new Color(230,230,230), 1, true));

        JLabel seatTitle = new JLabel("Ghế ngồi phổ biến (tỷ trọng)");
        seatTitle.setBorder(new EmptyBorder(10,12,0,12));
        seatTitle.setForeground(new Color(90,90,90));
        seatTitle.setFont(seatTitle.getFont().deriveFont(Font.BOLD, 13f));
        seatCard.add(seatTitle, BorderLayout.NORTH);

        seatTypeChart = new SimpleDonutChartPanel(Collections.emptyList());
        seatCard.add(seatTypeChart, BorderLayout.CENTER);

        bottom.add(seatCard);

        center.add(bottom, gc);
        add(center, BorderLayout.SOUTH);

        // events
        btnReload.addActionListener(e -> reload());
        cbYear.addActionListener(e -> reload());

        reload();
    }

    private JLabel kpiCard(String caption, String value) {
        JLabel lb = new JLabel(value, SwingConstants.CENTER);
        lb.setFont(lb.getFont().deriveFont(Font.BOLD, 22f));
        lb.putClientProperty("caption", caption);
        return lb;
    }

    private JPanel wrap(JLabel value, String caption) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(new Color(246, 248, 255));
        p.setBorder(new LineBorder(new Color(230,230,230), 1, true));
        JLabel cap = new JLabel(caption);
        cap.setBorder(new EmptyBorder(8,12,0,12));
        cap.setForeground(new Color(90,90,90));
        p.add(cap, BorderLayout.NORTH);
        value.setBorder(new EmptyBorder(8,12,12,12));
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private void reload() {
        Integer year = null;
        String sel = Objects.toString(cbYear.getSelectedItem(), "Tất cả");
        if (!"Tất cả".equals(sel)) {
            try { year = Integer.parseInt(sel); } catch (NumberFormatException ignore) {}
        }

        List<MonthlyRevenue> rows;
        try {
            rows = dao.getRevenueByMonth(year);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu doanh thu: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // KPIs
        BigDecimal total = rows.stream()
                .map(MonthlyRevenue::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText(formatCurrency(total));

        String growth = "-";
        if (rows.size() >= 2) {
            BigDecimal last = rows.get(rows.size() - 1).getRevenue();
            BigDecimal prev = rows.get(rows.size() - 2).getRevenue();
            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                double g = (last.doubleValue() - prev.doubleValue()) / prev.doubleValue() * 100.0;
                growth = String.format(Locale.US, "%+.2f%%", g);
            } else if (last.compareTo(BigDecimal.ZERO) > 0) {
                growth = "+100%";
            } else {
                growth = "0%";
            }
        }
        lblGrowth.setText(growth);

        // “Số tháng”
        lblMonths.setText(Integer.toString(rows.size()));

        // Chart
        chart.setData(rows);

        // Top trips (đổ vào model với màu sắc)
        try {
            List<TripCount> topTrips = dao.getTopTrips(year, 5);
            DefaultListModel<TopTripItem> lm = new DefaultListModel<>();
            Color[] colors = {
                    new Color(190, 190, 190),
                    new Color(142, 124, 195),
                    new Color(20, 184, 166),
                    new Color(234, 179, 8),
                    new Color(59, 130, 246)
            };
            int i = 0;
            for (TripCount t : topTrips) {
                lm.addElement(new TopTripItem(t.getTuyen(), t.getSoVe(), colors[i % colors.length]));
                i++;
            }
            listTopTrips.setModel(lm);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Seat type donut
        try {
            List<SeatTypeStat> seatStats = dao.getSeatTypeStats(year);
            List<Slice> slices = new ArrayList<>();
            for (SeatTypeStat st : seatStats) {
                slices.add(new Slice(st.getTenLoaiGhe(), st.getSoVe()));
            }
            seatTypeChart.setData(slices);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private String formatCurrency(BigDecimal v) {
        long value = v.longValue();
        String s = String.format("%,d", value).replace(',', '.');
        return s + " đ";
    }

    // ====== Model hiển thị top trips + renderer tuỳ biến ======
    static class TopTripItem {
        final String route;
        final int tickets;
        final Color color;
        TopTripItem(String route, int tickets, Color color) {
            this.route = route; this.tickets = tickets; this.color = color;
        }
    }

    static class TopTripRenderer extends JPanel implements ListCellRenderer<TopTripItem> {
        private final JLabel dot = new JLabel();
        private final JLabel title = new JLabel();
        private final JLabel subtitle = new JLabel();

        TopTripRenderer() {
            setLayout(new BorderLayout(10,0));
            setOpaque(true);
            JPanel center = new JPanel(new GridLayout(2,1));
            center.setOpaque(false);

            title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
            subtitle.setFont(subtitle.getFont().deriveFont(12f));
            subtitle.setForeground(new Color(130,130,130));

            center.add(title);
            center.add(subtitle);

            dot.setPreferredSize(new Dimension(18,18));

            add(dot, BorderLayout.WEST);
            add(center, BorderLayout.CENTER);
            setBorder(new EmptyBorder(6,10,6,10));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends TopTripItem> list, TopTripItem value, int index,
                boolean isSelected, boolean cellHasFocus) {

            title.setText(value.route);
            subtitle.setText(value.tickets + " vé đã bán được");
            dot.setIcon(new DotIcon(value.color, 10));

            if (isSelected) {
                setBackground(new Color(245,247,255));
            } else {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }

    /** Vẽ chấm tròn màu dùng cho list item */
    static class DotIcon implements Icon {
        private final Color color;
        private final int d;
        DotIcon(Color color, int diameter) { this.color = color; this.d = diameter; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillOval(x, y, d, d);
            g.setColor(new Color(255,255,255,180));
            g.drawOval(x, y, d, d);
        }
        @Override public int getIconWidth() { return d; }
        @Override public int getIconHeight() { return d; }
    }
}
