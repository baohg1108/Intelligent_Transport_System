package com.example.doanmonhocltm;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.doanmonhocltm.callapi.ApiClient;
import com.example.doanmonhocltm.callapi.ApiService;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanPersonActivity extends AppCompatActivity {
    private static final String TAG = "ScanPersonActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private MaterialButton btnCaptureFace;
    private MaterialButton btnConfirmFace;
    private FloatingActionButton btnBack;

    private ImageCapture imageCapture;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Uri savedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_person);

        initializeViews();
        setupWindowInsets();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        setupButtons();
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        btnCaptureFace = findViewById(R.id.btnCaptureFace);
        btnConfirmFace = findViewById(R.id.btnConfirmFace);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupButtons() {
        btnCaptureFace.setOnClickListener(v -> captureImage());

        btnConfirmFace.setOnClickListener(v -> {
            if (savedImageUri != null) {
                sendImageToApi(savedImageUri);
            } else {
                Toast.makeText(this, "Vui lòng chụp ảnh trước", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Thiết lập preview camera
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Thiết lập image capture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Thử sử dụng camera trước, nếu không có thì dùng camera sau
                CameraSelector cameraSelector;
                try {
                    cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build();

                    // Kiểm tra xem có thể sử dụng camera trước không
                    if (!cameraProvider.hasCamera(cameraSelector)) {
                        Log.w(TAG, "Camera trước không khả dụng, chuyển sang camera sau");
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Không thể sử dụng camera trước: " + e.getMessage());
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                }

                // Xóa use cases trước khi gắn mới
                cameraProvider.unbindAll();

                // Gắn use cases vào camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                Log.d(TAG, "Camera được khởi tạo thành công");

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Lỗi khi khởi động camera: ", e);
                Toast.makeText(ScanPersonActivity.this,
                        "Không thể khởi động camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Không thể tìm thấy camera phù hợp: ", e);
                Toast.makeText(ScanPersonActivity.this,
                        "Không tìm thấy camera phù hợp trên thiết bị",
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo tên file dựa trên timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String fileName = "FACE_" + timestamp + ".jpg";

        // Thiết lập output options
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        // Chụp ảnh
        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                savedImageUri = outputFileResults.getSavedUri();

                runOnUiThread(() -> {
                    Toast.makeText(ScanPersonActivity.this, "Đã chụp ảnh thành công", Toast.LENGTH_SHORT).show();
                    btnConfirmFace.setEnabled(true);
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    Toast.makeText(ScanPersonActivity.this, "Lỗi khi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
                Log.e(TAG, "Lỗi khi chụp ảnh: ", exception);
            }
        });
    }

    private void sendImageToApi(Uri imageUri) {
        try {
            // Lấy file từ URI
            File imageFile = new File(getRealPathFromURI(imageUri));
            if (!imageFile.exists()) {
                Toast.makeText(this, "Không tìm thấy ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            // Tạo API service và request
            ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

            // Tạo request body với file ảnh
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "image",  // Tên tham số trong API
                    imageFile.getName(),
                    requestBody
            );

            // Gọi API
            Call<ResultFaceRecognition> call = apiService.identifyFace(filePart);

            // Hiển thị thông báo loading
            Toast.makeText(this, "Đang xử lý...", Toast.LENGTH_SHORT).show();

            // Thực hiện gọi API bất đồng bộ
            call.enqueue(new Callback<ResultFaceRecognition>() {
                @Override
                public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ResultFaceRecognition result = response.body();
                        Log.d(TAG, "Kết quả nhận diện: " + result.toString());

                        // Xử lý kết quả trả về từ API ở đây
                        handleFaceRecognitionResult(result);
                    } else {
                        Toast.makeText(ScanPersonActivity.this,
                                "Lỗi từ server: " + response.code(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                    Toast.makeText(ScanPersonActivity.this,
                            "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi khi gọi API: ", t);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi khi gửi ảnh lên API: ", e);
        }
    }

    private void handleFaceRecognitionResult(ResultFaceRecognition result) {
        // Xử lý kết quả nhận diện khuôn mặt
        Toast.makeText(this, "Nhận diện thành công!", Toast.LENGTH_SHORT).show();
        // Có thể thêm logic chuyển màn hình hoặc hiển thị thông tin người dùng

        Bundle bundle = new Bundle();
        bundle.putString("id", result.getId());
        bundle.putString("fullName", result.getFullName());
        bundle.putLong("birthDate", result.getBirthDate());
        bundle.putString("gender", result.getGender());
        bundle.putString("address", result.getAddress());
        bundle.putString("phoneNumber", result.getPhoneNumber());




        Intent intent = new Intent(ScanPersonActivity.this, PersonInfoActivity.class);

        intent.putExtra("result", bundle);

        startActivity(intent);

    }

    // Phương thức lấy đường dẫn thực từ URI
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        android.database.Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return contentUri.getPath();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();

        return path;
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
                Toast.makeText(this, "Cần cấp quyền truy cập camera để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
