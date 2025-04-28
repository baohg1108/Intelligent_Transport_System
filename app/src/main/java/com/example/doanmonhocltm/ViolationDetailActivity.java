package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.ViolationAll;
import com.example.doanmonhocltm.model.ViolationDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViolationDetailActivity extends AppCompatActivity {

    private static final String TAG = "ViolationDetailActivity";
    private ConstraintLayout mainLayout;
    private TextView tvReportId, tvStatus, tvPlate, tvOwner, tvPenaltyType;
    private TextView tvReportTime, tvDeadline, tvLocation;
    private TextView tvCarBrand, tvCarColor;
    private TextView tvOfficerName, tvOfficerId;
    private TextView tvTotalAmount, userName;
    private LinearLayout llViolationDetails;
    private ImageButton backButton;
    private CircleImageView userAvatar;
    private BottomNavigationView bottomNavigation;

    private int violationId;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_detail);

        // Get violation ID from intent
        Intent intent = getIntent();
        violationId = intent.getIntExtra("VIOLATION_ID", -1);
        type = intent.getIntExtra("TYPE", -1);
        if (violationId == -1 || type == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin vi phạm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupBottomNavigation();
        setupSessionInfo();
        fetchViolationDetails();
    }

    private void initViews() {
        // Initialize the main layout
        mainLayout = findViewById(R.id.main);

        // Initialize header views
        backButton = findViewById(R.id.backButton);
        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);

        // Initialize report info views
        tvReportId = findViewById(R.id.tvReportId);
        tvStatus = findViewById(R.id.tvStatus);
        tvPlate = findViewById(R.id.tvPlate);
        tvOwner = findViewById(R.id.tvOwner);
        tvPenaltyType = findViewById(R.id.tvPenaltyType);

        // Initialize time and location views
        tvReportTime = findViewById(R.id.tvReportTime);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvLocation = findViewById(R.id.tvLocation);

        // Initialize vehicle info views
        tvCarBrand = findViewById(R.id.tvCarBrand);
        tvCarColor = findViewById(R.id.tvCarColor);

        // Initialize officer info views
        tvOfficerName = findViewById(R.id.tvOfficerName);
        tvOfficerId = findViewById(R.id.tvOfficerId);

        // Initialize violation details container
        llViolationDetails = findViewById(R.id.llViolationDetails);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);

        // Setup back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupSessionInfo() {
        SessionManager sessionManager = new SessionManager(this);
        userName.setText(sessionManager.getNamePerson());

        Bitmap image = sessionManager.loadImageFromPrefs();
        if (image != null) {
            userAvatar.setImageBitmap(image);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(ViolationDetailActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(ViolationDetailActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(ViolationDetailActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }

    private void fetchViolationDetails() {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        Call<ViolationAll> call;
        if (type == 1)
        {
            call = apiService.getCarViolationById(violationId);
        }
        else
        {
            call = apiService.getMotorcycleViolationById(violationId);
        }

        call.enqueue(new Callback<ViolationAll>() {
            @Override
            public void onResponse(Call<ViolationAll> call, Response<ViolationAll> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayViolationDetails(response.body());
                } else {
                    Log.e(TAG, "Error fetching violation details: " + response.message());
                    Toast.makeText(ViolationDetailActivity.this,
                            "Lỗi khi tải thông tin chi tiết vi phạm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ViolationAll> call, Throwable t) {
                Log.e(TAG, "API call failed: ", t);
                Toast.makeText(ViolationDetailActivity.this,
                        "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayViolationDetails(ViolationAll violation) {
        // Set report ID and status
        tvReportId.setText("Biên bản #" + violation.getId());

        if (violation.isResolutionStatus()) {
            tvStatus.setText("Đã xử lý");
            tvStatus.setBackgroundResource(R.drawable.status_completed_bg);
        } else {
            tvStatus.setText("Chưa xử lý");
            tvStatus.setBackgroundResource(R.drawable.status_pending_bg);
        }

        // Set vehicle and owner info
        tvPlate.setText(violation.getLicensePlate());
        tvOwner.setText(violation.getViolatorName());
        tvPenaltyType.setText(violation.getPenaltyType());

        // Format and set dates
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date reportDate = inputFormat.parse(violation.getReportTime());
            tvReportTime.setText(outputFormat.format(reportDate));

            Date deadlineDate = inputFormat.parse(violation.getResolutionDeadline());
            tvDeadline.setText(dateOnlyFormat.format(deadlineDate));
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing error: ", e);
            tvReportTime.setText(violation.getReportTime());
            tvDeadline.setText(violation.getResolutionDeadline());
        }

        // Set location
        tvLocation.setText(violation.getReportLocation());

        // Set vehicle info

        tvCarBrand.setText(capitalize(violation.getCarBrand()));
        tvCarColor.setText(capitalize(violation.getCarColor()));




        // Set officer info
        tvOfficerName.setText(violation.getOfficerName());
        tvOfficerId.setText(violation.getOfficerId());

        // Display violation details
        displayViolationItems(violation.getViolationDetails());
    }

    private void displayViolationItems(List<ViolationDetail> violationDetails) {
        // Clear previous items
        llViolationDetails.removeAllViews();

        double totalAmount = 0;

        for (ViolationDetail detail : violationDetails) {
            View itemView = getLayoutInflater().inflate(R.layout.item_violation_detail, llViolationDetails, false);

            TextView tvViolationType = itemView.findViewById(R.id.tvViolationType);
            TextView tvFineAmount = itemView.findViewById(R.id.tvViolationFine);

            tvViolationType.setText(detail.getViolationType());

            NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            tvFineAmount.setText(currencyFormatter.format(detail.getFineAmount()) + " VNĐ");

            llViolationDetails.addView(itemView);

            totalAmount += detail.getFineAmount();
        }

        // Set total amount
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalAmount.setText(currencyFormatter.format(totalAmount) + " VNĐ");
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}