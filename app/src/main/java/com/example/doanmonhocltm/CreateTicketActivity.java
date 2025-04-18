// Gói ứng dụng thuộc package com.example.doanmonhocltm
package com.example.doanmonhocltm;

// Các import cần thiết từ Android SDK

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

// Các thư viện AndroidX hỗ trợ tương thích
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

// Các class liên quan đến gọi API
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;

// Các model dữ liệu
import com.example.doanmonhocltm.model.CarViolationDetail;
import com.example.doanmonhocltm.model.CarViolationReport;
import com.example.doanmonhocltm.model.MotorcycleViolationDetail;
import com.example.doanmonhocltm.model.MotorcycleViolationReport;
import com.example.doanmonhocltm.model.Violation;

// Thư viện dịch vụ vị trí từ Google
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

// Widget nhập văn bản có viền của Material Design
import com.google.android.material.textfield.TextInputEditText;

// Các class Java cơ bản
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

// Thư viện Retrofit để gọi API bất đồng bộ
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Activity chính để tạo biên bản xử phạt
public class CreateTicketActivity extends AppCompatActivity implements ViolationAdapter.OnViolationDeleteListener {

    // Hằng số xin quyền truy cập vị trí
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Loại phương tiện (xe máy hay ô tô)
    private int type;

    // Các thành phần UI
    private AutoCompleteTextView dropdownViolationType, dropdownPenaltyType;
    private RecyclerView rvSelectedViolations;
    private TextView tvTotalFine, tvCCCD, tvPlateNumber, tvDriverName, tvDateTime;
    private TextInputEditText etDueDate, etLocation;
    private Button btnAddViolation, btnFinalizeTicket;
    private ImageButton btnBack;

    // Danh sách các hành vi vi phạm mẫu và mức phạt
    private final String[] violationNames = {
            "Vượt đèn đỏ", "Không đội mũ bảo hiểm", "Đi sai làn", "Không có bằng lái"
    };
    private final int[] violationFines = {
            1500000, 300000, 1000000, 2000000
    };

    // Các hình thức xử phạt
    private final String[] penaltyTypes = {
            "Phạt tiền", "Tạm giữ phương tiện", "Phạt tiền + tạm giữ phương tiện", "Tước GPLX", "Cảnh cáo"
    };

    // Danh sách vi phạm đã chọn
    private ArrayList<Violation> selectedViolations = new ArrayList<>();
    private ViolationAdapter violationAdapter;

    // Đối tượng dùng để lấy vị trí hiện tại
    private FusedLocationProviderClient fusedLocationClient;

    // Hàm khởi tạo activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Hỗ trợ toàn màn hình
        setContentView(R.layout.activity_create_ticket);

