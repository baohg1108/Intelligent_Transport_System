package com.example.doanmonhocltm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.LoginRequest;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.example.doanmonhocltm.model.ResultLogin;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private MaterialButton btnLogin;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupEventListeners();
    }

    private void setupEventListeners() {
        btnLogin.setOnClickListener(v -> {
            String usernameInput = edtUsername.getText().toString();
            String password = edtPassword.getText().toString();

            LoginRequest loginRequest = new LoginRequest(usernameInput, password);
            ApiService apiService = ApiClient.getClient(LoginActivity.this).create(ApiService.class);

            Call<ResultLogin> resultLogin = apiService.login(loginRequest);

            resultLogin.enqueue(new Callback<ResultLogin>() {
                @Override
                public void onResponse(Call<ResultLogin> call, Response<ResultLogin> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ResultLogin result = response.body();
                        String token = result.getToken();
                        String resUsername = result.getUsername(); // rename để tránh trùng tên
                        String userId = result.getId();
                        // Tạo session và lưu thông tin
                        SessionManager sessionManager = new SessionManager(LoginActivity.this);
                        sessionManager.saveToken(token);
                        // Gọi để lấy thêm namePerson
                        Call<ResultFaceRecognition> resultFaceRecognition = apiService.getPersonById(userId);

                        resultFaceRecognition.enqueue(new Callback<ResultFaceRecognition>() {
                            @Override
                            public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String namePerson = response.body().getFullName();


                                    sessionManager.saveUserSession(token, userId, resUsername);
                                    // Lưu thêm họ tên
                                    sessionManager.saveNamePerson(namePerson);

                                    // Chuyển màn
                                    Intent intent = new Intent(LoginActivity.this, FindLicensePlateActivity.class);
                                    startActivity(intent);
                                } else {
                                    System.out.println("⚠️ Không lấy được thông tin namePerson");
                                }
                            }

                            @Override
                            public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
                    } else {
                        System.out.println("❌ Đăng nhập thất bại: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ResultLogin> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        });
    }

    private void initializeViews() {
        this.edtUsername = findViewById(R.id.edtUsername);
        this.edtPassword = findViewById(R.id.edtPassword);
        this.btnLogin = findViewById(R.id.btnLogin);
    }
}
