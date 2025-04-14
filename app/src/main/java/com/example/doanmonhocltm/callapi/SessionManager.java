package com.example.doanmonhocltm.callapi;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Tên file SharedPreferences
    private static final String PREF_NAME = "my_app";

    // Key để lưu token
    private static final String KEY_TOKEN = "access_token";

    private SharedPreferences prefs;

    // Constructor nhận context để khởi tạo SharedPreferences
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Gọi khi login thành công để lưu token
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    // Lấy token đã lưu để gửi trong mỗi API
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Xóa token khi logout
    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }
}
