package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import entity.InvoicePdfInfo;
import entity.InvoicePdfItem;

import java.awt.Color;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class HDPdfExporter {

    // A4 dọc và lề thoáng
    private static final Rectangle PAGE_SIZE = PageSize.A4;
    private static final float MARGIN_L = 36f;
    private static final float MARGIN_R = 36f;
    private static final float MARGIN_T = 36f;
    private static final float MARGIN_B = 48f;

    // Đường dẫn font Unicode
    private static final String FONT_PATH = "C:/Windows/Fonts/times.ttf";

    // ====== Model đơn giản ======
    private static final String COMPANY_NAME = "Công ty CP Vận tải Đường sắt Sài Gòn";
    private static final String COMPANY_ADDRESS = "01 Nguyễn Thông, P.9, Q.3, TP.HCM";
    private static final String PAYMENT_METHOD = "Tiền mặt";
    private static final String TITLE = "HÓA ĐƠN GIÁ TRỊ GIA TĂNG";

    public static void export(InvoicePdfInfo invoice, String outPath) throws Exception {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice info must not be null");
        }

        BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font fTitle = new Font(bf, 16, Font.BOLD);
        Font fSub = new Font(bf, 11, Font.NORMAL);
        Font fLabel = new Font(bf, 11, Font.NORMAL);
        Font fBold = new Font(bf, 11, Font.BOLD);
        Font fCell = new Font(bf, 10, Font.NORMAL);
        Font fCellB = new Font(bf, 10, Font.BOLD);
        Font fSmall = new Font(bf, 9, Font.NORMAL);

        Document doc = new Document(PAGE_SIZE, MARGIN_L, MARGIN_R, MARGIN_T, MARGIN_B);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outPath));
        doc.open();

        // ===== Tiêu đề + ngày =====
        LocalDate ngayLap = invoice.getNgayLap() != null ? invoice.getNgayLap() : LocalDate.now();

        Paragraph title = new Paragraph(TITLE, fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);

        String ngayText = "Ngày " + ngayLap.getDayOfMonth() + " tháng " + ngayLap.getMonthValue() + " năm " + ngayLap.getYear();
        Paragraph ngay = new Paragraph(ngayText, fSub);
        ngay.setAlignment(Element.ALIGN_CENTER);
        ngay.setSpacingAfter(10f);
        doc.add(ngay);

        // ===== Khối thông tin người bán =====
        PdfPTable seller = new PdfPTable(new float[]{20, 80});
        seller.setWidthPercentage(100);
        seller.setHorizontalAlignment(Element.ALIGN_LEFT);

        seller.addCell(labelCell("Mã Hóa Đơn:", fLabel));
        seller.addCell(fillLineCell(nullToEmpty(invoice.getMaHoaDon()), fLabel));

        seller.addCell(labelCell("Đơn vị bán hàng:", fLabel));
        seller.addCell(fillLineCell(COMPANY_NAME, fLabel));

        seller.addCell(labelCell("Địa chỉ:", fLabel));
        seller.addCell(fillLineCell(COMPANY_ADDRESS, fLabel));

        seller.addCell(labelCell("Nhân viên lập hóa đơn:", fLabel));
        seller.addCell(fillLineCell(nullToEmpty(invoice.getNhanVienLap()), fLabel));

        seller.addCell(labelCell("Điện thoại Nhân viên:", fLabel));
        seller.addCell(fillLineCell(nullToEmpty(invoice.getDienThoaiNhanVien()), fLabel));

        seller.setSpacingAfter(8f);
        doc.add(seller);

        // ===== Khối thông tin khách + Hình thức thanh toán =====
        PdfPTable buyer = new PdfPTable(new float[]{18, 32, 22, 28});
        buyer.setWidthPercentage(100);

        buyer.addCell(labelCell("Họ tên khách hàng:", fLabel));
        buyer.addCell(fillLineCell(nullToEmpty(invoice.getTenKhachHang()), fLabel));
        buyer.addCell(labelCell("Điện thoại Khách hàng:", fLabel));
        buyer.addCell(fillLineCell(nullToEmpty(invoice.getDienThoaiKhachHang()), fLabel));

        buyer.addCell(labelCell("Hình thức thanh toán:", fLabel));
        PdfPCell pay = fillLineCell(PAYMENT_METHOD, fLabel);
        pay.setColspan(3);
        pay.setPaddingLeft(10f);
        buyer.addCell(pay);

        buyer.setSpacingAfter(8f);
        doc.add(buyer);

        // ===== Bảng chi tiết =====
        PdfPTable tbl = new PdfPTable(new float[]{6, 14, 32, 9, 12, 13, 10, 14});
        tbl.setWidthPercentage(100);

        // Header
        headerCell(tbl, "STT", fCellB);
        headerCell(tbl, "Mã vé", fCellB);
        headerCell(tbl, "Tên dịch vụ", fCellB);
        headerCell(tbl, "Số lượng", fCellB);
        headerCell(tbl, "Đơn giá", fCellB);
        headerCell(tbl, "Thành tiền\nchưa có thuế", fCellB);
        headerCell(tbl, "Thuế\nGTGT", fCellB);
        headerCell(tbl, "TT có thuế", fCellB);

        NumberFormat vnd = currencyFormatter();

        BigDecimal sumChuaThue = BigDecimal.ZERO;
        BigDecimal sumThue = BigDecimal.ZERO;
        BigDecimal sumCoThue = BigDecimal.ZERO;

        List<InvoicePdfItem> items = invoice.getItems();
        for (int i = 0; i < items.size(); i++) {
            InvoicePdfItem it = items.get(i);
            sumChuaThue = sumChuaThue.add(it.getThanhTienChuaThue());
            sumThue = sumThue.add(it.getThueGTGT());
            sumCoThue = sumCoThue.add(it.getThanhTienCoThue());

            bodyCell(tbl, String.valueOf(i + 1), fCell, Element.ALIGN_CENTER);
            bodyCell(tbl, nullToEmpty(it.getMaVe()), fCell, Element.ALIGN_LEFT);
            bodyCell(tbl, nullToEmpty(it.getTenDichVu()), fCell, Element.ALIGN_LEFT);
            bodyCell(tbl, String.valueOf(it.getSoLuong()), fCell, Element.ALIGN_RIGHT);
            bodyCell(tbl, formatCurrency(it.getDonGiaChuaThue(), vnd), fCell, Element.ALIGN_RIGHT);
            bodyCell(tbl, formatCurrency(it.getThanhTienChuaThue(), vnd), fCell, Element.ALIGN_RIGHT);
            bodyCell(tbl, formatCurrency(it.getThueGTGT(), vnd), fCell, Element.ALIGN_RIGHT);
            bodyCell(tbl, formatCurrency(it.getThanhTienCoThue(), vnd), fCell, Element.ALIGN_RIGHT);
        }

        // Dòng tổng cộng (nếu muốn hiển thị)
        PdfPCell totalLab = new PdfPCell(new Phrase("Tổng cộng", fCellB));
        totalLab.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLab.setColspan(5);
        styleBody(totalLab);
        tbl.addCell(totalLab);

        PdfPCell totalChua = new PdfPCell(new Phrase(formatCurrency(sumChuaThue, vnd), fCellB));
        totalChua.setHorizontalAlignment(Element.ALIGN_RIGHT);
        styleBody(totalChua);
        tbl.addCell(totalChua);

        PdfPCell totalThue = new PdfPCell(new Phrase(formatCurrency(sumThue, vnd), fCellB));
        totalThue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        styleBody(totalThue);
        tbl.addCell(totalThue);

        PdfPCell totalCo = new PdfPCell(new Phrase(formatCurrency(sumCoThue, vnd), fCellB));
        totalCo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        styleBody(totalCo);
        tbl.addCell(totalCo);
        
        tbl.setSpacingAfter(6f);
        doc.add(tbl);

        // ===== Ghi chú =====
        PdfPTable sign = new PdfPTable(new float[]{50, 50});
        sign.setWidthPercentage(60);
        sign.setHorizontalAlignment(Element.ALIGN_CENTER);
        sign.setSpacingBefore(12f);

        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.NO_BORDER);
        left.addElement(new Paragraph("Người mua hàng", fBold));
        left.addElement(new Paragraph("(Ký, ghi rõ họ tên)", fSmall));
        left.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.addElement(new Paragraph("Người bán hàng", fBold));
        right.addElement(new Paragraph("(Ký, ghi rõ họ tên)", fSmall));
        right.setHorizontalAlignment(Element.ALIGN_CENTER);

        sign.addCell(left);
        sign.addCell(right);
        doc.add(sign);

        // ===== Footer – Ngày in ghim dưới đáy trang =====
        String printed = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(LocalDateTime.now());
        Phrase foot = new Phrase("(Ngày in/Printed date:)  " + printed, fSmall);
        PdfContentByte canvas = writer.getDirectContent();
        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_CENTER,
                foot,
                (doc.left() + doc.right()) / 2f,
                doc.bottom() - 10f,
                0
        );

        doc.close();
        writer.close();
    }

    // ===== Helpers =====
    private static NumberFormat currencyFormatter() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf;
    }

    private static String formatCurrency(BigDecimal amount, NumberFormat nf) {
        BigDecimal value = amount != null ? amount : BigDecimal.ZERO;
        return nf.format(value).replace('₫', 'đ');
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static PdfPCell labelCell(String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(3f);
        c.setNoWrap(true);
        c.setUseAscender(true);
        c.setUseDescender(true);
        return c;
    }

    private static PdfPCell fillLineCell(String content, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(content == null ? "" : content, f));
        c.setBorder(Rectangle.BOTTOM);
        c.setPadding(3f);
        c.setBorderWidthBottom(0.8f);
        c.setNoWrap(true);          //chống xuống dòng
        c.setUseAscender(true);
        c.setUseDescender(true);
        c.setMinimumHeight(16f);    // cao dòng ổn định
        return c;
    }

    private static void headerCell(PdfPTable t, String text, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5f);
        c.setBackgroundColor(new Color(240, 245, 255));
        c.setBorderWidth(1f);
        t.addCell(c);
    }

    private static void bodyCell(PdfPTable t, String text, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        styleBody(c);
        t.addCell(c);
    }

    private static void styleBody(PdfPCell c) {
        c.setPadding(5f);
        c.setBorderWidthLeft(1f);
        c.setBorderWidthRight(1f);
        c.setBorderWidthTop(0.7f);
        c.setBorderWidthBottom(0.7f);
    }
}
