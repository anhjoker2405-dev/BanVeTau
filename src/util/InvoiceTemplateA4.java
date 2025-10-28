
package util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;

/** Mẫu PDF A4 giống bố cục: tiêu đề, địa chỉ, khối KH, khối chuyến đi,
 *  bảng Ghế–Thành tiền, bảng Chi tiết thanh toán, Tổng tiền to. */
public class InvoiceTemplateA4 {

    private static Font font(float size, int style) throws Exception {
        Font f = OpenPdfUtil.createUnicodeFont(size);
        f.setStyle(style);
        return f;
    }

    public static void writeInvoice(
            String title,
            String diaChi1, String diaChi2,
            String kh_ten, String kh_sdt, String kh_cccd,
            String tau, String chuyen, String ngayGioDi, String ngayGioDen,
            List<String[]> gheThanhTien, // mỗi phần tử: [mô tả ghế, tiền]
            BigDecimal tongTienVe, BigDecimal vat, BigDecimal khuyenMai,
            BigDecimal tongCuoi,
            OutputStream out
    ) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font h1    = font(18, Font.BOLD);
        Font small = font(10.5f, Font.NORMAL);
        Font label = font(12, Font.BOLD);
        Font text  = font(12, Font.NORMAL);
        Font total = font(14, Font.BOLD);

        // Tiêu đề
        Paragraph p = new Paragraph(title == null ? "Hóa Đơn" : title, h1);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(Chunk.NEWLINE);

        if (diaChi1 != null) {
            p = new Paragraph(diaChi1, small); p.setAlignment(Element.ALIGN_CENTER); doc.add(p);
        }
        if (diaChi2 != null) {
            p = new Paragraph(diaChi2, small); p.setAlignment(Element.ALIGN_CENTER); doc.add(p);
        }
        doc.add(new Paragraph("────────────────────────────────────────────────", small));

        // Thông tin khách hàng
        doc.add(new Paragraph("Thông tin khách hàng", label));
        PdfPTable tblKH = new PdfPTable(2); tblKH.setWidthPercentage(100); tblKH.setSpacingBefore(6);
        tblKH.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        tblKH.addCell(new Phrase("Tên:", text));       tblKH.addCell(new Phrase(nullToEmpty(kh_ten), text));
        tblKH.addCell(new Phrase("SĐT:", text));       tblKH.addCell(new Phrase(nullToEmpty(kh_sdt), text));
        tblKH.addCell(new Phrase("Số CCCD:", text));   tblKH.addCell(new Phrase(nullToEmpty(kh_cccd), text));
        doc.add(tblKH);

        // Thông tin chuyến đi
        doc.add(new Paragraph("Thông tin chuyến đi", label));
        PdfPTable tblCD = new PdfPTable(2); tblCD.setWidthPercentage(100); tblCD.setSpacingBefore(6);
        tblCD.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        if (tau != null)       { tblCD.addCell(new Phrase("Tàu:", text));           tblCD.addCell(new Phrase(tau, text)); }
        if (chuyen != null)    { tblCD.addCell(new Phrase("Chuyến đi:", text));     tblCD.addCell(new Phrase(chuyen, text)); }
        if (ngayGioDi != null) { tblCD.addCell(new Phrase("Ngày đi:", text));       tblCD.addCell(new Phrase(ngayGioDi, text)); }
        if (ngayGioDen != null){ tblCD.addCell(new Phrase("Ngày đến dự kiến:", text)); tblCD.addCell(new Phrase(ngayGioDen, text)); }
        doc.add(tblCD);

        // Bảng ghế & thành tiền
        PdfPTable tbl = new PdfPTable(new float[]{3f, 1.5f});
        tbl.setWidthPercentage(100); tbl.setSpacingBefore(8);
        PdfPCell c1 = new PdfPCell(new Phrase("Ghế", label));
        PdfPCell c2 = new PdfPCell(new Phrase("Thành tiền", label));
        c1.setHorizontalAlignment(Element.ALIGN_LEFT); c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tbl.addCell(c1); tbl.addCell(c2);
        if (gheThanhTien != null) {
            for (String[] row : gheThanhTien) {
                PdfPCell a = new PdfPCell(new Phrase(row != null && row.length > 0 ? nullToEmpty(row[0]) : "", text));
                PdfPCell b = new PdfPCell(new Phrase(row != null && row.length > 1 ? nullToEmpty(row[1]) : "", text));
                a.setHorizontalAlignment(Element.ALIGN_LEFT);
                b.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tbl.addCell(a); tbl.addCell(b);
            }
        }
        doc.add(tbl);

        // Chi tiết thanh toán
        Paragraph sub = new Paragraph("Chi tiết thanh toán", label);
        sub.setSpacingBefore(8); doc.add(sub);

        PdfPTable tblPay = new PdfPTable(new float[]{3f, 1.5f});
        tblPay.setWidthPercentage(100);
        PdfPCell pa = new PdfPCell(new Phrase("Tổng tiền vé", text));
        PdfPCell pb = new PdfPCell(new Phrase(fmtMoney(tongTienVe), text));
        PdfPCell pc = new PdfPCell(new Phrase("VAT", text));
        PdfPCell pd = new PdfPCell(new Phrase(fmtPercent(vat), text));
        PdfPCell pe = new PdfPCell(new Phrase("Khuyến mãi", text));
        PdfPCell pf = new PdfPCell(new Phrase(fmtMoney(khuyenMai), text));
        // Bỏ viền
        for (PdfPCell cell : new PdfPCell[]{pa,pb,pc,pd,pe,pf}) cell.setBorder(Rectangle.NO_BORDER);
        pb.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pd.setHorizontalAlignment(Element.ALIGN_RIGHT);
        pf.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tblPay.addCell(pa); tblPay.addCell(pb);
        tblPay.addCell(pc); tblPay.addCell(pd);
        tblPay.addCell(pe); tblPay.addCell(pf);
        doc.add(tblPay);

        doc.add(new Paragraph(" ", text));
        Paragraph totalLine = new Paragraph("Tổng tiền: " + fmtMoney(tongCuoi), total);
        totalLine.setAlignment(Element.ALIGN_CENTER);
        doc.add(totalLine);

        doc.close();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String fmtMoney(BigDecimal v) {
        if (v == null) return "0";
        java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance();
        return nf.format(v.longValue()) + " VND";
    }

    private static String fmtPercent(BigDecimal v) {
        if (v == null) return "0%";
        return v.stripTrailingZeros().toPlainString() + " %";
    }
}
