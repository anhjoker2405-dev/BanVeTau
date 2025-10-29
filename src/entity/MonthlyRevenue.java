package entity;

import java.math.BigDecimal;

/** DTO biểu diễn doanh thu theo tháng */
public class MonthlyRevenue {
    private final int year;
    private final int month; // 1-12
    private final BigDecimal revenue;

    public MonthlyRevenue(int year, int month, BigDecimal revenue) {
        this.year = year;
        this.month = month;
        this.revenue = revenue == null ? BigDecimal.ZERO : revenue;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getRevenue() { return revenue; }

    public String getMonthLabel() {
        String m = (month < 10 ? "0" : "") + month;
        return m + "/" + year;
    }
}