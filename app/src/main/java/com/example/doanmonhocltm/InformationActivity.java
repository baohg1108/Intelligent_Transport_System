package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doanmonhocltm.HomeActivity;
import com.example.doanmonhocltm.PoliceNoAccidentActivity;
import com.example.doanmonhocltm.SettingActivity;

public class InformationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        //get id from .xml
        LinearLayout informationHeader = findViewById(R.id.informationHeader);
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnInformation = findViewById(R.id.btnInformation);
        LinearLayout btnAccident = findViewById(R.id.btnAccident);
        LinearLayout btnSetting = findViewById(R.id.btnSetting);

        // xử lý header: nhấn vào chuyển qua page InformationActivity
        informationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        // nhấn quay trở lại HomeActivity
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // nhấn Thông tin vẫn giữa nguyên page  Information
        btnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });


        // chuyển tab tai nạn: mặc địch là chưa có tai nạn
        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

        // chuyển đến cài đặt
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

}