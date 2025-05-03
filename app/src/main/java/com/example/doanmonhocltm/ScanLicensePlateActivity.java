package com.example.doanmonhocltm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.Person;
import com.example.doanmonhocltm.model.ScanLog;
import com.example.doanmonhocltm.model.Vehicles;
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
    private int vehicleType;

    private static final String TAG = "Scan_LicensePlate";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    // UI Elements
    private PreviewView previewView;
    private TextView tvLicensePlate;
    private Button btnCapture;
    private FloatingActionButton btnBack;
    private ImageButton btnSwitchCamera;

    // Camera & Recognition Components
    private ExecutorService cameraExecutor;
    private ImageAnalysis imageAnalysis;
    private TextRecognizer textRecognizer;
    private CameraSelector cameraSelector;

    // State variables
    private String detectedLicensePlate = "";
    private boolean isProcessingImage = false;
    private int currentCameraSelector = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_license_plate);
        setupEdgeToEdge();
        vehicleType = getIntent().getIntExtra("type", -1);
        initializeViews();
        setupListeners();

        // Initialize camera components
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        // Check permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void setupEdgeToEdge() {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        btnCapture = findViewById(R.id.btnCapture);
        btnBack = findViewById(R.id.btnBack);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCapture.setOnClickListener(v -> startLicensePlateRecognition(vehicleType));
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
    }

    private void switchCamera() {
        // Switch between front and back camera
        currentCameraSelector = (currentCameraSelector == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCameraSelector)
                .build();

        startCamera();
    }

//    private void showVehicleTypeDialog() {
//        final Dialog dialog = new Dialog(this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_vehicle_type);
//
//        setupDialogWindow(dialog);
//        setupDialogViews(dialog);
//
//        dialog.show();
//    }

//    private void setupDialogWindow(Dialog dialog) {
//        Window window = dialog.getWindow();
//        if (window != null) {
//            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        }
//    }

