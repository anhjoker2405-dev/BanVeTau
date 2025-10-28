
package util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.PageSize;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import java.io.*;
import java.util.List;

/** Tiện ích dùng OpenPDF để ghi PDF với font hỗ trợ Unicode (tiếng Việt). */
public class OpenPdfUtil {

    /** Tạo Font Unicode với kích thước mong muốn. Cố gắng tìm TTF trong:
     *  - classpath: /font/DejaVuSans.ttf hoặc /resources/DejaVuSans.ttf
     *  - đường dẫn làm việc: src/font/DejaVuSans.ttf hoặc src/font/TIMES.TTF
     *  Nếu không tìm thấy sẽ rơi về font chuẩn (không đảm bảo đủ glyph). */
    public static Font createUnicodeFont(float size) throws IOException, DocumentException {
        BaseFont bf = tryLoadBaseFont("/font/DejaVuSans.ttf");
        if (bf == null) bf = tryLoadBaseFont("/resources/DejaVuSans.ttf");
        if (bf == null) bf = tryLoadBaseFontFromFile("src/font/DejaVuSans.ttf");
        if (bf == null) bf = tryLoadBaseFontFromFile("src/font/TIMES.TTF");
        if (bf == null) {
            // Fallback – không đủ tiếng Việt nhưng đảm bảo chạy
            return new Font(Font.HELVETICA, size);
        }
        return new Font(bf, size);
    }

    private static BaseFont tryLoadBaseFont(String resourcePath) {
        try (InputStream in = OpenPdfUtil.class.getResourceAsStream(resourcePath)) {
            if (in == null) return null;
            // Đọc toàn bộ thành file tạm để OpenPDF nhận vào
            File tmp = File.createTempFile("font", ".ttf");
            try (OutputStream out = new FileOutputStream(tmp)) {
                in.transferTo(out);
            }
            BaseFont bf = BaseFont.createFont(tmp.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            tmp.delete();
            return bf;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static BaseFont tryLoadBaseFontFromFile(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) return null;
            return BaseFont.createFont(f.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Ghi danh sách dòng text vào 1 trang A4, lề 0.5 inch. */
    public static void writeLinesAsPdfSinglePage(List<String> lines, OutputStream out)
            throws IOException, DocumentException {
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, out);
        doc.open();
        Font font = createUnicodeFont(12f);
        for (String line : lines) {
            doc.add(new Paragraph(line == null ? "" : line, font));
        }
        doc.close();
    }
    public static void openFile(java.io.File f) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(f);
            }
        } catch (Exception ignored) {}
    }
}
