// ApiService.java
package com.example.doanmonhocltm.callapi;

import com.example.doanmonhocltm.model.Car;
import com.example.doanmonhocltm.model.CarViolationReport;
import com.example.doanmonhocltm.model.LoginHistory;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.Logout;
import com.example.doanmonhocltm.model.Motorcycle;
import com.example.doanmonhocltm.model.MotorcycleViolationReport;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.example.doanmonhocltm.model.ResultLogin;
import com.example.doanmonhocltm.model.ScanLog;

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
    Call<Motorcycle> getMotorcycleByLicensePlate(@Path("licensePlate") String licensePlate);

    @Multipart
    @POST("quet/api/face-recognition/identify")
    Call<ResultFaceRecognition> identifyFace(@Part MultipartBody.Part image);

    @GET("/quet/api/person/{id}")
    Call<ResultFaceRecognition> getPersonById(@Path("id") String id);

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

}

