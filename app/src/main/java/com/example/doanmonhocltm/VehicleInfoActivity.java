// VehicleInfoActivity.java
package com.example.doanmonhocltm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VehicleInfoActivity extends AppCompatActivity {


    private TextView tvOwner, tvPlate, tvBrand, tvColor, tvLastScan, tvViolationInfo;


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

        tvOwner = findViewById(R.id.tvOwner);
        tvPlate = findViewById(R.id.tvPlate);
        tvBrand = findViewById(R.id.tvBrand);
        tvColor = findViewById(R.id.tvColor);
        tvLastScan = findViewById(R.id.tvLastScan);
        tvViolationInfo = findViewById(R.id.tvViolationInfo);

        // Lay du lieu Intent
        Intent intent = getIntent();

        // Lay du lieu bunlde

        Bundle bundle = intent.getBundleExtra("carInfor");

        String licensePlate = bundle.getString("licensePlate");
        String brand = bundle.getString("brand");
        String color = bundle.getString("color");


        tvPlate.setText(licensePlate);
        tvBrand.setText(brand);
        tvColor.setText(color);




    }
}