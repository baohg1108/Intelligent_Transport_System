package com.example.doanmonhocltm.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DriverLicense {
    private int id;
    private String licenseNumber;
    private String personId;
    private String issueDate;
    private String expiryDate;
    private String licenseClass;
    private String placeOfIssue;
    private String status;


    private String convertDate(String input){
        // Thêm giây để hợp lệ hóa định dạng ISO 8601
        if (input.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:?")) {
            input += "00"; // thêm giây nếu bị thiếu
        }

        // B1: Parse từ ISO-like format thành Date
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = isoFormat.parse(input);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // B2: Format sang dd-MM-yyyy
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        String formattedDate = outputFormat.format(date);

        return formattedDate;
    }
    public DriverLicense(int id, String licenseNumber, String personId, String issueDate, String expiryDate, String licenseClass, String placeOfIssue, String status) throws ParseException {
        this.id = id;
        this.licenseNumber = licenseNumber;
        this.personId = personId;
        this.issueDate = convertDate(issueDate);
        this.expiryDate = convertDate(expiryDate);
        this.licenseClass = licenseClass;
        this.placeOfIssue = placeOfIssue;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getLicenseClass() {
        return licenseClass;
    }

    public void setLicenseClass(String licenseClass) {
        this.licenseClass = licenseClass;
    }

    public String getPlaceOfIssue() {
        return placeOfIssue;
    }

    public void setPlaceOfIssue(String placeOfIssue) {
        this.placeOfIssue = placeOfIssue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

//{
//    "id": 1,
//    "licenseNumber": "580243000905",
//    "personId": "058205002155",
//    "issueDate": "2024-01-31T00:00:00",
//    "expiryDate": "9999-12-31T00:00:00",
//    "licenseClass": "A1",
//    "placeOfIssue": "Ninh Thuận",
//    "status": "VALID"
//}