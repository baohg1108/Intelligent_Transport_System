package com.example.doanmonhocltm.networks;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http(s)://tang-hoc-phi.com/";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            //fix thieu retrofit2
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;

    }
}