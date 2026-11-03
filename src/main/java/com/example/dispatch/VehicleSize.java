package com.example.dispatch;

public enum VehicleSize {
    TEN_TON("10t"),
    EIGHT_TON("8t"),
    FOUR_TON("4t");

    private final String label;

    VehicleSize(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}