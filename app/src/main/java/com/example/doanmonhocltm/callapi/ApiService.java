// ApiService.java
package com.example.doanmonhocltm.callapi;

import com.example.doanmonhocltm.model.Car;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.ResultLogin;

import retrofit2.*;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("quet/api/auth/signin")
    Call<ResultLogin> login(@Body LoginRequest loginRequest);

    @GET("quet/api/vehicles/cars/{licensePlate}")
    Call<Car> getCarByLicensePlate(@Path("licensePlate") String licensePlate);

}
