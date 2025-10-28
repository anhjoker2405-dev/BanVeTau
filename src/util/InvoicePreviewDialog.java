package util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import util.UserPrefs;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Hộp thoại xem hoá đơn ở dạng văn bản monospace + các nút lưu PDF. */
public class InvoicePreviewDialog extends JDialog {

    private final JTextArea textArea = new JTextArea(30, 80);
    private final List<String> lines;

    public InvoicePreviewDialog(Window owner, String title, List<String> lines) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.lines = lines;
        buildUI();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        StringBuilder sb = new StringBuilder();
        if (lines != null) {
            for (String line : lines) sb.append(line == null ? "" : line).append('\n');
        }
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);

        JScrollPane sp = new JScrollPane(textArea);
        sp.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(sp, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Lưu PDF...");
        JButton btnSaveDefault = new JButton("Lưu vào thư mục mặc định");
        JButton btnClose = new JButton("Đóng");
        JButton btnChooseDefault = new JButton("Chọn thư mục mặc định...");

        btnSave.setMnemonic(KeyEvent.VK_S);
        btnClose.setMnemonic(KeyEvent.VK_C);

        btnSave.addActionListener(e -> doSaveWithChooser());
        btnSaveDefault.addActionListener(e -> doSaveToDefault());
        btnChooseDefault.addActionListener(e -> chooseDefaultDir());
        btnClose.addActionListener(e -> dispose());

        buttons.add(btnSave);
        buttons.add(btnSaveDefault);
        buttons.add(btnChooseDefault);
        buttons.add(btnClose);
        add(buttons, BorderLayout.SOUTH);

        pack();
    }

    private void doSaveWithChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hoá đơn PDF");
        chooser.setSelectedFile(new File(defaultFileName()));
        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                InvoicePdfExporter.writeLinesToPdf(lines, fos);
                JOptionPane.showMessageDialog(this, "Đã lưu: " + file.getAbsolutePath());
                tryOpen(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lưu PDF thất bại: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void doSaveToDefault() {
        File dir = new File(defaultDir());
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, defaultFileName());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            InvoicePdfExporter.writeLinesToPdf(lines, fos);
            JOptionPane.showMessageDialog(this, "Đã lưu: " + file.getAbsolutePath());
            tryOpen(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lưu PDF thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void tryOpen(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {}
    }

    private static String defaultDir() {
        return UserPrefs.getDefaultSaveDir()
            .map(File::getAbsolutePath)
            .orElseGet(() -> {
                String home = System.getProperty("user.home", ".");
                File downloads = new File(home, "Downloads");
                return downloads.exists() ? downloads.getAbsolutePath() : home;
            });
    }

    private static String defaultFileName() {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "hoa-don_" + ts + ".pdf";
    }

    /** Hiển thị hộp thoại preview. */
    public static void showPreview(Component parent, String title, List<String> lines) {
        Window owner = parent instanceof Window ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);
        InvoicePreviewDialog d = new InvoicePreviewDialog(owner, title, lines);
        d.setVisible(true);
    }
    private void chooseDefaultDir() {
        JFileChooser c = new JFileChooser();
        c.setDialogTitle("Chọn thư mục mặc định");
        c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (c.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dir = c.getSelectedFile();
            UserPrefs.setDefaultSaveDir(dir);
            JOptionPane.showMessageDialog(this,
                    "Đã đặt thư mục mặc định: " + dir.getAbsolutePath());
        }
    }
}