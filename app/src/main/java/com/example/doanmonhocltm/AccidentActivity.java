package com.example.doanmonhocltm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class AccidentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private double userLatitude;
    private double userLongitude;
    private double accidentLatitude;
    private double accidentLongitude;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);

        // tạo map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.accidentUserMapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // get data từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            userLatitude = intent.getDoubleExtra("userLatitude", 0.0);
            userLongitude = intent.getDoubleExtra("userLongitude", 0.0);
            accidentLatitude = intent.getDoubleExtra("accidentLatitude", 0.0);
            accidentLongitude = intent.getDoubleExtra("accidentLongitude", 0.0);
            String distance = intent.getStringExtra("distance");
            String currentTime = intent.getStringExtra("currentTime");

            TextView tvDistance = findViewById(R.id.tvDistance);
            TextView tvCoordinates = findViewById(R.id.tvCoordinates);
            TextView tvReceivedTime = findViewById(R.id.tvReceivedTime);

            tvDistance.setText("Khoảng cách ước tính: " + distance);
            tvReceivedTime.setText(currentTime);
            tvCoordinates.setText(String.format(Locale.getDefault(), "%.4f° N, %.4f° E", accidentLatitude, accidentLongitude));
        }

        Button btnDecline = findViewById(R.id.btnDecline);
        Button btnAccept = findViewById(R.id.btnAccept);

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccidentActivity.this, NoAccidentActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null) {
                    // zoom tới vị trí tai nạn
                    LatLng accidentLocation = new LatLng(accidentLatitude, accidentLongitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 15));

                    // mở map
                    String uri = "https://www.google.com/maps/dir/?api=1" +
                            "&origin=" + userLatitude + "," + userLongitude +
                            "&destination=" + accidentLatitude + "," + accidentLongitude +
                            "&travelmode=driving";
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    mapIntent.setPackage("com.google.android.apps.maps");

                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        Toast.makeText(AccidentActivity.this, "Ứng dụng Google Maps không được cài đặt.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // điểm đánh dấu vị trí tai nạn
        LatLng accidentLocation = new LatLng(accidentLatitude, accidentLongitude);
        mMap.addMarker(new MarkerOptions()
                .position(accidentLocation)
                .title("Vị trí tai nạn"));

        // zôom tới cái vị trí mà tai nạn
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(accidentLocation, 15));
    }
}