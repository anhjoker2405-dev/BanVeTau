package util;

/** Lưu tạm thông tin phiên đăng nhập */
public final class AppSession {
    private static String maNV;   // set sau khi đăng nhập

    private AppSession() {}

    public static void setMaNV(String ma) { maNV = ma; }
    public static String getMaNV() { return maNV; }
}
