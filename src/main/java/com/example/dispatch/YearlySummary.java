package com.example.dispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YearlySummary {

    private final int year;
    private final String view;                          // company / size / item
    private final List<SummaryColumn> columns;          // 表に並べる列
    private final List<Integer> months = new ArrayList<>();   // 1〜12
    private final Map<String, Long> counts = new HashMap<>(); // 「月|列」→ 台数

    public YearlySummary(int year, String view,
                         List<SummaryColumn> columns,
                         List<DispatchRepository.MonthlyCount> rows) {
        this.year = year;
        this.view = view;
        this.columns = columns;

        for (int m = 1; m <= 12; m++) {
            months.add(m);
        }

        for (DispatchRepository.MonthlyCount r : rows) {
            String colKey = columnKey(r);
            if (colKey == null || r.getMonthNo() == null || r.getTotal() == null) {
                continue;
            }
            counts.merge(r.getMonthNo() + "|" + colKey, r.getTotal(), Long::sum);
        }
    }

    // 集計の1行が、どの列に入るかを決める(表示の切り替えによって変わる)
    private String columnKey(DispatchRepository.MonthlyCount r) {
        return switch (view) {
            case "size" -> (r.getVehicleSize() == null) ? null : r.getVehicleSize().name();
            case "item" -> (r.getItemId() == null) ? "i-" : "i" + r.getItemId();
            default -> (r.getCompanyId() == null) ? null : "c" + r.getCompanyId();
        };
    }

    // ある月・ある列の台数
    public long count(int month, SummaryColumn column) {
        return counts.getOrDefault(month + "|" + column.getKey(), 0L);
    }

    // ある月の合計
    public long monthTotal(int month) {
        long sum = 0;
        for (SummaryColumn c : columns) {
            sum += count(month, c);
        }
        return sum;
    }

    // ある月の、社内/社外の合計(会社別のときだけ使う)
    private long typeTotal(int month, CompanyType type) {
        long sum = 0;
        for (SummaryColumn c : columns) {
            if (c.getType() == type) {
                sum += count(month, c);
            }
        }
        return sum;
    }

    public long internalTotal(int month) { return typeTotal(month, CompanyType.INTERNAL); }
    public long externalTotal(int month) { return typeTotal(month, CompanyType.EXTERNAL); }

    // 1年分の、ある列の合計(表のいちばん下の行)
    public long columnTotal(SummaryColumn column) {
        long sum = 0;
        for (int m : months) {
            sum += count(m, column);
        }
        return sum;
    }

    public long internalYearTotal() {
        long sum = 0;
        for (int m : months) {
            sum += internalTotal(m);
        }
        return sum;
    }

    public long externalYearTotal() {
        long sum = 0;
        for (int m : months) {
            sum += externalTotal(m);
        }
        return sum;
    }

    public long yearTotal() {
        long sum = 0;
        for (int m : months) {
            sum += monthTotal(m);
        }
        return sum;
    }

    public boolean isCompanyView() {
        return "company".equals(view);
    }

    public int getYear() { return year; }
    public String getView() { return view; }
    public List<SummaryColumn> getColumns() { return columns; }
    public List<Integer> getMonths() { return months; }
}
