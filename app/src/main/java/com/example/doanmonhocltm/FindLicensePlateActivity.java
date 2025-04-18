// ScanLicensePlateActivity.java
package com.example.doanmonhocltm;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.Car;
import com.example.doanmonhocltm.model.Motorcycle;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.example.doanmonhocltm.model.ScanLog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.*;

public class FindLicensePlateActivity extends AppCompatActivity {

    private MaterialButton btnChupAnh;
    private MaterialButton btnTraCuu;

    private TextInputEditText editTextLicensePlate;

    private BottomNavigationView bottomNavigation;

    private TextView userName;

    private void showCustomVehicleTypeDialog() {
        // Tạo dialog với custom layout
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_vehicle_type);

        // Set dialog width to match parent
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Khai báo các view trong dialog
        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
        LinearLayout carOption = dialog.findViewById(R.id.option_car);
        LinearLayout motorcycleOption = dialog.findViewById(R.id.option_motorcycle);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        // Set tiêu đề
        dialogTitle.setText("Chọn loại phương tiện");

        // Set sự kiện click cho các lựa chọn
        carOption.setOnClickListener(v -> {
            performVehicleLookup("Xe ô tô");
            dialog.dismiss();
        });

        motorcycleOption.setOnClickListener(v -> {
            performVehicleLookup("Xe máy");
            dialog.dismiss();
        });

        // Set sự kiện cho nút hủy
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Hiển thị dialog
        dialog.show();
    }

    private void performVehicleLookup(String vehicleType) {
        String vehicleInfo = editTextLicensePlate.getText().toString();

        if (vehicleInfo.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập biển số xe", Toast.LENGTH_SHORT).show();
            return;
        }
        vehicleInfo = vehicleInfo.replace("-", "").replace(".", "").replace(" ", "");
        if (vehicleType.equals("Xe ô tô")) {
            ApiService apiService = ApiClient.getClient(FindLicensePlateActivity.this).create(ApiService.class);
            Call<Car> carCall = apiService.getCarByLicensePlate(vehicleInfo);
            carCall.enqueue(new Callback<Car>() {
                @Override
                public void onResponse(Call<Car> call, Response<Car> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Car car = response.body();

                        Call<ResultFaceRecognition> resultFaceRecognitionCall = apiService.getPersonById(car.getOwnerId());

                        resultFaceRecognitionCall.enqueue(new Callback<ResultFaceRecognition>() {
                            @Override
                            public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                                if (response.isSuccessful()) {
                                    ResultFaceRecognition resultFaceRecognition = response.body();
                                    // Chuyển sang màn hình VehicleInfoActivity
                                    Intent intent = new Intent(FindLicensePlateActivity.this, VehicleInfoActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("type", 1); // 1: xe hoi
                                    bundle.putString("licensePlate", car.getLicensePlate());
                                    bundle.putString("brand", car.getBrand());
                                    bundle.putString("color", car.getColor());
                                    bundle.putString("owner", resultFaceRecognition.getFullName());
                                    intent.putExtra("Infor", bundle);


                                    Call<ScanLog> scanLogCall = apiService.createCarScanLog(new ScanLog(car.getLicensePlate(), car.getOwnerId()));
                                    scanLogCall.enqueue(new Callback<ScanLog>() {

                                        @Override
                                        public void onResponse(Call<ScanLog> call, Response<ScanLog> response) {
                                            if (response.isSuccessful()) {
                                                startActivity(intent);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ScanLog> call, Throwable t) {
                                            Toast.makeText(FindLicensePlateActivity.this,
                                                    "Lỗi kết nối: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });

//                                    startActivity(intent);
                                } else {
                                    Toast.makeText(FindLicensePlateActivity.this,
                                            "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                                Toast.makeText(FindLicensePlateActivity.this,
                                        "Lỗi kết nối: " + t.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(FindLicensePlateActivity.this,
                                "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                        System.out.println("Response error code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<Car> call, Throwable t) {
                    Toast.makeText(FindLicensePlateActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } else if (vehicleType.equals("Xe máy")) {
//            // Xử lý tra cứu cho xe máy
//            Toast.makeText(this, "Chức năng tra cứu xe máy đang được phát triển", Toast.LENGTH_SHORT).show();


            ApiService apiService = ApiClient.getClient(FindLicensePlateActivity.this).create(ApiService.class);
            Call<Motorcycle> carCall = apiService.getMotorcycleByLicensePlate(vehicleInfo);
            carCall.enqueue(new Callback<Motorcycle>() {

                @Override
                public void onResponse(Call<Motorcycle> call, Response<Motorcycle> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Motorcycle motorcycle = response.body();

                        Call<ResultFaceRecognition> resultFaceRecognitionCall = apiService.getPersonById(motorcycle.getOwnerId());

                        resultFaceRecognitionCall.enqueue(new Callback<ResultFaceRecognition>() {
                            @Override
                            public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                                if (response.isSuccessful()) {
                                    ResultFaceRecognition resultFaceRecognition = response.body();
                                    // Chuyển sang màn hình VehicleInfoActivity
                                    Intent intent = new Intent(FindLicensePlateActivity.this, VehicleInfoActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("type", 2);
                                    bundle.putString("licensePlate", motorcycle.getLicensePlate());
                                    bundle.putString("brand", motorcycle.getBrand());
                                    bundle.putString("color", motorcycle.getColor());
                                    bundle.putString("owner", resultFaceRecognition.getFullName());

                                    intent.putExtra("Infor", bundle);

                                    Call<ScanLog> scanLogCall = apiService.createMotorcycleScanLog(new ScanLog(motorcycle.getLicensePlate(), motorcycle.getOwnerId()));
                                    scanLogCall.enqueue(new Callback<ScanLog>() {
                                        @Override
                                        public void onResponse(Call<ScanLog> call, Response<ScanLog> response) {
                                            if (response.isSuccessful()) {
                                                startActivity(intent);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ScanLog> call, Throwable t) {
                                            Toast.makeText(FindLicensePlateActivity.this,
                                                    "Lỗi kết nối: " + t.getMessage(),
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
//                                    startActivity(intent);
                                } else {
                                    Toast.makeText(FindLicensePlateActivity.this,
                                            "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                            Toast.LENGTH_SHORT).show();

                                }
                            }

                            @Override
                            public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                                Toast.makeText(FindLicensePlateActivity.this,
                                        "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                                System.out.println("Response error code: " + response.code());
                            }
                        });

                    } else {
                        Toast.makeText(FindLicensePlateActivity.this,
                                "Không tìm thấy thông tin xe. Mã lỗi: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                        System.out.println("Response error code: " + response.code());
                    }

                }

                @Override
                public void onFailure(Call<Motorcycle> call, Throwable t) {
                    Toast.makeText(FindLicensePlateActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    t.printStackTrace();

                }
            });
        }
    }

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

        SessionManager sessionManager = new SessionManager(FindLicensePlateActivity.this);
        userName = findViewById(R.id.userName);
        userName.setText(sessionManager.getNamePerson());
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
                showCustomVehicleTypeDialog();
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