package com.example.doanmonhocltm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;


public class CreateTicketActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private TextView tvCCCD;
    private TextView tvPlateNumber;
    private TextView tvDriverName;
    private TextView tvDateTime;

    private TextInputEditText etLocation;

//    private TextView tvLocation;
//    private TextView tvDateTime;
//    private TextView tvViolationType;
//    private TextView tvTotalFine;

    private ImageButton btnBack;

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_ticket);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Truyền data từ intent
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("ticketData");

        if (bundle != null) {
            String cccd = bundle.getString("driverCCCD");
            String plateNumber = bundle.getString("licensePlate");
            String driverName = bundle.getString("driverName");

            tvCCCD.setText(cccd);
            tvPlateNumber.setText(plateNumber);
            tvDriverName.setText(driverName);

            // Lấy ngày và giờ hiện tại
            Date currentTime = new Date();

            // Định dạng: dd/MM/yyyy HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

            // Gán vào TextView
            String formattedTime = sdf.format(currentTime);
            tvDateTime.setText(formattedTime);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Kiểm tra quyền ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa có quyền, yêu cầu quyền từ người dùng
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            } else {
                // Nếu quyền đã được cấp, thực hiện hành động cần quyền (ví dụ: lấy vị trí)
                getCurrentLocation();
            }


        }


        // Thiết lập các sự kiện
        setupEventListeners();

    }

    private void setupEventListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeViews() {
        tvCCCD = findViewById(R.id.tvCCCD);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDateTime = findViewById(R.id.tvDateTime);
        etLocation = findViewById(R.id.etLocation);

        btnBack = findViewById(R.id.btnBack);

    }

    private void getCurrentLocation() {
        try {
            LocationRequest locationRequest = LocationRequest.create()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000)
                    .setFastestInterval(2000);
            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) return;

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();

                        // Dùng Geocoder để lấy địa chỉ
                        Geocoder geocoder = new Geocoder(CreateTicketActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                String address = addresses.get(0).getAddressLine(0);

                                // Hiển thị lên TextView
                                etLocation.setText(address);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Sau khi có vị trí, dừng lại để tiết kiệm pin
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            // Xử lý lỗi khi không có quyền
            e.printStackTrace();
            Toast.makeText(this, "Không có quyền truy cập vị trí!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, thực hiện hành động yêu cầu quyền
                getCurrentLocation();
            } else {
                // Quyền bị từ chối, thông báo cho người dùng
                Toast.makeText(this, "Bạn cần cấp quyền truy cập vị trí để tiếp tục", Toast.LENGTH_SHORT).show();
            }
        }
    }


}