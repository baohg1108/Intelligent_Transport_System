package com.example.doanmonhocltm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.model.Violation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.Manifest;


public class CreateTicketActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Giao diện xử lý vi phạm
    private AutoCompleteTextView dropdownViolationType;
    private RecyclerView rvSelectedViolations;
    private TextView tvTotalFine;
    private Button btnAddViolation;

    // Danh sách vi phạm đã chọn
    private ArrayList<Violation> selectedViolations = new ArrayList<>();
    private ViolationAdapter violationAdapter;

    // Danh sách các loại vi phạm và tiền phạt tương ứng
    private final String[] violationNames = {
            "Vượt đèn đỏ", "Không đội mũ bảo hiểm", "Đi sai làn", "Không có bằng lái"
    };
    private final int[] violationFines = {
            1500000, 300000, 1000000, 2000000
    };

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

        // Sự kiện thêm vi phạm
        btnAddViolation.setOnClickListener(v -> {
            int index = dropdownViolationType.getListSelection();
            if (index == -1) {
                index = findIndexByName(dropdownViolationType.getText().toString());
            }

            if (index >= 0) {
                Violation violation = new Violation(violationNames[index], violationFines[index]);
                selectedViolations.add(violation);
                violationAdapter.notifyItemInserted(selectedViolations.size() - 1);
                updateTotalFine();
            } else {
                Toast.makeText(this, "Hãy chọn hành vi vi phạm hợp lệ!", Toast.LENGTH_SHORT).show();
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

        dropdownViolationType = findViewById(R.id.dropdownViolationType);
        rvSelectedViolations = findViewById(R.id.rvSelectedViolations);
        tvTotalFine = findViewById(R.id.tvTotalFine);
        btnAddViolation = findViewById(R.id.btnAddViolation);

        // Cấu hình RecyclerView
        rvSelectedViolations.setLayoutManager(new LinearLayoutManager(this));
        violationAdapter = new ViolationAdapter(selectedViolations);
        rvSelectedViolations.setAdapter(violationAdapter);

        // Gắn danh sách dropdown
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                violationNames
        );
        dropdownViolationType.setAdapter(dropdownAdapter);


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

    // Cập nhật tổng tiền phạt
    private void updateTotalFine() {
        int total = 0;
        for (Violation v : selectedViolations) {
            total += v.getFineAmount();
        }
        String formatted = NumberFormat.getNumberInstance(Locale.US).format(total) + " VNĐ";
        tvTotalFine.setText(formatted);
    }

    // Tìm chỉ số của hành vi vi phạm theo tên
    private int findIndexByName(String name) {
        for (int i = 0; i < violationNames.length; i++) {
            if (violationNames[i].equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }



}