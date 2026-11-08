package com.example.dispatch;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlySummary {

    private final YearMonth yearMonth;
    private final List<Company> companies;           // 表に並べる会社
    private final List<LocalDate> days = new ArrayList<>();  // 1日〜末日
    private final Map<String, Long> counts = new HashMap<>(); // 「日付|会社|車格」→ 台数

    public MonthlySummary(YearMonth yearMonth,
                          List<Company> companies,
                          List<DispatchRepository.DailyCount> rows) {
        this.yearMonth = yearMonth;
        this.companies = companies;

        // 1日から末日までの日付を作る
        for (int d = 1; d <= yearMonth.lengthOfMonth(); d++) {
            days.add(yearMonth.atDay(d));
        }

        // SQLの結果を、あとから取り出しやすいように Map に入れる
        for (DispatchRepository.DailyCount r : rows) {
            if (r.getCompanyId() == null || r.getVehicleSize() == null) {
                continue;
            }
            counts.merge(key(r.getDispatchDate(), r.getCompanyId(), r.getVehicleSize()),
                    r.getTotal(), Long::sum);
        }
    }

    private String key(LocalDate day, Long companyId, VehicleSize size) {
        return day + "|" + companyId + "|" + size;
    }

    // ある日・ある会社・ある車格の台数
    public long count(LocalDate day, Company company, VehicleSize size) {
        return counts.getOrDefault(key(day, company.getId(), size), 0L);
    }

    // ある日の、指定した区分(社内/社外)の合計
    private long typeTotal(LocalDate day, CompanyType type) {
        long sum = 0;
        for (Company c : companies) {
            if (c.getType() == type) {
                for (VehicleSize s : VehicleSize.values()) {
                    sum += count(day, c, s);
                }
            }
        }
        return sum;
    }

    public long internalTotal(LocalDate day) { return typeTotal(day, CompanyType.INTERNAL); }
    public long externalTotal(LocalDate day) { return typeTotal(day, CompanyType.EXTERNAL); }
    public long dayTotal(LocalDate day) { return internalTotal(day) + externalTotal(day); }

    // 1か月分の、ある会社・ある車格の合計(表のいちばん下の行)
    public long columnTotal(Company company, VehicleSize size) {
        long sum = 0;
        for (LocalDate day : days) {
            sum += count(day, company, size);
        }
        return sum;
    }

    public long internalMonthTotal() {
        long sum = 0;
        for (LocalDate day : days) {
            sum += internalTotal(day);
        }
        return sum;
    }

    public long externalMonthTotal() {
        long sum = 0;
        for (LocalDate day : days) {
            sum += externalTotal(day);
        }
        return sum;
    }

    public long monthTotal() { return internalMonthTotal() + externalMonthTotal(); }

    public YearMonth getYearMonth() { return yearMonth; }
    public List<Company> getCompanies() { return companies; }
    public List<LocalDate> getDays() { return days; }
}
