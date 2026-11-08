package com.example.dispatch;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemTonSummary {

    private final List<?> rows;        // 行(月別なら日付、年別なら月)
    private final List<Item> items;    // 列(品目)
    private final Map<String, BigDecimal> values = new HashMap<>();  // 「行|品目ID」→ トン数

    public ItemTonSummary(List<?> rows, List<Item> items) {
        this.rows = rows;
        this.items = items;
    }

    // 集計結果を1件ずつ入れる
    public void add(Object row, Long itemId, BigDecimal total) {
        if (row == null || itemId == null || total == null) {
            return;
        }
        values.merge(row + "|" + itemId, total, BigDecimal::add);
    }

    // ある行・ある品目のトン数
    public BigDecimal value(Object row, Item item) {
        return values.getOrDefault(row + "|" + item.getId(), BigDecimal.ZERO);
    }

    // ある行の合計(右端の「合計」の列)
    public BigDecimal rowTotal(Object row) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Item item : items) {
            sum = sum.add(value(row, item));
        }
        return sum;
    }

    // ある品目の合計(一番下の「合計」の行)
    public BigDecimal itemTotal(Item item) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Object row : rows) {
            sum = sum.add(value(row, item));
        }
        return sum;
    }

    // 全部の合計
    public BigDecimal grandTotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Object row : rows) {
            sum = sum.add(rowTotal(row));
        }
        return sum;
    }

    public List<?> getRows() { return rows; }
    public List<Item> getItems() { return items; }
}
