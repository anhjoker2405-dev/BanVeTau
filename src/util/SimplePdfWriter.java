package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Trình ghi PDF tối giản (không cần thư viện ngoài). Chỉ hỗ trợ text đơn giản, font Courier. */
public class SimplePdfWriter {
    private static String esc(String s) {
        if (s == null) return "";
        // Escape backslash and parentheses for PDF string literals
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    /** Ghi 1 trang A4 với các dòng text mono (10pt). Return bytes length. */
    public static void writeSinglePage(List<String> lines, OutputStream out) throws IOException {
        if (lines == null) lines = new ArrayList<>();

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        List<Integer> xref = new ArrayList<>();

        // helpers
        final String NL = "\n";
        final byte[] NLb = NL.getBytes(StandardCharsets.US_ASCII);
        java.nio.charset.Charset ascii = StandardCharsets.US_ASCII;

        // header
        buf.write("%PDF-1.4\n".getBytes(ascii));
        buf.write("%\u00E2\u00E3\u00CF\u00D3\n".getBytes(ascii)); // binary marker

        // object 1: Catalog
        xref.add(buf.size());
        buf.write("1 0 obj\n".getBytes(ascii));
        buf.write("<< /Type /Catalog /Pages 2 0 R >>\n".getBytes(ascii));
        buf.write("endobj\n".getBytes(ascii));

        // object 2: Pages
        xref.add(buf.size());
        buf.write("2 0 obj\n".getBytes(ascii));
        buf.write("<< /Type /Pages /Kids [3 0 R] /Count 1 >>\n".getBytes(ascii));
        buf.write("endobj\n".getBytes(ascii));

        // object 3: Page
        xref.add(buf.size());
        buf.write("3 0 obj\n".getBytes(ascii));
        buf.write("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ".getBytes(ascii));
        buf.write("/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>\n".getBytes(ascii));
        buf.write("endobj\n".getBytes(ascii));

        // object 4: Content stream
        // Build content
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n");
        sb.append("/F1 10 Tf\n");
        sb.append("1 0 0 1 50 780 Tm\n"); // position
        sb.append("12 TL\n"); // line leading
        for (String line : lines) {
            sb.append("(").append(esc(line)).append(") Tj\nT*\n");
        }
        sb.append("ET\n");
        byte[] content = sb.toString().getBytes(ascii);

        xref.add(buf.size());
        buf.write("4 0 obj\n".getBytes(ascii));
        buf.write(("<< /Length " + content.length + " >>\n").getBytes(ascii));
        buf.write("stream\n".getBytes(ascii));
        buf.write(content);
        buf.write("endstream\n".getBytes(ascii));
        buf.write("endobj\n".getBytes(ascii));

        // object 5: Font (Type1 Courier – sẵn có)
        xref.add(buf.size());
        buf.write("5 0 obj\n".getBytes(ascii));
        buf.write("<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\n".getBytes(ascii));
        buf.write("endobj\n".getBytes(ascii));

        // xref
        int xrefStart = buf.size();
        buf.write(("xref\n0 " + (xref.size()+1) + "\n").getBytes(ascii));
        buf.write("0000000000 65535 f \n".getBytes(ascii)); // free obj 0
        for (int off : xref) {
            String line = String.format("%010d 00000 n \n", off);
            buf.write(line.getBytes(ascii));
        }

        // trailer
        buf.write(("trailer\n<< /Size " + (xref.size()+1) + " /Root 1 0 R >>\nstartxref\n").getBytes(ascii));
        buf.write((String.valueOf(xrefStart) + "\n%%EOF").getBytes(ascii));

        // write out
        buf.writeTo(out);
    }
}
