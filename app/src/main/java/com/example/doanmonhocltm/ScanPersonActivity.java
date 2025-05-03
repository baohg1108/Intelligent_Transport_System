package com.example.doanmonhocltm;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageButton;
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
import com.example.doanmonhocltm.model.Person;
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
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int TIMEOUT_SECONDS = 60;
    private static final int MAX_IMAGE_SIZE = 500 * 1024;
    private static final int JPEG_QUALITY = 80;

    private int type;
    private String licensePlate;
    private CameraSelector cameraSelector;
    private int currentCameraSelector = CameraSelector.LENS_FACING_FRONT; // Default to front camera
    private PreviewView previewView;
    private ImageButton btnSwitchCamera;
    private MaterialButton btnCaptureFace;
    private FloatingActionButton btnBack;
    private ImageCapture imageCapture;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Uri savedImageUri;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_person);

        // Initialize the camera selector early
        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCameraSelector)
                .build();

        // Get intent data
        extractIntentData();

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize views and setup UI
        initializeViews();
        setupWindowInsets();
        setupButtons();

        // Request permissions if needed
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        Bundle ticketData = intent.getBundleExtra("ticketData");
        if (ticketData != null) {
            type = ticketData.getInt("type");
            licensePlate = ticketData.getString("licensePlate");
        }
    }

    private void initializeViews() {
        previewView = findViewById(R.id.previewView);
        btnCaptureFace = findViewById(R.id.btnCaptureFace);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
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
        btnBack.setOnClickListener(v -> finish());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
    }

    private void switchCamera() {
        currentCameraSelector = (currentCameraSelector == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCameraSelector)
                .build();

        startCamera();
    }

    private void navigateToLogin() {
        sessionManager.clearSession();
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

                // Setup preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Setup image capture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Unbind previous use cases before binding new ones
                cameraProvider.unbindAll();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                Log.d(TAG, "Camera initialized successfully");

            } catch (ExecutionException | InterruptedException e) {
                handleCameraError("Error starting camera: " + e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                handleCameraError("No suitable camera found on device", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void handleCameraError(String message, Exception e) {
        Log.e(TAG, message, e);
        Toast.makeText(ScanPersonActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void captureImage() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create filename based on timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String fileName = "FACE_" + timestamp + ".jpg";

        // Setup output options
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
        ).build();

        // Show capturing notification
        Toast.makeText(ScanPersonActivity.this, "Capturing image...", Toast.LENGTH_SHORT).show();

        // Take picture
        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                savedImageUri = outputFileResults.getSavedUri();

                runOnUiThread(() -> {
                    Toast.makeText(ScanPersonActivity.this, "Image captured successfully", Toast.LENGTH_SHORT).show();
                    // Automatically process the image after capture
                    if (savedImageUri != null) {
                        processAndSendImage(savedImageUri);
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> {
                    Toast.makeText(ScanPersonActivity.this, "Error capturing image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                });
                Log.e(TAG, "Error capturing image: ", exception);
            }
        });
    }

    private void processAndSendImage(Uri imageUri) {
        // Check login status before sending
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "You are not logged in or your session has expired", Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        // Send image to API
        sendImageToApi(imageUri);
    }

    private void sendImageToApi(Uri imageUri) {
        try {
            // Compress image to reduce size
            File compressedImageFile = compressImage(imageUri);

            if (compressedImageFile == null || !compressedImageFile.exists()) {
                Toast.makeText(this, "Could not process image", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Compressed image size: " + compressedImageFile.length() + " bytes");

            // Create OkHttpClient with increased timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();

            // Get API service from ApiClient
            ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

            // Create request body with compressed image file
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), compressedImageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "image",  // API parameter name
                    compressedImageFile.getName(),
                    requestBody
            );

            // Show loading notification
            Toast.makeText(this, "Processing facial recognition...", Toast.LENGTH_LONG).show();

            // Call API
            Call<Person> call = apiService.identifyFace(filePart);

            // Execute API call asynchronously
            call.enqueue(new Callback<Person>() {
                @Override
                public void onResponse(Call<Person> call, Response<Person> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Person result = response.body();
                        Log.d(TAG, "Recognition result: " + result.toString());

                        // Process result from API
                        handleFaceRecognitionResult(result);
                    } else {
                        handleApiError(response);
                    }
                }

                @Override
                public void onFailure(Call<Person> call, Throwable t) {
                    handleApiFailure(t);
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Processing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error sending image to API: ", e);
        }
    }

    private void handleApiError(Response<Person> response) {
        String errorMessage = "Server error: " + response.code();
        try {
            if (response.errorBody() != null) {
                errorMessage += " - " + response.errorBody().string();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not read error response: ", e);
        }

        // Check if error is related to authentication
        if (response.code() == 401 || response.code() == 403) {
            Toast.makeText(ScanPersonActivity.this,
                    "Session expired, please log in again",
                    Toast.LENGTH_LONG).show();
            navigateToLogin();
            return;
        }

        Toast.makeText(ScanPersonActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, errorMessage);
    }

    private void handleApiFailure(Throwable t) {
        String errorMessage = "Connection error: " + t.getMessage();
        Toast.makeText(ScanPersonActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error calling API: ", t);

        // Check for specific errors
        if (t.getMessage() != null && t.getMessage().contains("timeout")) {
            Toast.makeText(ScanPersonActivity.this,
                    "Connection to server timed out. Please check your network connection and try again.",
                    Toast.LENGTH_LONG).show();
        }

        if (t.getMessage() != null &&
                (t.getMessage().contains("jwt") || t.getMessage().contains("token") ||
                        t.getMessage().contains("Unauthorized"))) {
            Toast.makeText(ScanPersonActivity.this,
                    "Authentication error. Please log in again.",
                    Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }

    private File compressImage(Uri imageUri) {
        try {
            // Read bitmap from URI
            Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            // Create temporary file to save compressed image
            File outputDir = getCacheDir();
            File outputFile = File.createTempFile("compressed_", ".jpg", outputDir);

            // Compress image with decreasing quality until desired size is reached
            int quality = JPEG_QUALITY;
            boolean fileSizeOk = false;

            while (!fileSizeOk && quality > 10) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(baos.toByteArray());
                }

                // Check file size
                if (outputFile.length() <= MAX_IMAGE_SIZE) {
                    fileSizeOk = true;
                } else {
                    // Decrease quality if file is still too large
                    quality -= 10;
                }
            }

            // If still too large, try reducing resolution
            if (!fileSizeOk) {
                // Calculate scale ratio
                float scale = 0.8f; // Reduce by 20%

                // Scale image
                int width = Math.round(originalBitmap.getWidth() * scale);
                int height = Math.round(originalBitmap.getHeight() * scale);

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

                // Compress again with initial quality
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);

                // Write to file
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(baos.toByteArray());
                }
            }

            Log.d(TAG, "Image compressed: " + outputFile.length() + " bytes");
            return outputFile;

        } catch (IOException e) {
            Log.e(TAG, "Error compressing image: ", e);
            return null;
        }
    }

    private void handleFaceRecognitionResult(Person result) {
        // Process face recognition result
        Toast.makeText(this, "Recognition successful: " + result.getFullName(), Toast.LENGTH_SHORT).show();

        // Check received data
        if (result.getId() == null || result.getId().isEmpty()) {
            Toast.makeText(this, "User ID not received", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type == 3) {
            // Person info flow
            navigateToPersonInfo(result);
        } else {
            // Ticket creation flow
            navigateToCreateTicket(result);
        }
    }

    private void navigateToPersonInfo(Person result) {
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
    }

    private void navigateToCreateTicket(Person result) {
        // Create Bundle with required information
        Bundle ticketData = new Bundle();
        ticketData.putInt("type", type);
        ticketData.putString("licensePlate", licensePlate);
        ticketData.putString("driverCCCD", result.getId());
        ticketData.putString("driverName", result.getFullName());

        // Navigate to ticket creation screen
        Intent intent = new Intent(ScanPersonActivity.this, CreateTicketActivity.class);
        intent.putExtra("ticketData", ticketData);
        startActivity(intent);
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
                Toast.makeText(this, "Camera permission required for this feature", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}