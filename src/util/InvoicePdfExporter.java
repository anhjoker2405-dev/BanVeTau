package util;

import javax.swing.*;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Tạo nội dung hoá đơn ở dạng các dòng văn bản (monospace) và xuất ra PDF.
 * Dùng OpenPDF (thông qua {@link OpenPdfUtil}) để ghi PDF hỗ trợ Unicode.
 */
public class InvoicePdfExporter {

    private InvoicePdfExporter() {}

    /** Mở hộp thoại preview hoá đơn. */
    public static void previewInvoice(Component parent, String title, List<String> lines) {
        InvoicePreviewDialog.showPreview(parent, title, lines);
    }

    /** Ghi các dòng hoá đơn thành 1 trang PDF. */
    public static void exportToPdf(File file, List<String> lines) throws IOException, com.lowagie.text.DocumentException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            writeLinesToPdf(lines, out);
        }
    }

    /** Phương thức cũ trong code gọi nhầm tên. Giữ alias để tương thích. */
    public static List<String> buildLines(
            String maHD,
            String tenKH, String sdtKH,
            String maTau, String tuyen, String thoiGian,
            List<String> seatLines,
            int soVe,
            java.math.BigDecimal donGia, java.math.BigDecimal vat) {
        return buildInvoiceLines(maHD, tenKH, sdtKH, maTau, tuyen, thoiGian, seatLines, soVe, donGia, vat);
    }

    /** Tạo nội dung hoá đơn dưới dạng danh sách dòng. */
    public static List<String> buildInvoiceLines(
            String maHD,
            String tenKH, String sdtKH,
            String maTau, String tuyen, String thoiGian,
            List<String> seatLines,
            int soVe,
            java.math.BigDecimal donGia, java.math.BigDecimal vat) {

        List<String> lines = new ArrayList<>();
        String now = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());

        // Header
        lines.add(center("CÔNG TY ĐƯỜNG SẮT XYZ", 60));
        lines.add(center("HÓA ĐƠN BÁN VÉ", 60));
        lines.add(repeat('-', 60));

        // Thông tin chung
        lines.add("Mã hoá đơn : " + safe(maHD));
        lines.add("Ngày in     : " + now);
        if (tenKH != null || sdtKH != null) {
            lines.add("Khách hàng  : " + safe(tenKH) + (isBlank(sdtKH) ? "" : "  |  " + safe(sdtKH)));
        }
        if (!isBlank(maTau) || !isBlank(tuyen) || !isBlank(thoiGian)) {
            lines.add("Mã tàu      : " + safe(maTau));
            lines.add("Tuyến       : " + safe(tuyen));
            lines.add("Khởi hành   : " + safe(thoiGian));
        }
        lines.add(repeat('-', 60));

        // Danh sách ghế
        lines.add(padRight("Danh sách ghế", 60));
        if (seatLines != null && !seatLines.isEmpty()) {
            for (String s : seatLines) {
                lines.add(" - " + safe(s));
            }
        } else {
            lines.add(" (Trống)");
        }

        // Tính tiền
        lines.add(repeat('-', 60));
        int sl = Math.max(0, soVe);
        java.math.BigDecimal unit = donGia != null ? donGia : java.math.BigDecimal.ZERO;
        java.math.BigDecimal subtotal = unit.multiply(java.math.BigDecimal.valueOf(sl));
        java.math.BigDecimal vatAmount = vat != null ? subtotal.multiply(vat).divide(java.math.BigDecimal.valueOf(100)) : java.math.BigDecimal.ZERO;
        java.math.BigDecimal total = subtotal.add(vatAmount);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        DecimalFormat moneyFmt = new DecimalFormat("#,##0", symbols);

        lines.add(padRight(String.format(java.util.Locale.ROOT, "Số vé x Đơn giá (%d x %s₫)", sl, moneyFmt.format(unit)), 60));
        lines.add("VAT (" + (vat == null ? "0" : vat.stripTrailingZeros().toPlainString()) + "%): " + moneyFmt.format(vatAmount) + "₫");
        lines.add(repeat('-', 60));
        lines.add(padRight("TỔNG CỘNG", 60));
        lines.add(center(moneyFmt.format(total) + "₫", 60));
        lines.add(repeat('=', 60));
        lines.add(center("Cảm ơn Quý khách!", 60));

        return lines;
    }

    // == Utilities ==

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private static String center(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        int left = (width - s.length()) / 2;
        int right = width - s.length() - left;
        StringBuilder sb = new StringBuilder(width);
        for (int i = 0; i < left; i++) sb.append(' ');
        sb.append(s);
        for (int i = 0; i < right; i++) sb.append(' ');
        return sb.toString();
    }

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s.substring(0, n);
        StringBuilder sb = new StringBuilder(n);
        sb.append(s);
        while (sb.length() < n) sb.append(' ');
        return sb.toString();
    }

    private static String padLeft(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s;
        StringBuilder sb = new StringBuilder(n);
        while (sb.length() + s.length() < n) sb.append(' ');
        sb.append(s);
        return sb.toString();
    }

    /** Ghi danh sách dòng ra PDF 1 trang. */
    public static void writeLinesToPdf(List<String> lines, FileOutputStream out)
            throws IOException, com.lowagie.text.DocumentException {
        OpenPdfUtil.writeLinesAsPdfSinglePage(lines, out);
    }
}