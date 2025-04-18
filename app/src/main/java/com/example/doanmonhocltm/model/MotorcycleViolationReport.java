package com.example.doanmonhocltm.model;

import java.util.List;

public class MotorcycleViolationReport {

    private String licensePlate;
    private String violatorId;
    private String officerId;

    private String reportTime;             // Định dạng: "yyyy-MM-dd'T'HH:mm:ss"
    private String reportLocation;
    private String penaltyType;
    private String resolutionDeadline;     // Định dạng: "yyyy-MM-dd'T'HH:mm:ss"

    private List<MotorcycleViolationDetail> violationDetails;

    public MotorcycleViolationReport(String licensePlate, String violatorId, String officerId,
                                     String reportTime, String reportLocation, String penaltyType,
                                     String resolutionDeadline, List<MotorcycleViolationDetail> violationDetails) {
        this.licensePlate = licensePlate;
        this.violatorId = violatorId;
        this.officerId = officerId;
        this.reportTime = reportTime;
        this.reportLocation = reportLocation;
        this.penaltyType = penaltyType;
        this.resolutionDeadline = resolutionDeadline;
        this.violationDetails = violationDetails;
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

    public List<MotorcycleViolationDetail> getViolationDetails() {
        return violationDetails;
    }

    public void setViolationDetails(List<MotorcycleViolationDetail> violationDetails) {
        this.violationDetails = violationDetails;
    }
}
