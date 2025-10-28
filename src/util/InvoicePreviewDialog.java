
package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/** Hộp thoại xem hoá đơn (không lưu gì mặc định). Người dùng có thể chọn "Lưu PDF..." nếu muốn. */
public class InvoicePreviewDialog extends JDialog {

    public static void showPreview(Component parent, String title, List<String> lines) {
        Frame owner = JOptionPane.getFrameForComponent(parent);
        InvoicePreviewDialog dlg = new InvoicePreviewDialog(owner, title, lines);
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    private InvoicePreviewDialog(Frame owner, String title, List<String> lines) {
        super(owner, title != null ? title : "HÓA ĐƠN", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(520, 600));
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        StringBuilder sb = new StringBuilder();
        if (lines != null) {
            for (String s : lines) {
                sb.append(s == null ? "" : s).append("\n");
            }
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(new EmptyBorder(10,10,10,10));
        getContentPane().add(sp, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnSave = new JButton("Lưu PDF…");
        JButton btnClose = new JButton("Đóng");

        btnSave.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("HoaDon.pdf"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".pdf")) {
                    f = new File(f.getParentFile(), f.getName() + ".pdf");
                }
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    SimplePdfWriter.writeSinglePage(java.util.Arrays.asList(area.getText().split("\\n")), fos);
                    JOptionPane.showMessageDialog(this, "Đã lưu: " + f.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lưu PDF thất bại: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnClose.addActionListener(e -> dispose());

        buttons.add(btnSave);
        buttons.add(btnClose);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        pack();
    }
}
