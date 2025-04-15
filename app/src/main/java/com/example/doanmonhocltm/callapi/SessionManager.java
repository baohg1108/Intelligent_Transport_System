package com.example.doanmonhocltm.callapi;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "my_app";

    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NAMEPERSON = "nameperson";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Gọi khi login để lưu token, user ID và username
    public void saveUserSession(String token, String userId, String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public void saveToken(String token) {
        this.prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    // Gọi khi lấy thêm họ tên người dùng
    public void saveNamePerson(String namePerson) {
        prefs.edit().putString(KEY_NAMEPERSON, namePerson).apply();
    }

    // Lấy các thông tin đã lưu
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    public String getNamePerson() {
        return prefs.getString(KEY_NAMEPERSON, null);
    }

    // Xóa toàn bộ session khi logout
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    // Kiểm tra xem người dùng đã login chưa (dựa trên token)
    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
