package ui;

import dao.TraVe_Dao;
import entity.TicketExchangeInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class TraVeThongTinPanel extends JPanel {
    private final TraVe_Dao traVeDao = new TraVe_Dao();
    private final JButton btnXacNhan = Ui.primary("Xác nhận");
    private final JButton btnQuayLai = Ui.primary("Quay lại");

    private final JLabel maVeValue = makeValue("-");
    private final JLabel hanhKhachValue = makeValue("-");
    private final JLabel soDienThoaiValue = makeValue("-");
    private final JLabel tuyenValue = makeValue("-");
    private final JLabel thoiGianKhoiHanhValue = makeValue("-");
    private final JLabel toaGheValue = makeValue("-");
    private final JLabel khoangValue = makeValue("-");
    private final JLabel loaiVeValue = makeValue("-");

    private final JLabel giaVeValue = makeValue("-");
    private final JLabel mucKhauTruValue = makeValue("-");
    private final JLabel phiKhauTruValue = makeValue("-");
    private final JLabel soTienHoanValue = makeValue("-");

    private TicketExchangeInfo currentInfo;
    private Runnable onBack;
    private Runnable onSuccess;

    public TraVeThongTinPanel() {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(24, 24, 24, 24));

        body.add(Ui.card(buildTicketInfo(), "THÔNG TIN VÉ"));
        body.add(Box.createVerticalStrut(16));
        body.add(Ui.card(buildRefundInfo(), "THÔNG TIN HOÀN TIỀN"));

        add(body, BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        btnQuayLai.addActionListener(e -> {
            if (onBack != null) {
                onBack.run();
            }
        });
        btnXacNhan.addActionListener(e -> handleConfirm());
    }

    private JComponent buildTicketInfo() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(makeLabel("Mã vé:"));
        grid.add(maVeValue);

        grid.add(makeLabel("Hành khách:"));
        grid.add(hanhKhachValue);

        grid.add(makeLabel("Số điện thoại:"));
        grid.add(soDienThoaiValue);

        grid.add(makeLabel("Tuyến:"));
        grid.add(tuyenValue);

        grid.add(makeLabel("Khởi hành:"));
        grid.add(thoiGianKhoiHanhValue);

        grid.add(makeLabel("Tàu/Toa/Ghế:"));
        grid.add(toaGheValue);

        grid.add(makeLabel("Khoang:"));
        grid.add(khoangValue);

        grid.add(makeLabel("Loại vé:"));
        grid.add(loaiVeValue);

        return grid;
    }

    private JComponent buildRefundInfo() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 10));
        grid.setOpaque(false);

        grid.add(makeLabel("Giá vé gốc:"));
        grid.add(giaVeValue);

        grid.add(makeLabel("Mức khấu trừ:"));
        grid.add(mucKhauTruValue);

        grid.add(makeLabel("Phí khấu trừ:"));
        grid.add(phiKhauTruValue);

        JLabel hoanLabel = makeLabel("Số tiền hoàn lại:");
        hoanLabel.setFont(hoanLabel.getFont().deriveFont(Font.BOLD));
        grid.add(hoanLabel);

        soTienHoanValue.setFont(soTienHoanValue.getFont().deriveFont(Font.BOLD, 16f));
        soTienHoanValue.setForeground(new Color(0x0B8043));
        grid.add(soTienHoanValue);

        return grid;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        actions.setOpaque(false);
        actions.add(btnQuayLai);
        actions.add(btnXacNhan);
        return actions;
    }

    public void setTicketInfo(TicketExchangeInfo info,
                              String giaVeText,
                              String phiKhauTruText,
                              String soTienHoanText,
                              BigDecimal feeRate,
                              String thoiGianKhoiHanh) {
        this.currentInfo = info;
        maVeValue.setText(safe(info.getMaVe()));
        hanhKhachValue.setText(safe(info.getHoTen()));
        soDienThoaiValue.setText(safe(info.getSoDienThoai()));
        tuyenValue.setText(safe(info.getGaDi()) + " → " + safe(info.getGaDen()));
        thoiGianKhoiHanhValue.setText(thoiGianKhoiHanh);
        toaGheValue.setText("Tàu " + safe(info.getTenTau()) + ", Toa " + info.getSoToa() + ", Ghế " + safe(info.getSoGhe()));
        khoangValue.setText(safe(info.getTenKhoang()));
        loaiVeValue.setText(safe(info.getTenLoaiVe()));

        giaVeValue.setText(giaVeText + " đ");
        mucKhauTruValue.setText(formatPercent(feeRate));
        phiKhauTruValue.setText(phiKhauTruText + " đ");
        soTienHoanValue.setText(soTienHoanText + " đ");

        btnXacNhan.setEnabled(true);
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    private void handleConfirm() {
        if (currentInfo == null) {
            return;
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        int choice = JOptionPane.showConfirmDialog(window,
                "Xác nhận hoàn tiền và hủy vé " + currentInfo.getMaVe() + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        btnXacNhan.setEnabled(false);
        try {
            boolean updated = traVeDao.cancelTicket(currentInfo.getMaVe());
            if (updated) {
                JOptionPane.showMessageDialog(window,
                        "Đã cập nhật trạng thái vé sang 'Đã hủy'.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                JOptionPane.showMessageDialog(window,
                        "Không thể cập nhật trạng thái vé. Vui lòng kiểm tra lại.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                btnXacNhan.setEnabled(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(window,
                    "Không thể cập nhật trạng thái vé: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            btnXacNhan.setEnabled(true);
        }
    }

    private JLabel makeLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(new Color(0x1F2937));
        return lb;
    }

    private JLabel makeValue(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(new Color(0x111827));
        lb.setFont(lb.getFont().deriveFont(Font.BOLD));
        return lb;
    }

    private String formatPercent(BigDecimal rate) {
        BigDecimal percent = rate.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP);
        return percent.toPlainString() + "%";
    }

    private String safe(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }
}