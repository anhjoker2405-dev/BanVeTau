package entity;

import java.util.Objects;

/**
 * Mẫu dữ liệu đơn giản cho các lựa chọn trong JComboBox.
 */
public class ComboItem {
    private final String value;
    private final String label;

    public ComboItem(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComboItem)) return false;
        ComboItem that = (ComboItem) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}