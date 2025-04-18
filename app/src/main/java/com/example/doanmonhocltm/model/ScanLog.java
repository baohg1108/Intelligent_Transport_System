package com.example.doanmonhocltm.model;

public class ScanLog {
    private String licensePlate;
    private String operatorId;

    public ScanLog(String licensePlate, String operatorId) {
        this.licensePlate = licensePlate;
        this.operatorId = operatorId;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
}
