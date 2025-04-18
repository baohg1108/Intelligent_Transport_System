package com.example.doanmonhocltm.model;

public class MotorcycleViolationDetail {

    private String violationType;
    private float fineAmount;

    public MotorcycleViolationDetail(String violationType, float fineAmount) {
        this.violationType = violationType;
        this.fineAmount = fineAmount;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public float getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(float fineAmount) {
        this.fineAmount = fineAmount;
    }
}
