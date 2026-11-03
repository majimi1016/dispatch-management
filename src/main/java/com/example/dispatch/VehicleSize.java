package com.example.dispatch;

public enum VehicleSize {
    TEN_TON("大型(10t)"),
    EIGHT_TON("中型(8t)"),
    FOUR_TON("小型(4t)");

    private final String label;

    VehicleSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}