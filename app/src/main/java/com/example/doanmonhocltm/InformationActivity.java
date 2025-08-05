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

        //find va gan LinearLayout tu XML bang ID
        LinearLayout informationHeader = findViewById(R.id.informationHeader);
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnInformation = findViewById(R.id.btnInformation);
        LinearLayout btnAccident = findViewById(R.id.btnAccident);
        LinearLayout btnSetting = findViewById(R.id.btnSetting);

        // sủ lý cái phần information đầu (logo, name, ...) nhấn vào qua page Thông tin luôn
        informationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        //nhấn Home tự chuyển lại Home
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // nhấn Information chuyển Information
        btnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });


        //nhấn Tai nạn chuyển Tai nạn
        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

        //nhấn cài đăt chuyển cài đặt
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(InformationActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

}