package com.example.doanmonhocltm.model;

public class LoginHistory {
    private String accountId;
    private String ipAddress;

    public LoginHistory(String accountId, String ipAddress, String deviceInfo, String loginStatus) {
        this.accountId = accountId;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
        this.loginStatus = loginStatus;
    }

    public LoginHistory(String accountId, String ipAddress, String deviceInfo) {
        this.accountId = accountId;
        this.ipAddress = ipAddress;
        this.deviceInfo = deviceInfo;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }

    private String deviceInfo;
    private String loginStatus;
}


