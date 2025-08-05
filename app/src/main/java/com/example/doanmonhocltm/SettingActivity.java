package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;

import com.example.doanmonhocltm.HomeActivity;
import com.example.doanmonhocltm.InformationActivity;
import com.example.doanmonhocltm.PoliceNoAccidentActivity;
import com.example.doanmonhocltm.LoginActivity;
import com.example.doanmonhocltm.TermsAndConditionsActivity;


public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //get id
        LinearLayout informationHeader = findViewById(R.id.informationHeader);
        LinearLayout btnHome = findViewById(R.id.btnHome);
        LinearLayout btnInformation = findViewById(R.id.btnInformation);
        LinearLayout btnAccident = findViewById(R.id.btnAccident);
        LinearLayout btnSetting = findViewById(R.id.btnSetting);
        LinearLayout btnAccountInfo = findViewById(R.id.btnAccountInfo);
        LinearLayout btnNotifications = findViewById(R.id.btnNotifications);
        LinearLayout btnTerms = findViewById(R.id.btnTerms);
        LinearLayout btnLogout = findViewById(R.id.btnLogout);

        //xử lý các link
        // thông tin tài khoản
        btnAccountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        // thông báo
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiển thị dialog thông báo đang phát triển
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("Thông báo")
                        .setMessage("Chức năng này đang được phát triển. Vui lòng thử lại sau.")
                        .setPositiveButton("OK", null) // Nút OK chỉ để đóng dialog
                        .show();
            }
        });

    // đièu khoản sử dụng
        btnTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent intent = new Intent(SettingActivity.this, TermsAndConditionsActivity.class);
                 startActivity(intent);
            }
        });

        // đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiển thị dialog xác nhận đăng xuất
                new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("Xác nhận đăng xuất")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                        .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                 Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                                 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                 startActivity(intent);
                                 finish();
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });



        // nhấn header chuyển page Information
        informationHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });

        // nhấn Home tự chuyển lại Home
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // nhấn Information chuyển Information
        btnInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingActivity.this, InformationActivity.class);
                startActivity(intent);
            }
        });


        //nhấn Tai nạn chuyển Tai nạn, mặc định chưa có tai nạn
        btnAccident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

        //nhấn cài đăt chuyển cài đặt
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SettingActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
    }

}