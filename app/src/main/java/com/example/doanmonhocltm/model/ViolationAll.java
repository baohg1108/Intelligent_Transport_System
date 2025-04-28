package com.example.doanmonhocltm.model;

import java.util.List;

public class ViolationAll {
    private int id;
    private String licensePlate;
    private String violatorId;
    private String officerId;
    private String reportTime;
    private String reportLocation;
    private String penaltyType;
    private String resolutionDeadline;
    private boolean resolutionStatus;
    private String violatorName;
    private String officerName;
    private String carBrand;
    private String carColor;
    private List<ViolationDetail> violationDetails;

    public ViolationAll() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getViolatorId() {
        return violatorId;
    }

    public void setViolatorId(String violatorId) {
        this.violatorId = violatorId;
    }

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getReportLocation() {
        return reportLocation;
    }

    public void setReportLocation(String reportLocation) {
        this.reportLocation = reportLocation;
    }

    public String getPenaltyType() {
        return penaltyType;
    }

    public void setPenaltyType(String penaltyType) {
        this.penaltyType = penaltyType;
    }

    public String getResolutionDeadline() {
        return resolutionDeadline;
    }

    public void setResolutionDeadline(String resolutionDeadline) {
        this.resolutionDeadline = resolutionDeadline;
    }

    public boolean isResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(boolean resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public String getViolatorName() {
        return violatorName;
    }

    public void setViolatorName(String violatorName) {
        this.violatorName = violatorName;
    }

    public String getOfficerName() {
        return officerName;
    }

    public void setOfficerName(String officerName) {
        this.officerName = officerName;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public List<ViolationDetail> getViolationDetails() {
        return violationDetails;
    }

    public void setViolationDetails(List<ViolationDetail> violationDetails) {
        this.violationDetails = violationDetails;
    }

    // Method to calculate total fine
    public float getTotalFine() {
        float total = 0;
        if (violationDetails != null) {
            for (ViolationDetail detail : violationDetails) {
                total += detail.getFineAmount();
            }
        }
        return total;
    }
}