package com.example.doanmonhocltm.networks;

import com.example.doanmonhocltm.models.ApiResponse;
import com.example.doanmonhocltm.models.StatusUpdateRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Path;


public interface RetrofitApi {
    // gửi yêu cầu cập nhật status tai nạn cụ thể
    @PUT("accident/{id}")
    Call<ApiResponse> updateAccidentStatus(
    @Path("id") String accidentId,
    @Body StatusUpdateRequest statusUpdateRequest
    );
}
