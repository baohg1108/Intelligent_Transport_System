package com.example.doanmonhocltm.callapi;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
//        private static String BASE_URL = "http://10.0.2.2:8087/"; // Dành cho giả lập Android
    private static String BASE_URL = "http://192.168.100.92:8087/"; // Địa chỉ IP của server

    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            SessionManager sessionManager = new SessionManager(context);

            // Tạo HttpLoggingInterceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log toàn bộ request/response

            // Cấu hình OkHttpClient với interceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)  // Thêm interceptor vào client
                    .addInterceptor(new AuthInterceptor(sessionManager))  // Interceptor của bạn
                    .build();

            // Tạo Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
