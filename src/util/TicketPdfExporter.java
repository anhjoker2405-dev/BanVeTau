package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import entity.TicketPdfInfo;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TicketPdfExporter {

    private static final Rectangle PAGE_SIZE = PageSize.A6; // A6 dọc
    private static final float MARGIN = 24f;
    private static final String FONT_PATH = "C:/Windows/Fonts/times.ttf"; // hoặc "fonts/NotoSans-Regular.ttf"

    public static void export(TicketPdfInfo info, String outPath) throws Exception {
        if (info == null) {
            throw new IllegalArgumentException("Ticket info must not be null");
        }
        String ticketId = safe(info.getMaVe());
        String gaDi = safe(info.getGaDi());
        String gaDen = safe(info.getGaDen());
        String tau = safe(info.getTrainDisplay());
        String ngayDi = safe(info.getNgayDiDisplay());
        String gioDi = safe(info.getGioDiDisplay());
        String toa = safe(info.getCoachDisplay());
        String cho = safe(info.getSeatDisplay());
        String loaiCho = safe(info.getSeatClassDisplay());
        String loaiVe = safe(info.getTenLoaiVe());
        String hoTen = safe(info.getTenHanhKhach());

        String giayTo = safe(info.getCccd());
        String cccd = giayTo.replaceAll("\\D", "");
        if (cccd.length() > 12) {
            cccd = cccd.substring(0, 12);
        }

        BaseFont bf = BaseFont.createFont(FONT_PATH, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fTitleBig = new Font(bf, 12, Font.BOLD);
        Font fTitle    = new Font(bf, 11, Font.BOLD);
        Font fLabelB   = new Font(bf, 9.5f, Font.BOLD);
        Font fLabel    = new Font(bf, 9.5f, Font.NORMAL);
        Font fValue    = new Font(bf, 10, Font.NORMAL);
        Font fValueB   = new Font(bf, 10, Font.BOLD);
        Font fSmall    = new Font(bf, 9, Font.NORMAL);

        Document doc = new Document(PAGE_SIZE, MARGIN, MARGIN, 16f, 18f);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(outPath));
        doc.open();

        // ===== Header =====
        Paragraph p1 = new Paragraph("CÔNG TY CỔ PHẦN VẬN TẢI", fTitleBig); p1.setAlignment(Element.ALIGN_CENTER);
        Paragraph p2 = new Paragraph("ĐƯỜNG SẮT SÀI GÒN",       fTitleBig); p2.setAlignment(Element.ALIGN_CENTER);
        doc.add(p1); doc.add(p2);
        doc.add(Chunk.NEWLINE);

        Paragraph p3 = new Paragraph("THẺ LÊN TÀU HỎA / BOARDING PASS", fTitle);
        p3.setAlignment(Element.ALIGN_CENTER);
        doc.add(p3);
        doc.add(Chunk.NEWLINE);

        // ===== Barcode =====
        PdfContentByte cb = writer.getDirectContent();
        Barcode128 code128 = new Barcode128();
        code128.setCode(ticketId);
        code128.setBarHeight(34f);
        code128.setX(0.8f);
        Image barcode = code128.createImageWithBarcode(cb, null, null);
        barcode.setAlignment(Image.ALIGN_CENTER);
        doc.add(barcode);

        Paragraph pid = new Paragraph("Mã vé/TicketID:  " + ticketId, fLabel);
        pid.setAlignment(Element.ALIGN_CENTER);
        pid.setSpacingBefore(2f); pid.setSpacingAfter(6f);
        doc.add(pid);

        // ===== Bảng chính =====
        PdfPTable tbl = new PdfPTable(new float[]{23, 27, 23, 27});
        tbl.setWidthPercentage(100);

        // ===== Ga đi / Ga đến (giữa trang) =====
        PdfPTable tblGa = new PdfPTable(4);
        tblGa.setWidthPercentage(100);

        PdfPCell c;
        c = labelCell("Ga đi", fLabelB);
        c.setColspan(2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tblGa.addCell(c);

        c = labelCell("Ga đến", fLabelB);
        c.setColspan(2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tblGa.addCell(c);

        c = valueCell(gaDi, fValueB, true);
        c.setColspan(2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tblGa.addCell(c);

        c = valueCell(gaDen, fValueB, true);
        c.setColspan(2);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        tblGa.addCell(c);

        PdfPCell wrapGa = new PdfPCell(tblGa);
        wrapGa.setColspan(4);
        wrapGa.setBorder(Rectangle.NO_BORDER);
        wrapGa.setHorizontalAlignment(Element.ALIGN_CENTER);
        wrapGa.setPaddingBottom(6f);
        tbl.addCell(wrapGa);

        // ===== Tàu/Train =====
        tbl.addCell(labelCell("Tàu/Train:", fLabel));
        tbl.addCell(valueCell(tau, fValue, true));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        // ===== Ngày đi / Giờ đi =====
        tbl.addCell(labelCell("Ngày đi/Date:", fLabel));
        tbl.addCell(valueCell(ngayDi, fValue, true));
        tbl.addCell(labelCell("Giờ đi/Time:", fLabel));
        tbl.addCell(valueCell(gioDi, fValue, true));

        // ===== Toa / Chỗ =====
        tbl.addCell(labelCell("Toa/Coach:", fLabel));
        tbl.addCell(valueCell(toa, fValue, true));
        tbl.addCell(labelCell("Chỗ/Seat:", fLabel));
        tbl.addCell(valueCell(cho, fValue, true));

        // ===== Loại chỗ =====
        tbl.addCell(pairCell("Loại chỗ/Class:", loaiCho, fLabel, fValue, 2));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        // ===== Loại vé =====
        tbl.addCell(pairCell("Loại vé/Ticket:", loaiVe, fLabel, fValue, 2));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        // ===== Họ tên =====
        tbl.addCell(pairCell("Họ tên/Name:", hoTen, fLabel, fValueB, 2));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        // ===== Giấy tờ =====
        tbl.addCell(pairCell("Giấy tờ/Passport:", cccd, fLabel, fValue, 2));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        // ===== Giá =====
        String giaStr = formatCurrency(info.getGiaVe());
        tbl.addCell(labelCell("Giá/Price:", fLabelB));
        tbl.addCell(valueCell(giaStr, fValueB, true));
        tbl.addCell(emptyCell()); tbl.addCell(emptyCell());

        doc.add(tbl);

        // ===== Footer ở đáy trang =====
        String printed = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy").format(LocalDateTime.now());
        Phrase foot = new Phrase("Ngày in/Printed date:  " + printed, fSmall);
        PdfContentByte canvas = writer.getDirectContent();
        ColumnText.showTextAligned(
                canvas,
                Element.ALIGN_CENTER,
                foot,
                (doc.left() + doc.right()) / 2f,
                doc.bottom() - 6f,
                0
        );

        doc.close();
        writer.close();
    }

    // ===== Helpers =====
    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    private static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        nf.setMinimumFractionDigits(0);
        return nf.format(amount).replace('₫', 'đ');
    }
    
    private static PdfPCell labelCell(String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setPadding(4.5f);
        c.setBorder(Rectangle.NO_BORDER);
        c.setNoWrap(true);
        c.setUseAscender(true);
        c.setUseDescender(true);
        return c;
    }

    private static PdfPCell valueCell(String txt, Font f, boolean noWrap) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setPadding(4.5f);
        c.setBorder(Rectangle.NO_BORDER);
        c.setNoWrap(noWrap);
        c.setUseAscender(true);
        c.setUseDescender(true);
        return c;
    }

    // Gộp nhãn + giá trị trong 1 cell, có khoảng trắng rõ
    private static PdfPCell pairCell(String label, String value, Font fLabel, Font fValue, int colspan) {
        Phrase ph = new Phrase();
        ph.add(new Chunk(label, fLabel));
        ph.add(new Chunk("  ", fLabel));
        ph.add(new Chunk(value, fValue));
        PdfPCell c = new PdfPCell(ph);
        c.setPadding(4.5f);
        c.setBorder(Rectangle.NO_BORDER);
        c.setNoWrap(true);
        c.setUseAscender(true);
        c.setUseDescender(true);
        c.setColspan(colspan);
        return c;
    }

    private static PdfPCell emptyCell() {
        PdfPCell c = new PdfPCell(new Phrase(""));
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }
}
