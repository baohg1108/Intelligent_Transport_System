package com.example.doanmonhocltm;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.doanmonhocltm.callapi.SessionManager;
import com.example.doanmonhocltm.model.ResultFaceRecognition;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanPersonActivity extends AppCompatActivity {
    private static final String TAG = "ScanPersonActivity";

    private int type;
    private String licensePlate;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView previewView;
    private MaterialButton btnCaptureFace;
    private MaterialButton btnConfirmFace;
    private FloatingActionButton btnBack;

    private ImageCapture imageCapture;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Uri savedImageUri;

    // Thời gian chờ API - tăng lên để tránh timeout
    private static final int TIMEOUT_SECONDS = 60;

    // Kích thước tối đa cho ảnh (500KB)
    private static final int MAX_IMAGE_SIZE = 500 * 1024;

    // Quality for JPEG compression (0-100)
    private static final int JPEG_QUALITY = 80;

    // SessionManager để xử lý JWT
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_person);

        Intent intent = getIntent();
        Bundle ticketData = intent.getBundleExtra("ticketData");
        if (ticketData != null) {
            type = ticketData.getInt("type");
            licensePlate = ticketData.getString("licensePlate");

            Toast.makeText(ScanPersonActivity.this, "type: " + type + " - LicensePale " + licensePlate, Toast.LENGTH_SHORT).show();

        }


        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

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

        // Vô hiệu hóa nút xác nhận ban đầu
        btnConfirmFace.setEnabled(false);
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
                // Vô hiệu hóa nút trong khi đang xử lý
                btnConfirmFace.setEnabled(false);

                // Kiểm tra token trước khi gửi
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn", Toast.LENGTH_LONG).show();
                    // Chuyển về màn hình đăng nhập
                    navigateToLogin();
                    return;
                }

                sendImageToApi(savedImageUri);
            } else {
                Toast.makeText(this, "Vui lòng chụp ảnh trước", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void navigateToLogin() {
        // Xóa session hiện tại
        sessionManager.clearSession();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(ScanPersonActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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

        // Hiển thị thông báo đang chụp
        Toast.makeText(ScanPersonActivity.this, "Đang chụp ảnh...", Toast.LENGTH_SHORT).show();

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
            // Lấy file từ URI và nén ảnh để giảm kích thước
            File compressedImageFile = compressImage(imageUri);

            if (compressedImageFile == null || !compressedImageFile.exists()) {
                Toast.makeText(this, "Không thể xử lý ảnh", Toast.LENGTH_SHORT).show();
                btnConfirmFace.setEnabled(true);
                return;
            }

            Log.d(TAG, "Kích thước ảnh sau khi nén: " + compressedImageFile.length() + " bytes");

            // Tạo OkHttpClient với timeout tăng lên
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();

            // Lấy API service từ ApiClient
            ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

            // Tạo request body với file ảnh đã nén
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), compressedImageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "image",  // Tên tham số trong API
                    compressedImageFile.getName(),
                    requestBody
            );

            // Hiển thị thông báo loading
            Toast.makeText(this, "Đang xử lý nhận diện khuôn mặt...", Toast.LENGTH_LONG).show();

            // Gọi API
            Call<ResultFaceRecognition> call = apiService.identifyFace(filePart);

            // Thực hiện gọi API bất đồng bộ
            call.enqueue(new Callback<ResultFaceRecognition>() {
                @Override
                public void onResponse(Call<ResultFaceRecognition> call, Response<ResultFaceRecognition> response) {
                    // Kích hoạt lại nút xác nhận
                    btnConfirmFace.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        ResultFaceRecognition result = response.body();
                        Log.d(TAG, "Kết quả nhận diện: " + result.toString());

                        // Xử lý kết quả trả về từ API ở đây
                        handleFaceRecognitionResult(result);
                    } else {
                        String errorMessage = "Lỗi từ server: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                errorMessage += " - " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Không thể đọc lỗi response: ", e);
                        }

                        // Kiểm tra nếu lỗi liên quan đến xác thực
                        if (response.code() == 401 || response.code() == 403) {
                            Toast.makeText(ScanPersonActivity.this,
                                    "Phiên đăng nhập hết hạn, vui lòng đăng nhập lại",
                                    Toast.LENGTH_LONG).show();
                            navigateToLogin();
                            return;
                        }

                        Toast.makeText(ScanPersonActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        Log.e(TAG, errorMessage);
                    }
                }

                @Override
                public void onFailure(Call<ResultFaceRecognition> call, Throwable t) {
                    // Kích hoạt lại nút xác nhận
                    btnConfirmFace.setEnabled(true);

                    String errorMessage = "Lỗi kết nối: " + t.getMessage();
                    Toast.makeText(ScanPersonActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi khi gọi API: ", t);

                    // Kiểm tra các lỗi cụ thể
                    if (t.getMessage() != null && t.getMessage().contains("timeout")) {
                        Toast.makeText(ScanPersonActivity.this,
                                "Kết nối tới máy chủ bị timeout. Vui lòng kiểm tra kết nối mạng và thử lại sau.",
                                Toast.LENGTH_LONG).show();
                    }

                    if (t.getMessage() != null &&
                            (t.getMessage().contains("jwt") || t.getMessage().contains("token") ||
                                    t.getMessage().contains("Unauthorized"))) {
                        Toast.makeText(ScanPersonActivity.this,
                                "Lỗi xác thực. Vui lòng đăng nhập lại.",
                                Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    }
                }
            });
        } catch (Exception e) {
            // Kích hoạt lại nút xác nhận
            btnConfirmFace.setEnabled(true);

            Toast.makeText(this, "Lỗi xử lý: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi khi gửi ảnh lên API: ", e);
        }
    }

    // Phương thức nén ảnh để giảm kích thước
    private File compressImage(Uri imageUri) {
        try {
            // Đọc bitmap từ URI
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Tạo file tạm thời để lưu ảnh đã nén
            File outputDir = getCacheDir();
            File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);

            // Nén ảnh với chất lượng giảm dần cho đến khi đạt kích thước mong muốn
            int quality = JPEG_QUALITY;
            boolean fileSizeOk = false;

            while (!fileSizeOk && quality > 10) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                // Ghi ra file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(baos.toByteArray());
                }

                // Kiểm tra kích thước file
                if (outputFile.length() <= MAX_IMAGE_SIZE) {
                    fileSizeOk = true;
                } else {
                    // Giảm chất lượng nếu file vẫn còn lớn
                    quality -= 10;
                }
            }

            // Nếu vẫn còn lớn, thử giảm độ phân giải
            if (!fileSizeOk) {
                // Tính toán tỷ lệ scale
                float scale = 0.8f; // Giảm 20% kích thước

                // Scale ảnh
                int width = Math.round(originalBitmap.getWidth() * scale);
                int height = Math.round(originalBitmap.getHeight() * scale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

                // Nén lại với chất lượng ban đầu
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);

                // Ghi ra file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(baos.toByteArray());
                }
            }

            Log.d(TAG, "Ảnh đã được nén: " + outputFile.length() + " bytes");
            return outputFile;

        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi nén ảnh: ", e);
            return null;
        }
    }

    private void handleFaceRecognitionResult(ResultFaceRecognition result) {
        // Xử lý kết quả nhận diện khuôn mặt
        Toast.makeText(this, "Nhận diện thành công: " + result.getFullName(), Toast.LENGTH_SHORT).show();

        // Kiểm tra dữ liệu nhận được
        if (result.getId() == null || result.getId().isEmpty()) {
            Toast.makeText(this, "Không nhận được ID người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type == 3) {
            Bundle bundle = new Bundle();
            bundle.putString("id", result.getId());
            bundle.putString("fullName", result.getFullName());
            bundle.putLong("birthDate", result.getBirthDate());
            bundle.putString("gender", result.getGender());
            bundle.putString("address", result.getAddress());
            bundle.putString("phoneNumber", result.getPhoneNumber());
            bundle.putString("facePath", result.getFacePath());
            Intent intent = new Intent(ScanPersonActivity.this, PersonInfoActivity.class);
            intent.putExtra("result", bundle);
            startActivity(intent);
        } else {
            // Tạo Bundle chứa thông tin cần thiết
            Bundle ticketData = new Bundle();
            ticketData.putInt("type", type);
            ticketData.putString("licensePlate", licensePlate);
            ticketData.putString("driverCCCD", result.getId());
            ticketData.putString("driverName", result.getFullName());

            // Chuyển sang màn hình tạo biên bản
            Intent intent = new Intent(ScanPersonActivity.this, CreateTicketActivity.class);
            intent.putExtra("ticketData", ticketData);
            startActivity(intent);

        }


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