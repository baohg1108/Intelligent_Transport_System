package com.example.doanmonhocltm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.Car;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanLicensePlateActivity extends AppCompatActivity {
    private static final String TAG = "Scan_Bike";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private TextView tvLicensePlate;
    private Button btnCapture;
    private Button btnConfirm;
    private FloatingActionButton btnBack;

    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private TextRecognizer textRecognizer;
    private String detectedLicensePlate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_license_plate);

        previewView = findViewById(R.id.previewView);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        btnCapture = findViewById(R.id.btnCapture);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnBack = findViewById(R.id.btnBack);

        // Thiết lập nút quay lại
        btnBack.setOnClickListener(v -> {
            finish(); // Kết thúc activity hiện tại và quay lại activity trước đó
        });

        // Khởi tạo text recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Ban đầu ẩn nút xác nhận vì chưa có biển số
        btnConfirm.setEnabled(false);

        // Kiểm tra quyền
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Thiết lập nút chụp
        btnCapture.setOnClickListener(v -> captureImage());

        // Thiết lập nút xác nhận
        btnConfirm.setOnClickListener(v -> showConfirmationDialog());

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void showConfirmationDialog() {
        if (detectedLicensePlate.isEmpty()) {
            Toast.makeText(this, "Vui lòng quét biển số trước", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận tra cứu")
                .setMessage("Bạn có xác nhận tra cứu biển số " + detectedLicensePlate + " này không?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Xử lý khi người dùng xác nhận
                    Toast.makeText(ScanLicensePlateActivity.this, "Đang tra cứu biển số: " + detectedLicensePlate, Toast.LENGTH_LONG).show();
                    // Thêm code xử lý tra cứu biển số ở đây
                    // Ví dụ: chuyển đến activity mới hoặc gửi request lên server


                    String cleanedPlate = detectedLicensePlate.replace("-", "").replace(".", "").replace(" ", "");


                    Log.e("BienSoXe", cleanedPlate);

                    ApiService apiService = ApiClient.getClient(ScanLicensePlateActivity.this).create(ApiService.class);

                    Call<Car> car = apiService.getCarByLicensePlate(cleanedPlate);

                    car.enqueue(new Callback<Car>() {

                        @Override
                        public void onResponse(Call<Car> call, Response<Car> response) {
                            if (response.isSuccessful()) {
                                Car car = response.body();

                                // Chuyển sang màn hình VehicleInfoActivity
                                Intent intent = new Intent(ScanLicensePlateActivity.this, VehicleInfoActivity.class);

                                Bundle bundle = new Bundle();

                                bundle.putString("licensePlate", car.getLicensePlate());
                                bundle.putString("brand", car.getBrand());
                                bundle.putString("color", car.getColor());

                                intent.putExtra("carInfor", bundle);
                                startActivity(intent);

                            } else {
                                System.out.println("Response error code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Car> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });


                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Thiết lập preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Thiết lập image capture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Thiết lập image analysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Chọn camera sau
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind use cases trước khi rebind
                cameraProvider.unbindAll();

                // Bind use cases với camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        if (imageCapture == null) {
            return;
        }

        tvLicensePlate.setText("Đang xử lý...");
        btnConfirm.setEnabled(false);
        detectedLicensePlate = "";

        imageCapture.takePicture(
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    @SuppressLint("UnsafeOptInUsageError")
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        Image mediaImage = imageProxy.getImage();
                        if (mediaImage != null) {
                            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                            processTextRecognition(image, imageProxy);
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Image capture failed: " + exception.getMessage());
                        tvLicensePlate.setText("Lỗi khi chụp ảnh");
                        btnConfirm.setEnabled(false);
                    }
                }
        );
    }

    private void processTextRecognition(InputImage image, ImageProxy imageProxy) {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    // Giải phóng tài nguyên
                    imageProxy.close();

                    // Xử lý kết quả nhận dạng văn bản
                    String detectedText = text.getText();
                    String licensePlate = extractLicensePlate(detectedText);

                    if (licensePlate != null && !licensePlate.isEmpty()) {
                        detectedLicensePlate = licensePlate;
                        tvLicensePlate.setText(licensePlate);
                        btnConfirm.setEnabled(true);
                    } else {
                        tvLicensePlate.setText("Không tìm thấy biển số xe");
                        btnConfirm.setEnabled(false);
                    }
                })
                .addOnFailureListener(e -> {
                    imageProxy.close();
                    Log.e(TAG, "Text recognition failed: " + e.getMessage());
                    tvLicensePlate.setText("Lỗi nhận dạng văn bản");
                    btnConfirm.setEnabled(false);
                });
    }

    private String extractLicensePlate(String text) {
        // Xử lý văn bản đầu vào để loại bỏ các khoảng trắng không cần thiết
        text = text.replace("\n", " ").trim();

        // Mẫu cho biển số xe ô tô: ví dụ 29A-123.45
        String carRegex = "\\b(\\d{2}[A-Z]-\\d{3}\\.\\d{2})\\b";

        // Mẫu cho biển số xe máy: ví dụ 59-D1 23456 hoặc 85-AA 12345
        String motorbikeRegex = "\\b(\\d{2}\\s*-\\s*([A-Z]\\d|[A-Z]{2})\\s*\\d{5})\\b";

        // Mẫu cho biển số xe máy 50cc: ví dụ 59-MĐ1 23456
        String moped50ccRegex = "\\b(\\d{2}\\s*-\\s*MĐ\\d\\s*\\d{5})\\b";

        // Mẫu cho biển số xe máy điện: ví dụ 59-MĐ1 23456, 59-ĐK1 23456 hoặc 85-MD1 67891
        String electricBikeRegex = "\\b(\\d{2}\\s*-\\s*(MĐ|ĐK|MD)\\d\\s*\\d{5})\\b";

        // Thử tìm biển số xe ô tô trước
        Pattern carPattern = Pattern.compile(carRegex);
        Matcher carMatcher = carPattern.matcher(text);
        if (carMatcher.find()) {
            return carMatcher.group(0);
        }

        // Tìm biển số xe máy điện
        Pattern electricBikePattern = Pattern.compile(electricBikeRegex);
        Matcher electricBikeMatcher = electricBikePattern.matcher(text);
        if (electricBikeMatcher.find()) {
            return electricBikeMatcher.group(0);
        }

        // Tìm biển số xe máy 50cc
        Pattern moped50ccPattern = Pattern.compile(moped50ccRegex);
        Matcher moped50ccMatcher = moped50ccPattern.matcher(text);
        if (moped50ccMatcher.find()) {
            return moped50ccMatcher.group(0);
        }

        // Tìm biển số xe máy thông thường
        Pattern motorbikePattern = Pattern.compile(motorbikeRegex);
        Matcher motorbikeMatcher = motorbikePattern.matcher(text);
        if (motorbikeMatcher.find()) {
            return motorbikeMatcher.group(0);
        }

        // Mở rộng tìm kiếm cho các biến thể có thể (khoảng trắng, thiếu dấu chấm/gạch ngang)
        // Xe ô tô: có thể thiếu dấu chấm hoặc gạch ngang
        String carRegexRelaxed = "\\b(\\d{2}[A-Z][- ]?\\d{3}[. ]?\\d{2})\\b";
        Pattern carPatternRelaxed = Pattern.compile(carRegexRelaxed);
        Matcher carMatcherRelaxed = carPatternRelaxed.matcher(text);
        if (carMatcherRelaxed.find()) {
            return carMatcherRelaxed.group(0);
        }

        // Xe máy điện/50cc: có thể thiếu khoảng trắng hoặc gạch ngang
        String electricBikeRegexRelaxed = "\\b(\\d{2}\\s*-?\\s*(MĐ|ĐK|MD)\\d\\s*\\d{5})\\b";
        Pattern electricBikePatternRelaxed = Pattern.compile(electricBikeRegexRelaxed);
        Matcher electricBikeMatcherRelaxed = electricBikePatternRelaxed.matcher(text);
        if (electricBikeMatcherRelaxed.find()) {
            return electricBikeMatcherRelaxed.group(0);
        }

        // Xe máy: có thể thiếu khoảng trắng hoặc gạch ngang
        String motorbikeRegexRelaxed = "\\b(\\d{2}\\s*-?\\s*([A-Z]\\d|[A-Z]{2})\\s*\\d{5})\\b";
        Pattern motorbikePatternRelaxed = Pattern.compile(motorbikeRegexRelaxed);
        Matcher motorbikeMatcherRelaxed = motorbikePatternRelaxed.matcher(text);
        if (motorbikeMatcherRelaxed.find()) {
            return motorbikeMatcherRelaxed.group(0);
        }

        // Nếu không tìm thấy theo các định dạng, thử tìm bất kỳ chuỗi số/chữ cái nào có thể là biển số
        String generalRegex = "\\b(\\d{2}\\s*-?\\s*([A-Z]{1,2}|MĐ|ĐK|MD)\\d?\\s*\\d{3,5}[.\\s]?\\d{0,2})\\b";
        Pattern generalPattern = Pattern.compile(generalRegex);
        Matcher generalMatcher = generalPattern.matcher(text);
        if (generalMatcher.find()) {
            return generalMatcher.group(0);
        }

        return null;
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Cần cấp quyền camera để sử dụng ứng dụng!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}