        // Xử lý padding để tránh vùng notch của màn hình
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Gọi các hàm khởi tạo view, lấy dữ liệu, xử lý sự kiện
        initializeViews();
        extractIntentData();
        setupEventListeners();
    }

    // Khởi tạo các thành phần giao diện
    private void initializeViews() {
        // Gán các view từ XML
        tvCCCD = findViewById(R.id.tvCCCD);
        tvPlateNumber = findViewById(R.id.tvPlateNumber);
        tvDriverName = findViewById(R.id.tvDriverName);
        tvDateTime = findViewById(R.id.tvDateTime);
        etLocation = findViewById(R.id.etLocation);

        btnBack = findViewById(R.id.btnBack);
        btnAddViolation = findViewById(R.id.btnAddViolation);
        btnFinalizeTicket = findViewById(R.id.btnFinalizeTicket);

        dropdownViolationType = findViewById(R.id.dropdownViolationType);
        dropdownPenaltyType = findViewById(R.id.dropdownPenaltyType);
        etDueDate = findViewById(R.id.etDueDate);
        rvSelectedViolations = findViewById(R.id.rvSelectedViolations);
        tvTotalFine = findViewById(R.id.tvTotalFine);

        // Cấu hình RecyclerView hiển thị danh sách vi phạm
        rvSelectedViolations.setLayoutManager(new LinearLayoutManager(this));
        violationAdapter = new ViolationAdapter(selectedViolations);
        violationAdapter.setOnViolationDeleteListener(this);
        rvSelectedViolations.setAdapter(violationAdapter);

        // Gán adapter cho dropdown hành vi và hình phạt
        dropdownViolationType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, violationNames));
        dropdownPenaltyType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, penaltyTypes));

        // Cài đặt ngày mặc định cho hạn giải quyết là 15 ngày sau
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 15);
        etDueDate.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
    }

    // Lấy dữ liệu được truyền từ màn trước qua intent
    private void extractIntentData() {
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("ticketData");

        if (bundle != null) {
            type = bundle.getInt("type");
            tvCCCD.setText(bundle.getString("driverCCCD"));
            tvPlateNumber.setText(bundle.getString("licensePlate"));
            tvDriverName.setText(bundle.getString("driverName"));

            // Hiển thị thời gian hiện tại
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            tvDateTime.setText(sdf.format(new Date()));

            // Kiểm tra quyền vị trí và lấy vị trí hiện tại
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                getCurrentLocation();
            }
        }
    }

    // Cài đặt các sự kiện click cho các nút và trường nhập
    private void setupEventListeners() {
        // Nút quay lại màn trước
        btnBack.setOnClickListener(v -> finish());

        // Nút thêm hành vi vi phạm
        btnAddViolation.setOnClickListener(v -> {
            // Lấy vị trí hành vi được chọn trong dropdown
            int index = dropdownViolationType.getListSelection();
            if (index == -1) index = findIndexByName(dropdownViolationType.getText().toString());

            // Nếu chọn hợp lệ, thêm vào danh sách
            if (index >= 0) {
                Violation violation = new Violation(violationNames[index], violationFines[index]);
                selectedViolations.add(violation);
                violationAdapter.notifyItemInserted(selectedViolations.size() - 1);
                updateTotalFine(); // Cập nhật tổng tiền phạt
            } else {
                Toast.makeText(this, "Hãy chọn hành vi vi phạm hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        // Hiển thị dialog chọn ngày khi click vào etDueDate
        etDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateTicketActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                        etDueDate.setText(formattedDate); // Gán ngày đã chọn vào ô nhập
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Nút hoàn tất tạo biên bản
        btnFinalizeTicket.setOnClickListener(v -> {
            // Kiểm tra có vi phạm nào chưa
            if (selectedViolations.isEmpty()) {
                Toast.makeText(this, "Vui lòng thêm ít nhất một vi phạm", Toast.LENGTH_SHORT).show();
                return;
            }
            // Kiểm tra hạn xử lý đã chọn chưa
            if (etDueDate.getText().toString().isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn thời hạn giải quyết", Toast.LENGTH_SHORT).show();
                return;
            }



            String formattedReportTime = null;
            try {
                Date reportDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                        .parse(tvDateTime.getText().toString());
                formattedReportTime = formatToIso8601(reportDate.getTime()); // Chuyển sang ISO 8601
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi định dạng ngày giờ", Toast.LENGTH_SHORT).show();
                return;
            }

            String formattedDeadline = null;
            try {
                Date deadlineDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(etDueDate.getText().toString());
                formattedDeadline = formatToIso8601(deadlineDate.getTime()); // Chuyển sang ISO 8601
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi phân tích thời hạn", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chỉ xử lý nếu là ô tô (type == 1)
            if (type == 1) {
                // Chuyển danh sách vi phạm sang dạng đối tượng chi tiết
                List<CarViolationDetail> violationDetails = new ArrayList<>();
                for (Violation item : selectedViolations) {
                    violationDetails.add(new CarViolationDetail(item.getName(), item.getFineAmount()));
                }

                // Tạo đối tượng report gửi lên server
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

                // Gọi API để gửi dữ liệu
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
                // Tạo đối tượng report gửi lên server
                // Chuyển danh sách vi phạm sang dạng đối tượng chi tiết
                List<MotorcycleViolationDetail> violationDetails = new ArrayList<>();
                for (Violation item : selectedViolations) {
                    violationDetails.add(new MotorcycleViolationDetail(item.getName(), item.getFineAmount()));
                }

                MotorcycleViolationReport motorcycleViolationReport = new MotorcycleViolationReport(
                        tvPlateNumber.getText().toString(),
                        tvCCCD.getText().toString(),
                        new SessionManager(this).getUserId(),
                        formattedReportTime,
                        etLocation.getText().toString(),
                        dropdownPenaltyType.getText().toString(),
                        formattedDeadline,
                        violationDetails
                );

                // Gọi API để gửi dữ liệu
                ApiService apiService = ApiClient.getClient(CreateTicketActivity.this).create(ApiService.class);
                apiService.createMotorcycleViolationReport(motorcycleViolationReport).enqueue(new Callback<MotorcycleViolationReport>() {
                    @Override
                    public void onResponse(Call<MotorcycleViolationReport> call, Response<MotorcycleViolationReport> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CreateTicketActivity.this, "Đã tạo biên bản", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(CreateTicketActivity.this, "Lỗi khi tạo biên bản", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<MotorcycleViolationReport> call, Throwable t) {
                        Toast.makeText(CreateTicketActivity.this, "Lỗi khi tạo biên bản", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // Hàm lấy vị trí hiện tại của thiết bị
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
                        // Dùng Geocoder để chuyển tọa độ thành địa chỉ
                        Geocoder geocoder = new Geocoder(CreateTicketActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                etLocation.setText(addresses.get(0).getAddressLine(0));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // Ngừng cập nhật sau khi lấy xong
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            }, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không có quyền truy cập vị trí!", Toast.LENGTH_SHORT).show();
        }
    }

    // Cập nhật tổng tiền phạt hiển thị lên giao diện
    private void updateTotalFine() {
        int total = 0;
        for (Violation v : selectedViolations) total += v.getFineAmount();
        tvTotalFine.setText(NumberFormat.getNumberInstance(Locale.US).format(total) + " VNĐ");
    }

    // Tìm index của hành vi theo tên trong danh sách
    private int findIndexByName(String name) {
        for (int i = 0; i < violationNames.length; i++) {
            if (violationNames[i].equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    // Định dạng thời gian sang chuẩn ISO 8601 theo giờ Việt Nam
    private String formatToIso8601(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // Định dạng chuẩn UTC

        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        int offsetMillis = vietnamTimeZone.getRawOffset();

        long adjustedTimestamp = timestamp + offsetMillis; // Chuyển về giờ Việt Nam
        return sdf.format(new Date(adjustedTimestamp));
    }

    // Gọi khi người dùng xóa 1 vi phạm trong danh sách
    @Override
    public void onViolationDelete(int position) {
        if (position >= 0 && position < selectedViolations.size()) {
            selectedViolations.remove(position);
            violationAdapter.notifyItemRemoved(position);
            updateTotalFine(); // Cập nhật tổng phạt mới
            Toast.makeText(this, "Đã xóa vi phạm", Toast.LENGTH_SHORT).show();
        }
    }

    // Gọi khi xin quyền truy cập vị trí và nhận kết quả
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

