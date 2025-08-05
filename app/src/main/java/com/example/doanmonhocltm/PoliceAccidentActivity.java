package com.example.doanmonhocltm;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.net.Uri; // get location realtime map

import com.example.doanmonhocltm.InformationActivity;
import com.example.doanmonhocltm.PoliceNoAccidentActivity;
import com.example.doanmonhocltm.SettingActivity;

public class PoliceAccidentActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police_accident);

        //get id
      Button btnDecline = findViewById(R.id.btnDecline);
      Button btnAccept = findViewById(R.id.btnAccept);
      View btnUserMapView = findViewById(R.id.accidentUserMapView);

        // nếu từ chối thì sẽ tự chuyển về trang khi chưa nhận tín hiệu PoliceNoAccidentActivity
        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(PoliceAccidentActivity.this, PoliceNoAccidentActivity.class);
                startActivity(intent);
            }
        });

        // nếu nhấn Accept thì id=AccidentUserMapView hiển thị Map tìm đường ngắn nhất đến chỗ tai nạn đó
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // location - api
                double latitude_destination = 10.7758; // vi do
                double longitude_destination = 106.7017; // Kinh do

                // create url open map
                String uri = "http://maps.google.com/maps?daddr=" + latitude_destination + "," + longitude_destination;
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

                // name pâckage
                mapIntent.setPackage("com.google.android.apps.maps");

                // kiem tra application map open map
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });


    }

}