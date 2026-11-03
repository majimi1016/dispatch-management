package com.example.dispatch;

public enum Shift {
    DAY("昼"),
    NIGHT("夜");

    private final String label;

    Shift(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}