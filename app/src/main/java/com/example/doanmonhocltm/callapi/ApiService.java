// ApiService.java
package com.example.doanmonhocltm.callapi;

import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.Logout;


import java.util.List;

import okhttp3.ResponseBody;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("quet/api/auth/signin")
    Call<ResultLogin> login(@Body LoginRequest loginRequest);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Car> getCarByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/vehicles/motorcycles/{licensePlate}")
    Call<Vehicles> getMotorcycle(@Path("licensePlate") String licensePlate);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Vehicles> getCar(@Path("licensePlate") String licensePlate);


    @Multipart
    @POST("quet/api/face-recognition/identify")
    Call<Person> identifyFace(@Part MultipartBody.Part image);

    @GET("/quet/api/person/{id}")
    Call<Person> getPersonById(@Path("id") String id);

    @POST("/quet/api/car-violations")
    Call<CarViolationReport> createCarViolationReport(@Body CarViolationReport carViolationReport);

    @POST("/quet/api/motorcycle-violations")
    Call<MotorcycleViolationReport> createMotorcycleViolationReport(@Body MotorcycleViolationReport motorcycleViolationReport);

    @POST("/quet/api/scan_logs/motorcycles")
    Call<ScanLog> createMotorcycleScanLog(@Body ScanLog scanLog);

    @POST("/quet/api/scan_logs/cars")
    Call<ScanLog> createCarScanLog(@Body ScanLog scanLog);

    @POST("/quet/api/auth/login-history")
    Call<LoginHistory> createLoginHistory(@Body LoginHistory loginHistory);

    @PATCH("/quet/api/auth/logout/{accountId}")
    Call<Logout> logout(@Path("accountId") String accountId);

    @GET("/quet/api/images/{filename}")
    Call<ResponseBody> getImage(@Path("filename") String filename);

    @GET("/quet/api/account/{id}")
    Call<User> getUserMail(@Path("id") String id);

    @GET("quet/api/car-violations/license-plate/{licensePlate}")
    Call<List<ViolationAll>> getCarViolationsByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/motorcycle-violations/license-plate/{licensePlate}")
    Call<List<ViolationAll>> getMotorcycleViolationsByLicensePlate(@Path("licensePlate") String licensePlate);

    @GET("quet/api/car-violations/{id}")
    Call<ViolationAll> getCarViolationById(@Path("id") int id);
    @GET("quet/api/motorcycle-violations/{id}")
    Call<ViolationAll> getMotorcycleViolationById(@Path("id") int id);
    @GET("quet/api/driving-license/person/{personId}")
    Call<List<DriverLicense>> getDriverLicenseByPersonId(@Path("personId") String personId);

}

