package util;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Xuất hoá đơn ra PDF (không cần thư viện ngoài). */
public class InvoicePdfExporter {

    public static void export(String maHD,
                              String tenHK, String sdtHK,
                              String maChuyen, String route, String depart,
                              List<String> seatLines,
                              int soVe, int donGiaInt, BigDecimal vat,
                              int tong,
                              java.awt.Component parent) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("HÓA ĐƠN BÁN VÉ");
        lines.add("----------------------------------------");
        lines.add("Mã HĐ: " + (maHD != null ? maHD : ""));
        lines.add("Khách: " + safe(tenHK) + " - " + safe(sdtHK));
        if (maChuyen != null && !maChuyen.isBlank()) lines.add("Chuyến: " + maChuyen);
        if (route != null && !route.isBlank()) lines.add("Tuyến: " + route);
        if (depart != null && !depart.isBlank()) lines.add("Giờ đi: " + depart);
        lines.add("----------------------------------------");
        if (seatLines != null) {
            lines.addAll(seatLines);
            lines.add("----------------------------------------");
        }
        String vatText = formatVat(vat);
        lines.add("Số vé: " + soVe);
        lines.add("Đơn giá: " + formatVND(donGiaInt));
        lines.add("VAT: " + vatText);
        lines.add("TỔNG: " + formatVND(tong));

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Lưu hoá đơn PDF");
        chooser.setSelectedFile(new File("HoaDon_" + (maHD != null ? maHD : "HD") + ".pdf"));
        int choice = chooser.showSaveDialog(parent);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getParentFile(), file.getName() + ".pdf");
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                SimplePdfWriter.writeSinglePage(lines, fos);
            }
            JOptionPane.showMessageDialog(parent, "Đã lưu PDF: " + file.getAbsolutePath());
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String formatVat(BigDecimal vat) {
        if (vat == null) return "0%";
        BigDecimal hundred = new BigDecimal(100);
        try {
            return vat.multiply(hundred).stripTrailingZeros().toPlainString() + "%";
        } catch (Exception e) {
            return vat.toPlainString();
        }
    }

    /** Định dạng VND đơn giản (x,xxx,xxx ₫) */
    private static String formatVND(int amount) {
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return nf.format(amount) + " ₫";
    }

    /** Xuất hoá đơn ra PDF (file tạm) và tự động mở bằng trình đọc PDF mặc định. */
    public static void exportAndOpen(String maHD,
                              String tenHK, String sdtHK,
                              String maChuyen, String route, String depart,
                              List<String> seatLines,
                              int soVe, int donGiaInt, BigDecimal vat,
                              int tong,
                              java.awt.Component parent) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("HÓA ĐƠN BÁN VÉ");
        lines.add("----------------------------------------");
        lines.add("Mã HĐ: " + (maHD != null ? maHD : ""));
        lines.add("Khách: " + safe(tenHK) + " - " + safe(sdtHK));
        if (maChuyen != null && !maChuyen.isBlank()) lines.add("Chuyến: " + maChuyen);
        if (route != null && !route.isBlank()) lines.add("Tuyến: " + route);
        if (depart != null && !depart.isBlank()) lines.add("Giờ đi: " + depart);
        lines.add("----------------------------------------");
        if (seatLines != null) {
            lines.addAll(seatLines);
            lines.add("----------------------------------------");
        }
        String vatText = formatVat(vat);
        lines.add("Số vé: " + soVe);
        lines.add("Đơn giá: " + formatVND(donGiaInt));
        lines.add("VAT: " + vatText);
        lines.add("TỔNG: " + formatVND(tong));

        java.io.File tmp = java.io.File.createTempFile("HoaDon_" + (maHD != null ? maHD : "HD"), ".pdf");
        tmp.deleteOnExit();
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tmp)) {
            SimplePdfWriter.writeSinglePage(lines, fos);
        }
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tmp);
            } else {
                // Fallback best-effort theo nền tảng
            	String os = System.getProperty("os.name").toLowerCase();
            	if (os.contains("win")) {
            	    // "start" cần một tham số đầu tiên là title -> truyền chuỗi rỗng ""
            	    new ProcessBuilder("cmd", "/c", "start", "", "\"" + tmp.getAbsolutePath() + "\"").start();
            	} else if (os.contains("mac")) {
            	    new ProcessBuilder("open", tmp.getAbsolutePath()).start();
            	} else {
            	    new ProcessBuilder("xdg-open", tmp.getAbsolutePath()).start();
            	}
            }
        } catch (Exception ex) {
            // Thông báo vị trí file tạm nếu không mở được
            javax.swing.JOptionPane.showMessageDialog(parent, "Không mở được trình xem PDF. File đã lưu tạm: " + tmp.getAbsolutePath());
        }
    }


    /** Tạo danh sách dòng nội dung hoá đơn (dùng cho preview hoặc xuất PDF). */
    public static java.util.List<String> buildLines(String maHD,
                              String tenHK, String sdtHK,
                              String maChuyen, String route, String depart,
                              java.util.List<String> seatLines,
                              int soVe, int donGiaInt, java.math.BigDecimal vat,
                              int tong) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        lines.add("HÓA ĐƠN BÁN VÉ");
        lines.add("----------------------------------------");
        lines.add("Mã HĐ: " + (maHD != null ? maHD : ""));
        lines.add("Khách: " + (tenHK == null ? "" : tenHK) + " - " + (sdtHK == null ? "" : sdtHK));
        if (maChuyen != null && !maChuyen.isBlank()) lines.add("Chuyến: " + maChuyen);
        if (route != null && !route.isBlank()) lines.add("Tuyến: " + route);
        if (depart != null && !depart.isBlank()) lines.add("Giờ đi: " + depart);
        lines.add("----------------------------------------");
        if (seatLines != null && !seatLines.isEmpty()) {
            lines.addAll(seatLines);
            lines.add("----------------------------------------");
        }
        String vatText = formatVat(vat);
        lines.add("Số vé: " + soVe);
        lines.add("Đơn giá: " + formatVND(donGiaInt));
        lines.add("VAT: " + vatText);
        lines.add("TỔNG: " + formatVND(tong));
        return lines;
    }

}
