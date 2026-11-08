package com.example.dispatch;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class DateDisplay {

    // 「11/8(日)」の形
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("M/d(E)", Locale.JAPANESE);

    // 「2026年11月8日(日)」の形
    private static final DateTimeFormatter LONG =
            DateTimeFormatter.ofPattern("yyyy年M月d日(E)", Locale.JAPANESE);

    public String shortDate(LocalDate date) {
        return (date == null) ? "" : date.format(SHORT);
    }

    public String longDate(LocalDate date) {
        return (date == null) ? "" : date.format(LONG);
    }

    // 土曜日なら "sat"、日曜日なら "sun"、それ以外は ""(色分け用)
    public String dayClass(LocalDate date) {
        if (date == null) {
            return "";
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return "sat";
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return "sun";
        }
        return "";
    }
}