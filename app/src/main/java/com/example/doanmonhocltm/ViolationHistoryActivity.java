package com.example.doanmonhocltm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doanmonhocltm.adapter.ViolationAllAdapter;
import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.ViolationAll;
import com.example.doanmonhocltm.model.ViolationDetail;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViolationHistoryActivity extends AppCompatActivity implements ViolationAllAdapter.OnViolationClickListener {

    private static final String TAG = "ViolationHistoryActivity";
    private RecyclerView violationRecyclerView;
    private ViolationAllAdapter adapter;
    private TextView tvPlate, tvOwner, tvViolationCount, tvTotalFine, tvCommonViolation, userName;
    private ImageButton backButton;
    private CircleImageView userAvatar;
    private BottomNavigationView bottomNavigation;
    private String licensePlate;

    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_history);

        // Get license plate from intent
        Intent intent = getIntent();
        Bundle vehicleBundle = intent.getBundleExtra("vehicleData");
        licensePlate = vehicleBundle.getString("licensePlate");
        type = vehicleBundle.getInt("type");

//        licensePlate = intent.getStringExtra("LICENSE_PLATE");
//        if (licensePlate == null) {
//            // For testing purposes, use a default license plate
//            licensePlate = "85C140689";
//        }

        initViews();
        setupBottomNavigation();
        fetchViolationData();
    }

    private void initViews() {
        // Initialize views
        tvPlate = findViewById(R.id.tvPlate);
        tvOwner = findViewById(R.id.tvOwner);
        tvViolationCount = findViewById(R.id.tvViolationCount);
        tvTotalFine = findViewById(R.id.tvTotalFine);
        tvCommonViolation = findViewById(R.id.tvCommonViolation);
        backButton = findViewById(R.id.backButton);
        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);

        // Set license plate
        tvPlate.setText(licensePlate);

        // Setup RecyclerView
        violationRecyclerView = findViewById(R.id.violationRecyclerView);
        violationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //__________________________________________________________________________________________________________
            SessionManager sessionManager = new SessionManager(ViolationHistoryActivity.this);
        userName = findViewById(R.id.userName);
        userName.setText(sessionManager.getNamePerson());
        //__________________________________________________________________________________________________________

        //__________________________________________________________________________________________________________
        userAvatar = findViewById(R.id.userAvatar);
        Bitmap image = sessionManager.loadImageFromPrefs();
        userAvatar.setImageBitmap(image);
        //__________________________________________________________________________________________________________

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(ViolationHistoryActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(ViolationHistoryActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(ViolationHistoryActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }

    private void fetchViolationData() {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        Call<List<ViolationAll>> call;
        Log.e(TAG, "TYPE" + type );
        if (type == 1)
        {
            call = apiService.getCarViolationsByLicensePlate(licensePlate);
        }
        else
        {
            call = apiService.getMotorcycleViolationsByLicensePlate(licensePlate);
        }

        call.enqueue(new Callback<List<ViolationAll>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<List<ViolationAll>> call, Response<List<ViolationAll>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ViolationAll> violations = response.body();
                    Log.e(TAG,"onResponse: " + response.body() );
                    updateUI(violations);
                } else {
                    Log.e(TAG, "Error fetching violations: " + response.message());
                    Toast.makeText(ViolationHistoryActivity.this,
                            "Lỗi khi tải dữ liệu vi phạm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ViolationAll>> call, Throwable t) {
                Log.e(TAG, "API call failed: ", t);
                Toast.makeText(ViolationHistoryActivity.this,
                        "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateUI(List<ViolationAll> violations) {
        if (violations.isEmpty()) {
            Toast.makeText(this, "Không có dữ liệu vi phạm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set owner name from first violation
        tvOwner.setText(violations.get(0).getViolatorName());

        // Set violation count
        tvViolationCount.setText(String.valueOf(violations.size()));

        // Calculate total fine
        float totalFine = 0;
        Map<String, Integer> violationCounts = new HashMap<>();

        for (ViolationAll violation : violations) {
            totalFine += violation.getTotalFine();

            // Count occurrence of each violation type
            for (ViolationDetail detail : violation.getViolationDetails()) {
                String type = detail.getViolationType();
                violationCounts.put(type, violationCounts.getOrDefault(type, 0) + 1);
            }
        }

        // Format and set total fine
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalFine.setText(currencyFormatter.format(totalFine) + " VNĐ");

        // Find most common violation
        String mostCommonViolation = "";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : violationCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostCommonViolation = entry.getKey();
            }
        }

        tvCommonViolation.setText(mostCommonViolation);

        // Set up the adapter and RecyclerView
        adapter = new ViolationAllAdapter(this, violations, this);
        violationRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDetailClick(ViolationAll violation) {
        // Handle detail click
        Intent intent = new Intent(this, ViolationDetailActivity.class);
        intent.putExtra("VIOLATION_ID", violation.getId());
        intent.putExtra("TYPE", type);
        startActivity(intent);
    }

    @Override
    public void onResolveClick(ViolationAll violation) {
        // Handle resolve click
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("VIOLATION_ID", violation.getId());
        startActivity(intent);
    }
}