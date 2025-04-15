// ApiService.java
package com.example.doanmonhocltm.callapi;

import com.example.doanmonhocltm.model.Car;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.example.doanmonhocltm.model.ResultLogin;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @POST("quet/api/auth/signin")
    Call<ResultLogin> login(@Body LoginRequest loginRequest);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Car> getCarByLicensePlate(@Path("licensePlate") String licensePlate);

    @Multipart
    @POST("quet/api/face-recognition/identify")
    Call<ResultFaceRecognition> identifyFace(@Part MultipartBody.Part image);

    @GET("/quet/api/person/{id}")
    Call<ResultFaceRecognition> getPersonById(@Path("id") String id);


}
