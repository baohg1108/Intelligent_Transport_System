package com.example.doanmonhocltm.model;

public class ViolationItem {
    private String name;
    private int fineAmount;

    private String punishmentType;

    public ViolationItem(String name, int fineAmount) {
        this.name = name;
        this.fineAmount = fineAmount;
    }

    public String getName() {
        return name;
    }

    public int getFineAmount() {
        return fineAmount;
    }
}
