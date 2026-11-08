package com.example.dispatch;

public enum ReservationStatus {
    RESERVED("予約中"),
    PICKED_UP("引取済み"),
    CANCELLED("キャンセル");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}