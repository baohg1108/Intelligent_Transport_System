package com.example.doanmonhocltm.callapi;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private SessionManager sessionManager;


    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Lấy token hiện tại từ SessionManager
        String token = sessionManager.getToken();

        // Nếu không có token → gửi request như cũ
        if (token == null) {
            return chain.proceed(originalRequest);
        }


        // Gắn header Authorization: Bearer <token>
        Request requestWithToken = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        return chain.proceed(requestWithToken);
    }
}