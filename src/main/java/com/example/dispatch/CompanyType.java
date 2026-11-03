package com.example.dispatch;

public enum CompanyType {
    INTERNAL("社内"),
    EXTERNAL("社外");

    private final String label;

    CompanyType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}