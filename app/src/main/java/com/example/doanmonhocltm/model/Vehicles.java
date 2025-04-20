package com.example.doanmonhocltm.model;

public class Vehicles {
    private int id;
    private String licensePlate;
    private String brand;
    private String color;
    private String ownerId;

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

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public Vehicles(int id, String licensePlate, String brand, String color, String ownerId) {
        this.id = id;
        this.licensePlate = licensePlate;
        this.brand = brand;
        this.color = color;
        this.ownerId = ownerId;
    }
}
