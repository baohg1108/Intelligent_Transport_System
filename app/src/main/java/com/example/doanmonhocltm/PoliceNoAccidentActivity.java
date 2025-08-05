package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

import com.example.doanmonhocltm.HomeActivity;
import com.example.doanmonhocltm.InformationActivity;
import com.example.doanmonhocltm.SettingActivity;

public class PoliceNoAccidentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_no_accident);

        //find va gan LinearLayout tu XML bang ID
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnInformation = findViewById(R.id.btnInformation);
        LinearLayout btnAccident = findViewById(R.id.btnAccident);
        LinearLayout btnSetting = findViewById(R.id.btnSetting);
        Button accidentBtnReportAccident = findViewById(R.id.accidentBtnReportAccident);

        //nhấn Home tự chuyển lại Home
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceNoAccidentActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // nhấn Information chuyển Information
        btnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceNoAccidentActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });


        //nhấn Tai nạn chuyển Tai nạn
        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceNoAccidentActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

        //nhấn cài đăt chuyển cài đặt
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceNoAccidentActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });



        accidentBtnReportAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceNoAccidentActivity.this, PoliceAccidentActivity.class);
                startActivity(intent);
            }
        });
    }

}