//    private void setupDialogViews(Dialog dialog) {
//        TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
//        LinearLayout carOption = dialog.findViewById(R.id.option_car);
//        LinearLayout motorcycleOption = dialog.findViewById(R.id.option_motorcycle);
//        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
//
//        dialogTitle.setText("Chọn loại phương tiện");
//
//        carOption.setOnClickListener(v -> {
//            startLicensePlateRecognition("Xe ô tô");
//            dialog.dismiss();
//        });
//
//        motorcycleOption.setOnClickListener(v -> {
//            startLicensePlateRecognition("Xe máy");
//            dialog.dismiss();
//        });
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//    }

    private void startLicensePlateRecognition(int vehicleType) {
        isProcessingImage = false;
        tvLicensePlate.setText("Đang quét biển số...");
        setupImageAnalyzer(vehicleType);
    }

    private void setupImageAnalyzer(int vehicleType) {
        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class)
            @Override
            public void analyze(@NonNull androidx.camera.core.ImageProxy imageProxy) {
                Image mediaImage = imageProxy.getImage();
                if (mediaImage != null && !isProcessingImage) {
                    isProcessingImage = true;
                    InputImage image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.getImageInfo().getRotationDegrees()
                    );
                    processImageForLicensePlate(image, imageProxy, vehicleType);
                } else {
                    imageProxy.close();
                }
            }
        });
    }

    private void processImageForLicensePlate(InputImage image, androidx.camera.core.ImageProxy imageProxy, int vehicleType) {
        textRecognizer.process(image)
                .addOnSuccessListener(text -> {
                    String detectedText = text.getText();
                    String licensePlate = extractLicensePlate(detectedText);

                    if (licensePlate != null && !licensePlate.isEmpty()) {
                        detectedLicensePlate = licensePlate;
                        tvLicensePlate.setText(licensePlate);

                        // Process the license plate with the API
                        String cleanedPlate = cleanLicensePlate(licensePlate);
                        lookupVehicleInformation(cleanedPlate, vehicleType);

                        // Stop analyzing more frames
                        imageAnalysis.clearAnalyzer();
                    } else {
                        tvLicensePlate.setText("Không tìm thấy biển số xe");
                        isProcessingImage = false;
                    }

                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text recognition failed: " + e.getMessage());
                    tvLicensePlate.setText("Lỗi nhận dạng văn bản");
                    isProcessingImage = false;
                    imageProxy.close();
                });
    }

    private String cleanLicensePlate(String licensePlate) {
        return licensePlate.replace("-", "").replace(".", "").replace(" ", "");
    }

    private void lookupVehicleInformation(String licensePlate, int vehicleType) {
        if (licensePlate.trim().isEmpty()) {
            Toast.makeText(this, "Không tìm thấy biển số xe", Toast.LENGTH_SHORT).show();
            isProcessingImage = false;
            return;
        }


        if (vehicleType == -1) {
            Toast.makeText(this, "Lỗi chọn phương tiện", Toast.LENGTH_SHORT).show();
            isProcessingImage = false;
            return;
        }

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        Call<Vehicles> vehiclesCall = (vehicleType == 1)
                ? apiService.getCar(licensePlate)
                : apiService.getMotorcycle(licensePlate);

        vehiclesCall.enqueue(new Callback<Vehicles>() {
            @Override
            public void onResponse(Call<Vehicles> call, Response<Vehicles> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Vehicles vehicle = response.body();
                    fetchOwnerInformation(apiService, vehicle, vehicleType);
                } else {
                    handleApiError("Không tìm thấy thông tin xe", response.code());
                }
            }

            @Override
            public void onFailure(Call<Vehicles> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }

//    private int getVehicleTypeCode(String vehicleType) {
//        return vehicleType.equals("Xe ô tô") ? 1 :
//                vehicleType.equals("Xe máy") ? 2 : -1;
//    }

    private void fetchOwnerInformation(ApiService apiService, Vehicles vehicle, int vehicleTypeCode) {
        Call<Person> personCall = apiService.getPersonById(vehicle.getOwnerId());

        personCall.enqueue(new Callback<Person>() {
            @Override
            public void onResponse(Call<Person> call, Response<Person> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Person owner = response.body();
                    createScanLogAndNavigate(apiService, vehicle, owner, vehicleTypeCode);
                } else {
                    handleApiError("Không tìm thấy thông tin chủ xe", response.code());
                }
            }

            @Override
            public void onFailure(Call<Person> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }

    private void createScanLogAndNavigate(ApiService apiService, Vehicles vehicle, Person owner, int vehicleTypeCode) {
        ScanLog scanLog = new ScanLog(vehicle.getLicensePlate(), vehicle.getOwnerId());
        Call<ScanLog> scanLogCall = (vehicleTypeCode == 1)
                ? apiService.createCarScanLog(scanLog)
                : apiService.createMotorcycleScanLog(scanLog);

        scanLogCall.enqueue(new Callback<ScanLog>() {
            @Override
            public void onResponse(Call<ScanLog> call, Response<ScanLog> response) {
                if (response.isSuccessful()) {
                    navigateToVehicleInfo(vehicle, owner, vehicleTypeCode);
                } else {
                    handleApiError("Không thể lưu lịch sử quét", response.code());
                }
            }

            @Override
            public void onFailure(Call<ScanLog> call, Throwable t) {
                handleNetworkError(t);
            }
        });
    }

    private void navigateToVehicleInfo(Vehicles vehicle, Person owner, int vehicleTypeCode) {
        Intent intent = new Intent(this, VehicleInfoActivity.class);
        Bundle bundle = new Bundle();

        bundle.putInt("type", vehicleTypeCode);
        bundle.putString("licensePlate", vehicle.getLicensePlate());
        bundle.putString("brand", vehicle.getBrand());
        bundle.putString("color", vehicle.getColor());
        bundle.putString("owner", owner.getFullName());

        intent.putExtra("Infor", bundle);
        startActivity(intent);
    }

    private void handleApiError(String message, int errorCode) {
        Toast.makeText(this, message + ". Mã lỗi: " + errorCode, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "API Error: " + message + " Code: " + errorCode);
        isProcessingImage = false;
    }

    private void handleNetworkError(Throwable t) {
        Toast.makeText(this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Network Error: " + t.getMessage());
        t.printStackTrace();
        isProcessingImage = false;
    }

    private String extractLicensePlate(String text) {
        // Xử lý văn bản đầu vào để loại bỏ các khoảng trắng không cần thiết
        text = text.replace("\n", " ").trim();

        // Mảng các regex patterns cho các loại biển số khác nhau
        String[][] regexPatterns = {
                // Biển số xe ô tô
                {"\\b(\\d{2}[A-Z]-\\d{3}\\.\\d{2})\\b", "\\b(\\d{2}[A-Z][- ]?\\d{3}[. ]?\\d{2})\\b"},

                // Biển số xe máy điện
                {"\\b(\\d{2}\\s*-\\s*(MĐ|ĐK|MD)\\d\\s*\\d{5})\\b", "\\b(\\d{2}\\s*-?\\s*(MĐ|ĐK|MD)\\d\\s*\\d{5})\\b"},

                // Biển số xe máy 50cc
                {"\\b(\\d{2}\\s*-\\s*MĐ\\d\\s*\\d{5})\\b"},

                // Biển số xe máy thông thường
                {"\\b(\\d{2}\\s*-\\s*([A-Z]\\d|[A-Z]{2})\\s*\\d{5})\\b", "\\b(\\d{2}\\s*-?\\s*([A-Z]\\d|[A-Z]{2})\\s*\\d{5})\\b"},

                // Bất kỳ chuỗi số/chữ cái nào có thể là biển số
                {"\\b(\\d{2}\\s*-?\\s*([A-Z]{1,2}|MĐ|ĐK|MD)\\d?\\s*\\d{3,5}[.\\s]?\\d{0,2})\\b"}
        };

        // Duyệt qua từng pattern và tìm biển số
        for (String[] patternGroup : regexPatterns) {
            for (String regex : patternGroup) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    return matcher.group(0);
                }
            }
        }

        return null;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        // Thiết lập preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Thiết lập image analysis
        imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        // Unbind use cases trước khi rebind
        cameraProvider.unbindAll();

        // Bind use cases với camera
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}