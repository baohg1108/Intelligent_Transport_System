// VehicleInfoActivity.java
package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleInfoActivity extends AppCompatActivity {
    private int type;
    private TextView tvOwner, tvPlate, tvBrand, tvColor, tvLastScan, tvViolationInfo, vehicleTypeText;
    private BottomNavigationView bottomNavigation;
    private TextView userName;

    // Khai báo các view mới
    private Button btnViewViolations;
    private Button btnCreateTicket;
    private CardView idVerificationContainer;
    private Button btnCancelVerification;
    private Button btnConfirmVerification;
    private Button btnScanFace;
    private EditText etCCCD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vehicle_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo view cũ
        tvOwner = findViewById(R.id.tvOwner);
        tvPlate = findViewById(R.id.tvPlate);
        tvBrand = findViewById(R.id.tvBrand);
        tvColor = findViewById(R.id.tvColor);
//        tvLastScan = findViewById(R.id.tvLastScan);
//        tvViolationInfo = findViewById(R.id.tvViolationInfo);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_bien_so);

        // Khởi tạo các view mới
        btnViewViolations = findViewById(R.id.btnViewViolations);
        btnCreateTicket = findViewById(R.id.btnCreateTicket);
        idVerificationContainer = findViewById(R.id.idVerificationContainer);
        btnCancelVerification = findViewById(R.id.btnCancelVerification);
        btnConfirmVerification = findViewById(R.id.btnConfirmVerification);
        btnScanFace = findViewById(R.id.btnScanFace);
        etCCCD = findViewById(R.id.etCCCD);
        vehicleTypeText = findViewById(R.id.vehicleTypeText);


        //__________________________________________________________________________________________________________

        // Thiết lập sự kiện cho các button mới
        setupButtonListeners();

        //__________________________________________________________________________________________________________
        SessionManager sessionManager = new SessionManager(VehicleInfoActivity.this);
        userName = findViewById(R.id.userName);
        userName.setText(sessionManager.getNamePerson());
        //__________________________________________________________________________________________________________

        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(VehicleInfoActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(VehicleInfoActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(VehicleInfoActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });

        // Lay du lieu Intent
        Intent intent = getIntent();

        // Lay du lieu bunlde
        Bundle bundle = intent.getBundleExtra("Infor");
        if (bundle != null) {
            type = bundle.getInt("type");
            String licensePlate = bundle.getString("licensePlate");
            String brand = bundle.getString("brand");
            String color = bundle.getString("color");
            String owner = bundle.getString("owner");

            tvPlate.setText(licensePlate);
            tvBrand.setText(brand);
            tvColor.setText(color);
            tvOwner.setText(owner);

            if (type == 1) {
                vehicleTypeText.setText("Thông Tin Xe Hơi");
            } else {
                vehicleTypeText.setText("Thông Tin Xe Máy");
            }
        }

    }

    /**
     * Thiết lập các sự kiện xử lý cho các button mới thêm vào
     */
    private void setupButtonListeners() {
        // Nút xem lịch sử vi phạm
        btnViewViolations.setOnClickListener(v -> {
            // Tạo Bundle chứa thông tin xe
            Bundle vehicleBundle = new Bundle();
            vehicleBundle.putString("licensePlate", tvPlate.getText().toString());

            // Tạo Intent chuyển đến màn hình lịch sử vi phạm
            Intent intent = new Intent(VehicleInfoActivity.this, MainActivity.class);
            intent.putExtra("vehicleData", vehicleBundle);
            startActivity(intent);
        });

        // Nút tạo biên bản xử phạt - hiển thị form xác thực
        btnCreateTicket.setOnClickListener(v -> {
            // Hiển thị container nhập CCCD và quét khuôn mặt
            idVerificationContainer.setVisibility(View.VISIBLE);

            // Reset input fields
            etCCCD.setText("");
        });

        // Nút hủy xác thực
        btnCancelVerification.setOnClickListener(v -> {
            // Ẩn container xác thực
            idVerificationContainer.setVisibility(View.GONE);
        });

        // Nút xác nhận biên bản xử phạt
        btnConfirmVerification.setOnClickListener(v -> {
            String cccd = etCCCD.getText().toString().trim();

            // Kiểm tra CCCD đã được nhập chưa
            if (cccd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số CCCD hoặc quét khuôn mặt", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra độ dài CCCD
            if (cccd.length() < 9 || cccd.length() > 12) {
                Toast.makeText(this, "Số CCCD/CMND không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call api CCCD
            ApiService apiService = ApiClient.getClient(VehicleInfoActivity.this).create(ApiService.class);
            Call<ResultFaceRecognition> resultFaceRecognitionCall = apiService.getPersonById(cccd);

            resultFaceRecognitionCall.enqueue(new Callback<ResultFaceRecognition>() {
                @Override
                public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                    if (response.isSuccessful()) {
                        ResultFaceRecognition resultFaceRecognition = response.body();
                        if (resultFaceRecognition != null) {
                            // Hiển thị thông báo xác thực thành công
                            Toast.makeText(VehicleInfoActivity.this, "Xác thực thành công", Toast.LENGTH_SHORT).show();


                            // Tạo Bundle chứa thông tin cần thiết
                            Bundle ticketData = new Bundle();
                            ticketData.putInt("type", type);
                            ticketData.putString("licensePlate", tvPlate.getText().toString());
//                            ticketData.putString("brand", tvBrand.getText().toString());
//                            ticketData.putString("color", tvColor.getText().toString());
//                            ticketData.putString("ownerName", tvOwner.getText().toString());
                            ticketData.putString("driverCCCD", resultFaceRecognition.getId());
                            ticketData.putString("driverName", resultFaceRecognition.getFullName());

                            // Chuyển sang màn hình tạo biên bản
                            Intent intent = new Intent(VehicleInfoActivity.this, CreateTicketActivity.class);
                            intent.putExtra("ticketData", ticketData);
                            startActivity(intent);


                            // Ẩn container xác thực
                            idVerificationContainer.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                    Toast.makeText(VehicleInfoActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
            //__________________________________________________________________________________________________

        });

        // Nút quét khuôn mặt
        btnScanFace.setOnClickListener(v -> {


            Bundle ticketData = new Bundle();
            ticketData.putInt("type", type);
            ticketData.putString("licensePlate", tvPlate.getText().toString());

            Toast.makeText(VehicleInfoActivity.this, "licensePlate: " + tvPlate.getText().toString(), Toast.LENGTH_SHORT).show();

            // Tạo Intent để mở camera hoặc màn hình quét khuôn mặt
            Intent faceIntent = new Intent(VehicleInfoActivity.this, ScanPersonActivity.class);

            faceIntent.putExtra("ticketData", ticketData);

//            startActivity(faceIntent);


            // Định nghĩa requestCode, ví dụ 100
            int FACE_SCAN_REQUEST_CODE = 100;

            // Khởi động Activity với startActivityForResult
            startActivityForResult(faceIntent, FACE_SCAN_REQUEST_CODE);
        });
    }

    /**
     * Xử lý kết quả trả về từ các Activity con
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả từ FaceScanActivity (với requestCode = 100)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Lấy CCCD từ kết quả quét khuôn mặt
            String cccdFromFaceScan = data.getStringExtra("cccdResult");

            // Cập nhật lên EditText
            if (cccdFromFaceScan != null && !cccdFromFaceScan.isEmpty()) {
                etCCCD.setText(cccdFromFaceScan);
            }
        }
    }
}


