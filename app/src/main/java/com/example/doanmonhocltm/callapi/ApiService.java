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
import com.example.doanmonhocltm.model.User;
import com.example.doanmonhocltm.model.Vehicles;

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

    // ___________________________________________________________________________
    @GET("quet/api/vehicles/motorcycles/{licensePlate}")
    Call<Vehicles> getMotorcycle(@Path("licensePlate") String licensePlate);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Vehicles> getCar(@Path("licensePlate") String licensePlate);


    // ___________________________________________________________________________

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

    @GET("/quet/api/images/{filename}")
    Call<ResponseBody> getImage(@Path("filename") String filename);

    @GET("/quet/api/account/{id}")
    Call<User> getUserMail(@Path("id") String id);
}

