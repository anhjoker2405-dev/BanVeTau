
package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Giao diện "Tìm kiếm vé trả" theo mẫu ảnh (chỉ UI) */
public class TraVe extends JPanel {
    private final JTextField tfMaVe = Ui.field();
    private final JButton btnTraCuu = Ui.primary("Tra Cứu");

    public TraVe(){
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7FB));

        add(Ui.banner("TÌM KIẾM VÉ TRẢ"), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14,14,14,14));

        // Quy định trả vé
        JPanel rules = Ui.card(Ui.infoBox(
                "Vé chỉ được trả trước thời gian khởi hành tối thiểu 8 giờ.",
                "Vé đã trả sẽ không được khôi phục.",
                "Phí trả vé: 10 - 20% giá vé gốc tùy vào thời gian trả vé trước khi tàu khởi hành, " +
                        "số tiền còn lại sẽ được hoàn trực tiếp bằng tiền mặt tại quầy."
        ), "QUY ĐỊNH TRẢ VÉ");
        body.add(rules);
        body.add(Box.createVerticalStrut(16));

        // Ô nhập mã vé + nút
        JPanel searchCard = Ui.card(buildSearchBox(), "");
        body.add(searchCard);

        add(body, BorderLayout.CENTER);
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
}
