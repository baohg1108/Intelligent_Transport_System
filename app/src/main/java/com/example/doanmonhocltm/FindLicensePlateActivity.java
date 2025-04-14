// ScanLicensePlateActivity.java
package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.Car;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.*;


public class FindLicensePlateActivity extends AppCompatActivity {

    private MaterialButton btnChupAnh;
    private MaterialButton btnTraCuu;

    private TextInputEditText editTextLicensePlate;

    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_license_plate);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Thiết lập các sự kiện
        setupEventListeners();


    }

    private void initializeViews() {
        btnChupAnh = findViewById(R.id.btnChupAnh);
        btnTraCuu = findViewById(R.id.btnTraCuu);
        editTextLicensePlate = findViewById(R.id.editTextLicensePlate);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_bien_so);
    }

    private void setupEventListeners() {
        // Xử lý sự kiện khi nhấn nút "Chụp Ảnh"
        btnChupAnh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình Scan_Bike
                Intent intent = new Intent(FindLicensePlateActivity.this, ScanLicensePlateActivity.class);
                startActivity(intent);
            }
        });

        btnTraCuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String VehicleInfo = editTextLicensePlate.getText().toString();

                if (!VehicleInfo.trim().isEmpty()) {
//                    String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQSIsImlhdCI6MTc0NDU2MDAxMywiZXhwIjoxNzQ0NTYzNjEzfQ.8Yz2vnLI4ADM7dztJ5vLGG4vE4hXJFRqr26sVao9zxCQoXFS9NJVgFXmedgfpKlJ4dlRr6bHDFz6wODEpfebaw";
//                    ApiService apiService = ApiClient.getClient(token).create(ApiService.class);

                    ApiService apiService = ApiClient.getClient(FindLicensePlateActivity.this).create(ApiService.class);

                    Call<Car> car = apiService.getCarByLicensePlate(VehicleInfo);

                    car.enqueue(new Callback<Car>() {

                        @Override
                        public void onResponse(Call<Car> call, Response<Car> response) {
                            if (response.isSuccessful()) {
                                Car car = response.body();

                                // Chuyển sang màn hình VehicleInfoActivity
                                Intent intent = new Intent(FindLicensePlateActivity.this, VehicleInfoActivity.class);

                                Bundle bundle = new Bundle();

                                bundle.putString("licensePlate", car.getLicensePlate());
                                bundle.putString("brand", car.getBrand());
                                bundle.putString("color", car.getColor());

                                intent.putExtra("carInfor", bundle);
                                startActivity(intent);

                            } else {
                                System.out.println("Response error code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Car> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                }
            }
        });

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_bien_so) {
                    // Đang ở đây rồi
                    return true;

                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(FindLicensePlateActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(FindLicensePlateActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }
}