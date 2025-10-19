
package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/** Giao diện "Tìm kiếm vé đổi" theo mẫu ảnh (chỉ UI) */
public class DoiVe extends JPanel {

    private final JTextField tfMaVeSearch = Ui.field();
    private final JButton btnTimVe = Ui.primary("Tìm Vé");

    // form thông tin vé (bên phải)
    private final Map<String, JTextField> fields = new LinkedHashMap<>();
    private final JButton btnChonVeMoi = Ui.primary("Chọn Vé Mới");

    public DoiVe(){
        setLayout(new BorderLayout());
        setBackground(new Color(0xF5F7FB));

        add(Ui.banner("TÌM KIẾM VÉ ĐỔI"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(14,14,14,14));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0,0,0,12);
        gc.gridx = 0; gc.gridy = 0; gc.fill = GridBagConstraints.BOTH; gc.weightx = 0.42; gc.weighty = 1;

        // Cột trái: Quy định + ô tìm
        body.add(leftColumn(), gc);

        // Cột phải: Thông tin vé
        gc.gridx = 1; gc.weightx = 0.58;
        body.add(rightColumn(), gc);

        add(body, BorderLayout.CENTER);
    }

    private JComponent leftColumn(){
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        JPanel rules = Ui.card(Ui.infoBox(
                "Vé chỉ được Đổi trước thời gian khởi hành tối thiểu 8 giờ.",
                "Vé đổi phải cùng tuyến tàu và ghế mới phải còn chỗ trống.",
                "Phí đổi vé: 10% giá vé gốc, cộng trực tiếp vào giá trị vé mới."
        ), "QUY ĐỊNH ĐỔI VÉ");
        col.add(rules);
        col.add(Box.createVerticalStrut(16));

        // Card tìm vé
        JPanel search = new JPanel();
        search.setOpaque(false);
        search.setLayout(new BoxLayout(search, BoxLayout.Y_AXIS));

        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.setOpaque(false);
        JLabel lb = new JLabel("Mã Vé");
        lb.setBorder(new EmptyBorder(0,2,4,12));
        tfMaVeSearch.setPreferredSize(new Dimension(300, 34));
        line.add(lb); line.add(tfMaVeSearch);
        search.add(line);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnRow.add(btnTimVe);
        search.add(btnRow);

        col.add(Ui.card(search, ""));
        return col;
    }

    private JComponent rightColumn(){
        JPanel col = new JPanel(new BorderLayout());
        col.setOpaque(false);

        JPanel form = new JPanel(new GridLayout(0,2,12,10));
        form.setOpaque(false);
        String[] names = {
                "Mã Vé","Họ Tên","Năm Sinh","Số CCCD",
                "Chuyến Đi","Tàu","Toa","Khoang",
                "Hạng","Ghế","Loại Vé","Tiền Vé"
        };
        for (String n : names){
            JTextField f = Ui.field(); f.setEditable(false);
            fields.put(n, f);
            form.add(new JLabel(n)); form.add(f);
        }

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setOpaque(false);
        btnChonVeMoi.setEnabled(false);
        btnRow.add(btnChonVeMoi);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.CENTER);
        wrap.add(btnRow, BorderLayout.SOUTH);

        col.add(Ui.card(wrap, "THÔNG TIN VÉ"), BorderLayout.CENTER);

        // sự kiện demo
        btnTimVe.addActionListener(e -> fillMockTicket());
        return col;
    }

    private void fillMockTicket(){
        String[][] demo = {
                {"Mã Vé","DV012345"},
                {"Họ Tên","Nguyễn Minh Phúc"},
                {"Năm Sinh","1999"},
                {"Số CCCD","010001002345"},
                {"Chuyến Đi","Sài Gòn → Hà Nội (08/10/2025)"},
                {"Tàu","SE5"}, {"Toa","02"}, {"Khoang","01"},
                {"Hạng","Ngồi mềm điều hòa"}, {"Ghế","18"},
                {"Loại Vé","Vé dành cho học sinh, sinh viên"},
                {"Tiền Vé","320.000₫"}
        };
        for (String[] kv : demo){
            JTextField f = fields.get(kv[0]);
            if (f != null) f.setText(kv[1]);
        }
        btnChonVeMoi.setEnabled(true);
    }
}
