package dao;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra dữ liệu đầu vào cho các form sử dụng chung trong ứng dụng.
 */
public final class FormValidator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{Lu}][\\p{L}]{1,}(\\s[\\p{Lu}][\\p{L}]{1,})+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^(\\d{9}|\\d{12})$");

    private FormValidator() {
        // Utility class
    }

    public static boolean isValidPersonName(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        if (normalized.length() < 2 || normalized.length() > 50) {
            return false;
        }
        return NAME_PATTERN.matcher(normalized).matches();
    }

    public static boolean isValidPhoneNumber(String value) {
        if (value == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidEmail(String value) {
        if (value == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isValidCccd(String value) {
        if (value == null) {
            return false;
        }
        return CCCD_PATTERN.matcher(value.trim()).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}