package com.example.doanmonhocltm.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import com.example.doanmonhocltm.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoAccidentActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_accident);

        // Khởi tạo FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // lấy SupportMapFragment and thông báo khi bản đồ đã sẵn sàng
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.accidentMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // xử lý nút điều hướng, nếu thanh điều hướng dưới thay đổi ý m, thì m đổi sau nha
        findViewById(R.id.btnHome).setOnClickListener(view -> {
            Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnInformation).setOnClickListener(view -> {
            Toast.makeText(this, "Thông tin", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnStatistics).setOnClickListener(view -> {
            Toast.makeText(this, "Tai nạn", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnSettings).setOnClickListener(view -> {
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
        });

        // xử lý nút hiển thị menu
        FloatingActionButton mapTypeFab = findViewById(R.id.mapTypeFab);
        if (mapTypeFab != null) {
            mapTypeFab.setOnClickListener(this::showMapTypeMenu);
        }

        // xử lý nút chuyển trang
        Button reportAccidentButton = findViewById(R.id.accidentBtnReportAccident);
        if (reportAccidentButton != null) {
            reportAccidentButton.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.getLastLocation()
                            .addOnSuccessListener(this, location -> {
                                if (location != null) {
                                    // tọa độ tai nạn chính là tọa độ người dùng
                                    double accidentLat = location.getLatitude();
                                    double accidentLon = location.getLongitude();

                                    double userLat = location.getLatitude();
                                    double userLon = location.getLongitude();

                                    // tọa đồ người dùng để tính
                                    double distanceInKm = calculateDistance(userLat, userLon, accidentLat, accidentLon);
                                    String distanceString = String.format(Locale.getDefault(), "%.2f km", distanceInKm);

                                    String currentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());

                                    Intent intent = new Intent(NoAccidentActivity.this, AccidentActivity.class);

                                    // gắn cái tọa độ tai nạn và user vào Intent , để bên kia lấy
                                    intent.putExtra("accidentLatitude", accidentLat);
                                    intent.putExtra("accidentLongitude", accidentLon);
                                    intent.putExtra("userLatitude", userLat);
                                    intent.putExtra("userLongitude", userLon);
                                    intent.putExtra("distance", distanceString);
                                    intent.putExtra("currentTime", currentTime);

                                    startActivity(intent);
                                } else {
                                    Toast.makeText(NoAccidentActivity.this, "Không thể lấy vị trí hiện tại. Vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(this, "Vui lòng cấp quyền truy cập vị trí để báo cáo tai nạn", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                            } else {
                                LatLng defaultLocation = new LatLng(21.028511, 105.804817);
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
                                Toast.makeText(NoAccidentActivity.this, "Không tìm thấy vị trí. Hiển thị vị trí mặc định", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền truy cập vị trí để sử dụng chức năng này", Toast.LENGTH_LONG).show();
            }
        }
    }


    // tính khoảng cách  - bán kính Trái Đất là 6371 km , return kết quả cũng là km
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // list style bản đồ
    private void showMapTypeMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenuInflater().inflate(R.menu.map_type_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (mMap != null) {
                int itemId = item.getItemId();
                if (itemId == R.id.map_type_normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                } else if (itemId == R.id.map_type_hybrid) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                } else if (itemId == R.id.map_type_satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                } else if (itemId == R.id.map_type_terrain) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                }
            }
            return false;
        });
        popup.show();



    }
}