// ResultFaceRecognition.java
package com.example.doanmonhocltm.model;

import androidx.annotation.NonNull;

import com.example.doanmonhocltm.util.DateDeserializer;
import com.google.gson.annotations.JsonAdapter;

public class Person {
    private String id;
    private String fullName;
    @JsonAdapter(DateDeserializer.class)
    private Long  birthDate;
    private String gender;
    private String address;
    private String phoneNumber;
    private String facePath;

    @NonNull
    @Override
    public String toString() {
        return "ResultFaceRecognition{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", facePath='" + facePath + '\'' +
                '}';
    }


    public Person(String id, String fullName, Long birthDate, String gender, String address, String phoneNumber, String facePath) {
        this.id = id;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.facePath = facePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Long  birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFacePath() {
        return facePath;
    }

    public void setFacePath(String facePath) {
        this.facePath = facePath;
    }


}
