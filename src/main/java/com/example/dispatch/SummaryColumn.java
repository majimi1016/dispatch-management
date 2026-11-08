package com.example.dispatch;

public class SummaryColumn {

    private final String key;         // 列を見分けるための記号(例:c1、TEN_TON、i3)
    private final String label;       // 見出しに出す名前
    private final CompanyType type;   // 会社別のときだけ使う(社内/社外)。それ以外は null

    public SummaryColumn(String key, String label, CompanyType type) {
        this.key = key;
        this.label = label;
        this.type = type;
    }

    public String getKey() { return key; }
    public String getLabel() { return label; }
    public CompanyType getType() { return type; }
}
