
package ui;

import dao.DoiVe_Dao;
import entity.TicketExchangeInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Giao diện "Tìm kiếm vé trả" theo mẫu ảnh (chỉ UI) */
public class TraVe extends JPanel {
    private static final String CARD_SEARCH = "search";
    private static final String CARD_DETAIL = "detail";
    
    private final JTextField tfMaVe = Ui.field();
    private final JButton btnTraCuu = Ui.primary("Tra Cứu");
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final TraVeThongTinPanel detailPanel = new TraVeThongTinPanel();

    public TraVe(){
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7FB));
        
        currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        currencyFormat.setMinimumFractionDigits(0);

        cards.setOpaque(false);
        cards.add(buildSearchView(), CARD_SEARCH);
        cards.add(buildDetailView(), CARD_DETAIL);

        add(cards, BorderLayout.CENTER);

        btnTraCuu.addActionListener(e -> handleTraCuu());
        tfMaVe.addActionListener(e -> handleTraCuu());

        detailPanel.setOnBack(this::showSearch);
        detailPanel.setOnSuccess(() -> {
            tfMaVe.setText("");
            showSearch();
        });

        showSearch();
    }

    private JComponent buildSearchView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(Ui.banner("TÌM KIẾM VÉ TRẢ"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14, 14, 14, 14));

        // Quy định trả vé
        JPanel rules = Ui.card(Ui.infoBox(
                "Vé chỉ được trả trước thời gian khởi hành tối thiểu 4 giờ.",
                "Vé đã trả sẽ không được khôi phục.",
                "Phí trả vé: 10 - 20% giá vé gốc tùy vào thời gian trả vé trước khi tàu khởi hành, " +
                        "số tiền còn lại sẽ được hoàn trực tiếp bằng tiền mặt tại quầy."
        ), "QUY ĐỊNH TRẢ VÉ");
        body.add(rules);
        body.add(Box.createVerticalStrut(16));

        // Ô nhập mã vé + nút
        JPanel searchCard = Ui.card(buildSearchBox(), "");
        body.add(searchCard);

        panel.add(body, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildDetailView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        panel.add(Ui.banner("XÁC NHẬN TRẢ VÉ"), BorderLayout.NORTH);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(detailPanel, BorderLayout.CENTER);
        panel.add(wrap, BorderLayout.CENTER);

        return panel;
    }

    private void showSearch() {
        cardLayout.show(cards, CARD_SEARCH);
    }

    private void showDetail() {
        cardLayout.show(cards, CARD_DETAIL);
    }

    private JComponent buildSearchBox(){
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.setOpaque(false);
        JLabel lb = new JLabel("Mã Vé");
        lb.setBorder(new EmptyBorder(0,2,4,12));
        tfMaVe.setPreferredSize(new Dimension(420, 34));
        line.add(lb); line.add(tfMaVe);
        p.add(line);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(btnTraCuu);
        p.add(btnRow);
        return p;
    }
    
    private void handleTraCuu() {
        String maVe = tfMaVe.getText();
        if (maVe == null || maVe.isBlank()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mã vé cần tra cứu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        TicketExchangeInfo info;
        try {
            info = new DoiVe_Dao().findByMaVe(maVe.trim());
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể tra cứu vé: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (info == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy vé với mã " + maVe + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!"Đã bán".equalsIgnoreCase(info.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Vé không ở trạng thái 'Đã bán' nên không thể thực hiện trả vé.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        LocalDateTime thoiGianKhoiHanh = info.getThoiGianKhoiHanh();
        if (thoiGianKhoiHanh == null) {
            JOptionPane.showMessageDialog(this,
                    "Không xác định được thời gian khởi hành của vé, vui lòng kiểm tra lại.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, thoiGianKhoiHanh);
        long minutesUntilDeparture = duration.toMinutes();
        if (minutesUntilDeparture < 240) {
            JOptionPane.showMessageDialog(this,
                    "Vé không đủ điều kiện trả (phải trước giờ khởi hành ít nhất 4 giờ).",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal feeRate = minutesUntilDeparture >= 24 * 60 ? BigDecimal.valueOf(0.10) : BigDecimal.valueOf(0.20);
        BigDecimal giaVe = info.getGiaVe() != null ? info.getGiaVe() : BigDecimal.ZERO;
        BigDecimal phiKhauTru = giaVe.multiply(feeRate).setScale(0, RoundingMode.HALF_UP);
        BigDecimal soTienHoan = giaVe.subtract(phiKhauTru);
        if (soTienHoan.compareTo(BigDecimal.ZERO) < 0) {
            soTienHoan = BigDecimal.ZERO;
        }

        String thoiGianText = thoiGianKhoiHanh.format(dateTimeFormatter);
        detailPanel.setTicketInfo(info,
                currencyFormat.format(giaVe),
                currencyFormat.format(phiKhauTru),
                currencyFormat.format(soTienHoan),
                feeRate,
                thoiGianText);
        showDetail();
    }
}
