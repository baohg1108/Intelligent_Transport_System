package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.example.doanmonhocltm.InformationActivity;
import com.example.doanmonhocltm.PoliceNoAccidentActivity;
import com.example.doanmonhocltm.SettingActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //get id from .xml
        LinearLayout informationHeader = findViewById(R.id.informationHeader);
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnInformation = findViewById(R.id.btnInformation);
        LinearLayout btnAccident = findViewById(R.id.btnAccident);
        LinearLayout btnSetting = findViewById(R.id.btnSetting);
        Button accidentBtnReportAccident = findViewById(R.id.accidentBtnReportAccident);

        // process header , auto next page information when click
        informationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        // home
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // next page information
        btnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });


        // next page accident: default là chưa có tai nạn xảy ra nên trang PoliceNoAccidentActivity luôn xuất hiện
        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

    // next page setting
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // next page PoliceAccidentActivity khi báo cáo có tai nạn
        accidentBtnReportAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(HomeActivity.this, PoliceAccidentActivity.class);
                startActivity(intent);
            }
        });
    }

}