package com.example.doanmonhocltm.model;

import java.util.List;

public class CarViolationReport {

    private int carId;
    private String violatorId;
    private String officerId;
    private String reportTime;
    private List<CarViolationDetail> violationDetails;


}


//{
//        "carId":123,
//        "violatorId":"V12345",
//        "officerId":"O98765",
//        "reportTime":"2025-04-16T10:30:00",
//        "violationDetails":[
//        {
//        "violationType":"SPEEDING",
//        "description":"Vượt quá tốc độ cho phép 20km/h",
//        "penaltyType":"FINE",
//        "fineAmount":1000000
//        },
//        {
//        "violationType":"NO_LICENSE",
//        "description":"Không mang theo giấy phép lái xe",
//        "penaltyType":"FINE",
//        "fineAmount":500000
//        }
//        ]
//        }