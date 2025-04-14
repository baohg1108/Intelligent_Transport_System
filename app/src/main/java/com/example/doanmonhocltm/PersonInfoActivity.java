package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PersonInfoActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigation;

    private TextView tvPersonId;
    private TextView tvFullName;
    private TextView tvBirthDate;
    private TextView tvGender;
    private TextView tvAddress;
    private TextView tvPhoneNumber;
    private ImageView personImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_person_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo các thành phần giao diện
        initializeViews();

        // Đặt dữ liệu cho các thành phần giao diện
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("result");
        if (bundle != null) {
            String personId = bundle.getString("id");
            String fullName = bundle.getString("fullName");
            Long birthDate = bundle.getLong("birthDate");
            String gender = bundle.getString("gender");
            String address = bundle.getString("address");
            String phoneNumber = bundle.getString("phoneNumber");


            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(birthDate));


            tvPersonId.setText(personId);
            tvFullName.setText(fullName);
            tvBirthDate.setText(formattedDate);
            tvGender.setText(gender);
            tvAddress.setText(address);
            tvPhoneNumber.setText(phoneNumber);

        }


        // Thiết lập các sự kiện
        setupEventListeners();
    }

    private void setupEventListeners() {
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_tra_nguoi_lai) {
                    startActivity(new Intent(PersonInfoActivity.this, FindPersonActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_tra_bien_so) {
                    startActivity(new Intent(PersonInfoActivity.this, FindLicensePlateActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;

                } else if (id == R.id.nav_thong_tin_user) {
                    startActivity(new Intent(PersonInfoActivity.this, UserInfoActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }

                return false;
            }
        });
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_tra_nguoi_lai);

        tvPersonId = findViewById(R.id.tvPersonId);
        tvFullName = findViewById(R.id.tvFullName);
        tvBirthDate = findViewById(R.id.tvBirthDate);
        tvGender = findViewById(R.id.tvGender);
        tvAddress = findViewById(R.id.tvAddress);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        personImage = findViewById(R.id.personImage);


    }

}