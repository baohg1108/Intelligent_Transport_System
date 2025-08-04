package com.example.doanmonhocltm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.util.DeviceUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private ApiService apiService;
    private SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setupWindowInsets();

        initializeViews();
        initializeServices();
        setupEventListeners();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeServices() {
        this.apiService = ApiClient.getClient(LoginActivity.this).create(ApiService.class);
        this.sessionManager = new SessionManager(LoginActivity.this);
    }

    private void initializeViews() {
        this.edtUsername = findViewById(R.id.txtUsername);
        this.edtPassword = findViewById(R.id.txtPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
        this.progressBar = findViewById(R.id.progressBar);
    }

    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> handleLoginButtonClick());
    }

    private void handleLoginButtonClick() {
        String username = edtUsername.getText().toString();
        String password = edtPassword.getText().toString();
        Log.e("username", username);
        Log.e("password", password);

        if (!validateLoginInput(username, password)) {
            return;
        }

        showLoading(true);
        performLogin(username, password);
    }

    private boolean validateLoginInput(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin đăng nhập", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void performLogin(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        Call<ResultLogin> resultLogin = apiService.login(loginRequest);

        resultLogin.enqueue(new Callback<ResultLogin>() {
            @Override
            public void onResponse(Call<ResultLogin> call, Response<ResultLogin> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body());
                } else {
                    handleFailedLogin(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResultLogin> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void handleSuccessfulLogin(ResultLogin result) {
        String token = result.getToken();
        String username = result.getUsername();
        String userId = result.getId();

        sessionManager.saveToken(token);
        fetchUserDetails(userId, token, username);
    }

    private void fetchUserDetails(String userId, String token, String username) {
        Call<Person> resultFaceRecognition = apiService.getPersonById(userId);

        resultFaceRecognition.enqueue(new Callback<Person>() {
            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String fullName = response.body().getFullName();
                    fetchUserEmail(userId, token, username, fullName);
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void fetchUserEmail(String userId, String token, String username, String fullName) {
        Call<User> getUserMailCall = apiService.getUserMail(userId);

        getUserMailCall.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    String email = response.body().getEmail();
                    fetchFacePath(userId, token, username, fullName, email);
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Không lấy được email người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void fetchFacePath(String userId, String token, String username, String fullName, String email) {
        Call<Person> facePathCall = apiService.getPersonById(userId);
        facePathCall.enqueue(new Callback<Person>() {

            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful()) {
                    String facePath = response.body().getFacePath();
                    fetchUserImage(userId, token, username, fullName, email, facePath);
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi lấy facePath", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                handleServerError();
            }
        });
    }


    private void fetchUserImage(String userId, String token, String username, String fullName, String email, String facePathCall) {
        Call<ResponseBody> imageCall = apiService.getImage(facePathCall);

        imageCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        byte[] imageBytes = response.body().bytes();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                        saveUserSession(userId, token, username, fullName, email, imageBytes);
                        logLoginHistory(userId);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, "Lỗi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showLoading(false);
                    Log.e("API", "Image not found or error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showLoading(false);
                Log.e("API", "Request failed: " + t.getMessage());
            }
        });
    }

    private void saveUserSession(String userId, String token, String username, String fullName,
                                 String email, byte[] imageBytes) {
        sessionManager.saveUserSession(token, userId, username, fullName, email);
        sessionManager.saveImageToPrefs(imageBytes);
    }

    private void logLoginHistory(String userId) {
        LoginHistory loginHistory = new LoginHistory(
                userId,
                DeviceUtil.getIPAddress(true),
                DeviceUtil.getDeviceInfo(),
                "SUCCESS"
        );

        Call<LoginHistory> loginHistoryCall = apiService.createLoginHistory(loginHistory);
        loginHistoryCall.enqueue(new Callback<LoginHistory>() {
            @Override
            public void onResponse(Call<LoginHistory> call, Response<LoginHistory> response) {
                if (response.isSuccessful()) {
                    navigateToMainScreen();
                } else {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Lỗi ghi nhật ký đăng nhập", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginHistory> call, Throwable t) {
                handleServerError();
            }
        });
    }

    private void navigateToMainScreen() {
        showLoading(false);
        Toast.makeText(LoginActivity.this, "Đăng Nhập Thành Công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, FindLicensePlateActivity.class);
        startActivity(intent);
        finish(); // Đóng LoginActivity khi đã đăng nhập thành công
    }

    private void handleFailedLogin(int responseCode) {
        showLoading(false);
        System.out.println("❌ Đăng nhập thất bại: " + responseCode);
        Toast.makeText(LoginActivity.this, "Tên Đăng Nhập Hoặc Mật Khẩu Không Đúng", Toast.LENGTH_SHORT).show();
    }

    private void handleServerError() {
        showLoading(false);
        Toast.makeText(LoginActivity.this, "Server Đang Gặp Lỗi", Toast.LENGTH_SHORT).show();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText("Đăng nhập");
        }
    }
}