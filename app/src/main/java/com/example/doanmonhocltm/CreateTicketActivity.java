package com.example.doanmonhocltm;

import android.Manifest;
import android.app.DatePickerDialog;
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

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.CarViolationDetail;
import com.example.doanmonhocltm.model.CarViolationReport;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTicketActivity extends AppCompatActivity implements ViolationAdapter.OnViolationDeleteListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private int type;

    private AutoCompleteTextView dropdownViolationType, dropdownPenaltyType;
    private RecyclerView rvSelectedViolations;
    private TextView tvTotalFine, tvCCCD, tvPlateNumber, tvDriverName, tvDateTime;
    private TextInputEditText etDueDate, etLocation;
    private Button btnAddViolation, btnFinalizeTicket;
    private ImageButton btnBack, btnSave;

    private final String[] violationNames = {
            "Vượt đèn đỏ", "Không đội mũ bảo hiểm", "Đi sai làn", "Không có bằng lái"
    };
    private final int[] violationFines = {
            1500000, 300000, 1000000, 2000000
    };
    private final String[] penaltyTypes = {
            "Phạt tiền", "Tạm giữ phương tiện", "Phạt tiền + tạm giữ phương tiện", "Tước GPLX", "Cảnh cáo"
    };

    private ArrayList<Violation> selectedViolations = new ArrayList<>();
    private ViolationAdapter violationAdapter;
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

        initializeViews();
        extractIntentData();
        setupEventListeners();
    }

    private void initializeViews() {
        tvCCCD = findViewById(R.id.tvCCCD);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDateTime = findViewById(R.id.tvDateTime);
        etLocation = findViewById(R.id.etLocation);

        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        btnAddViolation = findViewById(R.id.btnAddViolation);
        btnFinalizeTicket = findViewById(R.id.btnFinalizeTicket);

        dropdownViolationType = findViewById(R.id.dropdownViolationType);
        dropdownPenaltyType = findViewById(R.id.dropdownPenaltyType);
        etDueDate = findViewById(R.id.etDueDate);
        rvSelectedViolations = findViewById(R.id.rvSelectedViolations);
        tvTotalFine = findViewById(R.id.tvTotalFine);

        rvSelectedViolations.setLayoutManager(new LinearLayoutManager(this));
        violationAdapter = new ViolationAdapter(selectedViolations);
        violationAdapter.setOnViolationDeleteListener(this);
        rvSelectedViolations.setAdapter(violationAdapter);

        dropdownViolationType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, violationNames));
        dropdownPenaltyType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, penaltyTypes));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        etDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("ticketData");

        if (bundle != null) {
            type = bundle.getInt("type");
            tvCCCD.setText(bundle.getString("driverCCCD"));
            tvPlateNumber.setText(bundle.getString("licensePlate"));
            tvDriverName.setText(bundle.getString("driverName"));

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date()));

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                getCurrentLocation();
            }
        }
    }

    private void setupEventListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAddViolation.setOnClickListener(v -> {
            int index = dropdownViolationType.getListSelection();
            if (index == -1) index = findIndexByName(dropdownViolationType.getText().toString());

            if (index >= 0) {
                Violation violation = new Violation(violationNames[index], violationFines[index]);
                selectedViolations.add(violation);
                violationAdapter.notifyItemInserted(selectedViolations.size() - 1);
                updateTotalFine();
            } else {
                Toast.makeText(this, "Hãy chọn hành vi vi phạm hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        etDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateTicketActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        etDueDate.setText(formattedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        btnSave.setOnClickListener(v -> Toast.makeText(this, "Đã lưu biên bản xử phạt", Toast.LENGTH_SHORT).show());

        btnFinalizeTicket.setOnClickListener(v -> {
            if (selectedViolations.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất một vi phạm", Toast.LENGTH_SHORT).show();
                return;
            }
            if (etDueDate.getText().toString().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn thời hạn giải quyết", Toast.LENGTH_SHORT).show();
                return;
            }

            List<CarViolationDetail> violationDetails = new ArrayList<>();
            for (Violation item : selectedViolations) {
                violationDetails.add(new CarViolationDetail(item.getName(), item.getFineAmount()));
            }

            if (type == 1) {
                String formattedReportTime = null;
                try {
                    Date reportDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                            .parse(tvDateTime.getText().toString());
                    formattedReportTime = formatToIso8601(reportDate.getTime());
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi định dạng ngày giờ", Toast.LENGTH_SHORT).show();
                    return;
                }

                String formattedDeadline = null;
                try {
                    Date deadlineDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .parse(etDueDate.getText().toString());
                    formattedDeadline = formatToIso8601(deadlineDate.getTime());
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi khi phân tích thời hạn", Toast.LENGTH_SHORT).show();
                    return;
                }

                CarViolationReport carViolationReport = new CarViolationReport(
                        tvPlateNumber.getText().toString(),
                        tvCCCD.getText().toString(),
                        new SessionManager(this).getUserId(),
                        formattedReportTime,
                        etLocation.getText().toString(),
                        dropdownPenaltyType.getText().toString(),
                        formattedDeadline,
                        violationDetails
                );

                ApiService apiService = ApiClient.getClient(CreateTicketActivity.this).create(ApiService.class);
                apiService.createCarViolationReport(carViolationReport).enqueue(new Callback<CarViolationReport>() {
                    @Override
                    public void onResponse(Call<CarViolationReport> call, Response<CarViolationReport> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateTicketActivity.this, "Đã tạo biên bản", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateTicketActivity.this, "Lỗi khi tạo biên bản", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<CarViolationReport> call, Throwable t) {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi khi tạo biên bản", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Xe máy thì đang phát triển", Toast.LENGTH_SHORT).show();
            }
        });
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
                        Geocoder geocoder = new Geocoder(CreateTicketActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                etLocation.setText(addresses.get(0).getAddressLine(0));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không có quyền truy cập vị trí!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTotalFine() {
        int total = 0;
        for (Violation v : selectedViolations) total += v.getFineAmount();
        tvTotalFine.setText(NumberFormat.getNumberInstance(Locale.US).format(total) + " VNĐ");
    }

    private int findIndexByName(String name) {
        for (int i = 0; i < violationNames.length; i++) {
            if (violationNames[i].equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

//    private String formatToIso8601(long timestamp) {
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
//        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//        return sdf.format(new Date(timestamp));
//    }

    private String formatToIso8601(long timestamp) {
        // Sử dụng múi giờ UTC để format với Z ở cuối
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Cố định múi giờ Việt Nam (UTC+7)
        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

        // Offset cố định cho múi giờ Việt Nam (7 giờ = 7 * 60 * 60 * 1000 milliseconds)
        int offsetMillis = vietnamTimeZone.getRawOffset();

        // Điều chỉnh timestamp để khi chuyển sang UTC vẫn giữ đúng thời gian Việt Nam
        long adjustedTimestamp = timestamp + offsetMillis;

        return sdf.format(new Date(adjustedTimestamp));
    }

    @Override
    public void onViolationDelete(int position) {
        if (position >= 0 && position < selectedViolations.size()) {
            selectedViolations.remove(position);
            violationAdapter.notifyItemRemoved(position);
            updateTotalFine();
            Toast.makeText(this, "Đã xóa vi phạm", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Bạn cần cấp quyền truy cập vị trí để tiếp tục", Toast.LENGTH_SHORT).show();
        }
    }
}
