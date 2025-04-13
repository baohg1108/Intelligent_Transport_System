package com.example.doanmonhocltm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Scan_Face extends AppCompatActivity {
    private static final String TAG = "FaceDetection";
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private static final String FACE_DATA_FILE = "face_data.txt"; // Tên file để lưu trữ dữ liệu khuôn mặt

    private PreviewView previewView;
    private TextView faceDataTextView;
    private ExecutorService cameraExecutor;
    private FaceDetector faceDetector;
    private Set<String> existingFaceData; // Set để lưu trữ dữ liệu khuôn mặt đã có

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_scan_face);

            previewView = findViewById(R.id.previewView);
            faceDataTextView = findViewById(R.id.faceDataTextView);

            if (previewView == null || faceDataTextView == null) {
                Log.e(TAG, "Không tìm thấy view trong layout");
                Toast.makeText(this, "Lỗi khởi tạo giao diện", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Khởi tạo Face Detector với các tùy chọn
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .enableTracking()
                    .build();

            faceDetector = FaceDetection.getClient(options);
            cameraExecutor = Executors.newSingleThreadExecutor();
            existingFaceData = loadExistingFaceData(); // Load dữ liệu khuôn mặt đã có từ file

            // Kiểm tra quyền truy cập camera
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                requestCameraPermission();
            }

        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onCreate: ", e);
            Toast.makeText(this, "Lỗi khởi tạo ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void requestCameraPermission() {
        try {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi yêu cầu quyền camera: ", e);
            Toast.makeText(this, "Không thể yêu cầu quyền camera", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == PERMISSION_REQUEST_CAMERA) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Yêu cầu quyền Camera bị từ chối. Ứng dụng cần quyền này để hoạt động.",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onRequestPermissionsResult: ", e);
            finish();
        }
    }

    private void startCamera() {
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                    ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindCameraUseCases(cameraProvider);
                } catch (Exception e) {
                    Log.e(TAG, "Không thể khởi động camera: ", e);
                    runOnUiThread(() -> {
                        Toast.makeText(Scan_Face.this, "Lỗi khởi tạo camera: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }
            }, ContextCompat.getMainExecutor(this));
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi khởi tạo camera: ", e);
            Toast.makeText(this, "Lỗi khởi tạo camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        try {
            // Thiết lập preview cho camera
            Preview preview = new Preview.Builder().build();

            // Kiểm tra camera trước có khả dụng không
            CameraSelector frontCameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build();

            // Chuẩn bị camera sau để dự phòng
            CameraSelector backCameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();

            // Kiểm tra camera trước có khả dụng không
            CameraSelector cameraSelector;
            try {
                boolean hasFrontCamera = cameraProvider.hasCamera(frontCameraSelector);
                if (hasFrontCamera) {
                    cameraSelector = frontCameraSelector;
                    Log.d(TAG, "Sử dụng camera trước");
                } else {
                    cameraSelector = backCameraSelector;
                    Log.d(TAG, "Camera trước không khả dụng, chuyển sang camera sau");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi kiểm tra camera trước, sẽ thử camera sau: ", e);

                // Thử camera sau nếu có lỗi khi kiểm tra camera trước
                try {
                    if (cameraProvider.hasCamera(backCameraSelector)) {
                        cameraSelector = backCameraSelector;
                        Log.d(TAG, "Sử dụng camera sau");
                    } else {
                        // Nếu không có camera nào khả dụng, sử dụng selector mặc định
                        cameraSelector = new CameraSelector.Builder().build();
                        Log.d(TAG, "Không tìm thấy camera cụ thể, sử dụng camera mặc định");
                    }
                } catch (Exception ex) {
                    // Nếu vẫn lỗi, sử dụng selector mặc định
                    cameraSelector = new CameraSelector.Builder().build();
                    Log.d(TAG, "Không thể kiểm tra camera, sử dụng camera mặc định");
                }
            }

            // Đảm bảo PreviewView đã được khởi tạo
            if (previewView != null) {
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            }

            // Thiết lập image analysis
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .setTargetResolution(new Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build();

            imageAnalysis.setAnalyzer(cameraExecutor, new FaceAnalyzer());

            try {
                // Unbind use cases trước khi rebind
                cameraProvider.unbindAll();

                // Bind use cases với camera
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi bind camera: ", e);
                runOnUiThread(() -> {
                    Toast.makeText(Scan_Face.this, "Lỗi khi khởi tạo camera: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi cấu hình camera: ", e);
            runOnUiThread(() -> {
                Toast.makeText(Scan_Face.this, "Lỗi khi cấu hình camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private class FaceAnalyzer implements ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError")
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            try {
                if (imageProxy.getImage() == null) {
                    imageProxy.close();
                    return;
                }

                InputImage image = InputImage.fromMediaImage(
                        imageProxy.getImage(),
                        imageProxy.getImageInfo().getRotationDegrees());

                faceDetector.process(image)
                        .addOnSuccessListener(faces -> {
                            try {
                                processFaceResults(faces);
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi khi xử lý kết quả: ", e);
                                updateUIWithError("Lỗi khi xử lý kết quả: " + e.getMessage());
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Phát hiện khuôn mặt thất bại: ", e);
                            updateUIWithError("Phát hiện khuôn mặt thất bại: " + e.getMessage());
                        })
                        .addOnCompleteListener(task -> {
                            imageProxy.close();
                        });
            } catch (Exception e) {
                Log.e(TAG, "Lỗi trong quá trình phân tích: ", e);
                updateUIWithError("Lỗi trong quá trình phân tích: " + e.getMessage());
                imageProxy.close();
            }
        }
    }

    private void updateUIWithError(String errorMessage) {
        runOnUiThread(() -> {
            try {
                if (faceDataTextView != null) {
                    faceDataTextView.setText("Lỗi: " + errorMessage);
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi cập nhật UI với thông báo lỗi: ", e);
            }
        });
    }

    private void processFaceResults(List<Face> faces) {
        try {
            final StringBuilder faceData = new StringBuilder();

            // Kiểm tra xem có khuôn mặt được phát hiện không
            if (faces == null || faces.isEmpty()) {
                faceData.append("Không phát hiện được khuôn mặt");
            } else {
                // Lấy khuôn mặt đầu tiên
                Face face = faces.get(0);

                // Thông tin cơ bản
                Rect bounds = face.getBoundingBox();
                faceData.append("Thông tin khuôn mặt:\n\n");

                Integer trackingId = face.getTrackingId();
                if (trackingId != null) {
                    faceData.append("ID Theo dõi: ").append(trackingId).append("\n");
                }

                faceData.append("Tọa độ (L,T,R,B): ").append(bounds.left).append(", ")
                        .append(bounds.top).append(", ")
                        .append(bounds.right).append(", ")
                        .append(bounds.bottom).append("\n\n");



                // Thông tin về góc quay
                faceData.append("Góc xoay: ").append("\n");
                faceData.append("- X: ").append(face.getHeadEulerAngleX()).append("°\n");
                faceData.append("- Y: ").append(face.getHeadEulerAngleY()).append("°\n");
                faceData.append("- Z: ").append(face.getHeadEulerAngleZ()).append("°\n\n");

                // Xác suất mở mắt và cười
                Float rightEyeOpenProb = face.getRightEyeOpenProbability();
                if (rightEyeOpenProb != null) {
                    faceData.append("Mắt phải mở: ")
                            .append(Math.round(rightEyeOpenProb * 100))
                            .append("%\n");
                }

                Float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                if (leftEyeOpenProb != null) {
                    faceData.append("Mắt trái mở: ")
                            .append(Math.round(leftEyeOpenProb * 100))
                            .append("%\n");
                }

                Float smilingProb = face.getSmilingProbability();
                if (smilingProb != null) {
                    faceData.append("Xác suất cười: ")
                            .append(Math.round(smilingProb * 100))
                            .append("%\n\n");
                }



                // Các điểm đặc trưng trên khuôn mặt
                faceData.append("Điểm đặc trưng:\n");
                boolean hasLandmarks = false;

                FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                if (leftEye != null) {
                    hasLandmarks = true;
                    faceData.append("- Mắt trái tại: (")
                            .append(Math.round(leftEye.getPosition().x))
                            .append(", ")
                            .append(Math.round(leftEye.getPosition().y))
                            .append(")\n");
                }

                FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                if (rightEye != null) {
                    hasLandmarks = true;
                    faceData.append("- Mắt phải tại: (")
                            .append(Math.round(rightEye.getPosition().x))
                            .append(", ")
                            .append(Math.round(rightEye.getPosition().y))
                            .append(")\n");
                }

                FaceLandmark nose = face.getLandmark(FaceLandmark.NOSE_BASE);
                if (nose != null) {
                    hasLandmarks = true;
                    faceData.append("- Mũi tại: (")
                            .append(Math.round(nose.getPosition().x))
                            .append(", ")
                            .append(Math.round(nose.getPosition().y))
                            .append(")\n");
                }

                FaceLandmark mouthLeft = face.getLandmark(FaceLandmark.MOUTH_LEFT);
                if (mouthLeft != null) {
                    hasLandmarks = true;
                    faceData.append("- Miệng trái tại: (")
                            .append(Math.round(mouthLeft.getPosition().x))
                            .append(", ")
                            .append(Math.round(mouthLeft.getPosition().y))
                            .append(")\n");
                }

                FaceLandmark mouthRight = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
                if (mouthRight != null) {
                    hasLandmarks = true;
                    faceData.append("- Miệng phải tại: (")
                            .append(Math.round(mouthRight.getPosition().x))
                            .append(", ")
                            .append(Math.round(mouthRight.getPosition().y))
                            .append(")\n");
                }

                if (!hasLandmarks) {
                    faceData.append("- Không phát hiện được điểm đặc trưng\n");
                }
            }

            // Log dữ liệu khuôn mặt
            Log.e(TAG, "Dữ liệu khuôn mặt: " + faceData.toString());

            // Hiển thị dữ liệu khuôn mặt lên TextView
            final String faceDataText = faceData.toString();
            runOnUiThread(() -> {
                try {
                    if (faceDataTextView != null) {
                        faceDataTextView.setText(faceDataText);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi khi cập nhật dữ liệu UI: ", e);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi xử lý dữ liệu khuôn mặt: ", e);
            updateUIWithError("Lỗi khi xử lý dữ liệu khuôn mặt: " + e.getMessage());
        }
    }

    private void saveFaceData(String faceData) {
        try {
            File file = new File(getFilesDir(), FACE_DATA_FILE);
            FileWriter fw = new FileWriter(file, true); // true: append
            PrintWriter pw = new PrintWriter(fw);
            pw.println(faceData);
            pw.close();
            fw.close();
            Log.d(TAG, "Đã lưu dữ liệu khuôn mặt vào file");
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi lưu dữ liệu khuôn mặt vào file: ", e);
            showToast("Lỗi khi lưu dữ liệu khuôn mặt");
        }
    }

    private Set<String> loadExistingFaceData() {
        Set<String> faceData = new HashSet<>();
        try {
            File file = new File(getFilesDir(), FACE_DATA_FILE);
            if (file.exists()) {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    faceData.add(line);
                }
                br.close();
                fr.close();
                Log.d(TAG, "Đã tải dữ liệu khuôn mặt hiện có từ file");
            }
        } catch (IOException e) {
            Log.e(TAG, "Lỗi khi tải dữ liệu khuôn mặt từ file: ", e);
            showToast("Lỗi khi tải dữ liệu khuôn mặt");
        }
        return faceData;
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(Scan_Face.this, message, Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        try {
            if (cameraExecutor != null) {
                cameraExecutor.shutdown();
            }
            super.onDestroy();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi trong onDestroy: ", e);
            super.onDestroy();
        }
    }
}
